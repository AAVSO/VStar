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
import java.security.Policy;

import javax.swing.UIManager;

import org.aavso.tools.vstar.input.database.AAVSODatabaseConnector;
import org.aavso.tools.vstar.ui.dialog.MessageBox;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.resources.PluginLoader;
import org.aavso.tools.vstar.util.property.ApplicationProperties;

/**
 * The VStar GUI.
 */
public class VStar {

	private static boolean loadPlugins = true;

	public static void main(String[] args) {
		// Apply VStar Java policy for all code.
		URL policyUrl = Thread.currentThread().getContextClassLoader()
				.getResource("/etc/vstar.java.policy");
		Policy.getPolicy().refresh();

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

		processCmdLineArgs(args);
		
		if (loadPlugins) {
			// Load plugins, if any exist and plugin loading is enabled.
			PluginLoader.loadPlugins();
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
			MainFrame frame = new MainFrame();
			final ApplicationProperties appProps = new ApplicationProperties(
					frame);

			frame.setSize(appProps.getMainWdwWidth(), appProps
					.getMainWdwHeight());
			frame.setLocation(appProps.getMainWdwUpperLeftX(), appProps
					.getMainWdwUpperLeftY());

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
					AAVSODatabaseConnector.cleanup();
				}
			};

			Runtime.getRuntime().addShutdownHook(
					new Thread(shutdownTask, "Application shutdown task"));
		} catch (Throwable t) {
			MessageBox.showErrorDialog(Mediator.getUI().getComponent(), "Error", t);
		}
	}

	/**
	 * Process the command-line arguments. Note: If we do anything more complex
	 * than this, consideration should be given to using a library such as:
	 * http://commons.apache.org/cli/
	 * 
	 * @param args The command-line arguments; may be empty.
	 */
	private static void processCmdLineArgs(String[] args) {
		for (String arg : args) {
			if ("--help".equals(arg)) {
				System.out.println("usage: vstar [--noplugins]");
				System.exit(0);
			} else if ("--noplugins".equals(arg)) {
				loadPlugins = false;
			}
		}
	}
}
