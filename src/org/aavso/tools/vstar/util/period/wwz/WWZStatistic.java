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
package org.aavso.tools.vstar.util.period.wwz;

import org.aavso.tools.vstar.util.period.IPeriodAnalysisDatum;
import org.aavso.tools.vstar.util.period.PeriodAnalysisCoordinateType;

/**
 * A single Weighted Wavelet Transform datapoint.
 * 
 * The comments for each data member below are taken from the WWZ
 * documentation, (C) AAVSO.
 * 
 * @see org.aavso.tools.vstar.util.period.wwz.WeightedWaveletZTransform
 */
public class WWZStatistic implements IPeriodAnalysisDatum {

	/** The time being examined, in time units. */
	private double tau;

	/** The frequency being tested, in cycles per time unit. */
	private double freq;

	/**
	 * Value of the WWZ; this is approximately an F-statistic with N(eff) and 2
	 * degrees of freedom, and expected value 1. It indicates whether or not
	 * there is a periodic fluctuation at the given time, of the given
	 * frequency.
	 */
	private double wwz;

	/**
	 * Weighted wavelet amplitude; if the signal is periodic at the frequency
	 * being tested, this gives the (real semi-) amplitude of the corresponding
	 * best-fit sinusoid.
	 */
	private double amp;

	/**
	 * Mean apparent magnitude of the object at time tau.
	 */
	private double mave;

	/**
	 * The effective number of data for the given time and frequency being
	 * tested.
	 */
	private double neff;

	/**
	 * Constructor.
	 * 
	 * @param tau
	 *            The time being examined, in time units.
	 * @param freq
	 *            The frequency being tested, in cycles per time unit.
	 * @param wwz
	 *            Value of the WWZ.
	 * @param amp
	 *            Weighted wavelet amplitude.
	 * @param mave
	 *            Mean apparent magnitude of the object at time tau.
	 * @param neff
	 *            The effective number of data for the given time and frequency.
	 */
	public WWZStatistic(double tau, double freq, double wwz, double amp,
			double mave, double neff) {
		this.tau = tau;
		this.freq = freq;
		this.wwz = wwz;
		this.amp = amp;
		this.mave = mave;
		this.neff = neff;
	}

	/**
	 * @return the tau
	 */
	public double getTau() {
		return tau;
	}

	/**
	 * @return the freq
	 */
	public double getFrequency() {
		return freq;
	}

	/**
	 * @return the period (reciprocal of the frequency).
	 */
	public double getPeriod() {
		return 1.0 / freq;
	}

	/**
	 * @return the wwz
	 */
	public double getWwz() {
		return wwz;
	}

	/**
	 * @return the amp
	 */
	public double getAmplitude() {
		return amp;
	}

	/**
	 * @return the mave
	 */
	public double getMave() {
		return mave;
	}

	/**
	 * @return the neff
	 */
	public double getNeff() {
		return neff;
	}

	// Remaining methods from IPeriodAnalysisDatum

	@Override
	public double getPower() {
		return getWwz();
	}

	@Override
	public boolean equals(Object other) {
		boolean isEqual = false;

		if (other instanceof WWZStatistic) {
			WWZStatistic that = (WWZStatistic) other;
			isEqual = this.tau == that.tau && this.freq == that.freq
					&& this.wwz == that.wwz && this.amp == that.amp
					&& this.mave == that.mave && this.neff == that.neff;
		}

		return isEqual;
	}

	@Override
	public String toString() {
		return String
				.format("tau=%1.4f, freq=%1.4f, wwz=%1.4f, amp=%1.4f, mave=%1.4f, neff=%1.4f",
						tau, freq, wwz, amp, mave, neff);
	}

	/**
	 * Given a coordinate type, return the corresponding value from this
	 * statistic object.
	 * 
	 * @param type
	 *            The coordinate type.
	 * @return The value of that coordinate for this instance.
	 */
	public final double getValue(WWZCoordinateType type) {
		double value = 0;

		switch (type) {
		case TAU:
			value = getTau();
			break;
		case FREQUENCY:
			value = getFrequency();
			break;
		case PERIOD:
			value = getPeriod();
			break;
		case WWZ:
			value = getWwz();
			break;
		case SEMI_AMPLITUDE:
			value = getAmplitude();
			break;
		case MEAN_MAG:
			value = getMave();
			break;
		case EFFECTIVE_NUM_DATA:
			value = getNeff();
			break;
		}

		return value;
	}

	@Override
	// Required by IPeriodAnalysisDatum implement clause; probably unused by
	// caller.
	// TODO: use additional PeriodAnalysisCoordinateType, abolishing the need
	// for WWZCoordinateType.
	public double getValue(PeriodAnalysisCoordinateType type) {
		double value = 0;

		if (type == PeriodAnalysisCoordinateType.FREQUENCY) {
			value = getFrequency();
		} else if (type == PeriodAnalysisCoordinateType.PERIOD) {
			value = getPeriod();
		} else if (type == PeriodAnalysisCoordinateType.AMPLITUDE) {
			value = getAmplitude();
		} else if (type == PeriodAnalysisCoordinateType.POWER) {
			value = getPower();
		}

		return value;
	}
}
