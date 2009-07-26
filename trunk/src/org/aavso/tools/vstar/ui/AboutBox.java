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

import java.awt.Component;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Icon;

/**
 * This class creates and displays VStar's About Box.
 * 
 * TODO: create a JDialog with image and panel components instead of what we do
 * here so we have more control over the form and content.
 */
public class AboutBox {

	// Comment by Aaron in email (6 July 2009):
	//	
	// I think you can use anything in HOA or the Citizen Sky web site.
	// However, you'll need to retain whatever credit is shown in VSA and
	// maybe add "as appeared in VSA at
	// "http://www.aavso.org/education/vsa/".
	//
	// Add a credit to the National Science Foundation to the About
	// box (if you haven't already). A good NSF logo is here:
	// http://www.nsf.gov/policies/logos.jsp
	//
	// The NSF credit should be something like "This project was funded in
	// part by grant No. 000379097 from the National Science Foundation."

	// This file has had its "Revision" keyword property set via:
	// svn propset svn:keywords "Revision" AboutBox.java
	// such that upon all commits, the revision will be updated.
	private static final String REVISION = "$Rev$";

	private static final Pattern revNumPat = Pattern
			.compile("^\\$Rev: (\\d+) \\$$");

	public static void showAboutBox(Component parent) {
		StringBuffer strBuf = new StringBuffer();
		strBuf.append("VStar (revision ");
		strBuf.append(getRevNum());
		strBuf.append(")\n\n");

		strBuf.append("A variable star observation data analysis tool\n");
		strBuf.append("developed for:\n\n");
		strBuf.append("  The American Association of Variable Star\n");
		strBuf.append("  Observers: http://www.aavso.org/\n\n");
		strBuf.append("as part of\n\n");
		strBuf
				.append("  The CitizenSky Project: http://www.citizensky.org/\n\n");

		strBuf
				.append("This project was funded in part by grant No. 000379097\n");
		strBuf.append("from the National Science Foundation.\n\n");

		strBuf.append("Code by: David Benn\n");
		strBuf.append("Contact: aavso@aavso.org\n");
		strBuf.append("License: GNU Affero General Public License\n\n");
		strBuf.append("Images as appeared in Variable Star Astronomy at\n");
		strBuf.append("http://www.aavso.org/education/vsa/\n\n");

		strBuf
				.append("Thanks to the staff of AAVSO for their support, in particular:\n\n");
		strBuf.append(" Sara Beck, Arne Henden, Doc Kinne, Aaron Price,\n");
		strBuf
				.append(" Matt Templeton, Rebecca Turner, and Elizabeth Waagen.\n\n");

		strBuf.append("and to the following people for testing VStar:\n\n");
		strBuf.append("  Michael Umbricht (and others to be added).");

		MessageBox.showMessageDialog(parent, "About VStar", strBuf.toString(),
				getIcon("/images/tenstar_artist_conception1.jpg", parent));
	}

	// Helpers

	private static String getRevNum() {
		// Use whole revision string in case regex match fails.
		String revNum = REVISION;

		Matcher matcher = revNumPat.matcher(REVISION);

		if (matcher.matches()) {
			revNum = matcher.group(1);
		}

		return revNum;
	}

	private static Icon getIcon(String path, Component parent) {
		Icon icon = ResourceAccessor.getIconResource(path);

		if (icon == null) {
			MessageBox.showErrorDialog(parent, "VStar About Box",
					"Can't locate icon: " + path);
		}

		return icon;
	}
}
