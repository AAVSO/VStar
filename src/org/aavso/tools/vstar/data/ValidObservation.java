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
package org.aavso.tools.vstar.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.aavso.tools.vstar.ui.mediator.AnalysisType;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.util.prefs.NumericPrecisionPrefs;

/**
 * <p>
 * This class corresponds to a single valid variable star observation. Depending
 * upon the source, some fields may be null. Some are not permitted to be null
 * however and these are documented below.
 * </p>
 * 
 * <p>
 * For reference, here are the fields in the order they appear in the AAVSO
 * download format:
 * </p>
 * 
 * <p>
 * JD(0), MAGNITUDE(1), UNCERTAINTY(2), HQ_UNCERTAINTY(3), BAND(4),
 * OBSERVER_CODE(5), COMMENT_CODE(6), COMP_STAR_1(7), COMP_STAR_2(8), CHARTS(9),
 * COMMENTS(10), TRANSFORMED(11), AIRMASS(12), VALFLAG(13), CMAG(14), KMAG(15),
 * HJD(16), NAME(17), AFFILIATION(18), MTYPE(19), GROUP(20), ADS_REFERENCE(21),
 * DIGITIZER(22), CREDIT(23)
 * </p>
 * 
 * <p>
 * The simple format file has these fields:
 * </p>
 * 
 * <p>
 * JD MAGNITUDE [UNCERTAINTY] [OBSERVER_CODE] [VALFLAG]
 * </p>
 * 
 * <p>
 * When VStar was first developed, observation source plugins were not
 * anticipated, but should have been. The additional details members permit
 * other typed information to be stored for an observation.
 * </p>
 */
public class ValidObservation extends Observation {

	public enum JDflavour {
		UNKNOWN("Time"), JD("JD"), HJD("HJD"), BJD("BJD");

		public final String label;

		private JDflavour(String label) {
			this.label = label;
		}
	}

	// Julian Day, calendar date, and cache.
	private DateInfo dateInfo = null;
	private final static WeakHashMap<DateInfo, DateInfo> dateInfoCache;
	static {
		dateInfoCache = new WeakHashMap<DateInfo, DateInfo>();
	}

	// Magnitude, uncertainty, fainter/brighter-than, and cache.
	private Magnitude magnitude = null;
	private final static WeakHashMap<Magnitude, Magnitude> magnitudeCache;
	static {
		magnitudeCache = new WeakHashMap<Magnitude, Magnitude>();
	}

	private Double hqUncertainty = null;
	private SeriesType band = null;
	private SeriesType series = null; // series and band may differ on copy

	// Comment codes and cache.
	private CommentCodes commentCode = null;
	private final static WeakHashMap<CommentCodes, CommentCodes> commentCodeCache;
	static {
		commentCodeCache = new WeakHashMap<CommentCodes, CommentCodes>();
	}

	private boolean transformed = false;
	private ValidationType validationType = null;

	// Heliocentric vs Geocentric Julian Date; uses dateInfo cache.
	private DateInfo hJD = null;

	private MTypeType mType = MTypeType.STD;

	private String obsType = ObsType.UNKNOWN.getDescription();

	// Phase values will be computed later, if a phase plot is requested.
	// They may change over the lifetime of a ValidObservation instance
	// since different epoch determination methods will result in different
	// phase values.
	private Double standardPhase = null;
	private Double previousCyclePhase = null;

	private boolean excluded = false;

	private JDflavour jdFlavour = JDflavour.UNKNOWN;

	// Optional string-based observation details.
	private Map<String, Property> details;

	// Optional observation detail titles, and shadow save collection.
	private static Map<String, String> detailTitles = new HashMap<String, String>();
	private static Map<String, String> savedDetailTitles = null;

	// Optional observation detail types, and shadow save collection.
	// @deprecated
	private static Map<String, Class<?>> detailTypes = new HashMap<String, Class<?>>();
	private static Map<String, Class<?>> savedDetailTypes = null;

	// Ordering of keys via an index of insertion to titles table.
	private static int detailIndex = 0;
	private static int savedDetailIndex = 0;
	private static Map<Integer, String> indexToDetailKey = new HashMap<Integer, String>();
	private static Map<Integer, String> savedIndexToDetailKey = null;
	private static Map<String, Integer> detailKeyToIndex = new HashMap<String, Integer>();
	private static Map<String, Integer> savedDetailKeyToIndex = null;

	private final static String nameKey = "NAME";
	private final static String nameTitle = "Name";

	private final static String obsCodeKey = "OBS_CODE";
	private final static String obsCodeTitle = "Observer Code";

	private final static String compStar1Key = "COMP_STAR1";
	private final static String compStar1Title = "Comparison Star 1";

	private final static String compStar2Key = "COMP_STAR2";
	private final static String compStar2Title = "Comparison Star 2";

	private final static String chartsKey = "CHARTS";
	private final static String chartsTitle = "Charts";

	private final static String commentsKey = "COMMENTS";
	private final static String commentsTitle = "Comments";

