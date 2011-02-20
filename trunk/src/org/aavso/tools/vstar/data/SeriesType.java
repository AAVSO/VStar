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
package org.aavso.tools.vstar.data;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;

import org.aavso.tools.vstar.util.notification.Notifier;

// TODO:
// - Note that this enum-based approach won't necessarily work once we
//   try to extend VStar to accept other sources, so...
// - Eventually change this enum so that it starts out with a default set
//   of series enum values but when connected to AID, it "refreshes" that
//   Set<SeriesType> dynamically. Or just live with periodic code-generated
//   updates to this class's enums. Other possibilities? Use a class with a
//   Singleton Registry of instances of the class.
// - Review all places where null or default type is permitted and
//   eliminate.

/**
 * A type for bands and other series types, e.g. fainter-thans, means.
 */
public enum SeriesType {

	// ** Auto-generated bands from aid.bands start here **

	Visual(0, "Visual", "Vis.", new Color(0, 0, 0)), Unknown(1, "Unknown",
			"N/A", new Color(255, 255, 0)), Johnson_R(10, "Johnson R", "RJ",
			new Color(192, 0, 64)), Johnson_I(11, "Johnson I", "IJ", new Color(
			192, 64, 128)), Johnson_V(2, "Johnson V", "V", new Color(0, 255, 0)), Blue(
			21, "Blue", "Blue-Vis.", new Color(0, 0, 128)), Green(22, "Green",
			"Green-Vis.", new Color(0, 128, 0)), Red(23, "Red", "Red-Vis.",
			new Color(128, 0, 0)), Yellow(24, "Yellow", "Yellow-Vis.",
			new Color(255, 255, 128)), K_NIR_2pt2micron(26, "K NIR 2.2micron",
			"K", new Color(255, 128, 255)), H_NIR_1pt6micron(27,
			"H NIR 1.6micron", "H", new Color(128, 128, 128)), J_NIR_1pt2micron(
			28, "J NIR 1.2micron", "J", new Color(255, 0, 255)), Sloan_z(29,
			"Sloan z", "SZ", new Color(255, 192, 0)), Johnson_B(3, "Johnson B",
			"B", new Color(0, 0, 255)), Stromgren_u(30, "Stromgren u", "STU",
			new Color(0, 192, 255)), Stromgren_v(31, "Stromgren v", "STV",
			new Color(0, 255, 192)), Stromgren_b(32, "Stromgren b", "STB",
			new Color(0, 0, 192)), Stromgren_y(33, "Stromgren y", "STY",
			new Color(192, 255, 0)), Stromgren_Hbw(34, "Stromgren Hbw",
			"STHBW", new Color(0, 128, 255)), Stromgren_Hbn(35,
			"Stromgren Hbn", "STHBN", new Color(0, 128, 192)), Cousins_R(4,
			"Cousins R", "R", new Color(255, 0, 0)), Sloan_u(40, "Sloan u",
			"SU", new Color(192, 192, 0)), Sloan_g(41, "Sloan g", "SG",
			new Color(0, 64, 64)), Sloan_r(42, "Sloan r", "SR", new Color(128,
			64, 0)), Sloan_i(43, "Sloan i", "SI", new Color(192, 64, 0)), Cousins_I(
			5, "Cousins I", "I", new Color(255, 64, 0)), Tri_Color_Blue(50,
			"Tri-Color Blue", "TB", new Color(0, 0, 64)), Tri_Color_Green(51,
			"Tri-Color Green", "TG", new Color(0, 64, 0)), Tri_Color_Red(52,
			"Tri-Color Red", "TR", new Color(64, 0, 0)), Optec_Wing_A(55,
			"Optec Wing A", "MA", new Color(128, 64, 255)), Optec_Wing_B(56,
			"Optec Wing B", "MB", new Color(128, 64, 128)), Optec_Wing_C(57,
			"Optec Wing C", "MI", new Color(128, 0, 192)), Orange_Liller(6,
			"Orange (Liller)", "Orange", new Color(255, 128, 0)), Johnson_U(7,
			"Johnson U", "U", new Color(0, 255, 255)), Unfiltered_with_V_Zeropoint(
			8, "Unfiltered with V Zeropoint", "CV", new Color(0, 192, 0)), Unfiltered_with_Red_Zeropoint(
			9, "Unfiltered with Red Zeropoint", "CR", new Color(192, 0, 0)),

