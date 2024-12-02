/**
 * VStar: a statistical analysis tool for variable star data.
 * Copyright (C) 2013 AAVSO (http://www.aavso.org/)
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
package org.aavso.tools.vstar.plugin.model.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.aavso.tools.vstar.data.DateInfo;
import org.aavso.tools.vstar.data.Magnitude;
import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.exception.AlgorithmError;
import org.aavso.tools.vstar.plugin.ModelCreatorPluginBase;
import org.aavso.tools.vstar.ui.dialog.PolynomialDegreeDialog;
import org.aavso.tools.vstar.ui.mediator.AnalysisType;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.model.plot.ContinuousModelFunction;
import org.aavso.tools.vstar.ui.model.plot.ICoordSource;
import org.aavso.tools.vstar.ui.model.plot.JDCoordSource;
import org.aavso.tools.vstar.ui.model.plot.JDTimeElementEntity;
import org.aavso.tools.vstar.ui.model.plot.StandardPhaseCoordSource;
import org.aavso.tools.vstar.util.ApacheCommonsDerivativeBasedExtremaFinder;
import org.aavso.tools.vstar.util.Tolerance;
import org.aavso.tools.vstar.util.comparator.JDComparator;
import org.aavso.tools.vstar.util.comparator.StandardPhaseComparator;
import org.aavso.tools.vstar.util.locale.LocaleProps;
import org.aavso.tools.vstar.util.model.IModel;
import org.aavso.tools.vstar.util.model.PeriodFitParameters;
import org.aavso.tools.vstar.util.prefs.NumericPrecisionPrefs;
import org.aavso.tools.vstar.util.stats.DescStats;
import org.apache.commons.math.ConvergenceException;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.DifferentiableUnivariateRealFunction;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math.optimization.fitting.PolynomialFitter;
import org.apache.commons.math.optimization.general.AbstractLeastSquaresOptimizer;
import org.apache.commons.math.optimization.general.LevenbergMarquardtOptimizer;

/**
 * A polynomial model creator plugin that uses an Apache Commons polynomial
 * fitter.
 */
public class ApacheCommonsPolynomialFitCreatorPlugin extends ModelCreatorPluginBase {

    private boolean needGUI = true;

    private int degree;

    private ICoordSource timeCoordSource;
    private Comparator<ValidObservation> timeComparator;

    public ApacheCommonsPolynomialFitCreatorPlugin() {
        super();
    }

    @Override
    public String getDescription() {
        return LocaleProps.get("ANALYSIS_MENU_POLYNOMIAL_FIT");
    }

    @Override
    public String getDisplayName() {
        return LocaleProps.get("ANALYSIS_MENU_POLYNOMIAL_FIT");
    }

    @Override
    public IModel getModel(List<ValidObservation> obs) {
        return new PolynomialFitCreator(obs);
    }

    /**
     * This is intended for setting parameters from the scripting API or plug-in
     * test.
     */
    @Override
    public void setParams(Object[] params) {
        assert (params.length == 1);
        double degree = (double) params[0];
        setDegree((int) degree);
        needGUI = false;
    }

    private void setDegree(int degree) {
        this.degree = degree;
    }

    public int getDegree() {
        return degree;
    }

    private int getMinDegree() {
        return 0;
    }

    private int getMaxDegree() {
        // TODO: make this a preference
        return 30;
    }

    class PolynomialFitCreator implements IModel {
        List<ValidObservation> obs;
        double zeroPoint;
        boolean interrupted;
        List<ValidObservation> fit;
        List<ValidObservation> residuals;
        PolynomialFunction function;
        PolynomialFitter fitter;
        AbstractLeastSquaresOptimizer optimizer;
        double aic = Double.NaN;
        double bic = Double.NaN;
        Map<String, String> functionStrMap;

