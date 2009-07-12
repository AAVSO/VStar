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

import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * The purpose of this class is to provide access to non-class resources such as
 * images and html files.
 */
public class ResourceAccessor {

	/**
	 * Returns an image icon given a resource URL string.
	 * 
	 * @param urlStr
	 *            The URL string.
	 * @return The icon, or null if the resource was not found.
	 */
	public static Icon getIconResource(String urlStr) {
		Icon icon = null;

		if (urlStr != null) {
			URL url = ResourceAccessor.class.getResource(urlStr);
			if (url != null) {
				icon = new ImageIcon(url);
			}
		}

		return icon;
	}

	/**
	 * Returns the HTML help resource URL.
	 * 
	 * @return The URL of the HTML help file.
	 */
	public static URL getHelpHTMLResource() {
		// This is where it will be in vstar.jar (see build.xml).
		URL url = ResourceAccessor.class
				.getResource("/help/html/HelpContents.html");

		if (url == null) {
			// Otherwise, look in resources dir under ui (e.g. if running
			// from Eclipse, not from a distribution of vstar.jar.
			url = ResourceAccessor.class
					.getResource("resources/help/html/HelpContents.html");
		}

		return url;
	}
}
