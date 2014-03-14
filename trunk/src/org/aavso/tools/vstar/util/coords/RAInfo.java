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
package org.aavso.tools.vstar.util.coords;

/**
 * This class represents a Right Ascension coordinate.
 */
public class RAInfo {

	private int epoch;
	private double degs;

	/**
	 * Constructor
	 * 
	 * @param epoch
	 *            The epoch of the RA coord.
	 * @param hours
	 *            The hours component of the RA coord.
	 * @param minutes
	 *            The minutes component of the RA coord.
	 * @param seconds
	 *            The seconds component of the RA coord.
	 */
	public RAInfo(int epoch, int hours, int minutes, double seconds) {
		degs = 15 * (hours + minutes / 60.0 + seconds / 3600.0);
	}

	/**
	 * Construct an RAInfo instance from hours as decimal degrees.
	 * 
	 * @param ra
	 *            The RA in decimal degrees.
	 */
	public RAInfo(int epoch, double ra) {
		this.epoch = epoch;
		degs = ra;
	}

	/**
	 * @return the epoch
	 */
	public int getEpoch() {
		return epoch;
	}

	public String toString() {
		return String.format("RA (%d): %f degrees", epoch, degs);
	}

	public double toDegrees() {
		return degs;
	}
}