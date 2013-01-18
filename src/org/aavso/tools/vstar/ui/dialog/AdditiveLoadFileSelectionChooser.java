/**
 * VStar: a statistical analysis tool for variable star data.
 * Copyright (C) 2013  AAVSO (http://www.aavso.org/)
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
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;

/**
 * This class aggregates a JFileChooser and an additive load checkbox component.
 */
public class AdditiveLoadFileSelectionChooser {

	private JFileChooser fileChooser;
	private JCheckBox additiveLoadCheckbox;

	/**
	 * Constructor
	 */
	public AdditiveLoadFileSelectionChooser() {
		fileChooser = new JFileChooser();
		fileChooser.setAccessory(createAdditiveLoadCheckboxPane());
	}

	/**
	 * This component provides an additive load checkbox.
	 */
	private JPanel createAdditiveLoadCheckboxPane() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.setBorder(BorderFactory.createTitledBorder("Additive Load"));

		additiveLoadCheckbox = new JCheckBox("Add to current?");
		panel.add(additiveLoadCheckbox);

		return panel;
	}

	/**
	 * Set the chooser's file filter.
	 * 
	 * @param filter
	 *            The file filter.
	 */
	public void setFileFilter(FileFilter filter) {
		fileChooser.setFileFilter(filter);
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
		return fileChooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION;
	}

	/**
	 * @return The selected file.
	 */
	public File getSelectedFile() {
		return fileChooser.getSelectedFile();
	}

	/**
	 * Return whether or not the load is additive.
	 * 
	 * @return Whether or not the load is additive.
	 */
	public boolean isLoadAdditive() {
		return additiveLoadCheckbox.isSelected();
	}
}