	private final static String airmassKey = "AIRMASS";
	private final static String airmassTitle = "Airmass";

	private final static String cMagKey = "CMAG";
	private final static String cMagTitle = "CMag";

	private final static String kMagKey = "KMAG";
	private final static String kMagTitle = "KMag";

	private final static String affiliationKey = "AFFILIATION";
	private final static String affiliationTitle = "Affiliation";

	private final static String groupKey = "GROUP";
	private final static String groupTitle = "Group";

	private final static String pubrefKey = "PUBREF";
	private final static String pubrefTitle = "ADS Reference";

	private final static String digitizerKey = "DIGTIZER";
	private final static String digitizerTitle = "Digitizer";

	private final static String creditKey = "CREDIT";
	private final static String creditTitle = "Credit";

	// The set of standard detail keys.
	private final static Set<String> standardDetailKeys;

	static {
		standardDetailKeys = new HashSet<String>();
		standardDetailKeys.add(nameKey);
		standardDetailKeys.add(obsCodeKey);
		standardDetailKeys.add(compStar1Key);
		standardDetailKeys.add(compStar2Key);
		standardDetailKeys.add(chartsKey);
		standardDetailKeys.add(commentsKey);
		standardDetailKeys.add(airmassKey);
		standardDetailKeys.add(cMagKey);
		standardDetailKeys.add(kMagKey);
		standardDetailKeys.add(affiliationKey);
		standardDetailKeys.add(groupKey);
		standardDetailKeys.add(pubrefKey);
		standardDetailKeys.add(digitizerKey);
		standardDetailKeys.add(creditKey);
	}

	// A cache of detail values.
	private static final WeakHashMap<Property, Property> detailValueCache;

	static {
		detailValueCache = new WeakHashMap<Property, Property>();
	}

	/**
	 * Constructor.
	 * 
	 * All fields start out as null or false.
	 */
	public ValidObservation() {
		super(0);
		details = new HashMap<String, Property>();
	}

	/**
	 * Creates and returns a new ValidObservation instance that is a copy of the
	 * current instance.<br/>
	 * See https://github.com/AAVSO/VStar/issues/51
	 * 
	 * @param type the series with which the copied observation will be associated
	 *             (may be null)
	 * @return the new observation instance
	 */
	public ValidObservation copy(SeriesType series) {
		ValidObservation ob = new ValidObservation();

		ob.setJD(this.getJD());
		ob.setJDflavour(this.getJDflavour());
		ob.setMagnitude(this.getMagnitude().copy());
		ob.setHqUncertainty(this.getHqUncertainty());
		ob.setBand(this.getBand());
		if (series != null) ob.setSeries(series);
		ob.setCommentCode(this.getCommentCode());
		ob.setTransformed(this.isTransformed());
		ob.setValidationType(this.getValidationType());
		ob.setHJD(this.getHJD());
		ob.setMType(this.getMType());
		ob.setObsType(this.getObsType());
		ob.setStandardPhase(this.getStandardPhase());
		ob.setPreviousCyclePhase(this.getPreviousCyclePhase());
		ob.setExcluded(this.isExcluded());
		ob.details = new HashMap<String, Property>(this.details);

		return ob;
	}

	/**
	 * Creates and returns a new ValidObservation instance that is a copy of the
	 * current instance that is not associated with a different series.<br/>
	 * 
	 * @return the new observation instance
	 */
	public ValidObservation copy() {
		return copy(null);
	}

	/**
	 * Reset static non-cache maps and detail index in readiness for a new dataset.
	 */
	public static void reset() {
		if (detailTitles != null) {
			savedDetailTitles = new HashMap<String, String>(detailTitles);
			detailTitles.clear();
		}
		
		if (detailTypes != null) {
			savedDetailTypes = new HashMap<String, Class<?>>(detailTypes);
			detailTypes.clear();
		}

		if (indexToDetailKey != null) {
			savedIndexToDetailKey = new HashMap<Integer, String>(indexToDetailKey);
			indexToDetailKey.clear();
		}

		if (detailKeyToIndex != null) {
			savedDetailKeyToIndex = new HashMap<String, Integer>(detailKeyToIndex);
			detailKeyToIndex.clear();
		}

		savedDetailIndex = detailIndex;
		detailIndex = 0;
	}

	/**
	 * Restore static non-cache maps and detail index when a dataset load failure
	 * occurs.
	 */
	public static void restore() {
		// Don't restore to null values, e.g. in the case of a first observation
		// load failure, the saved map values may still be at their default of
		// null.

		if (savedDetailTitles != null) {
			detailTitles = savedDetailTitles;
		}

		if (savedDetailTypes != null) {
			detailTypes = savedDetailTypes;
		}

		if (savedIndexToDetailKey != null) {
			indexToDetailKey = savedIndexToDetailKey;
		}

		if (savedDetailKeyToIndex != null) {
			detailKeyToIndex = savedDetailKeyToIndex;
		}

		detailIndex = savedDetailIndex;
	}

	// Getters and Setters

