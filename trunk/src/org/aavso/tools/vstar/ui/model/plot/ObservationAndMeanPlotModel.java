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
package org.aavso.tools.vstar.ui.model.plot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.message.DiscrepantObservationMessage;
import org.aavso.tools.vstar.ui.mediator.message.ExcludedObservationMessage;
import org.aavso.tools.vstar.ui.mediator.message.FilteredObservationMessage;
import org.aavso.tools.vstar.ui.mediator.message.ModelSelectionMessage;
import org.aavso.tools.vstar.util.model.IModel;
import org.aavso.tools.vstar.util.notification.Listener;
import org.aavso.tools.vstar.util.notification.Notifier;
import org.aavso.tools.vstar.util.stats.BinningResult;
import org.aavso.tools.vstar.util.stats.DescStats;

/**
 * This class is a model that represents a series of valid variable star
 * observations, e.g. for different bands (or from different sources) along with
 * a means series that can change over time.
 */
@SuppressWarnings("serial")
public class ObservationAndMeanPlotModel extends ObservationPlotModel {

	public static final int NO_SERIES = -1;

	// The series number of the series that is the source of the
	// means series.
	protected int meanSourceSeriesNum;

	// The series number of the means series.
	protected int meansSeriesNum;

	// An observation time source.
	protected ITimeElementEntity timeElementEntity;

	// The number of time elements in a means series bin.
	protected double timeElementsInBin;

	// The observations that constitute the means series.
	protected List<ValidObservation> meanObsList;

	// The binning result associated with this mean observation list.
	protected BinningResult binningResult;

	protected Notifier<BinningResult> meansChangeNotifier;

	// The current model function series number; may be NO_SERIES.
	protected int modelFunctionSeriesNum;

	// The current model function (polynomial fit, DCDFT, ...); may be
	// null.
	protected ContinuousModelFunction modelFunction;

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
	 */
	public ObservationAndMeanPlotModel(
			Map<SeriesType, List<ValidObservation>> obsSourceListMap,
			ICoordSource coordSrc, Comparator<ValidObservation> obComparator,
			ITimeElementEntity timeElementEntity,
			Map<SeriesType, Boolean> seriesVisibilityMap) {

		super(obsSourceListMap, coordSrc, obComparator, seriesVisibilityMap);

		this.meansSeriesNum = NO_SERIES;

		this.timeElementEntity = timeElementEntity;

		this.timeElementsInBin = this.timeElementEntity
				.getDefaultTimeElementsInBin();

		this.meansChangeNotifier = new Notifier<BinningResult>();

		this.meanSourceSeriesNum = determineMeanSeriesSource();

		this.modelFunctionSeriesNum = NO_SERIES;
		this.modelFunction = null;

		// It doesn't actually matter whether we pass true or false here
		// since the parameter won't apply to the first mean series created.
		this.setMeanSeries(false);

		Mediator.getInstance().getDiscrepantObservationNotifier().addListener(
				createDiscrepantChangeListener());

		Mediator.getInstance().getExcludedObservationNotifier().addListener(
				createExcludedChangeListener());

		Mediator.getInstance().getModelSelectionNofitier().addListener(
				createModelSelectionListener());

		Mediator.getInstance().getFilteredObservationNotifier().addListener(
				createFilteredObservationListener());
	}

	/**
	 * @return the timeElementEntity
	 */
	public ITimeElementEntity getTimeElementEntity() {
		return timeElementEntity;
	}

