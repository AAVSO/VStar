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
package org.aavso.tools.vstar.util.date;

import org.aavso.tools.vstar.util.coords.DecInfo;
import org.aavso.tools.vstar.util.coords.EpochType;
import org.aavso.tools.vstar.util.coords.RAInfo;

/**
 * All HJD converters must extend this base class which also acts as a Factory
 * Method to select a converter for a specified epoch.
 */
public abstract class AbstractHJDConverter {

	private static AbstractHJDConverter J2000Converter = new J2000HJDConverter();
	private static AbstractHJDConverter B1950Converter = new B1950HJDConverter();

	/**
	 * Factory method to return suitable converter for a given epoch.
	 * 
	 * @return The HJD converter for the specified epoch.
	 */
	public static AbstractHJDConverter getInstance(EpochType epoch) {
		AbstractHJDConverter converter = null;

		switch (epoch) {
		case J2000:
			converter = J2000Converter;
			break;
		case B1950:
			converter = B1950Converter;
			break;
		default:
			break;
		}

		return converter;
	}

	/**
	 * Given a JD, RA, and Dec, return HJD.
	 * 
	 * @param jd
	 *            The Julian Date to be converted.
	 * @param ra
	 *            The right ascension coordinate.
	 * @param dec
	 *            The declination coordinate.
	 * @return The corresponding Heliocentric Julian Date.
	 */
	public abstract double convert(double jd, RAInfo ra, DecInfo dec);
}