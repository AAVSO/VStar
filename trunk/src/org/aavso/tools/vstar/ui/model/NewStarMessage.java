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

import org.aavso.tools.vstar.ui.ObservationAndMeanPlotPane;
import org.aavso.tools.vstar.ui.ObservationListPane;
import org.aavso.tools.vstar.ui.ObservationPlotPane;

/**
 * A message class containing new star type and GUI component information.
 */
public class NewStarMessage {

	private NewStarType newStarType;

	// GUI table and chart components.
	// TODO: we could use NamedComponents instead, e.g. to get tooltips
	private ObservationPlotPane obsChartPane;
	private ObservationAndMeanPlotPane obsAndMeanChartPane;
	private ObservationListPane obsTablePane;
	// TODO: add meansTablePane

	/**
	 * Constructor
	 * 
	 * @param newStarType
	 *            The new star type enum.
	 * @param obsChartPane
	 *            The observation plot GUI component.
	 * @param obsWithMeanChartPane
	 *            The observation-and-mean plot GUI component.
	 * @param obsTablePane
	 *            The observation table GUI component.
	 */
	public NewStarMessage(NewStarType newStarType,
			ObservationPlotPane obsChartPane,
			ObservationAndMeanPlotPane obsAndMeanChartPane,
			ObservationListPane obsTablePane) {
		this.newStarType = newStarType;
		
		this.obsChartPane = obsChartPane;
		this.obsAndMeanChartPane = obsAndMeanChartPane;
		
		this.obsTablePane = obsTablePane;
	}

	/**
	 * @return the newStarType
	 */
	public NewStarType getNewStarType() {
		return newStarType;
	}

	/**
	 * @return the obsChartPane
	 */
	public ObservationPlotPane getObsChartPane() {
		return obsChartPane;
	}

	/**
	 * @return the obsTablePane
	 */
	public ObservationListPane getObsTablePane() {
		return obsTablePane;
	}

	/**
	 * @return the obsAndMeanChartPane
	 */
	public ObservationPlotPane getObsAndMeanChartPane() {
		return obsAndMeanChartPane;
	}
}
