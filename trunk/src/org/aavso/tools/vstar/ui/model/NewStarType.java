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

/**
 * A new star creation type. It also encodes the required number of fields for
 * each observation in the source.
 */
public enum NewStarType {
	
	// TODO: also create a NewStarInfo message class with GUI component refs for DataPane
	
	NEW_STAR_FROM_SIMPLE_FILE(5,5), NEW_STAR_FROM_DOWNLOAD_FILE(18,18), NEW_STAR_FROM_DATABASE(
			18,18);

	private int minFields;
	private int maxFields;

	/**
	 * Constructor.
	 * 
	 * @param minFields
	 *            The minimum allowed number of fields.
	 * @param maxFields
	 *            The maximum allowed number of fields.
	 */
	private NewStarType(int minFields, int maxFields) {
		this.minFields = minFields;
		this.maxFields = maxFields;
	}

	/**
	 * @return the minFields
	 */
	public int getMinFields() {
		return minFields;
	}

	/**
	 * @return the maxFields
	 */
	public int getMaxFields() {
		return maxFields;
	}
}
