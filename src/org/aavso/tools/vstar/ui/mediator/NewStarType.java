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
package org.aavso.tools.vstar.ui.mediator;

import org.aavso.tools.vstar.data.validation.AAVSOFormatFieldInfoSource;
import org.aavso.tools.vstar.data.validation.IFieldInfoSource;
import org.aavso.tools.vstar.data.validation.SimpleFormatFieldInfoSource;
import org.aavso.tools.vstar.ui.model.list.AAVSOFormatPhasePlotColumnInfoSource;
import org.aavso.tools.vstar.ui.model.list.AAVSOFormatRawDataColumnInfoSource;
import org.aavso.tools.vstar.ui.model.list.ExternalFormatPhasePlotColumnInfoSource;
import org.aavso.tools.vstar.ui.model.list.ExternalFormatRawDataColumnInfoSource;
import org.aavso.tools.vstar.ui.model.list.ITableColumnInfoSource;
import org.aavso.tools.vstar.ui.model.list.SimpleFormatPhasePlotColumnInfoSource;
import org.aavso.tools.vstar.ui.model.list.SimpleFormatRawDataColumnInfoSource;

/**
 * A new star creation type. It also encodes the required number of fields for
 * each observation in the source, and acts as a Factory Method (GoF pattern)
 * for determining text format validator (simple or AAVSO download format), and
 * table column information.
 */
public enum NewStarType {

	NEW_STAR_FROM_SIMPLE_FILE(2, SimpleFormatFieldInfoSource.FIELD_COUNT,
			SimpleFormatFieldInfoSource.instance),

	NEW_STAR_FROM_DOWNLOAD_FILE(AAVSOFormatFieldInfoSource.FIELD_COUNT - 3,
			AAVSOFormatFieldInfoSource.FIELD_COUNT,
			AAVSOFormatFieldInfoSource.instance),

	NEW_STAR_FROM_DATABASE,

	NEW_STAR_FROM_EXTERNAL_SOURCE;

	private final int minFields;
	private final int maxFields;
	private final IFieldInfoSource fieldInfoSource;

	/**
	 * Constructor.
	 * 
	 * @param minFields
	 *            The minimum allowed number of fields.
	 * @param maxFields
	 *            The maximum allowed number of fields.
	 * @param fieldInfoSource
	 *            An object that provides information about text format fields.
	 *            May be null for new-star-from-database.
	 */
	private NewStarType(int minFields, int maxFields,
			IFieldInfoSource fieldInfoSource) {
		this.minFields = minFields;
		this.maxFields = maxFields;
		this.fieldInfoSource = fieldInfoSource;
	}

	/**
	 * Constructor.
	 * 
	 * No min or max fields or field information source.
	 */
	private NewStarType() {
		this(0, 0, null);
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
		ITableColumnInfoSource source = null;
		
		switch (this) {
		case NEW_STAR_FROM_DATABASE:
			source = new AAVSOFormatRawDataColumnInfoSource(false);
			break;
		case NEW_STAR_FROM_DOWNLOAD_FILE:
			source = new AAVSOFormatRawDataColumnInfoSource(true);
			break;
		case NEW_STAR_FROM_SIMPLE_FILE:
			source = new SimpleFormatRawDataColumnInfoSource();
			break;
		case NEW_STAR_FROM_EXTERNAL_SOURCE:
			source = new ExternalFormatRawDataColumnInfoSource();   
			break;
		}
		
		return source;
	}

	/**
	 * @return the phasePlotTableColumnInfoSource
	 */
	public ITableColumnInfoSource getPhasePlotTableColumnInfoSource() {
		ITableColumnInfoSource source = null;
		
		switch (this) {
		case NEW_STAR_FROM_DATABASE:
			source = new AAVSOFormatPhasePlotColumnInfoSource(false);
			break;
		case NEW_STAR_FROM_DOWNLOAD_FILE:
			source = new AAVSOFormatPhasePlotColumnInfoSource(true);
			break;
		case NEW_STAR_FROM_SIMPLE_FILE:
			source = new SimpleFormatPhasePlotColumnInfoSource();
			break;
		case NEW_STAR_FROM_EXTERNAL_SOURCE:
			source = new ExternalFormatPhasePlotColumnInfoSource();   
			break;
		}
		
		return source;
	}

	public IFieldInfoSource getFieldInfoSource() {
		return this.fieldInfoSource;
	}
	
	public String toString() {
		String str = "";
		
		switch (this) {
		case NEW_STAR_FROM_DATABASE:
			str = "AAVSO International Database";
			break;
		case NEW_STAR_FROM_DOWNLOAD_FILE:
			str = "AAVSO Download Format File";
			break;
		case NEW_STAR_FROM_SIMPLE_FILE:
			str = "Simple Format File";
			break;
		case NEW_STAR_FROM_EXTERNAL_SOURCE:
			str = "External Source";
			break;
		}
		
		return str;
	}
}
