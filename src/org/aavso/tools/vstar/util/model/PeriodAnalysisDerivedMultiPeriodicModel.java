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
package org.aavso.tools.vstar.util.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.exception.AlgorithmError;
import org.aavso.tools.vstar.ui.model.plot.ContinuousModelFunction;
import org.aavso.tools.vstar.util.Pair;
import org.aavso.tools.vstar.util.locale.LocaleProps;
import org.aavso.tools.vstar.util.period.IPeriodAnalysisAlgorithm;
import org.aavso.tools.vstar.util.period.PeriodAnalysisCoordinateType;
import org.aavso.tools.vstar.util.period.dcdft.PeriodAnalysisDataPoint;
import org.aavso.tools.vstar.util.prefs.NumericPrecisionPrefs;
import org.aavso.tools.vstar.util.stats.DescStats;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.UnivariateRealFunction;

/**
 * This class creates a multi-periodic fit model for the specified observations
 * and provides measures of error.
 */
public class PeriodAnalysisDerivedMultiPeriodicModel implements IModel {

	private PeriodAnalysisDataPoint topDataPoint;
	private List<Harmonic> harmonics;
	private IPeriodAnalysisAlgorithm algorithm;

	private List<ValidObservation> fit;
	private List<ValidObservation> residuals;

	// TODO: PeriodFitParameters could instead be a generic parameter per concrete
	// model since this will differ for each model type.
	private List<PeriodFitParameters> parameters;

	private String desc;

	private Map<String, String> functionStrMap;

	/**
	 * Constructor
	 * 
	 * @param topDataPoint The top period analysis datapoint May be null!
	 * @param harmonics    A list of harmonics used as input to the fit algorithm.
	 * @param algorithm    The algorithm to be executed to generate the fit.
	 */
	public PeriodAnalysisDerivedMultiPeriodicModel(PeriodAnalysisDataPoint topDataPoint, List<Harmonic> harmonics,
			IPeriodAnalysisAlgorithm algorithm) {
		this.topDataPoint = topDataPoint;
		this.harmonics = harmonics;
		this.algorithm = algorithm;

		this.fit = new ArrayList<ValidObservation>();
		this.residuals = new ArrayList<ValidObservation>();
		this.parameters = new ArrayList<PeriodFitParameters>();
		this.functionStrMap = new LinkedHashMap<String, String>();

		desc = null;
	}

	/**
	 * @see org.aavso.tools.vstar.util.model.IModel#getDescription()
	 */
	@Override
	public String getDescription() {
		if (desc == null) {
			desc = getKind() + " from periods: ";
			for (Harmonic harmonic : harmonics) {
				desc += NumericPrecisionPrefs.formatOther(harmonic.getPeriod()) + " ";
			}
		}

		return desc;
	}

	/**
	 * @see org.aavso.tools.vstar.util.model.IModel#getKind()
	 */
	@Override
	public String getKind() {
		return "Fit";
	}

	/**
	 * @return the harmonics
	 */
	public List<Harmonic> getHarmonics() {
		return harmonics;
	}

	/**
	 * @see org.aavso.tools.vstar.util.model.IModel#getFit()
	 */
	@Override
	public List<ValidObservation> getFit() {
		return fit;
	}

	/**
	 * @see org.aavso.tools.vstar.util.model.IModel#getResiduals()
	 */
	@Override
	public List<ValidObservation> getResiduals() {
		return residuals;
	}

	/**
	 * @see org.aavso.tools.vstar.util.model.IModel#getParameters()
	 */
	@Override
	public List<PeriodFitParameters> getParameters() {
		return parameters;
	}

	/**
	 * @see org.aavso.tools.vstar.util.IAlgorithm#execute()
	 */
	@Override
	public void execute() throws AlgorithmError {

		try {
			algorithm.multiPeriodicFit(harmonics, this);

			String uncertaintyStr;
			if (!algorithm.getResultSeries().get(PeriodAnalysisCoordinateType.FREQUENCY).isEmpty()) {
				uncertaintyStr = toUncertaintyString();
			} else {
				uncertaintyStr = "A period analysis must be carried out for uncertainty to be computed.";
			}

			functionStrMap.put(LocaleProps.get("MODEL_INFO_UNCERTAINTY"), uncertaintyStr);

			functionStrMap.put(LocaleProps.get("MODEL_INFO_FUNCTION_TITLE"), toString());

			functionStrMap.put(LocaleProps.get("MODEL_INFO_EXCEL_TITLE"), toExcelString());

			functionStrMap.put(LocaleProps.get("MODEL_INFO_R_TITLE"), toRString());
		} catch (InterruptedException e) {
			// Do nothing; just return.
		}
	}

