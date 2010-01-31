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

import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.ObservationChange;
import org.aavso.tools.vstar.ui.mediator.ObservationChangeType;
import org.aavso.tools.vstar.util.notification.Listener;
import org.aavso.tools.vstar.util.notification.Notifier;

/**
 * A table model for valid observations.
 */
public class ValidObservationTableModel extends AbstractTableModel implements
		Listener<ObservationChange> {

	/**
	 * The list of valid observations retrieved.
	 */
	private final List<ValidObservation> validObservations;

	/**
	 * The source of column information.
	 */
	private final ITableColumnInfoSource columnInfoSource;

	/**
	 * The total number of columns in the table.
	 */
	private final int columnCount;

	/**
	 * This notifies listeners when an observation in the model changes. Right
	 * now, the only possible change is for an observation to be marked as
	 * discrepant or to have this designation removed.
	 */
	//private Notifier<ValidObservation> observationChangeNotifier;

	/**
	 * Constructor
	 * 
	 * @param validObservations
	 *            A list of valid observations.
	 * @param columnInfoSource
	 *            The source of column information for the table.
	 */
	public ValidObservationTableModel(List<ValidObservation> validObservations,
			ITableColumnInfoSource columnInfoSource) {
		this.validObservations = validObservations;
		this.columnInfoSource = columnInfoSource;
		this.columnCount = columnInfoSource.getColumnCount();
		//this.observationChangeNotifier = new Notifier<ValidObservation>();
		Mediator.getInstance().getObservationChangeNotifier().addListener(this);
	}

	/**
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	public int getColumnCount() {
		return columnCount;
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
		assert column < columnCount;
		return this.columnInfoSource.getTableColumnTitle(column);
	}

	/**
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	public Object getValueAt(int rowIndex, int columnIndex) {
		assert columnIndex < columnCount;
		ValidObservation validOb = this.validObservations.get(rowIndex);
		return this.columnInfoSource.getTableColumnValue(columnIndex, validOb);
	}

	/**
	 * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object,
	 *      int, int)
	 */
	public void setValueAt(Object value, int rowIndex, int columnIndex) {
		if (columnIndex == columnInfoSource.getDiscrepantColumnIndex()) {
			// Toggle "is-discrepant" checkbox and value.
			ValidObservation ob = this.validObservations.get(rowIndex);
			boolean discrepant = ob.isDiscrepant();
			ob.setDiscrepant(!discrepant);
//			observationChangeNotifier.notifyListeners(ob);
			// Tell anyone who's listening about the change.
			ObservationChange message = new ObservationChange(ob, ObservationChangeType.DISCREPANT, this);
			Mediator.getInstance().getObservationChangeNotifier().notifyListeners(message);
		}
	}

	/**
	 * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
	 */
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		// "is-discrepant" check box?
		return columnIndex == columnInfoSource.getDiscrepantColumnIndex();
	}

	/**
	 * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
	 */
	public Class<?> getColumnClass(int columnIndex) {
		return columnInfoSource.getTableColumnClass(columnIndex);
	}

	/**
	 * Listen for valid observation change notification, e.g. an observation's
	 * discrepant notification is changed.
	 */
	public void update(ObservationChange info) {
		if (info.getSource() != this) {
			for (ObservationChangeType change : info.getChanges()) {
				switch (change) {
				case DISCREPANT:
					this.fireTableDataChanged();
					break;
				}
			}
		}
	}
	
	/**
	 * @see org.aavso.tools.vstar.util.notification.Listener#canBeRemoved()
	 */
	public boolean canBeRemoved() {
		return true;
	}
}
