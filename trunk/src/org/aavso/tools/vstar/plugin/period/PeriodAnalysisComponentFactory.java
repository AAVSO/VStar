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

import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JTabbedPane;

import org.aavso.tools.vstar.ui.NamedComponent;
import org.aavso.tools.vstar.ui.dialog.period.PeriodAnalysis2DChartPane;
import org.aavso.tools.vstar.ui.dialog.period.PeriodAnalysisDataTablePane;
import org.aavso.tools.vstar.ui.model.list.PeriodAnalysisDataTableModel;
import org.aavso.tools.vstar.ui.model.plot.PeriodAnalysis2DPlotModel;
import org.aavso.tools.vstar.util.period.PeriodAnalysisCoordinateType;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.title.TextTitle;

/**
 * This factory class creates GUI (e.g. charts and tables) components suitable for
 * returning from the PeriodAnalysisDialogBase.createContent() method that must
 * be overridden by period analysis plugin subclasses.
 */
public class PeriodAnalysisComponentFactory {

	/**
	 * <p>
	 * Create a line plot given arrays of domain and range values.
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
	 * @param domainValues
	 *            An array of domain values.
	 * @param rangeValues
	 *            An array of range values.
	 * @param domainType
	 *            The domain coordinate type.
	 * @param rangeType
	 *            The range coordinate type.
	 * @return A GUI line plot component.
	 */
	public static Component createLinePlot(String title, String subtitle,
			double[] domainValues, double[] rangeValues,
			PeriodAnalysisCoordinateType domainType,
			PeriodAnalysisCoordinateType rangeType) {

		// Create the plot model from the data.
		List<Double> domainList = new ArrayList<Double>();
		for (double x : domainValues) {
			domainList.add(x);
		}

		List<Double> rangeList = new ArrayList<Double>();
		for (double y : rangeValues) {
			rangeList.add(y);
		}

		return createLinePlot(title, subtitle, domainList, rangeList,
				domainType, rangeType);
	}

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
	 * @param domainValues
	 *            A list of domain values.
	 * @param rangeValues
	 *            A list of range values.
	 * @param domainType
	 *            The domain coordinate type.
	 * @param rangeType
	 *            The range coordinate type.
	 * @return A GUI line plot component.
	 */
	public static Component createLinePlot(String title, String subtitle,
			List<Double> domainValues, List<Double> rangeValues,
			PeriodAnalysisCoordinateType domainType,
			PeriodAnalysisCoordinateType rangeType) {

		PeriodAnalysis2DPlotModel model = new PeriodAnalysis2DPlotModel(
				domainValues, rangeValues, domainType, rangeType);

		// Create a line chart with legend, tool-tips, and URLs showing
		// and add it to the panel.
		ChartPanel chartPanel = new PeriodAnalysis2DChartPane(ChartFactory
				.createXYLineChart(title, model.getDomainType()
						.getDescription(), model.getRangeType()
						.getDescription(), model, PlotOrientation.VERTICAL,
						true, true, true), model);

		chartPanel.getChart().addSubtitle(new TextTitle(subtitle));

		return chartPanel;
	}

	/**
	 * <p>
	 * Create a line plot given a 2D plot model.
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
	 * @return A GUI line plot component.
	 */
	public static Component createLinePlot(String title, String subtitle,
			PeriodAnalysis2DPlotModel model) {

		// Create a line chart with legend, tool-tips, and URLs showing
		// and add it to the panel.
		ChartPanel chartPanel = new PeriodAnalysis2DChartPane(ChartFactory
				.createXYLineChart(title, model.getDomainType()
						.getDescription(), model.getRangeType()
						.getDescription(), model, PlotOrientation.VERTICAL,
						true, true, true), model);

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
	 * @return A GUI data table component.
	 */
	public static Component createDataTable(
			PeriodAnalysisCoordinateType[] columnTypes, double[][] dataArrays) {

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
				new PeriodAnalysisDataTableModel(columnTypes, dataListMap));
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
	 * @return A GUI data table component.
	 */
	public static Component createDataTable(
			PeriodAnalysisCoordinateType[] columnTypes,
			Map<PeriodAnalysisCoordinateType, List<Double>> dataMap) {

		return new PeriodAnalysisDataTablePane(
				new PeriodAnalysisDataTableModel(columnTypes, dataMap));
	}

	/**
	 * Create a tabbed pane component from a list of named components.
	 * 
	 * @param components
	 *            An list of named component parameters.
	 * @return The tabbed pane component.
	 */
	public static JTabbedPane createTabs(List<NamedComponent> components) {
		JTabbedPane tabs = new JTabbedPane();

		for (NamedComponent component : components) {
			tabs.addTab(component.getName(), null, component.getComponent(),
					component.getTip());
		}

		return tabs;
	}

	/**
	 * Create a tabbed pane component from a list of named components.
	 * 
	 * @param components
	 *            An arbitrary number of named component parameters.
	 * @return The tabbed pane component.
	 */
	public static JTabbedPane createTabs(NamedComponent... components) {
		JTabbedPane tabs = new JTabbedPane();

		for (NamedComponent component : components) {
			tabs.addTab(component.getName(), null, component.getComponent(),
					component.getTip());
		}

		return tabs;
	}
}
