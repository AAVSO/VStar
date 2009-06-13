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

import javax.swing.table.AbstractTableModel;

import org.aavso.tools.vstar.data.ValidObservation;

/**
 * A table model for valid observations.
 * 
 * TODO: 
 * - May want an abstract factory that returns a family of
 *   observation and data table model class instances (and 
 *   maybe other classes, e.g. plot-related classes) given
 *   the ObservationRetrieverBase subclass.
 * - Also implement methods for cell editing, cell type, ...
 */
public class ValidObservationDataModel extends AbstractTableModel {

	private final static int COLUMNS = 6;

	/**
	 * The list of valid observations retrieved.
	 */
	private List<ValidObservation> validObservations;
	
	/**
	 * Constructor
	 * 
	 * @param validObservations A list of valid observations.
	 */
	public ValidObservationDataModel(List<ValidObservation> validObservations) {
		this.validObservations = validObservations;
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
		return this.validObservations.size();
	}

	/**
	 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
	 */
	public String getColumnName(int column) {
		String columnName = null;

		// TODO: put into an array!!
		
		switch (column) {
		case 0:
			columnName = "Line";
			break;
		case 1:
			columnName = "Julian Day";
			break;
		case 2:
			columnName = "Calendar Date";
			break;
		case 3:
			columnName = "Magnitude";
			break;
		case 4:
			columnName = "Observer Code";
			break;
		case 5:
			columnName = "Discrepant?";
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
		ValidObservation validOb = this.validObservations.get(rowIndex);
		
		switch(columnIndex) {
		case 0:
			value = validOb.getLineNumber();
			break;
		case 1:
			value = validOb.getDateInfo().getJulianDay();
			break;
		case 2:
			value = validOb.getDateInfo().getCalendarDate();
			break;
		case 3:
			value = validOb.getMagnitude().toString();
			break;
		case 4:
			value = validOb.getObsCode();
			break;
		case 5:
			value = this.validObservations.get(rowIndex).isDiscrepant();
			break;
		}
		
		return value;
	}

	/**
	 * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object, int, int)
	 */
	public void setValueAt(Object value, int rowIndex, int columnIndex) {
		assert columnIndex == 5;

		switch(columnIndex) {		
		case 0:
			break;
		case 1:
			break;
		case 2:
			break;
		case 3:
			break;
		case 4:
			break;
		case 5:
			// Toggle discrepant value.
			ValidObservation ob = this.validObservations.get(rowIndex);
			boolean discrepant = ob.isDiscrepant(); 
			ob.setDiscrepant(!discrepant);
			break;
		}
	}

	/**
	 * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
	 */
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		// Discrepant check box. TODO: what other fields?
		return columnIndex == 5;
	}

	/**
	 * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
	 */
	public Class<?> getColumnClass(int columnIndex) {
		Class<?> clazz = Object.class;
		
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
		case 3:
			clazz = String.class;
			break;
		case 4:
			clazz = String.class;
			break;
		case 5:
			clazz = Boolean.class;
			break;
		}
		
		return clazz;
	}
}
