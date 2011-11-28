/**
 * VStar: a statistical analysis tool for variable star data.
 * Copyright (C) 2011  AAVSO (http://www.aavso.org/)
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
 * This class represents an Nth harmonic. Conversion between frequency and
 * period is permitted.
 */
public class Harmonic implements Comparable<Harmonic> {

	public final static int FUNDAMENTAL = 1;

	private double frequency;
	private int harmonic;
	private String str;

	/**
	 * Constructor
	 * 
	 * @param frequency
	 *            The particular frequency.
	 * @param harmonic
	 *            The harmonic represented by this frequency (1 = fundamental).
	 */
	public Harmonic(double frequency, int harmonic) {
		if (frequency <= 0) {
			throw new IllegalArgumentException("Invalid frequency: "
					+ frequency);
		}

		if (harmonic < FUNDAMENTAL) {
			throw new IllegalArgumentException("Invalid harmonic: " + harmonic);
		}

		this.frequency = frequency;
		this.harmonic = harmonic;
		str = null;
	}

	/**
	 * Construct a Harmonic that represents the fundamental.
	 * 
	 * @param frequency
	 *            The particular frequency.
	 */
	public Harmonic(double frequency) {
		this(frequency, FUNDAMENTAL);
	}

	/**
	 * @return the frequency
	 */
	public double getFrequency() {
		return frequency;
	}

	/**
	 * @return the harmonic
	 */
	public int getHarmonicNumber() {
		return harmonic;
	}

	/**
	 * Does this represent a fundamental frequency?
	 * 
	 * @return Is this a fundamental frequency?
	 */
	public boolean isFundamental() {
		return harmonic == FUNDAMENTAL;
	}

	/**
	 * @return The period (reciprocal of frequency).
	 */
	public double getPeriod() {
		return 1.0 / frequency;
	}

	/**
	 * Return the fundamental frequency. If the harmonic number is 1, this is
	 * just the frequency itself.
	 * 
	 * @return The fundamental frequency.
	 */
	public double getFundamentalFrequency() {
		return frequency / harmonic;
	}

	/**
	 * Is the specified harmonic a multiple of this one and the current one is a
	 * fundamental.
	 * 
	 * @param other
	 *            The other harmonic to check.
	 * @return Whether the specified harmonic is a multiple of this one.
	 */
	public boolean isHarmonic(Harmonic other) {
		return isFundamental()
				&& frequency * other.getHarmonicNumber() == other
						.getFrequency();
	}

	public String toString() {
		if (str == null) {
			str = String.format(NumericPrecisionPrefs.getOtherOutputFormat(),
					frequency);
		}

		return str;
	}

	@Override
	public int compareTo(Harmonic other) {
		int result = 0;

		if (frequency < other.getFrequency()) {
			result = -1;
		} else if (frequency > other.getFrequency()) {
			result = 1;
		}

		return result;
	}
}