	/**
	 * Generic cached value getter.
	 * 
	 * @param <T>   The type of the cached value.
	 * @param cache The cache in which to look for the value.
	 * @param value The value to look up.
	 * @return The present or future cached value.
	 */
	private static <T> T getCachedValue(WeakHashMap<T, T> cache, T value) {
		if (cache.containsKey(value)) {
			value = cache.get(value);
		} else {
			cache.put(value, value);
		}

		return value;
	}

	/**
	 * @return details map
	 */
	public Map<String, Property> getDetails() {
		return details;
	}

	/**
	 * @return the detailTitles
	 */
	public static Map<String, String> getDetailTitles() {
		return detailTitles;
	}

	/**
	 * @return the detail types
	 */
	public static Map<String, Class<?>> getDetailTypes() {
		return detailTypes;
	}

	/**
	 * Return the detail key given the detail ordering index.
	 * 
	 * @param the detail index
	 * @return the detail key
	 */
	public static String getDetailKey(int index) {
		return indexToDetailKey.get(index);
	}

	/**
	 * Return the detail index given the key.
	 * 
	 * @param the detail key
	 * @return the detail index
	 */
	public static int getDetailIndex(String key) {
		return detailKeyToIndex.get(key);
	}

	/**
	 * Add an observation detail, if the value is not null.
	 * 
	 * @param key   The detail key.
	 * @param value The detail property value.
	 * @param title The detail title, e.g. for use in table column, observation
	 *              details.
	 */
	public void addDetail(String key, Property value, String title) {
		if (key != null && value != null) {
			value = getCachedValue(detailValueCache, value);
			details.put(key, value);
			if (!detailTitles.containsKey(key)) {
				detailTitles.put(key, title);
				detailTypes.put(key, value.getClazz());
				indexToDetailKey.put(detailIndex, key);
				detailKeyToIndex.put(key, detailIndex);
				detailIndex++;
			}
		}
	}

	/**
	 * Add an observation detail, whose value is of type integer.
	 * 
	 * @param key   The detail key.
	 * @param value The detail integer value.
	 * @param title The detail title, e.g. for use in table column, observation
	 *              details.
	 */
	public void addDetail(String key, Integer value, String title) {
		addDetail(key, new Property(value), title);
	}

	/**
	 * Add an observation detail, whose value is of type real.
	 * 
	 * @param key   The detail key.
	 * @param value The detail real value.
	 * @param title The detail title, e.g. for use in table column, observation
	 *              details.
	 */
	public void addDetail(String key, Double value, String title) {
		addDetail(key, new Property(value), title);
	}

	/**
	 * Add an observation detail, whose value is of type Boolean.
	 * 
	 * @param key   The detail key.
	 * @param value The detail Boolean value.
	 * @param title The detail title, e.g. for use in table column, observation
	 *              details.
	 */
	public void addDetail(String key, Boolean value, String title) {
		addDetail(key, new Property(value), title);
	}

	/**
	 * Add an observation detail, whose value is of type string.
	 * 
	 * @param key   The detail key.
	 * @param value The detail string value.
	 * @param title The detail title, e.g. for use in table column, observation
	 *              details.
	 */
	public void addDetail(String key, String value, String title) {
		addDetail(key, new Property(value), title);
	}

	/**
	 * @return details map value given a key, if it exists, otherwise the empty
	 *         string.
	 */
	public Property getDetail(String key) {
		return detailExists(key) ? details.get(key) : Property.NO_VALUE;
	}

	/**
	 * @return detail titles map value given a key
	 */
//	public String getDetailTitle(String key) {
//		return detailTitles.get(key);
//	}

	/**
	 * @return detail types map value given a key
	 */
//	public Class<?> getDetailType(String key) {
//		return detailTypes.get(key);
//	}

	/**
	 * Does the specified detail key exist?
	 * 
	 * @param key The detail key.
	 * @return Whether or not detail exists.
	 */
	public boolean detailExists(String key) {
		return details.keySet().contains(key);
	}

	/**
	 * Does the specified detail key exist and is it non-empty?
	 * 
	 * @param key The detail key.
	 * @return Whether or not non-empty detail exists.
	 */
	public boolean nonEmptyDetailExists(String key) {
		return detailExists(key) && !isEmpty(key);
	}

	/**
	 * Does the specified detail title key exist?
	 * 
	 * @param key The detail key.
	 * @return Whether or not the detail title exists.
	 */
	public boolean detailTitleExists(String key) {
		return detailTitles.keySet().contains(key);
	}

	/**
	 * Does the specified detail key correspond to a standard detail key?
	 * 
	 * @param key The detail key.
	 * @return Whether or not the detail key is standard.
	 */
	public boolean isStandardDetailKey(String key) {
		return standardDetailKeys.contains(key);
	}

	/**
	 * @return the standarddetailkeys
	 */
	public static Set<String> getStandardDetailKeys() {
		return standardDetailKeys;
	}

	/**
	 * @return the dateInfo
	 */
	public DateInfo getDateInfo() {
		return dateInfo;
	}

