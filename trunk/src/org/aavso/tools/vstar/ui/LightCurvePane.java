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

import org.aavso.tools.vstar.ui.model.ObservationPlotModel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;

/**
 * This class represents a chart pane containing a light curve.
 */
public class LightCurvePane extends ChartPanel {

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
	public LightCurvePane(String title, ObservationPlotModel obsModel, Dimension bounds) {
		// Create a chart with legend, tooltips, and URLs showing
		// and add it to the panel.
		super(ChartFactory.createScatterPlot(title, "Julian Day",
				"Magnitude", obsModel, PlotOrientation.VERTICAL, true, true,
				true));

		this.setPreferredSize(bounds);
		
		JFreeChart chart = this.getChart();
		
		// We want the magnitude scale to go from high to low as we ascend the Y axis
		// since as magnitude values get smaller, brightness increases.
		NumberAxis rangeAxis = (NumberAxis) chart.getXYPlot().getRangeAxis();
		rangeAxis.setInverted(true);
	}
}
