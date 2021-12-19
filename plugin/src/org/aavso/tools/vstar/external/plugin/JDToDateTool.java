/**
 * 
 */
package org.aavso.tools.vstar.external.plugin;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.time.Instant;
import java.time.YearMonth;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import org.aavso.tools.vstar.plugin.GeneralToolPluginBase;
import org.aavso.tools.vstar.ui.dialog.MessageBox;
import org.aavso.tools.vstar.ui.dialog.NumberFieldBase;
import org.aavso.tools.vstar.ui.mediator.DocumentManager;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.resources.ResourceAccessor;
import org.aavso.tools.vstar.util.date.AbstractDateUtil;
import org.aavso.tools.vstar.util.date.YMD;
import org.aavso.tools.vstar.util.prefs.NumericPrecisionPrefs;
import org.aavso.tools.vstar.ui.dialog.DoubleField;
import org.aavso.tools.vstar.ui.dialog.IntegerField;

/**
 * A general tool plug-in that converts a JD to a calendar date and vice versa.
 */
public class JDToDateTool extends GeneralToolPluginBase {
	
	// JD <-> Date conversion procedures used here gives results differ from ones given by the
	// AAVSO JD Calculator (https://www.aavso.org/jd-calculator) for years < 1583.
	// This probably connected with the introduction of the Gregorian Calendar that year.
	// (1583 (MDLXXXIII) was a common year starting on Saturday of the Gregorian calendar -- Wikipedia)	
	// So I (PMAK) restricted the date range.
	// Min Date = 1600-01-01T00:00:00
	// Max Date = 9999-12-31T00:00:00
	
	private static final int MIN_YEAR = 1600;
	private static final int MAX_YEAR = 9999;
	
	// Julian day of the Java Epoch 1970-01-01T00:00:00Z
	private static final double JAVA_EPOCH_JD = 2440587.5;

	@Override
	public void invoke() {
		new JDToDateDialog();
	}

	@Override
	public String getDescription() {
		return "Convert JD to Calendar Date";
	}

	@Override
	public String getDisplayName() {
		return "JD to Calendar Date";
	}
	
	// Current Julian Day value
    private static double julianDayNow()
    {
        return Instant.now().getEpochSecond() / (24.0 * 60.0 * 60.0) + JAVA_EPOCH_JD;
    }
	
	@SuppressWarnings("serial")
	class JDToDateDialog extends JDialog {
		private static final String sTITLE = "JD <-> Calendar Date";
		private DoubleFieldTime fieldJulianDay;
		private IntegerField yearField;
		private IntegerField monthField;
		private IntegerField dayField;
		private IntegerField hourField;
		private IntegerField minField;
		private IntegerField secField;
		
		/**
		 * Constructor
		 */
		public JDToDateDialog()	{
			super(DocumentManager.findActiveWindow());
			setTitle(sTITLE);
			setModalityType(Dialog.ModalityType.MODELESS);
			
			ActionListener cancelListener = createCancelButtonListener();
		    getRootPane().registerKeyboardAction(cancelListener, 
		    		KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), 
		    		JComponent.WHEN_IN_FOCUSED_WINDOW);

			Container contentPane = this.getContentPane();

			JPanel topPane = new JPanel();
			topPane.setLayout(new BoxLayout(topPane, BoxLayout.PAGE_AXIS));
			topPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

			topPane.add(createJulianDayPane());
			topPane.add(createButtonPane1());
			topPane.add(createDatePane());
			topPane.add(createButtonPane2(cancelListener));
			
			contentPane.add(topPane);
			
			setCurrentTime();			
		    
			this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			
			this.pack();
			setLocationRelativeTo(Mediator.getUI().getContentPane());
			this.setVisible(true);
		}
		
		private JPanel createJulianDayPane() {
			JPanel panel = new JPanel(new BorderLayout());
			fieldJulianDay = new DoubleFieldTime("Julian Day", 
					AbstractDateUtil.getInstance().calendarToJD(MIN_YEAR, 1, 1), 
					AbstractDateUtil.getInstance().calendarToJD(MAX_YEAR, 12, 31),
					0.0);
			panel.add(fieldJulianDay.getUIComponent(), BorderLayout.CENTER);
			return panel;
		}
		
		private JPanel createDatePane() {
			JPanel panel = new JPanel(new FlowLayout());

			yearField = new IntegerField("Year", MIN_YEAR, MAX_YEAR, 0);
			panel.add(setTextFieldPreferredSize((JTextField)(yearField.getUIComponent()), "WWWWW"));
			
			monthField = new IntegerField("Mon", 1, 12, 0);
			panel.add(setTextFieldPreferredSize((JTextField)(monthField.getUIComponent()), "WWWW"));
			
			dayField = new IntegerField("Day", 1, 31, 0);
			panel.add(setTextFieldPreferredSize((JTextField)(dayField.getUIComponent()), "WWWW"));
			
			panel.add(new JLabel("  "));
			
			hourField = new IntegerField("Hr", 0, 23, 0);
			panel.add(setTextFieldPreferredSize((JTextField)(hourField.getUIComponent()), "WWWW"));
			
			minField = new IntegerField("Min", 0, 59, 0);
			panel.add(setTextFieldPreferredSize((JTextField)(minField.getUIComponent()), "WWWW"));
			
			secField = new IntegerField("Sec", 0, 59, 0);
			panel.add(setTextFieldPreferredSize((JTextField)(secField.getUIComponent()), "WWWW"));
			return panel;
		}

