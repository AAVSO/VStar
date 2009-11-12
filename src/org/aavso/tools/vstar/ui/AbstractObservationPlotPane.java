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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.ui.dialog.ObservationDetailsDialog;
import org.aavso.tools.vstar.ui.model.ObservationPlotModel;
import org.aavso.tools.vstar.ui.model.SeriesType;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.xy.XYErrorRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.Range;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DatasetChangeListener;

/**
 * This class is the base class for chart panes containing a plot of a set of
 * valid observations. It is genericised on observation model.
 */
abstract public class AbstractObservationPlotPane<T extends ObservationPlotModel>
		extends JPanel implements ChartMouseListener, DatasetChangeListener {

	protected T obsModel;

	protected JFreeChart chart;

	protected ChartPanel chartPanel;

	protected JPanel chartControlPanel;

	protected JTextArea obsInfo;

	// We use this renderer in order to be able to plot error bars.
	// TODO: or should we use StatisticalLineAndShapeRenderer? (for means plot?)
	protected XYErrorRenderer renderer;

	// Show error bars?
	protected boolean showErrorBars;

	protected JButton visibilityButton;

	// Axis titles.
	public static String JD_TITLE = "Time (Julian Date)";
	public static String PHASE_TITLE = "Phase";
	public static String MAG_TITLE = "Brightness (magnitude)";

	/**
	 * Constructor
	 * 
	 * @param title
	 *            The title for the chart.
	 * @param subTitle
	 *            The sub-title for the chart.
	 * @param domainTitle
	 *            The domain title (e.g. Julian Date, phase).
	 * @param rangeTitle
	 *            The range title (e.g. magnitude).
	 * @param obsModel
	 *            The data model to plot.
	 * @param bounds
	 *            The bounding box to which to set the chart's preferred size.
	 */
	public AbstractObservationPlotPane(String title, String subTitle,
			String domainTitle, String rangeTitle, T obsModel, Dimension bounds) {
		super();

		this.obsModel = obsModel;

		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

		this.showErrorBars = true;

		// Create a chart with legend, tooltips, and URLs showing
		// and add it to the panel.
		this.chartPanel = new ChartPanel(ChartFactory.createScatterPlot(title,
				domainTitle, rangeTitle, obsModel, PlotOrientation.VERTICAL,
				true, true, true));

		this.chartPanel.setPreferredSize(bounds);

		this.chart = chartPanel.getChart();

		this.chart.addSubtitle(new TextTitle(subTitle));

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

		setMagScale();
		
		obsModel.addChangeListener(this);

		this.add(chartPanel);
		this.add(Box.createRigidArea(new Dimension(0, 10)));

		// Create a panel that can be used to add chart control widgets.
		chartControlPanel = createChartControlPanel();
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
	protected JPanel createChartControlPanel() {
		chartControlPanel = new JPanel();
		chartControlPanel.setLayout(new BoxLayout(chartControlPanel,
				BoxLayout.LINE_AXIS));

		chartControlPanel.setBorder(BorderFactory
				.createTitledBorder("Plot Control"));

		// A button to change series visibility.
		JButton visibilityButton = new JButton("Change Series");
		visibilityButton.addActionListener(createSeriesChangeButtonListener());
		chartControlPanel.add(visibilityButton);

		// A checkbox to show/hide error bars.
		JCheckBox errorBarCheckBox = new JCheckBox("Show error bars?");
		errorBarCheckBox.setSelected(this.showErrorBars);
		errorBarCheckBox.addActionListener(createErrorBarCheckBoxListener());
		chartControlPanel.add(errorBarCheckBox);

		return chartControlPanel;
	}

	// / Return a listener for the error bar visibility checkbox.
	private ActionListener createErrorBarCheckBoxListener() {
		final AbstractObservationPlotPane<T> self = this;
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
	 * Was there a change in the series visibility? Some callers may want to
	 * invoke this only for its side effects, while others may want to know the
	 * result.
	 * 
	 * @param deltaMap
	 *            A mapping from series number to whether or not each series'
	 *            visibility was changed.
	 * 
	 * @return Was there a change in the visibility of any series?
	 */
	protected boolean seriesVisibilityChange(Map<Integer, Boolean> deltaMap) {
		boolean delta = false;

		for (int seriesNum : deltaMap.keySet()) {
			boolean visibility = deltaMap.get(seriesNum);
			delta |= obsModel.changeSeriesVisibility(seriesNum, visibility);
			renderer.setSeriesVisible(seriesNum, visibility);
		}

		return delta;
	}

	/**
	 * Set the appearance of each series with respect to size, shape, and color.
	 */
	private void setSeriesAppearance() {
		Map<Integer, SeriesType> seriesToTypeMap = obsModel.getSeriesNumToSrcTypeMap();
		
		for (int seriesNum : seriesToTypeMap.keySet()) {
			Color color = seriesToTypeMap.get(seriesNum).getColor();
			renderer.setSeriesPaint(seriesNum, color);
			// TODO: what do we want this to be?
			//RectangularShape shape = new Rectangle2D.Double(-1, -1, 3, 3);
			//RectangularShape shape = new Ellipse2D.Double(-1, 1, 3, 3);
			//renderer.setSeriesShape(seriesNum, shape);
		}
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
			new ObservationDetailsDialog(obsModel.getValidObservation(series,
					item));
		}
	}

	// From ChartMouseListener
	public void chartMouseMoved(ChartMouseEvent arg0) {
		// Nothing to do here.
		// TODO: Zoom?
	}

	/**
	 * From DatasetChangeListener.
	 * 
	 * When the dataset changes, e.g. series visibility, we want to set the
	 * appropriate magnitude value range, ignoring any series that is not
	 * visible.
	 */
	public void datasetChanged(DatasetChangeEvent event) {
		setMagScale();
	}
	
	// Helpers
	
	/**
	 * Set the appropriate magnitude value scale, ignoring any series that 
	 * is not visible.
	 */
	private void setMagScale() {
		double min = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;

		Map<Integer, List<ValidObservation>> seriesNumToObsMap = obsModel
				.getSeriesNumToObSrcListMap();

		Map<Integer, Boolean> seriesVisibilityMap = obsModel.getSeriesVisibilityMap();
		
		for (int series : seriesNumToObsMap.keySet()) {
			if (seriesVisibilityMap.get(series)) {
				for (ValidObservation ob : seriesNumToObsMap.get(series)) {
					double mag = ob.getMagnitude().getMagValue();
					if (mag < min) {
						min = mag;
					} else if (mag > max) {
						max = mag;
					}
				}
			}
		}
		
		// Add a small (1%) margin around min/max.
		double margin = (max-min)/100;
		min -= margin;
		max += margin;
		
		NumberAxis magAxis = (NumberAxis) chart.getXYPlot().getRangeAxis();
		magAxis.setRange(new Range(min, max));
	}	
}
