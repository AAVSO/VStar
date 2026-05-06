#!/usr/bin/env python3
"""
Compute GUI / non-GUI / total coverage figures from a JaCoCo XML report.

The "GUI" classification is per source file: a file counts as GUI if it
contains a top-level ``import`` of ``javax.swing.*`` or ``java.awt.*``
(at any sub-package depth). The headline coverage figure on the project
dashboard should reflect *non-GUI* code so that the well-known lack of
unit tests for Swing-coupled code does not artificially deflate it; the
GUI figure is reported alongside, not hidden, so the deficit stays
visible.

The script reads JaCoCo's ``coverage.xml``, walks every ``<class>``
element, attributes its counters to GUI or non-GUI based on the source
file's imports, and prints:

  * a human-readable table on stdout, and
  * (optionally, with ``--github-output``) ``key=value`` pairs suitable
    for ``$GITHUB_OUTPUT`` so GitHub Actions can publish both figures.

It is invoked both from ``ant coverage-report`` (so local builds see the
same split) and from ``.github/workflows/vstar-UT.yml``.

Issue: https://github.com/AAVSO/VStar/issues/579
"""

from __future__ import annotations

import argparse
import os
import re
import sys
import xml.etree.ElementTree as ET
from dataclasses import dataclass, field
from pathlib import Path
from typing import Iterable


# Match a top-level Java import of javax.swing.* or java.awt.* (any
# sub-package). The leading anchor avoids matching the same text inside
# block comments or strings; we still tolerate leading whitespace in
# case the source uses indented imports (which is unusual but legal).
_GUI_IMPORT_RE = re.compile(
    r"^\s*import\s+(?:static\s+)?(?:javax\.swing|java\.awt)[.;]",
    re.MULTILINE,
)


def is_gui_source(path: Path) -> bool:
    """Return True if the Java source file imports javax.swing or java.awt."""
    try:
        text = path.read_text(encoding="utf-8", errors="replace")
    except OSError:
        return False
    return bool(_GUI_IMPORT_RE.search(text))


def collect_gui_sources(src_root: Path) -> set[str]:
    """Return the set of relative source paths (e.g. 'org/aavso/.../Foo.java')
    that are classified as GUI."""
    gui: set[str] = set()
    for java in src_root.rglob("*.java"):
        if is_gui_source(java):
            rel = java.relative_to(src_root).as_posix()
            gui.add(rel)
    return gui


@dataclass
class Counters:
    """JaCoCo counter pair (missed, covered) for a single counter type."""

    missed: int = 0
    covered: int = 0

    def add(self, other: "Counters") -> None:
        self.missed += other.missed
        self.covered += other.covered

    @property
    def total(self) -> int:
        return self.missed + self.covered

    def percent(self) -> str:
        if self.total == 0:
            return "n/a"
        return f"{(self.covered / self.total) * 100:.1f}%"

    def percent_int(self) -> str:
        """Integer-percent string matching the existing dashboard format
        (e.g. '73%'). Returns 'N/A' when there is nothing to measure."""
        if self.total == 0:
            return "N/A"
        return f"{(self.covered * 100) // self.total}%"


@dataclass
class Totals:
    """All JaCoCo counter types for one partition (GUI / non-GUI / total)."""

    line: Counters = field(default_factory=Counters)
    branch: Counters = field(default_factory=Counters)
    method: Counters = field(default_factory=Counters)
    instr: Counters = field(default_factory=Counters)
    classes: Counters = field(default_factory=Counters)

    def add_jacoco_counter(self, ctype: str, missed: int, covered: int) -> None:
        c = Counters(missed, covered)
        if ctype == "LINE":
            self.line.add(c)
        elif ctype == "BRANCH":
            self.branch.add(c)
        elif ctype == "METHOD":
            self.method.add(c)
        elif ctype == "INSTRUCTION":
            self.instr.add(c)
        elif ctype == "CLASS":
            self.classes.add(c)


def aggregate(
    coverage_xml: Path, gui_sources: set[str]
) -> tuple[Totals, Totals, Totals]:
    """Walk a JaCoCo coverage.xml and accumulate counters into
    (gui, non_gui, total).

    We partition on ``<sourcefile>`` rather than ``<class>`` because
    JaCoCo's class-level line counters double-count physical lines that
    host multiple bytecode classes (lambdas, anonymous classes, inner
    classes) -- the de-duplicated, dashboard-style numbers live on
    ``<sourcefile>``. Method/branch/instruction counters, which are not
    on ``<sourcefile>``, fall back to per-class summation.
    """
    parser = ET.XMLParser()
    tree = ET.parse(coverage_xml, parser=parser)
    root = tree.getroot()

    gui = Totals()
    non_gui = Totals()
    total = Totals()

    for pkg in root.findall("package"):
        pkg_name = pkg.get("name", "")

        # Source-file-level counters: take the deduplicated counters
        # (LINE/BRANCH/INSTRUCTION) from <sourcefile>. This matches
        # JaCoCo's report-level totals.
        sf_counter_types = {"LINE", "BRANCH", "INSTRUCTION"}
        for sf in pkg.findall("sourcefile"):
            name = sf.get("name", "")
            rel = f"{pkg_name}/{name}" if pkg_name else name
            is_gui = rel in gui_sources
            bucket = gui if is_gui else non_gui
            for counter in sf.findall("counter"):
                ctype = counter.get("type", "")
                if ctype not in sf_counter_types:
                    continue
                missed = int(counter.get("missed", "0"))
                covered = int(counter.get("covered", "0"))
                bucket.add_jacoco_counter(ctype, missed, covered)
                total.add_jacoco_counter(ctype, missed, covered)

        # METHOD and CLASS counters are not on <sourcefile>; pick them
        # up from <class>, routed by the class's sourcefilename so they
        # share the same partition.
        cls_counter_types = {"METHOD", "CLASS"}
        for cls in pkg.findall("class"):
            srcfile = cls.get("sourcefilename")
            rel = (
                f"{pkg_name}/{srcfile}" if (pkg_name and srcfile) else srcfile
            )
            is_gui = rel is not None and rel in gui_sources
            bucket = gui if is_gui else non_gui
            for counter in cls.findall("counter"):
                ctype = counter.get("type", "")
                if ctype not in cls_counter_types:
                    continue
                missed = int(counter.get("missed", "0"))
                covered = int(counter.get("covered", "0"))
                bucket.add_jacoco_counter(ctype, missed, covered)
                total.add_jacoco_counter(ctype, missed, covered)

    return gui, non_gui, total


