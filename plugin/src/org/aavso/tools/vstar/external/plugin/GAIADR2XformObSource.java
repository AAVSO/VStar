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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;

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
import org.aavso.tools.vstar.exception.CancellationException;
import org.aavso.tools.vstar.exception.ObservationReadError;
import org.aavso.tools.vstar.exception.ObservationValidationError;
import org.aavso.tools.vstar.input.AbstractObservationRetriever;
import org.aavso.tools.vstar.plugin.InputType;
import org.aavso.tools.vstar.plugin.ObservationSourcePluginBase;
import org.aavso.tools.vstar.plugin.PluginComponentFactory;
import org.aavso.tools.vstar.ui.dialog.AbstractOkCancelDialog;
import org.aavso.tools.vstar.ui.dialog.MessageBox;
import org.aavso.tools.vstar.ui.dialog.TextArea;
import org.aavso.tools.vstar.ui.dialog.TextField;
import org.aavso.tools.vstar.util.Pair;

/**
 * This plug-in class reads from the Gaia DR2/DR3 Photometry Web Service in CSV
 * format or the same saved to files, yielding an observation list.
 * 
 * See the following for information about the Gaia DR2/DR3 photometry service:
 * 
 * https://www.cosmos.esa.int/web/gaia-users/archive/programmatic-access
 *  
 * @author Cliff Kotnik
 * @version 1.0 - 2018-09-18
 * 
 * Revised by dbenn, max
 * 
 */
public class GAIADR2XformObSource extends ObservationSourcePluginBase {

	private Map<String, Integer> fieldIndices;
	
	private SeriesType gaiaGseries;
	private SeriesType gaiaBPseries;
	private SeriesType gaiaRPseries;
	//private SeriesType gaiaUnknownSeries;
	
	private boolean transform = false;
	private boolean ignoreFlags = false;
	private boolean useGaiaDR2 = false;
	private String baseURL = "https://gea.esac.esa.int/data-server/data?RETRIEVAL_TYPE=epoch_photometry&FORMAT=CSV&ID=";

	// Create static VeLa filter field here since cannot create it in
	// inner dialog class.
	private static Pair<TextArea, JPanel> velaFilterFieldPanelPair;

	static {
		velaFilterFieldPanelPair = PluginComponentFactory
				.createVeLaFilterPane();
	}
	
	GAIAParameterDialog paramDialog;

