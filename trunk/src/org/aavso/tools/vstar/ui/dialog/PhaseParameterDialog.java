/**
 * VStar: a statistical analysis tool for variable star data.
 * Copyright (C) 2009  AAVSO (http://www.aavso.org/)
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

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.aavso.tools.vstar.ui.MainFrame;

/**
 * This class represents a dialog to obtain parameters for phase plot
 * calculation: period, epoch determination method.
 */
public class PhaseParameterDialog extends AbstractOkCancelDialog {

	private static Pattern periodPattern = Pattern
			.compile("^\\s*(\\d+(\\.\\d+)?)\\s*$");

	private JTextField periodField;

	private double period;

	/**
	 * Constructor.
	 */
	public PhaseParameterDialog() {
		super("Phase Parameter Selection");

		Container contentPane = this.getContentPane();

		JPanel topPane = new JPanel();
		topPane.setLayout(new BoxLayout(topPane, BoxLayout.PAGE_AXIS));
		topPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		topPane.add(createPeriodFieldPane());
		topPane.add(createButtonPane());
		contentPane.add(topPane);

		this.pack();
		periodField.requestFocusInWindow();
		this.setLocationRelativeTo(MainFrame.getInstance().getContentPane());
		this.setVisible(true);
	}

	private JPanel createPeriodFieldPane() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
		panel.setBorder(BorderFactory.createTitledBorder("Period (days)"));

		periodField = new JTextField();
		periodField.addActionListener(createPeriodFieldActionListener());
		periodField.setToolTipText("Enter star name, alias or AUID");
		panel.add(periodField);

		return panel;
	}

	// Return a listener for the period field.
	private ActionListener createPeriodFieldActionListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// checkInput();
			}
		};
	}

	/**
	 * @see org.aavso.tools.vstar.ui.dialog.AbstractOkCancelDialog#cancelAction()
	 */
	protected void cancelAction() {
		// Nothing to do
	}

	/**
	 * @see org.aavso.tools.vstar.ui.dialog.AbstractOkCancelDialog#okAction()
	 */
	protected void okAction() {
		String periodText = periodField.getText();

		if (periodText != null) {
			Matcher periodMatcher = periodPattern.matcher(periodText);
			if (periodMatcher.matches()) {
				String periodStr = periodMatcher.group(1);
				period = Double.parseDouble(periodStr);
				if (period > 0) {
					cancelled = false;
					setVisible(false);
					dispose();
				}
			}
		}
	}

	/**
	 * @return the period
	 */
	public double getPeriod() {
		return period;
	}
}
