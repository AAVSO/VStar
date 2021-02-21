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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.prefs.Preferences;

/**
 * Numeric input/output format preferences.
 */
public class NumericPrecisionPrefs {

	private enum Type {
		MAG, TIME, OTHER;

		public String toString() {
			String s = null;
			switch (this) {
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

	private static Map<Integer, DecimalFormat> timeOutputFormats = new HashMap<Integer, DecimalFormat>();
	private static Map<Integer, DecimalFormat> magOutputFormats = new HashMap<Integer, DecimalFormat>();
	private static Map<Integer, DecimalFormat> otherOutputFormats = new HashMap<Integer, DecimalFormat>();

	// Default decimal place values.

	private static int DEFAULT_TIME_DECIMAL_PLACES = 5;
	private static int DEFAULT_MAG_DECIMAL_PLACES = 6;
	private static int DEFAULT_OTHER_DECIMAL_PLACES = 6;

	// Current decimal place values.

	private static int timeDecimalPlaces = DEFAULT_TIME_DECIMAL_PLACES;
	private static int magDecimalPlaces = DEFAULT_MAG_DECIMAL_PLACES;
	private static int otherDecimalPlaces = DEFAULT_OTHER_DECIMAL_PLACES;

	/**
	 * @return the timeDecimalPlaces
	 */
	public static int getTimeDecimalPlaces() {
		return timeDecimalPlaces;
	}

	/**
	 * @param timeDecimalPlaces
	 *            the timeDecimalPlaces to set
	 */
	public static void setTimeDecimalPlaces(int timeDecimalPlaces) {
		NumericPrecisionPrefs.timeDecimalPlaces = timeDecimalPlaces;
	}

	/**
	 * @return the magDecimalPlaces
	 */
	public static int getMagDecimalPlaces() {
		return magDecimalPlaces;
	}

	/**
	 * @param magDecimalPlaces
	 *            the magDecimalPlaces to set
	 */
	public static void setMagDecimalPlaces(int magDecimalPlaces) {
		NumericPrecisionPrefs.magDecimalPlaces = magDecimalPlaces;
	}

	/**
	 * @return the otherDecimalPlaces
	 */
	public static int getOtherDecimalPlaces() {
		return otherDecimalPlaces;
	}

	/**
	 * @param otherDecimalPlaces
	 *            the otherDecimalPlaces to set
	 */
	public static void setOtherDecimalPlaces(int otherDecimalPlaces) {
		NumericPrecisionPrefs.otherDecimalPlaces = otherDecimalPlaces;
	}

	// Time (JD, phase)

	public static String formatTime(double num) {
		return getTimeOutputFormat().format(num);
	}

	public static DecimalFormat getTimeOutputFormat() {
		return getOutputFormat(timeDecimalPlaces, Type.TIME);
	}

	public static String getTimeInputFormat() {
		return getInputFormatString(timeDecimalPlaces, Type.TIME);
	}

	// Magnitude

	public static String formatMag(double num) {
		return getMagOutputFormat().format(num);
	}

	public static DecimalFormat getMagOutputFormat() {
		return getOutputFormat(magDecimalPlaces, Type.MAG);
	}

	public static String getMagInputFormat() {
		return getInputFormatString(magDecimalPlaces, Type.MAG);
	}

	// Other

	public static String formatOther(double num) {
		return getOtherOutputFormat().format(num);
	}

	public static DecimalFormat getOtherOutputFormat() {
		return getOutputFormat(otherDecimalPlaces, Type.OTHER);
	}

	public static String getOtherInputFormat() {
		return getInputFormatString(otherDecimalPlaces, Type.OTHER);
	}

	// Helpers

	// Construct a formatter for numeric output.
	private static DecimalFormat getOutputFormat(int decimalPlaces, Type type) {
		Map<Integer, DecimalFormat> formats = null;

		switch (type) {
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
			formats.put(decimalPlaces, getOutputFormat(decimalPlaces));
		}

		return formats.get(decimalPlaces);
	}

	// Construct a format string of the form #.##... where the ellipsis
	// denotes a variable number of hashes. This can be used with input text
	// boxes where a numeric input is required.
	private static String getInputFormatString(int decimalPlaces, Type type) {
		Map<Integer, String> formats = null;

		switch (type) {
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
			formats.put(decimalPlaces, getFormatString(decimalPlaces));
		}

		return formats.get(decimalPlaces);
	}

	private static DecimalFormat getOutputFormat(int decimalPlaces) {
		DecimalFormat decFormatter = new DecimalFormat(
				getFormatString(decimalPlaces), new DecimalFormatSymbols(Locale
						.getDefault()));

		return decFormatter;
	}

	private static String getFormatString(int decimalPlaces) {
		String s = "";
		for (int i = 1; i <= decimalPlaces; i++) {
			s += "#";
		}
		return "#." + s;
	}

	// Preferences members.

	private final static String PREFS_PREFIX = "NUMERIC_DECIMAL_PLACES_";

	private static Preferences prefs;

	static {
		// Create preferences node for numeric precision.
		try {
			prefs = Preferences.userNodeForPackage(NumericPrecisionPrefs.class);
			retrieveDecimalPlacesPrefs();
		} catch (Throwable t) {
			// We need VStar to function in the absence of prefs.
		}
	}

	private static void retrieveDecimalPlacesPrefs() {
		timeDecimalPlaces = prefs.getInt(PREFS_PREFIX + "time_decimal_places",
				DEFAULT_TIME_DECIMAL_PLACES);

		magDecimalPlaces = prefs.getInt(PREFS_PREFIX + "mag_decimal_places",
				DEFAULT_MAG_DECIMAL_PLACES);

		otherDecimalPlaces = prefs.getInt(
				PREFS_PREFIX + "other_decimal_places",
				DEFAULT_OTHER_DECIMAL_PLACES);
	}

	public static void storeDecimalPlacesPrefs() {
		try {
			prefs.putInt(PREFS_PREFIX + "time_decimal_places",
					timeDecimalPlaces);
			prefs.putInt(PREFS_PREFIX + "mag_decimal_places", magDecimalPlaces);
			prefs.putInt(PREFS_PREFIX + "other_decimal_places",
					otherDecimalPlaces);
			prefs.flush();
		} catch (Throwable t) {
			// We need VStar to function in the absence of prefs.
		}
	}

	public static void setDefaultDecimalPlacePrefs() {
		timeDecimalPlaces = DEFAULT_TIME_DECIMAL_PLACES;
		magDecimalPlaces = DEFAULT_MAG_DECIMAL_PLACES;
		otherDecimalPlaces = DEFAULT_OTHER_DECIMAL_PLACES;
		storeDecimalPlacesPrefs();
	}
}
