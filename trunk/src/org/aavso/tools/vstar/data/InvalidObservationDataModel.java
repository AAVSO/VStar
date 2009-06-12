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

import java.util.List;

import javax.swing.table.AbstractTableModel;

/**
 * 
 */
public class InvalidObservationDataModel extends AbstractTableModel {

	private final static int COLUMNS = 2;

	/**
	 * The list of invalid observations retrieved.
	 */
	protected List<InvalidObservation> invalidObservations;

	/**
	 * Constructor
	 * 
	 * @param invalidObservations
	 */
	public InvalidObservationDataModel(
			List<InvalidObservation> invalidObservations) {
		super();
		this.invalidObservations = invalidObservations;
	}

	/**
	 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
	 */
	public String getColumnName(int column) {
		String columnName = null;

		switch (column) {
		case 0:
			columnName = "Observation";
			break;
		case 1:
			columnName = "Comment";
			break;
		}

		return columnName;
	}

	/**
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	public int getColumnCount() {
		return COLUMNS;
	}

	/**
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	public int getRowCount() {
		return invalidObservations.size();
	}

	/**
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	public Object getValueAt(int rowIndex, int columnIndex) {
		assert columnIndex < COLUMNS;

		Object value = null;
		InvalidObservation invalidOb = invalidObservations.get(rowIndex);
		switch (columnIndex) {
		case 0:
			value = invalidOb.getInputLine();
			break;
		case 1:
			value = invalidOb.getError();
			break;
		}
		return value;
	}
}
