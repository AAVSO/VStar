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
package org.aavso.tools.vstar.plugin.period;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.aavso.tools.vstar.ui.MainFrame;

/**
 * This can be used as the base class for period analysis dialogs.
 */
abstract public class PeriodAnalysisDialogBase extends JDialog {

	private JButton newPhasePlotButton;
	private JPanel topPane;
	
	/**
	 * Constructor.
	 * 
	 * @param title The dialog title.
	 * @param isModal Should the dialog be modal.
	 */
	public PeriodAnalysisDialogBase(String title, boolean isModal) {
		super();
		this.setTitle(title);
		this.setModal(isModal);		
	}
	
	/**
	 * Constructor for a non-modal dialog.
	 * 
	 * @param title The dialog title.
	 */
	public PeriodAnalysisDialogBase(String title) {
		this(title, false);
	}
	
	/**
	 * A subclass must invoke this when it wants to add the dialog's
	 * content and prepare it for visibility. It will in turn call
	 * createContent() and createButtonPanel().
	 */
	protected void prepareDialog() {
		topPane = new JPanel();
		topPane.setLayout(new BoxLayout(topPane, BoxLayout.PAGE_AXIS));
		topPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));	

		topPane.add(createContent());
		topPane.add(createButtonPanel());

		this.getContentPane().add(topPane);
		this.pack();
		this.setLocationRelativeTo(MainFrame.getInstance().getContentPane());		
	}
	
	// Methods that must be overridden by subclasses.
	
	/**
	 * This method is invoked when the phase plot button is clicked.
	 */
	abstract protected void newPhasePlotButtonAction();

	/**
	 * Create the content to be added to the dialog's content pane.
	 */
	abstract protected Component createContent();
		
	// Protected methods for use by subclasses.
	
	protected JPanel createButtonPanel() {
		JPanel buttonPane = new JPanel();

		newPhasePlotButton = new JButton("New Phase Plot");
		newPhasePlotButton.addActionListener(createNewPhasePlotButtonHandler());
		newPhasePlotButton.setEnabled(false);
		buttonPane.add(newPhasePlotButton, BorderLayout.LINE_START);

		JButton dismissButton = new JButton("Dismiss");
		dismissButton.addActionListener(createDismissButtonHandler());
		buttonPane.add(dismissButton, BorderLayout.LINE_END);

		return buttonPane;
	}

	/**
	 * Sets the enabled state of the new-phase-plot button.
	 * @param state The desired boolean state.
	 */
	protected void setNewPhasePlotButtonState(boolean state) {
		this.newPhasePlotButton.setEnabled(state);
	}
		
	// Dismiss button listener.
	private ActionListener createDismissButtonHandler() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				dispose();
			}
		};
	}

	// Send a period change message when the new-phase-plot button is clicked.
	private ActionListener createNewPhasePlotButtonHandler() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				newPhasePlotButtonAction();
			}
		};
	}
}
