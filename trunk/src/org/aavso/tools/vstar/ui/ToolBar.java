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

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import org.aavso.tools.vstar.ui.dialog.MessageBox;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.message.ObservationSelectionMessage;
import org.aavso.tools.vstar.ui.mediator.message.ProgressInfo;
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

	private Icon polynomialFitIcon;

	private Icon obDetailsIcon;

	private Icon zoomInIcon;
	private Icon zoomOutIcon;

	private Icon panLeftIcon;
	private Icon panRightIcon;
	private Icon panUpIcon;
	private Icon panDownIcon;

	private Icon filterIcon;

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

	private JButton polynomialFitButton;

	private JButton obDetailsButton;

	private JButton zoomInButton;
	private JButton zoomOutButton;

	private JButton panLeftButton;
	private JButton panRightButton;
	private JButton panUpButton;
	private JButton panDownButton;

	private JButton filterButton;

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

		this.mediator.getObservationSelectionNotifier().addListener(
				createObservationSelectionListener());
	}

	// Helpers

	private void retrieveToolBarIcons() {
		// Create the toolbar icons.

		newStarFromDatabaseIcon = ResourceAccessor
				.getIconResource("/nico/toolbarIcons/_24_/NewStarFromDatabase.png");

		newStarFromFileIcon = ResourceAccessor
				.getIconResource("/nico/toolbarIcons/_24_/NewStarFromFile.png");

		saveIcon = ResourceAccessor
				.getIconResource("/nico/toolbarIcons/_24_/Save.png");
		printIcon = ResourceAccessor
				.getIconResource("/nico/toolbarIcons/_24_/Print.png");

		infoIcon = ResourceAccessor
				.getIconResource("/nico/toolbarIcons/_24_/Information.png");

		prefsIcon = ResourceAccessor
				.getIconResource("/nico/toolbarIcons/_24_/Preferences.png");

		rawDataIcon = ResourceAccessor
				.getIconResource("/nico/toolbarIcons/_24_/RawView.png");

		phasePlotIcon = ResourceAccessor
				.getIconResource("/nico/toolbarIcons/_24_/PhasePlotView.png");

		polynomialFitIcon = ResourceAccessor
				.getIconResource("/nico/toolbarIcons/_24_/PolynomialFit.png");

		obDetailsIcon = ResourceAccessor
				.getIconResource("/nico/toolbarIcons/_24_/Details.png");

		zoomInIcon = ResourceAccessor
				.getIconResource("/nico/toolbarIcons/_24_/ZoomIn.png");

		zoomOutIcon = ResourceAccessor
				.getIconResource("/nico/toolbarIcons/_24_/ZoomOut.png");

		panLeftIcon = ResourceAccessor
				.getIconResource("/nico/toolbarIcons/_24_/LeftArrow.png");

		panRightIcon = ResourceAccessor
				.getIconResource("/nico/toolbarIcons/_24_/RightArrow.png");

		panUpIcon = ResourceAccessor
				.getIconResource("/nico/toolbarIcons/_24_/UpArrow.png");

		panDownIcon = ResourceAccessor
				.getIconResource("/nico/toolbarIcons/_24_/DownArrow.png");

		filterIcon = ResourceAccessor
				.getIconResource("/nico/toolbarIcons/_24_/Find.png");

		helpContentsIcon = ResourceAccessor
				.getIconResource("/nico/toolbarIcons/_24_/Help.png");

		if (newStarFromDatabaseIcon == null || newStarFromDatabaseIcon == null
				|| infoIcon == null || saveIcon == null || printIcon == null
				|| rawDataIcon == null || phasePlotIcon == null
				|| polynomialFitIcon == null || zoomInIcon == null
				|| obDetailsIcon == null || zoomOutIcon == null
				|| panLeftIcon == null || panRightIcon == null
				|| panUpIcon == null || panDownIcon == null
				|| filterIcon == null || prefsIcon == null
				|| helpContentsIcon == null) {

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
		newStarFromDatabaseButton.setBorder(BorderFactory.createEmptyBorder());
		buttonPanel.add(newStarFromDatabaseButton);

		newStarFromFileButton = new JButton(newStarFromFileIcon);
		newStarFromFileButton.setToolTipText(MenuBar.NEW_STAR_FROM_FILE);
		newStarFromFileButton.addActionListener(menuBar
				.createNewStarFromFileListener());
		newStarFromFileButton.setBorder(BorderFactory.createEmptyBorder());
		buttonPanel.add(newStarFromFileButton);

		buttonPanel.add(Box.createHorizontalStrut(10));

		saveButton = new JButton(saveIcon);
		saveButton.setToolTipText(MenuBar.SAVE);
		saveButton.addActionListener(menuBar.createSaveListener());
		saveButton.setEnabled(false);
		saveButton.setBorder(BorderFactory.createEmptyBorder());
		buttonPanel.add(saveButton);

		printButton = new JButton(printIcon);
		printButton.setToolTipText(MenuBar.PRINT);
		printButton.addActionListener(menuBar.createPrintListener());
		printButton.setEnabled(false);
		printButton.setBorder(BorderFactory.createEmptyBorder());
		buttonPanel.add(printButton);

		buttonPanel.add(Box.createHorizontalStrut(10));

		infoButton = new JButton(infoIcon);
		infoButton.setToolTipText(MenuBar.INFO);
		infoButton.addActionListener(menuBar.createInfoListener());
		infoButton.setEnabled(false);
		infoButton.setBorder(BorderFactory.createEmptyBorder());
		buttonPanel.add(infoButton);

		obDetailsButton = new JButton(obDetailsIcon);
		obDetailsButton.setToolTipText(MenuBar.OB_DETAILS);
		obDetailsButton.addActionListener(menuBar.createObDetailsListener());
		obDetailsButton.setEnabled(false);
		obDetailsButton.setBorder(BorderFactory.createEmptyBorder());
		buttonPanel.add(obDetailsButton);

		buttonPanel.add(Box.createHorizontalStrut(10));

		rawDataButton = new JButton(rawDataIcon);
		rawDataButton.setToolTipText(MenuBar.RAW_DATA);
		rawDataButton.addActionListener(menuBar.createRawDataListener());
		rawDataButton.setEnabled(false);
		rawDataButton.setBorder(BorderFactory.createEmptyBorder());
		buttonPanel.add(rawDataButton);

		phasePlotButton = new JButton(phasePlotIcon);
		phasePlotButton.setToolTipText(MenuBar.PHASE_PLOT);
		phasePlotButton.addActionListener(menuBar.createPhasePlotListener());
		phasePlotButton.setEnabled(false);
		phasePlotButton.setBorder(BorderFactory.createEmptyBorder());
		buttonPanel.add(phasePlotButton);

		buttonPanel.add(Box.createHorizontalStrut(10));

		polynomialFitButton = new JButton(polynomialFitIcon);
		polynomialFitButton.setToolTipText(MenuBar.POLYNOMIAL_FIT);
		polynomialFitButton.addActionListener(menuBar
				.createPolynomialFitListener());
		polynomialFitButton.setEnabled(false);
		polynomialFitButton.setBorder(BorderFactory.createEmptyBorder());
		buttonPanel.add(polynomialFitButton);

		buttonPanel.add(Box.createHorizontalStrut(10));

		zoomInButton = new JButton(zoomInIcon);
		zoomInButton.setToolTipText(MenuBar.ZOOM_IN);
		zoomInButton.addActionListener(menuBar.createZoomInListener());
		zoomInButton.setEnabled(false);
		zoomInButton.setBorder(BorderFactory.createEmptyBorder());
		buttonPanel.add(zoomInButton);

		zoomOutButton = new JButton(zoomOutIcon);
		zoomOutButton.setToolTipText(MenuBar.ZOOM_OUT);
		zoomOutButton.addActionListener(menuBar.createZoomOutListener());
		zoomOutButton.setEnabled(false);
		zoomOutButton.setBorder(BorderFactory.createEmptyBorder());
		buttonPanel.add(zoomOutButton);

		buttonPanel.add(Box.createHorizontalStrut(10));

		panLeftButton = new JButton(panLeftIcon);
		panLeftButton.setToolTipText(MenuBar.PAN_LEFT);
		panLeftButton.addActionListener(menuBar.createPanLeftListener());
		panLeftButton.setEnabled(false);
		panLeftButton.setBorder(BorderFactory.createEmptyBorder());
		buttonPanel.add(panLeftButton);

		panRightButton = new JButton(panRightIcon);
		panRightButton.setToolTipText(MenuBar.PAN_LEFT);
		panRightButton.addActionListener(menuBar.createPanRightListener());
		panRightButton.setEnabled(false);
		panRightButton.setBorder(BorderFactory.createEmptyBorder());
		buttonPanel.add(panRightButton);

		panUpButton = new JButton(panUpIcon);
		panUpButton.setToolTipText(MenuBar.PAN_UP);
		panUpButton.addActionListener(menuBar.createPanUpListener());
		panUpButton.setEnabled(false);
		panUpButton.setBorder(BorderFactory.createEmptyBorder());
		buttonPanel.add(panUpButton);

		panDownButton = new JButton(panDownIcon);
		panDownButton.setToolTipText(MenuBar.PAN_DOWN);
		panDownButton.addActionListener(menuBar.createPanDownListener());
		panDownButton.setEnabled(false);
		panDownButton.setBorder(BorderFactory.createEmptyBorder());
		buttonPanel.add(panDownButton);

		buttonPanel.add(Box.createHorizontalStrut(10));

		filterButton = new JButton(filterIcon);
		filterButton.setToolTipText(MenuBar.FILTER);
		filterButton.addActionListener(menuBar.createFilterListener());
		filterButton.setEnabled(false);
		filterButton.setBorder(BorderFactory.createEmptyBorder());
		buttonPanel.add(filterButton);

		buttonPanel.add(Box.createHorizontalStrut(10));

		prefsButton = new JButton(prefsIcon);
		prefsButton.setToolTipText(MenuBar.PREFS);
		prefsButton.addActionListener(menuBar.createPrefsListener());
		prefsButton.setBorder(BorderFactory.createEmptyBorder());
		buttonPanel.add(prefsButton);

		buttonPanel.add(Box.createHorizontalStrut(10));

		helpContentsButton = new JButton(helpContentsIcon);
		helpContentsButton.setToolTipText(MenuBar.HELP_CONTENTS);
		helpContentsButton.addActionListener(menuBar
				.createHelpContentsListener());
		helpContentsButton.setBorder(BorderFactory.createEmptyBorder());
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

		polynomialFitButton.setEnabled(state);

		panLeftButton.setEnabled(state);
		panRightButton.setEnabled(state);
		panUpButton.setEnabled(state);
		panDownButton.setEnabled(state);

		filterButton.setEnabled(state);
	}

	// Returns an observation selection listener that sets enables certain
	// buttons.
	public Listener<ObservationSelectionMessage> createObservationSelectionListener() {
		return new Listener<ObservationSelectionMessage>() {
			@Override
			public void update(ObservationSelectionMessage info) {
				obDetailsButton.setEnabled(true);
				zoomInButton.setEnabled(true);
				zoomOutButton.setEnabled(true);
			}

			@Override
			public boolean canBeRemoved() {
				return false;
			}
		};
	}
}
