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

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.aavso.tools.vstar.ui.model.ObservationPlotModel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.xy.XYErrorRenderer;

// TODO: either fork this class to handle obs vs obs-with-means plots
// or generalise for both

/**
 * This class represents a chart pane containing a plot for a set of valid
 * observations.
 */
public class ObservationPlotPane extends JPanel {

	private ChartPanel chartPanel;

	private JTextArea obsInfo;

	// We use this renderer in order to be able to plot error bars.
	// TODO: or should we use StatisticalLineAndShapeRenderer? (for means plot?)
	private XYErrorRenderer renderer;

	// Show error bars?
	private boolean showErrorBars;

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
	public ObservationPlotPane(String title, ObservationPlotModel obsModel,
			Dimension bounds) {
		super();

		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

		this.showErrorBars = true;

		// Create a chart with legend, tooltips, and URLs showing
		// and add it to the panel.
		this.chartPanel = new ChartPanel(ChartFactory.createScatterPlot(title,
				"Julian Day", "Magnitude", obsModel, PlotOrientation.VERTICAL,
				true, true, true));

		this.chartPanel.setPreferredSize(bounds);

		JFreeChart chart = chartPanel.getChart();

		this.renderer = new XYErrorRenderer();
		this.renderer.setDrawYError(this.showErrorBars);
		
		// Tell renderer which series's elements should be rendered
		// as visually joined with lines.
		for (int series : obsModel.getSeriesWhoseElementsShouldBeJoinedVisually()) {
			this.renderer.setSeriesLinesVisible(series, true);
		}

		chart.getXYPlot().setRenderer(renderer);

		// We want the magnitude scale to go from high to low as we ascend the
		// Y axis since as magnitude values get smaller, brightness increases.
		NumberAxis rangeAxis = (NumberAxis) chart.getXYPlot().getRangeAxis();
		rangeAxis.setInverted(true);

		this.add(chartPanel);

		this.add(Box.createRigidArea(new Dimension(0, 10)));

		// Checkbox to show/hide error bars.
		JCheckBox errorBarCheckBox = new JCheckBox("Show error bars?");
		errorBarCheckBox.setSelected(this.showErrorBars);
		errorBarCheckBox.addActionListener(createErrorBarCheckBoxListener());
		this.add(errorBarCheckBox);

		this.add(Box.createRigidArea(new Dimension(0, 10)));

		// Create the observation information text area.
		this.obsInfo = new JTextArea();
		obsInfo.setBorder(BorderFactory.createEtchedBorder());
		obsInfo.setPreferredSize(new Dimension((int) (bounds.width),
				(int) (bounds.height * 0.1)));
		obsInfo.setEditable(false);
		this.add(obsInfo);
	}

	/**
	 * @return the chartPanel
	 */
	public ChartPanel getChartPanel() {
		return chartPanel;
	}

	/**
	 * Show/hide the error bars.
	 */
	public void toggleErrorBars() {
		this.showErrorBars = !this.showErrorBars;
		this.renderer.setDrawYError(this.showErrorBars);
	}

	// Return a listener for the error bar visibility checkbox.
	private ActionListener createErrorBarCheckBoxListener() {
		final ObservationPlotPane self = this;
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				self.showErrorBars = !self.showErrorBars;
				self.renderer.setDrawYError(self.showErrorBars);
			}
		};
	}
}
