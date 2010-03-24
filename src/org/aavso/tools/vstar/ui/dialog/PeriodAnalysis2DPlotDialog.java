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

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.aavso.tools.vstar.ui.MainFrame;
import org.aavso.tools.vstar.ui.model.plot.PeriodAnalysis2DPlotModel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;

/**
 * This class is used to render 2D period analysis plots.
 */
public class PeriodAnalysis2DPlotDialog extends JDialog {

	protected JFreeChart chart;

	protected ChartPanel chartPanel;

	// protected XYErrorRenderer renderer;

	/**
	 * Constructor
	 * 
	 * @param title
	 *            The title for the chart.
	 * @param domainTitle
	 *            The domain title (e.g. Julian Date, phase).
	 * @param rangeTitle
	 *            The range title (e.g. magnitude).
	 * @param model
	 *            The data model to plot.
	 */
	public PeriodAnalysis2DPlotDialog(String title,
			String domainTitle,
			String rangeTitle, PeriodAnalysis2DPlotModel model) {
		super();
		this.setTitle(title);
		this.setModal(false);

		// Create a line chart with legend, tooltips, and URLs showing
		// and add it to the panel.
		this.chartPanel = new ChartPanel(ChartFactory.createXYLineChart(title,
				domainTitle, rangeTitle, model, PlotOrientation.VERTICAL, true,
				true, true));

		this.chart = chartPanel.getChart();

		// this.renderer = new VStarPlotDataRenderer();
		// this.chart.getXYPlot().setRenderer(this.renderer);

		// TODO: may need tabbed panes, one per range type (period, power,
		// amplitude)
		
		// TODO: may also need table with data
		
		// TODO: need progress bar (of "busy" variety)
		
		JPanel topPane = new JPanel();
		topPane.setLayout(new BoxLayout(topPane, BoxLayout.PAGE_AXIS));
		topPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		topPane.add(chartPanel);

		this.getContentPane().add(topPane);

		this.pack();
		this.setLocationRelativeTo(MainFrame.getInstance().getContentPane());
		this.setAlwaysOnTop(true);
		this.setVisible(true);
	}
}
