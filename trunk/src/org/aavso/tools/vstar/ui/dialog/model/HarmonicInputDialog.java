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

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.aavso.tools.vstar.ui.dialog.AbstractOkCancelDialog;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.util.model.Harmonic;

/**
 * This dialog gathers the harmonics to be used as input to model creation.
 */
@SuppressWarnings("serial")
public class HarmonicInputDialog extends AbstractOkCancelDialog {

	private Map<Double, List<Harmonic>> freqToHarmonics;

	private List<Harmonic> harmonicsPerSelectedPeriod;

	private List<HarmonicPeriodPane> harmonicPeriodPanes;

	/**
	 * Constructor
	 * 
	 * @param parent
	 *            The parent component relative to which this dialog should be
	 *            displayed.
	 * @param userSelectedFreqs
	 *            The user selected frequencies.
	 * @param lastHarmonicSearchResult
	 *            The most recent harmonic search result.
	 */
	public HarmonicInputDialog(Component parent,
			List<Double> userSelectedFreqs,
			Map<Double, List<Harmonic>> freqToHarmonicsMap) {
		super("Periods (days)");

		this.freqToHarmonics = freqToHarmonicsMap;

		harmonicsPerSelectedPeriod = new ArrayList<Harmonic>();

		Container contentPane = this.getContentPane();

		JPanel topPane = new JPanel();
		topPane.setLayout(new BoxLayout(topPane, BoxLayout.PAGE_AXIS));

		// Number-of-harmonics per user-specified frequency/period pane.
		JScrollPane harmonicPerUserPeriodScroller = new JScrollPane(
				createHarmonicPerUserPeriodPane(userSelectedFreqs));
		topPane.add(harmonicPerUserPeriodScroller);

		// OK, Cancel
		topPane.add(createButtonPane());

		contentPane.add(topPane);

		this.pack();
		setLocationRelativeTo(Mediator.getUI().getContentPane());
		this.setVisible(true);
	}

	private JPanel createHarmonicPerUserPeriodPane(
			List<Double> userSelectedFreqs) {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.setBorder(BorderFactory
				.createTitledBorder("Harmonics per period"));

		harmonicPeriodPanes = new ArrayList<HarmonicPeriodPane>();

		for (double freq : userSelectedFreqs) {
			int maxHarmonics = getMaxHarmonicsForFreq(freq);
			HarmonicPeriodPane harmonicPeriodPane = new HarmonicPeriodPane(
					freq, maxHarmonics, Harmonic.FUNDAMENTAL);
			harmonicPeriodPanes.add(harmonicPeriodPane);
			panel.add(harmonicPeriodPane);
			panel.add(Box.createRigidArea(new Dimension(10, 10)));
		}

		return panel;
	}

	// Get an appropriate max-number-of-harmonics value for the specified
	// frequency, based upon the last harmonic search result if one exists.
	private int getMaxHarmonicsForFreq(double freq) {
		int maxHarmonics = 12; // TODO: make this a preference

		// If there are less harmonics for the frequency than the default, use
		// this. Allowing this to have no reasonable bound just doesn't make
		// sense
		// from a computational feasibility viewpoint anyway.
		// if (freqToHarmonics.containsKey(freq)
		// && freqToHarmonics.get(freq).size() < maxHarmonics) {
		// maxHarmonics = freqToHarmonics.get(freq).size();
		// }

		return maxHarmonics;
	}

	/**
	 * Return all harmonics per user-specified period and harmonic number.
	 * 
	 * @return A list of harmonics for each user-specified frequency/period that
	 *         includes the frequency itself and the number of harmonics of that
	 *         frequency (e.g. to be modelled).
	 */
	public List<Harmonic> getHarmonics() {
		for (HarmonicPeriodPane pane : harmonicPeriodPanes) {
			List<Harmonic> harmonicsForPeriod = pane.getHarmonicListForPeriod();
			if (!harmonicsForPeriod.isEmpty()) {
				harmonicsPerSelectedPeriod.addAll(harmonicsForPeriod);
			} else {
				return Collections.EMPTY_LIST;
			}
		}

		return harmonicsPerSelectedPeriod;
	}

	/**
	 * @see org.aavso.tools.vstar.ui.dialog.AbstractOkCancelDialog#cancelAction()
	 */
	@Override
	protected void cancelAction() {
		// Nothing to do.
	}

	/**
	 * @see org.aavso.tools.vstar.ui.dialog.AbstractOkCancelDialog#okAction()
	 */
	@Override
	protected void okAction() {
		cancelled = false;
		setVisible(false);
		dispose();
	}
}
