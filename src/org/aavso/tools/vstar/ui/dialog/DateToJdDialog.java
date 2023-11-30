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
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.YearMonth;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.aavso.tools.vstar.util.date.AbstractDateUtil;
import org.aavso.tools.vstar.util.date.YMD;

@SuppressWarnings("serial")
public class DateToJdDialog extends AbstractOkCancelDialog {
	private IntegerField yearField;
	private IntegerField monthField;
	private IntegerField dayField;
	private IntegerField hourField;
	private IntegerField minField;
	private DoubleFieldSeconds secField;
	
	private static final int MIN_YEAR = 1600;
	private static final int MAX_YEAR = 9999;
	
	// Julian day of the Java Epoch 1970-01-01T00:00:00Z
	private static final double JAVA_EPOCH_JD = 2440587.5;
	
	/**
	 * Constructor
	 */
	public DateToJdDialog(String title) {
		super(title);
		
		Container contentPane = this.getContentPane();

		JPanel topPane = new JPanel();
		topPane.setLayout(new BoxLayout(topPane, BoxLayout.PAGE_AXIS));
		topPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		topPane.add(createDatePane());
		topPane.add(createButtonPaneX());
		topPane.add(createButtonPane());
		
		contentPane.add(topPane);
		
		setCurrentTime();			
	    
		this.pack();
	}
	
	private JPanel createDatePane() {
		JPanel panel = new JPanel(new FlowLayout());

		yearField = new IntegerField("Year", MIN_YEAR, MAX_YEAR, 0);
		panel.add(setNumberFieldColumns(yearField, 5));
		
		monthField = new IntegerField("Mon", 1, 12, 0);
		panel.add(setNumberFieldColumns(monthField, 4));
		
		dayField = new IntegerField("Day", 1, 31, 0);
		panel.add(setNumberFieldColumns(dayField, 4));
		
		panel.add(new JLabel("  "));
		
		hourField = new IntegerField("Hr", 0, 23, 0);
		panel.add(setNumberFieldColumns(hourField, 4));
		
		minField = new IntegerField("Min", 0, 59, 0);
		panel.add(setNumberFieldColumns(minField, 4));
		
		secField = new DoubleFieldSeconds("Sec", 0.0, 59.99, 0.00);
		panel.add(setNumberFieldColumns(secField, 5));
		return panel;
	}

	protected void cancelAction() {
		// Nothing to do.
	}
	
	protected void okAction() {
		Double d = getJD();
		if (d != null) {
			cancelled = false;
			setVisible(false);
			dispose();
		}
	}
	
	private JPanel createButtonPaneX() {
		JPanel panel = new JPanel(new BorderLayout());
		
		JButton timeNowButton = new JButton("Current Time (UT)");			
		timeNowButton.addActionListener(createTimeNowButtonListener());
		panel.add(timeNowButton, BorderLayout.LINE_END);
		
		return panel;
	}
	
