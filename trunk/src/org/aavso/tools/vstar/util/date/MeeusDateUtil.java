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
 * This class implements AbstractDateUtil methods using Jean Meeus's
 * Astronomical Algorithms 1991, 1st edition, chapter 7.
 */
public class MeeusDateUtil extends AbstractDateUtil {

	/**
	 * This method determines the Calendar day given the Julian Day. It is not
	 * valid for negative Julian Days (but is valid for negative year results).
	 * 
	 * @see org.aavso.tools.vstar.util.date.AbstractDateUtil#jdToCalendar(double)
	 */
	public String jdToCalendar(double jd) throws IllegalArgumentException {

		jd += 0.5;
		int z = (int) jd;
		double f = jd - z;

		int a;
		if (z < 2299161) {
			a = z;
		} else {
			int alpha = (int) ((z - 1867216.25) / 36524.25);
			a = z + 1 + alpha - (int) (alpha / 4);
		}

		int b = a + 1524;
		int c = (int) ((b - 122.1) / 365.25);
		int d = (int) (365.25 * c);
		int e = (int) ((b - d) / 30.6001);

		double day = b - d - (int) (30.6001 * e) + f;

		int month;
		if (e < 14) {
			month = e - 1;
		} else if (e == 14 || e == 15) {
			month = e - 13;
		} else {
			throw new IllegalArgumentException("Unable to convert Julian Day '"
					+ jd + "'");
		}
		
		int year;
		if (month > 2) {
			year = c-4716;
		} else if (month == 1 || month == 2) {
			year = c-4715;
		} else {
			throw new IllegalArgumentException("Unable to convert Julian Day '"
					+ jd + "'");			
		}

		return (year + " " + getMonthName(month) + " " + (int) day);
	}

	/**
	 * The method determines the Julian Day for any year, including dates prior
	 * to the start of the Gregorian Calendar. It is not valid for negative
	 * Julian Day results (but is valid for negative years).
	 * 
	 * @see org.aavso.tools.vstar.util.date.AbstractDateUtil#calendarToJD(int, int,
	 *      double)
	 */
	public double calendarToJD(int year, int month, double day)
			throws IllegalArgumentException {
		int a, b;
		double jd;

		if (month == 1 || month == 2) {
			year--;
			month += 12;
		}

		a = (int) (year / 100);
		if (inGregorianCalendar((int) day, month, year)) {
			b = 2 - a + (int) (a / 4);
		} else {
			b = 0;
		}

		jd = (int) (365.25 * (year + 4716)) + (int) (30.6001 * (month + 1))
				+ day + b - 1524.5;

		return jd;
	}

	/**
	 * Is the specified date in the Gregorian Calendar which started the day
	 * after Oct 4 1582? This day was in fact Oct 15 1582.
	 * 
	 * @param day
	 *            The (integer) day.
	 * @param month
	 *            The month (1..12).
	 * @param year
	 *            The year.
	 */
	private boolean inGregorianCalendar(int day, int month, int year) {
		if (year > 1582)
			return true;
		else if (year == 1582 && month > 10)
			return true;
		else if (year == 1582 && month == 10 && day >= 15)
			return true;
		else
			return false;
	}
}
