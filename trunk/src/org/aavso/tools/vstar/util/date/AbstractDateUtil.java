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

package org.aavso.tools.vstar.util.date;


/**
 * This abstract class is the base for classes that need to convert between
 * Julian Day and Calendar Date.
 */
public abstract class AbstractDateUtil {

	// The concrete AbstractDateUtil Singleton.
	private final static AbstractDateUtil dateUtilInstance = new MeeusDateUtil();

	/**
	 * Getter for date AbstractDateUtil Singleton.
	 * 
	 * @return The concrete AbstractDateUtil Singleton instance.
	 */
	public static AbstractDateUtil getInstance() {
		return dateUtilInstance;
	}

	/**
	 * Method to convert the integer part of a JD into a calendar date.
	 * 
	 * @param jd
	 *            Julian Day (double)
	 * @return calendar date string of the form "YEAR MON DAY"
	 */

	public abstract String jdToCalendar(double jd)
			throws IllegalArgumentException;

	/**
	 * Method to convert the Julian Day corresponding to the specified year,
	 * month, and day.
	 * 
	 * @param year
	 *            The year.
	 * @param month
	 *            The month (1..12).
	 * @param day
	 *            The day which may contain a fractional component.
	 * @return The Julian Day (double)
	 */
	public abstract double calendarToJD(int year, int month, double day)
			throws IllegalArgumentException;

	/**
	 * Returns the name of the month given the month number.
	 * 
	 * @param month
	 *            The month number from 1..12
	 * @return The month name
	 */
	public String getMonthName(int month) throws IllegalArgumentException {
		String mon = null;

		switch (month) {
		case 1:
			mon = "JAN";
			break;
		case 2:
			mon = "FEB";
			break;
		case 3:
			mon = "MAR";
			break;
		case 4:
			mon = "APR";
			break;
		case 5:
			mon = "MAY";
			break;
		case 6:
			mon = "JUN";
			break;
		case 7:
			mon = "JUL";
			break;
		case 8:
			mon = "AUG";
			break;
		case 9:
			mon = "SEP";
			break;
		case 10:
			mon = "OCT";
			break;
		case 11:
			mon = "NOV";
			break;
		case 12:
			mon = "DEC";
			break;
		default:
			throw new IllegalArgumentException("Month number '" + month
					+ "' out of range.");
		}

		return mon;
	}
}