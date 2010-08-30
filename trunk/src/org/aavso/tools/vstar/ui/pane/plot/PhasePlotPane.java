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
import java.util.Map;

import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.ui.mediator.AnalysisType;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.ViewModeType;
import org.aavso.tools.vstar.ui.mediator.message.FilteredObservationMessage;
import org.aavso.tools.vstar.ui.mediator.message.ObservationSelectionMessage;
import org.aavso.tools.vstar.ui.mediator.message.PolynomialFitMessage;
import org.aavso.tools.vstar.ui.mediator.message.ZoomRequestMessage;
import org.aavso.tools.vstar.ui.model.plot.IVisibilityMapSource;
import org.aavso.tools.vstar.ui.model.plot.ObservationPlotModel;
import org.aavso.tools.vstar.util.notification.Listener;

/**
 * This class represents a chart pane containing a phase plot for a set of valid
 * observations (magnitude vs standard phase).
 */
public class PhasePlotPane extends ObservationPlotPane implements
		IVisibilityMapSource {

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
	public PhasePlotPane(String title, String subTitle,
			ObservationPlotModel obsModel, Dimension bounds) {
		super(title, subTitle, PHASE_TITLE, MAG_TITLE, obsModel, bounds);

		this.getChartControlPanel().add(new NewPhasePlotButtonPane(this));
	}

	public Map<Integer, Boolean> getVisibilityMap() {
		return obsModel.getSeriesVisibilityMap();
	}

	// Returns an observation selection listener.
	protected Listener<ObservationSelectionMessage> createObservationSelectionListener() {
		return new Listener<ObservationSelectionMessage>() {

			public void update(ObservationSelectionMessage message) {
				// Move the cross hairs if this is not a mean observation and
				// we have phase information since this plot's domain is phase.
				if (message.getSource() != this
						&& message.getObservation().getStandardPhase() != null
						&& message.getObservation().getBand() != SeriesType.MEANS) {
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
						Mediator.getInstance().getViewMode() == ViewModeType.PLOT_OBS_MODE) {
					doZoom(info.getZoomType());
				}
			}
			
			public boolean canBeRemoved() {
				return true;
			}
		};
	}

	@Override
	protected Listener<FilteredObservationMessage> createFilteredObservationListener() {
		return new Listener<FilteredObservationMessage>() {
			public void update(FilteredObservationMessage info) {
				// Do nothing for phase plots currently.
				// When we do eventually enable this,
				// this method should only do something
				// if a phase plot has been created, otherwise
				// we see assertion errors from PhaseCoordSource.getXCoord()
				// since phase values will be null.
			}
			
			public boolean canBeRemoved() {
				return false;
			}
		};
	}
	
	// Returns a polynomial fit listener.
	protected Listener<PolynomialFitMessage> createPolynomialFitListener() {
		return new Listener<PolynomialFitMessage>() {

			@Override
			public void update(PolynomialFitMessage info) {
				// Do nothing for phase plots currently.
				// When we do eventually enable this,
				// this method should only do something
				// if a phase plot has been created, otherwise
				// we see assertion errors from PhaseCoordSource.getXCoord()
				// since phase values will be null.
			}

			@Override
			public boolean canBeRemoved() {
				return true;
			}
		};
	}
}
