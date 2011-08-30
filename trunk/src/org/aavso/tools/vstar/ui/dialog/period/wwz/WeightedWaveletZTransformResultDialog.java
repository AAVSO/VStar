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
package org.aavso.tools.vstar.ui.dialog.period.wwz;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTabbedPane;

import org.aavso.tools.vstar.plugin.PluginComponentFactory;
import org.aavso.tools.vstar.plugin.period.PeriodAnalysisDialogBase;
import org.aavso.tools.vstar.ui.NamedComponent;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.message.PeriodAnalysisSelectionMessage;
import org.aavso.tools.vstar.ui.mediator.message.PeriodChangeMessage;
import org.aavso.tools.vstar.ui.model.list.WWZDataTableModel;
import org.aavso.tools.vstar.ui.model.plot.WWZ2DPlotModel;
import org.aavso.tools.vstar.util.notification.Listener;
import org.aavso.tools.vstar.util.period.IPeriodAnalysisDatum;
import org.aavso.tools.vstar.util.period.wwz.WWZCoordinateType;
import org.aavso.tools.vstar.util.period.wwz.WWZStatistic;
import org.aavso.tools.vstar.util.period.wwz.WeightedWaveletZTransform;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.math.plot.Plot3DPanel;

/**
 * This dialog class is used to visualise WWZ algorithm results.
 */
public class WeightedWaveletZTransformResultDialog extends
		PeriodAnalysisDialogBase {

	private String chartTitle;
	private IPeriodAnalysisDatum selectedDataPoint;
	private WeightedWaveletZTransform wwt;

	/**
	 * Constructor.
	 * 
	 * @param title
	 *            The title for the dialog.
	 * @param chartTitle
	 *            The title for the chart.
	 */
	public WeightedWaveletZTransformResultDialog(String title,
			String chartTitle, WeightedWaveletZTransform wwt) {
		super(title, false, true);

		this.chartTitle = chartTitle;
		this.wwt = wwt;

		selectedDataPoint = null;

		Mediator.getInstance().getPeriodAnalysisSelectionNotifier()
				.addListener(this.createPeriodAnalysisListener());

		prepareDialog();
	}

	/**
	 * @see org.aavso.tools.vstar.plugin.period.PeriodAnalysisDialogBase#createContent()
	 */
	@Override
	protected Component createContent() {
		return createTabs();
	}

	private JTabbedPane createTabs() {
		List<NamedComponent> namedComponents = new ArrayList<NamedComponent>();

		// Period vs time plot.
		namedComponents.add(createChart(new WWZ2DPlotModel(wwt
				.getMaximalStats(), WWZCoordinateType.TAU,
				WWZCoordinateType.PERIOD), wwt.getMinPeriod(), wwt
				.getMaxPeriod()));

		// Semi-amplitude vs time plot.
		namedComponents.add(createChart(new WWZ2DPlotModel(wwt
				.getMaximalStats(), WWZCoordinateType.TAU,
				WWZCoordinateType.SEMI_AMPLITUDE), wwt.getMinAmp(), wwt
				.getMaxAmp()));

		// 3D plot from maximal stats.
		namedComponents.add(create3DMaximalStatsPlot(WWZCoordinateType.TAU,
				WWZCoordinateType.PERIOD, WWZCoordinateType.WWZ));

		// Tables for all and maximal statistics.
		namedComponents
				.add(new NamedComponent("WWZ Results", new WWZDataTablePane(
						new WWZDataTableModel(wwt.getStats(), wwt))));

		namedComponents.add(new NamedComponent("Maximal WWZ Results",
				new WWZDataTablePane(new WWZDataTableModel(wwt
						.getMaximalStats(), wwt))));

		return PluginComponentFactory.createTabs(namedComponents);
	}

	/**
	 * The new phase plot button will only be enabled when a period analysis
	 * selection message has been received by this class, so we *know* without
	 * having to ask that there is a selected row in the data table.
	 */
	@Override
	protected void newPhasePlotButtonAction() {
		PeriodChangeMessage message = new PeriodChangeMessage(this,
				selectedDataPoint.getPeriod());
		Mediator.getInstance().getPeriodChangeNotifier().notifyListeners(
				message);
	}

	// Enable the new phase plot button and store the selected
	// period analysis data point.
	private Listener<PeriodAnalysisSelectionMessage> createPeriodAnalysisListener() {
		return new Listener<PeriodAnalysisSelectionMessage>() {
			public void update(PeriodAnalysisSelectionMessage info) {
				setNewPhasePlotButtonState(true);
				selectedDataPoint = info.getDataPoint();
			}

			public boolean canBeRemoved() {
				return false;
			}
		};
	}

	// Helpers

	private NamedComponent createChart(WWZ2DPlotModel model, double minRange,
			double maxRange) {
		JFreeChart chart = ChartFactory.createXYLineChart(chartTitle, model
				.getDomainType().toString(), model.getRangeType().toString(),
				model, PlotOrientation.VERTICAL, true, true, false);

		double rangeMargin = (maxRange - minRange) / 100;
		minRange -= rangeMargin;
		maxRange += rangeMargin;

		String tabName = model.getRangeType().toString() + " vs "
				+ model.getDomainType().toString();

		return new NamedComponent(tabName, new WWZ2DPlotPane(chart, model,
				minRange, maxRange));
	}

	/**
	 * Create a 3D plot of 3 WWZ coordinates, e.g. of of time, period, wwz from
	 * maximal stats.
	 * 
	 * @return A named component suitable for adding to dialog.
	 */
	private NamedComponent create3DMaximalStatsPlot(WWZCoordinateType xType,
			WWZCoordinateType yType, WWZCoordinateType zType) {
		Plot3DPanel plot = new Plot3DPanel();
		plot
				.setAxisLabels(xType.toString(), yType.toString(), zType
						.toString());

		int size = wwt.getMaximalStats().size();
		double[][] xyz = new double[3][size];

		for (int i = 0; i < size; i++) {
			WWZStatistic stat = wwt.getMaximalStats().get(i);
			xyz[0][i] = stat.getValue(xType);
			xyz[1][i] = stat.getValue(yType);
			xyz[2][i] = stat.getValue(zType);
		}

		plot.addBarPlot("WWZ Maximal Statistics 3D plot", Color.GREEN, xyz);

		String tabName = yType.toString() + " vs " + xType.toString() + " vs "
				+ zType.toString();

		return new NamedComponent(tabName, plot);
	}
}
