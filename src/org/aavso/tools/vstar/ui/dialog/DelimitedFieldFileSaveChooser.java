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
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;

import org.aavso.tools.vstar.plugin.ObservationSinkPluginBase;
import org.aavso.tools.vstar.ui.resources.PluginLoader;
import org.aavso.tools.vstar.util.locale.LocaleProps;

/**
 * This class aggregates a JFileChooser, a plugin selector, and a field
 * delimiter selection component.
 */
public class DelimitedFieldFileSaveChooser {

	private JFileChooser fileChooser;

	private DefaultComboBoxModel<String> delimitersModel;
	private JComboBox<String> delimiterChooser;
	private Map<String, String> delimiters;
	private String delimiter;

	private Map<String, ObservationSinkPluginBase> plugins;
	private JComboBox<String> pluginChooser;

	/**
	 * Constructor
	 */
	public DelimitedFieldFileSaveChooser() {
		fileChooser = new JFileChooser();

		delimiter = null;
		delimitersModel = new DefaultComboBoxModel<String>();
		delimiterChooser = new JComboBox<String>();

		JPanel accessoryPane = new JPanel();
		accessoryPane.setLayout(new BoxLayout(accessoryPane,
				BoxLayout.PAGE_AXIS));
		accessoryPane.add(createPluginsList());
		accessoryPane.add(createDelimiterSelectionPane());

		fileChooser.setAccessory(accessoryPane);
	}

	/**
	 * Return the currently selected observation sink plugin.
	 * 
	 * @return The plugin instance.
	 */
	public ObservationSinkPluginBase getSelectedPlugin() {
		return plugins.get(pluginChooser.getSelectedItem());
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
	 * Create plugin list and add a listener to change delimiters when a plugin
	 * is selected.
	 */
	private JPanel createPluginsList() {
		JPanel pane = new JPanel();

		plugins = new HashMap<String, ObservationSinkPluginBase>();

		for (ObservationSinkPluginBase plugin : PluginLoader
				.getObservationSinkPlugins()) {
			String name = plugin.getDisplayName();
			plugins.put(name, plugin);
		}

		pluginChooser = new JComboBox<String>(plugins.keySet().toArray(
				new String[0]));
		pluginChooser.setSelectedItem(LocaleProps.get("TEXT_FORMAT_FILE"));
		pluginChooser.setBorder(BorderFactory.createTitledBorder("Type"));

		pluginChooser.addActionListener(e -> {
			String name = (String) pluginChooser.getSelectedItem();
			updateDelimiterChoices(plugins.get(name));
		});

		if (!plugins.isEmpty()) {
			// Initial delimiter choices.
			String name = (String) pluginChooser.getSelectedItem();
			updateDelimiterChoices(plugins.get(name));
		}

		pane.add(pluginChooser);

		return pane;
	}

	/**
	 * Update the delimiter choices for a plugin.
	 * 
	 * @param plugin
	 *            The selected plugin.
	 */
	private void updateDelimiterChoices(ObservationSinkPluginBase plugin) {
		delimitersModel.removeAllElements();

		if (plugin.getDelimiterNameValuePairs() != null) {
			delimiters = plugin
					.getDelimiterNameValuePairs();

			for (String delim : delimiters.keySet().toArray(new String[0])) {
				delimitersModel.addElement(delim);
			}

			delimiterChooser.setModel(delimitersModel);
			delimiterChooser.setSelectedIndex(0);
		} else {
			delimiterChooser.setModel(delimitersModel);
		}
	}

	/**
	 * This component permits selection between field delimiters.
	 */
	private JPanel createDelimiterSelectionPane() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.setBorder(BorderFactory.createTitledBorder("Delimiter"));

		delimiterChooser.addActionListener(e -> {
			String name = (String) delimiterChooser.getSelectedItem();
			delimiter = delimiters.get(name);
		});

		if (delimiters != null && !delimiters.isEmpty()) {
			// Initial delimiter selection.
			String name = (String) delimiterChooser.getSelectedItem();
			delimiter = delimiters.get(name);
		}

		panel.add(delimiterChooser);

		return panel;
	}
}
