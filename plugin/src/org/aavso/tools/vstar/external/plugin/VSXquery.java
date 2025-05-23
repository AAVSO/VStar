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
import java.text.DecimalFormat;
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
import java.awt.Image;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
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
import org.aavso.tools.vstar.ui.resources.ResourceAccessor;
import org.aavso.tools.vstar.ui.mediator.DocumentManager;
import org.aavso.tools.vstar.util.date.YMD;
import org.aavso.tools.vstar.util.help.Help;
import org.aavso.tools.vstar.util.prefs.NumericPrecisionPrefs;
import org.aavso.tools.vstar.util.date.AbstractDateUtil;

/**
 * VSX query by name
 */
public class VSXquery extends GeneralToolPluginBase {

	protected static final String VSX_URL = 
	        ResourceAccessor.getVsxApiUrlBase() + "api.object&ident=";
	
	protected static final String IMAGE_URL_TEMPLATE = 
			"https://archive.stsci.edu/cgi-bin/dss_search?v=poss2ukstu_red&r=%s&d=%s&e=J2000&h=5&w=5&f=gif&c=none&fov=NONE&v3=";
	
	protected static final String IMAGE_TITLE_TEMPLATE = 
			"POSS2/UKSTU Red 5'x5': RA= %s; Dec= %s";

	protected static final String SIMBAD_URL_TEMPLATE =
			"https://simbad.u-strasbg.fr/simbad/sim-coo?Coord=%s+%s&CooFrame=ICRS&CooEqui=2000.0&CooEpoch=J2000&Radius.unit=arcmin&Radius=10";
	
	private static String lastVSXname = "";

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

	/**
	 * @see org.aavso.tools.vstar.plugin.IPlugin#getDocName()
	 */
	@Override
	public String getDocName() {
		return "VSX_Query_Plugin.pdf";
	}

	class DoubleField2 extends DoubleField {

		private int MIN_DECIMAL_PLACES = 20;

		public DoubleField2(String name, Double min, Double max, Double initial) {
			super(name, min, max, initial);
		}

		@Override
		public void setValue(Double value) {
			//textField.setText(value.toString());
			//Locale-specific version, more decimals than by default (PMAK).
			//Default settings for decimal places = 6 (can be changed in "preferences")
			if (value != null) {
				DecimalFormat df = new DecimalFormat("0");
				int fractionDigits = NumericPrecisionPrefs.getOtherDecimalPlaces();
				if (fractionDigits < MIN_DECIMAL_PLACES) fractionDigits = MIN_DECIMAL_PLACES;
				df.setMaximumFractionDigits(fractionDigits);
				textField.setText(df.format(value));
			} else {
				textField.setText("");
			}
		}

	}

	@SuppressWarnings("serial")
	class QueryVSXdialog extends JDialog {
		protected static final String sTITLE = "Query VSX";

		protected TextField fieldVSXname;
		protected DoubleField2 fieldVSXperiod;
		protected DoubleField2 fieldVSXepoch;
		protected TextField fieldVSXvarType;
		protected TextField fieldVSXspectralType;
		protected TextField fieldVSXcoordinates;
		protected JTextArea textArea;
		protected DoubleField2 fieldEphemerisFrom;
		protected DoubleField2 fieldEphemerisTo;
		protected DoubleField2 fieldEphemerisPhase;
		protected DoubleField2 fieldTimeZoneOffset;

		protected boolean closed = false;

		protected JButton queryButton;
		protected JButton phaseDialogButton;
		protected JButton resetButton;
		protected JButton calcEphemeris;
		protected JButton showImage;
		protected JButton goSIMBAD;

		private String starVSXname = "";
		private EphemerisDialog ephemerisDialog = null;
		
		private Double starRA = null;
		private Double starDec = null;
		private ImageDialog imageDialog = null;
		
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
			topPane.add(createInfoPane1());
			topPane.add(createInfoPane2());
			topPane.add(createInfoPane3());
			topPane.add(createInfoPane4());
			topPane.add(createButtonPane());
			topPane.add(createParamPane2());
			topPane.add(createButtonPane2());
			topPane.add(new JLabel(" "));
			topPane.add(new JSeparator(SwingConstants.HORIZONTAL));
			topPane.add(new JLabel(" "));
			topPane.add(createButtonPane3());
			//topPane.add(new JPanel());
			topPane.add(new JLabel(" "));
			topPane.add(new JSeparator(SwingConstants.HORIZONTAL));
			topPane.add(new JLabel(" "));
			topPane.add(createButtonPaneX(cancelListener));

			//this.getRootPane().setDefaultButton(queryButton);

