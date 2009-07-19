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
package org.aavso.tools.vstar.ui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.aavso.tools.vstar.ui.model.ObservationAndMeanPlotModel;

/**
 * This class represents a chart pane containing a plot for a set of valid
 * observations along with mean-based data.
 */
public class ObservationAndMeanPlotPane extends ObservationPlotPane {

	private ObservationAndMeanPlotModel obsAndMeanModel;

	// Should the means series elements be joined visually?
	private boolean joinMeans;

	/**
	 * Constructor
	 * 
	 * @param title
	 *            The title for the chart.
	 * @param obsModel
	 *            The data model to plot.
	 * @param bounds
	 *            The bounding box to which to set the chart's preferred size.
	 */
	public ObservationAndMeanPlotPane(String title,
			ObservationAndMeanPlotModel obsAndMeanModel, Dimension bounds) {
		super(title, obsAndMeanModel, bounds);
		this.obsAndMeanModel = obsAndMeanModel;
		this.joinMeans = true;
	}

	/**
	 * @see org.aavso.tools.vstar.ui.ObservationPlotPane#createChartControlPanel(javax.swing.JPanel)
	 */
	protected void createChartControlPanel(JPanel chartControlPanel) {
		super.createChartControlPanel(chartControlPanel);

		// A checkbox to determine whether or not to join mean series elements.
		JCheckBox joinMeansCheckBox = new JCheckBox("Join means?");
		joinMeansCheckBox.setSelected(true);
		joinMeansCheckBox.addActionListener(createJoinMeansCheckBoxListener());
		chartControlPanel.add(joinMeansCheckBox);

		// TODO: add...
		// - slider/spinbox for days-per-bin and an associated update button
	}

	// Return a listener for the "join means visually" checkbox.
	private ActionListener createJoinMeansCheckBoxListener() {
		final ObservationAndMeanPlotPane self = this;
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				self.toggleJoinMeansSetting();
			}
		};
	}

	// Toggle the "join means" setting which dictates whether or 
	// not the means are visually joined (by lines).
	private void toggleJoinMeansSetting() {
		this.joinMeans = !this.joinMeans;
		this.getRenderer().setSeriesLinesVisible(
				this.obsAndMeanModel.getMeansSeriesNum(), this.joinMeans);
	}
}
