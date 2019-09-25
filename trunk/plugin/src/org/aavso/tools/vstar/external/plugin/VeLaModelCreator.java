package org.aavso.tools.vstar.external.plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.aavso.tools.vstar.data.DateInfo;
import org.aavso.tools.vstar.data.Magnitude;
import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.exception.AlgorithmError;
import org.aavso.tools.vstar.plugin.ModelCreatorPluginBase;
import org.aavso.tools.vstar.ui.dialog.vela.VeLaDialog;
import org.aavso.tools.vstar.ui.mediator.AnalysisType;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.model.plot.ContinuousModelFunction;
import org.aavso.tools.vstar.ui.model.plot.ICoordSource;
import org.aavso.tools.vstar.ui.model.plot.JDCoordSource;
import org.aavso.tools.vstar.ui.model.plot.StandardPhaseCoordSource;
import org.aavso.tools.vstar.util.ApacheCommonsDerivativeBasedExtremaFinder;
import org.aavso.tools.vstar.util.comparator.JDComparator;
import org.aavso.tools.vstar.util.comparator.StandardPhaseComparator;
import org.aavso.tools.vstar.util.locale.LocaleProps;
import org.aavso.tools.vstar.util.model.IModel;
import org.aavso.tools.vstar.util.model.PeriodFitParameters;
import org.aavso.tools.vstar.vela.FunctionExecutor;
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

	private static VeLaDialog velaDialog;

	private ICoordSource timeCoordSource;
	private Comparator<ValidObservation> timeComparator;

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

	@Override
	public IModel getModel(List<ValidObservation> obs) {
		VeLaModel velaModel = new VeLaModel(obs);
		return velaModel.createModel();
	}

	class VeLaUnivariateRealFunction implements
			DifferentiableUnivariateRealFunction {

		private VeLaInterpreter vela;
		private String funcName;

		public VeLaUnivariateRealFunction(VeLaInterpreter vela, String funcName) {
			this.vela = vela;
			this.funcName = funcName;
		}

		/**
		 * Return the value of the model function or its derivative.
		 * 
		 * @param t
		 *            The time value.
		 * @return The model value at time t.
		 * @throws FunctionEvaluationException
		 *             If there is an error during function evaluation.
		 */
		@Override
		public double value(double t) throws FunctionEvaluationException {
			String funCall = funcName + "(" + t + ")";
			Optional<Operand> result = vela.program(funCall);
			if (result.isPresent()) {
				return result.get().doubleVal();
			} else {
				throw new FunctionEvaluationException(t);
			}
		}

		/**
		 * If the derivative (df) function doesn't exist, this will never be
		 * called since we will bypass extrema determination.
		 */
		@Override
		public UnivariateRealFunction derivative() {
			return new VeLaUnivariateRealFunction(vela, DERIV_FUNC_NAME);
		}
	}

	class VeLaModel {
		private List<ValidObservation> obs;
		private double zeroPoint;
		private VeLaInterpreter vela;

		VeLaModel(List<ValidObservation> obs) {
			// Create a VeLa interpreter instance.
			vela = new VeLaInterpreter();

			// Select time mode (JD or phase).
			switch (Mediator.getInstance().getAnalysisType()) {
			case RAW_DATA:
				timeCoordSource = JDCoordSource.instance;
				timeComparator = JDComparator.instance;
				this.obs = obs;
				// zeroPoint = DescStats.calcTimeElementMean(obs,
				// JDTimeElementEntity.instance);
				zeroPoint = 0;
				List<Operand> jdList = obs.stream()
						.map(ob -> new Operand(Type.REAL, ob.getJD()))
						.collect(Collectors.toList());
				vela.bind("TIMES", new Operand(Type.LIST, jdList), true);
				break;

			case PHASE_PLOT:
				timeCoordSource = StandardPhaseCoordSource.instance;
				timeComparator = StandardPhaseComparator.instance;
				this.obs = new ArrayList<ValidObservation>(obs);
				Collections.sort(this.obs, timeComparator);
				zeroPoint = 0;
				List<Operand> phaseList = this.obs
						.stream()
						.map(ob -> new Operand(Type.REAL, ob.getStandardPhase()))
						.collect(Collectors.toList());
				vela.bind("TIMES", new Operand(Type.LIST, phaseList), true);
				break;
			}

			List<Operand> magList = this.obs.stream()
					.map(ob -> new Operand(Type.REAL, ob.getMag()))
					.collect(Collectors.toList());
			Operand mags = new Operand(Type.LIST, magList);
			vela.bind("MAGS", mags, true);
		}

		// Create a VeLa model.
		IModel createModel() {
			IModel model = null;

			if (velaDialog == null) {

				velaDialog = new VeLaDialog(
						"Function Code [model function is assumed to be f(t)]");

			} else {
				velaDialog.showDialog();
			}

			if (!velaDialog.isCancelled()) {
				String velaModelFunctionStr = velaDialog.getCode();
				// double resolution = resolutionField.getValue();

				model = new IModel() {
					boolean interrupted = false;
					List<ValidObservation> fit;
					List<ValidObservation> residuals;
					UnivariateRealFunction function;
					Map<String, String> functionStrMap = new LinkedHashMap<String, String>();

					@Override
					public String getDescription() {
						return velaDialog.getPath() + " applied to "
								+ obs.get(0).getBand() + " series";
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
						return "VeLa Model";
					}

					@Override
					public List<PeriodFitParameters> getParameters() {
						// None for a VeLa model.
						return null;
					}

					@Override
					public boolean hasFuncDesc() {
						return true;
					}

					@Override
					public String toString() {
						return velaModelFunctionStr;
					}

					@Override
					public ContinuousModelFunction getModelFunction() {
						return new ContinuousModelFunction(function, fit,
								zeroPoint);
					}

					@Override
					public void execute() throws AlgorithmError {
						if (!interrupted) {
							try {
								// Evaluate the VeLa model code.
								// A univariate function f(t:real):real is
								// assumed to exist after this completes.
								vela.program(velaModelFunctionStr);

								// Create an arity 1 real function instance.
								// TODO: actually needs to be a function that returns a function
								String funcName = FUNC_NAME;

								function = new VeLaUnivariateRealFunction(vela,
										funcName);

								fit = new ArrayList<ValidObservation>();
								residuals = new ArrayList<ValidObservation>();

								String comment = velaModelFunctionStr;

								// Create fit and residual observations.
								for (int i = 0; i < obs.size() && !interrupted; i++) {
									ValidObservation ob = obs.get(i);

									// Push an environment that makes the
									// observation available to VeLa code.
									vela.pushEnvironment(new VeLaValidObservationEnvironment(
											ob));

									double x = timeCoordSource
											.getXCoord(i, obs);

									// double zeroedX = x - zeroPoint;
									double y = function.value(x);

									ValidObservation fitOb = new ValidObservation();
									fitOb.setDateInfo(new DateInfo(ob.getJD()));
									if (Mediator.getInstance()
											.getAnalysisType() == AnalysisType.PHASE_PLOT) {
										fitOb.setPreviousCyclePhase(ob
												.getPreviousCyclePhase());
										fitOb.setStandardPhase(ob
												.getStandardPhase());
									}
									fitOb.setMagnitude(new Magnitude(y, 0));
									fitOb.setBand(SeriesType.Model);
									fitOb.setComments(comment);
									fit.add(fitOb);

									ValidObservation resOb = new ValidObservation();
									resOb.setDateInfo(new DateInfo(ob.getJD()));
									if (Mediator.getInstance()
											.getAnalysisType() == AnalysisType.PHASE_PLOT) {
										resOb.setPreviousCyclePhase(ob
												.getPreviousCyclePhase());
										resOb.setStandardPhase(ob
												.getStandardPhase());
									}
									double residual = ob.getMag() - y;
									resOb.setMagnitude(new Magnitude(residual,
											0));
									resOb.setBand(SeriesType.Residuals);
									resOb.setComments(comment);
									residuals.add(resOb);

									// Pop the observation environment.
									vela.popEnvironment();
								}

								functionStrMap.put(LocaleProps
										.get("MODEL_INFO_FUNCTION_TITLE"),
										toString());

								Optional<List<FunctionExecutor>> funcs = vela
										.lookupFunctions(FUNC_NAME);

								// Has a derivative function been defined?
								if (vela.lookupFunctions(DERIV_FUNC_NAME)
										.isPresent()) {
									ApacheCommonsDerivativeBasedExtremaFinder finder = new ApacheCommonsDerivativeBasedExtremaFinder(
											fit,
											(DifferentiableUnivariateRealFunction) function,
											timeCoordSource, zeroPoint);

									String extremaStr = finder.toString();

									if (extremaStr != null) {
										String title = LocaleProps
												.get("MODEL_INFO_EXTREMA_TITLE");

										functionStrMap.put(title, extremaStr);
									}
								}
							} catch (FunctionEvaluationException e) {
								throw new AlgorithmError(
										e.getLocalizedMessage());
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
				};
			}

			return model;
		}
	}
}
