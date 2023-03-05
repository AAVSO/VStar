/**
 * VStar: a statistical analysis tool for variable star data.
 * Copyright (C) 2010  AAVSO (http://www.aavso.org/)
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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.aavso.tools.vstar.data.DateInfo;
import org.aavso.tools.vstar.data.InvalidObservation;
import org.aavso.tools.vstar.data.MTypeType;
import org.aavso.tools.vstar.data.Magnitude;
import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.data.ValidObservation.JDflavour;
import org.aavso.tools.vstar.data.validation.InclusiveRangePredicate;
import org.aavso.tools.vstar.data.validation.JulianDayValidator;
import org.aavso.tools.vstar.data.validation.MagnitudeFieldValidator;
import org.aavso.tools.vstar.data.validation.UncertaintyValueValidator;
import org.aavso.tools.vstar.exception.CancellationException;
import org.aavso.tools.vstar.exception.ObservationReadError;
import org.aavso.tools.vstar.exception.ObservationValidationError;
import org.aavso.tools.vstar.input.AbstractObservationRetriever;
import org.aavso.tools.vstar.plugin.InputType;
import org.aavso.tools.vstar.plugin.ObservationSourcePluginBase;
import org.aavso.tools.vstar.plugin.PluginComponentFactory;
import org.aavso.tools.vstar.ui.dialog.AbstractOkCancelDialog;
import org.aavso.tools.vstar.ui.dialog.MessageBox;
import org.aavso.tools.vstar.ui.dialog.TextArea;
import org.aavso.tools.vstar.ui.dialog.TextField;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.util.Pair;

/**
 * 
 * @author max (PMAK)
 * 
 */
public class ZTFObSource extends ObservationSourcePluginBase {

	// start of the header like
	private static final String HEADER_LINE_START = 
			"oid\texpid\thjd\tmjd\tmag\tmagerr\tcatflags\tfiltercode\t" + 
			"ra\tdec\tchi\tsharp\tfilefracday\tfield\tccdid\tqid\t" + 
			"limitmag\tmagzp\tmagzprms\tclrcoeff\tclrcounc\texptime\t" + 
			"airmass\tprogramid";
	
	private SeriesType ztfgSeries;
	private SeriesType ztfrSeries;
	private SeriesType ztfiSeries;
	private SeriesType ztfUnknownSeries;
	
	private String objectID;
	private String baseURL = "https://irsa.ipac.caltech.edu/cgi-bin/ZTF/nph_light_curves?FORMAT=TSV&ID=";
	
	// Create static VeLa filter field here since cannot create it in
	// inner dialog class.
	private static Pair<TextArea, JPanel> velaFilterFieldPanelPair;

	static {
		velaFilterFieldPanelPair = PluginComponentFactory.createVeLaFilterPane();
	}

	/**
	 * Constructor
	 */
	public ZTFObSource() {
		super();
		ztfgSeries = SeriesType.create("ZTF zg", "ZTF zg", Color.GREEN, false, false);
		ztfrSeries = SeriesType.create("ZTF zr", "ZTF zr", Color.RED, false, false);
		ztfiSeries = SeriesType.create("ZTF zi", "ZTF zi", new Color(192, 64, 0), false, false);
		ztfUnknownSeries = SeriesType.create("ZTF unknown", "ZTF unknown", new Color(255, 255, 0), false, false);
	}
	
	/**
	 * @see org.aavso.tools.vstar.plugin.ObservationSourcePluginBase#getInputType()
	 */
	@Override
	public InputType getInputType() {
		return InputType.URL;
	}
	
