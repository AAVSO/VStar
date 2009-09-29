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
	
	// Note: from database we can have:
	// 
	//	P = Published observation (should be treated as Good)
	//	T = Discrepant (mapped to 'D' in SQL query)
	//	V = Good 
	//	Y = Deleted
	//	Z = Prevalidated 
	//
	// See https://sourceforge.net/apps/mediawiki/vstar/index.php?title=Valflag:
	//
	// In AAVSO download format files we also see 'G' for "Good".
	
	GOOD,
	DISCREPANT,
	PREVALIDATION,
	DELETED;	
	
	/**
	 * Given a valflag from an input file or database, return
	 * the corresponding validation type.
	 */
	public static ValidationType getTypeFromFlag(String valflag) {
		ValidationType valtype = null;
		
		if ("G".equals(valflag)) {
			valtype = GOOD; // passed AAVSO validation tests
		} else if ("D".equals(valflag)) {
			valtype = DISCREPANT;
		} else if ("P".equals(valflag)) {
			valtype = GOOD;
		} else if ("V".equals(valflag)) {
			valtype = GOOD;
		} else if ("Z".equals(valflag)) {
			valtype = PREVALIDATION;
		} else if ("Y".equals(valflag)) {
			valtype = DELETED;
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
		case GOOD:
			str = "G";
			break;			
		case DISCREPANT:
			str = "D";
			break;			
		case PREVALIDATION:
			str = "P";
			break;			
		case DELETED:
			str = "Y";
			break;			
		}
		
		assert(str != null);
		
		return str;
	}
	
	// TODO: add toString() for human readability
}
