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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.util.DescStats;

/**
 * This class is a model that represents a series of valid variable star
 * observations, e.g. for different bands (or from different sources).
 */
public class ObservationAndMeanPlotModel extends ObservationPlotModel {

	private static final String MEANS_SERIES_NAME = "Means";

	/**
	 * Constructor
	 * 
	 * We add a named observation source list to a unique series number.
	 * 
	 * @param name
	 *            Name of observation source list.
	 * @param obsSourceList
	 *            The list of observation sources.
	 */
	// public ObservationAndMeanPlotModel(String name,
	// List<ValidObservation> obsSourceList) {
	// super(name, obsSourceList);
	// }

	/**
	 * Constructor
	 * 
	 * We add named observation source lists to unique series numbers.
	 * Then we add the initial mean-based series.
	 * 
	 * @param observations
	 *            The complete list of valid observations.
	 * @param obsSourceListMap
	 *            A mapping from source name to lists of observation sources.
	 */
	public ObservationAndMeanPlotModel(List<ValidObservation> observations,
			Map<String, List<ValidObservation>> obsSourceListMap) {
		super(obsSourceListMap);
		this.addInitialMeanSeries(observations);
	}

	/**
	 * Add a mean-based series with the specified bin size.
	 * 
	 * @param observations
	 *            A sequence of valid observations from which to select bins.
	 * @param binSize
	 *            The number of elements in the bin.
	 */
	public void addMeanSeries(List<ValidObservation> observations, int binSize) {

		List<ValidObservation> meanObsList = DescStats
				.createdBinnedObservations(observations, binSize);

		boolean found = false;

		for (Map.Entry<Integer, String> entry : this.seriesNumToSrcNameMap
				.entrySet()) {
			if (MEANS_SERIES_NAME.equals(entry.getValue())) {
				int series = entry.getKey();
				this.seriesNumToObSrcListMap.put(series, meanObsList);
				this.fireDatasetChanged();
				found = true;
				break;
			}
		}

		// Is this the first time the means series has been added?
		if (!found) {
			this.addObservationSeries(MEANS_SERIES_NAME, meanObsList);
		}
	}

	/**
	 * Add a mean-based series using a default bin size.
	 * 
	 * @param observations
	 *            A sequence of valid observations from which to select bins.
	 */
	public void addInitialMeanSeries(List<ValidObservation> observations) {
		// Determine default bin size as a percentage of observations.
		// TODO: We may need to change this to be a JD-based bin size
		// (number of days) instead of number of elements in bin, as
		// per specification.
		int binSize = observations.size() * DescStats.DEFAULT_BIN_PERCENTAGE
				/ 100;

		if (binSize >= 1) {
			addMeanSeries(observations, binSize);
		}

		// TODO: otherwise throw an exception?
	}

	/**
	 * Which series' elements should be joined visually (e.g. with lines)?
	 * 
	 * @return An array of series numbers for series whose elements should be
	 *         joined visually.
	 */
	public int[] getSeriesWhoseElementsShouldBeJoinedVisually() {
		List<Integer> seriesNumList = new ArrayList<Integer>();

		for (Map.Entry<Integer, String> entry : this.seriesNumToSrcNameMap
				.entrySet()) {
			if (MEANS_SERIES_NAME.equals(entry.getValue())) {
				seriesNumList.add(entry.getKey());
				break;
			}
		}

		int[] seriesNums = new int[seriesNumList.size()];
		int i = 0;
		for (Integer series : seriesNumList) {
			seriesNums[i++] = series;
		}

		return seriesNums;
	}

	/**
	 * Return the error associated with the magnitude. We skip the series and
	 * item legality check to improve performance on the assumption that this
	 * has been checked already when calling getMagAsYCoord(). So this is a
	 * precondition of calling the current function.
	 * 
	 * @param series
	 *            The series number.
	 * @param item
	 *            The item number within the series.
	 * @return The error value associated with the mean.
	 */
	protected double getMagError(int series, int item) {

		// TODO: handle Means series!

		return super.getMagError(series, item);
	}
}
