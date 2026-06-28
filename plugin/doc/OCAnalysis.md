# O-C Analysis Plug-in

## Overview

This plug-in set computes **O-C** (observed minus computed) values for times of
light-curve extrema and displays an O-C diagram. O-C analysis tests a fixed
ephemeris (period and epoch) against observed times of maximum (or minimum)
light.

Primary reference: Grant Foster, *Analyzing Light Curves*, chapter 13 (AAVSO
Variable Star Analytics; clock analogy in Tables 13.1–13.2).

The tool appears under **Tools** (general tools section). **Imported timings**
can be analysed without a loaded light curve; **From observations** requires
observations in VStar.

## Installation

From the `plugin` directory run `ant install`, then **restart VStar**. Three
separate plug-in JARs are installed (VStar loads one main class per JAR):

| JAR | Menu location | Purpose |
|-----|---------------|---------|
| `OCAnalysisTool.jar` | **Tools → O-C Analysis…** | Main O-C analysis tool |
| `OCAnalysisDemoObservationSource.jar` | **File → O-C Analysis demo data…** | Foster clock light curves |
| `OCAnalysisExportSink.jar` | **File → Save** (after running the tool) | CSV export of latest results |

All three share this help topic (`OCAnalysis.md`). Sample Foster timing files
ship under `plugin/doc/foster/` (see **Validation against Foster** below).

## Components

### O-C Analysis tool

A **general tool**: it opens from **Tools** even when no light curve is loaded
if you use **Imported timings file**. For **From observations**, load a star
first.

After **OK** on the parameter dialog:

- **From observations** — choose a series, then view results.
- **Imported timings file** — a file chooser opens for the timings file.

**Quadratic fit:** there is no quadratic option in the parameter dialog. After
results are computed, open the **Fit summary** tab — a quadratic least-squares
fit of O-C versus cycle is shown automatically when there are at least three
O-C points (Foster clock 6 / evolving period). It is not overlaid on the chart;
linear and optional two-segment fits are drawn on the O-C diagram.

### O-C Analysis demo data (observation source)

Loads synthetic light curves whose **maximum timings** match Foster Table 13.1
(clocks 1–6). The bump shape is the same for every clock; only timing differs,
as in Foster’s analogy. Run **Tools → O-C Analysis…** on the loaded V series
with the suggested ephemeris (P = 1 d, epoch shown at load).

Foster does **not** use an eclipsing-binary clock example in chapter 13. The
demo set follows his six clocks only. For real eclipsing binaries, use
**Event → Both maxima and minima** on your own data.

### O-C Analysis CSV Export (observation sink)

Exports the most recent O-C results. Run the tool first; use **Export CSV…** in
the results dialog or **File → Save → O-C Analysis CSV Export**.

## Usage

1. **Tools → O-C Analysis…**
2. Parameter dialog (compact two-column layout): data source, ephemeris, timing
   options as applicable. **Ephemeris source** is a drop-down only; edit period
   and epoch in their fields.
3. **OK** (and file chooser if importing timings).
4. Results dialog: **O-C diagram**, **Data table**, **Fit summary**.

## Validation against Foster (Tables 13.1–13.2)

Foster’s six test clocks use theory **P = 1 day**, **epoch = cycle 0** (t = 0).
Computed time C_n = n. O-C = observed time O_n − C_n (Table 13.2).

### Reference O-C values (Table 13.2)

| Cycle | Clock 1 | Clock 2 | Clock 3 | Clock 4 | Clock 5 | Clock 6 |
|------:|--------:|--------:|--------:|--------:|--------:|--------:|
| 0 | 0 | +0.0035 | 0 | −0.0014 | 0 | 0 |
| 1 | 0 | +0.0035 | +0.0021 | −0.0014 | −0.0014 | 0 |
| 2 | 0 | +0.0035 | +0.0042 | −0.0014 | −0.0028 | +0.0007 |
| 3 | 0 | +0.0035 | +0.0062 | −0.0014 | −0.0042 | +0.0021 |
| 4 | 0 | +0.0035 | +0.0083 | **+0.0257** | −0.0056 | +0.0042 |
| 5 | 0 | +0.0035 | +0.0104 | +0.0257 | −0.0069 | +0.0069 |
| 6 | 0 | +0.0035 | +0.0125 | +0.0257 | −0.0062 | +0.0104 |
| 7 | 0 | +0.0035 | +0.0146 | +0.0257 | −0.0056 | +0.0146 |
| 8 | 0 | +0.0035 | +0.0167 | +0.0257 | −0.0049 | +0.0194 |
| 9 | 0 | +0.0035 | +0.0188 | +0.0257 | −0.0042 | +0.0250 |

### Demo scenario mapping

