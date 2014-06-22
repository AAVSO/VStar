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

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JTabbedPane;

import org.aavso.tools.vstar.plugin.PluginComponentFactory;
import org.aavso.tools.vstar.plugin.period.PeriodAnalysisComponentFactory;
import org.aavso.tools.vstar.plugin.period.PeriodAnalysisDialogBase;
import org.aavso.tools.vstar.ui.NamedComponent;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.message.HarmonicSearchResultMessage;
import org.aavso.tools.vstar.ui.mediator.message.PeriodAnalysisSelectionMessage;
import org.aavso.tools.vstar.ui.mediator.message.PeriodChangeMessage;
import org.aavso.tools.vstar.ui.model.list.PeriodAnalysisDataTableModel;
import org.aavso.tools.vstar.ui.model.plot.PeriodAnalysis2DPlotModel;
import org.aavso.tools.vstar.util.locale.LocaleProps;
import org.aavso.tools.vstar.util.model.Harmonic;
import org.aavso.tools.vstar.util.notification.Listener;
import org.aavso.tools.vstar.util.period.IPeriodAnalysisAlgorithm;
import org.aavso.tools.vstar.util.period.IPeriodAnalysisDatum;
import org.aavso.tools.vstar.util.period.PeriodAnalysisCoordinateType;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.DatasetRenderingOrder;

/**
 * This dialog class is used to visualise period analysis results.
 */
@SuppressWarnings("serial")
public class PeriodAnalysis2DResultDialog extends PeriodAnalysisDialogBase {

	private final static PeriodAnalysisCoordinateType[] DATA_COLUMN_TYPES = {
			PeriodAnalysisCoordinateType.FREQUENCY,
			PeriodAnalysisCoordinateType.PERIOD,
			PeriodAnalysisCoordinateType.POWER,
			PeriodAnalysisCoordinateType.AMPLITUDE };

	private String seriesTitle;
	private String chartTitle;

	private IPeriodAnalysisAlgorithm algorithm;
	private Map<PeriodAnalysisCoordinateType, List<Double>> resultDataMap;

	private List<PeriodAnalysis2DPlotModel> plotModels;
	private PeriodAnalysisDataTableModel dataTableModel;
	private PeriodAnalysisDataTableModel topHitsTableModel;

	private PeriodAnalysisDataTablePane dataTablePane;
	private PeriodAnalysisTopHitsTablePane topHitsTablePane;

	private List<PeriodAnalysis2DChartPane> plotPanes;

	private IPeriodAnalysisDatum selectedDataPoint;

	private Listener<PeriodAnalysisSelectionMessage> periodAnalysisListener;

	/**
	 * Constructor.
	 * 
	 * @param title
	 *            The title for the chart.
	 * @param seriesTitle
	 *            The source series sub-title for the chart.
	 * @param algorithm
	 *            The period analysis algorithm.
	 */
	public PeriodAnalysis2DResultDialog(String title, String seriesTitle,
			IPeriodAnalysisAlgorithm algorithm) {
		super(title, false, true);

		selectedDataPoint = null;

		resultDataMap = algorithm.getResultSeries();

		this.seriesTitle = seriesTitle;
		this.chartTitle = title;

		this.algorithm = algorithm;

		dataTableModel = new PeriodAnalysisDataTableModel(DATA_COLUMN_TYPES,
				resultDataMap);
		topHitsTableModel = new PeriodAnalysisDataTableModel(DATA_COLUMN_TYPES,
				algorithm.getTopHits());

		plotModels = new ArrayList<PeriodAnalysis2DPlotModel>();

		// Frequency vs Power
		plotModels.add(new PeriodAnalysis2DPlotModel(resultDataMap,
				PeriodAnalysisCoordinateType.FREQUENCY,
				PeriodAnalysisCoordinateType.POWER, false));

		// Frequency vs Amplitude
		plotModels.add(new PeriodAnalysis2DPlotModel(resultDataMap,
				PeriodAnalysisCoordinateType.FREQUENCY,
				PeriodAnalysisCoordinateType.AMPLITUDE, false));

		// Period vs Power
		// plotModels.add(new PeriodAnalysis2DPlotModel(resultDataMap,
		// PeriodAnalysisCoordinateType.PERIOD,
		// PeriodAnalysisCoordinateType.POWER, false));

		// Period vs Amplitude
		// plotModels.add(new PeriodAnalysis2DPlotModel(resultDataMap,
		// PeriodAnalysisCoordinateType.PERIOD,
		// PeriodAnalysisCoordinateType.AMPLITUDE, false));

		prepareDialog();

		startup();
	}

	protected Component createContent() {
		return createTabs();
	}

