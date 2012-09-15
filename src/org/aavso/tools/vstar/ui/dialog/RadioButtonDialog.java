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

import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.aavso.tools.vstar.ui.MainFrame;

/**
 * This dialog class permits selection from multiple named options represented
 * by radio buttons.
 */
@SuppressWarnings("serial")
public class RadioButtonDialog extends AbstractOkCancelDialog implements ActionListener {

	private Collection<String> options;
	private String initialOption;
	private String selectedOption;

	/**
	 * Constructor
	 * 
	 * @param title
	 *            Title for the dialog.
	 * @param options
	 *            The collection of option strings.
	 * @param initialOption
	 *            The initially selected option string.
	 */
	public RadioButtonDialog(String title, Collection<String> options,
			String initialOption) {
		super(title);

		this.options = options;
		this.initialOption = initialOption;
		selectedOption = initialOption;
		
		Container contentPane = this.getContentPane();

		JPanel topPane = new JPanel();
		topPane.setLayout(new BoxLayout(topPane, BoxLayout.PAGE_AXIS));
		topPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		topPane.add(createRadioButtonPane());

		// OK, Cancel
		topPane.add(createButtonPane());

		contentPane.add(topPane);

		this.pack();
		setLocationRelativeTo(MainFrame.getInstance().getContentPane());
		this.setVisible(true);
	}

	/**
	 * @return the selectedOption
	 */
	public String getSelectedOption() {
		return selectedOption;
	}

	// Add the radio buttons.
	private JPanel createRadioButtonPane() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

		ButtonGroup radioButtonGroup = new ButtonGroup();

		for (String option : options) {
			JRadioButton radioButton = new JRadioButton(option);
			radioButton.setActionCommand(option);
			radioButton.addActionListener(this);
			radioButton.setSelected(option.equals(initialOption));
			radioButtonGroup.add(radioButton);
			panel.add(radioButton);
			panel.add(Box.createRigidArea(new Dimension(3, 3)));
		}

		return panel;
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		selectedOption = event.getActionCommand();
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
		cancelled = false;
		setVisible(false);
		dispose();
	}
}
