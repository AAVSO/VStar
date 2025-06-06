/**
 * VStar: a statistical analysis tool for variable star data.
 * Copyright (C) 2010  AAVSO (http://www.aavso.org/)
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
package org.aavso.tools.vstar.ui.dialog.plugin.manager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.prefs.Preferences;

import javax.swing.SwingUtilities;

import org.aavso.tools.vstar.plugin.IPlugin;
import org.aavso.tools.vstar.ui.VStar;
import org.aavso.tools.vstar.ui.dialog.MessageBox;
import org.aavso.tools.vstar.ui.resources.ResourceAccessor;
import org.aavso.tools.vstar.util.Pair;

/**
 * This class manages plug-in installation, deletion, and update.
 */
public class PluginManager {

	public final static String DEFAULT_PLUGIN_BASE_URL_STR = "https://archive.aavso.org/sites/default/files/vstar-plugins/vstar-plugins-"
			+ ResourceAccessor.getVersionString();

	// public final static String DEFAULT_PLUGIN_BASE_URL_STR =
	// "file:///Users/david/tmp/vstar-plugins/vstar-plugins-"
	// + ResourceAccessor.getVersionString();

	public final static String PLUGINS_LIST_FILE = ".plugins.lst";

	public final static String PLUGINS_DIR = "vstar_plugins";

	public final static String PLUGIN_LIBS_DIR = "vstar_plugin_libs";

	private final static String PLUGIN_PREFS_PREFIX = "PLUGIN_";

	public enum Operation {
		INSTALL, UPDATE;
	}

	private String pluginBaseUrl;

	private static Preferences prefs;

	static {
		// Create preferences node for plug-in management.
		try {
			prefs = Preferences.userNodeForPackage(PluginManager.class);
		} catch (Throwable t) {
			// We need VStar to function in the absence of prefs.
		}
	}

	/**
	 * A mapping from plugin jar name to plugin files available to be installed
	 * for the current version of VStar.
	 */
	private Map<String, URL> remotePlugins;

	/**
	 * A mapping from description to remote plugin jar name.
	 */
	private Map<String, String> remoteDescriptions;

	/**
	 * A mapping from description to remote plugin document names.
	 */
	private Map<String, String> remoteDocNames;
	
	/**
	 * A mapping from plugin jar name to plugin files installed locally.
	 */
	private Map<String, File> localPlugins;

	/**
	 * A mapping from description to local plugin jar name.
	 */
	private Map<String, String> localDescriptions;
	
	/**
	 * A mapping from description to local plugin document names.
	 */
	private Map<String, String> localDocNames;

	/**
	 * A mapping from plugin jar name to dependent library files available to be
	 * installed for the current version of VStar.
	 */
	private Map<String, List<URL>> libs;

	/**
	 * A mapping from description to dependent library jar name.
	 */
	private Map<String, Set<String>> libDescriptions;

	/**
	 * A mapping from dependent library jar name to integers for reference
	 * counting.
	 */
	private Map<String, Integer> libRefs;

	/**
	 * A mapping from description to equality of local and remote plugins
	 * corresponding to the same description.
	 */
	private Map<String, Boolean> remoteAndLocalPluginEquality;

	/**
	 * Has the current operation been interrupted?
	 */
	private boolean interrupted;

	/**
	 * Constructor
	 */
	public PluginManager() {
		pluginBaseUrl = getPluginsBaseUrl();
	}
	
	/**
	 * @return the remote plugins map
	 */
	public Map<String, URL> getRemotePluginsByJarName() {
		return remotePlugins;
	}

	/**
	 * Initialise manager.
	 */
	public void init() {
		retrieveRemotePluginInfo();
		retrieveLocalPluginInfo();
		determinePluginEquality();
	}

	/**
	 * Should plug-ins be loaded according to preferences?
	 */
	public static boolean shouldLoadPlugins() {
		boolean loadPlugins = true;

		try {
			loadPlugins = prefs.getBoolean(
					PLUGIN_PREFS_PREFIX + "LOAD_PLUGINS", true);
		} catch (Throwable t) {
			// We need VStar to function in the absence of prefs.
		}

		return loadPlugins;
	}

