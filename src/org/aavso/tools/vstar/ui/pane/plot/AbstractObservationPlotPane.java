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
package org.aavso.tools.vstar.ui.pane.plot;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.input.AbstractObservationRetriever;
import org.aavso.tools.vstar.ui.VStar;
import org.aavso.tools.vstar.ui.dialog.ObservationDetailsDialog;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.message.ObservationSelectionMessage;
import org.aavso.tools.vstar.ui.mediator.message.PanRequestMessage;
import org.aavso.tools.vstar.ui.mediator.message.ZoomRequestMessage;
import org.aavso.tools.vstar.ui.mediator.message.ZoomType;
import org.aavso.tools.vstar.ui.model.plot.ObservationAndMeanPlotModel;
import org.aavso.tools.vstar.util.locale.LocaleProps;
import org.aavso.tools.vstar.util.notification.Listener;
import org.aavso.tools.vstar.util.prefs.ChartPropertiesPrefs;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.SeriesRenderingOrder;
import org.jfree.chart.renderer.xy.XYErrorRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.Range;
import org.jfree.data.general.Dataset;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DatasetChangeListener;

/**
 * This class is the base class for chart panes containing a plot of a set of
 * valid observations. It is genericised on observation model.
 */
@SuppressWarnings("serial")
abstract public class AbstractObservationPlotPane<T extends ObservationAndMeanPlotModel>
		extends JPanel implements ChartMouseListener, DatasetChangeListener {

	protected T obsModel;

	protected Dimension bounds;

	protected String title;
	protected String subTitle;
	protected String domainTitle;
	protected String rangeTitle;

	protected AbstractObservationRetriever retriever;

	protected JFreeChart chart;

	protected ChartPanel chartPanel;

	protected JPanel chartControlPanel;

	protected JTextArea obsInfo;

	protected VStarPlotDataRenderer renderer;

	// Show error bars?
	protected boolean showErrorBars;

	// Show cross-hairs?
	protected boolean showCrossHairs;

	protected JButton visibilityButton;

	// Last selected point and observation.
	protected Point2D lastPointClicked;
	protected ValidObservation lastObSelected;
	protected Dataset lastDatasetSelected;

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
	 * @param retriever
	 *            The observation retriever for observations in this plot.
	 */
	public AbstractObservationPlotPane(String title, String subTitle,
			String domainTitle, String rangeTitle, T obsModel,
			Dimension bounds, AbstractObservationRetriever retriever) {
		super();

		this.title = title;
		this.subTitle = subTitle;
		this.domainTitle = domainTitle;
		this.rangeTitle = rangeTitle;

		this.obsModel = obsModel;

		this.bounds = bounds;

		this.retriever = retriever;

		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

		this.showErrorBars = true;
		this.showCrossHairs = true;

		// Create a chart with legend, tooltips, and URLs showing
		// and add it to the panel.
		this.chartPanel = new ChartPanel(ChartFactory.createScatterPlot(title,
				domainTitle, rangeTitle, obsModel, PlotOrientation.VERTICAL,
				true, true, true));

		this.chartPanel.setPreferredSize(bounds);

		this.chart = chartPanel.getChart();

		if (subTitle != null && "".equals(subTitle.trim())) {
			this.chart.addSubtitle(new TextTitle(subTitle));
		}

		this.renderer = new VStarPlotDataRenderer();
		this.renderer.setDrawYError(this.showErrorBars);

		this.lastPointClicked = null;
		this.lastObSelected = null;
		this.lastDatasetSelected = null;

		// Should reduce number of Java2D draw operations.
		// this.renderer.setDrawSeriesLineAsPath(true);

		// Tell renderer which series' elements should initially be
		// rendered (i.e. visible) or joined.
		// TODO: in future, we should isolate series joining logic to this view
		// class and its subclasses; see also comments in
		// ObservationAndMeanPlotModel.
		setJoinedSeries();
		setSeriesVisibility();

		// this.chart.getXYPlot().setSeriesRenderingOrder(
		// SeriesRenderingOrder.FORWARD);
		this.chart.getXYPlot().setSeriesRenderingOrder(
				SeriesRenderingOrder.REVERSE);

		this.chart.getXYPlot().setDomainCrosshairLockedOnData(true);
		this.chart.getXYPlot().setRangeCrosshairLockedOnData(true);

		// Make it possible to pan the plot.
		chart.getXYPlot().setDomainPannable(true);
		chart.getXYPlot().setRangePannable(true);

		chart.getXYPlot().setRenderer(renderer);

		setupCrossHairs();

		setSeriesColors();
		setSeriesSizes();

		SeriesType.getSeriesColorChangeNotifier().addListener(
				createSeriesColorChangeListener());

		SeriesType.getSeriesSizeChangeNotifier().addListener(
				createSeriesSizeChangeListener());

		// We want the magnitude scale to go from high to low as we ascend the
		// Y axis since as magnitude values get smaller, brightness increases.
		NumberAxis rangeAxis = (NumberAxis) chart.getXYPlot().getRangeAxis();
		rangeAxis.setInverted(true);

		setMagScale();

		obsModel.addChangeListener(this);

		this.add(chartPanel);
		this.add(Box.createRigidArea(new Dimension(0, 10)));

		// Listen to events.

		Mediator.getInstance().getObservationSelectionNotifier()
				.addListener(createObservationSelectionListener());

		Mediator.getInstance().getZoomRequestNotifier()
				.addListener(createZoomRequestListener());

		Mediator.getInstance().getPanRequestNotifier()
				.addListener(createPanRequestListener());
		
		// Update chart properties.
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				// Invoke in the UI thread (some paranoia)				
				updateChartProperties();
			}
		});
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

	/**
	 * @return the lastPointClicked
	 */
	public Point2D getLastPointClicked() {
		return lastPointClicked;
	}

	/**
	 * @return the lastObSelected
	 */
	public ValidObservation getLastObSelected() {
		return lastObSelected;
	}

	/**
	 * @return the lastDatasetSelected
	 */
	public Dataset getLastDatasetSelected() {
		return lastDatasetSelected;
	}

	/**
	 * Returns a series color change listener.
	 */
	private Listener<Map<SeriesType, Color>> createSeriesColorChangeListener() {
		return new Listener<Map<SeriesType, Color>>() {
			// Update the series colors.
			public void update(Map<SeriesType, Color> info) {
				setSeriesColors();
			}

			public boolean canBeRemoved() {
				return true;
			}
		};
	}

	/**
	 * Returns a series size change listener.
	 */
	private Listener<Map<SeriesType, Integer>> createSeriesSizeChangeListener() {
		return new Listener<Map<SeriesType, Integer>>() {
			// Update the series sizes.
			public void update(Map<SeriesType, Integer> info) {
				setSeriesSizes();
			}

			public boolean canBeRemoved() {
				return true;
			}
		};
	}

	/**
	 * Was there a change in the series visibility? Some callers may want to
	 * invoke this only for its side effects, while others may also want to know
	 * the result.
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
		}

		return delta;
	}

	/**
	 * Set the visibility of each series.
	 */
	protected void setSeriesVisibility() {
		Map<SeriesType, Boolean> seriesVisibilityMap = obsModel
				.getSeriesVisibilityMap();

		for (SeriesType seriesType : seriesVisibilityMap.keySet()) {
			int seriesNum = obsModel.getSrcTypeToSeriesNumMap().get(seriesType);
			renderer.setSeriesVisible(seriesNum,
					seriesVisibilityMap.get(seriesType));
		}
	}

	/**
	 * Set the color of each series.
	 */
	private void setSeriesColors() {
		Map<Integer, SeriesType> seriesNumToTypeMap = obsModel
				.getSeriesNumToSrcTypeMap();

		for (int seriesNum : seriesNumToTypeMap.keySet()) {
			Color color = SeriesType.getColorFromSeries(seriesNumToTypeMap
					.get(seriesNum));
			renderer.setSeriesPaint(seriesNum, color);
		}
	}

	/**
	 * Set the size of each series.
	 */
	private void setSeriesSizes() {
		Map<Integer, SeriesType> seriesNumToTypeMap = obsModel
				.getSeriesNumToSrcTypeMap();

		for (int seriesNum : seriesNumToTypeMap.keySet()) {
			int size = SeriesType.getSizeFromSeries(seriesNumToTypeMap
					.get(seriesNum));
			renderer.setSeriesSize(seriesNum, size);
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
	
	/**
	 * 
	 * Updates properties of the chart
	 * 
	 * @param backgroundColor
	 *            chart background color
	 *            
	 * @param gridlinesColor
	 *             color of gridlines
	 * 
	 */
	public void updateChartProperties() {
		this.chart.getXYPlot().setBackgroundPaint(ChartPropertiesPrefs.getChartBackgroundColor());
		this.chart.getXYPlot().setDomainGridlinePaint(ChartPropertiesPrefs.getChartGridlinesColor());
		this.chart.getXYPlot().setRangeGridlinePaint(ChartPropertiesPrefs.getChartGridlinesColor());
	}

	// From ChartMouseListener interface.
	// If the user double-clicks on a plot point, send a selection
	// message and open an information dialog. Also record the selection.
	public void chartMouseClicked(ChartMouseEvent event) {

		// Now, have we selected an observation?
		if (event.getEntity() instanceof XYItemEntity) {
			// The trigger point should correspond to the XYItemEntity's
			// position.
			lastPointClicked = event.getTrigger().getPoint();

			XYItemEntity entity = (XYItemEntity) event.getEntity();
			lastDatasetSelected = entity.getDataset();
			int series = entity.getSeriesIndex();
			int item = entity.getItem();
			lastObSelected = obsModel.getValidObservation(series, item);

			if (event.getTrigger().getClickCount() == 2) {
				new ObservationDetailsDialog(lastObSelected);
			}
		} else {
			// ...else if not XYItemEntity as subject of the event, select a
			// valid observation by asking: which XYItemEntity is closest to the
			// cross hairs?

			// Where are the cross hairs pointing?
			lastPointClicked = chartPanel.getAnchor();

			EntityCollection entities = chartPanel.getChartRenderingInfo()
					.getEntityCollection();

			double closestDist = Double.MAX_VALUE;

			// Note: This operation is linear in the number of visible
			// observations!
			// Unfortunately, the list of XYItemEntities must always be searched
			// exhaustively since we don't know which XYItemEntity will turn out
			// to be closest to the mouse selection. Actually, this may not be
			// the case if we can assume an ordering of XYItemEntities by domain
			// (X). If so, once itemBounds.getCenterX() is greater than
			// lastPointClicked.getX(), we could terminate the loop. But I don't
			// know if we can make that assumption.
			@SuppressWarnings("unchecked")
			Iterator it = entities.iterator();
			while (it.hasNext()) {
				Object o = it.next();
				if (o instanceof XYItemEntity) {
					XYItemEntity entity = (XYItemEntity) o;
					Rectangle2D itemBounds = entity.getArea().getBounds2D();
					Point2D centerPt = new Point2D.Double(
							itemBounds.getCenterX(), itemBounds.getCenterY());

					double dist = centerPt.distance(lastPointClicked);
					if (dist < closestDist) {
						closestDist = dist;
						lastDatasetSelected = entity.getDataset();
						lastObSelected = obsModel.getValidObservation(
								entity.getSeriesIndex(), entity.getItem());
					}

					// Note: The approach below definitely does not work.
					// if (item.getArea().contains(lastPointClicked)) {
					// lastObSelected = obsModel.getValidObservation(item
					// .getSeriesIndex(), item.getItem());
					//
					// }
				}
			}
		}

		// If we found an observation (should always), send an observation
		// selection message.
		if (lastObSelected != null) {
			ObservationSelectionMessage message = new ObservationSelectionMessage(
					lastObSelected, this);
			Mediator.getInstance().getObservationSelectionNotifier()
					.notifyListeners(message);
		}
	}

	/**
	 * @return The time axis label.
	 */
	protected static String getTimeAxisLabel(AbstractObservationRetriever retriever) {
		// Custom axis title
		String axis_title;
		axis_title = retriever.getDomainTitle();
		if (axis_title != null && !"".equals(axis_title))
			return axis_title;
		else
			return String.format(LocaleProps.get("TIME"));
	}

	/**
	 * @return The brightness axis label.
	 */
	protected static String getBrightnessAxisLabel(AbstractObservationRetriever retriever) {
		// Custom axis title
		String axis_title;
		axis_title = retriever.getRangeTitle();
		if (axis_title != null && !"".equals(axis_title))
			return axis_title;
		else
			return String.format("%s (%s)", LocaleProps.get("BRIGHTNESS"), retriever.getBrightnessUnits());
	}

	/**
	 * Given an observation, update the last observation and x,y selections.
	 * 
	 * @param ob
	 *            The observation in question.
	 */
	protected void updateSelectionFromObservation(ValidObservation ob) {
		lastObSelected = ob;

		EntityCollection entities = chartPanel.getChartRenderingInfo()
				.getEntityCollection();

		// Note: This operation is linear in the number of observations!
		// However, the loop will on average terminate before exhaustively
		// searching all entries, i.e. when the observation is matched up
		// with the corresponding XYItemEntity. It's still O(n) though.
		// TODO: Use RendererUtilities method to return a list of entity indices
		// that lie in a given x (JD/phase) range; this would reduce n.
		Iterator it = entities.iterator();
		while (it.hasNext()) {
			Object o = it.next();
			if (o instanceof XYItemEntity) {
				XYItemEntity item = (XYItemEntity) o;
				// Dataset may not be same as primary observation model, e.g.
				// could be model function dataset (continuous model).
				if (item.getDataset() == obsModel) {
					try {
						double domainValue = obsModel.getXValue(
								item.getSeriesIndex(), item.getItem());
						double mag = obsModel.getYValue(item.getSeriesIndex(),
								item.getItem());

						// Since the data in the observations and in the
						// XYItemEntities should be the same, using equality
						// here ought to be safe.
						List<ValidObservation> obs = obsModel
								.getSeriesNumToObSrcListMap().get(
										item.getSeriesIndex());
						if (obsModel.getTimeElementEntity().getTimeElement(obs,
								item.getItem()) == domainValue
								&& ob.getMag() == mag) {
							Rectangle2D itemBounds = item.getArea()
									.getBounds2D();
							Point2D centerPt = new Point2D.Double(
									itemBounds.getCenterX(),
									itemBounds.getCenterY());

							lastPointClicked = centerPt;
							break;
						}
					} catch (IndexOutOfBoundsException e) {
						// Sometimes the series-index, item-index pair will have
						// changed or have become non-existent. Ignore but log.
						VStar.LOGGER.log(Level.WARNING,
								"Observation selection error", e);
					}
				}
			}
		}
	}

	// Returns an observation selection listener specific to the concrete plot.
	abstract protected Listener<ObservationSelectionMessage> createObservationSelectionListener();

	// Returns a zoom request listener specific to the concrete plot.
	abstract protected Listener<ZoomRequestMessage> createZoomRequestListener();

	// Returns a pan request listener specific to the concrete plot object.
	abstract protected Listener<PanRequestMessage> createPanRequestListener();

	/**
	 * Perform a zoom on the current plot.
	 * 
	 * @param info
	 *            The zoom message.
	 */
	protected void doZoom(ZoomType zoomType) {
		// "Reset" zoom level.
		if (zoomType == ZoomType.ZOOM_TO_FIT) {
			setMagScale();
		}

		// Only zoom if we have a selection in this plot.

		// See also
		// http://stackoverflow.com/questions/1512112/jfreechart-get-mouse-coordinates
		// if we are unconvinced about getting the right point at all zoom
		// levels.

		if (lastPointClicked != null) {
			// Determine zoom factor.
			double zoomDelta = 0.25; // TODO: get from prefs

			double factor = 1;

			if (zoomType == ZoomType.ZOOM_IN) {
				factor = 1 - zoomDelta;
			} else if (zoomType == ZoomType.ZOOM_OUT) {
				factor = 1 + zoomDelta;
			}

			// Zoom in on the specified point.
			PlotRenderingInfo plotInfo = chartPanel.getChartRenderingInfo()
					.getPlotInfo();

			Point2D sourcePoint = null;

			sourcePoint = lastPointClicked;

			boolean anchorOnPoint = lastPointClicked != null;

			chart.getXYPlot().zoomDomainAxes(factor, plotInfo, sourcePoint,
					anchorOnPoint);

			chart.getXYPlot().zoomRangeAxes(factor, plotInfo, sourcePoint,
					anchorOnPoint);
		}
	}

	/**
	 * From DatasetChangeListener.
	 * 
	 * When the dataset changes, e.g. series visibility, we want to set the
	 * appropriate magnitude value range, ignoring any series that is not
	 * visible. We also make sure that the loaded series colors are all set.
	 */
	public void datasetChanged(DatasetChangeEvent event) {
		setSeriesVisibility();
		setSeriesColors();
		setSeriesSizes();
		setMagScale();
	}

	// Tell renderer which series elements should be rendered
	// as visually joined with lines.
	protected void setJoinedSeries() {
		for (int series : obsModel
				.getSeriesWhoseElementsShouldBeJoinedVisually()) {
			this.renderer.setSeriesLinesVisible(series, true);
		}
	}

	// Helpers

	/**
	 * Set the appropriate magnitude value scale, ignoring any series that is
	 * not visible.
	 * 
	 * Note: for large datasets, this could be very expensive! Should maintain
	 * last min and max and only check observations for bands that have changed.
	 * Perhaps maintain a mappings from SeriesType to min/max mag.
	 */
	private void setMagScale() {
		double min = Double.MAX_VALUE;
		double max = -Double.MAX_VALUE;

		double minTimeElement = Double.MAX_VALUE;

		Map<Integer, List<ValidObservation>> seriesNumToObsMap = obsModel
				.getSeriesNumToObSrcListMap();

		Map<SeriesType, Boolean> seriesVisibilityMap = obsModel
				.getSeriesVisibilityMap();

		for (int seriesNum : seriesNumToObsMap.keySet()) {
			SeriesType seriesType = obsModel.getSeriesNumToSrcTypeMap().get(
					seriesNum);
			if (seriesVisibilityMap.get(seriesType)) {

				List<ValidObservation> obs = seriesNumToObsMap.get(seriesNum);
				int index = 0;
				for (ValidObservation ob : obs) {
					double mag = ob.getMagnitude().getMagValue();
					double uncert = ob.getMagnitude().getUncertainty();
					// If uncertainty not given, get HQ uncertainty if present.
					if (uncert == 0.0 && ob.getHqUncertainty() != null) {
						uncert = ob.getHqUncertainty();
					}

					if (mag - uncert < min) {
						min = mag - uncert;
					}

					if (mag + uncert > max) {
						max = mag + uncert;
					}

					// Get JD or phase.
					double timeElement = obsModel.getTimeElementEntity()
							.getTimeElement(obs, index);

					if (timeElement < minTimeElement) {
						minTimeElement = timeElement;
					}

					index++;
				}
			}
		}

		boolean obsToPlot = min <= max;

		if (obsToPlot) {
			if (min == max) {
				// For just one observation we will simply have one point at the
				// centre of the range.
				double mag = min;
				min = mag - 1;
				max = mag + 1;
			}

			// Add a small (1%) margin around min/max.
			double margin = (max - min) / 100;
			min -= margin;
			max += margin;

			NumberAxis magAxis = (NumberAxis) chart.getXYPlot().getRangeAxis();
			magAxis.setRange(new Range(min, max));

			// chart.getXYPlot().getDomainAxis().setAutoRange(false);
			// chart.getXYPlot().getRangeAxis().setAutoRange(false);

			// chart.getXYPlot().getDomainAxis().setAutoRangeMinimumSize(minJD);
			// chart.getXYPlot().getRangeAxis().setAutoRangeMinimumSize(min);
		}
	}

	// TODO: see also
	// chart.getXYPlot().get{Domain,Range}Axis().setAutoRangeMinimumSize()
	// as a way to fix the zoom-out-auto-range bug! Same as Range(min, max)
	// above?
}
