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

import java.awt.Image;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.aavso.tools.vstar.ui.dialog.MessageBox;
import org.aavso.tools.vstar.ui.mediator.Mediator;

/**
 * The purpose of this class is to provide access to non-class resources such as
 * images and HTML files, observer code.
 */
public class ResourceAccessor {

	// ** User name, observer code, login type. **

	private static LoginInfo loginInfo = new LoginInfo();

	// ** Image resource accessor. **

	/**
	 * @return the loginInfo
	 */
	public static LoginInfo getLoginInfo() {
		return loginInfo;
	}

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

			if (url == null) {
				// Otherwise, look in resources dir under ui (e.g. if running
				// from Eclipse, not from a distribution of vstar.jar).
				url = ResourceAccessor.class.getResource(urlStr.substring(1));
			}

			if (url != null) {
				icon = new ImageIcon(url);
			} else {
				MessageBox.showErrorDialog(Mediator.getUI().getComponent(),
						"VStar", "Can't locate icon: " + urlStr);
			}
		}

		return icon;
	}

	/**
	 * Returns an image given a resource URL string.
	 * 
	 * @param urlStr
	 *            The URL string.
	 * @return The image, or null if the resource was not found.
	 */
	public static Image getImageResource(String urlStr) {
		Image image = null;

		if (urlStr != null) {
			URL url = ResourceAccessor.class.getResource(urlStr);

			if (url == null) {
				// Otherwise, look in resources dir under ui (e.g. if running
				// from Eclipse, not from a distribution of vstar.jar).
				url = ResourceAccessor.class.getResource(urlStr.substring(1));
			}

			if (url != null) {
				try {
					image = ImageIO.read(url);
				} catch (IOException e) {
					MessageBox.showErrorDialog(Mediator.getUI().getComponent(),
							"VStar", "Can't locate image: " + urlStr);
				}
			} else {
				MessageBox.showErrorDialog(Mediator.getUI().getComponent(),
						"VStar", "Can't locate image: " + urlStr);
			}
		}

		return image;
	}

	// ** HTML help URL resource accessor. ***

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
			// from Eclipse, not from a distribution of vstar.jar).
			url = ResourceAccessor.class
					.getResource("help/html/HelpContents.html");
		}

		return url;
	}

	// ** Version info. **

	public static String getVersionString() {
		return "2.22.0dev";
	}

	public static String getRevNum() {
		return RevisionAccessor.getRevNum();
	}
	
	public static String getBuildTimeStamp() {
		return RevisionAccessor.getBuildTimeStamp();
	}
}
