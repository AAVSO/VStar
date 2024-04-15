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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;

import org.aavso.tools.vstar.plugin.ObservationSinkPluginBase;
import org.aavso.tools.vstar.ui.resources.PluginLoader;
import org.aavso.tools.vstar.util.help.Help;
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
	 * Return the selected file. If no suffix was specified and one is
	 * associated with the selected delimiter, a suffix will be added to the
	 * file name.
	 * 
	 * @return The selected file (absolute path as a File object).
	 */
	public File getSelectedFile() {
		File file = fileChooser.getSelectedFile();

		ObservationSinkPluginBase plugin = getSelectedPlugin();
		Map<String, String> delimiter2suffixes = plugin
				.getDelimiterSuffixValuePairs();

		String selectedDelimiterName = (String) delimiterChooser
				.getSelectedItem();
		String suffix = delimiter2suffixes.get(selectedDelimiterName);

		if (!file.getName().endsWith(suffix) && delimiter2suffixes != null) {
			file = new File(file.getAbsolutePath() + "." + suffix);
		}

		return file;
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

		pane.setBorder(BorderFactory.createTitledBorder("Type"));
		
		plugins = new HashMap<String, ObservationSinkPluginBase>();

		for (ObservationSinkPluginBase plugin : PluginLoader
				.getObservationSinkPlugins()) {
			String name = plugin.getDisplayName();
			plugins.put(name, plugin);
		}

		pluginChooser = new JComboBox<String>(plugins.keySet().toArray(
				new String[0]));
		
		pluginChooser.setSelectedItem(LocaleProps.get("DOWNLOAD_FORMAT_FILE"));
		//pluginChooser.setBorder(BorderFactory.createTitledBorder("Type"));

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
		
		JButton helpButton = new JButton("?");
		
		helpButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ObservationSinkPluginBase plugin = getSelectedPlugin();
				Help.openPluginHelp(plugin != null ? plugin.getDocName() : null);
			}
		});

		pane.add(helpButton);

		return pane;
	}

	/**
	 * Update the delimiter choices for a plugin.
	 * 
	 * @param plugin
	 *            The selected plugin.
	 */
	private void updateDelimiterChoices(ObservationSinkPluginBase plugin) {
		try {
			delimitersModel = new DefaultComboBoxModel<String>();

			if (plugin.getDelimiterNameValuePairs() != null) {
				delimiters = plugin.getDelimiterNameValuePairs();

				for (String delim : delimiters.keySet().toArray(new String[0])) {
					delimitersModel.addElement(delim);
				}

				delimiterChooser.setModel(delimitersModel);
				delimiterChooser.setSelectedIndex(0);
			} else {
				delimiterChooser.setModel(delimitersModel);
			}
		} catch (Exception e) {
			Exception f = e;
		}
	}

	/**
	 * This component permits selection between field delimiters.
	 */
	private JPanel createDelimiterSelectionPane() {
		JPanel panel = new JPanel();
		//panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.setLayout(new BorderLayout());
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

		//panel.add(delimiterChooser);
		panel.add(delimiterChooser, BorderLayout.PAGE_START);

		return panel;
	}
	
}
