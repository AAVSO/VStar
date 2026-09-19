#!/usr/bin/env python3
"""
Count property-based unit tests (PBTs) under VStar test trees.

A JUnit test method counts as a PBT if either:

  1. it lives in a class whose source file name ends with ``PBTTest.java``, or
  2. its method body contains ``qt().forAll`` (QuickTheories), covering PBTs
     embedded in mixed suites such as ``PhaseCalcsTest`` / ``VeLaTest``.

Each such method still contributes 1 toward the overall ``Tests run`` total;
this script reports the PBT *subset* of that total, not QuickTheories sample
iterations.

Used by CI:

  - ``.github/workflows/vstar-UT.yml`` — default root ``test/org`` → core
    ``coverage.json`` field ``pbts``
  - ``.github/workflows/plugin-UT.yml`` — ``--test-root plugin/test`` →
    ``plugin-coverage.json`` field ``pbts``
"""

from __future__ import annotations

import argparse
import re
import sys
from pathlib import Path

# JUnit 3-style test methods: public void testFoo(...)
_TEST_METHOD_RE = re.compile(
    r"public\s+void\s+(test\w*)\s*\([^)]*\)\s*(?:throws\s+[^{]+)?\{",
    re.MULTILINE,
)

_QT_FOR_ALL_RE = re.compile(r"qt\s*\(\s*\)\s*\.\s*forAll")


def _method_bodies(source: str) -> list[tuple[str, str]]:
    """Return (method_name, body) for each public void test* method."""
    methods: list[tuple[str, str]] = []
    for match in _TEST_METHOD_RE.finditer(source):
        name = match.group(1)
        start = match.end()  # position just after opening '{'
        depth = 1
        i = start
        while i < len(source) and depth > 0:
            ch = source[i]
            if ch == "{":
                depth += 1
            elif ch == "}":
                depth -= 1
            i += 1
        body = source[start : i - 1]
        methods.append((name, body))
    return methods


def count_pbts_in_file(path: Path) -> int:
    text = path.read_text(encoding="utf-8", errors="replace")
    is_pbt_class = path.name.endswith("PBTTest.java")
    count = 0
    for _name, body in _method_bodies(text):
        if is_pbt_class or _QT_FOR_ALL_RE.search(body):
            count += 1
    return count


def count_pbts(test_root: Path) -> int:
    total = 0
    for path in sorted(test_root.rglob("*Test.java")):
        total += count_pbts_in_file(path)
    return total


def main(argv: list[str] | None = None) -> int:
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument(
        "--test-root",
        type=Path,
        action="append",
        dest="test_roots",
        metavar="DIR",
        help="Root of Java test sources (repeatable; default: test/org)",
    )
    p.add_argument(
        "--github-output",
        type=Path,
        default=None,
        help="Append pbts=<n> for GitHub Actions to this file",
    )
    p.add_argument(
        "--verbose",
        action="store_true",
        help="List per-file counts on stderr",
    )
    args = p.parse_args(argv)

    roots = args.test_roots if args.test_roots else [Path("test/org")]
    total = 0
    for root in roots:
        if not root.is_dir():
            print(f"error: test root not found: {root}", file=sys.stderr)
            return 2
        for path in sorted(root.rglob("*Test.java")):
            n = count_pbts_in_file(path)
            if n and args.verbose:
                print(f"{n:4d}  {path}", file=sys.stderr)
            total += n

    print(total)
    if args.github_output is not None:
        with args.github_output.open("a", encoding="utf-8") as f:
            f.write(f"pbts={total}\n")
    return 0


if __name__ == "__main__":
    sys.exit(main())
