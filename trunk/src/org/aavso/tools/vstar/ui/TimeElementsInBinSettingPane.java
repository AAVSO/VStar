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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.ui.model.ITimeElementEntity;
import org.aavso.tools.vstar.ui.model.ObservationAndMeanPlotModel;

/**
 * This component permits the time-elements-in-bin value to be changed which in
 * turn can be used to modify the means series in an observations and means
 * plot.
 */
public class TimeElementsInBinSettingPane extends JPanel {

	private ObservationAndMeanPlotModel obsAndMeanModel;

	private JSpinner timeElementsInBinSpinner;
	private SpinnerNumberModel timeElementsInBinSpinnerModel;

	/**
	 * Constructor.
	 * 
	 * @param spinnerTitle
	 *            A title for the spinner.
	 * @param obsAndMeanModel
	 *            An observation and mean model.
	 * @param timeElementEntity
	 *            A time element source for observations.
	 */
	public TimeElementsInBinSettingPane(String spinnerTitle,
			ObservationAndMeanPlotModel obsAndMeanModel,
			ITimeElementEntity timeElementEntity) {

		super();

		this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		this.obsAndMeanModel = obsAndMeanModel;

		this.setBorder(BorderFactory.createTitledBorder(spinnerTitle));

		// Spinner for time-elements-in-bin.

		this.add(Box.createHorizontalGlue());

		// Given the source-series of the means series, determine the
		// maximum day range for the time-elements-in-bin spinner.
		List<ValidObservation> meanSrcObsList = obsAndMeanModel
				.getSeriesNumToObSrcListMap().get(
						obsAndMeanModel.getMeanSourceSeriesNum());

		double max = timeElementEntity.getTimeElement(meanSrcObsList,
				meanSrcObsList.size() - 1)
				- timeElementEntity.getTimeElement(meanSrcObsList, 0);

		// Spinner for time-elements-in-bin with the specified current, min, and max
		// values, and step size (1 day). If the "current time elements in bin" value
		// is larger than the calculated max value, correct that.
		double currTimeElementsInBin = obsAndMeanModel.getTimeElementsInBin();
		currTimeElementsInBin = currTimeElementsInBin <= max ? currTimeElementsInBin : max;
		obsAndMeanModel.setTimeElementsInBin(currTimeElementsInBin);

		timeElementsInBinSpinnerModel = new SpinnerNumberModel(currTimeElementsInBin, 0,
				max, timeElementEntity.getDefaultTimeIncrements());
		timeElementsInBinSpinner = new JSpinner(timeElementsInBinSpinnerModel);
		this.add(timeElementsInBinSpinner);

		this.add(Box.createHorizontalGlue());

		// Update button for time-elements-in-bin.
		JButton updateButton = new JButton("Update");
		updateButton.addActionListener(createUpdateMeansButtonListener());
		this.add(updateButton);

		this.add(Box.createHorizontalGlue());
	}

	// Return a listener for the "update means" button.
	private ActionListener createUpdateMeansButtonListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Get the value and change the means series.
				double timeElementsInBin = timeElementsInBinSpinnerModel.getNumber()
						.doubleValue();
				obsAndMeanModel.changeMeansSeries(timeElementsInBin);
			}
		};
	}
}
