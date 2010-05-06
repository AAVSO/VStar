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

import javax.swing.UIManager;

import org.aavso.tools.vstar.ui.dialog.MessageBox;
import org.aavso.tools.vstar.util.property.ApplicationProperties;

/**
 * The VStar GUI.
 */
public class VStar {

	public static void main(String[] args) {
		// For Mac OS X, make it look more native by using the screen
		// menu bar. Suggested by Adam Weber.
		try {
			String os_name = System.getProperty("os.name");
			if (os_name.startsWith("Mac OS X")) {
				System.setProperty("apple.laf.useScreenMenuBar", "true");
				System.setProperty(
						"com.apple.mrj.application.apple.menu.about.name",
						"VStar");
			}
		} catch (Exception e) {
			System.err.println("Unable to detect operating system. Exiting.");
			System.exit(1);
		}

		// Set the Look & Feel of the application to be native.
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			System.err.println("Unable to set native look & feel. Exiting.");
			System.exit(1);
		}

		// Schedule a job for the event-dispatching thread:
		// creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}

	/**
	 * Create and display the main window.
	 */
	private static void createAndShowGUI() {
		try {
			MainFrame frame = MainFrame.getInstance();
			final ApplicationProperties appProps = new ApplicationProperties(frame);
			
			int x = appProps.getMainWdwWidth();
			int y = appProps.getMainWdwHeight();
			
			frame.setSize(appProps.getMainWdwWidth(), appProps.getMainWdwHeight());
			frame.setLocation(appProps.getMainWdwUpperLeftX(), appProps.getMainWdwUpperLeftY());

			frame.setVisible(true);

			// We create a shutdown task rather than a window listener
			// to store application properties, otherwise on the Mac, we
			// would also have to trap the VStar (vs File) menu Quit item.
			// This shutdown task should work uniformly across operating
			// systems. The frame stored within appProps cannot be GC'd
			// until appProps is, so its state will still be valid at the
			// time run() is invoked.
			Runnable shutdownTask = new Runnable() {
				public void run() {
					appProps.update();
				}
			};

			Runtime.getRuntime().addShutdownHook(
					new Thread(shutdownTask,
							"Application shutdown task"));
		} catch (Throwable t) {
			MessageBox.showErrorDialog(MainFrame.getInstance(), "Error", t);
		}
	}
}