| Foster clock | Demo scenario (observation source) | Expected in plug-in |
|--------------|-----------------------------------|---------------------|
| 1 | Foster clock 1 — correct ephemeris | Flat O-C ≈ 0 |
| 2 | Foster clock 2 — epoch offset | Flat O-C ≈ +0.0035 d |
| 3 | Foster clock 3 — period error | Linear slope ≈ +0.0021 d/cycle |
| 4 | Foster clock 4 — epoch jump | Step at cycle 4; two-segment break **4** |
| 5 | Foster clock 5 — period change | Slope change; two-segment break **6** |
| 6 | Foster clock 6 — slowing clock | Curved O-C; **quadratic** on Fit summary |

### Method A — imported timings (closest to Foster)

Use the sample files in `plugin/doc/foster/`:

| File | Clock |
|------|-------|
| `foster_clock1.txt` … `foster_clock6.txt` | Tables 13.1 observed times as HJD |

For each file:

1. **Tools → O-C Analysis…**
2. **Data source:** Imported timings file → select the file.
3. **Period:** 1.0 d, **Epoch:** 2450000.0 HJD (as in file headers).
4. Compare O-C points and **Fit summary** to Table 13.2 and the table above.

This tests timing import and O-C arithmetic without the synthetic light-curve
step.

### Method B — demo observation source

1. **File → O-C Analysis demo data…** → pick a Foster clock scenario.
2. Note suggested P, epoch, and break cycle (if any).
3. **Tools → O-C Analysis…** → **From observations**, V series, same ephemeris.
4. Compare to Table 13.2.

Light curves look similar because Foster’s lesson is about **O-C**, not different
bump shapes.

### Method C — automated tests

`cd plugin && ant test` runs unit tests that check clock 1–3 O-C patterns and
Foster timing arrays.

## Ephemeris source

When **From observations** is selected, **Ephemeris source** pre-fills period
and epoch (drop-down only):

| Source | Fills from |
|--------|------------|
| Document period (phase plot / PA) | Phase plot / PA period and epoch |
| Star metadata | Loaded star record |
| Manual entry | Best default, then edit |

Disabled for **Imported timings** — enter period and epoch directly.

## Imported timings file format

One timing per line: `cycle HJD [sigma]` or `HJD [sigma]`. Whitespace, comma,
or semicolon separators. `#` comments allowed.

## Timing methods (From observations)

- **Parabolic interpolation** — parabolic refinement at discrete extremum.
- **Mean JD of extreme N%** — mean time of brightest N% in each cycle.
- **From current model function** — analytic extrema from a JD-based model.

## Event type

- **Maximum** or **Minimum**
- **Both maxima and minima** — for eclipsing binaries on **real** data (not in
  Foster’s six-clock tutorial set)

## Interpreting O-C diagrams

| Pattern | Likely cause |
|---------|----------------|
| Flat at 0 | Ephemeris matches |
| Flat, offset | Epoch wrong, period OK |
| Linear slope | Period wrong |
| Broken line, parallel segments | Epoch jump |
| Broken line, different slopes | Period change |
| Curved (parabolic) | Evolving period → **Fit summary** quadratic text |

## Two-segment fit (optional)

On **Fit summary** after results: enter **break cycle**, click **Apply**. Not in
the initial parameter dialog. Needs ≥4 O-C points and ≥2 per segment.

## Parameters (initial dialog)

| Parameter | Description |
|-----------|-------------|
| Data source | Observations or imported timings |
| Ephemeris source | Pre-fill (observations only) |
| Period, epoch | Test ephemeris (required) |
| Event | Maximum, minimum, or both |
| Timing method, extreme N%, min obs | Observations only |

## Fit summary (after results)

| Output | When |
|--------|------|
| Linear fit | ≥2 O-C points; drawn on chart |
| Quadratic fit | ≥3 O-C points; text only (Foster clock 6) |
| Two-segment fit | User break cycle + Apply; drawn on chart |

## References

- Foster, G. *Analyzing Light Curves*, chapter 13 — [AAVSO Chapter 13 PDF](https://www.aavso.org/sites/default/files/education/vsa/Chapter13.pdf)
- [VStar issue #93](https://github.com/AAVSO/VStar/issues/93)

## Version history

- **1.3** — Foster Table 13.1 demo timings; six clocks only (removed mislabeled EB
  demo); `doc/foster/*.txt` import samples; validation section; compact dialog;
  general tool; break cycle on Fit summary; stable chart markers.
- **1.2** — Imported timings, CSV export, quadratic fit, BOTH event type, demo
  observation source.
- **1.1** — Model timing, linear/two-segment fits, error bars.
- **1.0** — Initial observation-based O-C plot and table.
