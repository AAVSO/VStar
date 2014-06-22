/**
 * VStar: a statistical analysis tool for variable star data.
 * Copyright (C) 2012  AAVSO (http://www.aavso.org/)
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
package org.aavso.tools.vstar.ui.dialog;

import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;

/**
 * This GUI component permits one of a number of strings to be selected via a
 * combo-box and the result retrieved.
 */
public class SelectableTextField implements ITextComponent<String> {

	private String name;
	private boolean canBeEmpty;
	private boolean readOnly;

	private JComboBox textChooser;

	/**
	 * Constructor
	 * 
	 * @param name
	 *            The field's name.
	 * @param values
	 *            The collection of values to choose from.
	 * @param initialValue
	 *            The field's initial value, or null if none.
	 * @param readOnly
	 *            Is this field read-only?
	 * @param canBeEmpty
	 *            Can this field be empty?
	 * @param kind
	 *            The kind of field: line or area.
	 */
	public SelectableTextField(String name, Collection<String> values,
			String initialValue, boolean readOnly, boolean canBeEmpty) {
		this.name = name;
		this.readOnly = readOnly;
		this.canBeEmpty = canBeEmpty;

		textChooser = new JComboBox(values.toArray(new String[0]));
		if (initialValue != null) {
			textChooser.setSelectedItem(initialValue);
		}

		textChooser.setBorder(BorderFactory.createTitledBorder(name));
		textChooser.setToolTipText("Select " + name);
	}

	/**
	 * Construct a component with no initial value selected.
	 * 
	 * @param name
	 *            The field's name.
	 * @param values
	 *            The list of values to choose from.
	 */
	public SelectableTextField(String name, Collection<String> values) {
		this(name, values, null, false, false);
	}

	/**
	 * Construct a component with an initial value selected.
	 * 
	 * @param name
	 *            The field's name.
	 * @param values
	 *            The list of values to choose from.
	 * @param initialValue
	 *            The field's initial value.
	 */
	public SelectableTextField(String name, Collection<String> values,
			String initialValue) {
		this(name, values, initialValue, true, true);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean canBeEmpty() {
		return canBeEmpty;
	}

	@Override
	public boolean isReadOnly() {
		return readOnly;
	}

	@Override
	public String getValue() {
		return (String) textChooser.getSelectedItem();
	}

	@Override
	public String getStringValue() {
		return getValue();
	}

	@Override
	public JComponent getUIComponent() {
		return textChooser;
	}

	@Override
	public void setEditable(boolean state) {
		textChooser.setEditable(state);
	}

	public void addActionListener(ActionListener l) {
		textChooser.addActionListener(l);
	}
}
