package org.aavso.tools.vstar.external.plugin;

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

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.aavso.tools.vstar.data.DateInfo;
import org.aavso.tools.vstar.data.InvalidObservation;
import org.aavso.tools.vstar.data.Magnitude;
import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.data.ValidObservation.JDflavour;
import org.aavso.tools.vstar.data.validation.InclusiveRangePredicate;
import org.aavso.tools.vstar.data.validation.JulianDayValidator;
import org.aavso.tools.vstar.data.validation.MagnitudeFieldValidator;
import org.aavso.tools.vstar.data.validation.UncertaintyValueValidator;
import org.aavso.tools.vstar.exception.ObservationReadError;
import org.aavso.tools.vstar.exception.ObservationValidationError;
import org.aavso.tools.vstar.input.AbstractObservationRetriever;
import org.aavso.tools.vstar.plugin.InputType;
import org.aavso.tools.vstar.plugin.ObservationSourcePluginBase;
//12/02/2018 C. Kotnik added name to observations so they can be
//saved and reloaded from a file.
//2019-05-21 (1) by PMAK: added 'filter' field recognition
//2019-05-21 (2) by PMAK: added dialog to select maximum error
//   The plugin can generate six different datasets at most:
//      1) Normal V
//      2) Normal g
//      3) 5 sigma limit V
//      4) 5 sigma limit g
//      5) user-defined excluded V
//      6) user-defined excluded g
//
//2019-05-23 by PMAK: load parameters dialog
//2019-05-30 by PMAK: support of ASAS-SN Photometry Database CSV.
//2019-05-31 by PMAK: Unknown series for all filters but V, g: support for possible future filters.
//       Those observaions are loaded without splitting into normal, 5sigma, Excluded
//
//2023-03-29 by PMAK: dynamic fields: more chance the dataset will be readable if the format is changed again 
//

import org.aavso.tools.vstar.ui.dialog.AbstractOkCancelDialog;
import org.aavso.tools.vstar.ui.dialog.DoubleField;
import org.aavso.tools.vstar.ui.mediator.Mediator;

/**
 * 
 * ASAS-SN file observation source plug-in for CSV observation files obtained
 * from https://asas-sn.osu.edu
 * 
 * This plug-in reads CSV files in this format:
 * 
 * HJD,UT Date,Camera,FWHM,Limit,mag,mag_err,flux(mJy),flux_err[,Filter]
 * 2458000.47009,2017-09-03.9710173,be,1.77,15.518,14.839,0.117,4.448,0.477[,V]
 * 2458000.47137,2017-09-03.9722892,be,1.76,15.730,14.760,0.090,4.789,0.392[,V]
 * 2458000.47263,2017-09-03.9735547,be,1.72,15.751,14.759,0.088,4.788,0.385[,V]
 * 2458002.51020,2017-09-06.0112978,be,2.03,14.814,>14.814,99.990,4.020,0.911[,V]
 * 2458002.51147,2017-09-06.0125694,be,2.03,14.729,>14.729,99.990,3.640,0.985[,V]
 * ...
 *
 * New format ASAS-SN Phometry Database added by PMAK (updated 2023-01-11 to reflect ASAS-SN portal changes):
 *
 * hjd,camera,mag,mag_err,flux,flux_err
 * 2457981.84031,bc,11.221,0.02,124.549,2.292
 * 2457478.08718,bc,11.167,0.02,130.945,2.409
 * 2456897.81061,bc,11.227,0.02,123.962,2.281
 * 2458372.79859,bc,11.252,0.02,121.073,2.228
 * 2457065.13389,bc,11.411,0.02,104.597,1.925
 * 2457303.71059,bc,11.452,0.02,100.68,1.853
 * ...
 *
 * ASAS-SN Sky Patrol V2.0
 * https://arxiv.org/abs/2304.03791
 * http://asas-sn.ifa.hawaii.edu/skypatrol/  
 * 
 * #<several comment lines>
 * <empty lines>
 * JD,Flux,Flux Error,Mag,Mag Error,Limit,FWHM,Filter,Quality
 * 2457148.1143957,26.0455,0.1300,12.9204,0.0054,16.9278,1.70,V,G
 * 2457164.0441139,27.0177,0.0732,12.8807,0.0029,17.5513,1.98,V,G
 * 2457275.7573557,22.4170,0.1109,13.0833,0.0054,17.1004,2.06,V,G
 * 2457372.724288,23.7647,0.0925,13.0199,0.0042,17.2975,2.16,V,G
 * ...
 * 
 * 
 */
