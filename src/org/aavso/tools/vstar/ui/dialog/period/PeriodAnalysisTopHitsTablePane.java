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

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.PeriodAnalysisSelectionMessage;
import org.aavso.tools.vstar.ui.model.list.PeriodAnalysisTopHitsTableModel;

/**
 * This class represents a period analysis top-hits table pane
 * (e.g. ranked by power, depending upon the model).
 */
public class PeriodAnalysisTopHitsTablePane extends JPanel implements
		ListSelectionListener {

	private PeriodAnalysisTopHitsTableModel model;
	private JTable table;
	
	/**
	 * Constructor
	 * 
	 * @param model
	 *            The period analysis top-hits table model.
	 */
	public PeriodAnalysisTopHitsTablePane(PeriodAnalysisTopHitsTableModel model) {
		super(new GridLayout(1, 1));
		
		this.model = model;
		
		table = new JTable(model);
		table.setColumnSelectionAllowed(false);
		table.setRowSelectionAllowed(true);
		JScrollPane scrollPane = new JScrollPane(table);

		this.add(scrollPane);

		// We generate period analysis selection messages.
		table.getSelectionModel().addListSelectionListener(this);
	}

	// We send a row selection event when the value has "settled".
	// This event could be consumed by other views such as plots.
	public void valueChanged(ListSelectionEvent e) {
		if (e.getSource() == table.getSelectionModel()
				&& table.getRowSelectionAllowed() && !e.getValueIsAdjusting()) {
			int row = table.getSelectedRow();

			if (row >= 0) {
				// TODO: rather than a double[][] here, we should probably use a class
				// since the second element is actuall an integer!
				int item = (int) this.model.getTopPowerIndexPairs()[row][1]; // 1 = index into data lists
				PeriodAnalysisSelectionMessage message = new PeriodAnalysisSelectionMessage(
						this, item);
				Mediator.getInstance().getPeriodAnalysisSelectionNotifier()
						.notifyListeners(message);
			}
		}
	}
}
