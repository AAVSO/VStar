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

package org.aavso.tools.vstar.util;

//JDCalculator.java

/**
 * This class has been minimally adapted (AbstractDateUtil base class 
 * extension) for VStar from Sara Beck's code used in Zapper.
 * 
 * This utility class contains the formula for converting the integer
 * part of a Julian Date into a calendar date. The formula comes from
 * Sky & Telescope Magazine (May 1984, page 455). In addition to providing
 * a JD/Calendar date conversion for Zapper, this program can be run
 * standalone to convert a JD into a calendar date (no time) by typing:
 * java JDCalulator.java at the prompt. NOTE: There is a problem with
 * negative years which caused the JD to be off by 2 days.
 *
 * @author Sara Beck
 * @version 06/12/08
 */

import java.util.Scanner;

public class JDCalculator extends AbstractDateUtil {
	private int a1, c, d1, e, j1, month, year;
	private double a, b, f, s, jd, day;
	private String mon;
	private String calendarDate;

	/**
	 * @see org.aavso.tools.vstar.util.AbstractDateUtil#jdToCalendar(double)
	 */

	public String jdToCalendar(double jd) throws IllegalArgumentException {
		a1 = (int) ((jd / 36524.25) - 51.12264);
		a = jd + 1 + a1 - (int) (a1 / 4);
		b = a + 1524;
		c = (int) ((b / 365.25) - .3343);
		day = (int) (365.25 * c);
		e = (int) ((b - day) / 30.61);
		day = b - day - (int) (30.61 * e) + .5;
		month = e - 1;
		year = c - 4716;
		if (e > 13.5) {
			month = month - 12;
		}
		if (month < 2.5) {
			year = year + 1;
		}
		return (year + " " + getMonthName(month) + " " + (int) day);
	}

	/**
	 * @see org.aavso.tools.vstar.util.AbstractDateUtil#calendarToJD(int, int, double)
	 */
	public double calendarToJD(int year, int month, double day) {
		d1 = (int) day;
		f = day - d1 - .5;
		jd = -(int) (7 * ((int) ((month + 9) / 12) + year) / 4);
		s = Math.signum(month - 9);
		a = Math.abs(month - 9);
		j1 = (int) (year + s * (int) (a / 7));
		j1 = -(int) (((int) (j1 / 100) + 1) * 3 / 4);
		jd = jd + (int) (275 * month / 9) + d1 + j1;
		jd = jd + 1721027 + 2 + 367 * year;
		if (f < 0) {
			f = f + 1;
			jd = jd - 1;
		}
		jd = jd + f;
		return jd;
	}

	public static void main(String[] args) {
		String answer;
		JDCalculator c = new JDCalculator();
		Scanner keyboard = new Scanner(System.in);
		System.out
				.print("Please type \"J\" if you are converting JD to calendar \nor \"C\" if you are converting calendar to JD: ");
		answer = keyboard.nextLine();
		if (answer.equals("J") || answer.equals("j")) {
			System.out.print("Enter a JD: ");
			c.jd = keyboard.nextDouble();
			System.out.println(c.jdToCalendar(c.jd));
		}
		if (answer.equals("C") || answer.equals("c")) {
			System.out.print("Enter a calendar year (YYYY): ");
			c.year = keyboard.nextInt();
			System.out.print("Enter a month number (MM): ");
			c.month = keyboard.nextInt();
			System.out.print("Enter a day number (DD.DDDD...): ");
			c.day = keyboard.nextDouble();
			System.out.println(c.calendarToJD(c.year, c.month, c.day));
		}
	}
}