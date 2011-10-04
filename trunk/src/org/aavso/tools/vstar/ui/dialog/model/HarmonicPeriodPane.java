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
package org.aavso.tools.vstar.ui.dialog.model;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.aavso.tools.vstar.util.model.Harmonic;
import org.aavso.tools.vstar.util.prefs.NumericPrecisionPrefs;

/**
 * This component defines a pane that shows a period and a combo-box requesting
 * the number of harmonics (e.g. to be found and tested in some algorithm such
 * as CLEANest) for that period.
 */
public class HarmonicPeriodPane extends JPanel {

	private double frequency;
	private double period;
	private int defaultNumHarmonics;

	private JTextField periodField;
	private JComboBox harmonicSelector;

	/**
	 * Constructor.
	 * 
	 * @param frequency
	 *            The frequency to be displayed as a period.
	 * @param numHarmonics
	 *            The maximum number of harmonics that can be selected from.
	 * @param numDefaultNumHarmonics
	 *            The default number of harmonics for the frequency.
	 */
	public HarmonicPeriodPane(double frequency, int numHarmonics,
			int defaultNumHarmonics) {
		this.frequency = frequency;
		period = 1.0 / frequency;
		this.defaultNumHarmonics = defaultNumHarmonics;

		setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

		periodField = new JTextField(String.format(NumericPrecisionPrefs
				.getOtherOutputFormat(), period));
		periodField.setEditable(false);
		periodField.setToolTipText(String.format("frequency="
				+ NumericPrecisionPrefs.getOtherDecimalPlaces(), frequency));
		add(periodField);

		add(Box.createRigidArea(new Dimension(10, 10)));

		String[] harmonicNumbers = new String[numHarmonics];
		for (int i = 0; i < numHarmonics; i++) {
			harmonicNumbers[i] = i+1 + "";
		}
		harmonicSelector = new JComboBox(harmonicNumbers);
		add(harmonicSelector);
	}

	/**
	 * @return the frequency
	 */
	public double getFrequency() {
		return frequency;
	}

	/**
	 * @return the period
	 */
	public double getPeriod() {
		return period;
	}

	/**
	 * Get the number of harmonics (e.g. to search for) for this period.
	 * 
	 * @return The number of harmonics.
	 */
	public int getNumberOfHarmonics() {
		return Integer.parseInt((String) harmonicSelector.getSelectedItem());
	}

	/**
	 * Create and return a harmonic object for this period and harmonic count
	 * selection.
	 * 
	 * @return A harmonic object corresponding to the frequency and harmonic
	 *         count selection.
	 */
	public Harmonic getHarmonic() {
		return new Harmonic(frequency, getNumberOfHarmonics());
	}

	/**
	 * A list of Harmonic objects, each representing a frequency and harmonic
	 * number (up to user selection) with respect to some fundamental frequency.
	 * 
	 * @return A list of Harmonic objects.
	 */
	public List<Harmonic> getHarmonicListForPeriod() {
		List<Harmonic> harmonics = new ArrayList<Harmonic>();
		for (int i=1;i<=getNumberOfHarmonics();i++) {
			harmonics.add(new Harmonic(frequency*i, i));
		}
		
		return harmonics;
	}
}
