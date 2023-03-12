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

import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.aavso.tools.vstar.data.DateInfo;
import org.aavso.tools.vstar.data.InvalidObservation;
import org.aavso.tools.vstar.data.MTypeType;
import org.aavso.tools.vstar.data.Magnitude;
import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.data.ValidObservation.JDflavour;
import org.aavso.tools.vstar.data.validation.InclusiveRangePredicate;
import org.aavso.tools.vstar.data.validation.JulianDayValidator;
import org.aavso.tools.vstar.data.validation.MagnitudeFieldValidator;
import org.aavso.tools.vstar.data.validation.UncertaintyValueValidator;
import org.aavso.tools.vstar.exception.CancellationException;
import org.aavso.tools.vstar.exception.ObservationReadError;
import org.aavso.tools.vstar.exception.ObservationValidationError;
import org.aavso.tools.vstar.input.AbstractObservationRetriever;
import org.aavso.tools.vstar.input.database.VSXWebServiceStarInfoSource;
import org.aavso.tools.vstar.plugin.InputType;
import org.aavso.tools.vstar.plugin.ObservationSourcePluginBase;
import org.aavso.tools.vstar.plugin.PluginComponentFactory;
import org.aavso.tools.vstar.ui.dialog.AbstractOkCancelDialog;
import org.aavso.tools.vstar.ui.dialog.MessageBox;
import org.aavso.tools.vstar.ui.dialog.TextArea;
import org.aavso.tools.vstar.ui.dialog.TextField;
import org.aavso.tools.vstar.ui.mediator.StarInfo;
import org.aavso.tools.vstar.util.Pair;
import org.aavso.tools.vstar.util.locale.NumberParser;

/**
 * 
 * @author max (PMAK)
 * 
 * see https://irsa.ipac.caltech.edu/docs/program_interface/ztf_lightcurve_api.html
 * 
 */
public class ZTFObSource extends ObservationSourcePluginBase {

	private Map<String, Integer> fieldIndices;
	
	private SeriesType ztfgSeries;
	private SeriesType ztfrSeries;
	private SeriesType ztfiSeries;
	private SeriesType ztfUnknownSeries;
	
	private String baseURL = "https://irsa.ipac.caltech.edu/cgi-bin/ZTF/nph_light_curves?FORMAT=TSV&";
	
	//private StarInfo starInfo;
	
	// Create static VeLa filter field here since cannot create it in
	// inner dialog class.
	private static Pair<TextArea, JPanel> velaFilterFieldPanelPair;

	static {
		velaFilterFieldPanelPair = PluginComponentFactory.createVeLaFilterPane();
	}
	
	private ZTFParameterDialog paramDialog;

	/**
	 * Constructor
	 */
	public ZTFObSource() {
		super();
		ztfgSeries = SeriesType.create("ZTF zg", "ZTF zg", Color.GREEN, false, false);
		ztfrSeries = SeriesType.create("ZTF zr", "ZTF zr", Color.RED, false, false);
		ztfiSeries = SeriesType.create("ZTF zi", "ZTF zi", new Color(192, 64, 0), false, false);
		ztfUnknownSeries = SeriesType.create("ZTF unknown", "ZTF unknown", new Color(255, 255, 0), false, false);
		fieldIndices = new HashMap<String, Integer>();
		fieldIndices.put("oid", -1);
		fieldIndices.put("hjd", -1);
		fieldIndices.put("mag", -1);
		fieldIndices.put("magerr", -1);
		fieldIndices.put("catflags", -1);
		fieldIndices.put("filtercode", -1);
		fieldIndices.put("exptime", -1);
		fieldIndices.put("airmass", -1);
	}
	
	/**
	 * @see org.aavso.tools.vstar.plugin.ObservationSourcePluginBase#getInputType()
	 */
	@Override
	public InputType getInputType() {
		return InputType.URL;
	}
	
