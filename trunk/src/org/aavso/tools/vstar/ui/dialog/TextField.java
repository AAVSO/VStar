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

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

/**
 * This class encapsulates the name and value of a text field along with a GUI
 * textField and methods to operate upon it.
 */
public class TextField implements ITextComponent<String> {

	private String name;
	private boolean canBeEmpty;
	private boolean readOnly;

	private JTextComponent textField;

	/**
	 * Constructor.
	 * 
	 * @param name
	 *            The field's name.
	 * @param initialValue
	 *            The field's initial value.
	 * @param readOnly
	 *            Is this field read-only?
	 * @param canBeEmpty
	 *            Can this field be empty?
	 */
	public TextField(String name, String initialValue, boolean readOnly,
			boolean canBeEmpty) {
		this.name = name;
		this.readOnly = readOnly;
		this.canBeEmpty = canBeEmpty;

		this.textField = new JTextField(initialValue == null ? ""
				: initialValue);

		textField.setBorder(BorderFactory.createTitledBorder(name));
		if (!isReadOnly()) {
			textField.setToolTipText("Enter " + name);
		}
	}

	/**
	 * Construct a writable field with no initial value that cannot be empty.
	 * 
	 * @param name
	 *            The field's name.
	 */
	public TextField(String name) {
		this(name, null, false, false);
	}

	/**
	 * Construct a read-only field with an initial value; it doesn't matter
	 * whether or not it can be empty, so we permit this in order to prevent
	 * field validation from failing, and e.g. stopping a dialog from closing.
	 * 
	 * @param name
	 *            The field's name.
	 * @param initialValue
	 *            The field's initial value.
	 */
	public TextField(String name, String initialValue) {
		this(name, initialValue, true, true);
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
		return textField.getText();
	}

	@Override
	public String getStringValue() {
		return getValue();
	}

	@Override
	public JComponent getUIComponent() {
		return textField;
	}

	@Override
	public void setEditable(boolean state) {
		textField.setEditable(state);
	}

	public void setValue(String value) {
		textField.setText(value);
	}
}
