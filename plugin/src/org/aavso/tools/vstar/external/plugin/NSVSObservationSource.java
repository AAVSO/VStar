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
import org.aavso.tools.vstar.exception.ObservationReadError;
import org.aavso.tools.vstar.input.AbstractObservationRetriever;
import org.aavso.tools.vstar.plugin.InputType;
import org.aavso.tools.vstar.plugin.ObservationSourcePluginBase;

/**
 * Northern Sky Variability Survey (NSVS) observation source plugin.
 */
public class NSVSObservationSource extends ObservationSourcePluginBase {

	enum State {
		// Start out assuming a header.
		HEADER,
		// Read a dataset.
		DATA
	};

	@Override
	public InputType getInputType() {
		return InputType.FILE;
	}

	@Override
	public AbstractObservationRetriever getObservationRetriever() {
		return new NSVSFileReader();
	}

	@Override
	public String getDescription() {
		return "An NSVS file reader.";
	}

	@Override
	public String getDisplayName() {
		return "New Star from NSVS File...";
	}

	class NSVSFileReader extends AbstractObservationRetriever {

		private State state;
		private SeriesType series;

		public NSVSFileReader() {
			state = State.HEADER;
			series = SeriesType.create("NSVS", "NSVS", Color.RED, false, false);
		}

		@Override
		public String getSourceName() {
			return getInputName();
		}

		@Override
		public String getSourceType() {
			return "NSVS File";
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
							case HEADER:
								if (line.startsWith("MJD-50000")) {
									state = State.DATA;
								} else if (Character.isDigit(line.charAt(0))) {
									state = State.DATA;
									handleData(line, lineNum);
								}
								break;

							case DATA:
								handleData(line, lineNum);
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
					ob.setRecordNumber(lineNum);
					addInvalidObservation(ob);
				}
			} while (line != null);
		}

		// ** Helper methods. **

		private void handleData(String line, int lineNum) throws ObservationReadError {
			String[] fields = line.split("\t");

			// Modified Julian Day (MJD) is defined as MJD = JD - 2400000.5.
			// NSVS uses MJD-50000.
			double jd = Double.parseDouble(fields[0].trim()) + 50000 + 2400000.5;
			double mag = Double.parseDouble(fields[1].trim());
			double magErr = Double.parseDouble(fields[2].trim());
			String flags = fields[3];

			ValidObservation ob = new ValidObservation();
			ob.setDateInfo(new DateInfo(jd));
			ob.setMagnitude(new Magnitude(mag, magErr));
			ob.setBand(series);
			ob.setRecordNumber(lineNum);
			ob.addDetail("FLAGS", flags, "Flags");
			collectObservation(ob);
		}

		private boolean isEmpty(String str) {
			return str != null && "".equals(str.trim());
		}
	}
}