	@Override
	public List<URL> getURLs() throws Exception {
		List<URL> urls = new ArrayList<URL>();

		//starInfo = null;
		
		if (paramDialog == null) {
			paramDialog = new ZTFParameterDialog(isAdditive());
		}
		paramDialog.showDialog();
		if (!paramDialog.isCancelled()) {
			//StarInfo starInfo = paramDialog.getStarInfo();
			
			setAdditive(paramDialog.isLoadAdditive());
			
			String url;
			if (paramDialog.getSearchByID()) {
				url = baseURL + "ID=" + paramDialog.getObjectID();
			} else {
				url = baseURL + "POS=CIRCLE%20" +
						String.format(Locale.ENGLISH, "%.5f%%20%.5f%%20%.5f", paramDialog.getRA(), paramDialog.getDec(), paramDialog.getRadius());  
			}
			
			if (paramDialog.isCatflagsZero()) {
				url += "&BAD_CATFLAGS_MASK=65535";
			}
			
			try {
				System.out.println(url);
				urls.add(new URL(url));
			} catch (MalformedURLException e) {
				throw new ObservationReadError("Cannot construct ZTF URL (reason: " + e.getLocalizedMessage() + ")");
			}
			
			setVelaFilterStr(paramDialog.getVelaFilterStr());
		} else {
			throw new CancellationException();
		}
		return urls;
	}
	
	/**
	 * @see org.aavso.tools.vstar.plugin.ObservationSourcePluginBase#
	 *      getObservationRetriever ()
	 */
	@Override
	public AbstractObservationRetriever getObservationRetriever() {
		return new ZTFFormatRetriever();
	}

	/**
	 * @see org.aavso.tools.vstar.plugin.IPlugin#getDescription()
	 */
	@Override
	public String getDescription() {
		return "ZTF Photometry Format reader";
	}

	/**
	 * @see org.aavso.tools.vstar.plugin.IPlugin#getDisplayName()
	 */
	@Override
	public String getDisplayName() {
		return "New Star from ZTF Photometry ...";
	}

	class ZTFFormatRetriever extends AbstractObservationRetriever {

		//private String obscode = "ZTF";
		private String delimiter = "\t";
		//private String objectName;
		private HashSet<String> ztfObjects;
		
		private List<String> lines;


		private JulianDayValidator julianDayValidator;
		private MagnitudeFieldValidator magnitudeFieldValidator;
		private UncertaintyValueValidator uncertaintyValueValidator;

		/**
		 * Constructor
		 */
		public ZTFFormatRetriever() {
			super(getVelaFilterStr());
			julianDayValidator = new JulianDayValidator();
			magnitudeFieldValidator = new MagnitudeFieldValidator();
			uncertaintyValueValidator = new UncertaintyValueValidator(new InclusiveRangePredicate(0, 1));
			ztfObjects = new HashSet<String>();
		}
		
		/**
		 * Extended information
		 */
		/*
		@Override
		public StarInfo getStarInfo() {
			if (starInfo != null) {
				starInfo.setRetriever(this);
				return starInfo;
			} else {
				return new StarInfo(this, getSourceName());
			}
		}
		*/
		
		@Override
		public void retrieveObservations() throws ObservationReadError,
				InterruptedException {

			setJDflavour(JDflavour.HJD);

			// read lines and determine the number of them 
			getNumberOfRecords();

			boolean headerFound = false;
			
			String firstError = null;
			for (int i = 0; i < lines.size(); i++) {
				String line = lines.get(i);
				if (line != null) {
					line = line.trim();
					if (!"".equals(line)) {
						if (headerFound) {
							try {
								ValidObservation vo = readNextObservation(line.split(delimiter), i + 1);
								collectObservation(vo);
							} catch (Exception e) {
								// Create an invalid observation.
								String error = e.getLocalizedMessage();
								if (firstError == null) firstError = error;
								InvalidObservation ob = new InvalidObservation(line, error);
								ob.setRecordNumber(i + 1);
								addInvalidObservation(ob);
							}
						} else {
							headerFound = checkForHeaderAndFillFieldIndices(line.split(delimiter));
						}
					}
				}
				incrementProgress();
			}
			
			if (!headerFound)
				throw new ObservationReadError("Cannot find ZTF header");
			
			if (validObservations.size() == 0 && firstError != null) {
				throw new ObservationReadError("No observations found. The first error message:\n" + firstError);
			}
			
		}
		
		private boolean checkForHeaderAndFillFieldIndices(String[] fields) {
			for (Map.Entry<String, Integer> entry : fieldIndices.entrySet()) {
				int i = indexInArray(entry.getKey(), fields);
				if (i >= 0) {
					entry.setValue(i);
				} else {
					return false;
				}
			}
			return true;
		}
		
