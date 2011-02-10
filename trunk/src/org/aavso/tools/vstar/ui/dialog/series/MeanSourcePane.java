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
import java.util.Enumeration;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.ui.model.plot.ObservationAndMeanPlotModel;

/**
 * This class represents a pane with radio buttons showing which series is to be
 * used as the source to create the means series. The source series can be
 * modified.
 */
public class MeanSourcePane extends JPanel implements ActionListener {

	private ObservationAndMeanPlotModel obsPlotModel;

	private int seriesNum;
	private int lastSelectedSeriesNum;

	private ButtonGroup seriesGroup;

	/**
	 * Constructor
	 */
	public MeanSourcePane(ObservationAndMeanPlotModel obsPlotModel) {
		super();

		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		this.setBorder(BorderFactory.createTitledBorder("Mean Series Source"));
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
		this.add(Box.createRigidArea(new Dimension(75, 1)));

		seriesGroup = new ButtonGroup();

		for (SeriesType series : this.obsPlotModel.getSeriesKeys()) {
			// We want to be able to select from any series except
			// "means", "fainter-than", and "discrepant".
			// Actually, we may want to be able to select from discrepants, e.g.
			// consider comments from Aaron re: this.
			// TODO: we could delegate this filtering to SeriesType
			if (series != SeriesType.MEANS && series != SeriesType.FAINTER_THAN
					&& series != SeriesType.DISCREPANT) {
				String seriesName = series.getDescription();
				JRadioButton seriesRadioButton = new JRadioButton(seriesName);
				seriesRadioButton.setActionCommand(seriesName);
				seriesRadioButton.addActionListener(this);
				this.add(seriesRadioButton);
				this.add(Box.createRigidArea(new Dimension(3, 3)));
				seriesGroup.add(seriesRadioButton);

				if (obsPlotModel.getSrcTypeToSeriesNumMap().get(series) == obsPlotModel
						.getMeanSourceSeriesNum()) {
					seriesRadioButton.setSelected(true);
					lastSelectedSeriesNum = obsPlotModel
							.getMeanSourceSeriesNum();
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
		this.seriesNum = obsPlotModel.getSrcTypeToSeriesNumMap().get(
				SeriesType.getSeriesFromDescription(seriesName));

		if (this.seriesNum != obsPlotModel.getMeanSourceSeriesNum()) {
			obsPlotModel.setMeanSourceSeriesNum(this.seriesNum);
			boolean changed = obsPlotModel.changeMeansSeries(obsPlotModel
					.getTimeElementsInBin());
			if (changed) {
				lastSelectedSeriesNum = obsPlotModel.getMeanSourceSeriesNum();
			} else {
				// Means not changed (e.g. too few data points in selected
				// series), so restore radio buttons to last selected.
				SeriesType seriesToRevertTo = obsPlotModel
						.getSeriesNumToSrcTypeMap().get(lastSelectedSeriesNum);

				Enumeration<AbstractButton> elts = seriesGroup.getElements();
				
				while (elts.hasMoreElements()) {
					AbstractButton radioButton = elts.nextElement();
					String label = radioButton.getActionCommand();
					if (SeriesType.getSeriesFromDescription(label) == seriesToRevertTo) {
						radioButton.setSelected(true);
						obsPlotModel
								.setMeanSourceSeriesNum(lastSelectedSeriesNum);
					}
				}
			}
		}
	}

	/**
	 * @return the seriesNum
	 */
	public int getSeriesNum() {
		return seriesNum;
	}
}
