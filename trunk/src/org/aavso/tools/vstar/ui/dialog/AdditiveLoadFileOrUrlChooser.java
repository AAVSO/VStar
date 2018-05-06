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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;

/**
 * This class aggregates a JFileChooser and additive load checkbox and URL entry
 * components.
 */
public class AdditiveLoadFileOrUrlChooser {

	private JFileChooser fileChooser;
	private JCheckBox additiveLoadCheckbox;
	private boolean urlProvided;
	private TextField urlField;
	private List<String> extensions = new ArrayList<String>();

	/**
	 * Constructor
	 * 
	 * @param allowURL
	 *            Should a URL entry be allowed?
	 */
	public AdditiveLoadFileOrUrlChooser(boolean allowURL) {
		fileChooser = new JFileChooser();
		urlProvided = false;

		// Default file extensions.
		extensions.add("csv");
		extensions.add("dat");
		extensions.add("tsv");
		extensions.add("txt");
		setFileExtensions(extensions);

		JPanel accessoryPane = new JPanel();
		accessoryPane.setLayout(new BoxLayout(accessoryPane,
				BoxLayout.PAGE_AXIS));
		accessoryPane.add(createAdditiveLoadCheckboxPane());
		if (allowURL) {
			accessoryPane.add(createUrlPane());
		}
		fileChooser.setAccessory(accessoryPane);
	}

	/**
	 * Returns the default file extensions.
	 * 
	 * @return the list of file extension strings.
	 */
	public List<String> getDefaultFileExtensions() {
		return extensions;
	}

	/**
	 * Set file chooser extensions filter.
	 * 
	 * @param extensions
	 */
	public synchronized void setFileExtensions(List<String> extensions) {
		fileChooser.setFileFilter(new FileExtensionFilter(extensions));
	}

	/**
	 * This component provides an additive load checkbox.
	 */
	private JPanel createAdditiveLoadCheckboxPane() {
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createTitledBorder("Additive Load"));

		additiveLoadCheckbox = new JCheckBox("Add to current?");
		panel.add(additiveLoadCheckbox);

		return panel;
	}

	/**
	 * This component provides a URL request button and corresponding action.
	 */
	private JPanel createUrlPane() {
		JPanel pane = new JPanel();

		JButton urlRequestButton = new JButton("Request URL");

		urlRequestButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				urlField = new TextField("URL");
				TextDialog urlDialog = new TextDialog("Enter URL", urlField);
				if (!urlDialog.isCancelled()
						&& !urlField.getValue().matches("^\\s*$")) {
					urlProvided = true;
					fileChooser.cancelSelection();
				}
			}
		});

		pane.add(urlRequestButton);

		return pane;
	}

	/**
	 * Show the file dialog.
	 * 
	 * @param parent
	 *            The parent component to which this dialog should be positioned
	 *            relative.
	 * @return Whether the dialog was "approved".
	 */
	public synchronized boolean showDialog(Component parent) {
		int result = fileChooser.showOpenDialog(parent);
		return result == JFileChooser.APPROVE_OPTION;
	}

	/**
	 * @return The selected file.
	 */
	public File getSelectedFile() {
		return fileChooser.getSelectedFile();
	}

	/**
	 * Was a URL string of some kind provided and accepted?
	 * 
	 * @return True or false.
	 */
	public boolean isUrlProvided() {
		return urlProvided;
	}

	/**
	 * @param urlProvided
	 *            the urlProvided to set
	 */
	public synchronized void setUrlProvided(boolean urlProvided) {
		this.urlProvided = urlProvided;
	}

	/**
	 * @return The URL string.
	 */
	public String getUrlString() {
		return urlField.getValue().trim();
	}

	/**
	 * Return whether or not the load is additive.
	 * 
	 * @return Whether or not the load is additive.
	 */
	public boolean isLoadAdditive() {
		return additiveLoadCheckbox.isSelected();
	}

	/**
	 * Reset this file selector's state before use.
	 */
	public synchronized void reset() {
		setUrlProvided(false);
	}
}
