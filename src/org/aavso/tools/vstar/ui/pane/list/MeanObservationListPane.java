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
package org.aavso.tools.vstar.ui.pane.list;

import java.awt.GridLayout;
import java.awt.Rectangle;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableRowSorter;

import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.message.ObservationSelectionMessage;
import org.aavso.tools.vstar.ui.model.list.AbstractMeanObservationTableModel;
import org.aavso.tools.vstar.util.notification.Listener;

/**
 * This class is a component that renders observation mean and standard error of
 * the average data.
 */
public class MeanObservationListPane extends JPanel implements
		ListSelectionListener {

	private AbstractMeanObservationTableModel meanObsTableModel;
	private JTable meanObsTable;
	private TableRowSorter<AbstractMeanObservationTableModel> rowSorter;
	private ValidObservation lastObSelected = null;

	/**
	 * Constructor.
	 * 
	 * @param meanObsTableModel
	 *            The mean observation table model.
	 */
	public MeanObservationListPane(
			AbstractMeanObservationTableModel meanObsTableModel) {
		super(new GridLayout(1, 1));

		this.meanObsTableModel = meanObsTableModel;
		this.meanObsTable = new JTable(meanObsTableModel);

		// Enable table sorting by clicking on a column.
		rowSorter = new TableRowSorter<AbstractMeanObservationTableModel>(
				meanObsTableModel);
		meanObsTable.setRowSorter(rowSorter);

		JScrollPane meanObsTableScrollPane = new JScrollPane(meanObsTable);

		this.add(meanObsTableScrollPane);

		// Listen for observation selection events. Notice that this class
		// also generates these, but ignores them if sent by itself.
		Mediator.getInstance().getObservationSelectionNotifier().addListener(
				createObservationSelectionListener());

		// Listen to filtered observation messages so we can filter what's
		// displayed in the table.
//		Mediator.getInstance().getFilteredObservationNotifier().addListener(
//				createFilteredObservationListener());

		// List row selection handling.
		this.meanObsTable.getSelectionModel().addListSelectionListener(this);
	}

	/**
	 * @return the meanObsTable
	 */
	public JTable getMeanObsTable() {
		return meanObsTable;
	}

	/**
	 * @return the lastObSelected
	 */
	public ValidObservation getLastObSelected() {
		return lastObSelected;
	}

	// Returns an observation selection listener.
	private Listener<ObservationSelectionMessage> createObservationSelectionListener() {
		return new Listener<ObservationSelectionMessage>() {

			public void update(ObservationSelectionMessage message) {
				if (message.getSource() != this) {
					List<ValidObservation> obs = meanObsTableModel
							.getMeanObsData();
					ValidObservation ob = message.getObservation();
					Integer rowIndex = meanObsTableModel
							.getRowIndexFromObservation(ob);
					if (rowIndex != null) {
						// Convert to view index!
						rowIndex = meanObsTable.convertRowIndexToView(rowIndex);
						
						// Scroll to an arbitrary column (zeroth) within
						// the selected row, then select that row.
						// Assumption: we are specifying the zeroth cell
						// within row i as an x,y coordinate relative to
						// the top of the table pane.
						// Note that we could call this on the scroll
						// pane, which would then forward the request to
						// the table pane anyway.
						int colWidth = (int) meanObsTable.getCellRect(rowIndex,
								0, true).getWidth();
						int rowHeight = meanObsTable.getRowHeight(rowIndex);
						meanObsTable.scrollRectToVisible(new Rectangle(
								colWidth, rowHeight * rowIndex, colWidth,
								rowHeight));

						meanObsTable
								.setRowSelectionInterval(rowIndex, rowIndex);
						
						lastObSelected = ob;
					}
				}
			}

			public boolean canBeRemoved() {
				return true;
			}
		};
	}

	// List row selection event handler.
	// We send an observation selection event when the value has
	// "settled". This event could be consumed by other views such
	// as plots.
	public void valueChanged(ListSelectionEvent e) {
		if (e.getSource() == meanObsTable.getSelectionModel()
				&& meanObsTable.getRowSelectionAllowed()
				&& !e.getValueIsAdjusting()) {
			int row = meanObsTable.getSelectedRow();
			
			if (row >= 0) {
				row = meanObsTable.convertRowIndexToModel(row);
				ValidObservation ob = meanObsTableModel.getMeanObsData().get(
						row);
				ObservationSelectionMessage message = new ObservationSelectionMessage(
						ob, this);
				lastObSelected = ob;
				Mediator.getInstance().getObservationSelectionNotifier()
						.notifyListeners(message);
			}
		}
	}
}
