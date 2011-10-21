/**
 * VStar: a statistical analysis tool for variable star data.
 * Copyright (C) 2010  AAVSO (http://www.aavso.org/)
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
package org.aavso.tools.vstar.plugin.period;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aavso.tools.vstar.ui.dialog.period.PeriodAnalysis2DChartPane;
import org.aavso.tools.vstar.ui.dialog.period.PeriodAnalysisDataTablePane;
import org.aavso.tools.vstar.ui.model.list.PeriodAnalysisDataTableModel;
import org.aavso.tools.vstar.ui.model.plot.PeriodAnalysis2DPlotModel;
import org.aavso.tools.vstar.util.period.IPeriodAnalysisAlgorithm;
import org.aavso.tools.vstar.util.period.PeriodAnalysisCoordinateType;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.title.TextTitle;

/**
 * This factory class creates GUI components (e.g. charts and tables) suitable
 * for returning from the PeriodAnalysisDialogBase.createContent() method that
 * must be overridden by period analysis plug-in subclasses.
 */
public class PeriodAnalysisComponentFactory {

	/**
	 * <p>
	 * Create a line plot given lists of domain and range values.
	 * </p>
	 * 
	 * <p>
	 * The component sends and receives period analysis selection messages.
	 * </p>
	 * 
	 * @see org.aavso.tools.vstar.ui.mediator.message.PeriodAnalysisSelectionMessage
	 * 
	 * @param title
	 *            The main title of the plot.
	 * @param subtitle
	 *            The subtitle of the plot.
	 * @param analysisValues
	 *            A mapping from period analysis coordinate type to lists of
	 *            values.
	 * @param domainType
	 *            The domain coordinate type.
	 * @param rangeType
	 *            The range coordinate type.
	 * @param permitLogarithmic
	 *            Should it be possible to toggle the plot between a normal and
	 *            logarithmic range?
	 * @param isLogarithmic
	 *            Should range values be logarithmic by default?
	 * @return A GUI line plot component.
	 */
	public static PeriodAnalysis2DChartPane createLinePlot(String title,
			String subtitle,
			Map<PeriodAnalysisCoordinateType, List<Double>> analysisValues,
			PeriodAnalysisCoordinateType domainType,
			PeriodAnalysisCoordinateType rangeType, boolean permitLogarithmic,
			boolean isLogarithmic) {

		PeriodAnalysis2DPlotModel model = new PeriodAnalysis2DPlotModel(
				analysisValues, domainType, rangeType, isLogarithmic);

		// Create a line chart with legend, tool-tips, and URLs showing
		// and add it to the panel.
		PeriodAnalysis2DChartPane chartPanel = new PeriodAnalysis2DChartPane(
				ChartFactory.createXYLineChart(title, model.getDomainType()
						.getDescription(), model.getRangeType()
						.getDescription(), model, PlotOrientation.VERTICAL,
						true, true, true), model, permitLogarithmic);

		chartPanel.getChart().addSubtitle(new TextTitle(subtitle));

		return chartPanel;
	}

	/**
	 * <p>
	 * Create a scatter plot given a 2D plot model.
	 * </p>
	 * 
	 * <p>
	 * The component sends and receives period analysis selection messages.
	 * </p>
	 * 
	 * @see org.aavso.tools.vstar.ui.mediator.message.PeriodAnalysisSelectionMessage
	 * 
	 * @param title
	 *            The main title of the plot.
	 * @param subtitle
	 *            The subtitle of the plot.
	 * @param model
	 *            A 2D plot model.
	 * @param permitLogarithmic
	 *            Should it be possible to toggle the plot between a normal and
	 *            logarithmic range?
	 * @return A GUI line plot component.
	 */
	public static PeriodAnalysis2DChartPane createScatterPlot(String title,
			String subtitle, PeriodAnalysis2DPlotModel model,
			boolean permitLogarithmic) {

		// Create a scatter plot with legend, tool-tips, and URLs showing
		// and add it to the panel.
		PeriodAnalysis2DChartPane chartPanel = new PeriodAnalysis2DChartPane(
				ChartFactory.createScatterPlot(title, model.getDomainType()
						.getDescription(), model.getRangeType()
						.getDescription(), model, PlotOrientation.VERTICAL,
						true, true, true), model, permitLogarithmic);

		chartPanel.getChart().addSubtitle(new TextTitle(subtitle));

		return chartPanel;
	}

