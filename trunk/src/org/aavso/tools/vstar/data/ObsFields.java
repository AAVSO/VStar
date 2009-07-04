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
 * Observation fields.
 */
public enum ObsFields {
	
	JD(0),
	MAGNITUDE(1),
	UNCERTAINTY(2),
	HQ_UNCERTAINTY(3),
	BAND(4),
	OBSERVER_CODE(5),
	COMMENT_CODE(6),
	COMP_STAR_1(7),
	COMP_STAR_2(8),
	CHARTS(9),
	COMMENTS(10),
	TRANSFORMED(11),
	AIRMASS(12),
	VALFLAG(13),
	CMAG(14),
	KMAG(15),
	HJD(16),
	NAME(17);
	
	// Index associated with current enum value.
	private int index;
	
	/**
	 * Private constructor.
	 * @param index The index associated with current enum value.
	 */
	private ObsFields(int index) {
		this.index = index;
	}

	/**
	 * @return the index
	 */
	public int getIndex() {
		return index;
	}
}
