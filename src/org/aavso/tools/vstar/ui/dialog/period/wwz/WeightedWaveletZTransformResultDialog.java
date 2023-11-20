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
import java.awt.Font;
import java.awt.GradientPaint;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.aavso.tools.vstar.plugin.PluginComponentFactory;
import org.aavso.tools.vstar.plugin.period.PeriodAnalysisDialogBase;
import org.aavso.tools.vstar.ui.NamedComponent;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.message.PeriodAnalysisSelectionMessage;
import org.aavso.tools.vstar.ui.mediator.message.PeriodChangeMessage;
import org.aavso.tools.vstar.ui.model.list.WWZDataTableModel;
import org.aavso.tools.vstar.ui.model.plot.WWZ2DPlotModel;
import org.aavso.tools.vstar.ui.model.plot.WWZ3DPlotModel;
import org.aavso.tools.vstar.util.IStartAndCleanup;
import org.aavso.tools.vstar.util.locale.LocaleProps;
import org.aavso.tools.vstar.util.notification.Listener;
import org.aavso.tools.vstar.util.period.IPeriodAnalysisDatum;
import org.aavso.tools.vstar.util.period.wwz.WWZCoordinateType;
import org.aavso.tools.vstar.util.period.wwz.WWZStatistic;
import org.aavso.tools.vstar.util.period.wwz.WeightedWaveletZTransform;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.LookupPaintScale;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.chart.title.PaintScaleLegend;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.RectangleInsets;
import org.math.plot.Plot3DPanel;

/**
 * This dialog class is used to visualise WWZ algorithm results.
 */
