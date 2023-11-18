/**
 * VStar: a statistical analysis tool for variable star data.
 * Copyright (C) 2014 AAVSO (http://www.aavso.org/)
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
//12/02/2018 C. Kotnik added name to observations so they can be
//saved and reloaded from a file.
/**
 * HipparcosObservationSource is a VStar observation source plug-in tool which
 * reads Hipparcos/Tycho catalogue data from a URL of the form 
 * http://cdsarc.u-strasbg.fr/viz-bin/nph-Plot/Vgraph/htm?I/239/76343, as 
 * constructed to access observational data for the star HIP 76343 ...
 * 
 * To find the data for a particular star, construct a URL like the one above 
 * replacing “76343” with the HIP number of interest. 
 * 
 * Note that Hipparcos makes use of the Barycentric Julian Date (BJD).
 * 
 * @author Paul F. York
 * @version 1.0 - 22 Jul 2014: Original
 * @version 1.1 - 08 Aug 2014: Refactored the code
 * @version 1.2 - 17 Aug 2014: Removed unnecessary .isEmpty method and call
 * @version 1.3 - 15 Sep 2014: Fixed incorrect record count
 * @version 1.5 - 04 Mar 2021: Documentation updated
 * @version 1.6 - 13 Mar 2021: Further update to documentation
 */

public class HipparcosObservationSource extends ObservationSourcePluginBase {

	enum State {
		// Reading a line from the header ...
		HEADER,
		// Reading a line of data ...
		DATA
	};

	@Override
	public InputType getInputType() {
		return InputType.FILE_OR_URL;
	}

	@Override
	public AbstractObservationRetriever getObservationRetriever() {
		return new HipparcosFileReader();
	}

	@Override
	public String getDescription() {
		return "Hipparcos file reader";
	}

	@Override
	public String getDisplayName() {
		return "New Star from Hipparcos File...";
	}

	/**
	 * @see org.aavso.tools.vstar.plugin.IPlugin#getDocName()
	 */
	@Override
	public String getDocName() {
		return "HipparcosObservationSource.pdf";
	}

	class HipparcosFileReader extends AbstractObservationRetriever {

		private State state;
		private SeriesType series;

		public HipparcosFileReader() {
			super(getVelaFilterStr());
			state = State.HEADER; // Start out assuming a header ...
			series = SeriesType.create("Hipparcos", "Hipparcos", Color.ORANGE,
					false, false);
			this.setBarycentric(true);
		}

		@Override
		public String getSourceName() {
			return getInputName();
		}

		@Override
		public String getSourceType() {
			return "Hipparcos File";
		}

		@Override
		public String getTimeUnits() {
			return "BJD(TT)";
		}

		@Override
		public void retrieveObservations() throws ObservationReadError,
				InterruptedException {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					getInputStreams().get(0)));
			String line = null;

			int lineNum = 1;
			line = getNextLine(reader, lineNum); // Fetch the first line

			while (line != null) {
				// State machine cases ...
				switch (state) {
				case HEADER:
					if (line.startsWith("JD") || line.startsWith("HT1")) {
						state = State.DATA;
					}
					break;

				case DATA:
					handleData(line, lineNum);
					lineNum++;
					break;
				}
				line = getNextLine(reader, lineNum); // Fetch next line
			}
		}

		// ** Helper methods **

		private void handleData(String line, int lineNum)
				throws ObservationReadError {

			// Hipparcos makes use of the Barycentric Julian Date (BJD), as
			// follows -
			// Observation epochs (the JDs in field HT1) are given in
			// Terrestrial Time (TT), offset with respect to the Reduced
			// Julian Date (RJD) 2400000.0, and further offset by 40000, to keep
			// the field size to a minimum. They have been corrected for light
			// propogation time to the solar system barycentre, and are
			// therefore referred to as BJD(TT).

			String[] fields = line.split("\\|");
			
			try {
				double bjd = Double.parseDouble(fields[0].trim()) + 40000 + 2400000.0;
				double mag = Double.parseDouble(fields[1].trim());
				double magErr = Double.parseDouble(fields[2].trim());
				String flags = fields[3].trim();

				ValidObservation ob = new ValidObservation();
				ob.setName(getInputName());
				ob.setDateInfo(new DateInfo(bjd));
				ob.setMagnitude(new Magnitude(mag, magErr));
				ob.setBand(series);
				ob.setRecordNumber(lineNum);
				ob.addDetail("FLAGS", flags, "Flags");
				collectObservation(ob);

			} catch (Exception e) {
				// Create an invalid observation. Record the line number
				// rather than the observation number for error reporting
				// purposes, but still increment the latter.
				String error = e.getLocalizedMessage();
				InvalidObservation ob = new InvalidObservation(line, error);
				ob.setRecordNumber(lineNum);
				addInvalidObservation(ob);
			}
			

		}

		private String getNextLine(BufferedReader reader, int lineNum) {

			// Reads the next line from the input file ...

			String line = null;
			try {
				line = reader.readLine();

			} catch (Exception e) {
				// Create an invalid observation. Record the line number
				// rather than the observation number for error reporting
				// purposes, but still increment the latter.
				String error = e.getLocalizedMessage();
				InvalidObservation ob = new InvalidObservation(line, error);
				ob.setRecordNumber(lineNum);
				addInvalidObservation(ob);
			}
			return line;
		}
	}
}