	// ** Auto-generated bands from aid.bands end here **

	FAINTER_THAN(SeriesType.NO_INDEX, "Fainter Than", "FainterThan",
			Color.YELLOW),

	MEANS(SeriesType.NO_INDEX, "Means", "Means", Color.BLUE),

	// Aaron's suggestion was to make Discrepant points light gray.
	DISCREPANT(SeriesType.NO_INDEX, "Discrepant", "Discrepant",
			Color.LIGHT_GRAY),

	Unspecified(SeriesType.NO_INDEX, "Unspecified", "Unspecified", Color.ORANGE),

	Filtered(SeriesType.NO_INDEX, "Filtered", "Filtered",
			new Color(0, 153, 204)),

	// Polynomial fit series.
	PolynomialFit(SeriesType.NO_INDEX, "Polynomial Fit", "Polynomial Fit",
			Color.RED),

	// Polynomial fit residuals series.
	Residuals(SeriesType.NO_INDEX, "Residuals", "Residuals", Color.CYAN),

	// This series can be used to mark an observation as being excluded for some
	// other reason than it being discrepant.
	Excluded(SeriesType.NO_INDEX, "Excluded", "Excluded", Color.DARK_GRAY);

	// Static members

	private final static int NO_INDEX = -1;

	private final static String PREFS_PREFIX = "SERIES_COLOR_";

	private static Map<Integer, SeriesType> index2SeriesMap = new HashMap<Integer, SeriesType>();
	private static Map<String, SeriesType> shortName2SeriesMap = new HashMap<String, SeriesType>();
	private static Map<String, SeriesType> description2SeriesMap = new HashMap<String, SeriesType>();
	private static Map<SeriesType, Color> series2ColorMap = new HashMap<SeriesType, Color>();

	private static Notifier<Map<SeriesType, Color>> seriesColorChangeNotifier = new Notifier<Map<SeriesType, Color>>();

	private static Preferences prefs;

	static {
		// Create preferences node for series colors.
		try {
			prefs = Preferences.userNodeForPackage(SeriesType.class);
		} catch (Throwable t) {
			// We need VStar to function in the absence of prefs.
		}

		// Populate mappings from index & name to type, and type to color.
		for (SeriesType type : values()) {
			index2SeriesMap.put(type.getIndex(), type);
			shortName2SeriesMap.put(type.getShortName(), type);
			description2SeriesMap.put(type.getDescription(), type);

			Color colorPref = getColorPref(type);
			series2ColorMap.put(type, colorPref == null ? type.getColor()
					: colorPref);
		}
	}

	/**
	 * @return The series color change notifier.
	 */
	public static Notifier<Map<SeriesType, Color>> getSeriesColorChangeNotifier() {
		return seriesColorChangeNotifier;
	}

	// Instance members per SeriesType value.
	private int index;
	private String description;
	private String shortName;
	private Color color;

	/**
	 * Private constructor.
	 * 
	 * @param index
	 *            The series type's index (AID.Code).
	 * @param description
	 *            The series type's description.
	 * @param shortName
	 *            The series type's short name (AID.bands).
	 * @param color
	 *            The series type's color.
	 */
	private SeriesType(int index, String description, String shortName,
			Color color) {
		this.index = index;
		this.description = description;
		this.shortName = shortName;
		this.color = color;
	}

	/**
	 * @return the index
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return the shortName
	 */
	public String getShortName() {
		return shortName;
	}

	/**
	 * @return the color
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * Map from AID band index to series type.
	 * 
	 * @param index
	 *            The integer band index
	 * @return The band, Unspecified if not found.
	 */
	public static SeriesType getSeriesFromIndex(int index) {
		SeriesType type = index2SeriesMap.get(index);

		if (type == null) {
			type = getDefault();
		}

		return type;
	}

