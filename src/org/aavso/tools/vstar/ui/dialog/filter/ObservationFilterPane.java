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
package org.aavso.tools.vstar.ui.dialog.filter;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.aavso.tools.vstar.data.filter.IObservationFieldMatcher;
import org.aavso.tools.vstar.data.filter.ObservationFilter;
import org.aavso.tools.vstar.data.filter.ObservationMatcherOp;

/**
 * This class represents a single filter pane.
 */
public class ObservationFilterPane extends JPanel {

	private final static String NONE = "                                       ";
	private final static int TEXT_WIDTH = 15 * 10;

	private JComboBox filterNamesList;
	private JComboBox filterOpsList;
	private JTextField valueField;

	private ActionListener filterOpsListener;

	private IObservationFieldMatcher currFilter;
	private ObservationMatcherOp currOp;

	/**
	 * Constructor.
	 */
	public ObservationFilterPane() {
		this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		this.setBorder(BorderFactory.createEtchedBorder());

		// Create the filter name menu.
		filterNamesList = new JComboBox(new String[] { NONE });
		for (String filterName : ObservationFilter.MATCHERS.keySet()) {
			filterNamesList.addItem(filterName);
		}
		filterNamesList.addActionListener(createFilterNameListener());

		this.add(filterNamesList);

		this.add(Box.createRigidArea(new Dimension(10, 10)));

		// Create the filter operations menu.
		filterOpsList = new JComboBox(new String[] { NONE });
		filterOpsListener = createFilterOpsListener();
		this.add(filterOpsList);

		this.add(Box.createRigidArea(new Dimension(10, 10)));

		// Create the value text field.
		valueField = new JTextField();
		valueField.setMinimumSize(new Dimension(TEXT_WIDTH, 20));
		valueField.setMaximumSize(new Dimension(TEXT_WIDTH, 20));
		valueField.setPreferredSize(new Dimension(TEXT_WIDTH, 20));
		this.add(valueField);

		this.add(Box.createRigidArea(new Dimension(10, 10)));

		currFilter = null;
		currOp = null;
	}

	/**
	 * Return a field matcher corresponding to the selection. If no matcher is
	 * selected or the entered value does not match the type of the filter, null
	 * is returned.
	 */
	public IObservationFieldMatcher getFieldMatcher() {
		IObservationFieldMatcher matcher = null;

		if (currFilter != null) {
			matcher = currFilter.create(valueField.getText(), currOp);
		}

		return matcher;
	}

	// Listen to the name list in order to update the operations list
	// and related values according to the operations specific to the
	// selected filter.
	private ActionListener createFilterNameListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Re-populate the operations list according to the
				// selected matcher.
				String name = (String) filterNamesList.getSelectedItem();

				filterOpsList.removeAllItems();

				if (NONE.equals(name)) {
					currFilter = null;
					currOp = null;
					filterOpsList.addItem(NONE);
					valueField.setText("");
				} else {
					IObservationFieldMatcher filter = ObservationFilter.MATCHERS
							.get(name);
					// Update operator list and current values if a different
					// matcher has been selected.
					if (filter != currFilter) {
						currFilter = filter;
						filterOpsList.removeActionListener(filterOpsListener);
						for (ObservationMatcherOp op : currFilter
								.getMatcherOps()) {
							String opStr = op.toString();
							filterOpsList.addItem(op.toString());
						}
						filterOpsList.addActionListener(filterOpsListener);
						String opName = (String) filterOpsList.getItemAt(0);
						currOp = ObservationMatcherOp.fromString(opName);
						valueField.setText("");
					}
				}
			}
		};
	}

	// Listen to the operators list in order to update the
	// current operator value.
	private ActionListener createFilterOpsListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String opName = (String) filterOpsList.getSelectedItem();

				if (!NONE.equals(opName)) {
					currOp = ObservationMatcherOp.fromString(opName);
				}
			}
		};
	}
}
