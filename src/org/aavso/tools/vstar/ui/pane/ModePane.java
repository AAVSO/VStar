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

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.aavso.tools.vstar.ui.mediator.AnalysisTypeChangeMessage;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.ModeType;
import org.aavso.tools.vstar.util.notification.Listener;

/**
 * Create the mode radio button group pane.
 */
public class ModePane extends JPanel implements ActionListener {

	private Mediator mediator = Mediator.getInstance();
	
	private JRadioButton plotObsRadioButton;
	private JRadioButton plotObsAndMeansRadioButton;
	private JRadioButton listObsRadioButton;
	private JRadioButton listMeansRadioButton;
	
	/**
	 * Constructor.
	 */
	public ModePane() {
		super(new GridLayout(0,1));
		
		createModeButtonPanel();
		
		this.setBorder(BorderFactory.createTitledBorder("Mode"));
		this.setPreferredSize(new Dimension(250,200));		
		
		mediator.getAnalysisTypeChangeNotifier().addListener(createAnalysisTypeListener());
	}
	
	/**
	 * Create an analysis type change listener.
	 */
	private Listener<AnalysisTypeChangeMessage> createAnalysisTypeListener() {
		return new Listener<AnalysisTypeChangeMessage>() {
			public void update(AnalysisTypeChangeMessage msg) {
				// Make sure the radio buttons are consistent with the mode.
				// Maybe this is wrong and what we need is one or more stacks
				// of modes.
				switch(msg.getMode()) {
				case PLOT_OBS_MODE:
					plotObsRadioButton.setSelected(true);
					plotObsAndMeansRadioButton.setSelected(false);
					listObsRadioButton.setSelected(false);
					listMeansRadioButton.setSelected(false);
					break;
				case PLOT_OBS_AND_MEANS_MODE:
					plotObsRadioButton.setSelected(false);
					plotObsAndMeansRadioButton.setSelected(true);
					listObsRadioButton.setSelected(false);
					listMeansRadioButton.setSelected(false);
					break;
				case LIST_OBS_MODE:
					plotObsRadioButton.setSelected(false);
					plotObsAndMeansRadioButton.setSelected(false);
					listObsRadioButton.setSelected(true);
					listMeansRadioButton.setSelected(false);
					break;
				case LIST_MEANS_MODE:
					plotObsRadioButton.setSelected(false);
					plotObsAndMeansRadioButton.setSelected(false);
					listObsRadioButton.setSelected(false);
					listMeansRadioButton.setSelected(true);
					break;
				}
			}
			
			public boolean canBeRemoved() {
				return false;
			}
		};
	}
	
	// Create a radio button panel with N rows and 1 column, a
	// radio button group, and each radio button and its action
	// listener.
	private void createModeButtonPanel() {
		ButtonGroup modeGroup = new ButtonGroup();
		
		plotObsRadioButton = new JRadioButton(ModeType.PLOT_OBS_MODE_DESC);
		plotObsRadioButton.setActionCommand(ModeType.PLOT_OBS_MODE_DESC);
		plotObsRadioButton.addActionListener(this);
		this.add(plotObsRadioButton);
		modeGroup.add(plotObsRadioButton);
		
		plotObsAndMeansRadioButton = new JRadioButton(
				ModeType.PLOT_OBS_AND_MEANS_MODE_DESC);
		plotObsAndMeansRadioButton.setActionCommand(ModeType.PLOT_OBS_AND_MEANS_MODE_DESC);
		plotObsAndMeansRadioButton.addActionListener(this);
		this.add(plotObsAndMeansRadioButton);
		modeGroup.add(plotObsAndMeansRadioButton);
		
		listObsRadioButton = new JRadioButton(ModeType.LIST_OBS_MODE_DESC);
		listObsRadioButton.setActionCommand(ModeType.LIST_OBS_MODE_DESC);
		listObsRadioButton.addActionListener(this);
		this.add(listObsRadioButton);		
		modeGroup.add(listObsRadioButton);
		
		listMeansRadioButton = new JRadioButton(ModeType.LIST_MEANS_MODE_DESC);
		listMeansRadioButton.setActionCommand(ModeType.LIST_MEANS_MODE_DESC);
		listMeansRadioButton.addActionListener(this);
		this.add(listMeansRadioButton);
		modeGroup.add(listMeansRadioButton);
		
		// Select the light curve pane by default.
		plotObsRadioButton.setSelected(true);
	}

	// This method will be called when a radio button is selected.
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		ModeType mode = ModeType.getModeFromDesc(command);
		this.mediator.changeMode(mode);
	}
}
