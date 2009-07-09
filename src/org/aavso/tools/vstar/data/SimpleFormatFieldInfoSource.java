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

import org.aavso.tools.vstar.ui.model.ITableColumnInfoSource;

/**
 * Simple file format field information source.
 */
public class SimpleFormatFieldInfoSource implements ITableColumnInfoSource {

	// Text format fields.
	public static final int JD_FIELD = 0;
	public static final int MAGNITUDE_FIELD = 1;
	public static final int UNCERTAINTY_FIELD = 2;
	public static final int OBSERVER_CODE_FIELD = 3;
	public static final int VALFLAG_FIELD = 4;

	// Table columns.
	public static final int JD_COLUMN = 0;
	public static final int CALENDAR_DATE_COLUMN = 1;
	public static final int MAGNITUDE_COLUMN = 2;
	public static final int OBSERVER_CODE_COLUMN = 3;
	public static final int LINE_NUM_COLUMN = 4;
	public static final int DISCREPANT_COLUMN = 5;
	
	private static final int COLUMNS = 6;

	public int getColumnCount() {
		return COLUMNS;
	}

	public int getDiscrepantColumnIndex() {
		return DISCREPANT_COLUMN;
	}

	public String getTableColumnTitle(int index) {
		String columnName = null;

		switch (index) {
		case JD_COLUMN:
			columnName = "Julian Day";
			break;
		case CALENDAR_DATE_COLUMN:
			columnName = "Calendar Date";
			break;
		case MAGNITUDE_COLUMN:
			columnName = "Magnitude";
			break;
		case OBSERVER_CODE_COLUMN:
			columnName = "Observer Code";
			break;
		case LINE_NUM_COLUMN:
			columnName = "Line";
			break;
		case DISCREPANT_COLUMN:
			columnName = "Discrepant?";
			break;
		}

		return columnName;
	}

	public Object getTableColumnValue(int index, ValidObservation ob) {
		Object value = null;
		
		switch (index) {
		case JD_COLUMN:
			value = ob.getDateInfo().getJulianDay();
			break;
		case CALENDAR_DATE_COLUMN:
			value = ob.getDateInfo().getCalendarDate();
			break;
		case MAGNITUDE_COLUMN:
			value = ob.getMagnitude().toString();
			break;
		case OBSERVER_CODE_COLUMN:
			value = ob.getObsCode();
			break;
		case LINE_NUM_COLUMN:
			value = ob.getLineNumber();
			break;
		case DISCREPANT_COLUMN:
			value = ob.isDiscrepant();
			break;
		}

		return value;
	}
}
