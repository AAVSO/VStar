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

import org.aavso.tools.vstar.data.validation.IFieldInfoSource;
import org.aavso.tools.vstar.data.validation.SimpleFormatFieldInfoSource;
import org.aavso.tools.vstar.data.validation.AAVSOFormatFieldInfoSource;

/**
 * A new star creation type. It also encodes the required number of fields for
 * each observation in the source, and acts as a Factory Method (GoF pattern)
 * for determining text format validator (simple or AAVSO download format), and
 * table column information.
 */
public enum NewStarType {

	NEW_STAR_FROM_SIMPLE_FILE(2, SimpleFormatFieldInfoSource.FIELD_COUNT,
			SimpleFormatRawDataColumnInfoSource.instance,
			SimpleFormatPhasePlotColumnInfoSource.instance,
			SimpleFormatFieldInfoSource.instance),

	NEW_STAR_FROM_DOWNLOAD_FILE(AAVSOFormatFieldInfoSource.FIELD_COUNT - 3,
			AAVSOFormatFieldInfoSource.FIELD_COUNT,
			AAVSOFormatRawDataColumnInfoSource.fileInstance,
			AAVSOFormatPhasePlotColumnInfoSource.fileInstance,
			AAVSOFormatFieldInfoSource.instance),

	NEW_STAR_FROM_DATABASE(AAVSOFormatRawDataColumnInfoSource.databaseInstance,
			AAVSOFormatPhasePlotColumnInfoSource.databaseInstance);

	private final int minFields;
	private final int maxFields;
	private final ITableColumnInfoSource rawDataTableColumnInfoSource;
	private final ITableColumnInfoSource phasePlotTableColumnInfoSource;
	private final IFieldInfoSource fieldInfoSource;

	/**
	 * Constructor.
	 * 
	 * @param minFields
	 *            The minimum allowed number of fields.
	 * @param maxFields
	 *            The maximum allowed number of fields.
	 * @param rawDataTableColumnInfoSource
	 *            An object that supplies information about raw data table
	 *            columns.
	 * @param phasePlotTableColumnInfoSource
	 *            An object that supplies information about phase plot table
	 *            columns.
	 * @param fieldInfoSource
	 *            An object that provides information about text format fields.
	 *            May be null for new-star-from-database.
	 */
	private NewStarType(int minFields, int maxFields,
			ITableColumnInfoSource rawDataTableColumnInfoSource,
			ITableColumnInfoSource phasePlotTableColumnInfoSource,
			IFieldInfoSource fieldInfoSource) {
		this.minFields = minFields;
		this.maxFields = maxFields;
		this.rawDataTableColumnInfoSource = rawDataTableColumnInfoSource;
		this.phasePlotTableColumnInfoSource = phasePlotTableColumnInfoSource;
		this.fieldInfoSource = fieldInfoSource;
	}

	/**
	 * Constructor.
	 * 
	 * No min or max fields or field information source.
	 * 
	 * @param rawDataTableColumnInfoSource
	 *            An object that supplies information about raw data table
	 *            columns.
	 * @param phasePlotTableColumnInfoSource
	 *            An object that supplies information about phase plot table
	 *            columns.
	 */
	private NewStarType(ITableColumnInfoSource rawDataTableColumnInfoSource,
			ITableColumnInfoSource phasePlotTableColumnInfoSource) {
		this(0, 0, rawDataTableColumnInfoSource,
				phasePlotTableColumnInfoSource, null);
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
	 * @return the rawDataTableColumnInfoSource
	 */
	public ITableColumnInfoSource getRawDataTableColumnInfoSource() {
		return rawDataTableColumnInfoSource;
	}

	/**
	 * @return the phasePlotTableColumnInfoSource
	 */
	public ITableColumnInfoSource getPhasePlotTableColumnInfoSource() {
		return phasePlotTableColumnInfoSource;
	}

	public IFieldInfoSource getFieldInfoSource() {
		return this.fieldInfoSource;
	}
}
