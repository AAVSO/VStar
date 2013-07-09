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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.aavso.tools.vstar.data.DateInfo;
import org.aavso.tools.vstar.data.Magnitude;
import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.exception.AlgorithmError;
import org.aavso.tools.vstar.plugin.ModelCreatorPluginBase;
import org.aavso.tools.vstar.ui.dialog.PolynomialDegreeDialog;
import org.aavso.tools.vstar.ui.model.plot.ContinuousModelFunction;
import org.aavso.tools.vstar.ui.model.plot.ICoordSource;
import org.aavso.tools.vstar.ui.model.plot.JDCoordSource;
import org.aavso.tools.vstar.ui.model.plot.JDTimeElementEntity;
import org.aavso.tools.vstar.util.locale.LocaleProps;
import org.aavso.tools.vstar.util.model.IModel;
import org.aavso.tools.vstar.util.model.PeriodFitParameters;
import org.aavso.tools.vstar.util.prefs.NumericPrecisionPrefs;
import org.aavso.tools.vstar.util.stats.DescStats;
import org.apache.commons.math.ConvergenceException;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MaxIterationsExceededException;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.fitting.PolynomialFitter;
import org.apache.commons.math.optimization.general.AbstractLeastSquaresOptimizer;
import org.apache.commons.math.optimization.general.LevenbergMarquardtOptimizer;
import org.apache.commons.math.optimization.univariate.BrentOptimizer;

/**
 * A polynomial model creator plugin that uses an Apache Commons polynomial
 * fitter.
 */