	/**
	 * Set the load plug-ins preference.
	 * 
	 * @param state
	 *            The true/false state to set.
	 */
	public static void setLoadPlugins(boolean state) {
		try {
			prefs.putBoolean(PLUGIN_PREFS_PREFIX + "LOAD_PLUGINS", state);
		} catch (Throwable t) {
			// We need VStar to function in the absence of prefs.
		}
	}

	/**
	 * Should all observation source plug-ins be shown in the file menu
	 * according to preferences? Defaults to false.
	 */
	public static boolean shouldAllObsSourcePluginsBeInFileMenu() {
		boolean loadPlugins = true;

		try {
			loadPlugins = prefs.getBoolean(PLUGIN_PREFS_PREFIX
					+ "OBS_SOURCE_PLUGINS_IN_FILE_MENU", false);
		} catch (Throwable t) {
			// We need VStar to function in the absence of prefs.
		}

		return loadPlugins;
	}

	/**
	 * Set whether all observation source plug-ins be shown in the file menu
	 * according to preferences?
	 * 
	 * @param state
	 *            The true/false state to set.
	 */
	public static void setAllObsSourcePluginsInFileMenu(boolean state) {
		try {
			prefs.putBoolean(PLUGIN_PREFS_PREFIX
					+ "OBS_SOURCE_PLUGINS_IN_FILE_MENU", state);
		} catch (Throwable t) {
			// We need VStar to function in the absence of prefs.
		}
	}

	/**
	 * Return the plug-ins base URL according to preferences.
	 */
	public static String getPluginsBaseUrl() {
		String baseUrl = DEFAULT_PLUGIN_BASE_URL_STR;

		try {
			baseUrl = prefs.get(PLUGIN_PREFS_PREFIX + "BASE_URL",
					DEFAULT_PLUGIN_BASE_URL_STR);
		} catch (Throwable t) {
			// We need VStar to function in the absence of prefs.
		}

		return baseUrl;
	}

	/**
	 * Set the plug-ins base URL preference.
	 * 
	 * @param url
	 *            The URL to set.
	 */
	public static void setPluginsBaseUrl(String url) {
		try {
			prefs.put(PLUGIN_PREFS_PREFIX + "BASE_URL", url);
		} catch (Throwable t) {
			// We need VStar to function in the absence of prefs.
		}
	}

	/**
	 * @return the remote plugin map
	 */
	public Map<String, String> getRemoteDescriptionsToJarName() {
		return remoteDescriptions;
	}

	/**
	 * @return the remote plugin descriptions
	 */
	public Set<String> getRemoteDescriptions() {
		return remoteDescriptions.keySet();
	}

	/**
	 * @return the local plugins map
	 */
	public Map<String, File> getLocalPluginsByJarName() {
		return localPlugins;
	}

	/**
	 * @return the local descriptions map
	 */
	public Map<String, String> getLocalDescriptionsToJarName() {
		return localDescriptions;
	}

	/**
	 * @return the local plugin descriptions
	 */
	public Set<String> getLocalDescriptions() {
		return localDescriptions.keySet();
	}

	/**
	 * @return the libs
	 */
	public Map<String, List<URL>> getLibs() {
		return libs;
	}

	/**
	 * @return the plugin document name.
	 */
	public String getPluginDocName(String description) {
		String doc_name = localDocNames.get(description);
		if (doc_name == null || "".equals(doc_name)) {
			doc_name = remoteDocNames.get(description);
		}
		return doc_name;
	}

	/**
	 * Does the plugin description correspond to a remote plugin?
	 * 
	 * @param description
	 *            The description.
	 * @return True if the plugin is remote, false if not.
	 */
	public boolean isRemote(String description) {
		return getRemoteDescriptionsToJarName().containsKey(description);
	}

	/**
	 * Does the plugin description correspond to a local plugin?
	 * 
	 * @param description
	 *            The description.
	 * @return True if the plugin is local, false if not.
	 */
	public boolean isLocal(String description) {
		return getLocalDescriptionsToJarName().containsKey(description);
	}

	/**
	 * Does the plugin description correspond to both a remote and local plugin?
	 * 
	 * @param description
	 *            The description.
	 * @return True if the plugin is both remote and local, false if not.
	 */
	public boolean isRemoteAndLocal(String description) {
		return isLocal(description) && isRemote(description);
	}

