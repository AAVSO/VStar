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
package org.aavso.tools.vstar.data;

/**
 * Observation type, used in AAVSO sources.
 */
public enum ObsType {

	Visual(1),
	CCD(2),
	PEP(3),
	WEDGE_PHOTOMETER(4),
	PTG(5), 
	DSLR(6),
	VISDIG(7),
	UNKNOWN(8);
	
	private int index;
	
	public static ObsType getObsTypeFromAIDIndex(int index) {
		ObsType type = null;

		switch(index) {
		case 1:
			type = Visual;
			break;
		case 2:
			type = CCD;
			break;
		case 3:
			type = PEP;
			break;
		case 4:
			type = WEDGE_PHOTOMETER;
			break;
		case 5:
			type = PTG;
			break;
		case 6:
			type = DSLR;
			break;
		case 7:
			type = VISDIG;
			break;
		case 8:
			type = UNKNOWN;
			break;
		default:
			type = UNKNOWN;
			break;
		}
		
		return type;
	}
	
	private ObsType(int index) {
		this.index = index;
	}

	/**
	 * @return the index
	 */
	public int getIndex() {
		return index;
	}
	
	public String getDescription() {
		String desc = null;
		switch(index) {
		case 1:
			desc = "Visual";
			break;
		case 2:
			desc = "CCD";
			break;
		case 3:
			desc = "PEP";
			break;
		case 4:
			desc = "Wedge Photometer";
			break;
		case 5:
			desc = "PTG";
			break;
		case 6:
			desc = "DSLR";
			break;
		case 7:
			desc = "VISDIG";
			break;
		case 8:
			desc = "Unknown";
			break;
		default:
			desc = "Unknown";
			break;
		}
		return desc;
	}
}
