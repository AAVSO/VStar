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
package org.aavso.tools.vstar.plugin.ob.src.impl;

/**
 * The source of an AAVSO photometry observation.
 */
public enum AAVSOPhotometrySource {

	SRO35(18), BSM_NM(28), W28(30), W30(31), K35(35), BSM_S(37), BSM_Hamren(40), BSM_Berry(
			41), BSM_HQ(45), Unknown(0);

	private int id;

	private AAVSOPhotometrySource(int id) {
		this.id = id;
	}

	/**
	 * Given a numeric ID, return the enumerated value.
	 * 
	 * @param id
	 *            The numeric ID of the source.
	 * @return The source as an enumerated type.
	 */
	public static AAVSOPhotometrySource fromID(int id) {
		AAVSOPhotometrySource source = null;

		switch (id) {
		case 18:
			source = SRO35;
			break;
		case 28:
			source = BSM_NM;
			break;
		case 30:
			source = W28;
			break;
		case 31:
			source = W30;
			break;
		case 35:
			source = K35;
			break;
		case 37:
			source = BSM_S;
			break;
		case 40:
			source = BSM_Hamren;
			break;
		case 41:
			source = BSM_Berry;
			break;
		case 45:
			source = BSM_HQ;
			break;
		default:
			source = Unknown;
			break;
		}

		return source;
	}
}