	@Override
	public List<URL> getURLs() throws Exception {
		List<URL> urls = new ArrayList<URL>();

		String s = "";
		while (true) {
			ZTFParameterDialog paramDialog = new ZTFParameterDialog(s, isAdditive);
			if (!paramDialog.isCancelled()) {
				s = paramDialog.getObjectID().trim();
				if (s != null && !"".equals(s) && s.matches("[0-9]+")) {
					setAdditive(paramDialog.isLoadAdditive());
					objectID = s;
					try {
						urls.add(new URL(baseURL + objectID));
					} catch (MalformedURLException e) {
						throw new ObservationReadError("Cannot construct ZTF URL (reason: " + e.getLocalizedMessage() + ")");
					}
					
					return urls;
				} else {
					MessageBox.showErrorDialog("ZTF", "ZTF source must be numeric");
				}
			} else {
				throw new CancellationException();
			}
				
		}
	}
	
	/**
	 * @see org.aavso.tools.vstar.plugin.ObservationSourcePluginBase#
	 *      getObservationRetriever ()
	 */
	@Override
	public AbstractObservationRetriever getObservationRetriever() {
		return new ZTFFormatRetriever();
	}
	
	/**
	 * @see org.aavso.tools.vstar.plugin.IPlugin#getDescription()
	 */
	@Override
	public String getDescription() {
		return "ZTF Photometry Format reader";
	}

	/**
	 * @see org.aavso.tools.vstar.plugin.IPlugin#getDisplayName()
	 */
	@Override
	public String getDisplayName() {
		return "New Star from ZTF Photometry ...";
	}

	class ZTFFormatRetriever extends AbstractObservationRetriever {

		private String obscode = "ZTF";
		private String delimiter = "\t";
		private String objectName;
		
		private List<String> lines;


		private JulianDayValidator julianDayValidator;
		private MagnitudeFieldValidator magnitudeFieldValidator;
		private UncertaintyValueValidator uncertaintyValueValidator;

		/**
		 * Constructor
		 */
		public ZTFFormatRetriever() {
			super(getVelaFilterStr());
			julianDayValidator = new JulianDayValidator();
			magnitudeFieldValidator = new MagnitudeFieldValidator();
			uncertaintyValueValidator = new UncertaintyValueValidator(new InclusiveRangePredicate(0, 1));
		}

		@Override
		public void retrieveObservations() throws ObservationReadError,
				InterruptedException {

			setJDflavour(JDflavour.HJD);

			// read lines and determine the number of them 
			getNumberOfRecords();

			int headerLineNum = -1;
			
			// look for the header line
			for (int i = 0; i < lines.size(); i++) {
				String line = lines.get(i);
				if (line != null ) {
					line = line.trim();
					if (HEADER_LINE_START.equals(line.substring(0, HEADER_LINE_START.length()))) {
						// header found
						headerLineNum = i;
						break;
					}
				}
				incrementProgress();
			}
			
			if (headerLineNum < 0)
				throw new ObservationReadError("Cannot find ZTF header");
				
			// The header has been found. Read the rest of lines.
			for (int i = headerLineNum + 1; i < lines.size(); i++) {
				String line = lines.get(i);				
				try {
					if (line != null ) {
						line = line.trim();
						if (!"".equals(line)) {
							String[] fields = line.split(delimiter);
							ValidObservation vo = readNextObservation(fields, i + 1);
							collectObservation(vo);
						}
					}
				} catch (Exception e) {
					// Create an invalid observation.
					String error = e.getLocalizedMessage();
					InvalidObservation ob = new InvalidObservation(line, error);
					ob.setRecordNumber(i + 1);
					addInvalidObservation(ob);
				}
				incrementProgress();
			}
		}

		@Override
		public Integer getNumberOfRecords() throws ObservationReadError {
			if (lines == null) {
				try {
					readLines();
				} catch (IOException e) {
					throw new ObservationReadError("Error reading lines");
				}
			}

			return lines.size();
		}

		// Read all lines from the source.
		private void readLines() throws IOException {
			lines = new ArrayList<String>();

			BufferedReader reader = new BufferedReader(new InputStreamReader(getInputStreams().get(0)));

			String line = null;

			while ((line = reader.readLine()) != null) {
				lines.add(line);
			}
		}