	// Return a listener for the "Now" button.
	private ActionListener createTimeNowButtonListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setCurrentTime();
			}
		};
	}
	
	private JTextField setNumberFieldColumns(NumberFieldBase<?> field, int columns) {
		JTextField textField = (JTextField)(field.getUIComponent());
		textField.setColumns(columns);
		return textField;
	}

	public void setJD(double jd) {
		setDateFromJd(jd);
	}
	
	public Double getJD() {
		return getJdFromDate();
	}
	
	// Set form fields to values corresponding to the current time
	private void setCurrentTime() {
		double jd = julianDayNow();
		setDateFromJd(jd);			
	}

	private void setDateFromJd(Double jd) {
		yearField.setValue(null);
		monthField.setValue(null);
		dayField.setValue(null);
		hourField.setValue(null);
		minField.setValue(null);
		secField.setValue(null);
		
		if (jd == null) return; 
		
		YMD ymd = AbstractDateUtil.getInstance().jdToYMD(jd);

		int year = ymd.getYear();
		int month = ymd.getMonth();
		double day = ymd.getDay();
		int iday = (int)day;
		
		int hour = (int)((day - iday) * 24.0);
		int min = (int)((day - iday - hour / 24.0) * 24.0 * 60.0);
		double sec = ((day - iday - hour / 24.0 - min / 24.0 / 60.0) * 24.0 * 60.0 * 60.0);

		// round seconds to 0.01, like in the input field.
		double rsec = Math.round(sec * 100.0) / 100.0;
		
		// After rounding, rsec can be up to 60.00.
		// This is a fix for the case when rounded seconds = 60: 
		if (rsec > 59.99) {
			// Create LocalDateTime object using integer number of seconds. 
			LocalDateTime aTime = LocalDateTime.of(year, month, iday, hour, min, 59);
			// Add 1 second.
			aTime = aTime.plusSeconds(1);
			// Now we can get correct values of Date/Time fields after adding a second (potentially, adding a second can change them all).
			year = aTime.getYear();
			month = aTime.getMonthValue();
			iday = aTime.getDayOfMonth();
			hour = aTime.getHour();
			min = aTime.getMinute();
			rsec = aTime.getSecond(); // should be 0 
		}
		
		yearField.setValue(year);
		monthField.setValue(month);
		dayField.setValue(iday);
		hourField.setValue(hour);
		minField.setValue(min);
		secField.setValue(rsec);
	}
	
	private Double getJdFromDate() {
		
		Integer year =	getAndCheck(yearField);
		if (year == null) return null;
		
		Integer month = getAndCheck(monthField);
		if (month == null) return null;
		
		Integer day = getAndCheck(dayField);
		YearMonth yearMonthObject = YearMonth.of(year, month);
		int daysInMonth = yearMonthObject.lengthOfMonth();
		if (day > daysInMonth) {
			MessageBox.showErrorDialog(this, "Error", dayField.getName() + ": Invalid number of days in the month!");
			(dayField.getUIComponent()).requestFocus();
			((JTextField)(dayField.getUIComponent())).selectAll();
			day = null;
		}
		if (day == null) return null;
		
		Integer hour = getAndCheck(hourField);
		if (hour == null) return null;

		Integer min = getAndCheck(minField);
		if (min == null) return null;

		Double sec = getAndCheck(secField);
		if (sec == null) return null;

		double dday = day + hour / 24.0 + min / 24.0 / 60.0 + sec / 24.0 / 60.0 / 60.0;   
		double jd = AbstractDateUtil.getInstance().calendarToJD(year, month, dday);
		
		return jd;
	}
	
	// Get value of IntegerField, show message in case of error and set focus to the field
	private Integer getAndCheck(IntegerField input) {
		Integer i;
		try {
			i = input.getValue();
		} catch (Exception e) {
			// We should never be here as soon as getValue catches parseInteger() exceptions.  
			i = null;
		}
		if (i == null) {
			MessageBox.showErrorDialog(this, "Error", input.getName() + ": Invalid value!\n" +
					"Value must be integer.\n" +
					numberFieldInfo(input));				
			(input.getUIComponent()).requestFocus();
			((JTextField)(input.getUIComponent())).selectAll();
		}
		return i;
	}

	// Get value of DoubleField, show message in case of error and set focus to the field
	private Double getAndCheck(DoubleField input) {
		Double d;
		try {
			d = input.getValue();
		} catch (Exception e) {
			// We should never be here as soon as getValue catches catches all possible exceptions.  
			d = null;
		}
		if (d == null) {
			MessageBox.showErrorDialog(this, "Error", input.getName() + ": Invalid value!\n" +
					numberFieldInfo(input));				
			(input.getUIComponent()).requestFocus();
			((JTextField)(input.getUIComponent())).selectAll();
		}
		return d;
	}
	
	private String numberFieldInfo(NumberFieldBase<?> field) {
		String s = "";
		if (field.getMin() == null && field.getMax() != null)
			s = "Only values <= " + field.getMax() + " allowed.";
		else if (field.getMin() != null && field.getMax() == null)
			s = "Only values >= " + field.getMin() + " allowed.";
		else if (field.getMin() != null && field.getMax() != null)
			s = "Only values between " + field.getMin() + " and " + field.getMax() + " allowed.";
		return s; 
	}
	
	// Current Julian Day value
    private static double julianDayNow()
    {
        return Instant.now().getEpochSecond() / (24.0 * 60.0 * 60.0) + JAVA_EPOCH_JD;
    }

	/*
	 * Unlike DoubleField, DoubleFieldTime uses special format
	 */
	private class DoubleFieldSeconds extends DoubleField {
	
		private DecimalFormat format = new DecimalFormat("#.##");
		
		public DoubleFieldSeconds(String name, Double min, Double max, Double initial) {
			super(name, min, max, initial);
			setValue(initial); // special formatting
		}
	
		@Override
		public void setValue(Double value) {
			textField.setText(value == null ? "" : format.format(value));
		}
		
	}
	
}