public class ASASSNObservationSource extends ObservationSourcePluginBase {

	private SeriesType asassnVSeries;
	private SeriesType asassn5SigmaLimitSeries;     // mag_err = 99.99	
	private SeriesType asassn_g_Series;
	private SeriesType asassn5SigmaLimit_g_Series;  // mag_err = 99.99
	private SeriesType asassnExcludedVseries;       // above user-defined limit
	private SeriesType asassnExcluded_g_Series;     // above user-defined limit
	
	protected double paramUserDefinedErrLimit = 99.99;
	protected boolean paramLoadExcludedObs = true;
	protected boolean paramLoadVmagnitudes = true;
	protected boolean paramLoad_g_magnitudes = true;
	protected boolean paramLoadUnknownFilterMagnitudes = false;
	protected boolean paramLoadASASSN_V_as_Johnson_V = false;
	protected boolean paramLoadASASSN_g_as_Sloan_g = false;
	
	/**
	 * Constructor
	 */
	public ASASSNObservationSource() {
		super();
		
		asassnVSeries = SeriesType.create("ASAS-SN V", "ASAS-SN V", Color.GREEN,
				false, false);
		asassn5SigmaLimitSeries = SeriesType.create("ASAS-SN V 5\u03C3 limit",
				"ASAS-SN limit", Color.BLUE, false, false);
		asassnExcludedVseries = SeriesType.create("ASAS-SN V Excluded",
				"ASAS-SN excluded", Color.CYAN, false, false);
		asassn_g_Series = SeriesType.create("ASAS-SN g", "ASAS-SN g", Color.MAGENTA,
				false, false);
		asassn5SigmaLimit_g_Series = SeriesType.create("ASAS-SN g 5\u03C3 limit",
				"ASAS-SN limit g", Color.GRAY, false, false);
		asassnExcluded_g_Series = SeriesType.create("ASAS-SN g Excluded",
				"ASAS-SN excluded g", Color.PINK, false, false);

	}

	/**
	 * @see org.aavso.tools.vstar.plugin.ObservationSourcePluginBase#getInputType()
	 */
	@Override
	public InputType getInputType() {
		return InputType.FILE_OR_URL;
	}

	/**
	 * @see org.aavso.tools.vstar.plugin.IPlugin#getDescription()
	 */
	@Override
	public String getDescription() {
		return "ASAS-SN CSV file reader";
	}

	/**
	 * @see org.aavso.tools.vstar.plugin.IPlugin#getDisplayName()
	 */
	@Override
	public String getDisplayName() {
		return "New Star from ASAS-SN V2.0 File...";
	}

	/**
	 * @see org.aavso.tools.vstar.plugin.ObservationSourcePluginBase#getObservationRetriever()
	 */
	@Override
	public AbstractObservationRetriever getObservationRetriever() {
		// Dialog moved from retrieveObservations() where it invoked from non-UI thread
		// to this more natural place.
		// No annoying "No observations for the specified period" messages.
		//System.out.println(Thread.currentThread().getId());
		ASASSNParameterDialog paramDialog = new ASASSNParameterDialog();
		if (paramDialog.isCancelled()) {
			// It seems it is safe to return null here.
			return null;
		}
		paramUserDefinedErrLimit = paramDialog.getErrorLimit();
		paramLoadVmagnitudes = paramDialog.isLoadVmagnitudes();
		paramLoad_g_magnitudes = paramDialog.isLoad_g_magnitudes();
		paramLoadUnknownFilterMagnitudes = paramDialog.isLoadUnknownFilterMagnitudes();
		paramLoadExcludedObs = paramDialog.isLoadExcludedObs();
		paramLoadASASSN_V_as_Johnson_V = paramDialog.isLoadASASSN_V_as_Johnson_V();
		paramLoadASASSN_g_as_Sloan_g = paramDialog.isLoadASASSN_g_as_Sloan_g();

		return new ASASSNFileReader(
				paramUserDefinedErrLimit, paramLoadExcludedObs,  
				paramLoadVmagnitudes, paramLoad_g_magnitudes, 
				paramLoadUnknownFilterMagnitudes, 
				paramLoadASASSN_V_as_Johnson_V,
				paramLoadASASSN_g_as_Sloan_g);
	}

