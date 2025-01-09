/**
 * VStar: a statistical analysis tool for variable star data.
 * Copyright (C) 2009  AAVSO (http://www.aavso.org/)
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
package org.aavso.tools.vstar.external.plugin;

import java.util.ArrayList;
import java.util.List;

import org.aavso.tools.vstar.data.DateInfo;
import org.aavso.tools.vstar.data.Magnitude;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.exception.AlgorithmError;
import org.aavso.tools.vstar.plugin.ModelCreatorPluginBase;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.model.plot.ContinuousModelFunction;
import org.aavso.tools.vstar.ui.model.plot.ICoordSource;
import org.aavso.tools.vstar.ui.model.plot.JDCoordSource;
import org.aavso.tools.vstar.ui.model.plot.ObservationAndMeanPlotModel;
import org.aavso.tools.vstar.util.Tolerance;
import org.aavso.tools.vstar.util.locale.LocaleProps;
import org.aavso.tools.vstar.util.model.AbstractModel;
import org.aavso.tools.vstar.util.prefs.NumericPrecisionPrefs;
import org.apache.commons.math.analysis.DifferentiableUnivariateRealFunction;
import org.apache.commons.math.analysis.UnivariateRealFunction;

/**
 * This plug-in creates a piecewise linear model from the current means series.
 * 
 * TODO<br/>
 * - f'(x) = m, f''(x) = 0
 * - change to set mean series rather than retrieved from Mediator, e.g. via
 * setParams() for AoV plug-in; same for timeCoordSource (e.g. for AoV) => could
 * default to current mode means<br/>
 * - disable AoV model button until selection of result plus phase plot
 * mode<br/>
 */
public class PiecewiseLinearMeanSeriesModel extends ModelCreatorPluginBase {

    private final String DESC = "Piecewise linear model from Means";

    public PiecewiseLinearMeanSeriesModel() {
        super();
    }

    @Override
    public AbstractModel getModel(List<ValidObservation> obs) {
        return new PiecewiseLinearModel(obs);
    }

    @Override
    public String getDescription() {
        return DESC;
    }

    @Override
    public String getDisplayName() {
        return DESC;
    }

    /**
     * Represents the function for a line segment
     */
    class LinearFunction implements UnivariateRealFunction {

        private double t1;
        private double t2;
        private double mag1;
        private double mag2;
        private double m;
        private double c;

        LinearFunction(double t1, double t2, double mag1, double mag2) {
            this.t1 = t1;
            this.t2 = t2;
            this.mag1 = mag1;
            this.mag2 = mag2;

            // y = mx + c
            m = slope();
            c = mag1 - m * t1;
        }

        double endTime() {
            return t2;
        }

        double slope() {
            return (mag2 - mag1) / (t2 - t1);
        }

        @Override
        public double value(double t) {
            return m * t + c;
        }

        public String toVeLaString(boolean first, boolean last) {
            // For the first line segment, we only need to check
            // the second bound. For the last line segment, we don't
            // need to check either bound.
            StringBuffer buf = new StringBuffer();

            if (!first && !last) {
                buf.append("t >= ");
                buf.append(NumericPrecisionPrefs.formatTimeLocaleIndependent(t1));
                buf.append(" and ");
            }

            if (!last) {
                buf.append("t < ");
                buf.append(NumericPrecisionPrefs.formatTimeLocaleIndependent(t2));
            } else {
                buf.append("true ");
            }

            buf.append(" -> ");

            buf.append(NumericPrecisionPrefs.formatOtherLocaleIndependent(m));
            buf.append("*t + ");
            buf.append(NumericPrecisionPrefs.formatOtherLocaleIndependent(c));

            if (!last)
                buf.append("\n");

            return buf.toString();
        }
    }

    // Note: currently unused since extrema determination not implemented
    // TODO: global minimum and maximum can actually be determined via mags
    // at t2 of one function, t1 of second where slope of 2 functions changes
    class LinearFunctionDerivative implements UnivariateRealFunction {

        private LinearFunction function;

        LinearFunctionDerivative(LinearFunction function) {
            this.function = function;
        }

        @Override
        public double value(double t) {
            // the slope is the same everywhere along a line segment
            return function.slope();
        }
    }

    class PiecewiseLinearFunction implements DifferentiableUnivariateRealFunction {
        private List<LinearFunction> functions;
        private int funIndex;

        PiecewiseLinearFunction(List<ValidObservation> obs, ICoordSource timeCoordSource) {
            functions = new ArrayList<LinearFunction>();
            funIndex = 0;

            for (int i = 0; i < obs.size() - 1; i++) {
                ValidObservation ob1 = obs.get(i);
                ValidObservation ob2 = obs.get(i + 1);
                double t1 = timeCoordSource.getXCoord(i, obs);
                double t2 = timeCoordSource.getXCoord(i + 1, obs);
                functions.add(new LinearFunction(t1, t2, ob1.getMag(), ob2.getMag()));
            }
        }

        @Override
        public double value(double t) {
            LinearFunction func = functions.get(funIndex);

            if (t > func.endTime() && funIndex < functions.size() - 1) {
                funIndex++;
                func = functions.get(funIndex);
            }

            return func.value(t);
        }

        @Override
        public UnivariateRealFunction derivative() {
            // Also needs to be differentiable to get 2nd derivative
            return null;
        }

