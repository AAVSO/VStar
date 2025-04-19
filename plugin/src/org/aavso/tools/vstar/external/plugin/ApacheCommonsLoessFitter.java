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
package org.aavso.tools.vstar.external.plugin;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.exception.AlgorithmError;
import org.aavso.tools.vstar.plugin.ModelCreatorPluginBase;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.model.plot.ContinuousModelFunction;
import org.aavso.tools.vstar.util.locale.LocaleProps;
import org.aavso.tools.vstar.util.model.AbstractModel;
import org.aavso.tools.vstar.util.prefs.NumericPrecisionPrefs;
import org.apache.commons.math.MathException;
import org.apache.commons.math.analysis.interpolation.LoessInterpolator;
import org.apache.commons.math.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math.analysis.polynomials.PolynomialSplineFunction;

/**
 * A Loess (Local Regression algorithm) model creator plugin that uses an Apache
 * Commons Loess interpolator.
 * 
 * See https://www.aavso.org/sites/default/files/Cleveland1979%20LOESS_0.pdf
 * (thanks to Brad Walter for pointing me to this)
 */
public class ApacheCommonsLoessFitter extends ModelCreatorPluginBase {

    public ApacheCommonsLoessFitter() {
        super();
    }

    @Override
    public String getDescription() {
        return "Loess Fit";
    }

    @Override
    public String getDisplayName() {
        return getDescription();
    }

    /**
     * @see org.aavso.tools.vstar.plugin.IPlugin#getDocName()
     */
    @Override
    public String getDocName() {
        return "ApacheCommonsLoessFilter.pdf";
    }

    @Override
    public AbstractModel getModel(List<ValidObservation> obs) {
        return new LoessFitCreator(obs);
    }

    class LoessFitCreator extends AbstractModel {

        LoessFitCreator(List<ValidObservation> obs) {
            super(obs);
        }

        PolynomialSplineFunction function;

        @Override
        public String getDescription() {
            return getKind() + " for " + obs.get(0).getBand() + " series";
        }

        @Override
        public String getKind() {
            return "Loess Fit";
        }

        @Override
        public boolean hasFuncDesc() {
            return true;
        }

        @Override
        public String toString() {
            return toVeLaString();
        }

        @Override
        public String toVeLaString() {
            String strRepr = functionStrMap.get(LocaleProps.get("MODEL_INFO_FUNCTION_TITLE"));

            double[] knots = function.getKnots();
            PolynomialFunction[] polyFuncs = function.getPolynomials();

            if (strRepr == null) {
                StringBuffer buf = new StringBuffer();

                buf.append("# List of knots\n");
                buf.append("knots is [ ");
                for (int i = 0; i < knots.length; i++) {
                    String knot = NumericPrecisionPrefs.formatTimeLocaleIndependent(knots[i]);
                    buf.append(knot);
                    if (i % 10 == 0) {
                        buf.append("\n   ");
                    } else {
                        buf.append(" ");
                    }
                }
                buf.append("]\n\n");

                buf.append("# List of knot to magnitude functions.\n");
                buf.append("    mag_funcs is [\n");

                for (int i = 0; i < knots.length - 1; i++) {
                    String y = "*(x)";
                    String polyFunc = polyFuncs[i].toString().replace(" x", y);
                    buf.append("        λ(x:real):real{");
                    buf.append(polyFunc);
                    buf.append("}\n");
                }

                buf.append("    ]\n\n");

                buf.append("# Find the lower index of the knot range within which\n");
                buf.append("# the supplied time value (x) lies.\n");
                buf.append("findknot(x:real knots:list) : integer {\n");
                buf.append("    last_index is length(knots)-1\n\n");

                buf.append("    if x < nth(knots 0) then {\n");
                buf.append("        index is 0\n");
                buf.append("    } else if x >= nth(knots last_index) then {\n");
                buf.append("        index is last_index - 1\n");
                buf.append("    } else {\n");
                buf.append("       index is pairwisefind(λ(x1:real x2:real):boolean {x >= x1 and x < x2} knots 1)\n");
                buf.append("    }\n\n");

                buf.append("    index\n");
                buf.append("  }\n\n");

                buf.append("f(t:real) : real {\n");
                buf.append("    # Find the index of the knot.\n");
                buf.append("    index is findknot(t knots)\n\n");

                buf.append("    # Compute the magnitude value using the function\n");
                buf.append("    # corresponding to the knot.\n");
                buf.append("    mag_func is nth(mag_funcs index)\n\n");

                buf.append("    # Return the computed magnitude value.\n");
                buf.append("    mag_func(t - nth(knots index))\n");
                buf.append("}\n");

                strRepr = buf.toString();
            }

            return strRepr;
        }

