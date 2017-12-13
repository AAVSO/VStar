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
 * ASAS-SN file observation source plug-in for CSV observation files obtained
 * from https://asas-sn.osu.edu
 * </p>
 * <p>
 * This plug-in reads CSV files in this format:
 * </p>
 * HJD,UT Date,Camera,FWHM,Limit,mag,mag_err,flux(mJy),flux_err
 * 2458000.47009,2017-09-03.9710173,be,1.77,15.518,14.839,0.117,4.448,0.477
 * 2458000.47137,2017-09-03.9722892,be,1.76,15.730,14.760,0.090,4.789,0.392
 * 2458000.47263,2017-09-03.9735547,be,1.72,15.751,14.759,0.088,4.788,0.385
 * 2458002.51020,2017-09-06.0112978,be,2.03,14.814,>14.814,99.990,4.020,0.911
 * 2458002.51147,2017-09-06.0125694,be,2.03,14.729,>14.729,99.990,3.640,0.985
 * ...<br/>
 */
public class ASASSNObservationSource extends ObservationSourcePluginBase {

	private SeriesType asassnVSeries;
	private SeriesType asassn5SigmaLimitSeries;

	/**
	 * Constructor
	 */
	public ASASSNObservationSource() {
		asassnVSeries = SeriesType.create("ASAS-SN", "ASAS-SN", Color.GREEN,
				false, false);
		asassn5SigmaLimitSeries = SeriesType.create("ASAS-SN 5\u03C3 limit",
				"ASAS-SN limit", Color.BLUE, false, false);
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
		return new ASASSNFileReader();
	}

	class ASASSNFileReader extends AbstractObservationRetriever {

		private static final double MAX = 99.99;

		private JulianDayValidator julianDayValidator;
		private MagnitudeFieldValidator magnitudeFieldValidator;
		private UncertaintyValueValidator uncertaintyValueValidator;

		public ASASSNFileReader() {
			julianDayValidator = new JulianDayValidator();
			magnitudeFieldValidator = new MagnitudeFieldValidator();
			uncertaintyValueValidator = new UncertaintyValueValidator(
					new InclusiveRangePredicate(0, 99.99));
			setHeliocentric(true);
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
			int lineNum = 1;
			int obNum = 1;

			do {
				try {
					line = reader.readLine();
					if (line != null) {
						line = line.replaceFirst("\n", "").trim();
						if (!isEmpty(line)) {
							// Skip header row
							if (!line.startsWith("HJD")) {
								String[] fields = line.split(",");
								ValidObservation ob = readNextObservation(
										fields, obNum);
								Magnitude mag = ob.getMagnitude();
								// Skip any observation whose magnitude and
								// error are 99.99
								if (mag.getMagValue() != MAX
										&& mag.getUncertainty() != MAX) {
									collectObservation(ob);
									obNum++;
								}
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

			SeriesType series = asassnVSeries;

			DateInfo hjd = julianDayValidator.validate(fields[0]);

			Magnitude mag = magnitudeFieldValidator.validate(fields[5]);
			double err = uncertaintyValueValidator.validate(fields[6]);
			if (err == MAX) {
				err = 0.0;
				series = asassn5SigmaLimitSeries;
			}
			mag.setUncertainty(err);

			observation.setMagnitude(mag);
			observation.setDateInfo(hjd);
			observation.setRecordNumber(obNum);
			observation.setBand(series);
			observation.addDetail("UT", fields[1], "UT Date");
			observation.addDetail("Camera", fields[2], "Camera");
			observation.addDetail("FWHM", fields[3], "FWHM");
			observation.addDetail("LIMIT", fields[4], "Limit");
			observation.addDetail("FLUX", fields[7], "Flux (mJy)");
			observation.addDetail("FLUX_ERR", fields[8], "Flux error");

			return observation;
		}

		private boolean isEmpty(String str) {
			return str != null && "".equals(str.trim());
		}
	}
}
