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

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Calendar;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.aavso.tools.vstar.data.DateInfo;
import org.aavso.tools.vstar.data.validation.JulianDayValidator;
import org.aavso.tools.vstar.util.date.AbstractDateUtil;
import org.aavso.tools.vstar.util.locale.LocaleProps;
import org.aavso.tools.vstar.util.locale.NumberParser;
import org.aavso.tools.vstar.util.prefs.NumericPrecisionPrefs;

/**
 * This dialog allows the user to select a star.
 */
@SuppressWarnings("serial")
public class StarSelectorDialog extends AbstractOkCancelDialog {

	private static AbstractDateUtil dateUtil = AbstractDateUtil.getInstance();

	private Container contentPane;

	private StarGroupSelectionPane starGroupSelectionPane;
	private JTextField starField;
	private JTextField minJDField;
	private JTextField maxJDField;
	private JCheckBox allDataCheckBox;

	private JulianDayValidator jdValidator;

	private String starName;
	private String auid;
	private DateInfo minDate;
	private DateInfo maxDate;
	private boolean wantAllData;

	private Calendar cal;
	private int year, month, day;

	private static Pattern whitespacePattern = Pattern.compile("^\\s*$");

	// Regex pattern for AUID (AAVSO unique ID per star).
	private static Pattern auidPattern = Pattern
			.compile("^\\d{3}\\-\\w{3}\\-\\d{3}$");

	/**
	 * Constructor (singleton)
	 */
	private StarSelectorDialog() {
		super(LocaleProps.get("NEW_STAR_FROM_AID_DLG_TITLE"));

		this.starName = null;
		this.auid = null;
		this.minDate = null;
		this.maxDate = null;
		this.wantAllData = false;

		this.jdValidator = new JulianDayValidator();

		cal = Calendar.getInstance();
		year = cal.get(Calendar.YEAR);
		month = cal.get(Calendar.MONTH) + 1; // 0..11 -> 1..12
		day = cal.get(Calendar.DAY_OF_MONTH);

		contentPane = this.getContentPane();

		JPanel topPane = new JPanel();
		topPane.setLayout(new BoxLayout(topPane, BoxLayout.PAGE_AXIS));
		topPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		topPane
				.setToolTipText("Select a star from drop-down or enter a name, AUID or alias.");

		JPanel starFieldPane = createStarFieldPane();
		starGroupSelectionPane = new StarGroupSelectionPane(starField);
		topPane.add(starGroupSelectionPane);
		topPane.add(Box.createRigidArea(new Dimension(10, 10)));
		topPane.add(starFieldPane);
		topPane.add(Box.createRigidArea(new Dimension(10, 10)));
		topPane.add(createMinJDFieldPane());
		topPane.add(Box.createRigidArea(new Dimension(10, 10)));
		topPane.add(createMaxJDFieldPane());
		topPane.add(Box.createRigidArea(new Dimension(10, 10)));
		topPane.add(createAllDataCheckBoxPane());
		topPane.add(Box.createRigidArea(new Dimension(10, 10)));
		topPane.add(createButtonPane());

		contentPane.add(topPane);

		// this.addWindowListener(this.createWindowListener());

		this.pack();
		starGroupSelectionPane.requestFocusInWindow();
	}

	// GUI components

	private JPanel createStarFieldPane() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
		panel.setBorder(BorderFactory.createTitledBorder(LocaleProps
				.get("NEW_STAR_FROM_AID_DLG_OTHER_STAR")));

		starField = new JTextField();
		starField.addActionListener(createStarFieldActionListener());
		starField.setToolTipText("Enter star name, alias or AUID");
		panel.add(starField);

