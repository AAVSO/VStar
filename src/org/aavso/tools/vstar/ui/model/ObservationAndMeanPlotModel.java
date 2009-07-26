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
import org.aavso.tools.vstar.util.Notifier;

/**
 * This class is a model that represents a series of valid variable star
 * observations, e.g. for different bands (or from different sources).
 */
public class ObservationAndMeanPlotModel extends ObservationPlotModel {

	public static final String MEANS_SERIES_NAME = "Means";

	public static final int NO_MEANS_SERIES = -1;

	private int meansSeriesNum;

	private double daysInBin;

	private List<ValidObservation> observations;

	private List<ValidObservation> meanObsList;
	
	private Notifier<List<ValidObservation>> meansChangeNotifier;

	/**
	 * Constructor
	 * 
	 * We add named observation source lists to unique series numbers. Then we
	 * add the initial mean-based series.
	 * 
	 * @param observations
	 *            The complete list of valid observations.
	 * @param obsSourceListMap
	 *            A mapping from source name to lists of observation sources.
	 */
	public ObservationAndMeanPlotModel(List<ValidObservation> observations,
			Map<String, List<ValidObservation>> obsSourceListMap) {
		super(obsSourceListMap);
		this.observations = observations;
		this.meansSeriesNum = NO_MEANS_SERIES;
		this.daysInBin = DescStats.DEFAULT_BIN_DAYS; // TODO: or just define
		// this in this class?

		this.meansChangeNotifier = new Notifier<List<ValidObservation>>();

		this.setMeanSeries();
	}

	/**
	 * Set the mean-based series with the specified bin size.
	 * 
	 * @param daysInBin
	 *            The number of days in the bin.
	 */
	public void setMeanSeries() {

		meanObsList = DescStats.createdBinnedObservations(observations,
				daysInBin);

		// As long as there were enough observations to create a means list
		// to make a "means" series, we do so.
		if (!meanObsList.isEmpty()) {
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
				this.meansSeriesNum = this.addObservationSeries(
						MEANS_SERIES_NAME, meanObsList);
			}

			// Notify listeners.
			this.meansChangeNotifier.notifyListeners(meanObsList);

		} else {
			// TODO: remove empty check; should never happen because of way
			// binning is done
		}
	}

	public void changeMeansSeries(double daysInBin) {
		this.daysInBin = daysInBin;
		this.setMeanSeries();
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
		if (series != this.meansSeriesNum) {
			// The series is something other than the means series
			// so just default to the superclass behaviour.
			return super.getMagError(series, item);
		} else {
			// For the means series, we store the mean magnitude error
			// value as the magnitude's uncertainty. TODO: change this?
			return this.seriesNumToObSrcListMap.get(series).get(item)
					.getMagnitude().getUncertainty();
		}
	}

	/**
	 * @return the means series number
	 */
	public int getMeansSeriesNum() {
		return meansSeriesNum;
	}

	/**
	 * @return the daysInBin
	 */
	public double getDaysInBin() {
		return daysInBin;
	}

	/**
	 * @return the meanObsList
	 */
	public List<ValidObservation> getMeanObsList() {
		return meanObsList;
	}

	/**
	 * @return the meansChangeNotifier
	 */
	public Notifier<List<ValidObservation>> getMeansChangeNotifier() {
		return meansChangeNotifier;
	}
}
