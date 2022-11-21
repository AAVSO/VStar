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
import java.util.logging.Level;

import javax.swing.table.AbstractTableModel;

import org.aavso.tools.vstar.data.IOrderedObservationSource;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.exception.AuthenticationError;
import org.aavso.tools.vstar.exception.CancellationException;
import org.aavso.tools.vstar.exception.ConnectionException;
import org.aavso.tools.vstar.ui.VStar;
import org.aavso.tools.vstar.ui.dialog.MessageBox;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.message.DiscrepantObservationMessage;
import org.aavso.tools.vstar.ui.mediator.message.SeriesCreationMessage;
import org.aavso.tools.vstar.util.ObservationInserter;
import org.aavso.tools.vstar.util.notification.Listener;

/**
 * A table model for valid observations.
 */
@SuppressWarnings("serial")
public class ValidObservationTableModel extends AbstractTableModel implements IOrderedObservationSource {

	/**
	 * The list of valid observations retrieved.
	 */
	private List<ValidObservation> validObservations;

	/**
	 * A weak reference hash map from observations to row indices. We only want this
	 * map's entries to exist if they (ValidObservation instances in particular) are
	 * in use elsewhere.
	 */
	private WeakHashMap<ValidObservation, Integer> validObservationToRowIndexMap;

	private ObservationInserter obsInserter;

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
	 * @param observations     The initial set of observations for the table model.
	 * @param columnInfoSource A source of table column information.
	 */
	public ValidObservationTableModel(List<ValidObservation> observations, ITableColumnInfoSource columnInfoSource) {

		this.columnInfoSource = columnInfoSource;
		this.columnCount = columnInfoSource.getColumnCount();

		this.obsInserter = new ObservationInserter();
		updateObservationsList(observations);

		Mediator.getInstance().getDiscrepantObservationNotifier().addListener(createDiscrepantChangeListener());
		Mediator.getInstance().getSeriesCreationNotifier().addListener(createSeriesCreationListener());
	}

	/**
	 * @return the columnInfoSource
	 */
	public ITableColumnInfoSource getColumnInfoSource() {
		return columnInfoSource;
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
	 * @param ob a valid observation whose row index we want.
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
		Object result = null;

		try {
			assert columnIndex < columnCount;
			ValidObservation validOb = this.validObservations.get(rowIndex);
			result = this.columnInfoSource.getTableColumnValue(columnIndex, validOb);
		} catch (IndexOutOfBoundsException e) {
			// Sometimes the series-index, item-index pair will have
			// changed or have become non-existent. Ignore but log.
			VStar.LOGGER.log(Level.WARNING, "Observation value retrieval error", e);
		}

		return result;
	}

	/**
	 * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object, int,
	 *      int)
	 */
	public void setValueAt(Object value, int rowIndex, int columnIndex) {
		if (columnIndex == columnInfoSource.getDiscrepantColumnIndex()) {
			// if (Mediator.getInstance().getAnalysisType() ==
			// AnalysisType.RAW_DATA) {

			ValidObservation ob = this.validObservations.get(rowIndex);

			try {
				toggleDiscrepantStatus(ob);

				// If the loaded dataset comes from AID, open
				// report-to-HQ dialog.
				Mediator.getInstance().reportDiscrepantObservation(ob, null);

				// Tell anyone who's listening about the change.
				DiscrepantObservationMessage message = new DiscrepantObservationMessage(ob, this);

				Mediator.getInstance().getDiscrepantObservationNotifier().notifyListeners(message);

			} catch (CancellationException ex) {
				toggleDiscrepantStatus(ob);
			} catch (ConnectionException ex) {
				toggleDiscrepantStatus(ob);

				MessageBox.showErrorDialog("Authentication Source Error", ex);
			} catch (AuthenticationError ex) {
				toggleDiscrepantStatus(ob);

				MessageBox.showErrorDialog("Authentication Error", "Login failed.");
			} catch (Exception ex) {
				toggleDiscrepantStatus(ob);

				MessageBox.showErrorDialog("Discrepant Reporting Error", ex.getLocalizedMessage());
			}
			// }
		}
	}

	private void toggleDiscrepantStatus(ValidObservation ob) {
		ob.setDiscrepant(!ob.isDiscrepant());
	}

	/**
	 * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
	 */
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		// "is-discrepant" check box?
		boolean is_discrepant_checkbox_editable = columnIndex == columnInfoSource.getDiscrepantColumnIndex()
		/* && Mediator.getInstance().getAnalysisType() == AnalysisType.RAW_DATA */;

		return is_discrepant_checkbox_editable;
	}

	/**
	 * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
	 */
	public Class<?> getColumnClass(int columnIndex) {
		return columnInfoSource.getTableColumnClass(columnIndex);
	}

	// TODO: needed? exists in row filter; and if needed, why not also one for
	// excluded?

	/**
	 * Listen for discrepant observation change notification.
	 */
	protected Listener<DiscrepantObservationMessage> createDiscrepantChangeListener() {

		return new Listener<DiscrepantObservationMessage>() {

			@Override
			public void update(DiscrepantObservationMessage info) {
				fireTableDataChanged();
			}

			@Override
			public boolean canBeRemoved() {
				return true;
			}
		};
	}

	protected Listener<SeriesCreationMessage> createSeriesCreationListener() {

		return new Listener<SeriesCreationMessage>() {

			@Override
			public void update(SeriesCreationMessage info) {
				updateObservationsList(info.getObs());
				fireTableDataChanged();
			}

			@Override
			public boolean canBeRemoved() {
				return true;
			}
		};
	}

	// Helpers

	/**
	 * Accept observations into this table model.
	 * 
	 * @param observations The observations to accept; may be null
	 */
	private void updateObservationsList(List<ValidObservation> observations) {
		// maintain ordering, keep track of min/max
		validObservations = obsInserter.addValidObservations(observations);
//		observations.stream().forEach(ob -> obsInserter.addValidObservation(ob));
//		validObservations = obsInserter.getValidObservations();

		// re-map *all* observations to row indices
		validObservationToRowIndexMap = new WeakHashMap<ValidObservation, Integer>();
		for (int i = 0; i < validObservations.size(); i++) {
			validObservationToRowIndexMap.put(validObservations.get(i), i);
		}
	}
}
