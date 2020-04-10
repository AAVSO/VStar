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
import java.util.ArrayList;
import java.util.HashMap;
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
import org.aavso.tools.vstar.ui.model.plot.JDTimeElementEntity;
import org.aavso.tools.vstar.util.stats.DescStats;
//12/02/2018 C. Kotnik added name to observations so they can be
//saved and reloaded from a file.
/**
 * Observation source for IRIS Automatic Photometry file format.
 * 
 * @author David Benn
 * @version 1.0 - 18 Feb 2015
 */

public class IRISAutomaticPhotometryObservationSource extends
		ObservationSourcePluginBase {

	enum State {
		// Reading a line from the header ...
		HEADER,
		// Reading a line of data ...
		DATA
	};

	private final static Color[] COLORS = { Color.RED, Color.ORANGE,
			Color.GREEN, Color.BLUE, Color.MAGENTA, Color.LIGHT_GRAY,
			Color.CYAN, Color.YELLOW, Color.BLACK };

	@Override
	public InputType getInputType() {
		return InputType.FILE;
	}

	@Override
	public AbstractObservationRetriever getObservationRetriever() {
		return new IRISAutomaticPhotometryFileReader();
	}

	@Override
	public String getDescription() {
		return "IRIS Automatic Photometry file reader";
	}

	@Override
	public String getDisplayName() {
		return "New Star from IRIS Automatic Photometry File...";
	}

	class IRISAutomaticPhotometryFileReader extends
			AbstractObservationRetriever {

		private State state;
		private Map<Integer, SeriesType> seriesMap;
		private Map<SeriesType, List<ValidObservation>> seriesToObsMap;
		private int seriesColor;

		public IRISAutomaticPhotometryFileReader() {
			super(getVelaFilterStr());
			state = State.HEADER;
			seriesMap = new HashMap<Integer, SeriesType>();
			seriesToObsMap = new HashMap<SeriesType, List<ValidObservation>>();
			seriesColor = 0;
		}

		@Override
		public String getSourceName() {
			return getInputName();
		}

		@Override
		public String getSourceType() {
			return "IRIS Automatic Photometry File";
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
					handleHeader(line);
					state = State.DATA;
					break;

				case DATA:
					handleData(line, lineNum);
					lineNum++;
					break;
				}
				line = getNextLine(reader, lineNum); // Fetch next line
			}

			// Iterate over series, add magnitude error, collect observations.
			for (SeriesType type : seriesToObsMap.keySet()) {
				List<ValidObservation> obs = seriesToObsMap.get(type);
				double[] means = DescStats.calcMagMeanInRange(obs,
						JDTimeElementEntity.instance, 0, obs.size() - 1);
				for (ValidObservation ob : obs) {
					double err = Math.abs(ob.getMag()
							- means[DescStats.MEAN_MAG_INDEX]);
					ob.setMagnitude(new Magnitude(ob.getMag(), err));
					collectObservation(ob);
				}

			}
		}

		// ** Helper methods **

		private void handleHeader(String line) {
			// Assume: # series-name-1, series-name-2, ..., series-name-N
			if (line.startsWith("#")) {
				line = line.substring(1);
				String[] fields = line.split(",");
				for (int i = 0; i < fields.length; i++) {
					String name = fields[i];
					SeriesType seriesType = SeriesType.create(name, name,
							COLORS[seriesColor++], false, false);
					if (seriesColor == COLORS.length) {
						seriesColor = 0;
					}

					// Use same index start value as shown in handleData().
					seriesMap.put(i + 1, seriesType);
				}
			}
		}

		private void handleData(String line, int lineNum)
				throws ObservationReadError {

			// A single line may contain observations from N targets of the
			// form:
			//
			// JD val1 val2 .. valN
			//
			// where values are instrumental magnitudes from a camera and JD
			// corresponds to the UT start time of the observation.
			//
			// The delimiter is multiple spaces.

			String[] fields = line.split("\\s+");

			double jd = Double.parseDouble(fields[0].trim());

			// Add to map of observations for each series, deferring computation
			// of error (standard deviation) until we have all observations.
			for (int i = 1; i < fields.length; i++) {
				List<ValidObservation> obsForSeries = null;

				SeriesType type = getSeries(i);

				if (seriesToObsMap.containsKey(type)) {
					obsForSeries = seriesToObsMap.get(type);
				} else {
					obsForSeries = new ArrayList<ValidObservation>();
					seriesToObsMap.put(type, obsForSeries);

				}

				double mag = Double.parseDouble(fields[i].trim());
				ValidObservation ob = new ValidObservation();
				ob.setName(getInputName());
				ob.setDateInfo(new DateInfo(jd));
				ob.setMagnitude(new Magnitude(mag, 0));
				ob.setRecordNumber(lineNum);
				ob.setBand(getSeries(i));

				obsForSeries.add(ob);
			}
		}

		private SeriesType getSeries(Integer index) {
			SeriesType seriesType = null;

			if (seriesMap.containsKey(index)) {
				seriesType = seriesMap.get(index);
			} else {
				String seriesName = "IRIS-" + index;
				seriesType = SeriesType.create(seriesName, seriesName,
						COLORS[seriesColor++], false, false);
				if (seriesColor == COLORS.length) {
					seriesColor = 0;
				}
				seriesMap.put(index, seriesType);
			}

			return seriesType;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.aavso.tools.vstar.plugin.ObservationSourcePluginBase#
	 * getAdditionalFileExtensions()
	 */
	@Override
	public List<String> getAdditionalFileExtensions() {
		List<String> xtns = new ArrayList<String>();

		xtns.add(".lst");

		return xtns;
	}
}