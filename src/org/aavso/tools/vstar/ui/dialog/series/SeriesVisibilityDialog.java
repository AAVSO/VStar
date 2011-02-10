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

import javax.swing.JPanel;

import org.aavso.tools.vstar.ui.model.plot.ObservationPlotModel;

/**
 * This dialog permits the visibility of plot series to be changed.
 * 
 * @deprecated
 */
public class SeriesVisibilityDialog extends AbstractSeriesVisibilityDialog<JPanel> {

	public SeriesVisibilityDialog(ObservationPlotModel obsPlotModel) {
		super(obsPlotModel);
	}
}
