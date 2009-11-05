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
package org.aavso.tools.vstar.ui.dialog;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JOptionPane;

import org.aavso.tools.vstar.ui.resources.ResourceAccessor;

/**
 * Message dialog box utility class.
 */
public class MessageBox {

	private final static Icon VSTAR_ICON = ResourceAccessor
			.getIconResource("/images/tenstar_artist_conception1.jpg");

	/**
	 * Pop-up an informational message dialog box.
	 * 
	 * @param parent
	 *            The component to appear with respect to.
	 * @param title
	 *            The title of the dialog.
	 * @param msg
	 *            The message that is the content of the dialog.
	 * @param icon
	 *            The icon to be displayed in the dialog.
	 */
	public static void showMessageDialog(Component parent, String title,
			String msg, Icon icon) {
		JOptionPane.showMessageDialog(parent, msg, title,
				JOptionPane.INFORMATION_MESSAGE, icon);
	}

	/**
	 * Pop-up an informational message dialog box.
	 * 
	 * @param parent
	 *            The component to appear with respect to.
	 * @param title
	 *            The title of the dialog.
	 * @param msg
	 *            The message that is the content of the dialog.
	 */
	public static void showMessageDialog(Component parent, String title,
			String msg) {
		JOptionPane.showMessageDialog(parent, msg, title,
				JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * Pop-up an error message dialog box.
	 * 
	 * @param parent
	 *            The component to appear with respect to.
	 * @param title
	 *            The title of the dialog.
	 * @param msg
	 *            The message that is the content of the dialog.
	 */
	public static void showErrorDialog(Component parent, String title,
			String msg) {
		JOptionPane.showMessageDialog(parent, msg, title,
				JOptionPane.ERROR_MESSAGE);
		parent.setCursor(null); // turn off the wait cursor, in case it's
		// enabled
	}

	/**
	 * Pop-up an error message dialog box given an exception.
	 * 
	 * @param parent
	 *            The component to appear with respect to.
	 * @param title
	 *            The title of the dialog.
	 * @param e
	 *            The exception whose message will be the content of the dialog.
	 */
	public static void showErrorDialog(Component parent, String title,
			Exception e) {
		String msg = e.getMessage();
		if (msg == null || "".equals(msg)) {
			StackTraceElement elt = e.getStackTrace()[e.getStackTrace().length-1];
			msg = "Error in file " + elt.getFileName() + " at line "
					+ elt.getLineNumber();
		}
		JOptionPane.showMessageDialog(parent, msg, title,
				JOptionPane.ERROR_MESSAGE);
		// Turn off the wait cursor, in case it's enabled.
		parent.setCursor(null); 
	}
}