		private int indexInArray(String s, String[] a) {
			for (int i = 0; i < a.length; i++) {
				if (s.equals(a[i])) {
					return i;
				}
			}
			return -1;
		}		
		
		@Override
		public Integer getNumberOfRecords() throws ObservationReadError {
			if (lines == null) {
				try {
					readLines();
				} catch (IOException e) {
					throw new ObservationReadError("Error reading lines");
				}
			}

			return lines.size();
		}

		// Read all lines from the source.
		private void readLines() throws IOException {
			lines = new ArrayList<String>();
			BufferedReader reader = new BufferedReader(new InputStreamReader(getInputStreams().get(0)));
			String line;
			while ((line = reader.readLine()) != null) {
				lines.add(line);
			}
		}

		// ZTF format observation reader.
		private ValidObservation readNextObservation(String[] fields, int recordNumber)
				throws ObservationValidationError {

			ValidObservation observation = new ValidObservation();

			String name = fields[fieldIndices.get("oid")].trim();
			ztfObjects.add(name);

			observation.setRecordNumber(recordNumber);
			observation.setName(name);
			//observation.setObsCode(obscode);

			DateInfo dateInfo = new DateInfo(julianDayValidator.validate(fields[fieldIndices.get("hjd")].trim()).getJulianDay());
			observation.setDateInfo(dateInfo);

			Magnitude magnitude = magnitudeFieldValidator.validate(fields[fieldIndices.get("mag")].trim());
			observation.setMagnitude(magnitude);
			double uncertainty = uncertaintyValueValidator.validate(fields[fieldIndices.get("magerr")].trim());
			observation.getMagnitude().setUncertainty(uncertainty);

			String filter = fields[fieldIndices.get("filtercode")].trim();
			SeriesType band;
			if (filter.equals("zg")) {
				band = ztfgSeries;
			} else if (filter.equals("zr")) {
				band = ztfrSeries;
			} else if (filter.equals("zi")) {
				band = ztfiSeries;
			} else {
				band = ztfUnknownSeries;
			}
			observation.setBand(band);

			// ValidObservation defaults to STD.
			observation.setMType(MTypeType.STD);

			//observation.setComments("");

			observation.addDetail("CATFLAGS", fields[fieldIndices.get("catflags")], "catflags");
			observation.addDetail("EXPTIME", fields[fieldIndices.get("exptime")], "exptime");
			observation.addDetail("AIRMASS", fields[fieldIndices.get("airmass")], "airmass");
			// todo: add other details
			
			return observation;
		}

		@Override
		public String getSourceName() {
			if (ztfObjects.size() == 0)
				return "ZTF object";
			String name = ""; 
			for (String ztf : ztfObjects) {
				if (name.length() > 0)
					name += ", ";
				name += ztf;
			}
			if (ztfObjects.size() == 1)
				return "ZTF object " + name;
			else
				return "ZTF objects " + name;
		}

		@Override
		public String getSourceType() {
			return "ZTF Format";
		}
	}

	@SuppressWarnings("serial")
	class ZTFParameterDialog extends AbstractOkCancelDialog {

		//private final int MIN_DECIMAL_PLACES = 20;
		
		private TextField objectIDField;
		private TextField objectRAField;
		private TextField objectDecField;
		private TextField objectRadiusField;
		private TextField objectVSXNameField;
		private JCheckBox additiveLoadCheckbox;
		private JCheckBox catflagsZeroCheckbox;
		private JTabbedPane searchParamPane;
		private JTabbedPane searchParamPane2;
		
		private String objectID;
		private Double objectRA;
		private Double objectDec;
		private Double objectRadius;
		//private StarInfo starInfo;		
		
		private Cursor waitCursor = new Cursor(Cursor.WAIT_CURSOR);
		