	/**
	 * Determine whether plugins that are both local and remote refer to the
	 * same jar and cache this information.
	 */
	public void determinePluginEquality() {
		// First, create a common set consisting of the intersection of remote
		// and local plugins...
		Set<String> remoteDescSet = new HashSet<String>(
				remoteDescriptions.keySet());
		Set<String> localDescSet = new HashSet<String>(
				localDescriptions.keySet());
		Set<String> commonDescSet = new HashSet<String>();

		for (String desc : remoteDescSet) {
			if (isRemoteAndLocal(desc)) {
				commonDescSet.add(desc);
			}
		}

		for (String desc : localDescSet) {
			if (isRemoteAndLocal(desc)) {
				commonDescSet.add(desc);
			}
		}

		// ...then populate the remote+local plugin equality map.
		remoteAndLocalPluginEquality = new HashMap<String, Boolean>();

		for (String desc : commonDescSet) {
			String localJarName = null;
			String remoteJarName = null;
			try {
				localJarName = localDescriptions.get(desc);
				remoteJarName = remoteDescriptions.get(desc);
				URL localUrl = localPlugins.get(localJarName).toURI().toURL();
				URL remoteUrl = remotePlugins.get(remoteJarName);
				remoteAndLocalPluginEquality.put(desc,
						areURLReferentsEqual(localUrl, remoteUrl));
			} catch (IOException e) {
				String msg = String.format(
						"Error comparing remote and local plugins: %s and %s",
						remoteJarName, localJarName);
				throw new PluginManagerException(msg);
			}
		}
	}

	/**
	 * Does the plugin description for a remote and local plugin refer to the
	 * same jar?
	 * 
	 * @param description
	 *            The description.
	 * @return True iff the equal, false if not.
	 */
	public boolean arePluginsEqual(String description) {
		return remoteAndLocalPluginEquality.containsKey(description)
				&& remoteAndLocalPluginEquality.get(description);
	}

	/**
	 * Retrieve information about the available remotePlugins for this version
	 * of VStar.
	 * 
	 * @param baseUrlStr
	 *            The base URL string from where to obtain remotePlugins.
	 */
	public void retrieveRemotePluginInfo(String baseUrlStr) {

		interrupted = false;

		remotePlugins = new TreeMap<String, URL>();
		remoteDescriptions = new TreeMap<String, String>();
		remoteDocNames = new TreeMap<String, String>();
		libs = new TreeMap<String, List<URL>>();
		libDescriptions = new TreeMap<String, Set<String>>();
		libRefs = new HashMap<String, Integer>();

		String[] lines = null;

		try {
			URL infoUrl = new URL(baseUrlStr + "/" + PLUGINS_LIST_FILE);

			URLConnection conn = infoUrl.openConnection();
			
			List<String> lineList = new ArrayList<String>();
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
				String line;
				while ((line = reader.readLine()) != null) {
					lineList.add(line);
				}
			}

			lines = lineList.toArray(new String[0]);

		} catch (Exception e) {
			SwingUtilities.invokeLater( new Runnable() { 
				public void run() {
					MessageBox.showErrorDialog("Plug-in Manager",
							"Error reading remote plug-in information.\nErrror:\n" + 
							e.getLocalizedMessage());
				}
			} );
			return;
		}

		String pluginBaseURLStr = baseUrlStr + "/" + PLUGINS_DIR;
		String libBaseURLStr = baseUrlStr + "/" + PLUGIN_LIBS_DIR;
		
		ArrayList<String> errors = new ArrayList<String>(); 
		
