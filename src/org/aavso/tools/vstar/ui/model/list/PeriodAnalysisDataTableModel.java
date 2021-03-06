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
import java.util.Map;

import javax.swing.table.AbstractTableModel;

import org.aavso.tools.vstar.util.period.PeriodAnalysisCoordinateType;
import org.aavso.tools.vstar.util.period.dcdft.PeriodAnalysisDataPoint;
import org.aavso.tools.vstar.util.prefs.NumericPrecisionPrefs;

/**
 * This class represents a model for a period analysis table.
 */
@SuppressWarnings("serial")
public class PeriodAnalysisDataTableModel extends AbstractTableModel {

	private PeriodAnalysisCoordinateType[] columnTypes;
	private Map<PeriodAnalysisCoordinateType, List<Double>> data;

	/**
	 * Period analysis data model constructor.
	 * 
	 * @param columnTypes
	 *            An array of column types as they are to appear in the table.
	 * @param data
	 *            The result data mapping from coordinate type to list of
	 *            values.
	 */
	public PeriodAnalysisDataTableModel(
			PeriodAnalysisCoordinateType[] columnTypes,
			Map<PeriodAnalysisCoordinateType, List<Double>> data) {
		this.columnTypes = columnTypes;
		this.data = data;
	}

	/**
	 * @return a mapping from period analysis coordinate type to a list of
	 *         values of that type.
	 */
	public Map<PeriodAnalysisCoordinateType, List<Double>> getData() {
		return data;
	}

	/**
	 * Set the data and notify listeners that it has changed. All data values
	 * are deselected.
	 * 
	 * @param data
	 *            The mapping from period analysis coordinate type to a list of
	 *            values of that type.
	 */
	public void setData(Map<PeriodAnalysisCoordinateType, List<Double>> data) {
		this.data = data;
		fireTableDataChanged();
	}

	/**
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	public int getColumnCount() {
		// column: coordinate type (freq, period, power, ampl[, selected])
		return data.keySet().size();
	}

	/**
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	public int getRowCount() {
		// Arbitrarily choose one coordinate and ask how many data-points
		// it has (same for all coordinates).
		return data.get(columnTypes[0]).size();
	}

	/**
	 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
	 */
	public String getColumnName(int column) {
		return columnTypes[column].getDescription();
	}

	/**
	 * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
	 */
	public Class<?> getColumnClass(int columnIndex) {
		return String.class;
	}

	/**
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	public Object getValueAt(int rowIndex, int columnIndex) {
		// column: coordinate type (freq, period, power, ampl[, selected])
		// row: value within the chosen coordinate's list
		PeriodAnalysisCoordinateType columnType = columnTypes[columnIndex];
		double val = data.get(columnType).get(rowIndex);

		return NumericPrecisionPrefs.formatOther(val);
	}

	/**
	 * Return the period value at the specified row.
	 * 
	 * @param rowIndex
	 *            The specified row.
	 * @return The period value.
	 */
	public Object getPeriodValueInRow(int rowIndex) {
		Object period = null;

		for (int i = 0; i < columnTypes.length; i++) {
			if (columnTypes[i] == PeriodAnalysisCoordinateType.PERIOD) {
				period = getValueAt(rowIndex, i);
			}
		}

		return period;
	}

	/**
	 * Return the frequency value at the specified row.
	 * 
	 * @param rowIndex
	 *            The specified row.
	 * @return The frequency value.
	 */
	public Double getFrequencyValueInRow(int rowIndex) {
		return data.get(PeriodAnalysisCoordinateType.FREQUENCY).get(rowIndex);
	}

	/**
	 * Return a period analysis data point from the values at the specified row.
	 * 
	 * @param rowIndex
	 *            The specified row.
	 * @return The data point.
	 */
	public PeriodAnalysisDataPoint getDataPointFromRow(int rowIndex) {

		// TODO: why not just use a map rather than PeriodAnalysisDataPoint?
		
		double[] values = new double[columnTypes.length];
		for (int i=0;i<columnTypes.length;i++) {
			values[i] = data.get(columnTypes[i]).get(rowIndex);
		}
				
		return new PeriodAnalysisDataPoint(columnTypes, values);
	}
}
