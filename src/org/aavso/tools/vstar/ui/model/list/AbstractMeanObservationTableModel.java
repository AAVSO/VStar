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
import org.aavso.tools.vstar.util.notification.Listener;

/**
 * This class is a base table model for derived mean observation data. from raw
 * data.
 * 
 * The model is notified of wholesale mean data change.
 */
public abstract class AbstractMeanObservationTableModel extends
		AbstractTableModel implements Listener<List<ValidObservation>> {

	protected List<ValidObservation> meanObsData;

	/**
	 * A weak reference hash map from observations to row indices.
	 * We only want this map's entries to exist if they (ValidObservation
	 * instances in particular) are in use elsewhere.
	 */
	protected final WeakHashMap<ValidObservation, Integer> meanObservationToRowIndexMap;

	/**
	 * Constructor.
	 * 
	 * @param meanObsData
	 *            The mean initial observation data. The mean data can be 
	 *            updated later via this class's listener interface.
	 */
	public AbstractMeanObservationTableModel(List<ValidObservation> meanObsData) {
		this.meanObsData = meanObsData;
		
		this.meanObservationToRowIndexMap = new WeakHashMap<ValidObservation, Integer>();
		populateObsToRowMap();
	}

	/**
	 * Returns the row index (0..n-1) given an observation. 
	 * 
	 * @param ob a valid observation whose row index we want.
	 * @return The observation's row index.
	 */
	public Integer getRowIndexFromObservation(ValidObservation ob) {
		return meanObservationToRowIndexMap.get(ob);
	}

	/**
	 * This method returns the underlying data for this table model.
	 * 
	 * @return the meanObsData
	 */
	public List<ValidObservation> getMeanObsData() {
		return meanObsData;
	}

	/**
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	public abstract int getColumnCount();

	/**
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	public abstract int getRowCount();

	/**
	 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
	 */
	public abstract String getColumnName(int column);

	/**
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	public abstract Object getValueAt(int rowIndex, int columnIndex);

	/**
	 * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
	 */
	public abstract Class<?> getColumnClass(int columnIndex);

	/**
	 * Listen for updates to the mean data observation list, e.g.
	 * if the bin size has changed.
	 */
	public void update(List<ValidObservation> obs) {
		this.meanObsData = obs;
		populateObsToRowMap();
		this.fireTableDataChanged();
	}
	
	/**
	 * @see org.aavso.tools.vstar.util.notification.Listener#canBeRemoved()
	 */
	public boolean canBeRemoved() {
		return true;
	}
	
	// Helpers
	
	private void populateObsToRowMap() {
		this.meanObservationToRowIndexMap.clear();
		
		for (int i=0;i<meanObsData.size();i++) {
			this.meanObservationToRowIndexMap.put(meanObsData.get(i), i);
		}
	}
}