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
package org.aavso.tools.vstar.ui.model.plot;

import java.util.List;
import java.util.Map;

import org.aavso.tools.vstar.data.ValidObservation;

/**
 * A standard phase based coordinate source.
 */
public class StandardPhaseCoordSource implements ICoordSource {

	public static StandardPhaseCoordSource instance = new StandardPhaseCoordSource();

	/**
	 * Get the number of items in the series.
	 * 
	 * @param series
	 *            The series of interest.
	 * @return The number of items in this series.
	 */
	public int getItemCount(int series,
			Map<Integer, List<ValidObservation>> seriesNumToObSrcListMap) {

		return seriesNumToObSrcListMap.get(series).size();
	}

	/**
	 * Get the phase associated with the specified series and item.
	 * 
	 * @param series
	 *            The series of interest.
	 * @param item
	 *            The target item.
	 * @param seriesNumToObSrcListMap
	 *            A mapping from series number to a list of observations.
	 * @return The X coordinate (standard phase).
	 */
	public double getXCoord(int series, int item,
			Map<Integer, List<ValidObservation>> seriesNumToObSrcListMap) {

		double phase;

		try {
			phase = seriesNumToObSrcListMap.get(series).get(item).getStandardPhase();
		} catch (Exception e) {
			phase = Double.NaN;
		}

		return phase;
	}

	/**
	 * Get the phase coordinate value associated with the specified item.
	 * 
	 * @param item
	 *            The target item.
	 * @param obs
	 *            A list of observations.
	 * @return The standard phase.
	 */
	@Override
	public double getXCoord(int item, List<ValidObservation> obs) {
		return obs.get(item).getStandardPhase();
	}

	/**
	 * The actual item number for the Y coordinate is in fact, just item in this
	 * case.
	 * 
	 * @param series
	 *            The series of interest.
	 * @param item
	 *            The target item.
	 * @param seriesNumToObSrcListMap
	 *            A mapping from series number to a list of observations.
	 * @return The actual Y item number.
	 */
	public int getActualYItemNum(int series, int item,
			Map<Integer, List<ValidObservation>> seriesNumToObSrcListMap) {
		return item;
	}

	/**
	 * Given a series and item number, return the corresponding observation.
	 * 
	 * @param series
	 *            The series number.
	 * @param item
	 *            The item within the series.
	 * @param seriesNumToObSrcListMap
	 *            A mapping from series number to a list of observations.
	 * @return The valid observation.
	 * @throws IllegalArgumentException
	 *             if series or item are out of range.
	 */
	public ValidObservation getValidObservation(int series, int item,
			Map<Integer, List<ValidObservation>> seriesNumToObSrcListMap) {

		return seriesNumToObSrcListMap.get(series).get(item);
	}
	
	@Override
	public String getUnit() {
		return "Phase";
	}
}
