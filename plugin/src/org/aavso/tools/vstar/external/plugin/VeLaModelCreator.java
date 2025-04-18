package org.aavso.tools.vstar.external.plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.aavso.tools.vstar.data.DateInfo;
import org.aavso.tools.vstar.data.Magnitude;
import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.exception.AlgorithmError;
import org.aavso.tools.vstar.plugin.ModelCreatorPluginBase;
import org.aavso.tools.vstar.ui.dialog.MessageBox;
import org.aavso.tools.vstar.ui.mediator.AnalysisType;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.model.plot.ContinuousModelFunction;
import org.aavso.tools.vstar.ui.vela.VeLaDialog;
import org.aavso.tools.vstar.util.ApacheCommonsDerivativeBasedExtremaFinder;
import org.aavso.tools.vstar.util.Tolerance;
import org.aavso.tools.vstar.util.locale.LocaleProps;
import org.aavso.tools.vstar.util.model.AbstractModel;
import org.aavso.tools.vstar.util.prefs.NumericPrecisionPrefs;
import org.aavso.tools.vstar.vela.Operand;
import org.aavso.tools.vstar.vela.Type;
import org.aavso.tools.vstar.vela.VeLaInterpreter;
import org.aavso.tools.vstar.vela.VeLaValidObservationEnvironment;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.DifferentiableUnivariateRealFunction;
import org.apache.commons.math.analysis.UnivariateRealFunction;

/**
 * A model creator that allows a VeLa function to be applied to observations.
 */
public class VeLaModelCreator extends ModelCreatorPluginBase {

    private static final String FUNC_NAME = "F";
    private static final String DERIV_FUNC_NAME = "DF";
    private static final String RESOLUTION_VAR = "RESOLUTION";

    private static VeLaDialog velaDialog;

    public VeLaModelCreator() {
        super();
    }

    @Override
    public String getDescription() {
        return "VeLa model creator";
    }

    @Override
    public String getDisplayName() {
        return "VeLa Model";
    }

    /**
     * @see org.aavso.tools.vstar.plugin.IPlugin#getDocName()
     */
    @Override
    public String getDocName() {
        return "VeLa Model Creator Plug-In.pdf";
    }

    @Override
    public AbstractModel getModel(List<ValidObservation> obs) {
        return new VeLaModel(obs);
    }

    class VeLaUnivariateRealFunction implements DifferentiableUnivariateRealFunction {

        private VeLaInterpreter vela;
        private String funcName;

        public VeLaUnivariateRealFunction(VeLaInterpreter vela, String funcName) {
            this.vela = vela;
            this.funcName = funcName;
        }

        /**
         * Return the value of the model function or its derivative.
         * 
         * @param t The time value.
         * @return The model value at time t.
         * @throws FunctionEvaluationException If there is an error during function
         *                                     evaluation.
         */
        @Override
        public double value(double t) throws FunctionEvaluationException {
            String funCall = funcName + "(" + NumericPrecisionPrefs.formatTime(t) + ")";
            Optional<Operand> result = vela.program(funCall);
            if (result.isPresent()) {
                return result.get().doubleVal();
            } else {
                throw new FunctionEvaluationException(t);
            }
        }

        /**
         * If the derivative (df) function doesn't exist, this will never be called
         * since we will bypass extrema determination.
         */
        @Override
        public UnivariateRealFunction derivative() {
            return new VeLaUnivariateRealFunction(vela, DERIV_FUNC_NAME);
        }
    }

    class VeLaModel extends AbstractModel {
        double zeroPoint;
        UnivariateRealFunction function;
        VeLaInterpreter vela;
        String velaModelFunctionStr;
        String modelName;

        VeLaModel(List<ValidObservation> obs) {
            super(obs);

            // Create a VeLa interpreter instance.
            vela = new VeLaInterpreter();

            // Select time mode (JD or phase).
            switch (Mediator.getInstance().getAnalysisType()) {
            case RAW_DATA:
                zeroPoint = 0;
                List<Operand> jdList = obs.stream().map(ob -> new Operand(Type.REAL, ob.getJD()))
                        .collect(Collectors.toList());
                vela.bind("TIMES", new Operand(Type.LIST, jdList), true);
                break;

            case PHASE_PLOT:
                Collections.sort(this.obs, timeComparator);
                zeroPoint = 0;
                List<Operand> phaseList = this.obs.stream().map(ob -> new Operand(Type.REAL, ob.getStandardPhase()))
                        .collect(Collectors.toList());
                vela.bind("TIMES", new Operand(Type.LIST, phaseList), true);
                break;
            }

            List<Operand> magList = this.obs.stream().map(ob -> new Operand(Type.REAL, ob.getMag()))
                    .collect(Collectors.toList());
            Operand mags = new Operand(Type.LIST, magList);
            vela.bind("MAGS", mags, true);

            String modelFuncStr = null;
            String modelNameStr = null;

            if (inTestMode()) {
                modelFuncStr = getTestModelFunc();
                modelNameStr = getTestModelName();
            } else {
                if (velaDialog == null) {
                    velaDialog = new VeLaDialog("Function Code [model: f(t), optional derivative: df(t)]");
                } else {
                    velaDialog.showDialog();
                }

                if (!velaDialog.isCancelled()) {
                    modelFuncStr = velaDialog.getCode();
                    modelNameStr = velaDialog.getPath();
                }
            }

            velaModelFunctionStr = modelFuncStr;
            modelName = modelNameStr;
        }

