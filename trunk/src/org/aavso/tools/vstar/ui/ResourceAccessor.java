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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * The purpose of this class is to provide access to non-class resources such as
 * images and html files, and subversion revision number.
 */
public class ResourceAccessor {

	// This file has had its "Revision" keyword property set via:
	// svn propset svn:keywords "Revision" AboutBox.java
	// such that upon all commits, the revision will be updated.
	private static final String REVISION = "$Rev: $";
	private static final String INC = "1"; // change this before each commit

	private static final Pattern revNumPat = Pattern
			.compile("^\\$Rev: (\\d+) \\$$");

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
				// from Eclipse, not from a distribution of vstar.jar.
				url = ResourceAccessor.class.getResource("resources/" + urlStr);
			}

			if (url != null) {
				icon = new ImageIcon(url);
			} else {
				MessageBox.showErrorDialog(MainFrame.getInstance(),
						"VStar About Box", "Can't locate icon: " + urlStr);
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

	public static String getRevNum() {
		// Use whole revision string in case regex match fails.
		String revNum = REVISION;

		Matcher matcher = revNumPat.matcher(REVISION);

		if (matcher.matches()) {
			revNum = matcher.group(1);
		}

		return revNum;
	}

	public static String getParam(int n) {
		assert n >= 0 && n < data.length;
		byte[] bytes = new byte[data[n].length];
		for (int i = 0; i < data[n].length; i++) {
			bytes[i] = (byte) (data[n][i] / pdata[i]);
		}
		return new String(bytes);
	}

	private static int[] hdata = { 18093, 17127, 16137, 16781, 19729, 8878,
			19109, 19303, 24898, 25645, 25197, 12466, 30747, 32034, 29149 };

	private static int[] d0data = { 19234, 19895, 18908, 16781, 20634, 22388,
			19897, 22885, 24476 };

	private static int[] d1data = { 16300, 19722, 19071, 19376, 17557, 20844 };

	private static int[] pdata = { 163, 173, 163, 173, 181, 193, 197, 199, 211,
			223, 227, 271, 277, 281, 283, 293, 307, 383, 389, 401, 409, 419,
			431, 433, 479, 487, 491, 499, 503, 509, 521, 557, 563, 569, 571,
			587, 617, 619, 631, 643, 647, 653, 661, 701, 709, 743, 751, 757,
			761, 769, 839, 853, 857, 859 };

	private static int[][] data = { hdata, d0data, d1data };
}
