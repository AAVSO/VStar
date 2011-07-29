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

package org.aavso.tools.vstar.input;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.aavso.tools.vstar.data.InvalidObservation;
import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.exception.ObservationReadError;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.message.StopRequestMessage;
import org.aavso.tools.vstar.util.notification.Listener;

/**
 * This is the abstract base class for all observation retrieval classes,
 * irrespective of source (AAVSO standard file format, simple file format, VStar
 * database).
 */
public abstract class AbstractObservationRetriever {

	private final static int DEFAULT = -1;

	private double minMag;
	private double maxMag;

	protected boolean interrupted;

	/**
	 * The list of valid observations retrieved.
	 */
	protected ArrayList<ValidObservation> validObservations;

	/**
	 * The list of invalid observations retrieved.
	 */
	protected List<InvalidObservation> invalidObservations;

	/**
	 * A mapping from observation category (e.g. band, fainter-than) to list of
	 * valid observations.
	 */
	protected Map<SeriesType, List<ValidObservation>> validObservationCategoryMap;

	/**
	 * Constructor.
	 * 
	 * @param initialCapacity
	 *            The initial capacity of the valid observation list.
	 */
	public AbstractObservationRetriever(int initialCapacity) {
		this.validObservations = new ArrayList<ValidObservation>();
		this.invalidObservations = new ArrayList<InvalidObservation>();

		// Optionally set the capacity of the valid observation list to speed up
		// out-of-order insertion due to the shifting operations required.
		if (initialCapacity != DEFAULT) {
			this.validObservations.ensureCapacity(initialCapacity);
		}

		// Create observation category map and add discrepant and excluded
		// series list so these are available if needed.
		// In case filtered, model, and residual obs are later created, add
		// these to the map. Means are created for each data set loaded, so
		// don't need to add them here. TODO: we *could* add means here
		// though...
		// ...that might simplify handling of that series in model code...
		this.validObservationCategoryMap = new TreeMap<SeriesType, List<ValidObservation>>();

		this.validObservationCategoryMap.put(SeriesType.DISCREPANT,
				new ArrayList<ValidObservation>());

		this.validObservationCategoryMap.put(SeriesType.Excluded,
				new ArrayList<ValidObservation>());

		this.validObservationCategoryMap.put(SeriesType.Filtered,
				new ArrayList<ValidObservation>());

		this.validObservationCategoryMap.put(SeriesType.Model,
				new ArrayList<ValidObservation>());

		this.validObservationCategoryMap.put(SeriesType.Residuals,
				new ArrayList<ValidObservation>());

		this.minMag = Double.MAX_VALUE;
		this.maxMag = -Double.MAX_VALUE;

		interrupted = false;

		Mediator.getInstance().getStopRequestNotifier().addListener(
				createStopRequestListener());
	}

	/**
	 * Constructor.
	 */
	public AbstractObservationRetriever() {
		this(DEFAULT);
	}

	/**
	 * @return the minimum magnitude
	 */
	public double getMinMag() {
		return minMag;
	}

	/**
	 * @param minMag
	 *            the minimum magnitude to set
	 */
	public void setMinMag(double minMag) {
		this.minMag = minMag;
	}

	/**
	 * @return the maximum magnitude
	 */
	public double getMaxMag() {
		return maxMag;
	}

	/**
	 * @param maxMag
	 *            the maximum magnitude to set
	 */
	public void setMaxMag(double maxMag) {
		this.maxMag = maxMag;
	}

	/**
	 * @return was this retriever interrupted?
	 */
	public final boolean wasInterrupted() {
		return interrupted;
	}

	/**
	 * Retrieve the set of observations from the specified source.
	 * 
	 * @throws throws ObservationReadError
	 */
	public abstract void retrieveObservations() throws ObservationReadError,
			InterruptedException;

	/**
	 * Retrieve the name of the source of the observations.
	 * 
	 * @return The source name.
	 */
	public abstract String getSourceName();

	/**
	 * @return the validObservations
	 */
	public List<ValidObservation> getValidObservations() {
		return validObservations;
	}

	/**
	 * @return the invalidObservations
	 */
	public List<InvalidObservation> getInvalidObservations() {
		return invalidObservations;
	}

	/**
	 * @return the validObservationCategoryMap
	 */
	public Map<SeriesType, List<ValidObservation>> getValidObservationCategoryMap() {
		return validObservationCategoryMap;
	}

