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
package org.aavso.tools.vstar.ui.dialog;

import java.awt.event.ActionListener;
import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JTextField;

/**
 * This abstract base class encapsulates the name, range, and value of a numeric
 * textField along with a GUI text field and methods to operate upon it.
 * Subclasses must implement the getValue() method.
 */
public abstract class NumberFieldBase<T extends Number> implements
		ITextComponent<T> {

	protected String name;
	protected T min;
	protected T max;
	protected T initial;

	protected JTextField textField;

	/**
	 * Constructor
	 * 
	 * @param numberFormat
	 *            The numeric format to be used to display the initial value.
	 * @param name
	 *            The name of the textField.
	 * @param min
	 *            The value that the entered value must be greater than (may be
	 *            null).
	 * @param max
	 *            The value that the entered value must be less than (may be
	 *            null).
	 * @param initial
	 *            The initial value.
	 */
	public NumberFieldBase(NumberFormat numberFormat, String name, T min,
			T max, T initial) {
		this.name = name;
		this.min = min;
		this.max = max;
		this.initial = initial;

		String initialStr = initial == null ? "" : numberFormat.format(initial);
		textField = new JTextField(initialStr);
		textField.setBorder(BorderFactory.createTitledBorder(name));
		textField.setToolTipText("Enter " + name);
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the min
	 */
	public T getMin() {
		return min;
	}

	/**
	 * @return the max
	 */
	public T getMax() {
		return max;
	}

	/**
	 * @return the initial
	 */
	public T getInitial() {
		return initial;
	}

	/**
	 * @return the textField
	 */
	public JComponent getUIComponent() {
		return textField;
	}

	/**
	 * Get the numeric value from the textField, if possible, otherwise return
	 * null if no valid number is present in the textField.
	 * 
	 * @return The numeric value or null.
	 */
	public abstract T getValue();

	@Override
	public String getStringValue() {
		return textField.getText();
	}

	public void addActionListener(ActionListener l) {
		textField.addActionListener(l);
	}

	@Override
	public boolean canBeEmpty() {
		return false;
	}

	@Override
	public boolean isReadOnly() {
		return false;
	}

	@Override
	public void setEditable(boolean state) {
		// Always editable otherwise a TextField should be used!
	}
}
