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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.ui.model.plot.ObservationAndMeanPlotModel;

/**
 * This class defines a pane with radio buttons for all series permitting a
 * single series to be selected.
 */
public class SingleSeriesSelectionPane extends JPanel implements ActionListener {

	private ObservationAndMeanPlotModel obsPlotModel;
	private ButtonGroup seriesGroup;

	/**
	 * Constructor.
	 */
	public SingleSeriesSelectionPane(ObservationAndMeanPlotModel obsPlotModel) {
		super();

		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		this.setToolTipText("Select a single series.");

		this.obsPlotModel = obsPlotModel;

		// If no series has yet been selected, choose the
		// current mean source series since it's likely to be of primary
		// interest.
		if (obsPlotModel.getLastSinglySelectedSeries() == null) {
			int seriesNum = obsPlotModel.determineMeanSeriesSource();
			obsPlotModel.setLastSinglySelectedSeries(obsPlotModel
					.getSeriesNumToSrcTypeMap().get(seriesNum));
		}

		addSeriesRadioButtons();
	}

	// Create a radio button for each series, selecting the one
	// that corresponds to the current mean source series.
	private void addSeriesRadioButtons() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));

		panel.add(createDataSeriesRadioButtons());
		panel.add(createOtherSeriesRadioButtons());

		this.add(panel);
	}

	private JPanel createDataSeriesRadioButtons() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.setBorder(BorderFactory.createTitledBorder("Data"));

		seriesGroup = new ButtonGroup();

		SeriesType selectedSeries = obsPlotModel.getLastSinglySelectedSeries();

		for (SeriesType series : this.obsPlotModel.getSeriesKeys()) {
			if (!series.isSynthetic()) {
				// Add radio button for all non-synthetic series.
				String seriesName = series.getDescription();
				JRadioButton seriesRadioButton = new JRadioButton(seriesName);
				seriesRadioButton.setActionCommand(seriesName);
				seriesRadioButton.addActionListener(this);
				seriesRadioButton.setEnabled(isSeriesNonEmpty(series));
				panel.add(seriesRadioButton);
				panel.add(Box.createRigidArea(new Dimension(3, 3)));
				seriesGroup.add(seriesRadioButton);

				// Select the initial series radio button.
				if (series == selectedSeries) {
					seriesRadioButton.setSelected(true);
				}
			}
		}

		// Ensure the panel is wide enough for textual border.
		panel.add(Box.createRigidArea(new Dimension(75, 1)));

		return panel;
	}

	private JPanel createOtherSeriesRadioButtons() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.setBorder(BorderFactory.createTitledBorder("Analysis"));

		SeriesType selectedSeries = obsPlotModel.getLastSinglySelectedSeries();

		for (SeriesType series : this.obsPlotModel.getSeriesKeys()) {
			if (series.isSynthetic()) {
				// Add radio button for all non-synthetic series.
				String seriesName = series.getDescription();
				JRadioButton seriesRadioButton = new JRadioButton(seriesName);
				seriesRadioButton.setActionCommand(seriesName);
				seriesRadioButton.addActionListener(this);
				seriesRadioButton.setEnabled(isSeriesNonEmpty(series));
				panel.add(seriesRadioButton);
				panel.add(Box.createRigidArea(new Dimension(3, 3)));
				seriesGroup.add(seriesRadioButton);

				// Select the initial series radio button.
				if (series == selectedSeries) {
					seriesRadioButton.setSelected(true);
				}
			}
		}

		return panel;
	}

	/**
	 * Does the specified series have corresponding observations?
	 * 
	 * @param type
	 *            The series type in question.
	 * @return Whether or not there are observations for this series type.
	 */
	private boolean isSeriesNonEmpty(SeriesType type) {
		Integer num = obsPlotModel.getSrcTypeToSeriesNumMap().get(type);

		// This series exists and has obs (or not), so allow it to be selected
		// (or not).
		boolean hasObs = obsPlotModel.getSeriesNumToObSrcListMap().containsKey(
				num)
				&& !obsPlotModel.getSeriesNumToObSrcListMap().get(num)
						.isEmpty();

		return hasObs;
	}

	// This method will be called when a radio button is selected.
	// If the selected series is different from the model's last
	// singly selected series number, store it in the model.
	public void actionPerformed(ActionEvent e) {
		String seriesName = e.getActionCommand();
		SeriesType selectedSeries = SeriesType
				.getSeriesFromDescription(seriesName);

		if (selectedSeries != obsPlotModel.getLastSinglySelectedSeries()) {
			obsPlotModel.setLastSinglySelectedSeries(selectedSeries);
		}
	}
}
