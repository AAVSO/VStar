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

import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.input.AbstractObservationRetriever;
import org.aavso.tools.vstar.ui.mediator.AnalysisType;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.ViewModeType;
import org.aavso.tools.vstar.ui.mediator.message.NewStarMessage;
import org.aavso.tools.vstar.ui.mediator.message.ObservationSelectionMessage;
import org.aavso.tools.vstar.ui.mediator.message.PanRequestMessage;
import org.aavso.tools.vstar.ui.mediator.message.SeriesVisibilityChangeMessage;
import org.aavso.tools.vstar.ui.mediator.message.ZoomRequestMessage;
import org.aavso.tools.vstar.ui.model.plot.ObservationAndMeanPlotModel;
import org.aavso.tools.vstar.ui.model.plot.PhasedObservationAndMeanPlotModel;
import org.aavso.tools.vstar.util.locale.LocaleProps;
import org.aavso.tools.vstar.util.notification.Listener;
import org.aavso.tools.vstar.util.prefs.NumericPrecisionPrefs;
import org.aavso.tools.vstar.util.stats.BinningResult;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.general.Dataset;

/**
 * This class represents a chart pane containing a phase plot for a set of valid
 * observations along with mean-based data.
 */
@SuppressWarnings("serial")
public class PhaseAndMeanPlotPane extends ObservationAndMeanPlotPane {

	public static String PHASE = LocaleProps.get("PHASE");

	private double epoch;
	private double period;

	private String xyMsgFormat;

	private ObservationAndMeanPlotModel[] obsAndMeanModels;

	// Did the last selection correspond to a standard phase domain value?
	// The alternative is a previous cycle phase selection, or null, meaning
	// no selection.
	private Dataset previousCyclePhaseModel;
	private Dataset standardPhaseModel;
	private Boolean wasLastSelectionStdPhase;

	/**
	 * Constructor.
	 * 
	 * @param title
	 *            The title for the chart.
	 * @param subTitle
	 *            The sub-title for the chart.
	 * @param bounds
	 *            The bounding box to which to set the chart's preferred size.
	 * @param epoch
	 *            The starting JD for the current phase plot.
	 * @param period
	 *            The period for the current phase plot.
	 * @param retriever
	 *            The observation retriever for observations in this plot.
	 * @param obsAndMeanModels
	 *            The data models to plot.
	 */
	public PhaseAndMeanPlotPane(String title, String subTitle,
			Dimension bounds, double epoch, double period,
			AbstractObservationRetriever retriever,
			PhasedObservationAndMeanPlotModel... obsAndMeanModels) {

		super(title, subTitle, PHASE, getBrightnessAxisLabel(retriever
				.getBrightnessUnits()), obsAndMeanModels[0], bounds, retriever);

		this.epoch = epoch;
		this.period = period;

		this.obsAndMeanModels = obsAndMeanModels;

		this.wasLastSelectionStdPhase = null;

		xyMsgFormat = "%s, %s";

		this.chart.getXYPlot().setDataset(1, obsAndMeanModels[1]);

		previousCyclePhaseModel = this.chart.getXYPlot().getDataset(0);
		standardPhaseModel = this.chart.getXYPlot().getDataset(1);

		// setSeriesVisibility();
	}

	/**
	 * @return the epoch
	 */
	public double getEpoch() {
		return epoch;
	}

	/**
	 * @return the period
	 */
	public double getPeriod() {
		return period;
	}

	/**
	 * @return the obsAndMeanModels
	 */
	public ObservationAndMeanPlotModel[] getObsModels() {
		return obsAndMeanModels;
	}

	/**
	 * @return the wasLastSelectionStdPhase
	 */
	public Boolean wasLastSelectionStdPhase() {
		return wasLastSelectionStdPhase;
	}

	/**
	 * @param meanSourceSeriesNum
	 *            the meanSourceSeriesNum to set
	 */
	public void setMeanSourceSeriesNum(int meanSourceSeriesNum) {
		for (ObservationAndMeanPlotModel obsModel : obsAndMeanModels) {
			obsModel.setMeanSourceSeriesNum(meanSourceSeriesNum);
		}
	}

	/**
	 * Attempt to create a new mean series with the specified number of time
	 * elements per bin.
	 * 
	 * @param timeElementsInBin
	 *            The number of days or phase steps to be created per bin.
	 * @return Whether or not the series was changed.
	 */
	public boolean changeMeansSeries(double timeElementsInBin) {
		boolean changed = false;

		for (ObservationAndMeanPlotModel obsModel : obsAndMeanModels) {
			changed |= obsModel.changeMeansSeries(timeElementsInBin);
		}

		return changed;
	}

