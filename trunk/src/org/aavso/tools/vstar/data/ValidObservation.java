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

import org.aavso.tools.vstar.data.visitor.ObservationVisitor;

/**
 * This class corresponds to a single valid variable star observation. 
 * Depending upon the source, some fields may be null. Some are not 
 * permitted to be null however and these are documented below.
 * TODO: or we will specialise classes for input source type
 */
public class ValidObservation implements Observation {

	// Data members
	private String starName; // TODO: probably not required
	private DateInfo dateInfo;
	private Magnitude magnitude;
	private String obsCode;

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
	public ValidObservation(String starName,
			DateInfo dateInfo, Magnitude magnitude, String obsCode) {
		this.starName = starName;
		this.dateInfo = dateInfo;
		this.magnitude = magnitude;
		this.obsCode = obsCode;
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
	public ValidObservation(
			DateInfo dateInfo, Magnitude magnitude, String obsCode) {
		this.starName = null;
		this.dateInfo = dateInfo;
		this.magnitude = magnitude;
		this.obsCode = obsCode;
	}

	// Getters and Setters
	
	public String getStarName() {
		return starName;
	}

	public DateInfo getDateInfo() {
		return dateInfo;
	}

	public Magnitude getMagnitude() {
		return magnitude;
	}

	public String getObsCode() {
		return obsCode;
	}

	public void accept(ObservationVisitor v) {
		v.visit(this);
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
