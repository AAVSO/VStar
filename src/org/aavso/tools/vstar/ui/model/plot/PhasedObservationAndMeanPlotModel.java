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
package org.aavso.tools.vstar.ui.model.plot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.ui.mediator.message.FilteredObservationMessage;
import org.aavso.tools.vstar.ui.mediator.message.ModelSelectionMessage;
import org.aavso.tools.vstar.util.notification.Listener;

/**
 * This class is a model that represents a series of phased valid variable star
 * observations, e.g. for different bands (or from different sources) along with
 * a means series that can change over time.
 */
@SuppressWarnings("serial")
public class PhasedObservationAndMeanPlotModel extends
		ObservationAndMeanPlotModel {

	/**
	 * Constructor
	 * 
	 * We add named observation source lists to unique series numbers. Then we
	 * add the initial mean-based series.
	 * 
	 * @param obsSourceListMap
	 *            A mapping from source series to lists of observation sources.
	 * @param coordSrc
	 *            coordinate and error source.
	 * @param obComparator
	 *            A valid observation comparator (e.g. by JD or phase).
	 * @param timeElementEntity
	 *            A time element source for observations.
	 * @param seriesVisibilityMap
	 *            A mapping from series type to visibility status.
	 * @param modelFunction
	 *            A model containing a function; may be null.
	 * @param modelFunctionSeriesNum
	 *            The corresponding model function series number; may be
	 *            NO_SERIES.
	 */
	public PhasedObservationAndMeanPlotModel(
			Map<SeriesType, List<ValidObservation>> obsSourceListMap,
			ICoordSource coordSrc, Comparator<ValidObservation> obComparator,
			ITimeElementEntity timeElementEntity,
			Map<SeriesType, Boolean> seriesVisibilityMap,
			ContinuousModelFunction modelFunction, int modelFunctionSeriesNum) {
		super(obsSourceListMap, coordSrc, obComparator, timeElementEntity,
				seriesVisibilityMap);
		this.modelFunction = modelFunction;
		this.modelFunctionSeriesNum = modelFunctionSeriesNum;
	}

	/**
	 * @see org.aavso.tools.vstar.ui.model.plot.ObservationPlotModel#createModelSelectionListener()
	 */
	@Override
	protected Listener<ModelSelectionMessage> createModelSelectionListener() {
		final ObservationAndMeanPlotModel plotModel = this;

		return new Listener<ModelSelectionMessage>() {
			@Override
			public void update(ModelSelectionMessage info) {
				// Copy and sort fit and residual observations to ensure correct
				// mean handling.
				List<ValidObservation> modelObs = new ArrayList<ValidObservation>(
						info.getModel().getFit());
				Collections.sort(modelObs, obComparator);

				List<ValidObservation> residuals = new ArrayList<ValidObservation>(
						info.getModel().getResiduals());
				Collections.sort(residuals, obComparator);

				updateModelSeries(modelObs, residuals, info.getModel());

				// If the means sources series is model or residuals (from
				// previous modelling operation), re-compute the means series.
				if (seriesNumToSrcTypeMap.get(meanSourceSeriesNum) == SeriesType.Model
						|| seriesNumToSrcTypeMap.get(meanSourceSeriesNum) == SeriesType.Residuals) {
					plotModel.setMeanSeries(false);
				}
			}

			@Override
			public boolean canBeRemoved() {
				return true;
			}
		};
	}

	/**
	 * @see org.aavso.tools.vstar.ui.model.plot.ObservationPlotModel#createFilteredObservationListener()
	 */
	@Override
	protected Listener<FilteredObservationMessage> createFilteredObservationListener() {

		final ObservationAndMeanPlotModel model = this;

		return new Listener<FilteredObservationMessage>() {

			@Override
			public void update(FilteredObservationMessage info) {
				if (!handleNoFilter(info)) {
					// Convert set of filtered observations to list then add
					// or replace the filter series.
					List<ValidObservation> obs = new ArrayList<ValidObservation>(
							info.getFilteredObs());
					Collections.sort(obs, obComparator);

					updateFilteredSeries(obs);

					// If the means sources series is filtered (from
					// previous filtering operation), re-compute the means
					// series.
					if (seriesNumToSrcTypeMap.get(meanSourceSeriesNum) == SeriesType.Filtered) {
						model.setMeanSeries(false);
					}
				}
			}

			@Override
			public boolean canBeRemoved() {
				return true;
			}
		};
	}
}
