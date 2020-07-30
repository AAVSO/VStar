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
package org.aavso.tools.vstar.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.net.URL;
import java.security.Policy;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JApplet;
import javax.swing.JPanel;

import org.aavso.tools.vstar.ui.dialog.MessageBox;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.ViewModeType;
import org.aavso.tools.vstar.ui.resources.PluginLoader;
import org.aavso.tools.vstar.util.locale.LocaleProps;

/**
 * The VStar applet.
 */
@SuppressWarnings("serial")
public class VStarApplet extends JApplet implements IMainUI {

	private static boolean loadPlugins = true;

	// The user interface type.
	private UIType uiType;

	// The application's menu bar.
	private MenuBar menuBar;

	// The status bar which includes text and progress bar components.
	private StatusPane statusPane;

	// Are we in scripting mode?
	private boolean scriptingMode;

	public void init() {
		// Apply VStar Java policy for all code.
		URL policyUrl = Thread.currentThread().getContextClassLoader()
				.getResource("/etc/vstar.java.policy");
		Policy.getPolicy().refresh();

		this.uiType = UIType.APPLET;

		Mediator.getInstance().setUI(this);

		if (loadPlugins) {
			// Load plugins, if any exist and plugin loading is enabled.
			PluginLoader.loadPlugins();
		}

		// Schedule a job for the event-dispatching thread:
		// creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}

	/**
	 * @see javax.swing.JApplet#remove(java.awt.Component)
	 */
	@Override
	public void remove(Component component) {
		// Nothing to do.
	}

	/**
	 * Create and display the applet UI.
	 */
	private void createAndShowGUI() {
		try {
			this.menuBar = new MenuBar(this, uiType);

			this.setJMenuBar(menuBar);

			this.setContentPane(createContent());
		} catch (Throwable t) {
			MessageBox.showErrorDialog(Mediator.getUI().getComponent(),
					"Error", t);
		}
	}

	// Create everything inside the main GUI view except for
	// menus, but including the tool-bar; essentially the interior
	// content of the GUI that represents the core functionality of
	// interest to the user.
	private JPanel createContent() {
		// Top-level content pane to include status pane.
		JPanel topPane = new JPanel(new BorderLayout());

		// topPane.add(new JLabel("VStar " +
		// ResourceAccessor.getVersionString()),
		// BorderLayout.CENTER);

		topPane.add(new ToolBar(this.menuBar), BorderLayout.PAGE_START);

		JPanel majorPane = new JPanel();
		majorPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		majorPane.setLayout(new BoxLayout(majorPane, BoxLayout.LINE_AXIS));

		majorPane.add(new TabbedDataPane());

		topPane.add(majorPane, BorderLayout.CENTER);

		// Add status pane with an initial message.
		statusPane = new StatusPane(LocaleProps
				.get("STATUS_PANE_SELECT_NEW_STAR_FROM_FILE"));
		topPane.add(statusPane, BorderLayout.PAGE_END);

		return topPane;
	}

	@Override
	public Component getComponent() {
		return this;
	}

	@Override
	public StatusPane getStatusPane() {
		return statusPane;
	}

	@Override
	public UIType getUiType() {
		return UIType.APPLET;
	}
	
	@Override
	public void setScriptingStatus(boolean status) {
		scriptingMode = status;
	}

	@Override
	public boolean isScriptingMode() {
		return scriptingMode;
	}

	@Override
	public void addTab(String name, ViewModeType viewMode, Component component, boolean canClose) {
		// Do nothing
	}
}
