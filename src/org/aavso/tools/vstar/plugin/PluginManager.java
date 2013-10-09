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
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.aavso.tools.vstar.ui.dialog.MessageBox;

/**
 * This class manages plug-in installation, deletion, and update.
 */
public class PluginManager {

	public final String DEFAULT_PLUGIN_URL_STR = "http://www.aavso.org/sites/default/files/vstar-plugins/vstar-plugins-2.15.2/";

	/**
	 * A mapping from plugin descriptions to plugin files available to be
	 * installed for the current version of VStar.
	 */
	private Map<String, URL> plugins;

	/**
	 * A mapping from plugin descriptions to dependent library files available
	 * to be installed for the current version of VStar.
	 * */
	private Map<String, URL> libs;

	/**
	 * Constructor
	 */
	public PluginManager() {
		// Nothing to do.
	}

	/**
	 * Retrieve information about the available plugins for this version of
	 * VStar.
	 * 
	 * @param urlStr
	 *            The base URL string from where to obtain plugins.
	 * @return Was the plugin information successfully obtained?
	 */
	public boolean retrievePluginInfo(String urlStr) {

		boolean success = true;

		plugins = new TreeMap<String, URL>();

		try {
			URL url = new URL(urlStr);

			URLConnection stream = url.openConnection();
			BufferedInputStream buf = new BufferedInputStream(stream
					.getInputStream());

			String[] lines = readLines(buf);
			
			
		} catch (MalformedURLException e) {
			success = false;
			MessageBox.showErrorDialog("Plug-in Manager",
					"Invalid plug-in location.");
		} catch (IOException e) {
			success = false;
			MessageBox.showErrorDialog("Plug-in Manager",
					"Error reading plugin information.");
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
	public void installPlugins(Set<String> pluginDescs) {
		for (String desc : pluginDescs) {
			// TODO: get plugins, libs
			URL pluginURL = plugins.get(desc);
		}
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
}
