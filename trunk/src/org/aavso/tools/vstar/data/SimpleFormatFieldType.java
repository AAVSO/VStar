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
public enum SimpleFormatFieldType {
	
	JD(0),
	MAGNITUDE(1),
	UNCERTAINTY(2),
	OBSERVER_CODE(3),
	VALFLAG(4);
	
	// Index associated with current enum value.
	private int index;
	
	/**
	 * Private constructor.
	 * @param index The index associated with current enum value.
	 */
	private SimpleFormatFieldType(int index) {
		this.index = index;
	}

	/**
	 * @return the index
	 */
	public int getIndex() {
		return index;
	}
}