		for (String line : lines) {
			if (interrupted)
				break;

			if (line == null)
				continue;
			
			line = line.trim();
					
			if (line.length() == 0) {
				continue;
			}
			
			if (line.charAt(0) == '#') {
				continue;
			}
				

			// Separate jar file name from dependent libraries, if any exist.
			String[] fields = line.split("\\s*=>\\s*");

			if (fields.length == 0) {
				continue;
			}
			
			// Load plugin, store mappings from jar name to plugin
			// URL, lib URL, and description.
			String pluginJarFileName = fields[0].trim();
			// TODO: remove this after 2.6.10 after which no potential
			// for colon-prefixed jar names will exist
			if (pluginJarFileName.startsWith(":")) {
				// if (!ResourceAccessor.getLoginInfo().isMember()) {
				// // Member-only accessible plug-ins should be skipped
				// // if not appropriately authenticated.
				// continue;
				// } else {
				// Remove leading colon.
				pluginJarFileName = pluginJarFileName.substring(1);
				// }
			}

			String plugin_desc = null;
			String plugin_doc = null;
			URL pluginUrl = null;
			String className = pluginJarFileName.replace(".jar", "");			
			try {
				pluginUrl = new URL(pluginBaseURLStr + "/" + pluginJarFileName);
				// Max: in general, we also need all dependent libs to load plugin files correctly.
				// So we need to get list of plugin's libraries before calling getPluginDescription.
				// This partially duplicates code below so, probably, an optimization could be done.
				// See also the PluginLoader class.
				List<URL> depLibs = new ArrayList<URL>();
				if (fields.length > 1) {
					for (String libFileStr : fields[1].split("\\s*,\\s*")) {
						String libJarFileName = libFileStr.trim();
						URL libUrl = new URL(libBaseURLStr + "/" + libJarFileName);
						depLibs.add(libUrl);							
					}
				}
				Pair<String, String> info = getPluginDescription(pluginUrl, className, depLibs);
				plugin_desc = info.first;
				plugin_doc = info.second;
			} catch (Exception e) {
				//MessageBox.showErrorDialog("Plug-in Manager",
				//	"Error reading remote plug-in information: " + className + ".\nErrror:\n" + 
				//	e.getLocalizedMessage());
				errors.add("Plug-in: " + className + ", Error: " + e.getLocalizedMessage());
				continue;
			} catch (Throwable t) {
				//MessageBox.showErrorDialog("Plug-in Manager",
				//		"Error reading remote plug-in information: " + className + ".\nSevere Error:\n" + 
				//		t.getLocalizedMessage());
				errors.add("Plug-in: " + className + ", Error: " + t.getLocalizedMessage());
				continue;
			}
			
			remoteDescriptions.put(plugin_desc, pluginJarFileName);
			remoteDocNames.put(plugin_desc, plugin_doc);
			remotePlugins.put(pluginJarFileName, pluginUrl);

			// Store dependent libs, if any exist, by plugin key.
			if (fields.length > 1) {
				if (interrupted) 
					break;

				File pluginPath = new File(System.getProperty("user.home") + File.separator + PLUGINS_DIR);
				
				for (String libFileStr : fields[1].split("\\s*,\\s*")) {
					String libJarFileName = libFileStr.trim();
					URL libUrl = null;
					try {
						libUrl = new URL(libBaseURLStr + "/" + libJarFileName);
					} catch (Exception e) {
						//MessageBox.showErrorDialog("Plug-in Manager",
						//	"Error reading remote plug-in information.\nErrror:\n" + 
						//	e.getLocalizedMessage());
						errors.add("Plug-in: " + className + ", Library: " + libJarFileName + ", Error: " + e.getLocalizedMessage());
						break;
					}
					List<URL> libUrls = libs.get(pluginJarFileName);
					if (libUrls == null) {
						libUrls = new ArrayList<URL>();
						libs.put(pluginJarFileName, libUrls);
					}
					libUrls.add(libUrl);

					// Populate dependent library name and reference
					// counting maps. Note that we treat the remote
					// plugin information as the source of truth for the
					// basis of checking against local library jar
					// files. Our reference counting will only be as
					// good as this remote/local correspondence.
					
					File locaPluginJarFilePath = new File(pluginPath, pluginJarFileName);

					if (locaPluginJarFilePath.exists()) {
						if (!libDescriptions.containsKey(plugin_desc)) {
							libDescriptions.put(plugin_desc, new HashSet<String>());
						}
						libDescriptions.get(plugin_desc).add(libJarFileName);
						if (!libRefs.containsKey(libJarFileName)) {
							libRefs.put(libJarFileName, 1);
						} else {
							int count = libRefs.get(libJarFileName) + 1;
							libRefs.put(libJarFileName, count);
						}
					}
				}
			}
		}
		
