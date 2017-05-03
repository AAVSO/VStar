/**
 * VStar: a statistical analysis tool for variable star data.
 * Copyright (C) 2015  AAVSO (http://www.aavso.org/)
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
package org.aavso.tools.vstar.ui.model.plot;

import java.util.LinkedHashSet;
import java.util.Set;

import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.data.filter.IFilterDescription;
import org.aavso.tools.vstar.ui.dialog.MessageBox;
import org.aavso.tools.vstar.ui.mediator.AnalysisType;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.message.FilteredObservationMessage;
import org.aavso.tools.vstar.ui.pane.plot.ObservationAndMeanPlotPane;
import org.aavso.tools.vstar.util.prefs.NumericPrecisionPrefs;

/**
 * This class creates a filtered subset of observations based upon what is
 * currently in the raw plot.
 */
public class InViewObservationFilter {

	private double lowerTimeBound;
	private double upperTimeBound;
	private double lowerMagBound;
	private double upperMagBound;

	private ObservationPlotModel model;

	private String seriesListStr;

	private Set<ValidObservation> filteredObs;

	public InViewObservationFilter() {
		// Assume raw mode!
		AnalysisType analysisType = AnalysisType.RAW_DATA;

		model = Mediator.getInstance().getObservationPlotModel(analysisType);

		ObservationAndMeanPlotPane plotPane = Mediator.getInstance()
				.getPlotPane(analysisType);

		lowerTimeBound = plotPane.getChartPanel().getChart().getXYPlot()
				.getDomainAxis().getRange().getLowerBound();

		upperTimeBound = plotPane.getChartPanel().getChart().getXYPlot()
				.getDomainAxis().getRange().getUpperBound();

		lowerMagBound = plotPane.getChartPanel().getChart().getXYPlot()
				.getRangeAxis().getRange().getLowerBound();

		upperMagBound = plotPane.getChartPanel().getChart().getXYPlot()
				.getRangeAxis().getRange().getUpperBound();

		seriesListStr = "";
	}

	/**
	 * Creates a filter of observations visible in the range and sends a
	 * filtered observation message to be consumed by listeners (plots, tables).
	 */
	public void execute() {
		filter();

		// Removing trailing comma and space from series list string.
		if (!seriesListStr.isEmpty()) {
			seriesListStr = seriesListStr.substring(0,
					seriesListStr.length() - 2);
		}

		if (filteredObs.size() != 0) {

			IFilterDescription desc = new IFilterDescription() {

				private String lowerTimeBoundStr = NumericPrecisionPrefs
						.formatTime(lowerTimeBound);
				private String upperTimeBoundStr = NumericPrecisionPrefs
						.formatTime(upperTimeBound);

				private String lowerMagBoundStr = NumericPrecisionPrefs
						.formatMag(lowerMagBound);
				private String upperMagBoundStr = NumericPrecisionPrefs
						.formatMag(upperMagBound);

				@Override
				public boolean isParsable() {
					return true;
				}

				@Override
				public String getFilterName() {
					return String.format("Observations in time range %s..%s "
							+ "and magnitude range %s..%s in [%s]",
							lowerTimeBoundStr, upperTimeBoundStr,
							lowerMagBoundStr, upperMagBoundStr, seriesListStr);
				}

				@Override
				public String getFilterDescription() {
					return "time >= " + lowerTimeBoundStr + " AND time <= "
							+ upperTimeBoundStr + " AND magnitude >= "
							+ lowerMagBoundStr + " AND magnitude <= "
							+ upperMagBoundStr + " AND series in ["
							+ seriesListStr + "]";
				}
			};

			FilteredObservationMessage msg = new FilteredObservationMessage(
					this, desc, filteredObs);

			Mediator.getInstance().getFilteredObservationNotifier()
					.notifyListeners(msg);
		} else {
			String errMsg = "No observations matched.";
			MessageBox.showWarningDialog(Mediator.getUI().getComponent(),
					"Observation Filter", errMsg);
		}
	}

	/**
	 * Create a sorted subset of observations in the range.
	 */
	private void filter() {
		// We use a LinkedHashSet to maintain addition and lookup efficiency
		// while maintaining insertion order.
		filteredObs = new LinkedHashSet<ValidObservation>();

		for (SeriesType series : model.getVisibleSeries()) {
			seriesListStr += series.getShortName() + ", ";

			for (ValidObservation ob : model.getObservations(series)) {
				// Skip to the start of the time range and stop when we exceed
				// the upper bound of the time range.
				if (ob.getJD() < lowerTimeBound) {
					continue;
				} else if (ob.getJD() > upperTimeBound) {
					break;
				} else {
					// Include observation in filtered subset if in the
					// view's magnitude range.
					if (ob.getMag() >= lowerMagBound
							&& ob.getMag() <= upperMagBound) {
						filteredObs.add(ob);
					}
				}
			}
		}
	}
}
