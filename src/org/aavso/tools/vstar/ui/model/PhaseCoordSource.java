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
	 * Duplicated mean observations for previous and standard phases. We do this
	 * for the pragmatic reason that not doing so will cause a mean series plot
	 * to eat its tail (i.e. a circuit).
	 * 
	 * TODO: actually, this solution is completely bogus! The only ways to solve
	 * this is either: o subclass the mean obs model and have *two* means
	 * series! o disable joining of means series for phase plots, possibly also
	 * via a subclass => start with the 2nd and if time permits, do the first;
	 * focus on dialog first!
	 */
	// private List<ValidObservation> meanObsPrevious; // for phases -1..<0
	// private List<ValidObservation> meanObsStandard; // for phases 0..1

	/**
	 * The series item number of the mean series. We use this to distinguish the
	 * mean series from all others and give it special treatment.
	 */
	// private int meanSeriesNum;

	/**
	 * Set the mean obs list and series number and set the phases for each
	 * observation.
	 * 
	 * @param meanObs
	 *            The list of mean observations.
	 * @param meanSeriesNum
	 *            The series number of the mean.
	 * @param epoch
	 *            The epoch to be used for phase calculations.
	 * @param period
	 *            The period to be used for phase calculations.
	 */
	// public void setMeanObs(List<ValidObservation> meanObs, int meanSeriesNum,
	// double epoch, double period) {

	// TODO: meanObs should be a notifying list with which we register!
	// If we do this, we will need to turn off notifications for setPhases()
	// above! >:^/
	// this.meanObsPrevious = new ArrayList<ValidObservation>();
	// this.meanObsPrevious.addAll(meanObs);
	//
	// this.meanObsStandard = new ArrayList<ValidObservation>();
	// this.meanObsStandard.addAll(meanObs);
	//
	// this.meanSeriesNum = meanSeriesNum;
	// }

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

		// if (!seriesNumToObSrcListMap.containsKey(series)) {
		// throw new IllegalArgumentException("Series number '" + series
		// + "' out of range.");
		// }
		//
		// if (item >= seriesNumToObSrcListMap.get(series).size() * 2) {
		// throw new IllegalArgumentException("Item number '" + item
		// + "' out of range.");
		// }

		// Everything is modulo the number of elements in the series
		// except the means series which we treat separately.
		double phase = -99;
		int itemCount = seriesNumToObSrcListMap.get(series).size();
		if (item < itemCount) {
			// -1..<0
			// if (series == this.meanSeriesNum) {
			// phase = this.meanObsPrevious.get(item).getPreviousCyclePhase();
			// } else {
			phase = seriesNumToObSrcListMap.get(series).get(item)
					.getPreviousCyclePhase();
			// }
		} else {
			// 0..1
			// if (series == this.meanSeriesNum) {
			// phase = this.meanObsStandard.get(item %
			// itemCount).getStandardPhase();
			// } else {
			phase = seriesNumToObSrcListMap.get(series).get(item % itemCount)
					.getStandardPhase();
			// }
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
		return seriesNumToObSrcListMap.get(series).get(
				item % seriesNumToObSrcListMap.get(series).size());
	}
}
