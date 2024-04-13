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
package org.aavso.tools.vstar.util.help;

import java.awt.Desktop;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;

import org.aavso.tools.vstar.plugin.IPlugin;
import org.aavso.tools.vstar.ui.dialog.MessageBox;

public class Help {

	public static String getAIDWebServiceHelpPage() {
		return "https://github.com/AAVSO/VStar/wiki/Observation-Source-Plug%E2%80%90ins#load-observations-from-the-aavso-internation-database";
	}

	public static String getAAVSOtextFormatHelpPage() {
		return "https://github.com/AAVSO/VStar/wiki/Observation-Source-Plug%E2%80%90ins#aavso-download-and-simple-formats-reader";
	}
	
	public static String getAAVSOtextFormatSinkHelpPage() {
		return "https://github.com/AAVSO/VStar/wiki/Observation-Sink-Plug%E2%80%90ins#aavso-download-and-simple-observation-sinks";
	}

	/**
	 * Open the VStar manual page.
	 */
	public static void openVStarManual() {
		openHelpURLInWebBrowser("https://github.com/AAVSO/VStar/blob/master/doc/user_manual/VStarUserManual.pdf");
	}

	/**
	 * Open the VStar Web page.
	 */
	public static void openVStarWebPage() {
		openHelpURLInWebBrowser("https://www.aavso.org/vstar-overview");
	}
	
	/**
	 * Plug-in help
	 */
	public static void openPluginHelp(String plugin_doc_name) {
		String urlStr = "https://github.com/AAVSO/VStar/tree/master/plugin/doc/";
		if (plugin_doc_name != null) {
			// plugin_doc_name can be a file name (without path) resided in the base plug-in doc directory.
			// In this case it may contain spaces and other special characters.
			// Or it can be a document URL, in this case, spaces and special characters must be properly encoded (i.e. %20 instead of space).
			// First, we try to convert the string into URI.
			// If the conversion was successful, the string is presumably a full document path.
			// If not, consider it as a file name.
			URI uri = getURIfromStringSafe(plugin_doc_name);
			if (uri != null) {
				urlStr = uri.toString();
			}
			else {
				try {
					plugin_doc_name = URLEncoder.encode(plugin_doc_name, "UTF-8").replace("+", "%20");
				} catch (UnsupportedEncodingException ex) {
					plugin_doc_name = "";
				}
				urlStr = urlStr += plugin_doc_name;
			}
		}
		openHelpURLInWebBrowser(urlStr);
	}

	/**
	 * Plug-in help
	 */
	public static void openPluginHelp(IPlugin plugin) {
		openPluginHelp(plugin != null ? plugin.getDocName() : null);
	}

	/**
	 * Open a help page.
	 */
	public static void openHelpURLInWebBrowser(final String urlStr) {
		openURLInWebBrowser(urlStr, "VStar Help");
	}

	/**
	 * Open a web page.
	 */
	public static void openURLInWebBrowser(final String urlStr, final String errorTitle) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				// Try to open a web page in the default web browser.
				if (Desktop.isDesktopSupported()) {
					Desktop desktop = Desktop.getDesktop();
					URL url = null;
					try {
						url = new URL(urlStr);
						if (desktop.isSupported(Desktop.Action.BROWSE)) {
							try {
								desktop.browse(url.toURI());
							} catch (IOException e) {
								MessageBox.showErrorDialog(errorTitle,
										"Error reading from '" + urlStr + "'");
							} catch (URISyntaxException e) {
								MessageBox.showErrorDialog(errorTitle,
										"Invalid address: '" + urlStr + "'");
							}
						}
					} catch (MalformedURLException e) {
						MessageBox.showErrorDialog(errorTitle, "Invalid address.");
					}
				}
			}
		});
	}

	private static URI getURIfromStringSafe(String s) {
		try {
			return (new URL(s)).toURI();
		} catch (MalformedURLException ex) {
			return null;
		} catch (URISyntaxException ex) {
			return null;
		}
	}

}
