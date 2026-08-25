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
package org.aavso.tools.vstar.ui.dialog.prefs;

import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.aavso.tools.vstar.plugin.IPlugin;
import org.aavso.tools.vstar.ui.dialog.AbstractOkCancelDialog;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.resources.PluginLoader;

/**
 * Preferences Dialog.
 */
@SuppressWarnings("serial")
public class PreferencesDialog extends AbstractOkCancelDialog {

	private SeriesColorSelectionPane seriesColorPane;
	private SeriesSizeSelectionPane seriesSizePane;
	private ChartPropertiesSelectionPane chartPropertiesPane;
	private NumericPrecisionSelectionPane numericPrecisionPane;
	private StarGroupManagementPane starGroupManagementPane;
	private PluginSettingsPane pluginSettingsPane;
	private LocaleSelectionPane localeSelectionPane;
	private VeLaSettingsPane veLaSettingsPane;
	private DirectoriesSettingsPane directoriesSettingsPane;
	
	private List<Object> pluginPrefs = null;
		
	/**
	 * Constructor.
	 */
	private PreferencesDialog() {
		super("Preferences");

		Container contentPane = this.getContentPane();

		JPanel topPane = new JPanel();
		topPane.setLayout(new BoxLayout(topPane, BoxLayout.PAGE_AXIS));
		topPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		topPane.add(createTabs());
		topPane.add(createButtonPane());

		contentPane.add(topPane);

		this.pack();
		this.setLocationRelativeTo(Mediator.getUI().getContentPane());
	}

	private JTabbedPane createTabs() {
		JTabbedPane tabs = new JTabbedPane();

		seriesColorPane = new SeriesColorSelectionPane();
		tabs.addTab("Series Colors", seriesColorPane);

		seriesSizePane = new SeriesSizeSelectionPane();
		tabs.addTab("Series Size", seriesSizePane);
		
		chartPropertiesPane = new ChartPropertiesSelectionPane();
		tabs.addTab("Chart Properties", chartPropertiesPane);
		
		numericPrecisionPane = new NumericPrecisionSelectionPane();
		tabs.addTab("Numeric Precision", numericPrecisionPane);
		
		starGroupManagementPane = new StarGroupManagementPane();
		tabs.addTab("Star Groups", starGroupManagementPane);
		
		pluginSettingsPane = new PluginSettingsPane();
		tabs.addTab("Plug-ins", pluginSettingsPane);

		localeSelectionPane = new LocaleSelectionPane();
		tabs.addTab("Locale", localeSelectionPane);
		
		veLaSettingsPane = new VeLaSettingsPane();
		tabs.addTab("VeLa", veLaSettingsPane);

		directoriesSettingsPane = new DirectoriesSettingsPane();
		tabs.addTab("Directories", directoriesSettingsPane);

		List<IPlugin> plugin_list = PluginLoader.getPluginList();
		if (plugin_list != null) {
			Set<String> seenPreferenceIds = new HashSet<String>();
			List<Component> addedPanes = new ArrayList<Component>();
			for (IPlugin plugin : plugin_list) {
				Component pane = plugin.getPreferencesPane();
				if (PreferenceTabDeduper.shouldAddPreferenceTab(pane,
						plugin.getPreferencesId(), addedPanes,
						seenPreferenceIds)) {
					tabs.addTab(plugin.getDisplayName(), pane);
					if (pluginPrefs == null) pluginPrefs = new ArrayList<Object>();
					pluginPrefs.add(pane);
					addedPanes.add(pane);
					PreferenceTabDeduper.recordPreferenceTab(pane,
							plugin.getPreferencesId(), seenPreferenceIds);
				}
			}
		}
		
		return tabs;
	}

	/**
	 * @see org.aavso.tools.vstar.ui.dialog.AbstractOkCancelDialog#cancelAction()
	 */
	protected void cancelAction() {
		// Nothing to do.
	}

	/**
	 * @see org.aavso.tools.vstar.ui.dialog.AbstractOkCancelDialog#okAction()
	 */
	protected void okAction() {
		seriesColorPane.update();
		seriesSizePane.update();
		chartPropertiesPane.update();
		numericPrecisionPane.update();
		starGroupManagementPane.update();
		pluginSettingsPane.update();
		localeSelectionPane.update();
		veLaSettingsPane.update();
		directoriesSettingsPane.update();
		
		if (pluginPrefs != null) {
			for (Object pane : pluginPrefs) {
				if (pane != null && pane instanceof IPreferenceComponent) {
					((IPreferenceComponent)pane).update();
				}
			}
		}
		
		this.setVisible(false);
	}

	/**
	 * @see org.aavso.tools.vstar.ui.dialog.AbstractOkCancelDialog#reset()
	 */
	protected void reset() {
		seriesColorPane.reset();
		seriesSizePane.reset();
		chartPropertiesPane.reset();
		numericPrecisionPane.reset();
		starGroupManagementPane.reset();
		pluginSettingsPane.reset();
		localeSelectionPane.reset();
		veLaSettingsPane.reset();
		directoriesSettingsPane.reset();
		
		if (pluginPrefs != null) {
			for (Object pane : pluginPrefs) {
				if (pane != null && pane instanceof IPreferenceComponent) {
					((IPreferenceComponent)pane).reset();
				}
			}
		}
	}

	/**
	 * Singleton
	 */
	private static PreferencesDialog instance = new PreferencesDialog();

	public static PreferencesDialog getInstance() {
		return instance;
	}
}
