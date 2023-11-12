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
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;

import org.aavso.tools.vstar.plugin.InputType;
import org.aavso.tools.vstar.plugin.ObservationSourcePluginBase;
import org.aavso.tools.vstar.plugin.PluginComponentFactory;
import org.aavso.tools.vstar.ui.dialog.plugin.manager.PluginManager;
import org.aavso.tools.vstar.ui.resources.PluginLoader;
import org.aavso.tools.vstar.util.Pair;
import org.aavso.tools.vstar.util.help.Help;
import org.aavso.tools.vstar.util.locale.LocaleProps;

/**
 * This class aggregates a JFileChooser and additive load checkbox and URL entry
 * components.
 */
public class AdditiveLoadFileOrUrlChooser {

	private JFileChooser fileChooser;
	private JCheckBox additiveLoadCheckbox;
	private boolean urlProvided;
	private JButton urlRequestButton;
	private TextField urlField;
	private TextArea velaFilterField;
	private List<String> DEFAULT_EXTENSIONS = new ArrayList<String>();
	private List<String> extensions = new ArrayList<String>();
	private Map<String, ObservationSourcePluginBase> plugins;
	private JComboBox<String> pluginChooser;

	/**
	 * Constructor
	 * 
	 * @param allowURL
	 *            Should a URL entry be allowed?
	 */
	public AdditiveLoadFileOrUrlChooser(boolean allowURL) {
		fileChooser = new JFileChooser();
		urlProvided = false;
		plugins = new TreeMap<String, ObservationSourcePluginBase>();

		// Default file extensions.
		DEFAULT_EXTENSIONS.add("csv");
		DEFAULT_EXTENSIONS.add("dat");
		DEFAULT_EXTENSIONS.add("tsv");
		DEFAULT_EXTENSIONS.add("txt");
		setFileExtensions(DEFAULT_EXTENSIONS);

		JPanel accessoryPane = new JPanel();
		accessoryPane.setLayout(new BoxLayout(accessoryPane,
				BoxLayout.PAGE_AXIS));
		accessoryPane.add(createAdditiveLoadCheckboxPane());
		if (allowURL) {
			accessoryPane.add(createUrlPane());
		}

		if (!PluginManager.shouldAllObsSourcePluginsBeInFileMenu()) {
			accessoryPane.add(createPluginsList());
		}

		Pair<TextArea, JPanel> pair = PluginComponentFactory
				.createVeLaFilterPane();
		velaFilterField = pair.first;
		accessoryPane.add(pair.second);

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
		fileChooser.removeChoosableFileFilter(fileChooser.getFileFilter());
		fileChooser.setFileFilter(new FileExtensionFilter(extensions));
	}

	/**
	 * Returns the content of the VeLa filter field.
	 * 
	 * @return the string content of the VeLa filter field.
	 */
	public String getVeLaFilter() {
		return velaFilterField.getValue().trim();
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
	 * This component creates a URL request button and corresponding action.
	 */
	private JPanel createUrlPane() {
		JPanel pane = new JPanel();

		urlRequestButton = new JButton("Request URL");

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
	 * Create plugin list and add a listener to change extensions when a plugin
	 * is selected.
	 */
	private JPanel createPluginsList() {
		JPanel pane = new JPanel();

		pane.setBorder(BorderFactory.createTitledBorder("Source"));
		
		for (ObservationSourcePluginBase plugin : PluginLoader
				.getObservationSourcePlugins()) {

			switch (plugin.getInputType()) {
			case FILE:
			case FILE_OR_URL:
				String name = plugin.getDisplayName();
				if (name.equals(LocaleProps.get("FILE_MENU_NEW_STAR_FROM_FILE"))) {
					// Handle localised "New Star from File"
					name = LocaleProps.get("TEXT_FORMAT_FILE");
				} else {
					// Shorten other "New Star from " plugin names.
					name = name.replace("New Star from ", "");
					name = name.replace(" File", "");
					name = name.replace("...", "");
				}
				plugins.put(name, plugin);
			default:
			}
		}

		pluginChooser = new JComboBox<String>(plugins.keySet().toArray(
				new String[0]));
		pluginChooser.setSelectedItem(LocaleProps.get("TEXT_FORMAT_FILE"));
		//pluginChooser.setBorder(BorderFactory.createTitledBorder("Source"));

		pluginChooser
				.addActionListener(e -> {
					String name = (String) pluginChooser.getSelectedItem();
					ObservationSourcePluginBase plugin = plugins.get(name);

					List<String> additional = new ArrayList<String>();
					additional.addAll(DEFAULT_EXTENSIONS);
					if (plugin.getAdditionalFileExtensions() != null) {
						additional.addAll(plugin.getAdditionalFileExtensions());
					}
					setFileExtensions(additional);

					boolean urlAllowed = plugin.getInputType() == InputType.FILE_OR_URL;
					urlRequestButton.setEnabled(urlAllowed);
				});

		pane.add(pluginChooser);

		JButton helpButton = new JButton("?");

		helpButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Optional<ObservationSourcePluginBase> plugin = getSelectedPlugin();
				Help.openPluginHelp(!plugin.isEmpty() ? plugin.get().getDocName() : null);
			}
		});

		pane.add(helpButton);

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
	 * Return the optional currently selected observation source plugin.
	 * 
	 * @return The optional plugin instance.
	 */
	public Optional<ObservationSourcePluginBase> getSelectedPlugin() {
		Optional<ObservationSourcePluginBase> plugin;

		if (PluginManager.shouldAllObsSourcePluginsBeInFileMenu()) {
			plugin = Optional.empty();
		} else {
			plugin = Optional.of(plugins.get(pluginChooser.getSelectedItem()));
		}

		return plugin;
	}

	/**
	 * Reset this file selector's state before use.
	 */
	public synchronized void reset() {
		setUrlProvided(false);
	}
}
