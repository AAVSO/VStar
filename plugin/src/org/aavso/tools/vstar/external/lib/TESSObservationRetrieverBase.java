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

/**
 *
 * Based on C. Kotnik's and PMAK's KeplerFITSObservationSource code
 * 2018-2023
 * 
*/

package org.aavso.tools.vstar.external.lib;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.aavso.tools.vstar.data.DateInfo;
import org.aavso.tools.vstar.data.InvalidObservation;
import org.aavso.tools.vstar.data.Magnitude;
import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.data.ValidObservation.JDflavour;
import org.aavso.tools.vstar.exception.ObservationReadError;
import org.aavso.tools.vstar.input.AbstractObservationRetriever;
import org.aavso.tools.vstar.plugin.ObservationSourcePluginBase;
import org.aavso.tools.vstar.ui.mediator.StarInfo;
import org.apache.commons.math.stat.descriptive.rank.Median;

import nom.tam.fits.BasicHDU;
import nom.tam.fits.BinaryTableHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.ImageHDU;

public abstract class TESSObservationRetrieverBase extends AbstractObservationRetriever {

	public static final double INVALID_MAG = 99.99;
	
	public enum BinaryTableFieldType {
		UNKNOWN,
		TIME,
		FLUX,
		FLUX_ERROR,
		QUALITY_FLAGS
	}
	
	@SuppressWarnings("serial")
	public class ObservationReadErrorFITS extends Exception {
		
		private double time;
		private double flux;
		private double flux_error;
		
		public ObservationReadErrorFITS(String message, double time, double flux, double flux_error) {
			super(message);
			this.time = time;
			this.flux = flux;
			this.flux_error = flux_error;
		}

		public double getTime() {
			return time;
		}
		
		public double getFlux() {
			return flux;
		}
		
		public double getFluxError() {
			return flux_error;
		}
	}
	
	private class RawObservationData {
		int row;
		double time;
		double intensity;
		double error;
		Integer quality;
	}
	
	private List<BasicHDU[]> hdusList = null;

	private String objName = null;
	
	private ObservationSourcePluginBase hostPlugin;
	
	public TESSObservationRetrieverBase(ObservationSourcePluginBase hostPlugin) {
		super(hostPlugin.getVelaFilterStr());
		this.hostPlugin = hostPlugin;
		this.hdusList = new ArrayList<BasicHDU[]>();
	}
	
	/**
	 * 
	 * @param hdus
	 * @return false if the FITS structure does not match the source
	 */
	public abstract boolean validateFITS(BasicHDU[] hdus);

	/**
	 * 
	 * @param hdus
	 * @return the series type
	 */
	public abstract SeriesType getSeriesType(BasicHDU[] hdus);

	/**
	 * 
	 * @param hdus
	 * @return reference magnitude from the fits header
	 */
	public abstract double getRefMagnitude(BasicHDU[] hdus);

	/**
	 * 
	 * @param hdus
	 * @return magnitude description
	 */
	public abstract String getRefMagnitudeDescription(BasicHDU[] hdus);
	
	/**
	 * 
	 * @param hdus
	 * @return reference epoch
	 */
	public abstract Double getTimeRef(BasicHDU[] hdus);

	/**
	 * 
	 * @param hdus
	 * @return column index for the specified column type
	 */
	public abstract int getColumnIndex(BasicHDU[] hdus, BinaryTableFieldType field);
	
	@Override
	public void retrieveObservations() throws ObservationReadError,
			InterruptedException {
				
		setJDflavour(JDflavour.BJD);
		try {
            // BasicHDU initialization in getNumberOfRecords
		    for (BasicHDU[] hdus : hdusList) {
    	        retrieveObservations(hdus);
		    }
		} catch (Exception e) {
			throw new ObservationReadError(e.getLocalizedMessage());
		}
	}
	
