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

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.external.lib.TESSObservationRetrieverBase;
import org.aavso.tools.vstar.input.AbstractObservationRetriever;
import org.aavso.tools.vstar.plugin.InputType;
import org.aavso.tools.vstar.plugin.ObservationSourcePluginBase;
import org.aavso.tools.vstar.ui.dialog.AbstractOkCancelDialog;
import org.aavso.tools.vstar.ui.mediator.Mediator;

import nom.tam.fits.BasicHDU;
import nom.tam.fits.BinaryTableHDU;
import nom.tam.fits.Header;
import nom.tam.fits.ImageHDU;

/**
 * A QLP FITS file observation source plug-in that uses the
 * Topcat FITS library.
 * 
 * See also:<br/>
 * o http://archive.stsci.edu/hlsp/qlp<br/>
 * 
 */

// PMAK, 2023-03-28
// 1) QLPFITSObservationRetriever inherits TESSObservationRetrieverBase

public class QLPFITSObservationSource extends ObservationSourcePluginBase {

	private SeriesType dataSeriesQLP;

	private SeriesType dataSeriesQLP_raw;
	
	private boolean loadRawData = true; // it seems SAP (raw) data are better for QLP!

	public QLPFITSObservationSource() {
		super();
		dataSeriesQLP      = SeriesType.create("QLP", "QLP", Color.GREEN, false, false);
		dataSeriesQLP_raw  = SeriesType.create("QLP raw", "QLP raw", Color.GREEN, false, false);
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
		String str = "QLP FITS file v0.4 observation source";
		return str;
	}

	@Override
	public String getDisplayName() {
		String str = "New Star from QLP FITS File v0.4...";
		return str;
	}

	/**
	 * @see org.aavso.tools.vstar.plugin.IPlugin#getDocName()
	 */
	@Override
	public String getDocName() {
		return "QLP_FITS_PlugIn.pdf";
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
		loadRawData = paramDialog.getLoadRaw();
		return new QLPFITSObservationRetriever(loadRawData);
	}

   @Override
    public boolean isMultipleFileSelectionAllowed() {
        return true;
    }

	class QLPFITSObservationRetriever extends TESSObservationRetrieverBase {

		private static final String BJDREF_INT   = "BJDREFI";
		private static final String BJDREF_FLOAT = "BJDREFR";
		
		private boolean loadRaw;
		
		public QLPFITSObservationRetriever(boolean loadRaw) {
			super(QLPFITSObservationSource.this);
			this.loadRaw = loadRaw;
		}

		@Override
		public boolean validateFITS(BasicHDU[] hdus) {
			if (!(hdus.length > 1 && hdus[0] instanceof ImageHDU && hdus[1] instanceof BinaryTableHDU))
				return false;
			BinaryTableHDU tableHDU = (BinaryTableHDU) hdus[1];
			if (!"TIME".equals(tableHDU.getColumnName(0)) ||
				!"SAP_FLUX".equals(tableHDU.getColumnName(2)) ||
				!(
						// Sector 56: column names were changed: KSPSAP_FLUX -> DET_FLUX, KSPSAP_FLUX_ERR -> DET_FLUX_ERR
						("KSPSAP_FLUX".equals(tableHDU.getColumnName(3)) && "KSPSAP_FLUX_ERR".equals(tableHDU.getColumnName(4))) ||
						("DET_FLUX".equals(tableHDU.getColumnName(3)) && "DET_FLUX_ERR".equals(tableHDU.getColumnName(4)))
				) ||
				!"QUALITY".equals(tableHDU.getColumnName(5))) {
				return false;
			}
			return true;
		}
		
		@Override		
		public SeriesType getSeriesType(BasicHDU[] hdus) {
			if (loadRaw) 
				return dataSeriesQLP_raw; 
			else 
				return dataSeriesQLP;
		}

		@Override
		public double getRefMagnitude(BasicHDU[] hdus) {
			ImageHDU infohdu = (ImageHDU)hdus[0];
			return infohdu.getHeader().getDoubleValue("TESSMAG", TESSObservationRetrieverBase.INVALID_MAG);			
		}
		
		@Override
		public String getRefMagnitudeDescription(BasicHDU[] hdus) {
			return "TESS Magnitude";
		}
		
		@Override
		public Double getTimeRef(BasicHDU[] hdus) {
			BinaryTableHDU tableHDU = (BinaryTableHDU) hdus[1];
			Header tableHeader = tableHDU.getHeader();
			if (tableHeader.containsKey(BJDREF_INT) && tableHeader.containsKey(BJDREF_FLOAT)) {			
				double timei = tableHeader.getDoubleValue(BJDREF_INT);
				double timef = tableHeader.getDoubleValue(BJDREF_FLOAT);
				return timei + timef;
			} else {
				return null;
			}
		}

		@Override
		public int getColumnIndex(BasicHDU[] hdus, BinaryTableFieldType field) {
			if (field == BinaryTableFieldType.TIME)
				return 0;
			if (field == BinaryTableFieldType.FLUX)
				return loadRaw ? 2 : 3;
			if (field == BinaryTableFieldType.FLUX_ERROR)
				return loadRaw ? -1 : 4;
			if (field == BinaryTableFieldType.QUALITY_FLAGS) {
				return 5;
			}
			return -1;
		}
		
		@Override
		public String getSourceType() {
			return "QLP FITS File";
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

			fitsLoadCorRadioButton = new JRadioButton("Corrected");
			fitsLoadRawRadioButton = new JRadioButton("Raw (SAP_FLUX)");
						
			if (!loadRawData) {
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