	/**
	 * @param dateInfo the dateInfo to set
	 */
	public void setDateInfo(DateInfo dateInfo) {
		this.dateInfo = getCachedValue(dateInfoCache, dateInfo);
	}

	/**
	 * @return the magnitude
	 */
	public Magnitude getMagnitude() {
		return magnitude;
	}

	/**
	 * @param magnitude the magnitude to set
	 */
	public void setMagnitude(Magnitude magnitude) {
//		this.magnitude = getCachedValue(magnitudeCache, magnitude);
		this.magnitude = magnitude;
	}

	/**
	 * @param mag the magnitude component to set.
	 */
	public void setMag(double mag) {
//		setMagnitude(new Magnitude(mag, magnitude.getUncertainty()));
		this.magnitude.setMagValue(mag);
	}

	/**
	 * @return the obsCode
	 */
	public String getObsCode() {
		return getDetail(obsCodeKey).getStrVal();
	}

	/**
	 * @param obsCode the obsCode to set
	 */
	public void setObsCode(String obsCode) {
		addDetail(obsCodeKey, new Property(obsCode), obsCodeTitle);
	}

	/**
	 * @return whether this observation is discrepant
	 */
	public boolean isDiscrepant() {
		return ValidationType.DISCREPANT.equals(validationType);
	}

	/**
	 * @param discrepant the discrepant to set
	 */
	public void setDiscrepant(boolean discrepant) {
		// TODO: Should we keep a record of the last known value of
		// this field before it was marked as discrepant? Right now,
		// we are going from {G,D,P} -> D -> G -> D -> G ... so we are
		// potentially losing information. This is a good candidate
		// for undoable edits.
		this.validationType = discrepant ? ValidationType.DISCREPANT : ValidationType.GOOD;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return getDetail(nameKey).getStrVal();
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		addDetail(nameKey, new Property(name), nameTitle);
	}

	/**
	 * @return the validationType
	 */
	public ValidationType getValidationType() {
		return validationType;
	}

	/**
	 * @param validationType the validationType to set
	 */
	public void setValidationType(ValidationType validationType) {
		this.validationType = validationType;
	}

	/**
	 * @return the hqUncertainty
	 */
	public Double getHqUncertainty() {
		return hqUncertainty;
	}

	/**
	 * @param hqUncertainty the hqUncertainty to set
	 */
	public void setHqUncertainty(Double hqUncertainty) {
		this.hqUncertainty = hqUncertainty;
	}

	/**
	 * @return the band
	 */
	public SeriesType getBand() {
		return band;
	}

	/**
	 * @param band the band to set
	 */
	public void setBand(SeriesType band) {
		this.band = band;
		setSeries(band);
	}

	/**
	 * @return the series with which this observation is associated
	 */
	public SeriesType getSeries() {
		return series;
	}

	/**
	 * @param series the series with which this observation is associated
	 */
	public void setSeries(SeriesType series) {
		this.series = series;
	}

	/**
	 * @return the commentCode
	 */
	public CommentCodes getCommentCode() {
		return commentCode;
	}

	/**
	 * @param commentCodeStr the comment code string to set
	 */
	public void setCommentCode(String commentCodeStr) {
		this.commentCode = getCachedValue(commentCodeCache, new CommentCodes(commentCodeStr));
	}

	/**
	 * @param commentCodes the comment codes to set
	 */
	public void setCommentCode(CommentCodes commentCodes) {
		this.commentCode = getCachedValue(commentCodeCache, commentCodes);
	}

	/**
	 * @return the compStar1
	 */
	public String getCompStar1() {
		return getDetail(compStar1Key).getStrVal();
	}

	/**
	 * @param compStar1 the compStar1 to set
	 */
	public void setCompStar1(String compStar1) {
		addDetail(compStar1Key, new Property(compStar1), compStar1Title);
	}

	/**
	 * @return the compStar2
	 */
	public String getCompStar2() {
		return getDetail(compStar2Key).getStrVal();
	}

	/**
	 * @param compStar2 the compStar2 to set
	 */
	public void setCompStar2(String compStar2) {
		addDetail(compStar2Key, new Property(compStar2), compStar2Title);
	}

	/**
	 * @return the charts
	 */
	public String getCharts() {
		return getDetail(chartsKey).getStrVal();
	}

	/**
	 * @param charts the charts to set
	 */
	public void setCharts(String charts) {
		addDetail(chartsKey, new Property(charts), chartsTitle);
	}

	/**
	 * @return the comments
	 */
	public String getComments() {
		return getDetail(commentsKey).getStrVal();
	}

	/**
	 * @param comments the comments to set
	 */
	public void setComments(String comments) {
		addDetail(commentsKey, new Property(comments), commentsTitle);
	}

	/**
	 * @return the transformed
	 */
	public boolean isTransformed() {
		return transformed;
	}

	/**
	 * @param transformed the transformed to set
	 */
	public void setTransformed(boolean transformed) {
		this.transformed = transformed;
	}

	/**
	 * @return the airmass
	 */
	public String getAirmass() {
		return getDetail(airmassKey).getStrVal();
	}

