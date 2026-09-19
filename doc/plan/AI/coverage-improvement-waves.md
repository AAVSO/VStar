# Coverage improvement waves & related health work

Status: **in progress** (Waves 1–3 algorithmic rows done on `562-coverage-improvements`; VeLa PBT still planned).

## Context

- Headline health coverage is **non-GUI** (#579). Prefer algorithmic UTs/PBTs over Swing.
- PBT examples: `PropertyPBTTest`, `DescStatsPBTTest`, `HJDConverterPBTTest`.
- Dashboard publishes `tests` + `pbts` (subset) via `script/count_pbts.py` after merge to `master`.

## Plan B — PBT count on health dashboard

### Core (done)

- `script/count_pbts.py`: `*PBTTest` methods + `qt().forAll` in mixed suites (default root `test/org`).
- CI (`vstar-UT.yml`) → `coverage.json` field `pbts`.
- Health card: **Tests** (total) with indented split **Unit tests** (= tests − pbts) and **Property-based (PBTs)**.
- Live site updates only on **master** Java-17 publish.

### Plugins (done)

- `count_pbts.py --test-root plugin/test` (repeatable `--test-root` supported).
- `plugin-UT.yml` publishes `pbts` in step summary, PR comment, and `plugin-coverage.json`.
- Same Tests / Unit tests / PBTs split on the plugin card (counts stay separate from core).
- Plugin PBTs stay **0** until `plugin/test` gains `*PBTTest` / `qt().forAll` methods.

## Deprecated types & JaCoCo (done / ongoing)

- JaCoCo does **not** skip `@Deprecated` by default.
- `script/exclude_deprecated_from_coverage.py` stages classfiles omitting **type-level** `@Deprecated` / javadoc `@deprecated`.
- Method-only deprecations: **not** excluded (JaCoCo declined; optional XML post-process deferred).
- Unused notifying-list cluster (`NotifyingArrayList`, `NotifyingList`, `ListChangeMessage`, `ListChangeType`) marked `@deprecated`; related new UTs removed.

## Wave 1 — core leverage (done)

| Area | Status |
|------|--------|
| `data.filter` (`ObservationMatcherOp`, `JDFieldMatcher` PBT) | Done |
| `ValidObservation` examples + PBT | Done |
| Extrema (`ApacheCommonsBrentOptimiserExtremaFinder`) | Done |
| `Harmonic` PBT | Done |

## Wave 2 — core follow-ons

| Area | Status |
|------|--------|
| `NumericPrecisionPrefs` | Done (uncommitted locally at last check) |
| `NotifyingArrayList` | **Dropped** (unused / deprecated) |
| Period-analysis value types (`PeriodAnalysisDataPoint`, coordinate types, `WWZStatistic`) | Done (uncommitted) |
| **VeLa PBT expansion** | **Deferred** → see [vela-pbt-improvements.md](vela-pbt-improvements.md) |

## Wave 3 — plugin algorithms (no Swing)

| # | Target | Status |
|---|--------|--------|
| 1 | `MinimumScatterPeriodFinder` + `DifferentialPhotometry` | **Cancelled** — jar targets commented out; types `@Deprecated` |
| 2 | `AoVPeriodSearch` algorithm core | **Done** (uncommitted) |
| 3 | `PeriodLuminosityDistanceCalculator` | **Done** (uncommitted) |
| 4 | `DescStatsBySeries` | **Done** (uncommitted) |

Commented-out packaging in `plugin/build.xml` (also `@Deprecated`): `IRISAutomaticPhotometryObservationSource`, `VSOLJObservationSource`, `VSPChartInfoRetriever`. Skip UTs for these until re-enabled.

Prefer dedicated JUnit under `plugin/test/` (MMRD / Kwee / Piecewise pattern). Keep co-located test helpers out of JaCoCo counts (e.g. `DFTandSpectralWindowTest` exclude).

## Delivery notes

- One cluster per PR when possible.
- Name dedicated PBTs `*PBTTest`; mixed QT as `*Property` / methods containing `qt().forAll`.
- Verify with `ant coverage-report` / plugin `coverage-report` and `script/count_pbts.py`.
