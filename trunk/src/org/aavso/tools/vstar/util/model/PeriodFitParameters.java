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

/**
 * This class represents a single period-based fit parameter.
 * 
 * Such a parameter could be used to re-create a model fit.
 */
public class PeriodFitParameters {

	private double frequency;
	private double period;
	private double amplitude;
	private double sineCoefficient;
	private double cosineCoefficient;
	private double constantCoefficient;

	/**
	 * Constructor.
	 * 
	 * @param frequency
	 * @param period
	 * @param amplitude
	 * @param sineCoefficient
	 * @param cosineCoefficient
	 * @param constantCoefficient
	 */
	public PeriodFitParameters(double frequency, double period,
			double amplitude, double cosineCoefficient,
			double sineCoefficient, double constantCoefficient) {
		this.frequency = frequency;
		this.period = period;
		this.amplitude = amplitude;
		this.cosineCoefficient = cosineCoefficient;
		this.sineCoefficient = sineCoefficient;
		this.constantCoefficient = constantCoefficient;
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
}