		private JPanel createButtonPane1() {
			Icon panUpIcon = ResourceAccessor.getIconResource("/nico/toolbarIcons/_24_/UpArrow.png");
			Icon panDownIcon = ResourceAccessor.getIconResource("/nico/toolbarIcons/_24_/DownArrow.png");
			
			JPanel panel = new JPanel(new FlowLayout());

			JButton jDtoDateButton = createIconButton(panDownIcon);
			jDtoDateButton.addActionListener(createJdToDateListener());
			panel.add(jDtoDateButton);
			
			panel.add(new JLabel("    "));
			
			JButton dateToJdButton = createIconButton(panUpIcon);
			dateToJdButton.addActionListener(createDateToJdListener());
			panel.add(dateToJdButton);
			
			return panel;
		}
		
		private JPanel createButtonPane2(ActionListener cancelListener) {
			JPanel panel = new JPanel(new BorderLayout());
			
			JButton cancelButton = new JButton("Close");
			cancelButton.addActionListener(cancelListener);
			panel.add(cancelButton, BorderLayout.LINE_START);
			
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
		
		// Return a listener for the "Close" button.
		private ActionListener createCancelButtonListener() {
			return new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setVisible(false);
					dispose();
				}
			};
		}

		// Return a listener for the "JD to Date" button.
		private ActionListener createJdToDateListener() {
			return new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setDateFromJd();
				}
			};
		}

		// Return a listener for the "Date to JD" button.
		private ActionListener createDateToJdListener() {
			return new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setJdFromDate();
				}
			};
		}
		
		private JTextField setTextFieldPreferredSize(JTextField textField, String sampleText) {
			int w = textField.getFontMetrics(textField.getFont()).stringWidth(sampleText);
			int h = textField.getPreferredSize().height;
			textField.setPreferredSize(new Dimension(w, h));
			return textField;
		}
		
		private JButton createIconButton(Icon icon) {
			JButton button = new JButton(icon);
			button.setPreferredSize(new Dimension(icon.getIconWidth() + 4, icon.getIconHeight() + 4));
			return button;
		}
		
		// Set form fields to values corresponding to the current time
		private void setCurrentTime() {
			fieldJulianDay.setValue(julianDayNow());
			setDateFromJd();
			setJdFromDate(); // to synchronize fields after rounding seconds.
		}

		// Date/Time fields (YYYY-MM-DD HH:MM:SS) from Julian Day field 
		private void setDateFromJd() {
			yearField.setValue(null);
			monthField.setValue(null);
			dayField.setValue(null);
			hourField.setValue(null);
			minField.setValue(null);
			secField.setValue(null);
			Double jd;
			try {
				jd = fieldJulianDay.getValue();
			} catch (Exception e) {
				// We should never be here as soon as getValue() catches all possible exceptions.
				jd = null;
			}
			if (jd != null) {
				YMD ymd = AbstractDateUtil.getInstance().jdToYMD(jd);
				double day = ymd.getDay();
				int hour = (int)((day - (int)day) * 24.0);
				int min = (int)((day - (int)day - hour / 24.0) * 24.0 * 60.0);
				int sec = (int)((day - (int)day - hour / 24.0 - min / 24.0 / 60.0) * 24.0 * 60.0 * 60.0);
				yearField.setValue(ymd.getYear());
				monthField.setValue(ymd.getMonth());
				dayField.setValue((int)day);
				hourField.setValue(hour);
				minField.setValue(min);
				secField.setValue(sec);
			} else {
				MessageBox.showErrorDialog("Error", fieldJulianDay.getName() + ": Invalid value!\n" + numberFieldInfo(fieldJulianDay));
				(fieldJulianDay.getUIComponent()).requestFocus();
				((JTextField)(fieldJulianDay.getUIComponent())).selectAll();
			}
		}
		
		// Set Julian Day field to a value calculated from date fields (YYYY-MM-DD HH:MM:SS) 
		private void setJdFromDate() {
			fieldJulianDay.setValue(null);
			
			Integer year =	getAndCheck(yearField);
			if (year == null) return;
			
			Integer month = getAndCheck(monthField);
			if (month == null) return;
			
			Integer day = getAndCheck(dayField);
			YearMonth yearMonthObject = YearMonth.of(year, month);
			int daysInMonth = yearMonthObject.lengthOfMonth();
			if (day > daysInMonth) {
				MessageBox.showErrorDialog("Error", dayField.getName() + ": Invalid number of days in the month!");
				(dayField.getUIComponent()).requestFocus();
				((JTextField)(dayField.getUIComponent())).selectAll();
				day = null;
			}
			if (day == null) return;
			
			Integer hour = getAndCheck(hourField);
			if (hour == null) return;

			Integer min = getAndCheck(minField);
			if (min == null) return;

			Integer sec = getAndCheck(secField);
			if (sec == null) return;

			double dday = day + hour / 24.0 + min / 24.0 / 60.0 + sec / 24.0 / 60.0 / 60.0;   
			double jd = AbstractDateUtil.getInstance().calendarToJD(year, month, dday);
			
			fieldJulianDay.setValue(jd);
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
				MessageBox.showErrorDialog("Error", input.getName() + ": Invalid value!\n" +
						"Value must be integer.\n" +
						numberFieldInfo(input));				
				(input.getUIComponent()).requestFocus();
				((JTextField)(input.getUIComponent())).selectAll();
			}
			return i;
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
		
	}

	/*
	 * Unlike DoubleField, DoubleFieldTime uses 'timeOutputFormat' instead of 'otherOutputFormat'
	 */
	private class DoubleFieldTime extends DoubleField {

		public DoubleFieldTime(String name, Double min, Double max, Double initial) {
			super(name, min, max, initial);
			setValue(initial); // because super() uses NumericPrecisionPrefs.getOtherOutputFormat()
		}

		@Override
		public void setValue(Double value) {
			textField.setText(value == null ? "" : NumericPrecisionPrefs.getTimeOutputFormat().format(value));
		}
		
	}

}
