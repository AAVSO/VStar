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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

/**
 * Create the mode radio button group pane.
 */
public class ModePane extends JPanel implements ActionListener {

	/**
	 * Constructor.
	 */
	public ModePane() {
		super(new GridLayout(0,1));
		
		createModeButtonPanel();
		
		this.setBorder(BorderFactory.createTitledBorder("Mode"));
		this.setPreferredSize(new Dimension(250,200));		
	}
	
	// Create a radio button panel with N rows and 1 column, a
	// radio button group, and each radio button and its action
	// listener.
	private void createModeButtonPanel() {
		ButtonGroup modeGroup = new ButtonGroup();
		
		JRadioButton plotObsRadioButton = new JRadioButton(MainFrame.PLOT_OBS);
		plotObsRadioButton.setActionCommand(MainFrame.PLOT_OBS);
		plotObsRadioButton.addActionListener(this);
		this.add(plotObsRadioButton);
		modeGroup.add(plotObsRadioButton);
		
		JRadioButton plotObsAndMeansRadioButton = new JRadioButton(MainFrame.PLOT_OBS_AND_MEANS);
		plotObsAndMeansRadioButton.setActionCommand(MainFrame.PLOT_OBS_AND_MEANS);
		plotObsAndMeansRadioButton.addActionListener(this);
		this.add(plotObsAndMeansRadioButton);
		modeGroup.add(plotObsAndMeansRadioButton);
		
		JRadioButton listObsRadioButton = new JRadioButton(MainFrame.LIST_OBS);
		listObsRadioButton.setActionCommand(MainFrame.LIST_OBS);
		listObsRadioButton.addActionListener(this);
		this.add(listObsRadioButton);		
		modeGroup.add(listObsRadioButton);
		
		JRadioButton listMeansRadioButton = new JRadioButton(MainFrame.LIST_MEANS);
		listMeansRadioButton.setActionCommand(MainFrame.LIST_MEANS);
		listMeansRadioButton.addActionListener(this);
		this.add(listMeansRadioButton);
		modeGroup.add(listMeansRadioButton);
		
		plotObsRadioButton.setSelected(true);
	}

	// This method will be called when a radio button is selected.
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		// TODO: Combine command with Analysis menu selection to
		// set the table/plot to view from DocumentManager. For now
		// just assume Analysis->Raw Data.
		// May want DocumentManager to notify the DataPane which in turn 
		// calls showCard().
	}
}
