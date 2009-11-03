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
package org.aavso.tools.vstar.ui.model;

import java.util.List;
import java.util.Map;

import org.aavso.tools.vstar.data.ValidObservation;

/**
 * A phase based coordinate source.
 */
public class PhaseCoordSource implements ICoordSource {

	/**
	 * Twice the number of items in the map, since we want to facilitate a
	 * Standard Phase Diagram where the phase ranges over -1..1 inclusive.
	 * 
	 * @param series
	 *            The series of interest.
	 * @return The number of items in this series.
	 */
	public int getItemCount(int series,
			Map<Integer, List<ValidObservation>> seriesNumToObSrcListMap) {

		if (!seriesNumToObSrcListMap.containsKey(series)) {
			throw new IllegalArgumentException("Series number '" + series
					+ "' out of range.");
		}

		return seriesNumToObSrcListMap.get(series).size() * 2;
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
	 * @return The X coordinate (phase).
	 */
	public double getXCoord(int series, int item,
			Map<Integer, List<ValidObservation>> seriesNumToObSrcListMap) {

		if (!seriesNumToObSrcListMap.containsKey(series)) {
			throw new IllegalArgumentException("Series number '" + series
					+ "' out of range.");
		}

		if (item >= seriesNumToObSrcListMap.get(series).size() * 2) {
			throw new IllegalArgumentException("Item number '" + item
					+ "' out of range.");
		}

		// Everything is modulo the number of elements in the series.
		double phase = -99;
		int itemCount = seriesNumToObSrcListMap.get(series).size();
		if (item < itemCount) {
			List<ValidObservation> obs;
			ValidObservation ob;
			Double ph;
			try {
				// -1..
				obs = seriesNumToObSrcListMap
						.get(series);
				ob = obs.get(item);
				ph = ob.getPreviousCyclePhase();
				phase = ph;
				//phase = seriesNumToObSrcListMap.get(series).get(item)
				//		.getPreviousCyclePhase();
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
		} else {
			// 0..1
			phase = seriesNumToObSrcListMap.get(series).get(item % itemCount)
					.getStandardPhase();
		}

		return phase;
	}

	/**
	 * The actual item number for the Y coordinate is modulo the number of
	 * elements in the series in this case.
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
		return item % seriesNumToObSrcListMap.get(series).size();
	}
}