public class ApacheCommonsPolynomialFitCreatorPlugin extends
		ModelCreatorPluginBase {

	private int degree;

	private PolynomialFitCreator fitCreator;

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
		fitCreator = new PolynomialFitCreator(obs);
		return fitCreator.createModel();
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

	class PolynomialFitCreator {
		private List<ValidObservation> obs;

		PolynomialFitCreator(List<ValidObservation> obs) {
			this.obs = obs;
		}

		// Create a model representing a polynomial fit of the requested degree.
		IModel createModel() {
			IModel model = null;

			final double zeroPoint = DescStats.calcTimeElementMean(obs,
					JDTimeElementEntity.instance);

			int minDegree = getMinDegree();
			int maxDegree = getMaxDegree();

			PolynomialDegreeDialog polyDegreeDialog = new PolynomialDegreeDialog(
					minDegree, maxDegree);

			if (!polyDegreeDialog.isCancelled()) {
				setDegree(polyDegreeDialog.getDegree());

				final AbstractLeastSquaresOptimizer optimizer = new LevenbergMarquardtOptimizer();

				final PolynomialFitter fitter = new PolynomialFitter(
						getDegree(), optimizer);

				model = new IModel() {
					boolean interrupted = false;
					List<ValidObservation> fit;
					List<ValidObservation> residuals;
					PolynomialFunction function;
					ICoordSource coordSrc = JDCoordSource.instance;
					Map<String, String> functionStrMap = new LinkedHashMap<String, String>();
					double aic = Double.NaN;
					double bic = Double.NaN;

					@Override
					public String getDescription() {
						return LocaleProps
								.get("MODEL_INFO_POLYNOMIAL_DEGREE_DESC")
								+ degree;
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
						String strRepr = functionStrMap
								.get("MODEL_INFO_FIT_METRICS_TITLE");

						String fmt = NumericPrecisionPrefs
								.getOtherOutputFormat();

						List<Double> derivs = new ArrayList<Double>();

						if (strRepr == null) {
							// Goodness of fit.
							strRepr = String.format("RMS: " + fmt, optimizer
									.getRMS());

							// Akaike and Bayesean Information Criteria.
							if (aic != Double.NaN && bic != Double.NaN) {
								strRepr += String.format("\nAIC: " + fmt, aic);
								strRepr += String.format("\nBIC: " + fmt, bic);
							}
						}

						return strRepr;
					}

					private String toMinimaString() throws AlgorithmError {
						String strRepr = functionStrMap.get(LocaleProps
								.get("MODEL_INFO_MINIMA_TITLE"));

						String fmt = NumericPrecisionPrefs
								.getOtherOutputFormat();

						// TODO: jd or mag?
						double min = obs.get(0).getJD();
						double max = obs.get(obs.size() - 1).getJD();
						// TODO: inappropriate result...
						BrentOptimizer extremaFinder = new BrentOptimizer();
						try {
							double maximum = extremaFinder.optimize(function,
									GoalType.MAXIMIZE, min, max);
							strRepr = String.format(fmt, maximum);
						} catch (FunctionEvaluationException e) {
							throw new AlgorithmError(e.getLocalizedMessage());
						} catch (MaxIterationsExceededException e) {
							throw new AlgorithmError(e.getLocalizedMessage());
						}

						return strRepr;
					}

					private String toMaximaString() throws AlgorithmError {
						String strRepr = functionStrMap.get(LocaleProps
								.get("MODEL_INFO_MAXIMA_TITLE"));

						String fmt = NumericPrecisionPrefs
								.getOtherOutputFormat();

						double min = obs.get(0).getJD();
						double max = obs.get(obs.size() - 1).getJD();
						BrentOptimizer extremaFinder = new BrentOptimizer();
						try {
							double maximum = extremaFinder.optimize(function,
									GoalType.MAXIMIZE, min, max);
							strRepr = String.format(fmt, maximum);
						} catch (FunctionEvaluationException e) {
							throw new AlgorithmError(e.getLocalizedMessage());
						} catch (MaxIterationsExceededException e) {
							throw new AlgorithmError(e.getLocalizedMessage());
						}

						return strRepr;
					}

					public String toExcelString() {
						String strRepr = functionStrMap.get(LocaleProps
								.get("MODEL_INFO_EXCEL_TITLE"));

						if (strRepr == null) {
							strRepr = "=SUM(";

							double[] coeffs = function.getCoefficients();
							for (int i = coeffs.length - 1; i >= 1; i--) {
								strRepr += coeffs[i];
								strRepr += "*(A1-" + zeroPoint + ")^" + i
										+ ",\n";
							}
							strRepr += coeffs[0] + ")";
						}

						return strRepr;
					}

					public String toRString() {
						String strRepr = functionStrMap.get(LocaleProps
								.get("MODEL_INFO_R_TITLE"));

						if (strRepr == null) {
							strRepr = "model <- function(t) ";

							double[] coeffs = function.getCoefficients();
							for (int i = coeffs.length - 1; i >= 1; i--) {
								strRepr += coeffs[i];
								strRepr += "*(t-" + zeroPoint + ")^" + i
										+ " +\n";
							}
							strRepr += coeffs[0];
						}

						return strRepr;
					}

					@Override
					public ContinuousModelFunction getModelFunction() {
//						UnivariateRealFunction func = new UnivariateRealFunction() {
//							@Override
//							public double value(double x)
//									throws FunctionEvaluationException {
//								double y = 0;
//								double[] coeffs = function.getCoefficients();
//								for (int i = coeffs.length - 1; i >= 1; i--) {
//									y += coeffs[i] * Math.pow(x, i);
//								}
//								y += coeffs[0];
//								return y;
//							}
//						};

						return new ContinuousModelFunction(function, fit, zeroPoint);
					}

					// An alternative implementation for getModelFunction() that
					// uses Horner's method to avoid exponentiation.
					public UnivariateRealFunction getModelFunctionHorner() {
						UnivariateRealFunction func = new UnivariateRealFunction() {
							@Override
							public double value(double x)
									throws FunctionEvaluationException {
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
							ValidObservation ob = obs.get(i);
							fitter.addObservedPoint(1.0,
									ob.getJD() - zeroPoint, ob.getMag());
						}

						if (!interrupted) {
							try {
								function = fitter.fit();

								fit = new ArrayList<ValidObservation>();
								residuals = new ArrayList<ValidObservation>();
								double sumSqResiduals = 0;

								String comment = LocaleProps
										.get("MODEL_INFO_POLYNOMIAL_DEGREE_DESC")
										+ degree;

								// Create fit and residual observations and
								// compute the sum of squares of residuals for
								// Akaike and Bayesean Information Criteria.
								for (int i = 0; i < obs.size() && !interrupted; i++) {
									ValidObservation ob = obs.get(i);

									double y = function.value(ob.getJD()
											- zeroPoint);

									ValidObservation fitOb = new ValidObservation();
									fitOb.setDateInfo(new DateInfo(ob.getJD()));
									fitOb.setMagnitude(new Magnitude(y, 0));
									fitOb.setBand(SeriesType.Model);
									fitOb.setComments(comment);
									fit.add(fitOb);

									ValidObservation resOb = new ValidObservation();
									resOb.setDateInfo(new DateInfo(ob.getJD()));
									double residual = ob.getMag() - y;
									resOb.setMagnitude(new Magnitude(residual,
											0));
									resOb.setBand(SeriesType.Residuals);
									resOb.setComments(comment);
									residuals.add(resOb);

									sumSqResiduals += (residual * residual);
								}

								int n = residuals.size();
								if (n != 0 && sumSqResiduals / n != 0) {
									double commonIC = n
											* Math.log(sumSqResiduals / n);
									aic = commonIC + 2 * degree;
									bic = commonIC + degree * Math.log(n);
								}

								functionStrMap.put(LocaleProps
										.get("MODEL_INFO_FIT_METRICS_TITLE"),
										toFitMetricsString());
								// functionStrMap.put(LocaleProps
								// .get("MODEL_INFO_MINIMA_TITLE"),
								// toMinimaString());
								// functionStrMap.put(LocaleProps
								// .get("MODEL_INFO_MAXIMA_TITLE"),
								// toMaximaString());
								functionStrMap.put(LocaleProps
										.get("MODEL_INFO_EXCEL_TITLE"),
										toExcelString());
								functionStrMap
										.put(LocaleProps
												.get("MODEL_INFO_R_TITLE"),
												toRString());

							} catch (ConvergenceException e) {
								throw new AlgorithmError(e
										.getLocalizedMessage());
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
