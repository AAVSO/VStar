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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.aavso.tools.vstar.data.DateInfo;
import org.aavso.tools.vstar.data.InvalidObservation;
import org.aavso.tools.vstar.data.Magnitude;
import org.aavso.tools.vstar.data.MagnitudeModifier;
import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.data.validation.MagnitudeFieldValidator;
import org.aavso.tools.vstar.exception.ObservationReadError;
import org.aavso.tools.vstar.exception.ObservationValidationError;
import org.aavso.tools.vstar.input.AbstractObservationRetriever;
import org.aavso.tools.vstar.plugin.InputType;
import org.aavso.tools.vstar.plugin.ObservationSourcePluginBase;
import org.aavso.tools.vstar.util.date.AbstractDateUtil;

/**
 * <p>
 * VSOLJ observation source plugin.
 * </p>
 * <p>
 * Looking for a line ending in "VSOLJ" followed by data lines, e.g.
 * </p>
 * 2006 Feb. VSOLJ<br/>
 * =======================================================================<br/>
 * AQLeta 190607130500 3.90 xyz<br/>
 * AQLeta 190607180327 4.25 abc<br/>
 * AQLeta 190607240624 4.86 xyz<br/>
 * ...</br>
 * =======================================================================<br/>
 * Total: N obs.</br>
 * <p>
 * See http://vsolj.cetus-net.org/cgi-bin/obs_search.cgi<br/>
 * </p>
 */
public class VSOLJObservationSource extends ObservationSourcePluginBase {

	enum State {
		// Seek the start of the data.
		SEEK,
		// Data start.
		DATA_START,
		// Read data.
		DATA,
		// Data end.
		DATA_END
	};

	final Pattern VSOLJ_TIME = Pattern
			.compile("(\\d{4})(\\d{2})(\\d{2})(\\d{4,6})");

	final Pattern VSOLJ_MAG = Pattern
			.compile("((<|>)?\\d+(\\.\\d+)?\\:?)(\\w*)");

	@Override
	public InputType getInputType() {
		return InputType.FILE;
	}

	@Override
	public String getDescription() {
		return "VSOLJ observation file reader";
	}

	@Override
	public String getDisplayName() {
		return "New Star from VSOLJ File...";
	}

	@Override
	public AbstractObservationRetriever getObservationRetriever() {
		return new VSOLJObservationRetriever();
	}

	class VSOLJObservationRetriever extends AbstractObservationRetriever {

		private State state;
		private MagnitudeFieldValidator magValidator;

		public VSOLJObservationRetriever() {
			super(getVelaFilterStr());
			state = State.SEEK;
			magValidator = new MagnitudeFieldValidator();
		}

		@Override
		public String getSourceName() {
			return getInputName();
		}

		@Override
		public String getSourceType() {
			return "VSOLJ File";
		}

		@Override
		public void retrieveObservations() throws ObservationReadError,
				InterruptedException {

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					getInputStreams().get(0)));

			String line = null;
			int lineNum = 1;

			do {
				try {
					line = reader.readLine();

					if (line != null) {

						line = line.replaceFirst("\n", "").replaceFirst("\r",
								"").trim();

						if (!isEmpty(line)) {
							// State machine cases.
							switch (state) {
							case SEEK:
								// e.g. "2006 Feb. VSOLJ", followed by data
								// lines.
								if (line.endsWith("VSOLJ")) {
									state = State.DATA_START;
								}
								break;

							case DATA_START:
								// Skip "=====..." before data.
								state = State.DATA;
								break;

							case DATA:
								if (line.startsWith("=")) {
									state = State.DATA_END;
								} else {
									handleData(line);
								}
								break;

							case DATA_END:
								// Ignore everything else.
								break;
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
					ob.setRecordNumber(lineNum++);
					addInvalidObservation(ob);
				}
			} while (line != null);
		}

		private void handleData(String line) throws ObservationReadError {
			// e.g. "AQLeta 190607130500 3.90 xyz"
			String[] fields = line.split(" ");
			String name = fields[0];
			Matcher timeMatcher = VSOLJ_TIME.matcher(fields[1]);
			if (timeMatcher.matches()) {
				double jd = getJD(timeMatcher);

				try {
					String magStr = fields[2];

					Matcher magMatcher = VSOLJ_MAG.matcher(magStr);
					if (magMatcher.matches()) {
						ValidObservation ob = new ValidObservation();
						ob.setName(name);
						ob.setDateInfo(new DateInfo(jd));
						ob.setMagnitude(getMag(magMatcher));
						ob.setBand(getBand(magMatcher));
						ob.setObsCode(fields[3]);
						ob.addDetail("SOURCE", "VSOLJ", "Source");

						collectObservation(ob);
					} else {
						throw new ObservationReadError("Invalid magnitude");
					}
				} catch (ObservationValidationError e) {
					throw new ObservationReadError(e.getLocalizedMessage());
				}
			} else {
				throw new ObservationReadError("Invalid observation time");
			}
		}

		private double getJD(Matcher timeMatcher) {
			int year = Integer.parseInt(timeMatcher.group(1));
			int month = Integer.parseInt(timeMatcher.group(2));
			int dd = Integer.parseInt(timeMatcher.group(3));
			String time = timeMatcher.group(4);
			int hour = Integer.parseInt(time.substring(0, 2));
			int min = Integer.parseInt(time.substring(2, 4));
			int sec = 0;
			if (time.length() == 6) {
				sec = Integer.parseInt(time.substring(4, 6));
			}
			double day = dd + (hour + min / 60 + sec / 3600) / 24;
			return AbstractDateUtil.getInstance()
					.calendarToJD(year, month, day);
		}

		private Magnitude getMag(Matcher magMatcher)
				throws ObservationValidationError {
			String magStr = magMatcher.group(1);
			Magnitude mag = magValidator.validate(magStr);

			// For magnitudes without a decimal point, divide by 10.
			if (!magStr.contains(".")) {
				MagnitudeModifier modifier = null;

				// Note: This shows that we need to be able to set all fields of
				// a Magnitude object for convenience and to reduce the number
				// of objects created.
				if (mag.isFainterThan()) {
					modifier = MagnitudeModifier.FAINTER_THAN;
				} else if (mag.isBrighterThan()) {
					modifier = MagnitudeModifier.BRIGHTER_THAN;
				} else {
					modifier = MagnitudeModifier.NO_DELTA;
				}

				mag = new Magnitude(mag.getMagValue() / 10, modifier, mag
						.isUncertain(), mag.getUncertainty());
			}

			return mag;
		}

		private SeriesType getBand(Matcher magMatcher) {
			SeriesType seriesType = null;

			String bandStr = magMatcher.group(4);

			if (bandStr.equals("C")) {
				return SeriesType.Unknown;
			}

			if (bandStr.toUpperCase().equals("RC")) {
				// Cousins R
				bandStr = "R";
			}

			seriesType = SeriesType.getSeriesFromShortName(bandStr);

			// If we don't know what the band is, assume it's visual.
			if (seriesType == SeriesType.getDefault()) {
				seriesType = SeriesType.Visual;
			}

			return seriesType;
		}

		private boolean isEmpty(String str) {
			return str != null && "".equals(str.trim());
		}
	}
}
