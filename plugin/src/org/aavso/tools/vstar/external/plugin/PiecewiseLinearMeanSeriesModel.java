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
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.aavso.tools.vstar.data.DateInfo;
import org.aavso.tools.vstar.data.Magnitude;
import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.exception.AlgorithmError;
import org.aavso.tools.vstar.plugin.ModelCreatorPluginBase;
import org.aavso.tools.vstar.ui.mediator.AnalysisType;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.model.plot.ContinuousModelFunction;
import org.aavso.tools.vstar.ui.model.plot.ICoordSource;
import org.aavso.tools.vstar.ui.model.plot.JDCoordSource;
import org.aavso.tools.vstar.ui.model.plot.ObservationAndMeanPlotModel;
import org.aavso.tools.vstar.ui.model.plot.StandardPhaseCoordSource;
import org.aavso.tools.vstar.util.comparator.JDComparator;
import org.aavso.tools.vstar.util.comparator.StandardPhaseComparator;
import org.aavso.tools.vstar.util.model.IModel;
import org.aavso.tools.vstar.util.model.PeriodFitParameters;
import org.apache.commons.math.analysis.DifferentiableUnivariateRealFunction;
import org.apache.commons.math.analysis.UnivariateRealFunction;

/**
 * This plug-in creates a piecewise linear model from the current means series.
 * 
 * TODO: - add base class function to request for obs of particular series vs
 * ask whether to open series dialog - change to set mean series rather than
 * retrieved from Mediator, e.g. via setParams() for AoV plug-in; same for
 * timeCoordSource (e.g. for AoV) => could default to current mode means -
 * change PiecewiseLinearFunction to set currLinearFunc, currLinearFuncDeriv as
 * part of t > ... check - Disable model button until selection of result plus
 * phase plot mode - derivative - extrema, ctor: reuse - function strings - fit
 * goodness, e.g. RMS, AIC, BIC and refactoring
 */
public class PiecewiseLinearMeanSeriesModel extends ModelCreatorPluginBase {

    private final String DESC = "Piecewise linear model from Means";

    private ICoordSource timeCoordSource;
    private Comparator<ValidObservation> timeComparator;

    public PiecewiseLinearMeanSeriesModel() {
        super();
    }

    @Override
    public IModel getModel(List<ValidObservation> obs) {
        return new PiecewiseLinearModelCreator(obs);
    }

    @Override
    public String getDescription() {
        return DESC;
    }

    @Override
    public String getDisplayName() {
        return DESC;
    }

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
    }

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

            // TODO: string methods here and in LinearFunction for VeLa, R, ...
        }

        @Override
        public double value(double t) {
            LinearFunction func = functions.get(funIndex);

            // TODO: record and check against last time?
            if (t > func.endTime() && funIndex < functions.size() - 1) {
                funIndex++;
                func = functions.get(funIndex);
            }

            return func.value(t);
        }

        @Override
        public UnivariateRealFunction derivative() {
            // TODO
            return null;
        }
    }

    class PiecewiseLinearModelCreator implements IModel {
        private List<ValidObservation> obs;
        private List<ValidObservation> meanObs;
        private boolean interrupted;
        private List<ValidObservation> fit;
        private List<ValidObservation> residuals;
        private Map<String, String> functionStrMap;
        private PiecewiseLinearFunction piecewiseFunction;

        PiecewiseLinearModelCreator(List<ValidObservation> obs) {
            // TODO: some code (e.g. the switch) in this block should
            // be refactored into the model creator base class or elsewhere
            // (see also PolynomialFitCreator)

            // Select time mode (JD or phase).
            switch (Mediator.getInstance().getAnalysisType()) {
            case RAW_DATA:
                timeCoordSource = JDCoordSource.instance;
                timeComparator = JDComparator.instance;
                this.obs = obs;
                break;

            case PHASE_PLOT:
                timeCoordSource = StandardPhaseCoordSource.instance;
                timeComparator = StandardPhaseComparator.instance;
                this.obs = new ArrayList<ValidObservation>(obs);
                Collections.sort(this.obs, timeComparator);
                break;
            }

            // Get the mean observation list for the current mode
            Mediator mediator = Mediator.getInstance();
            ObservationAndMeanPlotModel plotModel = mediator.getObservationPlotModel(mediator.getAnalysisType());
            meanObs = plotModel.getMeanObsList();

            interrupted = false;
            fit = new ArrayList<ValidObservation>();
            residuals = new ArrayList<ValidObservation>();
            functionStrMap = new TreeMap<String, String>();

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

                // TODO: need a base class method to collect fit & residual obs

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
            }
        }

        @Override
        public void interrupt() {
            interrupted = true;
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
        public List<PeriodFitParameters> getParameters() {
            // None
            return null;
        }

        @Override
        public boolean hasFuncDesc() {
            return false;
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
        public Map<String, String> getFunctionStrings() {
            functionStrMap = new LinkedHashMap<String, String>();
            // TODO VeLa, R, Excel, Python, Julia, LaTeX, ...
            return null;
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

        LinearFunction function = new LinearFunction(2459645, 2459640, 10, 12.5);

        double m = -0.5;
        result &= function.slope() == m;
        result &= function.c == 10 - (m * 2459645);
//        result &= function.derivative(2459645) == m;
//        result &= function.derivative(2459640) == m;
        result &= function.value(2459642) == m * 2459642 + function.c;

        return result;
    }

    private boolean testPiecewiseLinearFunction() {
        boolean result = true;

        List<ValidObservation> meanObs = getTestMeanObs();
        PiecewiseLinearFunction plf = new PiecewiseLinearFunction(meanObs, JDCoordSource.instance);

        List<ValidObservation> obs = getTestObs();

        double t1 = obs.get(0).getJD();
        LinearFunction function1 = plf.functions.get(0);
        result &= plf.value(t1) == function1.m * t1 + function1.c;

        double t2 = obs.get(1).getJD();
        LinearFunction function2 = plf.functions.get(1);
        result &= plf.value(t2) == function2.m * t2 + function2.c;

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