			contentPane.add(topPane);

			this.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent we) {
					closed = true;
					closeEphemerisDialog(true);
					closeImageDialog(true);
				}
			});
			this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			
			this.pack();
			setLocationRelativeTo(Mediator.getUI().getContentPane());
			this.setVisible(true);
			
		}
		
		private JPanel createNamePane()	{
			JPanel panel = new JPanel(new BorderLayout());
			fieldVSXname = new TextField("VSX Name", lastVSXname);
			fieldVSXname.getUIComponent().addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					if(e.getKeyCode() == KeyEvent.VK_ENTER) {
						// Check queryButton state to prevent sending another request 
						// while async queryVSX() is running 
						if (queryButton.isEnabled())
							queryVSX();
					}
				}
			});
			panel.add(fieldVSXname.getUIComponent(), BorderLayout.CENTER);
			queryButton = new JButton("Query");
			queryButton.addActionListener(createQueryButtonListener());
			panel.add(queryButton, BorderLayout.LINE_END);

			return panel;
		}

		private JPanel createInfoPane1() {
			JPanel panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

			textArea = new JTextArea(12, 40);
			textArea.setEditable(false);
			JScrollPane scrollPane = new JScrollPane(textArea);
			panel.add(scrollPane);

			return panel;
		}
		
		private JPanel createInfoPane2() {
			JPanel panel = new JPanel();
			panel.setLayout(new GridLayout(0, 2));

			fieldVSXvarType = new TextField("Variability Type", "");
			fieldVSXvarType.setEditable(false);
			panel.add(fieldVSXvarType.getUIComponent());

			fieldVSXspectralType = new TextField("Spectral Type", "");
			fieldVSXspectralType.setEditable(false);
			panel.add(fieldVSXspectralType.getUIComponent());

			return panel;
		}

		private JPanel createInfoPane3() {
			JPanel panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

			fieldVSXcoordinates =  new TextField("J2000.0", "");
			fieldVSXcoordinates.setEditable(false);
			panel.add(fieldVSXcoordinates.getUIComponent());

			return panel;
		}

		private JPanel createInfoPane4() {
			JPanel panel = new JPanel();
			panel.setLayout(new GridLayout(0, 2));

			fieldVSXperiod = new DoubleField2("Period", null, null, null);
			fieldVSXperiod.setValue(0.0);
			//((JTextComponent) (fieldVSXperiod.getUIComponent())).setEditable(false);
			panel.add(fieldVSXperiod.getUIComponent());

			fieldVSXepoch = new DoubleField2("Epoch", null, null, null);
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
			fieldEphemerisFrom = new DoubleField2("Minimum JD", null, null, 0.0);
			fieldEphemerisTo = new DoubleField2("Maximum JD", null, null, 0.0);
			fieldEphemerisPhase = new DoubleField2("For Phase", null, null, 0.0);
			fieldTimeZoneOffset = new DoubleField2("Zone Offset (hours)", null, null, 0.0);
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
			//JPanel panel = new JPanel();
			calcEphemeris = new JButton("Calculate Ephemeris");
			calcEphemeris.addActionListener(createEphemerisButtonListener());
			panel.add(calcEphemeris);
			return panel;
		}

		private JPanel createButtonPane3() {
			//JPanel panel = new JPanel(new BorderLayout());
			JPanel panel = new JPanel();
			panel.setLayout(new GridLayout(0, 2));			
			showImage = new JButton("Show Image");
			showImage.addActionListener(createImageButtonListener());
			panel.add(showImage);
			goSIMBAD = new JButton("SIMBAD");
			goSIMBAD.addActionListener(createSIMBADButtonListener());
			panel.add(goSIMBAD);
			return panel;
		}
		
		private JPanel createButtonPaneX(ActionListener cancelListener) {
			JPanel panel = new JPanel();
			JButton helpButton = new JButton("Help");
			helpButton.addActionListener(createHelpButtonListener());
			panel.add(helpButton);
			JButton cancelButton = new JButton("Close");
			cancelButton.addActionListener(cancelListener);
			panel.add(cancelButton);
			resetButton = new JButton("Reset");
			resetButton.addActionListener(createResetButtonListener());
			panel.add(resetButton);

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

		// Return a listener for the "Help" button.
		protected ActionListener createHelpButtonListener() {
			return new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Help.openPluginHelp(getDocName());
				}
			};
		}
		
		// Return a listener for the "Reset" button.
		private ActionListener createResetButtonListener() {
			return new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					closeEphemerisDialog(false);
					closeImageDialog(false);
					fieldVSXname.setValue("");
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
						ShowError("Period must be > 0");
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
					closeImageDialog(true);
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
					starName = starVSXname;
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

		// Return a listener for the "Show Image" button.
		private ActionListener createImageButtonListener() {
			return new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (starRA == null || starDec == null) {
						ShowError("RA/Dec not specified");
						return;
					}
					if (imageDialog == null) {
						imageDialog = new ImageDialog(QueryVSXdialog.this, starRA, starDec);
					} else {
						imageDialog.setVisible(false);
						imageDialog.updateImage(starRA, starDec);
						imageDialog.setVisible(true);
					}
				}
			};
		}

		private void closeImageDialog(boolean destroy) {
			if (imageDialog != null) {
				imageDialog.setVisible(false);
				if (destroy) {
					imageDialog.dispose();
					imageDialog = null;
				}
			}
		}
		
		private ActionListener createSIMBADButtonListener() {
			return new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (starRA == null || starDec == null) {
						ShowError("RA/Dec not specified");
						return;
					}
					String sURL = String.format(SIMBAD_URL_TEMPLATE, starRA.toString(), starDec.toString());
					Help.openURLInWebBrowser(sURL, "Error");
				}
			};
		}
		
		private void resetVSXFields() {
			starRA = null;
			starDec = null;
			textArea.setText("");
			fieldVSXperiod.setValue(0.0);
			fieldVSXepoch.setValue(0.0);
			fieldVSXvarType.setValue("");
			fieldVSXspectralType.setValue("");
			fieldVSXcoordinates.setValue("");
		}

		private void queryVSX() {
			closeEphemerisDialog(false);
			closeImageDialog(false);
			starVSXname = fieldVSXname.getValue().trim();
			lastVSXname = starVSXname;
			if ("".equals(starVSXname))
				return;
			resetVSXFields();
			textArea.setText("Please wait...");			
			setTitle("Wait...");
			//pack();
			SwingWorker<VSQqueryResult, VSQqueryResult> worker = new VSXquerySwingWorker(this, starVSXname);
			queryButton.setEnabled(false);
			phaseDialogButton.setEnabled(false);
			resetButton.setEnabled(false);
			calcEphemeris.setEnabled(false);
			showImage.setEnabled(false);
			goSIMBAD.setEnabled(false);
			worker.execute();
		}
		
		private void ShowError(String msg) {
			MessageBox.showErrorDialog(QueryVSXdialog.this, "Error", msg);
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
			if (phase < 0) 
				phase += 1.0;
			// Epoch for chosen phase
			epoch += phase * period;
			// Calculate nearest epoch <= fromJD.
			double nearestEpoch = epoch + Math.floor((fromJD - epoch) / period) * period;
			
			String result = "Epoch\tUT\tUT+Offset\tPhase [0 - 1]\n";
			int i = 0;
			double jd = nearestEpoch;
			while (jd <= toJD) {
				if (jd >= fromJD && jd <= toJD) {
					if (i == maxEphemeris) {
						result += "(maximum number of rows reached)\n";
						return result;
					}
					result += String.format("%.5f", jd);
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
	
	@SuppressWarnings("serial")
	class ImageDialog extends JDialog {
		
		private JLabel label = null;
		
		protected ImageDialog(JDialog parent, Double starRA, Double starDec) {
			super(parent, "Image");
			setModalityType(Dialog.ModalityType.MODELESS);
			
			ActionListener cancelListener = createCancelButtonListener();
			getRootPane().registerKeyboardAction(cancelListener, 
					KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), 
					JComponent.WHEN_IN_FOCUSED_WINDOW);

			Container contentPane = this.getContentPane();

			JPanel panel = new JPanel();
			panel.setLayout(new BorderLayout());
			
			label = new JLabel();			
			panel.add(label, BorderLayout.CENTER);

			updateImage(starRA, starDec);

			JButton cancelButton = new JButton("Close");
			cancelButton.addActionListener(cancelListener);
			panel.add(cancelButton, BorderLayout.PAGE_END);

			contentPane.add(panel);

			this.pack();
			setLocationRelativeTo(parent);
			this.setVisible(true);
			
		}
		
		protected void updateImage(Double starRA, Double starDec)
		{
			if (starRA != null && starDec != null) {
				String sTitle = String.format(IMAGE_TITLE_TEMPLATE, starRA.toString(), starDec.toString());
				this.setTitle(sTitle);
				String sURL = String.format(IMAGE_URL_TEMPLATE, starRA.toString(), starDec.toString());
				try {
					getParent().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					try {
						Image image = ImageIO.read(new URL(sURL));
						if (image == null)
							throw new Exception("Cannot retrieve image");
						label.setIcon(new ImageIcon(image));
						this.pack();
					} finally {
						getParent().setCursor(null);
					}
				} catch (Exception e) {
					MessageBox.showErrorDialog(this, "Error", e.getLocalizedMessage());
				}
			}
		}
		
		private ActionListener createCancelButtonListener() {
			return new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setVisible(false);
				}
			};
		}

	}

	class VSQqueryResult {
		protected String name;
		protected Double period;
		protected Double epoch;
		protected String varType;
		protected String spType;
		protected String stringResult;

		private Double ra2000;
		private Double declination2000;

		protected String getCoordinates() {
			if (ra2000 == null || declination2000 == null)
				return null;
			double hours = ra2000 / 15.0;
			if (hours >= 0.0 && hours < 24.0 && declination2000 >= -90.0 && declination2000 <= 90.0) {
				int h = (int)(hours);
				int m = (int)((hours - h) * 60.0);
				double s = (hours - h - m / 60.0) * 60.0 * 60.0;
				// round to the number of digits specified after decimal separator in String.format() below. 
				s = Math.round(s * 100.0) / 100.0;
				// can be 60.00 after rounding!
				if (s > 59.99) {
					s = 0.0;
					m++;
					if (m > 59) {
						m = 0;
						h++;
						if (h == 24)
							h = 0;
					}
				}

				double declination_abs = Math.abs(declination2000);
				int deg = (int)(declination_abs);
				int arcmin = (int)((declination_abs - deg) * 60.0);
				double arcsec = (declination_abs - deg - arcmin / 60.0) * 60.0 * 60.0;
				// round to the number of digits specified after decimal separator in String.format() below.
				arcsec = Math.round(arcsec * 10.0) / 10.0;
				// can be 60.00 after rounding!
				if (arcsec > 59.9) {
					arcsec = 0.0;
					arcmin++;
					if (arcmin > 59) {
						arcmin = 0;
						deg++;
					}
				}

				char sign = declination2000 < 0 ? '-' : '+';

				return String.format("%02d:%02d:%05.2f %c%02d:%02d:%04.1f", h, m, s, sign, deg, arcmin, arcsec);
			}
			else
				return null;
		}

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
					dialog.showImage.setEnabled(true);
					dialog.goSIMBAD.setEnabled(true);
					dialog.fieldVSXname.setValue(vsxName);
					if (vsxResult != null) {
						dialog.textArea.setText(vsxResult.stringResult);
						dialog.textArea.setCaretPosition(0);
						dialog.fieldVSXperiod.setValue(vsxResult.period != null ? vsxResult.period : 0.0);
						dialog.fieldVSXepoch.setValue(vsxResult.epoch != null ? vsxResult.epoch : 0.0);
						dialog.fieldVSXvarType.setValue(vsxResult.varType);
						dialog.fieldVSXspectralType.setValue(vsxResult.spType);
						dialog.fieldVSXcoordinates.setValue(vsxResult.getCoordinates());
						dialog.starRA = vsxResult.ra2000;
						dialog.starDec = vsxResult.declination2000;
					} else {
						dialog.textArea.setText(error);
						dialog.fieldVSXperiod.setValue(0.0);
						dialog.fieldVSXepoch.setValue(0.0);
						dialog.fieldVSXvarType.setValue("");
						dialog.fieldVSXspectralType.setValue("");
						dialog.fieldVSXcoordinates.setValue("");
						dialog.starRA = null;
						dialog.starDec = null;
					}
					dialog.pack();
				}
			}
		}

		private VSQqueryResult queryVSX(String sVSXname, int maxStars)
				throws UnsupportedEncodingException, ParserConfigurationException, IOException, SAXException {
			VSQqueryResult vsxResult = new VSQqueryResult();
			String result = "";

			// Example: https://vsx.aavso.org/index.php?view=api.object&ident=pmak+v41
			String sURL = VSXquery.VSX_URL + URLEncoder.encode(sVSXname, StandardCharsets.UTF_8.name());
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
				else if ("ra2000".equalsIgnoreCase(tag))
					try {
						vsxResult.ra2000 = Double.parseDouble(content);
					} catch (NumberFormatException ex) {
						vsxResult.ra2000 = null;
					}
				else if ("Declination2000".equalsIgnoreCase(tag))
					try {
						vsxResult.declination2000 = Double.parseDouble(content);
					} catch (NumberFormatException ex) {
						vsxResult.declination2000 = null;
					} 
				result += tag + ": " + content + "\n";
			}
			if ("".equals(result))
				result = "No valid data returned by the query.";
			vsxResult.stringResult = result;
			return vsxResult;
		}
	}

}
