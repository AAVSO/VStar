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
 * - Use an enum for columns
 */
public class ValidObservationTableModel extends AbstractTableModel {

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
	public ValidObservationTableModel(List<ValidObservation> validObservations) {
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
		// (especially for download version of this!)
		
		switch (column) {
		case 0:
			columnName = "Discrepant?";
			break;
		case 1:
			columnName = "Line";
			break;
		case 2:
			columnName = "Julian Day";
			break;
		case 3:
			columnName = "Calendar Date";
			break;
		case 4:
			columnName = "Magnitude";
			break;
		case 5:
			columnName = "Observer Code";
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
			value = this.validObservations.get(rowIndex).isDiscrepant();
			break;
		case 1:
			value = validOb.getLineNumber();
			break;
		case 2:
			value = validOb.getDateInfo().getJulianDay();
			break;
		case 3:
			value = validOb.getDateInfo().getCalendarDate();
			break;
		case 4:
			value = validOb.getMagnitude().toString();
			break;
		case 5:
			value = validOb.getObsCode();
			break;
		}
		
		return value;
	}

	/**
	 * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object, int, int)
	 */
	public void setValueAt(Object value, int rowIndex, int columnIndex) {
		assert columnIndex == 0;

		switch(columnIndex) {		
		case 0:
			// Toggle discrepant value.
			ValidObservation ob = this.validObservations.get(rowIndex);
			boolean discrepant = ob.isDiscrepant(); 
			ob.setDiscrepant(!discrepant);
			break;
		case 1:
		case 2:
		case 3:
		case 4:
		case 5:
			break;
		}
	}

	/**
	 * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
	 */
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		// Discrepant check box. TODO: what other fields?
		return columnIndex == 0;
	}

	/**
	 * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
	 */
	public Class<?> getColumnClass(int columnIndex) {
		Class<?> clazz = Object.class;
		
		switch(columnIndex) {
		case 0:
			clazz = Boolean.class;
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
			clazz = String.class;
			break;
		}
		
		return clazz;
	}
}