		// ZTF format observation reader.
		// see https://irsa.ipac.caltech.edu/docs/program_interface/ztf_lightcurve_api.html
		//
		private ValidObservation readNextObservation(String[] fields, int recordNumber)
				throws ObservationValidationError {

			ValidObservation observation = new ValidObservation();

			String name = "ZTF object " + fields[0].trim();
			objectName = name;

			observation.setRecordNumber(recordNumber);
			observation.setName(name);
			observation.setObsCode(obscode);

			DateInfo dateInfo = new DateInfo(julianDayValidator.validate(fields[2].trim()).getJulianDay());
			observation.setDateInfo(dateInfo);

			Magnitude magnitude = magnitudeFieldValidator.validate(fields[4].trim());
			observation.setMagnitude(magnitude);
			double uncertainty = uncertaintyValueValidator.validate(fields[5].trim());
			observation.getMagnitude().setUncertainty(uncertainty);

			String filter = fields[7].trim();
			SeriesType band;
			if (filter.equals("zg")) {
				band = ztfgSeries;
			} else if (filter.equals("zr")) {
				band = ztfrSeries;
			} else if (filter.equals("zi")) {
				band = ztfiSeries;
			} else {
				band = ztfUnknownSeries;
			}
			observation.setBand(band);

			// ValidObservation defaults to STD.
			observation.setMType(MTypeType.STD);

			//observation.setComments("");

			observation.addDetail("CATFLAGS", fields[6], "catflags");
			observation.addDetail("EXPTIME", fields[21], "exptime");
			observation.addDetail("AIRMASS", fields[22], "airmass");
			// todo: add other details
			
			return observation;
		}

		@Override
		public String getSourceName() {
			return objectName;
		}

		@Override
		public String getSourceType() {
			return "ZTF Format";
		}
	}

	@SuppressWarnings("serial")
	class ZTFParameterDialog extends AbstractOkCancelDialog {

		private TextField objectIDField;
		private JCheckBox additiveLoadCheckbox;

		public ZTFParameterDialog(String ztfObjectID, boolean additiveChecked) {
			super("ZTF Object");

			Container contentPane = this.getContentPane();

			JPanel topPane = new JPanel();
			topPane.setLayout(new BoxLayout(topPane, BoxLayout.PAGE_AXIS));
			topPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

			topPane.add(createParameterPane(ztfObjectID));

			topPane.add(velaFilterFieldPanelPair.second);

			topPane.add(createAdditiveLoadCheckboxPane(additiveChecked));

			// OK, Cancel
			topPane.add(createButtonPane());

			contentPane.add(topPane);

			this.pack();
			setLocationRelativeTo(Mediator.getUI().getContentPane());
			this.setVisible(true);
		}

		private JPanel createParameterPane(String ztfObjectID) {
			JPanel panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

			objectIDField = new TextField("ZTF object ID", ztfObjectID);
			panel.add(objectIDField.getUIComponent());
			panel.add(Box.createRigidArea(new Dimension(75, 10)));

			return panel;
		}

		private JPanel createAdditiveLoadCheckboxPane(boolean checked) {
			JPanel panel = new JPanel();
			panel.setBorder(BorderFactory.createTitledBorder("Additive Load"));

			additiveLoadCheckbox = new JCheckBox("Add to current?", checked);
			panel.add(additiveLoadCheckbox);

			return panel;
		}

		public String getObjectID() {
			return objectIDField.getValue();
		}

		/**
		 * Return whether or not the load is additive.
		 * 
		 * @return Whether or not the load is additive.
		 */
		public boolean isLoadAdditive() {
			return additiveLoadCheckbox.isSelected();
		}

		/**
		 * @return The VeLa filter string.
		 */
		public String getVelaFilterStr() {
			return velaFilterFieldPanelPair.first.getValue().trim();
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

			if (ok) {
				cancelled = false;
				setVisible(false);
				dispose();
			}
		}
	}

}
