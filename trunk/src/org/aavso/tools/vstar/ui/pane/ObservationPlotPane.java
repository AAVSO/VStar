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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.ui.dialog.series.SeriesVisibilityDialog;
import org.aavso.tools.vstar.ui.mediator.AnalysisType;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.ObservationSelectionMessage;
import org.aavso.tools.vstar.ui.mediator.ViewModeType;
import org.aavso.tools.vstar.ui.mediator.ZoomRequestMessage;
import org.aavso.tools.vstar.ui.model.plot.ObservationPlotModel;
import org.aavso.tools.vstar.util.notification.Listener;

/**
 * This class represents a chart pane containing a raw plot for a set of valid
 * observations (magnitude vs Julian Day).
 */
public class ObservationPlotPane extends
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

	// Return a listener for the "change series visibility" button.
	protected ActionListener createSeriesChangeButtonListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SeriesVisibilityDialog dialog = new SeriesVisibilityDialog(
						obsModel);

				if (!dialog.isCancelled()) {
					seriesVisibilityChange(dialog.getVisibilityDeltaMap());
				}
			}
		};
	}

	// Returns an observation selection listener.
	protected Listener<ObservationSelectionMessage> createObservationSelectionListener() {
		return new Listener<ObservationSelectionMessage>() {
			public void update(ObservationSelectionMessage message) {
				// Move the cross hairs if this is not a mean observation and
				// we have date information since this plot's domain is JD.
				if (message.getSource() != this
						&& message.getObservation().getDateInfo() != null
						&& message.getObservation().getBand() != SeriesType.MEANS) {
					chart.getXYPlot().setDomainCrosshairValue(
							message.getObservation().getJD());
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
				if (Mediator.getInstance().getAnalysisType() == AnalysisType.RAW_DATA &&
						Mediator.getInstance().getViewMode() == ViewModeType.PLOT_OBS_MODE) {
					doZoom(info.getZoomType());
				}
			}
			
			public boolean canBeRemoved() {
				return true;
			}
		};
	}
}