        public String toExcelString() {
            String strRepr = functionStrMap.get(LocaleProps.get("MODEL_INFO_EXCEL_TITLE"));

            if (strRepr == null) {
                strRepr = Mediator.NOT_IMPLEMENTED_YET;
            }

            return strRepr;
        }

        // Note: There is already a Loess fit function in R, so it
        // would be interesting to compare the results of that and this
        // plugin.
        // toRString must be locale-independent!
        public String toRString() {
            String strRepr = functionStrMap.get(LocaleProps.get("MODEL_INFO_R_TITLE"));

            if (strRepr == null) {
                strRepr = Mediator.NOT_IMPLEMENTED_YET;
            }

            return strRepr;
        }

        @Override
        public ContinuousModelFunction getModelFunction() {
            return new ContinuousModelFunction(function, fit);
        }

        @Override
        public void execute() throws AlgorithmError {

            // The Loess fitter requires a strictly increasing sequence
            // on the domain (e.g. JD, phase values), i.e. no duplicates.
            Map<Double, Double> timeToMagMap = new TreeMap<Double, Double>();

            for (int i = 0; i < obs.size(); i++) {
                ValidObservation ob = obs.get(i);
                timeToMagMap.put(timeCoordSource.getXCoord(i, obs), ob.getMag());
            }

            double[] xvals = new double[timeToMagMap.size()];
            double[] yvals = new double[timeToMagMap.size()];

            int index = 0;
            for (Double time : timeToMagMap.keySet()) {
                xvals[index] = time;
                yvals[index++] = timeToMagMap.get(time);
            }

            try {
                final LoessInterpolator interpolator = new LoessInterpolator();
                function = interpolator.interpolate(xvals, yvals);

                String comment = "From Loess fit";

                for (int i = 0; i < obs.size() && !interrupted; i++) {
                    ValidObservation ob = obs.get(i);

                    double x = timeCoordSource.getXCoord(i, obs);
                    double y = function.value(x);

                    collectObs(y, ob, comment);
                }

                // Get the maximum degree value of all polynomials for
                // goodness of fit. I suspect this will always be 3 if
                // the Apache implementation is the classical one. Best
                // not to make that assumption though. From the viewpoint of
                // goodness-of-fit purposes, a fixed degree value seems
                // unsatisfactory since the BIC and AIC will never change.
                // On the other hand, compared with a piecewise linear model,
                // where the number of piecewise functions differs with the
                // number of binned means, a clear visual improvement can be
                // seen, so the number of functions seems like a better value
                // to use for degrees there. But what about loess? Knots,
                // polynomial functions as a proxy for degrees?
                //
                // References
                // ----------
                // https://people.computing.clemson.edu/~dhouse/courses/405/notes/splines.pdf
                // https://en.wikipedia.org/wiki/Spline_interpolation
                double degree = Arrays.asList(function.getPolynomials()).stream()
                        .mapToDouble(p -> p.degree())
                        .max()
                        .getAsDouble();

                rootMeanSquare();
                informationCriteria(degree);
                fitMetrics();

                // Minimum/maximum.
                // TODO: use derivative approach
                // ApacheCommonsBrentOptimiserExtremaFinder finder = new
                // ApacheCommonsBrentOptimiserExtremaFinder(
                // fit, function, timeCoordSource, 0);
                //
                // String extremaStr = finder.toString();
                //
                // if (extremaStr != null) {
                // String title = LocaleProps
                // .get("MODEL_INFO_EXTREMA_TITLE");
                //
                // functionStrMap.put(title, extremaStr);
                // }

                // Excel, R equations.
                // TODO: consider Python, e.g. for use with matplotlib.
                functionStrings();
//                functionStrMap.put(LocaleProps.get("MODEL_INFO_R_TITLE"), toRString());
            } catch (MathException e) {
                throw new AlgorithmError(e.getLocalizedMessage());
            }
        }
    }

    // Plug-in test

    double[][] test_data = { { 47551.614000, 9.30710 }, { 47560.675000, 9.34120 }, { 47568.928000, 9.44000 },
            { 47580.007200, 9.39380 }, { 47599.200100, 9.38330 }, { 47609.626200, 9.44620 }, { 47630.002000, 9.26000 },
            { 47638.450000, 9.27500 }, { 47650.587000, 8.87000 }, { 47662.466700, 8.86670 }, { 47671.247800, 8.65000 },
            { 47680.044400, 8.43330 }, { 47691.020000, 8.06000 }, { 47698.759600, 8.05560 }, { 47710.560700, 8.13570 },
            { 47719.444600, 8.27500 }, { 47730.007800, 8.40710 }, { 47741.224600, 8.57220 }, { 47749.907000, 8.92000 },
            { 47761.632300, 9.35620 } };

    @Override
    public Boolean test() {
        boolean result = true;

        return result;
    }

}
