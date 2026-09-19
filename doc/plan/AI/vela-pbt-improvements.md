# VeLa property-based test improvements

Status: **planned** (Wave 2 follow-on; do not block Wave 3).

## Goal

Grow meaningful QuickTheories coverage of VeLa beyond the ~7–8 `qt().forAll` methods currently embedded in the large mixed suite `test/.../vela/VeLaTest.java`, without counting every ordinary VeLa example as a PBT.

## Current state

- `VeLaTest` mixes example/golden tests with embedded PBTs (`WithQuickTheories`).
- Draft / unused sketches: `test/PBT/VeLaPropertyBasedTest.java.txt` (older junit-quickcheck style).
- ANTLR-generated listeners/parsers are low-value for coverage; skip or keep excluded if deprecated/generated.

## Recommended approach

1. **Add `VeLaPBTTest`** (or split by concern: `VeLaExprPBTTest`, `VeLaEnvPBTTest`) under `test/org/aavso/tools/vstar/vela/`.
2. **Move or duplicate** the strongest properties from `VeLaTest` into the dedicated class so `count_pbts.py` attributes them clearly; leave thin smoke examples in `VeLaTest`.
3. **Target properties** (priority order):
   - Literal / arithmetic round-trips (int, real, bool, string) where the grammar is stable.
   - Algebraic laws on operators VeLa exposes (`+`, `*`, boolean `and`/`or`/`not`) for well-typed random ASTs or string programs.
   - Environment binding: define then resolve symbol; shadowing / list ops if cheap to generate.
   - Failure modes: ill-typed or illegal programs throw expected VeLa errors (generators constrained to known-bad shapes).
4. **Generators**: keep domains small (short programs, bounded nesting) to avoid flaky timeouts; mirror `PropertyPBTTest` / `DescStatsPBTTest` style.
5. **Avoid**: GUI/scripting entry points; full interpreter fuzz without shrinking budgets.

## Success criteria

- New dedicated `*PBTTest` class(es) with several properties each.
- `script/count_pbts.py` count rises by the new PBT methods only.
- No material slowdown of `ant test` / CI (keep QT sample sizes modest).
- Optional: register interesting packages in PIT `targetClasses` if mutation testing is extended to VeLa later.

## Out of scope

- Formal verification / KeY (PBTs remain informal specs).
- Rewriting the entire `VeLaTest` suite.
