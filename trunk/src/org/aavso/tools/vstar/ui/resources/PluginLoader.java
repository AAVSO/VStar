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

import org.aavso.tools.vstar.plugin.CustomFilterPluginBase;
import org.aavso.tools.vstar.plugin.GeneralToolPluginBase;
import org.aavso.tools.vstar.plugin.IPlugin;
import org.aavso.tools.vstar.plugin.ModelCreatorPluginBase;
import org.aavso.tools.vstar.plugin.ObservationSourcePluginBase;
import org.aavso.tools.vstar.plugin.ObservationToolPluginBase;
import org.aavso.tools.vstar.plugin.model.impl.ApacheCommonsPolynomialFitCreatorPlugin;
import org.aavso.tools.vstar.plugin.ob.src.impl.TextFormatObservationSourcePlugin;
import org.aavso.tools.vstar.plugin.period.PeriodAnalysisPluginBase;
import org.aavso.tools.vstar.plugin.period.impl.DcDftFrequencyRangePeriodAnalysisPlugin;
import org.aavso.tools.vstar.plugin.period.impl.DcDftPeriodRangePeriodAnalysisPlugin;
import org.aavso.tools.vstar.plugin.period.impl.DcDftStandardScanPeriodAnalysisPlugin;
import org.aavso.tools.vstar.plugin.period.impl.WeightedWaveletZTransformWithFrequencyRangePlugin;
import org.aavso.tools.vstar.plugin.period.impl.WeightedWaveletZTransformWithPeriodRangePlugin;
import org.aavso.tools.vstar.ui.VStar;
import org.aavso.tools.vstar.ui.dialog.MessageBox;

/**
 * This class loads VStar plugins.
 */
public class PluginLoader {

	public final static String VSTAR_PLUGINS_DIR_NAME = "vstar_plugins";
	public final static String VSTAR_PLUGIN_LIBS_DIR_NAME = "vstar_plugin_libs";
	
	// List to store plugins, if any exist.
	private static List<IPlugin> plugins = new ArrayList<IPlugin>();

	/**
	 * Return a list of Period Analysis plugins, whether internal to VStar or
	 * dynamically loaded.
	 */
	public static List<PeriodAnalysisPluginBase> getPeriodAnalysisPlugins() {
		List<PeriodAnalysisPluginBase> periodAnalysisPlugins = new ArrayList<PeriodAnalysisPluginBase>();

		// First, add in-built DC DFT and WWZ plugins.
		periodAnalysisPlugins.add(new DcDftStandardScanPeriodAnalysisPlugin());
		periodAnalysisPlugins
				.add(new DcDftFrequencyRangePeriodAnalysisPlugin());
		periodAnalysisPlugins.add(new DcDftPeriodRangePeriodAnalysisPlugin());

		periodAnalysisPlugins
				.add(new WeightedWaveletZTransformWithFrequencyRangePlugin());
		periodAnalysisPlugins
				.add(new WeightedWaveletZTransformWithPeriodRangePlugin());

		// Next, add all external period analysis plugins.
		for (IPlugin plugin : plugins) {
			if (plugin instanceof PeriodAnalysisPluginBase) {
				periodAnalysisPlugins.add((PeriodAnalysisPluginBase) plugin);
			}
		}

		return periodAnalysisPlugins;
	}

	/**
	 * Return a list of Model Creator plugins, whether internal to VStar or
	 * dynamically loaded.
	 */
	public static List<ModelCreatorPluginBase> getModelCreatorPlugins() {
		List<ModelCreatorPluginBase> modelCreatorPlugins = new ArrayList<ModelCreatorPluginBase>();

		// First, add in-built polynomial fit plugin.
		modelCreatorPlugins.add(new ApacheCommonsPolynomialFitCreatorPlugin());

		// Next, add all external model creator plugins.
		for (IPlugin plugin : plugins) {
			if (plugin instanceof ModelCreatorPluginBase) {
				modelCreatorPlugins.add((ModelCreatorPluginBase) plugin);
			}
		}

		return modelCreatorPlugins;
	}

	/**
	 * Return a list of VStar Observation Tool plugins.
	 */
	public static List<ObservationToolPluginBase> getObservationToolPlugins() {
		List<ObservationToolPluginBase> toolPlugins = new ArrayList<ObservationToolPluginBase>();

		for (IPlugin plugin : plugins) {
			if (plugin instanceof ObservationToolPluginBase) {
				toolPlugins.add((ObservationToolPluginBase) plugin);
			}
		}

		return toolPlugins;
	}