	@Override
	public boolean hasFuncDesc() {
		return true;
	}

	// See
	// - https://github.com/AAVSO/VStar/issues/255
	// - https://github.com/AAVSO/VStar/issues/294
	public String toUncertaintyString() throws AlgorithmError {
		String strRepr = "Could not determine uncertainty for this model.";

		if (!algorithm.getResultSeries().get(PeriodAnalysisCoordinateType.FREQUENCY).isEmpty()) {
			double freq = harmonics.get(0).getFrequency();
			double period = harmonics.get(0).getPeriod();

			try {
				double semiAmplitude = topDataPoint.getSemiAmplitude();
				double power = topDataPoint.getPower();

				strRepr = functionStrMap.get(LocaleProps.get("MODEL_INFO_UNCERTAINTY"));

				if (strRepr == null) {
					strRepr = String.format("For frequency %s, period %s, power %s, semi-amplitude %s:\n\n",
							NumericPrecisionPrefs.formatOther(freq), NumericPrecisionPrefs.formatOther(period),
							NumericPrecisionPrefs.formatOther(power), NumericPrecisionPrefs.formatOther(semiAmplitude));

					int index = findIndexOfTopHitInFullResultData();
					if (index != -1) {
						// Full Width Half Maximum
						Pair<Double, Double> fwhm = fwhm(index);
						strRepr += "  FWHM for frequency:\n";
						strRepr += "        Lower bound: " + NumericPrecisionPrefs.formatOther(fwhm.first) + "\n";
						strRepr += "        Upper bound: " + NumericPrecisionPrefs.formatOther(fwhm.second) + "\n";
						double fwhmError = Math.abs(fwhm.second - fwhm.first) / 2;
						strRepr += "     Resulting error: " + NumericPrecisionPrefs.formatOther(fwhmError) + "\n\n";
					}

					if (harmonics.size() == 1) {
						// Standard error of the frequency and semi-amplitude.
						// Only makes sense for a model where just the fundamental frequency is
						// included, otherwise the additional harmonics would change the residuals.
						strRepr += "  Standard Error of the Frequency: "
								+ NumericPrecisionPrefs.formatOther(standardErrorOfTheFrequency()) + "\n";

						strRepr += "  Standard Error of the Semi-Amplitude: "
								+ NumericPrecisionPrefs.formatOther(standardErrorOfTheSemiAmplitude());
					}
				}
			} catch (AlgorithmError e) {
				// can't report uncertainty
			}
		}

		return strRepr;

	}

	public String toString() {
		String strRepr = functionStrMap.get(LocaleProps.get("MODEL_INFO_FUNCTION_TITLE"));

		if (strRepr == null) {
			// Factor out the zero point so variant models can be more easily
			// created; the zero point will be the same for all parameters, so
			// just get it from the first.
			strRepr = "zeroPoint is " + NumericPrecisionPrefs.formatTime(parameters.get(0).getZeroPointOffset())
					+ "\n\n";

			strRepr += "f(t:real) : real {\n";

			double constantCoefficient = parameters.get(0).getConstantCoefficient();
			strRepr += NumericPrecisionPrefs.formatOther(constantCoefficient) + "\n";

			for (int i = 0; i < parameters.size(); i++) {
				PeriodFitParameters params = parameters.get(i);
				strRepr += params.toString() + "\n";
			}

			strRepr = strRepr.trim();

			strRepr += "\n}";
		}

		return strRepr;
	}

	private String toExcelString() {
		String strRepr = functionStrMap.get(LocaleProps.get("MODEL_INFO_EXCEL_TITLE"));

		if (strRepr == null) {
			strRepr = "=";

			double constantCoefficient = parameters.get(0).getConstantCoefficient();
			strRepr += NumericPrecisionPrefs.formatOther(constantCoefficient) + "\n";

			for (int i = 0; i < parameters.size(); i++) {
				PeriodFitParameters params = parameters.get(i);
				strRepr += params.toExcelString() + "\n";
			}

		}

		return strRepr;
	}

	// toRString must be locale-independent!
	private String toRString() {
		String strRepr = functionStrMap.get(LocaleProps.get("MODEL_INFO_R_TITLE"));

		if (strRepr == null) {
			// Factor out the zero point so variant models can be more easily
			// created; the zero point will be the same for all parameters, so
			// just get it from the first.
			strRepr = "zeroPoint <- "
					+ NumericPrecisionPrefs.formatTimeLocaleIndependent(parameters.get(0).getZeroPointOffset())
					+ "\n\n";

			strRepr += "model <- function(t)\n";

			double constantCoefficient = parameters.get(0).getConstantCoefficient();
			strRepr += NumericPrecisionPrefs.formatOtherLocaleIndependent(constantCoefficient);

			for (int i = 0; i < parameters.size(); i++) {
				PeriodFitParameters params = parameters.get(i);
				strRepr += params.toRString();
			}

			strRepr = strRepr.trim();
		}

		return strRepr;
	}

