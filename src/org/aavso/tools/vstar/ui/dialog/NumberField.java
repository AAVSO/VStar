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

import javax.swing.BorderFactory;
import javax.swing.JTextField;

import org.aavso.tools.vstar.util.locale.NumberParser;
import org.aavso.tools.vstar.util.prefs.NumericPrecisionPrefs;

/**
 * This class encapsulates the name, range, and value of a numeric textField
 * along with a GUI text textField and methods to operate upon it.
 */
public class NumberField {

	private String name;
	private Double min;
	private Double max;
	private Double initial;

	private JTextField textField;

	/**
	 * Constructor
	 * 
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
	public NumberField(String name, Double min, Double max, Double initial) {
		this.name = name;
		this.min = min;
		this.max = max;
		this.initial = initial;

		String initialStr = initial == null ? "" : String.format(
				NumericPrecisionPrefs.getOtherOutputFormat(), initial);
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
	public double getMin() {
		return min;
	}

	/**
	 * @return the max
	 */
	public double getMax() {
		return max;
	}

	/**
	 * @return the initial
	 */
	public Double getInitial() {
		return initial;
	}

	/**
	 * @return the textField
	 */
	public JTextField getTextField() {
		return textField;
	}

	/**
	 * Get the double value from the textField, if possible, otherwise return
	 * null if no valid number is present in the textField.
	 * 
	 * @return The double value or null.
	 */
	public Double getValue() {
		Double value = null;

		try {
			value = NumberParser.parseDouble(textField.getText());

			if (min != null && value <= min) {
				value = null;
			}

			if (max != null && value >= max) {
				value = null;
			}
		} catch (NumberFormatException e) {
			// Nothing to do; return null.
		}

		return value;
	}
}