def format_table(gui: Totals, non_gui: Totals, total: Totals) -> str:
    """Render a fixed-width text table for stdout / Ant console output."""
    rows = (
        ("Non-GUI (headline)", non_gui),
        ("GUI", gui),
        ("Total", total),
    )

    header = (
        "VStar Coverage by Area (issue #579)",
        "===================================",
        "",
        f"  GUI rule: source file imports javax.swing or java.awt",
        "",
        f"  {'Area':<22}  {'Lines':>10}  {'Line%':>7}  "
        f"{'Branch':>10}  {'Brch%':>7}  {'Method':>10}  {'Mthd%':>7}",
        f"  {'-' * 22}  {'-' * 10}  {'-' * 7}  "
        f"{'-' * 10}  {'-' * 7}  {'-' * 10}  {'-' * 7}",
    )

    lines = list(header)
    for label, t in rows:
        lines.append(
            f"  {label:<22}  {t.line.total:>10}  {t.line.percent():>7}  "
            f"{t.branch.total:>10}  {t.branch.percent():>7}  "
            f"{t.method.total:>10}  {t.method.percent():>7}"
        )
    lines.append("")
    return "\n".join(lines)


def emit_github_output(
    path: Path, gui: Totals, non_gui: Totals, total: Totals
) -> None:
    """Append GitHub Actions ``key=value`` outputs.

    The non-GUI figures are the headline (``line_cov`` / ``branch_cov``)
    so the dashboard banner reflects code we actually test. GUI figures
    are emitted alongside so the deficit stays visible. ``all_*`` covers
    the original combined number for continuity if anyone wants it.
    """
    pairs = [
        # Non-GUI headline: integer precision keeps badge text stable.
        ("line_cov", non_gui.line.percent_int()),
        ("branch_cov", non_gui.branch.percent_int()),
        ("method_cov", non_gui.method.percent_int()),
        # GUI and combined: one-decimal so small test additions are visible.
        ("gui_line_cov", gui.line.percent()),
        ("gui_branch_cov", gui.branch.percent()),
        ("gui_method_cov", gui.method.percent()),
        ("all_line_cov", total.line.percent()),
        ("all_branch_cov", total.branch.percent()),
        ("all_method_cov", total.method.percent()),
        ("non_gui_lines_total", str(non_gui.line.total)),
        ("gui_lines_total", str(gui.line.total)),
        ("all_lines_total", str(total.line.total)),
    ]
    with path.open("a", encoding="utf-8") as fh:
        for k, v in pairs:
            fh.write(f"{k}={v}\n")


def main(argv: Iterable[str] | None = None) -> int:
    p = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    p.add_argument(
        "--src",
        type=Path,
        default=Path("src"),
        help="Java source root used to classify GUI vs non-GUI (default: src)",
    )
    p.add_argument(
        "--coverage-xml",
        type=Path,
        default=Path("test_report/coverage/coverage.xml"),
        help="Path to JaCoCo coverage.xml",
    )
    p.add_argument(
        "--github-output",
        type=Path,
        default=None,
        help="Append key=value lines for GitHub Actions to this file",
    )
    p.add_argument(
        "--out",
        type=Path,
        default=None,
        help="Also write the human table to this file (e.g. test_report/coverage/area-summary.txt)",
    )
    args = p.parse_args(list(argv) if argv is not None else None)

    if not args.coverage_xml.is_file():
        print(f"error: coverage XML not found: {args.coverage_xml}", file=sys.stderr)
        return 2
    if not args.src.is_dir():
        print(f"error: source directory not found: {args.src}", file=sys.stderr)
        return 2

    gui_sources = collect_gui_sources(args.src)
    gui, non_gui, total = aggregate(args.coverage_xml, gui_sources)

    table = format_table(gui, non_gui, total)
    print(table)
    if args.out is not None:
        args.out.parent.mkdir(parents=True, exist_ok=True)
        args.out.write_text(table + "\n", encoding="utf-8")
    if args.github_output is not None:
        emit_github_output(args.github_output, gui, non_gui, total)

    return 0


if __name__ == "__main__":
    sys.exit(main())