	/**
	 * <p>
	 * Create a combined line and scatter plot given a 2D plot model.
	 * </p>
	 * 
	 * <p>
	 * The component sends and receives period analysis selection messages.
	 * </p>
	 * 
	 * @see org.aavso.tools.vstar.ui.mediator.message.PeriodAnalysisSelectionMessage
	 * 
	 * @param title
	 *            The main title of the plot.
	 * @param subtitle
	 *            The subtitle of the plot.
	 * @param model
	 *            A 2D plot model.
	 * @param permitLogarithmic
	 *            Should it be possible to toggle the plot between a normal and
	 *            logarithmic range?
	 * @return A GUI line plot component.
	 */
	public static PeriodAnalysis2DChartPane createLineAndScatterPlot(
			String title, String subtitle, PeriodAnalysis2DPlotModel model,
			boolean permitLogarithmic) {

		// Create a line chart with legend, tool-tips, and URLs showing
		// and add it to the panel.
		JFreeChart chart1 = ChartFactory.createXYLineChart(title, model
				.getDomainType().getDescription(), model.getRangeType()
				.getDescription(), model, PlotOrientation.VERTICAL, true, true,
				true);

		JFreeChart chart2 = ChartFactory.createScatterPlot(title, model
				.getDomainType().getDescription(), model.getRangeType()
				.getDescription(), model, PlotOrientation.VERTICAL, true, true,
				true);

		// Make a combined chart.
		chart2.getXYPlot().setDataset(1, model);
		chart2.getXYPlot().setRenderer(1, chart1.getXYPlot().getRenderer());
		chart2.getXYPlot().setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);

		PeriodAnalysis2DChartPane chartPanel = new PeriodAnalysis2DChartPane(
				chart2, model, permitLogarithmic);

		chartPanel.getChart().addSubtitle(new TextTitle(subtitle));

		return chartPanel;
	}

	/**
	 * <p>
	 * Create a combined line and scatter plot given a 2D plot model.
	 * </p>
	 * 
	 * <p>
	 * The component sends and receives period analysis selection messages.
	 * </p>
	 * 
	 * @see org.aavso.tools.vstar.ui.mediator.message.PeriodAnalysisSelectionMessage
	 * 
	 * @param title
	 *            The main title of the plot.
	 * @param subtitle
	 *            The subtitle of the plot.
	 * @param model
	 *            A 2D plot model.
	 * @param permitLogarithmic
	 *            Should it be possible to toggle the plot between a normal and
	 *            logarithmic range?
	 * @return A GUI line plot component.
	 */
	public static PeriodAnalysis2DChartPane createLinePlot(String title,
			String subtitle, PeriodAnalysis2DPlotModel model,
			boolean permitLogarithmic) {

		// Create a line chart with legend, tool-tips, and URLs showing
		// and add it to the panel.
		PeriodAnalysis2DChartPane chartPanel = new PeriodAnalysis2DChartPane(
				ChartFactory.createXYLineChart(title, model.getDomainType()
						.getDescription(), model.getRangeType()
						.getDescription(), model, PlotOrientation.VERTICAL,
						true, true, true), model, permitLogarithmic);

		chartPanel.getChart().addSubtitle(new TextTitle(subtitle));

		return chartPanel;
	}

	/**
	 * <p>
	 * Create a data table given column type and data arrays.
	 * </p>
	 * 
	 * <p>
	 * The component sends and receives period analysis selection messages.
	 * </p>
	 * 
	 * @param columnTypes
	 *            An array of column types as they are to appear in the table.
	 * @param dataArrays
	 *            An array of data arrays, where each data array corresponds to
	 *            each element in the columnTypes array.
	 * @param algorithm
	 *            The period analysis algorithm.
	 * @return A GUI data table component.
	 */
	public static PeriodAnalysisDataTablePane createDataTable(
			PeriodAnalysisCoordinateType[] columnTypes, double[][] dataArrays,
			IPeriodAnalysisAlgorithm algorithm) {

		Map<PeriodAnalysisCoordinateType, List<Double>> dataListMap = new HashMap<PeriodAnalysisCoordinateType, List<Double>>();
		for (int i = 0; i < dataArrays.length; i++) {
			double[] dataArray = dataArrays[i];
			List<Double> dataList = new ArrayList<Double>();
			for (double x : dataArray) {
				dataList.add(x);
			}
			dataListMap.put(columnTypes[i], dataList);
		}

		return new PeriodAnalysisDataTablePane(
				new PeriodAnalysisDataTableModel(columnTypes, dataListMap),
				algorithm);
	}

	/**
	 * <p>
	 * Create a data table given column type array and data list.
	 * </p>
	 * 
	 * <p>
	 * The component sends and receives period analysis selection messages.
	 * </p>
	 * 
	 * @param columnTypes
	 *            An array of column types as they are to appear in the table.
	 * @param dataMap
	 *            A mapping from coordinate type to lists of data values.
	 * @param algorithm
	 *            The period analysis algorithm.
	 * @return A GUI data table component.
	 */
	public static PeriodAnalysisDataTablePane createDataTable(
			PeriodAnalysisCoordinateType[] columnTypes,
			Map<PeriodAnalysisCoordinateType, List<Double>> dataMap,
			IPeriodAnalysisAlgorithm algorithm) {

		return new PeriodAnalysisDataTablePane(
				new PeriodAnalysisDataTableModel(columnTypes, dataMap),
				algorithm);
	}
}
