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
 * <p>
 * ASAS observation source plug-in.
 * </p>
 * <ul>
 * <li>The magnitude and error columns appropriate to the object are used.</li>
 * <li>Each dataset is assigned the series ASAS-n where n is the dataset number.
 * </li>
 * <li>No dataset is discarded since in some cases, they are similar in size.</li>
 * <li>Class C and D data is discarded from every data set.</li>
 * <li>Class, frame, designation are collected for each observation.</li>
 * </ul>
 * <p>
 * See also:
 * <ul>
 * <li>http://www.astrouw.edu.pl/asas/?page=acvs</li>
 * <li>https://sourceforge.net/p/vstar/bugs-and-features/324</li>
 * </ul>
 * </p>
 */
public class ASASObservationSource extends ObservationSourcePluginBase {

	enum State {
		// Seek the start of the next dataset.
		SEEK,
		// Get information about the current dataset.
		INFO,
		// Read a dataset.
		DATA
	};

	private final static Color[] COLORS = { Color.RED, Color.GREEN, Color.BLUE,
			Color.ORANGE };

	/**
	 * Constructor
	 */
	public ASASObservationSource() {
	}

	@Override
	public InputType getInputType() {
		return InputType.FILE;
	}

	@Override
	public String getDescription() {
		return "An ASAS file reader.";
	}

	@Override
	public String getDisplayName() {
		return "New Star from ASAS File...";
	}

	@Override
	public AbstractObservationRetriever getObservationRetriever() {
		return new ASASFileReader();
	}

	class ASASFileReader extends AbstractObservationRetriever {

		private State state;

		private Integer numObs;
		private SeriesType seriesType;
		private String designation;

		private int seriesColor;

		public ASASFileReader() {
			state = State.SEEK;
			numObs = null;
			seriesType = null;
			designation = null;
			seriesColor = 0;
			this.setHeliocentric(true);
		}

		@Override
		public String getSourceName() {
			return getInputName();
		}

		@Override
		public String getSourceType() {
			return "ASAS File";
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
								if (line.startsWith("#ndata")) {
									numObs = getIntFromNameValuePair(line);
									state = State.INFO;
								}
								break;

							case INFO:
								handleInfo(line);
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
					ob.setRecordNumber(lineNum);
					addInvalidObservation(ob);
				}
			} while (line != null);
		}

		// ** State machine methods. **

		private void handleInfo(String line) {
			if (line.startsWith("#dataset")) {
				String dataset = getStringFromNameValuePair(line);
				int datasetNum = Integer
						.parseInt(dataset.split("\\s*;\\s*")[0]);
				String seriesName = "ASAS-" + datasetNum;
				seriesType = SeriesType.create(seriesName, seriesName,
						COLORS[seriesColor++], false, false);
				if (seriesColor == COLORS.length) {
					seriesColor = 0;
				}
			} else if (line.startsWith("#desig")) {
				designation = getStringFromNameValuePair(line);
			} else if (line.startsWith("#") && line.contains("HJD")) {
				// We have found the start of data.
				state = State.DATA;
			}
		}

		private void handleData(String line) throws ObservationReadError {
			if (line.startsWith("#dataset")) {
				// We've found the next part of a split
				// dataset and need to get the designation
				// then skip until we see data again.
				state = State.INFO;
			} else {
				String[] fields = line.split("\\s+");
				// Reject class C or D data.
				String dataClass = fields[11];
				if (!"C".equals(dataClass) && !"D".equals(dataClass)) {
					double hjd = Double.parseDouble(fields[0]) + 2450000;
					double mag = Double.parseDouble(fields[1]);
					double magErr = Double.parseDouble(fields[6]);

					ValidObservation ob = new ValidObservation();
					ob.setDateInfo(new DateInfo(hjd));
					ob.setMagnitude(new Magnitude(mag, magErr));
					ob.setBand(seriesType);
					ob.addDetail("DESIGNATION", designation, "Designation");
					ob.addDetail("CLASS", dataClass, "Class");
					ob.addDetail("FRAME", fields[12], "Frame");
					collectObservation(ob);
				}

				// Have we reached the end of the current dataset? If so,
				// start looking for the next one.
				numObs--;
				if (numObs == 0) {
					state = State.SEEK;
				}
			}
		}

		// ** String processing helper methods. **

		private boolean isEmpty(String str) {
			return str != null && "".equals(str.trim());
		}

		private int getIntFromNameValuePair(String str) {
			String[] fields = str.split("\\s*=\\s*");
			return Integer.parseInt(fields[1]);
		}

		private String getStringFromNameValuePair(String str) {
			String[] fields = str.split("\\s*=\\s*");
			return fields[1];
		}
	}
}
