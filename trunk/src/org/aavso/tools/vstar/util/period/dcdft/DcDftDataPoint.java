/**
 * VStar: a statistical analysis tool for variable star data.
 * Copyright (C) 2009  AAVSO (http://www.aavso.org/)
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
package org.aavso.tools.vstar.util.period.dcdft;

/**
 * This class represents a DC DFT data-point to be plotted on a chart.
 */
public class DcDftDataPoint {
	private double frequency, period, power, amplitude;
	
	/**
	 * Constructor
	 * 
	 * @param frequency
	 * @param period
	 * @param power
	 * @param amplitude
	 */
	public DcDftDataPoint(double frequency, double period, double power,
			double amplitude) {
		super();
		this.frequency = frequency;
		this.period = period;
		this.power = power;
		this.amplitude = amplitude;
	}

	/**
	 * @return the frequency
	 */
	public double getFrequency() {
		return frequency;
	}

	/**
	 * @return the period
	 */
	public double getPeriod() {
		return period;
	}

	/**
	 * @return the power
	 */
	public double getPower() {
		return power;
	}

	/**
	 * @return the amplitude
	 */
	public double getAmplitude() {
		return amplitude;
	}

	public String toString() {
		return String.format("%14.9f%10.4f%10.4f%10.4f", frequency, period, power, amplitude);
	}
}
