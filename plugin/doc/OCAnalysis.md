# O-C Analysis Plug-in

## Overview

This observation tool plug-in computes **O-C** (observed minus computed) values
for times of light-curve extrema and displays an O-C diagram. O-C analysis
tests a fixed ephemeris (period and epoch) against observed times of maximum
(or minimum) light.

Primary reference: Grant Foster, *Analyzing Light Curves*, chapter 13.

## Usage

1. Load a star and select **Tools → O-C Analysis…**
2. Choose the photometric series to analyse.
3. Set the ephemeris (period and epoch). Defaults are taken from the current
   phase-plot parameters if available, otherwise from star metadata.
4. Choose the event type, timing method, and minimum observations per cycle.
5. Review the O-C diagram (cycle number on the x-axis by default; toggle to
   observed time) and the data table.

## Timing methods

- **Parabolic interpolation** — finds the discrete magnitude extremum in each
  cycle and refines the time using a three-point parabolic fit.
- **Mean JD of extreme N%** — averages the Julian Dates of the brightest
  (for maxima) N% of observations in each cycle.

## Interpreting O-C diagrams

Following Foster (chapter 13):

| Pattern | Likely cause |
|---------|----------------|
| Flat line at O-C = 0 | Ephemeris matches the data |
| Horizontal line, offset from zero | Period correct, **epoch** needs correction |
| Straight line with non-zero slope | **Period** needs correction; slope ≈ ΔP |
| Broken line, same slope, different offset | **Epoch jump**, period unchanged |
| Broken line, different slopes | **Period change** at the break |
| Parabolic trend | Smoothly **evolving period** (Phase 2+) |

## Parameters

| Parameter | Description |
|-----------|-------------|
| Period | Ephemeris period (days) |
| Epoch | HJD of maximum (cycle 0) for the ephemeris |
| Event | Maximum or minimum light |
| Timing method | Parabolic or mean-of-extreme |
| Extreme N% | Used by the mean timing method (1–100) |
| Min obs per cycle | Cycles with fewer observations are skipped |

## References

- Foster, G. *Analyzing Light Curves*, chapter 13 (O-C Analysis)
- [VStar issue #93](https://github.com/AAVSO/VStar/issues/93)

## Version history

- 1.1 — Phase 2: model-based timing, linear/two-segment fits, O-C error bars,
  Foster-style fit interpretation.
- 1.0 — Initial release (Phase 1): observation-based timing, O-C plot and table.

## Phase 2 features

### Model timing

If a function-based model with JD coordinates is selected in VStar (e.g. a
Fourier or polynomial fit), choose **From current model function** as the
timing method. The plug-in finds analytic extrema per cycle using VStar's
Brent optimiser.

### Linear and two-segment fits

After computing O-C points, a linear fit of O-C versus cycle number is shown on
the **Fit summary** tab and overlaid on the chart. Optionally enter a **break
cycle** for a two-segment fit to investigate period changes (Foster, ch. 13).

### Error bars

When magnitude uncertainties are present, approximate O-C error bars are shown
on the chart and listed in the data table.
