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
package org.aavso.tools.vstar.ui.pane;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.ui.dialog.MeanSourceDialog;
import org.aavso.tools.vstar.ui.mediator.ObservationSelectionMessage;
import org.aavso.tools.vstar.ui.model.plot.ObservationAndMeanPlotModel;
import org.aavso.tools.vstar.util.notification.Listener;

/**
 * This class represents a chart pane containing a plot for a set of valid
 * observations along with mean-based data.
 */
public class ObservationAndMeanPlotPane extends
		AbstractObservationPlotPane<ObservationAndMeanPlotModel> {

	private TimeElementsInBinSettingPane timeElementsInBinSettingPane;

	// Should the means series elements be joined visually?
	private boolean joinMeans;

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
	 * @param timeElementsInBinSettingPane
	 *            The time-elements-in-mean-pane used by this observation and
	 *            mean plot pane.
	 * @param bounds
	 *            The bounding box to which to set the chart's preferred size.
	 */
	public ObservationAndMeanPlotPane(String title, String subTitle,
			String domainTitle, String rangeTitle,
			ObservationAndMeanPlotModel obsAndMeanModel,
			TimeElementsInBinSettingPane timeElementsInBinSettingPane,
			Dimension bounds) {

		super(title, subTitle, domainTitle, rangeTitle, obsAndMeanModel, bounds);

		this.timeElementsInBinSettingPane = timeElementsInBinSettingPane;

		this.joinMeans = true;

		addToChartControlPanel(this.getChartControlPanel());

		// Set the means series color.
		int meanSeriesNum = obsAndMeanModel.getMeansSeriesNum();
		if (meanSeriesNum != ObservationAndMeanPlotModel.NO_MEANS_SERIES) {
			this.getRenderer().setSeriesPaint(meanSeriesNum, SeriesType.getColorFromSeries(SeriesType.MEANS));
		}
	}

	/**
	 * Constructor
	 * 
	 * @param title
	 *            The title for the chart.
	 * @param subTitle
	 *            The sub-title for the chart.
	 * @param obsModel
	 *            The data model to plot.
	 * @param timeElementsInBinSettingPane
	 *            The time-elements-in-mean-pane used by this observation and
	 *            mean plot pane.
	 * @param bounds
	 *            The bounding box to which to set the chart's preferred size.
	 */
	public ObservationAndMeanPlotPane(String title, String subTitle,
			ObservationAndMeanPlotModel obsAndMeanModel,
			TimeElementsInBinSettingPane timeElementsInBinSettingPane,
			Dimension bounds) {

		this(title, subTitle, JD_TITLE, MAG_TITLE, obsAndMeanModel,
				timeElementsInBinSettingPane, bounds);
	}

	// Add means-specific widgets to chart control panel.
	private void addToChartControlPanel(JPanel chartControlPanel) {
		// A checkbox to determine whether or not to join mean series elements.
		JCheckBox joinMeansCheckBox = new JCheckBox("Join means?");
		joinMeansCheckBox.setSelected(true);
		joinMeansCheckBox.addActionListener(createJoinMeansCheckBoxListener());
		chartControlPanel.add(joinMeansCheckBox);

		chartControlPanel.add(Box.createHorizontalGlue());

		// An update time-elements-in-bin component.
		chartControlPanel.add(this.timeElementsInBinSettingPane);		
	}

	// Return a listener for the "join means visually" checkbox.
	private ActionListener createJoinMeansCheckBoxListener() {
		final ObservationAndMeanPlotPane self = this;
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				self.toggleJoinMeansSetting();
			}
		};
	}

	// Toggle the "join means" setting which dictates whether or
	// not the means are visually joined (by lines).
	private void toggleJoinMeansSetting() {
		this.joinMeans = !this.joinMeans;
		this.getRenderer().setSeriesLinesVisible(
				this.obsModel.getMeansSeriesNum(), this.joinMeans);
	}

	// Return a listener for the "change series visibility" button.
	protected ActionListener createSeriesChangeButtonListener() {
		final ObservationAndMeanPlotPane self = this;
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				int oldMeanSeriesSourceNum = self.obsModel
						.getMeanSourceSeriesNum();

				MeanSourceDialog dialog = new MeanSourceDialog(self.obsModel);

				if (!dialog.isCancelled()) {
					seriesVisibilityChange(dialog.getVisibilityDeltaMap());

					int newMeanSeriesSourceNum = dialog
							.getMeanSeriesSourceNum();

					if (newMeanSeriesSourceNum != oldMeanSeriesSourceNum) {
						// Update mean series based upon changed means
						// source series.
						obsModel.changeMeansSeries(obsModel
								.getTimeElementsInBin());
					}
				}
			}
		};
	}

	// Returns an observation selection listener.
	protected Listener<ObservationSelectionMessage> createObservationSelectionListener() {
		return new Listener<ObservationSelectionMessage>() {

			public void update(ObservationSelectionMessage message) {
				// Move the cross hairs if we have date information since
				// this plot's domain is JD.
				if (message.getSource() != this
						&& message.getObservation().getDateInfo() != null) {
					chart.getXYPlot().setDomainCrosshairValue(
							message.getObservation().getJD());
					chart.getXYPlot().setRangeCrosshairValue(
							message.getObservation().getMag());
				}
			}

			public boolean canBeRemoved() {
				return true;
			}
		};
	}
}