	class ASASSNFileReader extends AbstractObservationRetriever {

		private Map<String, Integer> fieldIndices;
		private Map<String, Integer> optionalFieldIndices;

		private List<String> lines;
		
		private double userDefinedErrLimit;
		private boolean loadExcludedObs;
		private boolean loadVmagnitudes;
		private boolean load_g_magnitudes;
		private boolean loadUnknownFilterMagnitudes;
		private boolean loadASASSN_V_as_Johnson_V;
		private boolean loadASASSN_g_as_Sloan_g;
		
		private static final double INVALID_MAG = 99.99;
		
		private static final String DELIMITER = ",";

		private JulianDayValidator julianDayValidator;
		private MagnitudeFieldValidator magnitudeFieldValidator;
		private UncertaintyValueValidator uncertaintyValueValidator;

		public ASASSNFileReader(
				double userDefinedErrLimit,
				boolean loadExcludedObs,
				boolean loadVmagnitudes,
				boolean load_g_magnitudes,
				boolean loadUnknownFilterMagnitudes,
				boolean loadASASSN_V_as_Johnson_V,
				boolean loadASASSN_g_as_Sloan_g
				) {
			super(getVelaFilterStr());

			this.userDefinedErrLimit = userDefinedErrLimit;
			this.loadExcludedObs = loadExcludedObs;
			this.loadVmagnitudes = loadVmagnitudes;
			this.load_g_magnitudes = load_g_magnitudes;
			this.loadUnknownFilterMagnitudes = loadUnknownFilterMagnitudes;
			this.loadASASSN_V_as_Johnson_V = loadASASSN_V_as_Johnson_V;
			this.loadASASSN_g_as_Sloan_g = loadASASSN_g_as_Sloan_g;
			
			fieldIndices = new HashMap<String, Integer>();
			optionalFieldIndices = new HashMap<String, Integer>();
			
			julianDayValidator = new JulianDayValidator();
			magnitudeFieldValidator = new MagnitudeFieldValidator();
			uncertaintyValueValidator = new UncertaintyValueValidator(new InclusiveRangePredicate(0, 99.99));
			
			setJDflavour(JDflavour.HJD);
		}

		@Override
		public String getSourceName() {
			return getInputName();
		}

		@Override
		public String getSourceType() {
			return "ASAS-SN V2.0 CSV File";
		}

		@Override
		public void retrieveObservations() throws ObservationReadError,
				InterruptedException {

			if (lines.size() == 0) {
				return;
			}
			
			String[] header = null;
			
			String firstError = null;
			for (int i = 0; i < lines.size(); i++) {
				String line = lines.get(i);
				if (line != null) {
					line = line.trim();
					if (!"".equals(line) && line.charAt(0) != '#') {
						if (header != null) {
							try {
								ValidObservation ob = readNextObservation(
										line.split(DELIMITER), i + 1,
										userDefinedErrLimit, loadASASSN_V_as_Johnson_V, loadASASSN_g_as_Sloan_g);
								if (ob != null)
								{
									Magnitude mag = ob.getMagnitude();
									// Skip any observation whose magnitude and
									// error are 99.99
									if (mag.getMagValue() != INVALID_MAG
										&& mag.getUncertainty() != INVALID_MAG 
										&& isBandSelected(ob))
									{
										collectObservation(ob);
									}
								}
							} catch (Exception e) {
								// Create an invalid observation.
								String error = e.getLocalizedMessage();
								if (firstError == null) firstError = error;
								InvalidObservation ob = new InvalidObservation(line, error);
								ob.setRecordNumber(i + 1);
								addInvalidObservation(ob);
							}
						} else {
							header = checkForHeaderAndFillFieldIndices(line.split(DELIMITER));
						}
					}
				}
				incrementProgress();
			}
		}

