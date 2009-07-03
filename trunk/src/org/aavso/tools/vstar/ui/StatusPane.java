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

import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

import org.aavso.tools.vstar.ui.model.ModelManager;
import org.aavso.tools.vstar.ui.model.NewStarType;
import org.aavso.tools.vstar.util.Listener;

/**
 * A status panel. The intention is that this should be added to
 * the bottom of the GUI.
 * 
 * This class will also listen to various events.
 */
public class StatusPane extends JPanel {
	
	private ModelManager modelMgr = ModelManager.getInstance();
	
	private JLabel statusLabel;
	
	/**
	 * Constructor.
	 * 
	 * @param firstMessage The first message we want to display.
	 */
	public StatusPane(String firstMessage) {
		super(false); // not double buffered
		this.setLayout(new GridLayout(1, 1));
		
		statusLabel = new JLabel();
		statusLabel.setHorizontalAlignment(JLabel.LEFT);
		
		this.add(statusLabel);
		this.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		
		this.setMessage(firstMessage);
		
		modelMgr.getNewStarNotifier().addListener(createNewStarListener());
	}
	
	/**
	 * Set the status message to be displayed.
	 * 
	 * @param msg The message to be displayed.
	 */
	public void setMessage(String msg) {
		this.statusLabel.setText(" " + msg);
	}
	
	/**
	 * Return a new star creation listener.
	 */
	private Listener<NewStarType> createNewStarListener() {
		return new Listener<NewStarType>() {
			public void update(NewStarType info) {
				if (info == NewStarType.NEW_STAR_FROM_SIMPLE_FILE) {
					StringBuffer strBuf = new StringBuffer();
					strBuf.append("'");
					strBuf.append(modelMgr.getNewStarFileName());
					strBuf.append("' loaded.");
					setMessage(strBuf.toString());
				}
			}
		};
	}
}