	/**
	 * @param airmass the airmass to set
	 */
	public void setAirmass(String airmass) {
		addDetail(airmassKey, new Property(airmass), airmassTitle);
	}

	/**
	 * @return the cMag
	 */
	public String getCMag() {
		return getDetail(cMagKey).getStrVal();
	}

	/**
	 * @param cMag the cMag to set
	 */
	public void setCMag(String cMag) {
		if ("0.ensemb".equals(cMag)) {
			cMag = "Ensemble";
		}
		addDetail(cMagKey, new Property(cMag), cMagTitle);
	}

	/**
	 * @return the kMag
	 */
	public String getKMag() {
		return getDetail(kMagKey).getStrVal();
	}

	/**
	 * @param kMag the kMag to set
	 */
	public void setKMag(String kMag) {
		addDetail(kMagKey, new Property(kMag), kMagTitle);
	}

	/**
	 * @return the hJD
	 */
	public DateInfo getHJD() {
		return hJD;
	}

	/**
	 * @param hJD the hJD to set
	 */
	public void setHJD(DateInfo hJD) {
		this.hJD = getCachedValue(dateInfoCache, hJD);
	}

	/**
	 * @return the affiliation
	 */
	public String getAffiliation() {
		return getDetail(affiliationKey).getStrVal();
	}

	/**
	 * @param affiliation the affiliation to set
	 */
	public void setAffiliation(String affiliation) {
		addDetail(affiliationKey, new Property(affiliation), affiliationTitle);
	}

	/**
	 * @return the mType
	 */
	public MTypeType getMType() {
		return mType;
	}

	/**
	 * @param mType the mType to set
	 */
	public void setMType(MTypeType mType) {
		this.mType = mType;
	}

	/**
	 * @return the group
	 */
	public String getGroup() {
		return getDetail(groupKey).getStrVal();
	}

	/**
	 * @param group the group of filters used for this observation
	 */
	public void setGroup(String group) {
		addDetail(groupKey, new Property(group), groupTitle);
	}

	/**
	 * @return the obsType
	 */
	public String getObsType() {
		return obsType;
	}

	/**
	 * @param obsType the obsType to set
	 */
	public void setObsType(String obsType) {
		this.obsType = obsType;
	}

	/**
	 * @return the ADS Reference
	 */
	public String getADSRef() {
		return getDetail(pubrefKey).getStrVal();
	}

	/**
	 * @param ADS Reference the ADS Reference to set
	 */
	public void setADSRef(String adsRef) {
		addDetail(pubrefKey, new Property(adsRef), pubrefTitle);
	}

	/**
	 * @return the digitizer
	 */
	public String getDigitizer() {
		return getDetail(digitizerKey).getStrVal();
	}

	/**
	 * @param digitizer the digitizer to set
	 */
	public void setDigitizer(String digitizer) {
		addDetail(digitizerKey, new Property(digitizer), digitizerTitle);
	}

	/**
	 * @return the credit
	 */
	public String getCredit() {
		return getDetail(creditKey).getStrVal();
	}

	/**
	 * @param credit the organization to be credited for the observation
	 */
	public void setCredit(String credit) {
		addDetail(creditKey, new Property(credit), creditTitle);
	}

	/**
	 * @return the standardPhase
	 */
	public Double getStandardPhase() {
		return standardPhase;
	}

	/**
	 * @param standardPhase the standardPhase to set
	 */
	public void setStandardPhase(Double standardPhase) {
		this.standardPhase = standardPhase;
	}

	/**
	 * @return the previousCyclePhase
	 */
	public Double getPreviousCyclePhase() {
		return previousCyclePhase;
	}

	/**
	 * @param previousCyclePhase the previousCyclePhase to set
	 */
	public void setPreviousCyclePhase(Double previousCyclePhase) {
		this.previousCyclePhase = previousCyclePhase;
	}

	/**
	 * @return the excluded
	 */
	public boolean isExcluded() {
		return excluded;
	}

	/**
	 * @param excluded the excluded to set
	 */
	public void setExcluded(boolean excluded) {
		this.excluded = excluded;
	}

	/**
	 * @return true if Heliocentric
	 */
	public boolean isHeliocentric() {
		return jdFlavour == JDflavour.HJD;
	}

	/**
	 * @return true if Barycentric
	 */
	public boolean isBarycentric() {
		return jdFlavour == JDflavour.BJD;
	}

	public JDflavour getJDflavour() {
		return jdFlavour;
	}

	public void setJDflavour(JDflavour jdFlavour) {
		this.jdFlavour = jdFlavour;
	}

	public String getTimeUnits() {
		return jdFlavour.label;
	}

	// Output formatting methods.

