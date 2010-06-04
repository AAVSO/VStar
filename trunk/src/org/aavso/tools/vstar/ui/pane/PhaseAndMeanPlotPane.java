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
package org.aavso.tools.vstar.ui.pane;

import java.awt.Dimension;
import java.util.HashMap;
import java.util.Map;

import org.aavso.tools.vstar.ui.mediator.AnalysisType;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.ObservationSelectionMessage;
import org.aavso.tools.vstar.ui.mediator.ViewModeType;
import org.aavso.tools.vstar.ui.mediator.ZoomRequestMessage;
import org.aavso.tools.vstar.ui.model.plot.IVisibilityMapSource;
import org.aavso.tools.vstar.ui.model.plot.ObservationAndMeanPlotModel;
import org.aavso.tools.vstar.ui.model.plot.PhaseTimeElementEntity;
import org.aavso.tools.vstar.util.notification.Listener;

/**
 * This class represents a chart pane containing a phase plot for a set of valid
 * observations along with mean-based data.
 */
public class PhaseAndMeanPlotPane extends ObservationAndMeanPlotPane implements
		IVisibilityMapSource {

	/**
	 * Constructor.
	 * 
	 * @param title
	 *            The title for the chart.
	 * @param subTitle
	 *            The sub-title for the chart.
	 * @param obsAndMeanModel
	 *            The data model to plot.
	 * @param bounds
	 *            The bounding box to which to set the chart's preferred size.
	 */
	public PhaseAndMeanPlotPane(String title, String subTitle,
			ObservationAndMeanPlotModel obsAndMeanModel, Dimension bounds) {

		super(title, subTitle, PHASE_TITLE, MAG_TITLE, obsAndMeanModel,
				new TimeElementsInBinSettingPane("Phase Steps per Mean Series Bin",
						obsAndMeanModel, PhaseTimeElementEntity.instance),
				bounds);

		this.getChartControlPanel().add(new NewPhasePlotButtonPane(this));
	}

	// Return a mapping from series number to visibility status,
	// filtering out the means series. The means series is always
	// visible, so we are simply excluding this from consideration
	// (in the context of creating a phase plot, see NewPhasePlotButtonPane).
	public Map<Integer, Boolean> getVisibilityMap() {
		Map<Integer, Boolean> visibilityMap = obsModel.getSeriesVisibilityMap();
		Map<Integer, Boolean> visibilityMapWithoutMeans = new HashMap<Integer, Boolean>();
		for (Integer seriesNum : visibilityMap.keySet()) {
			if (seriesNum != obsModel.getMeanSourceSeriesNum()) {
				visibilityMapWithoutMeans.put(seriesNum, visibilityMap
						.get(seriesNum));
			}
		}

		return null;
	}

	// Returns an observation selection listener.
	protected Listener<ObservationSelectionMessage> createObservationSelectionListener() {
		return new Listener<ObservationSelectionMessage>() {

			public void update(ObservationSelectionMessage message) {
				// Move the cross hairs if we have phase information since
				// this plot's domain is phase.
				if (message.getSource() != this
						&& message.getObservation().getStandardPhase() != null) {
					chart.getXYPlot().setDomainCrosshairValue(
							message.getObservation().getStandardPhase());
					chart.getXYPlot().setRangeCrosshairValue(
							message.getObservation().getMag());
				}
			}

			public boolean canBeRemoved() {
				return true;
			}
		};
	}
	
	// Returns a zoom request listener.
	protected Listener<ZoomRequestMessage> createZoomRequestListener() {
		return new Listener<ZoomRequestMessage>() {
			public void update(ZoomRequestMessage info) {
				if (Mediator.getInstance().getAnalysisType() == AnalysisType.PHASE_PLOT &&
						Mediator.getInstance().getViewMode() == ViewModeType.PLOT_OBS_AND_MEANS_MODE) {
					doZoom(info.getZoomType());
				}
			}
			
			public boolean canBeRemoved() {
				return true;
			}
		};
	}
}
