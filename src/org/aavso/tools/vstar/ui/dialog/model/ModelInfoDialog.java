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
package org.aavso.tools.vstar.ui.dialog.model;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.aavso.tools.vstar.ui.MainFrame;
import org.aavso.tools.vstar.util.model.IModel;

/**
 * This dialog displays information about a model.
 */
public class ModelInfoDialog extends JDialog {

	private IModel model;

	/**
	 * Constructor
	 * 
	 * @param parent
	 *            The parent dialog.
	 * @param model
	 *            The model about which information is to be displayed.
	 */
	public ModelInfoDialog(JDialog parent, IModel model) {
		super(parent);
		this.model = model;

		this.setTitle("Model Information");
		this.setModal(true);
		this.setSize(200, 200);

		JPanel topPane = new JPanel();
		topPane.setLayout(new BoxLayout(topPane, BoxLayout.PAGE_AXIS));
		topPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		JTextArea textArea = new JTextArea(model.toString());
		textArea.setBorder(BorderFactory.createTitledBorder("Function"));
		textArea.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(textArea);
		topPane.add(scrollPane);

		// TODO: add text areas for:
		// - info about each frequency, its harmonic number and fundamental
		// - relative parameters if model contains harmonics

		topPane.add(Box.createRigidArea(new Dimension(10, 10)));

		JPanel buttonPane = new JPanel();
		JButton okButton = new JButton("OK");
		okButton.addActionListener(createOKButtonHandler());
		buttonPane.add(okButton, BorderLayout.CENTER);
		topPane.add(buttonPane);

		this.getContentPane().add(topPane);

		this.getRootPane().setDefaultButton(okButton);

		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		this.pack();
		this.setLocationRelativeTo(MainFrame.getInstance().getContentPane());
		this.setAlwaysOnTop(true);
		this.setVisible(true);
	}

	private ActionListener createOKButtonHandler() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				dispose();
			}
		};
	}
}
