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

/**
 * DASCHObservationSource is a VStar observation source plug-in tool which reads
 * DASCH (Digital Access to a Sky Century @ Harvard) data as described at URL
 * http://....., from a supplied input file in tab-separated .txt format.
 * 
 * The input file for a particular star must first be extracted by ... 
 * 2-line header ...
 * Something
 * about the differences in ID (eg. SV* HV 873 versus APASS_J045423.3-705406)
 * Does DASCH make use of the Heliocentric Julian Date (HJD)? For more
 * information see: http://www.
 * 
 * @author Paul F. York
 * @version 1.0 - 06 Sep 2014: Original
 */

public class DASCHObservationSource extends ObservationSourcePluginBase {

	@Override
	public InputType getInputType() {
		return InputType.FILE;
	}

	@Override
	public AbstractObservationRetriever getObservationRetriever() {
		return new DASCHFileReader();
	}

	@Override
	public String getDescription() {
		return "DASCH file reader";
	}

	@Override
	public String getDisplayName() {
		return "New Star from DASCH File...";
	}

	class DASCHFileReader extends AbstractObservationRetriever {

		private SeriesType series;
		String starName = "      ";

		public DASCHFileReader() {

			series = SeriesType.create("DASCH", "DASCH", Color.PINK, false,
					false);
			this.setHeliocentric(true);
		}

		@Override
		public String getSourceName() {
			return starName;
		}

		@Override
		public String getSourceType() {
			return "DASCH File";
		}

		@Override
		public String getTimeUnits() {
			return "HJD";
		}

		@Override
		public void retrieveObservations() throws ObservationReadError,
				InterruptedException {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					getInputStreams().get(0)));
			
			String line = null;
			int headerLines = 2;	// Number of header lines in source file
			int lineNum = 1;
			
			for (int i = 1; i <= headerLines + 1; i++) {
				line = getNextLine(reader, lineNum); // Discard the 2-line header, read the first line of data
			}
			while (line != null) {

				handleData(line, lineNum);
				lineNum++;
				line = getNextLine(reader, lineNum); // Fetch next line
			}
		}

		// ** Helper methods **

		private void handleData(String line, int lineNum)
				throws ObservationReadError {

			// DASCH makes use of the Heliocentric Julian Date (HJD)

			String[] fields = line.split("\\t");
			Double magErrThreshold = 99.0;  // Obs with magErr of 99 to be excluded
			
			if (lineNum == 1) {
				starName = fields[0];  // Get star designation from data line 1 ...
				getSourceName();
			}
			double hjd = Double.parseDouble(fields[1].trim());
			double mag = Double.parseDouble(fields[3].trim());
			double magErr = Double.parseDouble(fields[4].trim());
			
			String limitingMag = fields[5].trim();
			String RA = fields[6].trim();
			String Dec = fields[7].trim();
			String thetaJ2000 = fields[8].trim();
			String ellipticity = fields[9].trim();
			String plateNum = fields[10];
			String versionID = fields[11];
			String flags = fields[12].trim();

			ValidObservation ob = new ValidObservation();
			ob.setDateInfo(new DateInfo(hjd));
			ob.setMagnitude(new Magnitude(mag, magErr));
			if (magErr >= magErrThreshold) {
				ob.setExcluded(true);
			}
			ob.setBand(series);
			ob.setRecordNumber(lineNum);
			
			ob.addDetail("LIMITING MAG", limitingMag, "LImiting Mag");
			ob.addDetail("RA", RA, "RA");
			ob.addDetail("DEC", Dec, "Dec");
			ob.addDetail("THETA J2000", thetaJ2000, "Theta J2000");
			ob.addDetail("ELLIPTICITY", ellipticity, "Ellipticity");
			ob.addDetail("PLATE", plateNum, "Plate");
			ob.addDetail("VERSION ID", versionID, "Version ID");
			ob.addDetail("FLAGS", flags, "Flags");
			collectObservation(ob);
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