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
package org.aavso.tools.vstar.external.lib;

import java.util.ArrayList;
import java.util.List;

import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.exception.AlgorithmError;
import org.aavso.tools.vstar.ui.model.plot.ContinuousModelFunction;
import org.aavso.tools.vstar.ui.model.plot.ICoordSource;
import org.aavso.tools.vstar.util.AbstractExtremaFinder;
import org.aavso.tools.vstar.util.locale.LocaleProps;
import org.aavso.tools.vstar.util.model.AbstractModel;
import org.aavso.tools.vstar.util.prefs.NumericPrecisionPrefs;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.optimization.GoalType;

/**
 * This class represents a piecewise linear model.
 */
public class PiecewiseLinearModel extends AbstractModel {

    private static final String DESC = "Piecewise linear model";

    private PiecewiseLinearFunction piecewiseFunction;

    public PiecewiseLinearModel(List<ValidObservation> obs, List<ValidObservation> meanObs) {
        super(obs);

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

        PiecewiseLinearFunctionExtremaFinder finder = new PiecewiseLinearFunctionExtremaFinder(fit, piecewiseFunction,
                timeCoordSource);

        String extremaStr = finder.toString();

        if (extremaStr != null) {
            String title = LocaleProps.get("MODEL_INFO_EXTREMA_TITLE");

            functionStrMap.put(title, extremaStr);
        }

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
        return getKind() + " with " + piecewiseFunction.numberOfFunctions() + " functions for " + obs.get(0).getBand()
                + " series";
    }

    @Override
    public String getKind() {
        return DESC;
    }

    @Override
    public ContinuousModelFunction getModelFunction() {
        return new ContinuousModelFunction(piecewiseFunction, fit, 0);
    }

    public static class PiecewiseLinearFunction implements UnivariateRealFunction {
        private List<LinearFunction> functions;
        private LinearFunction currFunc;
        private int funIndex;

        public PiecewiseLinearFunction(List<ValidObservation> obs, ICoordSource timeCoordSource) {
            functions = new ArrayList<LinearFunction>();

            for (int i = 0; i < obs.size() - 1; i++) {
                ValidObservation ob1 = obs.get(i);
                ValidObservation ob2 = obs.get(i + 1);
                double t1 = timeCoordSource.getXCoord(i, obs);
                double t2 = timeCoordSource.getXCoord(i + 1, obs);
                functions.add(new LinearFunction(t1, t2, ob1.getMag(), ob2.getMag()));
            }

            currFunc = functions.get(0);
            funIndex = 0;
        }

        @Override
        public double value(double t) {
            if (t > currFunc.endTime() && funIndex < functions.size() - 1) {
                funIndex++;
                currFunc = functions.get(funIndex);
            }

            return currFunc.value(t);
        }

        /**
         * @return The list of functions.
         */
        public List<LinearFunction> getFunctions() {
            return functions;
        }

        /**
         * Find the index of the function to which the target time corresponds.
         * 
         * @param t The target time.
         * @return The index of the function or -1 if t does not correspond to the time
         *         range of any linear function.
         */
        public int seekFunction(double t) {
            int index = -1;

            for (int i = 0; i < functions.size() - 1; i++) {
                LinearFunction linearFunc = functions.get(i);
                if (t >= linearFunc.t1 && t < linearFunc.t2) {
                    index = i;
                    break;
                }
            }

            return index;
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

    /**
     * Represents the function for a line segment
     */
    public static class LinearFunction implements UnivariateRealFunction {

        private double t1;
        private double t2;
        private double mag1;
        private double mag2;
        private double m;
        private double c;

        public LinearFunction(double t1, double t2, double mag1, double mag2) {
            this.t1 = t1;
            this.t2 = t2;
            this.mag1 = mag1;
            this.mag2 = mag2;

            // y = mx + c
            m = slope();
            c = mag1 - m * t1;
        }

        public double startTime() {
            return t1;
        }

        public double endTime() {
            return t2;
        }

        public double startMag() {
            return mag1;
        }

        public double endMag() {
            return mag2;
        }

        public double slope() {
            return (mag2 - mag1) / (t2 - t1);
        }

        public double yIntercept() {
            return c;
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

    public static class PiecewiseLinearFunctionExtremaFinder extends AbstractExtremaFinder {
        PiecewiseLinearFunction plf;

        public PiecewiseLinearFunctionExtremaFinder(List<ValidObservation> obs, PiecewiseLinearFunction function,
                ICoordSource timeCoordSource) {
            super(obs, function, timeCoordSource, 0);
            plf = function;
        }

        @Override
        public void find(GoalType goal, int[] bracketRange) throws AlgorithmError {
            extremeTime = Double.POSITIVE_INFINITY;
            extremeMag = Double.POSITIVE_INFINITY;

            double firstJD = obs.get(bracketRange[0]).getJD();
            double lastJD = obs.get(bracketRange[1]).getJD();

            int firstIndex = plf.seekFunction(firstJD);
            int lastIndex = plf.seekFunction(lastJD);

            // extrema should be at the meeting point of two linear functions
            boolean found = false;

            if (lastIndex == firstIndex + 1) {
                if (goal == GoalType.MINIMIZE && plf.functions.get(firstIndex).slope() < 0
                        && plf.functions.get(lastIndex).slope() > 0) {
                    found = true;
                } else if (goal == GoalType.MAXIMIZE && plf.functions.get(firstIndex).slope() > 0
                        && plf.functions.get(lastIndex).slope() < 0) {
                    found = true;
                }
            }

            if (found) {
                extremeTime = plf.functions.get(lastIndex).t1;
                extremeMag = plf.functions.get(lastIndex).value(extremeTime);
            }
        }
    }
}
