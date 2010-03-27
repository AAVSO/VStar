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
import java.util.WeakHashMap;

import javax.swing.table.AbstractTableModel;

import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.ui.mediator.AnalysisType;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.ObservationChangeMessage;
import org.aavso.tools.vstar.ui.mediator.ObservationChangeType;
import org.aavso.tools.vstar.util.notification.Listener;

/**
 * A table model for valid observations.
 */
public class ValidObservationTableModel extends AbstractTableModel implements
		Listener<ObservationChangeMessage> {

	/**
	 * The list of valid observations retrieved.
	 */
	private final List<ValidObservation> validObservations;

	/**
	 * A weak reference hash map from observations to row indices. We only want
	 * this map's entries to exist if they (ValidObservation instances in
	 * particular) are in use elsewhere.
	 */
	private final WeakHashMap<ValidObservation, Integer> validObservationToRowIndexMap;

	/**
	 * The source of column information.
	 */
	private final ITableColumnInfoSource columnInfoSource;

	/**
	 * The total number of columns in the table.
	 */
	private final int columnCount;

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

		this.validObservationToRowIndexMap = new WeakHashMap<ValidObservation, Integer>();
		for (int i = 0; i < validObservations.size(); i++) {
			this.validObservationToRowIndexMap.put(validObservations.get(i), i);
		}

		this.columnInfoSource = columnInfoSource;
		this.columnCount = columnInfoSource.getColumnCount();

		Mediator.getInstance().getObservationChangeNotifier().addListener(this);
	}

	/**
	 * @return the validObservations
	 */
	public List<ValidObservation> getObservations() {
		return validObservations;
	}

	/**
	 * Returns the row index (0..n-1) given an observation.
	 * 
	 * @param ob
	 *            a valid observation whose row index we want.
	 * @return The observation's row index.
	 */
	public Integer getRowIndexFromObservation(ValidObservation ob) {
		return validObservationToRowIndexMap.get(ob);
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
			// Tell anyone who's listening about the change.
			ObservationChangeMessage message = new ObservationChangeMessage(ob,
					ObservationChangeType.DISCREPANT, this);
			Mediator.getInstance().getObservationChangeNotifier()
					.notifyListeners(message);
		}
	}

	/**
	 * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
	 */
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		// "is-discrepant" check box?
		// We currently disable the discrepant checkbox for anything other 
		// than raw data mode due to this bug in which a chunk of data disappears 
		// after marking a point as discrepant, then unmarking it. Since the cross 
		// hair change is reflected in raw data mode also, this is no great user 
		// interface problem. The problem should be fixed though. See 
		// https://sourceforge.net/tracker/?func=detail&aid=2964224&group_id=263306&atid=1152052
		// for more detail.
		boolean is_discrepant_checkbox_editable = columnIndex == columnInfoSource
				.getDiscrepantColumnIndex()
				&& Mediator.getInstance().getAnalysisType() == AnalysisType.RAW_DATA;
		
		return is_discrepant_checkbox_editable;
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
	public void update(ObservationChangeMessage info) {
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
