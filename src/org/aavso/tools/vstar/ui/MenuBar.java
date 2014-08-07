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

import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import org.aavso.tools.vstar.exception.AuthenticationError;
import org.aavso.tools.vstar.exception.CancellationException;
import org.aavso.tools.vstar.exception.ConnectionException;
import org.aavso.tools.vstar.input.database.Authenticator;
import org.aavso.tools.vstar.plugin.CustomFilterPluginBase;
import org.aavso.tools.vstar.plugin.GeneralToolPluginBase;
import org.aavso.tools.vstar.plugin.IPlugin;
import org.aavso.tools.vstar.plugin.ModelCreatorPluginBase;
import org.aavso.tools.vstar.plugin.ObservationSourcePluginBase;
import org.aavso.tools.vstar.plugin.ObservationToolPluginBase;
import org.aavso.tools.vstar.plugin.period.PeriodAnalysisPluginBase;
import org.aavso.tools.vstar.scripting.ScriptRunner;
import org.aavso.tools.vstar.ui.dialog.AboutBox;
import org.aavso.tools.vstar.ui.dialog.AdditiveLoadFileOrUrlChooser;
import org.aavso.tools.vstar.ui.dialog.InfoDialog;
import org.aavso.tools.vstar.ui.dialog.MessageBox;
import org.aavso.tools.vstar.ui.dialog.StarSelectorDialog;
import org.aavso.tools.vstar.ui.dialog.plugin.manager.PluginManagementDialog;
import org.aavso.tools.vstar.ui.dialog.plugin.manager.PluginManagementOperation;
import org.aavso.tools.vstar.ui.dialog.plugin.manager.PluginManager;
import org.aavso.tools.vstar.ui.dialog.prefs.PreferencesDialog;
import org.aavso.tools.vstar.ui.mediator.AnalysisType;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.message.AnalysisTypeChangeMessage;
import org.aavso.tools.vstar.ui.mediator.message.FilteredObservationMessage;
import org.aavso.tools.vstar.ui.mediator.message.ModelCreationMessage;
import org.aavso.tools.vstar.ui.mediator.message.NewStarMessage;
import org.aavso.tools.vstar.ui.mediator.message.ObservationSelectionMessage;
import org.aavso.tools.vstar.ui.mediator.message.PanRequestMessage;
import org.aavso.tools.vstar.ui.mediator.message.PanType;
import org.aavso.tools.vstar.ui.mediator.message.PhaseChangeMessage;
import org.aavso.tools.vstar.ui.mediator.message.ProgressInfo;
import org.aavso.tools.vstar.ui.mediator.message.UndoActionMessage;
import org.aavso.tools.vstar.ui.mediator.message.UndoRedoType;
import org.aavso.tools.vstar.ui.mediator.message.ZoomRequestMessage;
import org.aavso.tools.vstar.ui.mediator.message.ZoomType;
import org.aavso.tools.vstar.ui.resources.PluginLoader;
import org.aavso.tools.vstar.ui.resources.ResourceAccessor;
import org.aavso.tools.vstar.util.locale.LocaleProps;
import org.aavso.tools.vstar.util.notification.Listener;

/**
 * VStar's menu bar.
 */
@SuppressWarnings("serial")
public class MenuBar extends JMenuBar {

	// File menu item names.
	public static final String NEW_STAR_FROM_DATABASE = LocaleProps
			.get("FILE_MENU_NEW_STAR_FROM_DATABASE");
	public static final String NEW_STAR_FROM_FILE = LocaleProps
			.get("FILE_MENU_NEW_STAR_FROM_FILE");
	public static final String SAVE = LocaleProps.get("FILE_MENU_SAVE");
	public static final String PRINT = LocaleProps.get("FILE_MENU_PRINT");
	public static final String INFO = LocaleProps.get("FILE_MENU_INFO");
	public static final String PREFS = LocaleProps.get("FILE_MENU_PREFS");
	public static final String QUIT = LocaleProps.get("FILE_MENU_QUIT");

