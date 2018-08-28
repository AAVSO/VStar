package org.aavso.tools.vstar.external.plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
import org.aavso.tools.vstar.ui.model.plot.JDTimeElementEntity;
import org.aavso.tools.vstar.ui.model.plot.StandardPhaseCoordSource;
import org.aavso.tools.vstar.util.comparator.JDComparator;
import org.aavso.tools.vstar.util.comparator.StandardPhaseComparator;
import org.aavso.tools.vstar.util.locale.LocaleProps;
import org.aavso.tools.vstar.util.model.IModel;
import org.aavso.tools.vstar.util.model.PeriodFitParameters;
import org.aavso.tools.vstar.util.stats.DescStats;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.UnivariateRealFunction;

/**
 * A model creator that allows a simple function to be applied to observations.
 */
public class ModelCreator extends ModelCreatorPluginBase {

	private ICoordSource timeCoordSource;
	private Comparator<ValidObservation> timeComparator;

	@Override
	public String getDescription() {
		return "Simple model creator";
	}

	@Override
	public String getDisplayName() {
		return "Simple Model";
	}

	@Override
	public IModel getModel(List<ValidObservation> obs) {
		SimpleModel simpleModel = new SimpleModel(obs);
		return simpleModel.createModel();
	}

	class SimpleUnivariateRealFunction implements UnivariateRealFunction {
		@Override
		public double value(double n) throws FunctionEvaluationException {
			return Math.sin(n) + Math.cos(n);
		}
	}

	class SimpleModel {
		private List<ValidObservation> obs;
		private double zeroPoint;

		SimpleModel(List<ValidObservation> obs) {
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

		// Create a simple model.
		IModel createModel() {
			IModel model = null;

			model = new IModel() {
				boolean interrupted = false;
				List<ValidObservation> fit;
				List<ValidObservation> residuals;
				UnivariateRealFunction function;
				Map<String, String> functionStrMap = new LinkedHashMap<String, String>();

				@Override
				public String getDescription() {
					return "Simple function for " + obs.get(0).getBand()
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
					return "Simple Model";
				}

				@Override
				public List<PeriodFitParameters> getParameters() {
					// None for this model.
					return null;
				}

				@Override
				public boolean hasFuncDesc() {
					return true;
				}

				@Override
				public String toString() {
					return functionStrMap.get(LocaleProps
							.get("MODEL_INFO_FUNCTION_TITLE"))
							+ "Simple Function";
				}

				@Override
				public ContinuousModelFunction getModelFunction() {
					return new ContinuousModelFunction(function, fit, zeroPoint);
				}

				@Override
				public void execute() throws AlgorithmError {
					if (!interrupted) {
						try {

							function = new SimpleUnivariateRealFunction(); 
									
							fit = new ArrayList<ValidObservation>();
							residuals = new ArrayList<ValidObservation>();

							String comment = "Simple Model";

							// Create fit and residual observations.
							for (int i = 0; i < obs.size() && !interrupted; i++) {
								ValidObservation ob = obs.get(i);

								double x = timeCoordSource.getXCoord(i, obs);
								double zeroedX = x - zeroPoint;
								double y = function.value(zeroedX);

								ValidObservation fitOb = new ValidObservation();
								fitOb.setDateInfo(new DateInfo(ob.getJD()));
								if (Mediator.getInstance().getAnalysisType() == AnalysisType.PHASE_PLOT) {
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
								if (Mediator.getInstance().getAnalysisType() == AnalysisType.PHASE_PLOT) {
									resOb.setPreviousCyclePhase(ob
											.getPreviousCyclePhase());
									resOb.setStandardPhase(ob
											.getStandardPhase());
								}
								double residual = ob.getMag() - y;
								resOb.setMagnitude(new Magnitude(residual, 0));
								resOb.setBand(SeriesType.Residuals);
								resOb.setComments(comment);
								residuals.add(resOb);
							}

							functionStrMap.put(LocaleProps
									.get("MODEL_INFO_FUNCTION_TITLE"),
									toString());

						} catch (FunctionEvaluationException e) {
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
			};

			return model;
		}
	}
}