	private void retrieveObservations(BasicHDU[] hdus)
			throws FitsException, ObservationReadError {

		if (!validateFITS(hdus)) {
		    String msg =
                    String.format("Not a valid FITS file: %s",
                    getSourceName());
            throw new ObservationReadError(msg);		}
		
		// KEPLER, TESS, QLP and LightKurve FITS
		if (hdus.length > 1 && hdus[0] instanceof ImageHDU && hdus[1] instanceof BinaryTableHDU) {
			
			// Lists to store observations before median level adjust
			List<RawObservationData> rawObsList = new ArrayList<RawObservationData>();
			List<InvalidObservation> invalidObsList = new ArrayList<InvalidObservation>();

			ImageHDU imageHDU = (ImageHDU)hdus[0];
			
			objName = imageHDU.getObject();
			SeriesType seriesType = getSeriesType(hdus);
			double refMag = getRefMagnitude(hdus);
			String refMagDescription = getRefMagnitudeDescription(hdus);
			
			BinaryTableHDU tableHDU = (BinaryTableHDU) hdus[1];
			
			int timeColumn        = getColumnIndex(hdus, BinaryTableFieldType.TIME);
			int fluxColumn        = getColumnIndex(hdus, BinaryTableFieldType.FLUX); 
			int fluxErrColumn     = getColumnIndex(hdus, BinaryTableFieldType.FLUX_ERROR); 
			int qalityFlagsColumn = getColumnIndex(hdus, BinaryTableFieldType.QUALITY_FLAGS);
			
			Double timeRef = getTimeRef(hdus);
			if (timeRef == null) {
				throw new ObservationReadError("Cannot find determine reference epoch");
			}

			for (int row = 0; row < tableHDU.getNRows()	&& !wasInterrupted(); row++) {
				try {
					double barytime = ((double[]) tableHDU.getElement(row, timeColumn))[0];
					double bjd = barytime + timeRef;
					
					float flux = ((float[]) tableHDU.getElement(row, fluxColumn))[0];
					float flux_err = 0;
					if (fluxErrColumn >= 0) {
						flux_err = ((float[]) tableHDU.getElement(row, fluxErrColumn))[0];
					}
					Integer qualityFlags = null;
					if (qalityFlagsColumn >= 0) {
						qualityFlags = ((int[]) tableHDU.getElement(row, qalityFlagsColumn))[0];
					}

					if (!Float.isInfinite(flux)	&& 
						!Float.isInfinite(flux_err)	&& 
						!Float.isNaN(flux) && 
						!Float.isNaN(flux_err) && 
						(flux > 0))
					{
						RawObservationData rawObs = new RawObservationData();
						rawObs.row = row;
						rawObs.time = bjd;
						rawObs.intensity = flux;
						rawObs.error = flux_err;
						rawObs.quality = qualityFlags;
						
						rawObsList.add(rawObs);							
					} else {
						throw new ObservationReadErrorFITS("Invalid flux or flux error", bjd, flux, flux_err);
					}
				} catch (Exception e) {
					String input;
					if (e instanceof ObservationReadErrorFITS) {
						input = String.format(Locale.ENGLISH, "Time = %f, Flux = %f, Flux error = %f", 
								((ObservationReadErrorFITS)e).getTime(), 
								((ObservationReadErrorFITS)e).getFlux(), 
								((ObservationReadErrorFITS)e).getFluxError());
					} else {
						input = "";
					}
					String error = e.getLocalizedMessage();
					InvalidObservation ob = new InvalidObservation(input, error);
					ob.setRecordNumber(row);
					invalidObsList.add(ob);
				}
			}
			
			// Calculating magShift (median of all points)
			double magShift = 15.0; // arbitrary value
			if (refMag != INVALID_MAG) {
				double flux[] = new double[rawObsList.size()];
				for (int i = 0; i < rawObsList.size(); i++) {
					flux[i] = rawObsList.get(i).intensity;
				}
				Median median = new Median();
				double median_flux = median.evaluate(flux);
				double median_inst_mag = -2.5 * Math.log10(median_flux);
				magShift = refMag - median_inst_mag;
			}
			
			for (RawObservationData rawObs : rawObsList) {
				double mag = magShift - 2.5 * Math.log10(rawObs.intensity);
				double magErr = 1.086 * rawObs.error / rawObs.intensity;
				
				ValidObservation ob = new ValidObservation();
				if (objName != null && !"".equals(objName.trim())) {
					ob.setName(objName);
				} else {
					ob.setName(hostPlugin.getInputName());
				}
				ob.setDateInfo(new DateInfo(rawObs.time));
				ob.setMagnitude(new Magnitude(mag, magErr));
				ob.setBand(seriesType);
				ob.setRecordNumber(rawObs.row);
				if (refMagDescription != null && refMag != INVALID_MAG) {
					ob.addDetail("HEADER_MAG", refMag, refMagDescription);
				};
				ob.addDetail("FLUX", rawObs.intensity, "Flux");
				if (qalityFlagsColumn >= 0 && rawObs.quality != null) {
					ob.addDetail("QUALITY",	rawObs.quality, "Quality");
				}
				collectObservation(ob);
				incrementProgress();
			}

			for (InvalidObservation ob : invalidObsList) {
				addInvalidObservation(ob);
				incrementProgress();
			}
		} else {
		    String msg =
                    String.format("Not a valid FITS file: %s",
                    getSourceName());
            throw new ObservationReadError(msg);		}
	}
	
	@Override
	public Integer getNumberOfRecords() throws ObservationReadError {

	    hdusList.clear();
	    
	    for (InputStream fitsStream : hostPlugin.getInputStreams()) {
			try {
				Fits fits = new Fits(fitsStream);
				BasicHDU[] hdus = fits.read();
	            if (hdus.length > 1 && hdus[1] instanceof BinaryTableHDU) {
	                hdusList.add(hdus);
	            } else {
	                String msg =
	                        String.format("Not a valid FITS file: %s",
	                        getSourceName());
	                throw new ObservationReadError(msg);
	            }
			} catch (Exception e) {
				throw new ObservationReadError(e.getLocalizedMessage());
			}
	    }

	    int records = 0;
	    for (BasicHDU[] hdus : hdusList) {
            BinaryTableHDU tableHDU = (BinaryTableHDU) hdus[1];
            records += tableHDU.getNRows();
	    }

	    return records;
	}

	@Override
	public String getSourceName() {
		return hostPlugin.getInputName();
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
