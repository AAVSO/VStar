# Kwee–van Woerden Timing Tool

The **Kwee–van Woerden** plug-in estimates the time of an eclipse or transit
minimum (or a maximum) and a timing uncertainty from a single, trimmed light-curve
segment. It appears under VStar's **Tools** menu as **Kwee-van Woerden**.

## Method

The time of minimum T₀ is obtained with the classic Kwee–van Woerden (1956)
folding procedure: the light curve is reflected about trial mid-times, the sum of
squared differences S(T) is formed at an odd number of folds, and a parabola
fitted to S(T) yields T₀.

The **timing error** follows Deeg (2020): after anchoring the fitted S(T₀) to
the photometric noise μ,

> σ²(T₀) = 2 μ² / a

where *a* is the quadratic coefficient of the S(T) fit. The original 1956
error formula is also computed when it is numerically defined; on modern low-noise
data (e.g. TESS) it often fails or underestimates σ. When the classic σ is
undefined, it is **omitted** from the result dialog (only the Deeg σ is shown).
When it is defined—as in the incomplete-eclipse example below—it appears as an
extra diagnostic line.

Default folds: **5** (3 = classic KvW; 7 also available).

## Workflow

1. Load photometry and switch to the **Raw Data** (JD) view.
2. Isolate **one eclipse** (filter or exclude off-eclipse points). The algorithm
   assumes a near-equidistant, eclipse-only segment.
3. Choose **Tools → Kwee-van Woerden**.
4. Select the series that contains the eclipse.
5. Set parameters (folds, initial T₁ estimate, event type, optional μ,
   resample).
6. Click **OK**.
7. Read T₀ ± σ (Deeg) from the result dialog. A compact CSV line is
   also copied to the clipboard when available.

For visual QC, the AAVSO “tracing paper” check is useful: reflect the light curve
about T₀ and overplot the forward and reversed segments.

## Parameters

| Parameter | Meaning |
|-----------|---------|
| Number of folds | 3, 5 (default), or 7 reflections about the initial mid-time |
| Initial time estimate | Midpoint of the segment, or the extreme magnitude |
| Event type | Minimum (eclipse/transit) or maximum |
| Photometric noise μ | Preferred: off-eclipse rms in the **same units** as the plotted values. Empty ⇒ estimate from min S |
| Resample if not equidistant | Linear interpolation onto median Δt when spacing varies |

## Worked example 1: complete eclipse (CM Dra epoch 7024)

