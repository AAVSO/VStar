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

/**
 * This plug-in class reads from the Gaia DR2/DR3 Photometry Web Service in CSV
 * format or the same saved to files, yielding an observation list.
 * 
 * See the following for information about the Gaia DR2/DR3 photometry service:
 * 
 * https://www.cosmos.esa.int/web/gaia-users/archive/programmatic-access
 *  
 * @author Cliff Kotnik
 * @author dbenn 
 * @author mpyat2
 * @version 2.0 - 2023-03-24
*/

package org.aavso.tools.vstar.external.lib;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
import org.aavso.tools.vstar.plugin.ObservationSourcePluginBase;

public abstract class GaiaObSourceBase extends ObservationSourcePluginBase {

	protected static enum GaiaRelease {UNKNOWN, DR2, DR3};
	
	protected SeriesType gaiaGseries;
	protected SeriesType gaiaBPseries;
	protected SeriesType gaiaRPseries;

	protected boolean paramTransform = false;
	protected boolean paramIgnoreFlags = false;
	protected GaiaRelease paramGaiaRelease = GaiaRelease.DR3;	
	
	public static final String GAIA_OB_SOURCE_VERSION = "JUN 2025";
	
	private static final double INVALID_MAG = 99.99;
	
	/**
	 * Constructor
	 */
	public GaiaObSourceBase() {
		super();
		gaiaGseries = SeriesType.create("Gaia G", "Gaia G", Color.GREEN, false, false);
		gaiaBPseries = SeriesType.create("Gaia BP", "Gaia BP", Color.BLUE, false, false);
		gaiaRPseries = SeriesType.create("Gaia RP", "Gaia RP", Color.RED, false, false);
	}
	
	/**
	 * @see org.aavso.tools.vstar.plugin.ObservationSourcePluginBase#
	 *      getObservationRetriever ()
	 */
	@Override
	public AbstractObservationRetriever getObservationRetriever() {
		return new GAIADR2FormatRetriever(paramTransform, paramIgnoreFlags, paramGaiaRelease);
	}
	
	class GAIADR2FormatRetriever extends AbstractObservationRetriever {

		protected Map<String, Integer> fieldIndices;
		
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
		
		private boolean transform = false;
		private boolean ignoreFlags = false;
		private GaiaRelease gaiaRelease = GaiaRelease.DR3;	

		private class ValidObservationEx {
			public ValidObservation ob;
			public String line;
		}
		
