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
import java.awt.Dimension;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.aavso.tools.vstar.ui.MainFrame;
import org.aavso.tools.vstar.ui.mediator.NewStarMessage;
import org.aavso.tools.vstar.ui.mediator.StarInfo;
import org.aavso.tools.vstar.ui.model.NewStarType;
import org.aavso.tools.vstar.util.notification.Listener;
import org.aavso.tools.vstar.util.stats.epoch.AlphaOmegaMeanJDEpochStrategy;
import org.aavso.tools.vstar.util.stats.epoch.IEpochStrategy;

/**
 * This class represents a dialog to obtain parameters for phase plot
 * calculation: period, epoch determination method.
 */
public class PhaseParameterDialog extends AbstractOkCancelDialog implements
		Listener<NewStarMessage> {

	private static Pattern realNumberPattern = Pattern
			.compile("^\\s*(\\d+(\\.\\d+)?)\\s*$");

	private JTextField periodField;
	private JTextField epochField;

	private double period;
	private double epoch;
	private IEpochStrategy epochStrategy;

	private boolean firstUse;

	/**
	 * Constructor.
	 */
	public PhaseParameterDialog() {
		super("Phase Plot");

		period = 0;
		epoch = 0;

		firstUse = true;

		// TODO: later perhaps we could incorporate the epoch strategy
		// pane along with the epoch text box so people could see the
		// effect (on the text box) of selecting a different strategy.
		// This assumes we can agree upon any other useful epoch determination
		// strategies.
		epochStrategy = new AlphaOmegaMeanJDEpochStrategy();

		Container contentPane = this.getContentPane();

		JPanel topPane = new JPanel();
		topPane.setLayout(new BoxLayout(topPane, BoxLayout.PAGE_AXIS));
		topPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		// Period
		topPane.add(createPeriodFieldPane());

		// Epoch
		topPane.add(Box.createRigidArea(new Dimension(75, 10)));
		topPane.add(createEpochFieldPane());

		// OK, Cancel
		topPane.add(createButtonPane());

		contentPane.add(topPane);

		// We want to be updated when a new star is loaded in order to
		// adjust our fields and instance variables.
		// Mediator.getInstance().getNewStarNotifier().addListener(this);

		this.pack();
		periodField.requestFocusInWindow();
	}

	/**
	 * Show the dialog.
	 */
	public void showDialog() {
		if (firstUse) {
			setLocationRelativeTo(MainFrame.getInstance().getContentPane());
			firstUse = false;
		}

		this.setVisible(true);
	}

	private JPanel createPeriodFieldPane() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
		panel.setBorder(BorderFactory.createTitledBorder("Period (days)"));

		periodField = new JTextField();
		periodField.setToolTipText("Enter period in days");
		panel.add(periodField);

		return panel;
	}

	private JPanel createEpochFieldPane() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
		panel
				.setBorder(BorderFactory
						.createTitledBorder("Epoch (HJD)"));

		epochField = new JTextField();
		panel.add(epochField);

		return panel;
	}

	// private JPanel createEpochStrategyPane() {
	// JPanel panel = new JPanel();
	// panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
	// panel.setBorder(BorderFactory.createTitledBorder("Epoch Strategy"));
	//
	// ButtonGroup strategyGroup = new ButtonGroup();
	//
	// boolean first = true;
	//
	// for (String key : PhaseCalcs.epochStrategyMap.keySet()) {
	// IEpochStrategy strategy = PhaseCalcs.epochStrategyMap.get(key);
	// String strategyDesc = strategy.getDescription();
	//
	// JRadioButton strategyRadioButton = new JRadioButton(strategyDesc);
	// strategyRadioButton.setActionCommand(key);
	// .addActionListener(createEpochStrategyActionListener());
	// strategyRadioButton
	// panel.add(strategyRadioButton);
	// panel.add(Box.createRigidArea(new Dimension(10, 10)));
	//
	// strategyGroup.add(strategyRadioButton);
	//
	// // Arbitrarily select the first strategy.
	// // TODO: should be able to set this as a Preference.
	// if (first) {
	// strategyRadioButton.setSelected(true);
	// epochStrategy = strategy;
	// first = false;
	// }
	// }
	//
	// assert (epochStrategy != null);
	//
	// // Without this, the bordered radio group will appear right-justified.
	// JPanel centeringPanel = new JPanel(new BorderLayout());
	// centeringPanel.add(panel, BorderLayout.CENTER);
	//
	// return centeringPanel;
	// }

	// private ActionListener createEpochStrategyActionListener() {
	// return new ActionListener() {
	// public void actionPerformed(ActionEvent e) {
	// String name = e.getActionCommand();
	// epochStrategy = PhaseCalcs.epochStrategyMap.get(name);
	// }
	// };
	// }

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
		String epochText = epochField.getText();

		if (periodText != null && epochText != null) {
			Matcher periodMatcher = realNumberPattern.matcher(periodText);
			if (periodMatcher.matches()) {
				String periodStr = periodMatcher.group(1);
				period = Double.parseDouble(periodStr);
			}

			Matcher epochMatcher = realNumberPattern.matcher(epochText);
			if (epochMatcher.matches()) {
				String epochStr = epochMatcher.group(1);
				epoch = Double.parseDouble(epochStr);
			}

			if (period > 0 && epoch >= 0) {
				cancelled = false;
				setVisible(false);
				dispose();
			}
		}
	}

	/**
	 * @return the period
	 */
	public double getPeriod() {
		return period;
	}

	/**
	 * @return the epoch
	 */
	public double getEpoch() {
		return epoch;
	}

	// /**
	// * @return the epochStrategy
	// */
	// public IEpochStrategy getEpochStrategy() {
	// return epochStrategy;
	// }

	/**
	 * Update with new star message. We either want to clear the fields or if
	 * period or epoch values are available to us, populate the fields
	 * accordingly. We also obtain the observations for the newly loaded star to
	 * which to apply the epoch determination strategy in the case where there
	 * is no epoch available. TODO: presumably if there is a period, there is
	 * also an epoch. If not, should we flag this as an error or warning (via a
	 * dialog) and then just continue?
	 */
	public void update(NewStarMessage msg) {
		if (msg.getNewStarType() == NewStarType.NEW_STAR_FROM_DATABASE) {
			StarInfo info = msg.getStarInfo();
			Double period = info.getPeriod();
			Double epoch = info.getEpoch();

			if (period == null) {
				this.periodField.setText("");
				this.period = 0;
			} else {
				this.periodField.setText(period.toString());
				this.period = period;
			}

			if (epoch == null) {
				this.epoch = this.epochStrategy.determineEpoch(msg
						.getObservations());
				this.epochField.setText(this.epoch + "");
				this.epochField.setToolTipText(epochStrategy.getDescription());
			} else {
				this.epochField.setText(epoch.toString());
				this.epochField.setToolTipText("Default epoch");
				this.epoch = epoch;
			}
		} else {
			// Just clear the fields since we have no parameters
			// with which to pre-populate the fields.
			this.periodField.setText("");
			this.period = 0;

			this.epoch = this.epochStrategy.determineEpoch(msg
					.getObservations());
			this.epochField.setText(this.epoch + "");
			this.epochField.setToolTipText(epochStrategy.getDescription());
		}
	}
}
