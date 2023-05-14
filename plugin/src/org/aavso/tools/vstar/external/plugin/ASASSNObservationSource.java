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
import java.io.InputStreamReader;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

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
import org.aavso.tools.vstar.ui.dialog.AbstractOkCancelDialog;
import org.aavso.tools.vstar.ui.dialog.DoubleField;
import org.aavso.tools.vstar.ui.mediator.Mediator;

/**
 * <p>
 * ASAS-SN file observation source plug-in for CSV observation files obtained
 * from https://asas-sn.osu.edu
 * </p>
 * <p>
 * This plug-in reads CSV files in this format:
 * </p>
 * HJD,UT Date,Camera,FWHM,Limit,mag,mag_err,flux(mJy),flux_err[,Filter]
 * 2458000.47009,2017-09-03.9710173,be,1.77,15.518,14.839,0.117,4.448,0.477[,V]
 * 2458000.47137,2017-09-03.9722892,be,1.76,15.730,14.760,0.090,4.789,0.392[,V]
 * 2458000.47263,2017-09-03.9735547,be,1.72,15.751,14.759,0.088,4.788,0.385[,V]
 * 2458002.51020,2017-09-06.0112978,be,2.03,14.814,>14.814,99.990,4.020,0.911[,V]
 * 2458002.51147,2017-09-06.0125694,be,2.03,14.729,>14.729,99.990,3.640,0.985[,V]
 * ...<br/>
 * <p>
 * New format ASAS-SN Photometry Database added by PMAK (updated 2023-01-11 to reflect ASAS-SN portal changes):
 * </p>
 * hjd,camera,mag,mag_err,flux,flux_err
 * 2457981.84031,bc,11.221,0.02,124.549,2.292
 * 2457478.08718,bc,11.167,0.02,130.945,2.409
 * 2456897.81061,bc,11.227,0.02,123.962,2.281
 * 2458372.79859,bc,11.252,0.02,121.073,2.228
 * 2457065.13389,bc,11.411,0.02,104.597,1.925
 * 2457303.71059,bc,11.452,0.02,100.68,1.853
 * ...<br/>
 */
public class ASASSNObservationSource extends ObservationSourcePluginBase {

	protected static final String SKY_PATROL_HEADER = "HJD,UT Date,Camera,FWHM,Limit,mag,mag_err,flux(mJy),flux_err"; // there can be optional Filter field at the end!
	protected static final String PHOT_DB_HEADER    = "hjd,camera,mag,mag_err,flux,flux_err";
	protected static final String HEADER_START      = "HJD";

	private SeriesType asassnVSeries;
	private SeriesType asassn5SigmaLimitSeries;     // mag_err = 99.99	
	private SeriesType asassn_g_Series;
	private SeriesType asassn5SigmaLimit_g_Series;  // mag_err = 99.99
	private SeriesType asassnExcludedVseries;       // above user-defined limit
	private SeriesType asassnExcluded_g_Series;     // above user-defined limit
	
	protected double userDefinedErrLimit = 99.99;
	protected boolean loadExcludedObs = true;
	protected boolean loadVmagnitudes = true;
	protected boolean load_g_magnitudes = true;
	protected boolean loadUnknownFilterMagnitudes = false;
	protected boolean loadASASSN_V_as_Johnson_V = false;
	protected boolean loadASASSN_g_as_Sloan_g = false;
	
	protected enum fileModeASASSNenum {
		AUTO,
		SKY_PATROL,
		PHOT_DB;
	}
	
	protected fileModeASASSNenum fileModeASASSN = fileModeASASSNenum.AUTO;

