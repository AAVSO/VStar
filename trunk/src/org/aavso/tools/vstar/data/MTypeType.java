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
 * This type enumerates valid mtype (magnitude type) values.
 * 
 * Yes folks, it really is MTypeType. I'm not repeating myself.
 * 
 * See also http://www.aavso.org/data/download/downloadformat.shtml
 */
public enum MTypeType {

	// Magnitude Type: describes the kind of magnitude used in an
	// observation.

	// Standard magnitude.
	STD,
	// Differential magnitude; value of comparison star 1 needed to
	// compute standard magnitude.
	DIFF,
	// Non-reduced step magnitude; given as 0.0 and the step sequence
	// may be found in the Comment Code field.
	STEP;
	
	public String toString() {
		String str = "";
		
		switch(this) {
		case STD:
			str = "Standard";
			break;
		case DIFF:
			str = "Differential";
			break;
		case STEP:
			str = "Step";
			break;
		}
		
		return str;
	}
}
