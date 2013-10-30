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
package org.aavso.tools.vstar.plugin;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.aavso.tools.vstar.ui.dialog.MessageBox;
import org.aavso.tools.vstar.ui.resources.ResourceAccessor;

/**
 * This class manages plug-in installation, deletion, and update.
 */
public class PluginManager {

	public final static String DEFAULT_PLUGIN_BASE_URL_STR = "http://www.aavso.org/sites/default/files/vstar-plugins/vstar-plugins-"
			+ ResourceAccessor.getVersionString();

	public final static String PLUGINS_LIST_FILE = ".plugins.lst";

	public final static String PLUGINS_DIR = "vstar_plugins";

	public final static String PLUGIN_LIBS_DIR = "vstar_plugin_libs";

	/**
	 * A mapping from plugin display name to plugin files available to be
	 * installed for the current version of VStar.
	 */
	private Map<String, URL> plugins;

	/**
	 * A mapping from plugin display name to dependent library files available
	 * to be installed for the current version of VStar.
	 */
	private Map<String, URL> libs;

	/**
	 * A mapping from plugin display name to description.
	 */
	private Map<String, String> descriptions;

	/**
	 * Constructor
	 */
	public PluginManager() {
		// Nothing to do.
	}

	/**
	 * @return the plugins
	 */
	public Map<String, URL> getPlugins() {
		return plugins;
	}

	/**
	 * @return the libs
	 */
	public Map<String, URL> getLibs() {
		return libs;
	}

	/**
	 * @return the descriptions
	 */
	public Map<String, String> getDescriptions() {
		return descriptions;
	}

	/**
	 * Retrieve information about the available plugins for this version of
	 * VStar.
	 * 
	 * @param baseUrlStr
	 *            The base URL string from where to obtain plugins.
	 * @return Was the plugin information successfully obtained?
	 */
	public boolean retrievePluginInfo(String baseUrlStr) {

		boolean success = true;

		plugins = new TreeMap<String, URL>();
		libs = new TreeMap<String, URL>();
		descriptions = new TreeMap<String, String>();

		try {
			URL infoUrl = new URL(baseUrlStr + "/" + PLUGINS_LIST_FILE);

			URLConnection stream = infoUrl.openConnection();
			BufferedInputStream buf = new BufferedInputStream(stream
					.getInputStream());

			String[] lines = readLines(buf);

			String pluginBaseURLStr = baseUrlStr + "/" + PLUGINS_DIR;
			String libBaseURLStr = baseUrlStr + "/" + PLUGIN_LIBS_DIR;

			for (String line : lines) {
				if (line.trim().length() == 0)
					continue;

				// TODO: doesn't load when java source in jar
				if (line.startsWith("my"))
					continue;

				// Separate jar file name from dependent libraries, if any
				// exist.
				String[] fields = line.split("\\s*=>\\s*");

				if (fields.length != 0) {
					// Load plugin, store mappings from display name to plugin
					// URL,
					// lib URL, and description.
					String pluginJarFileName = fields[0];
					URL pluginUrl = new URL(pluginBaseURLStr + "/"
							+ pluginJarFileName);
					String className = pluginJarFileName.replace(".jar", "");
					IPlugin plugin = createObjectFromJarURL(pluginUrl,
							className);
					String pluginKey = plugin.getDisplayName();
					descriptions.put(pluginKey, plugin.getDescription());
					plugins.put(pluginKey, pluginUrl);

					// Store dependent libs, if any exist, by plugin key.
					if (fields.length == 2) {
						for (String libFileStr : fields[1].split("\\s*,\\s*")) {
							String libJarFileName = libFileStr;
							URL libUrl = new URL(libBaseURLStr + "/"
									+ libJarFileName);
							libs.put(pluginKey, libUrl);
						}
					}
				} else {
					MessageBox.showErrorDialog("Plug-in Manager",
							"Error in plug-in information format.");
				}
			}

		} catch (MalformedURLException e) {
			success = false;
			MessageBox.showErrorDialog("Plug-in Manager",
					"Invalid plug-in location.");
		} catch (IOException e) {
			success = false;
			MessageBox.showErrorDialog("Plug-in Manager",
					"Error reading plug-in information.");
		} catch (ClassNotFoundException e) {
			success = false;
			MessageBox.showErrorDialog("Plug-in Manager",
					"Error reading plug-in information.");
		} catch (IllegalAccessException e) {
			success = false;
			MessageBox.showErrorDialog("Plug-in Manager",
					"Error reading plug-in information.");
		} catch (InstantiationException e) {
			success = false;
			MessageBox.showErrorDialog("Plug-in Manager",
					"Error reading plug-in information.");
		}

		return success;
	}

