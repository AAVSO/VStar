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
package org.aavso.tools.vstar.ui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.aavso.tools.vstar.data.DateInfo;
import org.aavso.tools.vstar.data.validation.JulianDayValidator;
import org.aavso.tools.vstar.exception.ObservationValidationError;
import org.aavso.tools.vstar.util.AbstractDateUtil;

/**
 * This dialog allows the user to select a star.
 */
public class StarSelectorDialog extends JDialog {

	private static AbstractDateUtil dateUtil = AbstractDateUtil.getInstance();

	private Map<String, String> tenStarMap;

	private Container contentPane;

	private JComboBox tenStarSelector;
	private JTextField starField;
	private JTextField minJDField;
	private JTextField maxJDField;

	private JulianDayValidator jdValidator;

	private String starName;
	private String auid;
	private DateInfo minDate;
	private DateInfo maxDate;

	private boolean cancelled;

	private Calendar cal;
	private int year, month, day;
	
	/**
	 * Constructor
	 */
	public StarSelectorDialog() {
		super();
		this.setTitle("Select a Star");
		this.setModal(true);

		this.starName = null;
		this.auid = null;
		this.minDate = null;
		this.maxDate = null;

		this.jdValidator = new JulianDayValidator();

		this.cancelled = false;

		cal = Calendar.getInstance();
		year = cal.get(Calendar.YEAR);
		month = cal.get(Calendar.MONTH)+1; // 0..11 -> 1..12
		day = cal.get(Calendar.DAY_OF_MONTH);
		
		createTenStarMap();

		contentPane = this.getContentPane();

		JPanel topPane = new JPanel();
		topPane.setLayout(new BoxLayout(topPane, BoxLayout.PAGE_AXIS));
		topPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		topPane
				.setToolTipText("Select a star from drop-down or enter a name, AUID or alias.");

		// topPane.add(new
		// JLabel("Select star from drop-down or\nenter name/AUID/alias"));
		// topPane.add(Box.createRigidArea(new Dimension(10, 10)));
		topPane.add(createTenStarSelectorPane());
		topPane.add(Box.createRigidArea(new Dimension(10, 10)));
		topPane.add(createStarFieldPane());
		topPane.add(Box.createRigidArea(new Dimension(10, 10)));
		topPane.add(createMinJDFieldPane());
		topPane.add(Box.createRigidArea(new Dimension(10, 10)));
		topPane.add(createMaxJDFieldPane());
		topPane.add(Box.createRigidArea(new Dimension(10, 10)));
		topPane.add(createButtonPane());
		contentPane.add(topPane);

		this.pack();
		tenStarSelector.requestFocusInWindow();
		this.setLocationRelativeTo(MainFrame.getInstance().getContentPane());
		this.setVisible(true);
	}

	// We know the AUID of the CitizenSky ten-stars, so there is
	// no need to query the database for these.
	// TODO: put these in a properties file?
	private void createTenStarMap() {
		tenStarMap = new TreeMap<String, String>();
		tenStarMap.put("Alpha Orionis", "000-BDY-978");
		tenStarMap.put("Beta Lyrae", "000-BDB-937");
		tenStarMap.put("Beta Persei", "000-BCY-922");
		tenStarMap.put("Delta Cephei", "000-BDC-570");
		tenStarMap.put("Epsilon Aurigae", "000-BCT-905");
		tenStarMap.put("Eta Aquilae", "000-BCT-763");
		tenStarMap.put("Eta Geminorum", "000-BBK-904");
		tenStarMap.put("Gamma Cassiopeia", "000-BCY-660");
		tenStarMap.put("Miu Cephei", "000-BCP-244");
		tenStarMap.put("R Lyrae", "000-BCD-657");
	}

	// GUI components

	private JPanel createTenStarSelectorPane() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
		panel.setBorder(BorderFactory
				.createTitledBorder("CitizenSky \"10 Stars\""));

