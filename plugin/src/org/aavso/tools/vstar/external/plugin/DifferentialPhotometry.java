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

import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;

import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.plugin.ObservationToolPluginBase;
import org.aavso.tools.vstar.ui.dialog.AbstractOkCancelDialog;
import org.aavso.tools.vstar.ui.dialog.DoubleField;
import org.aavso.tools.vstar.ui.dialog.ITextComponent;
import org.aavso.tools.vstar.ui.dialog.MultiEntryComponentDialog;
import org.aavso.tools.vstar.ui.dialog.TextDialog;
import org.aavso.tools.vstar.ui.dialog.TextField;
import org.aavso.tools.vstar.ui.dialog.TextField.Kind;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.model.plot.ISeriesInfoProvider;
import org.aavso.tools.vstar.ui.model.plot.JDTimeElementEntity;
import org.aavso.tools.vstar.util.prefs.NumericPrecisionPrefs;
import org.aavso.tools.vstar.util.stats.DescStats;

/**
 * This plug-in allows photometry to be carried out using loaded data on the
 * assumption that such data constitute instrumental magnitudes.
 * 
 * @author David Benn
 * @version 1.0 - 6 Apr 2015
 * 
 */
public class DifferentialPhotometry extends ObservationToolPluginBase {

	private boolean firstInvocation;

	private ISeriesInfoProvider seriesInfo;

	private SeriesType targetSeries;
	private SeriesType checkSeries;
	private List<SeriesType> compSeries;
	private Map<SeriesType, Double> refMags;

	public DifferentialPhotometry() {
		firstInvocation = true;
		compSeries = new ArrayList<SeriesType>();
		refMags = new HashMap<SeriesType, Double>();
	}

	@Override
	public String getDisplayName() {
		return "Differential photometry";
	}

	@Override
	public String getDescription() {
		return "Differential photometry from loaded observations";
	}

	@Override
	public void invoke(ISeriesInfoProvider seriesInfo) {
		this.seriesInfo = seriesInfo;

		if (firstInvocation) {
			firstInvocation = false;
			// TODO: new star listener to clear previous values
		}

		// Get the target star series.
		SingleSeriesSelectionDialog targetDialog = new SingleSeriesSelectionDialog(
				"Target", targetSeries, null, true);
		if (!targetDialog.isCancelled()) {
			targetSeries = targetDialog.getSelectedSeries();
		} else {
			return;
		}

		// Get the check star series (optional).
		SingleSeriesSelectionDialog checkDialog = new SingleSeriesSelectionDialog(
				"Check", checkSeries, targetSeries, false);
		if (!checkDialog.isCancelled()) {
			checkSeries = checkDialog.getSelectedSeries();
		}

		// Get the comparison star series (1 or more).
		ComparisonStarSeriesSelectionDialog compDialog = new ComparisonStarSeriesSelectionDialog(
				"Comps", targetSeries, checkSeries);
		if (!compDialog.isCancelled()) {
			compSeries = compDialog.getSelectedSeries();
		} else {
			return;
		}

		// Get the reference (comparison and check) star catalog values.
		if (!getRefStarCatalogValues()) {
			return;
		}

		showStats();
	}

	// Helpers

	@SuppressWarnings("serial")
	class SingleSeriesSelectionDialog extends AbstractOkCancelDialog implements
			ActionListener {

		private SeriesType previousSelection;
		private boolean isSelectionRequired;
		private JPanel seriesPane;
		private ButtonGroup seriesGroup;
		private SeriesType selectedSeries;
		private SeriesType seriesToExclude;

		public SingleSeriesSelectionDialog(String title,
				SeriesType previousSelection, SeriesType seriesToExclude,
				boolean isSelectionRequired) {
			super(title);
			this.previousSelection = previousSelection;
			this.seriesToExclude = seriesToExclude;
			this.isSelectionRequired = isSelectionRequired;

			Container contentPane = this.getContentPane();

			JPanel topPane = new JPanel();
			topPane.setLayout(new BoxLayout(topPane, BoxLayout.PAGE_AXIS));
			topPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

			seriesPane = new JPanel();
			seriesPane
					.setLayout(new BoxLayout(seriesPane, BoxLayout.LINE_AXIS));
			seriesPane.add(createSeriesRadioButtons());

			topPane.add(new JScrollPane(seriesPane));

			topPane.add(Box.createRigidArea(new Dimension(10, 10)));
			topPane.add(createButtonPane());

			contentPane.add(topPane);

			this.pack();
			this.setLocationRelativeTo(Mediator.getUI().getContentPane());
			this.setVisible(true);
		}

		/**
		 * @return the selectedSeries
		 */
		public SeriesType getSelectedSeries() {
			return selectedSeries;
		}

		private JPanel createSeriesRadioButtons() {
			JPanel panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

			seriesGroup = new ButtonGroup();

			for (SeriesType series : seriesInfo.getSeriesKeys()) {
				if (!series.isSynthetic() && !series.isUserDefined()
						&& series != SeriesType.DISCREPANT
						&& series != SeriesType.Excluded
						&& !seriesInfo.getObservations(series).isEmpty()
						&& series != seriesToExclude) {

					String seriesName = series.getDescription();
					JRadioButton seriesRadioButton = new JRadioButton(
							seriesName);

					if (series == previousSelection) {
						seriesRadioButton.setSelected(true);
						selectedSeries = series;
					}

					seriesRadioButton.setActionCommand(seriesName);
					seriesRadioButton.addActionListener(this);
					panel.add(seriesRadioButton);
					panel.add(Box.createRigidArea(new Dimension(3, 3)));
					seriesGroup.add(seriesRadioButton);
				}
			}

			// Ensure the panel is wide enough for textual border.
			panel.add(Box.createRigidArea(new Dimension(75, 1)));

			return panel;
		}

		// This method will be called when a radio button is selected.
		// If the selected series is different from the model's last
		// singly selected series number, store it in the model.
		public void actionPerformed(ActionEvent e) {
			String seriesName = e.getActionCommand();
			selectedSeries = SeriesType.getSeriesFromDescription(seriesName);
		}

		@Override
		protected void okAction() {
			if (!isSelectionRequired || selectedSeries != null) {
				cancelled = false;
				setVisible(false);
				dispose();
			}
		}

		@Override
		protected void cancelAction() {
			// Nothing to do
		}
	}

