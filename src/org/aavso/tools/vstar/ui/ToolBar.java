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

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import org.aavso.tools.vstar.ui.model.ModelManager;
import org.aavso.tools.vstar.ui.model.ProgressInfo;
import org.aavso.tools.vstar.ui.resources.ResourceAccessor;
import org.aavso.tools.vstar.util.Listener;

/**
 * The application's toolbar.
 */
public class ToolBar extends JPanel {

	private ModelManager modelMgr = ModelManager.getInstance();

	private MenuBar menuBar;

	private Icon newStarFromFileIcon;
	private Icon newStarFromDatabaseIcon;
	private Icon saveIcon;
	private Icon printIcon;
	private Icon prefsIcon;
	private Icon helpContentsIcon;

	private JToolBar toolBar;

	private JButton newStarFromDatabaseButton;
	private JButton newStarFromFileButton;
	private JButton saveButton;
	private JButton printButton;
	private JButton prefsButton;
	private JButton helpContentsButton;

	/**
	 * Creates the application's toolbar.
	 * 
	 * @param menuBar
	 *            Pass in menu bar to get listeners. TODO: put them into a
	 *            common class shared by both instead?
	 */
	public ToolBar(MenuBar menuBar) {
		super(new BorderLayout());

		this.menuBar = menuBar;

		retrieveToolBarIcons();

		this.toolBar = new JToolBar("VStar Operations");

		createToolbarButtons();

		// Add the toolbar to the panel.
		this.setPreferredSize(new Dimension(150, 35));
		this.add(toolBar, BorderLayout.PAGE_START);

		this.modelMgr.getProgressNotifier().addListener(
				createProgressListener());
	}

	// Helpers

	private void retrieveToolBarIcons() {
		// Create the toolbar icons.
		// TODO: put these paths into a Properties file

		newStarFromFileIcon = ResourceAccessor
				.getIconResource("/toolbarButtonGraphics/general/New24.gif");
		newStarFromDatabaseIcon = ResourceAccessor
				.getIconResource("/toolbarButtonGraphics/general/Import24.gif");
		saveIcon = ResourceAccessor
				.getIconResource("/toolbarButtonGraphics/general/Save24.gif");
		printIcon = ResourceAccessor
				.getIconResource("/toolbarButtonGraphics/general/Print24.gif");
		prefsIcon = ResourceAccessor
				.getIconResource("/toolbarButtonGraphics/general/Preferences24.gif");
		helpContentsIcon = ResourceAccessor
				.getIconResource("/toolbarButtonGraphics/general/Help24.gif");

		if (newStarFromDatabaseIcon == null || newStarFromDatabaseIcon == null
				|| saveIcon == null || printIcon == null || prefsIcon == null
				|| helpContentsIcon == null) {
			System.exit(1);
		}
	}

	private void createToolbarButtons() {
		newStarFromDatabaseButton = new JButton(newStarFromDatabaseIcon);
		newStarFromDatabaseButton
				.setToolTipText(MenuBar.NEW_STAR_FROM_DATABASE);
		newStarFromDatabaseButton.addActionListener(menuBar
				.createNewStarFromDatabaseListener());
		toolBar.add(newStarFromDatabaseButton);

		newStarFromFileButton = new JButton(newStarFromFileIcon);
		newStarFromFileButton.setToolTipText(MenuBar.NEW_STAR_FROM_FILE);
		newStarFromFileButton.addActionListener(menuBar
				.createNewStarFromFileListener());
		toolBar.add(newStarFromFileButton);

		saveButton = new JButton(saveIcon);
		saveButton.setToolTipText(MenuBar.SAVE);
		saveButton.addActionListener(menuBar.createSaveListener());
		saveButton.setEnabled(false);
		toolBar.add(saveButton);

		printButton = new JButton(printIcon);
		printButton.setToolTipText(MenuBar.PRINT);
		printButton.addActionListener(menuBar.createPrintListener());
		printButton.setEnabled(false);
		toolBar.add(printButton);

		prefsButton = new JButton(prefsIcon);
		prefsButton.setToolTipText(MenuBar.PREFS);
		prefsButton.addActionListener(menuBar.createPrefsListener());
		toolBar.add(prefsButton);

		helpContentsButton = new JButton(helpContentsIcon);
		helpContentsButton.setToolTipText(MenuBar.HELP_CONTENTS);
		helpContentsButton.addActionListener(menuBar
				.createHelpContentsListener());
		toolBar.add(helpContentsButton);
	}

	/**
	 * Return a progress listener.
	 */
	private Listener<ProgressInfo> createProgressListener() {
		return new Listener<ProgressInfo>() {
			public void update(ProgressInfo info) {
				switch (info.getType()) {
				case MIN_PROGRESS:
					break;
				case MAX_PROGRESS:
					break;
				case RESET_PROGRESS:
					setEnabledToolbarItems(false);
					break;
				case COMPLETE_PROGRESS:
					setEnabledToolbarItems(true);
					break;
				case INCREMENT_PROGRESS:
					break;
				}
			}
		};
	}

	private void setEnabledToolbarItems(boolean state) {
		saveButton.setEnabled(state);
		printButton.setEnabled(state);
	}
}
