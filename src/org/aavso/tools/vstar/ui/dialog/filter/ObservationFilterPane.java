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

import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.data.filter.IObservationFieldMatcher;
import org.aavso.tools.vstar.data.filter.ObservationFilter;
import org.aavso.tools.vstar.data.filter.ObservationMatcherOp;
import org.aavso.tools.vstar.ui.mediator.message.ObservationSelectionMessage;

/**
 * This class represents a single filter pane.
 */
@SuppressWarnings("serial")
public class ObservationFilterPane extends JPanel {

	private final static String NONE = "                                       ";
	private final static int TEXT_WIDTH = 15 * 10;

	private JComboBox filterNamesList;
	private JComboBox filterOpsList;
	// TODO: for booleans this should be a checkbox;
	// for enums a combo-box; base on currFilter.getType()
	// Need IValueWidget.{getValue() => String,setValue(String)}
	private JTextField valueField;

	private ActionListener filterOpsListener;

	private IObservationFieldMatcher currFilter;
	private ObservationMatcherOp currOp;

	private ValidObservation observation;

	/**
	 * Constructor
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

		// JPanel p = new JPanel(new BorderLayout());
		// p.add(new JLabel(""), BorderLayout.CENTER);
		// set min/max/pref sizes then add to map of Class<?> =? IValueWidget

		this.add(Box.createRigidArea(new Dimension(10, 10)));

		currFilter = null;
		currOp = null;

		observation = null;
	}

	/**
	 * Return a field matcher corresponding to the selection. If no matcher is
	 * selected, null is returned. If the value of the text field does not
	 * conform to the filter's type, an exception is thrown.
	 * 
	 * @return A field matcher or null.
	 * @throws IllegalArgumentException
	 *             if the entered value does not match the type of the filter.
	 */
	public IObservationFieldMatcher getFieldMatcher()
			throws IllegalArgumentException {
		IObservationFieldMatcher matcher = null;

		if (currFilter != null) {
			matcher = currFilter.create(valueField.getText().trim(), currOp);
			if (matcher == null) {
				String msg = "Invalid " + currFilter.getDisplayName()
						+ " value: '" + valueField.getText() + "'";
				throw new IllegalArgumentException(msg);
			}
		}

		return matcher;
	}

	/**
	 * This method resets this pane's filter-related members and UI elements so
	 * that no filter is selected.
	 */
	public void resetFilter() {
		currFilter = null;
		currOp = null;
		filterNamesList.setSelectedItem(NONE);
		filterOpsList.removeAllItems();
		filterOpsList.addItem(NONE);
		valueField.setText("");
	}

	/**
	 * Tell this filter pane to set its field value now, or when a filter has
	 * been selected, using the specified observation. If null is passed and a
	 * current filter is selected, the value field is cleared.
	 * 
	 * @param msg
	 *            The observation selection message from which to extract the
	 *            observation; may be null.
	 */
	public void useObservation(ObservationSelectionMessage msg) {
		if (msg != null) {
			this.observation = msg.getObservation();
		} else {
			this.observation = null;
		}

		if (currFilter != null) {
			currFilter.setSelectedObservationMessage(msg);
			
			if (this.observation != null) {
				valueField.setText(currFilter
						.getTestValueFromObservation(this.observation));
			} else {
				valueField.setText("");
			}
		}
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

				if (NONE.equals(name)) {
					resetFilter();
				} else {
					IObservationFieldMatcher filter = ObservationFilter.MATCHERS
							.get(name);
					// Update operator list and current values if a different
					// matcher has been selected.
					if (filter != currFilter) {
						filterOpsList.removeAllItems();
						currFilter = filter;
						filterOpsList.removeActionListener(filterOpsListener);
						for (ObservationMatcherOp op : currFilter
								.getMatcherOps()) {
							filterOpsList.addItem(op.toString());
						}
						filterOpsList.addActionListener(filterOpsListener);
						String opName = (String) filterOpsList.getItemAt(0);
						currOp = ObservationMatcherOp.fromString(opName);
						String testValue = null;
						if (observation != null) {
							testValue = currFilter
									.getTestValueFromObservation(observation);
						} else {
							testValue = currFilter.getDefaultTestValue();
						}
						valueField
								.setText((testValue == null) ? "" : testValue);
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
