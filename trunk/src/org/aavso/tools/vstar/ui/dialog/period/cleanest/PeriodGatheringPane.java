/**
 * VStar: a statistical analysis tool for variable star data.
 * Copyright (C) 2010  AAVSO (http://www.aavso.org/)
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
package org.aavso.tools.vstar.ui.dialog.period.cleanest;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.aavso.tools.vstar.ui.dialog.MessageBox;
import org.aavso.tools.vstar.util.locale.NumberParser;

/**
 * This component gathers periods and makes them available as a list.
 */
public class PeriodGatheringPane extends JPanel implements
		ListSelectionListener {

	private List<Double> periods;

	private JTextField periodField;
	private JButton addPeriodButton;
	
	private JList periodList;
	private DefaultListModel periodListModel;
	private JButton deleteButton;

	/**
	 * Constructor
	 * 
	 * @param title
	 *            The title to be shown for this component.
	 */
	public PeriodGatheringPane(String title) {
		periods = new ArrayList<Double>();

		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		setBorder(BorderFactory.createTitledBorder(title));

		add(createPeriodEntryPane());
		add(createListPane());
		add(createButtonPane());
	}

	/**
	 * Gets the collected periods.
	 * 
	 * @return the period list
	 */
	public List<Double> getPeriods() {
		return periods;
	}

	private JPanel createPeriodEntryPane() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));

		periodField = new JTextField();
		panel.add(periodField);

		addPeriodButton = new JButton("Add");
		addPeriodButton.addActionListener(createAddPeriodButtonListener());
		panel.add(addPeriodButton);
		
		return panel;
	}

	private JPanel createListPane() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));

		periodListModel = new DefaultListModel();
		periodList = new JList(periodListModel);
		periodList
				.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		periodList.addListSelectionListener(this);
		JScrollPane periodListScroller = new JScrollPane(periodList);

		panel.add(periodListScroller);

		return panel;
	}

	private JPanel createButtonPane() {
		JPanel panel = new JPanel();

		deleteButton = new JButton("Delete");
		deleteButton.addActionListener(createDeleteButtonListener());
		deleteButton.setEnabled(false);

		panel.add(deleteButton, BorderLayout.CENTER);

		return panel;
	}

	public void valueChanged(ListSelectionEvent e) {
		if (e.getValueIsAdjusting() == false) {

			if (periodList.getSelectedIndex() == -1) {
				deleteButton.setEnabled(false);
			} else {
				deleteButton.setEnabled(true);
			}
		}
	}

	// Return a listener for the "Add Period" button that adds a valid period to
	// the collection to be returned and the GUI list component. The period
	// field is then cleared ready for the next addition.
	private ActionListener createAddPeriodButtonListener() {
		final Component topPane = this;
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String periodStr = periodField.getText();
				if (!"".equals(periodStr.trim())) {
					try {
						double period = NumberParser.parseDouble(periodStr);
						periods.add(period);
						periodListModel.addElement(periodStr);
					} catch (NumberFormatException ex) {
						MessageBox.showErrorDialog(topPane,
								"Period Entry Error", "'" + periodStr
										+ "' is not a valid period value.");
					}
					periodField.setText("");
				}
			}
		};
	}

	// Return a listener for the "Delete" button that removes the selected item
	// from the period list.
	private ActionListener createDeleteButtonListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				periodListModel.remove(periodList.getSelectedIndex());
			}
		};
	}
}
