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
import org.aavso.tools.vstar.ui.model.list.PeriodAnalysisDataTableModel;
import org.aavso.tools.vstar.ui.model.list.PeriodAnalysisTopHitsTableModel;
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
	private List<PeriodAnalysis2DPlotModel> plotModels;
	private PeriodAnalysisDataTableModel dataTableModel;
	private PeriodAnalysisTopHitsTableModel topHitsTableModel;
	
	/**
	 * Constructor
	 * 
	 * @param title
	 *            The title for the chart.
	 * @param subTitle
	 *            The source series sub-title for the chart.
	 * @param plotModels
	 *            The data plotModels on which to base plots.
	 * @param dataTableModel
	 *            A model with which to display all data in a table.
	 */
	public PeriodAnalysis2DResultDialog(String title, String seriesTitle,
			List<PeriodAnalysis2DPlotModel> plotModels,
			PeriodAnalysisDataTableModel dataTableModel,
			PeriodAnalysisTopHitsTableModel topHitsTableModel) {
		super();

		this.setTitle(title);
		this.setModal(false);

		this.seriesTitle = seriesTitle;
		this.chartTitle = title;
		this.plotModels = plotModels;
		this.dataTableModel = dataTableModel;
		this.topHitsTableModel = topHitsTableModel;
		
		this.getContentPane().add(getTabs());

		this.pack();
		this.setLocationRelativeTo(MainFrame.getInstance().getContentPane());
		this.setAlwaysOnTop(false);
		this.setVisible(true);
	}

	// Return the tabs containing table and plots of frequency vs one of the
	// dependent variables of period, power, or amplitude. Is this what we want,
	// or something different?
	private JTabbedPane getTabs() {
		JTabbedPane tabs = new JTabbedPane();

		// Add plots.
		for (PeriodAnalysis2DPlotModel model : plotModels) {
			// Create a line chart with legend, tool-tips, and URLs showing
			// and add it to the panel.
			ChartPanel chartPanel = new PeriodAnalysis2DChartPane(ChartFactory
					.createXYLineChart(this.chartTitle, model.getDomainType()
							.getDescription(), model.getRangeType()
							.getDescription(), model, PlotOrientation.VERTICAL,
							true, true, true), model);

			chartPanel.getChart().addSubtitle(new TextTitle(this.seriesTitle));

			String tabName = model.getRangeType() + " vs "
					+ model.getDomainType();
			tabs.addTab(tabName, chartPanel);
		}

		// Add data table view.
		tabs.addTab("Data", new PeriodAnalysisDataTablePane(dataTableModel));

		// Add top-hits table view.
		tabs.addTab("Top Hits", new PeriodAnalysisTopHitsTablePane(topHitsTableModel));

		return tabs;
	}
}
