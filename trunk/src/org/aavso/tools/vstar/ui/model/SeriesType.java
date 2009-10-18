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
package org.aavso.tools.vstar.ui.model;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

/**
 * A type for bands and other series types, e.g. fainter-thans, means.
 */
public enum SeriesType {

	// From http://www.aavso.org/data/ql/bandhelp.shtml
	// TODO: This group and the next and any others missed from either
	// (see wiki re: bands for different input sources) need to become
	// one.
	UNKNOWN(-1, "Unknown", Color.BLACK),
	VISUAL(-1, "Visual", Color.BLACK),
	V(-1, "V", Color.BLACK),
	B(-1, "B", Color.BLACK),
	R(-1, "R", Color.BLACK),
	I(-1, "I", Color.BLACK),
	
	// From http://www.aavso.org/vstarwiki/index.php/Bands
	// The indices here correspond to database band numbers.
	// TODO: fix the colors (these are not currently used)!
	Visual(0, "Visual", Color.BLACK),
	Unknown(1, "Unknown", Color.BLACK),
	Johnson_V(2, "Johnson V", Color.BLACK),
	Johnson_B(3, "Johnson B", Color.BLACK),
	Cousins_R(4, "Cousins R", Color.BLACK),
	Cousins_I(5, "Cousins I", Color.BLACK),
	Orange(6, "Orange", Color.BLACK),
	Johnson_U(7, "Johnson U", Color.BLACK),
	Unfiltered_with_V_Zeropoint(8, "Unfiltered with V Zeropoint", Color.BLACK),
	Unfiltered_with_Red_Zeropoint(9, "Unfiltered with Red Zeropoint", Color.BLACK),
	blue(21, "blue", Color.BLACK),
	green(22, "green", Color.BLACK),
	red(23, "red", Color.BLACK),
	yellow(24, "yellow", Color.BLACK),
	Sloan_Z(29, "SloanZ", Color.BLACK),
	K_NIR_2pt2micron(26, "K_NIR_2.2micron", Color.BLACK),
	H_NIR_1pt6micron(27, "H NIR 16micron", Color.BLACK),
	J_NIR_1pt2micron(28, "J NIR 12micron", Color.BLACK),
	Stromgren_u(30, "Stromgren u", Color.BLACK),
	Stromgren_v(31, "Stromgren v", Color.BLACK),
	Stromgren_b(32, "Stromgren b", Color.BLACK),
	Stromgren_y(33, "Stromgreny", Color.BLACK),
	Stromgren_Hb21_H_beta_wide(34, "Stromgren Hbw (H beta wide)", Color.BLACK),
	Stromgren_Hbn_H_bet_narrow(35, "Stromgren Hbn (H bet narrow)", Color.BLACK),

	FAINTER_THAN(-1, "Fainter Than", Color.BLACK),
	MEANS(-1, "Means", Color.BLACK),
	DISCREPANT(-1, "Discrepant", Color.LIGHT_GRAY), // Aaron's suggestion
	
	Unspecified(-1, "Unspecified", Color.BLACK);

	// TODO: populate this with canonical band representations
	private static Map<Integer, String> index2NameMap = new HashMap<Integer, String>();
	
	private int index;
	private String name;
	private Color color;
	
	private SeriesType(int index, String name, Color color) {
		this.index = index;
		this.name =  name;
		this.color = color;
	}

	/**
	 * @return the index
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the color
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * Map from (database) band index to band name.
	 * TODO: should eventually return enum value
	 * 
	 * @param index The integer band index
	 * @return The band name or "Unspecified".
	 */
	public static String getNameFromIndex(int index) {
		String name = Unspecified.getName();
		
		for (SeriesType type : values()) {
			if (type.getIndex() == index) {
				name = type.getName();
				break;
			}
		}
		
		return name;
	}
}
