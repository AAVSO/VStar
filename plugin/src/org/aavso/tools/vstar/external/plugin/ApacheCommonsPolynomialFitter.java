/**
 * VStar: a statistical analysis tool for variable star data.
 * Copyright (C) 2010  AAVSO (http://www.aavso.org/)
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.aavso.tools.vstar.data.DateInfo;
import org.aavso.tools.vstar.data.Magnitude;
import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.exception.AlgorithmError;
import org.aavso.tools.vstar.plugin.ModelCreatorPluginBase;
import org.aavso.tools.vstar.ui.dialog.PolynomialDegreeDialog;
import org.aavso.tools.vstar.util.model.IModel;
import org.aavso.tools.vstar.util.model.PeriodFitParameters;
import org.aavso.tools.vstar.util.prefs.NumericPrecisionPrefs;
import org.apache.commons.math.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math.optimization.OptimizationException;
import org.apache.commons.math.optimization.fitting.PolynomialFitter;
import org.apache.commons.math.optimization.general.AbstractLeastSquaresOptimizer;
import org.apache.commons.math.optimization.general.LevenbergMarquardtOptimizer;

/**
 * A polynomial fitter model creator plugin that is uses an Apache Commons
 * polynomial fitter.
 */
public class ApacheCommonsPolynomialFitter extends ModelCreatorPluginBase {

	private Locale locale;

	private int degree;

	public ApacheCommonsPolynomialFitter() {
		super();
		locale = Locale.getDefault();
	}

	@Override
	public String getDescription() {
		String str = "Polynomial Fit (Apache Commons Math)";

		if (locale.equals("es")) {
			str = "Ajuste polin\u00F3mico (Apache Commons Math)";
		}

		return str;
	}

	@Override
	public String getDisplayName() {
		return getDescription();
	}

	@Override
	public IModel getModel(List<ValidObservation> obs) {
		PolynomialFitCreator fitCreator = new PolynomialFitCreator(obs);
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

			int minDegree = getMinDegree();
			int maxDegree = getMaxDegree();

			PolynomialDegreeDialog polyDegreeDialog = new PolynomialDegreeDialog(
					minDegree, maxDegree);

			if (!polyDegreeDialog.isCancelled()) {
				setDegree(polyDegreeDialog.getDegree());

				// final Map<String, AbstractLeastSquaresOptimizer> optimizerMap
				// = new TreeMap<String, AbstractLeastSquaresOptimizer>();
				//
				// optimizerMap.put("Levenberg Marquardt Optimizer",
				// new LevenbergMarquardtOptimizer());
				//
				// optimizerMap.put(
				// "Gauss-Newton Optimizer with LU decomposition",
				// new GaussNewtonOptimizer(true));
				//
				// optimizerMap.put(
				// "Gauss-Newton Optimizer with QR decomposition",
				// new GaussNewtonOptimizer(false));

				// RadioButtonDialog optimizerDialog = new RadioButtonDialog(
				// "Select Optimizer", optimizerMap.keySet(),
				// "Levenberg Marquardt Optimizer");

				AbstractLeastSquaresOptimizer optimizer = new LevenbergMarquardtOptimizer();

				// optimizer.setMaxIterations(1000);

				final PolynomialFitter fitter = new PolynomialFitter(
						getDegree(), optimizer);

				model = new IModel() {
					boolean interrupted = false;
					List<ValidObservation> fit;
					List<ValidObservation> residuals;
					PolynomialFunction function;
					Map<String, String> functionStrMap = new LinkedHashMap<String, String>();
					double aic = Double.NaN;
					double bic = Double.NaN;

					@Override
					public String getDescription() {
						// TODO: fix wrt locale
						return getKind() + " of degree " + degree;
					}

					@Override
					public List<ValidObservation> getFit() {
						return fit;
					}

					@Override
					public String getKind() {
						String str = "Polynomial Fit (Apache Commons Math)";

						if (locale.equals("es")) {
							str = "Ajuste polin\u00F3mico (Apache Commons Math)";
						}

						return str;
					}

					@Override
					public List<PeriodFitParameters> getParameters() {
						// None for a polynomial fit.
						return null;
					}

					@Override
					public List<ValidObservation> getResiduals() {
						return residuals;
					}

					@Override
					public boolean hasFuncDesc() {
						return true;
					}

					public String toString() {
						String strRepr = functionStrMap.get("Function");

						if (strRepr == null) {
							strRepr = "sum(";

							String fmt = NumericPrecisionPrefs
									.getOtherOutputFormat();

							double[] coeffs = function.getCoefficients();
							for (int i = coeffs.length - 1; i >= 1; i--) {
								strRepr += String.format(fmt, coeffs[i]);
								strRepr += "t^" + i + ",\n";
							}
							strRepr += String.format(fmt, coeffs[0]) + ")";

							// Akaike and Bayesean Information Criteria.
							if (aic != Double.NaN && bic != Double.NaN) {
								strRepr += String.format("\n\nAIC=" + fmt, aic);
								strRepr += String.format("\nBIC=" + fmt, bic);
							}
						}

						return strRepr;
					}

					public String toExcelString() {
						String strRepr = functionStrMap.get("Excel");

						if (strRepr == null) {
							strRepr = "=SUM(";

							String fmt = NumericPrecisionPrefs
									.getOtherOutputFormat();

							double[] coeffs = function.getCoefficients();
							for (int i = coeffs.length - 1; i >= 1; i--) {
								strRepr += String.format(fmt, coeffs[i]);
								strRepr += "*A1^" + i + ",\n";
							}
							strRepr += String.format(fmt, coeffs[0]) + ")";
						}

						return strRepr;
					}

					public String toRString() {
						String strRepr = functionStrMap.get("R");

						if (strRepr == null) {
							strRepr = "model <- function(t) ";

							String fmt = NumericPrecisionPrefs
									.getOtherOutputFormat();

							double[] coeffs = function.getCoefficients();
							for (int i = coeffs.length - 1; i >= 1; i--) {
								strRepr += String.format(fmt, coeffs[i]);
								strRepr += "*t^" + i + " +\n+ ";
							}
							strRepr += String.format(fmt, coeffs[0]);
						}

						return strRepr;
					}

					@Override
					public void execute() throws AlgorithmError {

						for (int i = 0; i < obs.size() && !interrupted; i++) {
							ValidObservation ob = obs.get(i);
							fitter.addObservedPoint(1.0, ob.getJD(), ob
									.getMag());
						}

						if (!interrupted) {
							try {
								function = fitter.fit();

								fit = new ArrayList<ValidObservation>();
								residuals = new ArrayList<ValidObservation>();
								double sumSqResiduals = 0;

								// TODO: fix wrt locale
								String comment = String.format(
										"From polynomial fit of degree %d",
										degree);

								// Create fit and residual observations and
								// compute the sum of squares of residuals for
								// Akaike and Bayesean Information Criteria.
								for (int i = 0; i < obs.size() && !interrupted; i++) {
									ValidObservation ob = obs.get(i);

									double y = function.value(ob.getJD());

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

								functionStrMap.put("Function", toString());
								functionStrMap.put("Excel", toExcelString());
								functionStrMap.put("R", toRString());

							} catch (OptimizationException e) {
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