	/**
	 * Return a list of General VStar Tool plugins.
	 */
	public static List<GeneralToolPluginBase> getGeneralToolPlugins() {
		List<GeneralToolPluginBase> toolPlugins = new ArrayList<GeneralToolPluginBase>();

		for (IPlugin plugin : plugins) {
			if (plugin instanceof GeneralToolPluginBase) {
				toolPlugins.add((GeneralToolPluginBase) plugin);
			}
		}

		return toolPlugins;
	}

	/**
	 * Return a list of VStar Custom Filter plugins.
	 */
	public static List<CustomFilterPluginBase> getCustomFilterPlugins() {
		List<CustomFilterPluginBase> customFilterPlugins = new ArrayList<CustomFilterPluginBase>();

		for (IPlugin plugin : plugins) {
			if (plugin instanceof CustomFilterPluginBase) {
				customFilterPlugins.add((CustomFilterPluginBase) plugin);
			}
		}

		return customFilterPlugins;
	}

	/**
	 * Return a list of VStar Observation Source plugins.
	 */
	public static List<ObservationSourcePluginBase> getObservationSourcePlugins() {
		List<ObservationSourcePluginBase> obSourcePlugins = new ArrayList<ObservationSourcePluginBase>();

		// First, add AAVSO Download/simple format reader plug-in.
		obSourcePlugins.add(new TextFormatObservationSourcePlugin());
		
		// Next, add all external observation source plug-ins.
		for (IPlugin plugin : plugins) {
			if (plugin instanceof ObservationSourcePluginBase) {
				obSourcePlugins.add((ObservationSourcePluginBase) plugin);
			}
		}

		return obSourcePlugins;
	}

	/**
	 * Load all VStar plugins and create an instance of each.
	 */
	public static void loadPlugins() {

		FilenameFilter jarFilter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".jar");
			}
		};

		// Locate additional libraries (as jars) on which plugins may be
		// dependent.
		List<URL> depLibs = new ArrayList<URL>();

		File pluginLibPath = new File(System.getProperty("user.home")
				+ File.separator + VSTAR_PLUGIN_LIBS_DIR_NAME);

		if (pluginLibPath.exists() && pluginLibPath.isDirectory()) {
			for (File file : pluginLibPath.listFiles(jarFilter)) {
				try {
					depLibs.add(file.toURI().toURL());
				} catch (MalformedURLException e) {
					MessageBox.showErrorDialog(null, "Plugin Loader",
							"Invalid plugin library jar file: "
									+ file.getAbsolutePath());
				}
			}
		}

		// Locate and store plugins, if any exist.

		File pluginPath = new File(System.getProperty("user.home")
				+ File.separator + VSTAR_PLUGINS_DIR_NAME);

		if (pluginPath.exists() && pluginPath.isDirectory()) {
			for (File file : pluginPath.listFiles(jarFilter)) {
				// Note: Currently assume the jar file name is the same
				// as the qualified class to be loaded. Instead, we could
				// use reflection to find the class implementing one or more
				// IPlugin methods.
				String qualifiedClassName = file.getName().replace(".jar", "");
				try {
					Class<?> clazz = loadClass(file, qualifiedClassName, depLibs);
					Object plugin = clazz.newInstance();
					plugins.add((IPlugin) plugin);
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
					MessageBox.showErrorDialog(null, "Plugin Loader",
							qualifiedClassName
									+ " is not an instance of IPlugin");
				} catch (NoClassDefFoundError e) {
					MessageBox.showErrorDialog(null, "Plugin Loader",
							"A class required by " + qualifiedClassName
									+ " was not found: "
									+ e.getLocalizedMessage());
				} catch (Throwable t) {
					MessageBox.showErrorDialog(null, "Plugin Loader",
							"An error occurred during plugin loading: "
									+ t.getLocalizedMessage());
				}
			}
		}
	}

	/**
	 * Load a class from the specified full-path to jar file.
	 * 
	 * @param jarFile
	 *            The full path to a jar file.
	 * @param qualifiedClass
	 *            A qualified class name.
	 * @param depLibs
	 *            The library (jar) files on which the jar plugin to be loaded
	 *            may be dependent.
	 * @return The loaded class.
	 * @throws MalformedURLException
	 *             If the jar path is not valid.
	 * @throws ClassNotFoundException
	 *             If the class cannot be loaded.
	 */
	private static Class<?> loadClass(File jarFile, String qualifiedClassName,
			List<URL> depLibs) throws MalformedURLException,
			ClassNotFoundException {
		URL url = jarFile.toURI().toURL();
		List<URL> urlList = new ArrayList<URL>();
		urlList.add(url);
		urlList.addAll(depLibs);
		URL[] urls = urlList.toArray(new URL[0]);
		ClassLoader cl = new URLClassLoader(urls, VStar.class.getClassLoader());

		return cl.loadClass(qualifiedClassName);
		// return new PluginClassLoader(urls).loadClass(qualifiedClassName);
	}
}
