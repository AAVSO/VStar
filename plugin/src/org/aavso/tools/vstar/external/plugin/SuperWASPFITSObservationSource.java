/**
 * VStar: a statistical analysis tool for variable star data.
 * Copyright (C) 2009  AAVSO (http://www.aavso.org/)
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
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import nom.tam.fits.BasicHDU;
import nom.tam.fits.BinaryTableHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.Header;

import org.aavso.tools.vstar.data.DateInfo;
import org.aavso.tools.vstar.data.InvalidObservation;
import org.aavso.tools.vstar.data.Magnitude;
import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.exception.ObservationReadError;
import org.aavso.tools.vstar.input.AbstractObservationRetriever;
import org.aavso.tools.vstar.plugin.InputType;
import org.aavso.tools.vstar.plugin.ObservationSourcePluginBase;
import org.aavso.tools.vstar.ui.NumberSelectionPane;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.util.prefs.NumericPrecisionPrefs;
import org.aavso.tools.vstar.ui.dialog.AbstractOkCancelDialog;

//12/02/2018 C. Kotnik added name to observations so they can be
//saved and reloaded from a file.

//2020-09-30: PMAK (Maksym Pyatnytskyy): 
// 1) TAMFLUX2 support: 
//   See https://exoplanetarchive.ipac.caltech.edu/docs/SuperWASPProcessing.html
//   MagErrorSelectionDialog is derived from AbstractOkCancelDialog now (instead of JDialog)
// 2) setHeliocentric(true)

/**
 * A FITS file observation source plug-in that uses the Topcat FITS library.
 */
public class SuperWASPFITSObservationSource extends ObservationSourcePluginBase {

	private SeriesType superWaspSeries_raw;
	private SeriesType superWaspSeries;
	
	private double magErrThreshold = 0.05;
	private boolean loadRaw = false;

