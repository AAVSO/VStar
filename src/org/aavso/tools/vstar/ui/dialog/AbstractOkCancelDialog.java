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

import org.aavso.tools.vstar.ui.MainFrame;

/**
 * This abstract class should be subclassed by any class that wants to have
 * modal OK-Cancel dialog behaviour. The default result is "cancelled".
 */
abstract public class AbstractOkCancelDialog extends JDialog {

	// Was this dialog cancelled?
	protected boolean cancelled;

	// Intended for Singleton subclasses.
	protected boolean firstUse;

	protected JButton okButton;

	public AbstractOkCancelDialog(String title, boolean isModal,
			boolean isAlwaysOnType) {
		super();
		this.setTitle(title);
		this.setModal(isModal);
		this.setAlwaysOnTop(isAlwaysOnType);
		this.cancelled = true;
		this.firstUse = true;
	}

	public AbstractOkCancelDialog(String title) {
		this(title, true, true);
	}

	protected JPanel createButtonPane() {
		JPanel panel = new JPanel(new BorderLayout());

		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(createCancelButtonListener());
		panel.add(cancelButton, BorderLayout.LINE_START);

		okButton = new JButton("OK");
		okButton.addActionListener(createOKButtonListener());
		panel.add(okButton, BorderLayout.LINE_END);

		this.getRootPane().setDefaultButton(okButton);

		return panel;
	}

	// Return a listener for the "OK" button.
	protected ActionListener createOKButtonListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				okAction();
			}
		};
	}

	// Return a listener for the "cancel" button.
	protected ActionListener createCancelButtonListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cancelAction();
				setVisible(false);
				dispose();
			}
		};
	}

	/**
	 * Show the dialog. Intended for Singleton subclasses.
	 */
	public void showDialog() {
		if (firstUse) {
			setLocationRelativeTo(MainFrame.getInstance().getContentPane());
			firstUse = false;
		}

		localReset();
		this.getRootPane().setDefaultButton(okButton);
		this.setVisible(true);
	}

	/**
	 * Reset this dialog's state so that we don't process old state. Intended
	 * for Singleton subclasses.
	 */
	private void localReset() {
		this.setCancelled(true);
		reset();
	}

	/**
	 * Subclasses should override this. Intended for Singleton subclasses, hence
	 * a default do-nothing implementation is provided.
	 */
	protected void reset() {
	}

	/**
	 * Set the cancelled status of this dialog.
	 * 
	 * @param status
	 *            The status.
	 */
	protected void setCancelled(boolean status) {
		cancelled = status;
	}

	/**
	 * @return whether this dialog box cancelled
	 */
	public boolean isCancelled() {
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