	public String toString() {
		StringBuffer strBuf = new StringBuffer();

		if (nonEmptyDetailExists(nameKey)) {
			strBuf.append(details.get(nameKey));
			strBuf.append("\n");
		}

		if (dateInfo != null) {
			strBuf.append(getTimeUnits());
			strBuf.append(": ");
			strBuf.append(NumericPrecisionPrefs.formatTime(dateInfo.getJulianDay()));
			strBuf.append("\n");

			strBuf.append("Calendar Date: ");
			strBuf.append(dateInfo.getCalendarDate());
			strBuf.append("\n");
		}

		// If we are not in phase plot mode, we should not represent ourselves
		// as having a phase.
		if (Mediator.getInstance().getAnalysisType() == AnalysisType.PHASE_PLOT) {
			if (standardPhase != null) {
				strBuf.append("Standard Phase: ");
				strBuf.append(NumericPrecisionPrefs.formatTime(standardPhase));
				strBuf.append("\n");
			}

			if (previousCyclePhase != null) {
				strBuf.append("Previous Cycle Phase: ");
				strBuf.append(NumericPrecisionPrefs.formatTime(previousCyclePhase));
				strBuf.append("\n");
			}
		}

		strBuf.append("Magnitude: ");
		strBuf.append(magnitude.toString());
		strBuf.append("\n");

		if (hqUncertainty != null) {
			strBuf.append("HQ Uncertainty: ");
			strBuf.append(NumericPrecisionPrefs.formatMag(hqUncertainty));
			strBuf.append("\n");
		}

		if (validationType != null) {
			strBuf.append("Validation: ");
			strBuf.append(validationType.toString());
			strBuf.append("\n");
		}

		if (obsType != null) {
			strBuf.append("Observation Type: ");
			strBuf.append(obsType);
			strBuf.append("\n");
		}

		if (band != null) {
			strBuf.append("Band: ");
			strBuf.append(band.getDescription());
			strBuf.append("\n");
		}

		if (nonEmptyDetailExists(obsCodeKey)) {
			strBuf.append(detailTitles.get(obsCodeKey) + ": ");
			strBuf.append(details.get(obsCodeKey));
			strBuf.append("\n");
		}

		if (commentCode != null) {
			String str = getCommentCode().getOrigString();
			if (str.trim().length() != 0) {
				strBuf.append("Comment Codes:\n");
				strBuf.append("[");
				strBuf.append(str);
				strBuf.append("]\n");
				strBuf.append(commentCode.toString());
			}
		}

		if (nonEmptyDetailExists(compStar1Key)) {
			strBuf.append(detailTitles.get(compStar1Key) + ": ");
			strBuf.append(details.get(compStar1Key));
			strBuf.append("\n");
		}

		if (nonEmptyDetailExists(compStar2Key)) {
			strBuf.append(detailTitles.get(compStar2Key) + ": ");
			strBuf.append(details.get(compStar2Key));
			strBuf.append("\n");
		}

		if (nonEmptyDetailExists(chartsKey)) {
			strBuf.append(detailTitles.get(chartsKey) + ": ");
			strBuf.append(details.get(chartsKey));
			strBuf.append("\n");
		}

		if (nonEmptyDetailExists(commentsKey)) {
			strBuf.append(detailTitles.get(commentsKey) + ": ");
			strBuf.append(details.get(commentsKey));
			strBuf.append("\n");
		}

		if (transformed) {
			strBuf.append("Transformed: yes\n");
		}

		if (nonEmptyDetailExists(airmassKey)) {
			strBuf.append(detailTitles.get(airmassKey) + ": ");
			strBuf.append(details.get(airmassKey));
			strBuf.append("\n");
		}

		if (nonEmptyDetailExists(cMagKey)) {
			strBuf.append(detailTitles.get(cMagKey) + ": ");
			strBuf.append(details.get(cMagKey));
			strBuf.append("\n");
		}

		if (nonEmptyDetailExists(kMagKey)) {
			strBuf.append(detailTitles.get(kMagKey) + ": ");
			strBuf.append(details.get(kMagKey));
			strBuf.append("\n");
		}

		if (hJD != null) {
			strBuf.append("Heliocentric Julian Day: ");
			strBuf.append(NumericPrecisionPrefs.formatTime(hJD.getJulianDay()));
			strBuf.append("\n");
		}

		if (nonEmptyDetailExists(groupKey)) {
			strBuf.append(detailTitles.get(groupKey) + ": ");
			strBuf.append(details.get(groupKey));
			strBuf.append("\n");
		}

		if (nonEmptyDetailExists(pubrefKey)) {
			strBuf.append(detailTitles.get(pubrefKey) + ": ");
			strBuf.append(details.get(pubrefKey));
			strBuf.append("\n");
		}

		if (nonEmptyDetailExists(digitizerKey)) {
			strBuf.append(detailTitles.get(digitizerKey) + ": ");
			strBuf.append(details.get(digitizerKey));
			strBuf.append("\n");
		}

		if (nonEmptyDetailExists(creditKey)) {
			strBuf.append(detailTitles.get(creditKey) + ": ");
			strBuf.append(details.get(creditKey));
			strBuf.append("\n");
		}

		// Add any remaining non-AAVSO details, e.g. for a plugin.
		for (String key : details.keySet()) {
			if (!standardDetailKeys.contains(key)) {
				strBuf.append(detailTitles.get(key) + ": ");
				strBuf.append(details.get(key));
				strBuf.append("\n");
			}
		}

		return strBuf.toString();
	}

