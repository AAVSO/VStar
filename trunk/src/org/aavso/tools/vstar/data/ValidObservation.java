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
 * HJD(16), NAME(17), AFFILIATION(18), MTYPE(19), GROUP(20)
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
 * other string-based information to be stored for an observation.
 * </p>
 */
public class ValidObservation extends Observation {

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

	// Phase values will be computed later, if a phase plot is requested.
	// They may change over the lifetime of a ValidObservation instance
	// since different epoch determination methods will result in different
	// phase values.
	private Double standardPhase = null;
	private Double previousCyclePhase = null;

	private boolean excluded = false;

	// Optional string-based observation details.
	private Map<String, String> details;

	// Optional string-based observation detail titles, and shadow save
	// collection.
	private static Map<String, String> detailTitles = new HashMap<String, String>();
	private static Map<String, String> savedDetailTitles = null;

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
	}

	// A cache of detail values.
	private static final WeakHashMap<String, String> detailValueCache;

	static {
		detailValueCache = new WeakHashMap<String, String>();
	}

	/**
	 * Constructor.
	 * 
	 * All fields start out as null or false.
	 */
	public ValidObservation() {
		super(0);
		details = new HashMap<String, String>();
	}

	/**
	 * Reset static non-cache maps and detail index in readiness for a new dataset.
	 */
	public static void reset() {
		savedDetailTitles = new HashMap<String, String>(detailTitles);
		detailTitles.clear();
		
		savedIndexToDetailKey = new HashMap<Integer, String>(indexToDetailKey);
		indexToDetailKey.clear();
		
		savedDetailKeyToIndex = new HashMap<String, Integer>(detailKeyToIndex);
		detailKeyToIndex.clear();
		
		savedDetailIndex = detailIndex;
		detailIndex = 0;
	}

	/**
	 * Restore static non-cache maps and detail index when a dataset load failure occurs.
	 */
	public static void restore() {
		detailTitles = savedDetailTitles;
		indexToDetailKey = savedIndexToDetailKey;
		detailKeyToIndex = savedDetailKeyToIndex;
		detailIndex = savedDetailIndex;
	}

	// Getters and Setters

	/**
	 * Generic cached value getter.
	 * 
	 * @param <T>
	 *            The type of the cached value.
	 * @param cache
	 *            The cache in which to look for the value.
	 * @param value
	 *            The value to look up.
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
	public Map<String, String> getDetails() {
		return details;
	}

	/**
	 * @return the detailTitles
	 */
	public static Map<String, String> getDetailTitles() {
		return detailTitles;
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
	 * @param key
	 *            The detail key.
	 * @param value
	 *            The string detail value.
	 * @param title
	 *            The detail title, e.g. for use in table column, observation
	 *            details.
	 */
	public void addDetail(String key, String value, String title) {
		if (value != null) {
			value = getCachedValue(detailValueCache, value);
			details.put(key, value);
			if (!detailTitles.containsKey(key)) {
				detailTitles.put(key, title);
				indexToDetailKey.put(detailIndex, key);
				detailKeyToIndex.put(key, detailIndex);
				detailIndex++;
			}
		}
	}

	/**
	 * @return details map value given a key, if it exists, otherwise the empty
	 *         string.
	 */
	public String getDetail(String key) {
		return detailExists(key) ? details.get(key) : "";
	}

	/**
	 * @return detail titles map value given a key
	 */
	public String getDetailTitle(String key) {
		return detailTitles.get(key);
	}

	/**
	 * Does the specified detail key exist?
	 * 
	 * @param key
	 *            The detail key.
	 * @return Whether or not detail exists.
	 */
	public boolean detailExists(String key) {
		return details.keySet().contains(key);
	}

	/**
	 * Does the specified detail title key exist?
	 * 
	 * @param key
	 *            The detail key.
	 * @return Whether or not the detail title exists.
	 */
	public boolean detailTitleExists(String key) {
		return detailTitles.keySet().contains(key);
	}

	/**
	 * Does the specified detail key correspond to a standard detail key?
	 * 
	 * @param key
	 *            The detail key.
	 * @return Whether or not the detail key is standard.
	 */
	public boolean isStandardDetailKey(String key) {
		return standardDetailKeys.contains(key);
	}

	/**
	 * @return the standarddetailkeys
	 */
	public static Set<String> getStandarddetailKeys() {
		return standardDetailKeys;
	}

	/**
	 * @return the dateInfo
	 */
	public DateInfo getDateInfo() {
		return dateInfo;
	}

	/**
	 * @param dateInfo
	 *            the dateInfo to set
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
	 * @param magnitude
	 *            the magnitude to set
	 */
	public void setMagnitude(Magnitude magnitude) {
		this.magnitude = getCachedValue(magnitudeCache, magnitude);
	}

	/**
	 * @return the obsCode
	 */
	public String getObsCode() {
		return getDetail(obsCodeKey);
	}

	/**
	 * @param obsCode
	 *            the obsCode to set
	 */
	public void setObsCode(String obsCode) {
		addDetail(obsCodeKey, obsCode, obsCodeTitle);
	}

	/**
	 * @return whether this observation is discrepant
	 */
	public boolean isDiscrepant() {
		return ValidationType.DISCREPANT.equals(validationType);
	}

	/**
	 * @param discrepant
	 *            the discrepant to set
	 */
	public void setDiscrepant(boolean discrepant) {
		// TODO: Should we keep a record of the last known value of
		// this field before it was marked as discrepant? Right now,
		// we are going from {G,D,P} -> D -> G -> D -> G ... so we are
		// potentially losing information. This is a good candidate
		// for undoable edits.
		this.validationType = discrepant ? ValidationType.DISCREPANT
				: ValidationType.GOOD;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return getDetail(nameKey);
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		addDetail(nameKey, name, nameTitle);
	}

	/**
	 * @return the validationType
	 */
	public ValidationType getValidationType() {
		return validationType;
	}

	/**
	 * @param validationType
	 *            the validationType to set
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
	 * @param hqUncertainty
	 *            the hqUncertainty to set
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
	 * @param band
	 *            the band to set
	 */
	public void setBand(SeriesType band) {
		this.band = band;
	}

	/**
	 * @return the commentCode
	 */
	public CommentCodes getCommentCode() {
		return commentCode;
	}

	/**
	 * @param commentCode
	 *            the commentCode to set
	 */
	public void setCommentCode(String commentCode) {
		this.commentCode = getCachedValue(commentCodeCache, new CommentCodes(
				commentCode));
	}

	/**
	 * @return the compStar1
	 */
	public String getCompStar1() {
		return getDetail(compStar1Key);
	}

	/**
	 * @param compStar1
	 *            the compStar1 to set
	 */
	public void setCompStar1(String compStar1) {
		addDetail(compStar1Key, compStar1, compStar1Title);
	}

	/**
	 * @return the compStar2
	 */
	public String getCompStar2() {
		return getDetail(compStar2Key);
	}

	/**
	 * @param compStar2
	 *            the compStar2 to set
	 */
	public void setCompStar2(String compStar2) {
		addDetail(compStar2Key, compStar2, compStar2Title);
	}

	/**
	 * @return the charts
	 */
	public String getCharts() {
		return getDetail(chartsKey);
	}

	/**
	 * @param charts
	 *            the charts to set
	 */
	public void setCharts(String charts) {
		addDetail(chartsKey, charts, chartsTitle);
	}

	/**
	 * @return the comments
	 */
	public String getComments() {
		return getDetail(commentsKey);
	}

	/**
	 * @param comments
	 *            the comments to set
	 */
	public void setComments(String comments) {
		addDetail(commentsKey, comments, commentsTitle);
	}

	/**
	 * @return the transformed
	 */
	public boolean isTransformed() {
		return transformed;
	}

	/**
	 * @param transformed
	 *            the transformed to set
	 */
	public void setTransformed(boolean transformed) {
		this.transformed = transformed;
	}

	/**
	 * @return the airmass
	 */
	public String getAirmass() {
		return getDetail(airmassKey);
	}

	/**
	 * @param airmass
	 *            the airmass to set
	 */
	public void setAirmass(String airmass) {
		addDetail(airmassKey, airmass, airmassTitle);
	}

	/**
	 * @return the cMag
	 */
	public String getCMag() {
		return getDetail(cMagKey);
	}

	/**
	 * @param cMag
	 *            the cMag to set
	 */
	public void setCMag(String cMag) {
		if ("0.ensemb".equals(cMag)) {
			cMag = "Ensemble";
		}
		addDetail(cMagKey, cMag, cMagTitle);
	}

	/**
	 * @return the kMag
	 */
	public String getKMag() {
		return getDetail(kMagKey);
	}

	/**
	 * @param kMag
	 *            the kMag to set
	 */
	public void setKMag(String kMag) {
		addDetail(kMagKey, kMag, kMagTitle);
	}

	/**
	 * @return the hJD
	 */
	public DateInfo getHJD() {
		return hJD;
	}

	/**
	 * @param hJD
	 *            the hJD to set
	 */
	public void setHJD(DateInfo hJD) {
		this.hJD = getCachedValue(dateInfoCache, hJD);
	}

	/**
	 * @return the mType
	 */
	public MTypeType getMType() {
		return mType;
	}

	/**
	 * @param mType
	 *            the mType to set
	 */
	public void setMType(MTypeType mType) {
		this.mType = mType;
	}

	/**
	 * @return the standardPhase
	 */
	public Double getStandardPhase() {
		return standardPhase;
	}

	/**
	 * @param standardPhase
	 *            the standardPhase to set
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
	 * @param previousCyclePhase
	 *            the previousCyclePhase to set
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
	 * @param excluded
	 *            the excluded to set
	 */
	public void setExcluded(boolean excluded) {
		this.excluded = excluded;
	}

	// Output formatting methods.

	public String toString() {
		StringBuffer strBuf = new StringBuffer();

		if (detailExists(nameKey)) {
			strBuf.append(details.get(nameKey));
			strBuf.append("\n");
		}

		if (dateInfo != null) {
			strBuf.append("Julian Date: ");
			strBuf.append(String.format(NumericPrecisionPrefs
					.getTimeOutputFormat(), dateInfo.getJulianDay()));
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
				strBuf.append(String.format(NumericPrecisionPrefs
						.getTimeOutputFormat(), standardPhase));
				strBuf.append("\n");
			}

			if (previousCyclePhase != null) {
				strBuf.append("Previous Cycle Phase: ");
				strBuf.append(String.format(NumericPrecisionPrefs
						.getTimeOutputFormat(), previousCyclePhase));
				strBuf.append("\n");
			}
		}

		strBuf.append("Magnitude: ");
		strBuf.append(magnitude.toString());
		strBuf.append("\n");

		if (validationType != null) {
			strBuf.append("Validation: ");
			strBuf.append(validationType.toString());
			strBuf.append("\n");
		}

		if (hqUncertainty != null) {
			strBuf.append("HQ Uncertainty: ");
			strBuf.append(String.format(NumericPrecisionPrefs
					.getMagOutputFormat(), hqUncertainty));
			strBuf.append("\n");
		}

		if (band != null) {
			strBuf.append("Band: ");
			strBuf.append(band.getDescription());
			strBuf.append("\n");
		}

		if (detailExists(obsCodeKey)) {
			strBuf.append(detailTitles.get(obsCodeKey) + ": ");
			strBuf.append(details.get(obsCodeKey));
			strBuf.append("\n");
		}

		if (commentCode != null) {
			strBuf.append("Comment Codes:\n");
			strBuf.append(commentCode.toString());
		}

		if (detailExists(compStar1Key)) {
			strBuf.append(detailTitles.get(compStar1Key) + ": ");
			strBuf.append(details.get(compStar1Key));
			strBuf.append("\n");
		}

		if (detailExists(compStar2Key)) {
			strBuf.append(detailTitles.get(compStar2Key) + ": ");
			strBuf.append(details.get(compStar2Key));
			strBuf.append("\n");
		}

		if (detailExists(chartsKey)) {
			strBuf.append(detailTitles.get(chartsKey) + ": ");
			strBuf.append(details.get(chartsKey));
			strBuf.append("\n");
		}

		if (detailExists(commentsKey)) {
			strBuf.append(detailTitles.get(commentsKey) + ": ");
			strBuf.append(details.get(commentsKey));
			strBuf.append("\n");
		}

		if (transformed) {
			strBuf.append("Transformed: yes\n");
		}

		if (detailExists(airmassKey)) {
			strBuf.append(detailTitles.get(airmassKey) + ": ");
			strBuf.append(details.get(airmassKey));
			strBuf.append("\n");
		}

		if (detailExists(cMagKey)) {
			strBuf.append(detailTitles.get(cMagKey) + ": ");
			strBuf.append(details.get(cMagKey));
			strBuf.append("\n");
		}

		if (detailExists(kMagKey)) {
			strBuf.append(detailTitles.get(kMagKey) + ": ");
			strBuf.append(details.get(kMagKey));
			strBuf.append("\n");
		}

		if (hJD != null) {
			strBuf.append("Heliocentric Julian Day: ");
			strBuf.append(String.format(NumericPrecisionPrefs
					.getTimeOutputFormat(), hJD.getJulianDay()));
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
	 * Returns a line in TSV format of the following fields (bracketed fields
	 * are optional):
	 * 
	 * JD,MAGNITUDE,[UNCERTAINTY],[OBSERVER_CODE],[VALFLAG]
	 * 
	 * @param delimiter
	 *            The field delimiter to use.
	 */
	public String toSimpleFormatString(String delimiter) {
		StringBuffer buf = new StringBuffer();

		buf.append(String.format(NumericPrecisionPrefs.getTimeOutputFormat(),
				this.getDateInfo().getJulianDay()));
		buf.append(delimiter);

		buf.append(this.getMagnitude().isFainterThan() ? "<" : "");
		buf.append(String.format(NumericPrecisionPrefs.getMagOutputFormat(),
				this.getMagnitude().getMagValue()));
		buf.append(delimiter);

		double uncertainty = this.getMagnitude().getUncertainty();
		// TODO: why != here and > in next method?
		if (uncertainty != 0.0) {
			buf.append(String.format(
					NumericPrecisionPrefs.getMagOutputFormat(), uncertainty));
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
	 * Returns a line in delimiter-separator (TSV, CSV, ...) AAVSO download
	 * format.
	 * 
	 * @param delimiter
	 *            The field delimiter to use.
	 */
	public String toAAVSOFormatString(String delimiter) {
		StringBuffer buf = new StringBuffer();

		buf.append(String.format(NumericPrecisionPrefs.getTimeOutputFormat(),
				this.getDateInfo().getJulianDay()));
		buf.append(delimiter);

		buf.append(this.getMagnitude().isFainterThan() ? "<" : "");
		buf.append(String.format(NumericPrecisionPrefs.getMagOutputFormat(),
				this.getMagnitude().getMagValue()));
		buf.append(delimiter);

		double uncertainty = this.getMagnitude().getUncertainty();
		if (uncertainty > 0.0) {
			buf.append(String.format(
					NumericPrecisionPrefs.getMagOutputFormat(), uncertainty));
		}
		buf.append(delimiter);

		if (this.getHqUncertainty() != null) {
			double hqUncertainty = this.getHqUncertainty();
			if (hqUncertainty > 0.0) {
				buf.append(String.format(NumericPrecisionPrefs
						.getMagOutputFormat(), hqUncertainty));
			}
		}
		buf.append(delimiter);

		buf.append(this.getBand().getShortName());
		buf.append(delimiter);

		if (detailExists(obsCodeKey)) {
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
			buf.append(this.getComments());
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
			buf.append(String.format(NumericPrecisionPrefs
					.getTimeOutputFormat(), hJD.getJulianDay()));
		}
		buf.append(delimiter);

		buf.append(this.getName());
		buf.append(delimiter);

		// Affiliation
		buf.append(delimiter);

		buf.append(this.getMType() != null ? this.getMType().getShortName()
				: MTypeType.STD.getShortName());
		buf.append(delimiter);

		// Group
		buf.append(delimiter);

		buf.append("\n");

		return buf.toString();
	}

	// Convenience methods.

	public double getJD() {
		return this.dateInfo.getJulianDay();
	}

	public double getMag() {
		return this.magnitude.getMagValue();
	}
}
