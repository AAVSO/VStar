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
package org.aavso.tools.vstar.ui.pane.plot;

import java.awt.Dimension;

import org.aavso.tools.vstar.ui.model.plot.ObservationPlotModel;

/**
 * This class represents a chart pane containing a raw plot for a set of valid
 * observations (magnitude vs Julian Day).
 */
abstract public class ObservationPlotPane extends
		AbstractObservationPlotPane<ObservationPlotModel> {

	/**
	 * Constructor.
	 * 
	 * @param title
	 *            The title of the plot.
	 * @param subTitle
	 *            The sub-title for the chart.
	 * @param domainTitle
	 *            The domain title (e.g. Julian Date, phase).
	 * @param rangeTitle
	 *            The range title (e.g. magnitude).
	 * @param obsModel
	 *            The observation model.
	 * @param bounds
	 *            The bounds of the pane.
	 */
	public ObservationPlotPane(String title, String subTitle,
			String domainTitle, String rangeTitle,
			ObservationPlotModel obsModel, Dimension bounds) {
		super(title, subTitle, domainTitle, rangeTitle, obsModel, bounds);
	}

	/**
	 * Constructor.
	 * 
	 * @param title
	 *            The title of the plot.
	 * @param subTitle
	 *            The sub-title for the chart.
	 * @param obsModel
	 *            The observation model.
	 * @param bounds
	 *            The bounds of the pane.
	 */
	public ObservationPlotPane(String title, String subTitle,
			ObservationPlotModel obsModel, Dimension bounds) {
		this(title, subTitle, JD_TITLE, MAG_TITLE, obsModel, bounds);
	}
}
