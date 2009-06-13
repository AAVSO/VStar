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

import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

/**
 * TODO: 
 * - JTable not JList
 * - Status bar with current star from plot, file loaded etc
 * - Undoable edits, Edit menu
 * - About box
 * - Splash Screen
 * - Toolbar
 * - Button in plot tab for loading data
 */

/**
 * The main VStar window.
 */
public class MainFrame extends JFrame {

	// Model for valid observations.
	private DefaultListModel dataListModel;

	// Model for invalid observations.
	private DefaultListModel dataErrorListModel;

	// The application's menu bar.
	private JMenuBar menuBar;

	// The tabs for the main window.
	private JTabbedPane tabs;
	
	/**
	 * Constructor
	 */
	public MainFrame() {
		super("VStar");

		this.dataListModel = new DefaultListModel();
		this.dataErrorListModel = new DefaultListModel();

		this.menuBar = new MenuBar(this);
		this.setJMenuBar(menuBar);
				
		this.tabs = this.createContent();
		JPanel panel = new JPanel(new GridLayout(1, 1));
		panel.add(tabs);
		this.setContentPane(panel);

		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	/**
	 * The main components of the main frame are created here.
	 * 
	 * @return The main contents in the form of a tabbed pane.
	 */
	private JTabbedPane createContent() {
		List<NamedComponent> namedComponents = new ArrayList<NamedComponent>();

//		namedComponents.add(new NamedComponent("Light Curve",
//				createTextPanel("Light Curve Goes Here"),
//				"A plot of magnitude against Julian Day"));
//
//		namedComponents.add(new NamedComponent("Phase Plot",
//				createTextPanel("Phase Plot Goes Here"),
//				"A phase plot against the light curve"));

		return new Tabs(namedComponents);
	}

	/**
	 * Create a text panel.
	 * 
	 * @param text
	 *            The text to be displayed.
	 * @return The panel component.
	 */
	private JComponent createTextPanel(String text) {
		JLabel label = new JLabel(text);
		label.setHorizontalAlignment(JLabel.CENTER);
		JPanel panel = new JPanel(false);
		panel.setLayout(new GridLayout(1, 1));
		panel.add(label);
		return panel;
	}

	/**
	 * Create a list in a scroll pane.
	 * 
	 * @param listModel
	 *            The list model to be used by the list.
	 * @return The scroll pane component.
	 */
	private JComponent createScrollPaneList(DefaultListModel listModel) {
		JList list = new JList(listModel);
		list.setSelectedIndex(0);
		list.setVisibleRowCount(20);
		return new JScrollPane(list);
	}

	// Getters

	public DefaultListModel getDataListModel() {
		return dataListModel;
	}

	public DefaultListModel getDataErrorListModel() {
		return dataErrorListModel;
	}

	public JTabbedPane getTabs() {
		return tabs;
	}
}
