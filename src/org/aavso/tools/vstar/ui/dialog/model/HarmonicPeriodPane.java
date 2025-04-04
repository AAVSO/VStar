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
import java.util.Collections;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.aavso.tools.vstar.util.locale.NumberParser;
import org.aavso.tools.vstar.util.model.Harmonic;
import org.aavso.tools.vstar.util.prefs.NumericPrecisionPrefs;
import org.aavso.tools.vstar.vela.VeLaEvalError;
import org.aavso.tools.vstar.vela.VeLaParseError;

/**
 * This component defines a pane that shows a period and a combo-box requesting
 * the number of harmonics (e.g. to be found and tested in some algorithm such
 * as CLEANest) for that period.
 */
@SuppressWarnings("serial")
public class HarmonicPeriodPane extends JPanel {

	private double frequency;

	private JTextField periodField;
	private String initialPeriodStr;

	private JComboBox harmonicSelector;

	/**
	 * Constructor.
	 * 
	 * @param frequency              The frequency to be displayed as a period.
	 * @param numHarmonics           The maximum number of harmonics that can be
	 *                               selected from.
	 * @param numDefaultNumHarmonics The default number of harmonics for the
	 *                               frequency.
	 */
	public HarmonicPeriodPane(double frequency, int numHarmonics, int defaultNumHarmonics) {

		this.frequency = frequency;

		double period = 1.0 / frequency;

		setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

		String periodStr = String.format("%g", period);
		initialPeriodStr = periodStr;

		periodField = new JTextField(periodStr);
		periodField.setEditable(true);
		periodField.setToolTipText("frequency=" + NumericPrecisionPrefs.formatOther(frequency));
		add(periodField);

		add(Box.createRigidArea(new Dimension(10, 10)));

		String[] harmonicNumbers = new String[numHarmonics];
		for (int i = 0; i < numHarmonics; i++) {
			harmonicNumbers[i] = i + 1 + "";
		}
		harmonicSelector = new JComboBox(harmonicNumbers);
		add(harmonicSelector);
	}

	/**
	 * Return the frequency or null if the field text is malformed.<br/>
	 * If the period text has not been edited, just return the initial frequency
	 * since the reciprocal of the period may have a different number of decimal
	 * places that does not actually correspond to a top hit, if being used in that
	 * context, for example, and context matters! Especially if you want to use a
	 * frequency value for a Fourier model uncertainty calculation which MUST
	 * correspond to an actual top hit that is at the peak of a local periodogram
	 * maximum.
	 * 
	 * @return the frequency
	 */
	public Double getFrequency() {
		Double freq = frequency;
		if (!periodField.getText().equals(initialPeriodStr)) {
			Double period = getPeriod();
			// Garbage may have been entered into the period text field which will yield
			// null from getPeriod().
			freq = period != null ? 1.0 / period : null;
		}
		return freq;
	}

	/**
	 * Return the period or null if the field text is malformed.
	 * 
	 * @return the period
	 */
	public Double getPeriod() {
		String periodText = periodField.getText();
		Double period = null;

		try {
			period = NumberParser.parseDouble(periodText);
		} catch (NumberFormatException e) {
			// Nothing to do; return null.
		} catch (VeLaParseError e) { // #PMAK#
			// Nothing to do; return null.
		} catch (VeLaEvalError e) { // #PMAK#
			// Nothing to do; return null.
		}

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
	 * @return A harmonic object corresponding to the frequency and harmonic count
	 *         selection, or null if the frequency is null.
	 */
	public Harmonic getHarmonic() {
		Double frequency = getFrequency();
		return frequency != null ? new Harmonic(frequency, getNumberOfHarmonics()) : null;
	}

	/**
	 * A list of Harmonic objects, each representing a frequency and harmonic number
	 * (up to user selection) with respect to some fundamental frequency.
	 * 
	 * @return A list of Harmonic objects or null if any frequency is null.
	 */
	public List<Harmonic> getHarmonicListForPeriod() {
		List<Harmonic> harmonics = new ArrayList<Harmonic>();
		for (int i = 1; i <= getNumberOfHarmonics(); i++) {
			Double frequency = getFrequency();
			if (frequency == null) {
				return Collections.EMPTY_LIST;
			}
			harmonics.add(new Harmonic(frequency * i, i));
		}

		return harmonics;
	}
}
