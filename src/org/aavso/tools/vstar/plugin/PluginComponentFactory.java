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

import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.aavso.tools.vstar.ui.NamedComponent;
import org.aavso.tools.vstar.ui.dialog.AdditiveLoadFileOrUrlChooser;
import org.aavso.tools.vstar.ui.dialog.TextArea;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.util.Pair;

/**
 * This factory class creates components and returns values for use by plug-ins.
 */
public class PluginComponentFactory {

	final private static Map<String, AdditiveLoadFileOrUrlChooser> fileChoosers = new HashMap<String, AdditiveLoadFileOrUrlChooser>();

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
	 * @param additionalFileExtensions
	 *            The list of file extensions to be added to the file chooser,
	 *            or null if none.
	 * @param allowURL
	 *            Should a URL entry be allowed?
	 * 
	 * @return The file chooser or null if no file was selected.
	 */
	public static AdditiveLoadFileOrUrlChooser chooseFileForReading(String id,
			List<String> additionalFileExtensions, boolean allowURL) {
		AdditiveLoadFileOrUrlChooser fileChooser = null;

		if (id != null) {
			if (!fileChoosers.containsKey(id)) {
				fileChoosers
						.put(id, new AdditiveLoadFileOrUrlChooser(allowURL));

				if (additionalFileExtensions != null) {
					List<String> newFileExtensions = new ArrayList<String>();
					newFileExtensions.addAll(additionalFileExtensions);
					newFileExtensions.addAll(fileChoosers.get(id)
							.getDefaultFileExtensions());
					fileChoosers.get(id).setFileExtensions(newFileExtensions);
				}
			}
			fileChooser = fileChoosers.get(id);
			fileChooser.reset();
		} else {
			fileChooser = new AdditiveLoadFileOrUrlChooser(allowURL);
		}

		// Was a file chosen or a URL string accepted?
		boolean approved = false;

		Component parent = Mediator.getUI().getComponent();

		if (fileChooser.showDialog(parent)) {
			approved = true;
		}

		if (fileChooser.isUrlProvided()) {
			approved = true;
		}

		return approved ? fileChooser : null;
	}
		
	/**
	 * Create a TextArea and a panel, add the former to the latter, and return
	 * both as a pair.
	 * 
	 * @param borderTitle
	 *            The title of the text area's border.
	 * @param toolTip
	 *            The tool tip for the text area.
	 * @param rows
	 *            The number of rows for the text area.
	 * @param cols
	 *            The number of columns for the text area.
	 * @return A pair containing the text area and its containing pane.
	 */
	public static Pair<TextArea, JPanel> createTextAreaPane(String borderTitle,
			String toolTip, int rows, int cols) {
		JPanel pane = new JPanel();
		pane.setLayout(new BoxLayout(pane, BoxLayout.PAGE_AXIS));

		TextArea field = new TextArea(borderTitle, rows, cols);
		field.getUIComponent().setToolTipText(toolTip);
		pane.add(field.getUIComponent());

		return new Pair<TextArea, JPanel>(field, pane);
	}

	/**
	 * This component creates a VeLa Filter pane and returns a text area and
	 * pane as a pair.
	 */
	public static Pair<TextArea, JPanel> createVeLaFilterPane() {
		Pair<TextArea, JPanel> pair = PluginComponentFactory
				.createTextAreaPane("VeLa Filter",
						"VeLa filter applied to each observation read", 2, 15);
		return pair;
	}

}
