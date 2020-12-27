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
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;
//import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.aavso.tools.vstar.data.DateInfo;
import org.aavso.tools.vstar.data.InvalidObservation;
import org.aavso.tools.vstar.data.Magnitude;
import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.exception.ObservationReadError;
import org.aavso.tools.vstar.input.AbstractObservationRetriever;
import org.aavso.tools.vstar.plugin.InputType;
import org.aavso.tools.vstar.plugin.ObservationSourcePluginBase;
import org.aavso.tools.vstar.ui.dialog.AbstractOkCancelDialog;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.StarInfo;
import org.apache.commons.math.stat.descriptive.rank.Median;

import nom.tam.fits.BasicHDU;
import nom.tam.fits.BinaryTableHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.ImageHDU;

/**
 * A QLP FITS file observation source plug-in that uses the
 * Topcat FITS library.
 * 
 * See also:<br/>
 * o http://archive.stsci.edu/hlsp/qlp<br/>
 * 
 */
public class QLPFITSObservationSource extends ObservationSourcePluginBase {

	//private  final SeriesType keplerSeries;
	private SeriesType dataSeriesQLP;

	private SeriesType dataSeriesQLP_raw;
	
	//private Locale locale;
	
	private boolean loadRaw = true; // it seems SAP (raw) data are better for QLP!

	public QLPFITSObservationSource() {
		super();
		dataSeriesQLP      = SeriesType.create("QLP", "QLP", Color.GREEN, false, false);
		dataSeriesQLP_raw  = SeriesType.create("QLP raw", "QLP raw", Color.GREEN, false, false);

		//locale = Locale.getDefault();
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
		String str = "QLP FITS file v0.2 observation source";
		return str;
	}

	@Override
	public String getDisplayName() {
		String str = "New Star from QLP FITS File v0.2...";
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
		FITSParameterDialog paramDialog = new FITSParameterDialog();
		if (paramDialog.isCancelled()) {
			// It seems it is safe to return null here.
			return null;
		}
		loadRaw = paramDialog.getLoadRaw();
		return new QLPFITSObservationRetriever();
	}

	private class RawObservationData {
		int row;
		double time;
		double intensity;
		double error;
		int quality;
	}

	class QLPFITSObservationRetriever extends AbstractObservationRetriever {

		private BasicHDU[] hdus = null;
		
		private String objName = null;
		
		public QLPFITSObservationRetriever() {
			super(getVelaFilterStr());
		}

		@Override
		public void retrieveObservations() throws ObservationReadError,
				InterruptedException {
			setBarycentric(true);
			try {
				// BasicHDU initialization moved to getNumberOfRecords
				retrieveQLPObservations(hdus);
			} catch (Exception e) {
				throw new ObservationReadError(e.getLocalizedMessage());
			}
		}

		// Collect observations from the table...
		private void retrieveQLPObservations(BasicHDU[] hdus)
				throws FitsException, ObservationReadError {

			// QLP light curve FITS contains primary HDU having keywords only and binary table extension.
			if (hdus.length >  1 && hdus[0] instanceof ImageHDU && hdus[1] instanceof BinaryTableHDU) {

				double invalidMag = 99.99;

				// Lists to store observations before median level adjust
				List<RawObservationData> rawObsList = new ArrayList<RawObservationData>();
				List<InvalidObservation> invalidObsList = new ArrayList<InvalidObservation>();
				double tessMag = invalidMag;

				SeriesType qlpSeries = dataSeriesQLP;
				if (loadRaw) qlpSeries = dataSeriesQLP_raw;

				ImageHDU imageHDU = (ImageHDU) hdus[0];
				
				// TESSMAG from FITS header
				tessMag = imageHDU.getHeader().getDoubleValue("TESSMAG", invalidMag);

				objName = imageHDU.getObject();

				BinaryTableHDU tableHDU = (BinaryTableHDU) hdus[1];
				
				// PMAK: Check field names to be sure we are using correct FITS.
				if (!"TIME".equals(tableHDU.getColumnName(0)) ||
					!"SAP_FLUX".equals(tableHDU.getColumnName(2)) ||
					!"KSPSAP_FLUX".equals(tableHDU.getColumnName(3)) ||
					!"KSPSAP_FLUX_ERR".equals(tableHDU.getColumnName(4)) ||
					!"QUALITY".equals(tableHDU.getColumnName(5))) {
					throw new ObservationReadError("Not a valid FITS file");
				}
			
				double timei = tableHDU.getHeader().getDoubleValue("BJDREFI");
				double timef = tableHDU.getHeader().getDoubleValue("BJDREFF");
				
				for (int row = 0; row < tableHDU.getNRows()	&& !wasInterrupted(); row++) {
					try {
						double barytime = ((double[]) tableHDU.getElement(row, 0))[0];
						float flux;
						float flux_err;
						if (!loadRaw) {
							flux = ((float[]) tableHDU.getElement(row, 3))[0];
							flux_err = ((float[]) tableHDU.getElement(row, 4))[0];
						} else {
							flux = ((float[]) tableHDU.getElement(row, 2))[0];
							flux_err = 0;
						}
						
						// Additional fields
						int quality = ((int[]) tableHDU.getElement(row, 5))[0];

						// Include only valid magnitude fluxes.
						if (!Float.isInfinite(flux)
								&& !Float.isInfinite(flux_err)
								&& !Float.isNaN(flux)
								&& !Float.isNaN(flux_err)) {

							double hjd = barytime + timei + timef;
							// Store data to temporary list
							RawObservationData rawObs = new RawObservationData();
							rawObs.row = row;
							rawObs.time = hjd;
							rawObs.intensity = flux;
							rawObs.error = flux_err;
							rawObs.quality = quality;
							rawObsList.add(rawObs);
						}
					} catch (Exception e) {
						String input = tableHDU.getRow(row).toString();
						String error = e.getLocalizedMessage();
						InvalidObservation ob = new InvalidObservation(input, error);
						ob.setRecordNumber(row);
						invalidObsList.add(ob);
					}
				}

				// Calculating magShift (median of all points)
				double magShift = 15.0; // arbitrary value
				if (tessMag != invalidMag) {
					double flux[] = new double[rawObsList.size()];
					for (int i = 0; i < rawObsList.size(); i++) {
						flux[i] = rawObsList.get(i).intensity;
					}
					Median median = new Median();
					double median_flux = median.evaluate(flux);
					double median_inst_mag = -2.5 * Math.log10(median_flux);
					magShift = tessMag - median_inst_mag;
				}

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
					ob.setBand(qlpSeries);
					ob.setRecordNumber(rawObs.row);
					ob.addDetail("Quality", Integer.toString(rawObs.quality), "Quality");
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
			String str = "QLP FITS File";
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
			super("QLP FITS Parameters");

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

			fitsLoadCorRadioButton = new JRadioButton("Corrected (KSPSAP_FLUX)");
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
