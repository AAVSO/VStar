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
	//	T = Discrepant (mapped to 'D' in SQL query, but may be present in text files)
	//	V = Good (passed AAVSO validation tests)
	//	Y = Deleted
	//	Z = Prevalidated 
	//
	// See https://sourceforge.net/apps/mediawiki/vstar/index.php?title=Valflag
	//
	// In older AAVSO download format files we see 'G' for "Good" instead of 'V'.
	// We could deprecate its use, but permitting it provides backward compatibility.
	//
	// According to http://www.aavso.org/data/download/downloadformat.shtml,
	// 'P' means "Pre-validated"; so there is a conflict between download
	// format and database originated validation flags. We assume this has
	// been mapped from 'P' to 'Z' in getTypeFromFlag() below.
	
	GOOD,
	DISCREPANT,
	PREVALIDATION;	
		
	/**
	 * Given a valflag from an input file or database, return
	 * the corresponding validation type.
	 */
	public static ValidationType getTypeFromFlag(String valflag) {
		ValidationType valtype = null;
		
		if ("G".equals(valflag)) {
			valtype = GOOD;
		} else if ("D".equals(valflag) || "T".equals(valflag)) {
			valtype = DISCREPANT;
		} else if ("P".equals(valflag)) {
			valtype = GOOD;
		} else if ("V".equals(valflag)) {
			valtype = GOOD;
		} else if ("Z".equals(valflag)) {
			valtype = PREVALIDATION;
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
			// Note that we choose the AAVSO Download format 'P' 
			// not database format 'Z' here. So long as we're using
			// this for saving files, this seems reasonable.
		case PREVALIDATION:
			str = "P";
			break;			
		}
		
		assert(str != null);
		
		return str;
	}
	
	/**
	 * Human readable validation type.
	 */
	public String toString() {
		String str = null;
		
		switch(this) {
		case GOOD:
			str = "Good";
			break;			
		case DISCREPANT:
			str = "Discrepant";
			break;			
		case PREVALIDATION:
			str = "Prevalidated";
			break;			
		}
		
		assert(str != null);
		
		return str;
	}
}