        @Override
        public String getDescription() {
            return modelName + " applied to " + obs.get(0).getBand() + " series";
        }

        @Override
        public String getKind() {
            return "VeLa Model";
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
            return velaModelFunctionStr;
        }

        @Override
        public ContinuousModelFunction getModelFunction() {
            return new ContinuousModelFunction(function, fit, zeroPoint);
        }

        @Override
        public void execute() throws AlgorithmError {
            if (!interrupted) {
                try {
                    if (velaModelFunctionStr != null) {
                        // Evaluate the VeLa model code.
                        // A univariate function f(t:real):real is
                        // assumed to exist after this completes.
                        vela.program(velaModelFunctionStr);

                        String funcName = FUNC_NAME;

                        // Has a model function been defined?
                        if (!vela.lookupFunctions(FUNC_NAME).isPresent()) {
                            MessageBox.showErrorDialog("VeLa Model Error", "f(t:real):real undefined");
                        } else {
                            function = new VeLaUnivariateRealFunction(vela, funcName);

                            fit = new ArrayList<ValidObservation>();
                            residuals = new ArrayList<ValidObservation>();

                            String comment = "\n" + velaModelFunctionStr;

                            // Create fit and residual observations.
                            for (int i = 0; i < obs.size() && !interrupted; i++) {
                                ValidObservation ob = obs.get(i);

                                // Push an environment that makes the
                                // observation available to VeLa code.
                                vela.pushEnvironment(new VeLaValidObservationEnvironment(ob));

                                double x = timeCoordSource.getXCoord(i, obs);

                                // double zeroedX = x - zeroPoint;
                                double y = function.value(x);

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

                                // Pop the observation environment.
                                vela.popEnvironment();
                            }

                            functionStrMap.put(LocaleProps.get("MODEL_INFO_FUNCTION_TITLE"), toString());

                            // Has a derivative function been defined?
                            // If so, carry out extrema determination.
                            if (vela.lookupFunctions(DERIV_FUNC_NAME).isPresent()) {
                                // Use a real VeLa resolution variable
                                // if it exists, else use a value of
                                // 0.1.
                                double resolution = 0.1;
                                Optional<Operand> resVar = vela.lookupBinding(RESOLUTION_VAR);
                                if (resVar.isPresent()) {
                                    switch (resVar.get().getType()) {
                                    case REAL:
                                        resolution = resVar.get().doubleVal();
                                        break;
                                    case INTEGER:
                                        resolution = resVar.get().intVal();
                                        break;
                                    default:
                                        MessageBox.showErrorDialog("VeLa Model Error", "Resolution must be numeric");
                                        break;
                                    }
                                }

                                ApacheCommonsDerivativeBasedExtremaFinder finder = new ApacheCommonsDerivativeBasedExtremaFinder(
                                        fit, (DifferentiableUnivariateRealFunction) function, timeCoordSource,
                                        zeroPoint, resolution);

                                String extremaStr = finder.toString();

                                if (extremaStr != null) {
                                    String title = LocaleProps.get("MODEL_INFO_EXTREMA_TITLE");

                                    functionStrMap.put(title, extremaStr);
                                }
                            }
                        }
                    }
                } catch (FunctionEvaluationException e) {
                    throw new AlgorithmError(e.getLocalizedMessage());
                }
            }
        }
    }

    // Plug-in test

    @Override
    public Boolean test() {
        boolean success = true;

        setTestMode(true);

        try {
            AbstractModel model = getModel(createObs());
            model.execute();
            success &= model.hasFuncDesc();
            String desc = getTestModelName() + " applied to Visual series";
            success &= model.getDescription().equals(desc);
            success &= !model.getFit().isEmpty();
            success &= !model.getResiduals().isEmpty();
            success &= model.getFit().size() == model.getResiduals().size();
            success &= Tolerance.areClose(12.34620932, model.getFit().get(0).getMag(), 1e-6, true);
        } catch (Exception e) {
            success = false;
        }

        setTestMode(false);

        return success;
    }

    private String getTestModelFunc() {
        String func = "";

        func += "f(t:real) : real {\n";
        func += "  11.7340392\n";
        func += "  -0.6588158 * cos(2*PI*0.0017177*(t-2451700))\n";
        func += "  +1.3908874 * sin(2*PI*0.0017177*(t-2451700))";
        func += "}\n";

        return func;
    }

    private String getTestModelName() {
        return "test model";
    }

    private List<ValidObservation> createObs() {
        List<ValidObservation> obs = new ArrayList<ValidObservation>();

        ValidObservation ob = new ValidObservation();
        ob.setMagnitude(new Magnitude(11, 0.1));
        ob.setDateInfo(new DateInfo(2447121.5));
        ob.setBand(SeriesType.Visual);
        ob.setObsCode("ABC");
        obs.add(ob);

        ob = new ValidObservation();
        ob.setMagnitude(new Magnitude(11.05, 0.02));
        ob.setDateInfo(new DateInfo(2447121.501));
        ob.setBand(SeriesType.Johnson_V);
        ob.setObsCode("XYZ");
        obs.add(ob);

        return obs;
    }
}