	/**
	 * Set the mean-based series.
	 * 
	 * This method creates a new means series based upon the current mean source
	 * series index and time-elements-in-bin. It then updates the view and any
	 * listeners.
	 * 
	 * @param updateAfterInitial
	 *            Should the mean series be made visible after the initial
	 *            series is replaced by another? Even if this is set to false,
	 *            if the series is already visible, it will remain so, and any
	 *            change to the mean curve will be visible more or less
	 *            immediately.
	 */
	public boolean setMeanSeries(boolean updateAfterInitial) {

		boolean changed = true;

		// Does this rely upon mean source series being sorted to determine time
		// range?
		// Perhaps the difference between when mean is selected via plot control
		// dialog vs model listener below?

		binningResult = DescStats.createSymmetricBinnedObservations(
				seriesNumToObSrcListMap.get(meanSourceSeriesNum),
				timeElementEntity, timeElementsInBin);

		meanObsList = binningResult.getMeanObservations();

		if (meanObsList != Collections.EMPTY_LIST) {
			// As long as there were enough observations to create a means list
			// to make a "means" series, we do so.
			boolean found = false;

			// TODO: do something like this instead of what follows below.
			// Is this the first time the means series has been added?
			// if (this.meansSeriesNum != NO_SERIES) {
			// // Replace the means series with the new one.
			// this.seriesNumToObSrcListMap.put(this.meanSourceSeriesNum,
			// meanObsList);
			// this.fireDatasetChanged();
			//				
			// // The mean series has been changed after the initial one. If it
			// // is not visible, make it so since the user has updated it and
			// // probably wants to see it right away.
			// if (updateAfterInitial) {
			// changeSeriesVisibility(this.meansSeriesNum, true);
			// }
			// } else {
			// // Create the means series.
			// this.meansSeriesNum = addObservationSeries(SeriesType.MEANS,
			// meanObsList);
			//
			// // Mean series not rendered by default.
			// getSeriesVisibilityMap().put(SeriesType.MEANS, false);
			// }

			for (Map.Entry<Integer, SeriesType> entry : this.seriesNumToSrcTypeMap
					.entrySet()) {
				if (SeriesType.MEANS.equals(entry.getValue())) {
					int series = entry.getKey();
					this.seriesNumToObSrcListMap.put(series, meanObsList);
					this.fireDatasetChanged();
					found = true;
					break;
				}
			}

			// Is this the first time the means series has been added?
			if (!found) {
				this.meansSeriesNum = addObservationSeries(SeriesType.MEANS,
						meanObsList);

				// Mean series not rendered by default.
				getSeriesVisibilityMap().put(SeriesType.MEANS, false);
			} else {
				// The mean series has been changed after the initial one. If it
				// is not visible, make it so since the user has updated it and
				// probably wants to see it right away.
				if (updateAfterInitial) {
					changeSeriesVisibility(this.meansSeriesNum, true);
				}
			}

			// Notify listeners.
			this.meansChangeNotifier.notifyListeners(binningResult);
		} else {
			changed = false;
		}

		return changed;
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
		this.timeElementsInBin = timeElementsInBin;
		return this.setMeanSeries(true);
	}

	/**
	 * @see org.aavso.tools.vstar.ui.model.plot.ObservationPlotModel#changeSeriesVisibility(int,
	 *      boolean)
	 */
	public boolean changeSeriesVisibility(int seriesNum, boolean visibility) {
		return super.changeSeriesVisibility(seriesNum, visibility);
	}

	/**
	 * Which series' elements should be joined visually (e.g. with lines)?
	 * 
	 * @return A collection of series numbers for series whose elements should
	 *         be joined visually.
	 */
	public Collection<Integer> getSeriesWhoseElementsShouldBeJoinedVisually() {
		List<Integer> seriesNumList = new ArrayList<Integer>();

		// See TODO in setMeanSeries() which also applies here.
		for (Map.Entry<Integer, SeriesType> entry : this.seriesNumToSrcTypeMap
				.entrySet()) {
			if (SeriesType.MEANS == entry.getValue()) {
				seriesNumList.add(entry.getKey());
				break;
			}
		}

		return seriesNumList;
	}

	/**
	 * Return the error associated with the magnitude. We skip the series and
	 * item legality check to improve performance on the assumption that this
	 * has been checked already when calling getMagAsYCoord(). So this is a
	 * precondition of calling the current function.
	 * 
	 * @param series
	 *            The series number.
	 * @param item
	 *            The item number within the series.
	 * @return The error value associated with the mean.
	 */
	protected double getMagError(int series, int item) {
		if (series != this.meansSeriesNum) {
			// The series is something other than the means series
			// so just default to the superclass behaviour.
			return super.getMagError(series, item);
		} else {
			// For the means series, we store the mean magnitude error
			// value as the magnitude's uncertainty, and we are only interested
			// in this (not HQ vs user-specified, since only one value exists
			// for
			// a mean observation).

			// For mean observations we double the error value to show the 95%
			// Confidence Interval, as suggested to me by Grant Foster. See his
			// book
			// "Analyzing Light Curves" re: this.
			return this.seriesNumToObSrcListMap.get(series).get(item)
					.getMagnitude().getUncertainty() * 2;
		}
	}

