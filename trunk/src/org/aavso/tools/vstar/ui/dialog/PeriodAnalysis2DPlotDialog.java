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

import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.aavso.tools.vstar.ui.MainFrame;
import org.aavso.tools.vstar.ui.model.plot.PeriodAnalysis2DPlotModel;
import org.aavso.tools.vstar.util.period.PeriodAnalysisCoordinateType;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;

/**
 * This class is used to render 2D period analysis plots.
 */
public class PeriodAnalysis2DPlotDialog extends JDialog {

	private String chartTitle;
	private String domainTitle;
	private List<PeriodAnalysis2DPlotModel> models;

	/**
	 * Constructor
	 * 
	 * @param title
	 *            The title for the chart.
	 * @param domainTitle
	 *            The domain title (e.g. Julian Date, phase).
	 * @param model
	 *            The data models on which to base plots.
	 */
	public PeriodAnalysis2DPlotDialog(String title, String domainTitle,
			List<PeriodAnalysis2DPlotModel> models) {
		super();

		this.setTitle(title);
		this.setModal(false);

		this.chartTitle = title;
		this.domainTitle = domainTitle;
		this.models = models;

		// TODO: may also need table with data

		// JPanel topPane = new JPanel();
		// topPane.setLayout(new BoxLayout(topPane, BoxLayout.PAGE_AXIS));
		// topPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		//
		// topPane.add(chartPanel);

		// this.getContentPane().add(topPane);

		this.getContentPane().add(getTabs());

		this.pack();
		this.setLocationRelativeTo(MainFrame.getInstance().getContentPane());
		this.setAlwaysOnTop(true); // TODO: or false?
		this.setVisible(true);
	}

	// Return the tabs containing plots of frequency vs one of the dependent
	// variables of period, power, or amplitude. Is this what we want, or
	// something
	// different?
	private JTabbedPane getTabs() {
		JTabbedPane tabs = new JTabbedPane();

		for (PeriodAnalysis2DPlotModel model : models) {
			// Create a line chart with legend, tooltips, and URLs showing
			// and add it to the panel.
			// TODO: for period plot, make Y scale logarithmic?
			ChartPanel chartPanel = new PeriodAnalysis2DChartPane(ChartFactory
					.createXYLineChart(this.chartTitle, domainTitle, model
							.getDependentDesc(), model,
							PlotOrientation.VERTICAL, true, true, true), model);

			tabs.addTab(model.getDependentDesc(), chartPanel);
			// JFreeChart chart = chartPanel.getChart();
		}

		return tabs;
	}
}
