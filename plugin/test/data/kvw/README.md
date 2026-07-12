# Kwee–van Woerden golden reference light curves

Source: [hdeeg/KvW example_data](https://github.com/hdeeg/KvW/tree/main/example_data)
(Deeg 2020/2021 CM Dra TESS Sector 16 eclipse snippets).

## Golden values

Produced by reimplementing the Deeg algorithm (same logic as `kvw.py` demo
kwargs: `nfold=5`, `init_minflux=1`, `rms=0.00138`). Matches the README demo
of https://github.com/hdeeg/KvW to the printed precision.

| File | T0 (BJD-2400000) | σ_Deeg | σ_classic |
|------|------------------|--------|-----------|
| CMDra7024.lc | 58739.9291169 | 0.0000125 | NaN |
| CMDra7023.lc | 58738.6607358 | 0.0000191 | 0.0000662 |

These files contain normalised flux (lower at mid-eclipse), not magnitudes.