        PolynomialFitCreator(List<ValidObservation> obs) {
            // TODO: the code in this block should be refactored into the model
            // creator base class or elsewhere

            // Select time mode (JD or phase).
            switch (Mediator.getInstance().getAnalysisType()) {
            case RAW_DATA:
                timeCoordSource = JDCoordSource.instance;
                timeComparator = JDComparator.instance;
                this.obs = obs;
                zeroPoint = DescStats.calcTimeElementMean(obs, JDTimeElementEntity.instance);
                break;

            case PHASE_PLOT:
                timeCoordSource = StandardPhaseCoordSource.instance;
                timeComparator = StandardPhaseComparator.instance;
                this.obs = new ArrayList<ValidObservation>(obs);
                Collections.sort(this.obs, timeComparator);
                zeroPoint = 0;
                break;
            }

            int minDegree = getMinDegree();
            int maxDegree = getMaxDegree();

            boolean cancelled = false;

            if (needGUI) {
                PolynomialDegreeDialog polyDegreeDialog = new PolynomialDegreeDialog(minDegree, maxDegree);

                setDegree(polyDegreeDialog.getDegree());

                cancelled = polyDegreeDialog.isCancelled();
            }

            if (!cancelled) {
                optimizer = new LevenbergMarquardtOptimizer();
                fitter = new PolynomialFitter(getDegree(), optimizer);
                interrupted = false;
                fit = new ArrayList<ValidObservation>();
                residuals = new ArrayList<ValidObservation>();
                functionStrMap = new TreeMap<String, String>();
            }
        }

        @Override
        public String getDescription() {
            return LocaleProps.get("MODEL_INFO_POLYNOMIAL_DEGREE_DESC") + degree + " for " + obs.get(0).getBand()
                    + " series";
        }

        @Override
        public List<ValidObservation> getFit() {
            return fit;
        }

        @Override
        public List<ValidObservation> getResiduals() {
            return residuals;
        }

        @Override
        public String getKind() {
            return LocaleProps.get("ANALYSIS_MENU_POLYNOMIAL_FIT");
        }

        // TODO: if this is not generalisable, it should be removed
        // as a requirement from base class or the name changed to
        // getPeriodFitParameters()
        @Override
        public List<PeriodFitParameters> getParameters() {
            // None for a polynomial fit.
            return null;
        }

        @Override
        public boolean hasFuncDesc() {
            return true;
        }

        public String toFitMetricsString() throws AlgorithmError {
            String strRepr = functionStrMap.get(LocaleProps.get("MODEL_INFO_FIT_METRICS_TITLE"));

            if (strRepr == null) {
                // Goodness of fit
                strRepr = "RMS: " + NumericPrecisionPrefs.formatOther(optimizer.getRMS());

                // Akaike and Bayesean Information Criteria
                if (aic != Double.NaN && bic != Double.NaN) {
                    strRepr += "\nAIC: " + NumericPrecisionPrefs.formatOther(aic);
                    strRepr += "\nBIC: " + NumericPrecisionPrefs.formatOther(bic);
                }
            }

            return strRepr;
        }

        @Override
        public String toString() {
            String strRepr = functionStrMap.get(LocaleProps.get("MODEL_INFO_FUNCTION_TITLE"));

            if (strRepr == null) {
                strRepr = "zeroPoint is " + NumericPrecisionPrefs.formatTime(zeroPoint) + "\n\n";

                strRepr += "f(t:real) : real {\n";

                double[] coeffs = function.getCoefficients();
                for (int i = coeffs.length - 1; i >= 1; i--) {
                    strRepr += "    " + NumericPrecisionPrefs.formatPolyCoef(coeffs[i]);
                    strRepr += "*(t-zeroPoint)^" + i + " +\n";
                }
                strRepr += "    " + NumericPrecisionPrefs.formatPolyCoef(coeffs[0]);
                strRepr += "\n}";
            }

            return strRepr;
        }

        public String toExcelString() {
            String strRepr = functionStrMap.get(LocaleProps.get("MODEL_INFO_EXCEL_TITLE"));

            if (strRepr == null) {
                strRepr = "=";

                double[] coeffs = function.getCoefficients();
                for (int i = coeffs.length - 1; i >= 1; i--) {
                    strRepr += NumericPrecisionPrefs.formatPolyCoef(coeffs[i]);
                    strRepr += "*(A1-" + NumericPrecisionPrefs.formatTime(zeroPoint) + ")^" + i + "+\n";
                }
                strRepr += NumericPrecisionPrefs.formatPolyCoef(coeffs[0]);
            }

            return strRepr;
        }

