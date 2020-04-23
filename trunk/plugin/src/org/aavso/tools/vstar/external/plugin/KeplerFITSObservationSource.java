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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import java.awt.Color;
import java.awt.Container;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import nom.tam.fits.BasicHDU;
import nom.tam.fits.ImageHDU;
import nom.tam.fits.BinaryTableHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;

import org.apache.commons.math.stat.descriptive.rank.Median;
import java.lang.reflect.Method;

import org.aavso.tools.vstar.data.DateInfo;
import org.aavso.tools.vstar.data.InvalidObservation;
import org.aavso.tools.vstar.data.Magnitude;
import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.Observation;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.exception.ObservationReadError;
import org.aavso.tools.vstar.input.AbstractObservationRetriever;
import org.aavso.tools.vstar.plugin.InputType;
import org.aavso.tools.vstar.plugin.ObservationSourcePluginBase;
import org.aavso.tools.vstar.ui.mediator.StarInfo;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.dialog.AbstractOkCancelDialog;


//12/02/2018 C. Kotnik added name to observations so they can be
//saved and reloaded from a file.

//01/06/2020 C. Kotnik Generalized logic to calculate BJD to include TESS by getting
//                     offset from FITS header.

// PMAK, 2020-FEB-07
// 1) KEPLER/TESS magnitude adjust: median series value to KEPMAG/TESSMAG value from FITS header.
// 2) Refactoring

