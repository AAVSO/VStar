package org.aavso.tools.vstar.external.plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.aavso.tools.vstar.data.DateInfo;
import org.aavso.tools.vstar.data.Magnitude;
import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.exception.AlgorithmError;
import org.aavso.tools.vstar.plugin.ModelCreatorPluginBase;
import org.aavso.tools.vstar.ui.dialog.ITextComponent;
import org.aavso.tools.vstar.ui.dialog.TextArea;
import org.aavso.tools.vstar.ui.dialog.TextDialog;
import org.aavso.tools.vstar.ui.mediator.AnalysisType;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.model.plot.ContinuousModelFunction;
import org.aavso.tools.vstar.ui.model.plot.ICoordSource;
import org.aavso.tools.vstar.ui.model.plot.JDCoordSource;
import org.aavso.tools.vstar.ui.model.plot.JDTimeElementEntity;
import org.aavso.tools.vstar.ui.model.plot.StandardPhaseCoordSource;
import org.aavso.tools.vstar.util.comparator.JDComparator;
import org.aavso.tools.vstar.util.comparator.StandardPhaseComparator;
import org.aavso.tools.vstar.util.locale.LocaleProps;
import org.aavso.tools.vstar.util.model.IModel;
import org.aavso.tools.vstar.util.model.PeriodFitParameters;
import org.aavso.tools.vstar.util.stats.DescStats;
import org.aavso.tools.vstar.vela.Operand;
import org.aavso.tools.vstar.vela.VeLaInterpreter;
import org.aavso.tools.vstar.vela.VeLaValidObservationEnvironment;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.UnivariateRealFunction;

/**
 * A model creator that allows a VeLa function to be applied to observations.
 */
public class VeLaModelCreator extends ModelCreatorPluginBase {

	private static TextDialog velaDialog;

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
		VeLaModel velaModelCreator = new VeLaModel(obs);
		return velaModelCreator.createModel();
	}

	// TODO: this class should go into the VeLa package