	// Edit menu items.
	public static final String UNDO = LocaleProps.get("EDIT_MENU_UNDO");
	public static final String REDO = LocaleProps.get("EDIT_MENU_REDO");
	public static final String EXCLUDE_SELECTION = LocaleProps
			.get("EDIT_MENU_EXCLUDE_SELECTION");

	// View menu item names.
	public static final String RAW_DATA_MODE = LocaleProps
			.get("VIEW_MENU_RAW_DATA_MODE");
	public static final String PHASE_PLOT_MODE = LocaleProps
			.get("VIEW_MENU_PHASE_PLOT_MODE");
	public static final String OB_DETAILS = LocaleProps
			.get("VIEW_MENU_OB_DETAILS");
	public static final String PLOT_CONTROL = LocaleProps
			.get("VIEW_MENU_PLOT_CONTROL");
	public static final String ZOOM_IN = LocaleProps.get("VIEW_MENU_ZOOM_IN");
	public static final String ZOOM_OUT = LocaleProps.get("VIEW_MENU_ZOOM_OUT");
	public static final String ZOOM_TO_FIT = LocaleProps
			.get("VIEW_MENU_ZOOM_TO_FIT");
	public static final String PAN_LEFT = LocaleProps.get("VIEW_MENU_PAN_LEFT");
	public static final String PAN_RIGHT = LocaleProps
			.get("VIEW_MENU_PAN_RIGHT");
	public static final String PAN_UP = LocaleProps.get("VIEW_MENU_PAN_UP");
	public static final String PAN_DOWN = LocaleProps.get("VIEW_MENU_PAN_DOWN");
	public static final String FILTER = LocaleProps.get("VIEW_MENU_FILTER");
	public static final String FILTERS = LocaleProps.get("VIEW_MENU_FILTERS");
	public static final String NO_FILTER = LocaleProps
			.get("VIEW_MENU_NO_FILTER");

	// Analysis menu item names.
	public static final String PHASE_PLOT = LocaleProps
			.get("ANALYSIS_MENU_PHASE_PLOT");
	public static final String PHASE_PLOTS = LocaleProps
			.get("ANALYSIS_MENU_PHASE_PLOTS");
	public static final String POLYNOMIAL_FIT = LocaleProps
			.get("ANALYSIS_MENU_POLYNOMIAL_FIT");
	public static final String MODELS = LocaleProps.get("ANALYSIS_MENU_MODELS");

	// Tool menu item names.
	public static final String PLUGIN_MANAGER = LocaleProps
			.get("TOOL_MENU_PLUGIN_MANAGER");
	public static final String RUN_SCRIPT = LocaleProps
			.get("TOOL_MENU_RUN_SCRIPT");

	// Help menu item names.
	public static final String HELP_CONTENTS = LocaleProps
			.get("HELP_MENU_HELP_CONTENTS");
	public static final String VSTAR_ONLINE = LocaleProps
			.get("HELP_MENU_VSTAR_ONLINE");
	public static final String ABOUT = LocaleProps.get("HELP_MENU_ABOUT");

	// Set of menu items NOT to be rendered in a minimal UI such as for an
	// applet.
	public static final Set<String> minimalUIExclusions;

	static {
		minimalUIExclusions = new HashSet<String>();
		minimalUIExclusions.add(NEW_STAR_FROM_FILE);
		minimalUIExclusions.add(SAVE);
		minimalUIExclusions.add(PRINT);
		minimalUIExclusions.add(QUIT);
		minimalUIExclusions.add(RUN_SCRIPT);
	}

	private Mediator mediator = Mediator.getInstance();

	private AdditiveLoadFileOrUrlChooser fileOpenDialog;

	// Plug-in menu name to plug-in object maps.
	private Map<String, ObservationSourcePluginBase> menuItemNameToObSourcePlugin;
	private Map<String, CustomFilterPluginBase> menuItemNameToCustomFilterPlugin;
	private Map<String, PeriodAnalysisPluginBase> menuItemNameToPeriodAnalysisPlugin;
	private Map<String, ModelCreatorPluginBase> menuItemNameToModelCreatorPlugin;
	private Map<String, ObservationToolPluginBase> menuItemNameToObsToolPlugin;
	private Map<String, GeneralToolPluginBase> menuItemNameToGenToolPlugin;