	@SuppressWarnings("serial")
	class ComparisonStarSeriesSelectionDialog extends AbstractOkCancelDialog {

		private SeriesType targetSeries;
		private SeriesType checkSeries;
		private JPanel seriesPane;
		private List<JCheckBox> checkBoxes;

		private List<SeriesType> selectedSeries;

		public ComparisonStarSeriesSelectionDialog(String title,
				SeriesType targetSeries, SeriesType checkSeries) {
			super(title);
			this.targetSeries = targetSeries;
			this.checkSeries = checkSeries;
			selectedSeries = new ArrayList<SeriesType>();

			Container contentPane = this.getContentPane();

			JPanel topPane = new JPanel();
			topPane.setLayout(new BoxLayout(topPane, BoxLayout.PAGE_AXIS));
			topPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

			seriesPane = new JPanel();
			seriesPane
					.setLayout(new BoxLayout(seriesPane, BoxLayout.LINE_AXIS));
			seriesPane.add(createSeriesCheckBoxes());

			topPane.add(new JScrollPane(seriesPane));

			topPane.add(Box.createRigidArea(new Dimension(10, 10)));
			topPane.add(createButtonPane());

			contentPane.add(topPane);

			this.pack();
			this.setLocationRelativeTo(Mediator.getUI().getContentPane());
			this.setVisible(true);
		}

		/**
		 * @return the selectedSeries
		 */
		public List<SeriesType> getSelectedSeries() {
			return selectedSeries;
		}

		// Create a checkbox for each candidate comparison star series, grouped
		// by type.
		private JPanel createSeriesCheckBoxes() {
			JPanel panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

			// Ensure the panel is always wide enough.
			this.add(Box.createRigidArea(new Dimension(75, 1)));

			checkBoxes = new ArrayList<JCheckBox>();

			// compSeries.clear();

			for (SeriesType series : seriesInfo.getSeriesKeys()) {
				if (!series.isSynthetic() && !series.isUserDefined()
						&& series != SeriesType.DISCREPANT
						&& series != SeriesType.Excluded
						&& series != targetSeries && series != checkSeries
						&& !seriesInfo.getObservations(series).isEmpty()) {

					String seriesName = series.getDescription();
					JCheckBox checkBox = new JCheckBox(seriesName);

					if (compSeries.contains(series)) {
						checkBox.setSelected(true);
					}

					panel.add(checkBox);
					panel.add(Box.createRigidArea(new Dimension(3, 3)));

					checkBoxes.add(checkBox);
				}
			}

			return panel;
		}

		@Override
		protected void okAction() {
			selectedSeries.clear();
			for (JCheckBox checkBox : checkBoxes) {
				if (checkBox.isSelected()) {
					String name = checkBox.getText();
					selectedSeries.add(SeriesType
							.getSeriesFromDescription(name));
				}
			}

			if (!selectedSeries.isEmpty()) {
				cancelled = false;
				setVisible(false);
				dispose();
			}
		}

		@Override
		protected void cancelAction() {
			// Nothing to do
		}
	}

