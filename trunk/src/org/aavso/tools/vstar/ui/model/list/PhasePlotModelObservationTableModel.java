/**
 * VStar: a statistical analysis tool for variable star data.
 * Copyright (C) 2010  AAVSO (http://www.aavso.org/)
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

import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.util.prefs.NumericPrecisionPrefs;

/**
 * This class is a table model for model observation data for a phase plot view.
 * 
 * The model is notified of model selection in order to update its data. The
 * data is updated according to the type specified in the constructor.
 */
public class PhasePlotModelObservationTableModel extends
		AbstractModelObservationTableModel  {

	private static final int PHASE_COLUMN = 0;
	private static final int MAG_COLUMN = 1;

	private static final int COLUMN_COUNT = 2;

	/**
	 * Constructor.
	 * 
	 * @param obs
	 *            The initial data. The data can be updated later via this
	 *            class's listener interface.
	 */
	public PhasePlotModelObservationTableModel(List<ValidObservation> obs,
			SeriesType seriesType) {
		super(obs, seriesType);
	}

	/**
	 * 
	 * @see org.aavso.tools.vstar.ui.model.list.AbstractSyntheticObservationTableModel
	 *      #getColumnClass(int)
	 */
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		Class<?> clazz = null;

		switch (columnIndex) {
		case PHASE_COLUMN:
			clazz = String.class;
			break;
		case MAG_COLUMN:
			clazz = String.class;
			break;
		}

		return clazz;
	}

	/**
	 * @see org.aavso.tools.vstar.ui.model.list.AbstractSyntheticObservationTableModel
	 *      #getColumnCount()
	 */
	@Override
	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	/**
	 * @see org.aavso.tools.vstar.ui.model.list.AbstractSyntheticObservationTableModel
	 *      #getColumnName(int)
	 */
	@Override
	public String getColumnName(int column) {
		assert column < COLUMN_COUNT;

		String columnName = null;

		switch (column) {
		case PHASE_COLUMN:
			columnName = "Phase";
			break;
		case MAG_COLUMN:
			columnName = "Magnitude";
			break;
		}
		
		return columnName;
	}

	/**
	 * @see org.aavso.tools.vstar.ui.model.list.AbstractSyntheticObservationTableModel
	 *      #getRowCount()
	 */
	@Override
	public int getRowCount() {
		return obs.size() * 2;
	}

	/**
	 * @see org.aavso.tools.vstar.ui.model.list.AbstractSyntheticObservationTableModel
	 *      #getValueAt(int, int)
	 */
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		assert columnIndex < COLUMN_COUNT;

		ValidObservation ob = obs.get(rowIndex % obs.size());

		Object value = null;

		switch (columnIndex) {
		case PHASE_COLUMN:
			if (rowIndex < obs.size() / 2) {
				value = String.format(NumericPrecisionPrefs
						.getTimeOutputFormat(), ob.getPreviousCyclePhase());
			} else {
				value = String.format(NumericPrecisionPrefs
						.getTimeOutputFormat(), ob.getStandardPhase());
			}
			break;
		case MAG_COLUMN:
			value = String.format(NumericPrecisionPrefs.getMagOutputFormat(), ob
					.getMagnitude().getMagValue());
			break;
		}
		
		return value;
	}
}
