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

import org.aavso.tools.vstar.data.InvalidObservation;

/**
 * A table model for invalid observations.
 */
public class InvalidObservationTableModel extends AbstractTableModel {

	private final static int COLUMNS = 3;

	/**
	 * The list of invalid observations retrieved.
	 */
	protected List<InvalidObservation> invalidObservations;

	/**
	 * Constructor
	 * 
	 * @param invalidObservations
	 */
	public InvalidObservationTableModel(
			List<InvalidObservation> invalidObservations) {
		super();
		this.invalidObservations = invalidObservations;
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
	 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
	 */
	public String getColumnName(int column) {
		String columnName = null;
		
		switch (column) {
		case 0:
			columnName = "Line";
			break;
		case 1:
			columnName = "Observation";
			break;
		case 2:
			columnName = "Error";
			break;
		}

		return columnName;
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
			// TODO: we really want to return an integer so that column 
			// sorting works properly for numbers!
			value = String.format("%d", invalidOb.getRecordNumber());
			break;
		case 1:
			value = invalidOb.getInputLine();
			break;
		case 2:
			value = invalidOb.getError();
			break;
		}
		return value;
	}

	/**
	 * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
	 */
	public Class<?> getColumnClass(int columnIndex) {
		Class<?> clazz = null;
		
		switch(columnIndex) {
		case 0:
			clazz = String.class;
			break;
		case 1:
			clazz = String.class;
			break;
		case 2:
			clazz = String.class;
			break;
		}
		
		return clazz;
	}	
}