	/**
	 * Populate the catalog values of the check and comparison stars and return.
	 * 
	 * @return whether the request was successful
	 */
	private boolean getRefStarCatalogValues() {

		Set<SeriesType> refSeries = new TreeSet<SeriesType>();
		refSeries.addAll(compSeries);
		if (checkSeries != null) {
			refSeries.add(checkSeries);
		}

		List<DoubleField> catMagFields = new ArrayList<DoubleField>();
		for (SeriesType series : refSeries) {
			catMagFields.add(new DoubleField(series.getDescription(), null,
					null, refMags.containsKey(series) ? refMags.get(series)
							: null));
		}

		// TODO: fix this redundancy!
		List<ITextComponent<?>> dialogFields = new ArrayList<ITextComponent<?>>();
		for (DoubleField field : catMagFields) {
			dialogFields.add(field);
		}

		MultiEntryComponentDialog dlg = new MultiEntryComponentDialog(
				"Cat Mags", dialogFields);

		refMags.clear();

		if (!dlg.isCancelled()) {
			for (DoubleField catMagField : catMagFields) {
				Double value = catMagField.getValue();
				if (value != null) {
					refMags.put(SeriesType.getSeriesFromDescription(catMagField
							.getName()), value);
				}
			}
		}

		return !dlg.isCancelled();
	}

	// Show summary statistics for each selected series along with differential
	// magnitudes and buttons to access statistics for each member of each
	// series.
	private void showStats() {
		List<ITextComponent<String>> infoFields = new ArrayList<ITextComponent<String>>();

		// Compute the mean check (optionally) and target series magnitudes
		double checkMagMean = 0;

		if (checkSeries != null) {
			List<ValidObservation> checkObs = seriesInfo
					.getObservations(checkSeries);

			checkMagMean = DescStats.calcMagMeanInRange(checkObs,
					JDTimeElementEntity.instance, 0, checkObs.size() - 1)[0];
		}

		List<ValidObservation> targetObs = seriesInfo
				.getObservations(targetSeries);

		double targetMagMean = DescStats.calcMagMeanInRange(targetObs,
				JDTimeElementEntity.instance, 0, targetObs.size() - 1)[0];

		// Summary statistics and differential magnitudes per series...
		for (SeriesType series : seriesInfo.getSeriesKeys()) {
			if (refMags.containsKey(series) || series == targetSeries) {
				List<ValidObservation> obs = seriesInfo.getObservations(series);

				double[] means = DescStats.calcMagMeanInRange(obs,
						JDTimeElementEntity.instance, 0, obs.size() - 1);
				double jdMean = means[1];
				double magMean = means[0];

				double stdev = DescStats.calcMagSampleStdDevInRange(obs, 0,
						obs.size() - 1);

				String jdMeanStr = NumericPrecisionPrefs.formatTime(jdMean);
				String magMeanStr = NumericPrecisionPrefs.formatMag(magMean);
				String magStdevStr = NumericPrecisionPrefs.formatMag(stdev);

				String infoStr = null;

				// For each comparison star, show the computed magnitude for the
				// check (if provided, along with difference from catalog value)
				// and target stars. For the check and target star, just show
				// the instrumental magnitude.
				if (series != targetSeries && series != checkSeries) {
					infoStr = String.format("%s: %s (%s)", jdMeanStr,
							magMeanStr, magStdevStr);

					if (checkSeries != null) {
						double checkMag = diffMag(checkMagMean, magMean,
								refMags.get(series));
						double checkMagDelta = Math.abs(refMags
								.get(checkSeries) - checkMag);

						String checkMagStr = NumericPrecisionPrefs
								.formatMag(checkMag);
						String checkMagDeltaStr = NumericPrecisionPrefs
								.formatMag(checkMagDelta);

						infoStr += String.format("\n  %s: %s (%s)",
								checkSeries.getDescription(), checkMagStr,
								checkMagDeltaStr);
					}

					double targetMag = diffMag(targetMagMean, magMean,
							refMags.get(series));
					String targetMagStr = NumericPrecisionPrefs
							.formatMag(targetMag);

					infoStr += String.format("\n  %s: %s",
							targetSeries.getDescription(), targetMagStr);
				} else {
					infoStr = String.format("%s: %s (%s)", jdMeanStr,
							magMeanStr, magStdevStr);
				}

				infoFields.add(new TextField(series.getDescription(), infoStr,
						Kind.AREA));
			}
		}

		new TextDialog("Photometry Results", infoFields);
	}

	/**
	 * Given a target and reference star instrumental magnitude and reference
	 * star catalog magnitude, compute the differential target magnitude.
	 * 
	 * See Eq 3 in Standardised Photometry section of DSLR Photometry Manual.
	 * 
	 * @param targetIMag
	 *            The target star instrumental magnitude.
	 * @param refIMag
	 *            The reference star instrumental magnitude.
	 * @param refCatMag
	 *            The reference star catalog magnitude.
	 * @return The differential target magnitude.
	 */
	private double diffMag(double targetIMag, double refIMag, double refCatMag) {
		return (targetIMag - refIMag) + refCatMag;
	}
}
