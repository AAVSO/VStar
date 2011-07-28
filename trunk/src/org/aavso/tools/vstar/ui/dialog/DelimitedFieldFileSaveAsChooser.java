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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

/**
 * This class aggregates a JFileChooser and a field delimiter selection
 * component.
 */
public class DelimitedFieldFileSaveAsChooser {

	private String delimiter;
	private JFileChooser fileChooser;

	/**
	 * Constructor
	 */
	public DelimitedFieldFileSaveAsChooser() {
		delimiter = "\t";
		fileChooser = new JFileChooser();
		fileChooser.setAccessory(createDelimiterSelectionPane());
	}

	/**
	 * This component permits selection between field delimiters.
	 */
	private JPanel createDelimiterSelectionPane() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.setBorder(BorderFactory.createTitledBorder("Delimiter"));

		ButtonGroup group = new ButtonGroup();

		JRadioButton tabButton = new JRadioButton("Tab");
		tabButton.setActionCommand("\t");
		tabButton.addActionListener(createDelimiterRadioButtonListener());
		tabButton.setSelected(true);
		panel.add(tabButton);
		group.add(tabButton);

		JRadioButton commaButton = new JRadioButton("Comma");
		commaButton.setActionCommand(",");
		commaButton.addActionListener(createDelimiterRadioButtonListener());
		panel.add(commaButton);
		group.add(commaButton);

		JRadioButton spaceButton = new JRadioButton("Space");
		spaceButton.setActionCommand(" ");
		spaceButton.addActionListener(createDelimiterRadioButtonListener());
		panel.add(spaceButton);
		group.add(spaceButton);

		return panel;
	}

	private ActionListener createDelimiterRadioButtonListener() {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				delimiter = e.getActionCommand();
			}
		};
	}

	/**
	 * Show the file dialog.
	 * 
	 * @param parent
	 *            The parent component to which this dialog should be positioned
	 *            relative.
	 * @return Whether the dialog was "approved".
	 */
	public boolean showDialog(Component parent) {
		return fileChooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION;
	}

	/**
	 * @return The selected file.
	 */
	public File getSelectedFile() {
		return fileChooser.getSelectedFile();
	}

	/**
	 * @return the delimiter
	 */
	public String getDelimiter() {
		return delimiter;
	}
}
