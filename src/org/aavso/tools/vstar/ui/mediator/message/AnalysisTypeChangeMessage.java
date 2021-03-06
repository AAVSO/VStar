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
package org.aavso.tools.vstar.ui.mediator.message;

import org.aavso.tools.vstar.ui.mediator.AnalysisType;
import org.aavso.tools.vstar.ui.mediator.ViewModeType;
import org.aavso.tools.vstar.ui.model.list.AbstractMeanObservationTableModel;
import org.aavso.tools.vstar.ui.pane.list.ObservationListPane;
import org.aavso.tools.vstar.ui.pane.list.SyntheticObservationListPane;
import org.aavso.tools.vstar.ui.pane.plot.ObservationAndMeanPlotPane;

/**
 * This message is intended to be sent to listeners when VStar's analysis type
 * changes (raw data, phase plot, period analysis) to provide new artefacts.
 */
public class AnalysisTypeChangeMessage {

	private AnalysisType analysisType;

	// GUI table and chart components.
	private ObservationAndMeanPlotPane obsAndMeanChartPane;
	private ObservationListPane obsListPane;
	private SyntheticObservationListPane<AbstractMeanObservationTableModel> meansListPane;

	// Include the required viewMode.
	private ViewModeType viewMode;

	/**
	 * Constructor.
	 * 
	 * @param analysisType
	 *            The new analysis type.
	 * @param obsWithMeanChartPane
	 *            The observation-and-mean plot GUI component.
	 * @param obsListPane
	 *            The observation table GUI component.
	 * @param meansTablePane
	 *            The means table GUI component.
	 * @param viewMode
	 *            The required viewMode.
	 */
	public AnalysisTypeChangeMessage(
			AnalysisType analysisType,
			ObservationAndMeanPlotPane obsAndMeanChartPane,
			ObservationListPane obsListPane,
			SyntheticObservationListPane<AbstractMeanObservationTableModel> meansListPane,
			ViewModeType viewMode) {
		super();

		this.analysisType = analysisType;
		this.viewMode = viewMode;

		this.obsAndMeanChartPane = obsAndMeanChartPane;
		this.obsListPane = obsListPane;
		this.meansListPane = meansListPane;
	}

	/**
	 * @return the analysisType
	 */
	public AnalysisType getAnalysisType() {
		return analysisType;
	}

	/**
	 * @return the obsAndMeanChartPane
	 */
	public ObservationAndMeanPlotPane getObsAndMeanChartPane() {
		return obsAndMeanChartPane;
	}

	/**
	 * @return the obsListPane
	 */
	public ObservationListPane getObsListPane() {
		return obsListPane;
	}

	/**
	 * @return the meansListPane
	 */
	public SyntheticObservationListPane<AbstractMeanObservationTableModel> getMeansListPane() {
		return meansListPane;
	}

	/**
	 * @return the viewMode
	 */
	public ViewModeType getViewMode() {
		return viewMode;
	}
}