	// Keep track of analysis menu items for enabling/disabling.
	private List<JMenuItem> analysisMenuItems;

	// The parent window.
	private IMainUI parent;

	// The user interface type.
	private UIType uiType;

	// Menu items.

	// File menu.
	JMenuItem fileNewStarFromDatabaseItem;
	JMenuItem fileNewStarFromFileItem;
	JMenuItem fileSaveItem;
	JMenuItem filePrintItem;
	JMenuItem fileInfoItem;
	JMenuItem filePrefsItem;
	JMenuItem fileQuitItem;

	// Edit menu.
	JMenu editMenu;
	JMenuItem editUndoItem;
	JMenuItem editRedoItem;
	JMenuItem editExcludeSelectionItem;

	// View menu.
	JCheckBoxMenuItem viewRawDataItem;
	JCheckBoxMenuItem viewPhasePlotItem;
	JMenuItem viewObDetailsItem;
	JMenuItem viewPlotControlItem;
	JMenuItem viewZoomInItem;
	JMenuItem viewZoomOutItem;
	JMenuItem viewZoomToFitItem;
	JMenuItem viewPanLeftItem;
	JMenuItem viewPanRightItem;
	JMenuItem viewPanUpItem;
	JMenuItem viewPanDownItem;
	JMenuItem viewFilterItem;
	JMenuItem viewFiltersItem;
	JMenuItem viewNoFilterItem;

	JMenu viewCustomFilterMenu;

	// Analysis menu.
	JMenuItem analysisPhasePlotItem;

	JMenuItem analysisModelsItem;
	JMenuItem analysisPhasePlotsItem;

	// Tool menu.
	JMenu toolMenu;
	JMenuItem toolPluginManager;
	JMenuItem toolRunScript;

	// Help menu.
	JMenuItem helpContentsItem;
	JMenuItem helpVStarOnlineItem;
	JMenuItem helpAboutItem;

	// New star message.
	private NewStarMessage newStarMessage;

	/**
	 * Constructor
	 * 
	 * @param parent
	 *            The frame's parent.
	 * @param uiType
	 *            The type of UI.
	 */
	public MenuBar(IMainUI parent, UIType uiType) {
		super();

		this.parent = parent;
		this.uiType = uiType;

		this.fileOpenDialog = new AdditiveLoadFileOrUrlChooser(false);

		createFileMenu();
		createEditMenu();
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

		this.mediator.getUndoActionNotifier().addListener(
				createUndoActionListener());

		this.mediator.getModelCreationNotifier().addListener(
				createModelCreationListener());

		this.mediator.getPhaseChangeNotifier().addListener(
				createPhaseChangeListener());

		this.mediator.getFilteredObservationNotifier().addListener(
				createObsFilterListener());
	}

	/**
	 * @return the uiType
	 */
	public UIType getUiType() {
		return uiType;
	}

	private void createFileMenu() {
		JMenu fileMenu = new JMenu(LocaleProps.get("FILE_MENU"));

		fileNewStarFromDatabaseItem = new JMenuItem(NEW_STAR_FROM_DATABASE);
		fileNewStarFromDatabaseItem
				.addActionListener(createNewStarFromDatabaseListener());
		fileMenu.add(fileNewStarFromDatabaseItem);

		// if (uiType != UIType.APPLET) {
		fileNewStarFromFileItem = new JMenuItem(NEW_STAR_FROM_FILE);
		fileNewStarFromFileItem
				.addActionListener(createNewStarFromFileListener());
		fileMenu.add(fileNewStarFromFileItem);
		// }

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

		// if (uiType != UIType.APPLET) {
		fileSaveItem = new JMenuItem(SAVE);
		fileSaveItem.addActionListener(this.createSaveListener());
		fileSaveItem.setEnabled(false);
		fileMenu.add(fileSaveItem);
		// }

		// if (uiType != UIType.APPLET) {
		filePrintItem = new JMenuItem(PRINT);
		filePrintItem.addActionListener(this.createPrintListener());
		filePrintItem.setEnabled(false);
		fileMenu.add(filePrintItem);

		fileMenu.addSeparator();
		// }

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
			if (uiType != UIType.APPLET) {
				fileMenu.addSeparator();

				fileQuitItem = new JMenuItem(QUIT, KeyEvent.VK_Q);
				// fileQuitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,
				// ActionEvent.META_MASK));
				fileQuitItem.addActionListener(createQuitListener());
				fileMenu.add(fileQuitItem);
			}
		}

