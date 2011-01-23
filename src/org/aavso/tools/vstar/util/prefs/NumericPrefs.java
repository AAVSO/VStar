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
package org.aavso.tools.vstar.util.prefs;

import java.util.HashMap;
import java.util.Map;

/**
 * Numeric preferences.
 */
public class NumericPrefs {

	private enum Type {
		MAG,
		TIME,
		OTHER;
		
		public String toString() {
			String s = null;
			switch(this) {
			case MAG:
				s = "Magnitude";
				break;
			case TIME:
				s = "Time";
				break;
			case OTHER:
				s = "Other";
				break;
			}
			return s;
		}
	};
	
	// Input decimal place maps.
	
	private static Map<Integer, String> timeInputFormats = new HashMap<Integer, String>();
	private static Map<Integer, String> magInputFormats = new HashMap<Integer, String>();
	private static Map<Integer, String> otherInputFormats = new HashMap<Integer, String>();

	// Output decimal place maps.
	
	private static Map<Integer, String> timeOutputFormats = new HashMap<Integer, String>();
	private static Map<Integer, String> magOutputFormats = new HashMap<Integer, String>();
	private static Map<Integer, String> otherOutputFormats = new HashMap<Integer, String>();
	
	// Current decimal place values.
	
	private static int timeOutputDecimalPlaces = 5;
	private static int timeInputDecimalPlaces = 5;

	private static int magOutputDecimalPlaces = 6;
	private static int magInputDecimalPlaces = 6;

	private static int otherOutputDecimalPlaces = 6;
	private static int otherInputDecimalPlaces = 6;

	// Time (JD, phase)
	
	public static String getTimeOutputFormat() {
		return getOutputFormatString(timeOutputDecimalPlaces, Type.TIME);
	}

	public static String getTimeInputFormat() {
		return getInputFormatString(timeInputDecimalPlaces, Type.TIME);
	}

	// Magnitude
	
	public static String getMagOutputFormat() {
		return getOutputFormatString(magOutputDecimalPlaces, Type.MAG);
	}

	public static String getMagInputFormat() {
		return getInputFormatString(magInputDecimalPlaces, Type.MAG);
	}

	// Other

	public static String getOtherOutputFormat() {
		return getOutputFormatString(otherOutputDecimalPlaces, Type.OTHER);
	}

	public static String getOtherInputFormat() {
		return getInputFormatString(otherInputDecimalPlaces, Type.OTHER);
	}

	// Helpers

	// Construct a printf-style format string for formatted numeric output.
	private static String getOutputFormatString(int decimalPlaces, Type type) {
		Map<Integer, String> formats = null;
		
		switch(type) {
		case MAG:
			formats = magOutputFormats;
			break;
		case TIME:
			formats = timeOutputFormats;
			break;
		case OTHER:
			formats = otherOutputFormats;
			break;
		}
		
		if (!formats.containsKey(decimalPlaces)) {
			formats.put(decimalPlaces, "%1." + String.format("%df", decimalPlaces));
		}
				
		return formats.get(decimalPlaces);
	}
	
	// Construct a format string of the form #.##... where the ellipsis
	// denotes a variable number of hashes. This can be used with input text 
	// boxes where a numeric input is required.
	private static String getInputFormatString(int decimalPlaces, Type type) {
		Map<Integer, String> formats = null;
		
		switch(type) {
		case MAG:
			formats = magInputFormats;
			break;
		case TIME:
			formats = timeInputFormats;
			break;
		case OTHER:
			formats = otherInputFormats;
			break;
		}
		
		if (!formats.containsKey(decimalPlaces)) {
			String s = "";
			for (int i=1;i<=decimalPlaces;i++) {
				s += "#";
			}
			formats.put(decimalPlaces, "#." + s);
		}
				
		return formats.get(decimalPlaces);
	}
}
