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
import java.util.logging.Level;

import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.aavso.tools.vstar.scripting.ScriptRunner;
import org.aavso.tools.vstar.ui.VStar;
import org.aavso.tools.vstar.ui.mediator.DocumentManager;
import org.aavso.tools.vstar.ui.mediator.Mediator;

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
				JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION,
				icon);
		JDialog dialog = pane.createDialog(parent, title);
		dialog.setVisible(true);
	}

	/**
	 * Pop-up an informational message dialog box relative to the top-most
	 * window.
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
				JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION,
				icon);
		JDialog dialog = pane.createDialog(DocumentManager.findActiveWindow(),
				title);
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
				JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION);
		JDialog dialog = pane.createDialog(parent, title);
		dialog.setVisible(true);
	}

	/**
	 * Pop-up an informational message dialog box relative to the top-most
	 * window.
	 * 
	 * @param title
	 *            The title of the dialog.
	 * @param msg
	 *            The message that is the content of the dialog.
	 */
	public static void showMessageDialog(String title, String msg) {
		JOptionPane pane = new JOptionPane(msg,
				JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION);
		JDialog dialog = pane.createDialog(DocumentManager.findActiveWindow(),
				title);
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

		VStar.LOGGER.log(Level.WARNING, msg);

	    if (!isScriptingMode()) {
			JOptionPane pane = new JOptionPane(msg,
					JOptionPane.WARNING_MESSAGE, JOptionPane.DEFAULT_OPTION);
			JDialog dialog = pane.createDialog(parent, title);
			dialog.setVisible(true);
		} else {
			ScriptRunner.getInstance().setWarning(msg);
		}
	}

	/**
	 * Pop-up a warning message dialog box relative to the top-most window.
	 * 
	 * @param title
	 *            The title of the dialog.
	 * @param msg
	 *            The message that is the content of the dialog.
	 */
	public static void showWarningDialog(String title, String msg) {
		VStar.LOGGER.log(Level.WARNING, msg);
		
	    if (!isScriptingMode()) {
			JOptionPane pane = new JOptionPane(msg,
					JOptionPane.WARNING_MESSAGE, JOptionPane.DEFAULT_OPTION);
			JDialog dialog = pane.createDialog(
					DocumentManager.findActiveWindow(), title);
			dialog.setVisible(true);
		} else {
			ScriptRunner.getInstance().setWarning(msg);
		}
	}

	/**
	 * Pop-up an error message dialog box.
	 * 
	 * @param parent
	 *            The component to appear with respect to; may be null
	 * @param title
	 *            The title of the dialog.
	 * @param msg
	 *            The message that is the content of the dialog.
	 */
	public static void showErrorDialog(Component parent, String title,
			String msg) {
		
		VStar.LOGGER.log(Level.SEVERE, msg);

		if (!isScriptingMode()) {
			JOptionPane pane = new JOptionPane(msg, JOptionPane.ERROR_MESSAGE,
					JOptionPane.DEFAULT_OPTION);
			JDialog dialog = pane.createDialog(parent, title);
			dialog.setVisible(true);

			// Turn off the wait cursor, in case it's enabled.
			if (parent != null) {
				parent.setCursor(null);
			}
		} else {
			ScriptRunner.getInstance().setError(msg);
		}
	}

	/**
	 * Pop-up an error message dialog box relative to the top-most window.
	 * 
	 * @param title
	 *            The title of the dialog.
	 * @param msg
	 *            The message that is the content of the dialog.
	 */
	public static void showErrorDialog(String title, String msg) {
		VStar.LOGGER.log(Level.SEVERE, msg);
	    
	    if (!isScriptingMode()) {
	    	Component parent = Mediator.getUI().getComponent();

			JOptionPane pane = new JOptionPane(msg, JOptionPane.ERROR_MESSAGE,
					JOptionPane.DEFAULT_OPTION);
			JDialog dialog = pane.createDialog(parent, title);
			dialog.setVisible(true);

			// Turn off the wait cursor, in case it's enabled.
			parent.setCursor(null);
		} else {
			ScriptRunner.getInstance().setError(msg);
		}
	}

	/**
	 * Pop-up an error message dialog box given an exception.
	 * 
	 * @param parent
	 *            The component to appear with respect to; may be null
	 * @param title
	 *            The title of the dialog.
	 * @param e
	 *            The exception whose content will be the content of the dialog.
	 */
	public static void showErrorDialog(Component parent, String title,
			Throwable e) {
		VStar.LOGGER.log(Level.SEVERE, title, e);

		// Getting an error dialog with a NPE is not useful and in my
		// experience, a previous error dialog will have been invoked
		// already. We log the NPE above.
		if (!(e instanceof NullPointerException)) {
			if (!isScriptingMode()) {
				String msg = e.getClass().getName() + ": "
						+ e.getLocalizedMessage();

				JOptionPane pane = new JOptionPane(msg,
						JOptionPane.ERROR_MESSAGE, JOptionPane.DEFAULT_OPTION);
				JDialog dialog = pane.createDialog(parent, title);
				dialog.setVisible(true);

				// e.printStackTrace();

				// Turn off the wait cursor, in case it's enabled.
				if (parent != null) {
					parent.setCursor(null);
				}
			}
		} else {
			ScriptRunner.getInstance().setError(e.getLocalizedMessage());
		}
	}

	/**
	 * Pop-up an error message dialog box given an exception relative to the
	 * top-most window.
	 * 
	 * @param title
	 *            The title of the dialog.
	 * @param e
	 *            The exception whose content will be the content of the dialog.
	 */
	public static void showErrorDialog(String title, Throwable e) {
		VStar.LOGGER.log(Level.SEVERE, title, e);

		if (!isScriptingMode()) {
			// Getting an error dialog with a NPE is not useful and in my
			// experience, a previous error dialog will have been invoked
			// already. We log the NPE above.
			if (!(e instanceof NullPointerException)) {
				Component parent = DocumentManager.findActiveWindow();

				String msg = e.getClass().getName() + ": "
						+ e.getLocalizedMessage();

				JOptionPane pane = new JOptionPane(msg,
						JOptionPane.ERROR_MESSAGE, JOptionPane.DEFAULT_OPTION);
				JDialog dialog = pane.createDialog(parent, title);
				dialog.setVisible(true);

				// e.printStackTrace();

				// Turn off the wait cursor, in case it's enabled.
				parent.setCursor(null);
			}
		} else {
			ScriptRunner.getInstance().setError(e.getLocalizedMessage());
		}
	}

	/**
	 * Pop-up a confirmation message dialog box and return the selection result.
	 * 
	 * @param title
	 *            The title of the dialog.
	 * @param msg
	 *            The message that is the content of the dialog.
	 * 
	 * @return True if "yes" was selected, false if "no" was selected.
	 */
	public static boolean showConfirmDialog(String title, String msg) {
		if (!isScriptingMode()) {
			int result = JOptionPane.showConfirmDialog(
					DocumentManager.findActiveWindow(), msg, title,
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

			return result == JOptionPane.YES_OPTION;
		} else {
			return true;
		}
	}
	
	// Helpers
	
	private static boolean isScriptingMode() {
		boolean isScriptingMode = false;
		
	    // PMAK 2020-04-18: 
	    // "ui" can be null when the dialog invoked at startup while loading plugins.
	    // When a plugin's constructor throws an exception, "ui" is not created yet.
	    // So NullPointerException is thrown if we do not check for null.
	    // This prevents VStar from starting at all.
	    if (Mediator.getUI() != null) {
	        isScriptingMode = Mediator.getUI().isScriptingMode();
	    }
	    
	    return isScriptingMode;
	}
}
