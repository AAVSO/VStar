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

// TODO:
// - Note that this enum-based approach won't necessarily work once we
//   try to extend VStar to accept other sources, e.g. photometrica, so...
// - Eventually change this enum so that it starts out with a default set
//   of series enum values but when connected to AID, it "refreshes" that
//   Set<SeriesType> dynamically. Or just live with periodic code-generated
//   updates to this class's enums. Other possibilities?

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
			"Tri-Color Red", "TR", new Color(64, 0, 0)), Orange_Liller(6,
			"Orange (Liller)", "Orange", new Color(255, 128, 0)), Johnson_U(7,
			"Johnson U", "U", new Color(0, 255, 255)), Unfiltered_with_V_Zeropoint(
			8, "Unfiltered with V Zeropoint", "CV", new Color(0, 192, 0)), Unfiltered_with_Red_Zeropoint(
			9, "Unfiltered with Red Zeropoint", "CR", new Color(192, 0, 0)),

	// ** Auto-generated bands from aid.bands end here **

	// Aaron's suggestion was to make Discrepant points light gray.
	// TODO: change dark gray to this below once chart background is white?

	FAINTER_THAN(-1, "Fainter Than", Color.YELLOW), MEANS(-1, "Means",
			Color.BLUE), DISCREPANT(-1, "Discrepant", Color.DARK_GRAY),

	Unspecified(-1, "Unspecified", Color.ORANGE);

	private static Map<Integer, SeriesType> index2SeriesMap = new HashMap<Integer, SeriesType>();
	private static Map<String, SeriesType> shortName2SeriesMap = new HashMap<String, SeriesType>();
	private static Map<String, SeriesType> description2SeriesMap = new HashMap<String, SeriesType>();

	static {
		for (SeriesType type : values()) {
			index2SeriesMap.put(type.getIndex(), type);
			shortName2SeriesMap.put(type.getShortName(), type);
			description2SeriesMap.put(type.getDescription(), type);
		}
	}

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
	 * Private constructor. TODO: remove me!
	 * 
	 * @param index
	 *            The series type's index (AID.Code).
	 * @param description
	 *            The series type's description.
	 * @param color
	 *            The series type's color.
	 */
	private SeriesType(int index, String description, Color color) {
		this(index, description, "", color);
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
			// downloaded new files to replace existing ones in the case 
			// where band short-names have changed!
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
