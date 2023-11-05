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

import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.external.lib.TESSObservationRetrieverBase;
import org.aavso.tools.vstar.input.AbstractObservationRetriever;
import org.aavso.tools.vstar.plugin.InputType;
import org.aavso.tools.vstar.plugin.ObservationSourcePluginBase;

import nom.tam.fits.BasicHDU;
import nom.tam.fits.BinaryTableHDU;
import nom.tam.fits.ImageHDU;

// PMAK, 2020-APR-24
// 1) LightKurve FITS loader based on KEPLER/TESS plugin version 0.2
// PMAK, 2020-SEP-09
// 1) Added common VeLa filter support
// PMAK, 2023-03-28
// 1) LightKurveFITSObservationRetriever inherits TESSObservationRetrieverBase

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
		return "Lightkurve output observation source v0.4";
	}

	@Override
	public String getDisplayName() {
		return "New Star from Lightkurve FITS File v0.4...";
	}

	/**
	 * @see org.aavso.tools.vstar.plugin.IPlugin#getDocName()
	 */
	@Override
	public String getDocName() {
		return "Lightkurve_FITS_observation_source.pdf";
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

	class LightKurveFITSObservationRetriever extends TESSObservationRetrieverBase {

		public LightKurveFITSObservationRetriever() {
			super(LightKurveFITSObservationSource.this);
		}

		@Override
		public boolean validateFITS(BasicHDU[] hdus) {
			if (!(hdus.length > 1 && hdus[0] instanceof ImageHDU && hdus[1] instanceof BinaryTableHDU))
				return false;
			BinaryTableHDU tableHDU = (BinaryTableHDU) hdus[1];
			if (!"TIME".equals(tableHDU.getColumnName(0)) ||
				!"FLUX".equals(tableHDU.getColumnName(1)) ||
				!"FLUX_ERR".equals(tableHDU.getColumnName(2))) {
				return false;
			}
			return true;
		}
		
		public SeriesType getSeriesType(BasicHDU[] hdus) {
			ImageHDU infohdu = (ImageHDU)hdus[0];
			String telescope = infohdu.getTelescope();
			if ("TESS".equals(telescope)) {
				return dataSeriesLightKurveTESS;
			}
			if ("Kepler".equals(telescope)) {
				return dataSeriesLightKurveKepler;
			}
			return dataSeriesLightKurve;
		}
		
		@Override
		public double getRefMagnitude(BasicHDU[] hdus) {
			return INVALID_MAG;			
		}
		
		@Override
		public String getRefMagnitudeDescription(BasicHDU[] hdus) {
			return null;
		}
		
		@Override
		public Double getTimeRef(BasicHDU[] hdus) {
			ImageHDU infohdu = (ImageHDU)hdus[0];
			String telescope = infohdu.getTelescope();
			if ("TESS".equals(telescope)) {
				return 2457000.0;
			}
			if ("Kepler".equals(telescope)) {
				return 2454833.0;
			}
			return null;
		}
		
		@Override
		public int getColumnIndex(BasicHDU[] hdus, BinaryTableFieldType field) {
			if (field == BinaryTableFieldType.TIME)
				return 0;
			if (field == BinaryTableFieldType.FLUX)
				return 1;
			if (field == BinaryTableFieldType.FLUX_ERROR)
				return 2;
			return -1;
		}
		
		@Override
		public String getSourceType() {
			return "Lightkurve FITS File";
		}
		
	}		
	
}