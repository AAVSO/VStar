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
package org.aavso.tools.vstar.ui.dialog.period;

import java.util.List;

import javax.swing.JDialog;
import javax.swing.JTabbedPane;

import org.aavso.tools.vstar.ui.MainFrame;
import org.aavso.tools.vstar.ui.model.list.PeriodAnalysisTableModel;
import org.aavso.tools.vstar.ui.model.plot.PeriodAnalysis2DPlotModel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.title.TextTitle;

/**
 * This class is used to visualise period analysis results.
 */
public class PeriodAnalysis2DResultDialog extends JDialog {

	private String seriesTitle;
	private String chartTitle;
	private String domainTitle;
	private List<PeriodAnalysis2DPlotModel> plotModels;
	private PeriodAnalysisTableModel tableModel;

	/**
	 * Constructor
	 * 
	 * @param title
	 *            The title for the chart.
	 * @param subTitle
	 *            The source series sub-title for the chart.
	 * @param domainTitle
	 *            The domain title (e.g. Julian Date, phase).
	 * @param plotModels
	 *            The data plotModels on which to base plots.
	 * @param tableModel
	 *            A model with which to display all data in a table.
	 */
	public PeriodAnalysis2DResultDialog(String title, String seriesTitle,
			String domainTitle, List<PeriodAnalysis2DPlotModel> plotModels,
			PeriodAnalysisTableModel tableModel) {
		super();

		this.setTitle(title);
		this.setModal(false);

		this.seriesTitle = seriesTitle;
		this.chartTitle = title;
		this.domainTitle = domainTitle;
		this.plotModels = plotModels;
		this.tableModel = tableModel;

		this.getContentPane().add(getTabs());

		this.pack();
		this.setLocationRelativeTo(MainFrame.getInstance().getContentPane());
		this.setAlwaysOnTop(false);
		this.setVisible(true);
	}

	// Return the tabs containing table and plots of frequency vs one of the
	// dependent
	// variables of period, power, or amplitude. Is this what we want, or
	// something different?
	private JTabbedPane getTabs() {
		JTabbedPane tabs = new JTabbedPane();

		// Add plots.
		for (PeriodAnalysis2DPlotModel model : plotModels) {
			// Create a line chart with legend, tooltips, and URLs showing
			// and add it to the panel.
			ChartPanel chartPanel = new PeriodAnalysis2DChartPane(ChartFactory
					.createXYLineChart(this.chartTitle, domainTitle, model
							.getDependentDesc(), model,
							PlotOrientation.VERTICAL, true, true, true), model);

			chartPanel.getChart().addSubtitle(new TextTitle(this.seriesTitle));
			
			tabs.addTab(model.getDependentDesc(), chartPanel);
		}

		// Add table view.
		tabs.addTab("Data", new PeriodAnalysisTablePane(tableModel));

		return tabs;
	}
}
