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

import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.aavso.tools.vstar.util.prefs.NumericPrecisionPrefs;

/**
 * This component defines a pane that shows a period and a combo-box requesting
 * the number of harmonics (e.g. to be found and tested in some algorithm such
 * as CLEANest) for that period.
 */
public class HarmonicPeriodPane extends JPanel {

	private double frequency;
	private double period;

	private JTextField periodField;
	private JComboBox harmonicSelector;

	/**
	 * Constructor.
	 * 
	 * @param frequency
	 *            The frequency to be displayed as a period.
	 * @param numHarmonics
	 *            The maximum number of harmonics that can be selected from.
	 */
	public HarmonicPeriodPane(double frequency, int numHarmonics) {
		this.frequency = frequency;
		period = 1.0 / frequency;

		setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

		periodField = new JTextField(String.format(NumericPrecisionPrefs
				.getOtherOutputFormat(), period));
		periodField.setEditable(false);
		add(periodField);

		add(Box.createRigidArea(new Dimension(10, 10)));

		String[] harmonicNumbers = new String[numHarmonics+1];
		for (int i = 0; i <= numHarmonics; i++) {
			harmonicNumbers[i] = i + "";
		}
		harmonicSelector = new JComboBox(harmonicNumbers);
		add(harmonicSelector);
	}

	/**
	 * Get the number of harmonics (e.g. to search for) for this period.
	 * 
	 * @return The number of harmonics.
	 */
	public int getNumberOfHarmonics() {
		return Integer.parseInt((String) harmonicSelector.getSelectedItem());
	}
}
