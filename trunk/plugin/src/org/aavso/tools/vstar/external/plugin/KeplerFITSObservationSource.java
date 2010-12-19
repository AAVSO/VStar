package org.aavso.tools.vstar.external.plugin;

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

/**
 * A Kepler FITS file observation source plug-inputStream that uses the Topcat
 * FITS library.
 */
public class KeplerFITSObservationSource extends ObservationSourcePluginBase {

	public KeplerFITSObservationSource() {
	}

	@Override
	public String getCurrentStarName() {
		return getInputName();
	}

	@Override
	public InputType getInputType() {
		return InputType.FILE;
	}

	@Override
	public AbstractObservationRetriever getObservationRetriever() {
		return new KeplerFITSObservationRetriever();
	}

	@Override
	public String getDescription() {
		return "A Kepler FITS file observation source plug-inputStream that uses the Topcat FITS library.";
	}

	@Override
	public String getDisplayName() {
		return "New Star from Kepler FITS File...";
	}

	class KeplerFITSObservationRetriever extends AbstractObservationRetriever {
		@Override
		public void retrieveObservations() throws ObservationReadError,
				InterruptedException {

			try {
				Fits fits = new Fits(getInputStream());
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

					for (int row = 0; row < tableHDU.getNRows(); row++) {
						try {
							double barytime = ((double[]) tableHDU.getElement(
									row, 0))[0];
							float timcorr = ((float[]) tableHDU.getElement(row,
									1))[0];
							int cadence_number = ((int[]) tableHDU.getElement(
									row, 2))[0];
							double ap_cent_row = ((double[]) tableHDU
									.getElement(row, 3))[0];
							float ap_cent_r_err = ((float[]) tableHDU
									.getElement(row, 4))[0];
							double ap_cent_col = ((double[]) tableHDU
									.getElement(row, 5))[0];
							float ap_cent_c_err = ((float[]) tableHDU
									.getElement(row, 6))[0];
							float ap_raw_flux = ((float[]) tableHDU.getElement(
									row, 7))[0];
							float ap_raw_err = ((float[]) tableHDU.getElement(
									row, 8))[0];
							float ap_corr_flux = ((float[]) tableHDU
									.getElement(row, 9))[0];
							float ap_corr_err = ((float[]) tableHDU.getElement(
									row, 10))[0];
							float ap_ins_flux = ((float[]) tableHDU.getElement(
									row, 11))[0];
							float ap_ins_err = ((float[]) tableHDU.getElement(
									row, 12))[0];
							float dia_raw_flux = ((float[]) tableHDU
									.getElement(row, 13))[0];
							float dia_raw_err = ((float[]) tableHDU.getElement(
									row, 14))[0];
							float dia_corr_flux = ((float[]) tableHDU
									.getElement(row, 15))[0];
							float dia_corr_err = ((float[]) tableHDU
									.getElement(row, 16))[0];
							float dia_ins_flux = ((float[]) tableHDU
									.getElement(row, 17))[0];
							float dia_ins_err = ((float[]) tableHDU.getElement(
									row, 18))[0];

							// For non-infinite magnitude fluxes...
							if (!Float.isInfinite(ap_corr_flux)
									&& !Float.isInfinite(ap_corr_err)) {

								double hjd = barytime + 2400000.5;
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
								ob.setDateInfo(new DateInfo(hjd));
								ob.setMagnitude(new Magnitude(mag, magErr));
								ob.setBand(SeriesType.Unspecified);
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
	}
}