        public int numberOfFunctions() {
            return functions.size();
        }

        public String toVeLaString() {
            String strRepr = "";

            strRepr += "f(t:real) : real {\n";
            strRepr += "    when\n";
            for (int i = 0; i < functions.size(); i++) {
                LinearFunction function = functions.get(i);
                boolean first = i == 0;
                boolean last = i == functions.size() - 1;
                strRepr += "        " + function.toVeLaString(first, last);
            }
            strRepr += "\n}";

            return strRepr;
        }
    }

    class PiecewiseLinearModel extends AbstractModel {
        private List<ValidObservation> meanObs;
        private PiecewiseLinearFunction piecewiseFunction;

        PiecewiseLinearModel(List<ValidObservation> obs) {
            super(obs);

            // Get the mean observation list for the current mode
            Mediator mediator = Mediator.getInstance();
            ObservationAndMeanPlotModel plotModel = mediator.getObservationPlotModel(mediator.getAnalysisType());
            meanObs = plotModel.getMeanObsList();

            // Create piecewise linear model
            piecewiseFunction = new PiecewiseLinearFunction(meanObs, timeCoordSource);
        }

        @Override
        public void execute() throws AlgorithmError {

            fit = new ArrayList<ValidObservation>();
            residuals = new ArrayList<ValidObservation>();

            String comment = DESC;

            for (int i = 0; i < obs.size() && !interrupted; i++) {
                ValidObservation ob = obs.get(i);

                double x = timeCoordSource.getXCoord(i, obs);
                double y = piecewiseFunction.value(x);

                collectObs(y, ob, comment);
            }

            rootMeanSquare();
            informationCriteria(piecewiseFunction.numberOfFunctions());
            fitMetrics();
            functionStrings();
        }

        @Override
        public boolean hasFuncDesc() {
            return true;
        }

        public String toVeLaString() {
            String strRepr = functionStrMap.get(LocaleProps.get("MODEL_INFO_FUNCTION_TITLE"));

            if (strRepr == null) {
                strRepr = piecewiseFunction.toVeLaString();
            }

            return strRepr;
        }

        @Override
        public String getDescription() {
            return DESC;
        }

        @Override
        public String getKind() {
            return "Piecewise linear model";
        }

        @Override
        public ContinuousModelFunction getModelFunction() {
            // TODO: None?
            return null;
        }
    }

    // Test

    @Override
    public Boolean test() {
        boolean result = true;

        result &= testLinearFunction();
        result &= testPiecewiseLinearFunction();

        return result;
    }

    private boolean testLinearFunction() {
        boolean result = true;

        double DELTA = 1e-6;

        LinearFunction function = new LinearFunction(2459645, 2459640, 10, 12.5);

        double m = -0.5;
        result &= Tolerance.areClose(m, function.slope(), DELTA, true);
        result &= Tolerance.areClose(10 - (m * 2459645), function.c, DELTA, true);
        result &= Tolerance.areClose(m * 2459642 + function.c, function.value(2459642), DELTA, true);

        return result;
    }

    private boolean testPiecewiseLinearFunction() {
        boolean result = true;

        double DELTA = 1e-6;

        List<ValidObservation> meanObs = getTestMeanObs();
        PiecewiseLinearFunction plf = new PiecewiseLinearFunction(meanObs, JDCoordSource.instance);

        List<ValidObservation> obs = getTestObs();

        double t1 = obs.get(0).getJD();
        LinearFunction function1 = plf.functions.get(0);
        result &= result &= Tolerance.areClose(function1.m * t1 + function1.c, plf.value(t1), DELTA, true);

        double t2 = obs.get(1).getJD();
        LinearFunction function2 = plf.functions.get(1);
        result &= result &= Tolerance.areClose(function2.m * t2 + function2.c, plf.value(t2), DELTA, true);

        return result;
    }

    private List<ValidObservation> getTestMeanObs() {
        List<ValidObservation> obs = new ArrayList<ValidObservation>();

        ValidObservation ob1 = new ValidObservation();
        ob1.setDateInfo(new DateInfo(2459644));
        ob1.setMagnitude(new Magnitude(4.5, 0));
        obs.add(ob1);

        ValidObservation ob2 = new ValidObservation();
        ob2.setDateInfo(new DateInfo(2459645.5));
        ob2.setMagnitude(new Magnitude(5.5, 0));
        obs.add(ob2);

        ValidObservation ob3 = new ValidObservation();
        ob3.setDateInfo(new DateInfo(22459645.5));
        ob3.setMagnitude(new Magnitude(5.5, 0));
        obs.add(ob3);

        ValidObservation ob4 = new ValidObservation();
        ob4.setDateInfo(new DateInfo(2459647));
        ob4.setMagnitude(new Magnitude(7, 0));
        obs.add(ob4);

        return obs;
    }

    private List<ValidObservation> getTestObs() {
        List<ValidObservation> obs = new ArrayList<ValidObservation>();

        ValidObservation ob1 = new ValidObservation();
        ob1.setDateInfo(new DateInfo(2459645.1134785));
        ob1.setMagnitude(new Magnitude(5, 0));
        obs.add(ob1);

        ValidObservation ob2 = new ValidObservation();
        ob2.setDateInfo(new DateInfo(2459646.2));
        ob2.setMagnitude(new Magnitude(6, 0));
        obs.add(ob2);

        return obs;
    }
}