// PMAK, 2020-02-22
// 1) Parameter's dialog

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

	//private  final SeriesType keplerSeries;
	private SeriesType dataSeriesKepler;
	private SeriesType dataSeriesTESS;
	private SeriesType dataSeriesMAST;

	private SeriesType dataSeriesKepler_raw;
	private SeriesType dataSeriesTESS_raw;
	private SeriesType dataSeriesMAST_raw;
	
	private Locale locale;
	
	private boolean loadRaw = false;

	public KeplerFITSObservationSource() {
		super();
		dataSeriesMAST       = SeriesType.create("MAST", "MAST", Color.GREEN, false, false);
		dataSeriesKepler     = SeriesType.create("Kepler", "Kepler", Color.GREEN, false, false);
		dataSeriesTESS       = SeriesType.create("TESS", "TESS", Color.GREEN, false, false);

		dataSeriesMAST_raw   = SeriesType.create("MAST raw", "MAST raw", Color.GREEN, false, false);
		dataSeriesKepler_raw = SeriesType.create("Kepler raw", "Kepler raw", Color.GREEN, false, false);
		dataSeriesTESS_raw   = SeriesType.create("TESS raw", "TESS raw", Color.GREEN, false, false);

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
		String str = "Kepler/TESS FITS file v2.2 observation source";

		if (locale.equals("es")) {
			str = "Observaciones de archivo FITS de Kepler/TESS v2.2 del plug-in que usa la biblioteca Topcat FITS.";
		}

		return str;
	}

	@Override
	public String getDisplayName() {
		String str = "New Star from Kepler/TESS FITS File v2.2...";

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

	private class RawObservationData {
		int row;
		double time;
		double intensity;
		double error;
	}

	class KeplerFITSObservationRetriever extends AbstractObservationRetriever {

		private BasicHDU[] hdus = null;
		
		private String objName = null;

		@Override
		public void retrieveObservations() throws ObservationReadError,
				InterruptedException {
					
			FITSParameterDialog paramDialog = new FITSParameterDialog();
			if (paramDialog.isCancelled()) return;

			loadRaw = paramDialog.getLoadRaw();
				
			setBarycentric(true);
			try {
				// BasicHDU initialization moved to getNumberOfRecords
				retrieveKeplerObservations(hdus);
			} catch (Exception e) {
				throw new ObservationReadError(e.getLocalizedMessage());
			}
		}

		// Collect observations from the table excluding those with nonsensical
		// flux values.
		private void retrieveKeplerObservations(BasicHDU[] hdus)
				throws FitsException, ObservationReadError {

			// Kepler and TESS light curve FITS contains primary HDU having keywords only, binary table extension, and image extension (aperture).
			if (hdus.length >  1 && hdus[0] instanceof ImageHDU && hdus[1] instanceof BinaryTableHDU) {

				double minMagErr = Double.MAX_VALUE;
				double maxMagErr = Double.MIN_VALUE;

				double invalidMag = 99.99;

				// Lists to store observations before median level adjust
				List<RawObservationData> rawObsList = new ArrayList<RawObservationData>();
				List<InvalidObservation> invalidObsList = new ArrayList<InvalidObservation>();
				double keplerOrTessMag = invalidMag;

				SeriesType keplerSeries = dataSeriesMAST;
				if (loadRaw) keplerSeries = dataSeriesMAST_raw;

				ImageHDU imageHDU = (ImageHDU) hdus[0];
				// CLK 2020-02-04
				// Find the TELESCOP FITS header and use it to name the band
				// Since this plugin just converts flux to magnitude with an 
				// arbitrary zero point, there is not a band in the normal sense.
				// Do this nameing to allow users to see the correct origin
				String telescope = imageHDU.getTelescope();
				if (telescope == null) {
					telescope = "MAST";
				}
				
				// TESSMAG/KEPMAG from FITS header
				if ("TESS".equals(telescope)) {
					if (loadRaw) keplerSeries = dataSeriesTESS_raw; else keplerSeries = dataSeriesTESS;
					keplerOrTessMag = imageHDU.getHeader().getDoubleValue("TESSMAG", invalidMag);
				}
				if ("Kepler".equals(telescope)) {
					if (loadRaw) keplerSeries = dataSeriesKepler_raw; else keplerSeries = dataSeriesKepler;
					keplerOrTessMag = imageHDU.getHeader().getDoubleValue("KEPMAG", invalidMag);
				}

				objName = imageHDU.getObject();

				BinaryTableHDU tableHDU = (BinaryTableHDU) hdus[1];
				double timei = tableHDU.getHeader().getDoubleValue("BJDREFI");
				double timef = tableHDU.getHeader().getDoubleValue("BJDREFF");
				
				for (int row = 0; row < tableHDU.getNRows()	&& !wasInterrupted(); row++) {
					try {
						double barytime = ((double[]) tableHDU.getElement(row, 0))[0];
						float flux;
						float flux_err;
						if (!loadRaw) {
							flux = ((float[]) tableHDU.getElement(row, 7))[0];
							flux_err = ((float[]) tableHDU.getElement(row, 8))[0];
						} else {
							flux = ((float[]) tableHDU.getElement(row, 3))[0];
							flux_err = ((float[]) tableHDU.getElement(row, 4))[0];
						}

						// Include only valid magnitude fluxes.
						// Question: why do we see such values in Kepler
						// data sets?
						if (!Float.isInfinite(flux)
								&& !Float.isInfinite(flux_err)
								&& !Float.isNaN(flux)
								&& !Float.isNaN(flux_err)) {

							//double hjd = barytime + 2454833.0;
							double hjd = barytime + timei + timef;
							/*
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
							*/
							// Store data to temporary list
							RawObservationData rawObs = new RawObservationData();
							rawObs.row = row;
							rawObs.time = hjd;
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
				if (keplerOrTessMag != invalidMag) {
					double flux[] = new double[rawObsList.size()];
					for (int i = 0; i < rawObsList.size(); i++) {
						flux[i] = rawObsList.get(i).intensity;
					}
					Median median = new Median();
					double median_flux = median.evaluate(flux);
					double median_inst_mag = -2.5 * Math.log10(median_flux);
					magShift = keplerOrTessMag - median_inst_mag;
				}

				for (RawObservationData rawObs : rawObsList) {
					double mag = magShift - 2.5 * Math.log10(rawObs.intensity);
					double magErr = 1.086 * rawObs.error / rawObs.intensity;
					// PMAK: it seems minMagErr and maxMagErr not used?
					if (magErr < minMagErr) {
						minMagErr = magErr;
					} else if (magErr > maxMagErr) {
						maxMagErr = magErr;
					}

					ValidObservation ob = new ValidObservation();
					if (objName != null && !"".equals(objName.trim())) {
						ob.setName(objName);
					} else {
						ob.setName(getInputName());
					}
					ob.setDateInfo(new DateInfo(rawObs.time));
					ob.setMagnitude(new Magnitude(mag, magErr));
					ob.setBand(keplerSeries);
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
			//System.out.println("getNumberOfRecords");
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
			String str = "Kepler/TESS FITS File";
			
			if (locale.equals("es")) {
				str = "De archivo FITS de Kepler/TESS";
			}

			return str;
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


	@SuppressWarnings("serial")
	class FITSParameterDialog extends AbstractOkCancelDialog {

		private JRadioButton fitsLoadCorRadioButton;
		private JRadioButton fitsLoadRawRadioButton;

		/**
		 * Constructor
		 */
		public FITSParameterDialog() {
			super("Parameters");

			Container contentPane = this.getContentPane();

			JPanel topPane = new JPanel();
			topPane.setLayout(new BoxLayout(topPane, BoxLayout.PAGE_AXIS));
			topPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

			topPane.add(createParameterPane());

			// OK, Cancel
			topPane.add(createButtonPane());

			contentPane.add(topPane);

			this.pack();
			setLocationRelativeTo(Mediator.getUI().getContentPane());
			this.setVisible(true);
		}
		
		private JPanel createParameterPane() {
			JPanel panel = new JPanel();
			panel.setBorder(BorderFactory.createTitledBorder("Data Version"));

			fitsLoadCorRadioButton = new JRadioButton("Corrected (PDCSAP_FLUX)");
			fitsLoadRawRadioButton = new JRadioButton("Raw (SAP_FLUX)");
						
			if (!loadRaw) {
				fitsLoadCorRadioButton.setSelected(true);
			} else {
				fitsLoadRawRadioButton.setSelected(true);
			}
						
			ButtonGroup group = new ButtonGroup();
			group.add(fitsLoadCorRadioButton);
			group.add(fitsLoadRawRadioButton);
			
			panel.add(fitsLoadCorRadioButton);
			panel.add(fitsLoadRawRadioButton);

			return panel;
		}

	
		/**
		 * 
		 */
		public boolean getLoadRaw() {
			return fitsLoadRawRadioButton.isSelected();
		}

		/**
		 * @see org.aavso.tools.vstar.ui.dialog.AbstractOkCancelDialog#cancelAction()
		 */
		@Override
		protected void cancelAction() {
			// Nothing to do.
		}

		/**
		 * @see org.aavso.tools.vstar.ui.dialog.AbstractOkCancelDialog#okAction()
		 */
		@Override
		protected void okAction() {
			boolean ok = true;

			// additional checks if needed...

			if (ok) {
				cancelled = false;
				setVisible(false);
				dispose();
			}
		}
	}

	
}
