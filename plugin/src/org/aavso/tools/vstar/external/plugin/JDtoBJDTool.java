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

package org.aavso.tools.vstar.external.plugin;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.InputStream;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;

import org.aavso.tools.vstar.input.database.VSXWebServiceStarInfoSource;
import org.aavso.tools.vstar.plugin.GeneralToolPluginBase;
import org.aavso.tools.vstar.ui.dialog.DoubleField;
import org.aavso.tools.vstar.ui.dialog.IntegerField;
import org.aavso.tools.vstar.ui.dialog.MessageBox;
import org.aavso.tools.vstar.ui.dialog.NumberFieldBase;
import org.aavso.tools.vstar.ui.mediator.DocumentManager;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.StarInfo;
import org.aavso.tools.vstar.util.Triple;
import org.aavso.tools.vstar.util.coords.DecInfo;
import org.aavso.tools.vstar.util.coords.EpochType;
import org.aavso.tools.vstar.util.coords.RAInfo;
import org.aavso.tools.vstar.util.date.AbstractHJDConverter;
import org.aavso.tools.vstar.util.help.Help;
import org.aavso.tools.vstar.util.locale.NumberParser;
import org.aavso.tools.vstar.util.prefs.NumericPrecisionPrefs;

public class JDtoBJDTool extends GeneralToolPluginBase {
	
	private Integer lastRaHour = null;
	private Integer lastRaMin = null;
	private Double lastRaSec = null;
	
	private Integer lastDecDeg = null;
	private Integer lastDecMin = null;
	private Double lastDecSec = null;
	
	@Override
	public void invoke() {
		new JDtoBJDToolDialog();
	}

	@Override
	public String getDescription() {
		return "Convert JD to BJD_TDB";
	}

	@Override
	public String getDisplayName() {
		return "JD to BJD_TDB";
	}
	
	/**
	 * @see org.aavso.tools.vstar.plugin.IPlugin#getDocName()
	 */
	@Override
	public String getDocName() {
		//return("https://github.com/AAVSO/VStar/wiki/General-Tool-Plug%E2%80%90ins#name-convert-bjd");
		return("JDtoBJDTool.pdf");
	}
	
	@SuppressWarnings("serial")
	class JDtoBJDToolDialog extends JDialog {
		
		private static final String ASTROUTILS_URL = "https://astroutils.astronomy.osu.edu";
		
		private static final String URL_TEMPLATE = ASTROUTILS_URL + "/time/convert.php?JDS=%s&RA=%s&DEC=%s&FUNCTION=%s"; 
		
		private static final String sTITLE = "BJD Converter";
		
		private IntegerField raHour;
		private IntegerField raMin;
		private DoubleField raSec;
		
		private IntegerField decDeg;
		private IntegerField decMin;
		private DoubleField decSec;
		
		private JButton bByName;
		private JTextArea textArea1;
		private JTextArea textArea2;
		private JButton bUTCtoBJD;
		private JButton bHJDtoBJD;
		private JButton bUTCtoHJD;
		
		protected boolean closed = false;
		
		private Cursor waitCursor = new Cursor(Cursor.WAIT_CURSOR);
		
