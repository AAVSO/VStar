/**
 * VStar: a statistical analysis tool for variable star data.
 * Copyright (C) 2026  AAVSO (http://www.aavso.org/)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */
package org.aavso.tools.vstar.external.lib;

import java.util.List;

import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.exception.AlgorithmError;
import org.aavso.tools.vstar.ui.model.plot.ContinuousModelFunction;
import org.aavso.tools.vstar.util.locale.LocaleProps;
import org.aavso.tools.vstar.util.model.AbstractModel;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.optimization.fitting.CurveFitter;
import org.apache.commons.math.optimization.fitting.ParametricRealFunction;
import org.apache.commons.math.optimization.OptimizationException;
import org.apache.commons.math.optimization.general.LevenbergMarquardtOptimizer;

/**
 * <p>
 * A nova decline light curve model of the form:
 * </p>
 * 
 * <p>
 * mag(t) = P1 - P2*exp(-P3*(t - t0))
 * </p>
 * 
 * <p>
 * where t0 is the time of the brightest observation, after Kok, Y. 2010,
 * JAAVSO, 38, 193, equation (10). The model is fitted, by non-linear least
 * squares (Levenberg-Marquardt), to the observations from the brightest
 * observation onward.
 * </p>
 * 
 * <p>
 * Once fitted, the times taken for the nova to decline by N magnitudes from
 * the fitted peak (e.g. t2, t3) are available in closed form:
 * </p>
 * 
 * <p>
 * t(deltaMag) = ln(P2/(P2 - deltaMag))/P3
 * </p>
 * 
 * <p>
 * This model assumes raw (JD) mode rather than phase plot mode.
 * </p>
 */
public class NovaExponentialModel extends AbstractModel {

    private double p1;
    private double p2;
    private double p3;

    private double peakJD;
    private double peakMag;

    private boolean fitted;
    private double[][] parameterCovariances;
    private double[] parameterErrors;

    public NovaExponentialModel(List<ValidObservation> obs) {
        super(obs);
        fitted = false;
        parameterCovariances = null;
        parameterErrors = null;
    }

    /**
     * @return the fitted asymptotic (post-outburst) magnitude, P1
     */
    public double getP1() {
        return p1;
    }

    /**
     * @return the fitted outburst amplitude above the asymptote, P2
     */
    public double getP2() {
        return p2;
    }

    /**
     * @return the fitted decline rate, P3, in inverse days
     */
    public double getP3() {
        return p3;
    }

    /**
     * @return the JD of the brightest observation, used as the fit origin, t0
     */
    public double getPeakJD() {
        return peakJD;
    }

    /**
     * @return the magnitude of the brightest observation
     */
    public double getObservedPeakMagnitude() {
        return peakMag;
    }

    /**
     * @return the fitted peak magnitude, P1 - P2, i.e. the model value at t0
     */
    public double getFittedPeakMagnitude() {
        return p1 - p2;
    }

    /**
     * Return the time in days, from the fitted peak, taken for the nova to
     * decline by the specified number of magnitudes.
     * 
     * @param deltaMag The decline in magnitudes, e.g. 2 for t2, 3 for t3.
     * @return The decline time in days, or null if the model has not been
     *         fitted or never declines by the requested amount (P2 <= deltaMag).
     */
    public Double timeToDecline(double deltaMag) {
        return fitted ? timeToDecline(deltaMag, getFittedPeakMagnitude()) : null;
    }

