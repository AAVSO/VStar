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
package org.aavso.tools.vstar.vela;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

import org.aavso.tools.vstar.vela.VeLaParser.RealContext;

/**
 * VeLa: VStar expression Language interpreter
 * 
 * Real number listener.
 */
public class RealListener extends VeLaBaseListener {

	// The number format for the locale with which the JVM was started.
	private static final NumberFormat FORMAT = NumberFormat
			.getNumberInstance(Locale.getDefault());

	private double n;

	public RealListener() {
		// TODO: pass in stack
	}

	@Override
	public void enterReal(RealContext ctx) {
		String str = ctx.getText();
		n = parseDouble(str);
	}

	@Override
	public void exitReal(RealContext ctx) {
		String str = ctx.getText();
		n = parseDouble(str);
	}

	
	/**
	 * @return the n
	 */
	public double getN() {
		return n;
	}

	/**
	 * Parse a string, returning a double primitive value, or if no valid double
	 * value is present, throw a NumberFormatException. The string is first
	 * trimmed of leading and trailing whitespace.
	 * 
	 * @param str
	 *            The string that (hopefully) contains a number.
	 * @return The double value corresponding to the initial parseable portion
	 *         of the string.
	 * @throws NumberFormatException
	 *             If no valid double value is present.
	 * 
	 */
	protected double parseDouble(String str) throws NumberFormatException {
		if (str == null) {
			throw new NumberFormatException("String was null");
		} else {
			try {
				str = str.trim();
				if (str.startsWith("+")) {
					// Leading "+" causes an exception to be thrown.
					str = str.substring(1);
				}
				return FORMAT.parse(str).doubleValue();
			} catch (ParseException e) {
				throw new NumberFormatException(e.getLocalizedMessage());
			}
		}
	}
}
