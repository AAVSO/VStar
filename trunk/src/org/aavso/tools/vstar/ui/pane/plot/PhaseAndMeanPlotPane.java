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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.ui.mediator.AnalysisType;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.ViewModeType;
import org.aavso.tools.vstar.ui.mediator.message.FilteredObservationMessage;
import org.aavso.tools.vstar.ui.mediator.message.ModelSelectionMessage;
import org.aavso.tools.vstar.ui.mediator.message.NewStarMessage;
import org.aavso.tools.vstar.ui.mediator.message.ObservationSelectionMessage;
import org.aavso.tools.vstar.ui.mediator.message.PanRequestMessage;
import org.aavso.tools.vstar.ui.mediator.message.ZoomRequestMessage;
import org.aavso.tools.vstar.ui.model.plot.ObservationAndMeanPlotModel;
import org.aavso.tools.vstar.ui.model.plot.PhaseTimeElementEntity;
import org.aavso.tools.vstar.util.comparator.StandardPhaseComparator;
import org.aavso.tools.vstar.util.model.IModel;
import org.aavso.tools.vstar.util.notification.Listener;
import org.aavso.tools.vstar.util.prefs.NumericPrecisionPrefs;
import org.aavso.tools.vstar.util.stats.BinningResult;
import org.aavso.tools.vstar.util.stats.PhaseCalcs;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;

/**
 * This class represents a chart pane containing a phase plot for a set of valid
 * observations along with mean-based data.
 */
public class PhaseAndMeanPlotPane extends ObservationAndMeanPlotPane {

	private double epoch;
	private double period;

	private String xyMsgFormat;

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
	 * @param epoch
	 *            The starting JD for the current phase plot.
	 * @param period
	 *            The period for the current phase plot.
	 */
	public PhaseAndMeanPlotPane(String title, String subTitle,
			ObservationAndMeanPlotModel obsAndMeanModel, Dimension bounds,
			double epoch, double period) {

		super(title, subTitle, PHASE_TITLE, MAG_TITLE, obsAndMeanModel,
				new TimeElementsInBinSettingPane(
						"Phase Steps per Mean Series Bin", obsAndMeanModel,
						PhaseTimeElementEntity.instance), bounds);

		this.epoch = epoch;
		this.period = period;

		xyMsgFormat = "Phase: " + NumericPrecisionPrefs.getTimeOutputFormat()
				+ ", Mag: " + NumericPrecisionPrefs.getMagOutputFormat();

//		Mediator.getInstance().getFilteredObservationNotifier().addListener(
//				createFilteredObservationListener());
//
//		Mediator.getInstance().getPolynomialFitNofitier().addListener(
//				createModelSelectionListener());
	}

	// From ChartMouseListener interface.
	// If the mouse is over a data point, set its tool-tip with phase and
	// magnitude.
	public void chartMouseMoved(ChartMouseEvent event) {
		ChartEntity entity = event.getEntity();
		if (entity instanceof XYItemEntity) {
			XYItemEntity item = (XYItemEntity) entity;
			ValidObservation ob = obsModel.getValidObservation(item
					.getSeriesIndex(), item.getItem());
			String xyMsg = String.format(xyMsgFormat, ob.getStandardPhase(), ob
					.getMag());
			item.setToolTipText(xyMsg);
		}
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
						.getNewStarMessage();

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
				return false;
			}
		};
	}

	// TODO: handle:
	// New PhaseChangeMessage here and in tables rather than creating a whole
	// new set of phase plot artefacts: setPhases(), fire changed.

	// Returns a filtered observation listener that updates the filtered data
	// series. We don't need to set the phases in the data because the
	// underlying data in the filter will already have had phases set since we
	// have an existing (this) phase plot.
	protected Listener<FilteredObservationMessage> createFilteredObservationListener() {
		return new Listener<FilteredObservationMessage>() {
			@Override
			public void update(FilteredObservationMessage info) {
				if (!handleNoFilter(info)) {
					// Convert set of filtered observations to list then add
					// or replace the filter series.
					List<ValidObservation> obs = new ArrayList<ValidObservation>();
					for (ValidObservation ob : info.getFilteredObs()) {
						obs.add(ob);
					}

					// Double and sort the filtered data.
					List<ValidObservation> filteredObs = new ArrayList<ValidObservation>();
					filteredObs.addAll(obs);
					Collections.sort(filteredObs,
							StandardPhaseComparator.instance);

					updateFilteredSeries(obs);
				}
			}

			@Override
			public boolean canBeRemoved() {
				return false;
			}
		};
	}

	// Returns a model selection listener that updates the model and residual
	// series including setting the current phase in the data.
	protected Listener<ModelSelectionMessage> createModelSelectionListener() {
		return new Listener<ModelSelectionMessage>() {

			@Override
			public void update(ModelSelectionMessage info) {
				IModel model = info.getModel();

				// Set the phases in the new model and residuals data.
				PhaseCalcs.setPhases(model.getFit(), epoch, period);
				PhaseCalcs.setPhases(model.getResiduals(), epoch, period);

				// Double and sort the model data.
				List<ValidObservation> modelObs = new ArrayList<ValidObservation>();
				modelObs.addAll(model.getFit());
				Collections.sort(modelObs, StandardPhaseComparator.instance);

				// Double and sort the residuals data.
				List<ValidObservation> residualObs = new ArrayList<ValidObservation>();
				residualObs.addAll(model.getResiduals());
				Collections.sort(residualObs, StandardPhaseComparator.instance);

				updateModelSeries(modelObs, residualObs);
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
				return false;
			}
		};
	}

	protected void updateAnovaSubtitle(BinningResult binningResult) {
		// Do nothing. See createBinChangeListener().
	}
}
