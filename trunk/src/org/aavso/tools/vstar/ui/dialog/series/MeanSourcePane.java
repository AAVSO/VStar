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
package org.aavso.tools.vstar.ui.dialog.series;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.ui.pane.plot.ObservationAndMeanPlotPane;
import org.aavso.tools.vstar.util.locale.LocaleProps;

/**
 * This class represents a pane with radio buttons showing which series is to be
 * used as the source to create the means series. The source series can be
 * modified.
 */
@SuppressWarnings("serial")
public class MeanSourcePane extends JPanel implements ActionListener {

	private ObservationAndMeanPlotPane obsPlotPane;

	private int seriesNum;
	private int lastSelectedSeriesNum;

	private ButtonGroup seriesGroup;

	private JRadioButton filteredRadioButton;
	private JRadioButton modelRadioButton;
	private JRadioButton residualsRadioButton;

	/**
	 * Constructor.
	 * 
	 * @param plotPane
	 *            An observation and mean plot pane.
	 */
	public MeanSourcePane(ObservationAndMeanPlotPane plotPane) {
		super();

		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		this.setBorder(BorderFactory.createTitledBorder(LocaleProps
				.get("MEAN_SERIES_SOURCE")));
		this
				.setToolTipText("Select series that will be the source of the means series.");

		this.obsPlotPane = plotPane;
		this.seriesNum = obsPlotPane.getObsModel().getMeanSourceSeriesNum();

		addSeriesRadioButtons();
	}

