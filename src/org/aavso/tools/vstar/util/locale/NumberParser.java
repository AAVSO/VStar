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
package org.aavso.tools.vstar.util.locale;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

/**
 * This class contains static methods that parse numbers in a locale-independent
 * way. They are intended to be replacements for Number-subclass methods of the
 * same names.
 */
public class NumberParser {

	// The number format for the locale with which the JVM was started.
	private static final NumberFormat FORMAT = NumberFormat.getNumberInstance(Locale
			.getDefault());

	/**
	 * Parse a string, returning a double primitive value, or if no valid double
	 * value is present, throw a NumberFormatException. The string is first trimmed
	 * of leading and trailing whitespace.
	 * 
	 * @param str
	 *            The string that (hopefully) contains a number.
	 * @return The double value corresponding to the initial parseable portion
	 *         of the string.
	 * @throws NumberFormatException
	 *             If no valid double value is present.
	 * 
	 */
	public static double parseDouble(String str) throws NumberFormatException {
		if (str == null) {
			throw new NumberFormatException("String was null");
		} else {
			try {
				return FORMAT.parse(str.trim()).doubleValue();
			} catch (ParseException e) {
				throw new NumberFormatException(e.getLocalizedMessage());
			}
		}
	}
}
