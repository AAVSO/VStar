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
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import org.aavso.tools.vstar.plugin.CustomFilterPluginBase;
import org.aavso.tools.vstar.plugin.GeneralToolPluginBase;
import org.aavso.tools.vstar.plugin.ObservationSourcePluginBase;
import org.aavso.tools.vstar.plugin.ObservationToolPluginBase;
import org.aavso.tools.vstar.plugin.period.PeriodAnalysisPluginBase;
import org.aavso.tools.vstar.ui.dialog.AboutBox;
import org.aavso.tools.vstar.ui.dialog.FileExtensionFilter;
import org.aavso.tools.vstar.ui.dialog.HelpContentsDialog;
import org.aavso.tools.vstar.ui.dialog.InfoDialog;
import org.aavso.tools.vstar.ui.dialog.MessageBox;
import org.aavso.tools.vstar.ui.dialog.StarSelectorDialog;
import org.aavso.tools.vstar.ui.dialog.prefs.PreferencesDialog;
import org.aavso.tools.vstar.ui.mediator.AnalysisType;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.message.AnalysisTypeChangeMessage;
import org.aavso.tools.vstar.ui.mediator.message.FilteredObservationMessage;
import org.aavso.tools.vstar.ui.mediator.message.NewStarMessage;
import org.aavso.tools.vstar.ui.mediator.message.ObservationSelectionMessage;
import org.aavso.tools.vstar.ui.mediator.message.PanRequestMessage;
import org.aavso.tools.vstar.ui.mediator.message.PanType;
import org.aavso.tools.vstar.ui.mediator.message.ProgressInfo;
import org.aavso.tools.vstar.ui.mediator.message.ZoomRequestMessage;
import org.aavso.tools.vstar.ui.mediator.message.ZoomType;
import org.aavso.tools.vstar.ui.resources.PluginLoader;
import org.aavso.tools.vstar.util.notification.Listener;

/**
 * VStar's menu bar.
 */
public class MenuBar extends JMenuBar {

	// File menu item names.
	public static final String NEW_STAR_FROM_DATABASE = "New Star from AAVSO Database...";
	public static final String NEW_STAR_FROM_FILE = "New Star from File...";
	public static final String SAVE = "Save...";
	public static final String PRINT = "Print...";
	public static final String INFO = "Info...";
	public static final String PREFS = "Preferences...";
	public static final String QUIT = "Quit";

	// View menu item names.
	public static final String OB_DETAILS = "Observation Details...";
	public static final String ZOOM_IN = "Zoom In";
	public static final String ZOOM_OUT = "Zoom Out";
	public static final String ZOOM_TO_FIT = "Zoom To Fit";
	public static final String PAN_LEFT = "Pan Left";
	public static final String PAN_RIGHT = "Pan Right";
	public static final String PAN_UP = "Pan Up";
	public static final String PAN_DOWN = "Pan Down";
	public static final String FILTER = "Filter...";
	public static final String NO_FILTER = "No Filter";

	// Analysis menu item names.
	public static final String RAW_DATA = "Raw Data";
	public static final String PHASE_PLOT = "Phase Plot...";
	public static final String POLYNOMIAL_FIT = "Polynomial Fit...";

	// Help menu item names.
	public static final String HELP_CONTENTS = "Help Contents...";
	public static final String ABOUT = "About...";

	private Mediator mediator = Mediator.getInstance();

	private JFileChooser fileOpenDialog;

	// Plug-in menu name to plug-in object maps.
	private Map<String, ObservationSourcePluginBase> menuItemNameToObSourcePlugin;
	private Map<String, CustomFilterPluginBase> menuItemNameToCustomFilterPlugin;
	private Map<String, PeriodAnalysisPluginBase> menuItemNameToPeriodAnalysisPlugin;
	private Map<String, ObservationToolPluginBase> menuItemNameToObsToolPlugin;
	private Map<String, GeneralToolPluginBase> menuItemNameToGenToolPlugin;

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

