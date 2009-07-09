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
import java.awt.Component;
import java.awt.Dimension;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToolBar;

/**
 * The application's toolbar.
 */
public class ToolBar extends JPanel {

	private MenuBar menuBar;

	/**
	 * Creates the application's toolbar.
	 * 
	 * @param menuBar
	 *            Pass in menu bar to get listeners. TODO: put them into a
	 *            common class shared by both instead?
	 */
	public ToolBar(Component parent, MenuBar menuBar,
			URL newStarFromDatabaseIconURL, URL newStarFromFileIconURL) {
		super(new BorderLayout());

		this.menuBar = menuBar;

		// Create the toolbar icons.
		Icon newStarFromFileIcon = null;

		if (newStarFromFileIconURL != null) {
			newStarFromFileIcon = new ImageIcon(newStarFromFileIconURL);
		} else {
			MessageBox.showErrorDialog(parent, "VStar Toolbar",
					"Can't locate file icon");
			return;
		}

		Icon newStarFromDatabaseIcon = null;

		if (newStarFromDatabaseIconURL != null) {
			newStarFromDatabaseIcon = new ImageIcon(newStarFromDatabaseIconURL);
		} else {
			MessageBox.showErrorDialog(parent, "VStar Toolbar",
					"Can't locate database icon");
			return;
		}

		JToolBar toolBar = new JToolBar("VStar operations");

		// Create the toolbar buttons.
		JButton newStarFromDatabaseButton = new JButton(newStarFromDatabaseIcon);
		newStarFromDatabaseButton
				.setToolTipText(MenuBar.NEW_STAR_FROM_DATABASE);
		newStarFromDatabaseButton.addActionListener(menuBar
				.createNewStarFromDatabaseListener());
		toolBar.add(newStarFromDatabaseButton);

		JButton newStarFromFileButton = new JButton(newStarFromFileIcon);
		newStarFromFileButton.setToolTipText(MenuBar.NEW_STAR_FROM_FILE);
		newStarFromFileButton.addActionListener(menuBar
				.createNewStarFromFileListener());
		toolBar.add(newStarFromFileButton);

		// Add the toolbar to the panel.
		this.setPreferredSize(new Dimension(150, 35));
		this.add(toolBar, BorderLayout.PAGE_START);
		
		// TODO: add save, print, help buttons
	}
}