	/**
	 * @return the meanSourceSeriesNum
	 */
	public int getMeanSourceSeriesNum() {
		return meanSourceSeriesNum;
	}

	/**
	 * @param meanSourceSeriesNum
	 *            the meanSourceSeriesNum to set
	 */
	public void setMeanSourceSeriesNum(int meanSourceSeriesNum) {
		this.meanSourceSeriesNum = meanSourceSeriesNum;
	}

	/**
	 * @return the means series number
	 */
	public int getMeansSeriesNum() {
		return meansSeriesNum;
	}

	/**
	 * @return the timeElementsInBin
	 */
	public double getTimeElementsInBin() {
		return timeElementsInBin;
	}

	/**
	 * @param timeElementsInBin
	 *            the timeElementsInBin to set
	 */
	public void setTimeElementsInBin(double timeElementsInBin) {
		this.timeElementsInBin = timeElementsInBin;
	}

	/**
	 * @return the meanObsList
	 */
	public List<ValidObservation> getMeanObsList() {
		return meanObsList;
	}

	/**
	 * @return the binningResult
	 */
	public BinningResult getBinningResult() {
		return binningResult;
	}

	/**
	 * @return the meansChangeNotifier
	 */
	public Notifier<BinningResult> getMeansChangeNotifier() {
		return meansChangeNotifier;
	}

	/**
	 * @return the modelFunctionSeriesNum
	 */
	public int getModelFunctionSeriesNum() {
		return modelFunctionSeriesNum;
	}

	/**
	 * @return the model
	 */
	public ContinuousModelFunction getModelFunction() {
		return modelFunction;
	}

	// Helper methods.

	/**
	 * Determine which series will initially be the source of the mean series.
	 * Note that this may be changed subsequently. Visual bands have the highest
	 * priority. If not found, the Unspecified series is looked at, otherwise
	 * the first band encountered other than fainter-than, excluded, or
	 * discrepant will be chosen.
	 * 
	 * @return The series number on which to base the mean series.
	 */
	public int determineMeanSeriesSource() {
		int seriesNum = NO_SERIES;

		// TODO:
		// - use keySet().contains() below!
		// - out of V and Visual, choose the series with the most observations

		// Look for Visual, then V.
		for (SeriesType series : srcTypeToSeriesNumMap.keySet()) {
			if (series == SeriesType.Visual) {
				// Visual band
				seriesNum = srcTypeToSeriesNumMap.get(series);
				break;
			}
		}

		if (seriesNum == NO_SERIES) {
			for (SeriesType series : srcTypeToSeriesNumMap.keySet()) {
				if (series == SeriesType.Johnson_V) {
					// Johnson V band
					seriesNum = srcTypeToSeriesNumMap.get(series);
					break;
				}
			}
		}

		// No visual bands present. Try 'Unspecified'.
		if (seriesNum == NO_SERIES) {
			for (SeriesType series : srcTypeToSeriesNumMap.keySet()) {
				if (series == SeriesType.Unspecified) {
					// Unspecified
					seriesNum = srcTypeToSeriesNumMap.get(series);
					break;
				}
			}
		}

		// No match: choose a non-empty series other than fainter-than,
		// discrepant, or excluded. More specifically, choose the series with
		// the greatest number of observations.
		int maxObsSeriesNum = NO_SERIES;
		int maxObs = Integer.MIN_VALUE;
		if (seriesNum == NO_SERIES) {
			for (SeriesType series : srcTypeToSeriesNumMap.keySet()) {
				if (series != SeriesType.FAINTER_THAN
						&& series != SeriesType.DISCREPANT
						&& series != SeriesType.Excluded
						&& !series.isSynthetic()
						&& !seriesNumToObSrcListMap.get(
								srcTypeToSeriesNumMap.get(series)).isEmpty()) {
					// 
					seriesNum = srcTypeToSeriesNumMap.get(series);
					int numObs = seriesNumToObSrcListMap.get(seriesNum).size();
					if (numObs > maxObs) {
						maxObsSeriesNum = seriesNum;
						maxObs = numObs;
					}

				}
			}
			seriesNum = maxObsSeriesNum;
		}

		// Still nothing? Okay, now we just choose the first series we come to,
		// including fainter-thans or discrepants.
		//
		// For this to happen should be very rare, but it could happen
		// For example, CM Cru as of Sep 2010 contains one data value
		// since around 1949 and that is a Fainter-than. Note that currently,
		// determineMeanSeriesSource() will ignore Fainter-thans, discrepants,
		// and excluded observations
		// with respect to mean curves. We might want to revise this. Of course,
		// fainter-thans can later be selected as the means-source.
		if (seriesNum == NO_SERIES) {
			for (SeriesType series : srcTypeToSeriesNumMap.keySet()) {
				seriesNum = srcTypeToSeriesNumMap.get(series);
				break;
			}
		}

		assert seriesNum != -1;

		return seriesNum;
	}