		return panel;
	}

	private JPanel createMinJDFieldPane() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
		panel.setBorder(BorderFactory.createTitledBorder(LocaleProps
				.get("NEW_STAR_FROM_AID_DLG_MINIMUM_JD")));

		double jd = dateUtil.calendarToJD(year - 2, month, day);
		minJDField = new JTextField(String.format(NumericPrecisionPrefs
				.getTimeOutputFormat(), jd));
		minJDField.addActionListener(createMinJDFieldActionListener());
		minJDField.addFocusListener(createMinJDFieldFocusListener());
		minJDField.setToolTipText(dateUtil.jdToCalendar(jd));
		panel.add(minJDField);

		return panel;
	}

	private JPanel createMaxJDFieldPane() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
		panel.setBorder(BorderFactory.createTitledBorder(LocaleProps
				.get("NEW_STAR_FROM_AID_DLG_MAXIMUM_JD")));

		double jd = dateUtil.calendarToJD(year, month, day);
		maxJDField = new JTextField(String.format(NumericPrecisionPrefs
				.getTimeOutputFormat(), jd));
		maxJDField.addActionListener(createMaxJDFieldActionListener());
		maxJDField.addFocusListener(createMaxJDFieldFocusListener());
		maxJDField.setToolTipText(dateUtil.jdToCalendar(jd));
		panel.add(maxJDField);

		return panel;
	}

	private JPanel createAllDataCheckBoxPane() {
		JPanel panel = new JPanel();

		allDataCheckBox = new JCheckBox(LocaleProps
				.get("NEW_STAR_FROM_AID_DLG_ALL_DATA"));
		allDataCheckBox
				.addActionListener(createAllDataCheckBoxActionListener());
		panel.add(allDataCheckBox, BorderLayout.CENTER);
		panel.add(allDataCheckBox);

		return panel;
	}

	// Event handlers

	// Return a listener for the star field.
	private ActionListener createStarFieldActionListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Nothing to do
			}
		};
	}

	// Return listeners for the minimum Julian Day field.

	private ActionListener createMinJDFieldActionListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// checkInput();
				minJDField.setToolTipText(dateUtil.jdToCalendar(Double
						.parseDouble(minJDField.getText())));
			}
		};
	}

	private FocusListener createMinJDFieldFocusListener() {
		return new FocusListener() {
			String prevString = "";

			public void focusGained(FocusEvent e) {
			}

			public void focusLost(FocusEvent e) {
				String current = minJDField.getText();
				if (!prevString.equals(current)) {
					minJDField.setToolTipText(dateUtil.jdToCalendar(Double
							.parseDouble(current)));
					prevString = current;
				}
			}
		};
	}

	// Return a listener for the all-data checkbox.
	private ActionListener createAllDataCheckBoxActionListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (allDataCheckBox.isSelected()) {
					// We want all data, so clear date fields.
					// minJDField.setText("");
					// maxJDField.setText("");
					minJDField.setEnabled(false);
					maxJDField.setEnabled(false);
				} else {
					// We've unchecked all data so we want to reenable fields
					// leaving the values that are there.
					minJDField.setEnabled(true);
					maxJDField.setEnabled(true);
				}
			}
		};
	}

	// Return listeners for the maximum Julian Day field.

	private ActionListener createMaxJDFieldActionListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// checkInput();
				maxJDField.setToolTipText(dateUtil.jdToCalendar(Double
						.parseDouble(maxJDField.getText())));
			}
		};
	}

	private FocusListener createMaxJDFieldFocusListener() {
		return new FocusListener() {
			String prevString = "";

			public void focusGained(FocusEvent e) {
			}

			public void focusLost(FocusEvent e) {
				String current = maxJDField.getText();
				if (!prevString.equals(current)) {
					maxJDField.setToolTipText(dateUtil.jdToCalendar(Double
							.parseDouble(current)));
					prevString = current;
				}
			}
		};
	}

	// Check that we have valid input in an appropriate subset
	// of dialog widgets. The dialog will not be dismissed until
	// there is a valid star selection and date range or the
	// all-data checkbox is selected.
	private void checkInput() {
		String text = starField.getText();
		if (!whitespacePattern.matcher(text).matches()) {
			// If text box is not empty, prioritise it over 10-star
			// drop-down menu. AUID or star name?
			text = sanitise(text);
			if (auidPattern.matcher(text).matches()) {
				auid = text.trim();
			} else {
				starName = text.trim();
			}
		} else {
			// There's nothing in the text field, so use the
			// selected star group item. Note that by only
			// setting AUID, we will force the lookup of star
			// info from the database, at least the name, but
			// also period and epoch if they are available.
			auid = starGroupSelectionPane.getSelectedAUID();
		}

		// Is the all-data checkbox selected?
		wantAllData = allDataCheckBox.isSelected();

		if (!wantAllData) {
			// Valid Julian Date range?
			try {
				String minJDText = minJDField.getText();
				minDate = new DateInfo(NumberParser.parseDouble(minJDText));
			} catch (NumberFormatException e) {
				MessageBox.showErrorDialog(this, "Minimum Julian Day", e);
			}

			try {
				String maxJDText = maxJDField.getText();
				maxDate = new DateInfo(NumberParser.parseDouble(maxJDText));
			} catch (NumberFormatException e) {
				MessageBox.showErrorDialog(this, "Maximum Julian Day", e);
			}
		}

		// Can we dismiss the dialog?
		if ((starName != null || auid != null)
				&& ((minDate != null && maxDate != null) || wantAllData)) {
			cancelled = false;
			setVisible(false);
			dispose();
		}
	}

	private String sanitise(String str) {
		return str.replace("\'", "");
	}

	// Getters

	/**
	 * @return the starName; a valid value is null in the case where an auid is
	 *         entered in the "Other Star" field rather than being selected from
	 *         the pull-down menu.
	 */
	public String getStarName() {
		return starName;
	}

	/**
	 * @return the auid; a valid value is null in the case where a name is
	 *         entered in the "Other Star" field rather than being selected from
	 *         the pull-down menu.
	 */
	public String getAuid() {
		return auid;
	}

	/**
	 * @return the minDate
	 */
	public DateInfo getMinDate() {
		return minDate;
	}

	/**
	 * @return the maxDate
	 */
	public DateInfo getMaxDate() {
		return maxDate;
	}

	/**
	 * @return return whether we want all the data
	 */
	public boolean wantAllData() {
		return wantAllData;
	}

	/**
	 * @return has the dialog been cancelled? TODO: isn't this in base class?
	 */
	public boolean isCancelled() {
		return cancelled;
	}

	protected void cancelAction() {
		// Nothing to do.
	}

	protected void okAction() {
		checkInput();
	}

	/**
	 * Reset this dialog's state so that we don't process old state. This is
	 * invoked by the base class's showDialog() method.
	 */
	public void reset() {
		// These fields will either be set to non-null values
		// by checkInput() before the dialog is dismissed, or
		// they will be irrelevant if wantAllData is true.
		this.auid = null;
		this.starName = null;
		this.minDate = null;
		this.maxDate = null;
		this.wantAllData = false;

		starGroupSelectionPane.refreshGroups();
	}

	// Singleton

	private static StarSelectorDialog instance = new StarSelectorDialog();

	public static StarSelectorDialog getInstance() {
		return instance;
	}
}
