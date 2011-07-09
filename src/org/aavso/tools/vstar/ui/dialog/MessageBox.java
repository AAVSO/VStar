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
import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.aavso.tools.vstar.ui.MainFrame;

/**
 * Message dialog box utility class.
 */
public class MessageBox {

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
		JOptionPane pane = new JOptionPane(msg,
				JOptionPane.INFORMATION_MESSAGE, JOptionPane.OK_CANCEL_OPTION, icon);
		JDialog dialog = pane.createDialog(parent, title);
		dialog.setAlwaysOnTop(true);
		dialog.setVisible(true);
	}

	/**
	 * Pop-up an informational message dialog box relative to the main window.
	 * 
	 * @param title
	 *            The title of the dialog.
	 * @param msg
	 *            The message that is the content of the dialog.
	 * @param icon
	 *            The icon to be displayed in the dialog.
	 */
	public static void showMessageDialog(String title, String msg, Icon icon) {
		JOptionPane pane = new JOptionPane(msg,
				JOptionPane.INFORMATION_MESSAGE, JOptionPane.OK_CANCEL_OPTION, icon);
		JDialog dialog = pane.createDialog(MainFrame.getInstance(), title);
		dialog.setAlwaysOnTop(true);
		dialog.setVisible(true);
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
		JOptionPane pane = new JOptionPane(msg,
				JOptionPane.INFORMATION_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
		JDialog dialog = pane.createDialog(parent, title);
		dialog.setAlwaysOnTop(true);
		dialog.setVisible(true);
	}

	/**
	 * Pop-up an informational message dialog box relative to the main window.
	 * 
	 * @param title
	 *            The title of the dialog.
	 * @param msg
	 *            The message that is the content of the dialog.
	 */
	public static void showMessageDialog(String title, String msg) {
		JOptionPane pane = new JOptionPane(msg,
				JOptionPane.INFORMATION_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
		JDialog dialog = pane.createDialog(MainFrame.getInstance(), title);
		dialog.setAlwaysOnTop(true);
		dialog.setVisible(true);
	}

	/**
	 * Pop-up a warning message dialog box.
	 * 
	 * @param parent
	 *            The component to appear with respect to.
	 * @param title
	 *            The title of the dialog.
	 * @param msg
	 *            The message that is the content of the dialog.
	 */
	public static void showWarningDialog(Component parent, String title,
			String msg) {
		JOptionPane pane = new JOptionPane(msg, JOptionPane.WARNING_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION);
		JDialog dialog = pane.createDialog(parent, title);
		dialog.setAlwaysOnTop(true);
		dialog.setVisible(true);
	}

	/**
	 * Pop-up a warning message dialog box relative to the main window.
	 * 
	 * @param title
	 *            The title of the dialog.
	 * @param msg
	 *            The message that is the content of the dialog.
	 */
	public static void showWarningDialog(String title, String msg) {
		JOptionPane pane = new JOptionPane(msg, JOptionPane.WARNING_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION);
		JDialog dialog = pane.createDialog(MainFrame.getInstance(), title);
		dialog.setAlwaysOnTop(true);
		dialog.setVisible(true);
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
		JOptionPane pane = new JOptionPane(msg, JOptionPane.ERROR_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION);
		JDialog dialog = pane.createDialog(parent, title);
		dialog.setAlwaysOnTop(true);
		dialog.setVisible(true);

		// Turn off the wait cursor, in case it's enabled.
		parent.setCursor(null);
	}

	/**
	 * Pop-up an error message dialog box relative to the main window.
	 * 
	 * @param title
	 *            The title of the dialog.
	 * @param msg
	 *            The message that is the content of the dialog.
	 */
	public static void showErrorDialog(String title, String msg) {
		Component parent = MainFrame.getInstance();

		JOptionPane pane = new JOptionPane(msg, JOptionPane.ERROR_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION);
		JDialog dialog = pane.createDialog(parent, title);
		dialog.setAlwaysOnTop(true);
		dialog.setVisible(true);

		// Turn off the wait cursor, in case it's enabled.
		parent.setCursor(null);
	}

	/**
	 * Pop-up an error message dialog box given an exception.
	 * 
	 * @param parent
	 *            The component to appear with respect to.
	 * @param title
	 *            The title of the dialog.
	 * @param e
	 *            The exception whose content will be the content of the dialog.
	 */
	public static void showErrorDialog(Component parent, String title,
			Throwable e) {
		String msg = e.getMessage();
		if (msg == null || msg.length() == 0) {
			msg = e.getClass().getName();
		}

		JOptionPane pane = new JOptionPane(msg, JOptionPane.ERROR_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION);
		JDialog dialog = pane.createDialog(parent, title);
		dialog.setAlwaysOnTop(true);
		dialog.setVisible(true);

		e.printStackTrace();
		
		// Turn off the wait cursor, in case it's enabled.
		parent.setCursor(null);
	}

	/**
	 * Pop-up an error message dialog box given an exception relative to the
	 * main window.
	 * 
	 * @param title
	 *            The title of the dialog.
	 * @param e
	 *            The exception whose content will be the content of the dialog.
	 */
	public static void showErrorDialog(String title, Throwable e) {
		Component parent = MainFrame.getInstance();

		String msg = e.getMessage();
		if (msg == null || msg.length() == 0) {
			msg = e.getClass().getName();
		}

		JOptionPane pane = new JOptionPane(msg, JOptionPane.ERROR_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION);
		JDialog dialog = pane.createDialog(parent, title);
		dialog.setAlwaysOnTop(true);
		dialog.setVisible(true);

		// Turn off the wait cursor, in case it's enabled.
		parent.setCursor(null);
	}
}
