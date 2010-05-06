/**
 * VStar: a statistical analysis tool for variable star data.
 * Copyright (C) 2009  AAVSO (http://www.aavso.org/)
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

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.aavso.tools.vstar.ui.pane.StatusPane;
import org.aavso.tools.vstar.ui.pane.TabbedDataPane;
import org.aavso.tools.vstar.ui.resources.ResourceAccessor;

/**
 * The main VStar window.
 */
public class MainFrame extends JFrame {

	public final static int WIDTH = 800;
	public final static int HEIGHT = 600;

	// The application's menu bar.
	private MenuBar menuBar;

	// The status bar which includes text and progress bar components.
	private StatusPane statusPane;

	// Singleton field and getter.
	private static final MainFrame instance = new MainFrame();

	public static MainFrame getInstance() {
		return instance;
	}

	/**
	 * Private constructor in support of Singleton.
	 */
	private MainFrame() {
		super("VStar " + ResourceAccessor.getVersionString());

		this.menuBar = new MenuBar(this);
		this.setJMenuBar(menuBar);

		this.setContentPane(createContent());

		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		this.pack();
	}

	// Create everything inside the main GUI view except for
	// menus, but including the tool-bar; essentially the interior
	// content of the GUI that represents the core functionality of
	// interest to the user.
	private JPanel createContent() {
		// Top-level content pane to include status pane.
		JPanel topPane = new JPanel(new BorderLayout());

		// Add the tool-bar.
		topPane.add(new ToolBar(this.menuBar), BorderLayout.PAGE_START);

		// Major pane with left to right layout and an empty border.
		JPanel majorPane = new JPanel();
		majorPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		majorPane.setLayout(new BoxLayout(majorPane, BoxLayout.LINE_AXIS));

		majorPane.add(new TabbedDataPane());

		topPane.add(majorPane, BorderLayout.CENTER);

		// Add status pane with an initial message.
		statusPane = new StatusPane(
				"Select a 'New Star' item from the File menu.");
		topPane.add(statusPane, BorderLayout.PAGE_END);

		return topPane;
	}
	
	/**
	 * @return the statusPane
	 */
	public StatusPane getStatusPane() {
		return statusPane;
	}
}