	/**
	 * Map from short band description to series type.
	 * 
	 * @param shortName
	 *            The short description of the band.
	 * @return The band, Unspecified if not found.
	 */
	public static SeriesType getSeriesFromShortName(String shortName) {
		SeriesType type = shortName2SeriesMap.get(shortName);

		if (type == null) {
			// TODO: We can remove this block when we have changed or
			// downloaded new (or just deleted) files to replace existing
			// ones in the case where band short-names have changed!
			// Actually, we still see such names as V and B in formats like
			// AAVSO extended upload file format.
			if (shortName.equals("Unknown")) {
				type = Unknown;
			} else if (shortName.equals("Visual")) {
				type = Visual;
			} else if (shortName.equals("V")) {
				type = Johnson_V;
			} else if (shortName.equals("B")) {
				type = Johnson_B;
			} else if (shortName.equals("R")) {
				type = Cousins_R;
			} else if (shortName.equals("I")) {
				type = Cousins_I;
			}
		}

		if (type == null) {
			type = getDefault();
		}

		return type;
	}

	/**
	 * Map from band descriptive name to series type.
	 * 
	 * @param description
	 *            The descriptive description of the band.
	 * @return The band, Unspecified if not found.
	 */
	public static SeriesType getSeriesFromDescription(String description) {
		SeriesType type = description2SeriesMap.get(description);

		if (type == null) {
			type = getDefault();
		}

		return type;
	}

	private static Color getColorPref(SeriesType series) {
		Color color = null;

		if (series != null) {
			String colorPrefName = PREFS_PREFIX + series.getDescription();
			try {
				String colorPrefValue = prefs.get(colorPrefName, null);
				if (colorPrefValue != null) {
					// We expect this to be an integer RGB color value
					// but we need a way to distinguish between there
					// being no preference for the value and a valid
					// color RGB value which there is no way of doing
					// with a primitive integer.
					color = new Color(Integer.parseInt(colorPrefValue));
				}
			} catch (Throwable t) {
				// We need VStar to function in the absence of prefs.
			}
		}

		return color;
	}

	private static void setColorPref(SeriesType series, Color color) {
		if (series != null && color != null) {
			String colorPrefName = PREFS_PREFIX + series.getDescription();
			try {
				prefs.put(colorPrefName, color.getRGB() + "");
			} catch (Throwable t) {
				// We need VStar to function in the absence of prefs.
			}
		}
	}

	/**
	 * Given a series, retrieve its color.
	 * 
	 * @param series
	 *            The series in question.
	 * @return The corresponding color.
	 */
	public static Color getColorFromSeries(SeriesType series) {
		Color color = series2ColorMap.get(series);

		if (color == null) {
			color = getDefault().getColor();
		}

		return color;
	}

	/**
	 * Updates the series to color mapping according to the pairs in the
	 * supplied map. Note that this may be a subset of all series-color pairs,
	 * so it may not completely replace the existing map, just overwrite some
	 * pairs. It also updates the series color preference and notifies listeners
	 * of the change.
	 * 
	 * @param newSeries2ColorMap
	 *            The map with which to update the series-color map.
	 */
	public static void updateSeriesColorMap(
			Map<SeriesType, Color> newSeries2ColorMap) {

		if (!newSeries2ColorMap.isEmpty()) {
			for (SeriesType series : newSeries2ColorMap.keySet()) {
				Color color = newSeries2ColorMap.get(series);
				series2ColorMap.put(series, color);
				setColorPref(series, color);
			}

			try {
				prefs.flush();
			} catch (Throwable t) {
				// We need VStar to function in the absence of prefs.
			}

			seriesColorChangeNotifier.notifyListeners(newSeries2ColorMap);
		}
	}

	/**
	 * Restore the default series colors and notifies listeners.
	 */
	public static void setDefaultSeriesColors() {
		series2ColorMap.clear();

		try {
			prefs.clear();
		} catch (Throwable t) {
			// We need VStar to function in the absence of prefs.
		}

		for (SeriesType type : values()) {
			Color color = type.getColor();
			series2ColorMap.put(type, color);
			setColorPref(type, color);
		}

		try {
			prefs.flush();
		} catch (Throwable t) {
			// We need VStar to function in the absence of prefs.
		}

		seriesColorChangeNotifier.notifyListeners(series2ColorMap);
	}

	/**
	 * Returns the default series type. This is like the equivalent of null for
	 * this type.
	 * 
	 * @return The default series type.
	 */
	public static SeriesType getDefault() {
		return Unspecified;
	}

	/**
	 * We override toString() to return description rather than enum name.
	 */
	public String toString() {
		return this.getDescription();
	}
}
