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

import java.util.List;

import org.aavso.tools.vstar.data.ValidObservation;

/**
 * This class is a table model for mean observation data derived from phase plot
 * data. The model is notified of wholesale mean data change.
 */
public class PhasePlotMeanObservationTableModel extends
		RawDataMeanObservationTableModel {

	private static final int PHASE_COLUMN = 0;

	/**
	 * Constructor.
	 * 
	 * @param meanObsData
	 *            The mean initial observation data. The mean data can be
	 *            updated later via this class's listener interface.
	 */
	public PhasePlotMeanObservationTableModel(List<ValidObservation> meanObsData) {
		super(meanObsData);
	}

	/**
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	public int getColumnCount() {
		return super.getColumnCount() + 1;
	}

	/**
	 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
	 */
	public String getColumnName(int column) {
		String columnName = null;

		switch (column) {
		case PHASE_COLUMN:
			columnName = "Phase";
			break;
		default:
			columnName = super.getColumnName(column - 1);
			break;
		}

		return columnName;
	}

	/**
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	public Object getValueAt(int rowIndex, int columnIndex) {
		ValidObservation ob = meanObsData.get(rowIndex);

		Object value = null;

		switch (columnIndex) {
		case PHASE_COLUMN:
			value = String.format("%1.3f", ob.getStandardPhase());
			break;
		default:
			value = super.getValueAt(rowIndex, columnIndex - 1);
			break;
		}

		return value;
	}

	/**
	 * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
	 */
	public Class<?> getColumnClass(int columnIndex) {
		Class<?> clazz = null;

		switch (columnIndex) {
		case PHASE_COLUMN:
			clazz = String.class;
			break;
		default:
			clazz = super.getColumnClass(columnIndex - 1);
			break;
		}

		return clazz;
	}
}