	/**
	 * <p>
	 * Add an observation to the list of valid observations.
	 * </p>
	 * 
	 * <p>
	 * This is a convenience method that adds an observation to the list of
	 * valid observations and categorises it by band. This method is
	 * particularly suitable for observation source plugins since it asks
	 * whether an observation satisfies the requirement that it has at least JD
	 * and magnitude values. The caller can either propagate this exception
	 * further or add to the invalid observation list, or do whatever else it
	 * considers to be appropriate.
	 * </p>
	 * 
	 * @param ob
	 *            The valid observation to be added to collections.
	 */
	protected void collectObservation(ValidObservation ob)
			throws ObservationReadError {
		if (ob.getDateInfo() == null) {
			throw new ObservationReadError("Observation #"
					+ ob.getRecordNumber() + " has no date.");
		}

		if (ob.getMagnitude() == null) {
			throw new ObservationReadError("Observation #"
					+ ob.getRecordNumber() + " has no magnitude.");
		}

		addValidObservation(ob);
		categoriseValidObservation(ob);
	}

	/**
	 * Here we categorise a valid observation in terms of whether it is a
	 * fainter-than or discrepant or belongs to a particular band, in that
	 * order.
	 * 
	 * @param validOb
	 *            A valid observation.
	 */
	protected void categoriseValidObservation(ValidObservation validOb) {
		SeriesType category = null;

		if (validOb.getMagnitude().isFainterThan()) {
			category = SeriesType.FAINTER_THAN;
		} else if (validOb.isDiscrepant()) {
			category = SeriesType.DISCREPANT;
		} else if (validOb.isExcluded()) {
			category = SeriesType.Excluded;
		} else {
			category = validOb.getBand();
		}

		List<ValidObservation> validObsList = validObservationCategoryMap
				.get(category);

		if (validObsList == null) {
			validObsList = new ArrayList<ValidObservation>();
			validObservationCategoryMap.put(category, validObsList);
		}

		insertObservation(validOb, validObsList);
	}

	/**
	 * Adds an observation to the list of valid observations. Also, updates
	 * min/max magnitude values for the dataset.
	 * 
	 * @param ob
	 *            The valid observation to be added.
	 */
	protected void addValidObservation(ValidObservation ob) {
		insertObservation(ob, validObservations);

		double uncert = ob.getMagnitude().getUncertainty();
		// If uncertainty not given, get HQ uncertainty if present.
		if (uncert == 0.0 && ob.getHqUncertainty() != null) {
			uncert = ob.getHqUncertainty();
		}

		if (ob.getMag() - uncert < minMag) {
			minMag = ob.getMag() - uncert;
		}

		if (ob.getMag() + uncert > maxMag) {
			maxMag = ob.getMag() + uncert;
		}
	}

	/**
	 * <p>
	 * Insert an observation into the observation list with the post-condition
	 * that the list remains sorted by JD. This post-condition is valid iff the
	 * pre-condition that the list is already sorted before the addition is
	 * true.
	 * </p>
	 * 
	 * <p>
	 * An observation source plug-in developer may want to completely override
	 * this method if data is expected to be mostly out of order; in the worst
	 * case, if all elements are out of order, the cost will be O(n^2) due to
	 * the O(n) traversal being carried out n times (for the number of
	 * observations inserted).
	 * </p>
	 * 
	 * @param ob
	 *            The observation to be inserted.
	 * @param obs
	 *            The list into which it is to be inserted.
	 */
	protected void insertObservation(ValidObservation ob,
			List<ValidObservation> obs) {
		double newJD = ob.getJD();
		int obListSize = obs.size();

		if (obListSize == 0 || newJD >= obs.get(obListSize - 1).getJD()) {
			// The list is empty or the observation's JD is at least as
			// high as that of the last observation in the list.
			obs.add(ob);
		} else {
			// The observation has a JD that is less than that of the
			// observation at the end of the list. Incur an O(n) cost to
			// insert the observation into the correct position in order to
			// satisfy the post-condition.
			int i = 0;
			while (i < obListSize && newJD > obs.get(i).getJD()) {
				i++;
			}
			obs.add(i, ob);
		}
	}

	/**
	 * Add an observation to the list of invalid observations.
	 * 
	 * @param ob
	 *            The invalid observation to be added.
	 */
	protected void addInvalidObservation(InvalidObservation ob) {
		invalidObservations.add(ob);
	}

	// Creates a stop request listener.
	private Listener<StopRequestMessage> createStopRequestListener() {
		return new Listener<StopRequestMessage>() {
			@Override
			public void update(StopRequestMessage info) {
				interrupted = true;
			}

			@Override
			public boolean canBeRemoved() {
				return false;
			}
		};
	}
}