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
import java.util.Locale;

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


//12/02/2018 C. Kotnik added name to observations so they can be
//saved and reloaded from a file.

//01/06/2020 C. Kotnik Generalized logic to calculate BJD to include TESS by getting
//                     offset from FITS header.

// PMAK, 2020-FEB-07
// 1) KEPLER/TESS magnitude adjust: median series value to KEPMAG/TESSMAG value from FITS header.
// 2) Refactoring

// PMAK, 2020-02-22
// 1) Parameter's dialog

// PMAK, 2023-03-28
// 1) KeplerFITSObservationRetriever inherits TESSObservationRetrieverBase

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
	
	private boolean loadRawData = false;
	
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
		String str = "Kepler/TESS FITS file v2.4 observation source";

		if (locale.equals("es")) {
			str = "Observaciones de archivo FITS de Kepler/TESS v2.4 del plug-in que usa la biblioteca Topcat FITS.";
		}

		return str;
	}

	@Override
	public String getDisplayName() {
		String str = "New Star from Kepler/TESS FITS File v2.4...";

		if (locale.equals("es")) {
			str = "Nueva estrella de archivo FITS de Kepler/TESS v2.4...";
		}

		return str;
	}

	/**
	 * @see org.aavso.tools.vstar.plugin.IPlugin#getDocName()
	 */
	@Override
	public String getDocName() {
		return "INSTRUCTIONS FOR USING THE KEPLER FITS v2.2 FILE VStar PLUG IN.pdf";
	}

	@Override
	public List<String> getAdditionalFileExtensions() {
		List<String> extensions = new ArrayList<String>();
		extensions.add("fits");
		return extensions;
	}

	@Override
	public AbstractObservationRetriever getObservationRetriever() {
		// Dialog moved from retrieveObservations() where it invoked from non-UI thread
		// to this more natural place.
		// No annoying "No observations for the specified period" messages.
		FITSParameterDialog paramDialog = new FITSParameterDialog();
		if (paramDialog.isCancelled()) {
			// It seems it is safe to return null here.
			return null;
		}
		loadRawData = paramDialog.getLoadRaw();
		return new KeplerFITSObservationRetriever(loadRawData);
	}
	
	@Override
    public boolean isMultipleFileSelectionAllowed() {
        return true;
    }

    class KeplerFITSObservationRetriever extends TESSObservationRetrieverBase {

		private static final String BJDREF_INT   = "BJDREFI";
		private static final String BJDREF_FLOAT = "BJDREFF";
		
		private boolean loadRaw;
		
		public KeplerFITSObservationRetriever(boolean loadRaw) {
			super(KeplerFITSObservationSource.this);
			this.loadRaw = loadRaw; 
		}

		@Override
		public boolean validateFITS(BasicHDU[] hdus) {
			if (!(hdus.length > 1 && hdus[0] instanceof ImageHDU && hdus[1] instanceof BinaryTableHDU))
				return false;
			BinaryTableHDU tableHDU = (BinaryTableHDU) hdus[1];
			if (!"TIME".equals(tableHDU.getColumnName(0)) ||
				!"SAP_FLUX".equals(tableHDU.getColumnName(3)) ||
				!"SAP_FLUX_ERR".equals(tableHDU.getColumnName(4)) ||
				!"PDCSAP_FLUX".equals(tableHDU.getColumnName(7)) ||
				!"PDCSAP_FLUX_ERR".equals(tableHDU.getColumnName(8))) {
				return false;
			}
			return true;
		}
		
		@Override		
		public SeriesType getSeriesType(BasicHDU[] hdus) {
			ImageHDU infohdu = (ImageHDU)hdus[0];
			String telescope = infohdu.getTelescope();
			if ("TESS".equals(telescope)) {
				if (loadRaw) 
					return dataSeriesTESS_raw; 
				else 
					return dataSeriesTESS;
			}
			if ("Kepler".equals(telescope)) {
				if (loadRaw) 
					return dataSeriesKepler_raw; 
				else 
					return dataSeriesKepler;
			}
			if (loadRaw) 
				return dataSeriesMAST_raw;
			else
				return dataSeriesMAST;
		};

		@Override
		public double getRefMagnitude(BasicHDU[] hdus) {
			ImageHDU infohdu = (ImageHDU)hdus[0];			
			String telescope = infohdu.getTelescope();			
			if ("TESS".equals(telescope)) {
				return infohdu.getHeader().getDoubleValue("TESSMAG", TESSObservationRetrieverBase.INVALID_MAG);
			}
			if ("Kepler".equals(telescope)) {
				return infohdu.getHeader().getDoubleValue("KEPMAG", TESSObservationRetrieverBase.INVALID_MAG);
			}
			return TESSObservationRetrieverBase.INVALID_MAG;
		}
		
		@Override
		public String getRefMagnitudeDescription(BasicHDU[] hdus) {
			ImageHDU infohdu = (ImageHDU)hdus[0];
			String telescope = infohdu.getTelescope();			
			if ("TESS".equals(telescope)) {
				return "TESS Magnitude";
			}
			if ("Kepler".equals(telescope)) {
				return "Kepler Magnitude";
			}
			return null;
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
				return loadRaw ? 3 : 7;
			if (field == BinaryTableFieldType.FLUX_ERROR)
				return loadRaw ? 4 : 8;
			if (field == BinaryTableFieldType.QUALITY_FLAGS) {
				ImageHDU infohdu = (ImageHDU)hdus[0];
				BinaryTableHDU tableHDU = (BinaryTableHDU) hdus[1];				
				String telescope = infohdu.getTelescope();
				if ("Kepler".equals(telescope)) {
					if ("SAP_QUALITY".equals(tableHDU.getColumnName(9))) 
						return 9;
				}
				if ("TESS".equals(telescope)) {
					if ("QUALITY".equals(tableHDU.getColumnName(9)))
						return 9;
				}
			}
			return -1;
		}
		
		@Override
		public String getSourceType() {
			return "Kepler/TESS FITS File";
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
