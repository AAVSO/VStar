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

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.aavso.tools.vstar.ui.dialog.MessageBox;
import org.aavso.tools.vstar.ui.mediator.DocumentManager;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.resources.PluginLoader;

/**
 * The plugin management dialog.
 */
@SuppressWarnings("serial")
public class PluginManagementDialog extends JDialog implements ListSelectionListener {

	private PluginManager manager;

	private JList pluginList;
	private DefaultListModel pluginListModel;

	private JButton dismissButton;
	private JButton installButton;
	private JButton updateButton;
	private JButton deleteButton;
	private JCheckBox allCheckBox;
	private JButton closeProgramButton;

	private final String DIALOG_TITLE = "Plug-in Manager";
	private final String DIALOG_TITLE_MOD = "Plug-in Manager: PROGRAM RESTART NEEDED";
	private boolean restartNeeded = false;

	private static boolean pluginManagementActive = false;

	/**
	 * Constructor
	 */
	public PluginManagementDialog(PluginManager manager) {
		super(DocumentManager.findActiveWindow());

		// System.out.println(Thread.currentThread().getId());

		// TODO: localise
		setTitle(DIALOG_TITLE);

		this.manager = manager;

		JPanel topPane = new JPanel();
		topPane.setLayout(new BoxLayout(topPane, BoxLayout.PAGE_AXIS));
		topPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		topPane.add(createListPane());
		topPane.add(createButtonPane());
		topPane.add(createButtonPane2());

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

		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				setPluginManagementActive(false);
			}
		});
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		// this.setModal(true);

		this.setLocationRelativeTo(Mediator.getUI().getContentPane());
		this.setVisible(true);
	}

	public static synchronized void setPluginManagementActive(boolean b) {
		// System.out.println("setPluginManagementActive: " +
		// Thread.currentThread().getId());
		pluginManagementActive = b;
	}

	public static synchronized boolean getPluginManagementActive() {
		// System.out.println("getPluginManagementActive: " +
		// Thread.currentThread().getId());
		return pluginManagementActive;
	}

	private void closePluginLoaders() {
		PluginLoader.closePluginLoaders();
	}

	private void setRestartFlag() {
		restartNeeded = true;
		setTitle(DIALOG_TITLE_MOD);
		closeProgramButton.setVisible(true);
		pack();
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
		// PluginManager can install/delete one plugin at once so multiply selection is
		// useless and may confuse
		pluginList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION/* .SINGLE_INTERVAL_SELECTION */);
		pluginList.addListSelectionListener(this);
		pluginList.setCellRenderer(new pluginManagerCellRenderer());
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

		allCheckBox = new JCheckBox("All?");
		allCheckBox.addActionListener((e) -> {
			enableAllButtons();			
			if (allCheckBox.isSelected()) {
				pluginList.setEnabled(false);
				setButtonStatesForBatchOp();
			} else {
				pluginList.setEnabled(true);
				setButtonStatesForSelectedPlugin();
			}
		});
		panel.add(allCheckBox);

		this.getRootPane().setDefaultButton(dismissButton);

		return panel;
	}

	private JPanel createButtonPane2() {
		JPanel panel = new JPanel(new FlowLayout());

		closeProgramButton = new JButton("VStar should be restarted. Click to close the program.");
		closeProgramButton.addActionListener(createCloseProgramButtonListener());
		closeProgramButton.setEnabled(true);
		closeProgramButton.setVisible(false);
		closeProgramButton.setBackground(Color.YELLOW);
		closeProgramButton.setForeground(Color.RED);
		panel.add(closeProgramButton);

		return panel;
	}

	private void enableAllButtons() {
		dismissButton.setEnabled(true);
		deleteButton.setEnabled(true);
		installButton.setEnabled(true);
		updateButton.setEnabled(true);
		allCheckBox.setEnabled(true);
		closeProgramButton.setEnabled(true);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
	}
	
	private void disableAllButtons() {
		dismissButton.setEnabled(false);
		deleteButton.setEnabled(false);
		installButton.setEnabled(false);
		updateButton.setEnabled(false);
		allCheckBox.setEnabled(false);
		closeProgramButton.setEnabled(false);
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
	}

	private void setButtonStatesForBatchOp() {
		deleteButton.setEnabled(true);
		installButton.setEnabled(true);
		updateButton.setEnabled(false);
	}
	
	// Set button states for currently selected plugin
	private void setButtonStatesForSelectedPlugin() {
		int index = pluginList.getSelectedIndex();
		if (index != -1) {
			String desc = (String) pluginListModel.get(index);
			setButtonStates(desc);
		}
	}
	
	// Set the button states based upon the specified description.
	private void setButtonStates(String desc) {
		// Start with a blank slate.
		installButton.setEnabled(false);
		updateButton.setEnabled(false);
		deleteButton.setEnabled(false);
		// deleteAllButton.setEnabled(false);

		if (manager.isLocal(desc) && !manager.isRemote(desc)) {
			deleteButton.setEnabled(true);
			// deleteAllButton.setEnabled(true);
		} else if (!manager.isLocal(desc) && manager.isRemote(desc)) {
			installButton.setEnabled(true);
		} else if (manager.isLocal(desc) && manager.isRemote(desc)) {
			if (manager.arePluginsEqual(desc)) {
				deleteButton.setEnabled(true);
				// deleteAllButton.setEnabled(true);
			} else {
				updateButton.setEnabled(true);
				deleteButton.setEnabled(true);
				// deleteAllButton.setEnabled(true);
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
				setPluginManagementActive(false);
				setVisible(false);
				dispose();
			}
		};
	}
	
  	private void updateInterfaceAfterPluginOp() {
		enableAllButtons();			
		if (allCheckBox.isSelected()) {
			setButtonStatesForBatchOp();
		} else {
			setButtonStatesForSelectedPlugin();
		}
  		setRestartFlag();
  		pluginList.repaint();
  	}
	

	// Return a listener for the "Install" button.
	private ActionListener createInstallButtonListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (allCheckBox.isSelected()) {
					if (!MessageBox.showConfirmDialog("Plug-in Manager", "Install all plug-ins?"))
						return;
				}
				int index = pluginList.getSelectedIndex();
				final Set<String> descs = new HashSet<String>();
				if (!allCheckBox.isSelected()) {
					descs.add((String) pluginListModel.get(index));
				} else {
					descs.addAll(manager.getRemoteDescriptions());
				}

				PluginManagementOperation op = new PluginManagementOperation(manager,
						"Performing Plug-in Install Operation") {
					@Override
					public void execute() {
						try {
							for (String desc : descs) {
								manager.installPlugin(desc, PluginManager.Operation.INSTALL);
							}
						} finally {
							javax.swing.SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									updateInterfaceAfterPluginOp();				
								}
							});
						}
					}
				};
				// in "Install All" mode all plugins, including existing, will be installed
				closePluginLoaders(); // under Windows, JAR file handles must be closed to allow overwriting
				disableAllButtons();
				Mediator.getInstance().performPluginManagerOperation(op);
			}
		};
	}

	// Return a listener for the "Update" button.
	private ActionListener createUpdateButtonListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (allCheckBox.isSelected()) {
					if (!MessageBox.showConfirmDialog("Plug-in Manager", "Update all plug-ins?"))
						return;
				}
				int index = pluginList.getSelectedIndex();
				final Set<String> descs = new HashSet<String>();
				if (!allCheckBox.isSelected()) {
					descs.add((String) pluginListModel.get(index));
				} else {
					descs.addAll(manager.getRemoteDescriptions());
				}
 
				PluginManagementOperation op = new PluginManagementOperation(manager,
						"Performing Plug-in Update Operation") {
					@Override
					public void execute() {
						try {
							for (String desc : descs) {
								manager.installPlugin(desc, PluginManager.Operation.UPDATE);
							}
						} finally {
							javax.swing.SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									updateInterfaceAfterPluginOp();				
								}
							});
						}
					}
				};
				closePluginLoaders(); // under Windows, JAR file handles must be closed to allow overwriting				
				disableAllButtons();
				Mediator.getInstance().performPluginManagerOperation(op);
			}
		};
	}

	// Return a listener for the "Delete" button.
	private ActionListener createDeleteButtonListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (allCheckBox.isSelected()) {
					if (!MessageBox.showConfirmDialog("Plug-in Manager", "Delete all plug-ins?"))
						return;
				}
				int index = pluginList.getSelectedIndex();
				final Set<String> descs = new HashSet<String>();
				if (!allCheckBox.isSelected()) {
					descs.add((String) pluginListModel.get(index));
				} else {
					descs.addAll(manager.getLocalDescriptions());
				}

				PluginManagementOperation op = new PluginManagementOperation(manager,
						"Performing Plug-in Delete Operation") {
					@Override
					public void execute() {
						// Do not use manager.deleteAllPlugins() here,
						// it displays dialogs yet would be called from background thread here.
						try {
							for (String desc : descs) {
								manager.deletePlugin(desc);
							}
						} finally {
							javax.swing.SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									// If not also remote, remove from list.
									for (String desc : descs) {
										if (!manager.isRemote(desc)) {
											pluginListModel.remove(pluginListModel.indexOf(desc));
										}
									}
									updateInterfaceAfterPluginOp();				
								}
							});
						}
					}
				};
				closePluginLoaders(); // under Windows, JAR file handles must be closed				
				disableAllButtons();
				Mediator.getInstance().performPluginManagerOperation(op);
			}
		};
	}

	// Return a listener for the "Close Program" button.
	private ActionListener createCloseProgramButtonListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Mediator mediator = Mediator.getInstance();
				mediator.quit();
			}
		};
	}

	
	private class pluginManagerCellRenderer extends DefaultListCellRenderer  {

		@Override
		public Component getListCellRendererComponent(JList<?> list,
			Object value, int index, boolean isSelected, boolean cellHasFocus) {
			JLabel l = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			String desc = (String) pluginListModel.get(index);
			Font f = l.getFont();
			if (manager.isLocal(desc)) {
				l.setFont(f.deriveFont(f.getStyle() | Font.BOLD));
				if (!manager.arePluginsEqual(desc)) {
					l.setForeground(Color.GRAY);
				}
			}
			return l;
		}
	}
}