		tenStarSelector = new JComboBox(tenStarMap.keySet().toArray(
				new String[0]));
		tenStarSelector
				.addActionListener(createTenStarSelectorActionListener());
		panel.add(tenStarSelector);

		return panel;
	}

	private JPanel createStarFieldPane() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
		panel.setBorder(BorderFactory.createTitledBorder("Arbitrary Star"));

		starField = new JTextField();
		starField.addActionListener(createStarFieldActionListener());
		starField.setToolTipText("Enter star name, alias or AUID");
		starField.setEnabled(false);
		panel.add(starField);

		panel.setEnabled(false);

		return panel;
	}

	private JPanel createMinJDFieldPane() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
		panel.setBorder(BorderFactory.createTitledBorder("Minimum Julian Day"));

		double jd = dateUtil.calendarToJD(year-2, month, day);
		minJDField = new JTextField(jd + "");
		minJDField.addActionListener(createMinJDFieldActionListener());
		minJDField
				.setToolTipText(dateUtil.jdToCalendar(jd));
		panel.add(minJDField);

		return panel;
	}

	private JPanel createMaxJDFieldPane() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
		panel.setBorder(BorderFactory.createTitledBorder("Maximum Julian Day"));

		double jd = dateUtil.calendarToJD(year, month, day);
		maxJDField = new JTextField(jd + "");
		maxJDField.addActionListener(createMaxJDFieldActionListener());
		maxJDField
				.setToolTipText(dateUtil.jdToCalendar(jd));
		panel.add(maxJDField);

		return panel;
	}

	private JPanel createButtonPane() {
		JPanel panel = new JPanel(new BorderLayout());

		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(createCancelButtonListener());
		panel.add(cancelButton, BorderLayout.LINE_START);

		JButton okButton = new JButton("OK");
		okButton.addActionListener(createOKButtonListener());
		// okButton.setEnabled(false);
		panel.add(okButton, BorderLayout.LINE_END);

		return panel;
	}

	// Event handlers

	// TODO: may want to get rid of all action listeners except for
	// buttons

	// Return a listener for the 10-star selector.
	private ActionListener createTenStarSelectorActionListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// checkInput();
			}
		};
	}

	// Return a listener for the star field.
	private ActionListener createStarFieldActionListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// checkInput();
			}
		};
	}

	// Return a listener for the minimum Julian Day field.
	private ActionListener createMinJDFieldActionListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// checkInput();
			}
		};
	}

	// Return a listener for the maximum Julian Day field.
	private ActionListener createMaxJDFieldActionListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// checkInput();
			}
		};
	}

	// Return a listener for the "OK" button.
	private ActionListener createOKButtonListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				checkInput();
			}
		};
	}

	// Return a listener for the "cancel" button.
	private ActionListener createCancelButtonListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cancelled = true;
				setVisible(false);
				dispose();
			}
		};
	}

	private void checkInput() {
		// TODO: check if text box is empty; if not, prioritise it

		starName = (String) tenStarSelector.getSelectedItem();
		auid = tenStarMap.get(starName);

		try {
			minDate = jdValidator.validate(minJDField.getText());
		} catch (ObservationValidationError ex) {
			MessageBox.showErrorDialog(MainFrame.getInstance(),
					"Minimum Julian Day", ex);
		}

		try {
			maxDate = jdValidator.validate(maxJDField.getText());
		} catch (ObservationValidationError ex) {
			MessageBox.showErrorDialog(MainFrame.getInstance(),
					"Maximum Julian Day", ex);
		}

		if (starName != null && auid != null && minDate != null
				&& maxDate != null) {
			setVisible(false);
			dispose();
		}
	}

	// Getters

	/**
	 * @return the starName
	 */
	public String getStarName() {
		return starName;
	}

	/**
	 * @return the auid
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
	 * @return has the dialog been cancelled?
	 */
	public boolean isCancelled() {
		return cancelled;
	}
}
