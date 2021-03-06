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
package org.aavso.tools.vstar.ui.dialog.period.refinement;

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
import javax.swing.JTextArea;

import org.aavso.tools.vstar.ui.dialog.AbstractOkCancelDialog;
import org.aavso.tools.vstar.ui.dialog.model.HarmonicPeriodPane;
import org.aavso.tools.vstar.ui.mediator.Mediator;

/**
 * This dialog unifies the collection of information required for CLEANest.
 */
public class RefinementParameterDialog extends AbstractOkCancelDialog {

	private int numHarmonics;
	private List<Integer> harmonicsPerSelectedPeriod;

	private List<HarmonicPeriodPane> harmonicPeriodPanes;

	private PeriodGatheringPane lockedPeriodPane;
	private PeriodGatheringPane variablePeriodPane;

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
	public RefinementParameterDialog(Component parent, List<Double> userSelectedFreqs,
			int maxHarmonics) {
		super("Refinement Parameters");

		this.numHarmonics = maxHarmonics;
		harmonicsPerSelectedPeriod = new ArrayList<Integer>();

		Container contentPane = this.getContentPane();

		JPanel topPane = new JPanel();
		topPane.setLayout(new BoxLayout(topPane, BoxLayout.PAGE_AXIS));

		// User-selected period pane.
		topPane.add(createUserPeriodPane(userSelectedFreqs));
		topPane.add(Box.createRigidArea(new Dimension(10, 10)));
		
		// Locked and variable period collection panes.
		lockedPeriodPane = new PeriodGatheringPane("Locked Periods");
		topPane.add(lockedPeriodPane);
		variablePeriodPane = new PeriodGatheringPane("Variable Periods");
		topPane.add(variablePeriodPane);

		// OK, Cancel
		topPane.add(createButtonPane());

		contentPane.add(topPane);

		this.pack();
		setLocationRelativeTo(Mediator.getUI().getContentPane());
		this.setVisible(true);
	}

	private JPanel createUserPeriodPane(
			List<Double> userSelectedFreqs) {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.setBorder(BorderFactory
				.createTitledBorder("Selected Periods"));

		JTextArea userPeriodTextArea = new JTextArea();
		String periods = "";
		for (double freq : userSelectedFreqs) {
			periods += 1.0/freq;
			periods += "\n";
		}
		userPeriodTextArea.setText(periods);
		userPeriodTextArea.setEditable(false);		
		panel.add(userPeriodTextArea);

		return panel;
	}

	/**
	 * @return the lockedPeriods
	 */
	public List<Double> getLockedPeriods() {
		return lockedPeriodPane.getPeriods();
	}

	/**
	 * @return the variablePeriods
	 */
	public List<Double> getVariablePeriods() {
		return variablePeriodPane.getPeriods();
	}

	// Returns a subordinate component listener.
	private ActionListener createSubordinateChangeListener() {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				pack();
			}
		};
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
		try {
			cancelled = false;
			setVisible(false);
			dispose();
		} catch (NumberFormatException e) {
			// Nothing to do. The dialog stays open.
		}
	}
}
