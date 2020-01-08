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
import java.util.Locale;

import nom.tam.fits.BasicHDU;
import nom.tam.fits.BinaryTableHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;

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

//01/06/2020 C. Kotnik Generalized logic to calculate BJD to include TESS by getting
//                     offset from FITS header.
/**
 * A Kepler FITS file v2.0 observation source plug-in that uses the
 * Topcat FITS library.
 * 
 * See also:<br/>
 * o http://archive.stsci.edu/mast_news.php?out=html&desc=t&id=392<br/>
 * o http://archive.stsci.edu/kepler/manuals/ArchiveManualNewFormat.pdf<br/>
 * o http://archive.stsci.edu/kepler/manuals/KDMC-10008-001_Archive_Manual.pdf<br/>
 * 
 * In section 2.1.2 of the archive manual ("Kepler Time System"), we have:
 * <p>
 *"Time is specified in the data files with an offset from BJD, either
 * BJD-2400000.0 (light curve files) or BJD-2454833.0 (target pixel files)."
 * </p>
 * This is at odds with the header comments in v2.0 light curve files. The
 * correct time base for light curve data appears to be 2454833.0. Thanks to
 * Doug Welch for helping me tracking this down who also suggests that the
 * BJD-2400000.0 reference was probably intended to be MJD and 2400000.5.
 */
public class KeplerFITSObservationSource extends ObservationSourcePluginBase {

	private final SeriesType keplerSeries;

	private Locale locale;

	public KeplerFITSObservationSource() {
		super();
		keplerSeries = SeriesType
				.create("Kepler", "Kepler", Color.GREEN, false, false);
		locale = Locale.getDefault();
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
		String str = "Kepler/TESS FITS file v2.0 observation source";

		if (locale.equals("es")) {
			str = "Observaciones de archivo FITS de Kepler/TESS v2.0 del plug-in que usa la biblioteca Topcat FITS.";
		}

		return str;
	}

	@Override
	public String getDisplayName() {
		String str = "New Star from Kepler/TESS FITS File v2.0...";

		if (locale.equals("es")) {
			str = "Nueva estrella de archivo FITS de Kepler/TESS...";
		}

		return str;
	}

	@Override
	public List<String> getAdditionalFileExtensions() {
		List<String> extensions = new ArrayList<String>();
		extensions.add("fits");
		return extensions;
	}

	@Override
	public AbstractObservationRetriever getObservationRetriever() {
		return new KeplerFITSObservationRetriever();
	}

	class KeplerFITSObservationRetriever extends AbstractObservationRetriever {
				
		@Override
		public void retrieveObservations() throws ObservationReadError,
				InterruptedException {
			setBarycentric(true);
			try {
				Fits fits = new Fits(getInputStreams().get(0));
				BasicHDU[] hdus = fits.read();
				retrieveKeplerObservations(hdus);
			} catch (FitsException e) {
				throw new ObservationReadError(e.getLocalizedMessage());
			}
		}

		// Collect observations from the table excluding those with nonsensical
		// flux values.
		private void retrieveKeplerObservations(BasicHDU[] hdus)
				throws FitsException, ObservationReadError {

			double minMagErr = Double.MAX_VALUE;
			double maxMagErr = Double.MIN_VALUE;

			for (BasicHDU hdu : hdus) {
				if (hdu instanceof BinaryTableHDU) {
					BinaryTableHDU tableHDU = (BinaryTableHDU) hdu;
					double timei = tableHDU.getHeader().getDoubleValue("BJDREFI");
					double timef = tableHDU.getHeader().getDoubleValue("BJDREFF");
					
					for (int row = 0; row < tableHDU.getNRows()
							&& !wasInterrupted(); row++) {
						try {
							double barytime = ((double[]) tableHDU.getElement(
									row, 0))[0];
							float ap_corr_flux = ((float[]) tableHDU
									.getElement(row, 7))[0];
							float ap_corr_err = ((float[]) tableHDU.getElement(
									row, 8))[0];

							// Include only valid magnitude fluxes.
							// Question: why do we see such values in Kepler
							// data sets?
							if (!Float.isInfinite(ap_corr_flux)
									&& !Float.isInfinite(ap_corr_err)
									&& !Float.isNaN(ap_corr_flux)
									&& !Float.isNaN(ap_corr_err)) {

								//double hjd = barytime + 2454833.0;
								double hjd = barytime + timei + timef;
								double mag = 15.0 - 2.5
										* Math.log(ap_corr_flux)
										/ Math.log(10.0);

								double magErr = 1.086 * ap_corr_err
										/ ap_corr_flux;

								if (magErr < minMagErr) {
									minMagErr = magErr;
								} else if (magErr > maxMagErr) {
									maxMagErr = magErr;
								}

								ValidObservation ob = new ValidObservation();
								ob.setName(getInputName());
								ob.setDateInfo(new DateInfo(hjd));
								ob.setMagnitude(new Magnitude(mag, magErr));
								ob.setBand(keplerSeries);
								ob.setRecordNumber(row);
								collectObservation(ob);
							}
						} catch (Exception e) {
							String input = tableHDU.getRow(row).toString();
							String error = e.getLocalizedMessage();
							InvalidObservation ob = new InvalidObservation(
									input, error);
							ob.setRecordNumber(row);
							addInvalidObservation(ob);
						}
					}
				}
			}
		}

		@Override
		public String getSourceName() {
			return getInputName();
		}

		@Override
		public String getSourceType() {
			String str = "Kepler/TESS FITS File";
			
			if (locale.equals("es")) {
				str = "De archivo FITS de Kepler/TESS";
			}

			return str;
		}
	}
}
