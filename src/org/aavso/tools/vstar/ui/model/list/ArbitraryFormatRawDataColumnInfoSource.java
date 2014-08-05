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

import java.util.HashMap;
import java.util.Map;

import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.util.prefs.NumericPrecisionPrefs;

/**
 * Arbitrary raw data format table column information source, associated with
 * observation source plugins.
 */
public class ArbitraryFormatRawDataColumnInfoSource implements
		ITableColumnInfoSource {

	// Table columns.
	private static final int JD_COLUMN = 0;
	private static final int CALENDAR_DATE_COLUMN = 1;
	private static final int MAGNITUDE_COLUMN = 2;
	private static final int UNCERTAINTY_COLUMN = 3;
	private static final int BAND_COLUMN = 4;
	private static final int RECORD_NUM_COLUMN = 5;
	private static final int DISCREPANT_COLUMN = 6;

	private static final String JD_COLUMN_NAME = "Julian Day";
	private static final String CALENDAR_DATE_COLUMN_NAME = "Calendar Date";
	private static final String MAGNITUDE_COLUMN_NAME = "Magnitude";
	private static final String UNCERTAINTY_COLUMN_NAME = "Uncertainty";
	private static final String BAND_COLUMN_NAME = "Band";
	private static final String RECORD_NUM_COLUMN_NAME = "Record";
	private static final String DISCREPANT_COLUMN_NAME = "Discrepant?";

	protected static final Map<String, Integer> COLUMN_NAMES = new HashMap<String, Integer>();

	static {
		COLUMN_NAMES.put(JD_COLUMN_NAME, JD_COLUMN);
		COLUMN_NAMES.put(CALENDAR_DATE_COLUMN_NAME, CALENDAR_DATE_COLUMN);
		COLUMN_NAMES.put(MAGNITUDE_COLUMN_NAME, MAGNITUDE_COLUMN);
		COLUMN_NAMES.put(UNCERTAINTY_COLUMN_NAME, UNCERTAINTY_COLUMN);
		COLUMN_NAMES.put(BAND_COLUMN_NAME, BAND_COLUMN);
		COLUMN_NAMES.put(RECORD_NUM_COLUMN_NAME, RECORD_NUM_COLUMN);
		COLUMN_NAMES.put(DISCREPANT_COLUMN_NAME, DISCREPANT_COLUMN);
	}

	public int getColumnCount() {
		int detailCount = ValidObservation.getDetailTitles().size();
		return DISCREPANT_COLUMN + detailCount + 1;
	}

	public int getDiscrepantColumnIndex() {
		return DISCREPANT_COLUMN;
	}

	public String getTableColumnTitle(int index) {
		String columnName = null;

		switch (index) {
		case JD_COLUMN:
			columnName = JD_COLUMN_NAME;
			break;
		case CALENDAR_DATE_COLUMN:
			columnName = CALENDAR_DATE_COLUMN_NAME;
			break;
		case MAGNITUDE_COLUMN:
			columnName = MAGNITUDE_COLUMN_NAME;
			break;
		case UNCERTAINTY_COLUMN:
			columnName = UNCERTAINTY_COLUMN_NAME;
			break;
		case BAND_COLUMN:
			columnName = BAND_COLUMN_NAME;
			break;
		case RECORD_NUM_COLUMN:
			columnName = RECORD_NUM_COLUMN_NAME;
			break;
		case DISCREPANT_COLUMN:
			columnName = DISCREPANT_COLUMN_NAME;
			break;
		default:
			String key = ValidObservation.getDetailKey(index
					- DISCREPANT_COLUMN - 1);
			columnName = ValidObservation.getDetailTitles().get(key);
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
		case UNCERTAINTY_COLUMN:
			break;
		case BAND_COLUMN:
			break;
		case RECORD_NUM_COLUMN:
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
			value = NumericPrecisionPrefs.formatTime(ob.getDateInfo().getJulianDay());
			break;
		case CALENDAR_DATE_COLUMN:
			value = ob.getDateInfo().getCalendarDate();
			break;
		case MAGNITUDE_COLUMN:
			value = NumericPrecisionPrefs.formatMag(ob.getMagnitude().getMagValue());
			break;
		case UNCERTAINTY_COLUMN:
			value = NumericPrecisionPrefs.formatMag(ob.getMagnitude().getUncertainty());
			break;
		case BAND_COLUMN:
			value = ob.getBand() == null ? "" : ob.getBand().getDescription();
			break;
		case RECORD_NUM_COLUMN:
			value = ob.getRecordNumber();
			break;
		case DISCREPANT_COLUMN:
			value = ob.isDiscrepant();
			break;
		default:
			String key = ValidObservation.getDetailKey(index
					- DISCREPANT_COLUMN - 1);
			value = ob.getDetails().get(key);
			break;
		}

		return value;
	}

	@Override
	public int getColumnIndexByName(String name)
			throws IllegalArgumentException {
		int index = 0;

		if (name == null) {
			throw new IllegalArgumentException("Null column name");
		} else if (COLUMN_NAMES.containsKey(name)) {
			index = COLUMN_NAMES.get(name);
		} else if (ValidObservation.getDetailTitles().containsKey(name)) {
			index = ValidObservation.getDetailIndex(name) + DISCREPANT_COLUMN
					+ 1;
		} else {
			throw new IllegalArgumentException("No column name: " + name);
		}

		return index;
	}
}
