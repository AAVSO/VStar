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

import java.awt.Container;
import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.aavso.tools.vstar.data.CommentType;
import org.aavso.tools.vstar.data.DateInfo;
import org.aavso.tools.vstar.data.InvalidObservation;
import org.aavso.tools.vstar.data.MTypeType;
import org.aavso.tools.vstar.data.Magnitude;
import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.data.validation.CommentCodeValidator;
import org.aavso.tools.vstar.data.validation.InclusiveRangePredicate;
import org.aavso.tools.vstar.data.validation.JulianDayValidator;
import org.aavso.tools.vstar.data.validation.MagnitudeFieldValidator;
import org.aavso.tools.vstar.data.validation.MagnitudeValueValidator;
import org.aavso.tools.vstar.data.validation.TransformedValidator;
import org.aavso.tools.vstar.data.validation.UncertaintyValueValidator;
import org.aavso.tools.vstar.exception.CancellationException;
import org.aavso.tools.vstar.exception.ObservationReadError;
import org.aavso.tools.vstar.exception.ObservationValidationError;
import org.aavso.tools.vstar.input.AbstractObservationRetriever;
import org.aavso.tools.vstar.plugin.InputType;
import org.aavso.tools.vstar.plugin.ObservationSourcePluginBase;
import org.aavso.tools.vstar.plugin.PluginComponentFactory;
import org.aavso.tools.vstar.ui.dialog.AbstractOkCancelDialog;
import org.aavso.tools.vstar.ui.dialog.TextArea;
import org.aavso.tools.vstar.ui.dialog.TextField;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.util.Pair;

/**
 * This plug-in class reads from the Gaia DR2 Photometry Web Service in CSV
 * format or the same saved to files, yielding an observation list.
 * 
 * See the following for information about the Gaia DR2 photometry service:
 * 
 * https://gea.esac.esa.int/archive-help/tutorials/datalink_lc/index.html
 * https:/
 * /gea.esac.esa.int/archive/documentation/GDR2/Gaia_archive/chap_datamodel
 * /sec_dm_datalink_tables/ssec_dm_light_curve.html
 * 
 * Example URL: https://gea.esac.esa.int/data-server/data?RETRIEVAL_TYPE=
 * epoch_photometry&FORMAT=CSV&ID=4116592768715278848 or 5085674696501016192 or
 * 6857141688677395840 or 3530587602644108800
 * 
 * @author Cliff Kotnik
 * @version 1.0 - 2018-09-18
 */
public class GAIADR2XformObSource extends ObservationSourcePluginBase {

	private boolean transform = false;
	private String source_id;
	private String baseURL = "https://gea.esac.esa.int/data-server/data?RETRIEVAL_TYPE=epoch_photometry&FORMAT=CSV&ID=";

	// Create static VeLa filter field here since cannot create it in
	// inner dialog class.
	private static Pair<TextArea, JPanel> velaFilterFieldPanelPair;

