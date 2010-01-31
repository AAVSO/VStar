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
package org.aavso.tools.vstar.ui.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.ui.MainFrame;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.ObservationChange;
import org.aavso.tools.vstar.ui.mediator.ObservationChangeType;

/**
 * This modeless dialog class displays info about a single observation.
 * 
 * It also allows us to change the discrepant status of the observation.
 * 
 * TODO: We should have a pool of these and clear the text for each use since
 * they take awhile to render otherwise and we are likely to create many per
 * session.
 */
public class ObservationDetailsDialog extends JDialog implements FocusListener {

	private JButton okButton;
	private ValidObservation ob;
	
	public ObservationDetailsDialog(ValidObservation ob) {
		super();
		
		this.ob = ob;
		
		this.setTitle("Observation Details");
		this.setModal(false);
		this.setSize(200, 200);

		JPanel topPane = new JPanel();
		topPane.setLayout(new BoxLayout(topPane, BoxLayout.PAGE_AXIS));
		topPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		JTextArea textArea = new JTextArea(ob.toString());
		textArea.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(textArea);
		topPane.add(scrollPane);

		topPane.add(Box.createRigidArea(new Dimension(10, 10)));

		JPanel checkBoxPane = new JPanel();		
		JCheckBox discrepantCheckBox = new JCheckBox("Discrepant?");
		discrepantCheckBox.addActionListener(createDiscreantCheckBoxHandler());
		discrepantCheckBox.setSelected(ob.isDiscrepant());
		checkBoxPane.add(discrepantCheckBox);
		topPane.add(checkBoxPane, BorderLayout.CENTER);
		
		JPanel buttonPane = new JPanel();
		okButton = new JButton("OK");
		okButton.addActionListener(createOKButtonHandler());
		buttonPane.add(okButton, BorderLayout.CENTER);
		topPane.add(buttonPane);

		this.getContentPane().add(topPane);

		this.getRootPane().setDefaultButton(okButton);		

		this.pack();
		this.setLocationRelativeTo(MainFrame.getInstance().getContentPane());
		this.setAlwaysOnTop(true);
		this.setVisible(true);
	}
	
	private void toggleDiscrepantStatus() {
		ob.setDiscrepant(!ob.isDiscrepant());
	}
	
	/**
	 * OK button handler.
	 */
	private ActionListener createOKButtonHandler() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				dispose();
			}
		};
	}

	/**
	 * Discrepant checkbox handler.
	 */
	private ActionListener createDiscreantCheckBoxHandler() {
		final ObservationDetailsDialog parent = this;
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Toggle the observation's discrepant status and 
				// tell anyone who's listening about the change.
				toggleDiscrepantStatus();
				ObservationChange message = new ObservationChange(ob,
						ObservationChangeType.DISCREPANT, parent);
				Mediator.getInstance().getObservationChangeNotifier()
						.notifyListeners(message);
			}
		};
	}

	// TODO: this method is not being invoked when the window regains focus; fix!
	public void focusGained(FocusEvent e) {
		this.getRootPane().setDefaultButton(okButton);		
	}

	public void focusLost(FocusEvent e) {
		// Nothing to do.		
	}
}
