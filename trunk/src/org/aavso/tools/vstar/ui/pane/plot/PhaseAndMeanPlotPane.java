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
import org.aavso.tools.vstar.ui.mediator.AnalysisType;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.ViewModeType;
import org.aavso.tools.vstar.ui.mediator.message.FilteredObservationMessage;
import org.aavso.tools.vstar.ui.mediator.message.NewStarMessage;
import org.aavso.tools.vstar.ui.mediator.message.ObservationSelectionMessage;
import org.aavso.tools.vstar.ui.mediator.message.PanRequestMessage;
import org.aavso.tools.vstar.ui.mediator.message.PolynomialFitMessage;
import org.aavso.tools.vstar.ui.mediator.message.ZoomRequestMessage;
import org.aavso.tools.vstar.ui.model.plot.ObservationAndMeanPlotModel;
import org.aavso.tools.vstar.ui.model.plot.PhaseTimeElementEntity;
import org.aavso.tools.vstar.util.notification.Listener;
import org.aavso.tools.vstar.util.prefs.NumericPrecisionPrefs;
import org.aavso.tools.vstar.util.stats.BinningResult;
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
	 */
	public PhaseAndMeanPlotPane(String title, String subTitle,
			ObservationAndMeanPlotModel obsAndMeanModel, Dimension bounds) {

		super(title, subTitle, PHASE_TITLE, MAG_TITLE, obsAndMeanModel,
				new TimeElementsInBinSettingPane(
						"Phase Steps per Mean Series Bin", obsAndMeanModel,
						PhaseTimeElementEntity.instance), bounds);

		xyMsgFormat = "Phase: " + NumericPrecisionPrefs.getTimeOutputFormat()
				+ ", Mag: " + NumericPrecisionPrefs.getMagOutputFormat();
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
	// 1. New PhaseChangeMessage here and in tables: setPhases(), fire changed.
	// 2. New polynomial fit.
	
	// Returns a polynomial fit listener.
	protected Listener<PolynomialFitMessage> createPolynomialFitListener() {
		return new Listener<PolynomialFitMessage>() {

//			TODO:
//				2. In our case here, we will double the lists and set phase values.
//				   That should be done in a separate method M local to this class.
//				   BUT WE DON'T HAVE PHASE AND PERIOD FOR THE LAST PHASE PLOT!
//                 BUT THIS CLASS OR ITS MODEL SHOULD KNOW ABOUT IT!
//		           Create a notifier for phase change, perhaps later.
//		           For now, just make Mediator a listener on polynomial fits and 
//		           perhaps filtered obs, and just add these to the category map
//		           so that at time of phase plot creation, they are in the map of
//		           series to be phased up; later allow update of existing phase plot.
//				3. In createPhasePlotArtefacts(), we need to call the above method
//				   or we need to narrowcast to just this listener by adding a notifier
//				   method that only notifies particular objects (that would also allow
//			       us to control notification order BTW). Much better to just call M!
			@Override
			public void update(PolynomialFitMessage info) {
				// Do nothing for phase plots currently.
				// When we do eventually enable this,
				// this method should only do something
				// if a phase plot has been created, otherwise
				// we see assertion errors from PhaseCoordSource.getXCoord()
				// since phase values will be null.
//				IPolynomialFitter model = info.getPolynomialFitter();
//				List<ValidObservation> modelObs = new ArrayList<ValidObservation>();
//				modelObs.addAll(model.getFit()); 
//				List<ValidObservation> residualObs = new ArrayList<ValidObservation>();
//				residualObs.addAll(model.getResiduals());
//				updateModelSeries(modelObs, residualObs);
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