	/**
	 * Returns a line in TSV format of the following fields (bracketed fields are
	 * optional):
	 * 
	 * [Phase,]JD,MAGNITUDE,[UNCERTAINTY],[OBSERVER_CODE],[VALFLAG]
	 * 
	 * @param delimiter The field delimiter to use.
	 */
	public String toSimpleFormatString(String delimiter) {
		return toSimpleFormatString(delimiter, true);
	}

	/**
	 * Returns a line in TSV format of the following fields (bracketed fields are
	 * optional):
	 * 
	 * [Phase,][JD,]MAGNITUDE,[UNCERTAINTY],[OBSERVER_CODE],[VALFLAG]
	 * 
	 * @param delimiter The field delimiter to use.
	 * @param includeJD Should the JD be included in the output?
	 */
	public String toSimpleFormatString(String delimiter, boolean includeJD) {
		StringBuffer buf = new StringBuffer();

		if (Mediator.getInstance().getAnalysisType() == AnalysisType.PHASE_PLOT) {
			buf.append(this.getStandardPhase());
			buf.append(delimiter);
		}

		if (includeJD && this.getDateInfo() != null) {
			buf.append(this.getDateInfo().getJulianDay());
			buf.append(delimiter);
		}

		buf.append(this.getMagnitude().isFainterThan() ? "<" : "");
		buf.append(this.getMagnitude().getMagValue());
		buf.append(delimiter);

		double uncertainty = this.getMagnitude().getUncertainty();
		// TODO: why != here and > in next method?
		if (uncertainty != 0.0) {
			buf.append(uncertainty);
		}
		buf.append(delimiter);

		if (details.keySet().contains(obsCodeKey)) {
			buf.append(details.get(obsCodeKey));
		}
		buf.append(delimiter);

		if (this.validationType != null) {
			buf.append(this.validationType.getValflag());
		}
		buf.append("\n");

		return buf.toString();
	}

	/**
	 * Returns a line in delimiter-separator (TSV, CSV, ...) AAVSO download format.
	 * 
	 * @param delimiter The field delimiter to use.
	 */
	public String toAAVSOFormatString(String delimiter) {
		return toAAVSOFormatString(delimiter, true);
	}

