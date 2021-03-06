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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.NewStarType;
import org.aavso.tools.vstar.ui.mediator.StarInfo;
import org.aavso.tools.vstar.ui.mediator.message.NewStarMessage;
import org.aavso.tools.vstar.util.locale.LocaleProps;
import org.aavso.tools.vstar.util.locale.NumberParser;
import org.aavso.tools.vstar.util.notification.Listener;
import org.aavso.tools.vstar.util.prefs.NumericPrecisionPrefs;
import org.aavso.tools.vstar.util.stats.epoch.AlphaOmegaMeanJDEpochStrategy;
import org.aavso.tools.vstar.util.stats.epoch.IEpochStrategy;

/**
 * This class represents a dialog to obtain parameters for phase plot
 * calculation: period, epoch.
 */
@SuppressWarnings("serial")
public class PhaseParameterDialog extends AbstractOkCancelDialog implements
		Listener<NewStarMessage>, PropertyChangeListener {

	private JTextField periodField;
	private JTextField epochField;

	private double period;
	private double epoch;
	private IEpochStrategy epochStrategy;

	private boolean firstUse; // TODO: also in base class!

	// TODO: so we can re-create this dialog each time from scratch, use
	// createPhasePlot() in Mediator to store the last phase and epoch and use
	// it here if present. The new star notifier would then clear it in the
	// Mediator. See also other TODO below.

	/**
	 * Constructor.
	 */
	public PhaseParameterDialog() {
		super(LocaleProps.get("PHASE_PARAMETER_DLG_PHASE_TITLE"));

		period = 0;
		epoch = 0;

		firstUse = true;

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

		this.pack();
		periodField.requestFocusInWindow();
	}

	/**
	 * Show the dialog. TODO: this is on the base class! refactor!
	 */
	public void showDialog() {
		if (firstUse) {
			setLocationRelativeTo(Mediator.getUI().getContentPane());
			firstUse = false;
		}

		this.setCancelled(true);
		this.setVisible(true);
	}

	private JPanel createPeriodFieldPane() {
		JPanel panel = new JPanel();

		panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
		panel.setBorder(BorderFactory.createTitledBorder(LocaleProps
				.get("PHASE_PARAMETER_DLG_PERIOD")
				+ " (" + LocaleProps.get("PHASE_PARAMETER_DLG_DAYS") + ")"));

		periodField = new JTextField();
		// periodField.setToolTipText("Enter period in days");
		// periodField.addPropertyChangeListener(this);
		panel.add(periodField);

		return panel;
	}

	private JPanel createEpochFieldPane() {
		JPanel panel = new JPanel();

		panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
		// TODO: show "(JD/HJD)"?
		panel.setBorder(BorderFactory.createTitledBorder(LocaleProps
				.get("PHASE_PARAMETER_DLG_EPOCH")
				+ " (" + LocaleProps.get("JD") + ")"));

		epochField = new JTextField();
		// epochField.setToolTipText("Enter epoch as JD");
		// epochField.addPropertyChangeListener(this);
		panel.add(epochField);

		return panel;
	}

	/**
	 * Field change handler for period and epoch fields to set tool-tips when
	 * fields cleared. TODO: Doesn't get fired when the fields are cleared by a
	 * user. Do we need JFormattedTextField instead?
	 */
	public void propertyChange(PropertyChangeEvent e) {
		Object source = e.getSource();

		if (source == this.periodField) {
			if ("".equals(this.periodField.getText())) {
				// this.periodField.setToolTipText("Enter period in days");
			}
		} else if (source == this.epochField) {
			if ("".equals(this.epochField.getText())) {
				// this.epochField.setToolTipText("Enter epoch as JD");
			}
		}
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
		String epochText = epochField.getText();

		if (periodText != null && epochText != null) {
			try {
				period = NumberParser.parseDouble(periodText);
				epoch = NumberParser.parseDouble(epochText);

				if (period > 0 && epoch >= 0) {
					cancelled = false;
					setVisible(false);
					dispose();
				}
			} catch (NumberFormatException e) {
				// Nothing to do. The dialog stays open.
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

	/**
	 * Set the period field.
	 * 
	 * @param period
	 *            the period to set
	 */
	public void setPeriodField(double period) {
		this.periodField.setText(NumericPrecisionPrefs.formatOther(period));
	}

	/**
	 * Set the epoch field.
	 * 
	 * @param epoch
	 *            the epoch to set
	 */
	public void setEpochField(double epoch) {
		this.epochField.setText(NumericPrecisionPrefs.formatTime(epoch));
	}

	/**
	 * Update from new star message. We either want to clear the fields or if
	 * period or epoch values are available to us, populate the fields
	 * accordingly. We also obtain the observations for the newly loaded star to
	 * which to apply the epoch determination strategy in the case where there
	 * is no epoch available.
	 */
	public void update(NewStarMessage msg) {
		if (msg.getNewStarType() == NewStarType.NEW_STAR_FROM_DATABASE) {
			// We may have period and/or epoch information from
			// the database.
			StarInfo info = msg.getStarInfo();
			Double period = info.getPeriod();
			Double epoch = info.getEpoch();

			if (period == null) {
				// No period available, so just clear the field.
				this.periodField.setText("");
				this.periodField.setToolTipText("Enter period in days");
				this.period = 0;
			} else {
				// Use the supplied period to set the period field.
				this.periodField.setText(NumericPrecisionPrefs
						.formatOther(period));
				// this.periodField.setToolTipText("Period in days");
				this.period = period;
			}

			if (epoch == null) {
				// No epoch available, so use the epoch strategy to
				// set the epoch field.
				this.epoch = this.epochStrategy.determineEpoch(msg
						.getObservations());
				this.epochField.setText(NumericPrecisionPrefs
						.formatTime(this.epoch));
				// this.epochField.setToolTipText(epochStrategy.getDescription());
			} else {
				// Use the supplied epoch to set the epoch field.
				this.epochField
						.setText(NumericPrecisionPrefs.formatTime(epoch));
				// this.epochField.setToolTipText("Epoch as HJD");
				this.epoch = epoch;
			}
		} else {
			// No period available, so just clear the field.
			this.periodField.setText("");
			// this.periodField.setToolTipText("Enter period in days");
			this.period = 0;

			// Use the epoch strategy to set the epoch field.
			this.epoch = this.epochStrategy.determineEpoch(msg
					.getObservations());
			this.epochField.setText(NumericPrecisionPrefs
					.formatTime(this.epoch));
			// this.epochField.setToolTipText(epochStrategy.getDescription());
		}
	}

	/**
	 * @see org.aavso.tools.vstar.util.notification.Listener#canBeRemoved()
	 */
	public boolean canBeRemoved() {
		return false;
	}
}