@SuppressWarnings("serial")
public class WeightedWaveletZTransformResultDialog extends
		PeriodAnalysisDialogBase {

	private String chartTitle;
	private IPeriodAnalysisDatum selectedDataPoint;
	private WeightedWaveletZTransform wwt;
	private WWZCoordinateType rangeType;

	List<IStartAndCleanup> startupAndCleanupComponents;

	private Listener<PeriodAnalysisSelectionMessage> periodAnalysisListener;

	/**
	 * Constructor.
	 * 
	 * @param title
	 *            The title for the dialog.
	 * @param chartTitle
	 *            The title for the chart.
	 * @param rangeType
	 *            The type of the range coordinate.
	 */
	public WeightedWaveletZTransformResultDialog(String title,
			String chartTitle, WeightedWaveletZTransform wwt,
			WWZCoordinateType rangeType) {
		super(title, false, true, false);

		this.chartTitle = chartTitle;
		this.wwt = wwt;
		this.rangeType = rangeType;

		selectedDataPoint = null;

		startupAndCleanupComponents = new ArrayList<IStartAndCleanup>();

		prepareDialog();

		startup();
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

		final String MAXIMAL_WWZ = "(" + LocaleProps.get("MAXIMAL") + " WWZ)";

		// Maximal period vs time plot.
		namedComponents.add(createChart(MAXIMAL_WWZ, new WWZ2DPlotModel(wwt
				.getMaximalStats(), WWZCoordinateType.TAU,
				WWZCoordinateType.PERIOD),
				getMinValue(WWZCoordinateType.PERIOD),
				getMaxValue(WWZCoordinateType.PERIOD)));

		// Maximal frequency vs time plot.
		namedComponents.add(createChart(MAXIMAL_WWZ, new WWZ2DPlotModel(wwt
				.getMaximalStats(), WWZCoordinateType.TAU,
				WWZCoordinateType.FREQUENCY),
				getMinValue(WWZCoordinateType.FREQUENCY),
				getMaxValue(WWZCoordinateType.FREQUENCY)));

		// Maximal semi-amplitude vs time plot.
		namedComponents.add(createChart(MAXIMAL_WWZ, new WWZ2DPlotModel(wwt
				.getMaximalStats(), WWZCoordinateType.TAU,
				WWZCoordinateType.SEMI_AMPLITUDE), wwt.getMinAmp(), wwt
				.getMaxAmp()));

		// Contour plot of time vs period vs WWZ.
		namedComponents.add(createContourChart("", new WWZ3DPlotModel(wwt
				.getStats(), WWZCoordinateType.TAU, WWZCoordinateType.PERIOD,
				WWZCoordinateType.WWZ), wwt.getStats().get(0).getTau(), wwt
				.getStats().get(wwt.getStats().size() - 1).getTau(),
				getMinValue(WWZCoordinateType.PERIOD),
				getMaxValue(WWZCoordinateType.PERIOD), wwt.getMinWWZ(), wwt
						.getMaxWWZ()));

		// 3D plot from maximal stats.
		namedComponents.add(create3DStatsPlot(MAXIMAL_WWZ,
				WWZCoordinateType.TAU, rangeType, WWZCoordinateType.WWZ, wwt
						.getMaximalStats()));

		// Tables for all and maximal statistics.
		WWZDataTablePane dataPane = new WWZDataTablePane(new WWZDataTableModel(
				wwt.getStats(), wwt));
		startupAndCleanupComponents.add(dataPane);
		namedComponents.add(new NamedComponent(LocaleProps.get("WWZ_RESULTS"),
				dataPane));

		WWZDataTablePane maximalPane = new WWZDataTablePane(
				new WWZDataTableModel(wwt.getMaximalStats(), wwt));
		startupAndCleanupComponents.add(maximalPane);
		namedComponents.add(new NamedComponent(LocaleProps
				.get("MAXIMAL_WWZ_RESULTS"), maximalPane));

		return PluginComponentFactory.createTabs(namedComponents);
	}

	protected JPanel createButtonPanel() {
		JPanel buttonPane = super.createButtonPanel();
		return buttonPane;
	}

	/**
	 * The new phase plot button will only be enabled when a period analysis
	 * selection message has been received, so we *know* without having to ask
	 * that there is a selected row in the data table.
	 */
	@Override
	protected void newPhasePlotButtonAction() {
		PeriodChangeMessage message = new PeriodChangeMessage(this,
				selectedDataPoint.getPeriod());
		message.setName(this.getName());
		Mediator.getInstance().getPeriodChangeNotifier().notifyListeners(
				message);
	}

	// Note: The harmonics finder methods are not currently used here.
	// Need to assess the validity or suitability of that functionality
	// in the WWZ context.

	@Override
	protected void findHarmonicsButtonAction() {
//		List<Harmonic> harmonics = findHarmonicsFromWWZStats(selectedDataPoint
//				.getFrequency());
//		HarmonicSearchResultMessage msg = new HarmonicSearchResultMessage(this,
//				harmonics, selectedDataPoint);
//		msg.setName(this.getName());
//		Mediator.getInstance().getHarmonicSearchNotifier().notifyListeners(msg);
	}

/*
	// TODO: Refactor so that the base class method takes an interface
	// with a method that returns a frequency from a sequence. Then
	// List<Double> or List<WWZStatistic> can be wrapped in an object
	// that implements that interface. Then this method can go away.
	protected List<Harmonic> findHarmonicsFromWWZStats(double freq) {
		List<Harmonic> harmonics = new ArrayList<Harmonic>();
		harmonics.add(new Harmonic(freq, Harmonic.FUNDAMENTAL));
		int n = Harmonic.FUNDAMENTAL + 1;

		List<WWZStatistic> data = wwt.getStats();

		for (int i = 0; i < data.size(); i++) {
			double candidateFreq = data.get(i).getFrequency();
			// Try it both ways in case of round-off errors.
			if (candidateFreq / n == freq || candidateFreq == freq * n) {
				harmonics.add(new Harmonic(freq * n, n));
				n++;
			}
		}

		return harmonics;
	}
*/	

	// Enable the new phase plot button and store the selected
	// period analysis data point.
	private Listener<PeriodAnalysisSelectionMessage> createPeriodAnalysisListener() {
		return new Listener<PeriodAnalysisSelectionMessage>() {
			public void update(PeriodAnalysisSelectionMessage info) {
				if (!Mediator.isMsgForDialog(Mediator.getParentDialog(WeightedWaveletZTransformResultDialog.this), info))
					return;
				setNewPhasePlotButtonState(true);
				selectedDataPoint = info.getDataPoint();
			}

			public boolean canBeRemoved() {
				return true;
			}
		};
	}

	// Helpers

	private NamedComponent createChart(String suffix, WWZ2DPlotModel model,
			double minRange, double maxRange) {

		String name = model.getRangeType().toString() + " "
				+ LocaleProps.get("VERSUS") + " "
				+ model.getDomainType().toString() + " " + suffix;

		JFreeChart chart1 = ChartFactory
				.createXYLineChart(chartTitle + ": " + name, model
						.getDomainType().toString(), model.getRangeType()
						.toString(), model, PlotOrientation.VERTICAL, true,
						true, false);

		JFreeChart chart2 = ChartFactory
				.createScatterPlot(chartTitle + ": " + name, model
						.getDomainType().toString(), model.getRangeType()
						.toString(), model, PlotOrientation.VERTICAL, true,
						true, false);

		// Make a combined chart.
		chart2.getXYPlot().setDataset(1, model);
		chart2.getXYPlot().setRenderer(1, chart1.getXYPlot().getRenderer());
		chart2.getXYPlot().setDatasetRenderingOrder(
				DatasetRenderingOrder.FORWARD);

		double rangeMargin = ((maxRange - minRange) / 100) * 10;
		minRange -= rangeMargin;
		maxRange += rangeMargin;

		WWZPlotPane pane = new WWZPlotPane(chart2, model, minRange, maxRange);
		startupAndCleanupComponents.add(pane);

		return new NamedComponent(name, pane);
	}

	private NamedComponent createContourChart(String prefix,
			WWZ3DPlotModel model, double minDomain, double maxDomain,
			double minRange, double maxRange, double minZ, double maxZ) {

		XYBlockRenderer renderer = new XYBlockRenderer();
		renderer.setBlockWidth(10);
		// renderer.setBlockHeight(100);

		double increments = (maxZ - minZ) / 6;
		if (minZ == maxZ)
			maxZ++; // make sure the scale is valid
		LookupPaintScale scale = new LookupPaintScale(minZ, maxZ, Color.white);
		// PaintScale scale = new GrayPaintScale(minZ, maxZ);
		scale.add(minZ, Color.MAGENTA);
		scale.add(minZ + increments, Color.BLUE);
		scale.add(minZ + increments * 2, Color.GREEN);
		scale.add(minZ + increments * 3, Color.YELLOW);
		scale.add(minZ + increments * 4, Color.ORANGE);
		scale.add(minZ + increments * 5, Color.RED);
		renderer.setPaintScale(scale);

		NumberAxis xAxis = new NumberAxis(model.getDomainType().toString());
		xAxis.setLowerBound(minDomain);
		xAxis.setUpperBound(maxDomain);
		NumberAxis yAxis = new NumberAxis(model.getRangeType().toString());
		XYPlot plot = new XYPlot(model, xAxis, yAxis, renderer);
		plot.setBackgroundPaint(Color.white);
		plot.setDomainGridlinesVisible(false);

		plot.setRangeGridlinePaint(Color.white);
		plot.setRangeGridlinesVisible(false);

		plot.setDomainCrosshairVisible(true);
		plot.setRangeCrosshairVisible(true);
		plot.setDomainCrosshairValue(0);
		plot.setRangeCrosshairValue(0);

		plot.setAxisOffset(new RectangleInsets(5, 5, 5, 5));
		plot.setOutlinePaint(Color.blue);

		JFreeChart chart = new JFreeChart(plot);

		NumberAxis scaleAxis = new NumberAxis(model.getZType().toString());
		scaleAxis.setAxisLinePaint(Color.white);
		scaleAxis.setTickMarkPaint(Color.white);
		scaleAxis.setTickLabelFont(new Font("Dialog", Font.PLAIN, 7));

		PaintScaleLegend legend = new PaintScaleLegend(scale, scaleAxis);
		legend.setStripOutlineVisible(false);
		legend.setSubdivisionCount(20);
		legend.setAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
		legend.setAxisOffset(5.0);
		legend.setMargin(new RectangleInsets(5, 5, 5, 5));
		legend.setFrame(new BlockBorder(Color.red));
		legend.setPadding(new RectangleInsets(10, 10, 10, 10));
		legend.setStripWidth(10);
		legend.setPosition(RectangleEdge.BOTTOM);
		chart.removeLegend();
		chart.addSubtitle(legend);
		chart.setBackgroundPaint(new GradientPaint(0, 0, Color.WHITE, 0, 1000,
				Color.WHITE));
		ChartUtils.applyCurrentTheme(chart);

		double rangeMargin = ((maxRange - minRange) / 100) * 25;
		minRange -= rangeMargin;
		maxRange += rangeMargin;

		String name = prefix + model.getRangeType().toString() + " "
				+ LocaleProps.get("VERSUS") + " "
				+ model.getDomainType().toString() + " "
				+ LocaleProps.get("VERSUS") + " " + model.getZType().toString()
				+ " " + LocaleProps.get("CONTOUR");

		WWZPlotPane pane = new WWZPlotPane(chart, model, minRange, maxRange);
		startupAndCleanupComponents.add(pane);

		return new NamedComponent(name, pane);
	}

	/**
	 * Create a 3D plot of 3 WWZ coordinates, e.g. of of time, period, wwz from
	 * maximal stats.
	 * 
	 * @return A named component suitable for adding to dialog.
	 */
	private NamedComponent create3DStatsPlot(String suffix,
			WWZCoordinateType xType, WWZCoordinateType yType,
			WWZCoordinateType zType, List<WWZStatistic> stats) {
		Plot3DPanel plot = new Plot3DPanel();
		plot
				.setAxisLabels(xType.toString(), yType.toString(), zType
						.toString());

		int size = stats.size();
		double[][] xyz = new double[3][size];

		for (int i = 0; i < size; i++) {
			WWZStatistic stat = stats.get(i);
			xyz[0][i] = stat.getValue(xType);
			xyz[1][i] = stat.getValue(yType);
			xyz[2][i] = stat.getValue(zType);
		}

		plot.addBarPlot(LocaleProps.get("WWZ_STATISTICS_3D_PLOT"), Color.GREEN,
				xyz);

		String name = yType.toString() + " " + LocaleProps.get("VERSUS") + " "
				+ xType.toString() + " " + LocaleProps.get("VERSUS") + " "
				+ zType.toString() + " 3D " + suffix;

		return new NamedComponent(name, plot);
	}

	private double getMaxValue(WWZCoordinateType type) {
		double value = 0;

		switch (type) {
		case PERIOD:
			value = wwt.getMaxPeriod();
			break;
		case FREQUENCY:
			value = wwt.getMaxFreq();
			break;
		case SEMI_AMPLITUDE:
			value = wwt.getMaxAmp();
			break;
		case WWZ:
			value = wwt.getMaxWWZ();
			break;
		default:
			throw new IllegalArgumentException("Invalid type: "
					+ type.toString());
		}

		return value;
	}

	private double getMinValue(WWZCoordinateType type) {
		double value = 0;

		switch (type) {
		case PERIOD:
			value = wwt.getMinPeriod();
			break;
		case FREQUENCY:
			value = wwt.getMinFreq();
			break;
		case SEMI_AMPLITUDE:
			value = wwt.getMinAmp();
			break;
		case WWZ:
			value = wwt.getMinWWZ();
			break;
		default:
			throw new IllegalArgumentException("Invalid type: "
					+ type.toString());
		}

		return value;
	}

	@Override
	public void startup() {
		periodAnalysisListener = this.createPeriodAnalysisListener();

		Mediator.getInstance().getPeriodAnalysisSelectionNotifier()
				.addListener(periodAnalysisListener);

		for (IStartAndCleanup component : startupAndCleanupComponents) {
			component.startup();
		}
	}

	@Override
	public void cleanup() {
		Mediator.getInstance().getPeriodAnalysisSelectionNotifier()
				.removeListenerIfWilling(periodAnalysisListener);

		for (IStartAndCleanup component : startupAndCleanupComponents) {
			component.cleanup();
		}
	}
}
