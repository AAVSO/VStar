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

import org.aavso.tools.vstar.util.locale.NumberParser;
import org.aavso.tools.vstar.util.prefs.NumericPrecisionPrefs;

/**
 * This class encapsulates the name, range, and value of a numeric text field
 * along with a GUI textField and methods to operate upon it.
 */
public class DoubleField extends NumberFieldBase<Double> {

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
	public DoubleField(String name, Double min, Double max, Double initial) {
		super(NumericPrecisionPrefs.getOtherOutputFormat(), name, min, max,
				initial);
	}

	/**
	 * Get the double value from the text field, if possible, otherwise return
	 * null if no valid number is present in the textField.
	 * 
	 * @return The double value or null.
	 */
	public Double getValue() {
		Double value = null;

		try {
			value = NumberParser.parseDouble(textField.getText());

			if (min != null && value < min) {
				value = null;
			}

			if (max != null && value > max) {
				value = null;
			}
		} catch (NumberFormatException e) {
			// Nothing to do; return null.
		}

		return value;
	}

	@Override
	public void setValue(Double value) {
		textField.setText(value.toString());
	}
}
