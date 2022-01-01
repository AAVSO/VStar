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
import java.util.List;
import java.util.Map;

import org.aavso.tools.vstar.data.DateInfo;
import org.aavso.tools.vstar.data.InvalidObservation;
import org.aavso.tools.vstar.data.Magnitude;
import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.exception.ObservationReadError;
import org.aavso.tools.vstar.input.AbstractObservationRetriever;
import org.aavso.tools.vstar.plugin.InputType;
import org.aavso.tools.vstar.plugin.ObservationSourcePluginBase;

//12/02/2018 C. Kotnik added name to observations so they can be
//saved and reloaded from a file.
/**
 * <p>
 * ASAS observation source plug-in.
 * </p>
 * <ul>
 * <li>The magnitude and error columns appropriate to the object are used.</li>
 * <li>Each dataset is assigned the series ASAS-n where n is the dataset number.
 * </li>
 * <li>No dataset is discarded since in some cases, they are similar in
 * size.</li>
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

	private final static Color[] COLORS = { Color.RED, Color.GREEN, Color.BLUE, Color.ORANGE };

	/**
	 * Constructor
	 */
	public ASASObservationSource() {
	}

	@Override
	public InputType getInputType() {
		return InputType.FILE_OR_URL;
	}

	@Override
	public String getDescription() {
		return "ASAS file reader";
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
		private Integer datasetNum;

		private int seriesColor;

		public ASASFileReader() {
			super(getVelaFilterStr());
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
		public void retrieveObservations() throws ObservationReadError, InterruptedException {
			BufferedReader reader = new BufferedReader(new InputStreamReader(getInputStreams().get(0)));

			String line = null;
			int lineNum = 1;

			do {
				try {
					line = reader.readLine();

					if (line != null) {

						line = line.replaceFirst("\n", "").replaceFirst("\r", "").trim();

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

		// ** State machine methods. **

		private void handleInfo(String line) {
			if (line.startsWith("#dataset")) {
				String dataset = getStringFromNameValuePair(line);
				datasetNum = Integer.parseInt(dataset.split("\\s*;\\s*")[0]);
				// String seriesName = "ASAS-" + datasetNum;

				// seriesType = SeriesType.create(seriesName, seriesName,
				// COLORS[seriesColor++], false, false);
				// if (seriesColor == COLORS.length) {
				// seriesColor = 0;
				// }
			} else if (line.startsWith("#desig")) {
				designation = getStringFromNameValuePair(line);
			} else if (line.startsWith("#") && line.contains("HJD")) {
				// We have found the start of data.
				state = State.DATA;
			}
		}

		private void handleData(String line, int lineNum) throws ObservationReadError {
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
					ob.setName("ASAS " + designation);
					ob.setComments("dataset " + datasetNum);
					ob.setDateInfo(new DateInfo(hjd));
					ob.setMagnitude(new Magnitude(mag, magErr));
					ob.setBand(SeriesType.Johnson_V);
					ob.setRecordNumber(lineNum);
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

	// Test methods

	@Override
	public Boolean test() {
		return etaAqlTest();
	}

	private boolean etaAqlTest() {
		// These lines were taken from an actual ASAS file. some header comment lines
		// were omitted and only the first data set (commencing with #ndata) was
		// extracted.
		String[] lines = { "# The All Sky Automated Survey Data\n", "# gp@astrouw.edu.pl\n", "# ...\n",
				"# ######### LIGHT CURVE BEGINS NEXT LINE ###########\n", "#ndata= 1\n",
				"#dataset= 1 ; 1 F1944-08_325\n", "#desig= 195229+0100.3\n", "#cra=   19.874652  19:52:28.7\n",
				"#cdec=   1.005554 01:00:20.0\n", "#class= 0\n", "#cmag_0=  5.719\n", "#cmer_0=  0.000\n",
				"#nskip_0= 0\n", "#cmag_1=  5.351\n", "#cmer_1=  0.000\n", "#nskip_1= 0\n", "#cmag_2=  5.215\n",
				"#cmer_2=  0.000\n", "#nskip_2= 0\n", "#cmag_3=  5.129\n", "#cmer_3=  0.000\n", "#nskip_3= 0\n",
				"#cmag_4=  5.114\n", "#cmer_4=  0.000\n", "#nskip_4= 0\n", "#ra=   19.874652  19:52:28.7\n",
				"#dec=   1.005554 01:00:20.0\n",
				"#     HJD      MAG_4  MAG_0  MAG_1  MAG_2  MAG_3    MER_4 MER_0 MER_1 MER_2 MER_3 GRADE FRAME\n",
				"   2140.58808  5.114  5.720  5.352  5.216  5.130    0.039 0.057 0.036 0.032 0.036  A 30163\n" };

		boolean success = true;

		try {
			AbstractObservationRetriever retriever = getTestRetriever(lines, "eta Aql");

			success &= retriever.isHeliocentric();

			Map<SeriesType, List<ValidObservation>> seriesMap = retriever.getValidObservationCategoryMap();

			List<ValidObservation> asasObs = seriesMap.get(SeriesType.getSeriesFromDescription("Johnson V"));
			success &= 1 == asasObs.size();

			ValidObservation ob = asasObs.get(0);
			success &= "195229+0100.3".equals(ob.getDetail("DESIGNATION"));
			success &= "30163".equals(ob.getDetail("FRAME"));
			success &= "A".equals(ob.getDetail("CLASS"));
			success &= 2452140.58808 == ob.getJD();
			success &= 5.114 == ob.getMag();
			success &= 0.039 == ob.getMagnitude().getUncertainty();
		} catch (Exception e) {
			success = false;
		}

		return success;
	}
}
