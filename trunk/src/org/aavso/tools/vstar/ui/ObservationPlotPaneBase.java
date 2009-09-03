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
package org.aavso.tools.vstar.ui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.aavso.tools.vstar.ui.dialog.ObservationInfoDialog;
import org.aavso.tools.vstar.ui.dialog.SeriesVisibilityDialog;
import org.aavso.tools.vstar.ui.model.ObservationPlotModel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.xy.XYErrorRenderer;

/**
 * This class is the base class for chart panes containing a plot of a set of
 * valid observations. It is genericised on observation model.
 */
abstract public class ObservationPlotPaneBase<T extends ObservationPlotModel>
		extends JPanel implements ChartMouseListener {

	protected T obsModel;

	protected JFreeChart chart;

	protected ChartPanel chartPanel;

	protected JPanel chartControlPanel;

	protected JTextArea obsInfo;

	// We use this renderer in order to be able to plot error bars.
	// TODO: or should we use StatisticalLineAndShapeRenderer? (for means plot?)
	protected XYErrorRenderer renderer;

	// Show error bars?
	private boolean showErrorBars;

	protected JButton visibilityButton;

	/**
	 * Constructor
	 * 
	 * @param title
	 *            The title for the chart.
	 * @param obsModel
	 *            The data model to plot.
	 * @param bounds
	 *            The bounding box to which to set the chart's preferred size.
	 */
	public ObservationPlotPaneBase(String title, T obsModel, Dimension bounds) {
		super();

		this.obsModel = obsModel;

		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

		this.showErrorBars = true;

		// Create a chart with legend, tooltips, and URLs showing
		// and add it to the panel.
		this.chartPanel = new ChartPanel(ChartFactory.createScatterPlot(title,
				"Julian Date", "Magnitude", obsModel, PlotOrientation.VERTICAL,
				true, true, true));

		this.chartPanel.setPreferredSize(bounds);

		chart = chartPanel.getChart();

		this.renderer = new XYErrorRenderer();
		this.renderer.setDrawYError(this.showErrorBars);

		// Tell renderer which series elements should be rendered
		// as visually joined with lines.
		// TODO: change return type of getter below to be Set<Integer>
		for (int series : obsModel
				.getSeriesWhoseElementsShouldBeJoinedVisually()) {
			this.renderer.setSeriesLinesVisible(series, true);
		}

		// Tell renderer which series' elements should initially be
		// rendered, i.e. visible.
		for (int series : obsModel.getSeriesVisibilityMap().keySet()) {
			this.renderer.setSeriesVisible(series, obsModel
					.getSeriesVisibilityMap().get(series));
		}

		chart.getXYPlot().setRenderer(renderer);

		setupCrossHairs();

		setSeriesAppearance();

		// We want the magnitude scale to go from high to low as we ascend the
		// Y axis since as magnitude values get smaller, brightness increases.
		NumberAxis rangeAxis = (NumberAxis) chart.getXYPlot().getRangeAxis();
		rangeAxis.setInverted(true);

		this.add(chartPanel);

		this.add(Box.createRigidArea(new Dimension(0, 10)));

		// Create a panel that can be used to add chart control widgets.
		chartControlPanel = new JPanel();
		chartControlPanel.setLayout(new BoxLayout(chartControlPanel,
				BoxLayout.LINE_AXIS));
		createChartControlPanel(chartControlPanel);
		this.add(chartControlPanel);
	}

	/**
	 * @return the chartPanel
	 */
	public ChartPanel getChartPanel() {
		return chartPanel;
	}

	/**
	 * @return the chartControlPanel
	 */
	public JPanel getChartControlPanel() {
		return chartControlPanel;
	}

	/**
	 * @return the obsInfo
	 */
	public JTextArea getObsInfo() {
		return obsInfo;
	}

	/**
	 * @return the renderer
	 */
	public XYErrorRenderer getRenderer() {
		return renderer;
	}

	// Populate a panel that can be used to add chart control widgets.
	protected void createChartControlPanel(JPanel chartControlPanel) {
		// A button to change series visibility.
		JButton visibilityButton = new JButton("Change Series");
		visibilityButton.addActionListener(createSeriesChangeButtonListener());
		chartControlPanel.add(visibilityButton);

		// A checkbox to show/hide error bars.
		JCheckBox errorBarCheckBox = new JCheckBox("Show error bars?");
		errorBarCheckBox.setSelected(this.showErrorBars);
		errorBarCheckBox.addActionListener(createErrorBarCheckBoxListener());
		chartControlPanel.add(errorBarCheckBox);
	}

	// Return a listener for the error bar visibility checkbox.
	private ActionListener createErrorBarCheckBoxListener() {
		final ObservationPlotPaneBase<T> self = this;
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				self.toggleErrorBars();
			}
		};
	}

	/**
	 * Show/hide the error bars.
	 */
	private void toggleErrorBars() {
		this.showErrorBars = !this.showErrorBars;
		this.renderer.setDrawYError(this.showErrorBars);
	}

	/**
	 * Return a listener for the "change series visibility" button.
	 */
	abstract protected ActionListener createSeriesChangeButtonListener();

	/**
	 * Invokes the series change dialog and return whether or not there was a
	 * change.
	 * 
	 * @return Was there a change?
	 */
	protected boolean invokeSeriesChangeDialog() {
		boolean delta = false;

		SeriesVisibilityDialog dialog = new SeriesVisibilityDialog(obsModel);
		if (!dialog.isCancelled()) {
			Map<Integer, Boolean> deltaMap = dialog.getVisibilityDeltaMap();
			for (int seriesNum : deltaMap.keySet()) {
				boolean visibility = deltaMap.get(seriesNum);
				delta |= obsModel.changeSeriesVisibility(seriesNum, visibility);
				renderer.setSeriesVisible(seriesNum, visibility);
			}
		}

		return delta;
	}

	/**
	 * Set the appearance of each series with respect to size, shape, and color.
	 */
	private void setSeriesAppearance() {
		// for (int i=0;i<obsModel.getSeriesCount();i++) {
		// renderer.setSeriesShape(i, new Rectangle2D.Double(-1,-1,3,3));
		// }
	}

	// Cross hair handling methods.

	private void setupCrossHairs() {
		chart.getXYPlot().setDomainCrosshairValue(0);
		chart.getXYPlot().setRangeCrosshairValue(0);

		chart.getXYPlot().setDomainCrosshairVisible(true);
		chart.getXYPlot().setRangeCrosshairVisible(true);

		chartPanel.addChartMouseListener(this);
	}

	// From ChartMouseListener
	public void chartMouseClicked(ChartMouseEvent event) {
		if (event.getEntity() instanceof XYItemEntity) {
			XYItemEntity entity = (XYItemEntity) event.getEntity();
			int series = entity.getSeriesIndex();
			int item = entity.getItem();
			new ObservationInfoDialog(obsModel
					.getValidObservation(series, item));
		}
	}

	public void chartMouseMoved(ChartMouseEvent arg0) {
		// Nothing to do here.
		// TODO: Zoom?
	}
}