		private String[] checkForHeaderAndFillFieldIndices(String[] fields) {

			// look for HJD, MAG, MAG_ERR
			int hjd_index = indexInArrayIgnoreCase("HJD", fields);
			int mag_index = indexInArrayIgnoreCase("MAG", fields);
			int err_index = indexInArrayIgnoreCase("MAG_ERR", fields);
			if (hjd_index >= 0 && mag_index >= 0 && err_index >= 0) {
				fieldIndices.put("HJD", hjd_index);
				fieldIndices.put("MAG", mag_index);
				fieldIndices.put("MAG_ERR", err_index);
			} else {
				// trying to find another combination (ASAS-SN V2): JD, Mag, Mag Error
				hjd_index = indexInArrayIgnoreCase("JD", fields);
				mag_index = indexInArrayIgnoreCase("MAG", fields);
				err_index = indexInArrayIgnoreCase("MAG ERROR", fields);
				if (hjd_index >= 0 && mag_index >= 0 && err_index >= 0) {
					fieldIndices.put("HJD", hjd_index);
					fieldIndices.put("MAG", mag_index);
					fieldIndices.put("MAG_ERR", err_index);
				} else {
					// not a header line
					return null;
				}
			}
			
			// Is there a Filter field? (not exist in the old format and photometry DB format: V by default)
			int index = indexInArrayIgnoreCase("Filter", fields);
			fieldIndices.put("FILTER", index); // index = -1 if there is no 'Filter' field
			
			// Optional fields
						
			index = indexInArrayIgnoreCase("UT Date", fields);
			optionalFieldIndices.put("UT", index);
			
			index = indexInArrayIgnoreCase("Camera", fields);
			optionalFieldIndices.put("CAMERA", index);

			index = indexInArrayIgnoreCase("FWHM", fields);
			optionalFieldIndices.put("FWHM", index);
			
			index = indexInArrayIgnoreCase("Limit", fields);
			optionalFieldIndices.put("LIMIT", index);
			
			index = indexInArrayIgnoreCase("flux(mJy)", fields);
			if (index < 0) {
				index = indexInArrayIgnoreCase("flux", fields);
			}
			optionalFieldIndices.put("FLUX", index);

			index = indexInArrayIgnoreCase("flux_err", fields);
			if (index < 0) {
				index = indexInArrayIgnoreCase("Flux Error", fields);
			}
			optionalFieldIndices.put("FLUX_ERR", index);
			
			index = indexInArrayIgnoreCase("Quality", fields);
			optionalFieldIndices.put("QUALITY", index);
			
			return fields;
		}
		
		
		private int indexInArrayIgnoreCase(String s, String[] a) {
			for (int i = 0; i < a.length; i++) {
				if (s.toUpperCase().equals(a[i].toUpperCase())) {
					return i;
				}
			}
			return -1;
		}		
		
		
		private boolean isBandSelected(ValidObservation ob)
		{
			if (ob == null) return false;
			SeriesType series = ob.getBand();
			return  loadVmagnitudes   && (series == asassnVSeries || series == SeriesType.Johnson_V) || 
					load_g_magnitudes && (series == asassn_g_Series || series == SeriesType.Sloan_g) || 
					loadExcludedObs   && loadVmagnitudes   && (series == asassn5SigmaLimitSeries || series == asassnExcludedVseries) ||
					loadExcludedObs   && load_g_magnitudes && (series == asassn5SigmaLimit_g_Series || series == asassnExcluded_g_Series) ||
					loadUnknownFilterMagnitudes && (series == SeriesType.Unknown);
		}

