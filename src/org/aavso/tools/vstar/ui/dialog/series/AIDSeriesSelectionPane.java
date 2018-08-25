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
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.util.locale.LocaleProps;

/**
 * This class represents a pane with checkboxes for AID series allowing
 * selection of multiple series.
 */
@SuppressWarnings("serial")
public class AIDSeriesSelectionPane extends JPanel {

	private List<JCheckBox> checkBoxes;

	/**
	 * Constructor
	 */
	public AIDSeriesSelectionPane() {
		super();

		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		this.setBorder(BorderFactory.createTitledBorder(LocaleProps
				.get("BAND_TO_LOAD")));
		this.setToolTipText("Select or deselect desired series.");

		this.checkBoxes = new ArrayList<JCheckBox>();

		addSeriesCheckBoxes();

		addButtons();
	}

	/**
	 * Return a list of the selected series.
	 */
	public List<SeriesType> getSelectedSeries() {
		List<SeriesType> list = new ArrayList<SeriesType>();

		checkBoxes.stream().forEach(
				checkBox -> {
					if (checkBox.isSelected()) {
						list.add(SeriesType.getSeriesFromDescription(checkBox
								.getText()));
					}
				});

		return list;
	}

	/**
	 * Set all checkboxes to the target state.
	 * 
	 * @param targetState
	 *            The target check-button state.
	 */
	public void setAllCheckboxesToState(boolean targetState) {
		checkBoxes.stream().forEach(checkBox -> {
			if (checkBox.isEnabled()) {
				checkBox.setSelected(targetState);
			}
		});
	}

	/**
	 * Set checkboxes for specified series to the requested states.
	 * 
	 * @param seriesStates
	 *            A series-description to state map.
	 */
	public void setCheckboxesToStates(Map<String,Boolean> seriesStates) {
		checkBoxes.stream().forEach(checkBox -> {
			if (seriesStates.containsKey(checkBox.getText())) {
				checkBox.setSelected(seriesStates.get(checkBox.getText()));
			}
		});
	}

	// Create a checkbox for each series, grouped by type.
	private void addSeriesCheckBoxes() {
		JTabbedPane tabs = new JTabbedPane();

		// tabs.addTab(
		// "Common",
		// createSeriesCheckboxes(SeriesType.Visual, SeriesType.Johnson_V,
		// SeriesType.Johnson_B, SeriesType.Johnson_R,
		// SeriesType.Johnson_I,
		// SeriesType.Unfiltered_with_V_Zeropoint,
		// SeriesType.Unfiltered_with_R_Zeropoint,
		// SeriesType.Tri_Color_Green, SeriesType.Tri_Color_Blue,
		// SeriesType.Tri_Color_Red, SeriesType.Cousins_I,
		// SeriesType.Cousins_R));
		
		tabs.addTab(
				"Standard",
				createSeriesCheckboxes(SeriesType.Visual, SeriesType.Johnson_V,
						SeriesType.Johnson_B, SeriesType.Johnson_R,
						SeriesType.Johnson_I, SeriesType.Cousins_I, SeriesType.Cousins_R));

		tabs.addTab("Unfiltered",
				createFilteredSeriesCheckboxPanel("Unfiltered"));

		tabs.addTab("Tri-Color", createFilteredSeriesCheckboxPanel("Tri-Color"));

//		tabs.addTab("Cousins", createFilteredSeriesCheckboxPanel("Cousins"));

		tabs.addTab(
				"NIR",
				createSeriesCheckboxes(SeriesType.K_NIR_2pt2micron,
						SeriesType.H_NIR_1pt6micron,
						SeriesType.J_NIR_1pt2micron));

		tabs.addTab("Sloan", createFilteredSeriesCheckboxPanel("Sloan"));

		tabs.addTab("Stromgren", createFilteredSeriesCheckboxPanel("Stromgren"));

		tabs.addTab("Optec Wing",
				createFilteredSeriesCheckboxPanel("Optec Wing"));

		tabs.addTab("PanSTARRS", createFilteredSeriesCheckboxPanel("PanSTARRS"));

		tabs.addTab("Halpha", createFilteredSeriesCheckboxPanel("Halpha"));

		tabs.addTab(
				"Color",
				createSeriesCheckboxes(SeriesType.Orange_Liller,
						SeriesType.Red, SeriesType.Blue, SeriesType.Green,
						SeriesType.Yellow, SeriesType.Clear_Blue_Blocking));

		this.add(tabs);
	}

	// Return a panel of checkboxes for a subset of series whose names have the
	// specified prefix. Could make this a predicate rather than a prefix for
	// generality.
	private JPanel createFilteredSeriesCheckboxPanel(String prefix) {
		return createSeriesCheckboxes(SeriesType.values().stream()
				.filter(series -> series.getDescription().startsWith(prefix))
				.toArray(SeriesType[]::new));
	}

	// Create series checkboxes in a panel and return it.
	private JPanel createSeriesCheckboxes(SeriesType... seriesTypes) {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		// panel.setBorder(BorderFactory.createTitledBorder(""));

		// Ensure the panel is always wide enough.
		this.add(Box.createRigidArea(new Dimension(75, 1)));

		for (int i = 0; i < seriesTypes.length; i++) {
			SeriesType series = seriesTypes[i];
			JCheckBox checkBox = new JCheckBox(series.getDescription());
			selectDefaultSeriesCheckbox(checkBox);
			panel.add(checkBox);
			panel.add(Box.createRigidArea(new Dimension(3, 3)));

			checkBoxes.add(checkBox);
		}

		return panel;
	}

	/**
	 * Does the checkbox correspond to a default-to-load series? If so, set the
	 * checkbox's state to selected.
	 * 
	 * @param checkBox
	 *            The checkbox in question.
	 */
	private void selectDefaultSeriesCheckbox(JCheckBox checkBox) {
		String seriesName = checkBox.getText();
		if (seriesName.equals(LocaleProps.get("VISUAL_SERIES"))
				|| seriesName.equals("Johnson V")) {
			checkBox.setSelected(true);
		}
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
	 * Return a listener for the "select/deselect all" checkbox.
	 * 
	 * @param targetState
	 *            The target check-button state.
	 * @return The button listener.
	 */
	private ActionListener createEnMasseSelectionButtonListener(
			final boolean targetState) {
		return (e -> {
			setAllCheckboxesToState(targetState);
		});
	}
}
