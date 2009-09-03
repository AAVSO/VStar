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
package org.aavso.tools.vstar.ui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import org.aavso.tools.vstar.ui.model.ObservationPlotModel;

/**
 * This class represents a chart pane containing a raw plot for a set 
 * of valid observations (magnitude vs Julian Day).
 */
public class ObservationPlotPane extends
		ObservationPlotPaneBase<ObservationPlotModel> {

	/**
	 * Constructor.
	 * 
	 * @param title The title of the plot.
	 * @param obsModel The observation model.
	 * @param bounds The bounds of the pane.
	 */
	public ObservationPlotPane(String title, ObservationPlotModel obsModel,
			Dimension bounds) {
		super(title, obsModel, bounds);
	}
	
	// Return a listener for the "change series visibility" button.
	protected ActionListener createSeriesChangeButtonListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				invokeSeriesChangeDialog();
			}
		};
	}	
}
