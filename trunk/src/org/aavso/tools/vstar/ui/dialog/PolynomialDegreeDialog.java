/**
 * VStar: a statistical analysis tool for variable star data.
 * Copyright (C) 2010  AAVSO (http://www.aavso.org/)
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
package org.aavso.tools.vstar.ui.dialog;

import java.awt.Component;
import java.awt.Container;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.aavso.tools.vstar.ui.MainFrame;

/**
 * This dialog allows the user to select the polynomial degree to be used for a
 * polynomial fit operation.
 */
public class PolynomialDegreeDialog extends AbstractOkCancelDialog implements
		ChangeListener {

	private int degree;
	private JLabel degreeLabel;
	
	/**
	 * Constructor
	 * 
	 * @param minDegree
	 *            The minimum degree value.
	 * @param maxDegree
	 *            The maximum degree value.
	 */
	public PolynomialDegreeDialog(int minDegree, int maxDegree) {
		super("Polynomial Degree");

		degree = (maxDegree-minDegree)/2;
		
		Container contentPane = this.getContentPane();

		JPanel topPane = new JPanel();
		topPane.setLayout(new BoxLayout(topPane, BoxLayout.PAGE_AXIS));
		topPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		topPane.add(createPolynomialDegreePane(minDegree, maxDegree));

		// OK, Cancel
		topPane.add(createButtonPane());

		contentPane.add(topPane);

		this.pack();
		setLocationRelativeTo(MainFrame.getInstance().getContentPane());
		this.setVisible(true);
	}

	/**
	 * Returns the currently selected degree value.
	 * 
	 * @return the degree
	 */
	public int getDegree() {
		return degree;
	}

	private Component createPolynomialDegreePane(int minDegree, int maxDegree) {
		JPanel panel = new JPanel();		
		panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));		
		panel.setBorder(BorderFactory.createTitledBorder("Select Polynomial Degree"));
		
		JSlider degreeSlider = new JSlider(JSlider.HORIZONTAL, minDegree,
				maxDegree, degree);

		degreeSlider.setMajorTickSpacing((maxDegree - minDegree) / 10);
		degreeSlider.setMinorTickSpacing(1);
		degreeSlider.setPaintTicks(true);
		degreeSlider.setPaintLabels(true);

		degreeSlider.addChangeListener(this);

		panel.add(degreeSlider);
		
		degreeLabel = new JLabel(degree+"");
		degreeLabel.setBorder(BorderFactory.createEtchedBorder());
		panel.add(degreeLabel);
		
		return panel;
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		JSlider slider = (JSlider) e.getSource();
		if (!slider.getValueIsAdjusting()) {
			degree = slider.getValue();
			degreeLabel.setText(degree+"");
		} else {
			degreeLabel.setText(slider.getValue()+"");			
		}
	}

	/**
	 * @see org.aavso.tools.vstar.ui.dialog.AbstractOkCancelDialog#cancelAction()
	 */
	@Override
	protected void cancelAction() {
		// Nothing to do.
	}

	/**
	 * @see org.aavso.tools.vstar.ui.dialog.AbstractOkCancelDialog#okAction()
	 */
	@Override
	protected void okAction() {
		// TODO: this should be the base class implementation of okAction().
		cancelled = false;
		setVisible(false);
		dispose();
	}
}
