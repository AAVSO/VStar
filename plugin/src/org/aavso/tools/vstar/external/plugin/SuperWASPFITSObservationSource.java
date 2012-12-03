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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

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
import org.aavso.tools.vstar.ui.MainFrame;
import org.aavso.tools.vstar.ui.NumberSelectionPane;
import org.aavso.tools.vstar.util.prefs.NumericPrecisionPrefs;

/**
 * A FITS file observation source plug-in that uses the Topcat FITS library.
 */
public class SuperWASPFITSObservationSource extends ObservationSourcePluginBase {

	private SeriesType superWaspSeries;
	
	public SuperWASPFITSObservationSource() {
		super();
		superWaspSeries = SeriesType.create("SuperWASP", "SuperWASP", Color.RED, false);
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
		return new FITSObservationRetriever();
	}

	@Override
	public String getDescription() {
		return "A FITS file observation source plug-in that uses the Topcat FITS library.";
	}

	@Override
	public String getDisplayName() {
		return "New Star from SuperWASP FITS File...";
	}

	class FITSObservationRetriever extends AbstractObservationRetriever {
		@Override
		public void retrieveObservations() throws ObservationReadError,
				InterruptedException {

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
			double minMagErr = Double.MAX_VALUE;
			double maxMagErr = Double.MIN_VALUE;

			for (BasicHDU hdu : hdus) {
				if (hdu instanceof BinaryTableHDU) {
					BinaryTableHDU tableHDU = (BinaryTableHDU) hdu;

					for (int row = 0; row < tableHDU.getNRows()
							&& !wasInterrupted(); row++) {
						try {
							int tmid = ((int[]) tableHDU.getElement(row, 0))[0];
							float flux = ((float[]) tableHDU.getElement(row, 1))[0];
							float fluxErr = ((float[]) tableHDU.getElement(row,
									2))[0];

							// TODO: make use of the additional fields...
							float tamFlux = ((float[]) tableHDU.getElement(row,
									3))[0];
							float tamFluxErr = ((float[]) tableHDU.getElement(
									row, 4))[0];
							String imageId = (String) tableHDU.getElement(row,
									5);
							short ccdX = ((short[]) tableHDU.getElement(row, 6))[0];
							short ccdY = ((short[]) tableHDU.getElement(row, 7))[0];
							short flag = ((short[]) tableHDU.getElement(row, 8))[0];

							if (flux > 1 && flux - fluxErr > 0) {
								double hjd = tmid / 86400.0 + jdRef;
								double mag = 15.0 - 2.5 * Math.log(flux)
										/ Math.log(10.0);
								double magErr = 1.086 * fluxErr / flux;

								if (magErr < minMagErr) {
									minMagErr = magErr;
								} else if (magErr > maxMagErr) {
									maxMagErr = magErr;
								}

								ValidObservation ob = new ValidObservation();
								ob.setDateInfo(new DateInfo(hjd));
								ob.setMagnitude(new Magnitude(mag, magErr));
								ob.setBand(superWaspSeries);
								ob.setRecordNumber(row);
								ob.addDetail("IMAGE_ID", imageId, "Image ID");

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

			collectObservations(obs, minMagErr, maxMagErr);
		}

		// Collect observations, distinguishing those with a magnitude error
		// greater than or equal to some threshold.
		private void collectObservations(List<ValidObservation> obs,
				double minMagErr, double maxMagErr) throws ObservationReadError {

			double magErrThreshold;
			magErrThreshold = (maxMagErr + minMagErr) / 2;

			double magErrIncrement = (maxMagErr - minMagErr) / 100;

			MagErrorSelectionDialog magErrThresholdDialog = new MagErrorSelectionDialog(
					minMagErr, maxMagErr, magErrIncrement, magErrThreshold);

			magErrThreshold = magErrThresholdDialog.getValue();

			for (ValidObservation ob : obs) {
				double magErr = ob.getMagnitude().getUncertainty();
				if (magErr >= magErrThreshold) {
					ob.setBand(superWaspSeries); // TODO: why do this here as well?
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
	class MagErrorSelectionDialog extends JDialog {

		private NumberSelectionPane magErrorSelector;

		public MagErrorSelectionDialog(double min, double max,
				double increment, double initial) {
			super();

			setTitle("Maximum Magnitude Error");
			setModal(true);
			setMinimumSize(new Dimension(250, 100));

			Container contentPane = this.getContentPane();

			JPanel topPane = new JPanel();
			topPane.setLayout(new BoxLayout(topPane, BoxLayout.PAGE_AXIS));
			topPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

			magErrorSelector = new NumberSelectionPane(
					"Select Maximum Magnitude Error", min, max, increment,
					initial, NumericPrecisionPrefs.getMagInputFormat());

			topPane.add(magErrorSelector);

			topPane.add(createButtonPane());

			contentPane.add(topPane);

			this.pack();
			setLocationRelativeTo(MainFrame.getInstance().getContentPane());
			this.setVisible(true);
		}

		protected JPanel createButtonPane() {
			JPanel panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
			JButton okButton = new JButton("OK");
			okButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setVisible(false);
					dispose();
				}
			});
			panel.add(okButton);

			this.getRootPane().setDefaultButton(okButton);

			return panel;
		}

		/**
		 * @return The value selected in the spinner.
		 */
		public double getValue() {
			return magErrorSelector.getValue();
		}
	}
}
