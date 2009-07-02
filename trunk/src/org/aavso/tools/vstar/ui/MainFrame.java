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

import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import org.aavso.tools.vstar.ui.model.ObservationPlotModel;
import org.jfree.chart.ChartPanel;

/**
 * TODO: 
 * - Status bar with current star from plot, file loaded etc?
 * - Undoable edits, Edit menu?
 * - About box
 * - Splash Screen?
 * - Toolbar
 * 
 * - We need a ModelManager class to store a mapping
 *   from data files to tabs/observation lists, and also to
 *   handle undo, document "is-dirty" handling, don't load same
 *   file twice etc.
 */

/**
 * The main VStar window.
 */
public class MainFrame extends JFrame {

	// The application's menu bar.
	private JMenuBar menuBar;

	/**
	 * Constructor
	 */
	public MainFrame() {
		super("VStar");

		this.menuBar = new MenuBar(this);
		this.setJMenuBar(menuBar);

		this.setContentPane(createContent());

		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	// Create everything inside the main GUI view.
	private JPanel createContent() {
		// Top-level pane with left to right layout and an empty border.
		JPanel topPane = new JPanel();
		topPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		topPane.setLayout(new BoxLayout(topPane, BoxLayout.LINE_AXIS));		

		// The first (left-most) pane containing mode buttons.
		JPanel firstPane = new JPanel();
		firstPane.setLayout(new BoxLayout(firstPane, BoxLayout.PAGE_AXIS));
		firstPane.add(Box.createVerticalGlue());
		firstPane.add(new ModePane());
		firstPane.add(Box.createVerticalGlue());
		topPane.add(firstPane);
		
		// Create space between the mode and data panes.
		topPane.add(Box.createRigidArea(new Dimension(10, 0)));
		
		// The second (right-most) pane containing data tables, plots,
		// and observation information.
		topPane.add(new DataPane());
		
		return topPane;
	}		
}
