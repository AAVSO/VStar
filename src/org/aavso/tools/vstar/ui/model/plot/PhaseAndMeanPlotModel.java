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
package org.aavso.tools.vstar.ui.model.plot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.util.stats.DescStats;
import org.aavso.tools.vstar.util.stats.PhaseCalcs;

/**
 * This class is a model that represents a series of valid variable star
 * observations, e.g. for different bands (or from different sources) along with
 * a means series that can change over time. The means series requires special
 * handling for a standard phase plot.
 */
public class PhaseAndMeanPlotModel extends ObservationAndMeanPlotModel {

	/**
	 * Duplicated mean observations for previous and standard phases. We do this
	 * for the pragmatic reason that not doing so will cause a mean series plot
	 * to eat its tail (i.e. a circuit).
	 */
	
	// TODO: temporary
	private List<Integer> joinedSeriesNumList = new ArrayList<Integer>();

	/**
	 * Constructor
	 * 
	 * We add named observation source lists to unique series numbers. Then we
	 * add the initial mean-based series.
	 * 
	 * @param obsSourceListMap
	 *            A mapping from source series to lists of observation sources.
	 * @param coordSrc
	 *            coordinate and error source.
	 * @param timeElementEntity
	 *            A time element source for observations.
	 */
	public PhaseAndMeanPlotModel(
			Map<SeriesType, List<ValidObservation>> obsSourceListMap,
			ICoordSource coordSrc, ITimeElementEntity timeElementEntity) {
		super(obsSourceListMap, coordSrc, timeElementEntity);
		this.joinedSeriesNumList = new ArrayList<Integer>();
	}

	/**
	 * Set the mean-based series.
	 * 
	 * This method creates a new means series based upon the current mean source
	 * series index and time-elements-in-bin. It then updates the view and any listeners.
	 */
	public void setMeanSeries() {

		meanObsList = DescStats.createSymmetricBinnedObservations(
				seriesNumToObSrcListMap.get(meanSourceSeriesNum),
				timeElementEntity, timeElementsInBin);

		// As long as there were enough observations to create a means list
		// to make a "means" series, we do so.
		if (!meanObsList.isEmpty()) {
			boolean found = false;

			// TODO: instead of this, why not just ask:
			// if (this.meansSeriesNum != NO_MEANS_SERIES) {
			// ...
			// } else {
			// ...do the if (!found) code below...
			// }
			for (Map.Entry<Integer, SeriesType> entry : this.seriesNumToSrcTypeMap
					.entrySet()) {
				if (SeriesType.MEANS.equals(entry.getValue())) {
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
						SeriesType.MEANS, meanObsList);

				// Make sure it's rendered!
				this.getSeriesVisibilityMap().put(this.meansSeriesNum, true);
			}

			// Notify listeners.
			this.meansChangeNotifier.notifyListeners(meanObsList);

		} else {
			// TODO: remove empty check; should never happen because of way
			// binning is done
		}
	}

	/**
	 * No series elements should be joined visually.
	 * This is a temporary solution.
	 */
	public Collection<Integer> getSeriesWhoseElementsShouldBeJoinedVisually() {
		return this.joinedSeriesNumList;
		//return super.getSeriesWhoseElementsShouldBeJoinedVisually();
	}
}
