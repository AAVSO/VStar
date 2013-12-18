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
import org.aavso.tools.vstar.util.prefs.NumericPrecisionPrefs;

/**
 * This class represents a model for a period analysis top-hits table (e.g.
 * ranked (e.g. by power or amplitude), depending upon the topRankedIndexPairs
 * array).
 * 
 * @deprecated
 */
@SuppressWarnings("serial")
public class PeriodAnalysisIndexedTopHitsTableModel extends AbstractTableModel {

	private PeriodAnalysisCoordinateType[] columnTypes;
	private Map<PeriodAnalysisCoordinateType, List<Double>> data;
	private double[][] topRankedIndexPairs;
	private int topN;

	/**
	 * Constructor
	 * 
	 * @param columnTypes
	 *            An array of column types as they are to appear in the table.
	 * @param data
	 *            The result data mapping from coordinate type to list of
	 *            values.
	 * @param topRankedIndexPairs
	 *            A 2-dimensional array of (row-index,data-index) pairs where
	 *            the data-index refers to an element in the data value lists.
	 * @param topN
	 *            The maximum number values to display in the table.
	 */
	public PeriodAnalysisIndexedTopHitsTableModel(
			PeriodAnalysisCoordinateType[] columnTypes,
			Map<PeriodAnalysisCoordinateType, List<Double>> data,
			double[][] topRankedIndexPairs, int topN) {
		this.columnTypes = columnTypes;
		this.data = data;
		this.topRankedIndexPairs = topRankedIndexPairs;
		this.topN = topN;
	}

	/**
	 * @return the data
	 */
	public Map<PeriodAnalysisCoordinateType, List<Double>> getData() {
		return data;
	}

	/**
	 * @return the topRankedIndexPairs
	 */
	public double[][] getTopPowerIndexPairs() {
		return topRankedIndexPairs;
	}

	/**
	 * @return the topN
	 */
	public int getTopN() {
		return topN;
	}

	/**
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	public int getColumnCount() {
		// column: coordinate type (freq, period, power, ampl)
		return this.data.keySet().size();
	}

	/**
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	public int getRowCount() {
		// No more than topN, but there may actually be less.
		return Math.min(this.topN, topRankedIndexPairs.length);
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
		// column: coordinate type (freq, period, power, ampl)
		// row: within the table; we need to convert this supplied index
		// (table-wise)
		// to the index of the value within the chosen coordinate's list

		// TODO: rather than a double[][] here, we should probably use a class
		// since the second element is actually an integer!
		int n = (int) this.topRankedIndexPairs[rowIndex][1]; // 1 = index into
		// data lists

		PeriodAnalysisCoordinateType columnType = columnTypes[columnIndex];
		double val = data.get(columnType).get(n);

		return String.format(NumericPrecisionPrefs.getOtherOutputFormat(), val);
	}
}
