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

import java.util.HashMap;
import java.util.Map;

import org.aavso.tools.vstar.data.validation.ITableFieldInfoSource;
import org.aavso.tools.vstar.ui.model.ITableColumnInfoSource;

/**
 * Simple file format field information source.
 */
public class SimpleFormatFieldInfoSource implements ITableColumnInfoSource,
		ITableFieldInfoSource {

	public final static SimpleFormatFieldInfoSource simpleFormatFieldInfoSource = new SimpleFormatFieldInfoSource();

	// Text format fields.
	private static final int JD_FIELD = 0;
	private static final int MAGNITUDE_FIELD = 1;
	private static final int UNCERTAINTY_FIELD = 2;
	private static final int OBSERVER_CODE_FIELD = 3;
	private static final int VALFLAG_FIELD = 4;

	public static final int FIELD_COUNT = VALFLAG_FIELD+1;
	
	// Table columns.
	private static final int JD_COLUMN = 0;
	private static final int CALENDAR_DATE_COLUMN = 1;
	private static final int MAGNITUDE_COLUMN = 2;
	private static final int OBSERVER_CODE_COLUMN = 3;
	private static final int LINE_NUM_COLUMN = 4;
	private static final int DISCREPANT_COLUMN = 5;

	private static final int COLUMNS = DISCREPANT_COLUMN + 1;

	private Map<String, Integer> fieldIndexMap;

	/**
	 * Constructor.
	 */
	public SimpleFormatFieldInfoSource() {
		this.fieldIndexMap = new HashMap<String, Integer>();
		this.fieldIndexMap.put("JD_FIELD", JD_FIELD);
		this.fieldIndexMap.put("MAGNITUDE_FIELD", MAGNITUDE_FIELD);
		this.fieldIndexMap.put("UNCERTAINTY_FIELD", UNCERTAINTY_FIELD);
		this.fieldIndexMap.put("OBSERVER_CODE_FIELD", OBSERVER_CODE_FIELD);
		this.fieldIndexMap.put("VALFLAG_FIELD", VALFLAG_FIELD);
	}

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

	public Class<?> getTableColumnClass(int index) {
		Class<?> clazz = String.class;

		switch (index) {
		case JD_COLUMN:
			clazz = String.class; 
			break;
		case CALENDAR_DATE_COLUMN:
			break;
		case MAGNITUDE_COLUMN:
			break;
		case OBSERVER_CODE_COLUMN:
			break;
		case LINE_NUM_COLUMN:
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
			value = String.format("%1.2f", ob.getDateInfo().getJulianDay());
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

	public Map<String, Integer> getFieldIndexMap() {
		return this.fieldIndexMap;
	}
}
