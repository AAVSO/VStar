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
import java.util.Map;
import java.util.TreeMap;

import org.aavso.tools.vstar.data.DateInfo;
import org.aavso.tools.vstar.data.Magnitude;
import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.exception.AlgorithmError;
import org.aavso.tools.vstar.plugin.ModelCreatorPluginBase;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.model.plot.ContinuousModelFunction;
import org.aavso.tools.vstar.ui.model.plot.ICoordSource;
import org.aavso.tools.vstar.ui.model.plot.JDCoordSource;
import org.aavso.tools.vstar.ui.model.plot.StandardPhaseCoordSource;
import org.aavso.tools.vstar.util.locale.LocaleProps;
import org.aavso.tools.vstar.util.model.IModel;
import org.aavso.tools.vstar.util.model.PeriodFitParameters;
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

	@Override
	public IModel getModel(List<ValidObservation> obs) {
		LoessFitCreator fitCreator = new LoessFitCreator(obs);
		return fitCreator.createModel();
	}

	class LoessFitCreator {
		private List<ValidObservation> obs;

		LoessFitCreator(List<ValidObservation> obs) {
			this.obs = obs;
		}

		// Create a model representing a polynomial fit of the requested degree.
		IModel createModel() {

			// TODO: create a dialog to permit entry of params for other
			// forms of ctor (Loess algorithm variants).

			return new IModel() {
				boolean interrupted = false;
				List<ValidObservation> fit;
				List<ValidObservation> residuals;
				PolynomialSplineFunction function;
				Map<String, String> functionStrMap = new LinkedHashMap<String, String>();
				double aic = Double.NaN;
				double bic = Double.NaN;

				// TODO: why am I not using this in model creation method!? (as
				// I do for polyfit)
				// final double zeroPoint = DescStats.calcTimeElementMean(obs,
				// JDTimeElementEntity.instance);

				@Override
				public String getDescription() {
					return getKind() + " for " + obs.get(0).getBand()
							+ " series";
				}

				@Override
				public List<ValidObservation> getFit() {
					return fit;
				}

				@Override
				public String getKind() {
					return "Loess Fit";
				}

				@Override
				public List<PeriodFitParameters> getParameters() {
					// None for a Loess fit.
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
					String strRepr = functionStrMap.get(LocaleProps
							.get("MODEL_INFO_FUNCTION_TITLE"));

					if (strRepr == null) {
						strRepr = "f(t) = ";

						double constCoeff = 0;

						for (PolynomialFunction f : function.getPolynomials()) {
							double[] coeffs = f.getCoefficients();
							for (int i = coeffs.length - 1; i >= 1; i--) {
								strRepr += coeffs[i];
								strRepr += "t^" + i + "+\n";
							}
							constCoeff += coeffs[0];
						}

						strRepr += constCoeff;
					}

					return strRepr;
				}

				public String toExcelString() {
					String strRepr = functionStrMap.get(LocaleProps
							.get("MODEL_INFO_EXCEL_TITLE"));

					if (strRepr == null) {
						strRepr = "=SUM(";

						double constCoeff = 0;

						for (PolynomialFunction f : function.getPolynomials()) {
							double[] coeffs = f.getCoefficients();
							for (int i = coeffs.length - 1; i >= 1; i--) {
								strRepr += coeffs[i];
								strRepr += "*A1^" + i + ",\n";
							}
							constCoeff += coeffs[0];
						}

						strRepr += constCoeff + ")";
					}

					return strRepr;
				}

				// Note: There is already a Loess fit function in R, so it
				// would be interesting to compare the results of that and this
				// plugin.
				public String toRString() {
					String strRepr = functionStrMap.get(LocaleProps
							.get("MODEL_INFO_R_TITLE"));

					if (strRepr == null) {
						strRepr = "model <- function(t) ";

						double constCoeff = 0;

						for (PolynomialFunction f : function.getPolynomials()) {
							double[] coeffs = f.getCoefficients();
							for (int i = coeffs.length - 1; i >= 1; i--) {
								strRepr += coeffs[i];
								strRepr += "*t^" + i + "+\n";
							}
							constCoeff += coeffs[0];
						}

						strRepr += constCoeff;
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

						String comment = "From Loess fit";

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

						// Fit metrics (AIC, BIC).
						int n = residuals.size();
						if (n != 0 && sumSqResiduals / n != 0) {
							double commonIC = n * Math.log(sumSqResiduals / n);
							aic = commonIC + 2 * degree;
							bic = commonIC + degree * Math.log(n);
						}

						ICoordSource timeCoordSource = null;
						switch (Mediator.getInstance().getAnalysisType()) {
						case RAW_DATA:
							timeCoordSource = JDCoordSource.instance;
							break;

						case PHASE_PLOT:
							timeCoordSource = StandardPhaseCoordSource.instance;
							break;
						}

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
						// functionStrMap.put("Function", toString());
						functionStrMap.put(
								LocaleProps.get("MODEL_INFO_FUNCTION_TITLE"),
								toString());
						functionStrMap.put(
								LocaleProps.get("MODEL_INFO_EXCEL_TITLE"),
								toExcelString());
						functionStrMap.put(
								LocaleProps.get("MODEL_INFO_R_TITLE"),
								toRString());

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
