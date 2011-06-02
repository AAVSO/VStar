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

import java.awt.Color;

import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.message.PeriodAnalysisRefinementMessage;
import org.aavso.tools.vstar.ui.mediator.message.PeriodAnalysisSelectionMessage;
import org.aavso.tools.vstar.ui.model.plot.PeriodAnalysis2DPlotModel;
import org.aavso.tools.vstar.util.notification.Listener;
import org.aavso.tools.vstar.util.period.PeriodAnalysisCoordinateType;
import org.aavso.tools.vstar.util.period.dcdft.PeriodAnalysisDataPoint;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYLineAnnotation;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DatasetChangeListener;

/**
 * This class represents a chart panel.
 */
public class PeriodAnalysis2DChartPane extends ChartPanel implements
		ChartMouseListener, DatasetChangeListener,
		Listener<PeriodAnalysisSelectionMessage> {

	private static final int DATA_SERIES = 0;
	private static final int TOP_HIT_SERIES = 1;

	private JFreeChart chart;
	private PeriodAnalysis2DPlotModel model;

	/**
	 * Constructor
	 * 
	 * @param chart
	 *            The JFreeChart chart.
	 */
	public PeriodAnalysis2DChartPane(JFreeChart chart,
			PeriodAnalysis2DPlotModel model) {
		super(chart);

		this.chart = chart;
		this.model = model;

		configureChart();

		this.addChartMouseListener(this);
		model.addChangeListener(this);

		// We listen for and generate period analysis selection messages.
		Mediator.getInstance().getPeriodAnalysisSelectionNotifier()
				.addListener(this);

		// We listen for period analysis refinement messages.
		Mediator.getInstance().getPeriodAnalysisRefinementNotifier()
				.addListener(createRefinementListener());
	}

	private void configureChart() {
		chart.getXYPlot().setBackgroundPaint(Color.WHITE);

		chart.getXYPlot().setDomainCrosshairValue(0);
		chart.getXYPlot().setRangeCrosshairValue(0);

		chart.getXYPlot().setDomainCrosshairVisible(true);
		chart.getXYPlot().setRangeCrosshairVisible(true);
	}

	public void chartMouseClicked(ChartMouseEvent event) {
//		double x = 0.02;
//		double y = 250;

		// XYPointerAnnotation pointer = new XYPointerAnnotation("Eureka!", x,
		// y, 270);
		// pointer.setTipRadius(10);
		// pointer.setBaseRadius(35);
		// pointer.setFont(new Font("SansSerif", Font.PLAIN, 9));
		// pointer.setPaint(Color.BLUE);
		// pointer.setTextAnchor(TextAnchor.HALF_ASCENT_RIGHT);
		// chart.getXYPlot().addAnnotation(pointer);
		//
		// XYLineAnnotation line = new XYLineAnnotation(x, 0, x, y);
		// chart.getXYPlot().addAnnotation(line);

		if (event.getEntity() instanceof XYItemEntity) {
			XYItemEntity entity = (XYItemEntity) event.getEntity();
			int item = entity.getItem();
			PeriodAnalysisSelectionMessage message = new PeriodAnalysisSelectionMessage(
					this, item);
			Mediator.getInstance().getPeriodAnalysisSelectionNotifier()
					.notifyListeners(message);
		}
	}

	public void chartMouseMoved(ChartMouseEvent event) {
		// Nothing to do.
	}

	@Override
	public void datasetChanged(DatasetChangeEvent event) {
		// Set series colors.
		XYItemRenderer renderer = chart.getXYPlot().getRenderer();
		for (int seriesNum = 0; seriesNum < model.getSeriesCount(); seriesNum++) {
			switch (seriesNum) {
			case DATA_SERIES:
				// renderer.setSeriesPaint(seriesNum, Color.PINK);
				break;
			case TOP_HIT_SERIES:
				// renderer.setSeriesPaint(seriesNum, Color.GREEN);
				break;
			}
		}
	}

	// PeriodAnalysisSelectionMessage listener methods.

	public boolean canBeRemoved() {
		return true;
	}

	public void update(PeriodAnalysisSelectionMessage info) {
		if (info.getSource() != this) {
			try {
				double x = model.getDomainValues().get(info.getItem());
				double y = model.getRangeValues().get(info.getItem());

				chart.getXYPlot().setDomainCrosshairValue(x);
				chart.getXYPlot().setRangeCrosshairValue(y);
			} catch (Throwable t) {
				// TODO: investigate! (e.g. Johnson V band, then click top-most
				// top hits table row.
				// t.printStackTrace();
			}
		}
	}

	// Create a period analysis refinement listener which sets annotations on
	// the plot according to the domain and range types.
	private Listener<PeriodAnalysisRefinementMessage> createRefinementListener() {
		return new Listener<PeriodAnalysisRefinementMessage>() {
			@Override
			public void update(PeriodAnalysisRefinementMessage info) {
				for (PeriodAnalysisDataPoint dataPoint : info.getNewTopHits()) {
					// TODO: The amplitude value for CLEANest always seems to be
					// the same. Why?
					if (model.getRangeType() == PeriodAnalysisCoordinateType.POWER) {
						double x = dataPoint.getValue(model.getDomainType());
						double y = dataPoint.getValue(model.getRangeType());
						XYLineAnnotation line = new XYLineAnnotation(x, 0, x, y);
						chart.getXYPlot().addAnnotation(line);
					}
				}
			}

			@Override
			public boolean canBeRemoved() {
				return false;
			}
		};
	}
}
