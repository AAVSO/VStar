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

import java.awt.Container;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.aavso.tools.vstar.data.ValidObservation;

/**
 * This modeless dialog class displays info about a single observation.
 * 
 *  TODO:
 *  - We should have a pool of these and clear the text for each use
 *    since they take awhile to render otherwise and we are likely
 *    to create many per session.
 */
public class ObservationInfoDialog extends JDialog {

	private ValidObservation ob;
	private Container contentPane;

	public ObservationInfoDialog(ValidObservation ob) {
		super();
		this.setTitle("Observation Information");
		this.setModal(false);
		this.setSize(200, 200);

		this.ob = ob;

		contentPane = this.getContentPane();
		
		JPanel topPane = new JPanel();
		topPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		JTextArea textArea = new JTextArea(ob.toString());
		textArea.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(textArea);
		topPane.add(scrollPane);
		
		contentPane.add(topPane);

		this.pack();
		this.setLocationRelativeTo(MainFrame.getInstance().getContentPane());
		this.setVisible(true);
	}	
}
