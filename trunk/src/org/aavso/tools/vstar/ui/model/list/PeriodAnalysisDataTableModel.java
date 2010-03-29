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

/**
 * This class represents a model for a period analysis table.
 */
public class PeriodAnalysisDataTableModel extends AbstractTableModel {

	private Map<PeriodAnalysisCoordinateType, List<Double>> data;

	/**
	 * Constructor
	 * 
	 * @param data
	 *            The result data mapping from coordinate type to list of
	 *            values.
	 * @param topPowerIndexPairs
	 *            A 2-dimensional array of power-index pairs where the index
	 *            refers to an element in the above value lists.
	 */
	public PeriodAnalysisDataTableModel(
			Map<PeriodAnalysisCoordinateType, List<Double>> data) {
		this.data = data;
	}

	/**
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	public int getColumnCount() {
		// column = coordinate type (freq, period, power, ampl)
		return this.data.keySet().size();
	}

	/**
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	public int getRowCount() {
		// Arbitrarily choose one coordinate and ask how many data-points
		// it has (same for all coordinates).
		return this.data.get(PeriodAnalysisCoordinateType.FREQUENCY).size();
	}

	/**
	 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
	 */
	public String getColumnName(int column) {
		return PeriodAnalysisCoordinateType.getTypeFromIndex(column)
				.getDescription();
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
		// column = coordinate type (freq, period, power, ampl)
		// row = value within the chosen coordinate's list
		double val = this.data.get(
				PeriodAnalysisCoordinateType.getTypeFromIndex(columnIndex))
				.get(rowIndex);
		return String.format("%10.4f", val);
	}
}
