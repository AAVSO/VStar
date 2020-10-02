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

// The plug-in shows variable star information taken from the VSX database.
// It could be useful if the star does exist in the VSX yet data for it
// are loaded from a source other then AAVSO Database.

// Maksym Pyatnytskyy (PMAK (AAVSO)) mpyat2@gmail.com
 
package org.aavso.tools.vstar.external.plugin;

import java.net.URLEncoder;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.w3c.dom.Element;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JPanel;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import org.aavso.tools.vstar.plugin.GeneralToolPluginBase;

import org.aavso.tools.vstar.ui.dialog.TextField;
import org.aavso.tools.vstar.ui.dialog.DoubleField;
import org.aavso.tools.vstar.ui.dialog.MessageBox;
import org.aavso.tools.vstar.ui.dialog.PhaseParameterDialog;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.DocumentManager;
import org.aavso.tools.vstar.util.date.YMD;

import org.aavso.tools.vstar.util.date.AbstractDateUtil;

/**
 * VSX query by name
 */
public class VSXquery extends GeneralToolPluginBase {

	private static String sVSXname = "";
	
	protected static final String sVSX_URL = "https://www.aavso.org/vsx/index.php?view=api.object&ident=";
	
	private EphemerisDialog ephemerisDialog = null; 

	@Override
	public void invoke() {
		new QueryVSXdialog();
	}

	@Override
	public String getDescription() {
		return "VSX Query";
	}

	@Override
	public String getDisplayName() {
		return "VSX Query";
	}
	
	@SuppressWarnings("serial")
	class QueryVSXdialog extends JDialog {
		protected static final String sTITLE = "Query VSX";
		
		protected TextField fieldVSXname;
		protected DoubleField fieldVSXperiod;
		protected DoubleField fieldVSXepoch;
		protected TextField fieldVSXvarType;
		protected TextField fieldVSXspectralType;
		protected JTextArea textArea;
		protected DoubleField fieldEphemerisFrom;
		protected DoubleField fieldEphemerisTo;
		protected DoubleField fieldEphemerisPhase;
		protected DoubleField fieldTimeZoneOffset;
		
		protected boolean closed = false;
		
		protected JButton queryButton;
		protected JButton phaseDialogButton;
		protected JButton resetButton;
		protected JButton calcEphemeris;

