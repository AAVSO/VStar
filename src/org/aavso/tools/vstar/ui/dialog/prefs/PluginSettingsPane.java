/**
 * VStar: a statistical analysis tool for variable star data.
 * Copyright (C) 2015  AAVSO (http://www.aavso.org/)
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
package org.aavso.tools.vstar.ui.dialog.prefs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.aavso.tools.vstar.ui.dialog.MessageBox;
import org.aavso.tools.vstar.ui.dialog.plugin.manager.PluginManager;
import org.aavso.tools.vstar.ui.mediator.DocumentManager;
import org.aavso.tools.vstar.util.locale.LocaleProps;

/**
 * Plug-in management preferences panel.
 */
@SuppressWarnings("serial")
public class PluginSettingsPane extends JPanel implements IPreferenceComponent {

	private JCheckBox loadPluginsCheckbox;
	private JTextField baseUrlField;
	private JFileChooser localDirChooser;

	/**
	 * Constructor.
	 */
	public PluginSettingsPane() {
		super();

		JPanel pluginManagementPane = new JPanel();
		pluginManagementPane.setLayout(new BoxLayout(pluginManagementPane,
				BoxLayout.PAGE_AXIS));
		pluginManagementPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5,
				5));

		loadPluginsCheckbox = new JCheckBox("Load plug-ins?");
		loadPluginsCheckbox.setSelected(PluginManager.shouldLoadPlugins());
		loadPluginsCheckbox
				.setToolTipText("Should plug-ins be loaded at startup?");
		loadPluginsCheckbox.setBorder(BorderFactory
				.createLineBorder(Color.BLACK));
		pluginManagementPane.add(loadPluginsCheckbox);

		pluginManagementPane.add(Box.createRigidArea(new Dimension(10, 10)));

		JPanel urlPanel = new JPanel();
		urlPanel.setLayout(new BoxLayout(urlPanel, BoxLayout.LINE_AXIS));

		baseUrlField = new JTextField(PluginManager.getPluginsBaseUrl());
		baseUrlField.setToolTipText("Set plug-in location base URL");
		baseUrlField.setBorder(BorderFactory
				.createTitledBorder("Plug-in location base URL"));
		urlPanel.add(baseUrlField);

		JButton selectLocalDirButton = new JButton("Select Local Directory");
		selectLocalDirButton
				.addActionListener(createSelectLocalDirButtonActionListener());
		urlPanel.add(selectLocalDirButton);

		pluginManagementPane.add(urlPanel);

		localDirChooser = new JFileChooser();
		localDirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		pluginManagementPane.add(Box.createRigidArea(new Dimension(10, 10)));

		JButton deleteAllButton = new JButton("Delete Installed Plug-ins");
		deleteAllButton.setToolTipText("Delete all plug-ins");
		deleteAllButton
				.addActionListener(createDeleteAllPluginsButtonActionListener());
		pluginManagementPane.add(deleteAllButton);

		// Add a local context button pane.
		pluginManagementPane.add(createButtonPane());

		this.add(pluginManagementPane);
	}

	protected JPanel createButtonPane() {
		JPanel panel = new JPanel(new BorderLayout());

		JButton setDefaultsButton = new JButton("Set Defaults");
		setDefaultsButton
				.addActionListener(createSetDefaultsButtonActionListener());
		panel.add(setDefaultsButton, BorderLayout.LINE_START);

		JButton applyButton = new JButton(LocaleProps.get("APPLY_BUTTON"));
		applyButton.addActionListener(createApplyButtonActionListener());
		panel.add(applyButton, BorderLayout.LINE_END);

		return panel;
	}

	// Delete all plug-ins button action listener creator.
	private ActionListener createDeleteAllPluginsButtonActionListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new PluginManager().deleteAllPlugins();
			}
		};
	}

	// Set defaults action button listener creator.
	private ActionListener createSetDefaultsButtonActionListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				loadPluginsCheckbox.setSelected(true);
				baseUrlField.setText(PluginManager.DEFAULT_PLUGIN_BASE_URL_STR);
			}
		};
	}

	// Set select local directory button listener creator.
	private ActionListener createSelectLocalDirButtonActionListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int retVal = localDirChooser.showOpenDialog(DocumentManager
						.findActiveWindow());

				if (retVal == JFileChooser.APPROVE_OPTION) {
					File file = localDirChooser.getSelectedFile();

					if (file.isDirectory()) {
						baseUrlField.setText("file://" + file.getAbsolutePath());
					} else {
						MessageBox.showErrorDialog(
								"Plug-in Preferences",
								String.format("'%s' is not a directory",
										file.getAbsolutePath()));
					}
				}
			}
		};
	}

	// Set apply button listener creator.
	private ActionListener createApplyButtonActionListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				update();
			}
		};
	}

	/**
	 * Updates the global values.
	 */
	@Override
	public void update() {
		PluginManager.setLoadPlugins(loadPluginsCheckbox.isSelected());
		PluginManager.setPluginsBaseUrl(baseUrlField.getText());
	}

	/**
	 * Prepare this pane for use by resetting whatever needs to be.
	 */
	@Override
	public void reset() {
		loadPluginsCheckbox.setSelected(PluginManager.shouldLoadPlugins());
		baseUrlField.setText(PluginManager.getPluginsBaseUrl());
	}
}