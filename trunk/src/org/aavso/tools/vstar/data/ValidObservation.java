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

import org.aavso.tools.vstar.data.CommentCodes;

/**
 * This class corresponds to a single valid variable star observation. Depending
 * upon the source, some fields may be null. Some are not permitted to be null
 * however and these are documented below.
 * 
 * For reference, here are the fields in the order they appear in the AAVSO
 * download format:
 * 
 * JD(0), MAGNITUDE(1), UNCERTAINTY(2), HQ_UNCERTAINTY(3), BAND(4),
 * OBSERVER_CODE(5), COMMENT_CODE(6), COMP_STAR_1(7), COMP_STAR_2(8), CHARTS(9),
 * COMMENTS(10), TRANSFORMED(11), AIRMASS(12), VALFLAG(13), CMAG(14), KMAG(15),
 * HJD(16), NAME(17), MTYPE(18)
 * 
 * The simple format file has these fields:
 * 
 * JD MAGNITUDE [UNCERTAINTY] [OBSERVER_CODE] [VALFLAG]
 */
public class ValidObservation extends Observation {

	private DateInfo dateInfo = null; // Julian Day, calendar date
	private Magnitude magnitude = null; // magnitude, uncertainty,
	// fainter/brighter-than
	private Double hqUncertainty = null;
	private SeriesType band = null;
	private String obsCode = null;
	private CommentCodes commentCode = null; // TODONE: made into an enum
	private String compStar1 = null;
	private String compStar2 = null;
	private String charts = null;
	private String comments = null;
	private boolean transformed = false;
	private String airmass = null;
	private ValidationType validationType = null;
	// Note: these next two should be double, but some values
	// in the database are non-numeric. VStar doesn't use these
	// fields anyway except for display purposes.
	private String cMag = null;
	private String kMag = null;
	private DateInfo hJD = null; // Heliocentric vs Geocentric Julian Date
	private String name = null;
	private MTypeType mType = MTypeType.STD;

	// Phase values will be computed later, if a phase plot is requested.
	// They may change over the lifetime of a ValidObservation instance
	// since different epoch determination methods will result in different
	// phase values.
	private Double standardPhase = null;
	private Double previousCyclePhase = null;

	/**
	 * Constructor.
	 * 
	 * All fields start out as null or false.
	 */
	public ValidObservation() {
		super(0);
	}

