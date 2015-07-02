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
package org.aavso.tools.vstar.ui.dialog.plugin.manager;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.aavso.tools.vstar.ui.mediator.DocumentManager;
import org.aavso.tools.vstar.ui.mediator.Mediator;

/**
 * The plugin management dialog.
 */
@SuppressWarnings("serial")
public class PluginManagementDialog extends JDialog implements
		ListSelectionListener {

	private PluginManager manager;

	private JList pluginList;
	private DefaultListModel pluginListModel;

	private JButton dismissButton;
	private JButton installButton;
	private JButton updateButton;
	private JButton deleteButton;
	private JButton deleteAllButton;

	/**
	 * Constructor
	 */
	public PluginManagementDialog(PluginManager manager) {
		super(DocumentManager.findActiveWindow());
		// TODO: localise
		this.setTitle("Plug-in Manager");

		this.manager = manager;

		JPanel topPane = new JPanel();
		topPane.setLayout(new BoxLayout(topPane, BoxLayout.PAGE_AXIS));
		topPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		topPane.add(createListPane());
		topPane.add(createButtonPane());

		getContentPane().add(topPane);

		this.getRootPane().setDefaultButton(dismissButton);

		this.pack();

		// Set the initial button states.
		if (!pluginListModel.isEmpty()) {
			pluginList.setSelectedIndex(0);
			int index = pluginList.getSelectedIndex();
			if (index != -1) {
				String desc = (String) pluginListModel.get(index);
				setButtonStates(desc);
			}
		}

		this.setLocationRelativeTo(Mediator.getUI().getContentPane());
		this.setVisible(true);
	}

	private JPanel createListPane() {
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		// Get a combined, unique list of plugin descriptions for adding to the
		// model.
		Set<String> pluginDescSet = new LinkedHashSet<String>();
		pluginDescSet.addAll(manager.getLocalDescriptions());
		pluginDescSet.addAll(manager.getRemoteDescriptions());

		pluginListModel = new DefaultListModel();

		for (String desc : pluginDescSet) {
			pluginListModel.addElement(desc);
		}

		pluginList = new JList(pluginListModel);
		pluginList
				.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		pluginList.addListSelectionListener(this);
		JScrollPane modelListScroller = new JScrollPane(pluginList);

		panel.add(modelListScroller);

		return panel;
	}

	private JPanel createButtonPane() {
		JPanel panel = new JPanel(new FlowLayout());

		dismissButton = new JButton("Dismiss");
		dismissButton.addActionListener(createDismissButtonListener());
		dismissButton.setEnabled(true);
		panel.add(dismissButton);

		installButton = new JButton("Install");
		installButton.addActionListener(createInstallButtonListener());
		installButton.setEnabled(false);
		panel.add(installButton);

		updateButton = new JButton("Update");
		updateButton.addActionListener(createUpdateButtonListener());
		updateButton.setEnabled(false);
		panel.add(updateButton);

		deleteButton = new JButton("Delete");
		deleteButton.addActionListener(createDeleteButtonListener());
		deleteButton.setEnabled(false);
		panel.add(deleteButton);

		deleteAllButton = new JButton("Delete All");
		deleteAllButton.addActionListener(createDeleteAllButtonListener());
		deleteAllButton.setEnabled(false);
		panel.add(deleteAllButton);

		this.getRootPane().setDefaultButton(dismissButton);

		return panel;
	}

	// Set the button states based upon the specified description.
	private void setButtonStates(String desc) {
		// Start with a blank slate.
		installButton.setEnabled(false);
		updateButton.setEnabled(false);
		deleteButton.setEnabled(false);
		deleteAllButton.setEnabled(false);

		if (manager.isLocal(desc) && !manager.isRemote(desc)) {
			deleteButton.setEnabled(true);
			deleteAllButton.setEnabled(true);
		} else if (!manager.isLocal(desc) && manager.isRemote(desc)) {
			installButton.setEnabled(true);
		} else if (manager.isLocal(desc) && manager.isRemote(desc)) {
			if (manager.arePluginsEqual(desc)) {
				deleteButton.setEnabled(true);
				deleteAllButton.setEnabled(true);
			} else {
				updateButton.setEnabled(true);
				deleteButton.setEnabled(true);
				deleteAllButton.setEnabled(true);
			}
		}
	}

	// List selection listener to update button states.
	public void valueChanged(ListSelectionEvent e) {
		if (e.getValueIsAdjusting() == false) {
			int index = pluginList.getSelectedIndex();
			if (index != -1) {
				String desc = (String) pluginListModel.get(index);
				setButtonStates(desc);
			}
		}
	}

	// Return a listener for the "Dismiss" button.
	private ActionListener createDismissButtonListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				dispose();
			}
		};
	}

	// Return a listener for the "Install" button.
	private ActionListener createInstallButtonListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int index = pluginList.getSelectedIndex();
				final String desc = (String) pluginListModel.get(index);
				PluginManagementOperation op = new PluginManagementOperation(
						manager, "Performing Plug-in Install Operation") {
					@Override
					public void execute() {
						manager.installPlugin(desc,
								PluginManager.Operation.INSTALL);
						setButtonStates(desc);
					}
				};
				Mediator.getInstance().performPluginManagerOperation(op);
			}
		};
	}

	// Return a listener for the "Update" button.
	private ActionListener createUpdateButtonListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int index = pluginList.getSelectedIndex();
				final String desc = (String) pluginListModel.get(index);
				PluginManagementOperation op = new PluginManagementOperation(
						manager, "Performing Plug-in Update Operation") {
					@Override
					public void execute() {
						manager.installPlugin(desc,
								PluginManager.Operation.UPDATE);
						setButtonStates(desc);
					}
				};
				Mediator.getInstance().performPluginManagerOperation(op);
			}
		};
	}

	// Return a listener for the "Delete" button.
	private ActionListener createDeleteButtonListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int index = pluginList.getSelectedIndex();
				final String desc = (String) pluginListModel.get(index);
				PluginManagementOperation op = new PluginManagementOperation(
						manager, "Performing Plug-in Delete Operation") {
					@Override
					public void execute() {
						manager.deletePlugin(desc);
						setButtonStates(desc);
					}
				};
				Mediator.getInstance().performPluginManagerOperation(op);

				// If not also remote, remove from list.
				if (!manager.isRemote(desc)) {
					pluginListModel.remove(index);
				}
			}
		};
	}

	// Return a listener for the "Delete All" button.
	private ActionListener createDeleteAllButtonListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final Set<String> descs = new HashSet<String>(
						manager.getLocalDescriptions());

				PluginManagementOperation op = new PluginManagementOperation(
						manager, "Performing Plug-in Delete All Operation") {
					@Override
					public void execute() {
						manager.deleteAllPlugins();
						for (String desc : descs) {
							setButtonStates(desc);
						}
					}
				};
				Mediator.getInstance().performPluginManagerOperation(op);

				// If not also remote, remove from list.
				for (String desc : descs) {
					if (!manager.isRemote(desc)) {
						pluginListModel.remove(pluginListModel.indexOf(desc));
					}
				}
			}
		};
	}

}