    /**
     * Return the time in days, from the fit origin t0 (the brightest
     * observation), at which the fitted curve crosses peakMag + deltaMag.
     * This permits the decline to be measured from a maximum magnitude
     * (e.g. read off the light curve, as in section 2.2 of Kok 2010) rather
     * than from the fitted peak, P1 - P2.
     * 
     * @param deltaMag The decline in magnitudes, e.g. 2 for t2, 3 for t3.
     * @param peakMag  The maximum magnitude from which the decline is
     *                 measured.
     * @return The crossing time in days, or null if the model has not been
     *         fitted or the curve never reaches peakMag + deltaMag
     *         (P1 - peakMag <= deltaMag).
     */
    public Double timeToDecline(double deltaMag, double peakMag) {
        Double time = null;

        double depth = p1 - peakMag - deltaMag;

        if (fitted && depth > 0 && p2 > 0 && p3 > 0) {
            time = Math.log(p2 / depth) / p3;
            // The crossing precedes the fit origin if the target magnitude
            // is brighter than the curve at t0.
            if (time < 0) {
                time = null;
            }
        }

        return time;
    }

    /**
     * Return the 1-sigma error in the time in days, from the fit origin t0,
     * at which the fitted curve crosses peakMag + deltaMag. The error is
     * propagated from the non-linear fit parameter covariance matrix.
     * 
     * @param deltaMag The decline in magnitudes, e.g. 2 for t2, 3 for t3.
     * @param peakMag  The maximum magnitude from which the decline is
     *                 measured.
     * @return The crossing time error in days, or null if no covariance
     *         estimate is available or the crossing is unavailable.
     */
    public Double timeToDeclineError(double deltaMag, double peakMag) {
        if (!fitted || parameterCovariances == null) {
            return null;
        }

        Double declineTime = timeToDecline(deltaMag, peakMag);
        if (declineTime == null) {
            return null;
        }

        double depth = p1 - peakMag - deltaMag;
        if (depth <= 0 || p2 <= 0 || p3 <= 0) {
            return null;
        }

        // t = ln(P2 / (P1 - peakMag - deltaMag)) / P3.
        // Propagate via grad(t)^T Cov(P) grad(t), treating peakMag as fixed.
        double[] gradient = new double[] {
                -1 / (p3 * depth),
                1 / (p3 * p2),
                -declineTime / p3
        };

        double variance = 0;
        for (int i = 0; i < gradient.length; i++) {
            for (int j = 0; j < gradient.length; j++) {
                variance += gradient[i] * parameterCovariances[i][j] * gradient[j];
            }
        }

        return variance >= 0 ? Math.sqrt(variance) : null;
    }

    /**
     * @return t2, the time in days for a decline of 2 magnitudes from the
     *         fitted peak, or null if not available
     */
    public Double getT2() {
        return timeToDecline(2);
    }

    /**
     * @return the error on t2 in days, or null if unavailable
     */
    public Double getT2Error() {
        return timeToDeclineError(2, getFittedPeakMagnitude());
    }

    /**
     * @return t3, the time in days for a decline of 3 magnitudes from the
     *         fitted peak, or null if not available
     */
    public Double getT3() {
        return timeToDecline(3);
    }

    /**
     * @return the error on t3 in days, or null if unavailable
     */
    public Double getT3Error() {
        return timeToDeclineError(3, getFittedPeakMagnitude());
    }

