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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

import org.aavso.tools.vstar.data.InvalidObservation;
import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.data.ValidObservation.JDflavour;
import org.aavso.tools.vstar.exception.ObservationReadError;
import org.aavso.tools.vstar.ui.dialog.MessageBox;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.StarInfo;
import org.aavso.tools.vstar.ui.mediator.message.ProgressInfo;
import org.aavso.tools.vstar.ui.mediator.message.ProgressType;
import org.aavso.tools.vstar.ui.mediator.message.StopRequestMessage;
import org.aavso.tools.vstar.util.locale.LocaleProps;
import org.aavso.tools.vstar.util.notification.Listener;
import org.aavso.tools.vstar.vela.Operand;
import org.aavso.tools.vstar.vela.Type;
import org.aavso.tools.vstar.vela.VeLaEvalError;
import org.aavso.tools.vstar.vela.VeLaInterpreter;
import org.aavso.tools.vstar.vela.VeLaParseError;
import org.aavso.tools.vstar.vela.VeLaValidObservationEnvironment;

/**
 * This is the abstract base class for all observation retrieval classes,
 * irrespective of source (AAVSO standard file format, simple file format, VStar
 * database).
 */
public abstract class AbstractObservationRetriever {

	public final String MAGNITUDE = LocaleProps.get("MAGNITUDE");

	public final static int DEFAULT_CAPACITY = -1;
	public final static String NO_VELA_FILTER = "";

	private final static boolean VERBOSE = false;
	private final static boolean ADD_VSTAR_API = false;
	
	private String velaFilterStr;

	private VeLaInterpreter vela;

	private boolean velaErrorReported;

	private double minMag;
	private double maxMag;

	protected boolean interrupted;

	protected JDflavour jdFlavour;
	
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
	 * @param velaFilterStr
	 *            The VeLa filter string to be applied for each observation
	 *            before being added to the valid observation list.
	 */
	public AbstractObservationRetriever(int initialCapacity,
			String velaFilterStr) {
		this.validObservations = new ArrayList<ValidObservation>();
		this.invalidObservations = new ArrayList<InvalidObservation>();

		// Optionally set the capacity of the valid observation list to speed up
		// out-of-order insertion due to the shifting operations required.
		if (initialCapacity != DEFAULT_CAPACITY) {
			this.validObservations.ensureCapacity(initialCapacity);
		}

		this.velaFilterStr = velaFilterStr.trim();
		velaErrorReported = false;
		vela = new VeLaInterpreter(VERBOSE, ADD_VSTAR_API, Collections.emptyList());

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

		jdFlavour = JDflavour.JD;

		Mediator.getInstance().getStopRequestNotifier()
				.addListener(createStopRequestListener());
	}

	/**
	 * Constructor
	 */
	public AbstractObservationRetriever(String velaFilterStr) {
		this(DEFAULT_CAPACITY, velaFilterStr);
	}

