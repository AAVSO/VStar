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

import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import org.aavso.tools.vstar.ui.dialog.AboutBox;
import org.aavso.tools.vstar.ui.dialog.HelpContentsDialog;
import org.aavso.tools.vstar.ui.dialog.InfoDialog;
import org.aavso.tools.vstar.ui.dialog.MessageBox;
import org.aavso.tools.vstar.ui.dialog.StarSelectorDialog;
import org.aavso.tools.vstar.ui.mediator.AnalysisType;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.NewStarMessage;
import org.aavso.tools.vstar.ui.mediator.ProgressInfo;
import org.aavso.tools.vstar.util.notification.Listener;

/**
 * VStar's menu bar.
 * 
 * TODO: - Factor out code to be shared by menu and tool bar?
 */
public class MenuBar extends JMenuBar implements Listener<NewStarMessage> {

	// File menu item names.
	public static final String NEW_STAR_FROM_DATABASE = "New Star from AAVSO Database...";
	public static final String NEW_STAR_FROM_FILE = "New Star from File...";	
	public static final String SAVE = "Save...";
	public static final String PRINT = "Print...";
	public static final String INFO = "Info...";
	public static final String PREFS = "Preferences...";
	public static final String QUIT = "Quit";
	
	// Analysis menu item names.
	public static final String RAW_DATA = "Raw Data";
	public static final String PHASE_PLOT = "Phase Plot...";
	public static final String PERIOD_SEARCH = "Period Search...";
	
	// Help menu item names.
	public static final String HELP_CONTENTS = "Help Contents...";
	public static final String ABOUT = "About...";
	
	private Mediator mediator = Mediator.getInstance();

	private JFileChooser fileOpenDialog;

	// The parent window.
	private MainFrame parent;

	// Menu items.
	
	// File menu.
	JMenuItem fileNewStarFromDatabaseItem;
	JMenuItem fileNewStarFromFileItem;
	JMenuItem fileSaveItem;
	JMenuItem filePrintItem;
	JMenuItem fileInfoItem;
	JMenuItem filePrefsItem;
	JMenuItem fileQuitItem;

	// Analysis menu.
	JCheckBoxMenuItem analysisRawDataItem;
	JCheckBoxMenuItem analysisPhasePlotItem;
	JCheckBoxMenuItem analysisPeriodSearchItem;

	// Help menu.
	JMenuItem helpContentsItem;
	JMenuItem helpAboutItem;

	// New star message.
	private NewStarMessage newStarMessage;
	
	/**
	 * Constructor
	 */
	public MenuBar(MainFrame parent) {
		super();

		this.parent = parent;

		List<String> extensions = new ArrayList<String>();
		extensions.add("csv");
		extensions.add("tsv");
		extensions.add("txt");

		this.fileOpenDialog = new JFileChooser();
		this.fileOpenDialog.setFileFilter(new FileExtensionFilter(extensions));

		createFileMenu();
		createAnalysisMenu();
		createHelpMenu();

		this.newStarMessage = null;
		
		this.mediator.getProgressNotifier().addListener(
				createProgressListener());
		
		this.mediator.getNewStarNotifier().addListener(this);
	}

	private void createFileMenu() {
		JMenu fileMenu = new JMenu("File");

		fileNewStarFromDatabaseItem = new JMenuItem(NEW_STAR_FROM_DATABASE);
		fileNewStarFromDatabaseItem
				.addActionListener(createNewStarFromDatabaseListener());
		fileMenu.add(fileNewStarFromDatabaseItem);

		fileNewStarFromFileItem = new JMenuItem(NEW_STAR_FROM_FILE);
		fileNewStarFromFileItem
				.addActionListener(createNewStarFromFileListener());
		fileMenu.add(fileNewStarFromFileItem);

		fileMenu.addSeparator();

		fileSaveItem = new JMenuItem(SAVE);
		fileSaveItem.addActionListener(this.createSaveListener());
		fileSaveItem.setEnabled(false);
		fileMenu.add(fileSaveItem);

		filePrintItem = new JMenuItem(PRINT);
		filePrintItem.addActionListener(this.createPrintListener());
		filePrintItem.setEnabled(false);
		fileMenu.add(filePrintItem);

		fileMenu.addSeparator();

		fileInfoItem = new JMenuItem(INFO);
		fileInfoItem.addActionListener(this.createInfoListener());
		fileMenu.add(fileInfoItem);

		fileMenu.addSeparator();

		filePrefsItem = new JMenuItem(PREFS);
		filePrefsItem.addActionListener(this.createPrefsListener());
		fileMenu.add(filePrefsItem);

		fileMenu.addSeparator();

		fileQuitItem = new JMenuItem(QUIT, KeyEvent.VK_Q);
		// fileQuitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,
		// ActionEvent.META_MASK));
		fileQuitItem.addActionListener(createQuitListener());
		fileMenu.add(fileQuitItem);

		this.add(fileMenu);
	}