	// View menu.
	JMenuItem viewObDetailsItem;
	JMenuItem viewZoomInItem;
	JMenuItem viewZoomOutItem;
	JMenuItem viewZoomToFitItem;
	JMenuItem viewPanLeftItem;
	JMenuItem viewPanRightItem;
	JMenuItem viewPanUpItem;
	JMenuItem viewPanDownItem;
	JMenuItem viewFilterItem;
	JMenuItem viewNoFilterItem;

	JMenu viewCustomFilterMenu;

	// Analysis menu.
	JCheckBoxMenuItem analysisRawDataItem;
	JCheckBoxMenuItem analysisPhasePlotItem;

	JMenu analysisPeriodSearchMenu;

	JMenuItem analysisPolynomialFitItem;

	// Tool menu.
	JMenu toolMenu;

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
		createViewMenu();
		createAnalysisMenu();
		createToolMenu();
		createHelpMenu();

		this.newStarMessage = null;

		// Listen to events

		this.mediator.getProgressNotifier().addListener(
				createProgressListener());

		this.mediator.getNewStarNotifier().addListener(createNewStarListener());

		this.mediator.getAnalysisTypeChangeNotifier().addListener(
				createAnalysisTypeChangeListener());

		this.mediator.getObservationSelectionNotifier().addListener(
				createObservationSelectionListener());
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

		List<ObservationSourcePluginBase> obSourcePlugins = PluginLoader
				.getObservationSourcePlugins();

		if (!obSourcePlugins.isEmpty()) {
			menuItemNameToObSourcePlugin = new TreeMap<String, ObservationSourcePluginBase>();

			ActionListener obSourceListener = createObservationSourceListener();

			for (ObservationSourcePluginBase plugin : obSourcePlugins) {
				String itemName = plugin.getDisplayName();

				JMenuItem obSourceMenuItem = new JMenuItem(itemName);
				obSourceMenuItem.addActionListener(obSourceListener);
				fileMenu.add(obSourceMenuItem);

				menuItemNameToObSourcePlugin.put(itemName, plugin);
			}

			fileMenu.addSeparator();
		}

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
		fileInfoItem.setEnabled(false);
		fileMenu.add(fileInfoItem);

		fileMenu.addSeparator();

		filePrefsItem = new JMenuItem(PREFS);
		filePrefsItem.addActionListener(this.createPrefsListener());
		fileMenu.add(filePrefsItem);

		// Mac OS X applications don't have Quit item in File menu,
		// but in application (VStar) menu. See also VStar.java.
		String os_name = System.getProperty("os.name");
		if (!os_name.startsWith("Mac OS X")) {
			fileMenu.addSeparator();

			fileQuitItem = new JMenuItem(QUIT, KeyEvent.VK_Q);
			// fileQuitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,
			// ActionEvent.META_MASK));
			fileQuitItem.addActionListener(createQuitListener());
			fileMenu.add(fileQuitItem);
		}