	/**
	 * Listen for discrepant observation change notification and add/remove it
	 * from the relevant collections. Since a discrepant observation is ignored
	 * for statistical analysis purposes (see DescStats class), we need to
	 * re-calculate the means series if the discrepant observation's series type
	 * is the same as the mean source series type.
	 */
	protected Listener<DiscrepantObservationMessage> createDiscrepantChangeListener() {

		final ObservationAndMeanPlotModel model = this;

		return new Listener<DiscrepantObservationMessage>() {
			public void update(DiscrepantObservationMessage info) {
				ValidObservation ob = info.getObservation();

				// Did we go to or from being discrepant?
				if (ob.isDiscrepant()) {
					// Now marked as discrepant so move observation from
					// its designated band series to the discrepant series.
					removeObservationFromSeries(ob, ob.getBand());
					addObservationToSeries(ob, SeriesType.DISCREPANT);
				} else {
					// Was marked as discrepant, now is not, so move
					// observation from the discrepant series to its
					// designated band series.
					removeObservationFromSeries(ob, SeriesType.DISCREPANT);
					addObservationToSeries(ob, ob.getBand());
				}

				fireDatasetChanged();

				// If the discrepant observation's band is the source of the
				// means series, re-compute the means series.
				if (info.getObservation().getBand() == seriesNumToSrcTypeMap
						.get(meanSourceSeriesNum)) {
					model.setMeanSeries(false);
				}
			}

			/**
			 * @see org.aavso.tools.vstar.util.notification.Listener#canBeRemoved()
			 */
			public boolean canBeRemoved() {
				return true;
			}
		};
	}

	/**
	 * Listen for excluded observation change notification and add/remove it
	 * from the relevant collections. We need to re-calculate the means series
	 * if any of the excluded observations' series type is the same as the mean
	 * source series type.
	 */
	protected Listener<ExcludedObservationMessage> createExcludedChangeListener() {

		final ObservationAndMeanPlotModel model = this;

		return new Listener<ExcludedObservationMessage>() {

			@Override
			public void update(ExcludedObservationMessage info) {
				List<ValidObservation> obs = info.getObservations();
				boolean excluded = obs.get(0).isExcluded();

				// Did we go to or from being excluded?
				if (excluded) {
					for (ValidObservation ob : info.getObservations()) {
						// Now marked as excluded so move observation from
						// its designated band to the excluded series.
						removeObservationFromSeries(ob, ob.getBand());
					}
					// All are going to the same series, so we can do this
					// en-masse. Note that we cannot do the reverse en-masse!
					addObservationsToSeries(obs, SeriesType.Excluded);
				} else {
					// Was previously marked as excluded, now is not, so move
					// observation from the excluded series to its
					// designated series. Reversing observation exclusion is
					// less efficient than the initial exclusion.
					for (ValidObservation ob : info.getObservations()) {
						removeObservationFromSeries(ob, SeriesType.Excluded);
						addObservationToSeries(ob, ob.getBand());
					}
				}

				fireDatasetChanged();

				// If any of the excluded observations bands is the source of
				// the means series, re-compute the means series.
				for (ValidObservation ob : info.getObservations()) {
					if (ob.getBand() == seriesNumToSrcTypeMap
							.get(meanSourceSeriesNum)) {
						model.setMeanSeries(false);
						break;
					}
				}
			}

			@Override
			public boolean canBeRemoved() {
				return true;
			}
		};
	}

