/**
 * VStar: a statistical analysis tool for variable star data.
 * Copyright (C) 2016  AAVSO (http://www.aavso.org/)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more detaisls.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */
package org.aavso.tools.vstar.util.date;

/**
 * Solar coordinates and related values.
 */
public class SolarCoords {

	private double ra;
	private double dec;
	private double apparentRA;
	private double apparentDec;
	private double Lo;
	private double M;
	private double C;

	/**
	 * Constructor
	 * 
	 * @param ra
	 *            The Sun's RA in decimal degrees.
	 * @param dec
	 *            The Sun's declination in decimal degrees.
	 * @param apparentRA
	 *            The Sun's apparent RA in decimal degrees.
	 * @param apparentDec
	 *            The Sun's apparent declination in decimal degrees.
	 * @param Lo
	 *            Geometric mean longitude of the Sun.
	 * @param M
	 *            The true solar anomaly.
	 * @param C
	 *            The Sun's equation of center.
	 */
	public SolarCoords(double ra, double dec, double apparentRA,
			double apparentDec, double Lo, double M, double C) {
		super();
		this.ra = ra;
		this.dec = dec;
		this.apparentRA = apparentRA;
		this.apparentDec = apparentDec;
		this.Lo = Lo;
		this.M = M;
		this.C = C;
	}

	/**
	 * @return the Sun's right ascension in decimal degrees.
	 */
	public double getRA() {
		return ra;
	}

	/**
	 * @return the Sun's declination in decimal degrees.
	 */
	public double getDec() {
		return dec;
	}

	/**
	 * @return the Sun's apparent right ascension in decimal degrees.
	 */
	public double getApparentRA() {
		return apparentRA;
	}

	/**
	 * @return the Sun's apparent declination in decimal degrees.
	 */
	public double getApparentDec() {
		return apparentDec;
	}

	/**
	 * @return the Sun's geometric mean longitude in radians.
	 */
	public double getGeometricMeanLongitude() {
		return Lo;
	}

	/**
	 * @return the Sun's true anomaly in radians.
	 */
	public double getTrueAnomaly() {
		return M;
	}

	/**
	 * @return the Sun's equation of center in radians.
	 */
	public double getEquationOfCenter() {
		return C;
	}
}