    @Override
    public void execute() throws AlgorithmError {
        if (obs.size() < 5) {
            throw new AlgorithmError("Too few observations for a nova exponential decline fit.");
        }

        // Find the brightest observation; the decline is fitted from there.
        int brightestIndex = 0;
        for (int i = 1; i < obs.size(); i++) {
            if (obs.get(i).getMag() < obs.get(brightestIndex).getMag()) {
                brightestIndex = i;
            }
        }

        List<ValidObservation> decline = obs.subList(brightestIndex, obs.size());

        if (decline.size() < 4) {
            throw new AlgorithmError("Too few post-maximum observations for a nova exponential decline fit.");
        }

        peakJD = decline.get(0).getJD();
        peakMag = decline.get(0).getMag();

        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        CurveFitter fitter = new CurveFitter(optimizer);

        double faintest = peakMag;
        for (ValidObservation ob : decline) {
            // Times are relative to the peak for numerical stability.
            fitter.addObservedPoint(ob.getJD() - peakJD, ob.getMag());
            if (ob.getMag() > faintest) {
                faintest = ob.getMag();
            }
        }

        // Initial parameter estimates: asymptote at the faintest magnitude,
        // amplitude as the observed decline, rate from the half-decline time.
        double p1Init = faintest;
        double p2Init = Math.max(faintest - peakMag, 0.1);
        double halfMag = peakMag + p2Init / 2;
        double p3Init = 0;
        for (ValidObservation ob : decline) {
            if (ob.getMag() >= halfMag && ob.getJD() > peakJD) {
                p3Init = Math.log(2) / (ob.getJD() - peakJD);
                break;
            }
        }
        if (p3Init == 0) {
            double timeSpan = decline.get(decline.size() - 1).getJD() - peakJD;
            p3Init = timeSpan > 0 ? 2 / timeSpan : 1;
        }

        try {
            double[] params = fitter.fit(new ExponentialDeclineFunction(),
                    new double[] { p1Init, p2Init, p3Init });

            p1 = params[0];
            p2 = params[1];
            p3 = params[2];

            try {
                parameterCovariances = optimizer.getCovariances();
                parameterErrors = optimizer.guessParametersErrors();
            } catch (FunctionEvaluationException e) {
                parameterCovariances = null;
                parameterErrors = null;
            } catch (OptimizationException e) {
                parameterCovariances = null;
                parameterErrors = null;
            }

            fitted = true;

            String comment = "From nova exponential decline fit";

            for (int i = 0; i < decline.size() && !interrupted; i++) {
                ValidObservation ob = decline.get(i);
                double modelValue = valueAt(ob.getJD());
                collectObs(modelValue, ob, comment);
            }

            rootMeanSquare();
            informationCriteria(3);
            fitMetrics();
            functionStrings();
        } catch (FunctionEvaluationException e) {
            throw new AlgorithmError(e.getLocalizedMessage());
        } catch (org.apache.commons.math.optimization.OptimizationException e) {
            throw new AlgorithmError(e.getLocalizedMessage());
        }
    }

    /**
     * Return the model magnitude at the specified time.
     * 
     * @param jd The time (JD) at which to evaluate the model.
     * @return The model magnitude.
     */
    public double valueAt(double jd) {
        return p1 - p2 * Math.exp(-p3 * (jd - peakJD));
    }

    @Override
    public ContinuousModelFunction getModelFunction() {
        UnivariateRealFunction function = new UnivariateRealFunction() {
            @Override
            public double value(double t) {
                return valueAt(t);
            }
        };

        return new ContinuousModelFunction(function, fit);
    }

    @Override
    public boolean hasFuncDesc() {
        return true;
    }

    @Override
    public String toVeLaString() {
        String strRepr = functionStrMap.get(LocaleProps.get("MODEL_INFO_FUNCTION_TITLE"));

        if (strRepr == null) {
            StringBuffer buf = new StringBuffer();

            buf.append("f(t:real) : real {\n");
            // Double.toString() is locale-independent.
            buf.append("    " + Double.toString(p1));
            buf.append(" - " + Double.toString(p2));
            buf.append(" * exp(-" + Double.toString(p3));
            buf.append(" * (t - " + Double.toString(peakJD) + "))\n");
            buf.append("}\n");

            strRepr = buf.toString();
        }

        return strRepr;
    }

    @Override
    public String toString() {
        return toVeLaString();
    }

    @Override
    public String getDescription() {
        return getKind() + " for " + obs.get(0).getBand() + " series";
    }

    @Override
    public String getKind() {
        return "Nova Exponential Decline Fit";
    }

    // The parametric function for the curve fitter:
    // f(x; P1, P2, P3) = P1 - P2*exp(-P3*x), x = t - t0
    private static class ExponentialDeclineFunction implements ParametricRealFunction {
        @Override
        public double value(double x, double[] p) {
            return p[0] - p[1] * Math.exp(-p[2] * x);
        }

        @Override
        public double[] gradient(double x, double[] p) {
            double expTerm = Math.exp(-p[2] * x);
            return new double[] { 1, -expTerm, p[1] * x * expTerm };
        }
    }
}