		public ZTFParameterDialog(boolean additiveChecked) {
			super("ZTF Photometry");

			Container contentPane = this.getContentPane();

			JPanel topPane = new JPanel();
			topPane.setLayout(new BoxLayout(topPane, BoxLayout.PAGE_AXIS));
			topPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

			createParameterPane();
			topPane.add(searchParamPane);

			searchParamPane.addChangeListener(
					new ChangeListener() {
						public void stateChanged(ChangeEvent e) {
							searchParamPaneUpdateFocus();
						}
					} );
			
			searchParamPane2.addChangeListener(
					new ChangeListener() {
						public void stateChanged(ChangeEvent e) {
							searchParamPane2UpdateFocus();
						}
					} );

			topPane.add(createCatflagsZeroCheckboxPane());			
			
			topPane.add(Box.createRigidArea(new Dimension(400, 20)));
			
			topPane.add(velaFilterFieldPanelPair.second);

			topPane.add(createAdditiveLoadCheckboxPane(additiveChecked));
			
			// OK, Cancel
			topPane.add(createButtonPane());

			contentPane.add(topPane);

			this.pack();
		}
		
		private void searchParamPaneUpdateFocus() {
			SwingUtilities.invokeLater( new Runnable() { 
				public void run() {
					switch (searchParamPane.getSelectedIndex()) {
						case 0:
							searchParamPane2UpdateFocus();
							break;
						case 1:
							objectIDField.getUIComponent().requestFocusInWindow();								
							break;
						default:
							break;
					}
				}
			} );
		}
		

		private void searchParamPane2UpdateFocus() {
			SwingUtilities.invokeLater( new Runnable() {
				public void run() {
					switch (searchParamPane2.getSelectedIndex()) {
						case 0:
							objectRAField.getUIComponent().requestFocusInWindow();
							break;
						case 1:
							objectVSXNameField.getUIComponent().requestFocusInWindow();
							break;
						default:
							break;
					}
				}
			} );
		}

		
		private void createParameterPane() {
			searchParamPane = new JTabbedPane();
			
			searchParamPane2 = new JTabbedPane();
			
			JPanel panelCoord = new JPanel();
			panelCoord.setLayout(new BoxLayout(panelCoord, BoxLayout.LINE_AXIS));
			objectRAField = new TextField("RA (degrees)", "0");
			panelCoord.add(objectRAField.getUIComponent());
			objectDecField = new TextField("Dec (degrees)", "0");
			panelCoord.add(objectDecField.getUIComponent());
			
			searchParamPane2.addTab("Coordinates", null, panelCoord, "Search by coordinates");
			
			JPanel panelVSX = new JPanel();
			panelVSX.setLayout(new BoxLayout(panelVSX, BoxLayout.LINE_AXIS));
			objectVSXNameField = new TextField("VSX name", "");
			panelVSX.add(objectVSXNameField.getUIComponent());
			
			searchParamPane2.addTab("VSX", null, panelVSX, "Search by VSX name");
			
			JPanel panelCoordAndVSX = new JPanel();
			panelCoordAndVSX.setLayout(new BoxLayout(panelCoordAndVSX, BoxLayout.PAGE_AXIS));
			panelCoordAndVSX.add(searchParamPane2);
			
			objectRadiusField = new TextField("Radius (degrees)", String.format(Locale.getDefault(), "%.4f", 0.0004));
			panelCoordAndVSX.add(objectRadiusField.getUIComponent());
			
			searchParamPane.addTab("Coordinates or VSX name", panelCoordAndVSX);
			
			JPanel panelID = new JPanel();
			panelID.setLayout(new BoxLayout(panelID, BoxLayout.PAGE_AXIS));
			panelID.add(Box.createRigidArea(new Dimension(100, 40)));
			objectIDField = new TextField("ZTF object ID", "");
			panelID.add(objectIDField.getUIComponent());
			panelID.add(Box.createRigidArea(new Dimension(100, 40)));
			
			searchParamPane.addTab("Object ID", null, panelID, "Search by ZTF object identifier");
		}

		private JPanel createCatflagsZeroCheckboxPane() {
			JPanel panel = new JPanel();
			panel.setBorder(BorderFactory.createTitledBorder("Catflags"));

			catflagsZeroCheckbox = new JCheckBox("ZTF data with catflags 0 only?", true);
			panel.add(catflagsZeroCheckbox);

			return panel;
		}
		
		private JPanel createAdditiveLoadCheckboxPane(boolean checked) {
			JPanel panel = new JPanel();
			panel.setBorder(BorderFactory.createTitledBorder("Additive Load"));

			additiveLoadCheckbox = new JCheckBox("Add to current?", checked);
			panel.add(additiveLoadCheckbox);

			return panel;
		}

		public String getObjectID() {
			return objectID;
		}
		
