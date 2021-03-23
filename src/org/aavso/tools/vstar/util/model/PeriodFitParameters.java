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

import org.aavso.tools.vstar.util.prefs.NumericPrecisionPrefs;

/**
 * This class represents a single period-based fit coefficient/parameter set
 * that could be be used to re-create a model fit.
 */
public class PeriodFitParameters implements Comparable<PeriodFitParameters> {

	private final static double TWOPI = 2 * Math.PI;

	private Harmonic harmonic;
	private double amplitude;
	private double phase;
	private double sineCoefficient;
	private double cosineCoefficient;
	private double constantCoefficient;
	private double zeroPointOffset;

	/**
	 * Constructor.
	 * 
	 * TODO: consider computing amplitude locally rather than passing it in!
	 * 
	 * @param harmonic
	 *            The harmonic, from which can be obtained period and frequency.
	 * @param amplitude
	 *            The amplitude parameter.
	 * @param sineCoefficient
	 *            The sine Fourier coefficient.
	 * @param cosineCoefficient
	 *            The cosine Fourier coefficient.
	 * @param constantCoefficient
	 *            The constant (Y intercept?; check) coefficient.
	 * @param zeroPointOffset
	 *            The zero point offset/term/subrahend to be subtracted from
	 *            each time step for which the model is computed.
	 */
	public PeriodFitParameters(Harmonic harmonic, double amplitude,
			double cosineCoefficient, double sineCoefficient,
			double constantCoefficient, double zeroPointOffset) {
		this.harmonic = harmonic;
		this.amplitude = amplitude;
		this.phase = Math.atan2(-sineCoefficient, cosineCoefficient);
		this.cosineCoefficient = cosineCoefficient;
		this.sineCoefficient = sineCoefficient;
		this.constantCoefficient = constantCoefficient;
		this.zeroPointOffset = zeroPointOffset;
	}

	/**
	 * @return the harmonic
	 */
	public Harmonic getHarmonic() {
		return harmonic;
	}

	/**
	 * @return the frequency
	 */
	public double getFrequency() {
		return harmonic.getFrequency();
	}

	/**
	 * @return the harmonic number of the corresponding frequency
	 */
	public int getHarmonicNumber() {
		return harmonic.getHarmonicNumber();
	}

	/**
	 * @return the period
	 */
	public double getPeriod() {
		return harmonic.getPeriod();
	}

	/**
	 * @return the amplitude
	 */
	public double getAmplitude() {
		return amplitude;
	}

	/**
	 * Get the relative amplitude given the first amplitude to which the current
	 * amplitude is relative.
	 * 
	 * @param firstAmplitude
	 *            The amplitude to which the current parameter's amplitude
	 *            should be taken to be relative.
	 * @return The relative amplitude.
	 */
	public double getRelativeAmplitude(double firstAmplitude) {
		return amplitude / firstAmplitude;
	}

	/**
	 * @return the phase in radians
	 */
	public double getPhase() {
		return phase;
	}

	/**
	 * Get the relative phase in radians given the first phase to which the
	 * current phase is relative. The result is adjusted to be in the range
	 * 0..2PI.
	 * 
	 * @param firstPhase
	 *            The phase to which the current parameter's phase should be
	 *            taken to be relative.
	 * @return The relative phase in radians.
	 */
	public double getRelativePhase(double firstPhase) {
		double result = phase - harmonic.getHarmonicNumber() * firstPhase;

		while (result < 0) {
			result += TWOPI;
		}

		while (result > TWOPI) {
			result -= TWOPI;
		}

		return result;
	}

	/**
	 * Get the relative phase in cycles (radians/2PI) given the first phase to
	 * which the current phase is relative.
	 * 
	 * @param firstPhase
	 *            The phase to which the current parameter's phase should be
	 *            taken to be relative.
	 * @return The relative phase in cycles.
	 */
	public double getRelativePhaseInCycles(double firstPhase) {
		return getRelativePhase(firstPhase) / TWOPI;
	}

	/**
	 * @return the sineCoefficient
	 */
	public double getSineCoefficient() {
		return sineCoefficient;
	}

	/**
	 * @return the cosineCoefficient
	 */
	public double getCosineCoefficient() {
		return cosineCoefficient;
	}

	/**
	 * @return the constantCoefficient
	 */
	public double getConstantCoefficient() {
		return constantCoefficient;
	}

