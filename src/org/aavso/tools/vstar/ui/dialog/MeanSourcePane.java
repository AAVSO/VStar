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
package org.aavso.tools.vstar.ui.dialog;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.aavso.tools.vstar.ui.model.ObservationAndMeanPlotModel;
import org.aavso.tools.vstar.ui.model.SeriesType;

/**
 * This class represents a pane with radio buttons showing which series is to be
 * used as the source to create the means series. The source series can be
 * modified.
 */
public class MeanSourcePane extends JPanel implements ActionListener {

	private ObservationAndMeanPlotModel obsPlotModel;
	private int seriesNum;

	/**
	 * Constructor
	 */
	public MeanSourcePane(ObservationAndMeanPlotModel obsPlotModel) {
		super();

		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		this.setBorder(BorderFactory.createTitledBorder("Means Source"));
		this
				.setToolTipText("Select series that will be the source of the means series.");

		this.obsPlotModel = obsPlotModel;
		this.seriesNum = obsPlotModel.getMeanSourceSeriesNum();

		addSeriesRadioButtons();
	}

	// Create a radio button for each series, selecting the one
	// that corresponds to the current mean source series.
	private void addSeriesRadioButtons() {
		// Ensure the panel is always wide enough.
		this.add(Box.createRigidArea(new Dimension(75, 10)));

		ButtonGroup seriesGroup = new ButtonGroup();

		for (String seriesName : this.obsPlotModel.getSeriesKeys()) {
			// We want to be able to select from any series except
			// "means", "fainter-than", and "discrepant".
			// TODO: and, again, we could delegate this filtering to SeriesType
			if (!SeriesType.MEANS.getName().equalsIgnoreCase(seriesName)
					&& !SeriesType.FAINTER_THAN.getName().equalsIgnoreCase(
							seriesName)
					&& !SeriesType.DISCREPANT.getName().equalsIgnoreCase(
							seriesName)) {
				JRadioButton seriesRadioButton = new JRadioButton(seriesName);
				seriesRadioButton.setActionCommand(seriesName);
				seriesRadioButton.addActionListener(this);
				this.add(seriesRadioButton);
				this.add(Box.createRigidArea(new Dimension(10, 10)));
				seriesGroup.add(seriesRadioButton);

				if (this.obsPlotModel.getSrcNameToSeriesNumMap()
						.get(seriesName) == this.obsPlotModel
						.getMeanSourceSeriesNum()) {
					seriesRadioButton.setSelected(true);
				}
			}
		}
	}

	// This method will be called when a radio button is selected.
	// If the selected series is different from the model's current
	// mean source series number (in the model), set the model's mean
	// source series number.
	public void actionPerformed(ActionEvent e) {
		String seriesName = e.getActionCommand();
		this.seriesNum = obsPlotModel.getSrcNameToSeriesNumMap()
				.get(seriesName);

		if (this.seriesNum != obsPlotModel.getMeanSourceSeriesNum()) {
			obsPlotModel.setMeanSourceSeriesNum(this.seriesNum);
		}
	}

	/**
	 * @return the seriesNum
	 */
	public int getSeriesNum() {
		return seriesNum;
	}
}