This example uses the complete primary eclipse of CM Dra at epoch 7024 from
[hdeeg/KvW `example_data`](https://github.com/hdeeg/KvW/tree/main/example_data)
(`CMDra7024.lc`). The GitHub `.lc` file is whitespace-separated and is **not**
readable as-is by VStar’s Download or Simple observation sources. Use the
converted CSV in [Appendix A](#appendix-a-cm-dra-epoch-7024-converted-to-csv) instead.

### Steps in VStar

1. Choose **File → New Star from File… → Download or Simple → Request Observation Text**.
2. Copy the CSV block from [Appendix A](#appendix-a-cm-dra-epoch-7024-converted-to-csv)
   and paste it into the observation text box, then load the star.
3. Ensure you are in the **Raw Data** view. The series is already eclipse-only.
4. **Tools → Kwee-van Woerden**, select the loaded series.
5. Parameters:
   - Folds: **5 (recommended)**
   - Initial time estimate: **Extreme magnitude**
   - Event type: **Maximum** — Appendix A places **normalised flux** in the
     magnitude column (lower at mid-eclipse). Choosing Maximum makes the
     algorithm treat that dip correctly when the column is read as a VStar
     magnitude field. For ordinary AAVSO magnitudes (larger = fainter at
     mid-eclipse), use **Minimum** instead.
   - Photometric noise μ: enter **0.00138** to match the Deeg/README flux-domain
     demo, or leave empty to estimate.
   - Resample: can be left on; this fixture is already equidistant.
6. Click **OK**.
7. Confirm T₀ is near **58739.9291169** (BJD−2400000, as in the pasted times)
   and σ (Deeg) ≈ **0.0000125** d (≈ 1.1 s). Classic KvW σ is omitted here
   (undefined for this light curve).

## Worked example 2: incomplete eclipse (CM Dra epoch 7023)

This uses the incomplete primary at epoch 7023 (`CMDra7023.lc`), which Deeg uses
to illustrate unbalanced coverage and S(T) branch cropping. Paste the CSV from
[Appendix B](#appendix-b-cm-dra-epoch-7023-converted-to-csv).

### Steps in VStar

1. Load Appendix B the same way as Appendix A
   (**File → New Star from File… → Download or Simple → Request Observation Text**).
2. **Tools → Kwee-van Woerden** with the same parameter choices as example 1
   (5 folds, Extreme magnitude, **Maximum**, μ = **0.00138**).
3. Click **OK**.
4. Confirm T₀ ≈ **58738.6607358**, σ (Deeg) ≈ **0.0000191** d. For this
   light curve the classic KvW σ is defined and should appear as approximately
   **0.0000662** d.

## AAVSO data example

*(Placeholder.)* A walkthrough on real AAVSO AID photometry for a suggested
eclipsing binary will be added when a target is chosen.

## Limitations

- Input should be **one eclipse**, not a long multi-cycle light curve.
- Strongly **asymmetric** eclipses violate the symmetry assumption.
- Large gaps or highly uneven sampling need resampling or pre-interpolation.
- μ must be in the **same units** as the values analysed.

## Attribution

This plug-in is a **Java implementation** based on the Deeg (2020/2021) paper,
not a transpile of the [hdeeg/KvW](https://github.com/hdeeg/KvW) Python/IDL code.
That repository was used for golden-value verification and to clarify procedural
details not fully specified in the paper (in particular half-integer fold pairing
and S(T) branch cropping for incomplete eclipses).

### References

- Kwee, K.K. & van Woerden, H. 1956, Bull. Astron. Inst. Netherlands, 12, 327.
- Deeg, H.J. 2020, “A Modified Kwee–Van Woerden Method for Eclipse Minimum Timing
  with Reliable Error Estimates”, *Galaxies*, 9, 1.
  [arXiv:2011.09231](https://arxiv.org/abs/2011.09231) ·
  [ADS](https://ui.adsabs.harvard.edu/abs/2020Galax...9....1D/abstract)
- Reference code: [https://github.com/hdeeg/KvW](https://github.com/hdeeg/KvW)
- AAVSO guidance on times of minimum:
  [Determining the Time of Minimum](https://www.aavso.org/determining-time-minimum)

## Appendix A: CM Dra epoch 7024 converted to CSV

Source: https://github.com/hdeeg/KvW/blob/main/example_data/CMDra7024.lc

Copy everything below (including the comment lines) and paste it into the text box
opened by **File → New Star from File… → Download or Simple → Request Observation Text**.

```
#lightcurve of primary eclipse of CM Dra at epoch 7024
# BJD-TDB,     norm. flux
58739.90842904,0.9472268
58739.90981790,0.9282367
58739.91120677,0.9027383
58739.91259563,0.8765095
58739.91398450,0.8457783
58739.91537336,0.8197890
58739.91676222,0.7847447
58739.91815109,0.7523733
58739.91953995,0.7204994
58739.92092881,0.6859992
58739.92231768,0.6516519
58739.92370654,0.6194330
58739.92509541,0.5881648
58739.92648427,0.5583567
58739.92787313,0.5329423
58739.92926200,0.5253384
58739.93065086,0.5385398
58739.93203973,0.5633745
58739.93342859,0.5947506
58739.93481745,0.6270559
58739.93620632,0.6588940
58739.93759518,0.6948199
58739.93898405,0.7258901
58739.94037291,0.7601001
58739.94176177,0.7934963
58739.94315064,0.8235733
58739.94453950,0.8524282
58739.94592836,0.8814062
58739.94731723,0.9084927
58739.94870609,0.9325403
```

## Appendix B: CM Dra epoch 7023 converted to CSV

Source: https://github.com/hdeeg/KvW/blob/main/example_data/CMDra7023.lc

Incomplete primary eclipse (ingress-heavy). Paste as for Appendix A.

```
#lightcurve of primary eclipse of CM Dra at epoch 7023
# BJD-TDB,     norm. flux
58738.65150805,0.7110910
58738.65289691,0.6779842
58738.65428578,0.6422326
58738.65567464,0.6104053
58738.65706350,0.5786916
58738.65845236,0.5521469
58738.65984122,0.5289302
58738.66123009,0.5262330
58738.66261895,0.5436970
58738.66400781,0.5722702
58738.66539667,0.6013605
58738.66678553,0.6352366
58738.66817440,0.6680585
58738.66956326,0.7037288
58738.67095212,0.7349917
58738.67234098,0.7687948
58738.67372985,0.8026004
58738.67511871,0.8316808
58738.67650757,0.8600320
58738.67789643,0.8872918
58738.67928529,0.9154191
58738.68067416,0.9374043
```
