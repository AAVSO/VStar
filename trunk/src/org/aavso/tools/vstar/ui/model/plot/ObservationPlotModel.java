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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.message.ObservationChangeMessage;
import org.aavso.tools.vstar.ui.mediator.message.ObservationChangeType;
import org.aavso.tools.vstar.util.notification.Listener;
import org.jfree.data.DomainOrder;
import org.jfree.data.xy.AbstractIntervalXYDataset;

/**
 * This is the base class for models that represent a series of valid variable
 * star observations, e.g. for different bands (or from different sources).
 */
public class ObservationPlotModel extends AbstractIntervalXYDataset implements
		Listener<ObservationChangeMessage> {

	/**
	 * Coordinate and error source.
	 */
	private ICoordSource coordSrc;

	/**
	 * An observation comparator (e.g. to provide an ordering over time: JD or
	 * phase).
	 */
	private Comparator<ValidObservation> obComparator;

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
	 * A collection of series to be joined visually.
	 */
	protected Set<Integer> seriesToBeJoinedVisually;

	/**
	 * Common constructor.
	 * 
	 * @param coordSrc
	 *            A coordinate and error source.
	 */
	private ObservationPlotModel(ICoordSource coordSrc) {
		super();
		this.coordSrc = coordSrc;
		this.seriesNum = 0;
		this.seriesNumToSrcTypeMap = new HashMap<Integer, SeriesType>();
		this.srcTypeToSeriesNumMap = new TreeMap<SeriesType, Integer>();
		this.seriesVisibilityMap = new HashMap<Integer, Boolean>();
		this.seriesNumToObSrcListMap = new HashMap<Integer, List<ValidObservation>>();
		this.atLeastOneVisualBandPresent = false;
		this.seriesToBeJoinedVisually = new HashSet<Integer>();

		Mediator.getInstance().getObservationChangeNotifier().addListener(this);
	}

	/**
	 * Constructor (for light curve plots).
	 * 
	 * We add named observation source lists to unique series numbers.
	 * 
	 * @param obsSourceListMap
	 *            A mapping from source series to lists of observation sources.
	 * @param coordSrc
	 *            A coordinate and error source.
	 * @param obComparator
	 *            A valid observation comparator (e.g. by JD or phase).
	 */
	public ObservationPlotModel(
			Map<SeriesType, List<ValidObservation>> obsSourceListMap,
			ICoordSource coordSrc, Comparator<ValidObservation> obComparator) {

		this(coordSrc);

		this.obComparator = obComparator;

		for (SeriesType type : obsSourceListMap.keySet()) {
			this.addObservationSeries(type, obsSourceListMap.get(type));
		}
		fireDatasetChanged();

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
	 * Constructor (for phase plots).
	 * 
	 * We add named observation source lists to unique series numbers, and if
	 * the visibility map is non-null, potentially change the set of visible
	 * series.
	 * 
	 * @param obsSourceListMap
	 *            A mapping from source series to lists of observation sources.
	 * @param coordSrc
	 *            A coordinate and error source.
	 * @param seriesVisibilityMap
	 *            A mapping from series number to visibility status.
	 * @param obComparator
	 *            A valid observation comparator (e.g. by JD or phase).
	 */
	public ObservationPlotModel(
			Map<SeriesType, List<ValidObservation>> obsSourceListMap,
			ICoordSource coordSrc, Comparator<ValidObservation> obComparator,
			Map<Integer, Boolean> seriesVisibilityMap) {

		this(obsSourceListMap, coordSrc, obComparator);

		if (seriesVisibilityMap != null) {
			for (int seriesNum : seriesVisibilityMap.keySet()) {
				changeSeriesVisibility(seriesNum, seriesVisibilityMap
						.get(seriesNum));
			}
		}
	}

	/**
	 * Add an observation series, first removing an existing entry for the
	 * specified series type if one exists.
	 * 
	 * @param type
	 *            The series type to be associated with the series.
	 * @param obs
	 *            A series (list) of observations, in particular, magnitude and
	 *            Julian Day.
	 * @return The number of the series added.
	 * @precondition The series has not yet been added to the plot.
	 * @postcondition Both seriesNumToObSrcListMap and seriesNumToSrcTypeMap
	 *                must be the same length.
	 */
	public int addObservationSeries(SeriesType type, List<ValidObservation> obs) {

		assert this.srcTypeToSeriesNumMap.get(type) == null;

		int seriesNum = this.getNextSeriesNum();

		this.srcTypeToSeriesNumMap.put(type, seriesNum);
		this.seriesNumToObSrcListMap.put(seriesNum, obs);
		this.seriesNumToSrcTypeMap.put(seriesNum, type);
		this.seriesVisibilityMap.put(seriesNum, isSeriesVisibleByDefault(type));

		assert (this.seriesNumToObSrcListMap.size() == this.seriesNumToSrcTypeMap
				.size());

		return seriesNum;
	}

	/**
	 * Add a single observation to a series list, creating the series first if
	 * necessary.
	 * 
	 * @param ob
	 *            A valid observation.
	 * @param series
	 *            A series.
	 */
	protected void addObservationToSeries(ValidObservation ob, SeriesType series) {
		Integer seriesNum = this.srcTypeToSeriesNumMap.get(series);

		if (seriesNum != null) {
			List<ValidObservation> obList = this.seriesNumToObSrcListMap
					.get(seriesNum);
			obList.add(ob);
			// TODO: this is an expensive operation for the addition of a
			// single observation! Perhaps we should mandate these lists as
			// having to be SortedSet. Traversal is no more expensive and
			// insertion is Log2(n).
			// SortedSet<E> l = null;
			Collections.sort(obList, obComparator);
		} else {
			// The series does not yet exist, so create it with
			// a single datapoint.
			List<ValidObservation> obsList = new ArrayList<ValidObservation>();
			obsList.add(ob);
			addObservationSeries(series, obsList);
		}
	}

	/**
	 * Replace an existing series
	 * 
	 * @param type
	 *            The series type to be associated with the series.
	 * @param obs
	 *            A series (list) of observations, in particular, magnitude and
	 *            Julian Day.
	 * @return The number of the series added.
	 * @precondition The series has already been added to the plot.
	 */
	public void replaceObservationSeries(SeriesType type,
			List<ValidObservation> obs) {
		Integer seriesNum = this.srcTypeToSeriesNumMap.get(type);
		assert seriesNum != null;
		this.seriesNumToObSrcListMap.put(seriesNum, obs);
		this.fireDatasetChanged();
	}

	/**
	 * Remove the specified series from the model.
	 * 
	 * Whether or not the named series was removed (it may not have existed to
	 * begin with) is returned. The caller can determine whether or not this
	 * matters.
	 * 
	 * Note: Be careful with this method, otherwise there will be gaps in the
	 * series number sequence. Arguably we should remove this method.
	 * 
	 * @param type
	 *            The series type.
	 * @return Whether or not the series was removed.
	 */
	public boolean removeObservationSeries(SeriesType type) {
		boolean found = false;

		Integer seriesNum = this.srcTypeToSeriesNumMap.get(type);

		if (seriesNum != null) {
			this.srcTypeToSeriesNumMap.remove(type);
			this.seriesNumToSrcTypeMap.remove(seriesNum);
			this.seriesNumToObSrcListMap.remove(seriesNum);
			this.seriesVisibilityMap.remove(seriesNum);
			this.fireDatasetChanged();
			found = true;
		}

		return found;
	}

	/**
	 * Remove a single observation from a series list.
	 * 
	 * @param ob
	 *            A valid observation.
	 * @param series
	 *            A series.
	 * @return Whether or not the observation was removed.
	 */
	protected boolean removeObservationFromSeries(ValidObservation ob,
			SeriesType series) {
		boolean removed = false;

		Integer seriesNum = this.srcTypeToSeriesNumMap.get(series);

		if (seriesNum != null) {
			removed = this.seriesNumToObSrcListMap.get(seriesNum).remove(ob);
		}

		return removed;
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
	 * Does the specified series type exist, i.e. has it been added to the plot?
	 * 
	 * @param type
	 *            The series type in question.
	 * @return Whether the series has been added to the plot.
	 */
	public boolean seriesExists(SeriesType type) {
		return this.srcTypeToSeriesNumMap.containsKey(type);
	}

	/**
	 * @see org.jfree.data.xy.XYDataset#getItemCount(int)
	 * @return The number of observations (items) in the requested series.
	 */
	public int getItemCount(int series) {
		return coordSrc.getItemCount(series, seriesNumToObSrcListMap);
	}

	// TODO: are these next two still required? (are they ever hit?)

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

	// TODO: in future, I think we want to get rid of this approach and just
	// leave all series join login in the view classes.
	// In addition, it ought to be possible for *any* series to joined, so we
	// need to unify this at the series change dialog level (for example).
	
	/**
	 * Which series' elements should be joined visually (e.g. with lines)?
	 * 
	 * @return A collection of series numbers for series whose elements should
	 *         be joined visually.
	 */
	public Collection<Integer> getSeriesWhoseElementsShouldBeJoinedVisually() {
		return seriesToBeJoinedVisually;
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

		if (hqUncertainty != null && hqUncertainty != 0) {
			error = hqUncertainty;
		} else {
			error = this.seriesNumToObSrcListMap.get(series).get(item)
					.getMagnitude().getUncertainty();
		}

		return error;
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
	 * @param series
	 *            The series name.
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

	/**
	 * Listen for observation change notification, e.g. an observation's
	 * discrepant status is changed.
	 */
	public void update(ObservationChangeMessage info) {
		for (ObservationChangeType change : info.getChanges()) {
			switch (change) {
			case DISCREPANT:
				// Did we go to or from being discrepant?
				ValidObservation ob = info.getObservation();
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
				break;
			}
		}
	}

	/**
	 * @see org.aavso.tools.vstar.util.notification.Listener#canBeRemoved()
	 */
	public boolean canBeRemoved() {
		return true;
	}
}
