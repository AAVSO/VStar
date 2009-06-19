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
 * however and these are documented below. TODO: or we will specialise classes
 * for input source type
 */
public class ValidObservation extends Observation implements IDateAndMagSource {

	private String starName; // TODO: probably don't want this here; map to set
								// of observations
	private DateInfo dateInfo;
	private Magnitude magnitude;
	private String obsCode;
	private boolean discrepant;
	
	/**
	 * Constructor for use with simple observation format.
	 * 
	 * @param starName
	 *            The name of the star to which this observation pertains.
	 * @param dateInfo
	 *            The Julian Day at which this observation was made.
	 * @param magnitude
	 *            The magnitude of the star for this observation.
	 * @param obsCode
	 *            The observer code.
	 */
	public ValidObservation(String starName, DateInfo dateInfo,
			Magnitude magnitude, String obsCode) {
		super(0);
		this.starName = starName;
		this.dateInfo = dateInfo;
		this.magnitude = magnitude;
		this.obsCode = obsCode;
		this.discrepant = false;
	}

	/**
	 * Constructor for use with simple observation format.
	 * 
	 * @param dateInfo
	 *            The Julian Day at which this observation was made.
	 * @param magnitude
	 *            The magnitude of the star for this observation.
	 * @param obsCode
	 *            The observer code.
	 */
	public ValidObservation(DateInfo dateInfo, Magnitude magnitude,
			String obsCode) {
		this("Unknown", dateInfo, magnitude, obsCode);
	}

	// Getters and Setters

	public String getStarName() {
		return starName;
	}

	/**
	 * @see org.aavso.tools.vstar.data.IDateAndMagSource#getDateInfo()
	 */
	public DateInfo getDateInfo() {
		return dateInfo;
	}

	/**
	 * @see org.aavso.tools.vstar.data.IDateAndMagSource#getMagnitude()
	 */
	public Magnitude getMagnitude() {
		return magnitude;
	}

	public String getObsCode() {
		return obsCode;
	}
	
	public boolean isDiscrepant() {
		return discrepant;
	}

	public void setDiscrepant(boolean discrepant) {
		// TODO: notify listeners if old and new values are different!
		this.discrepant = discrepant;
	}

	@Override
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

		return strBuf.toString();
	}
}
