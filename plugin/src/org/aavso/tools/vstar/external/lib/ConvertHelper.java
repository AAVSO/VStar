/**
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

package org.aavso.tools.vstar.external.lib;

import java.awt.Container;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;

import org.aavso.tools.vstar.input.database.VSXWebServiceStarInfoSource;
import org.aavso.tools.vstar.ui.dialog.AbstractOkCancelDialog;
import org.aavso.tools.vstar.ui.dialog.DoubleField;
import org.aavso.tools.vstar.ui.dialog.IntegerField;
import org.aavso.tools.vstar.ui.dialog.MessageBox;
import org.aavso.tools.vstar.ui.dialog.NumberFieldBase;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.StarInfo;
import org.aavso.tools.vstar.util.Pair;
import org.aavso.tools.vstar.util.Triple;
import org.aavso.tools.vstar.util.coords.DecInfo;
import org.aavso.tools.vstar.util.coords.EpochType;
import org.aavso.tools.vstar.util.coords.RAInfo;
import org.aavso.tools.vstar.util.help.Help;
import org.json.JSONObject;
import org.json.JSONArray;

public class ConvertHelper {

	public static final String ASTROUTILS_URL = "https://astroutils.astronomy.osu.edu";
	public static final String URL_TEMPLATE = ASTROUTILS_URL + "/time/convert.php?JDS=%s&RA=%s&DEC=%s&FUNCTION=%s";
	
	public static String localServiceURLstring = getLocalConvertServiceURLstring(); 
	
	/**
	 * A pane for entering RA/Dec with a button that gets coordinates from the VSX server by the VSX star name 
	 */
	@SuppressWarnings("serial")
	public static class CoordPane extends JPanel {
		
		private IntegerField2 raHour;
		private IntegerField2 raMin;
		private DoubleField2 raSec;
		
		private IntegerField2 decDeg;
		private IntegerField2 decMin;
		private DoubleField2 decSec;
		
		private JButton bByName;
	
		private static Cursor waitCursor = new Cursor(Cursor.WAIT_CURSOR);
	
		/**
		 * 
		 * @param ra
		 *                 initial RA value
		 * @param dec
		 *                 initial Dec value
		 * @param vertical
		 *                 layout
		 */
		public CoordPane(RAInfo ra, DecInfo dec, boolean vertical) {
			super();
			
			if (vertical)
				setLayout(new BoxLayout (this, BoxLayout.Y_AXIS));    
			
			JPanel subpanel = this;
	
			if (vertical) {
				subpanel = new JPanel();
				this.add(subpanel);
			}
				
			subpanel.add(new JLabel("RA: "));
			
			raHour = new IntegerField2("Hour", 0, 23, null);
			NumberFieldHelper.setNumberFieldColumns(raHour, 6);
			subpanel.add(raHour.getUIComponent());			
			
			raMin = new IntegerField2("Min", 0, 59, null);
			NumberFieldHelper.setNumberFieldColumns(raMin, 6);
			subpanel.add(raMin.getUIComponent());
			
			raSec = new DoubleField2("Sec", 0.0, 60.0, null);
			NumberFieldHelper.setNumberFieldColumns(raSec, 6);
			subpanel.add(raSec.getUIComponent());
			
			if (vertical) {
				subpanel = new JPanel();
				this.add(subpanel);
			}
			
			if (vertical)
				subpanel.add(new JLabel("Dec:"));
			else
				subpanel.add(new JLabel("    Dec: "));
			
			decDeg = new IntegerField2("Deg", -90, 90, null);
			NumberFieldHelper.setNumberFieldColumns(decDeg, 6);
			subpanel.add(decDeg.getUIComponent());
			
			decMin = new IntegerField2("Min", 0, 59, null);
			NumberFieldHelper.setNumberFieldColumns(decMin, 6);
			subpanel.add(decMin.getUIComponent());
			
			decSec = new DoubleField2("Sec", 0.0, 60.0, null);
			NumberFieldHelper.setNumberFieldColumns(decSec, 6);
			subpanel.add(decSec.getUIComponent());
			
			if (vertical)
				;
			else
				subpanel.add(new JLabel("    "));
			
			bByName = new JButton("VSX Name");
			bByName.addActionListener(createByNameButtonListener());
			
			this.add(bByName);
			
			setCoordinates(ra, dec);
		}
	
		/**
		 * 
		 * @return
		 *          returns coordinates as a Pair<RAInfo, DecInfo>  
		 */
		public Pair<RAInfo, DecInfo> getCoordinates() {
			
			Integer valRaHour = null;
			Integer valRaMin = null;
			Double valRaSec = null;
			Integer valDecDeg = null;
			Integer valDecMin = null;
			Double valDecSec = null;
	
			if ((valRaHour = raHour.getAndCheck()) == null) return null;
			if ((valRaMin = raMin.getAndCheck()) == null) return null;
			if ((valRaSec = raSec.getAndCheck()) == null) return null;
			if ((valDecDeg = decDeg.getAndCheck()) == null) return null;
			if ((valDecMin = decMin.getAndCheck()) == null) return null;
			if ((valDecSec = decSec.getAndCheck()) == null) return null;
			
			RAInfo ra = new RAInfo(EpochType.J2000, valRaHour, valRaMin, valRaSec);
			DecInfo dec = new DecInfo(EpochType.J2000, valDecDeg, valDecMin, valDecSec);
			
			return new Pair<RAInfo, DecInfo>(ra, dec);
		}
	
		/**
		 * Sets the initial coordinates
		 * 
		 * @param raInfo
		 * @param decInfo
		 */
		public void setCoordinates(RAInfo raInfo, DecInfo decInfo) {
			
			if (raInfo != null) {
				Triple<Integer, Integer, Double> ra = raInfo.toHMS();
				raHour.setValue(ra.first);
				raMin.setValue(ra.second);
				raSec.setValue(Math.round(ra.third * 100.0) / 100.0); //!!
			}
			
			if (decInfo != null) {
				Triple<Integer, Integer, Double> dec = decInfo.toDMS();
				decDeg.setValue(dec.first);
				decMin.setValue(dec.second);
				decSec.setValue(Math.round(dec.third * 100.0) / 100.0); //!!
			}
		}
		
		private ActionListener createByNameButtonListener() {
			return new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					VSXName();
				}
			};
		}

		/**
		 * Queries VSX for object's coordinates by an object name entered by the user 
		 */
		private void VSXName() {
			String name = JOptionPane.showInputDialog(Mediator.getUI().getContentPane(), "VSX Object Name");
			if (name == null || "".equals(name.trim()))
				return;
			Cursor defaultCursor = null;
			Container container = this.getParent();
			if (container != null) {
				defaultCursor = container.getCursor();
				container.setCursor(waitCursor);
			}
			try {
				StarInfo starInfo;
				VSXWebServiceStarInfoSource infoSrc = new VSXWebServiceStarInfoSource();
				try {
					starInfo = infoSrc.getStarByName(name);
				} catch (Exception ex) {
					MessageBox.showErrorDialog("Error", ex.getMessage());
					return;
				}
				
				setCoordinates(starInfo.getRA(), starInfo.getDec());
				
			} finally {
				if (container != null && defaultCursor != null) {
					container.setCursor(defaultCursor);
				}
			}
			
		}
		
		/**
		 * An extension of the IntegerField class with more user-friendly error checking  
		 */
		private class IntegerField2 extends IntegerField {
	
			public IntegerField2(String name, Integer min, Integer max, Integer initial) {
				super(name, min, max, initial);
			}
	
			public Integer getAndCheck() {
				return (Integer)NumberFieldHelper.getAndCheck(this);
			}
		}

		/**
		 * An extension of the DoubleField class with more user-friendly error checking  
		 */
		private class DoubleField2 extends DoubleField {
	
			public DoubleField2(String name, Double min, Double max, Double initial) {
				super(name, min, max, initial);
			}
	
			public Double getAndCheck() {
				return (Double)NumberFieldHelper.getAndCheck(this);
			}
		}
	
		private static class NumberFieldHelper {
			
			private static void setNumberFieldColumns(NumberFieldBase<?> field, int columns) {
				JTextField textField = (JTextField)(field.getUIComponent());
				textField.setColumns(columns);
			}
			
			// Get value of IntegerField, show message in case of error and set focus to the field
			public static Number getAndCheck(NumberFieldBase<?> input) {
				Number value;
				try {
					value = input.getValue();
				} catch (Exception e) {
					// We should never be here as far as getValue catches exceptions.  
					value = null;
				}
				if (value == null) {
					MessageBox.showErrorDialog("Error", input.getName() + ": Invalid value!\n" +
							"Value must be integer.\n" +
							numberFieldInfo(input));				
					(input.getUIComponent()).requestFocus();
					((JTextField)(input.getUIComponent())).selectAll();
				}
				return value;
			}
			
			private static String numberFieldInfo(NumberFieldBase<?> field) {
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
	}
	
	/**
	 * A general-purpose dialog for entering coordinates; utilizes CoordPane
	 */
	@SuppressWarnings("serial")
	public static class CoordDialog extends AbstractOkCancelDialog {
		
		private CoordPane coordPane;
		
		public CoordDialog() {
			super("Coordinates");
			
			Container contentPane = this.getContentPane();
			JPanel topPane = new JPanel();
			topPane.setLayout(new BoxLayout(topPane, BoxLayout.PAGE_AXIS));
			topPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			
			coordPane = new CoordPane(null, null, true);
			
			topPane.add(coordPane);
			
			// OK, Cancel
			topPane.add(createButtonPane());

			contentPane.add(topPane);

			this.pack();
			setLocationRelativeTo(Mediator.getUI().getContentPane());
			this.setVisible(true);
			
		}
		
		/**
		 * @see org.aavso.tools.vstar.ui.dialog.AbstractOkCancelDialog#cancelAction()
		 */
		@Override
		protected void cancelAction() {
			// Nothing to do.
		}

		/**
		 * @see org.aavso.tools.vstar.ui.dialog.AbstractOkCancelDialog#okAction()
		 */
		@Override
		protected void okAction() {
			boolean ok = true;

			if (getCoordinates() == null) {
				ok = false;
			}

			if (ok) {
				cancelled = false;
				setVisible(false);
				dispose();
			}
		}
		
		public Pair<RAInfo, DecInfo> getCoordinates() {
			return coordPane.getCoordinates();
		}
		
	}
	
	/**
	 * Return RA and Dec. First look for coordinates in any of our loaded
	 * datasets. Use the first coordinates found. We are making the simplifying
	 * assumption that all data sets correspond to the same object! If not
	 * found, ask the user to enter them. If none are supplied, null is
	 * returned.
	 * 
	 * @param info
	 *            a StarInfo object possibly containing coordinates
	 * @param otherCoords
	 *            Coordinates to use if info contains none.
	 * @return A pair of coordinates: RA and Declination
	 */
	public static Pair<RAInfo, DecInfo> getCoordinates(StarInfo info) {
		RAInfo ra = info.getRA();
		DecInfo dec = info.getDec();

		if (ra == null || dec == null) {
			ConvertHelper.CoordDialog coordDialog = new ConvertHelper.CoordDialog();
			if (coordDialog.isCancelled()) {
				return null;
			}
			Pair<RAInfo, DecInfo> coordinates = coordDialog.getCoordinates();
			ra = coordinates.first;
			dec = coordinates.second;
		}

		if (ra != null && dec != null) {
			return new Pair<RAInfo, DecInfo>(ra, dec);
		}
		
		return null;
	}
	
	/**
	 * This dialog can be moved to the VStar main package. 
	 */
	@SuppressWarnings("serial")
	public static class ConfirmDialogWithHelp extends AbstractOkCancelDialog {
		
		String helpTopic;

		public ConfirmDialogWithHelp(String title, String msg, String helpTopic) {
			super(title);
			initDialog(title, msg, helpTopic, false);
		}
		
		public ConfirmDialogWithHelp(String title, String msg, String helpTopic, boolean displayLocalServiceInfo) {
			super(title);
			initDialog(title, msg, helpTopic, displayLocalServiceInfo);
		}

		private void initDialog(String title, String msg, String helpTopic, boolean displayLocalServiceInfo) {	
			this.helpTopic = helpTopic;
			
			Container contentPane = this.getContentPane();

			JPanel topPane = new JPanel();
			topPane.setLayout(new BoxLayout(topPane, BoxLayout.PAGE_AXIS));
			topPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			
			topPane.add(createMessagePane(msg));

			// OK, Cancel, Help
			JPanel buttonPane = createButtonPane2();
			topPane.add(buttonPane);
			this.helpTopic = helpTopic;

			if (displayLocalServiceInfo && localServiceURLstring != null)
				topPane.add(createInfoPane("Local service: " + localServiceURLstring));
			
			contentPane.add(topPane);
			
			this.pack();
			setLocationRelativeTo(Mediator.getUI().getContentPane());
			okButton.requestFocusInWindow();
			this.setVisible(true);
		}
		
		private JPanel createMessagePane(String msg) {
			JPanel panel = new JPanel();
			JLabel labelMsg = new JLabel(msg);
			panel.add(labelMsg);
			return panel;
		}

		private JPanel createInfoPane(String msg) {
			JPanel panel = new JPanel();
			panel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
			JLabel labelMsg = new JLabel(msg);
			panel.add(labelMsg);
			return panel;
		}
		
		/**
		 * @see org.aavso.tools.vstar.ui.dialog.AbstractOkCancelDialog#helpAction()
		 */
		@Override
		protected void helpAction() {
			Help.openPluginHelp(helpTopic);
		}

		/**
		 * @see org.aavso.tools.vstar.ui.dialog.AbstractOkCancelDialog#cancelAction()
		 */
		@Override
		protected void cancelAction() {
			// Nothing to do.
		}

		/**
		 * @see org.aavso.tools.vstar.ui.dialog.AbstractOkCancelDialog#okAction()
		 */
		@Override
		protected void okAction() {
			cancelled = false;
			setVisible(false);
			dispose();
		}
	}
	
	public static String getLocalServiceURLstring() {
		return localServiceURLstring;
	}
	
	/**
	 * Uses the https://astroutils.astronomy.osu.edu service for conversion
	 * 
	 * @param times
	 *                   a list of JD or HJD epochs
	 * @param ra
	 *                   star's RA
	 * @param dec
	 *                   star's Dec
	 * @param func
	 *                   'utc2bjd': converts JD in UTC to BJD_TDB
	 *                   'hjd2bjd': converts HJD to BJD_TDB
	 * @return
	 *                  a list of BJD_TBD epochs
	 * @throws Exception
	 */
	public static List<Double> getConvertedListOfTimes(List<Double> times, double ra, double dec, String func)
			throws Exception {

		if (localServiceURLstring != null) {
			//System.out.println(localServiceURLstring);
			List<Double>out_times = convertWithLocalService(localServiceURLstring, times, ra, dec, func);
			return out_times;
		}
		
		Pair<String, String> result = getTextFromURLstring(getURLstring(times, ra, dec, func));
		if (result.second != null) {
			throw new Exception(result.second);
		}
		
		List<String> tempList = new ArrayList<String>(Arrays.asList(result.first.split("\n")));

		if (tempList.size() != times.size()) {
			throw new Exception("The server returned an invalid output:\n" + result.first);
		}
		
		List<Double>out_times = new ArrayList<Double>();
		for (String s1 : tempList) {
			double d;
			try {
				d = Double.parseDouble(s1);
			} catch (NumberFormatException e) {
				throw new Exception("The server returned an invalid output:\n" + result.first);
			}
			out_times.add(d);
		}
		
		return out_times;
	}

	private static List<Double> convertWithLocalService(String localServiceURLstring, List<Double> times, double ra, double dec, String func) 
			throws Exception {

        JSONObject json = new JSONObject();
        json.put("f", func);      // Conversion type  
        json.put("ra", ra);       // RA in degrees
        json.put("dec", dec);     // DEC in degrees
        json.put("lat", 0);       // Observer latitude
        json.put("lon", 0);       // Observer longitude
        json.put("elev", 0);      // Elevation in meters
		
        URL url = new URL(localServiceURLstring);
	    List<Double>out_times = new ArrayList<Double>();
	    json.put("jd", times);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        try {
	        conn.setRequestMethod("POST");
		    conn.setRequestProperty("Content-Type", "application/json");
		    conn.setDoOutput(true);
	        try (OutputStream os = conn.getOutputStream()) {
	        	byte[] input = json.toString().getBytes("utf-8");
	        	os.write(input, 0, input.length);
	        }
	        InputStream responseStream = conn.getInputStream();
	        String response = new BufferedReader(new InputStreamReader(responseStream)).lines().reduce("", (acc, line) -> acc + line);
	        JSONObject result = new JSONObject(response);
	        JSONArray jArray = result.getJSONArray("bjd_tdb");
	        if (jArray.length() != times.size()) {
	        	throw new Exception("convertWithLocalService error: invalid length of the resulting array");
	        }
	        for (int i = 0; i < jArray.length(); i++) {
	        	out_times.add(jArray.getDouble(i));
	        }	        
        } finally {
        	conn.disconnect();
        }
        return out_times;	        
	}
	
	
	private static String getCfgName() {
        try {
        	String home = System.getProperty("user.home");
        	File configFile = new File(home, ".vstar/vstar.properties");
    		return configFile.getPath();
        } catch (Exception e) {
            return null;
        }
    }
	
	private static String getLocalConvertServiceURLstring() {
		//return "http://localhost:5000/convert";
		Properties props = new Properties();
		try {
			try (FileInputStream in = new FileInputStream(getCfgName())) {
				props.load(in);
				String a = props.getProperty("localJDconverter.active");
				if (a != null) {
					if ("Y".equals(a.trim().toUpperCase())) {
						a = props.getProperty("localJDconverter.url");
						if (a != null) {
							a = a.trim();
							if (!"".equals(a))
								return a;
						}
					}
				}
			}
		} catch (Exception e) {
			return null;
		}
		return null;
	}
	
	private static String getURLstring(List<Double> times, double ra, double dec, String func) {

		String s = null;
		
		for (Double d : times) {
			if (s != null) s += ","; else s = "";
			s += String.valueOf(d);
		}
	
		return String.format(URL_TEMPLATE, s, String.valueOf(ra), String.valueOf(dec), func);
	}
		
	private static Pair<String, String> getTextFromURLstring(String urlString) 
			throws IOException, MalformedURLException, UnsupportedEncodingException {
		Pair<String, String> result = new Pair<String, String>(null, null); 
		
		URL url = new URL(urlString);
		URLConnection connection = url.openConnection();
		if (!(connection instanceof HttpURLConnection)) {
			result.second = "Not an HttpURLConnection";
			return result;
		}
		
		if (((HttpURLConnection)connection).getResponseCode() != HttpURLConnection.HTTP_OK) {
			result.second = ((HttpURLConnection)connection).getResponseMessage();
			return result;
		}
		
		InputStream stream = connection.getInputStream();
		StringBuilder textBuilder = new StringBuilder();
	    try (Reader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"))) {
	        int c = 0;
	        while ((c = reader.read()) != -1) {
	            textBuilder.append((char) c);
	        }
	        result.first = textBuilder.toString();
	        return result;
	    }
	
	}
}