		private class OvribGroup {
			public ValidObservation vObs;
			public ValidObservation rObs;
			public ValidObservation iObs;
			public ValidObservation bObs;
			public OvribGroup(
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
				bObs = new ValidObservation();
				bObs.setBand(SeriesType.Johnson_B);

				ValidObservation[] vri = {vObs, rObs, iObs, bObs};
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
		public GAIADR2FormatRetriever(boolean transform, boolean ignoreFlags, GaiaRelease gaiaRelease) {
			super(getVelaFilterStr());
			
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

			this.transform = transform;
			this.ignoreFlags = ignoreFlags;
			this.gaiaRelease = gaiaRelease;	
			
			julianDayValidator = new JulianDayValidator();
			magnitudeFieldValidator = new MagnitudeFieldValidator();
			uncertaintyValueValidator = new UncertaintyValueValidator(new InclusiveRangePredicate(0, 1));

		}

		private List<String>convertDR3toDR2format(List<String> lines) throws ObservationReadError {
			String headerLineDR3 = null;
			int startLineN = 0;
			for (String line : lines) {
				String[] fields = line.split(delimiter);
				if ((indexInArray("g_transit_time", fields) >= 0) &&
					(indexInArray("bp_obs_time", fields)    >= 0) &&
					(indexInArray("rp_obs_time", fields))   >= 0) {
					headerLineDR3 = line;
					break;
				}
				startLineN++;
			}
			if (headerLineDR3 == null) {
				// assume DR2: formats before DEC 2024 were identical.
				// gaiaRelease remains "UNKNOWN"
				return lines;
			} else {
				// If it is a file source, set the release version to DR3.
				// It is required for the correct transformation
				gaiaRelease = GaiaRelease.DR3;
			}
			startLineN++;
			
			String[] fields = headerLineDR3.split(delimiter);
			int source_id_idx = indexInArray("source_id", fields);
			int transit_id_idx = indexInArray("transit_id", fields);
			int time_g_idx = indexInArray("g_transit_time", fields);
			int time_bp_idx = indexInArray("bp_obs_time", fields);
			int time_rp_idx = indexInArray("rp_obs_time", fields);
			int mag_g_idx = indexInArray("g_transit_mag", fields);			
			int mag_bp_idx = indexInArray("bp_mag", fields);
			int mag_rp_idx = indexInArray("rp_mag", fields);
			int flux_g_idx = indexInArray("g_transit_flux", fields);
			int flux_bp_idx = indexInArray("bp_flux", fields);
			int flux_rp_idx = indexInArray("rp_flux", fields);
			int flux_g_error_idx = indexInArray("g_transit_flux_error", fields);
			int flux_bp_error_idx = indexInArray("bp_flux_error", fields);
			int flux_rp_error_idx = indexInArray("rp_flux_error", fields);
			int rejected_by_variability_g_idx = indexInArray("variability_flag_g_reject", fields);
			int rejected_by_variability_bp_idx = indexInArray("variability_flag_bp_reject", fields);
			int rejected_by_variability_rp_idx = indexInArray("variability_flag_rp_reject", fields);
			int other_flags_g_idx = indexInArray("g_other_flags", fields);
			int other_flags_bp_idx = indexInArray("bp_other_flags", fields);
			int other_flags_rp_idx = indexInArray("rp_other_flags", fields);
			int rejected_by_photometry_idx = indexInArray("rejected_by_photometry", fields);
			int solution_id_idx = indexInArray("solution_id", fields);
			
			if (source_id_idx < 0 ||
				transit_id_idx < 0 ||
				time_g_idx < 0 ||
				time_bp_idx < 0 ||
				time_rp_idx < 0 ||
				mag_g_idx < 0 ||
				mag_bp_idx < 0 ||
				mag_rp_idx < 0 ||
				flux_g_idx < 0 ||
				flux_bp_idx < 0 ||
				flux_rp_idx < 0 ||
				flux_g_error_idx < 0 ||
				flux_bp_error_idx < 0 ||
				flux_rp_error_idx < 0 ||
				rejected_by_variability_g_idx < 0 ||
				rejected_by_variability_bp_idx < 0 ||
				rejected_by_variability_rp_idx < 0 ||
				other_flags_g_idx < 0 ||
				other_flags_bp_idx < 0 ||
				other_flags_rp_idx < 0 ||
				rejected_by_photometry_idx < 0 ||
				solution_id_idx < 0) 
			{
				throw new ObservationReadError("GAIA DR3 Header differs from the expected one");
			}

			List<String> outList = new ArrayList<String>();
			
			// old header (DR2 and pre-Dec 2024 DR3)
			outList.add("source_id,transit_id,band,time,mag,flux,flux_error,flux_over_error,rejected_by_photometry,rejected_by_variability,other_flags,solution_id");

			// G
			for (int i = startLineN; i < lines.size(); i++) {
				String[] values = lines.get(i).split(delimiter);
				String line = values[source_id_idx];
				line += delimiter + values[transit_id_idx];
				line += delimiter + "G"; // band
				line += delimiter + values[time_g_idx];
				line += delimiter + values[mag_g_idx];
				line += delimiter + values[flux_g_idx];
				line += delimiter + values[flux_g_error_idx];
				line += delimiter + ""; // we do not use flux_over_error
				line += delimiter + values[rejected_by_photometry_idx];
				line += delimiter + values[rejected_by_variability_g_idx];
				line += delimiter + values[other_flags_g_idx];
				line += delimiter + values[solution_id_idx];
				outList.add(line);
			}
			// BP
			for (int i = startLineN; i < lines.size(); i++) {
				String[] values = lines.get(i).split(delimiter);
				String line = values[source_id_idx];
				line += delimiter + values[transit_id_idx];
				line += delimiter + "BP"; // band
				line += delimiter + values[time_bp_idx];
				line += delimiter + values[mag_bp_idx];
				line += delimiter + values[flux_bp_idx];
				line += delimiter + values[flux_bp_error_idx];
				line += delimiter + ""; // we do not use flux_over_error
				line += delimiter + values[rejected_by_photometry_idx];
				line += delimiter + values[rejected_by_variability_bp_idx];
				line += delimiter + values[other_flags_bp_idx];
				line += delimiter + values[solution_id_idx];
				outList.add(line);
			}
			// RP
			for (int i = startLineN; i < lines.size(); i++) {
				String[] values = lines.get(i).split(delimiter);
				String line = values[source_id_idx];
				line += delimiter + values[transit_id_idx];
				line += delimiter + "RP"; // band
				line += delimiter + values[time_rp_idx];
				line += delimiter + values[mag_rp_idx];
				line += delimiter + values[flux_rp_idx];
				line += delimiter + values[flux_rp_error_idx];
				line += delimiter + ""; // we do not use flux_over_error
				line += delimiter + values[rejected_by_photometry_idx];
				line += delimiter + values[rejected_by_variability_rp_idx];
				line += delimiter + values[other_flags_rp_idx];
				line += delimiter + values[solution_id_idx];
				outList.add(line);
			}
			
			return outList;
		}
		
		@Override
		public void retrieveObservations() throws ObservationReadError,
				InterruptedException {
			
// DR3 format changed (around Jan 2025).
// DR3 header:
//			solution_id,source_id,transit_id,
//			g_transit_time,g_transit_flux,g_transit_flux_error,g_transit_flux_over_error,g_transit_mag,
//			bp_obs_time,bp_flux,bp_flux_error,bp_flux_over_error,bp_mag,
//			rp_obs_time,rp_flux,rp_flux_error,rp_flux_over_error,rp_mag,
//			variability_flag_g_reject,
//			variability_flag_bp_reject,
//			variability_flag_rp_reject,
//			g_other_flags,
//			bp_other_flags,
//			rp_other_flags,
//			rejected_by_photometry
// DR2 header:
//			source_id,transit_id,band,time,
//			mag,flux,flux_error,flux_over_error,
//			rejected_by_photometry,rejected_by_variability,other_flags,solution_id

			lines = convertDR3toDR2format(lines);
			
			setJDflavour(JDflavour.BJD);
			
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
						OvribGroup vrib;
						if (gaiaRelease == GaiaRelease.DR3)
							vrib = transformVRIB_DR3(bObs, gObsEx.ob, rObs, i + 1, greenList.size());
						else
							vrib = transformVRIB(bObs, gObsEx.ob, rObs, i + 1, greenList.size());
						collectObservation(vrib.vObs);
						collectObservation(vrib.rObs);
						collectObservation(vrib.iObs);
						if (gaiaRelease == GaiaRelease.DR3)
							collectObservation(vrib.bObs);
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

		private ValidObservation closestObs(ValidObservation obs,
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

		private OvribGroup transformVRIB(ValidObservation bObs, ValidObservation gObs,
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
			double v = magCalc(g, bp_rp, 0.01760, 0.006860, 0.1732, 0.0, 0.0);
			double r = magCalc(g, bp_rp, 0.003226, -0.3833, 0.1345, 0.0, 0.0);
			double i = magCalc(g, bp_rp, -0.02085, -0.7419, 0.09631, 0.0, 0.0);

			double bp_rp_err = Math.sqrt(bperr * bperr + rperr * rperr);

			double verr = uncCalc(g, gerr, bp_rp, bp_rp_err, 0.01760, 0.006860,	0.1732, 0.0, 0.0);
			double rerr = uncCalc(g, gerr, bp_rp, bp_rp_err, 0.003226, -0.3833,	0.1345, 0.0, 0.0);
			double ierr = uncCalc(g, gerr, bp_rp, bp_rp_err, -0.02085, -0.7419,	0.09631, 0.0, 0.0);

			String comment = String.format(Locale.ENGLISH, 
					"Transformed from Gaia G= %.5f, BP= %.5f, RP= %.5f", 
					g, bp, rp);					
		
			// V, R, I
			// Consider all three observations as discrepant if any of the G,
			// BP, RP observations
			// was flagged as rejected
			OvribGroup vrib = new OvribGroup(gObs.getJD(), gObs.getName(), gObs.getObsCode(),
					bObs.isDiscrepant() || gObs.isDiscrepant() || rObs.isDiscrepant(),
					comment);

			vrib.vObs.setMagnitude(new Magnitude(v, verr));
			vrib.vObs.setRecordNumber(obsNum);
			
			vrib.rObs.setMagnitude(new Magnitude(r, rerr));
			vrib.rObs.setRecordNumber(nGroups + obsNum);

			vrib.iObs.setMagnitude(new Magnitude(i, ierr));
			vrib.iObs.setRecordNumber(2 * nGroups + obsNum);

			vrib.bObs.setMagnitude(new Magnitude(INVALID_MAG, 0));
			
			return vrib;
		}

		private OvribGroup transformVRIB_DR3(ValidObservation bObs, ValidObservation gObs,
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
			 * Use the transformation coefficients from here:
			 * https://gea.esac.esa.int/archive/documentation/GDR3/Data_processing/chap_cu5pho/cu5pho_sec_photSystem/cu5pho_ssec_photRelations.html
			 */
			double v = magCalc(g, bp_rp, 0.02704, -0.01424, 0.2156, -0.01426, 0.0);
			double r = magCalc(g, bp_rp, 0.02275, -0.3961, 0.1243, 0.01396, -0.003775);
			double i = magCalc(g, bp_rp, -0.01753, -0.76, 0.0991, 0.0, 0.0);
			double b = magCalc(g, bp_rp, -0.01448, 0.6874, 0.3604, -0.06718, 0.006061);

			double bp_rp_err = Math.sqrt(bperr * bperr + rperr * rperr);

			double verr = uncCalc(g, gerr, bp_rp, bp_rp_err, 0.02704, -0.01424, 0.2156, -0.01426, 0.0);
			double rerr = uncCalc(g, gerr, bp_rp, bp_rp_err, 0.02275, -0.3961, 0.1243, 0.01396, -0.003775);
			double ierr = uncCalc(g, gerr, bp_rp, bp_rp_err, -0.01753, -0.76, 0.0991, 0.0, 0.0);
			double berr = uncCalc(g, gerr, bp_rp, bp_rp_err, -0.01448, 0.6874, 0.3604, -0.06718, 0.006061);

			String comment = String.format(Locale.ENGLISH, 
					"Transformed from Gaia DR3 G= %.5f, BP= %.5f, RP= %.5f", 
					g, bp, rp);					
		
			// V, R, I, B
			// Consider all three observations as discrepant if any of the G,
			// BP, RP observations
			// was flagged as rejected
			OvribGroup vrib = new OvribGroup(gObs.getJD(), gObs.getName(), gObs.getObsCode(),
					bObs.isDiscrepant() || gObs.isDiscrepant() || rObs.isDiscrepant(),
					comment);

			vrib.vObs.setMagnitude(new Magnitude(v, verr));
			vrib.vObs.setRecordNumber(obsNum);
			
			vrib.rObs.setMagnitude(new Magnitude(r, rerr));
			vrib.rObs.setRecordNumber(nGroups + obsNum);

			vrib.iObs.setMagnitude(new Magnitude(i, ierr));
			vrib.iObs.setRecordNumber(2 * nGroups + obsNum);

			vrib.bObs.setMagnitude(new Magnitude(b, berr));
			vrib.bObs.setRecordNumber(3 * nGroups + obsNum);
			
			return vrib;
		}
		
		private double magCalc(double g, double bp_rp, 
				double c0, double c1, double c2, double c3, double c4) {
			return g + 
					c0 + 
					c1 * bp_rp + 
					c2 * bp_rp * bp_rp + 
					c3 * bp_rp * bp_rp * bp_rp + 
					c4 * bp_rp * bp_rp * bp_rp * bp_rp;
		}

		// Error propagation per John Taylor's
		// "An Introduction to Error Analysis"
		// 2nd edition, chapter 3
		private double uncCalc(double g, double gerr, double bp_rp,
				double bp_rp_err, double c0, double c1, double c2, double c3, double c4) {
            // derivative of c0 + c1 * bp_rp + c2 * bp_rp^2 + c3 * bp_rp^3 + c4 * bp_rp^4 = 
			//           c1 + 2 * c2 * bp_rp + 3 * c3 * bp_rp^2            + 4 * c4 * bp_rp^3  
			double der = 
					c1 + 
					2 * c2 * bp_rp + 
					3 * c3 * bp_rp * bp_rp + 
					4 * c4 * bp_rp * bp_rp * bp_rp; 
			double t1 = der * bp_rp_err; // df/dx * Δx = error of function of one var
			return Math.sqrt(gerr * gerr + t1 * t1); // error of sum of 2 independent vars
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
			String name = "Gaia";
			switch (gaiaRelease) {
				case DR2:
					name += "DR2";
					break;
				case DR3:
					name += "DR3";
					break;
				default:
					break;
			}
			String obsCode = name;
			name += " " + source_id;
			
			gaiaSrcID = name;

			observation.setRecordNumber(recordNumber);
			observation.setName(name);
			observation.setObsCode(obsCode);

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
				addDetailAsInteger(observation, "other_flags", fields[fieldIndices.get("other_flags")]);
			}

			return observation;
		}
		
		void addDetailAsInteger(ValidObservation observation, String key, String detail) {
			if (detail != null) {
				Integer i;
				try {
					i = Integer.valueOf(detail.trim());
				} catch (NumberFormatException e) {
					return;
				}
				observation.addDetail(key.toUpperCase(), i, key);
			}
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

}
