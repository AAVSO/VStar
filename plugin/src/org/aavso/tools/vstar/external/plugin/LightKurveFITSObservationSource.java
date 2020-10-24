/**
 * VStar: a statistical analysis tool for variable star data.
 * Copyright (C) 2009  AAVSO (http://www.aavso.org/)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed inputStream the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */
package org.aavso.tools.vstar.external.plugin;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.aavso.tools.vstar.data.DateInfo;
import org.aavso.tools.vstar.data.InvalidObservation;
import org.aavso.tools.vstar.data.Magnitude;
import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.exception.ObservationReadError;
import org.aavso.tools.vstar.input.AbstractObservationRetriever;
import org.aavso.tools.vstar.plugin.InputType;
import org.aavso.tools.vstar.plugin.ObservationSourcePluginBase;
import org.aavso.tools.vstar.ui.mediator.StarInfo;

import nom.tam.fits.BasicHDU;
import nom.tam.fits.BinaryTableHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.ImageHDU;

// PMAK, 2020-APR-24
// 1) LightKurve FITS loader based on KEPLER/TESS plugin version 0.2
// PMAK, 2020-SEP-09
// 1) Added common VeLa filter support

/**
 * LightKurve FITS loader
 */
public class LightKurveFITSObservationSource extends ObservationSourcePluginBase {

	private SeriesType dataSeriesLightKurve;
	private SeriesType dataSeriesLightKurveKepler;
	private SeriesType dataSeriesLightKurveTESS;
	
	//private Locale locale;
	
	public LightKurveFITSObservationSource() {
		super();
		dataSeriesLightKurveKepler  = SeriesType.create("Lightkurve Kepler (arbitrary mag)", "Lightkurve Kepler", Color.DARK_GRAY, false, false);
		dataSeriesLightKurveTESS    = SeriesType.create("Lightkurve TESS (arbitrary mag)", "Lightkurve TESS", Color.MAGENTA, false, false);
		dataSeriesLightKurve        = SeriesType.create("Lightkurve (arbitrary mag)", "Lightkurve", Color.PINK, false, false);
	}

	@Override
	public String getCurrentStarName() {
		return getInputName();
	}

	@Override
	public InputType getInputType() {
		return InputType.FILE_OR_URL;
	}

	@Override
	public String getDescription() {
		return "Lightkurve output observation source v0.2";
	}

	@Override
	public String getDisplayName() {
		return "New Star from Lightkurve FITS File v0.2...";
	}

	@Override
	public List<String> getAdditionalFileExtensions() {
		List<String> extensions = new ArrayList<String>();
		extensions.add("fits");
		return extensions;
	}

	@Override
	public AbstractObservationRetriever getObservationRetriever() {
		return new LightKurveFITSObservationRetriever();
	}

	private class RawObservationData {
		int row;
		double time;
		double intensity;
		double error;
	}

	class LightKurveFITSObservationRetriever extends AbstractObservationRetriever {

		private BasicHDU[] hdus = null;
		
		private String objName = null;
		
		public LightKurveFITSObservationRetriever() {
			super(getVelaFilterStr());
		}

		@Override
		public void retrieveObservations() throws ObservationReadError,
				InterruptedException {
					
			setBarycentric(true);
			try {
				// BasicHDU initialization moved to getNumberOfRecords
				retrieveLightKurveObservations(hdus);
			} catch (Exception e) {
				throw new ObservationReadError(e.getLocalizedMessage());
			}
		}

