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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import org.aavso.tools.vstar.data.DateInfo;
import org.aavso.tools.vstar.data.Magnitude;
import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.exception.AlgorithmError;
import org.aavso.tools.vstar.plugin.ModelCreatorPluginBase;
import org.aavso.tools.vstar.util.model.IModel;
import org.aavso.tools.vstar.util.model.PeriodFitParameters;
import org.aavso.tools.vstar.util.prefs.NumericPrecisionPrefs;
import org.apache.commons.math.MathException;
import org.apache.commons.math.analysis.interpolation.LoessInterpolator;
import org.apache.commons.math.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math.analysis.polynomials.PolynomialSplineFunction;

/**
 * A Loess (Lowess) fitter model creator plugin that uses an Apache Commons
 * Loess fitter.
 */
public class ApacheCommonsLowessFitter extends ModelCreatorPluginBase {

	private Locale locale;

	public ApacheCommonsLowessFitter() {
		super();
		locale = Locale.getDefault();
	}

	@Override
	public String getDescription() {
		String str = "Lowess Fit (Apache Commons Math)";

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

	class PolynomialFitCreator {
		private List<ValidObservation> obs;

		PolynomialFitCreator(List<ValidObservation> obs) {
			this.obs = obs;
		}

		// Create a model representing a polynomial fit of the requested degree.
		IModel createModel() {

			// TODO: create a dialog to permit entry of params for other
			// forms of ctor

			return new IModel() {
				boolean interrupted = false;
				List<ValidObservation> fit;
				List<ValidObservation> residuals;
				PolynomialSplineFunction function;
				Map<String, String> functionStrMap = new LinkedHashMap<String, String>();
				double aic = Double.NaN;
				double bic = Double.NaN;

				@Override
				public String getDescription() {
					// TODO: fix wrt locale
					return getKind();
				}

				@Override
				public List<ValidObservation> getFit() {
					return fit;
				}

				@Override
				public String getKind() {
					String str = "Lowess Fit (Apache Commons Math)";

					// TODO: fix
					if (locale.equals("es")) {
						str = "Lowess Fit (Apache Commons Math)";
					}

					return str;
				}

				@Override
				public List<PeriodFitParameters> getParameters() {
					// None for a Lowess fit.
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

						double constCoeff = 0;

						for (PolynomialFunction f : function.getPolynomials()) {
							double[] coeffs = f.getCoefficients();
							for (int i = coeffs.length - 1; i >= 1; i--) {
								strRepr += String.format(fmt, coeffs[i]);
								strRepr += "t^" + i + ",\n";
							}
							constCoeff += coeffs[0];
						}

						strRepr += String.format(fmt, constCoeff) + ")";

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

						double constCoeff = 0;

						for (PolynomialFunction f : function.getPolynomials()) {
							double[] coeffs = f.getCoefficients();
							for (int i = coeffs.length - 1; i >= 1; i--) {
								strRepr += String.format(fmt, coeffs[i]);
								strRepr += "*A1^" + i + ",\n";
							}
							constCoeff += coeffs[0];
						}

						strRepr += String.format(fmt, constCoeff) + ")";
					}

					return strRepr;
				}

				// Note: There is already a lowess fit function in R, so it
				// would be interesting to compare the results of that and this
				// plugin.
				public String toRString() {
					String strRepr = functionStrMap.get("R");

					if (strRepr == null) {
						strRepr = "model <- function(t) ";

						String fmt = NumericPrecisionPrefs
								.getOtherOutputFormat();

						double constCoeff = 0;

						for (PolynomialFunction f : function.getPolynomials()) {
							double[] coeffs = f.getCoefficients();
							for (int i = coeffs.length - 1; i >= 1; i--) {
								strRepr += String.format(fmt, coeffs[i]);
								strRepr += "*t^" + i + "+\n";
							}
							constCoeff += coeffs[0];
						}

						strRepr += String.format(fmt, constCoeff);
					}

					return strRepr;
				}

				@Override
				public void execute() throws AlgorithmError {

					// The Lowess fitter requires a strictly increasing sequence
					// on the domain (i.e. JD values), i.e. no duplicates.
					Map<Double, Double> jdToMagMap = new TreeMap<Double, Double>();

					for (int i = 0; i < obs.size(); i++) {
						ValidObservation ob = obs.get(i);
						// This means that the last magnitude for a JD wins!
						jdToMagMap.put(ob.getJD(), ob.getMag());
					}

					double[] xvals = new double[jdToMagMap.size()];
					double[] yvals = new double[jdToMagMap.size()];

					int index = 0;
					for (Double jd : jdToMagMap.keySet()) {
						xvals[index] = jd;
						yvals[index++] = jdToMagMap.get(jd);
					}

					try {
						final LoessInterpolator interpolator = new LoessInterpolator();
						function = interpolator.interpolate(xvals, yvals);

						fit = new ArrayList<ValidObservation>();
						residuals = new ArrayList<ValidObservation>();
						double sumSqResiduals = 0;

						// TODO: fix wrt locale
						String comment = "From Lowess fit";

						// Create fit and residual observations and
						// compute the sum of squares of residuals for
						// Akaike and Bayesean Information Criteria.
						for (int i = 0; i < xvals.length && !interrupted; i++) {
							double jd = xvals[i];
							double mag = yvals[i];

							double y = function.value(jd);

							ValidObservation fitOb = new ValidObservation();
							fitOb.setDateInfo(new DateInfo(jd));
							fitOb.setMagnitude(new Magnitude(y, 0));
							fitOb.setBand(SeriesType.Model);
							fitOb.setComments(comment);
							fit.add(fitOb);

							ValidObservation resOb = new ValidObservation();
							resOb.setDateInfo(new DateInfo(jd));
							double residual = mag - y;
							resOb.setMagnitude(new Magnitude(residual, 0));
							resOb.setBand(SeriesType.Residuals);
							resOb.setComments(comment);
							residuals.add(resOb);

							sumSqResiduals += (residual * residual);
						}

						// TODO: what to use for degree (or N) here?
						double degree = 0;

						for (PolynomialFunction f : function.getPolynomials()) {
							degree += f.getCoefficients().length;
						}

						int n = residuals.size();
						if (n != 0 && sumSqResiduals / n != 0) {
							double commonIC = n * Math.log(sumSqResiduals / n);
							aic = commonIC + 2 * degree;
							bic = commonIC + degree * Math.log(n);
						}

						functionStrMap.put("Function", toString());
						functionStrMap.put("Excel", toExcelString());
						functionStrMap.put("R", toRString());

					} catch (MathException e) {
						throw new AlgorithmError(e.getLocalizedMessage());
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
	}
}