	/**
	 * Constructor
	 */
	public GAIADR2XformObSource() {
		super();
		gaiaGseries = SeriesType.create("Gaia G", "Gaia G", Color.GREEN, false, false);
		gaiaBPseries = SeriesType.create("Gaia BP", "Gaia BP", Color.BLUE, false, false);
		gaiaRPseries = SeriesType.create("Gaia RP", "Gaia RP", Color.RED, false, false);
		//gaiaUnknownSeries = SeriesType.create("Gaia unknown", "Gaia unknown", new Color(255, 255, 0), false, false);
		fieldIndices = new HashMap<String, Integer>();
		fieldIndices.put("source_id", -1);
		fieldIndices.put("band", -1);
		fieldIndices.put("time", -1);
		fieldIndices.put("mag", -1);
		fieldIndices.put("flux", -1);
		fieldIndices.put("flux_error", -1);
		fieldIndices.put("rejected_by_photometry", -1);
		fieldIndices.put("rejected_by_variability", -1);
		fieldIndices.put("other_flags", -1);
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

		if (paramDialog == null) {		
			paramDialog = new GAIAParameterDialog(isAdditive());
		}
		paramDialog.showDialog();

		if (!paramDialog.isCancelled()) {
			
			transform = paramDialog.isTransform();
			ignoreFlags = paramDialog.isIgnoreFlags();
			useGaiaDR2 = paramDialog.isGaiaDR2();			
			setAdditive(paramDialog.isLoadAdditive());

			try {
				String url = baseURL + paramDialog.getSourceID() + "&RELEASE=" + (useGaiaDR2 ? "Gaia+DR2" : "Gaia+DR3");
				urls.add(new URL(url));
			} catch (MalformedURLException e) {
				throw new ObservationReadError("Cannot construct Gaia"
						+ " URL (reason: " + e.getLocalizedMessage() + ")");
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
		return new GAIADR2FormatRetriever();
	}

	/**
	 * @see org.aavso.tools.vstar.plugin.IPlugin#getDescription()
	 */
	@Override
	public String getDescription() {
		return "Gaia DR2/DR3 Photometry Format reader";
	}

	/**
	 * @see org.aavso.tools.vstar.plugin.IPlugin#getDisplayName()
	 */
	@Override
	public String getDisplayName() {
		return "New Star from Gaia DR2/DR3 Photometry ...";
	}

	class GAIADR2FormatRetriever extends AbstractObservationRetriever {

		private String obscode = useGaiaDR2 ? "GAIA_DR2" : "GAIA_DR3";
		private String delimiter = ",";
		private List<String> lines = null;
		private Double gaiaEpoch = 2455197.5;
		// delta T in days within which two observations are considered
		// close enough to be used together for a transform
		private Double maxDeltaT = 1.0 / 24.0 / 12.0; // tolerance = 5 min
		private String gaiaSrcID;

		private JulianDayValidator julianDayValidator;
		private MagnitudeFieldValidator magnitudeFieldValidator;
		private UncertaintyValueValidator uncertaintyValueValidator;

		private class ValidObservationEx {
			public ValidObservation ob;
			public String line;
		}
		
		private class OvriGroup {
			public ValidObservation vObs;
			public ValidObservation rObs;
			public ValidObservation iObs;
			public OvriGroup(
					double jd, 
					String name, 
					String obsCode,
					boolean discrepant,
					String comments) {
				vObs = new ValidObservation();
				vObs.setBand(SeriesType.Johnson_V);	
				rObs = new ValidObservation();
				rObs.setBand(SeriesType.Cousins_R);				
				iObs = new ValidObservation();
				iObs.setBand(SeriesType.Cousins_I);				

				ValidObservation[] vri = {vObs, rObs, iObs};
				for (ValidObservation ob : vri) {
					ob.setJD(jd);
					ob.setName(name);
					ob.setObsCode(obsCode);
					ob.setTransformed(true); // transformed by default
					ob.setDiscrepant(discrepant);
					ob.setComments(comments);
				}
			}
		}
		
		private ArrayList<ValidObservation> blueList;
		private ArrayList<ValidObservationEx> greenList;
		private ArrayList<ValidObservation> redList;

		/**
		 * Constructor
		 */
		public GAIADR2FormatRetriever() {
			super(getVelaFilterStr());

			julianDayValidator = new JulianDayValidator();

			magnitudeFieldValidator = new MagnitudeFieldValidator();

			uncertaintyValueValidator = new UncertaintyValueValidator(
					new InclusiveRangePredicate(0, 1));

		}

		@Override
		public void retrieveObservations() throws ObservationReadError,
				InterruptedException {
			
			setJDflavour(JDflavour.BJD);
			
			// read lines and determine the number of them
			getNumberOfRecords();

			if (lines.size() == 0) {
				return;
			}
			
			if (transform) {
				// we will cache obs here as lines are processed for subsequent
				// transformation
				blueList = new ArrayList<ValidObservation>();
				greenList = new ArrayList<ValidObservationEx>();
				redList = new ArrayList<ValidObservation>();
			}

			boolean headerFound = false;

			int obsCount = 0;
			String firstError = null;
			for (int i = 0; i < lines.size(); i++) {
				String line = lines.get(i);
				if (line != null) {
					line = line.trim();
					if (!"".equals(line)) {
						if (headerFound) {
							try {
								ValidObservation vo = readNextObservation(line.split(delimiter), i + 1, transform);
								if (transform) {
									// We just separate obs into passband lists to
									// be collected later
									cacheObservation(vo, line);
								} else {
									// Done with obs, add to final valid list
									collectObservation(vo);
								}
								obsCount++;
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
				throw new ObservationReadError("Cannot find Gaia header");

			if (obsCount == 0 && firstError != null) {
				throw new ObservationReadError("No observations found. The first error message:\n" + firstError);
			}
			
			if (transform) {
				firstError = retrieveTransformedObservations();
				if (validObservations.size() == 0 && firstError != null) {
					throw new ObservationReadError("No observations found. The first error message:\n" + firstError);
				}
				
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

		private void cacheObservation(ValidObservation obs, String line) 
				throws ObservationReadError {
			
			if (obs.getBand() == gaiaBPseries) {
				blueList.add(obs); // was BP
			} else if (obs.getBand() == gaiaGseries) {
				ValidObservationEx obsExt = new ValidObservationEx();
				obsExt.ob = obs;
				obsExt.line = line;
				greenList.add(obsExt); // was G
			} else if (obs.getBand() == gaiaRPseries) {
				redList.add(obs); // was RP
			} else {
				throw new ObservationReadError("Unknown Gaia band");
			}
		}

		public String retrieveTransformedObservations()
				throws ObservationReadError {

			String firstError = null;
			for (int i = 0; i < greenList.size(); i++) {
				ValidObservationEx gObsEx = greenList.get(i);
				try {
					ValidObservation rObs = closestObs(gObsEx.ob, redList);
					ValidObservation bObs = closestObs(gObsEx.ob, blueList);
					if ((rObs != null) && (bObs != null)) {
						OvriGroup vri = transformVRI(bObs, gObsEx.ob, rObs, i + 1, greenList.size());
						collectObservation(vri.vObs);
						collectObservation(vri.rObs);
						collectObservation(vri.iObs);
					} else {
						throw new ObservationReadError("No matching blue/red observation to transform");
					}
				} catch (Exception e) { 
					// Create an invalid observation.
					String error = e.getLocalizedMessage();
					if (firstError == null) firstError = error;
					InvalidObservation ob = new InvalidObservation(
							gObsEx.line,
							"No matching blue/red observation to transform");
					ob.setRecordNumber(gObsEx.ob.getRecordNumber());
					addInvalidObservation(ob);
				}
			}
			return firstError;
		}

		public ValidObservation closestObs(ValidObservation obs,
				ArrayList<ValidObservation> matchList) {
			double deltaT = 9999999.0;
			double targetDay = obs.getDateInfo().getJulianDay();
			ValidObservation closest = null;

			for (ValidObservation matchObs : matchList) {
				double matchDay = matchObs.getDateInfo().getJulianDay();
				if (Math.abs(targetDay - matchDay) < deltaT) {
					deltaT = Math.abs(targetDay - matchDay);
					closest = matchObs;
				}
			}
			if (deltaT < maxDeltaT) {
				return closest;
			} else {
				return null;
			}
		}

		public OvriGroup transformVRI(ValidObservation bObs, ValidObservation gObs,
				ValidObservation rObs, int obsNum, int nGroups) {
			// Create brand-new observations instead of mutating existing ones.
			// All three new bands originate from Gaia G with different transformations. 
			
			double g = gObs.getMagnitude().getMagValue();
			double bp = bObs.getMagnitude().getMagValue();
			double rp = rObs.getMagnitude().getMagValue();
			
			double bp_rp = bp - rp;
			
			double gerr = gObs.getMagnitude().getUncertainty();
			double bperr = bObs.getMagnitude().getUncertainty();
			double rperr = rObs.getMagnitude().getUncertainty();

			/*
			 * Use the tranformation equations found in appendix A of the Gaia
			 * Data Release 2: Photometric content and validation
			 * https://arxiv.org/abs/1804.09368 to transform the passbands from
			 * Gaia to VRI
			 */
			double v = magCalc(g, bp_rp, 0.01760, 0.006860, 0.1732);
			double r = magCalc(g, bp_rp, 0.003226, -0.3833, 0.1345);
			double i = magCalc(g, bp_rp, -0.02085, -0.7419, 0.09631);

			double bp_rp_err = Math.sqrt(bperr * bperr + rperr * rperr);

			double verr = uncCalc(g, gerr, bp_rp, bp_rp_err, 0.01760, 0.006860,
					0.1732);
			double rerr = uncCalc(g, gerr, bp_rp, bp_rp_err, 0.003226, -0.3833,
					0.1345);
			double ierr = uncCalc(g, gerr, bp_rp, bp_rp_err, -0.02085, -0.7419,
					0.09631);

			String comment = String.format(Locale.ENGLISH, 
					"Transformed from Gaia G= %.5f, BP= %.5f, RP= %.5f", 
					g, bp, rp);					
		
			// V, R, I
			// Consider all three observations as discrepant if any of the G,
			// BP, RP observations
			// was flagged as rejected
			OvriGroup vri = new OvriGroup(gObs.getJD(), gObs.getName(), obscode,
					bObs.isDiscrepant() || gObs.isDiscrepant() || rObs.isDiscrepant(),
					comment);

			vri.vObs.setMagnitude(new Magnitude(v, verr));
			vri.vObs.setRecordNumber(obsNum);
			
			vri.rObs.setMagnitude(new Magnitude(r, rerr));
			vri.rObs.setRecordNumber(nGroups + obsNum);

			vri.iObs.setMagnitude(new Magnitude(i, ierr));
			vri.iObs.setRecordNumber(2 * nGroups + obsNum);
			
			return vri;
		}

		public double magCalc(double g, double bp_rp, double c0, double c1,
				double c2) {
			return g + c0 + c1 * bp_rp + c2 * bp_rp * bp_rp;
		}

		// Error propagation per John Taylor's
		// "An Introduction to Error Analysis"
		// 2nd edition, chapter 3
		public double uncCalc(double g, double gerr, double bp_rp,
				double bp_rp_err, double c0, double c1, double c2) {
			double der = c1 + 2 * c2 * bp_rp; // derivative of c0 + c1 * bp_rp +
												// c2 * bp_rp * bp_rp
			double t1 = der * bp_rp_err; // eq. 3.23 error of function of one
											// var
			return Math.sqrt(gerr * gerr + t1 * t1); // eq. 3.16 error of
														// independent vars
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

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					getInputStreams().get(0)));

			String line = null;

			while ((line = reader.readLine()) != null) {
				lines.add(line);
			}
		}

		// Gaia DR2/DR3 format observation reader.
		// The Gaia lightcurve data in the file/web response will have the
		// following columns:
		//
		// source_id transit_id band time mag flux flux_error flux_over_error
		// rejected_by_photometry rejected_by_variability other_flags
		// solution_id
		//
		// See https://www.cosmos.esa.int/web/gaia-users/archive/programmatic-access#Sect_1_ss1.1
		//
		private ValidObservation readNextObservation(String[] fields, int recordNumber, boolean transformNeeded)
				throws ObservationValidationError {

			ValidObservation observation = new ValidObservation();

			String source_id = fields[fieldIndices.get("source_id")].trim();
			String name = "Gaia DR" + (useGaiaDR2 ? "2" : "3") + " " + source_id;
			gaiaSrcID = name;

			observation.setRecordNumber(recordNumber);
			observation.setName(name);
			observation.setObsCode(obscode);

			DateInfo dateInfo = new DateInfo(julianDayValidator.validate(
					fields[fieldIndices.get("time")].trim()).getJulianDay()
					+ gaiaEpoch);
			observation.setDateInfo(dateInfo);

			Magnitude magnitude = magnitudeFieldValidator.validate(fields[fieldIndices.get("mag")].trim());
			observation.setMagnitude(magnitude);

			double flux = Double.parseDouble(fields[fieldIndices.get("flux")].trim());
			double ferr = Double.parseDouble(fields[fieldIndices.get("flux_error")].trim());
			double uncertainty = -2.5 * Math.log10(flux / (flux + ferr));
			String uncertaintyStr = String.valueOf(uncertainty);
			uncertainty = uncertaintyValueValidator.validate(uncertaintyStr);
			observation.getMagnitude().setUncertainty(uncertainty);

			String filter = fields[fieldIndices.get("band")].trim();
			SeriesType band;
			if (filter.equals("BP")) {
				band = gaiaBPseries;
			} else if (filter.equals("RP")) {
				band = gaiaRPseries;
			} else if (filter.equals("G")) {
				band = gaiaGseries;
			} else {
				throw new ObservationValidationError("Unexpected Gaia band:"
						+ filter);
			}
			observation.setBand(band);

			observation.setTransformed(false);

			// ValidObservation defaults to STD.
			//observation.setMType(MTypeType.STD);

			//observation.setComments("");

			// use both flags
			String rejectedByPhotometry = fields[fieldIndices.get("rejected_by_photometry")].trim();
			String rejectedByVariability = fields[fieldIndices.get("rejected_by_variability")].trim();
			if (!ignoreFlags) {
				if (rejectedByPhotometry.equalsIgnoreCase("TRUE") || rejectedByVariability.equalsIgnoreCase("TRUE")) {
					observation.setDiscrepant(true);
				}
			}
			
			if (!transformNeeded) {
				observation.addDetail("REJECTED_BY_PHOTOMETRY", rejectedByPhotometry, "rejected_by_photometry");
				observation.addDetail("REJECTED_BY_VARIABILITY", rejectedByVariability, "rejected_by_variability");
				observation.addDetail("OTHER_FLAGS", fields[fieldIndices.get("other_flags")], "other_flags");
			}

			return observation;
		}

		@Override
		public String getSourceName() {
			return gaiaSrcID;
			// return getInputName();
		}

		@Override
		public String getSourceType() {
			return "Gaia DR2/DR3 Format";
		}
	}


	@SuppressWarnings("serial")
	class GAIAParameterDialog extends AbstractOkCancelDialog {

		private TextField sourceIDField;
		private JCheckBox transformCheckbox;
		private JCheckBox rejectCheckbox;
		private JCheckBox additiveLoadCheckbox;
		private JRadioButton gaiaDR2;
		private JRadioButton gaiaDR3;
		
		private String sourceID;

		/**
		 * Constructor
		 */
		public GAIAParameterDialog(boolean additiveChecked) {
			super("Gaia Load Parameters");

			Container contentPane = this.getContentPane();

			JPanel topPane = new JPanel();
			topPane.setLayout(new BoxLayout(topPane, BoxLayout.PAGE_AXIS));
			topPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

			topPane.add(createReleaseRadioButtonsPane(false));
			
			topPane.add(createParameterPane());
			
			topPane.add(createIgnoreRejectFlagsCheckboxPane(false));

			topPane.add(createTransformCheckboxPane(false));

			topPane.add(velaFilterFieldPanelPair.second);

			topPane.add(createAdditiveLoadCheckboxPane(additiveChecked));

			// OK, Cancel
			topPane.add(createButtonPane());

			contentPane.add(topPane);

			this.pack();
		}

		private JPanel createParameterPane() {
			JPanel panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

			sourceIDField = new TextField("Gaia source_id");
			panel.add(sourceIDField.getUIComponent());
			//panel.add(Box.createRigidArea(new Dimension(75, 10)));

			return panel;
		}

		private JPanel createAdditiveLoadCheckboxPane(boolean checked) {
			JPanel panel = new JPanel();
			panel.setBorder(BorderFactory.createTitledBorder("Additive Load"));

			additiveLoadCheckbox = new JCheckBox("Add to current?", checked);
			panel.add(additiveLoadCheckbox);

			return panel;
		}

		private JPanel createIgnoreRejectFlagsCheckboxPane(boolean checked) {
			JPanel panel = new JPanel();
			panel.setBorder(BorderFactory
					.createTitledBorder("Flags"));

			rejectCheckbox = new JCheckBox(
					"Ignore rejected_by_photometry, rejected_by_variability?", checked);
			panel.add(rejectCheckbox);

			return panel;
		}
		
		private JPanel createReleaseRadioButtonsPane(boolean dr2) {
			JPanel panel = new JPanel();
			
			panel.setBorder(BorderFactory.createTitledBorder("Gaia data release"));

			ButtonGroup bg = new ButtonGroup();   
			
			gaiaDR2 = new JRadioButton("Gaia DR2", dr2);
			gaiaDR3 = new JRadioButton("Gaia DR3", !dr2);			
			bg.add(gaiaDR2);
			bg.add(gaiaDR3);
			
			panel.add(gaiaDR2);
			panel.add(gaiaDR3);
			
			return panel;
		}
		
		private JPanel createTransformCheckboxPane(boolean checked) {
			JPanel panel = new JPanel();
			panel.setBorder(BorderFactory
					.createTitledBorder("Passband Transformation"));

			transformCheckbox = new JCheckBox(
					"Transform Gaia passbands to V,R,I?", checked);
			panel.add(transformCheckbox);

			return panel;
		}

		public String getSourceID() {
			return sourceID;
		}

		/**
		 * Should we ignore rejected_by_photometry and rejected_by_variability flags?
		 * 
		 * @return Whether or not to ignore rejected_by_photometry and rejected_by_variability
		 */
		public boolean isIgnoreFlags() {
			return rejectCheckbox.isSelected();
		}
		
		/**
		 * Use Gaia DR2 instead of Gaia DR3?
		 * 
		 * @return If true, use Gaia DR2 instead of Gaia DR3
		 */
		public boolean isGaiaDR2() {
			return gaiaDR2.isSelected();
		}
		
		/**
		 * Return whether or not the passbands should be transformed.
		 * 
		 * @return Whether or not the passbands are transformed.
		 */
		public boolean isTransform() {
			return transformCheckbox.isSelected();
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
			sourceID = null;

			SwingUtilities.invokeLater( new Runnable() { 
				public void run() {
					sourceIDField.getUIComponent().requestFocusInWindow();
				}
			} );
			
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
			sourceID = sourceIDField.getValue();
			if (sourceID != null) sourceID = sourceID.trim();
			if (sourceID == null || "".equals(sourceID) || !sourceID.matches("[0-9]+")) {
				sourceIDField.getUIComponent().requestFocusInWindow();
				MessageBox.showErrorDialog("Gaia", "Gaia source ID must be numeric");
				return;
			}

			cancelled = false;
			setVisible(false);
			dispose();
		}
	}

}