		/**
		 * Constructor
		 */
		public QueryVSXdialog()	{
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

			topPane.add(createNamePane());
			topPane.add(createInfoPane());
			topPane.add(createButtonPane());
			topPane.add(createParamPane2());
			topPane.add(createButtonPane2());
			//topPane.add(new JPanel());
			topPane.add(new JLabel(" "));
			topPane.add(new JSeparator(SwingConstants.HORIZONTAL));
			topPane.add(new JLabel(" "));
			topPane.add(createButtonPane3(cancelListener));
			
			//this.getRootPane().setDefaultButton(queryButton);

			contentPane.add(topPane);

			this.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent we) {
					closed = true;
					closeEphemerisDialog(true);
				}
			});
			this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			
			this.pack();
			setLocationRelativeTo(Mediator.getUI().getContentPane());
			this.setVisible(true);
			
		}
		
		private JPanel createNamePane()	{
			JPanel panel = new JPanel(new BorderLayout());
			fieldVSXname = new TextField("VSX Name", sVSXname);
			panel.add(fieldVSXname.getUIComponent(), BorderLayout.CENTER);
			queryButton = new JButton("Query");			
			queryButton.addActionListener(createQueryButtonListener());
			panel.add(queryButton, BorderLayout.LINE_END);
			
			return panel;
		}
		
		private JPanel createInfoPane()	{
			JPanel panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

			fieldVSXvarType = new TextField("VSX Variability Type", "");
			fieldVSXvarType.setEditable(false);			
			panel.add(fieldVSXvarType.getUIComponent());

			fieldVSXspectralType = new TextField("VSX Spectral Type", "");
			fieldVSXspectralType.setEditable(false);
			panel.add(fieldVSXspectralType.getUIComponent());
			
			textArea = new JTextArea(16, 64);
			//textArea.setFont(textArea.getFont().deriveFont(12f));
			textArea.setEditable(false);
			JScrollPane scrollPane = new JScrollPane(textArea);
			panel.add(scrollPane);
			
			fieldVSXperiod = new DoubleField("VSX Period", null, null, null);
			fieldVSXperiod.setValue(0.0);
			//((JTextComponent) (fieldVSXperiod.getUIComponent())).setEditable(false);
			panel.add(fieldVSXperiod.getUIComponent());
			
			fieldVSXepoch = new DoubleField("VSX Epoch", null, null, null);
			fieldVSXepoch.setValue(0.0);
			//((JTextComponent) (fieldVSXepoch.getUIComponent())).setEditable(false);
			panel.add(fieldVSXepoch.getUIComponent());
		
			return panel;
		}

		private JPanel createButtonPane() {
			JPanel panel = new JPanel(new BorderLayout());

			phaseDialogButton = new JButton("Populate Phase Dialog");
			phaseDialogButton.addActionListener(createPhaseDialogButtonListener());
			panel.add(phaseDialogButton, BorderLayout.PAGE_END);
			//phaseDialogButton.setEnabled(false);

			return panel;
		}
		
		private JPanel createParamPane2() {
			JPanel panel = new JPanel();
			panel.setLayout(new GridLayout(0, 2));
			panel.add(new JLabel("Ephemeris"));
			panel.add(new JLabel(""));			
			fieldEphemerisFrom = new DoubleField("Minimum JD", null, null, new Double(0));
			fieldEphemerisTo = new DoubleField("Maximum JD", null, null, new Double(0));
			fieldEphemerisPhase = new DoubleField("For Phase", null, null, new Double(0));
			fieldTimeZoneOffset = new DoubleField("Zone Offset (hours)", null, null, new Double(0));
			setDefaultEphemerisParams();
			panel.add(fieldEphemerisFrom.getUIComponent());
			panel.add(fieldEphemerisTo.getUIComponent());
			panel.add(fieldEphemerisPhase.getUIComponent());
			panel.add(fieldTimeZoneOffset.getUIComponent());
			return panel;
		}
		
		private void setDefaultEphemerisParams() {
			Calendar cal = Calendar.getInstance();
			TimeZone tz = cal.getTimeZone();
			double offset = tz.getOffset(new Date().getTime()) / 1000. / 60. / 60.;
			int year = cal.get(Calendar.YEAR);
			int month = cal.get(Calendar.MONTH) + 1; // 0..11 -> 1..12
			double day = cal.get(Calendar.DAY_OF_MONTH);
			double jd = AbstractDateUtil.getInstance().calendarToJD(year, month, day);
			fieldEphemerisFrom.setValue(jd);
			fieldEphemerisTo.setValue(jd + 100);
			fieldEphemerisPhase.setValue(0.0);
			fieldTimeZoneOffset.setValue(offset);
		}
		
		private JPanel createButtonPane2() {
			JPanel panel = new JPanel(new BorderLayout());
			calcEphemeris = new JButton("Calculate Ephemeris");
			calcEphemeris.addActionListener(createEphemerisButtonListener());
			panel.add(calcEphemeris);
			//calcEphemeris.setEnabled(false);
			return panel;
		}
		
		private JPanel createButtonPane3(ActionListener cancelListener) {
			JPanel panel = new JPanel(new BorderLayout());
			JButton cancelButton = new JButton("Close");
			cancelButton.addActionListener(cancelListener);
			panel.add(cancelButton, BorderLayout.LINE_START);
			resetButton = new JButton("Reset");
			resetButton.addActionListener(createResetButtonListener());
			panel.add(resetButton, BorderLayout.LINE_END);
			
			return panel;
		}
		
		// Return a listener for the "Query" button.
		private ActionListener createQueryButtonListener() {
			return new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					queryVSX();
				}
			};
		}
		
		// Return a listener for the "Reset" button.
		private ActionListener createResetButtonListener() {
			return new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					resetVSXFields();
					setDefaultEphemerisParams();
				}
			};
		}

		// Return a listener for the "Populate Phase Dialog" button.
		private ActionListener createPhaseDialogButtonListener() {
			return new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Double period = fieldVSXperiod.getValue();
					if (period == null || period <= 0) {
						MessageBox.showErrorDialog(QueryVSXdialog.this, "Error", "Period must be > 0");
						return;
					}
					Mediator mediator = Mediator.getInstance();
					PhaseParameterDialog phaseParam = mediator.getPhaseParameterDialog();
					phaseParam.setEpochField(fieldVSXepoch.getValue());
					phaseParam.setPeriodField(period);
					MessageBox.showMessageDialog(QueryVSXdialog.this, sTITLE, "Phase dialog parameters have been set");
				}
			};
		}
		
		// Return a listener for the "Cancel" button.
		private ActionListener createCancelButtonListener() {
			return new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					closed = true;
					closeEphemerisDialog(true);
					setVisible(false);
					dispose();
				}
			};
		}
		
		// Return a listener for the "Ephemeris" button.
		private ActionListener createEphemerisButtonListener() {
			return new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					String starName;
					Double period;
					Double epoch;
					Double minJD;
					Double maxJD;
					Double phase;
					Double zoneOffset;
					starName = sVSXname;
					if (starName == null) starName = "";
					period = fieldVSXperiod.getValue();
					if (period == null || period <= 0) {
						ShowError("Period must be > 0");
						return;
					}
					epoch = fieldVSXepoch.getValue();
					if (epoch == null) {
						ShowError("Invalid epoch");
						return;
					}
					minJD = fieldEphemerisFrom.getValue();
					if (minJD == null) {
						ShowError("Invalid Min JD");
						return;
					}
					maxJD = fieldEphemerisTo.getValue();
					if (maxJD == null) {
						ShowError("Invalid Max JD");
						return;
					}
					phase = fieldEphemerisPhase.getValue();
					if (phase == null) {
						ShowError("Invalid phase");
						return;
					}
					zoneOffset = fieldTimeZoneOffset.getValue();
					if (zoneOffset == null) {
						ShowError("Invalid Zone Offset");
						return;
					}
					if (ephemerisDialog == null) {
						ephemerisDialog = new EphemerisDialog(QueryVSXdialog.this, starName, period, epoch, minJD, maxJD, phase, zoneOffset);
					} else {
						ephemerisDialog.setVisible(false);
						ephemerisDialog.updateEphemeris(starName, period, epoch, minJD, maxJD, phase, zoneOffset);
						ephemerisDialog.setVisible(true);
					}
				}
				private void ShowError(String msg) {
					MessageBox.showErrorDialog(QueryVSXdialog.this, "Error", msg);
				}
			};
		}
		
		private void closeEphemerisDialog(boolean destroy) {
			if (ephemerisDialog != null) {
				ephemerisDialog.setVisible(false);
				if (destroy) {
					ephemerisDialog.dispose();
					ephemerisDialog = null;
				}
			}
		}
		
		private void resetVSXFields() {
			textArea.setText("");
			fieldVSXperiod.setValue(0.0);
			fieldVSXepoch.setValue(0.0);
			fieldVSXvarType.setValue("");
			fieldVSXspectralType.setValue("");
		}
		
		private void queryVSX() {
			closeEphemerisDialog(false);
			sVSXname = fieldVSXname.getValue().trim();
			if ("".equals(sVSXname))
				return;
			resetVSXFields();
			textArea.setText("Please wait...");			
			setTitle("Wait...");
			//pack();
			SwingWorker<VSQqueryResult, VSQqueryResult> worker = new VSXquerySwingWorker(this, sVSXname);
			queryButton.setEnabled(false);
			phaseDialogButton.setEnabled(false);
			resetButton.setEnabled(false);
			calcEphemeris.setEnabled(false);
			worker.execute();
		}
	
	}
	
	
	@SuppressWarnings("serial")
	class EphemerisDialog extends JDialog {
		
		private JTextArea textArea;
		
		protected EphemerisDialog(JDialog parent, String starName, Double period, Double epoch, Double fromJD, Double toJD, Double phase, Double zoneOffset) {
			super(parent, "Ephemeris");
			setModalityType(Dialog.ModalityType.MODELESS);
			
			ActionListener cancelListener = createCancelButtonListener();
		    getRootPane().registerKeyboardAction(cancelListener, 
		    		KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), 
		    		JComponent.WHEN_IN_FOCUSED_WINDOW);
			
			Container contentPane = this.getContentPane();

			JPanel panel = new JPanel();
			panel.setLayout(new BorderLayout());
			
			textArea = new JTextArea(16, 80);
			textArea.setFont(textArea.getFont().deriveFont(12f)); // https://stackoverflow.com/questions/6461506/jtextarea-default-font-very-small-in-windows
			textArea.setTabSize(22);
			textArea.setEditable(false);
			JScrollPane scrollPane = new JScrollPane(textArea);
			panel.add(scrollPane, BorderLayout.CENTER);
			
			updateEphemeris(starName, period, epoch, fromJD, toJD, phase, zoneOffset);
			
			JButton cancelButton = new JButton("Close");
			cancelButton.addActionListener(cancelListener);
			panel.add(cancelButton, BorderLayout.PAGE_END);
			
			contentPane.add(panel);
			
			this.pack();
			setLocationRelativeTo(parent);
			this.setVisible(true);
		}
		
		private ActionListener createCancelButtonListener() {
			return new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setVisible(false);
				}
			};
		}

		protected void updateEphemeris(String starName, Double period, Double epoch, Double fromJD, Double toJD, Double phase, Double zoneOffset) {
			if (starName == null || starName.trim() == "")
				starName = "<unknown>";
			this.setTitle("Ephemeris for " + starName  + ", period: " + period.toString() + ", epoch: " + epoch.toString());
			textArea.setText(getEphemerisAsText(period, epoch, fromJD, toJD, phase, zoneOffset));
			textArea.setCaretPosition(0);
		}

		
		private String getEphemerisAsText(Double period, Double epoch, Double fromJD, Double toJD, Double phase, Double zoneOffset) {
			if (period <= 0)
				return "Period must be greater than zero!";
			int maxEphemeris = 100; // max rows
			// Normalizing phase
			phase = Math.IEEEremainder(phase, 1.0);
			if (phase < 0) phase += 1.0;
			// Calculate nearest epoch <= fromJD.
			double nearestEpoch = epoch + Math.floor((fromJD - epoch) / period) * period + phase * period;
			if (nearestEpoch > fromJD)
				nearestEpoch -= period;
			String result = "Epoch\tUT\tUT+Offset\tPhase [0 - 1]\n";
			int i = 0;
			double jd = nearestEpoch;
			while (jd <= toJD) {
				if (jd >= fromJD && jd <= toJD) {
					if (i == maxEphemeris) {
						result += "(maximum number of rows reached)\n";
						return result;
					}
					result += String.format("%.4f", jd);
					result += "\t";
					YMD ymd = AbstractDateUtil.getInstance().jdToYMD(jd);
					result += formatDate(ymd);
					result += "\t";
					ymd = AbstractDateUtil.getInstance().jdToYMD(jd + zoneOffset / 24.);
					result += formatDate(ymd);
					result += "\t";
					result += String.format("%.3f", phase);
					result += "\n"; 
					i++;
				}
				jd = jd + period;
			}
			if (i == 0)
				result += "No events in this timespan!\n";
			return result;
		}
		
		private String formatDate(YMD ymd) {
			double day = ymd.getDay();
			double hours = (day - Math.floor(day)) * 24;
			double mins = (hours - Math.floor(hours)) * 60;
			double secs = (mins - Math.floor(mins)) * 60;
			int iday = (int)Math.floor(day);
			int ihours = (int)Math.floor(hours);
			int imins = (int)Math.floor(mins);
			int isecs = (int)Math.floor(secs);
			return String.format("%d-%02d-%02d %02d:%02d:%02d", ymd.getYear(), ymd.getMonth(), iday, ihours, imins, isecs);
		}
		
	}
	
	class VSQqueryResult {
		protected String name;
		protected Double period;
		protected Double epoch;
		protected String varType;
		protected String spType;		
		protected String stringResult;
	}

	class VSXquerySwingWorker extends SwingWorker<VSQqueryResult, VSQqueryResult>	{

		private VSXquery.QueryVSXdialog dialog;
		private String vsxName; 

		public VSXquerySwingWorker(VSXquery.QueryVSXdialog dialog, String name)
		{
			this.dialog = dialog;
			this.vsxName = name;
		}

		@Override
		protected VSQqueryResult doInBackground() throws Exception {
			return queryVSX(vsxName, 1);
		}

		@Override
		protected void done()  
		{ 
			VSQqueryResult vsxResult = null;
			String error = "Unknown error";
			try { 
				vsxResult = get(); 
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
					dialog.setTitle(VSXquery.QueryVSXdialog.sTITLE);
					dialog.queryButton.setEnabled(true);
					dialog.phaseDialogButton.setEnabled(true);
					dialog.resetButton.setEnabled(true);
					dialog.calcEphemeris.setEnabled(true);
					dialog.fieldVSXname.setValue(vsxName);
					if (vsxResult != null) {
						dialog.textArea.setText(vsxResult.stringResult);
						dialog.fieldVSXperiod.setValue(vsxResult.period != null ? vsxResult.period : 0.0);
						dialog.fieldVSXepoch.setValue(vsxResult.epoch != null ? vsxResult.epoch : 0.0);
						dialog.fieldVSXvarType.setValue(vsxResult.varType);
						dialog.fieldVSXspectralType.setValue(vsxResult.spType);
						//dialog.phaseDialogButton.setEnabled(true);
						//dialog.calcEphemeris.setEnabled(true);
					} else {
						dialog.textArea.setText(error);
						dialog.fieldVSXperiod.setValue(0.0);
						dialog.fieldVSXepoch.setValue(0.0);
						dialog.fieldVSXvarType.setValue("");
						dialog.fieldVSXspectralType.setValue("");
					}
					dialog.pack();
				}
			}
		}
		
		private VSQqueryResult queryVSX(String sVSXname, int maxStars)
				throws UnsupportedEncodingException, ParserConfigurationException, IOException, SAXException {
			VSQqueryResult vsxResult = new VSQqueryResult();
			String result = "";

			// Example: https://www.aavso.org/vsx/index.php?view=api.object&ident=pmak+v41
			String sURL = VSXquery.sVSX_URL + URLEncoder.encode(sVSXname, StandardCharsets.UTF_8.name());
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			URL url = new URL(sURL);
			InputStream stream = url.openStream();
			Document doc = db.parse(stream);
			doc.getDocumentElement().normalize();

			// Parse <VSXObject>
			Element root = doc.getDocumentElement();
			NodeList nodes = root.getChildNodes();
			for (int i = 0; i < nodes.getLength(); i++) {
				Element row = (Element) nodes.item(i);
				String tag = row.getTagName();
				String content = row.getTextContent();
				if ("Period".equalsIgnoreCase(tag))
					try {
						vsxResult.period = Double.parseDouble(content);
						if (vsxResult.period == 0)
							vsxResult.period = null;
					} catch (NumberFormatException ex) {
						vsxResult.period = null;
					}
				else if ("Epoch".equalsIgnoreCase(tag))
					try {
						vsxResult.epoch = Double.parseDouble(content);
						if (vsxResult.epoch == 0)  
							vsxResult.epoch = null;
					} catch (NumberFormatException ex) {
						vsxResult.epoch = null;
					}
				else if ("VariabilityType".equalsIgnoreCase(tag))
					vsxResult.varType = content;
				else if ("SpectralType".equalsIgnoreCase(tag))
					vsxResult.spType = content;
				result += tag + ": " + content + "\n";
			}
			if ("".equals(result))
				result = "No valid data returned by the query.";
			vsxResult.stringResult = result;
			return vsxResult;
		}
	}

}