	static {
		velaFilterFieldPanelPair = PluginComponentFactory
				.createVeLaFilterPane();
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

		GAIAParameterDialog paramDialog = new GAIAParameterDialog(isAdditive,
				transform);

		if (!paramDialog.isCancelled()) {
			source_id = paramDialog.getSourceID();
			transform = paramDialog.isTransform();
			setAdditive(paramDialog.isLoadAdditive());

			try {
				urls.add(new URL(baseURL + source_id));
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
		return "Gaia DR2 Photometry Format reader";
	}

	/**
	 * @see org.aavso.tools.vstar.plugin.IPlugin#getDisplayName()
	 */
	@Override
	public String getDisplayName() {
		return "New Star from Gaia DR2 Photometry ...";
	}

	class GAIADR2FormatRetriever extends AbstractObservationRetriever {
		// private String fileType;
		private String obscode = "GAIA";
		private String delimiter = ",";
		private String dateType = "BJD";
		private List<String> lines;
		private Double gaiaEpoch = 2455197.5;
		// delta T in days within which two observations are considered
		// close enough to be used together for a transform
		private Double maxDeltaT = 1.0 / 24.0 / 12.0;
		private String gaiaSrcID;

		private JulianDayValidator julianDayValidator;
		private MagnitudeFieldValidator magnitudeFieldValidator;
		private UncertaintyValueValidator uncertaintyValueValidator;
		private TransformedValidator transformedValidator;
		private MagnitudeValueValidator magnitudeValueValidator;
		private CommentCodeValidator commentCodeValidator;

		private ArrayList<ValidObservation> blueList;
		private ArrayList<ValidObservation> greenList;
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

			transformedValidator = new TransformedValidator();

			// What should the range be for CCD/PEP, Visual/PTG?
			magnitudeValueValidator = new MagnitudeValueValidator(
					new InclusiveRangePredicate(-10, 25));

			this.commentCodeValidator = new CommentCodeValidator(
					CommentType.getRegex());
		}

		@Override
		public void retrieveObservations() throws ObservationReadError,
				InterruptedException {

			setBarycentric(true);

			getNumberOfRecords();

			if (transform) {
				// we will cache obs here as lines are processed for subsequent
				// transformation
				blueList = new ArrayList<ValidObservation>();
				greenList = new ArrayList<ValidObservation>();
				redList = new ArrayList<ValidObservation>();
			}

			int lineNum = 1;
			int obNum = 1;

			for (String line : lines) {
				try {
					lineNum++;
					if ((line != null) && (!(line.contains("source_id")))) { // skip
																				// header
																				// line
						line = line.replaceFirst("\n", "").replaceFirst("\r",
								"");
						if (!isEmpty(line)) {
							String[] fields = line.split(delimiter);
							ValidObservation vo = readNextObservation(fields,
									obNum);
							if (transform) {
								// We just separate obs into passband lists to
								// be collected later
								cacheObservation(vo);
							} else {
								// Done with obs, add to final valid list
								collectObservation(vo);
							}
							obNum++;
						}

						incrementProgress();
					}
				} catch (Exception e) {
					// Create an invalid observation.
					// Record the line number rather than observation number for
					// error reporting purposes, but still increment the latter.
					String error = e.getLocalizedMessage();
					InvalidObservation ob = new InvalidObservation(line, error);
					ob.setRecordNumber(lineNum);
					obNum++;
					addInvalidObservation(ob);
				}
			}
			if (transform) {
				retrieveTransformedObservations();
			}
		}

		private void cacheObservation(ValidObservation obs) {
			if (obs.getBand() == SeriesType.Blue) {
				blueList.add(obs); // was BP
			} else if (obs.getBand() == SeriesType.Green) {
				greenList.add(obs); // was G
			} else {
				redList.add(obs); // was RP
			}
		}

		public void retrieveTransformedObservations()
				throws ObservationReadError {

			for (int i = 0; i < greenList.size(); i++) {
				ValidObservation gObs = greenList.get(i);
				ValidObservation rObs = closestObs(gObs, redList);
				ValidObservation bObs = closestObs(gObs, blueList);
				if ((rObs != null) && (bObs != null)) {
					transformVRI(bObs, gObs, rObs);
					collectObservation(bObs);
					collectObservation(gObs);
					collectObservation(rObs);
				} else {
					InvalidObservation io = new InvalidObservation(
							gObs.toSimpleFormatString(","),
							"No matching blue/red observation to transform");
					addInvalidObservation(io);
				}
			}
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

		public void transformVRI(ValidObservation bObs, ValidObservation gObs,
				ValidObservation rObs) {
			double avgDay = ((bObs.getJD() + gObs.getJD() + rObs.getJD()) / 3.0);
			Magnitude bpMagnitude = bObs.getMagnitude();
			double bp = bpMagnitude.getMagValue();
			double bperr = bpMagnitude.getUncertainty();
			Magnitude gMagnitude = gObs.getMagnitude();
			double g = gMagnitude.getMagValue();
			double gerr = gMagnitude.getUncertainty();
			Magnitude rpMagnitude = rObs.getMagnitude();
			double rp = rpMagnitude.getMagValue();
			double rperr = rpMagnitude.getUncertainty();

			double bp_rp = bp - rp;
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

			// Now blue, green, red for V, R, I
			bObs.setBand(SeriesType.Johnson_V);
			gObs.setBand(SeriesType.Cousins_R);
			rObs.setBand(SeriesType.Cousins_I);
			bObs.setJD(avgDay);
			gObs.setJD(avgDay);
			rObs.setJD(avgDay);

			bpMagnitude.setMagValue(v);
			bpMagnitude.setUncertainty(verr);
			// bObs.setMagnitude(bpMagnitude);
			gMagnitude.setMagValue(r);
			gMagnitude.setUncertainty(rerr);
			// gObs.setMagnitude(gMagnitude);
			rpMagnitude.setMagValue(i);
			rpMagnitude.setUncertainty(ierr);
			// rObs.setMagnitude(rpMagnitude);
			bObs.setTransformed(true);
			gObs.setTransformed(true);
			rObs.setTransformed(true);

			// Consider all three observations as discrepant if any of the G,
			// BP, RP observations
			// was flagged as rejected by GAI variability analysis
			if (bObs.isDiscrepant() || gObs.isDiscrepant()
					|| rObs.isDiscrepant()) {
				bObs.setDiscrepant(true);
				gObs.setDiscrepant(true);
				rObs.setDiscrepant(true);
			}

			String comment = "Transformed from Gaia DR2  BP="
					+ String.valueOf(bp) + "  G=" + String.valueOf(g) + "  RP="
					+ String.valueOf(rp);
			bObs.setComments(comment);
			gObs.setComments(comment);
			rObs.setComments(comment);

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

		// Gaia DR2 format observation reader.
		// The Gaia lightcurve data in the file/web response will have the
		// following columns:
		//
		// source_id transit_id band time mag flux flux_error flux_over_error
		// rejected_by_photometry rejected_by_variability other_flags
		// solution_id
		//
		// The description of these are contained in section 14.7.1 fo the Gaia
		// DR2 documentation
		// found at
		// https://gea.esac.esa.int/archive/documentation/GDR2/Gaia_archive/chap_datamodel/sec_dm_datalink_tables/ssec_dm_light_curve.html
		//
		private ValidObservation readNextObservation(String[] fields, int obNum)
				throws ObservationValidationError {

			ValidObservation observation = new ValidObservation();

			String name = "Gaia_" + fields[0].trim();
			gaiaSrcID = name;

			observation.setRecordNumber(obNum);
			observation.setName(name);
			observation.setObsCode(obscode);

			DateInfo dateInfo = new DateInfo(julianDayValidator.validate(
					fields[3].trim()).getJulianDay()
					+ gaiaEpoch);
			observation.setDateInfo(dateInfo);

			Magnitude magnitude = magnitudeFieldValidator.validate(fields[4]
					.trim());
			observation.setMagnitude(magnitude);

			double flux = Double.parseDouble(fields[5].trim());
			double ferr = Double.parseDouble(fields[6].trim());
			double uncertainty = -2.5 * Math.log10(flux / (flux + ferr));
			String uncertaintyStr = String.valueOf(uncertainty);
			uncertainty = uncertaintyValueValidator.validate(uncertaintyStr);
			observation.getMagnitude().setUncertainty(uncertainty);

			String filter = fields[2].trim();
			SeriesType band;
			if (filter.equals("BP")) {
				band = SeriesType.Blue;
			} else if (filter.equals("RP")) {
				band = SeriesType.Red;
			} else if (filter.equals("G")) {
				band = SeriesType.Green;
			} else {
				throw new ObservationValidationError("Unexpected Gaia band:"
						+ filter);
			}
			observation.setBand(band);

			observation.setTransformed(false);

			// ValidObservation defaults to STD.
			observation.setMType(MTypeType.STD);

			observation.setComments("Gaia DR2  G->Green BP->Blue RP->Red");

			String rejectedByVariability = fields[9].trim();
			if (rejectedByVariability.equalsIgnoreCase("TRUE")) {
				observation.setDiscrepant(true);
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
			return "Gaia DR2 Format";
		}
	}

	private boolean isEmpty(String str) {
		return str != null && "".equals(str.trim());
	}

	private boolean isNA(String str) {
		return str == null || "NA".equalsIgnoreCase(str);
	}

	@SuppressWarnings("serial")
	class GAIAParameterDialog extends AbstractOkCancelDialog {

		private TextField sourceIDField;
		private JCheckBox transformCheckbox;
		private JCheckBox additiveLoadCheckbox;

		/**
		 * Constructor
		 */
		public GAIAParameterDialog(boolean additiveChecked,
				boolean transformChecked) {
			super("Gaia Load Parameters");

			Container contentPane = this.getContentPane();

			JPanel topPane = new JPanel();
			topPane.setLayout(new BoxLayout(topPane, BoxLayout.PAGE_AXIS));
			topPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

			topPane.add(createParameterPane());

			topPane.add(createTransformCheckboxPane(transformChecked));

			topPane.add(velaFilterFieldPanelPair.second);

			topPane.add(createAdditiveLoadCheckboxPane(additiveChecked));

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

			sourceIDField = new TextField("Gaia source_id");
			panel.add(sourceIDField.getUIComponent());
			panel.add(Box.createRigidArea(new Dimension(75, 10)));

			return panel;
		}

		private JPanel createAdditiveLoadCheckboxPane(boolean checked) {
			JPanel panel = new JPanel();
			panel.setBorder(BorderFactory.createTitledBorder("Additive Load"));

			additiveLoadCheckbox = new JCheckBox("Add to current?", checked);
			panel.add(additiveLoadCheckbox);

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
			return sourceIDField.getValue();
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

			if (ok) {
				cancelled = false;
				setVisible(false);
				dispose();
			}
		}
	}

}
