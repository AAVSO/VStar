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
import javax.swing.JTextArea;

/**
 * This class encapsulates the name and value of a text area along with a GUI
 * textArea and methods to operate upon it.
 */
public class TextArea implements ITextComponent<String> {

	private String name;
	private boolean canBeEmpty;
	private boolean readOnly;

	private JTextArea textArea;
	
	/**
	 * Constructor.
	 * 
	 * @param name
	 *            The area's name.
	 * @param initialValue
	 *            The area's initial value.
	 * @param rows
	 *            The number of rows in this text area; 0 means don't set.
	 * @param cols
	 *            The number of rows in this text area; 0 means don't set.
	 * @param readOnly
	 *            Is this area read-only?
	 * @param canBeEmpty
	 *            Can this area be empty?
	 */
	public TextArea(String name, String initialValue, int rows, int cols,
			boolean readOnly, boolean canBeEmpty) {
		this.name = name;
		this.readOnly = readOnly;
		this.canBeEmpty = canBeEmpty;

		this.textArea = new JTextArea(initialValue == null ? "" : initialValue);
		this.textArea.setAutoscrolls(true);
		
		if (rows != 0) {
			this.textArea.setRows(rows);
		}

		if (cols != 0) {
			this.textArea.setColumns(cols);
		}

		textArea.setBorder(BorderFactory.createTitledBorder(name));
		if (!isReadOnly()) {
			textArea.setToolTipText("Enter " + name);
		}
	}

	/**
	 * Construct a writable field with no initial value that cannot be empty.
	 * 
	 * @param name
	 *            The field's name.
	 * @param initialValue
	 *            The field's initial value.
	 * @param rows
	 *            The number of rows in this text area; 0 means don't set.
	 * @param cols
	 *            The number of rows in this text area; 0 means don't set.
	 */
	public TextArea(String name, String initialValue, int rows, int cols) {
		this(name, initialValue, rows, cols, false, false);
	}

	/**
	 * Construct a writable field with no initial value that cannot be empty.
	 * 
	 * @param name
	 *            The field's name.
	 * @param initialValue
	 *            The field's initial value.
	 */
	public TextArea(String name, String initialValue) {
		this(name, initialValue, 0, 0, false, false);
	}

	/**
	 * Construct a writable field with no initial value that cannot be empty.
	 * 
	 * @param name
	 *            The field's name.
	 * @param rows
	 *            The number of rows in this text area; 0 means don't set.
	 * @param cols
	 *            The number of rows in this text area; 0 means don't set.
	 */
	public TextArea(String name, int rows, int cols) {
		this(name, null, rows, cols, false, false);
	}

	/**
	 * Construct a writable field with no initial value that cannot be empty.
	 * 
	 * @param name
	 *            The field's name.
	 */
	public TextArea(String name) {
		this(name, null, 0, 0, false, false);
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
		return textArea.getText();
	}

	@Override
	public String getStringValue() {
		return getValue();
	}

	@Override
	public JComponent getUIComponent() {
		return textArea;
	}

	@Override
	public void setEditable(boolean state) {
		textArea.setEditable(state);
	}

	public void setValue(String value) {
		textArea.setText(value);
	}
}
