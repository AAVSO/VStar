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
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToolBar;

/**
 * The application's toolbar.
 */
public class ToolBar extends JPanel {

	private Component parent;
	private MenuBar menuBar;

	private Icon newStarFromFileIcon;
	private Icon newStarFromDatabaseIcon;
	private Icon helpContentsIcon;

	private JToolBar toolBar;

	/**
	 * Creates the application's toolbar.
	 * 
	 * @param parent
	 *            The parent GUI component.
	 * @param menuBar
	 *            Pass in menu bar to get listeners. TODO: put them into a
	 *            common class shared by both instead?
	 */
	public ToolBar(Component parent, MenuBar menuBar,
			URL newStarFromDatabaseIconURL,
			URL newStarFromFileIconURL) {
		super(new BorderLayout());

		this.parent = parent;
		this.menuBar = menuBar;

		retrieveToolBarIcons();

		this.toolBar = new JToolBar("VStar operations");

		createToolbarButtons();

		// Add the toolbar to the panel.
		this.setPreferredSize(new Dimension(150, 35));
		this.add(toolBar, BorderLayout.PAGE_START);

		// TODO: add save, print buttons
	}

	// Helpers

	private void retrieveToolBarIcons() {
		// Create the toolbar icons.
		// TODO: put these paths into a Properties file

		newStarFromFileIcon = getIcon("/toolbarButtonGraphics/general/New24.gif");
		newStarFromDatabaseIcon = getIcon("/toolbarButtonGraphics/general/Import24.gif");
		helpContentsIcon = getIcon("/toolbarButtonGraphics/general/Help24.gif");

		if (newStarFromDatabaseIcon == null || newStarFromDatabaseIcon == null
				|| helpContentsIcon == null) {
			System.exit(1);
		}
	}

	private Icon getIcon(String path) {
		Icon icon = ResourceAccessor.getIconResource(path);

		if (icon == null) {
			MessageBox.showErrorDialog(parent, "VStar Toolbar",
					"Can't locate icon: " + path);
		}

		return icon;
	}

	private void createToolbarButtons() {
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

		JButton helpContentsButton = new JButton(helpContentsIcon);
		helpContentsButton.setToolTipText(MenuBar.HELP_CONTENTS);
		helpContentsButton.addActionListener(menuBar
				.createHelpContentsListener());
		toolBar.add(helpContentsButton);
	}
}
