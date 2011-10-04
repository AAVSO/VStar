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

public class PeriodFitParameters {

	private Harmonic harmonic;
	private double amplitude;
	private double sineCoefficient;
	private double cosineCoefficient;
	private double constantCoefficient;
	private String str;

	/**
	 * Constructor.
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
	 */
	public PeriodFitParameters(Harmonic harmonic, double amplitude,
			double cosineCoefficient, double sineCoefficient,
			double constantCoefficient) {
		this.harmonic = harmonic;
		this.amplitude = amplitude;
		this.cosineCoefficient = cosineCoefficient;
		this.sineCoefficient = sineCoefficient;
		this.constantCoefficient = constantCoefficient;
		this.str = null;
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
		}

		return equal;
	}

	public String toProsaicString() {
		String str = "parameters: ";

		str += "frequency=";
		str += String.format(NumericPrecisionPrefs.getOtherOutputFormat(),
				getFrequency());
		str += ", ";

		str += "period=";
		str += String.format(NumericPrecisionPrefs.getOtherOutputFormat(),
				getPeriod());
		str += ", ";

		str += "amplitude=";
		str += String.format(NumericPrecisionPrefs.getOtherOutputFormat(),
				amplitude);
		str += ", ";

		str += "cosine coefficient=";
		str += String.format(NumericPrecisionPrefs.getOtherOutputFormat(),
				cosineCoefficient);
		str += ", ";

		str += "sine coefficient=";
		str += String.format(NumericPrecisionPrefs.getOtherOutputFormat(),
				sineCoefficient);
		str += ", ";

		str += "constant coefficient=";
		str += String.format(NumericPrecisionPrefs.getOtherOutputFormat(),
				constantCoefficient);

		return str;
	}

	public String toString() {
		if (str == null) {
			String fmt = NumericPrecisionPrefs.getOtherOutputFormat();
			
			str = cosineCoefficient >= 0 ? "+" : "";
			
			String sincosParam = "2\u03C0" + harmonic + "t";

			str += String.format(fmt, cosineCoefficient) + " \u00D7 cos(";
			str += sincosParam;
			str += ")";

			str += sineCoefficient >= 0 ? "+" : "";

			str += String.format(fmt, sineCoefficient) + " \u00D7 sin(";
			str += sincosParam;
			str += ")";
		}
		
		return str;
	}
}