		// Collect observations from the table
		private void retrieveLightKurveObservations(BasicHDU[] hdus)
				throws FitsException, ObservationReadError {

			// LightKurve FITS contains primary HDU having keywords only, binary table extension, and image extension (aperture).
			if (hdus.length > 1 && hdus[0] instanceof ImageHDU && hdus[1] instanceof BinaryTableHDU) {

				// Lists to store observations before median level adjust
				List<RawObservationData> rawObsList = new ArrayList<RawObservationData>();
				List<InvalidObservation> invalidObsList = new ArrayList<InvalidObservation>();

				ImageHDU imageHDU = (ImageHDU) hdus[0];
				String telescope = imageHDU.getTelescope();
				
				SeriesType lightKurveSeries = dataSeriesLightKurve;
				
				BinaryTableHDU tableHDU = (BinaryTableHDU) hdus[1];
				
				double timeConstant = 0;
				if ("TESS".equals(telescope)) {
					lightKurveSeries = dataSeriesLightKurveTESS;
					timeConstant = 2457000;
				}
				if ("Kepler".equals(telescope)) {
					lightKurveSeries = dataSeriesLightKurveKepler;
					timeConstant = 2454833;
				}
				
				for (int row = 0; row < tableHDU.getNRows()	&& !wasInterrupted(); row++) {
					try {
						double barytime = ((double[]) tableHDU.getElement(row, 0))[0];
						float flux      = ((float[]) tableHDU.getElement(row, 1))[0];
						float flux_err  = ((float[]) tableHDU.getElement(row, 2))[0];

						if (!Float.isInfinite(flux)
								&& !Float.isInfinite(flux_err)
								&& !Float.isNaN(flux)
								&& !Float.isNaN(flux_err)) {

							double bjd = barytime + timeConstant;
							// Store data to temporary list
							RawObservationData rawObs = new RawObservationData();
							rawObs.row = row;
							rawObs.time = bjd;
							rawObs.intensity = flux;
							rawObs.error = flux_err;
							rawObsList.add(rawObs);
						}
					} catch (Exception e) {
						String input = tableHDU.getRow(row).toString();
						String error = e.getLocalizedMessage();
						InvalidObservation ob = new InvalidObservation(input, error);
						ob.setRecordNumber(row);
						// Store observation in a temporary list
						//addInvalidObservation(ob);
						invalidObsList.add(ob);
					}
				}

				// Calculating magShift (median of all points)
				double magShift = 15.0; // arbitrary value
				
				// There was code in Kepler/TESS plugin to determine zero level.
				// There is no info in LightKurve header to do such adjustment.
				
				for (RawObservationData rawObs : rawObsList) {
					double mag = magShift - 2.5 * Math.log10(rawObs.intensity);
					double magErr = 1.086 * rawObs.error / rawObs.intensity;

					ValidObservation ob = new ValidObservation();
					if (objName != null && !"".equals(objName.trim())) {
						ob.setName(objName);
					} else {
						ob.setName(getInputName());
					}
					ob.setDateInfo(new DateInfo(rawObs.time));
					ob.setMagnitude(new Magnitude(mag, magErr));
					ob.setBand(lightKurveSeries);
					ob.setRecordNumber(rawObs.row);
					collectObservation(ob);
					incrementProgress();
				}

				for (InvalidObservation ob : invalidObsList) {
					addInvalidObservation(ob);
					incrementProgress();
				}
			} else {
				throw new ObservationReadError("Not a valid FITS file");
			}
		}

		@Override
		public Integer getNumberOfRecords() throws ObservationReadError {
			try {
				Fits fits = new Fits(getInputStreams().get(0));
				hdus = fits.read();
				if (hdus.length > 1 && hdus[1] instanceof BinaryTableHDU) {
					BinaryTableHDU tableHDU = (BinaryTableHDU) hdus[1];
					return tableHDU.getNRows();
				} else {
					throw new ObservationReadError("Not a valid FITS file");
				}
			} catch (Exception e) {
				throw new ObservationReadError(e.getLocalizedMessage());
			}
		}
		
		@Override
		public String getSourceName() {
			return getInputName();
		}

		@Override
		public String getSourceType() {
			return "Lightkurve FITS File";
		}

		@Override
		public StarInfo getStarInfo() {

			String name = objName;

			if (name == null || "".equals(name.trim())) {
				name = getSourceName();
			}

			return new StarInfo(this, name);
		}
		
	}
	
}