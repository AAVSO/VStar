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
package org.aavso.tools.vstar.ui;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * This class provides a way to select a floating point number from a range with
 * a specified increment between values in the range.
 */
public class NumberSelectionPane extends JPanel {

	private JSpinner spinner;
	private SpinnerNumberModel spinnerModel;

	private double value;

	/**
	 * Constructor.
	 * 
	 * @param title
	 *            The pane's title.
	 * @param min
	 *            The minimum value for the range.
	 * @param max
	 *            The maximum value for the range.
	 * @param increment
	 *            The increment over the range.
	 * @param initial
	 *            The initial value in the range.
	 * @param inputFormat
	 *            The input format to be used in the number spinner.
	 */
	public NumberSelectionPane(String title, double min, double max,
			double increment, double initial, String inputFormat) {

		value = initial;

		this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		this.setBorder(BorderFactory.createTitledBorder(title));

		spinnerModel = new SpinnerNumberModel(initial, min, max, increment);
		spinner = new JSpinner(spinnerModel);
		spinner.setEditor(new JSpinner.NumberEditor(spinner, inputFormat));

		if (max < min) {
			throw new IllegalArgumentException(String.format(
					"Max (%f) < min (%f)", max, min));
		}

		if (initial < min || initial > max) {
			throw new IllegalArgumentException(String
					.format("Initial value (%f) not in range %f..%f", initial,
							min, max));
		}

		if (increment > (max - min)) {
			throw new IllegalArgumentException(String.format(
					"Increment (%f) does not make sense for range %f..%f",
					increment, min, max));
		}

		spinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				value = (Double) spinner.getValue();
			}
		});

		this.add(spinner);
	}

	/**
	 * @return the value
	 */
	public double getValue() {
		return value;
	}
}
