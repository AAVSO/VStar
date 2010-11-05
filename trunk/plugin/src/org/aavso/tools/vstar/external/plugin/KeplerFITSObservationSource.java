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
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;

import nom.tam.fits.BasicHDU;
import nom.tam.fits.BinaryTableHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.Header;

import org.aavso.tools.vstar.data.DateInfo;
import org.aavso.tools.vstar.data.Magnitude;
import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.exception.ObservationReadError;
import org.aavso.tools.vstar.input.AbstractObservationRetriever;
import org.aavso.tools.vstar.plugin.ObservationSourcePluginBase;
import org.aavso.tools.vstar.ui.MainFrame;
import org.aavso.tools.vstar.ui.NumberSelectionPane;

/**
 * A Kepler FITS file observation source plug-in that uses the Topcat FITS library.
 */
public class KeplerFITSObservationSource implements ObservationSourcePluginBase {

	private JFileChooser fileChooser;
	private File currFile;

	public KeplerFITSObservationSource() {
		fileChooser = new JFileChooser();
	}

	@Override
	public String getCurrentStarName() {
		return currFile.getName();
	}

	@Override
	public AbstractObservationRetriever getObservationRetriever() {
		return new KeplerFITSObservationRetriever();
	}

	@Override
	public String getDescription() {
		return "A Kepler FITS file observation source plug-in that uses the Topcat FITS library.";
	}

	@Override
	public String getDisplayName() {
		return "New Star from Kepler FITS File...";
	}

	class KeplerFITSObservationRetriever extends AbstractObservationRetriever {
		@Override
//		public void retrieveKeplerObservations() throws ObservationReadError,
		public void retrieveObservations() throws ObservationReadError,
				InterruptedException {

			int result = fileChooser.showOpenDialog(MainFrame.getInstance());

			if (result == JFileChooser.APPROVE_OPTION) {
				currFile = fileChooser.getSelectedFile();
				try {
					Fits fits = new Fits(currFile);
					BasicHDU[] hdus = fits.read();
//					retrieveObservations(hdus);
					retrieveKeplerObservations(hdus);
				} catch (FitsException e) {
					throw new ObservationReadError(e.getLocalizedMessage());
				}
			}
		}

		// Collect observations from the table excluding those with nonsensical
		// flux values.
		private void retrieveKeplerObservations(BasicHDU[] hdus)
				throws FitsException, ObservationReadError {

			List<ValidObservation> obs = new ArrayList<ValidObservation>();
			double minMagErr = Double.MAX_VALUE;
			double maxMagErr = Double.MIN_VALUE;

			for (BasicHDU hdu : hdus) {
				if (hdu instanceof BinaryTableHDU) {
					BinaryTableHDU tableHDU = (BinaryTableHDU) hdu;

					for (int row = 0; row < tableHDU.getNRows(); row++) {
						double barytime = ((double[]) tableHDU.getElement(row, 0))[0];
						float timcorr = ((float[]) tableHDU.getElement(row, 1))[0];
						int cadence_number = ((int[]) tableHDU.getElement(row, 2))[0];
						double ap_cent_row = ((double[]) tableHDU.getElement(row, 3))[0];
						float ap_cent_r_err = ((float[]) tableHDU.getElement(row, 4))[0];
						double ap_cent_col = ((double[]) tableHDU.getElement(row, 5))[0];
						float ap_cent_c_err = ((float[]) tableHDU.getElement(row, 6))[0];
						float ap_raw_flux = ((float[]) tableHDU.getElement(row, 7))[0];
						float ap_raw_err = ((float[]) tableHDU.getElement(row, 8))[0];
						float ap_corr_flux = ((float[]) tableHDU.getElement(row, 9))[0];
                                                float ap_corr_err = ((float[]) tableHDU.getElement(row, 10))[0];
						float ap_ins_flux = ((float[]) tableHDU.getElement(row, 11))[0];
                                                float ap_ins_err = ((float[]) tableHDU.getElement(row, 12))[0];
						float dia_raw_flux = ((float[]) tableHDU.getElement(row, 13))[0];
                                                float dia_raw_err = ((float[]) tableHDU.getElement(row, 14))[0];
						float dia_corr_flux = ((float[]) tableHDU.getElement(row, 15))[0];
                                                float dia_corr_err = ((float[]) tableHDU.getElement(row, 16))[0];
						float dia_ins_flux = ((float[]) tableHDU.getElement(row, 17))[0];
                                                float dia_ins_err = ((float[]) tableHDU.getElement(row, 18))[0];

						// For non-infinite magnitude fluxes ...
//						if (!ap_corr_flux.isInfinite && !ap_corr_err.isInfinite) {
                        if (!Float.isInfinite(ap_corr_flux) && !Float.isInfinite(ap_corr_err)) { 
                                                
							double hjd = barytime + 2400000.5;
							double mag = 15.0 - 2.5 * Math.log(ap_corr_flux)
									/ Math.log(10.0);
							double magErr = 1.086 * ap_corr_err / ap_corr_flux;

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

							obs.add(ob);
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

			MagErrorSelectionDialog magErrThresholdDialog = new MagErrorSelectionDialog(
					minMagErr, maxMagErr, 0.00001, magErrThreshold);

			magErrThreshold = magErrThresholdDialog.getValue();

			for (ValidObservation ob : obs) {
				double magErr = ob.getMagnitude().getUncertainty();
				if (magErr >= magErrThreshold) {
					ob.setBand(SeriesType.Excluded);
				}

				collectObservation(ob);
			}
		}
	}

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
					initial);

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