	// @Override
	// protected void setSeriesVisibility() {
	//
	// // super.setSeriesVisibility();
	//
	// if (obsAndMeanModels != null) {
	// for (ObservationAndMeanPlotModel obsModel : obsAndMeanModels) {
	// Map<SeriesType, Boolean> seriesVisibilityMap = obsModel
	// .getSeriesVisibilityMap();
	//
	// // taken from abstract plot pane... necessary?
	// for (SeriesType seriesType : seriesVisibilityMap.keySet()) {
	// int seriesNum = obsModel.getSrcTypeToSeriesNumMap().get(
	// seriesType);
	// renderer.setSeriesVisible(seriesNum, seriesVisibilityMap
	// .get(seriesType));
	// }
	//
	// boolean isModelFuncVisible = seriesVisibilityMap
	// .get(SeriesType.ModelFunction);
	//
	// if (isModelFuncVisible && obsModel.getModelFunction() != null) {
	// ContinuousModelPlotModel modelFuncModel = new ContinuousModelPlotModel(
	// obsModel.getModelFunction());
	//
	// JFreeChart modelFuncPlot = ChartFactory.createXYLineChart(
	// "", "", "", modelFuncModel,
	// PlotOrientation.VERTICAL, false, false, false);
	//
	// int modelFuncSeriesNum = obsModel
	// .getSrcTypeToSeriesNumMap().get(
	// SeriesType.ModelFunction);
	//
	// chart.getXYPlot().setDataset(modelFuncSeriesNum,
	// modelFuncModel);
	// chart.getXYPlot().setRenderer(modelFuncSeriesNum,
	// modelFuncPlot.getXYPlot().getRenderer());
	// Color color = SeriesType
	// .getColorFromSeries(SeriesType.ModelFunction);
	// chart.getXYPlot().getRenderer(modelFuncSeriesNum)
	// .setSeriesPaint(modelFuncSeriesNum, color);
	// chart.getXYPlot().getRenderer(modelFuncSeriesNum)
	// .setSeriesVisible(modelFuncSeriesNum, true);
	// }
	// }
	// }
	// }

	// From ChartMouseListener interface.
	// If the mouse is over a data point, set its tool-tip with phase and
	// magnitude.
	public void chartMouseMoved(ChartMouseEvent event) {
		ChartEntity entity = event.getEntity();
		if (entity instanceof XYItemEntity) {
			XYItemEntity item = (XYItemEntity) entity;
			ValidObservation ob = obsModel.getValidObservation(item
					.getSeriesIndex(), item.getItem());
			String xyMsg = String.format(xyMsgFormat, NumericPrecisionPrefs
					.formatTime(ob.getStandardPhase()), NumericPrecisionPrefs
					.formatMag(ob.getMag()));
			item.setToolTipText(xyMsg);
		}
	}

	// Returns a series visibility change listener to update the chart legends
	// when the set of visible series changes.
	protected Listener<SeriesVisibilityChangeMessage> createSeriesVisibilityChangeListener() {
		return new Listener<SeriesVisibilityChangeMessage>() {
			@Override
			public void update(SeriesVisibilityChangeMessage info) {
				// Nothing to do apparently.
			}

			@Override
			public boolean canBeRemoved() {
				return true;
			}
		};
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

					updateSelectionFromObservation(message.getObservation());
				}
			}

			public boolean canBeRemoved() {
				return true;
			}
		};
	}

	/**
	 * @see org.aavso.tools.vstar.ui.pane.plot.AbstractObservationPlotPane#updateSelectionFromObservation(org.aavso.tools.vstar.data.ValidObservation)
	 */
	@Override
	protected void updateSelectionFromObservation(ValidObservation ob) {
		super.updateSelectionFromObservation(ob);
		// We assume that the last selected dataset has been set before the
		// observation selection message was sent.
		wasLastSelectionStdPhase = getLastDatasetSelected() == standardPhaseModel;
	}

	// Returns a zoom request listener.
	protected Listener<ZoomRequestMessage> createZoomRequestListener() {
		return new Listener<ZoomRequestMessage>() {
			public void update(ZoomRequestMessage info) {
				if (Mediator.getInstance().getAnalysisType() == AnalysisType.PHASE_PLOT
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
						.getLatestNewStarMessage();

				switch (msg.getPanType()) {
				case LEFT:
					if (plot.getDomainAxis().getLowerBound() >= -1) {
						plot.panDomainAxes(-percentage, plotInfo, source);
					}
					break;
				case RIGHT:
					if (plot.getDomainAxis().getUpperBound() <= 1) {
						plot.panDomainAxes(percentage, plotInfo, source);
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
				return true;
			}
		};
	}

	// Returns a mean observation change (binning result) listener.
	protected Listener<BinningResult> createBinChangeListener() {
		return new Listener<BinningResult>() {
			public void update(BinningResult info) {
				// Do nothing. We may want to show ANOVA here, but I need to
				// understand whether this is useful for a phase plot by reading
				// more and talking with Grant et al.
			}

			public boolean canBeRemoved() {
				return true;
			}
		};
	}

	protected void updateAnovaSubtitle(BinningResult binningResult) {
		// Do nothing. See createBinChangeListener().
	}
}
