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
import java.util.Calendar;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.aavso.tools.vstar.data.DateInfo;
import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.plugin.PluginComponentFactory;
import org.aavso.tools.vstar.ui.dialog.series.AIDSeriesSelectionPane;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.util.Pair;
import org.aavso.tools.vstar.util.date.AbstractDateUtil;
import org.aavso.tools.vstar.util.help.Help;
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
	private TextArea obsCodesField;
	private TextArea velaFilterField;
	private JCheckBox allDataCheckBox;
	private JCheckBox additiveLoadCheckbox;
	private JCheckBox minFieldsCheckbox;

	// TODO: add a show counts button/checkbox that displays the counts for data
	// based upon the criteria specified in a dialog similar to Info

	private AIDSeriesSelectionPane aidSeriesPane;

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

	private String pluginDocName = null;

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

		cal = Calendar.getInstance();
		// Ensure the JD is well past today by adding one day, so that recently
		// added observations in AID will fall into the JD range. See ticket
		// #509 Make
		// default JD range slightly into future for AID loads:
		// https://sourceforge.net/p/vstar/bugs-and-features/509/
		cal.add(Calendar.DAY_OF_MONTH, 1);
		year = cal.get(Calendar.YEAR);
		month = cal.get(Calendar.MONTH) + 1; // 0..11 -> 1..12
		day = cal.get(Calendar.DAY_OF_MONTH);

		contentPane = this.getContentPane();

		JPanel leftPane = new JPanel();
		leftPane.setLayout(new BoxLayout(leftPane, BoxLayout.PAGE_AXIS));
		leftPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		leftPane.setToolTipText("Select a star from drop-down or enter a name, AUID or alias.");

		JPanel starFieldPane = createStarFieldPane();
		starGroupSelectionPane = new StarGroupSelectionPane(starField, false);
		leftPane.add(starGroupSelectionPane);

		leftPane.add(Box.createRigidArea(new Dimension(10, 10)));
		leftPane.add(starFieldPane);
		leftPane.add(Box.createRigidArea(new Dimension(10, 10)));
		leftPane.add(createMinJDFieldPane());
		leftPane.add(Box.createRigidArea(new Dimension(10, 10)));
		leftPane.add(createMaxJDFieldPane());
		leftPane.add(Box.createRigidArea(new Dimension(10, 10)));

		leftPane.add(createOptionsPane());

		JPanel rightPane = new JPanel();
		rightPane.setLayout(new BoxLayout(rightPane, BoxLayout.PAGE_AXIS));
		rightPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		rightPane.add(createSeriesSelectionPane());
		rightPane.add(createObsCodesPane());
		leftPane.add(Box.createRigidArea(new Dimension(10, 10)));
		rightPane.add(createVeLaFilterPane());

		// Default layout manager of content pane is BorderLayout
		contentPane.add(leftPane, BorderLayout.LINE_START);
		contentPane.add(rightPane, BorderLayout.LINE_END);
		//contentPane.add(createButtonPane(), BorderLayout.PAGE_END);
		contentPane.add(createButtonPane2(), BorderLayout.PAGE_END);

		// this.addWindowListener(this.createWindowListener());

		this.pack();
		starGroupSelectionPane.requestFocusInWindow();
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
	 * Return whether or not the load is additive.
	 * 
	 * @return Whether or not the load is additive.
	 */
	public boolean isLoadAdditive() {
		return additiveLoadCheckbox.isSelected();
	}

	/**
	 * Return whether or not to load minimal fields.
	 * 
	 * @return Whether or not to load minimal fields.
	 */
	public boolean loadMinimalFields() {
		return minFieldsCheckbox.isSelected();
	}

	/**
	 * Return the selected series.
	 */
	public List<SeriesType> getSelectedSeries() {
		return aidSeriesPane.getSelectedSeries();
	}

	/**
	 * Return a comma-delimited string of observer codes or null if there are
	 * none.
	 */
	public String getObsCodes() {
		String obscodes = null;

		String text = obsCodesField.getValue();
		if (text.trim().length() > 0) {
			StringBuffer obscodesBuf = new StringBuffer();
			String[] fields = text.split("\\s+");

			for (int i = 0; i < fields.length; i++) {
				obscodesBuf.append(fields[i].trim());
				if (i < fields.length - 1) {
					obscodesBuf.append(",");
				}
			}

			obscodes = obscodesBuf.toString();
		}

		return obscodes;
	}

	/**
	 * Returns the content of the VeLa filter field.
	 * 
	 * @return the string content of the VeLa filter field.
	 */
	public String getVeLaFilter() {
		return velaFilterField.getValue().trim();
	}

	/**
	 * @return has the dialog been cancelled? TODO: isn't this in base class?
	 */
	public boolean isCancelled() {
		return cancelled;
	}

	// Setters

	/**
	 * Set the star field text.
	 * 
	 * @param text
	 *            The text to be set in the star field.
	 */
	public void setStarField(String text) {
		starField.setText(text);
	}

	public void setPluginDocName(String pluginDocName) {
		this.pluginDocName = pluginDocName;
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
		minJDField = new JTextField(NumericPrecisionPrefs.formatTime(jd));
		//minJDField.addActionListener(createMinJDFieldActionListener());
		//minJDField.addFocusListener(createMinJDFieldFocusListener());
		minJDField.getDocument().addDocumentListener(createJDFieldDocumentListener(minJDField));
		minJDField.setToolTipText(dateUtil.jdToCalendar(jd));
		panel.add(minJDField);
		
		JButton convButton = new JButton("...");
		convButton.setName("ButtonMinJD");
		convButton.addActionListener(createConvButtonListener());
		panel.add(convButton);

		return panel;
	}

	private JPanel createMaxJDFieldPane() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
		panel.setBorder(BorderFactory.createTitledBorder(LocaleProps
				.get("NEW_STAR_FROM_AID_DLG_MAXIMUM_JD")));

		double jd = dateUtil.calendarToJD(year, month, day);
		maxJDField = new JTextField(NumericPrecisionPrefs.formatTime(jd));
		//maxJDField.addActionListener(createMaxJDFieldActionListener());
		//maxJDField.addFocusListener(createMaxJDFieldFocusListener());
		maxJDField.getDocument().addDocumentListener(createJDFieldDocumentListener(maxJDField));
		maxJDField.setToolTipText(dateUtil.jdToCalendar(jd));
		panel.add(maxJDField);
		
		JButton convButton = new JButton("...");
		convButton.setName("ButtonMaxJD");
		convButton.addActionListener(createConvButtonListener());
		panel.add(convButton);

		return panel;
	}

	private ActionListener createConvButtonListener() {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() instanceof JButton) {
					JButton b = (JButton)e.getSource();
					JTextField field = null;
					String dialogTitle = null;
					if ("ButtonMinJD".equals(b.getName())) {
						field = minJDField;
						dialogTitle = LocaleProps.get("NEW_STAR_FROM_AID_DLG_MINIMUM_JD");
					} else if ("ButtonMaxJD".equals(b.getName())) {
						field = maxJDField;
						dialogTitle = LocaleProps.get("NEW_STAR_FROM_AID_DLG_MAXIMUM_JD");
					}
					if (field != null) {
						String dateText = field.getText();
						Double date = NumberParser.parseDouble(dateText);
						DateToJdDialog dlg = new DateToJdDialog(dialogTitle);						
						dlg.setJD(date);
						dlg.showDialog();
						if (!dlg.isCancelled()) {
							field.setToolTipText(null);
							double jd = dlg.getJD(); 
							field.setText(NumericPrecisionPrefs.formatTime(jd));
							field.setToolTipText(dateUtil.jdToCalendar(jd));
						}
					}
				}
			}
		};
	}
	
	private DocumentListener createJDFieldDocumentListener(JTextField source) {
		return new DocumentListener() {
			
			private JTextField sourceField = source;
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				updateToolTip(e);
			}
			
			@Override
			public void removeUpdate(DocumentEvent e) {
				updateToolTip(e);
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
				updateToolTip(e);
			}
			
			private void updateToolTip(DocumentEvent e) {
				if (sourceField != null) {
					String calendarString = null;					
					String text = sourceField.getText().trim();
					if (!"".equals(text)) {
						try {
							double d = NumberParser.parseDouble(text);
							calendarString = dateUtil.jdToCalendar(d);
						} catch (Exception ex) {
							calendarString = null;
						}
					}
					sourceField.setToolTipText(calendarString);
					//System.out.println("Tooltip for " + sourceField.getName() + " was set to: " + (calendarString == null ? "<null>" : calendarString));					
				}
			}
		};
	}
	
	private JPanel createOptionsPane() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.setBorder(BorderFactory.createTitledBorder("Options"));

		panel.add(createAdditiveLoadCheckboxPane());
		panel.add(Box.createRigidArea(new Dimension(10, 10)));
		panel.add(createMinFieldsCheckBoxPane());
		panel.add(Box.createRigidArea(new Dimension(10, 10)));
		panel.add(createAllDataCheckBoxPane());

		return panel;
	}

	private JPanel createMinFieldsCheckBoxPane() {
		JPanel panel = new JPanel();

		minFieldsCheckbox = new JCheckBox("Minimal Fields?");
		panel.add(minFieldsCheckbox);

		return panel;
	}

	private JPanel createAdditiveLoadCheckboxPane() {
		JPanel panel = new JPanel();

		additiveLoadCheckbox = new JCheckBox("Add to current?");
		panel.add(additiveLoadCheckbox);

		return panel;
	}

	private JPanel createAllDataCheckBoxPane() {
		JPanel panel = new JPanel();

		allDataCheckBox = new JCheckBox(
				LocaleProps.get("NEW_STAR_FROM_AID_DLG_ALL_DATA"));
		allDataCheckBox
				.addActionListener(createAllDataCheckBoxActionListener());
		panel.add(allDataCheckBox, BorderLayout.CENTER);

		return panel;
	}

	private JPanel createSeriesSelectionPane() {
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createTitledBorder(""));

		aidSeriesPane = new AIDSeriesSelectionPane();
		panel.add(aidSeriesPane);

		return panel;
	}

	private JPanel createObsCodesPane() {
		Pair<TextArea, JPanel> pair = PluginComponentFactory
				.createTextAreaPane("Observer Codes",
						"Observer codes, separated by spaces", 1, 20);

		obsCodesField = pair.first;

		return pair.second;
	}

	/**
	 * This component creates a VeLa Filter pane.
	 */
	private JPanel createVeLaFilterPane() {
		Pair<TextArea, JPanel> pair = PluginComponentFactory
				.createVeLaFilterPane();
		velaFilterField = pair.first;

		return pair.second;
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

//	// Return listeners for the minimum Julian Day field.
//
//	private ActionListener createMinJDFieldActionListener() {
//		return new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				// checkInput();
//				minJDField.setToolTipText(dateUtil.jdToCalendar(NumberParser
//						.parseDouble(minJDField.getText())));
//			}
//		};
//	}
//
//	private FocusListener createMinJDFieldFocusListener() {
//		return new FocusListener() {
//			String prevString = "";
//
//			public void focusGained(FocusEvent e) {
//			}
//
//			public void focusLost(FocusEvent e) {
//				String current = minJDField.getText();
//				if (!prevString.equals(current)) {
//					minJDField.setToolTipText(dateUtil
//							.jdToCalendar(NumberParser.parseDouble(current)));
//					prevString = current;
//				}
//			}
//		};
//	}

	// Return a listener for the all-data checkbox.
	private ActionListener createAllDataCheckBoxActionListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (allDataCheckBox.isSelected()) {
					// Disable date fields to avoid usability confusion.
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

//	// Return listeners for the maximum Julian Day field.
//
//	private ActionListener createMaxJDFieldActionListener() {
//		return new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				// checkInput();
//				maxJDField.setToolTipText(dateUtil.jdToCalendar(NumberParser
//						.parseDouble(maxJDField.getText())));
//			}
//		};
//	}
//
//	private FocusListener createMaxJDFieldFocusListener() {
//		return new FocusListener() {
//			String prevString = "";
//
//			public void focusGained(FocusEvent e) {
//			}
//
//			public void focusLost(FocusEvent e) {
//				String current = maxJDField.getText();
//				if (!prevString.equals(current)) {
//					maxJDField.setToolTipText(dateUtil
//							.jdToCalendar(NumberParser.parseDouble(current)));
//					prevString = current;
//				}
//			}
//		};
//	}

	// Check that we have valid input in an appropriate subset
	// of dialog widgets. The dialog will not be dismissed until
	// there is an entry in the star text box and a date range
	// or the all-data checkbox is selected.
	private void checkInput() {
		String text = starField.getText();
		if (!whitespacePattern.matcher(text).matches()) {
			// AUID or star name?
			text = sanitise(text);
			if (auidPattern.matcher(text).matches()) {
				auid = text.trim();
			} else {
				starName = text.trim();
			}
		} else {
			// There's nothing in the text field, so use the
			// selected star group item. Note that by setting
			// the star name, we will force the lookup of star
			// info from the database.
			starName = starGroupSelectionPane.getSelectedStarName();
		}

		// Is the all-data checkbox selected?
		wantAllData = allDataCheckBox.isSelected();

		if (!wantAllData) {
			// Valid Julian Date range?
			try {
				String minJDText = minJDField.getText();
				minDate = new DateInfo(NumberParser.parseDouble(minJDText));
			} catch (NumberFormatException e) {
				MessageBox.showErrorDialog(Mediator.getUI().getComponent(),
						"Minimum Julian Day", e);
			}

			try {
				String maxJDText = maxJDField.getText();
				maxDate = new DateInfo(NumberParser.parseDouble(maxJDText));
			} catch (NumberFormatException e) {
				MessageBox.showErrorDialog(Mediator.getUI().getComponent(),
						"Maximum Julian Day", e);
			}
		}

		// Can we dismiss the dialog?
		if ((starName != null || auid != null)
				&& ((minDate != null && maxDate != null) || wantAllData)
				&& !getSelectedSeries().isEmpty()) {
			cancelled = false;
			setVisible(false);
			dispose();
		}
	}

	private String sanitise(String str) {
		return str.replace("\'", "");
	}

	protected void helpAction() {
		Help.openPluginHelp(pluginDocName);
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
	}

	@Override
	public void showDialog() {
		super.showDialog();
	}

	// Singleton

	private static StarSelectorDialog instance = new StarSelectorDialog();

	public static StarSelectorDialog getInstance() {
		return instance;
	}
}
