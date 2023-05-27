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
package org.aavso.tools.vstar.external.lib;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

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
import org.aavso.tools.vstar.exception.ObservationReadError;
import org.aavso.tools.vstar.exception.ObservationValidationError;
import org.aavso.tools.vstar.input.AbstractObservationRetriever;
import org.aavso.tools.vstar.plugin.ObservationSourcePluginBase;

/**
 * 
 * @author max (PMAK)
 * 
 * see https://irsa.ipac.caltech.edu/docs/program_interface/ztf_lightcurve_api.html
 * 
 */
public abstract class ZTFObSourceBase extends ObservationSourcePluginBase {

	protected SeriesType ztfgSeries;
	protected SeriesType ztfrSeries;
	protected SeriesType ztfiSeries;
	protected SeriesType ztfUnknownSeries;
	
	/**
	 * Constructor
	 */
	public ZTFObSourceBase() {
		super();
		ztfgSeries = SeriesType.create("ZTF zg", "ZTF zg", Color.GREEN, false, false);
		ztfrSeries = SeriesType.create("ZTF zr", "ZTF zr", Color.RED, false, false);
		ztfiSeries = SeriesType.create("ZTF zi", "ZTF zi", new Color(192, 64, 0), false, false);
		ztfUnknownSeries = SeriesType.create("ZTF unknown", "ZTF unknown", new Color(255, 255, 0), false, false);
	}
	
	/**
	 * @see org.aavso.tools.vstar.plugin.ObservationSourcePluginBase#
	 *      getObservationRetriever ()
	 */
	@Override
	public AbstractObservationRetriever getObservationRetriever() {
		return new ZTFFormatRetriever();
	}

	class ZTFFormatRetriever extends AbstractObservationRetriever {

		private Map<String, Integer> fieldIndices;
		
		//private String obscode = "ZTF";
		private String delimiter = null;
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
			
			fieldIndices = new HashMap<String, Integer>();
			fieldIndices.put("oid", -1);
			fieldIndices.put("hjd", -1);
			fieldIndices.put("mag", -1);
			fieldIndices.put("magerr", -1);
			fieldIndices.put("catflags", -1);
			fieldIndices.put("filtercode", -1);
			fieldIndices.put("exptime", -1);
			fieldIndices.put("airmass", -1);
			
			julianDayValidator = new JulianDayValidator();
			magnitudeFieldValidator = new MagnitudeFieldValidator();
			uncertaintyValueValidator = new UncertaintyValueValidator(new InclusiveRangePredicate(0, 1));
			ztfObjects = new HashSet<String>();
		}
		
		@Override
		public void retrieveObservations() throws ObservationReadError,
				InterruptedException {

			setJDflavour(JDflavour.HJD);

			if (lines.size() == 0) {
				return;
			}
			
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
							headerFound = checkForHeaderAndFillFieldIndices(line);
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
		
		private boolean checkForHeaderAndFillFieldIndices(String line) {
			String localDelim = "\t";
			if (checkForHeaderAndFillFieldIndices(line.split(localDelim))) {
				delimiter = localDelim;
				return true;
			} else {
				localDelim = ",";
				if (checkForHeaderAndFillFieldIndices(line.split(localDelim))) {
					delimiter = localDelim;
					return true;
				} else {
					// This special delimiter occurs when the user copies data from an HTML table generated via VSX "External Links" ZTF
					//localDelim = "\s*\t";
					localDelim = " *\t";
					if (checkForHeaderAndFillFieldIndices(line.split(localDelim))) {
						delimiter = localDelim;
						return true;
					}
				}
			}
			return false;
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

			addDetailAsInteger(observation, "catflags", fields[fieldIndices.get("catflags")]);
			addDetailAsDouble(observation, "exptime", fields[fieldIndices.get("exptime")]);			
			addDetailAsDouble(observation, "airmass", fields[fieldIndices.get("airmass")]);
			// todo: add other details
			
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

		void addDetailAsDouble(ValidObservation observation, String key, String detail) {
			if (detail != null) {
				Double d;
				try {
					d = Double.valueOf(detail.trim());
				} catch (NumberFormatException e) {
					return;
				}
				observation.addDetail(key.toUpperCase(), d, key);
			}
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

}
