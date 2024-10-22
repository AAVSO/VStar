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
package org.aavso.tools.vstar.ui.dialog.series;

import java.awt.BorderLayout;
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
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.ui.mediator.AnalysisType;
import org.aavso.tools.vstar.ui.model.plot.ObservationAndMeanPlotModel;
import org.aavso.tools.vstar.util.locale.LocaleProps;
import org.aavso.tools.vstar.util.notification.Listener;
import org.aavso.tools.vstar.util.stats.BinningResult;

/**
 * This class represents a pane with checkboxes showing those series that are
 * rendered. The series to be displayed can be changed.
 *
 * TODO: rename as MultipleSeriesSelectionPane
 */
@SuppressWarnings("serial")
public class SeriesVisibilityPane extends JPanel {

	private ObservationAndMeanPlotModel obsPlotModel;
	private AnalysisType analysisType;

	private Map<Integer, Boolean> visibilityDeltaMap;

	private List<JCheckBox> checkBoxes;

	private JCheckBox discrepantCheckBox;
	private JCheckBox excludedCheckBox;

	private JCheckBox meanCheckBox;
	private JCheckBox filteredCheckBox;
	private JCheckBox modelCheckBox;
	private JCheckBox residualsCheckBox;

	private int discrepantCount;
	private int excludedCount;

	private boolean includeSynthetic;
	private boolean modifyVisibility;

	/**
	 * Constructor.
	 * 
	 * @param obsPlotModel
	 *            The plot model.
	 * @param analysisType
	 *            The analysis type.
	 * @param includeSynthetic
	 *            Include synthetic series?
	 * @param modifyVisibility
	 *            Modify series visibility?
	 */
	public SeriesVisibilityPane(ObservationAndMeanPlotModel obsPlotModel,
			AnalysisType analysisType, boolean includeSynthetic,
			boolean modifyVisibility) {
		this(obsPlotModel, analysisType, includeSynthetic, modifyVisibility,
				true);
	}

	/**
	 * Constructor.
	 * 
	 * @param obsPlotModel
	 *            The plot model.
	 * @param analysisType
	 *            The analysis type.
	 * @param includeSynthetic
	 *            Include synthetic series?
	 * @param modifyVisibility
	 *            Modify series visibility?
	 * @param showVisibilityBorderTitle
	 *            show "Series Visibility" border title? For some uses of the
	 *            dialog, we just want to see the series checkboxes.
	 */
	public SeriesVisibilityPane(ObservationAndMeanPlotModel obsPlotModel,
			AnalysisType analysisType, boolean includeSynthetic,
			boolean modifyVisibility, boolean showVisibilityBorderTitle) {
		super();

		this.obsPlotModel = obsPlotModel;
		this.analysisType = analysisType;

		this.includeSynthetic = includeSynthetic;
		this.modifyVisibility = modifyVisibility;

		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		if (showVisibilityBorderTitle) {
			this.setBorder(BorderFactory.createTitledBorder(LocaleProps
					.get("VISIBILITY_TITLE")));
		}
		this.setToolTipText("Select or deselect series.");

		this.visibilityDeltaMap = new HashMap<Integer, Boolean>();

		this.checkBoxes = new ArrayList<JCheckBox>();

		filteredCheckBox = null;
		modelCheckBox = null;

		addSeriesCheckBoxes();

		// Get initial discrepant and excluded counts, if any.

		Integer discrepantSeriesNum = obsPlotModel.getSrcTypeToSeriesNumMap()
				.get(SeriesType.DISCREPANT);
		discrepantCount = obsPlotModel.getSeriesNumToObSrcListMap()
				.get(discrepantSeriesNum).size();

		Integer excludedSeriesNum = obsPlotModel.getSrcTypeToSeriesNumMap()
				.get(SeriesType.Excluded);
		excludedCount = obsPlotModel.getSeriesNumToObSrcListMap()
				.get(excludedSeriesNum).size();

		obsPlotModel.getMeansChangeNotifier().addListener(
				createMeanObsChangeListener());

		addButtons();
	}

