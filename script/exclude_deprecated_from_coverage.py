#!/usr/bin/env python3
"""
Identify top-level Java types marked deprecated and optionally stage class
files for JaCoCo so those types are omitted from coverage reports.

JaCoCo does **not** exclude ``@Deprecated`` / javadoc ``@deprecated`` types
by itself. This script:

  1. Scans source trees for *type-level* deprecation (class / interface /
     enum). Method-only ``@Deprecated`` does **not** exclude the enclosing
     type (e.g. ``ValidObservation``, ``DescStats`` stay in the report).
  2. With ``--classes-in`` / ``--classes-out``, copies ``.class`` files into
     a staging directory, omitting deprecated types and their nested
     ``Outer$*.class`` companions.

Used by ``ant coverage-report`` (core and plugins).
"""

from __future__ import annotations

import argparse
import re
import shutil
import sys
from pathlib import Path

# Type declaration at package / member level (not methods).
_TYPE_DECL_RE = re.compile(
    r"(?:^|\n)\s*(?:public\s+|protected\s+|private\s+)?"
    r"(?:abstract\s+|final\s+|static\s+|sealed\s+|non-sealed\s+)*"
    r"(class|interface|enum|record)\s+(\w+)",
    re.MULTILINE,
)

_ANNOTATION_DEPRECATED_RE = re.compile(r"@Deprecated\b")
_JAVADOC_DEPRECATED_RE = re.compile(r"@deprecated\b")


def _preamble_is_deprecated(preamble: str) -> bool:
    """True if the text immediately before a type decl deprecates that type."""
    # Only consider the gap after the previous member (last } or ;).
    # Otherwise method-level @Deprecated in generated parsers falsely
    # marks the following nested class (e.g. VeLaParser.SequenceContext).
    cut = max(preamble.rfind("}"), preamble.rfind(";"))
    chunk = preamble[cut + 1 :] if cut >= 0 else preamble

    lines = []
    for line in chunk.splitlines():
        stripped = line.lstrip()
        if stripped.startswith("//"):
            continue
        lines.append(line)
    text = "\n".join(lines)

    # Complete javadoc block immediately above the type.
    javadocs = list(re.finditer(r"/\*\*.*?\*/", text, re.DOTALL))
    if javadocs:
        last_doc = javadocs[-1].group(0)
        after_doc = text[javadocs[-1].end() :]
        if _JAVADOC_DEPRECATED_RE.search(last_doc):
            return True
        if _ANNOTATION_DEPRECATED_RE.search(after_doc):
            return True
        return False

    # Truncated leading javadoc (preamble window missed the opening /**)
    # but still ends with @deprecated ... */
    if re.search(r"@deprecated\b.*?\*/", text, re.DOTALL | re.IGNORECASE):
        return True

    # Annotation-only: @Deprecated immediately above the type.
    return bool(_ANNOTATION_DEPRECATED_RE.search(text))


def deprecated_type_names(java_source: str) -> set[str]:
    """Return simple names of deprecated top-level / member types in a file."""
    deprecated: set[str] = set()
    for match in _TYPE_DECL_RE.finditer(java_source):
        name = match.group(2)
        preamble = java_source[max(0, match.start() - 800) : match.start()]
        # Avoid matching @Deprecated on a *method* just above an inner class:
        # if the preamble ends with a method-like signature, skip annotation-only.
        if _preamble_is_deprecated(preamble):
            deprecated.add(name)
    return deprecated


def collect_deprecated_relative_stems(src_roots: list[Path]) -> set[str]:
    """
    Return path stems relative to each src root, using '/' separators, e.g.
    ``org/aavso/tools/vstar/util/notification/NotifyingArrayList``.
    """
    stems: set[str] = set()
    for src_root in src_roots:
        if not src_root.is_dir():
            continue
        for java in src_root.rglob("*.java"):
            text = java.read_text(encoding="utf-8", errors="replace")
            names = deprecated_type_names(text)
            if not names:
                continue
            rel = java.relative_to(src_root).with_suffix("")
            # Top-level type usually matches filename; also record nested names
            # as Outer$Inner when declared in the same file.
            top = rel.name
            package_path = rel.as_posix()
            parent_path = rel.parent.as_posix()
            for name in names:
                if name == top:
                    stems.add(package_path)
                else:
                    # Nested / secondary type in the same compilation unit.
                    if parent_path == ".":
                        stems.add(f"{top}${name}")
                    else:
                        stems.add(f"{parent_path}/{top}${name}")
    return stems


def stage_classes(
    classes_in: Path,
    classes_out: Path,
    deprecated_stems: set[str],
) -> tuple[int, int]:
    """Copy class files, skipping deprecated stems. Returns (copied, skipped)."""
    if classes_out.exists():
        shutil.rmtree(classes_out)
    classes_out.mkdir(parents=True, exist_ok=True)

    copied = 0
    skipped = 0
    for class_file in classes_in.rglob("*.class"):
        rel = class_file.relative_to(classes_in)
        stem = rel.with_suffix("").as_posix()
        # Skip Outer and Outer$* when Outer is deprecated.
        exclude = False
        for dep in deprecated_stems:
            if stem == dep or stem.startswith(dep + "$"):
                exclude = True
                break
        if exclude:
            skipped += 1
            continue
        dest = classes_out / rel
        dest.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(class_file, dest)
        copied += 1
    return copied, skipped


def main(argv: list[str] | None = None) -> int:
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument(
        "--src",
        action="append",
        type=Path,
        required=True,
        help="Java source root (repeatable)",
    )
    p.add_argument(
        "--classes-in",
        type=Path,
        default=None,
        help="Compiled classes directory to filter",
    )
    p.add_argument(
        "--classes-out",
        type=Path,
        default=None,
        help="Staging directory for JaCoCo classfiles",
    )
    p.add_argument(
        "--list",
        action="store_true",
        help="Print deprecated type stems to stdout",
    )
    args = p.parse_args(argv)

    stems = collect_deprecated_relative_stems(args.src)
    if args.list or (args.classes_in is None and args.classes_out is None):
        for s in sorted(stems):
            print(s)
        if args.classes_in is None:
            print(f"# {len(stems)} deprecated type(s)", file=sys.stderr)

    if args.classes_in is not None or args.classes_out is not None:
        if args.classes_in is None or args.classes_out is None:
            print("error: both --classes-in and --classes-out are required", file=sys.stderr)
            return 2
        if not args.classes_in.is_dir():
            print(f"error: classes dir not found: {args.classes_in}", file=sys.stderr)
            return 2
        copied, skipped = stage_classes(args.classes_in, args.classes_out, stems)
        print(
            f"Staged {copied} class file(s); skipped {skipped} deprecated "
            f"({len(stems)} deprecated type stem(s))",
            file=sys.stderr,
        )
    return 0


if __name__ == "__main__":
    sys.exit(main())
