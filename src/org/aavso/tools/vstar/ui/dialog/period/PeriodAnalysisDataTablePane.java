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
package org.aavso.tools.vstar.ui.dialog.period;

import java.awt.GridLayout;
import java.awt.Rectangle;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.message.PeriodAnalysisSelectionMessage;
import org.aavso.tools.vstar.ui.model.list.PeriodAnalysisDataTableModel;
import org.aavso.tools.vstar.util.notification.Listener;

/**
 * This class represents a period analysis data table pane.
 */
public class PeriodAnalysisDataTablePane extends JPanel implements
		ListSelectionListener, Listener<PeriodAnalysisSelectionMessage> {

	private JTable table;
//	private TableRowSorter<PeriodAnalysisDataTableModel> rowSorter;

	/**
	 * Constructor
	 * 
	 * @param model
	 *            The period analysis table model.
	 */
	public PeriodAnalysisDataTablePane(PeriodAnalysisDataTableModel model) {
		super(new GridLayout(1, 1));

		table = new JTable(model);
		table.setColumnSelectionAllowed(false);
		table.setRowSelectionAllowed(true);
		JScrollPane scrollPane = new JScrollPane(table);

		this.add(scrollPane);

		// We listen for and generate period analysis selection messages.
		Mediator.getInstance().getPeriodAnalysisSelectionNotifier()
				.addListener(this);
		
		table.getSelectionModel().addListSelectionListener(this);
		
		table.setAutoCreateRowSorter(true);
//		DoubleComparator comparator = new DoubleComparator();
//		rowSorter = new TableRowSorter<PeriodAnalysisDataTableModel>(model);
//		for (int i=0;i<model.getColumnCount();i++) {
//			rowSorter.setComparator(i, comparator);
//		}
//		table.setRowSorter(rowSorter);
	}

	// We send a row selection event when the value has "settled".
	// This event could be consumed by other views such as plots.
	public void valueChanged(ListSelectionEvent e) {
		if (e.getSource() == table.getSelectionModel()
				&& table.getRowSelectionAllowed() && !e.getValueIsAdjusting()) {
			int row = table.getSelectedRow();
			
			if (row >= 0) {
				row = table.convertRowIndexToModel(row);
				
				PeriodAnalysisSelectionMessage message = new PeriodAnalysisSelectionMessage(
						this, row);
				Mediator.getInstance().getPeriodAnalysisSelectionNotifier()
						.notifyListeners(message);
			}
		}
	}

	// PeriodAnalysisSelectionMessage listener methods.

	public boolean canBeRemoved() {
		return true;
	}

	public void update(PeriodAnalysisSelectionMessage info) {
		if (info.getSource() != this) {
			// Scroll to an arbitrary column (zeroth) within
			// the selected row, then select that row.
			// Assumption: we are specifying the zeroth cell
			// within row i as an x,y coordinate relative to
			// the top of the table pane.
			// Note that we could call this on the scroll
			// pane, which would then forward the request to
			// the table pane anyway.
			try {
				int row = info.getItem();
				// Convert to view index!
				row = table.convertRowIndexToView(row);
				
				int colWidth = (int) table.getCellRect(row, 0, true).getWidth();
				int rowHeight = table.getRowHeight(row);
				table.scrollRectToVisible(new Rectangle(colWidth, rowHeight
						* row, colWidth, rowHeight));

				table.setRowSelectionInterval(row, row);
			} catch (Throwable t) {
				// TODO: investigate! (e.g. Johnson V band, then click top-most
				// top hits table row.
				// t.printStackTrace();
			}
		}
	}
}