//	class VeLaUnivariateRealFunction implements DifferentiableUnivariateRealFunction {
	class VeLaUnivariateRealFunction implements UnivariateRealFunction {

		private VeLaInterpreter vela;
		private String funcName;

		public VeLaUnivariateRealFunction(VeLaInterpreter vela, String funcName) {
			this.vela = vela;
			this.funcName = funcName;
		}

		@Override
		public double value(double n) throws FunctionEvaluationException {
			String funCall = funcName + "(" + n + ")";
			Optional<Operand> result = vela.program(funCall);
			if (result.isPresent()) {
				return result.get().doubleVal();
			} else {
				throw new FunctionEvaluationException(n);
			}
		}

//		@Override
//		public UnivariateRealFunction derivative() {
//			// TODO: fix!
//			return this;
//		}
	}

	class VeLaModel {
		private List<ValidObservation> obs;
		private double zeroPoint;

		VeLaModel(List<ValidObservation> obs) {
			// Select time mode (JD or phase).
			switch (Mediator.getInstance().getAnalysisType()) {
			case RAW_DATA:
				timeCoordSource = JDCoordSource.instance;
				timeComparator = JDComparator.instance;
				this.obs = obs;
				zeroPoint = DescStats.calcTimeElementMean(obs,
						JDTimeElementEntity.instance);
				break;

			case PHASE_PLOT:
				timeCoordSource = StandardPhaseCoordSource.instance;
				timeComparator = StandardPhaseComparator.instance;
				this.obs = new ArrayList<ValidObservation>(obs);
				Collections.sort(this.obs, timeComparator);
				zeroPoint = 0;
				break;
			}
		}

		// Create a VeLa model.
		IModel createModel() {
			IModel model = null;

			if (velaDialog == null) {
				ITextComponent<String> velaCode = new TextArea("Function Code",
						10, 40);
				velaDialog = new TextDialog("VeLa Model", velaCode);
			} else {
				velaDialog.showDialog();
			}

			if (!velaDialog.isCancelled()) {
				// final AbstractLeastSquaresOptimizer optimizer = new
				// LevenbergMarquardtOptimizer();

				// final PolynomialFitter fitter = new PolynomialFitter(
				// getDegree(), optimizer);

				String velaModelFunctionStr = velaDialog.getTextFields().get(0)
						.getStringValue();

				model = new IModel() {
					boolean interrupted = false;
					List<ValidObservation> fit;
					List<ValidObservation> residuals;
					UnivariateRealFunction function;
					Map<String, String> functionStrMap = new LinkedHashMap<String, String>();

					// double aic = Double.NaN;
					// double bic = Double.NaN;

					@Override
					public String getDescription() {
						return velaModelFunctionStr + " for "
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

					public String toFitMetricsString() throws AlgorithmError {
						String strRepr = functionStrMap
								.get("MODEL_INFO_FIT_METRICS_TITLE");

						if (strRepr == null) {
							// Goodness of fit.
							// strRepr = "RMS: "
							// + NumericPrecisionPrefs
							// .formatOther(optimizer.getRMS());

							// // Akaike and Bayesean Information Criteria.
							// if (aic != Double.NaN && bic != Double.NaN) {
							// strRepr += "\nAIC: "
							// + NumericPrecisionPrefs
							// .formatOther(aic);
							// strRepr += "\nBIC: "
							// + NumericPrecisionPrefs
							// .formatOther(bic);
							// }
						}

						return strRepr;
					}

					@Override
					public String toString() {
						return functionStrMap.get(LocaleProps
								.get("MODEL_INFO_FUNCTION_TITLE"))
								+ velaModelFunctionStr;
					}

					@Override
					public ContinuousModelFunction getModelFunction() {
						return new ContinuousModelFunction(function, fit,
								zeroPoint);
					}

					@Override
					public void execute() throws AlgorithmError {

						// for (int i = 0; i < obs.size() && !interrupted; i++)
						// {
						// fitter.addObservedPoint(1.0,
						// timeCoordSource.getXCoord(i, obs)
						// - zeroPoint, obs.get(i).getMag());
						// }

						if (!interrupted) {
							try {
								// Create a VeLa interpreter instance and
								// compile the model function.
								VeLaInterpreter vela = new VeLaInterpreter();

								vela.program(velaModelFunctionStr);

								// Create a univariate real function instance.
								// Assume the function is called "f"
								String funcName = "f";

								function = new VeLaUnivariateRealFunction(vela,
										funcName);

								// function = fitter.fit();

								fit = new ArrayList<ValidObservation>();
								residuals = new ArrayList<ValidObservation>();

								// double sumSqResiduals = 0;

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
									double zeroedX = x - zeroPoint;
									double y = function.value(zeroedX);

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

									// sumSqResiduals += (residual * residual);
								}

								// // Fit metrics (AIC, BIC).
								// int n = residuals.size();
								// if (n != 0 && sumSqResiduals / n != 0) {
								// double commonIC = n
								// * Math.log(sumSqResiduals / n);
								// aic = commonIC + 2 * degree;
								// bic = commonIC + degree * Math.log(n);
								// }

								functionStrMap.put(LocaleProps
										.get("MODEL_INFO_FIT_METRICS_TITLE"),
										toFitMetricsString());

//								ApacheCommonsDerivativeBasedExtremaFinder finder = new ApacheCommonsDerivativeBasedExtremaFinder(
//										fit,
//										(DifferentiableUnivariateRealFunction) function,
//										timeCoordSource, zeroPoint);
//
//								String extremaStr = finder.toString();
//
//								if (extremaStr != null) {
//									String title = LocaleProps
//											.get("MODEL_INFO_EXTREMA_TITLE");
//
//									functionStrMap.put(title, extremaStr);
//								}

								functionStrMap.put(LocaleProps
										.get("MODEL_INFO_FUNCTION_TITLE"),
										toString());

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
