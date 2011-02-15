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
package org.aavso.tools.vstar.ui.dialog.series;

import org.aavso.tools.vstar.ui.model.plot.ObservationAndMeanPlotModel;

/**
 * This class represents a dialog that builds on the series visibility dialog in
 * order to permit selection of the series that is to be the source of the means
 * series. Only one band/series should be used to calculate the mean series.
 * 
 * @deprecated
 */
public class MeanSourceDialog extends AbstractSeriesVisibilityDialog<MeanSourcePane> {

	/**
	 * Constructor
	 * 
	 * @param obsPlotModel
	 *            The plot model.
	 */
	public MeanSourceDialog(ObservationAndMeanPlotModel obsPlotModel) {
		super(obsPlotModel, "Change Series", new MeanSourcePane(obsPlotModel, null));
	}
	
	/**
	 * What is the mean series number?
	 * @return The mean series source number.
	 */
	public int getMeanSeriesSourceNum() {
		return this.getNextPane().getSeriesNum();
	}
}
