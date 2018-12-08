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
package org.aavso.tools.vstar.plugin.ob.src.impl;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import org.aavso.tools.vstar.data.DateInfo;
import org.aavso.tools.vstar.data.Magnitude;
import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.exception.CancellationException;
import org.aavso.tools.vstar.exception.ObservationReadError;
import org.aavso.tools.vstar.input.AbstractObservationRetriever;
import org.aavso.tools.vstar.plugin.InputType;
import org.aavso.tools.vstar.plugin.ObservationSourcePluginBase;
import org.aavso.tools.vstar.ui.dialog.AbstractOkCancelDialog;
import org.aavso.tools.vstar.ui.dialog.DoubleField;
import org.aavso.tools.vstar.ui.dialog.TextDialog;
import org.aavso.tools.vstar.ui.dialog.TextField;
import org.aavso.tools.vstar.ui.mediator.Mediator;

/**
 * The base class for URL based AAVSO photometry observation source plugins.
 */
public class AAVSOPhotometryURLObservationSourceBase extends
		ObservationSourcePluginBase {

	private final String kind;

	protected final String baseURL;
	protected final Map<String, SeriesType> seriesNameToTypeMap;

	protected Locale locale;

	// Current parameter values.
	protected double raDegs;
	protected double decDegs;
	protected double radiusDegs;
	protected Set<String> seriesNames;
	protected boolean isHeliocentricObsSrc;

	// Ordered list of series.
	protected List<SeriesType> seriesList;

	/**
	 * Constructor
	 * 
	 * @param kind
	 *            The kind of photometry observation source this is.
	 * @param baseURL
	 *            The base URL to which query parameters can be added.
	 * @param user
	 *            The user name to pass to the authenticator.
	 * @param password
	 *            The password to pass to the authenticator.
	 * @param isHeliocentricObsSrc
	 *            Will the observations for this plug-in have heliocentric JD
	 *            values?
	 */
	public AAVSOPhotometryURLObservationSourceBase(String kind, String baseURL,
			String user, String password, boolean isHeliocentricObsSrc) {
		super(user, password);
		this.kind = kind;
		this.baseURL = baseURL;
		this.seriesNameToTypeMap = new LinkedHashMap<String, SeriesType>();
		this.seriesNames = new HashSet<String>();
		this.isHeliocentricObsSrc = isHeliocentricObsSrc;

		locale = Locale.getDefault();
	}

	@Override
	public InputType getInputType() {
		return InputType.URL;
	}

	@Override
	public AbstractObservationRetriever getObservationRetriever() {
		return new AAVSOPhotometryURLObservationRetriever();
	}

	@Override
	public String getDescription() {
		String str = kind + " epoch photometry observation source plug-in.";

		if (locale.equals("es")) {
			// TODO
		}

		return str;
	}

	@Override
	public String getDisplayName() {
		String str = "New Star from " + kind + " epoch photometry database...";

		if (locale.equals("es")) {
			// TODO
		}

		return str;
	}

	@Override
	public List<URL> getURLs() throws Exception {
		List<URL> urls = new ArrayList<URL>();
		seriesList = new ArrayList<SeriesType>();

		AAVSOPhotometryURLSearchParameterDialog paramDialog = new AAVSOPhotometryURLSearchParameterDialog();

		if (!paramDialog.isCancelled()) {
			raDegs = paramDialog.getRADeg();
			decDegs = paramDialog.getDecDeg();
			radiusDegs = paramDialog.getRadiusDeg();
			setAdditive(paramDialog.isLoadAdditive());

			// We want numbers in the the URL to correspond to the server's
			// locale!
			String params = String
					.format(Locale.ENGLISH, "radeg=%f&decdeg=%f&raddeg=%f",
							raDegs, decDegs, radiusDegs);

			for (String seriesName : seriesNameToTypeMap.keySet()) {
				try {
					if (seriesNames.contains(seriesName)) {
						URL url = new URL(baseURL + params + "&filter="
								+ seriesName);
						urls.add(url);
						seriesList.add(seriesNameToTypeMap.get(seriesName));
					}
				} catch (MalformedURLException e) {
					throw new ObservationReadError("Cannot construct " + kind
							+ " URL (reason: " + e.getLocalizedMessage() + ")");
				}
			}
		} else {
			throw new CancellationException();
		}

		return urls;
	}

	@Override
	public String getInputName() {
		String desc = String.format(": RA=%f, Dec=%f, radius=%f, filter=",
				raDegs, decDegs, radiusDegs);
		for (String seriesName : seriesNameToTypeMap.keySet()) {
			if (seriesNames.contains(seriesName)) {
				desc += seriesName + ", ";
			}
		}
		desc = desc.substring(0, desc.lastIndexOf(", "));
		return kind + desc;
	}

	@Override
	public Set<SeriesType> getVisibleSeriesTypes() {
		return new LinkedHashSet<SeriesType>(seriesList);
	}

	class AAVSOPhotometryURLObservationRetriever extends
			AbstractObservationRetriever {

		public AAVSOPhotometryURLObservationRetriever() {
			setHeliocentric(isHeliocentricObsSrc);
		}

		@Override
		public void retrieveObservations() throws ObservationReadError,
				InterruptedException {
			try {
				List<InputStream> streams = getInputStreams();

				if (streams == null || seriesList == null
						|| streams.size() != seriesList.size()) {
					// If getURLs() has completed correctly, we should not get
					// to this point.
					throw new ObservationReadError(kind
							+ " input stream configuration error.");
				}

				int i = 0;
				for (InputStream stream : streams) {
					retrieveAAVSOPhotometryURLObs(stream, seriesList.get(i));
					i++;
				}
			} catch (IOException e) {
				throw new ObservationReadError(e.getLocalizedMessage());
			}
		}

		/**
		 * Retrieve a set of observations for a particular filter.
		 * 
		 * @param stream
		 *            The observation input stream.
		 * @param series
		 *            The series type to be used for the observations' band.
		 * @throws IOException
		 *             if a HTTP I/O error occurs.
		 * @throws ObservationReadError
		 *             if an error occurs during observation.
		 */
		private void retrieveAAVSOPhotometryURLObs(InputStream stream,
				SeriesType series) throws IOException, ObservationReadError {
			LineNumberReader reader = new LineNumberReader(
					new InputStreamReader(stream));

			String line = reader.readLine();

			while (line != null) {
				if (line.contains("An error occured while trying to connect to database")) {
					throw new ObservationReadError("Cannot access " + kind
							+ " database.");
				} else if (line.contains("No rows were returned by query")) {
					break;
				} else if (line.startsWith("#")) {
					if (inputName == null) {
						setInputInfo(null, line.substring(2).trim());
					}
				} else {
					String[] fields = line.split(",");

					double jd = Double.parseDouble(fields[0]);
					double mag = Double.parseDouble(fields[1]);
					double error = Double.parseDouble(fields[2]);

					ValidObservation ob = new ValidObservation();
					ob.setDateInfo(new DateInfo(jd));
					ob.setMagnitude(new Magnitude(mag, error));
					ob.setBand(series);
					ob.setRecordNumber(reader.getLineNumber());

					if (fields.length == 4) {
						// Source field.
						int id = 0;
						try {
							id = Integer.parseInt(fields[3]);
						} catch (NumberFormatException e) {
							// Nothing to do.
						}
						String source = AAVSOPhotometrySource.fromID(id)
								.toString();
						ob.addDetail("SOURCE", source, "Source");
					}

					collectObservation(ob);
				}

				line = reader.readLine();
			}
		}

		@Override
		public String getSourceName() {
			return getInputName();
		}

		@Override
		public String getSourceType() {
			String str = kind + " epoch photometry database";

			if (locale.equals("es")) {
				// TODO
			}

			return str;
		}
	}

	@SuppressWarnings("serial")
	class AAVSOPhotometryURLSearchParameterDialog extends
			AbstractOkCancelDialog {

		private DoubleField raDegField;
		private DoubleField decDegField;
		private JComboBox radiusDegSelector;
		private List<JCheckBox> checkBoxes;
		private JCheckBox additiveLoadCheckbox;
		private TextField urlField;

		/**
		 * Constructor
		 */
		public AAVSOPhotometryURLSearchParameterDialog() {
			super(kind + " Search Parameters");

			Container contentPane = this.getContentPane();

			JPanel topPane = new JPanel();
			topPane.setLayout(new BoxLayout(topPane, BoxLayout.PAGE_AXIS));
			topPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

			topPane.add(createParameterPane());

			topPane.add(createAdditiveLoadCheckboxPane());

			// OK, Cancel
			topPane.add(createButtonPane());

			contentPane.add(topPane);

			this.pack();
			setLocationRelativeTo(Mediator.getUI().getContentPane());
			this.setVisible(true);
		}

		private JPanel createParameterPane() {
			JPanel panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

			raDegField = new DoubleField("RA (degrees)", 0.0, 360.0, raDegs);
			panel.add(raDegField.getUIComponent());
			panel.add(Box.createRigidArea(new Dimension(75, 10)));

			decDegField = new DoubleField("Dec (degrees)", -90.0, 90.0, decDegs);
			panel.add(decDegField.getUIComponent());
			panel.add(Box.createRigidArea(new Dimension(75, 10)));

			String[] radii = new String[] { "0.001 deg = 3.6 arcsec",
					"0.002 deg = 7.2 arcsec", "0.005 deg = 18.0 arcsec" };
			radiusDegSelector = new JComboBox(radii);
			radiusDegSelector.setBorder(BorderFactory
					.createTitledBorder("Radius (degrees"));
			panel.add(radiusDegSelector);
			panel.add(Box.createRigidArea(new Dimension(75, 10)));

			panel.add(createDataSeriesCheckboxes());
			panel.add(Box.createRigidArea(new Dimension(75, 10)));

			panel.add(createUrlPane());
			panel.add(Box.createRigidArea(new Dimension(75, 10)));

			return panel;
		}

		private JPanel createAdditiveLoadCheckboxPane() {
			JPanel panel = new JPanel();
			panel.setBorder(BorderFactory.createTitledBorder("Additive Load"));

			additiveLoadCheckbox = new JCheckBox("Add to current?");
			panel.add(additiveLoadCheckbox);

			return panel;
		}

		private JPanel createDataSeriesCheckboxes() {
			JPanel panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
			panel.setBorder(BorderFactory.createTitledBorder("Bands"));

			checkBoxes = new ArrayList<JCheckBox>();

			// Ensure the panel is always wide enough.
			this.add(Box.createRigidArea(new Dimension(75, 1)));

			for (String seriesName : seriesNameToTypeMap.keySet()) {
				JCheckBox checkBox = new JCheckBox(seriesName);

				checkBox.addActionListener(createSeriesVisibilityCheckBoxListener());

				checkBox.setSelected(seriesNames.contains(seriesName));

				panel.add(checkBox);
				panel.add(Box.createRigidArea(new Dimension(3, 3)));

				checkBoxes.add(checkBox);
			}

			return panel;
		}

		// Return a listener for the series visibility checkboxes.
		private ActionListener createSeriesVisibilityCheckBoxListener() {
			return new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JCheckBox checkBox = (JCheckBox) e.getSource();
					String seriesName = checkBox.getText();
					if (checkBox.isSelected()
							&& !seriesNames.contains(seriesName)) {
						seriesNames.add(seriesName);
					} else if (!checkBox.isSelected()
							&& seriesNames.contains(seriesName)) {
						seriesNames.remove(seriesName);
					}
				}
			};
		}

		/**
		 * This component provides a URL request button and corresponding
		 * action.
		 */
		private JPanel createUrlPane() {
			final Pattern raParamPattern = Pattern
					.compile("^.+radeg=(\\d+(\\.\\d+)?).+$");

			final Pattern decParamPattern = Pattern
					.compile("^.+decdeg=((\\-|\\+)?\\d+(\\.\\d+)?).+$");

			final Pattern radiusParamPattern = Pattern
					.compile("^.+raddeg=(\\d+\\.\\d+).*$");

			JPanel pane = new JPanel();

			JButton urlRequestButton = new JButton(
					"Populate Parameters From VSX URL");

			urlRequestButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					urlField = new TextField("URL");
					TextDialog urlDialog = new TextDialog("Enter URL", urlField);
					if (!urlDialog.isCancelled()
							&& !urlField.getValue().matches("^\\s*$")) {

						// Set RA, Dec, radius from URL and select all
						// checkboxes since the VSX URL contains no filter
						// information, returning all available.
						Matcher raMatcher = raParamPattern.matcher(urlField
								.getStringValue());

						if (raMatcher.matches()) {
							raDegField.setValue(Double.parseDouble(raMatcher
									.group(1)));
						}

						Matcher decMatcher = decParamPattern.matcher(urlField
								.getStringValue());

						if (decMatcher.matches()) {
							decDegField.setValue(Double.parseDouble(decMatcher
									.group(1)));
						}

						Matcher radiusMatcher = radiusParamPattern
								.matcher(urlField.getStringValue());

						if (radiusMatcher.matches()) {
							String radiusStr = radiusMatcher.group(1);
							switch (radiusStr) {
							case "0.001":
								radiusDegSelector.setSelectedIndex(0);
								break;
							case "0.002":
								radiusDegSelector.setSelectedIndex(1);
								break;
							case "0.005":
								radiusDegSelector.setSelectedIndex(2);
								break;
							}
						}

						for (JCheckBox checkbox : checkBoxes) {
							checkbox.setSelected(true);
						}
					}
				}
			});

			pane.add(urlRequestButton);

			return pane;
		}

		public double getRADeg() {
			return raDegField.getValue();
		}

		public double getDecDeg() {
			return decDegField.getValue();
		}

		public double getRadiusDeg() {
			// We'll just parse the first part of the string, which contains the
			// degrees value.
			String[] fields = ((String) radiusDegSelector.getSelectedItem())
					.split("\\s+");
			return Double.parseDouble(fields[0]);
		}

		/**
		 * @return The URL string.
		 */
		public String getUrlString() {
			return urlField.getValue().trim();
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

			if (raDegField.getValue() == null || decDegField.getValue() == null) {
				ok = false;
			}

			if (ok) {
				cancelled = false;
				setVisible(false);
				dispose();
			}
		}
	}

}
