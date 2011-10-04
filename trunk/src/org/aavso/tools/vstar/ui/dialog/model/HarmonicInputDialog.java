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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.aavso.tools.vstar.ui.MainFrame;
import org.aavso.tools.vstar.ui.dialog.AbstractOkCancelDialog;
import org.aavso.tools.vstar.ui.dialog.period.refinement.PeriodGatheringPane;
import org.aavso.tools.vstar.util.model.Harmonic;

/**
 * This dialog gathers the harmonics to be used as input to model creation.
 */
public class HarmonicInputDialog extends AbstractOkCancelDialog {

	private int maxHarmonics;
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
	 * @param maxHarmonics
	 *            The maximum number of harmonics that can be selected from per
	 *            user specified frequency/period.
	 */
	public HarmonicInputDialog(Component parent,
			List<Double> userSelectedFreqs, int maxHarmonics) {
		super("Harmonics");

		this.maxHarmonics = maxHarmonics;
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
		setLocationRelativeTo(MainFrame.getInstance().getContentPane());
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
			// TODO: use harmonic search to notify default value: currently 1
			// below; could use half way between 1 and maxHarmonics; it should
			// also be possible to override this max, e.g. via notification
			HarmonicPeriodPane harmonicPeriodPane = new HarmonicPeriodPane(
					freq, maxHarmonics, Harmonic.FUNDAMENTAL);
			harmonicPeriodPanes.add(harmonicPeriodPane);
			panel.add(harmonicPeriodPane);
			panel.add(Box.createRigidArea(new Dimension(10, 10)));
		}

		return panel;
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
			harmonicsPerSelectedPeriod.addAll(pane.getHarmonicListForPeriod());
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