	/**
	 * Constructor
	 */
	public ASASSNObservationSource() {
		asassnVSeries = SeriesType.create("ASAS-SN", "ASAS-SN", Color.GREEN,
				false, false);
		asassn5SigmaLimitSeries = SeriesType.create("ASAS-SN 5\u03C3 limit",
				"ASAS-SN limit", Color.BLUE, false, false);
		asassnExcludedVseries = SeriesType.create("ASAS-SN Excluded",
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
		return "New Star from ASAS-SN File...";
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
		userDefinedErrLimit = paramDialog.getErrorLimit();
		loadVmagnitudes = paramDialog.isLoadVmagnitudes();
		load_g_magnitudes = paramDialog.isLoad_g_magnitudes();
		loadUnknownFilterMagnitudes = paramDialog.isLoadUnknownFilterMagnitudes();
		loadExcludedObs = paramDialog.isLoadExcludedObs();
		loadASASSN_V_as_Johnson_V = paramDialog.isLoadASASSN_V_as_Johnson_V();
		loadASASSN_g_as_Sloan_g = paramDialog.isLoadASASSN_g_as_Sloan_g();
		fileModeASASSN = paramDialog.getFileModeASASSN();
		return new ASASSNFileReader();
	}

	class ASASSNFileReader extends AbstractObservationRetriever {

		private static final double MAX = 99.99;

		private JulianDayValidator julianDayValidator;
		private MagnitudeFieldValidator magnitudeFieldValidator;
		private UncertaintyValueValidator uncertaintyValueValidator;

		public ASASSNFileReader() {
			super(getVelaFilterStr());
			julianDayValidator = new JulianDayValidator();
			magnitudeFieldValidator = new MagnitudeFieldValidator();
			uncertaintyValueValidator = new UncertaintyValueValidator(
					new InclusiveRangePredicate(0, 99.99));
			setJDflavour(JDflavour.HJD);
		}

		@Override
		public String getSourceName() {
			return getInputName();
		}

		@Override
		public String getSourceType() {
			return "ASAS-SN CSV File";
		}

		@Override
		public void retrieveObservations() throws ObservationReadError,
				InterruptedException {

			BufferedReader reader = new BufferedReader(new InputStreamReader(
			getInputStreams().get(0)));

			String line = null;
			int lineNum = 0;
			//int obNum = 1;
			fileModeASASSNenum fileModeASASSNlocal = fileModeASASSN;			
			
			do {
				try {
					if (wasInterrupted())
						break;
					lineNum++;
					line = reader.readLine();
					if (line != null) {
						line = line.replaceFirst("\n", "").trim();
						if (!isEmpty(line)) {
							// Define CSV format by a header row or skip it if format has been defined by a user
							boolean skipLine = false;
							if (fileModeASASSNlocal == fileModeASASSNenum.AUTO)
							{
								skipLine = true;
								if (line.startsWith(SKY_PATROL_HEADER)) // there could be additional Filter field at the end!
									fileModeASASSNlocal = fileModeASASSNenum.SKY_PATROL;
								else
								if (line.startsWith(PHOT_DB_HEADER))
									fileModeASASSNlocal = fileModeASASSNenum.PHOT_DB;
								else
									; // do nothing!
							}
							else
								skipLine = line.toUpperCase().startsWith(HEADER_START);
							if (!skipLine) {
								String[] fields = line.split(",");
								ValidObservation ob = readNextObservation(
										fields, lineNum, 
										userDefinedErrLimit, loadASASSN_V_as_Johnson_V, loadASASSN_g_as_Sloan_g,
										fileModeASASSNlocal);
								if (ob != null)
								{
									Magnitude mag = ob.getMagnitude();
									// Skip any observation whose magnitude and
									// error are 99.99
									if (mag.getMagValue() != MAX
										&& mag.getUncertainty() != MAX 
										&& isBandSelected(ob))
									{
										collectObservation(ob);
									}
								}
							}
						}
					}
				} catch (Exception e) {
					// Create an invalid observation.
					// Record the line number rather than observation number for
					// error reporting purposes, but still increment the latter.
					String error = e.getLocalizedMessage();
					InvalidObservation ob = new InvalidObservation(line, error);
					ob.setRecordNumber(lineNum);
					addInvalidObservation(ob);
				}
			} while (line != null);
		}
		
		private boolean isBandSelected(ValidObservation ob)
		{
			if (ob == null) return false;
			return  loadVmagnitudes   && (ob.getBand() == asassnVSeries || ob.getBand() == SeriesType.Johnson_V) || 
					load_g_magnitudes && (ob.getBand() == asassn_g_Series || ob.getBand() == SeriesType.Sloan_g) || 
					loadExcludedObs   && loadVmagnitudes   && (ob.getBand() == asassn5SigmaLimitSeries || ob.getBand() == asassnExcludedVseries) ||
					loadExcludedObs   && load_g_magnitudes && (ob.getBand() == asassn5SigmaLimit_g_Series || ob.getBand() == asassnExcluded_g_Series) ||
					loadUnknownFilterMagnitudes && (ob.getBand() == SeriesType.Unknown);

		}

		private ValidObservation readNextObservation(String[] fields, int lineNum, double userDefinedErrLimit, boolean loadASASSN_V_as_Johnson_V, boolean loadASASSN_g_as_Sloan_g, fileModeASASSNenum fileMode)
				throws ObservationValidationError {

			SeriesType series = null;
			String filter = "";
			
			// Get filter and make rought chech CSV format for validity
			if (fileMode == fileModeASASSNenum.SKY_PATROL && fields.length == 9)
				// Old SkyPatrol CSV format 
				filter = "V";
			else
			if (fileMode == fileModeASASSNenum.SKY_PATROL && fields.length > 9)
				// New SkyPatrol CSV format
				filter = fields[9].trim();
			else
			if (fileMode == fileModeASASSNenum.PHOT_DB)
				// ASAS-SN Photometry Database CSV format
				filter = "V";
			else
				return null;
			
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

			DateInfo hjd = julianDayValidator.validate(fields[0]);

			if (fileMode == fileModeASASSNenum.SKY_PATROL)
			{
				// HJD,UT Date,Camera,FWHM,Limit,mag,mag_err,flux(mJy),flux_err[,Filter]
				Magnitude mag = magnitudeFieldValidator.validate(fields[5]);
				double err = uncertaintyValueValidator.validate(fields[6]);
				if (err == MAX) {
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
				observation.addDetail("UT", fields[1], "UT Date");
				observation.addDetail("CAMERA", fields[2], "Camera");
				observation.addDetail("FWHM", Double.valueOf(fields[3]), "FWHM");
				observation.addDetail("LIMIT", Double.valueOf(fields[4]), "Limit");
				observation.addDetail("FLUX", Double.valueOf(fields[7]), "Flux (mJy)");
				observation.addDetail("FLUX_ERR", Double.valueOf(fields[8]), "Flux error");
				observation.addDetail("FILTER", filter, "Filter");
			}
			else // PHOT_DB
			{
				// hjd,camera,filter,mag,mag err,flux (mJy),flux err
				Magnitude mag = magnitudeFieldValidator.validate(fields[2]);
				double err = uncertaintyValueValidator.validate(fields[3]);
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
				observation.addDetail("CAMERA", fields[1], "Camera");
				observation.addDetail("FLUX", fields[4], "Flux (mJy)");
				observation.addDetail("FLUX_ERR", fields[5], "Flux error");
			}

			return observation;
		}

		private boolean isEmpty(String str) {
			return str != null && "".equals(str.trim());
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
		private JRadioButton fileModeAutoRadioButton;
		private JRadioButton fileModeSkyPatrolRadioButton;
		private JRadioButton fileModePhotDbRadioButton;

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
			topPane.add(createParameterPane4());

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
			
			errorLimitField = new DoubleField("Exclude observations having an error greater than: ", 0.0, 99.99, userDefinedErrLimit);
			panel.add(errorLimitField.getUIComponent());
			panel.add(Box.createRigidArea(new Dimension(75, 10)));

			return panel;
		}

		private JPanel createParameterPane2() {
			JPanel panel = new JPanel();
			panel.setBorder(BorderFactory.createTitledBorder(""));

			loadVmagnitudesCheckBox = new JCheckBox("Load V magnitudes?");
			loadVmagnitudesCheckBox.setSelected(loadVmagnitudes);
			panel.add(loadVmagnitudesCheckBox);

			load_g_magnitudesCheckBox = new JCheckBox("Load g magnitudes?");
			load_g_magnitudesCheckBox.setSelected(load_g_magnitudes);
			panel.add(load_g_magnitudesCheckBox);
			
			loadExcludedObsCheckBox = new JCheckBox("Load 5 sigma / excluded observations?");
			loadExcludedObsCheckBox.setSelected(loadExcludedObs);
			panel.add(loadExcludedObsCheckBox);

			loadUnknownFilterMagnitudesCheckBox = new JCheckBox("Load Unknown Filters? (*)");
			loadUnknownFilterMagnitudesCheckBox.setToolTipText("Observations in Unknown Filtes (other tnan V, g) are loaded without splitting into normal / 5-sigma / excluded");
			loadUnknownFilterMagnitudesCheckBox.setSelected(loadUnknownFilterMagnitudes);
			panel.add(loadUnknownFilterMagnitudesCheckBox);

			return panel;
		}
		
		private JPanel createParameterPane3() {
			JPanel panel = new JPanel();
			panel.setBorder(BorderFactory.createTitledBorder(""));

			loadASASSN_V_as_Johnson_V_CheckBox = new JCheckBox("Load ASAS-SN V magnitudes as Johnson V?");
			loadASASSN_V_as_Johnson_V_CheckBox.setSelected(loadASASSN_V_as_Johnson_V);
			panel.add(loadASASSN_V_as_Johnson_V_CheckBox);

			loadASASSN_g_as_Sloan_g_CheckBox = new JCheckBox("Load ASAS-SN g magnitudes as Sloan g?");
			loadASASSN_g_as_Sloan_g_CheckBox.setSelected(loadASASSN_g_as_Sloan_g);
			panel.add(loadASASSN_g_as_Sloan_g_CheckBox);

			return panel;
		}
		
		private JPanel createParameterPane4() {
			JPanel panel = new JPanel();
			panel.setBorder(BorderFactory.createTitledBorder("ASAS-SN CSV file type"));

			fileModeAutoRadioButton = new JRadioButton("Auto");
			fileModeSkyPatrolRadioButton = new JRadioButton("Sky Patrol");
			fileModePhotDbRadioButton = new JRadioButton("Photometry DB");
						
			switch (fileModeASASSN)
			{
				case SKY_PATROL:
					fileModeSkyPatrolRadioButton.setSelected(true);
					break;
				case PHOT_DB:
					fileModePhotDbRadioButton.setSelected(true);
					break;
				default:
					fileModeAutoRadioButton.setSelected(true);
			}
						
			ButtonGroup group = new ButtonGroup();
			group.add(fileModeAutoRadioButton);
			group.add(fileModeSkyPatrolRadioButton);
			group.add(fileModePhotDbRadioButton);
			
			panel.add(fileModeAutoRadioButton);
			panel.add(fileModeSkyPatrolRadioButton);
			panel.add(fileModePhotDbRadioButton);

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
		 * 
		 */
		public fileModeASASSNenum getFileModeASASSN() {
			if (fileModeSkyPatrolRadioButton.isSelected())
				return fileModeASASSNenum.SKY_PATROL;
			else
			if (fileModePhotDbRadioButton.isSelected())
				return fileModeASASSNenum.PHOT_DB;
			else
				return fileModeASASSNenum.AUTO;
			
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