	/**
	 * Constructor
	 */
	public AbstractObservationRetriever() {
		this(DEFAULT_CAPACITY, NO_VELA_FILTER);
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
	 * Retrieve the type of the source of the observations.
	 * 
	 * @return The source type.
	 */
	public abstract String getSourceType();

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
	 * Has this observation retriever pulled in observations that correspond to
	 * heliocentric JD values.
	 * 
	 * @return whether this observation retriever pulled in observations that
	 *         correspond to heliocentric JD values.
	 */
	public boolean isHeliocentric() {
		return jdFlavour == JDflavour.HJD;
	}

	/**
	 * @param isHeliocentric
	 * 
	 * @deprecated
	 * This method is deprecated.
	 * <p> Use {@link AbstractObservationRetriever#setJDflavour(JDflavour)} instead.
	 * 
	 */
	public void setHeliocentric(boolean isHeliocentric) {
		jdFlavour = isHeliocentric ? JDflavour.HJD : JDflavour.JD; 
	}

	/**
	 * @return the isBarycentric
	 */
	public boolean isBarycentric() {
		return jdFlavour == JDflavour.BJD;
	}

	/**
	 * @param isBarycentric
	 * 
	 * @deprecated
	 * This method is deprecated.
	 * <p> Use {@link AbstractObservationRetriever#setJDflavour(JDflavour)} instead.
	 */
	public void setBarycentric(boolean isBarycentric) {
		jdFlavour = isBarycentric ? JDflavour.BJD : JDflavour.JD; 
	}
	
	public JDflavour getJDflavour() {
		return jdFlavour; 
	}
	
	public void setJDflavour(JDflavour jdFlavour) {
		this.jdFlavour = jdFlavour; 
	}

	/**
	 * Returns a StarInfo instance for the object whose observations are being
	 * loaded. Concrete subclasses may want to specialise this to add more
	 * detail.
	 * 
	 * @return The StarInfo object.
	 */
	public StarInfo getStarInfo() {
		return new StarInfo(this, getSourceName());
	}

	/**
	 * Returns the time units string (e.g. JD, HJD, BJD, ...).
	 * 
	 * @return The time units string.
	 */
	public String getTimeUnits() {
		return jdFlavour.label;
	}

	/**
	 * Returns the brightness units string (e.g. Magnitude, Flux, ...).
	 * 
	 * @return The brightness units string.
	 */
	public String getBrightnessUnits() {
		return MAGNITUDE;
	}

	/**
	 * Set the VeLa filter string.
	 * 
	 * @param velaFilterStr
	 *            the velaFilterStr to set
	 */
	public void setVelaFilter(String velaFilterStr) {
		this.velaFilterStr = velaFilterStr;
	}

	/**
	 * @return the validObservationCategoryMap
	 */
	public Map<SeriesType, List<ValidObservation>> getValidObservationCategoryMap() {
		return validObservationCategoryMap;
	}

	/**
	 * Are there any series that should be excluded from addition in
	 * collectAllValidObservations() and collectAllInvalidObservations()?
	 * 
	 * @return The set of series to be excluded from addition; may be null.
	 */
	public Set<SeriesType> seriesToExcludeWhenAdditive() {
		return null;
	}

	/**
	 * Adds all of the specified observations to the current observations,
	 * including classifying them by series. This can be used for additive load
	 * operations.
	 * 
	 * @param obs
	 *            The list of previously existing valid observations to be
	 *            added.
	 * @param newSourceName
	 *            The name of the source for new obs (in this retriever).
	 *
	 * @throws ObservationReadError
	 *             if the observation has no date or magnitude. The caller can
	 *             either propagate this exception further or add to the invalid
	 *             observation list, or do whatever else it considers to be
	 *             appropriate.
	 */
	public void collectAllObservations(List<ValidObservation> obs,
			String newSourceName) throws ObservationReadError {

		// Set source name for new obs (those in this retriever).
		for (ValidObservation ob : validObservations) {
			ob.addDetail("SOURCE", newSourceName, "Source");
		}

		// Add previously existing obs (those passed to this method).
		Set<SeriesType> seriesToExclude = seriesToExcludeWhenAdditive();

		for (ValidObservation ob : obs) {
			// If there are no series to exclude or the observation's band is
			// not in the list of series to be excluded, include it.
			if (seriesToExclude == null
					|| !seriesToExclude.contains(ob.getBand())) {
				collectObservation(ob);
			}
		}
	}

	/**
	 * Adds all the specified invalid observations to the existing invalid
	 * observations. This can be used for additive load operations.
	 * 
	 * @param obs
	 *            The list of previously existing invalid observations.
	 */
	public void addAllInvalidObservations(List<InvalidObservation> obs) {
		for (InvalidObservation ob : obs) {
			addInvalidObservation(ob);
		}
	}

	/**
	 * Return number of records to be read if this observation retriever
	 * supports progress tracking (e.g. per line) or null if not.
	 * 
	 * @return The number of records to be read or null if this cannot be
	 *         determined.
	 * @throws ObservationReadError
	 *             If an error occurs while determining the number of records.
	 */
	public Integer getNumberOfRecords() throws ObservationReadError {
		return null;
	}

	/**
	 * Increment observation retrieval progress.
	 */
	public void incrementProgress() {
		Mediator.getInstance().getProgressNotifier()
				.notifyListeners(ProgressInfo.INCREMENT_PROGRESS);
	}

	/**
	 * Increment observation retrieval progress by the specified number of
	 * steps.
	 * 
	 * @param steps
	 *            The numnber of steps.
	 */
	public void incrementProgress(int steps) {
		Mediator.getInstance()
				.getProgressNotifier()
				.notifyListeners(
						new ProgressInfo(ProgressType.INCREMENT_PROGRESS, 2));
	}

	/**
	 * <p>
	 * Add an observation to the list of valid observations.
	 * </p>
	 * 
	 * <p>
	 * This is a convenience method that adds an observation to the list of
	 * valid observations and categorises it by band/series. This method is
	 * particularly suitable for observation source plugins since it asks
	 * whether an observation satisfies the requirement that it has at least JD
	 * and magnitude values.
	 * </p>
	 * 
	 * @param ob
	 *            The valid observation to be added to collections.
	 * 
	 * @throws ObservationReadError
	 *             if the observation has no date or magnitude. The caller can
	 *             either propagate this exception further or add to the invalid
	 *             observation list, or do whatever else it considers to be
	 *             appropriate.
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

		boolean include = true;

		// If a VeLa filter string is present, apply it to each observation.
		if (!NO_VELA_FILTER.equals(velaFilterStr)) {
			vela.pushEnvironment(new VeLaValidObservationEnvironment(ob));
			try {
				Optional<Operand> value = vela.program(velaFilterStr);
				if (value.isPresent()) {
					// There may be no value present because everything
					// is commented or because no expression has been
					// evaluated (e.g. one or more functions or variables
					// have been defined but no expression has been evaluated
					// that uses them). In this case, there's nothing to do.
					if (value.get().getType() == Type.BOOLEAN) {
						include = value.get().booleanVal();
					} else {
						if (!velaErrorReported) {
							MessageBox.showErrorDialog("Type Error",
									"Expected a Boolean value");
							velaErrorReported = true;
						}
					}
				}
			} catch (VeLaParseError e) {
				if (!velaErrorReported) {
					MessageBox.showErrorDialog("Parse Error",
							messageFromException(e));
					velaErrorReported = true;
				}
			} catch (VeLaEvalError e) {
				if (!velaErrorReported) {
					MessageBox.showErrorDialog("Evaluation Error",
							messageFromException(e));
					velaErrorReported = true;
				}
			} finally {
				vela.popEnvironment();
			}
		}

		if (include) {
			addValidObservation(ob);
			categoriseValidObservation(ob);
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

	/**
	 * Here we categorise a valid observation in terms of whether it is
	 * fainter-than, discrepant or excluded, belongs to a user-defined series,
	 * or to a particular band. If this observation retriever is reading
	 * helio/barycentric observations, we set the "JD flavour" on the observation
	 * as well. The observation is then inserted into a map of categories and
	 * the valid observation list.
	 * 
	 * @param validOb
	 *            A valid observation.
	 */
	private void categoriseValidObservation(ValidObservation validOb) {
		SeriesType category = null;

		if (validOb.getMagnitude().isFainterThan()) {
			category = SeriesType.FAINTER_THAN;
		} else if (validOb.isDiscrepant()) {
			category = SeriesType.DISCREPANT;
		} else if (validOb.isExcluded()) {
			category = SeriesType.Excluded;
		} else if (validOb.getBand() != validOb.getSeries()) {
			category = validOb.getSeries();
		} else {
			category = validOb.getBand();
		}

		if (validOb.getJDflavour() == JDflavour.UNKNOWN) {
			validOb.setJDflavour(getJDflavour());
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
	public void addValidObservation(ValidObservation ob) {
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
	 * satisfied.
	 * </p>
	 * 
	 * <p>
	 * An observation source plug-in developer could, if so desired, completely
	 * override this method if data is expected to be mostly out of order; in
	 * the worst case, if all elements are out of order, the cost will be O(n^2)
	 * due to the O(n) traversal being carried out n times for the number of
	 * observations inserted.
	 * </p>
	 * 
	 * @param ob
	 *            The observation to be inserted.
	 * @param obs
	 *            The list into which it is to be inserted.
	 */
	public void insertObservation(ValidObservation ob,
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
	 * Skip any bytes at the start of a line that have an ordinal value of less
	 * than zero, e.g. a byte-order mark sequence. This is likely to be an
	 * exceptional case so low cost when amortised over all lines.
	 * 
	 * @param line
	 *            The line to be processed.
	 * @return The line without characters whose ordinal values are negative.
	 */
	protected String removeNegativeBytes(String line) {
		byte[] bytes = line.getBytes();

		int i = 0;
		for (; i < bytes.length && bytes[i] < 0; i++)
			;
		if (i > 0) {
			line = new String(bytes, i, line.length() - 1);
		}

		return line;
	}

	/**
	 * Is the string empty?
	 * 
	 * @param str
	 *            The string in question.
	 * @return Whether or not it's empty.
	 */
	private boolean isEmpty(String str) {
		return str != null && "".equals(str.trim());
	}

	/**
	 * Given a throwable, return a message string.
	 * 
	 * @param t
	 *            The throwable object.
	 * @return The message.
	 */
	private String messageFromException(Throwable t) {
		String msg = t.getMessage();

		if (msg == null || isEmpty(msg)) {
			msg = t.toString();
		}

		return msg;
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
				return true;
			}
		};
	}
}