		private ValidObservation readNextObservation(
				String[] fields, int lineNum,
				double userDefinedErrLimit, 
				boolean loadASASSN_V_as_Johnson_V, 
				boolean loadASASSN_g_as_Sloan_g)
				throws ObservationValidationError {

			SeriesType series = null;
			String filter = "";
			
			// Is there a Filter field? (absent in the old format and photometry DB format: V by default)
			int index = fieldIndices.get("FILTER");
			if (index < 0) {
				filter = "V";
			} else {
				filter = fields[index].trim();
			}
			
			if (filter.equals("V"))
			{
				if (!loadASASSN_V_as_Johnson_V)
					series = asassnVSeries;
				else
					series = SeriesType.Johnson_V;
			}
			else
			if (filter.equals("g"))
			{
				if (!loadASASSN_g_as_Sloan_g)
					series = asassn_g_Series;
				else
					series = SeriesType.Sloan_g;
			}
			else
				series = SeriesType.Unknown;

			ValidObservation observation = new ValidObservation();

			DateInfo hjd = julianDayValidator.validate(fields[fieldIndices.get("HJD")].trim());
			Magnitude mag = magnitudeFieldValidator.validate(fields[fieldIndices.get("MAG")].trim());
			double err = uncertaintyValueValidator.validate(fields[fieldIndices.get("MAG_ERR")]);
			if (err == INVALID_MAG) {
				err = 0.0;
				if (filter.equals("V"))
					series = asassn5SigmaLimitSeries;
				else
				if (filter.equals("g"))	
					series = asassn5SigmaLimit_g_Series;
				else
					; // do nothing: Unknown series
			}
			else
			if (err > userDefinedErrLimit)
			{
				if (filter.equals("V"))
					series = asassnExcludedVseries;
				else
				if (filter.equals("g"))
					series = asassnExcluded_g_Series;
				else
					; // do nothing: Unknown series
			}
			mag.setUncertainty(err);
			observation.setName(getInputName());
			observation.setMagnitude(mag);
			observation.setDateInfo(hjd);
			observation.setRecordNumber(lineNum);
			observation.setBand(series);
			
			// optional fields
			List<String> keys = new ArrayList<String>(optionalFieldIndices.keySet());
			Collections.sort(keys);
			for (String key : keys) {			
				int i = optionalFieldIndices.get(key);
				if (i >= 0) {
					observation.addDetail(key.toUpperCase(), fields[i].trim(), key);
				}
			}
			
			// Include original ASAS-SN band
			index = fieldIndices.get("FILTER");
			if (index < 0) {
				observation.addDetail("ASASSN_BAND", "", "ASASSN_BAND");
			} else {
				observation.addDetail("ASASSN_BAND", fields[index].trim(), "ASASSN_BAND");
			}
			
			return observation;
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
	
	}
	
