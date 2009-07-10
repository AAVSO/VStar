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

import org.aavso.tools.vstar.data.SimpleFormatFieldInfoSource;
import org.aavso.tools.vstar.data.validation.ITableFieldInfoSource;

/**
 * A new star creation type. It also encodes the required number of fields for
 * each observation in the source, and acts as a Factory Method (GoF pattern)
 * for determining text format validator (simple or AAVSO download format), and
 * table column information.
 */
public enum NewStarType {

	// TODO: also create a NewStarInfo message class with GUI component refs for
	// DataPane so that created components can be passed to listeners rather
	// than the latter having to receive an event and then query the model
	// manager.

	NEW_STAR_FROM_SIMPLE_FILE(5, 5,
			SimpleFormatFieldInfoSource.simpleFormatFieldInfoSource,
			SimpleFormatFieldInfoSource.simpleFormatFieldInfoSource), NEW_STAR_FROM_DOWNLOAD_FILE(
			18, 18, null, null), NEW_STAR_FROM_DATABASE(18, 18, null, null);

	private final int minFields;
	private final int maxFields;
	private final ITableColumnInfoSource columnInfoSource;
	private final ITableFieldInfoSource fieldInfoSource;

	/**
	 * Constructor.
	 * 
	 * @param minFields
	 *            The minimum allowed number of fields.
	 * @param maxFields
	 *            The maximum allowed number of fields.
	 * @param columnInfoSource
	 *            An object that supplies information about fields and table
	 *            columns.
	 */
	private NewStarType(int minFields, int maxFields,
			ITableColumnInfoSource columnInfoSource,
			ITableFieldInfoSource fieldInfoSource) {
		this.minFields = minFields;
		this.maxFields = maxFields;
		this.columnInfoSource = columnInfoSource;
		this.fieldInfoSource = fieldInfoSource;
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

	/**
	 * @return the columnInfoSource
	 */
	public ITableColumnInfoSource getColumnInfoSource() {
		return columnInfoSource;
	}

	public ITableFieldInfoSource getFieldInfoSource() {
		return this.fieldInfoSource;
	}
}