	/**
	 * Constructor for pane in which synthetic series are included and series
	 * visibility is modified.
	 * 
	 * @param obsPlotModel
	 *            The plot model.
	 * @param analysisType
	 *            The analysis type.
	 */
	public SeriesVisibilityPane(ObservationAndMeanPlotModel obsPlotModel,
			AnalysisType analysisType) {
		this(obsPlotModel, analysisType, true, true);
	}

	/**
	 * Return the complete set of selected series.
	 * 
	 * @return The selected series.
	 */
	public Set<SeriesType> getSelectedSeries() {
		Set<SeriesType> series = new TreeSet<SeriesType>();

		for (JCheckBox checkBox : checkBoxes) {
			if (checkBox.isSelected()) {
				String desc = checkBox.getText();
				series.add(SeriesType.getSeriesFromDescription(desc));
			}
		}

		return series;
	}

	// Create a checkbox for each series, grouped by type.
	private void addSeriesCheckBoxes() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
		panel.add(createDataSeriesCheckboxes());
		if (includeSynthetic) {
			panel.add(createDerivedSeriesCheckboxes());
			JPanel userPanel = createUserDefinedSeriesCheckboxes();
			if (userPanel != null) {
				panel.add(userPanel);
			}
		}
		this.add(panel);
	}

	private JPanel createDataSeriesCheckboxes() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.setBorder(BorderFactory.createTitledBorder(LocaleProps
				.get("DATA_TITLE")));

		// Ensure the panel is always wide enough.
		this.add(Box.createRigidArea(new Dimension(75, 1)));

		for (SeriesType series : this.obsPlotModel.getSeriesKeys()) {
			// We treat derived and user-defined series separately from data
			// series.
			if (!series.isSynthetic() && !series.isUserDefined()) {
				String seriesName = series.getDescription();
				JCheckBox checkBox = new JCheckBox(seriesName);

				checkBox.addActionListener(createSeriesVisibilityCheckBoxListener());

				// Enable/disable the series.
				boolean vis = obsPlotModel.getSeriesVisibilityMap().get(series);
				checkBox.setSelected(vis);

				panel.add(checkBox);
				panel.add(Box.createRigidArea(new Dimension(3, 3)));

				checkBoxes.add(checkBox);

				Integer seriesNum = obsPlotModel.getSrcTypeToSeriesNumMap()
						.get(series);

				// Listeners need access to discrepant and excluded checkboxes.
				// We also set the initial state for these checkboxes
				// conditionally, depending upon whether any observations are
				// present in these series. If they are already selected, it is
				// not disabled, e.g. catering for the case where a single
				// discrepant or excluded observation is undone.
				if (series == SeriesType.DISCREPANT) {
					discrepantCheckBox = checkBox;
					if (!discrepantCheckBox.isSelected()
							&& obsPlotModel.getSeriesNumToObSrcListMap()
									.get(seriesNum).isEmpty()) {
						setInitialCheckBoxState(series, discrepantCheckBox);
					}
				} else if (series == SeriesType.Excluded) {
					excludedCheckBox = checkBox;
					if (!excludedCheckBox.isSelected()
							&& obsPlotModel.getSeriesNumToObSrcListMap()
									.get(seriesNum).isEmpty()) {
						setInitialCheckBoxState(series, excludedCheckBox);
					}
				}
			}
		}

		return panel;
	}

	private JPanel createDerivedSeriesCheckboxes() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.setBorder(BorderFactory.createTitledBorder(LocaleProps
				.get("ANALYSIS_TITLE")));

		// Mean series.
		meanCheckBox = new JCheckBox(SeriesType.MEANS.getDescription());
		meanCheckBox
				.addActionListener(createSeriesVisibilityCheckBoxListener());
		setInitialCheckBoxState(SeriesType.MEANS, meanCheckBox);
		panel.add(meanCheckBox);
		panel.add(Box.createRigidArea(new Dimension(3, 3)));
		checkBoxes.add(meanCheckBox);

		// Filtered series.
		filteredCheckBox = new JCheckBox(SeriesType.Filtered.getDescription());
		filteredCheckBox
				.addActionListener(createSeriesVisibilityCheckBoxListener());
		setInitialCheckBoxState(SeriesType.Filtered, filteredCheckBox);
		panel.add(filteredCheckBox);
		panel.add(Box.createRigidArea(new Dimension(3, 3)));
		checkBoxes.add(filteredCheckBox);

		JPanel subPanel = new JPanel();
		subPanel.setLayout(new BoxLayout(subPanel, BoxLayout.PAGE_AXIS));
		// TODO: why bother with this panel? Just use parent Analysis panel!
		subPanel.setBorder(BorderFactory.createTitledBorder(LocaleProps
				.get("MODEL_TITLE")));
		subPanel.add(Box.createRigidArea(new Dimension(75, 1)));

		// Model series.
		modelCheckBox = new JCheckBox(SeriesType.Model.getDescription());
		modelCheckBox
				.addActionListener(createSeriesVisibilityCheckBoxListener());
		setInitialCheckBoxState(SeriesType.Model, modelCheckBox);
		subPanel.add(modelCheckBox);
		subPanel.add(Box.createRigidArea(new Dimension(3, 3)));
		checkBoxes.add(modelCheckBox);

		// Residuals series.
		residualsCheckBox = new JCheckBox(SeriesType.Residuals.getDescription());
		residualsCheckBox
				.addActionListener(createSeriesVisibilityCheckBoxListener());
		setInitialCheckBoxState(SeriesType.Residuals, residualsCheckBox);
		subPanel.add(residualsCheckBox);
		checkBoxes.add(residualsCheckBox);

		panel.add(subPanel);

		return panel;
	}

	private JPanel createUserDefinedSeriesCheckboxes() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.setBorder(BorderFactory.createTitledBorder(LocaleProps
				.get("USER_DEFINED_TITLE")));

		// Ensure the panel is always wide enough.
		this.add(Box.createRigidArea(new Dimension(75, 1)));

		boolean anyObs = false;

		for (SeriesType series : this.obsPlotModel.getSeriesKeys()) {

			if (series.isUserDefined()) {
				// Ignore user-defined series with no corresponding data in the
				// current dataset.
				Integer seriesNum = obsPlotModel.getSrcTypeToSeriesNumMap()
						.get(series);

				if (obsPlotModel.getSeriesNumToObSrcListMap().get(seriesNum)
						.isEmpty()) {
					continue;
				} else {
					if (!anyObs) {
						anyObs = true;
					}
				}

				String seriesName = series.getDescription();
				JCheckBox checkBox = new JCheckBox(seriesName);

				checkBox.addActionListener(createSeriesVisibilityCheckBoxListener());

				// Enable/disable the series.
				boolean vis = obsPlotModel.getSeriesVisibilityMap().get(series);
				checkBox.setSelected(vis);

				panel.add(checkBox);
				panel.add(Box.createRigidArea(new Dimension(3, 3)));

				checkBoxes.add(checkBox);
			}
		}

		if (!anyObs) {
			panel = null;
		}

		return panel;
	}

	/**
	 * Set the enabled and selected states of the checkbox corresponding to the
	 * specified series type according to the plot model's visibility map.
	 * 
	 * @param seriesType
	 *            The series type in question.
	 * @param checkbox
	 *            The checkbox whose state is to be set.
	 */
	protected void setInitialCheckBoxState(SeriesType seriesType,
			JCheckBox checkbox) {
		Integer seriesNum = obsPlotModel.getSrcTypeToSeriesNumMap().get(
				seriesType);

		// This series exists and has obs (or not), so allow it to be selected
		// (or not).
		boolean hasObs = obsPlotModel.getSeriesNumToObSrcListMap().containsKey(
				seriesNum)
				&& !obsPlotModel.getSeriesNumToObSrcListMap().get(seriesNum)
						.isEmpty();
		checkbox.setEnabled(hasObs);

		// The series is (or is not) marked as being visible so also select
		// (or don't) the checkbox. A series visibility may not have been set in
		// the map yet, unless it has been selected by default (e.g. visual
		// bands) or by the user.
		boolean visible = obsPlotModel.getSeriesVisibilityMap().containsKey(
				seriesType)
				&& obsPlotModel.getSeriesVisibilityMap().get(seriesType);
		checkbox.setSelected(visible);
	}

	// Return a listener for the series visibility checkboxes.
	private ActionListener createSeriesVisibilityCheckBoxListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (modifyVisibility) {
					JCheckBox checkBox = (JCheckBox) e.getSource();
					updateSeriesVisibilityMap(checkBox);
					seriesVisibilityChange(getVisibilityDeltaMap());
				}
			}
		};
	}

	// Create buttons for en-masse selection/deselection
	// of visibility checkboxes and an apply button.
	private void addButtons() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
		panel.setBorder(BorderFactory.createEtchedBorder());

		JButton selectAllButton = new JButton(
				LocaleProps.get("SELECT_ALL_BUTTON"));
		selectAllButton
				.addActionListener(createEnMasseSelectionButtonListener(true));
		panel.add(selectAllButton, BorderLayout.LINE_START);

		JButton deSelectAllButton = new JButton(
				LocaleProps.get("DESELECT_ALL_BUTTON"));
		deSelectAllButton
				.addActionListener(createEnMasseSelectionButtonListener(false));
		panel.add(deSelectAllButton, BorderLayout.LINE_END);

		this.add(panel, BorderLayout.CENTER);
	}

	/**
	 * Was there a change in the series visibility? Some callers may want to
	 * invoke this only for its side effects, while others may also want to know
	 * the result.
	 * 
	 * @param deltaMap
	 *            A mapping from series number to whether or not each series'
	 *            visibility was changed.
	 * 
	 * @return Was there a change in the visibility of any series?
	 */
	protected boolean seriesVisibilityChange(Map<Integer, Boolean> deltaMap) {
		boolean delta = false;

		for (int seriesNum : deltaMap.keySet()) {
			boolean visibility = deltaMap.get(seriesNum);
			delta |= obsPlotModel.changeSeriesVisibility(seriesNum, visibility);
		}

		return delta;
	}

	/**
	 * Return a listener for the "select/deselect all" checkbox.
	 * 
	 * @param target
	 *            The target check-button state.
	 * @return The button listener.
	 */
	private ActionListener createEnMasseSelectionButtonListener(
			final boolean target) {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for (JCheckBox checkBox : checkBoxes) {
					if (checkBox.isEnabled()) {
						checkBox.setSelected(target);
						if (modifyVisibility) {
						    updateSeriesVisibilityMap(checkBox);
						}
					}
				}

                if (modifyVisibility) {
                    seriesVisibilityChange(getVisibilityDeltaMap());
                }
			}
		};
	}

	/**
	 * Update the series visibility map according to the state of the
	 * checkboxes.
	 * 
	 * @param checkBox
	 *            The checkbox whose state we want to update from.
	 */
	private void updateSeriesVisibilityMap(JCheckBox checkBox) {
		String seriesName = checkBox.getText();
		int seriesNum = obsPlotModel.getSrcTypeToSeriesNumMap().get(
				SeriesType.getSeriesFromDescription(seriesName));
		visibilityDeltaMap.put(seriesNum, checkBox.isSelected());
	}

	/**
	 * @return the visibilityDeltaMap
	 */
	public Map<Integer, Boolean> getVisibilityDeltaMap() {
		return visibilityDeltaMap;
	}

	// Return a mean observation change listener to ensure that the
	// mean series checkbox is selected if a new binning operation takes
	// place that also set the mean series as visible, assuming it was not
	// already.
	private Listener<BinningResult> createMeanObsChangeListener() {
		return new Listener<BinningResult>() {
			@Override
			public void update(BinningResult info) {
				// Check that the series was actually marked as visible in the
				// model!
				boolean meanSeriesVisible = obsPlotModel
						.getSeriesVisibilityMap().get(SeriesType.MEANS);
				if (meanSeriesVisible) {
					meanCheckBox.setSelected(true);
				}
			}

			@Override
			public boolean canBeRemoved() {
				return true;
			}
		};
	}
}
