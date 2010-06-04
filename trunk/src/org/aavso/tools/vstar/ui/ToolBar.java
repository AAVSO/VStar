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
import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import org.aavso.tools.vstar.ui.dialog.MessageBox;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.ProgressInfo;
import org.aavso.tools.vstar.ui.resources.ResourceAccessor;
import org.aavso.tools.vstar.util.notification.Listener;

/**
 * The application's toolbar.
 */
public class ToolBar extends JPanel {

	private Mediator mediator = Mediator.getInstance();

	private MenuBar menuBar;

	private Icon newStarFromFileIcon;
	private Icon newStarFromDatabaseIcon;
	private Icon infoIcon;
	private Icon saveIcon;
	private Icon printIcon;

	private Icon rawDataIcon;
	private Icon phasePlotIcon;

	private Icon zoomInIcon;
	private Icon zoomOutIcon;

	private Icon prefsIcon;
	private Icon helpContentsIcon;

	private JToolBar toolBar;

	private JButton newStarFromDatabaseButton;
	private JButton newStarFromFileButton;
	private JButton infoButton;
	private JButton saveButton;
	private JButton printButton;

	private JButton rawDataButton;
	private JButton phasePlotButton;

	private JButton zoomInButton;
	private JButton zoomOutButton;

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

		// Add event listeners.