		if (errors.size() > 0) {
			SwingUtilities.invokeLater( new Runnable() { 
				public void run() {
					String err = String.join("\n", errors);
					MessageBox.showErrorDialog("Plug-in Manager",
							"Error reading remote plug-in information.\nErrors:\n" + err);
				}
			} );
		}
	}

	/**
	 * Retrieve information about the available remotePlugins for this version
	 * of VStar.
	 */
	public void retrieveRemotePluginInfo() {
		retrieveRemotePluginInfo(pluginBaseUrl);
	}

	/**
	 * Retrieve information about locally installed plugins.
	 */
	public void retrieveLocalPluginInfo() {

		interrupted = false;

		localPlugins = new TreeMap<String, File>();
		localDescriptions = new TreeMap<String, String>();
		localDocNames = new TreeMap<String, String>();

		FilenameFilter jarFilter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".jar");
			}
		};

		// Get information about local plugins.
		File pluginPath = new File(System.getProperty("user.home")
				+ File.separator + PLUGINS_DIR);
		File pluginLibPath = new File(System.getProperty("user.home")
				+ File.separator + PLUGIN_LIBS_DIR);

		ArrayList<String> errors = new ArrayList<String>();		
		
		List<URL> depLibs = new ArrayList<URL>();
		if (pluginLibPath.exists() && pluginLibPath.isDirectory()) {
			for (File file : pluginLibPath.listFiles(jarFilter)) {
				try {
					depLibs.add(file.toURI().toURL());
				} catch (Exception e) {
					//MessageBox.showErrorDialog(
					//		"Plug-in Manager",
					//		"Invalid plugin library file.\nError:\n" +
					//		e.getLocalizedMessage());
					errors.add("File: " + file.getName() + ", Error: " + e.getLocalizedMessage());
				}
			}
		}

		if (pluginPath.exists() && pluginPath.isDirectory()) {

			for (File file : pluginPath.listFiles(jarFilter)) {

				if (interrupted)
					break;

				// Load plugin, store mappings from jar name to plugin
				// file and description.
				String plugin_desc = null;
				String plugin_doc = null;
				String pluginJarFileName = file.getName();
				String className = pluginJarFileName.replace(".jar", "");				
				try {
					localPlugins.put(pluginJarFileName, file);
					Pair<String, String> info = getPluginDescription(file.toURI().toURL(), className, depLibs);
					plugin_desc = info.first;
					plugin_doc = info.second;
				} catch (Exception e) {
					//MessageBox.showErrorDialog("Plug-in Manager",
					//	"Error reading local plugin information: " + className + ".\nError:\n" +
					//	e.getLocalizedMessage());
					errors.add("Plug-in class: " + className + ", Error: " + e.getLocalizedMessage());
					continue;
				} catch (Throwable t) {
					//MessageBox.showErrorDialog("Plug-in Manager",
					//	"Error reading remote plug-in information: " + className + ".\nSevere Error:\n" + 
					//	t.getLocalizedMessage());
					errors.add("Plug-in class: " + className + ", Error: " + t.getLocalizedMessage());
					continue;
				}

				localDescriptions.put(plugin_desc, pluginJarFileName);
				localDocNames.put(plugin_desc, plugin_doc);
			}
		}
		if (errors.size() > 0) {
			SwingUtilities.invokeLater( new Runnable() { 
				public void run() {
					String err = String.join("\n", errors);
					MessageBox.showErrorDialog("Plug-in Manager",
							"Error reading local plug-in information.\nErrors:\n" + err);
				}
			} );
		}
	}

	/**
	 * Install the specified plugin and dependent libraries.
	 * 
	 * @param description
	 *            The plugin description corresponding to the remote plugin and
	 *            dependent libraries to be installed.
	 * @param op
	 *            The operation (install or update).
	 */
	public void installPlugin(String description, Operation op) {

		interrupted = false;

		try {
			// Install plugin jars.
			File pluginDirPath = new File(System.getProperty("user.home")
					+ File.separator + PLUGINS_DIR);

			if (!pluginDirPath.exists()) {
				pluginDirPath.mkdir();
			}

			String jarName = remoteDescriptions.get(description);
			URL pluginURL = remotePlugins.get(jarName);
			String pluginJarName = pluginURL.getPath().substring(
					pluginURL.getPath().lastIndexOf("/") + 1);
			File pluginJarFile = new File(pluginDirPath, pluginJarName);
			
			boolean pluginExisted = pluginJarFile.exists();			
			
			copy(pluginURL.openStream(), pluginJarFile);

			// Update maps after copy.
			localDescriptions.put(description, jarName);
			localPlugins.put(jarName, pluginJarFile);
			remoteAndLocalPluginEquality.put(description, true);

			// Install dependent jars.
			File pluginLibDirPath = new File(System.getProperty("user.home")
					+ File.separator + PLUGIN_LIBS_DIR);

			if (!pluginLibDirPath.exists()) {
				pluginLibDirPath.mkdir();
			}

			List<URL> libUrls = libs.get(jarName);
			if (libUrls != null) {
				for (URL libURL : libUrls) {
					if (interrupted)
						break;

					if (libURL != null) {
						String libJarName = libURL.getPath().substring(
								libURL.getPath().lastIndexOf("/") + 1);
						File targetPath = new File(pluginLibDirPath, libJarName);
						copy(libURL.openStream(), targetPath);

						// Library reference counting.
						switch (op) {
						case INSTALL:
							// Bind libJarName with the plug-in 
							if (!libDescriptions.containsKey(description)) {
								libDescriptions.put(description, new HashSet<String>());
							}
							libDescriptions.get(description).add(libJarName);
							// Implies a new dependency on any library file.
							if (!libRefs.keySet().contains(libJarName)) {
								// Actually, this should never occur, libRefs should be
								// already populated in retrieveRemotePluginInfo
								libRefs.put(libJarName, 1);
							} else {
								// Increment refcount ONLY if the plugin had not existed!
								if (!pluginExisted) {
									int count = libRefs.get(libJarName) + 1;
									libRefs.put(libJarName, count);
								}
							}
							break;
						case UPDATE:
							// The only dependency is upon libraries that are
							// additional to the current update of the plugin
							// compared to previous plugin version.
							if (!libRefs.keySet().contains(libJarName)) {
								// Actually, this should never occur, libRefs should be
								// already populated in retrieveRemotePluginInfo
								libRefs.put(libJarName, 1);
							}
							break;
						}
					}
				}
			}
		} catch (IOException e) {
			SwingUtilities.invokeLater( new Runnable() { 
				public void run() {
					MessageBox.showErrorDialog("Plug-in Manager",
						"An error occurred while installing remotePlugins.");
				}
			} );			
		}
	}

	/**
	 * Delete the specified plugin and dependent libraries.
	 * 
	 * @param description
	 *            The plugin description corresponding to the local plugin and
	 *            dependent libraries to be deleted.
	 */
	public void deletePlugin(String description) {

		interrupted = false;

		// Delete plugin jar.
		File pluginDirPath = new File(System.getProperty("user.home")
				+ File.separator + PLUGINS_DIR);

		String jarName = localDescriptions.get(description);
		File pluginJarPath = new File(pluginDirPath, jarName);
		if (pluginJarPath.exists()) {
			if (!pluginJarPath.delete()) {
				SwingUtilities.invokeLater( new Runnable() { 
					public void run() {
						MessageBox.showErrorDialog("Plug-in Manager",
							"Unable to delete plug-in " + jarName);
					}
				} );			
			} else {
				// Update maps after delete.
				localDescriptions.remove(description);
				localPlugins.remove(jarName);
				remoteAndLocalPluginEquality.remove(description);
			}
		} else {
			SwingUtilities.invokeLater( new Runnable() { 
				public void run() {
					MessageBox.showErrorDialog("Plug-in Manager", 
						"Plug-in " + jarName + 
						" does not exist so unable to delete");
				}
			} );			
		}

		// Delete dependent jars for this plug-in.
		File pluginLibDirPath = new File(System.getProperty("user.home")
				+ File.separator + PLUGIN_LIBS_DIR);

		Set<String> libJarNames = libDescriptions.get(description);
		if (libJarNames != null) {
			for (String libJarName : libJarNames) {
				if (interrupted)
					break;

				assert libRefs.containsKey(libJarName);

				libRefs.put(libJarName, libRefs.get(libJarName) - 1);
				// If the reference count for a library jar has fallen to
				// zero, delete the file.
				if (libRefs.get(libJarName) == 0) {
					File libJarPath = new File(pluginLibDirPath, libJarName);
					// This should be true but may not be, given the
					// vagaries of file systems or the possibility of
					// concurrent deletion.
					if (libJarPath.exists()) {
						if (!libJarPath.delete()) {
							String errMsg = String.format(
								"Unable to delete dependent library %s for plug-in %s",
								libJarName, jarName);
							SwingUtilities.invokeLater( new Runnable() { 
								public void run() {
									MessageBox.showErrorDialog("Plug-in Manager", errMsg);
								}
							} );			
							
						}
					} else {
						String errMsg = String.format(
								"The dependent library %s "
										+ "for the plug-in %s cannot be "
										+ "found so cannot be deleted.",
								libJarName, jarName);
						SwingUtilities.invokeLater( new Runnable() { 
							public void run() {
								MessageBox.showErrorDialog("Plug-in Manager", errMsg);								
							}
						} );			
					}
				}
			}
		}

		// Note that to avoid future lib jar clashes,
		// we may need to consider: plugin subdirs in plugin libs dir and a
		// separate class loader per plugin! => SF tracker
	}

	/**
	 * Delete all locally installed plug-ins.
	 */
	public void deleteAllPlugins() {
		// This method called from Preferences synchronously.
		//interrupted = false;
		boolean deleteError = false;
		
		//if (MessageBox.showConfirmDialog("Plug-in Manager",
		//		"Delete all plug-ins?")) {
			try {
				File pluginDirPath = new File(System.getProperty("user.home")
						+ File.separator + PLUGINS_DIR);

				File pluginLibDirPath = new File(
						System.getProperty("user.home") + File.separator
								+ PLUGIN_LIBS_DIR);

				if (pluginDirPath.isDirectory()
						&& pluginLibDirPath.isDirectory()) {
					File[] jarFiles = pluginDirPath.listFiles();
					for (File jarFile : jarFiles) {
						//if (interrupted) {
						//	deleteError = true;
						//	break;
						//}
						// Check existence, to avoid an insanely unlikely race
						// condition.
						if (jarFile.exists()) {
							if (!jarFile.delete()) {
								deleteError = true;
							}
						}
					}

					File[] libJarFiles = pluginLibDirPath.listFiles();
					for (File libJarFile : libJarFiles) {
						if (interrupted) {
							deleteError = true;
							break;
						}
						// Check existence, to avoid an insanely unlikely race
						// condition.
						if (libJarFile.exists()) {
							if (!libJarFile.delete()) {
								deleteError = true;
							}
						}
					}

					// Don't clear collections if null (e.g. as will be the
					// case when invoking this method from prefs pane).
					if (localDescriptions != null) {
						localDescriptions.clear();

						localPlugins.clear();

						libs.clear();
						libDescriptions.clear();
						libRefs.clear();

						remoteAndLocalPluginEquality.clear();
					}

					if (!deleteError)
						MessageBox.showMessageDialog("Plug-in Manager",
								"All installed plug-ins have been deleted.");
					else
						MessageBox.showErrorDialog("Plug-in Manager",
								"Cannot delete some plugins. Try to delete them manually.");
				}

			} catch (Throwable t) {
				MessageBox.showErrorDialog("Plug-in Manager", "Deletion error");
			}
		//}
	}

	/**
	 * Interrupts the current operation.
	 */
	public void interrupt() {
		interrupted = true;
	}

	// Helpers

	private void copy(InputStream in, File file) throws IOException {
		try {
			OutputStream out = new FileOutputStream(file);
			try {
				byte[] buf = new byte[4096];
				int len;
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
			} finally {
				out.close();
			}
		} finally {
			in.close();
		}
	}

	private Pair<String, String> getPluginDescription(URL url, String className, List<URL> depLibs)
		throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		List<URL> urlList = new ArrayList<URL>();
		urlList.add(url);
		urlList.addAll(depLibs);
		URL[] urls = urlList.toArray(new URL[0]);
		URLClassLoader loader = new URLClassLoader(urls, VStar.class.getClassLoader());		
		try {
			Class<?> clazz = loader.loadClass(className);
			IPlugin plugin = (IPlugin) clazz.newInstance();
			return new Pair<String, String>(plugin.getDescription(), plugin.getDocName());
		} finally {
			loader.close();
		}
	}

	private boolean areURLReferentsEqual(URL url1, URL url2) throws IOException {
		return getBytesFromURL(url1).equals(getBytesFromURL(url2));
	}

	private List<Byte> getBytesFromURL(URL url) throws IOException {
		List<Byte> byteList = new ArrayList<Byte>();
		InputStream stream = url.openStream();
		try {
			byte[] buf = new byte[4096];
			int len;
			while ((len = stream.read(buf)) > 0) {
				for (int i = 0; i < len; i++) {
					byteList.add(buf[i]);
				}
			}
		} finally {
			stream.close();
		}
		return byteList;
	}

}
