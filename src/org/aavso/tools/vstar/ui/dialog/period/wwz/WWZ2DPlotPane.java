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

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.message.PeriodAnalysisSelectionMessage;
import org.aavso.tools.vstar.ui.model.plot.WWZ2DPlotModel;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.Range;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DatasetChangeListener;

/**
 * This is a pane that contains a WWZ 2D plot chart.
 */
public class WWZ2DPlotPane extends JPanel implements ChartMouseListener,
		DatasetChangeListener {

	private JFreeChart chart;
	private WWZ2DPlotModel model;

	private double minRange;
	private double maxRange;

	/**
	 * Constructor
	 * 
	 */
	public WWZ2DPlotPane(JFreeChart chart, WWZ2DPlotModel model, double minRange,
			double maxRange) {
		super();

		this.chart = chart;
		this.model = model;

		this.minRange = minRange;
		this.maxRange = maxRange;

		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		this.setBorder(BorderFactory.createEtchedBorder());

		ChartPanel chartPanel = new ChartPanel(chart);
		this.add(chartPanel);

		configureChart();

		chartPanel.addChartMouseListener(this);
		model.addChangeListener(this);
	}

	/**
	 * @return the chart
	 */
	public JFreeChart getChart() {
		return chart;
	}

	/**
	 * @return the model
	 */
	public WWZ2DPlotModel getModel() {
		return model;
	}

	/**
	 * Set the renderer for the plot.
	 * 
	 * @param renderer
	 *            The XYItemRenderer subclass instance to set.
	 */
	public void setRenderer(XYItemRenderer renderer) {
		chart.getXYPlot().setRenderer(renderer);
	}

	protected void configureChart() {
		chart.getXYPlot().setBackgroundPaint(Color.WHITE);

		chart.getXYPlot().setDomainCrosshairValue(0);
		chart.getXYPlot().setRangeCrosshairValue(0);

		chart.getXYPlot().setDomainCrosshairVisible(true);
		chart.getXYPlot().setRangeCrosshairVisible(true);

		chart.getXYPlot().getRangeAxis()
				.setRange(new Range(minRange, maxRange));
	}

	@Override
	public void chartMouseClicked(ChartMouseEvent event) {
		if (event.getEntity() instanceof XYItemEntity) {
			XYItemEntity entity = (XYItemEntity) event.getEntity();
			int item = entity.getItem();
			PeriodAnalysisSelectionMessage message = new PeriodAnalysisSelectionMessage(
					this, model.getStats().get(item));
			Mediator.getInstance().getPeriodAnalysisSelectionNotifier()
					.notifyListeners(message);
		}
	}

	@Override
	public void chartMouseMoved(ChartMouseEvent event) {
		// Nothing to do: see period analysis 2D plot pane
	}

	@Override
	public void datasetChanged(DatasetChangeEvent event) {
		// Nothing to do: see period analysis 2D plot pane
	}
}
