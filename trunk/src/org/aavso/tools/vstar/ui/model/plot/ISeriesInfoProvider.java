/**
 * VStar: a statistical analysis tool for variable star data.
 * Copyright (C) 2013  AAVSO (http://www.aavso.org/)
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
import java.util.Set;

import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;

/**
 * Realisations of this interface provide information about currently available
 * series with respect to existence, visibility and corresponding observations.
 */
public interface ISeriesInfoProvider {

	/**
	 * What is the current set of visible series?
	 * 
	 * @return The current set of visible series?
	 */
	public abstract Set<SeriesType> getVisibleSeries();

	/**
	 * @see org.jfree.data.general.AbstractSeriesDataset#getSeriesCount()
	 * @param Return
	 *            the number of observation series that exist on the plot.
	 */
	public abstract int getSeriesCount();

	/**
	 * Get a set of series keys.
	 * 
	 * @return The set of series keys.
	 */
	public abstract Set<SeriesType> getSeriesKeys();

	/**
	 * Does the specified series type exist, i.e. has it been added to the plot?
	 * 
	 * @param type
	 *            The series type in question.
	 * @return Whether the series has been added to the plot.
	 */
	public abstract boolean seriesExists(SeriesType type);

	/**
	 * Get the observations for the specified series.
	 * 
	 * @param type
	 *            The series type for which observations are requested.
	 * @return The observation list for the specified series; may be null.
	 */
	public abstract List<ValidObservation> getObservations(SeriesType type);
}