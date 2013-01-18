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
 * This class represents a Declination coordinate.
 */
public class DecInfo {

	private int epoch;
	private int degrees;
	private int minutes;
	private double seconds;

	private double degs;

	/**
	 * Constructor
	 * 
	 * @param epoch
	 *            The epoch of the Dec coord.
	 * @param degrees
	 *            The degrees component of the Dec coord.
	 * @param minutes
	 *            The minutes component of the Dec coord.
	 * @param seconds
	 *            The seconds component of the Dec coord.
	 */
	public DecInfo(int epoch, int degrees, int minutes, double seconds) {
		this.epoch = epoch;
		this.degrees = degrees;
		this.minutes = minutes;
		this.seconds = seconds;
		degs = Math.signum(degrees)
				* (Math.abs(degrees) + minutes / 60.0 + seconds / 3600.0);
	}

	/**
	 * @return the epoch
	 */
	public int getEpoch() {
		return epoch;
	}

	/**
	 * @return the degrees
	 */
	public int getDegrees() {
		return degrees;
	}

	/**
	 * @return the minutes
	 */
	public int getMinutes() {
		return minutes;
	}

	/**
	 * @return the seconds
	 */
	public double getSeconds() {
		return seconds;
	}

	public String toString() {
		return String.format("Dec (%d): %dd %dm %fs", epoch, degrees, minutes,
				seconds);
	}

	public double toDegrees() {
		return degs;
	}
}
