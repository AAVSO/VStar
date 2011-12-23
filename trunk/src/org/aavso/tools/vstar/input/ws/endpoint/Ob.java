/**
 * VStar: a statistical analysis tool for variable star data.
 * Copyright (C) 2010  AAVSO (http://www.aavso.org/)
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
package org.aavso.tools.vstar.input.ws.endpoint;

/**
 * Simple observation class.
 */
public class Ob {

	private double jd;
	private double magnitude;
	private double uncertainty;

	public Ob() {	
	}
	
	/**
	 * Constructor
	 * 
	 * @param jd
	 * @param magnitude
	 * @param uncertainty
	 */
	public Ob(double jd, double magnitude, double uncertainty) {
		this.jd = jd;
		this.magnitude = magnitude;
		this.uncertainty = uncertainty;
	}

	/**
	 * @return the jd
	 */
	public double getJd() {
		return jd;
	}

	/**
	 * @param jd the jd to set
	 */
	public void setJd(double jd) {
		this.jd = jd;
	}

	/**
	 * @return the magnitude
	 */
	public double getMagnitude() {
		return magnitude;
	}

	/**
	 * @param magnitude the magnitude to set
	 */
	public void setMagnitude(double magnitude) {
		this.magnitude = magnitude;
	}

	/**
	 * @return the uncertainty
	 */
	public double getUncertainty() {
		return uncertainty;
	}

	/**
	 * @param uncertainty the uncertainty to set
	 */
	public void setUncertainty(double uncertainty) {
		this.uncertainty = uncertainty;
	}
}
