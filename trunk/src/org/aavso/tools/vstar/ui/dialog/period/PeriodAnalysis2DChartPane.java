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

import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.PeriodAnalysisSelectionMessage;
import org.aavso.tools.vstar.ui.model.plot.PeriodAnalysis2DPlotModel;
import org.aavso.tools.vstar.util.notification.Listener;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.XYItemEntity;

/**
 * This class represents a chart panel.
 */
public class PeriodAnalysis2DChartPane extends ChartPanel implements
		ChartMouseListener, Listener<PeriodAnalysisSelectionMessage> {

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
		Mediator.getInstance().getPeriodAnalysisSelectionNotifier()
				.addListener(this);
	}

	private void configureChart() {
		chart.getXYPlot().setDomainCrosshairValue(0);
		chart.getXYPlot().setRangeCrosshairValue(0);

		chart.getXYPlot().setDomainCrosshairVisible(true);
		chart.getXYPlot().setRangeCrosshairVisible(true);
	}

	public void chartMouseClicked(ChartMouseEvent event) {
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

	// PeriodAnalysisSelectionMessage listener methods.

	public boolean canBeRemoved() {
		return true;
	}

	public void update(PeriodAnalysisSelectionMessage info) {
		if (info.getSource() != this) {
			double x = model.getDomainValues().get(info.getItem());
			double y = model.getRangeValues().get(info.getItem());

			chart.getXYPlot().setDomainCrosshairValue(x);
			chart.getXYPlot().setRangeCrosshairValue(y);
		}
	}
}
