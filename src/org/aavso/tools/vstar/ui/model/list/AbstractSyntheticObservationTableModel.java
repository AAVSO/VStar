package org.aavso.tools.vstar.ui.model.list;

import java.util.List;
import java.util.WeakHashMap;

import javax.swing.table.AbstractTableModel;

import org.aavso.tools.vstar.data.ValidObservation;

/**
 * This class is a base table model for synthetic observation data (e.g. mean,
 * model, residual).
 */
@SuppressWarnings("serial")
public abstract class AbstractSyntheticObservationTableModel extends
		AbstractTableModel {

	protected List<ValidObservation> obs;

	/**
	 * A weak reference hash map from observations to row indices. We only want
	 * this map's entries to exist if they (ValidObservation instances in
	 * particular) are in use elsewhere.
	 */
	protected final WeakHashMap<ValidObservation, Integer> observationToRowIndexMap;

	/**
	 * Constructor
	 * 
	 * @param obs
	 *            The initial observation data. The data can be updated later
	 *            by subclasses.
	 */
	public AbstractSyntheticObservationTableModel(List<ValidObservation> obs) {
		super();
		this.obs = obs;
		this.observationToRowIndexMap = new WeakHashMap<ValidObservation, Integer>();
		populateObsToRowMap();
	}

	/**
	 * Returns the row index (0..n-1) given an observation.
	 * 
	 * @param ob
	 *            a valid observation whose row index we want.
	 * @return The observation's row index.
	 */
	public Integer getRowIndexFromObservation(ValidObservation ob) {
		return observationToRowIndexMap.get(ob);
	}

	/**
	 * This method returns the underlying data for this table model.
	 * 
	 * @return the obs
	 */
	public List<ValidObservation> getObs() {
		return obs;
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

	protected void populateObsToRowMap() {
		this.observationToRowIndexMap.clear();

		for (int i = 0; i < obs.size(); i++) {
			this.observationToRowIndexMap.put(obs.get(i), i);
		}
	}

}