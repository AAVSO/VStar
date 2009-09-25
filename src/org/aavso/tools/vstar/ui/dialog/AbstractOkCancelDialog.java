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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

/**
 * This abstract class should be subclassed  by any class that wants
 * to have modal OK-Cancel dialog behaviour. The default result is
 * "cancelled".
 */
abstract public class AbstractOkCancelDialog extends JDialog {

	protected boolean cancelled;
	
	public AbstractOkCancelDialog(String title) {
		super();
		this.setTitle(title);
		this.setModal(true);
		this.cancelled = true;
		//this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
	}
	
	protected JPanel createButtonPane() {
		JPanel panel = new JPanel(new BorderLayout());

		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(createCancelButtonListener());
		panel.add(cancelButton, BorderLayout.LINE_START);

		JButton okButton = new JButton("OK");
		okButton.addActionListener(createOKButtonListener());
		// okButton.setEnabled(false);
		panel.add(okButton, BorderLayout.LINE_END);

		return panel;
	}

	// Return a listener for the "OK" button.
	private ActionListener createOKButtonListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				okAction();
			}
		};
	}

	// Return a listener for the "cancel" button.
	private ActionListener createCancelButtonListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cancelAction();
				setVisible(false);
				dispose();				
			}
		};
	}
	
	/**
	 * Set the cancelled status of this dialog.
	 * 
	 * @param status The status.
	 */
	protected synchronized void setCancelled(boolean status) {
		cancelled = status;
	}
	
	/**
	 * @return whether this dialog box cancelled
	 */
	public synchronized boolean isCancelled() {
		return cancelled;
	}
	
	/**
	 * Implemented this method to execute an OK button action.
	 */
	abstract protected void okAction();

	/**
	 * Implemented this method to execute cancel button action.
	 */
	abstract protected void cancelAction();
}