        // toRString must be locale-independent!
        public String toRString() {
            String strRepr = functionStrMap.get(LocaleProps.get("MODEL_INFO_R_TITLE"));

            if (strRepr == null) {
                strRepr = "zeroPoint <- " + NumericPrecisionPrefs.formatTimeLocaleIndependent(zeroPoint) + "\n\n";

                strRepr += "model <- function(t)\n";

                double[] coeffs = function.getCoefficients();
                for (int i = coeffs.length - 1; i >= 1; i--) {
                    strRepr += NumericPrecisionPrefs.formatPolyCoefLocaleIndependent(coeffs[i]);
                    strRepr += "*(t-zeroPoint)^" + i + " +\n";
                }
                strRepr += NumericPrecisionPrefs.formatPolyCoefLocaleIndependent(coeffs[0]);
            }

            return strRepr;
        }

        @Override
        public ContinuousModelFunction getModelFunction() {
            // UnivariateRealFunction func = new
            // UnivariateRealFunction() {
            // @Override
            // public double value(double x)
            // throws FunctionEvaluationException {
            // double y = 0;
            // double[] coeffs = function.getCoefficients();
            // for (int i = coeffs.length - 1; i >= 1; i--) {
            // y += coeffs[i] * Math.pow(x, i);
            // }
            // y += coeffs[0];
            // return y;
            // }
            // };

            return new ContinuousModelFunction(function, fit, zeroPoint);
        }

        // An alternative implementation for getModelFunction() that
        // uses Horner's method to avoid exponentiation.
        public UnivariateRealFunction getModelFunctionHorner() {
            UnivariateRealFunction func = new UnivariateRealFunction() {
                @Override
                public double value(double x) throws FunctionEvaluationException {
                    // Compute the value of the polynomial for x via
                    // Horner's method.
                    double y = 0;
                    double[] coeffs = function.getCoefficients();
                    for (double coeff : coeffs) {
                        y = y * x + coeff;
                    }
                    return y;
                }
            };

            return func;
        }

        @Override
        public void execute() throws AlgorithmError {

            for (int i = 0; i < obs.size() && !interrupted; i++) {
                fitter.addObservedPoint(1.0, timeCoordSource.getXCoord(i, obs) - zeroPoint, obs.get(i).getMag());
            }

            if (!interrupted) {
                try {
                    function = fitter.fit();

                    fit = new ArrayList<ValidObservation>();
                    residuals = new ArrayList<ValidObservation>();
                    double sumSqResiduals = 0;

                    String comment = LocaleProps.get("MODEL_INFO_POLYNOMIAL_DEGREE_DESC") + degree;

                    // Create fit and residual observations and
                    // compute the sum of squares of residuals for
                    // Akaike and Bayesean Information Criteria.
                    for (int i = 0; i < obs.size() && !interrupted; i++) {
                        ValidObservation ob = obs.get(i);

                        double x = timeCoordSource.getXCoord(i, obs);
                        double zeroedX = x - zeroPoint;
                        double y = function.value(zeroedX);

                        ValidObservation fitOb = new ValidObservation();
                        fitOb.setDateInfo(new DateInfo(ob.getJD()));
                        if (Mediator.getInstance().getAnalysisType() == AnalysisType.PHASE_PLOT) {
                            fitOb.setPreviousCyclePhase(ob.getPreviousCyclePhase());
                            fitOb.setStandardPhase(ob.getStandardPhase());
                        }
                        fitOb.setMagnitude(new Magnitude(y, 0));
                        fitOb.setBand(SeriesType.Model);
                        fitOb.setComments(comment);
                        fit.add(fitOb);

                        ValidObservation resOb = new ValidObservation();
                        resOb.setDateInfo(new DateInfo(ob.getJD()));
                        if (Mediator.getInstance().getAnalysisType() == AnalysisType.PHASE_PLOT) {
                            resOb.setPreviousCyclePhase(ob.getPreviousCyclePhase());
                            resOb.setStandardPhase(ob.getStandardPhase());
                        }
                        double residual = ob.getMag() - y;
                        resOb.setMagnitude(new Magnitude(residual, 0));
                        resOb.setBand(SeriesType.Residuals);
                        resOb.setComments(comment);
                        residuals.add(resOb);

                        sumSqResiduals += (residual * residual);
                    }

                    // Fit metrics (AIC, BIC).
                    int n = residuals.size();
                    if (n != 0 && sumSqResiduals / n != 0) {
                        double commonIC = n * Math.log(sumSqResiduals / n);
                        aic = commonIC + 2 * degree;
                        bic = commonIC + degree * Math.log(n);
                    }

                    functionStrMap.put(LocaleProps.get("MODEL_INFO_FIT_METRICS_TITLE"), toFitMetricsString());

                    ApacheCommonsDerivativeBasedExtremaFinder finder = new ApacheCommonsDerivativeBasedExtremaFinder(
                            fit, (DifferentiableUnivariateRealFunction) function, timeCoordSource, zeroPoint);

                    String extremaStr = finder.toString();

                    if (extremaStr != null) {
                        String title = LocaleProps.get("MODEL_INFO_EXTREMA_TITLE");

                        functionStrMap.put(title, extremaStr);
                    }

                    // Minimum/maximum.
                    // ApacheCommonsBrentOptimiserExtremaFinder
                    // finder = new
                    // ApacheCommonsBrentOptimiserExtremaFinder(
                    // fit, function, timeCoordSource,
                    // zeroPoint);
                    //
                    // String extremaStr = finder.toString();
                    //
                    // if (extremaStr != null) {
                    // String title = LocaleProps
                    // .get("MODEL_INFO_EXTREMA_TITLE");
                    //
                    // functionStrMap.put(title, extremaStr);
                    // }

                    // VeLa, Excel, R equations.
                    // TODO: consider Python, e.g. for use with
                    // matplotlib.
                    functionStrMap.put(LocaleProps.get("MODEL_INFO_FUNCTION_TITLE"), toString());

                    functionStrMap.put(LocaleProps.get("MODEL_INFO_EXCEL_TITLE"), toExcelString());

                    functionStrMap.put(LocaleProps.get("MODEL_INFO_R_TITLE"), toRString());

                } catch (ConvergenceException e) {
                    throw new AlgorithmError(e.getLocalizedMessage());
                }
            }
        }

