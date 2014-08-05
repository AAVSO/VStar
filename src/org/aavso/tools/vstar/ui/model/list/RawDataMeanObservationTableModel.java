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

import java.util.List;

import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.util.prefs.NumericPrecisionPrefs;

/**
 * This class is a table model for mean observation data for a raw data view.
 * 
 * The model is notified of wholesale mean data change.
 */
@SuppressWarnings("serial")
public class RawDataMeanObservationTableModel extends
		AbstractMeanObservationTableModel {

	private static final int JD_COLUMN = 0;
	private static final int CALDATE_COLUMN = 1;
	private static final int MEAN_COLUMN = 2;
	private static final int STDERR_COLUMN = 3;

	private static final int COLUMN_COUNT = 4;

	/**
	 * Constructor.
	 * 
	 * @param obs
	 *            The mean initial observation data. The mean data can be
	 *            updated later via this class's listener interface.
	 */
	public RawDataMeanObservationTableModel(List<ValidObservation> meanObsData) {
		super(meanObsData);
	}

	/**
	 * @see org.aavso.tools.vstar.ui.model.list.AbstractMeanObservationTableModel#getColumnCount()
	 */
	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	/**
	 * @see org.aavso.tools.vstar.ui.model.list.AbstractMeanObservationTableModel#getRowCount()
	 */
	public int getRowCount() {
		return obs.size();
	}

	/**
	 * @see org.aavso.tools.vstar.ui.model.list.AbstractMeanObservationTableModel#getColumnName(int)
	 */
	public String getColumnName(int column) {
		assert column < COLUMN_COUNT;

		String columnName = null;

		switch (column) {
		case JD_COLUMN:
			columnName = "Julian Day";
			break;
		case CALDATE_COLUMN:
			columnName = "Calendar Date";
			break;
		case MEAN_COLUMN:
			columnName = "Mean Magnitude";
			break;
		case STDERR_COLUMN:
			columnName = "Standard Error of the Average";
			break;
		}

		return columnName;
	}

	/**
	 * @see org.aavso.tools.vstar.ui.model.list.AbstractMeanObservationTableModel#getValueAt(int,
	 *      int)
	 */
	public Object getValueAt(int rowIndex, int columnIndex) {
		assert columnIndex < COLUMN_COUNT;

		ValidObservation ob = obs.get(rowIndex);

		Object value = null;

		switch (columnIndex) {
		case JD_COLUMN:
			value = NumericPrecisionPrefs.formatTime(ob.getDateInfo()
					.getJulianDay());
			break;
		case CALDATE_COLUMN:
			value = ob.getDateInfo().getCalendarDate();
			break;
		case MEAN_COLUMN:
			// The mean magnitude.
			value = NumericPrecisionPrefs.formatMag(ob.getMagnitude()
					.getMagValue());
			break;
		case STDERR_COLUMN:
			// The standard error of the average.
			value = NumericPrecisionPrefs.formatMag(ob.getMagnitude().getUncertainty());
			break;
		}

		return value;
	}

	/**
	 * @see org.aavso.tools.vstar.ui.model.list.AbstractMeanObservationTableModel#getColumnClass(int)
	 */
	public Class<?> getColumnClass(int columnIndex) {
		Class<?> clazz = null;

		switch (columnIndex) {
		case JD_COLUMN:
			clazz = String.class;
			break;
		case CALDATE_COLUMN:
			clazz = String.class;
			break;
		case MEAN_COLUMN:
			clazz = String.class;
			break;
		case STDERR_COLUMN:
			clazz = String.class;
			break;
		}

		return clazz;
	}
}
