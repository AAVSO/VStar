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

/**
 * This enum represents "validation flag" found in all data sources.
 */
public enum ValidationType {
	DISCREPANT;
	// TODO: add more (for AAVSO format)
	
	/**
	 * Given a valflag from an input file or database, return
	 * the corresponding validation type.
	 */
	public static ValidationType getTypeFromFlag(String valflag) {
		ValidationType valtype = null;
		
		if ("D".equals(valflag)) {
			valtype = DISCREPANT;
		}
		
		assert(valtype != null);
		
		return valtype;
	}
	
	/**
	 * Return the valflag string corresponding to this this enum value.
	 */
	public String getValflag() {
		String str = null;
		switch(this) {
		case DISCREPANT:
			str = "D";
			break;			
		}
		
		assert(str != null);
		
		return str;
	}
}
