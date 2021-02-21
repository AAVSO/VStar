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
package org.aavso.tools.vstar.ui.dialog.prefs;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.util.locale.LocaleProps;
import org.aavso.tools.vstar.util.prefs.NumericPrecisionPrefs;

/**
 * This preferences pane permits the selection of numeric precision (primarily
 * as it relates to the number of decimal places in output, and in some cases,
 * input).
 */
@SuppressWarnings("serial")
public class NumericPrecisionSelectionPane extends JPanel implements
		IPreferenceComponent {

	private JSpinner timeDecimalPlacesSpinner;
	private JSpinner magDecimalPlacesSpinner;
	private JSpinner otherDecimalPlacesSpinner;

	private Integer changedTimeDecimalPlacesValue = null;
	private Integer changedMagDecimalPlacesValue = null;
	private Integer changedOtherDecimalPlacesValue = null;

	/**
	 * Constructor.
	 */
	public NumericPrecisionSelectionPane() {
		super();

		JPanel decimalPlacesPane = new JPanel();
		decimalPlacesPane.setLayout(new BoxLayout(decimalPlacesPane,
				BoxLayout.PAGE_AXIS));
		decimalPlacesPane
				.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		JTextArea infoText = new JTextArea(
				"Specify numeric precision in terms of decimal\n"
						+ "places for different categories of data.");
		infoText.setEditable(false);
		infoText.setBorder(BorderFactory.createEtchedBorder());
		decimalPlacesPane.add(infoText);

		decimalPlacesPane.add(Box.createRigidArea(new Dimension(10, 50)));

		// Add decimal place spinners.

		timeDecimalPlacesSpinner = createDecimalPlacesSpinner(
				NumericPrecisionPrefs.getTimeDecimalPlaces(),
				"Time (JD, phase)");
		decimalPlacesPane.add(timeDecimalPlacesSpinner);
		timeDecimalPlacesSpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				changedTimeDecimalPlacesValue = (Integer) timeDecimalPlacesSpinner
						.getValue();

			}
		});

		decimalPlacesPane.add(Box.createRigidArea(new Dimension(10, 10)));

		magDecimalPlacesSpinner = createDecimalPlacesSpinner(
				NumericPrecisionPrefs.getMagDecimalPlaces(),
				"Magnitude (including error or uncertainty)");
		decimalPlacesPane.add(magDecimalPlacesSpinner);
		magDecimalPlacesSpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				changedMagDecimalPlacesValue = (Integer) magDecimalPlacesSpinner
						.getValue();
			}
		});

		decimalPlacesPane.add(Box.createRigidArea(new Dimension(10, 10)));

		otherDecimalPlacesSpinner = createDecimalPlacesSpinner(
				NumericPrecisionPrefs.getOtherDecimalPlaces(),
				"All other values (e.g. period)");
		decimalPlacesPane.add(otherDecimalPlacesSpinner);
		otherDecimalPlacesSpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				changedOtherDecimalPlacesValue = (Integer) otherDecimalPlacesSpinner
						.getValue();
			}
		});

		decimalPlacesPane.add(Box.createRigidArea(new Dimension(10, 10)));

		// Add a local context button pane.
		decimalPlacesPane.add(createButtonPane());

		this.add(decimalPlacesPane);
	}

	private JSpinner createDecimalPlacesSpinner(int initial, String title) {
		SpinnerNumberModel decimalPlacesSpinnerModel = new SpinnerNumberModel(
				initial, 1, 15, 1);
		JSpinner spinner = new JSpinner(decimalPlacesSpinnerModel);
		spinner.setBorder(BorderFactory.createTitledBorder(title));
		return spinner;
	}

	protected JPanel createButtonPane() {
		JPanel panel = new JPanel(new BorderLayout());

		JButton setDefaultsButton = new JButton("Set Default Precision Values");
		setDefaultsButton
				.addActionListener(createSetDefaultsButtonActionListener());
		panel.add(setDefaultsButton, BorderLayout.LINE_START);

		JButton applyButton = new JButton(LocaleProps.get("APPLY_BUTTON"));
		applyButton.addActionListener(createApplyButtonActionListener());
		panel.add(applyButton, BorderLayout.LINE_END);

		return panel;
	}

	// Set defaults action button listener.
	private ActionListener createSetDefaultsButtonActionListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				NumericPrecisionPrefs.setDefaultDecimalPlacePrefs();
				reset();
				updateContentPane();
			}
		};
	}

	// Set apply button listener.
	private ActionListener createApplyButtonActionListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				update();
			}
		};
	}
	
	private void updateContentPane() {
		// Make changes visible immediately if a tab with data grid is active.  
		Mediator.getUI().getContentPane().repaint();
	}

	/**
	 * Updates the decimal places preferences with any changed values.
	 */
	@Override
	public void update() {
		boolean delta = false;

		if (changedTimeDecimalPlacesValue != null) {
			NumericPrecisionPrefs
					.setTimeDecimalPlaces(changedTimeDecimalPlacesValue);
			delta = true;
			changedTimeDecimalPlacesValue = null;
		}

		if (changedMagDecimalPlacesValue != null) {
			NumericPrecisionPrefs
					.setMagDecimalPlaces(changedMagDecimalPlacesValue);
			delta = true;
			changedMagDecimalPlacesValue = null;
		}

		if (changedOtherDecimalPlacesValue != null) {
			NumericPrecisionPrefs
					.setOtherDecimalPlaces(changedOtherDecimalPlacesValue);
			delta = true;
			changedOtherDecimalPlacesValue = null;
		}

		if (delta) {
			NumericPrecisionPrefs.storeDecimalPlacesPrefs();
			updateContentPane();
		}
	}

	/**
	 * Prepare this pane for use by resetting whatever needs to be, namely no
	 * values are to be considered as having changed and the spinners need to be
	 * updated from the preference values.
	 */
	@Override
	public void reset() {
		changedTimeDecimalPlacesValue = null;
		changedMagDecimalPlacesValue = null;
		changedOtherDecimalPlacesValue = null;

		timeDecimalPlacesSpinner.setValue(NumericPrecisionPrefs
				.getTimeDecimalPlaces());
		magDecimalPlacesSpinner.setValue(NumericPrecisionPrefs
				.getMagDecimalPlaces());
		otherDecimalPlacesSpinner.setValue(NumericPrecisionPrefs
				.getOtherDecimalPlaces());
	}
}