	/**
	 * Returns a line in delimiter-separator (TSV, CSV, ...) AAVSO download format.
	 * 
	 * @param delimiter The field delimiter to use.
	 * @param includeJD Should the JD be included in the output?
	 */
	public String toAAVSOFormatString(String delimiter, boolean includeJD) {
		StringBuffer buf = new StringBuffer();

		if (Mediator.getInstance().getAnalysisType() == AnalysisType.PHASE_PLOT) {
			buf.append(this.getStandardPhase());
			buf.append(delimiter);
		}

		if (includeJD && this.getDateInfo() != null) {
			buf.append(this.getDateInfo().getJulianDay());
			buf.append(delimiter);
		}

		buf.append(this.getMagnitude().isFainterThan() ? "<" : "");
		buf.append(this.getMagnitude().getMagValue());
		buf.append(delimiter);

		double uncertainty = this.getMagnitude().getUncertainty();
		if (uncertainty > 0.0) {
			buf.append(uncertainty);
		}
		buf.append(delimiter);

		if (this.getHqUncertainty() != null) {
			double hqUncertainty = this.getHqUncertainty();
			if (hqUncertainty > 0.0) {
				buf.append(hqUncertainty);
			}
		}
		buf.append(delimiter);

		buf.append(this.getBand().getShortName());
		buf.append(delimiter);

		if (nonEmptyDetailExists(obsCodeKey)) {
			buf.append(details.get(obsCodeKey));
		}
		buf.append(delimiter);

		if (this.getCommentCode() != null) {
			buf.append(this.getCommentCode().getOrigString());
		}
		buf.append(delimiter);

		if (this.getCompStar1() != null) {
			buf.append(this.getCompStar1());
		}
		buf.append(delimiter);

		if (this.getCompStar2() != null) {
			buf.append(this.getCompStar2());
		}
		buf.append(delimiter);

		if (this.getCharts() != null) {
			buf.append(this.getCharts());
		}
		buf.append(delimiter);

		if (this.getComments() != null) {
			buf.append(quoteForCSV(this.getComments()));
		}
		buf.append(delimiter);

		buf.append(this.isTransformed() ? "Yes" : "No");
		buf.append(delimiter);

		if (this.getAirmass() != null) {
			buf.append(this.getAirmass());
		}
		buf.append(delimiter);

		if (this.validationType != null) {
			buf.append(this.validationType.getValflag());
		}
		buf.append(delimiter);

		if (this.getCMag() != null) {
			buf.append(this.getCMag());
		}
		buf.append(delimiter);

		if (this.getKMag() != null) {
			buf.append(this.getKMag());
		}
		buf.append(delimiter);

		if (this.getHJD() != null) {
			buf.append(hJD.getJulianDay());
		}
		buf.append(delimiter);

		buf.append(!isEmpty(this.getName()) ? this.getName() : "Unknown");
		buf.append(delimiter);

		// Affiliation
		if (getAffiliation() != null) {
			buf.append(getAffiliation());
		}
		buf.append(delimiter);

		buf.append(this.getMType() != null ? this.getMType().getShortName() : MTypeType.STD.getShortName());
		buf.append(delimiter);

		// Group
		if (getGroup() != null) {
			buf.append(getGroup());
		}
		buf.append(delimiter);

		// ADS Reference
		if (getADSRef() != null) {
			buf.append(getADSRef());
		}
		buf.append(delimiter);

		// Digitizer
		if (getDigitizer() != null) {
			buf.append(getDigitizer());
		}
		buf.append(delimiter);

		// Credit
		if (getCredit() != null) {
			buf.append(getCredit());
		}
		buf.append(delimiter);

		// ObsType
		// TODO: in AID but not yet AAVSO download format!
		// buf.append(delimiter);

		// TODO: handle reading in aavso text and aid obs readers

		buf.append("\n");

		return buf.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((band == null) ? 0 : band.hashCode());
		result = prime * result + ((commentCode == null) ? 0 : commentCode.hashCode());
		result = prime * result + ((dateInfo == null) ? 0 : dateInfo.hashCode());
		result = prime * result + ((details == null) ? 0 : details.hashCode());
		result = prime * result + (excluded ? 1231 : 1237);
		result = prime * result + ((hJD == null) ? 0 : hJD.hashCode());
		result = prime * result + ((hqUncertainty == null) ? 0 : hqUncertainty.hashCode());
		result = prime * result + ((jdFlavour == null) ? 0 : jdFlavour.hashCode());
		result = prime * result + ((mType == null) ? 0 : mType.hashCode());
		result = prime * result + ((magnitude == null) ? 0 : magnitude.hashCode());
		result = prime * result + ((obsType == null) ? 0 : obsType.hashCode());
		result = prime * result + ((previousCyclePhase == null) ? 0 : previousCyclePhase.hashCode());
		result = prime * result + ((series == null) ? 0 : series.hashCode());
		result = prime * result + ((standardPhase == null) ? 0 : standardPhase.hashCode());
		result = prime * result + (transformed ? 1231 : 1237);
		result = prime * result + ((validationType == null) ? 0 : validationType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ValidObservation other = (ValidObservation) obj;
		if (band == null) {
			if (other.band != null)
				return false;
		} else if (!band.equals(other.band))
			return false;
		if (commentCode == null) {
			if (other.commentCode != null)
				return false;
		} else if (!commentCode.equals(other.commentCode))
			return false;
		if (dateInfo == null) {
			if (other.dateInfo != null)
				return false;
		} else if (!dateInfo.equals(other.dateInfo))
			return false;
		if (details == null) {
			if (other.details != null)
				return false;
		} else if (!details.equals(other.details))
			return false;
		if (excluded != other.excluded)
			return false;
		if (hJD == null) {
			if (other.hJD != null)
				return false;
		} else if (!hJD.equals(other.hJD))
			return false;
		if (hqUncertainty == null) {
			if (other.hqUncertainty != null)
				return false;
		} else if (!hqUncertainty.equals(other.hqUncertainty))
			return false;
		if (jdFlavour != other.jdFlavour)
			return false;
		if (mType != other.mType)
			return false;
		if (magnitude == null) {
			if (other.magnitude != null)
				return false;
		} else if (!magnitude.equals(other.magnitude))
			return false;
		if (obsType == null) {
			if (other.obsType != null)
				return false;
		} else if (!obsType.equals(other.obsType))
			return false;
		if (previousCyclePhase == null) {
			if (other.previousCyclePhase != null)
				return false;
		} else if (!previousCyclePhase.equals(other.previousCyclePhase))
			return false;
		if (series == null) {
			if (other.series != null)
				return false;
		} else if (!series.equals(other.series))
			return false;
		if (standardPhase == null) {
			if (other.standardPhase != null)
				return false;
		} else if (!standardPhase.equals(other.standardPhase))
			return false;
		if (transformed != other.transformed)
			return false;
		if (validationType != other.validationType)
			return false;
		return true;
	}

	// Convenience methods.

	public double getJD() {
		return this.dateInfo.getJulianDay();
	}

	public void setJD(double jd) {
		setDateInfo(new DateInfo(jd));
	}

	public double getMag() {
		return this.magnitude.getMagValue();
	}

	// Helpers

	private boolean isEmpty(String s) {
		return s == null || s.trim().length() == 0;
	}

	/**
	 * Precede any double quote in the argument with another and
	 * wrap the whole argument in double quotes.
	 * 
	 * @param field The field to be quoted
	 * @return The quoted field
	 */
	private String quoteForCSV(String field) {
		field = field.replace("\"", "\"\"");
		return "\"" + field + "\"";
	}
}
