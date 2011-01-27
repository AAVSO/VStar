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
package org.aavso.tools.vstar.ui.model.list;

import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.util.prefs.NumericPrecisionPrefs;

/**
 * Simple file format raw data table column information source.
 * 
 * TODO: can we instead inherit from RawData class and specialise each switch
 * function?
 */
public class SimpleFormatRawDataColumnInfoSource implements
		ITableColumnInfoSource {

	public static SimpleFormatRawDataColumnInfoSource instance = new SimpleFormatRawDataColumnInfoSource();

	// Table columns.
	private static final int JD_COLUMN = 0;
	private static final int CALENDAR_DATE_COLUMN = 1;
	private static final int MAGNITUDE_COLUMN = 2;
	private static final int OBSERVER_CODE_COLUMN = 3;
	private static final int LINE_NUM_COLUMN = 4;
	private static final int DISCREPANT_COLUMN = 5;

	public int getColumnCount() {
		return DISCREPANT_COLUMN + 1;
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

	public Class<?> getTableColumnClass(int index) {
		Class<?> clazz = String.class;

		switch (index) {
		case JD_COLUMN:
			break;
		case CALENDAR_DATE_COLUMN:
			break;
		case MAGNITUDE_COLUMN:
			break;
		case OBSERVER_CODE_COLUMN:
			break;
		case LINE_NUM_COLUMN:
			clazz = Integer.class;
			break;
		case DISCREPANT_COLUMN:
			clazz = Boolean.class;
			break;
		}

		return clazz;
	}

	public Object getTableColumnValue(int index, ValidObservation ob) {
		Object value = null;

		switch (index) {
		case JD_COLUMN:
			value = String.format(NumericPrecisionPrefs.getTimeOutputFormat(), ob
					.getDateInfo().getJulianDay());
			break;
		case CALENDAR_DATE_COLUMN:
			value = ob.getDateInfo().getCalendarDate();
			break;
		case MAGNITUDE_COLUMN:
			value = ob.getMagnitude();
			break;
		case OBSERVER_CODE_COLUMN:
			value = ob.getObsCode();
			break;
		case LINE_NUM_COLUMN:
			value = ob.getRecordNumber();
			break;
		case DISCREPANT_COLUMN:
			value = ob.isDiscrepant();
			break;
		}

		return value;
	}
}