		this.mediator.getProgressNotifier().addListener(
				createProgressListener());
	}

	// Helpers

	private void retrieveToolBarIcons() {
		// Create the toolbar icons.

		newStarFromFileIcon = ResourceAccessor
				.getIconResource("/toolbarButtonGraphics/general/New24.gif");
		newStarFromDatabaseIcon = ResourceAccessor
				.getIconResource("/toolbarButtonGraphics/general/Import24.gif");

		saveIcon = ResourceAccessor
				.getIconResource("/toolbarButtonGraphics/general/Save24.gif");
		printIcon = ResourceAccessor
				.getIconResource("/toolbarButtonGraphics/general/Print24.gif");

		infoIcon = ResourceAccessor
				.getIconResource("/toolbarButtonGraphics/general/Information24.gif");

		prefsIcon = ResourceAccessor
				.getIconResource("/toolbarButtonGraphics/general/Preferences24.gif");

		rawDataIcon = ResourceAccessor
				.getIconResource("/toolbarButtonGraphics/general/History24.gif");

		phasePlotIcon = ResourceAccessor
				.getIconResource("/toolbarButtonGraphics/general/Refresh24.gif");

		zoomInIcon = ResourceAccessor
				.getIconResource("/toolbarButtonGraphics/general/ZoomIn24.gif");

		zoomOutIcon = ResourceAccessor
				.getIconResource("/toolbarButtonGraphics/general/ZoomOut24.gif");

		helpContentsIcon = ResourceAccessor
				.getIconResource("/toolbarButtonGraphics/general/Help24.gif");

		if (newStarFromDatabaseIcon == null || newStarFromDatabaseIcon == null
				|| infoIcon == null || saveIcon == null || printIcon == null
				|| rawDataIcon == null || phasePlotIcon == null
				|| zoomInIcon == null || zoomOutIcon == null
				|| prefsIcon == null || helpContentsIcon == null) {
			
			MessageBox.showErrorDialog(MainFrame.getInstance(),
					"Resource Error",
					"Some icon resources are not available. Exiting.");
			
			System.exit(1);
		}
	}

	private void createToolbarButtons() {
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));

		newStarFromDatabaseButton = new JButton(newStarFromDatabaseIcon);
		newStarFromDatabaseButton
				.setToolTipText(MenuBar.NEW_STAR_FROM_DATABASE);
		newStarFromDatabaseButton.addActionListener(menuBar
				.createNewStarFromDatabaseListener());
		buttonPanel.add(newStarFromDatabaseButton);

		newStarFromFileButton = new JButton(newStarFromFileIcon);
		newStarFromFileButton.setToolTipText(MenuBar.NEW_STAR_FROM_FILE);
		newStarFromFileButton.addActionListener(menuBar
				.createNewStarFromFileListener());
		buttonPanel.add(newStarFromFileButton);

		buttonPanel.add(Box.createHorizontalStrut(10));

		saveButton = new JButton(saveIcon);
		saveButton.setToolTipText(MenuBar.SAVE);
		saveButton.addActionListener(menuBar.createSaveListener());
		saveButton.setEnabled(false);
		buttonPanel.add(saveButton);

		printButton = new JButton(printIcon);
		printButton.setToolTipText(MenuBar.PRINT);
		printButton.addActionListener(menuBar.createPrintListener());
		printButton.setEnabled(false);
		buttonPanel.add(printButton);

		buttonPanel.add(Box.createHorizontalStrut(10));

		infoButton = new JButton(infoIcon);
		infoButton.setToolTipText(MenuBar.INFO);
		infoButton.addActionListener(menuBar.createInfoListener());
		infoButton.setEnabled(false);
		buttonPanel.add(infoButton);

		buttonPanel.add(Box.createHorizontalStrut(10));

		rawDataButton = new JButton(rawDataIcon);
		rawDataButton.setToolTipText(MenuBar.RAW_DATA);
		rawDataButton.addActionListener(menuBar.createRawDataListener());
		rawDataButton.setEnabled(false);
		buttonPanel.add(rawDataButton);

		phasePlotButton = new JButton(phasePlotIcon);
		phasePlotButton.setToolTipText(MenuBar.PHASE_PLOT);
		phasePlotButton.addActionListener(menuBar.createPhasePlotListener());
		phasePlotButton.setEnabled(false);
		buttonPanel.add(phasePlotButton);

		buttonPanel.add(Box.createHorizontalStrut(10));

		zoomInButton = new JButton(zoomInIcon);
		zoomInButton.setToolTipText(MenuBar.ZOOM_IN);
		zoomInButton.addActionListener(menuBar.createZoomInListener());
		zoomInButton.setEnabled(false);
		buttonPanel.add(zoomInButton);

		zoomOutButton = new JButton(zoomOutIcon);
		zoomOutButton.setToolTipText(MenuBar.ZOOM_OUT);
		zoomOutButton.addActionListener(menuBar.createZoomOutListener());
		zoomOutButton.setEnabled(false);
		buttonPanel.add(zoomOutButton);

		buttonPanel.add(Box.createHorizontalStrut(10));

		prefsButton = new JButton(prefsIcon);
		prefsButton.setToolTipText(MenuBar.PREFS);
		prefsButton.addActionListener(menuBar.createPrefsListener());
		buttonPanel.add(prefsButton);

		buttonPanel.add(Box.createHorizontalStrut(10));

		helpContentsButton = new JButton(helpContentsIcon);
		helpContentsButton.setToolTipText(MenuBar.HELP_CONTENTS);
		helpContentsButton.addActionListener(menuBar
				.createHelpContentsListener());
		buttonPanel.add(helpContentsButton);

		toolBar.add(buttonPanel);
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
				case START_PROGRESS:
					setEnabledToolbarItems(false);
					break;
				case COMPLETE_PROGRESS:
					setEnabledToolbarItems(true);
					break;
				case CLEAR_PROGRESS:
					break;
				case INCREMENT_PROGRESS:
					break;
				}
			}

			public boolean canBeRemoved() {
				return false;
			}
		};
	}

	private void setEnabledToolbarItems(boolean state) {
		infoButton.setEnabled(state);
		saveButton.setEnabled(state);
		printButton.setEnabled(state);

		rawDataButton.setEnabled(state);
		phasePlotButton.setEnabled(state);
		
		zoomInButton.setEnabled(state);
		zoomOutButton.setEnabled(state);		
	}
}
