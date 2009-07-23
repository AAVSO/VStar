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

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.aavso.tools.vstar.ui.model.ObservationAndMeanPlotModel;

/**
 * This component permits the days-in-bin value to be changed
 * which in turn modifies the means series in the observations
 * and means plot.
 */
public class DaysInBinSettingPane extends JPanel {
	
	// TODO: add...
	// - the model needs to have a changeDaysPerBin(int daysPerBin) method
	//   which causes the current means series to be replaced. Get rid of
	//   addInitialMeanSeries() and just use addMeanSeries() also calling this
	//   from the ctor with the default days per bin. Probably should also
	//   store days-per-bin as a field in the model so that we can update
	//   the slider from the model.

	private ObservationAndMeanPlotModel obsAndMeanModel;

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
		
		// TODO: instead of a text field, we may a want a JSlider and JLabel		
		daysInBinField = new JTextField(this.obsAndMeanModel.getDaysInBin()+"");
		daysInBinField.setColumns(5);
		daysInBinField.addActionListener(createDaysInBinFieldListener());
		this.add(daysInBinField);
		
		//this.add(Box.createHorizontalGlue());
		
		JButton updateButton = new JButton("Update");
		updateButton.addActionListener(createUpdateMeansButtonListener());
		this.add(updateButton);
	}
	
	// Return a listener for the "days in bin" field.
	// TODO: what kind of listener do we need for validation?
	private ActionListener createDaysInBinFieldListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// TODO: validate as number?
			}
		};
	}

	// Return a listener for the "update means" button.
	private ActionListener createUpdateMeansButtonListener() {
		final DaysInBinSettingPane self = this;
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				double daysInBin = Double.parseDouble(self.daysInBinField.getText()); 
				self.obsAndMeanModel.changeMeansSeries(daysInBin);
			}
		};
	}

}