	public SuperWASPFITSObservationSource() {
		super();
		superWaspSeries = SeriesType.create("SuperWASP", "SuperWASP",
				Color.RED, false, false);
		superWaspSeries_raw = SeriesType.create("SuperWASP raw", "SuperWASP raw",
				Color.RED, false, false);
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
	public List<String> getAdditionalFileExtensions() {
		List<String> extensions = new ArrayList<String>();
		extensions.add("fits");
		return extensions;
	}

	@Override
	public AbstractObservationRetriever getObservationRetriever() {
		// Dialog moved here from collectObservations() where it invoked from non-UI thread
		// to this more natural place.
		// No annoying "No observations for the specified period" messages.
		MagErrorSelectionDialog magErrThresholdDialog = new MagErrorSelectionDialog(
				0.01, 99.0, 0.01, magErrThreshold, loadRaw);
		if (magErrThresholdDialog.isCancelled()) {
			// It seems it is safe to return null here.
			return null;
		};
		magErrThreshold = magErrThresholdDialog.getValue();
		loadRaw = magErrThresholdDialog.getLoadRaw();
		return new FITSObservationRetriever();
	}

	@Override
	public String getDescription() {
		return "SuperWASP file v2 observation reader";
	}

	@Override
	public String getDisplayName() {
		return "New Star from SuperWASP FITS File v2...";
	}

	class FITSObservationRetriever extends AbstractObservationRetriever {

		public FITSObservationRetriever() {
			super(getVelaFilterStr());
		}

		@Override
		public void retrieveObservations() throws ObservationReadError,
				InterruptedException {
			setHeliocentric(true); // added by PMAK 2020-09-30
			try {
				Fits fits = new Fits(getInputStreams().get(0));
				BasicHDU[] hdus = fits.read();
				double jdRef = retrieveJDReference(hdus);
				retrieveObservations(hdus, jdRef);
			} catch (FitsException e) {
				throw new ObservationReadError(e.getLocalizedMessage());
			}
		}

		private double retrieveJDReference(BasicHDU[] hdus) {
			double jdRef = 0;

			for (BasicHDU hdu : hdus) {
				// Get reference JD.
				Header header = hdu.getHeader();
				Double value = header.getDoubleValue("JD_REF");
				if (value != null) {
					jdRef = value;
					if (jdRef > 0)
						break;
				}
			}

			return jdRef;
		}

		// Collect observations from the table excluding those with nonsensical
		// flux values.
		private void retrieveObservations(BasicHDU[] hdus, double jdRef)
				throws FitsException, ObservationReadError {

			List<ValidObservation> obs = new ArrayList<ValidObservation>();
			
			// PMAK: minMagErr/maxMagErr are not used anymore
			//double minMagErr = Double.MAX_VALUE;
			//double maxMagErr = Double.MIN_VALUE;

			for (BasicHDU hdu : hdus) {
				if (hdu instanceof BinaryTableHDU) {
					BinaryTableHDU tableHDU = (BinaryTableHDU) hdu;
					
					// PMAK: Check field names to be sure we are using correct FITS.
					if (!"TMID".equals(tableHDU.getColumnName(0)) ||
						!"FLUX2".equals(tableHDU.getColumnName(1)) ||
						!"FLUX2_ERR".equals(tableHDU.getColumnName(2)) ||
						!"TAMFLUX2".equals(tableHDU.getColumnName(3)) ||
						!"TAMFLUX2_ERR".equals(tableHDU.getColumnName(4)) ||
						!"IMAGEID".equals(tableHDU.getColumnName(5)) ||
						!"CCDX".equals(tableHDU.getColumnName(6)) ||
						!"CCDY".equals(tableHDU.getColumnName(7))) {
						throw new ObservationReadError("Not a valid FITS file");
					}					
										
					int tableRows = tableHDU.getNRows();
					for (int row = 0; row < tableRows && !wasInterrupted(); row++) {
						try {
							int tmid = ((int[]) tableHDU.getElement(row, 0))[0];
							float rawFlux = ((float[]) tableHDU.getElement(row, 1))[0];
							float rawFluxErr = ((float[]) tableHDU.getElement(row, 2))[0];

							float tamFlux = ((float[]) tableHDU.getElement(row, 3))[0];
							float tamFluxErr = ((float[]) tableHDU.getElement(row, 4))[0];
							String imageId = (String) tableHDU.getElement(row, 5);
							short ccdX = ((short[]) tableHDU.getElement(row, 6))[0];
							short ccdY = ((short[]) tableHDU.getElement(row, 7))[0];
							// short flag = ((short[]) tableHDU.getElement(row,
							// 8))[0];
							
							float flux = loadRaw ? rawFlux : tamFlux;
							float fluxErr = loadRaw ? rawFluxErr : tamFluxErr;
							
							if (flux > 1 && flux - fluxErr > 0) {
								double hjd = tmid / 86400.0 + jdRef;
								double mag = 15.0 - 2.5 * Math.log(flux)
										/ Math.log(10.0);
								double magErr = 1.086 * fluxErr / flux;

								//if (magErr < minMagErr) {
								//	minMagErr = magErr;
								//} else if (magErr > maxMagErr) {
								//	maxMagErr = magErr;
								//}

								ValidObservation ob = new ValidObservation();
								ob.setName(getInputName());
								ob.setDateInfo(new DateInfo(hjd));
								ob.setMagnitude(new Magnitude(mag, magErr));
								ob.setBand(loadRaw ? superWaspSeries_raw : superWaspSeries);
								ob.setRecordNumber(row);
								ob.addDetail("IMAGE_ID", imageId, "Image ID");
								ob.addDetail("CCDX", ccdX + "", "CCD X");
								ob.addDetail("CCDY", ccdY + "", "CCD Y");
								ob.addDetail("RAW_FLUX", rawFlux + "", "Raw Flux");
								ob.addDetail("RAW_FLUX_ERR", rawFluxErr + "", "Raw Flux Error");
								ob.addDetail("TAM_FLUX", tamFlux + "", "TAM Flux");
								ob.addDetail("TAM_FLUX_ERR", tamFluxErr + "", "TAM Flux Error");
//								ob.addDetail("FLAG", flag + "", "Flag");
								obs.add(ob);
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

			collectObservations(obs);
		}

		// Collect observations, distinguishing those with a magnitude error
		// greater than or equal to some threshold.
		private void collectObservations(List<ValidObservation> obs) throws ObservationReadError {
			for (ValidObservation ob : obs) {
				double magErr = ob.getMagnitude().getUncertainty();
				if (magErr >= magErrThreshold) {
					ob.setExcluded(true);
				}
				collectObservation(ob);
			}
		}

		@Override
		public String getSourceName() {
			return getInputName();
		}

		@Override
		public String getSourceType() {
			return "SuperWASP FITS File";
		}
	}

	@SuppressWarnings("serial")
	class MagErrorSelectionDialog extends AbstractOkCancelDialog {

		private NumberSelectionPane magErrorSelector;
		private JRadioButton fitsLoadCorRadioButton;
		private JRadioButton fitsLoadRawRadioButton;
		
		public MagErrorSelectionDialog(double min, double max,
				double increment, double initial, boolean loadRaw) {
			super("Parameters");
			
			Container contentPane = this.getContentPane();
			
			cancelled = true;

			JPanel topPane = new JPanel();
			topPane.setLayout(new BoxLayout(topPane, BoxLayout.PAGE_AXIS));
			topPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

			magErrorSelector = new NumberSelectionPane(
					"Maximum Magnitude Error", min, max, increment,
					initial, NumericPrecisionPrefs.getMagInputFormat());

			topPane.add(magErrorSelector);

			topPane.add(createParameterPane());

			topPane.add(createButtonPane());

			contentPane.add(topPane);

			this.pack();
			setLocationRelativeTo(Mediator.getUI().getContentPane());
			this.setVisible(true);
		}

		private JPanel createParameterPane() {
			JPanel panel = new JPanel();
			panel.setBorder(BorderFactory.createTitledBorder("Data Version"));

			fitsLoadCorRadioButton = new JRadioButton("Corrected (TAMFLUX2)");
			fitsLoadRawRadioButton = new JRadioButton("Raw (FLUX2)");
						
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
		 * @return The value selected in the spinner.
		 */
		public double getValue() {
			return magErrorSelector.getValue();
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