	@SuppressWarnings("serial")
	class ASASSNParameterDialog extends
			AbstractOkCancelDialog {

		private DoubleField errorLimitField;
		private JCheckBox loadVmagnitudesCheckBox;
		private JCheckBox load_g_magnitudesCheckBox;
		private JCheckBox loadUnknownFilterMagnitudesCheckBox;
		private JCheckBox loadExcludedObsCheckBox;
		private JCheckBox loadASASSN_V_as_Johnson_V_CheckBox;
		private JCheckBox loadASASSN_g_as_Sloan_g_CheckBox;

		/**
		 * Constructor
		 */
		public ASASSNParameterDialog() {
			super("Parameters");

			Container contentPane = this.getContentPane();

			JPanel topPane = new JPanel();
			topPane.setLayout(new BoxLayout(topPane, BoxLayout.PAGE_AXIS));
			topPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

			topPane.add(createParameterPane());
			topPane.add(createParameterPane2());
			topPane.add(createParameterPane3());

			// OK, Cancel
			topPane.add(createButtonPane());

			contentPane.add(topPane);

			this.pack();
			setLocationRelativeTo(Mediator.getUI().getContentPane());
			this.setVisible(true);
		}
		
		private JPanel createParameterPane() {
			JPanel panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
			
			errorLimitField = new DoubleField("Exclude observations having an error greater than: ", 0.0, 99.99, paramUserDefinedErrLimit);
			panel.add(errorLimitField.getUIComponent());
			panel.add(Box.createRigidArea(new Dimension(75, 10)));

			return panel;
		}

		private JPanel createParameterPane2() {
			JPanel panel = new JPanel();
			panel.setBorder(BorderFactory.createTitledBorder(""));

			loadVmagnitudesCheckBox = new JCheckBox("Load V magnitudes?");
			loadVmagnitudesCheckBox.setSelected(paramLoadVmagnitudes);
			panel.add(loadVmagnitudesCheckBox);

			load_g_magnitudesCheckBox = new JCheckBox("Load g magnitudes?");
			load_g_magnitudesCheckBox.setSelected(paramLoad_g_magnitudes);
			panel.add(load_g_magnitudesCheckBox);
			
			loadExcludedObsCheckBox = new JCheckBox("Load 5 sigma / excluded observations?");
			loadExcludedObsCheckBox.setSelected(paramLoadExcludedObs);
			panel.add(loadExcludedObsCheckBox);

			loadUnknownFilterMagnitudesCheckBox = new JCheckBox("Load Unknown Filters? (*)");
			loadUnknownFilterMagnitudesCheckBox.setToolTipText("Observations in Unknown Filtes (other tnan V, g) are loaded without splitting into normal / 5-sigma / excluded");
			loadUnknownFilterMagnitudesCheckBox.setSelected(paramLoadUnknownFilterMagnitudes);
			panel.add(loadUnknownFilterMagnitudesCheckBox);

			return panel;
		}
		
		private JPanel createParameterPane3() {
			JPanel panel = new JPanel();
			panel.setBorder(BorderFactory.createTitledBorder(""));

			loadASASSN_V_as_Johnson_V_CheckBox = new JCheckBox("Load ASAS-SN V magnitudes as Johnson V?");
			loadASASSN_V_as_Johnson_V_CheckBox.setSelected(paramLoadASASSN_V_as_Johnson_V);
			panel.add(loadASASSN_V_as_Johnson_V_CheckBox);

			loadASASSN_g_as_Sloan_g_CheckBox = new JCheckBox("Load ASAS-SN g magnitudes as Sloan g?");
			loadASASSN_g_as_Sloan_g_CheckBox.setSelected(paramLoadASASSN_g_as_Sloan_g);
			panel.add(loadASASSN_g_as_Sloan_g_CheckBox);

			return panel;
		}
		
		/**
		 * 
		 */
		public double getErrorLimit() {
			return errorLimitField.getValue();
		}

		/**
		 * 
		 */
		public boolean isLoadVmagnitudes() {
			return loadVmagnitudesCheckBox.isSelected();
		}

		/**
		 * 
		 */
		public boolean isLoad_g_magnitudes() {
			return load_g_magnitudesCheckBox.isSelected();
		}
		
		/**
		 * 
		 */
		public boolean isLoadUnknownFilterMagnitudes() {
			return loadUnknownFilterMagnitudesCheckBox.isSelected();
		}

		/**
		 * 
		 */
		public boolean isLoadExcludedObs() {
			return loadExcludedObsCheckBox.isSelected();
		}
		
		/**
		 * 
		 */
		public boolean isLoadASASSN_V_as_Johnson_V() {
			return loadASASSN_V_as_Johnson_V_CheckBox.isSelected();
		}
		
		/**
		 * 
		 */
		public boolean isLoadASASSN_g_as_Sloan_g() {
			return loadASASSN_g_as_Sloan_g_CheckBox.isSelected();
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

			if (errorLimitField.getValue() == null) {
				ok = false;
			}

			if (ok) {
				cancelled = false;
				setVisible(false);
				dispose();
			}
		}
	}

}
