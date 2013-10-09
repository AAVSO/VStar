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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.aavso.tools.vstar.data.DateInfo;
import org.aavso.tools.vstar.data.InvalidObservation;
import org.aavso.tools.vstar.data.Magnitude;
import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.exception.ObservationReadError;
import org.aavso.tools.vstar.input.AbstractObservationRetriever;
import org.aavso.tools.vstar.plugin.InputType;
import org.aavso.tools.vstar.plugin.ObservationSourcePluginBase;
import org.aavso.tools.vstar.util.date.AbstractDateUtil;

/**
 * <p>
 * VSOLJ observation source plugin.
 * </p>
 * <p>
 * Looking for:
 * </p>
 * <p>
 * 2006 Feb. VSOLJ
 * </p>
 * <p>=
 * =======================================================================
 * </p>
 * <p>
 * AQLeta 190607130500 3.90 xyz
 * </p>
 * <p>
 * AQLeta 190607180327 4.25 abc
 * </p>
 * <p>
 * AQLeta 190607240624 4.86 xyz
 * </p>
 * ...</br>
 * <p>=
 * =======================================================================
 * </p>
 * <p>
 * Total: N obs.</br>
 * </p>
 * <p>
 * See http://vsolj.cetus-net.org/cgi-bin/obs_search.cgi<br/>
 * We assume the UT radio button as been selected.
 * </p>
 * <p>
 * TODO:
 * <ul>
 * <li>Divide by 10 for mags > N?</li>
 * <li>Handle bands</li>
 * <li>Check against final num obs line?</li>
 * </ul>
 * </p>
 */
public class VSOLJObservationSource extends ObservationSourcePluginBase {

	enum State {
		// Seek the start of the data.
		SEEK,
		// Read data.
		DATA,
		// Count line reached.
		COUNT
	};

	final Pattern VSOLJ_TIME = Pattern
			.compile("(\\d{4})(\\d{2})(\\d{2})(\\d{4,6})");

	@Override
	public InputType getInputType() {
		return InputType.FILE;
	}

	@Override
	public String getDescription() {
		return "A VSOLJ observation file reader.";
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

		private Integer numObs;
		private SeriesType seriesType;

		public VSOLJObservationRetriever() {
			state = State.SEEK;
			numObs = 0;
			seriesType = null;
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
									String seriesName = "VSOLJ";
									seriesType = SeriesType.create(seriesName,
											seriesName, Color.GREEN, false,
											false);
									state = State.DATA;
								}
								break;

							case DATA:
								handleData(line);
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
			if (line.startsWith("Total:")) {
				state = State.COUNT;
			} else {
				// e.g. "AQLeta 190607130500 3.90 xyz"
				String[] fields = line.split(" ");
				String name = fields[0];
				Matcher match = VSOLJ_TIME.matcher(fields[1]);
				if (match.matches()) {
					int year = Integer.parseInt(match.group(1));
					int month = Integer.parseInt(match.group(2));
					int dd = Integer.parseInt(match.group(3));
					String time = match.group(4);
					int hour = Integer.parseInt(time.substring(0, 2));
					int min = Integer.parseInt(time.substring(2, 4));
					int sec = 0;
					if (time.length() == 6) {
						sec = Integer.parseInt(time.substring(4, 6));
					}
					double day = dd + (hour + min / 60 + sec / 3600) / 24;
					double jd = AbstractDateUtil.getInstance().calendarToJD(
							year, month, day);

					double mag = Double.parseDouble(fields[2]);
					double magErr = 0;
					String obsCode = fields[3];

					ValidObservation ob = new ValidObservation();
					ob.setName(name);
					ob.setDateInfo(new DateInfo(jd));
					ob.setMagnitude(new Magnitude(mag, magErr));
					ob.setBand(seriesType);
					ob.setObsCode(obsCode);
					collectObservation(ob);

					numObs++;
				} else {
					throw new ObservationReadError("Invalid observation line.");
				}
			}
		}

		// ** String processing helper methods. **

		private boolean isEmpty(String str) {
			return str != null && "".equals(str.trim());
		}
	}
}