	@Override
	public ContinuousModelFunction getModelFunction() {
		UnivariateRealFunction func = new UnivariateRealFunction() {
			@Override
			public double value(double t) throws FunctionEvaluationException {
				double y = parameters.get(0).getConstantCoefficient();

				for (int i = 0; i < parameters.size(); i++) {
					PeriodFitParameters params = parameters.get(i);
					y += params.toValue(t);
				}
				return y;
			}
		};
		return new ContinuousModelFunction(func, fit);
	}

	@Override
	public void interrupt() {
		algorithm.interrupt();
	}

	@Override
	public Map<String, String> getFunctionStrings() {
		return functionStrMap;
	}

	// TODO: do the names of these std err functions reflect a typo in Foster's book?
	
	// Residuals-based standard error functions
	// see https://github.com/AAVSO/VStar/issues/255

	public double standardErrorOfTheFrequency() throws AlgorithmError {
		// Find the semi-amplitude for the fundamental frequency (zeroth harmonic)
		double semiAmplitude = topDataPoint.getSemiAmplitude();

		double sampleVariance = DescStats.calcMagSampleVarianceInRange(residuals, 0, residuals.size() - 1);

		double totalTimeSpan = residuals.get(residuals.size() - 1).getJD() - residuals.get(0).getJD();

		return Math.sqrt(6 * sampleVariance / (Math.PI * Math.PI * residuals.size() * semiAmplitude * semiAmplitude
				* totalTimeSpan * totalTimeSpan));
	}

	public double standardErrorOfTheSemiAmplitude() throws AlgorithmError {
		double sampleVariance = DescStats.calcMagSampleVarianceInRange(residuals, 0, residuals.size() - 1);
		return Math.sqrt(2 * sampleVariance / residuals.size());
	}

	// Full Width Half Maximum for the model's fundamental frequency (zeroth
	// harmonic from the selected top-hit).

	public Pair<Double, Double> fwhm(int topHitIndexInFullResult) throws AlgorithmError {
		List<Double> frequencies = algorithm.getResultSeries().get(PeriodAnalysisCoordinateType.FREQUENCY);
		double fwhmLo = frequencies.get(topHitIndexInFullResult);
		double fwhmHi = frequencies.get(topHitIndexInFullResult);

		// Obtain the power at the top-hit frequency
		List<Double> powers = algorithm.getResultSeries().get(PeriodAnalysisCoordinateType.POWER);
		double peakPower = powers.get(topHitIndexInFullResult);

		// Descend the left and right branches starting from the model's fundamental
		// peak frequency, returning the low (left branch) and high (right branch)
		// frequencies whose powers are greater than or equal to half the power at the
		// model's fundamental frequency.

		for (int i = topHitIndexInFullResult; i >= 0; i--) {
			if (powers.get(i) >= peakPower / 2) {
				fwhmLo = frequencies.get(i);
			} else {
				break;
			}
		}

		for (int i = topHitIndexInFullResult; i < powers.size(); i++) {
			if (powers.get(i) >= peakPower / 2) {
				fwhmHi = frequencies.get(i);
			} else {
				break;
			}
		}

		return new Pair<Double, Double>(fwhmLo, fwhmHi);
	}

	public int findIndexOfTopHitInFullResultData() throws AlgorithmError {
		int index = -1;

		if (topDataPoint != null) {
			Map<PeriodAnalysisCoordinateType, List<Double>> resultDataMap = algorithm.getResultSeries();

			for (int i = 0; i < resultDataMap.get(PeriodAnalysisCoordinateType.FREQUENCY).size(); i++) {
				double candidateFreq = resultDataMap.get(PeriodAnalysisCoordinateType.FREQUENCY).get(i);
				double candidatePeriod = resultDataMap.get(PeriodAnalysisCoordinateType.PERIOD).get(i);
				double candidatePower = resultDataMap.get(PeriodAnalysisCoordinateType.POWER).get(i);
				double candidateSemiAmplitude = resultDataMap.get(PeriodAnalysisCoordinateType.SEMI_AMPLITUDE).get(i);

				PeriodAnalysisDataPoint candidateDataPoint = new PeriodAnalysisDataPoint(candidateFreq, candidatePeriod,
						candidatePower, candidateSemiAmplitude);

				if (candidateDataPoint.hashCode() == topDataPoint.hashCode()) {
					index = i;
					break;
				}
			}
		}

		return index;
	}
}