	// Return the tabs containing table and plots of frequency vs one of the
	// dependent variables of period, power, or amplitude. Is this what we want,
	// or something different?
	private JTabbedPane createTabs() {
		List<NamedComponent> namedComponents = new ArrayList<NamedComponent>();

		plotPanes = new ArrayList<PeriodAnalysis2DChartPane>();

		// Add plots.
		for (PeriodAnalysis2DPlotModel model : plotModels) {
			boolean permitlogarithmic = model.getRangeType() == PeriodAnalysisCoordinateType.POWER;

			PeriodAnalysis2DChartPane plot = PeriodAnalysisComponentFactory
					.createLinePlot(chartTitle, seriesTitle, model,
							permitlogarithmic);

			PeriodAnalysis2DChartPane topHitsPlot = PeriodAnalysisComponentFactory
					.createScatterPlot(chartTitle, seriesTitle,
							new PeriodAnalysis2DPlotModel(algorithm
									.getTopHits(), model.getDomainType(), model
									.getRangeType(), model.isLogarithmic()),
							permitlogarithmic);

			// Add the above line plot's model to the scatter plot.
			// Render the scatter plot last so the "handles" will be
			// the first items selected by the mouse.
			JFreeChart chart = topHitsPlot.getChart();
			chart.getXYPlot().setDataset(PeriodAnalysis2DChartPane.DATA_SERIES,
					model);
			chart.getXYPlot().setRenderer(
					PeriodAnalysis2DChartPane.DATA_SERIES,
					plot.getChart().getXYPlot().getRenderer());
			chart.getXYPlot().setDatasetRenderingOrder(
					DatasetRenderingOrder.REVERSE);

			plot = topHitsPlot;

			String tabName = model.getRangeType() + " vs "
					+ model.getDomainType();

			namedComponents.add(new NamedComponent(tabName, plot));
			plotPanes.add(plot);
		}

		// Add data table view.
		dataTablePane = new PeriodAnalysisDataTablePane(dataTableModel,
				algorithm);
		namedComponents.add(new NamedComponent(LocaleProps.get("DATA_TAB"),
				dataTablePane));

		// Add top-hits table view.
		topHitsTablePane = new PeriodAnalysisTopHitsTablePane(
				topHitsTableModel, dataTableModel, algorithm);
		namedComponents.add(new NamedComponent(LocaleProps.get("TOP_HITS_TAB"),
				topHitsTablePane));

		return PluginComponentFactory.createTabs(namedComponents);
	}

	// The new phase plot button will only be enabled when a period
	// analysis selection message has been received by this class,
	// so we *know* without having to ask that there is a selected
	// row in the data table.
	@Override
	protected void newPhasePlotButtonAction() {
		PeriodChangeMessage message = new PeriodChangeMessage(this,
				selectedDataPoint.getPeriod());
		Mediator.getInstance().getPeriodChangeNotifier().notifyListeners(
				message);
	}

	@Override
	protected void findHarmonicsButtonAction() {
		List<Double> data = algorithm.getResultSeries().get(
				PeriodAnalysisCoordinateType.FREQUENCY);
		List<Harmonic> harmonics = findHarmonics(selectedDataPoint
				.getFrequency(), data);
		HarmonicSearchResultMessage msg = new HarmonicSearchResultMessage(this,
				harmonics, selectedDataPoint);
		Mediator.getInstance().getHarmonicSearchNotifier().notifyListeners(msg);
	}

	// Enable the new phase plot button and store the selected
	// period analysis data point.
	private Listener<PeriodAnalysisSelectionMessage> createPeriodAnalysisListener() {
		return new Listener<PeriodAnalysisSelectionMessage>() {
			public void update(PeriodAnalysisSelectionMessage info) {
				setNewPhasePlotButtonState(true);
				setFindHarmonicsButtonState(true);
				selectedDataPoint = info.getDataPoint();
			}

			public boolean canBeRemoved() {
				return true;
			}
		};
	}

	@Override
	public void startup() {
		periodAnalysisListener = createPeriodAnalysisListener();
		Mediator.getInstance().getPeriodAnalysisSelectionNotifier()
				.addListener(periodAnalysisListener);

		dataTablePane.startup();
		topHitsTablePane.startup();

		for (PeriodAnalysis2DChartPane plotPane : plotPanes) {
			plotPane.startup();
		}
	}

	@Override
	public void cleanup() {
		Mediator.getInstance().getPeriodAnalysisSelectionNotifier()
				.removeListenerIfWilling(periodAnalysisListener);

		dataTablePane.cleanup();
		topHitsTablePane.cleanup();

		for (PeriodAnalysis2DChartPane plotPane : plotPanes) {
			plotPane.cleanup();
		}
	}
}
