/**
 * VStar: a statistical analysis tool for variable star data.
 * Copyright (C) 2010  AAVSO (http://www.aavso.org/)
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
package org.aavso.tools.vstar.ui.dialog;

import org.aavso.tools.vstar.ui.NamedComponent;
import org.aavso.tools.vstar.ui.mediator.AnalysisType;
import org.aavso.tools.vstar.ui.model.plot.PhaseTimeElementEntity;
import org.aavso.tools.vstar.ui.pane.plot.NewPhasePlotButtonPane;
import org.aavso.tools.vstar.ui.pane.plot.PhaseAndMeanPlotPane;
import org.aavso.tools.vstar.ui.pane.plot.TimeElementsInBinSettingPane;

/**
 * The phase plot control dialog.
 */
public class PhasePlotControlDialog extends PlotControlDialogBase {

	/**
	 * Constructor.
	 * 
	 * @param plotPane
	 *            The plot pane.
	 */
	public PhasePlotControlDialog(PhaseAndMeanPlotPane plotPane) {
		super("Phase Plot Control", plotPane, new TimeElementsInBinSettingPane(
				"Phase Steps per Mean Series Bin", plotPane.getObsModel(),
				PhaseTimeElementEntity.instance), new NamedComponent(
				"Phase Plot", new NewPhasePlotButtonPane(plotPane)),
				AnalysisType.PHASE_PLOT);
	}
}
