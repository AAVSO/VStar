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

import javax.swing.table.AbstractTableModel;

import org.aavso.tools.vstar.util.period.wwz.WWZCoordinateType;
import org.aavso.tools.vstar.util.period.wwz.WWZStatistic;
import org.aavso.tools.vstar.util.prefs.NumericPrecisionPrefs;

/**
 * This class represents a model for a WWZ time-frequency series table.
 */
public class WWZDataTableModel extends AbstractTableModel {

	private List<WWZStatistic> stats;

	/**
	 * Period analysis data model constructor.
	 * 
	 * @param columnTypes
	 *            An array of column types as they are to appear in the table.
	 * @param data
	 *            The result data mapping from coordinate type to list of
	 *            values.
	 */
	public WWZDataTableModel(List<WWZStatistic> stats) {
		this.stats = stats;
	}

	/**
	 * @return the stats
	 */
	public List<WWZStatistic> getStats() {
		return stats;
	}

	/**
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	public int getColumnCount() {
		return WWZCoordinateType.values().length;
	}

	/**
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	public int getRowCount() {
		return stats.size();
	}

	/**
	 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
	 */
	public String getColumnName(int column) {
		return WWZCoordinateType.getTypeFromId(column).toString();
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
		double value = stats.get(rowIndex).getValue(
				WWZCoordinateType.getTypeFromId(columnIndex));
		return String.format(NumericPrecisionPrefs.getOtherOutputFormat(),
				value);
	}

	/**
	 * Return a WWZ statistic from the values at the specified row.
	 * 
	 * @param rowIndex
	 *            The specified row.
	 * @return The data point (a WWZ statistic in this case).
	 */
	public WWZStatistic getDataPointFromRow(int rowIndex) {
		return stats.get(rowIndex);
	}
}