		this.add(fileMenu);
	}

	private void createViewMenu() {
		JMenu viewMenu = new JMenu("View");

		viewObDetailsItem = new JMenuItem(OB_DETAILS);
		viewObDetailsItem.setEnabled(false);
		viewObDetailsItem.addActionListener(createObDetailsListener());
		viewMenu.add(viewObDetailsItem);

		viewMenu.addSeparator();

		viewZoomInItem = new JMenuItem(ZOOM_IN);
		viewZoomInItem.setEnabled(false);
		viewZoomInItem.addActionListener(createZoomInListener());
		viewMenu.add(viewZoomInItem);

		viewZoomOutItem = new JMenuItem(ZOOM_OUT);
		viewZoomOutItem.setEnabled(false);
		viewZoomOutItem.addActionListener(createZoomOutListener());
		viewMenu.add(viewZoomOutItem);

		viewZoomToFitItem = new JMenuItem(ZOOM_TO_FIT);
		viewZoomToFitItem.setEnabled(false);
		viewZoomToFitItem.addActionListener(createZoomToFitListener());
		// viewMenu.add(viewZoomToFitItem);

		viewMenu.addSeparator();

		viewPanLeftItem = new JMenuItem(PAN_LEFT);
		viewPanLeftItem.setEnabled(false);
		viewPanLeftItem.addActionListener(createPanLeftListener());
		viewMenu.add(viewPanLeftItem);

		viewPanRightItem = new JMenuItem(PAN_RIGHT);
		viewPanRightItem.setEnabled(false);
		viewPanRightItem.addActionListener(createPanRightListener());
		viewMenu.add(viewPanRightItem);

		viewPanUpItem = new JMenuItem(PAN_UP);
		viewPanUpItem.setEnabled(false);
		viewPanUpItem.addActionListener(createPanUpListener());
		viewMenu.add(viewPanUpItem);

		viewPanDownItem = new JMenuItem(PAN_DOWN);
		viewPanDownItem.setEnabled(false);
		viewPanDownItem.addActionListener(createPanDownListener());
		viewMenu.add(viewPanDownItem);

		viewMenu.addSeparator();

		viewFilterItem = new JMenuItem(FILTER);
		viewFilterItem.setEnabled(false);
		viewFilterItem.addActionListener(createFilterListener());
		viewMenu.add(viewFilterItem);

		viewCustomFilterMenu = new JMenu("Custom Filters");
		viewCustomFilterMenu.setEnabled(false);

		ActionListener customFilterListener = createCustomFilterListener();

		menuItemNameToCustomFilterPlugin = new TreeMap<String, CustomFilterPluginBase>();

		for (CustomFilterPluginBase plugin : PluginLoader
				.getCustomFilterPlugins()) {
			String itemName = plugin.getDisplayName() + "...";

			JMenuItem customFilterMenuItem = new JMenuItem(itemName);
			customFilterMenuItem.addActionListener(customFilterListener);
			viewCustomFilterMenu.add(customFilterMenuItem);

			menuItemNameToCustomFilterPlugin.put(itemName, plugin);
		}

		viewMenu.add(viewCustomFilterMenu);

		viewNoFilterItem = new JMenuItem(NO_FILTER);
		viewNoFilterItem.setEnabled(false);
		viewNoFilterItem.addActionListener(createShowAllListener());
		viewMenu.add(viewNoFilterItem);

		this.add(viewMenu);
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

		analysisMenu.addSeparator();

		analysisPeriodSearchMenu = new JMenu("Period Search");
		analysisPeriodSearchMenu.setEnabled(false);

		ActionListener periodSearchListener = createPeriodSearchListener();

		menuItemNameToPeriodAnalysisPlugin = new TreeMap<String, PeriodAnalysisPluginBase>();

		for (PeriodAnalysisPluginBase plugin : PluginLoader
				.getPeriodAnalysisPlugins()) {
			String itemName = plugin.getDisplayName() + "...";

			JMenuItem analysisPeriodSearchItem = new JMenuItem(itemName);
			analysisPeriodSearchItem.addActionListener(periodSearchListener);
			analysisPeriodSearchMenu.add(analysisPeriodSearchItem);

			menuItemNameToPeriodAnalysisPlugin.put(itemName, plugin);
		}

		analysisMenu.add(analysisPeriodSearchMenu);

		analysisMenu.addSeparator();

		analysisPolynomialFitItem = new JMenuItem(POLYNOMIAL_FIT);
		analysisPolynomialFitItem.setEnabled(false);
		analysisPolynomialFitItem
				.addActionListener(createPolynomialFitListener());
		analysisMenu.add(analysisPolynomialFitItem);

		this.add(analysisMenu);
	}

	private void createToolMenu() {
		toolMenu = new JMenu("Tool");
		// toolMenu.setEnabled(false);

		ActionListener obsToolMenuItemListener = createObsToolMenuItemListener();

		menuItemNameToObsToolPlugin = new TreeMap<String, ObservationToolPluginBase>();

		for (ObservationToolPluginBase plugin : PluginLoader
				.getObservationToolPlugins()) {
			String itemName = plugin.getDisplayName() + "...";

			JMenuItem toolMenuItem = new JMenuItem(itemName);
			toolMenuItem.addActionListener(obsToolMenuItemListener);
			toolMenu.add(toolMenuItem);

			menuItemNameToObsToolPlugin.put(itemName, plugin);
		}

		List<GeneralToolPluginBase> genToolPlugins = PluginLoader
				.getGeneralToolPlugins();

		if (!genToolPlugins.isEmpty()) {
			toolMenu.addSeparator();

			ActionListener genToolMenuItemListener = createGenToolMenuItemListener();

			menuItemNameToGenToolPlugin = new TreeMap<String, GeneralToolPluginBase>();

			for (GeneralToolPluginBase plugin : genToolPlugins) {
				String itemName = plugin.getDisplayName() + "...";

				JMenuItem toolMenuItem = new JMenuItem(itemName);
				toolMenuItem.addActionListener(genToolMenuItemListener);
				toolMenu.add(toolMenuItem);

				menuItemNameToGenToolPlugin.put(itemName, plugin);
			}
		}

		this.add(toolMenu);
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

	// ** File Menu listeners **

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
						double minJD, maxJD;
						// TODO: consider doing these value mods in the dialog
						// getters to make this code more declarative.
						if (!starSelectorDialog.wantAllData()) {
							minJD = starSelectorDialog.getMinDate()
									.getJulianDay();
							maxJD = starSelectorDialog.getMaxDate()
									.getJulianDay();
						} else {
							minJD = 0;
							maxJD = Double.MAX_VALUE;
						}

						mediator.createObservationArtefactsFromDatabase(
								starName, auid, minJD, maxJD);
					} else {
						MainFrame.getInstance().getStatusPane().setMessage("");
					}
				} catch (Exception ex) {
					completeProgress();
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
						mediator.createObservationArtefactsFromFile(f);
					} catch (Exception ex) {
						MessageBox.showErrorDialog(parent, NEW_STAR_FROM_FILE,
								ex);
					}
				}
			}
		};
	}

	/**
	 * Returns the action listener to be invoked for observation source menu
	 * item selections.
	 */
	public ActionListener createObservationSourceListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String item = e.getActionCommand();
				ObservationSourcePluginBase plugin = menuItemNameToObSourcePlugin
						.get(item);
				mediator.createObservationArtefactsFromObSourcePlugin(plugin);
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
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				PreferencesDialog prefsDialog = PreferencesDialog.getInstance();
				prefsDialog.showDialog();
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

	// ** View Menu listeners **

	/**
	 * Returns the action listener to be invoked for View->Observation
	 * Details...
	 */
	public ActionListener createObDetailsListener() {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mediator.showObservationDetails();
			}
		};
	}

	/**
	 * Returns the action listener to be invoked for View->Zoom In
	 */
	public ActionListener createZoomInListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ZoomRequestMessage msg = new ZoomRequestMessage(this,
						ZoomType.ZOOM_IN);
				mediator.getZoomRequestNotifier().notifyListeners(msg);
			}
		};
	}

	/**
	 * Returns the action listener to be invoked for View->Zoom Out
	 */
	public ActionListener createZoomOutListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ZoomRequestMessage msg = new ZoomRequestMessage(this,
						ZoomType.ZOOM_OUT);
				mediator.getZoomRequestNotifier().notifyListeners(msg);
			}
		};
	}

	/**
	 * Returns the action listener to be invoked for View->Zoom To Fit
	 */
	public ActionListener createZoomToFitListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ZoomRequestMessage msg = new ZoomRequestMessage(this,
						ZoomType.ZOOM_TO_FIT);
				mediator.getZoomRequestNotifier().notifyListeners(msg);
			}
		};
	}

	/**
	 * Returns the action listener to be invoked for View->Filter...
	 */
	public ActionListener createFilterListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mediator.getObsFilterDialog().showDialog();
			}
		};
	}

	/**
	 * Returns the action listener to be invoked for Custom Filter menu item
	 * selections.
	 */
	public ActionListener createCustomFilterListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String item = e.getActionCommand();
				CustomFilterPluginBase plugin = menuItemNameToCustomFilterPlugin
						.get(item);
				Mediator.getInstance().applyCustomFilterToCurrentObservations(
						plugin);
			}
		};
	}

	/**
	 * Returns the action listener to be invoked for View->Show All...
	 */
	public ActionListener createShowAllListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mediator.getFilteredObservationNotifier().notifyListeners(
						FilteredObservationMessage.NO_FILTER);
			}
		};
	}

	/**
	 * Returns the action listener to be invoked for View->Pan Left...
	 */
	public ActionListener createPanLeftListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				PanRequestMessage msg = new PanRequestMessage(this,
						PanType.LEFT);
				mediator.getPanRequestNotifier().notifyListeners(msg);
			}
		};
	}

	/**
	 * Returns the action listener to be invoked for View->Pan Right...
	 */
	public ActionListener createPanRightListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				PanRequestMessage msg = new PanRequestMessage(this,
						PanType.RIGHT);
				mediator.getPanRequestNotifier().notifyListeners(msg);
			}
		};
	}

	/**
	 * Returns the action listener to be invoked for View->Pan Up...
	 */
	public ActionListener createPanUpListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				PanRequestMessage msg = new PanRequestMessage(this, PanType.UP);
				mediator.getPanRequestNotifier().notifyListeners(msg);
			}
		};
	}

	/**
	 * Returns the action listener to be invoked for View->Pan Down...
	 */
	public ActionListener createPanDownListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				PanRequestMessage msg = new PanRequestMessage(this,
						PanType.DOWN);
				mediator.getPanRequestNotifier().notifyListeners(msg);
			}
		};
	}

	// ** Analysis Menu listeners **

	/**
	 * Returns the action listener to be invoked for Analysis->Raw Data
	 */
	public ActionListener createRawDataListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setRawDataAnalysisMenuItemState(true); // ensure selected
				mediator.changeAnalysisType(AnalysisType.RAW_DATA);
			}
		};
	}

	/**
	 * Returns the action listener to be invoked for Analysis->Phase Plot
	 */
	public ActionListener createPhasePlotListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setPhasePlotAnalysisMenuItemState(true); // ensure selected
				mediator.changeAnalysisType(AnalysisType.PHASE_PLOT);
			}
		};
	}

	/**
	 * Returns the action listener to be invoked for Analysis->Period Search
	 */
	public ActionListener createPeriodSearchListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String item = e.getActionCommand();
				PeriodAnalysisPluginBase plugin = menuItemNameToPeriodAnalysisPlugin
						.get(item);
				Mediator.getInstance().createPeriodAnalysisDialog(plugin);
			}
		};
	}

	/**
	 * Returns the action listener to be invoked for Analysis->Polynomial Fit
	 */
	public ActionListener createPolynomialFitListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mediator.performPolynomialFit();
			}
		};
	}

	// ** Tool menu listeners **

	/**
	 * Returns the action listener to be invoked for Observation Tool menu item
	 * selections.
	 */
	public ActionListener createObsToolMenuItemListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String item = e.getActionCommand();
				ObservationToolPluginBase plugin = menuItemNameToObsToolPlugin
						.get(item);
				Mediator.getInstance().invokeTool(plugin);
			}
		};
	}

	/**
	 * Returns the action listener to be invoked for General Tool menu item
	 * selections.
	 */
	public ActionListener createGenToolMenuItemListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String item = e.getActionCommand();
				GeneralToolPluginBase plugin = menuItemNameToGenToolPlugin
						.get(item);
				try {
					plugin.invoke();
				} catch (Throwable t) {
					MessageBox.showErrorDialog("Tool Error", t);
				}
			}
		};
	}

	// ** Help Menu listeners **

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
				case START_PROGRESS:
					resetProgress(parent);
					break;
				case COMPLETE_PROGRESS:
					completeProgress();
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

	/**
	 * Return an analysis type change listener.
	 */
	private Listener<AnalysisTypeChangeMessage> createAnalysisTypeChangeListener() {
		return new Listener<AnalysisTypeChangeMessage>() {
			public void update(AnalysisTypeChangeMessage info) {
				switch (info.getAnalysisType()) {
				case RAW_DATA:
					setRawDataAnalysisMenuItemState(true);
					setPhasePlotAnalysisMenuItemState(false);
					break;
				case PHASE_PLOT:
					setRawDataAnalysisMenuItemState(false);
					setPhasePlotAnalysisMenuItemState(true);
					break;
				}
			}

			public boolean canBeRemoved() {
				return false;
			}
		};
	}

	private void resetProgress(MainFrame parent) {
		// TODO: why not set cursor in MainFrame or StatusPane?
		parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		changeKeyMenuItemEnableState(false);
	}

	private void completeProgress() {
		// TODO: why not set cursor in MainFrame or StatusPane?
		parent.setCursor(null); // turn off the wait cursor
		changeKeyMenuItemEnableState(true);
	}

	// Returns a new star listener.
	private Listener<NewStarMessage> createNewStarListener() {
		return new Listener<NewStarMessage>() {

			/**
			 * New star listener update method.
			 */
			@Override
			public void update(NewStarMessage msg) {
				newStarMessage = msg;
				viewObDetailsItem.setEnabled(false);
				viewZoomInItem.setEnabled(false);
				viewZoomOutItem.setEnabled(false);
				viewZoomToFitItem.setEnabled(false);				
			}

			/**
			 * @see org.aavso.tools.vstar.util.notification.Listener#canBeRemoved()
			 */
			@Override
			public boolean canBeRemoved() {
				return false;
			}
		};
	}

	// Returns an observation selection listener that sets enables certain menu
	// items.
	private Listener<ObservationSelectionMessage> createObservationSelectionListener() {
		return new Listener<ObservationSelectionMessage>() {
			@Override
			public void update(ObservationSelectionMessage info) {
				// TODO: really need to distinguish between:
				// observation selection (for obs details, filtering) and
				// point selection (for zooming), and to do this across
				// plot views for raw and phase plot mode.
				viewObDetailsItem.setEnabled(true);
				viewZoomInItem.setEnabled(true);
				viewZoomOutItem.setEnabled(true);
				viewZoomToFitItem.setEnabled(true);
			}

			@Override
			public boolean canBeRemoved() {
				return false;
			}
		};
	}

	// Helper methods

	// Enables or disabled key menu items.
	private void changeKeyMenuItemEnableState(boolean state) {
		this.fileInfoItem.setEnabled(state);

		this.fileSaveItem.setEnabled(state);
		this.filePrintItem.setEnabled(state);

//		this.viewObDetailsItem.setEnabled(state);
//		this.viewZoomInItem.setEnabled(state);
//		this.viewZoomOutItem.setEnabled(state);
//		this.viewZoomToFitItem.setEnabled(state);				

		this.viewFilterItem.setEnabled(state);
		this.viewCustomFilterMenu.setEnabled(state);
		for (int i = 0; i < this.viewCustomFilterMenu.getItemCount(); i++) {
			this.viewCustomFilterMenu.getItem(i).setEnabled(state);
		}
		this.viewNoFilterItem.setEnabled(state);

		this.viewPanLeftItem.setEnabled(state);
		this.viewPanRightItem.setEnabled(state);
		this.viewPanUpItem.setEnabled(state);
		this.viewPanDownItem.setEnabled(state);

		this.analysisRawDataItem.setEnabled(state);
		this.analysisPhasePlotItem.setEnabled(state);
		this.analysisPeriodSearchMenu.setEnabled(state);
		for (int i = 0; i < this.analysisPeriodSearchMenu.getItemCount(); i++) {
			this.analysisPeriodSearchMenu.getItem(i).setEnabled(state);
		}
		this.analysisPolynomialFitItem.setEnabled(state);

		AnalysisType type = mediator.getAnalysisType();

		switch (type) {
		case RAW_DATA:
			setRawDataAnalysisMenuItemState(true);
			setPhasePlotAnalysisMenuItemState(false);
			break;
		case PHASE_PLOT:
			setRawDataAnalysisMenuItemState(false);
			setPhasePlotAnalysisMenuItemState(true);
			break;
		}
	}

	private void setRawDataAnalysisMenuItemState(boolean state) {
		this.analysisRawDataItem.setState(state);
	}

	private void setPhasePlotAnalysisMenuItemState(boolean state) {
		this.analysisPhasePlotItem.setState(state);
	}
}