	// Getters and Setters

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
		this.dateInfo = dateInfo;
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
		this.magnitude = magnitude;
	}

	/**
	 * @return the obsCode
	 */
	public String getObsCode() {
		return obsCode;
	}

	/**
	 * @param obsCode
	 *            the obsCode to set
	 */
	public void setObsCode(String obsCode) {
		this.obsCode = obsCode;
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
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
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
		this.commentCode = new CommentCodes(commentCode);
	}

	/**
	 * @return the compStar1
	 */
	public String getCompStar1() {
		return compStar1;
	}

	/**
	 * @param compStar1
	 *            the compStar1 to set
	 */
	public void setCompStar1(String compStar1) {
		this.compStar1 = compStar1;
	}

	/**
	 * @return the compStar2
	 */
	public String getCompStar2() {
		return compStar2;
	}

	/**
	 * @param compStar2
	 *            the compStar2 to set
	 */
	public void setCompStar2(String compStar2) {
		this.compStar2 = compStar2;
	}

	/**
	 * @return the charts
	 */
	public String getCharts() {
		return charts;
	}

	/**
	 * @param charts
	 *            the charts to set
	 */
	public void setCharts(String charts) {
		this.charts = charts;
	}

	/**
	 * @return the comments
	 */
	public String getComments() {
		return comments;
	}

	/**
	 * @param comments
	 *            the comments to set
	 */
	public void setComments(String comments) {
		this.comments = comments;
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
		return airmass;
	}

	/**
	 * @param airmass
	 *            the airmass to set
	 */
	public void setAirmass(String airmass) {
		this.airmass = airmass;
	}

	/**
	 * @return the cMag
	 */
	public String getCMag() {
		return cMag;
	}

	/**
	 * @param cMag
	 *            the cMag to set
	 */
	public void setCMag(String cMag) {
		this.cMag = cMag;
	}

	/**
	 * @return the kMag
	 */
	public String getKMag() {
		return kMag;
	}

	/**
	 * @param kMag
	 *            the kMag to set
	 */
	public void setKMag(String kMag) {
		this.kMag = kMag;
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
		this.hJD = hJD;
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

	public String toString() {
		StringBuffer strBuf = new StringBuffer();

		if (name != null) {
			strBuf.append(name);
			strBuf.append("\n");
		}

		if (dateInfo != null) {
			strBuf.append("Julian Date: ");
			strBuf.append(dateInfo.getJulianDay());
			strBuf.append("\n");

			strBuf.append("Calendar Date: ");
			strBuf.append(dateInfo.getCalendarDate());
			strBuf.append("\n");
		}

		// TODO: only show these if analysis mode is phase plot
		// or is it okay to show last-non-null phases in any mode?
		
		if (standardPhase != null) {
			strBuf.append("Standard Phase: ");
			strBuf.append(standardPhase);
			strBuf.append("\n");
		}

		if (previousCyclePhase != null) {
			strBuf.append("Previous Cycle Phase: ");
			strBuf.append(previousCyclePhase);
			strBuf.append("\n");
		}

		strBuf.append("Magnitude: ");
		strBuf.append(magnitude);
		strBuf.append("\n");

		if (validationType != null) {
			strBuf.append("Validation: ");
			strBuf.append(validationType.toString());
			strBuf.append("\n");
		}

		if (hqUncertainty != null) {
			strBuf.append("HQ Uncertainty: ");
			strBuf.append(hqUncertainty);
			strBuf.append("\n");
		}
		
		if (band != null) {
			strBuf.append("Band: ");
			strBuf.append(band.getDescription());
			strBuf.append("\n");
		}
		
		if (obsCode != null) {
			strBuf.append("Observer Code: ");
			strBuf.append(obsCode);
			strBuf.append("\n");
		}
		
		if (commentCode != null) {
			strBuf.append("Comment Codes:\n");
			strBuf.append(commentCode.toString());
		}
		
		if (compStar1 != null) {
			strBuf.append("Comparison Star 1: ");
			strBuf.append(compStar1);
			strBuf.append("\n");
		}

		if (compStar2 != null) {
			strBuf.append("Comparison Star 2: ");
			strBuf.append(compStar2);
			strBuf.append("\n");
		}

		if (charts != null) {
			strBuf.append("Charts: ");
			strBuf.append(charts);
			strBuf.append("\n");
		}
		if (comments != null) {
			strBuf.append("Comments: ");
			strBuf.append(comments);
			strBuf.append("\n");
		}

		strBuf.append("Transformed: ");
		strBuf.append(transformed ? "yes" : "no");
		strBuf.append("\n");

		if (airmass != null) {
			strBuf.append("Airmass: ");
			strBuf.append(airmass);
			strBuf.append("\n");
		}

		if (cMag != null) {
			strBuf.append("CMag: ");
			strBuf.append(cMag);
			strBuf.append("\n");
		}

		if (kMag != null) {
			strBuf.append("KMag: ");
			strBuf.append(kMag);
			strBuf.append("\n");
		}

		if (hJD != null) {
			strBuf.append("Heliocentric Julian Day: ");
			strBuf.append(hJD);
			strBuf.append("\n");
		}

		return strBuf.toString();
	}

	// IMagJDAndPhaseSource methods

	public double getJD() {
		return this.dateInfo.getJulianDay();
	}

	public double getMag() {
		return this.magnitude.getMagValue();
	}
}
