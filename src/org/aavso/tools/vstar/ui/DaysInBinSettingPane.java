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
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.ui.model.ObservationAndMeanPlotModel;

/**
 * This component permits the days-in-bin value to be changed which in turn
 * modifies the means series in the observations and means plot.
 */
public class DaysInBinSettingPane extends JPanel {

	private ObservationAndMeanPlotModel obsAndMeanModel;

	private JSlider daysInBinSlider;
	private JTextField daysInBinField;

	/**
	 * Constructor
	 * 
	 * @param obsAndMeanModel
	 */
	public DaysInBinSettingPane(ObservationAndMeanPlotModel obsAndMeanModel) {
		super();

		this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		this.obsAndMeanModel = obsAndMeanModel;

		this.setBorder(BorderFactory.createTitledBorder("Days in Means Bin"));

		this.add(Box.createHorizontalGlue());

		// Slider for days-in-bin.
		// Note that a slider can only handle integer values.
		List<ValidObservation> meanAndObsList = obsAndMeanModel
				.getMeanObsList();

		int max = (int) (meanAndObsList.get(meanAndObsList.size() - 1).getJD() - meanAndObsList
				.get(0).getJD());

		daysInBinSlider = new JSlider(JSlider.HORIZONTAL, 0, max,
				(int) obsAndMeanModel.getDaysInBin());
		daysInBinSlider.setValue((int) obsAndMeanModel.getDaysInBin());
		daysInBinSlider
				.addChangeListener(createDaysInBinSliderChangeListener());
		this.add(daysInBinSlider);

		// Text field for days-in-bin.
		// If we need to enter fractional days, we will be able to so here.
		// Would fractional days really be used in practice though?
		daysInBinField = new JTextField(this.obsAndMeanModel.getDaysInBin()
				+ "");
		daysInBinField.setColumns(5);
		this.add(daysInBinField);

		// Update button for days-in-bin.
		JButton updateButton = new JButton("Update");
		updateButton.addActionListener(createUpdateMeansButtonListener());
		this.add(updateButton);
	}

	// Return a listener for the days-in-bin slider.
	private ChangeListener createDaysInBinSliderChangeListener() {		
		return new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				
				// Update the days-in-bin text box as the slider is changing.
				daysInBinField.setText(source.getValue() + "");

				if (!source.getValueIsAdjusting()) {
					// Okay, the slider has stopped changing, so let's
					// tell the model. We could check whether the current
					// value is the same as the new value, but given that
					// we would need to round the value on the model, this
					// would just be misleading and may prevent a mean series
					// update when one is required (e.g. current value is 10.5,
					// new value from slider is 11, and we round up the former
					// to 11, whereupon an equality check inappropriately 
					// succeeds).
					// TODO: actually, we may not want to tell the model
					// until the update button is clicked since that will
					// also cause a means series update.
					int daysInBin = (int) source.getValue();
					//obsAndMeanModel.changeMeansSeries(daysInBin);
				}
			}
		};
	}

	// Return a listener for the "update means" button.
	private ActionListener createUpdateMeansButtonListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String text = daysInBinField.getText();
				try {
					double daysInBin = Double.parseDouble(text);
					obsAndMeanModel.changeMeansSeries(daysInBin);
					daysInBinSlider.setValue((int) obsAndMeanModel.getDaysInBin());
				} catch (NumberFormatException ex) {
					MessageBox.showErrorDialog(MainFrame.getInstance(),
							"Days in Bin", "Number format error for '" + text
									+ "'");

					daysInBinField.setText("");
				}
			}
		};
	}
}
