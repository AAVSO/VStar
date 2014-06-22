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
package org.aavso.tools.vstar.plugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JTabbedPane;

import org.aavso.tools.vstar.ui.NamedComponent;
import org.aavso.tools.vstar.ui.dialog.AdditiveLoadFileSelectionChooser;
import org.aavso.tools.vstar.ui.mediator.Mediator;

/**
 * This factory class creates components and returns values for use by plug-ins.
 */
public class PluginComponentFactory {

	final private static Map<String, AdditiveLoadFileSelectionChooser> fileChoosers = new HashMap<String, AdditiveLoadFileSelectionChooser>();

	/**
	 * Create a tabbed pane component from a list of named components.
	 * 
	 * @param components
	 *            An list of named component parameters.
	 * @return The tabbed pane component.
	 */
	public static JTabbedPane createTabs(List<NamedComponent> components) {
		JTabbedPane tabs = new JTabbedPane();

		for (NamedComponent component : components) {
			tabs.addTab(component.getName(), null, component.getComponent(),
					component.getTip());
		}

		return tabs;
	}

	/**
	 * Create a tabbed pane component from a list of named components.
	 * 
	 * @param components
	 *            An arbitrary number of named component parameters.
	 * @return The tabbed pane component.
	 */
	public static JTabbedPane createTabs(NamedComponent... components) {
		JTabbedPane tabs = new JTabbedPane();

		for (NamedComponent component : components) {
			tabs.addTab(component.getName(), null, component.getComponent(),
					component.getTip());
		}

		return tabs;
	}

	/**
	 * Open a "read" file chooser and return the selected file.
	 * 
	 * @param id
	 *            An identifier for the file chooser. If null, a new file
	 *            chooser will be created otherwise the file chooser first
	 *            created with this identifier will be used.
	 * @return The file chooser or null if no file was selected.
	 */
	public static AdditiveLoadFileSelectionChooser chooseFileForReading(String id) {
		AdditiveLoadFileSelectionChooser fileChooser = null;

		if (id != null) {
			if (!fileChoosers.containsKey(id)) {
				fileChoosers.put(id, new AdditiveLoadFileSelectionChooser());
			}
			fileChooser = fileChoosers.get(id);
		} else {
			fileChooser = new AdditiveLoadFileSelectionChooser();
		}

		boolean approved = fileChooser.showDialog(Mediator.getUI().getComponent());

		return approved ? fileChooser : null;
	}
}