	/**
	 * @return the zeroPointOffset
	 */
	public double getZeroPointOffset() {
		return zeroPointOffset;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object) Equality of parameters to
	 *      4 decimal places.
	 */
	@Override
	public boolean equals(Object other) {
		boolean equal = other instanceof PeriodFitParameters;

		if (equal) {
			PeriodFitParameters params = (PeriodFitParameters) other;

			equal &= String.format("%1.4f", params.getFrequency()).equals(
					String.format("%1.4f", getFrequency()));

			equal &= String.format("%1.4f", params.getPeriod()).equals(
					String.format("%1.4f", getPeriod()));

			equal &= String.format("%1.4f", params.getAmplitude()).equals(
					String.format("%1.4f", amplitude));

			equal &= String.format("%1.4f", params.getCosineCoefficient())
					.equals(String.format("%1.4f", cosineCoefficient));

			equal &= String.format("%1.4f", params.getSineCoefficient())
					.equals(String.format("%1.4f", sineCoefficient));

			equal &= String.format("%1.4f", params.getConstantCoefficient())
					.equals(String.format("%1.4f", constantCoefficient));

			equal &= String.format("%1.4f", params.getZeroPointOffset())
					.equals(String.format("%1.4f", zeroPointOffset));
		}

		return equal;
	}

	public String toProsaicString() {
		String str = "parameters: ";

		str += "frequency=";
		str += NumericPrecisionPrefs.formatOther(getFrequency());
		str += ", ";

		str += "period=";
		str += NumericPrecisionPrefs.formatOther(getPeriod());
		str += ", ";

		str += "amplitude=";
		str += NumericPrecisionPrefs.formatOther(amplitude);
		str += ", ";

		str += "cosine coefficient=";
		str += NumericPrecisionPrefs.formatOther(cosineCoefficient);
		str += ", ";

		str += "sine coefficient=";
		str += NumericPrecisionPrefs.formatOther(sineCoefficient);
		str += ", ";

		str += "constant coefficient=";
		str += NumericPrecisionPrefs.formatOther(constantCoefficient);

		str += "zero point offset=";
		str += NumericPrecisionPrefs.formatTime(zeroPointOffset);

		return str;
	}

	public String toString() {
		String str = cosineCoefficient >= 0 ? "+" : "";

		String sincosParam = "2*PI*" + harmonic + "*(t-zeroPoint" + ")";

		str += NumericPrecisionPrefs.formatOther(cosineCoefficient)
				+ " * cos(";
		str += sincosParam;
		str += ")";

		str += sineCoefficient >= 0 ? " + " : "";

		str += NumericPrecisionPrefs.formatOther(sineCoefficient)
				+ " * sin(";
		str += sincosParam;
		str += ")";

		return str;
	}

	public String toExcelString() {
		String str = null;

		str = cosineCoefficient != 0 ? NumericPrecisionPrefs.getExcelFormulaSeparator() + "\n" : "\n";

		String sincosParam = "2*PI()*" + harmonic + "*(A1-"
				+ NumericPrecisionPrefs.formatTime(zeroPointOffset) + ")";

		str += NumericPrecisionPrefs.formatOther(cosineCoefficient) + " * COS(";
		str += sincosParam;
		str += ")";

		str += sineCoefficient != 0 ? NumericPrecisionPrefs.getExcelFormulaSeparator() + "\n" : "\n";

		str += NumericPrecisionPrefs.formatOther(sineCoefficient) + " * SIN(";
		str += sincosParam;
		str += ")";

		return str;
	}

	// toRString must be locale-independent!
	public String toRString() {
		String str = "+\n";

		// harmonic.toString() is locale-dependent!
		String sincosParam = "2*pi*" + NumericPrecisionPrefs.formatOtherLocaleIndependent(harmonic.getFrequency()) + "*(t-zeroPoint" + ")";

		str += NumericPrecisionPrefs.formatOtherLocaleIndependent(cosineCoefficient) + " * cos(";
		str += sincosParam;
		str += ")";

		str += sineCoefficient >= 0 ? " + " : "";

		str += NumericPrecisionPrefs.formatOtherLocaleIndependent(sineCoefficient) + " * sin(";
		str += sincosParam;
		str += ")";

		return str;
	}

	public double toValue(double t) {
		double sincosParam = 2 * Math.PI * harmonic.getFrequency()
				* (t - zeroPointOffset);
		return cosineCoefficient * Math.cos(sincosParam) + sineCoefficient
				* Math.sin(sincosParam);
	}

	@Override
	public int compareTo(PeriodFitParameters other) {
		return getHarmonic().compareTo(other.getHarmonic());
	}
}
