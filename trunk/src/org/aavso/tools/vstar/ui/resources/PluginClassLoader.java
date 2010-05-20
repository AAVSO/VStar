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
package org.aavso.tools.vstar.ui.resources;

import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import org.aavso.tools.vstar.plugin.PluginBase;
import org.aavso.tools.vstar.plugin.period.PeriodAnalysisPluginBase;
import org.aavso.tools.vstar.plugin.period.impl.DcDftPeriodAnalysisPlugin;
import org.aavso.tools.vstar.ui.VStar;
import org.aavso.tools.vstar.ui.dialog.MessageBox;

/**
 * This class loads VStar plugins.
 */
public class PluginClassLoader {

	// The full path to the plugin directory.
	// TODO: make this a preference (add to Prefs Notes tracker)
	private final static String PLUGIN_DIR_PATH = System
			.getProperty("user.home")
			+ File.separator + "vstar_plugins";

	private static List<PluginBase> plugins = loadPlugins();
	
	/**
	 * Return a list of period analysis plugins, whether internal to VStar or
	 * dynamically loaded.
	 */
	public static List<PeriodAnalysisPluginBase> getPeriodAnalysisPlugins() {
		List<PeriodAnalysisPluginBase> periodAnalysisPlugins = new ArrayList<PeriodAnalysisPluginBase>();

		// First, add in-built DC DFT period analysis as a plugin.
		periodAnalysisPlugins.add(new DcDftPeriodAnalysisPlugin());

		// Next, add all external period analysis plugins.
		for (PluginBase plugin : plugins) {
			if (plugin instanceof PeriodAnalysisPluginBase) {
				periodAnalysisPlugins.add((PeriodAnalysisPluginBase) plugin);
			}
		}

		return periodAnalysisPlugins;
	}

	/**
	 * Load and return all VStar plugins and create an instance of each.
	 */
	private static List<PluginBase> loadPlugins() {
		List<PluginBase> plugins = new ArrayList<PluginBase>();

		// Locate external plugins, if any exist.
		FilenameFilter jarFilter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".jar");
			}
		};

		File pluginPath = new File(PLUGIN_DIR_PATH);
		if (pluginPath.exists() && pluginPath.isDirectory()) {
			for (File file : pluginPath.listFiles(jarFilter)) {
				// TODO: For now, assume the jar file name is the same 
				// as the qualified class to be loaded.
				// Need XML or properties file or prefs for this.
				String qualifiedClassName = file.getName().replace(".jar", "");
				try {
					Class pluginClass = loadClass(file, qualifiedClassName);
					Object plugin = pluginClass.newInstance();
					plugins.add((PluginBase) plugin);
				} catch (MalformedURLException e) {
					MessageBox.showErrorDialog(null, "Plugin Loader",
							"Invalid plugin jar file: "
									+ file.getAbsolutePath());
				} catch (ClassNotFoundException e) {
					MessageBox.showErrorDialog(null, "Plugin Loader",
							"Cannot load class: " + qualifiedClassName);
				} catch (IllegalAccessException e) {
					MessageBox.showErrorDialog(null, "Plugin Loader",
							"Cannot access a parameterless constructor of: "
									+ qualifiedClassName);
				} catch (InstantiationException e) {
					MessageBox.showErrorDialog(null, "Plugin Loader",
							"Cannot create an instance of: "
									+ qualifiedClassName);
				} catch (ClassCastException e) {
					MessageBox
							.showErrorDialog(
									null,
									"Plugin Loader",
									qualifiedClassName
											+ " is not an instance of PeriodAnalysisPluginBase");
				} catch (NoClassDefFoundError e) {
					MessageBox.showErrorDialog(null, "Plugin Loader",
							"A class required by " + qualifiedClassName
									+ " was not found: " + e.getMessage());
				}
			}
		}

		return plugins;
	}

	/**
	 * Load a class from the specified full-path to jar file.
	 * 
	 * @param jarFile
	 *            The full path to a jar file.
	 * @param qualifiedClass
	 *            A qualified class name.
	 * @return The loaded class.
	 * @throws MalformedURLException
	 *             If the jar path is not valid.
	 * @throws ClassNotFoundException
	 *             If the class cannot be loaded.
	 */
	private static Class loadClass(File jarFile, String qualifiedClassName)
			throws MalformedURLException, ClassNotFoundException {
		URL url = jarFile.toURI().toURL();
		URL[] urls = new URL[] { url };
		ClassLoader cl = new URLClassLoader(urls, VStar.class.getClassLoader());

		return cl.loadClass(qualifiedClassName);
	}
}