	/**
	 * Update the model's fit and residual observation collections.
	 */
	protected void updateModelSeries(List<ValidObservation> modelObs,
			List<ValidObservation> residualObs, IModel model) {

		// Add or replace model pointing to continuous function.
//		if (this.seriesExists(SeriesType.ModelFunction)) {
//			// Replace it.
//			this.modelFunction = model.getModelFunction();
//		} else {
//			// Add it.
//			// TODO: create up front like we do with some other series, e.g. in
//			// obs retriever?
//			modelFunctionSeriesNum = getNextSeriesNum();
//
//			this.srcTypeToSeriesNumMap.put(SeriesType.ModelFunction,
//					modelFunctionSeriesNum);
//
//			this.seriesNumToSrcTypeMap.put(modelFunctionSeriesNum,
//					SeriesType.ModelFunction);
//
//			this.seriesVisibilityMap.put(SeriesType.ModelFunction,
//					isSeriesVisibleByDefault(SeriesType.ModelFunction));
//
//			this.modelFunction = model.getModelFunction();
//		}

		// Add or replace a series for the model and make sure
		// the series is visible.
		if (this.seriesExists(SeriesType.Model)) {
			modelSeriesNum = replaceObservationSeries(SeriesType.Model,
					modelObs);
		} else {
			modelSeriesNum = addObservationSeries(SeriesType.Model, modelObs);
		}

		// Make the model series visible either because this
		// is its first appearance or because it may have been made
		// invisible via the change series dialog.
		this.changeSeriesVisibility(modelSeriesNum, true);

		// Make the model function series visible either because this
		// is its first appearance or because it may have been made
		// invisible via the change series dialog.
//		this.changeSeriesVisibility(modelFunctionSeriesNum, true);

		// TODO: do we really need this? if not, revert means join
		// handling code
		// this.addSeriesToBeJoinedVisually(modelSeriesNum);

		// Add or replace a series for the residuals.
		if (this.seriesExists(SeriesType.Residuals)) {
			this.replaceObservationSeries(SeriesType.Residuals, residualObs);
		} else {
			residualsSeriesNum = addObservationSeries(SeriesType.Residuals,
					residualObs);
		}

		// Hide the residuals series initially. We toggle the series
		// visibility to achieve this since the default is false. That
		// shouldn't be necessary; investigate.
		// this.changeSeriesVisibility(residualsSeriesNum, true);
		changeSeriesVisibility(residualsSeriesNum, false);
	}

	/**
	 * @see org.aavso.tools.vstar.ui.model.plot.ObservationPlotModel#createModelSelectionListener()
	 */
	@Override
	protected Listener<ModelSelectionMessage> createModelSelectionListener() {

		final ObservationAndMeanPlotModel model = this;

		return new Listener<ModelSelectionMessage>() {
			@Override
			public void update(ModelSelectionMessage info) {
				updateModelSeries(info.getModel().getFit(), info.getModel()
						.getResiduals(), info.getModel());

				// If the means sources series is model or residuals (from
				// previous modelling operation), re-compute the means series.
				if (seriesNumToSrcTypeMap.get(meanSourceSeriesNum) == SeriesType.Model
						|| seriesNumToSrcTypeMap.get(meanSourceSeriesNum) == SeriesType.Residuals) {
					model.setMeanSeries(false);
				}
			}

			@Override
			public boolean canBeRemoved() {
				return true;
			}
		};
	}

	protected boolean handleNoFilter(FilteredObservationMessage info) {
		boolean result = false;

		if (info == FilteredObservationMessage.NO_FILTER) {
			// No filter, so make the filtered series invisible.
			if (this.seriesExists(SeriesType.Filtered)) {
				int num = this.getSrcTypeToSeriesNumMap().get(
						SeriesType.Filtered);
				changeSeriesVisibility(num, false);
			}
			result = true;
		}

		return result;
	}

	protected void updateFilteredSeries(List<ValidObservation> obs) {
		if (this.seriesExists(SeriesType.Filtered)) {
			filterSeriesNum = replaceObservationSeries(SeriesType.Filtered, obs);
		} else {
			filterSeriesNum = addObservationSeries(SeriesType.Filtered, obs);
		}

		// Make the filter series visible either because this is
		// its first appearance or because it may have been made
		// invisible via a previous NO_FILTER message.
		changeSeriesVisibility(filterSeriesNum, true);
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
