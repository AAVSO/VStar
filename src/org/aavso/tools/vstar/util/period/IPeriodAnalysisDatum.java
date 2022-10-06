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
package org.aavso.tools.vstar.util.period;

/**
 * This interface defines methods that must be supported by a single period
 * analysis result datum/datapoint/result.
 * 
 * Exactly what "power" and "amplitude" denotes depends upon the source (e.g. DC
 * DFT vs WWZ). TODO: Perhaps this should instead give way to the use of a
 * mapping from names to double values that make sense for a given context.
 */
public interface IPeriodAnalysisDatum {
	/**
	 * @return the frequency
	 */
	public double getFrequency();

	/**
	 * @return the period
	 */
	public double getPeriod();

	/**
	 * @return the power
	 */
	public double getPower();

	/**
	 * @return the amplitude
	 */
	public double getSemiAmplitude();
	
	/**
	 * Retrieve a value by coordinate type.
	 * 
	 * @param type
	 *            The coordinate type.
	 * @return The value.
	 */
	public double getValue(PeriodAnalysisCoordinateType type);
}