		this.add(fileMenu);
	}

	private void createEditMenu() {
		editMenu = new JMenu(LocaleProps.get("EDIT_MENU"));

		editUndoItem = new JMenuItem(UNDO);
		editUndoItem.setEnabled(false);
		editUndoItem.addActionListener(createUndoListener());
		editMenu.add(editUndoItem);

		editRedoItem = new JMenuItem(REDO);
		editRedoItem.setEnabled(false);
		editRedoItem.addActionListener(createRedoListener());
		editMenu.add(editRedoItem);

		editMenu.addSeparator();

		editExcludeSelectionItem = new JMenuItem(EXCLUDE_SELECTION);
		editExcludeSelectionItem.setEnabled(false);
		editExcludeSelectionItem
				.addActionListener(createExcludeSelectionListener());
		editMenu.add(editExcludeSelectionItem);

		this.add(editMenu);
	}

	private void createViewMenu() {
		JMenu viewMenu = new JMenu(LocaleProps.get("VIEW_MENU"));

		viewRawDataItem = new JCheckBoxMenuItem(RAW_DATA_MODE);
		viewRawDataItem.setEnabled(false);
		viewRawDataItem.addActionListener(createRawDataListener());
		viewMenu.add(viewRawDataItem);

		viewPhasePlotItem = new JCheckBoxMenuItem(PHASE_PLOT_MODE);
		viewPhasePlotItem.setEnabled(false);
		viewPhasePlotItem.addActionListener(createPhasePlotListener());
		viewMenu.add(viewPhasePlotItem);

		viewMenu.addSeparator();

		viewObDetailsItem = new JMenuItem(OB_DETAILS);
		viewObDetailsItem.setEnabled(false);
		viewObDetailsItem.addActionListener(createObDetailsListener());
		viewMenu.add(viewObDetailsItem);

		viewPlotControlItem = new JMenuItem(PLOT_CONTROL);
		viewPlotControlItem.setEnabled(false);
		viewPlotControlItem.addActionListener(createPlotControlListener());
		viewMenu.add(viewPlotControlItem);

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

		viewFiltersItem = new JMenuItem(FILTERS);
		viewFiltersItem.setEnabled(false);
		viewFiltersItem.addActionListener(createFiltersListener());
		viewMenu.add(viewFiltersItem);

		viewCustomFilterMenu = new JMenu(LocaleProps
				.get("VIEW_MENU_CUSTOM_FILTERS"));
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
		JMenu analysisMenu = new JMenu(LocaleProps.get("ANALYSIS_MENU"));

		analysisPhasePlotItem = new JMenuItem(PHASE_PLOT);
		analysisPhasePlotItem.setEnabled(false);
		analysisPhasePlotItem
				.addActionListener(createCreatePhasePlotListener());
		analysisMenu.add(analysisPhasePlotItem);

		analysisPhasePlotsItem = new JMenuItem(PHASE_PLOTS);
		analysisPhasePlotsItem.setEnabled(false);
		analysisPhasePlotsItem.addActionListener(createPhasePlotsListener());
		analysisMenu.add(analysisPhasePlotsItem);

		analysisModelsItem = new JMenuItem(MODELS);
		analysisModelsItem.setEnabled(false);
		analysisModelsItem.addActionListener(createModelsListener());
		analysisMenu.add(analysisModelsItem);

		// Add period analysis and model creator plugins.
		analysisMenuItems = new ArrayList<JMenuItem>();
		String lastGroup = null;

		menuItemNameToPeriodAnalysisPlugin = new TreeMap<String, PeriodAnalysisPluginBase>();
		lastGroup = addAnalysisPlugins(analysisMenu,
				createPeriodSearchListener(), PluginLoader
						.getPeriodAnalysisPlugins(),
				menuItemNameToPeriodAnalysisPlugin, lastGroup);

		menuItemNameToModelCreatorPlugin = new TreeMap<String, ModelCreatorPluginBase>();
		lastGroup = addAnalysisPlugins(analysisMenu,
				createModelCreatorListener(), PluginLoader
						.getModelCreatorPlugins(),
				menuItemNameToModelCreatorPlugin, lastGroup);

		this.add(analysisMenu);
	}

	// Add items for analysis plugins of type P to the analysis menu.
	private <P extends IPlugin> String addAnalysisPlugins(JMenu analysisMenu,
			ActionListener listener, List<P> plugins,
			Map<String, P> menuItemToPluginMap, String lastGroup) {
		for (P plugin : plugins) {

			if (plugin.getGroup() != null
					&& !plugin.getGroup().equals(lastGroup)) {
				lastGroup = plugin.getGroup();
				analysisMenu.addSeparator();
			}

			String itemName = plugin.getDisplayName() + "...";

			JMenuItem analysisMenuItem = new JMenuItem(itemName);
			analysisMenuItem.addActionListener(listener);
			analysisMenu.add(analysisMenuItem);
			analysisMenuItem.setEnabled(false);

			menuItemToPluginMap.put(itemName, plugin);
			analysisMenuItems.add(analysisMenuItem);
		}

		return lastGroup;
	}

	private void createToolMenu() {
		toolMenu = new JMenu(LocaleProps.get("TOOL_MENU"));
		// toolMenu.setEnabled(false);

		toolPluginManager = new JMenuItem(PLUGIN_MANAGER);
		toolPluginManager.addActionListener(createPluginManagerListener());
		toolMenu.add(toolPluginManager);

		// if (uiType != UIType.APPLET) {
		toolRunScript = new JMenuItem(RUN_SCRIPT);
		toolRunScript.addActionListener(createRunScriptListener());
		toolMenu.add(toolRunScript);

		toolMenu.addSeparator();
		// }

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
		JMenu helpMenu = new JMenu(LocaleProps.get("HELP_MENU"));

		// If default browser support is available, add an online docs menu
		// items.
		if (Desktop.isDesktopSupported()) {
			Desktop desktop = Desktop.getDesktop();
			if (desktop.isSupported(Desktop.Action.BROWSE)) {
				// User manual.
				helpContentsItem = new JMenuItem(HELP_CONTENTS, KeyEvent.VK_H);
				helpContentsItem
						.addActionListener(createHelpContentsListener());
				helpMenu.add(helpContentsItem);

				// VStar online.
				helpVStarOnlineItem = new JMenuItem(VSTAR_ONLINE);
				helpVStarOnlineItem
						.addActionListener(createVStarOnlineListener());
				helpMenu.add(helpVStarOnlineItem);
			}
		}

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
					Mediator.getUI().getStatusPane().setMessage(
							LocaleProps.get("STATUS_PANE_SELECT_STAR"));
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
								starName, auid, minJD, maxJD,
								starSelectorDialog.isLoadAdditive());
					} else {
						Mediator.getUI().getStatusPane().setMessage("");
					}
				} catch (Exception ex) {
					completeProgress();
					MessageBox.showErrorDialog(Mediator.getUI().getComponent(),
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
		final AdditiveLoadFileOrUrlChooser fileOpenDialog = this.fileOpenDialog;
		fileOpenDialog.reset();

		final IMainUI parent = this.parent;

		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean approved = fileOpenDialog.showDialog(parent
						.getComponent());

				if (approved) {
					File f = fileOpenDialog.getSelectedFile();

					try {
						mediator.createObservationArtefactsFromFile(f,
								fileOpenDialog.isLoadAdditive());
					} catch (Exception ex) {
						MessageBox.showErrorDialog(parent.getComponent(),
								NEW_STAR_FROM_FILE, ex);
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
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mediator.saveCurrentMode(Mediator.getUI().getComponent());
			}
		};
	}

	/**
	 * Returns the action listener to be invoked for File->Print...
	 */
	public ActionListener createPrintListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mediator.printCurrentMode(Mediator.getUI().getComponent());
			}
		};
	}

	/**
	 * Returns the action listener to be invoked for File->Info...
	 */
	public ActionListener createInfoListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new InfoDialog(Mediator.getInstance().getNewStarMessageList());
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
			public void actionPerformed(ActionEvent e) {
				mediator.quit();
			}
		};
	}

	// ** Edit Menu listeners **

	/**
	 * Returns the action listener to be invoked for Edit->Undo
	 */
	public ActionListener createUndoListener() {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mediator.getUndoableActionManager().executeUndoAction();

				if (mediator.getUndoableActionManager().isUndoStackEmpty()) {
					editUndoItem.setText(UNDO);
					editUndoItem.setEnabled(false);
				}
			}
		};
	}

	/**
	 * Returns the action listener to be invoked for Edit->Redo
	 */
	public ActionListener createRedoListener() {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mediator.getUndoableActionManager().executeRedoAction();

				if (mediator.getUndoableActionManager().isRedoStackEmpty()) {
					editRedoItem.setText(REDO);
					editRedoItem.setEnabled(false);
				}
			}
		};
	}

	/**
	 * Returns the action listener to be invoked for Edit->Exclude Selection
	 */
	public ActionListener createExcludeSelectionListener() {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Mediator.getInstance().getUndoableActionManager()
						.excludeCurrentSelection();

				// Once this menu item has been used for a particular selection,
				// disable the menu item until the next selection is made.
				editExcludeSelectionItem.setEnabled(false);
			}
		};
	}

	// ** View Menu listeners **

	/**
	 * Returns the action listener to be invoked for View->Raw Data
	 */
	public ActionListener createRawDataListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mediator.changeAnalysisType(AnalysisType.RAW_DATA);
				setRawDataViewMenuItemState(true);
			}
		};
	}

	/**
	 * Returns the action listener to be invoked for View->Phase Plot
	 */
	public ActionListener createPhasePlotListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AnalysisType type = mediator
						.changeAnalysisType(AnalysisType.PHASE_PLOT);
				if (type == AnalysisType.PHASE_PLOT) {
					// It may be that no phase plot has been created because the
					// phase plot dialog was cancelled.
					setPhasePlotViewMenuItemState(true);
				}
			}
		};
	}

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
	 * Returns the action listener to be invoked for View->Plot Control...
	 */
	public ActionListener createPlotControlListener() {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mediator.showPlotControlDialog();
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
	 * Returns the action listener to be invoked for View->Filters...
	 */
	public ActionListener createFiltersListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mediator.showFiltersDialog();
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
	 * Returns the action listener to be invoked for Analysis->Phase Plot
	 */
	public ActionListener createCreatePhasePlotListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mediator.createPhasePlot();
			}
		};
	}

	/**
	 * Returns the action listener to be invoked for Analysis menu Period Search
	 * items.
	 */
	public ActionListener createPeriodSearchListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String item = e.getActionCommand();
				PeriodAnalysisPluginBase plugin = menuItemNameToPeriodAnalysisPlugin
						.get(item);
				Mediator.getInstance().performPeriodAnalysis(plugin);
			}
		};
	}

	/**
	 * Returns the action listener to be invoked for a particular Analysis menu
	 * Period Search item. TODO: interim solution until we have toolbar buttons
	 * with lists of items!
	 */
	public ActionListener createPeriodSearchListener(
			final String periodSearchItemName) {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				PeriodAnalysisPluginBase plugin = menuItemNameToPeriodAnalysisPlugin
						.get(periodSearchItemName);
				Mediator.getInstance().performPeriodAnalysis(plugin);
			}
		};
	}

	/**
	 * Returns the action listener to be invoked for Analysis menu model creator
	 * items.
	 */
	public ActionListener createModelCreatorListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String item = e.getActionCommand();
				ModelCreatorPluginBase plugin = menuItemNameToModelCreatorPlugin
						.get(item);
				Mediator.getInstance().performModellingOperation(plugin);
			}
		};
	}

	/**
	 * Returns the action listener to be invoked for Analysis polynomial fit
	 * item.<br/>
	 * TODO: interim solution until we have toolbar buttons with lists of items!
	 */
	public ActionListener createPolynomialFitListener(
			final String polyFitItemName) {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ModelCreatorPluginBase plugin = menuItemNameToModelCreatorPlugin
						.get(polyFitItemName);
				Mediator.getInstance().performModellingOperation(plugin);
			}
		};
	}

	/**
	 * Returns the action listener to be invoked for Analysis->Models...
	 */
	public ActionListener createModelsListener() {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mediator.showModelDialog();
			}
		};
	}

	/**
	 * Returns the action listener to be invoked for Analysis->Phase Plots...
	 */
	public ActionListener createPhasePlotsListener() {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mediator.showPhaseDialog();
			}
		};
	}

	// ** Tool menu listeners **

	/**
	 * Returns the action listener to be invoked for Tool -> Plug-in Manager...
	 */
	public ActionListener createPluginManagerListener() {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final PluginManager manager = new PluginManager();
				PluginManagementOperation op = new PluginManagementOperation(
						manager, "Initialising Plug-in Manager") {
					@Override
					public void execute() {
						try {
							Authenticator.getInstance().authenticate();
							Mediator.getUI().getStatusPane().setMessage(
									"Initialising Plug-in Manager...");
							manager.init();
							new PluginManagementDialog(manager);
							Mediator.getUI().getStatusPane().setMessage("");
						} catch (ConnectionException ex) {
							MessageBox.showErrorDialog(
									"Authentication Source Error", ex);
						} catch (AuthenticationError ex) {
							MessageBox.showErrorDialog("Authentication Error",
									ex);
						} catch (CancellationException e) {
							// Nothing to do.
						}
					}
				};
				mediator.performPluginManagerOperation(op);
			}
		};
	}

	/**
	 * Returns the action listener to be invoked for Tool -> Run Script...
	 */
	public ActionListener createRunScriptListener() {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ScriptRunner.getInstance().runScript();
			}
		};
	}

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
	 * Returns the action listener to be invoked for Help->User Manual Online...
	 */
	public ActionListener createHelpContentsListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openHelpURLInWebBrowser("http://www.aavso.org/files/vstar/VStarUserManual.pdf");
			}
		};
	}

	/**
	 * Returns the action listener to be invoked for Help->VStar Online...
	 */
	public ActionListener createVStarOnlineListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openHelpURLInWebBrowser("http://www.aavso.org/vstar-overview");
			}
		};
	}

	private void openHelpURLInWebBrowser(final String urlStr) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				// Try to open the VStar online page in the default web
				// browser.
				if (Desktop.isDesktopSupported()) {
					Desktop desktop = Desktop.getDesktop();
					URL url = null;
					try {
						url = new URL(urlStr);
						java.net.URL helpURL = ResourceAccessor
								.getHelpHTMLResource();
						if (desktop.isSupported(Desktop.Action.BROWSE)) {
							try {
								desktop.browse(url.toURI());
							} catch (IOException e) {
								MessageBox.showErrorDialog("VStar Help",
										"Error reading from '"
												+ helpURL.toString() + "'");
							} catch (URISyntaxException e) {
								MessageBox.showErrorDialog("VStar Help",
										"Invalid address: '"
												+ helpURL.toString() + "'");
							}
						}
					} catch (MalformedURLException e) {
						MessageBox.showErrorDialog("VStar Help",
								"Invalid address.");
					}
				}
			}
		});
	}

	/**
	 * Returns the action listener to be invoked for Help->About...
	 */
	private ActionListener createAboutListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new AboutBox();
			}
		};
	}

	/**
	 * Return a progress listener.
	 */
	private Listener<ProgressInfo> createProgressListener() {
		final IMainUI parent = this.parent;
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
					// Flip raw/phase plot mode checkboxes.
					setRawDataViewMenuItemState(true);
					setPhasePlotViewMenuItemState(false);
					break;
				case PHASE_PLOT:
					// Flip raw/phase plot mode checkboxes.
					setRawDataViewMenuItemState(false);
					setPhasePlotViewMenuItemState(true);
					break;
				}

				// Enable filtering.
				viewFilterItem.setEnabled(true);
				viewNoFilterItem.setEnabled(true);
				viewCustomFilterMenu.setEnabled(true);
			}

			public boolean canBeRemoved() {
				return false;
			}
		};
	}

	private void resetProgress(IMainUI parent) {
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

			@Override
			public void update(NewStarMessage msg) {
				newStarMessage = msg;

				editExcludeSelectionItem.setEnabled(false);

				viewPhasePlotItem.setEnabled(false);
				viewObDetailsItem.setEnabled(false);
				viewZoomInItem.setEnabled(false);
				viewZoomOutItem.setEnabled(false);
				viewZoomToFitItem.setEnabled(false);
				viewFiltersItem.setEnabled(false);

				analysisPhasePlotItem.setEnabled(true);
				analysisModelsItem.setEnabled(false);
				analysisPhasePlotsItem.setEnabled(false);
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

	// Returns an observation selection listener that enables certain menu
	// items and collects information about the selection.
	private Listener<ObservationSelectionMessage> createObservationSelectionListener() {
		return new Listener<ObservationSelectionMessage>() {
			@Override
			public void update(ObservationSelectionMessage info) {
				editExcludeSelectionItem.setEnabled(true);

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

	// Returns an undo/redo action message listener that is used to update the
	// Edit->Undo/Redo menu item.
	private Listener<UndoActionMessage> createUndoActionListener() {
		return new Listener<UndoActionMessage>() {
			@Override
			public void update(UndoActionMessage info) {
				String itemName = info.getType() + " "
						+ info.getAction().getDisplayString();

				JMenuItem item = null;
				if (info.getType() == UndoRedoType.UNDO) {
					item = editUndoItem;
				} else {
					item = editRedoItem;
				}

				item.setText(itemName);
				item.setEnabled(true);
			}

			@Override
			public boolean canBeRemoved() {
				return false;
			}
		};
	}

	/**
	 * Return a model creation listener.
	 */
	public Listener<ModelCreationMessage> createModelCreationListener() {
		return new Listener<ModelCreationMessage>() {
			@Override
			public void update(ModelCreationMessage info) {
				if (!analysisModelsItem.isEnabled()) {
					analysisModelsItem.setEnabled(true);
				}
			}

			@Override
			public boolean canBeRemoved() {
				return false;
			}
		};
	}

	/**
	 * Return a phase change listener.
	 */
	public Listener<PhaseChangeMessage> createPhaseChangeListener() {
		return new Listener<PhaseChangeMessage>() {
			@Override
			public void update(PhaseChangeMessage info) {
				if (!viewPhasePlotItem.isEnabled()) {
					viewPhasePlotItem.setEnabled(true);
				}

				if (!analysisPhasePlotsItem.isEnabled()) {
					analysisPhasePlotsItem.setEnabled(true);
				}
			}

			@Override
			public boolean canBeRemoved() {
				return false;
			}
		};
	}

	/**
	 * Return an observation filter listener.
	 */
	public Listener<FilteredObservationMessage> createObsFilterListener() {
		return new Listener<FilteredObservationMessage>() {
			@Override
			public void update(FilteredObservationMessage info) {
				viewFiltersItem.setEnabled(true);
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

		// this.editMenu.setEnabled(state);

		this.viewObDetailsItem.setEnabled(state);
		this.viewPlotControlItem.setEnabled(state);
		this.viewZoomInItem.setEnabled(state);
		this.viewZoomOutItem.setEnabled(state);
		// this.viewZoomToFitItem.setEnabled(state);

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

		this.viewRawDataItem.setEnabled(state);

		for (JMenuItem item : analysisMenuItems) {
			item.setEnabled(state);
		}

		AnalysisType type = mediator.getAnalysisType();

		switch (type) {
		case RAW_DATA:
			setRawDataViewMenuItemState(true);
			setPhasePlotViewMenuItemState(false);
			break;
		case PHASE_PLOT:
			setRawDataViewMenuItemState(false);
			setPhasePlotViewMenuItemState(true);
			break;
		}
	}

	private void setRawDataViewMenuItemState(boolean state) {
		this.viewRawDataItem.setState(state);
	}

	private void setPhasePlotViewMenuItemState(boolean state) {
		this.viewPhasePlotItem.setState(state);
	}
}
