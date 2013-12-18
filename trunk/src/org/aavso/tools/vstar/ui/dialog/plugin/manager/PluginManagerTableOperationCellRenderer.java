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
 * @deprecated
 */
package org.aavso.tools.vstar.ui.dialog.plugin.manager;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import org.aavso.tools.vstar.ui.mediator.Mediator;

//instead, make this the basis for a button pane that shows the buttons; 
//we'll get to it by clicking on a list item; perhaps use a JList instead of a JTable;
//we have code for this; buttons can be enabled/disabled/title-changed at dialog end

/**
 * A table cell renderer for plugin manager operations.
 * @deprecated
 */
public class PluginManagerTableOperationCellRenderer implements
		TableCellRenderer {

	private PluginManager manager;
	private PluginTableModel model;
	private Map<String, JButton> installButtons;
	private Map<String, JButton> deleteButtons;

	/**
	 * Constructor
	 * 
	 * @param manager
	 *            The plugin manager.
	 */
	public PluginManagerTableOperationCellRenderer(PluginManager manager,
			PluginTableModel model) {
		super();
		this.manager = manager;
		this.model = model;
		installButtons = new HashMap<String, JButton>();
		deleteButtons = new HashMap<String, JButton>();
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object color,
			boolean isSelected, boolean hasFocus, int row, int column) {
		// This should only be called for the column we set this renderer for.
		return createOperationComponent(row);
	}

	private JPanel createOperationComponent(int row) {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
		// TODO: localise
		String desc = model.getPluginDescriptions().get(row);

		JButton installOrUpdateButton = null;
		JButton deleteButton = null;
		
		if (manager.isLocal(desc) && !manager.isRemote(desc)) {
			deleteButton = getButton(deleteButtons, desc, "Delete");
		} else if (!manager.isLocal(desc) && manager.isRemote(desc)) {
			installOrUpdateButton = getButton(installButtons, desc, "Install");
		} else if (manager.isLocal(desc) && manager.isRemote(desc)) {
			if (manager.arePluginsEqual(desc)) {
				deleteButton = getButton(deleteButtons, desc, "Delete");
			} else {
				installOrUpdateButton = getButton(installButtons, desc, "Update");
				deleteButton = getButton(deleteButtons, desc, "Delete");
			}
		}

		if (installOrUpdateButton != null) {
			installOrUpdateButton
					.addActionListener(createInstallOrUpdateActionListener());
			installOrUpdateButton.setActionCommand(desc);
			panel.add(installOrUpdateButton);
			panel.add(Box.createRigidArea(new Dimension(10, 10)));
			installButtons.put(desc, installOrUpdateButton);
		}

		if (deleteButton != null) {
			deleteButton.addActionListener(createDeleteActionListener());
			deleteButton.setActionCommand(desc);
			panel.add(deleteButton);
			deleteButtons.put(desc, deleteButton);
		}

		return panel;
	}
	
	// Create or get a cached button, setting the title if it has changed.
	private JButton getButton(Map<String, JButton> buttons, String desc, String title) {
		JButton button = null;
		
		if (buttons.containsKey(desc)) {
			button = buttons.get(desc);
			if (!title.equals(button.getText())) {
				button.setText(title);
			}
		} else {
			button = new JButton(title);
		}
		
		return button;
	}
	
	private ActionListener createInstallOrUpdateActionListener() {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final String desc = e.getActionCommand();
				PluginManagementOperation op = new PluginManagementOperation(
						manager, String.format(
								"Performing Plug-in %s Operation",
								installButtons.get(desc))) {
					@Override
					public void execute() {
//						manager.installPlugin(desc);
						new PluginManagementDialog(manager);
					}
				};
				Mediator.getInstance().performPluginManagerOperation(op);
			}
		};
	}

	private ActionListener createDeleteActionListener() {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final String desc = e.getActionCommand();
				PluginManagementOperation op = new PluginManagementOperation(
						manager, "Performing Plug-in Delete Operation") {
					@Override
					public void execute() {
						manager.deletePlugin(desc);
						new PluginManagementDialog(manager);
					}
				};
				Mediator.getInstance().performPluginManagerOperation(op);
			}
		};
	}
}