	private void createAnalysisMenu() {
		JMenu analysisMenu = new JMenu("Analysis");

		analysisRawDataItem = new JCheckBoxMenuItem(RAW_DATA);
		analysisRawDataItem.setEnabled(false);
		analysisRawDataItem.addActionListener(createRawDataListener());
		analysisMenu.add(analysisRawDataItem);

		analysisPhasePlotItem = new JCheckBoxMenuItem(PHASE_PLOT);
		analysisPhasePlotItem.setEnabled(false);
		analysisPhasePlotItem.addActionListener(createPhasePlotListener());
		analysisMenu.add(analysisPhasePlotItem);

		analysisPeriodSearchItem = new JCheckBoxMenuItem(PERIOD_SEARCH);
		analysisPeriodSearchItem.setEnabled(false);
		analysisPeriodSearchItem
				.addActionListener(createPeriodSearchListener());
		analysisMenu.add(analysisPeriodSearchItem);

		this.add(analysisMenu);
	}

	private void createHelpMenu() {
		JMenu helpMenu = new JMenu("Help");

		helpContentsItem = new JMenuItem(HELP_CONTENTS, KeyEvent.VK_H);
		helpContentsItem.addActionListener(createHelpContentsListener());
		helpMenu.add(helpContentsItem);

		helpMenu.addSeparator();

		helpAboutItem = new JMenuItem(ABOUT, KeyEvent.VK_A);
		helpAboutItem.addActionListener(createAboutListener());
		helpMenu.add(helpAboutItem);

		this.add(helpMenu);
	}

	/**
	 * Returns the action listener to be invoked for File->New Star from AAVSO
	 * Database...
	 * 
	 * The action is to: a. ask the user for star and date range details; b.
	 * open a database connection and get the data for star in that range; c.
	 * create the corresponding observation models and GUI elements.
	 */
	public ActionListener createNewStarFromDatabaseListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					// Prompt user for star and JD range selection.
					MainFrame.getInstance().getStatusPane().setMessage(
							"Select a star...");
					StarSelectorDialog starSelectorDialog = StarSelectorDialog
							.getInstance();
					starSelectorDialog.showDialog();

