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
import java.awt.geom.Point2D;
import java.util.List;

import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.ui.mediator.AnalysisType;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.NewStarType;
import org.aavso.tools.vstar.ui.mediator.ViewModeType;
import org.aavso.tools.vstar.ui.mediator.message.NewStarMessage;
import org.aavso.tools.vstar.ui.mediator.message.ObservationSelectionMessage;
import org.aavso.tools.vstar.ui.mediator.message.PanRequestMessage;
import org.aavso.tools.vstar.ui.mediator.message.ZoomRequestMessage;
import org.aavso.tools.vstar.ui.model.plot.ObservationAndMeanPlotModel;
import org.aavso.tools.vstar.util.notification.Listener;
import org.aavso.tools.vstar.util.prefs.NumericPrecisionPrefs;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;

/**
 * This class represents a chart pane containing a plot for a set of valid
 * observations along with mean-based data.
 */
public class ObservationAndMeanPlotPane extends
		AbstractObservationPlotPane<ObservationAndMeanPlotModel> {

	private String xyMsgFormat;

	/**
	 * Constructor
	 * 
	 * @param title
	 *            The title for the chart.
	 * @param subTitle
	 *            The sub-title for the chart.
	 * @param domainTitle
	 *            The domain title (e.g. Julian Date, phase).
	 * @param rangeTitle
	 *            The range title (e.g. magnitude).
	 * @param obsModel
	 *            The data model to plot.
	 * @param timeElementsInBinSettingPane
	 *            The time-elements-in-mean-pane used by this observation and
	 *            mean plot pane.
	 * @param bounds
	 *            The bounding box to which to set the chart's preferred size.
	 */
	public ObservationAndMeanPlotPane(String title, String subTitle,
			String domainTitle, String rangeTitle,
			ObservationAndMeanPlotModel obsAndMeanModel,
			TimeElementsInBinSettingPane timeElementsInBinSettingPane,
			Dimension bounds) {

		super(title, subTitle, domainTitle, rangeTitle, obsAndMeanModel, bounds);

		xyMsgFormat = "JD: " + NumericPrecisionPrefs.getTimeOutputFormat()
				+ " (%s), Mag: " + NumericPrecisionPrefs.getMagOutputFormat();

		// Set the means series color.
		int meanSeriesNum = obsAndMeanModel.getMeansSeriesNum();
		if (meanSeriesNum != ObservationAndMeanPlotModel.NO_MEANS_SERIES) {
			this.getRenderer().setSeriesPaint(meanSeriesNum,
					SeriesType.getColorFromSeries(SeriesType.MEANS));
		}

		// Update joined series to ensure that the means series is initially
		// joined since the base class won't include it in its set.
		setJoinedSeries();
	}

	/**
	 * @return The observation model.
	 */
	public ObservationAndMeanPlotModel getObsModel() {
		return obsModel;
	}

	/**
	 * Constructor
	 * 
	 * @param title
	 *            The title for the chart.
	 * @param subTitle
	 *            The sub-title for the chart.
	 * @param obsModel
	 *            The data model to plot.
	 * @param timeElementsInBinSettingPane
	 *            The time-elements-in-mean-pane used by this observation and
	 *            mean plot pane.
	 * @param bounds
	 *            The bounding box to which to set the chart's preferred size.
	 */
	public ObservationAndMeanPlotPane(String title, String subTitle,
			ObservationAndMeanPlotModel obsAndMeanModel,
			TimeElementsInBinSettingPane timeElementsInBinSettingPane,
			Dimension bounds) {

		this(title, subTitle, JD_TITLE, MAG_TITLE, obsAndMeanModel,
				timeElementsInBinSettingPane, bounds);
	}

	// From ChartMouseListener interface.
	// If the mouse is over a data point, set its tool-tip with JD and
	// magnitude.
	public void chartMouseMoved(ChartMouseEvent event) {
		ChartEntity entity = event.getEntity();
		if (entity instanceof XYItemEntity) {
			XYItemEntity item = (XYItemEntity) entity;
			ValidObservation ob = obsModel.getValidObservation(item
					.getSeriesIndex(), item.getItem());
			String xyMsg = String.format(xyMsgFormat, ob.getJD(), ob
					.getDateInfo().getCalendarDate(), ob.getMag());
			item.setToolTipText(xyMsg);
		}
	}

	// Returns an observation selection listener.
	protected Listener<ObservationSelectionMessage> createObservationSelectionListener() {
		return new Listener<ObservationSelectionMessage>() {

			public void update(ObservationSelectionMessage message) {
				// Move the cross hairs if we have date information since
				// this plot's domain is JD.
				if (message.getSource() != this
						&& message.getObservation().getDateInfo() != null) {
					chart.getXYPlot().setDomainCrosshairValue(
							message.getObservation().getJD());
					chart.getXYPlot().setRangeCrosshairValue(
							message.getObservation().getMag());

					updateSelectionFromObservation(message.getObservation());
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
				if (Mediator.getInstance().getAnalysisType() == AnalysisType.RAW_DATA
						&& Mediator.getInstance().getViewMode() == ViewModeType.PLOT_OBS_MODE) {
					doZoom(info.getZoomType());
				}
			}

			public boolean canBeRemoved() {
				return true;
			}
		};
	}

	// Returns a pan request listener.
	protected Listener<PanRequestMessage> createPanRequestListener() {
		return new Listener<PanRequestMessage>() {
			@Override
			public void update(PanRequestMessage msg) {
				final PlotRenderingInfo plotInfo = chartPanel
						.getChartRenderingInfo().getPlotInfo();

				final Point2D source = new Point2D.Double(0, 0);

				double percentage = 0.01;

				XYPlot plot = chart.getXYPlot();
				NewStarMessage newStarMsg = Mediator.getInstance()
						.getNewStarMessage();
				List<ValidObservation> obs = newStarMsg.getObservations();

				switch (msg.getPanType()) {
				case LEFT:
					if (plot.getDomainAxis().getLowerBound() >= obs.get(0)
							.getJD()) {
						plot.panDomainAxes(-percentage, plotInfo, source);
					} else {
						if (newStarMsg.getNewStarType() == NewStarType.NEW_STAR_FROM_DATABASE) {
							// TODO: ask whether to read more AID data before
							// last JD
						}
					}
					break;
				case RIGHT:
					if (plot.getDomainAxis().getUpperBound() <= obs.get(
							obs.size() - 1).getJD()) {
						plot.panDomainAxes(percentage, plotInfo, source);
					} else {
						if (newStarMsg.getNewStarType() == NewStarType.NEW_STAR_FROM_DATABASE) {
							// TODO: ask whether to read more AID data after
							// last JD
						}
					}
					break;
				case UP:
					if (newStarMsg.getMinMag() <= plot.getRangeAxis()
							.getLowerBound()) {
						plot.panRangeAxes(percentage, plotInfo, source);
					}
					break;
				case DOWN:
					if (newStarMsg.getMaxMag() >= plot.getRangeAxis()
							.getUpperBound()) {
						plot.panRangeAxes(-percentage, plotInfo, source);
					}
					break;
				}
			}

			@Override
			public boolean canBeRemoved() {
				return false;
			}
		};
	}
}