	/**
	 * Install the specified set of plugins and dependent libraries.
	 * 
	 * @param pluginDescs
	 *            The set of plugin descriptions corresponding to the plugins
	 *            and dependent libraries to be installed.
	 */
	public boolean installPlugins(Set<String> pluginDescs) {
		boolean success = true;

		try {
			// Install plugin jars.
			File pluginDirPath = new File(System.getProperty("user.home")
					+ File.separator + "vstar_plugins");

			if (!pluginDirPath.exists()) {
				pluginDirPath.mkdir();
			}

			for (String desc : pluginDescs) {
				URL pluginURL = plugins.get(desc);
				String pluginJarName = pluginURL.getPath().substring(
						pluginURL.getPath().lastIndexOf("/") + 1);
				File outputFile = new File(pluginDirPath, pluginJarName);
				copy(pluginURL.openStream(), outputFile);
			}

			// Install dependent jars.
			File pluginLibDirPath = new File(System.getProperty("user.home")
					+ File.separator + "vstar_plugin_libs");

			if (!pluginLibDirPath.exists()) {
				pluginLibDirPath.mkdir();
			}

			for (String desc : pluginDescs) {
				URL libURL = libs.get(desc);
				if (libURL != null) {
					String libJarName = libURL.getPath().substring(
							libURL.getPath().lastIndexOf("/") + 1);
					File outputFile = new File(pluginLibDirPath, libJarName);
					copy(libURL.openStream(), outputFile);
				}
			}
		} catch (IOException e) {
			success = false;
			MessageBox.showErrorDialog("Plug-in Manager",
					"An error occurred while installing plugins.");

		}

		return success;
	}

	// Helpers

	private String[] readLines(BufferedInputStream stream) throws IOException {

		StringBuffer strBuf = new StringBuffer();
		int len = stream.available();
		while (len > 0) {
			byte[] bytes = new byte[len];
			stream.read(bytes, 0, len);
			strBuf.append(new String(bytes));
			len = stream.available();
		}

		return strBuf.toString().split("\n");
	}

	private void copy(InputStream in, File file) throws IOException {
		OutputStream out = new FileOutputStream(file);
		byte[] buf = new byte[4096];
		int len;
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		out.close();
		in.close();
	}

	private byte[] readBytes(InputStream stream) throws IOException {

		List<byte[]> byteList = new ArrayList<byte[]>();
		int len = stream.available();
		while (len > 0) {
			byte[] bytes = new byte[len];
			stream.read(bytes, 0, len);
			byteList.add(bytes);
			len = stream.available();
		}

		int numBytes = 0;
		for (byte[] bytes : byteList) {
			numBytes += bytes.length;
		}

		// Note: Inefficient! Fix

		byte[] allBytes = new byte[numBytes];
		int i = 0;
		for (byte[] bytes : byteList) {
			for (byte b : bytes) {
				allBytes[i++] = b;
			}
		}

		return allBytes;
	}

	private IPlugin createObjectFromJarURL(URL url, String className)
			throws ClassNotFoundException, IllegalAccessException,
			InstantiationException {
		URLClassLoader loader = URLClassLoader.newInstance(new URL[] { url });
		Class<?> clazz = loader.loadClass(className);
		return (IPlugin) clazz.newInstance();
	}
}