					if (!starSelectorDialog.isCancelled()) {
						String starName = starSelectorDialog.getStarName();
						String auid = starSelectorDialog.getAuid();
						double minJD = starSelectorDialog.getMinDate()
								.getJulianDay();
						double maxJD = starSelectorDialog.getMaxDate()
								.getJulianDay();

						mediator.createObservationArtefactsFromDatabase(
								starName, auid, minJD, maxJD);
					} else {
						MainFrame.getInstance().getStatusPane().setMessage("");
					}
				} catch (Exception ex) {
					MessageBox.showErrorDialog(MainFrame.getInstance(),
							"Star Selection", ex);
				}
			}
		};
	}

	/**
	 * Returns the action listener to be invoked for File->New Star from File...
	 * 
	 * The action is to open a file dialog to allow the user to select a single
	 * file.
	 */
	public ActionListener createNewStarFromFileListener() {
		final JFileChooser fileOpenDialog = this.fileOpenDialog;
		final MainFrame parent = this.parent;

		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int returnVal = fileOpenDialog.showOpenDialog(parent);

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File f = fileOpenDialog.getSelectedFile();

					try {
						mediator.createObservationArtefactsFromFile(f, parent);
					} catch (Exception ex) {
						MessageBox.showErrorDialog(parent, NEW_STAR_FROM_FILE,
								ex);
					}
				}
			}
		};
	}

	/**
	 * Returns the action listener to be invoked for File->Save...
	 */
	public ActionListener createSaveListener() {
		final Component parent = this.parent;
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mediator.saveCurrentMode(parent);
			}
		};
	}

	/**
	 * Returns the action listener to be invoked for File->Print...
	 */
	public ActionListener createPrintListener() {
		final Component parent = this.parent;
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mediator.printCurrentMode(parent);
			}
		};
	}

	/**
	 * Returns the action listener to be invoked for File->Info...
	 */
	public ActionListener createInfoListener() {
		final MenuBar self = this;
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new InfoDialog(self.newStarMessage);
			}
		};
	}

	/**
	 * Returns the action listener to be invoked for File->Preferences...
	 */
	public ActionListener createPrefsListener() {
		final Component parent = this.parent;
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MessageBox.showMessageDialog(parent, "Preferences...",
						Mediator.NOT_IMPLEMENTED_YET);
			}
		};
	}

	/**
	 * Returns the action listener to be invoked for File->Quit
	 */
	private ActionListener createQuitListener() {
		return new ActionListener() {
			// TODO: do other cleanup, e.g. if file needs saving;
			// need a document model including undo for this;
			// defer to Mediator.
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		};
	}

	/**
	 * Returns the action listener to be invoked for Analysis->Raw Data
	 */
	public ActionListener createRawDataListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AnalysisType type = mediator
						.changeAnalysisType(AnalysisType.RAW_DATA);
				if (type == AnalysisType.RAW_DATA) {
					setRawDataAnalysisMenuItemState(true);
					setPhasePlotAnalysisMenuItemState(false);
					setPeriodSearchAnalysisMenuItemState(false);
				}
			}
		};
	}

	/**
	 * Returns the action listener to be invoked for Analysis->Phase Plot
	 */
	public ActionListener createPhasePlotListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AnalysisType type = mediator
						.changeAnalysisType(AnalysisType.PHASE_PLOT);
				if (type == AnalysisType.PHASE_PLOT) {
					setRawDataAnalysisMenuItemState(false);
					setPhasePlotAnalysisMenuItemState(true);
					setPeriodSearchAnalysisMenuItemState(false);
				} else {
					setRawDataAnalysisMenuItemState(true);
					setPhasePlotAnalysisMenuItemState(false);
					setPeriodSearchAnalysisMenuItemState(false);
				}
			}
		};
	}

	/**
	 * Returns the action listener to be invoked for Analysis->Period Search
	 */
	public ActionListener createPeriodSearchListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// TODO: change when enabled!
				setRawDataAnalysisMenuItemState(true);
				setPhasePlotAnalysisMenuItemState(false);
				setPeriodSearchAnalysisMenuItemState(false);

				MessageBox.showMessageDialog(MainFrame.getInstance(),
						PERIOD_SEARCH, Mediator.NOT_IMPLEMENTED_YET);
			}
		};
	}

	/**
	 * Returns the action listener to be invoked for Help->Help Contents...
	 */
	public ActionListener createHelpContentsListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				javax.swing.SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						HelpContentsDialog helpContentsDialog = new HelpContentsDialog();
						helpContentsDialog.pack();
						helpContentsDialog.setVisible(true);
					}
				});
			}
		};
	}

	/**
	 * Returns the action listener to be invoked for Help->About...
	 * 
	 * TODO: Make a separate component for the About Box. Put text into a
	 * resource file and use a JEditorPane to render HTML or use a JDialog with
	 * JLabels and images.
	 */
	private ActionListener createAboutListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AboutBox.showAboutBox(parent);
			}
		};
	}

	/**
	 * Return a progress listener.
	 * TODO: why are we doing the wait-cursor handling here rather than 
	 * in MainFrame?
	 */
	private Listener<ProgressInfo> createProgressListener() {
		final MainFrame parent = this.parent;
		return new Listener<ProgressInfo>() {
			public void update(ProgressInfo info) {
				switch (info.getType()) {
				case MIN_PROGRESS:
					break;
				case MAX_PROGRESS:
					break;
				case RESET_PROGRESS:
					parent.setCursor(Cursor
							.getPredefinedCursor(Cursor.WAIT_CURSOR));
					setEnabledFileAndAnalysisMenuItems(false);
					break;
				case COMPLETE_PROGRESS:
					parent.setCursor(null); // turn off the wait cursor
					setEnabledFileAndAnalysisMenuItems(true);
					break;
				case INCREMENT_PROGRESS:
					break;
				}
			}
		};
	}

	/**
	 * New star listener.
	 */
	public void update(NewStarMessage msg) {
		this.newStarMessage = msg;
	}
	
	// Enables or disabled File and Analysis menu items.
	private void setEnabledFileAndAnalysisMenuItems(boolean state) {
		this.fileNewStarFromDatabaseItem.setEnabled(state);
		this.fileNewStarFromFileItem.setEnabled(state);
		this.fileSaveItem.setEnabled(state);
		this.filePrintItem.setEnabled(state);
		this.fileInfoItem.setEnabled(state);
		
		this.analysisRawDataItem.setEnabled(state);
		this.analysisPhasePlotItem.setEnabled(state);
		this.analysisPeriodSearchItem.setEnabled(state);

		AnalysisType type = mediator.getAnalysisType();

		switch (type) {
		case RAW_DATA:
			setRawDataAnalysisMenuItemState(true);
			setPhasePlotAnalysisMenuItemState(false);
			setPeriodSearchAnalysisMenuItemState(false);
			break;
		case PHASE_PLOT:
			setRawDataAnalysisMenuItemState(false);
			setPhasePlotAnalysisMenuItemState(true);
			setPeriodSearchAnalysisMenuItemState(false);
			break;
		case PERIOD_SEARCH:
			setRawDataAnalysisMenuItemState(false);
			setPhasePlotAnalysisMenuItemState(false);
			setPeriodSearchAnalysisMenuItemState(true);
			break;
		}
	}

	private void setRawDataAnalysisMenuItemState(boolean state) {
		this.analysisRawDataItem.setState(state);
	}

	private void setPhasePlotAnalysisMenuItemState(boolean state) {
		this.analysisPhasePlotItem.setState(state);
	}

	private void setPeriodSearchAnalysisMenuItemState(boolean state) {
		this.analysisPeriodSearchItem.setState(state);
	}
}