        @Override
        public void interrupt() {
            interrupted = true;
        }

        @Override
        public Map<String, String> getFunctionStrings() {
            return functionStrMap;
        }
    }

    // Test
    // see also TSPolynomialFitterTest (under test directory)

    @Override
    public Boolean test() {
        boolean result = true;

        setTestMode(true);
        needGUI = false;

        result &= testPolynomialFit();

        setTestMode(false);

        return result;
    }

    private boolean testPolynomialFit() {
        boolean result = true;

        List<ValidObservation> obs = getTestObs();

        setDegree(9);

        PolynomialFitCreator model = (PolynomialFitCreator) getModel(obs);

        try {
            model.execute();

            double DELTA = 1e-3;

            List<ValidObservation> fit = model.getFit();
            ValidObservation fitOb = fit.get(0);
            result &= fitOb.getJD() == 2459301.0;
            System.err.println(result);
            result &= Tolerance.areClose(0.629248, fitOb.getMag(), DELTA, true);
            System.err.println(result);
            
            List<ValidObservation> residuals = model.getResiduals();
            ValidObservation resOb = residuals.get(0);
            result &= resOb.getJD() == 2459301.0;
            System.err.println(result);
            result &= Tolerance.areClose(0.000073, resOb.getMag(), DELTA, true);
            System.err.println(result);

            result &= Tolerance.areClose(-7923.218889035116, model.aic, DELTA, true);
            System.err.println(result);
            result &= Tolerance.areClose(-7888.243952752065, model.bic, DELTA, true);
            System.err.println(result);

        } catch (AlgorithmError e) {
            result = false;
        }

        return result;
    }

    private List<ValidObservation> getTestObs() {
        List<ValidObservation> obs = new ArrayList<ValidObservation>();

        for (int t = 1; t <= 360; t++) {
            double time = 2459300 + t;
            double mag = Math.sin(Math.toRadians(time));
            ValidObservation ob = new ValidObservation();
            ob.setDateInfo(new DateInfo(time));
            ob.setMagnitude(new Magnitude(mag, 0));
            obs.add(ob);
        }

        return obs;
    }
}
