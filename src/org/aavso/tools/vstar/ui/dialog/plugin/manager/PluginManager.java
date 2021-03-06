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
import java.net.MalformedURLException;
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

import org.aavso.tools.vstar.plugin.IPlugin;
import org.aavso.tools.vstar.ui.VStar;
import org.aavso.tools.vstar.ui.dialog.MessageBox;
import org.aavso.tools.vstar.ui.resources.ResourceAccessor;
import org.aavso.tools.vstar.util.Pair;

/**
 * This class manages plug-in installation, deletion, and update.
 */
public class PluginManager {

	public final static String DEFAULT_PLUGIN_BASE_URL_STR = "https://www.aavso.org/sites/default/files/vstar-plugins/vstar-plugins-"
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
	 * A mapping from plugin jar name to plugin files installed locally.
	 */
	private Map<String, File> localPlugins;

	/**
	 * A mapping from description to local plugin jar name.
	 */
	private Map<String, String> localDescriptions;

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

		} catch (MalformedURLException e) {
			MessageBox.showErrorDialog("Plug-in Manager",
					"Invalid remote plug-in location.");
		} catch (IOException e) {
			MessageBox.showErrorDialog("Plug-in Manager",
					"Error reading remote plug-in information.");
		}

		String pluginBaseURLStr = baseUrlStr + "/" + PLUGINS_DIR;
		String libBaseURLStr = baseUrlStr + "/" + PLUGIN_LIBS_DIR;

		for (String line : lines) {
			try {
				if (interrupted)
					break;

				if (line.trim().length() == 0) {
					continue;
				}

				// Separate jar file name from dependent libraries, if any
				// exist.
				String[] fields = line.split("\\s*=>\\s*");

				if (fields.length != 0) {
					// Load plugin, store mappings from jar name to plugin
					// URL, lib URL, and description.
					String pluginJarFileName = fields[0];
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

					URL pluginUrl = new URL(pluginBaseURLStr + "/"
							+ pluginJarFileName);
					String className = pluginJarFileName.replace(".jar", "");

					//Max: get class loader with plugin and close it at the end!
                    // https://docs.oracle.com/javase/8/docs/technotes/guides/net/ClassLoader.html
					Pair<IPlugin, URLClassLoader> plugin_cl = createObjectFromJarURL(pluginUrl,
							className);
					try {
						remoteDescriptions.put(plugin_cl.first.getDescription(),
								pluginJarFileName);
						remotePlugins.put(pluginJarFileName, pluginUrl);

						// Store dependent libs, if any exist, by plugin key.
						if (fields.length == 2) {
							if (interrupted)
								break;

							File pluginLibDirPath = new File(
									System.getProperty("user.home")
										+ File.separator + PLUGIN_LIBS_DIR);

							for (String libFileStr : fields[1].split("\\s*,\\s*")) {
								String libJarFileName = libFileStr;
								URL libUrl = new URL(libBaseURLStr + "/"
										+ libJarFileName);
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
								File localLibJarFilePath = new File(
										pluginLibDirPath, libJarFileName);

								if (localLibJarFilePath.exists()) {
									if (!libDescriptions.containsKey(plugin_cl.first
											.getDescription())) {
										libDescriptions.put(
												plugin_cl.first.getDescription(),
												new HashSet<String>());
									}
									libDescriptions.get(plugin_cl.first.getDescription())
										.add(libJarFileName);

									if (!libRefs.containsKey(libJarFileName)) {
										libRefs.put(libJarFileName, 1);
									} else {
										int count = libRefs.get(libJarFileName) + 1;
										libRefs.put(libJarFileName, count);
									}
								}
							}
						}
					} finally {
						try {
							plugin_cl.second.close();
						} catch (IOException ex) {
							MessageBox.showErrorDialog("Plug-in Manager",
									"Error closing ClassLoader.");
						}
					}
				} else {
					MessageBox.showErrorDialog("Plug-in Manager",
							"Error in plug-in information format.");
				}
			} catch (MalformedURLException e) {
				MessageBox.showErrorDialog("Plug-in Manager",
						"Invalid remote plug-in location.");
			} catch (ClassNotFoundException e) {
				MessageBox.showErrorDialog("Plug-in Manager",
						"Error reading remote plug-in information.");
			} catch (IllegalAccessException e) {
				MessageBox.showErrorDialog("Plug-in Manager",
						"Error reading remote plug-in information.");
			} catch (InstantiationException e) {
				MessageBox.showErrorDialog("Plug-in Manager",
						"Error reading remote plug-in information.");
			}
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

		FilenameFilter jarFilter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".jar");
			}
		};

		// Get information about local plugins.
		File pluginPath = new File(System.getProperty("user.home")
				+ File.separator + PLUGINS_DIR);

		if (pluginPath.exists() && pluginPath.isDirectory()) {

			for (File file : pluginPath.listFiles(jarFilter)) {

				if (interrupted)
					break;

				try {
					// Load plugin, store mappings from jar name to plugin
					// file and description.
					String pluginJarFileName = file.getName();
					localPlugins.put(pluginJarFileName, file);
					String className = pluginJarFileName.replace(".jar", "");

                    // https://docs.oracle.com/javase/8/docs/technotes/guides/net/ClassLoader.html 
					Pair<IPlugin, URLClassLoader> plugin_cl = createObjectFromJarURL(file.toURI()
							.toURL(), className);
					try {
						localDescriptions.put(plugin_cl.first.getDescription(),
								pluginJarFileName);
					} finally {
						try {
							plugin_cl.second.close();
						} catch (IOException ex) {
							MessageBox.showErrorDialog("Plug-in Manager",
									"Error closing ClassLoader.");
						}
					}
				} catch (MalformedURLException e) {
					MessageBox.showErrorDialog("Plug-in Manager",
							"Invalid local plug-in location.");
				} catch (ClassNotFoundException e) {
					MessageBox.showErrorDialog("Plug-in Manager",
							"Error reading local plug-in information.");
				} catch (IllegalAccessException e) {
					MessageBox.showErrorDialog("Plug-in Manager",
							"Error reading local plug-in information.");
				} catch (InstantiationException e) {
					MessageBox.showErrorDialog("Plug-in Manager",
							"Error reading local plug-in information.");
				}
			}
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
							// Implies a new dependency on any library file.
							if (!libRefs.keySet().contains(libJarName)) {
								libRefs.put(libJarName, 1);
							} else {
								int count = libRefs.get(libJarName) + 1;
								libRefs.put(libJarName, count);
							}
							break;
						case UPDATE:
							// The only dependency is upon libraries that are
							// additional to the current update of the plugin
							// compared to previous plugin version.
							if (!libRefs.keySet().contains(libJarName)) {
								libRefs.put(libJarName, 1);
							}
							break;
						}
					}
				}
			}
		} catch (IOException e) {
			MessageBox.showErrorDialog("Plug-in Manager",
					"An error occurred while installing remotePlugins.");
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
				MessageBox.showErrorDialog("Plug-in Manager",
						"Unable to delete plug-in " + jarName);
			} else {
				// Update maps after delete.
				localDescriptions.remove(description);
				localPlugins.remove(jarName);
				remoteAndLocalPluginEquality.remove(description);
			}
		} else {
			MessageBox.showErrorDialog("Plug-in Manager", "Plug-in " + jarName
					+ " does not exist so unable to delete");
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
						// if (libJarPath.delete()) {
						// String errMsg = String.format(
						// "Unable to delete dependent library %s "
						// + "for plug-in %s", libJarName,
						// jarName);
						// throw new PluginManagerException(errMsg);
						// }
					} else {
						String errMsg = String.format(
								"The dependent library %s "
										+ "for the plug-in %s cannot be "
										+ "found so cannot be deleted.",
								libJarName, jarName);
						MessageBox.showErrorDialog("Plug-in Manager", errMsg);
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
		interrupted = false;
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
						if (interrupted) {
							deleteError = true;
							break;
						}
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

	// return class loader here!
	private Pair<IPlugin, URLClassLoader> createObjectFromJarURL(URL url, String className)
			throws ClassNotFoundException, IllegalAccessException,
			InstantiationException {
		URLClassLoader loader = new URLClassLoader(new URL[] { url },
				VStar.class.getClassLoader());
		Class<?> clazz = loader.loadClass(className);
		IPlugin plugin = (IPlugin) clazz.newInstance(); 
		return new Pair<IPlugin, URLClassLoader>(plugin, loader); 
	}

	private boolean areURLReferentsEqual(URL url1, URL url2) throws IOException {
		return getBytesFromURL(url1).equals(getBytesFromURL(url2));
	}

	private List<byte[]> getBytesFromURL(URL url) throws IOException {
		List<byte[]> byteList = new ArrayList<byte[]>();
		InputStream stream = url.openStream();
		try {
			int len = stream.available();
			while (len > 0) {
				byte[] bytes = new byte[len];
				stream.read(bytes, 0, len);
				byteList.add(bytes);
				len = stream.available();
			}
		} finally {
			stream.close();
		}

		return byteList;
	}
}
