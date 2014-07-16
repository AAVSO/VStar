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
import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.aavso.tools.vstar.data.DateInfo;
import org.aavso.tools.vstar.data.InvalidObservation;
import org.aavso.tools.vstar.data.Magnitude;
import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.data.validation.InclusiveRangePredicate;
import org.aavso.tools.vstar.data.validation.JulianDayValidator;
import org.aavso.tools.vstar.data.validation.MagnitudeFieldValidator;
import org.aavso.tools.vstar.data.validation.UncertaintyValueValidator;
import org.aavso.tools.vstar.exception.ObservationReadError;
import org.aavso.tools.vstar.exception.ObservationValidationError;
import org.aavso.tools.vstar.input.AbstractObservationRetriever;
import org.aavso.tools.vstar.plugin.InputType;
import org.aavso.tools.vstar.plugin.ObservationSourcePluginBase;

/**
 * <p>
 * Catalina Sky Survey data file observation source plugin available from:<br/>
 * http://nesssi.cacr.caltech.edu/cgi-bin/getcssconedb_release.cgi
 * </p>
 * <p>
 * See http://nesssi.cacr.caltech.edu/DataRelease/usage_cone.html
 * </p>
 * <p>
 * This plugin currently handles "short format" CSV files, as defined on the
 * usage page above, of the following form:
 * </p>
 * MasterID Mag Magerr RA Dec MJD Blend<br/>
 * 1140046004742,13.05,0.05,164.84942,39.73488,53712.41388,0<br/>
 * 1140046004742,13.04,0.05,164.84938,39.73486,53712.42062,0<br/>
 * 1140046004742,13.07,0.05,164.84940,39.73488,53712.42735,0<br/>
 * 1140046004742,13.03,0.05,164.84940,39.73488,53712.43415,0<br/>
 * ...<br/>
 * 
 * Long format is not currently handled. Neither is HTML or VOTable output. See
 * the "Advanced parameters" link on the cone search page referred to above.
 */
public class CatalinaSkySurveyObservationSource extends
		ObservationSourcePluginBase {

	private SeriesType catalinaSeries;

	/**
	 * Constructor.
	 */
	public CatalinaSkySurveyObservationSource() {
		catalinaSeries = SeriesType.create("CSS", "CSS", Color.MAGENTA, false,
				false);
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
		return "Catalina Sky Survey file reader";
	}

	/**
	 * @see org.aavso.tools.vstar.plugin.IPlugin#getDisplayName()
	 */
	@Override
	public String getDisplayName() {
		return "New Star from Catalina Sky Survey File...";
	}

	/**
	 * @see org.aavso.tools.vstar.plugin.ObservationSourcePluginBase#getObservationRetriever()
	 */
	@Override
	public AbstractObservationRetriever getObservationRetriever() {
		return new CatalinaSkySurveyFileReader();
	}

	class CatalinaSkySurveyFileReader extends AbstractObservationRetriever {

		private JulianDayValidator julianDayValidator;
		private MagnitudeFieldValidator magnitudeFieldValidator;
		private UncertaintyValueValidator uncertaintyValueValidator;

		public CatalinaSkySurveyFileReader() {
			julianDayValidator = new JulianDayValidator();
			magnitudeFieldValidator = new MagnitudeFieldValidator();
			uncertaintyValueValidator = new UncertaintyValueValidator(
					new InclusiveRangePredicate(0, 1));
		}

		@Override
		public String getSourceName() {
			return getInputName();
		}

		@Override
		public String getSourceType() {
			return "Catalina Sky Survey File";
		}

		@Override
		public void retrieveObservations() throws ObservationReadError,
				InterruptedException {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					getInputStreams().get(0)));

			String line = null;
			int lineNum = 1;
			int obNum = 1;

			do {
				try {
					line = reader.readLine();
					if (line != null) {
						line = line.replaceFirst("\n", "").replaceFirst("\r",
								"").trim();
						if (!isEmpty(line)) {
							if (!line.startsWith("MasterID")) {
								String[] fields = line.split(",");
								collectObservation(readNextObservation(fields,
										obNum));
								obNum++;
							}
						}
						lineNum++;
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
			} while (line != null);
		}

		private ValidObservation readNextObservation(String[] fields, int obNum)
				throws ObservationValidationError {
			ValidObservation observation = new ValidObservation();

			Magnitude mag = magnitudeFieldValidator.validate(fields[1]);
			double err = uncertaintyValueValidator.validate(fields[2]);
			mag.setUncertainty(err);

			DateInfo mjdDate = julianDayValidator.validate(fields[5]);
			DateInfo jdDate = new DateInfo(2400000 + mjdDate.getJulianDay());

			observation.setMagnitude(mag);
			observation.setDateInfo(jdDate);
			observation.setRecordNumber(obNum);
			observation.setBand(catalinaSeries);
			observation.addDetail("MasterID", fields[0], "Master ID");
			observation.addDetail("RA", fields[3], "RA");
			observation.addDetail("Dec", fields[4], "Dec");
			observation.addDetail("Blend", fields[6], "Blend");

			return observation;
		}

		private boolean isEmpty(String str) {
			return str != null && "".equals(str.trim());
		}
	}
}
