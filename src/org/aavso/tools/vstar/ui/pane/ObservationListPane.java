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
package org.aavso.tools.vstar.ui.pane;

import java.awt.GridLayout;
import java.awt.Rectangle;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;

import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.ui.mediator.FilteredObservationMessage;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.ObservationSelectionMessage;
import org.aavso.tools.vstar.ui.model.list.InvalidObservationTableModel;
import org.aavso.tools.vstar.ui.model.list.ValidObservationTableModel;
import org.aavso.tools.vstar.util.notification.Listener;

/**
 * This class represents a GUI component that renders information about
 * observation data, including one or both of valid and invalid observation
 * data. If both are present, they are rendered as tables in a vertical split
 * pane. Otherwise, a single table will appear.
 */
public class ObservationListPane extends JPanel implements
		ListSelectionListener {

	private JTable validDataTable;
	private JTable invalidDataTable;
	private ValidObservationTableModel validDataModel;
	private TableRowSorter<ValidObservationTableModel> rowSorter;

	/**
	 * Constructor
	 * 
	 * @param validDataModel
	 *            A table data model that encapsulates valid observations.
	 * @param invalidDataModel
	 *            A table data model that encapsulates invalid observations.
	 * @param enableAutoResize
	 *            Enable auto-resize of columns? If true, we won't get a
	 *            horizontal scrollbar for valid observation table.
	 */
	public ObservationListPane(ValidObservationTableModel validDataModel,
			InvalidObservationTableModel invalidDataModel,
			boolean enableAutoResize) {

		super(new GridLayout(1, 1));

		JScrollPane validDataScrollPane = null;

		if (validDataModel != null) {
			this.validDataModel = validDataModel;

			validDataTable = new JTable(validDataModel);
			if (!enableAutoResize) {
				// Ensure we get a horizontal scrollbar if necessary rather than
				// trying to cram all the columns into the visible pane.
				validDataTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			}

			// Set selection mode to be row-only.
			// These appear to be the defaults anyway.
			validDataTable.setColumnSelectionAllowed(false);
			validDataTable.setRowSelectionAllowed(true);

			// Enable table sorting by clicking on a column.
			rowSorter = new TableRowSorter<ValidObservationTableModel>(
					validDataModel);
			validDataTable.setRowSorter(rowSorter);

			validDataScrollPane = new JScrollPane(validDataTable);
		}

		JScrollPane invalidDataScrollPane = null;

		if (invalidDataModel != null) {
			invalidDataTable = new JTable(invalidDataModel);

			// Ensure we get a horizontal scrollbar if necessary rather than
			// trying to cram all the columns into the visible pane. This is
			// particularly pertinent to the invalid data table since one of
			// its columns contains the original line in the case of a file
			// source.
			invalidDataTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

			// Set the columns containing the observation and error to be
			// greater than the default width of the others.
			TableColumnModel colModel = invalidDataTable.getColumnModel();
			int totalWidth = colModel.getTotalColumnWidth();
			colModel.getColumn(1).setPreferredWidth((int) (totalWidth * 2.5));
			colModel.getColumn(2).setPreferredWidth((int) (totalWidth * 2));

			// Enable table sorting by clicking on a column.
			invalidDataTable.setAutoCreateRowSorter(true);

			invalidDataScrollPane = new JScrollPane(invalidDataTable);
		}

		// In the presence of both valid and invalid data, we put
		// them into a split pane.
		if (validDataScrollPane != null && invalidDataScrollPane != null) {
			JSplitPane splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
			splitter.setToolTipText("Valid and invalid data");
			splitter.setTopComponent(validDataScrollPane);
			splitter.setBottomComponent(invalidDataScrollPane);
			splitter.setResizeWeight(0.5);
			this.add(splitter);
		} else if (validDataScrollPane != null) {
			// Just valid data.
			this.add(validDataScrollPane);
		} else if (invalidDataScrollPane != null) {
			// Just invalid data.
			this.add(invalidDataScrollPane);
		} else {
			// We have no data at all. Let's say so.
			JLabel label = new JLabel("There is no data to be displayed");
			label.setHorizontalAlignment(JLabel.CENTER);
			this.setLayout(new GridLayout(1, 1));
			this.add(label);
		}

		// Listen for observation selection events. Notice that this class
		// also generates these, but ignores them if sent by itself.
		Mediator.getInstance().getObservationSelectionNotifier().addListener(
				createObservationSelectionListener());

		// Listen to filtered observation messages so we can filter what's
		// displayed in the table.
		Mediator.getInstance().getFilteredObservationNotifier().addListener(
				createFilteredObservationListener());

		// List row selection handling.
		this.validDataTable.getSelectionModel().addListSelectionListener(this);
	}

	/**
	 * @return the validDataTable
	 */
	public JTable getValidDataTable() {
		return validDataTable;
	}

	/**
	 * @return the invalidDataTable
	 */
	public JTable getInvalidDataTable() {
		return invalidDataTable;
	}

	// Returns an observation selection listener.
	private Listener<ObservationSelectionMessage> createObservationSelectionListener() {
		return new Listener<ObservationSelectionMessage>() {

			public void update(ObservationSelectionMessage message) {
				if (message.getSource() != this) {
					List<ValidObservation> obs = validDataModel
							.getObservations();
					ValidObservation ob = message.getObservation();
					Integer rowIndex = validDataModel
							.getRowIndexFromObservation(ob);
					if (rowIndex != null) {
						// Convert to view index!
						rowIndex = validDataTable
								.convertRowIndexToView(rowIndex);

						// Scroll to an arbitrary column (zeroth) within
						// the selected row, then select that row.
						// Assumption: we are specifying the zeroth cell
						// within row i as an x,y coordinate relative to
						// the top of the table pane.
						// Note that we could call this on the scroll
						// pane, which would then forward the request to
						// the table pane anyway.
						int colWidth = (int) validDataTable.getCellRect(
								rowIndex, 0, true).getWidth();
						int rowHeight = validDataTable.getRowHeight(rowIndex);
						validDataTable.scrollRectToVisible(new Rectangle(
								colWidth, rowHeight * rowIndex, colWidth,
								rowHeight));

						validDataTable.setRowSelectionInterval(rowIndex,
								rowIndex);
					}
				}
			}

			public boolean canBeRemoved() {
				return true;
			}
		};
	}

	// Returns a filtered observation listener.
	private Listener<FilteredObservationMessage> createFilteredObservationListener() {
		return new Listener<FilteredObservationMessage>() {
			public void update(FilteredObservationMessage info) {
				if (info == FilteredObservationMessage.NO_FILTER) {
					rowSorter.setRowFilter(null);
				} else {
					rowSorter.setRowFilter(new ObservationTableRowFilter(info));
				}
			}

			public boolean canBeRemoved() {
				return false;
			}
		};
	}

	/**
	 * We send an observation selection event when the value has "settled". This
	 * event could be consumed by other views such as plots.
	 * 
	 * @param e
	 *            The list selection event.
	 */
	public void valueChanged(ListSelectionEvent e) {
		if (e.getSource() == validDataTable.getSelectionModel()
				&& validDataTable.getRowSelectionAllowed()
				&& !e.getValueIsAdjusting()) {
			int row = validDataTable.getSelectedRow();

			if (row >= 0) {
				row = validDataTable.convertRowIndexToModel(row);
				ValidObservation ob = validDataModel.getObservations().get(row);
				ObservationSelectionMessage message = new ObservationSelectionMessage(
						ob, this);
				Mediator.getInstance().getObservationSelectionNotifier()
						.notifyListeners(message);
			}
		}
	}
}