		/**
		 * Constructor
		 */
		public JDtoBJDToolDialog()	{
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

			topPane.add(createCoordPane());
			topPane.add(createMainPane());
			topPane.add(createButtonPane2(cancelListener));
			
			contentPane.add(topPane);
			
			this.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent we) {
					closed = true;
				}
			});
			this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			
			this.pack();
			setLocationRelativeTo(Mediator.getUI().getContentPane());
			this.setVisible(true);
		}
		
		private JPanel createCoordPane() {
			JPanel panel = new JPanel();
			
			panel.add(new JLabel("RA: "));
			
			raHour = new IntegerField("Hour", 0, 23, lastRaHour);
			setNumberFieldColumns(raHour, 6);
			panel.add(raHour.getUIComponent());			
			
			raMin = new IntegerField("Min", 0, 59, lastRaMin);
			setNumberFieldColumns(raMin, 6);
			panel.add(raMin.getUIComponent());
			
			raSec = new DoubleField("Sec", 0.0, 60.0, lastRaSec);
			setNumberFieldColumns(raSec, 6);
			panel.add(raSec.getUIComponent());
			
			panel.add(new JLabel("    Dec: "));
			
			decDeg = new IntegerField("Deg", -90, 90, lastDecDeg);
			setNumberFieldColumns(decDeg, 6);
			panel.add(decDeg.getUIComponent());
			
			decMin = new IntegerField("Min", 0, 59, lastDecMin);
			setNumberFieldColumns(decMin, 6);
			panel.add(decMin.getUIComponent());
			
			decSec = new DoubleField("Sec", 0.0, 60.0, lastDecSec);
			setNumberFieldColumns(decSec, 6);
			panel.add(decSec.getUIComponent());
			
			panel.add(new JLabel("    "));
			
			bByName = new JButton("VSX Name");
			bByName.addActionListener(createByNameButtonListener());
			
			panel.add(bByName);
			
			return panel;
		}
		
		private JPanel createMainPane() {
			JPanel panel = new JPanel();
			textArea1 = new JTextArea(20, 30);
			panel.add(new JScrollPane(textArea1), BorderLayout.EAST);
			panel.add(createButtonPane(), BorderLayout.CENTER);
			textArea2 = new JTextArea(20, 30);
			textArea2.setEditable(false);
			panel.add(new JScrollPane(textArea2), BorderLayout.WEST);
			return panel;
		}

		private JPanel createButtonPane() {
			JPanel panel = new JPanel();
			
			bUTCtoBJD = new JButton("UTC->BJD");
			bUTCtoBJD.setToolTipText("Via " + ASTROUTILS_URL + " service");
			bUTCtoBJD.addActionListener(createUTCtoBJDButtonListener());
			panel.add(bUTCtoBJD, BorderLayout.NORTH);
			
			bHJDtoBJD = new JButton("HJD->BJD");
			bHJDtoBJD.setToolTipText("Via " + ASTROUTILS_URL + " service");
			bHJDtoBJD.addActionListener(createHJDtoBJDButtonListener());
			panel.add(bHJDtoBJD, BorderLayout.CENTER);

			bUTCtoHJD = new JButton("UTC->HJD");
			bUTCtoHJD.setToolTipText("Via internal routine");
			bUTCtoHJD.addActionListener(createUTCtoHJDButtonListener());
			panel.add(bUTCtoHJD, BorderLayout.SOUTH);
			
			panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
			return panel;
		}
		
		private JPanel createButtonPane2(ActionListener cancelListener) {
			JPanel panel = new JPanel();

			JButton helpButton = new JButton("Help");
			helpButton.addActionListener(createHelpButtonListener());
			panel.add(helpButton);
			
			JButton cancelButton = new JButton("Close");
			cancelButton.addActionListener(cancelListener);
			panel.add(cancelButton);
			
			return panel;
		}

		private ActionListener createByNameButtonListener() {
			return new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					VSXName();
				}
			};
		}
		
		// Return a listener for the "UTC to BJD" button.
		private ActionListener createUTCtoBJDButtonListener() {
			return new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					ConvertUTCtoBJD();
				}
			};
		}

		// Return a listener for the "HJD to BJD" button.
		private ActionListener createHJDtoBJDButtonListener() {
			return new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					ConvertHJDtoBJD();
				}
			};
		}

		// Return a listener for the "HJD to BJD" button.
		private ActionListener createUTCtoHJDButtonListener() {
			return new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					ConvertUTCtoHJD();
				}
			};
		}
		
		// Return a listener for the "Close" button.
		private ActionListener createCancelButtonListener() {
			return new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					closed = true;
					setVisible(false);
					dispose();
				}
			};
		}
		
		// Return a listener for the "Help" button.
		protected ActionListener createHelpButtonListener() {
			return new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Help.openPluginHelp(getDocName());
				}
			};
		}
		
		private void VSXName() {
			String name = JOptionPane.showInputDialog(this, "VSX Object Name", "VSX", JOptionPane.PLAIN_MESSAGE);
			if (name == null || "".equals(name.trim()))
				return;
			Cursor defaultCursor = getCursor();
			setCursor(waitCursor);
			try {
				StarInfo starInfo;
				VSXWebServiceStarInfoSource infoSrc = new VSXWebServiceStarInfoSource();
				try {
					starInfo = infoSrc.getStarByName(name);
				} catch (Exception ex) {
					MessageBox.showErrorDialog("Error", ex.getMessage());
					return;
				}

				Triple<Integer, Integer, Double> ra = starInfo.getRA().toHMS();
				Triple<Integer, Integer, Double> dec = starInfo.getDec().toDMS();
				
				raHour.setValue(ra.first);
				raMin.setValue(ra.second);
				raSec.setValue(ra.third);

				decDeg.setValue(dec.first);
				decMin.setValue(dec.second);
				decSec.setValue(dec.third);
				
			} finally {
				setCursor(defaultCursor);
			}
			
		}
		
		private void ConvertUTCtoBJD() {
			ConvertProc("utc2bjd");
		}
		
		private void ConvertHJDtoBJD() {
			ConvertProc("hjd2bjd");
		}
		
		private void ConvertUTCtoHJD() {
			ConvertProc("utc2hjd");
		}
		
		private void ConvertProc(String func) {
			lastRaHour = getAndCheck(raHour);
			if (lastRaHour == null) return;
			lastRaMin = getAndCheck(raMin);
			if (lastRaMin == null) return;
			lastRaSec = getAndCheck(raSec);
			if (lastRaSec == null) return;

			lastDecDeg = getAndCheck(decDeg);
			if (lastDecDeg == null) return;
			lastDecMin = getAndCheck(decMin);
			if (lastDecMin == null) return;
			lastDecSec = getAndCheck(decSec);
			if (lastDecSec == null) return;

			double ra;
			double dec;
			
			{
				double result = lastRaHour;
				result += lastRaMin / 60.0;
				result += lastRaSec / 60.0 / 60.0;
				result *= 15;
				if (result < 0.0 || result > 360.0) {
					MessageBox.showErrorDialog("Error", "RA: Invalid input");
				}
				ra = result;
			}
			
			{
				double result = lastDecDeg;
				double sign = result < 0 ? -1 : 1;
				result = Math.abs(result);
				result += lastDecMin / 60.0;
				result += lastDecSec / 60.0 / 60.0;
				result = sign * result;
				if (result < -90.0 || result > 90.0) {
					MessageBox.showErrorDialog("Error", "Dec: Invalid input");
				}
				dec = result;
			}
			
			
			String times = null;
			String s = textArea1.getText().trim();
			
			if ("".equals(s)) {
				MessageBox.showErrorDialog("Error", "Empty list");
				return;
			}
			
			List<String> tempList = new ArrayList<String>(Arrays.asList(s.split("\n")));
			s = null;
			for (String s1: tempList) {
				String s2 = s1.trim();
				if (!"".equals(s2)) {
					double d;
					try {
						d = NumberParser.parseDouble(s2);
					} catch (Exception ex) {
						MessageBox.showErrorDialog("Error", ex.getMessage());
						return;
					}
					if (times != null) 
						times += ",";
					else
						times = "";
					times += String.valueOf(d);
					if (s != null) 
						s += "\n";
					else
						s = "";
					s += s2;
				}
			}
			textArea1.setText(s);
			
			if ("utc2hjd".equals(func)) {
				performLocalConvertUTC2HJD(times, ra, dec);
			} else {
				String urlString = String.format(URL_TEMPLATE, times, String.valueOf(ra), String.valueOf(dec), func);
				performConvert(urlString);
			}
		}
	
		void performLocalConvertUTC2HJD(String times, double ra, double dec) {
			String result = "";
			List<String> tempList = new ArrayList<String>(Arrays.asList(times.split(",")));
			AbstractHJDConverter converter = AbstractHJDConverter.getInstance(EpochType.J2000);			
			for (String s1 : tempList) {
				double d = Double.parseDouble(s1);
				d = converter.convert(d, new RAInfo(EpochType.J2000, ra), new DecInfo(EpochType.J2000, dec));
				result += (String.valueOf(d) + "\n");
			}
			displayOutput(result);
		}
		
		private void performConvert(String urlString) {
			//System.out.println(urlString);
			textArea2.setText("Please wait...");
			bUTCtoBJD.setEnabled(false);
			bHJDtoBJD.setEnabled(false);
			bUTCtoHJD.setEnabled(false);
			SwingWorker<ConvertResult, Object> worker = new JDtoBJDToolSwingWorker(this, urlString);
			worker.execute();
		}
		
		public void displayOutput(String s) {
			textArea2.setText(null);
			String s_out = "";
			if (s != null) {
				List<String> tempList = new ArrayList<String>(Arrays.asList(s.split("\n")));
				for (String s1 : tempList) {
					double d;
					try {
						d = Double.parseDouble(s1);
					} catch (NumberFormatException e) {
						MessageBox.showErrorDialog("Error", "The server returned an invalid output:\n" + s);
						return;
					}
					s1 = NumericPrecisionPrefs.getTimeOutputFormat().format(d);
					s_out += (s1 + "\n");
				}
			}
			textArea2.setText(s_out);
		}
		
		private JTextField setNumberFieldColumns(NumberFieldBase<?> field, int columns) {
			JTextField textField = (JTextField)(field.getUIComponent());
			textField.setColumns(columns);
			return textField;
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

	}
	
	private class ConvertResult {
		public String text = null;
		public String error = null;
	}
	
	class JDtoBJDToolSwingWorker extends SwingWorker<ConvertResult, Object>	{

		private JDtoBJDTool.JDtoBJDToolDialog dialog;
		private String urlString;

		public JDtoBJDToolSwingWorker(JDtoBJDTool.JDtoBJDToolDialog dialog, String urlString)
		{
			this.dialog = dialog;
			this.urlString = urlString;
		}
		
		private ConvertResult getTextFromURLstring(String urlString) {
			ConvertResult result = new ConvertResult();
			try {
				URL url = new URL(urlString);
				InputStream stream = url.openConnection().getInputStream();
				StringBuilder textBuilder = new StringBuilder();
			    try (Reader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"))) {
			        int c = 0;
			        while ((c = reader.read()) != -1) {
			            textBuilder.append((char) c);
			        }
				    result.text = textBuilder.toString();
				    return result;
			    }
			} catch (Exception ex) {
				result.error = ex.toString();
				return result;
			}
		}
		
		@Override
		protected ConvertResult doInBackground() throws Exception {
			return getTextFromURLstring(urlString);
		}
		
		@Override
		protected void done()  
		{
			ConvertResult result = null;
			String error = "Unknown error";
			try { 
				result = get();
				if (result != null && result.error != null) {
					error = result.error;
					result = null;
				}
			} catch (InterruptedException ex) { 
				error = "Interrupted";
			} catch (ExecutionException ex)	{ 
				Throwable cause = ex.getCause();
				error = cause.getLocalizedMessage();
				if (error == null || "".equals(error))
					error = cause.toString();
				error = "Execution Exception: \n" + error;
			} finally {
				if (!dialog.closed)
				{
					dialog.bUTCtoBJD.setEnabled(true);
					dialog.bHJDtoBJD.setEnabled(true);
					dialog.bUTCtoHJD.setEnabled(true);
					if (result != null) {
						dialog.displayOutput(result.text);
					} else {
						dialog.displayOutput(null);
						MessageBox.showErrorDialog("Error", error);
					}
					dialog.pack();
				}
			}
		}
				
				
		
	}

}
