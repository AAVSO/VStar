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
package org.aavso.tools.vstar.ui.model;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.util.notification.Listener;
import org.jfree.data.DomainOrder;
import org.jfree.data.xy.AbstractIntervalXYDataset;

/**
 * This is the base class for models that represent a series of valid variable
 * star observations, e.g. for different bands (or from different sources).
 */
public class ObservationPlotModel extends AbstractIntervalXYDataset implements
		Listener<ValidObservation> {

	/**
	 * Coordinate and error source.
	 */
	private ICoordSource coordSrc;

	/**
	 * A unique next series number for this model.
	 */
	private int seriesNum;

	/**
	 * A mapping from series number to a list of observations where each such
	 * list is a data series.
	 */
	protected Map<Integer, List<ValidObservation>> seriesNumToObSrcListMap;

	/**
	 * A mapping from series number to source name.
	 */
	protected Map<Integer, SeriesType> seriesNumToSrcTypeMap;

	/**
	 * A mapping from source name to series number.
	 */
	protected Map<SeriesType, Integer> srcTypeToSeriesNumMap;

	/**
	 * A mapping from series numbers to whether or not they visible.
	 */
	protected Map<Integer, Boolean> seriesVisibilityMap;

	/**
	 * Is at least one visual band observation present?
	 */
	protected boolean atLeastOneVisualBandPresent;

	/**
	 * Constructor.
	 * 
	 * @param coordSrc
	 *            A coordinate and error source.
	 */
	public ObservationPlotModel(ICoordSource coordSrc) {
		super();
		this.coordSrc = coordSrc;
		this.seriesNum = 0;
		this.seriesNumToSrcTypeMap = new TreeMap<Integer, SeriesType>();
		this.srcTypeToSeriesNumMap = new TreeMap<SeriesType, Integer>();
		this.seriesVisibilityMap = new TreeMap<Integer, Boolean>();
		this.seriesNumToObSrcListMap = new TreeMap<Integer, List<ValidObservation>>();
		this.atLeastOneVisualBandPresent = false;
	}

	/**
	 * Constructor
	 * 
	 * We add named observation source lists to unique series numbers.
	 * 
	 * @param obsSourceListMap
	 *            A mapping from source series to lists of observation sources.
	 * @param coordSrc
	 *            A coordinate and error source.
	 */
	public ObservationPlotModel(
			Map<SeriesType, List<ValidObservation>> obsSourceListMap,
			ICoordSource coordSrc) {

		this(coordSrc);

		for (SeriesType type : obsSourceListMap.keySet()) {
			this.addObservationSeries(type, obsSourceListMap.get(type));
		}

		// We should only make "unspecified" band-based observations visible
		// by default if one of the visual bands is not present.
		// See
		// https://sourceforge.net/tracker/?func=detail&aid=2837957&group_id=263306&atid=1152052
		if (atLeastOneVisualBandPresent) {
			if (srcTypeToSeriesNumMap.containsKey(SeriesType.Unspecified)) {
				int unspecifiedSeriesNum = srcTypeToSeriesNumMap
						.get(SeriesType.Unspecified);
				if (seriesVisibilityMap.get(unspecifiedSeriesNum) == true) {
					seriesVisibilityMap.put(unspecifiedSeriesNum, false);
					fireDatasetChanged();
				}
			}
		}
	}

	/**
	 * Constructor
	 * 
	 * We add named observation source lists to unique series numbers,
	 * and if the map is non-null, potentially change the set of visible 
	 * series.
	 * 
	 * @param obsSourceListMap
	 *            A mapping from source series to lists of observation sources.
	 * @param coordSrc
	 *            A coordinate and error source.
	 * @param seriesVisibilityMap
	 *            A mapping from series number to visibility status.
	 */
	public ObservationPlotModel(
			Map<SeriesType, List<ValidObservation>> obsSourceListMap,
			ICoordSource coordSrc, Map<Integer, Boolean> seriesVisibilityMap) {

		this(obsSourceListMap, coordSrc);
		
		if (seriesVisibilityMap != null) {
			for (int seriesNum : seriesVisibilityMap.keySet()) {
				changeSeriesVisibility(seriesNum, seriesVisibilityMap.get(seriesNum));
			}
		}
	}

	/**
	 * Add an observation series.
	 * 
	 * @param type
	 *            The series type to be associated with the series.
	 * @param obSourceList
	 *            A series (list) of observations, in particular, magnitude and
	 *            Julian Day.
	 * @return The number of the series added.
	 * @postcondition Both seriesNumToObSrcListMap and seriesNumToSrcTypeMap
	 *                must be the same length.
	 */
	public int addObservationSeries(SeriesType type,
			List<ValidObservation> obSourceList) {

		int seriesNum = this.getNextSeriesNum();

		this.srcTypeToSeriesNumMap.put(type, seriesNum);
		this.seriesNumToObSrcListMap.put(seriesNum, obSourceList);
		this.seriesNumToSrcTypeMap.put(seriesNum, type);
		this.seriesVisibilityMap.put(seriesNum, isSeriesVisibleByDefault(type));

		assert (this.seriesNumToObSrcListMap.size() == this.seriesNumToSrcTypeMap
				.size());

		fireDatasetChanged();

		return seriesNum;
	}

	/**
	 * Remove the named series from the model. This operation has time
	 * complexity O(n) but n (the number of series) will never be too large.
	 * 
	 * Whether or not the named series was removed (it may not have existed to
	 * begin with) is returned. The caller can determine whether or not this
	 * matters.
	 * 
	 * @param name
	 *            The source name of the series.
	 * @return Whether or not the series was removed.
	 */
	public boolean removeObservationSeries(String name) {
		boolean found = false;

		for (Map.Entry<Integer, SeriesType> entry : this.seriesNumToSrcTypeMap
				.entrySet()) {
			if (name.equals(entry.getValue())) {
				this.srcTypeToSeriesNumMap.remove(name);
				int series = entry.getKey();
				this.seriesNumToSrcTypeMap.remove(series);
				this.seriesNumToObSrcListMap.remove(series);
				this.seriesVisibilityMap.remove(series);
				this.fireDatasetChanged();
				found = true;
				break;
			}
		}

		return found;
	}

	/**
	 * Attempt to change the specified series' visibility.
	 * 
	 * @param seriesNum
	 *            The series number of interest.
	 * @param visibility
	 *            Whether this series should be visible.
	 * @return Whether or not the visibility of the object changed.
	 */
	public boolean changeSeriesVisibility(int seriesNum, boolean visibility) {
		Boolean currVis = this.seriesVisibilityMap.get(seriesNum);

		boolean changed = currVis != null && currVis != visibility;

		if (changed) {
			this.seriesVisibilityMap.put(seriesNum, visibility);
			this.fireDatasetChanged();
		}

		return changed;
	}

	/**
	 * @see org.jfree.data.general.AbstractSeriesDataset#getSeriesCount()
	 * @param Return
	 *            the number of observation series that exist on the plot.
	 */
	public int getSeriesCount() {
		return this.seriesNumToObSrcListMap.size();
	}

	/**
	 * @see org.jfree.data.general.AbstractSeriesDataset#getSeriesKey(int)
	 */
	public Comparable getSeriesKey(int series) {
		if (!seriesNumToObSrcListMap.containsKey(series)) {
			throw new IllegalArgumentException("Series number '" + series
					+ "' out of range.");
		}

		return this.seriesNumToSrcTypeMap.get(series);
	}

	/**
	 * Get an array of series keys.
	 * 
	 * @return The array of series keys.
	 */
	public SeriesType[] getSeriesKeys() {
		return this.srcTypeToSeriesNumMap.keySet().toArray(new SeriesType[0]);
	}

	/**
	 * @see org.jfree.data.xy.XYDataset#getItemCount(int)
	 * @return The number of observations (items) in the requested series.
	 */
	public int getItemCount(int series) {
		return coordSrc.getItemCount(series, seriesNumToObSrcListMap);
	}

	// TODO: are these next two still required?

	/**
	 * @see org.jfree.data.xy.XYDataset#getX(int, int)
	 */
	public Number getX(int series, int item) {
		return coordSrc.getXCoord(series, item, this.seriesNumToObSrcListMap);
	}

	/**
	 * @see org.jfree.data.xy.XYDataset#getY(int, int)
	 */
	public Number getY(int series, int item) {
		return getMagAsYCoord(series, coordSrc.getActualYItemNum(series, item,
				seriesNumToObSrcListMap));
	}

	/**
	 * @see org.jfree.data.xy.AbstractXYDataset#getDomainOrder()
	 */
	public DomainOrder getDomainOrder() {
		return DomainOrder.ASCENDING;
	}

	/**
	 * Which series' elements should be joined visually (e.g. with lines)?
	 * 
	 * TODO: need to specialise this later to join observer's obs etc?
	 * 
	 * @return A collection of series numbers for series whose elements should
	 *         be joined visually.
	 */
	public Collection<Integer> getSeriesWhoseElementsShouldBeJoinedVisually() {
		return new TreeSet<Integer>();
	}

	// AbstractIntervalXYDataSet methods.
	// To be used for error bar handling.

	public Number getStartX(int series, int item) {
		return coordSrc.getXCoord(series, item, this.seriesNumToObSrcListMap);
	}

	public Number getEndX(int series, int item) {
		return coordSrc.getXCoord(series, item, this.seriesNumToObSrcListMap);
	}

	public Number getStartY(int series, int item) {
		int actualItem = coordSrc.getActualYItemNum(series, item,
				seriesNumToObSrcListMap);
		return getMagAsYCoord(series, actualItem)
				- getMagError(series, actualItem);
	}

	public Number getEndY(int series, int item) {
		int actualItem = coordSrc.getActualYItemNum(series, item,
				seriesNumToObSrcListMap);
		return getMagAsYCoord(series, actualItem)
				+ getMagError(series, actualItem);
	}

	// Helpers

	private int getNextSeriesNum() {
		return seriesNum++;
	}

	/**
	 * Return the magnitude as the Y coordinate.
	 * 
	 * @param series
	 *            The series number.
	 * @param item
	 *            The item number within the series.
	 * @return The magnitude value.
	 */
	protected double getMagAsYCoord(int series, int item) {
		return this.seriesNumToObSrcListMap.get(series).get(item)
				.getMagnitude().getMagValue();
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
		double error = 0;

		// If the HQ uncertainty field is non-null, use that, otherwise
		// use the uncertainty value, which may be zero, in which case
		// the error will be zero.

		Double hqUncertainty = this.seriesNumToObSrcListMap.get(series).get(
				item).getHqUncertainty();

		if (hqUncertainty != null) {
			error = hqUncertainty;
		} else {
			error = this.seriesNumToObSrcListMap.get(series).get(item)
					.getMagnitude().getUncertainty();
		}

		return error;
	}

	/**
	 * Listen for valid observation change notification, e.g. an observation is
	 * marked as discrepant.
	 */
	public void update(ValidObservation ob) {
		// TODO: We do nothing for now. What we do need to
		// do is to plot the value in a different color,
		// and the best way to do that is to move it
		// to a different band. This of course assumes
		// that the change to ob is of the "discrepant"
		// value.
	}

	/**
	 * Given a series and item number, return the corresponding observation.
	 * 
	 * @param series
	 *            The series number.
	 * @param item
	 *            The item within the series.
	 * @return The valid observation.
	 * @throws IllegalArgumentException
	 *             if series or item are out of range.
	 */
	public ValidObservation getValidObservation(int series, int item) {
		return coordSrc.getValidObservation(series, item,
				seriesNumToObSrcListMap);
	}

	/**
	 * @return the seriesNumToObSrcListMap
	 */
	public Map<Integer, List<ValidObservation>> getSeriesNumToObSrcListMap() {
		return seriesNumToObSrcListMap;
	}

	/**
	 * @return the srcTypeToSeriesNumMap
	 */
	public Map<SeriesType, Integer> getSrcTypeToSeriesNumMap() {
		return srcTypeToSeriesNumMap;
	}

	/**
	 * @return the seriesNumToSrcTypeMap
	 */
	public Map<Integer, SeriesType> getSeriesNumToSrcTypeMap() {
		return seriesNumToSrcTypeMap;
	}

	/**
	 * @return the seriesVisibilityMap
	 */
	public Map<Integer, Boolean> getSeriesVisibilityMap() {
		return seriesVisibilityMap;
	}

	// Helper methods.

	/**
	 * Should the specified series be visible by default?
	 * 
	 * TODO: for now we just look for visual bands; eventually this should be
	 * specifiable via Preferences; check that we add this to the notes ticket
	 * on this topic.
	 * 
	 * @param series
	 *            The series name. TODO: should eventually be enum.
	 * @return Whether or not the series should be visible by default.
	 */
	protected boolean isSeriesVisibleByDefault(SeriesType series) {
		boolean visible = false;

		// Look for Visual, then V.

		// TODO: We could delegate "is visible" to SeriesType which
		// could also be used by means plot model code.

		visible |= series == SeriesType.Visual;

		visible |= series == SeriesType.Johnson_V;

		if (!atLeastOneVisualBandPresent && visible) {
			atLeastOneVisualBandPresent = true;
		}

		// We also allow for unspecified series type, e.g. since the source
		// could be from a simple observation file where no band is
		// specified.
		//
		// People could use this for visual, CCD/DSLR photometry observation
		// etc.
		visible |= series == SeriesType.Unspecified;

		return visible;
	}
}
