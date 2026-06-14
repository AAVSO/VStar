# Nova Exponential Decline Model

The **Nova Exponential Decline Model** fits the early decline of a nova
outburst with the function used by Kok (2010), equation (10):

$$
m(t) = P_1 - P_2 e^{-P_3(t-t_0)}
$$

This plug-in appears in VStar's **Analysis > Models** menu as **Nova
Exponential Decline Model**. The same model is also used internally by the
MMRD nova distance calculator when the **Exponential model fit (Kok 2010,
eq. 10)** source is selected.

## Parameters

| Parameter | Meaning |
|-----------|---------|
| $P_1$ | asymptotic post-outburst magnitude |
| $P_2$ | outburst amplitude above the asymptote |
| $P_3$ | decline rate in inverse days |
| $t_0$ | fit origin, taken to be the JD of the brightest observation |

The model is fitted by non-linear least squares using a Levenberg-Marquardt
optimizer. The fit is applied to observations from the brightest observation
onward.

![Nova exponential fit](images/nova_exp_fit_lightcurve.png)

TODO: add screenshot of a nova light curve with the fitted model series.

## Decline Times

The model has a closed-form crossing time. For a decline of $\Delta$
magnitudes from reference maximum $m_0$:

$$
t_\Delta = \frac{\ln(P_2 / (P_1 - m_0 - \Delta))}{P_3}
$$

The MMRD calculator uses this to compute:

- $t_2$, where $\Delta = 2$
- $t_3$, where $\Delta = 3$

When the model is run directly from the **Analysis > Models** menu, the model
series is added to the plot and can be inspected like other VStar models.

## Relationship to the MMRD Calculator

The MMRD nova distance calculator uses this model to smooth noisy nova
decline data before extracting $t_2$ and $t_3$. This is especially useful for
raw visual observations from AID, where direct crossing detection can be
misled by scatter.

For Kok-style MMRD testing:

1. Load the nova observations over the same JD window used in the paper.
2. Fit the exponential model.
3. Measure $t_2$ and $t_3$ from the fitted curve relative to the observed or
   chart-read maximum.
4. Use those values in the MMRD nova distance calculator.

## Uncertainties

The model retains the fit covariance matrix when available. The MMRD
calculator uses that covariance to estimate errors in $t_2$ and $t_3$ by
propagating the parameter covariance through the closed-form crossing time.

If the covariance cannot be estimated, the MMRD calculator leaves the
$\sigma t_2$ and $\sigma t_3$ fields blank. The user may still enter values
manually, for example from a published table.

## When to Use This Model

Use this model when:

- the nova has a reasonably smooth decline after maximum,
- you need $t_2$ and $t_3$ for MMRD distance estimation,
- raw observations are too noisy for direct peak+2/peak+3 crossing detection,
- you want a visible model series for inspection before running the MMRD tool.

## Limitations

- The model is empirical and intended for the early decline phase.
- It requires enough post-maximum observations to constrain three parameters.
- If the fitted amplitude does not reach peak + 2 or peak + 3 magnitudes, the
  corresponding $t_2$ or $t_3$ is unavailable.
- The fit origin is the brightest observation, so a spurious bright point can
  affect the model. Review the input data and filters before fitting.

## Reference

Kok, Y. 2010, *Absolute Magnitudes and Distances of Recent Novae*, JAAVSO,
38, 193.
