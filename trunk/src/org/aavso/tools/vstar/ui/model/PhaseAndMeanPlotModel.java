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
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.aavso.tools.vstar.data.ValidObservation;

/**
 * This class is a model that represents a series of valid variable star
 * observations, e.g. for different bands (or from different sources) along with
 * a means series that can change over time. The means series requires special
 * handling for a standard phase plot.
 */
public class PhaseAndMeanPlotModel extends ObservationAndMeanPlotModel {

	private List<Integer> joinedSeriesNumList = new ArrayList<Integer>();

	/**
	 * Constructor
	 * 
	 * We add named observation source lists to unique series numbers. Then we
	 * add the initial mean-based series.
	 * 
	 * @param obsSourceListMap
	 *            A mapping from source name to lists of observation sources.
	 * @param coordSrc
	 *            coordinate and error source.
	 */
	public PhaseAndMeanPlotModel(
			Map<String, List<ValidObservation>> obsSourceListMap,
			ICoordSource coordSrc) {
		super(obsSourceListMap, coordSrc);
		this.joinedSeriesNumList = new ArrayList<Integer>();
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