		public Double getRA() {
			return objectRA;
		}

		public Double getDec() {
			return objectDec;
		}

		public Double getRadius() {
			return objectRadius;
		}
		
		public boolean isCatflagsZero() {
			return catflagsZeroCheckbox.isSelected();
		}
		
		//public StarInfo getStarInfo() {
		//	return starInfo;
		//}
		
		public boolean getSearchByID() {
			return searchParamPane.getSelectedIndex() == 1;
		}
		
		public boolean getCoordinateSearchManual() {
			return searchParamPane2.getSelectedIndex() == 0;
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
		 * @return The VeLa filter string.
		 */
		public String getVelaFilterStr() {
			return velaFilterFieldPanelPair.first.getValue().trim();
		}

		@Override
		public void showDialog() {
			objectID = null;
			objectRA = null;
			objectDec = null;
			objectRadius = null;
			//starInfo = null;
			searchParamPaneUpdateFocus();			
			super.showDialog();			
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
			if (getSearchByID()) {
				objectID = objectIDField.getValue();
				if (objectID != null) objectID = objectID.trim();
				if (objectID == null || "".equals(objectID) || !objectID.matches("[0-9]+")) {
					objectIDField.getUIComponent().requestFocusInWindow();
					MessageBox.showErrorDialog("ZTF", "ZTF object ID must be numeric");
					return;
				}
			} else {
				objectRadius = getDouble(objectRadiusField, 0, 0.005, true, true, "Radius must be >= 0 and <= 0.005");
				if (objectRadius == null) {
					return;
				}
				if (getCoordinateSearchManual()) {
					objectRA = getDouble(objectRAField, 0, 360, true, false, "RA must be >= 0 and < 360");
					if (objectRA == null) {
						return;
					}
					objectDec = getDouble(objectDecField, -90, 90, true, true, "Dec must be >= -90 and <= 90");
					if (objectDec == null) {
						return;
					}
				}
				else {
					String vsxName = objectVSXNameField.getValue();
					if (vsxName != null) vsxName = vsxName.trim();
					if (vsxName == null || "".equals(vsxName)) {
						objectVSXNameField.getUIComponent().requestFocusInWindow();
						MessageBox.showErrorDialog("VSX", "VSX name must be specified");
						return;
					}
					
					try {
						StarInfo starInfo = ResolveVSXidentifier(vsxName);
						objectRA = starInfo.getRA().toDegrees();
						objectDec = starInfo.getDec().toDegrees();
					} catch (Exception e) {
						objectVSXNameField.getUIComponent().requestFocusInWindow();
						MessageBox.showErrorDialog("VSX", "Cannot resolve the VSX identifier.\nError message:\n" + 
								e.getLocalizedMessage());
						return;
					}
				}		
			}
			
			cancelled = false;
			setVisible(false);
			dispose();
		}

		/*
		private String DoubleToStringMaxDigits(Double value) {
			if (value != null) {
				DecimalFormat df = new DecimalFormat("0");
				int fractionDigits = NumericPrecisionPrefs.getOtherDecimalPlaces();
				if (fractionDigits < MIN_DECIMAL_PLACES) fractionDigits = MIN_DECIMAL_PLACES;
				df.setMaximumFractionDigits(fractionDigits);
				return df.format(value);
			} else {
				return "";
			}
		}
		*/
		
		private StarInfo ResolveVSXidentifier(String id) {
			Cursor defaultCursor = getCursor();
			setCursor(waitCursor);
			try {
				VSXWebServiceStarInfoSource infoSrc = new VSXWebServiceStarInfoSource();
				return infoSrc.getStarByName(id);
			} finally {
				setCursor(defaultCursor);
			}
		}
		
		private Double getDouble(TextField f, double min, double max, boolean min_inclusive, boolean max_inclusive, String errorMessage) {
			Double v;
			try {
				v = NumberParser.parseDouble(f.getValue());
			} catch (Exception e) {
				v = null;
				errorMessage = e.getLocalizedMessage();
			}
			if (v != null && (v > min || min_inclusive && v == min) && (v < max || max_inclusive && v == max)) {
				return v;
			} else {
				f.getUIComponent().requestFocusInWindow();
				MessageBox.showErrorDialog(f.getName(), errorMessage);
				return null;
			}
		}

	}

}
