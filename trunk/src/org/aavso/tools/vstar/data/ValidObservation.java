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

/**
 * This class corresponds to a single valid variable star observation. Depending
 * upon the source, some fields may be null. Some are not permitted to be null
 * however and these are documented below.
 * 
 * For reference, here are the fields in the order they appear in the 
 * AAVSO download format:
 *  
 * JD(0), MAGNITUDE(1), UNCERTAINTY(2), HQ_UNCERTAINTY(3), BAND(4),
 * OBSERVER_CODE(5), COMMENT_CODE(6), COMP_STAR_1(7), COMP_STAR_2(8), CHARTS(9),
 * COMMENTS(10), TRANSFORMED(11), AIRMASS(12), VALFLAG(13), CMAG(14), KMAG(15),
 * HJD(16), NAME(17)
 * 
 * The simple format file has these fields:
 * 
 * JD MAGNITUDE [UNCERTAINTY] [OBSERVER_CODE] [VALFLAG]
 */
public class ValidObservation extends Observation implements IDateAndMagSource {

	private DateInfo dateInfo; // JD
	private Magnitude magnitude; // MAGNITUDE {<N:}, UNCERTAINTY
	private String obsCode; // OBSERVER_CODE
	private ValidationType validationType; // VALFLAG
	private boolean discrepant; // TODO: get rid of this!
	private String starName;

	/**
	 * Constructor.
	 * 
	 * All fields start out as null or false.
	 */
	public ValidObservation() {
		super(0);
		this.dateInfo = null;
		this.magnitude = null;
		
		this.obsCode = null;
		this.discrepant = false;
		this.starName = null;
	}

	// Getters and Setters

	// public boolean isDiscrepant() {
	// return discrepant; // TODO: use an enum comparison
	// }
	//
	// public void setDiscrepant(boolean discrepant) {
	// // TODO: notify listeners if old and new values are different!
	// this.discrepant = discrepant;
	// }

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
		this.validationType = ValidationType.DISCREPANT;
	}

	/**
	 * @return the starName
	 */
	public String getStarName() {
		return starName;
	}

	/**
	 * @param starName
	 *            the starName to set
	 */
	public void setStarName(String starName) {
		this.starName = starName;
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

	public String toString() {
		StringBuffer strBuf = new StringBuffer();

		if (starName != null) {
			strBuf.append(starName);
			strBuf.append(", ");
		}
		strBuf.append(dateInfo);
		strBuf.append(", ");
		strBuf.append(magnitude);
		strBuf.append(", Observer code: ");
		strBuf.append(obsCode);
		strBuf.append(validationType.toString());

		return strBuf.toString();
	}
}
