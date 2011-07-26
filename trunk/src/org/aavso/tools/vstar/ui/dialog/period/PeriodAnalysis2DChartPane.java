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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

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
public class PeriodAnalysis2DChartPane extends JPanel implements
		ChartMouseListener, DatasetChangeListener {

	private static final int DATA_SERIES = 0;
	private static final int TOP_HIT_SERIES = 1;

	private ChartPanel chartPanel;
	private JFreeChart chart;
	private PeriodAnalysis2DPlotModel model;

	private boolean permitLogarithmic;

	/**
	 * Constructor
	 * 
	 * @param chart
	 *            The JFreeChart chart.
	 * @param model
	 *            The plot model.
	 * @param permitLogarithmic
	 *            Should it be possible to toggle the plot between a normal and
	 *            logarithmic range?
	 */
	public PeriodAnalysis2DChartPane(JFreeChart chart,
			PeriodAnalysis2DPlotModel model, boolean permitLogarithmic) {
		super();

		this.chart = chart;
		this.model = model;
		this.permitLogarithmic = permitLogarithmic;

		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		this.setBorder(BorderFactory.createEtchedBorder());

		chartPanel = new ChartPanel(chart);
		this.add(chartPanel);

		this.add(createControlPanel());

		configureChart();

		chartPanel.addChartMouseListener(this);
		model.addChangeListener(this);

		// We listen for and generate period analysis selection messages.
		Mediator.getInstance().getPeriodAnalysisSelectionNotifier()
				.addListener(createPeriodAnalysisListener());

		// We listen for period analysis refinement messages.
		Mediator.getInstance().getPeriodAnalysisRefinementNotifier()
				.addListener(createRefinementListener());
	}

	/**
	 * @return the chartPanel
	 */
	public JFreeChart getChart() {
		return chart;
	}

	// Create and return a component that permits the "is logarithmic" property
	// of the model to be toggled.
	private JPanel createControlPanel() {
		JPanel panel = new JPanel();

		if (permitLogarithmic) {
			final JCheckBox logarithmicCheckBox = new JCheckBox("Logarithmic?");
			logarithmicCheckBox.setSelected(model.isLogarithmic());
			logarithmicCheckBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent event) {
					model.setLogarithmic(logarithmicCheckBox.isSelected());
					model.refresh();
				}
			});
			panel.add(logarithmicCheckBox, BorderLayout.CENTER);
		}

		return panel;
	}

	private void configureChart() {
		chart.getXYPlot().setBackgroundPaint(Color.WHITE);

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
					this, model.getDataPointFromItem(item));
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

	/**
	 * Update the crosshairs according to the selected data point.
	 */
	protected Listener<PeriodAnalysisSelectionMessage> createPeriodAnalysisListener() {
		final Component parent = this;

		return new Listener<PeriodAnalysisSelectionMessage>() {
			@Override
			public void update(PeriodAnalysisSelectionMessage info) {
				if (info.getSource() != parent) {
					double x = Double.NaN;
					double y = Double.NaN;

					switch (model.getDomainType()) {
					case FREQUENCY:
						x = info.getDataPoint().getFrequency();
						break;
					case PERIOD:
						x = info.getDataPoint().getPeriod();
						break;
					default:
						assert false;
						break;
					}

					switch (model.getRangeType()) {
					case POWER:
						y = info.getDataPoint().getPower();
						break;
					case AMPLITUDE:
						y = info.getDataPoint().getAmplitude();
						break;
					default:
						assert false;
						break;
					}

					chart.getXYPlot().setDomainCrosshairValue(x);
					chart.getXYPlot().setRangeCrosshairValue(y);
				}
			}

			@Override
			public boolean canBeRemoved() {
				return true;
			}
		};
	}

	// Create a period analysis refinement listener which sets annotations on
	// the plot according to the domain and range types.
	private Listener<PeriodAnalysisRefinementMessage> createRefinementListener() {
		return new Listener<PeriodAnalysisRefinementMessage>() {
			@Override
			public void update(PeriodAnalysisRefinementMessage info) {
				for (PeriodAnalysisDataPoint dataPoint : info.getNewTopHits()) {
					if (model.getRangeType() == PeriodAnalysisCoordinateType.POWER) {
						double x = dataPoint.getValue(model.getDomainType());
						double y = dataPoint.getValue(model.getRangeType());
						XYLineAnnotation line = new XYLineAnnotation(x, 0, x, y);
						chart.getXYPlot().addAnnotation(line);
					}

					model.refresh();
				}
			}

			@Override
			public boolean canBeRemoved() {
				return false;
			}
		};
	}
}