	// Create a radio button for each series, selecting the one
	// that corresponds to the current mean source series.
	private void addSeriesRadioButtons() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));

		panel.add(createDataSeriesRadioButtons());
		panel.add(createOtherSeriesRadioButtons());

		this.add(panel);
	}

	private JPanel createDataSeriesRadioButtons() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.setBorder(BorderFactory.createTitledBorder(LocaleProps
				.get("DATA_TITLE")));

		seriesGroup = new ButtonGroup();

		for (SeriesType series : this.obsPlotPane.getObsModel().getSeriesKeys()) {
			if (!series.isSynthetic()) {
				String seriesName = series.getDescription();
				JRadioButton seriesRadioButton = new JRadioButton(seriesName);
				seriesRadioButton.setActionCommand(seriesName);
				seriesRadioButton.addActionListener(this);
				seriesRadioButton.setEnabled(isSeriesNonEmpty(series));
				panel.add(seriesRadioButton);
				panel.add(Box.createRigidArea(new Dimension(3, 3)));
				seriesGroup.add(seriesRadioButton);
				checkInitialMeanSourceSeries(series, seriesRadioButton);
			}
		}

		// Ensure the panel is wide enough for textual border.
		panel.add(Box.createRigidArea(new Dimension(75, 1)));

		return panel;
	}

	private JPanel createOtherSeriesRadioButtons() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.setBorder(BorderFactory.createTitledBorder(LocaleProps
				.get("ANALYSIS_TITLE")));

		// Filtered series.
		filteredRadioButton = new JRadioButton(SeriesType.Filtered
				.getDescription());
		filteredRadioButton.setActionCommand(SeriesType.Filtered
				.getDescription());
		filteredRadioButton.addActionListener(this);
		filteredRadioButton.setEnabled(isSeriesNonEmpty(SeriesType.Filtered));
		panel.add(filteredRadioButton);
		panel.add(Box.createRigidArea(new Dimension(3, 3)));
		seriesGroup.add(filteredRadioButton);
		checkInitialMeanSourceSeries(SeriesType.Filtered, filteredRadioButton);

		JPanel subPanel = new JPanel();
		subPanel.setLayout(new BoxLayout(subPanel, BoxLayout.PAGE_AXIS));
		subPanel.setBorder(BorderFactory.createTitledBorder(LocaleProps
				.get("MODEL_TITLE")));
		subPanel.add(Box.createRigidArea(new Dimension(75, 1)));

		// Model series.
		modelRadioButton = new JRadioButton(SeriesType.Model.getDescription());
		modelRadioButton.setActionCommand(SeriesType.Model.getDescription());
		modelRadioButton.addActionListener(this);
		modelRadioButton.setEnabled(isSeriesNonEmpty(SeriesType.Model));
		subPanel.add(modelRadioButton);
		subPanel.add(Box.createRigidArea(new Dimension(3, 3)));
		seriesGroup.add(modelRadioButton);
		checkInitialMeanSourceSeries(SeriesType.Model, modelRadioButton);

		// Residuals series.
		residualsRadioButton = new JRadioButton(SeriesType.Residuals
				.getDescription());
		residualsRadioButton.setActionCommand(SeriesType.Residuals
				.getDescription());
		residualsRadioButton.addActionListener(this);
		residualsRadioButton.setEnabled(isSeriesNonEmpty(SeriesType.Residuals));
		subPanel.add(residualsRadioButton);
		subPanel.add(Box.createRigidArea(new Dimension(3, 3)));
		seriesGroup.add(residualsRadioButton);
		checkInitialMeanSourceSeries(SeriesType.Residuals, residualsRadioButton);

		panel.add(subPanel);

		return panel;
	}

	/**
	 * Determine whether the specified series is the current mean source series,
	 * i.e. the series from which the means series is derived. If it is, the
	 * radio button is selected and the series is recorded for this session as
	 * the last mean source series.
	 * 
	 * @param series
	 *            The series type.
	 * @param seriesRadioButton
	 *            The series radio button.
	 */
	private void checkInitialMeanSourceSeries(SeriesType series,
			JRadioButton seriesRadioButton) {
		if (obsPlotPane.getObsModel().getSrcTypeToSeriesNumMap().get(series) == obsPlotPane
				.getObsModel().getMeanSourceSeriesNum()) {
			seriesRadioButton.setSelected(true);
			lastSelectedSeriesNum = obsPlotPane.getObsModel()
					.getMeanSourceSeriesNum();
		}
	}

	/**
	 * Does the specified series have corresponding observations?
	 * 
	 * @param type
	 *            The series type in question.
	 * @return Whether or not there are observations for this series type.
	 */
	private boolean isSeriesNonEmpty(SeriesType type) {
		Integer num = obsPlotPane.getObsModel().getSrcTypeToSeriesNumMap().get(
				type);

		// This series exists and has obs (or not), so allow it to be selected
		// (or not).
		boolean hasObs = obsPlotPane.getObsModel().getSeriesNumToObSrcListMap()
				.containsKey(num)
				&& !obsPlotPane.getObsModel().getSeriesNumToObSrcListMap().get(
						num).isEmpty();

		return hasObs;
	}

	// This method will be called when a radio button is selected.
	// If the selected series is different from the model's current
	// mean source series number (in the model), set the model's mean
	// source series number.

	public void actionPerformed(ActionEvent e) {
		String seriesName = e.getActionCommand();
		this.seriesNum = obsPlotPane.getObsModel().getSrcTypeToSeriesNumMap()
				.get(SeriesType.getSeriesFromDescription(seriesName));

		if (this.seriesNum != obsPlotPane.getObsModel()
				.getMeanSourceSeriesNum()) {
			obsPlotPane.setMeanSourceSeriesNum(this.seriesNum);
			boolean changed = obsPlotPane.changeMeansSeries(obsPlotPane
					.getObsModel().getTimeElementsInBin());
			if (changed) {
				lastSelectedSeriesNum = obsPlotPane.getObsModel()
						.getMeanSourceSeriesNum();
			} else {
				// Means not changed (e.g. too few data points in selected
				// series), so restore radio buttons to last selected.
				SeriesType seriesToRevertTo = obsPlotPane.getObsModel()
						.getSeriesNumToSrcTypeMap().get(lastSelectedSeriesNum);

				Enumeration<AbstractButton> elts = seriesGroup.getElements();

				while (elts.hasMoreElements()) {
					AbstractButton radioButton = elts.nextElement();
					String label = radioButton.getActionCommand();
					if (SeriesType.getSeriesFromDescription(label) == seriesToRevertTo) {
						radioButton.setSelected(true);
						obsPlotPane
								.setMeanSourceSeriesNum(lastSelectedSeriesNum);
					}
				}
			}
		}
	}

	/**
	 * @return the seriesNum
	 */
	public int getSeriesNum() {
		return seriesNum;
	}
}
