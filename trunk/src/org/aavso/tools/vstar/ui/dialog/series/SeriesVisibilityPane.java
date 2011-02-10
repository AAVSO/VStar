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

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.ui.model.plot.ObservationPlotModel;

/**
 * This class represents a pane with checkboxes showing those series that are
 * rendered. The series to be displayed can be changed.
 */
public class SeriesVisibilityPane extends JPanel {

	private ObservationPlotModel obsPlotModel;
	private Map<Integer, Boolean> visibilityDeltaMap;
	private List<JCheckBox> checkBoxes;

	/**
	 * Constructor
	 */
	public SeriesVisibilityPane(ObservationPlotModel obsPlotModel) {
		super();
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		this.setBorder(BorderFactory.createTitledBorder("Visibility"));
		this
				.setToolTipText("Select or deselect series for desired visibility.");

		this.obsPlotModel = obsPlotModel;
		this.visibilityDeltaMap = new HashMap<Integer, Boolean>();

		addSeriesCheckBoxes();
		addButtons();
	}

	// Create a checkbox for each series.
	private void addSeriesCheckBoxes() {
		// Ensure the panel is always wide enough.
		this.add(Box.createRigidArea(new Dimension(75, 1)));

		checkBoxes = new ArrayList<JCheckBox>();

		for (SeriesType series : this.obsPlotModel.getSeriesKeys()) {
			String seriesName = series.getDescription();
			JCheckBox checkBox = new JCheckBox(seriesName);
			checkBox
					.addActionListener(createSeriesVisibilityCheckBoxListener());
			int seriesNum = obsPlotModel.getSrcTypeToSeriesNumMap().get(series);
			boolean vis = obsPlotModel.getSeriesVisibilityMap().get(seriesNum);
			checkBox.setSelected(vis);
			this.add(checkBox);
			this.add(Box.createRigidArea(new Dimension(3, 3)));

			checkBoxes.add(checkBox);
		}
	}

	// Return a listener for the series visibility checkboxes.
	private ActionListener createSeriesVisibilityCheckBoxListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JCheckBox checkBox = (JCheckBox) e.getSource();
				updateSeriesVisibilityMap(checkBox);
				seriesVisibilityChange(getVisibilityDeltaMap());
			}
		};
	}

	// Create buttons for en-masse selection/deselection
	// of visibility checkboxes and an apply button.
	private void addButtons() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
		panel.setBorder(BorderFactory.createEtchedBorder());

		JButton selectAllButton = new JButton("Select All");
		selectAllButton
				.addActionListener(createEnMasseSelectionButtonListener(true));
		panel.add(selectAllButton, BorderLayout.LINE_START);

		JButton deSelectAllButton = new JButton("Deselect All");
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
					checkBox.setSelected(target);
					updateSeriesVisibilityMap(checkBox);
				}

				seriesVisibilityChange(getVisibilityDeltaMap());
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
}
