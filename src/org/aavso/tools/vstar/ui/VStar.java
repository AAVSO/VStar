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

import java.io.File;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.URL;
import java.security.Policy;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.swing.UIManager;

import org.aavso.tools.vstar.scripting.ScriptRunner;
import org.aavso.tools.vstar.ui.dialog.MessageBox;
import org.aavso.tools.vstar.ui.dialog.plugin.manager.PluginManager;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.resources.PluginLoader;
import org.aavso.tools.vstar.util.property.ApplicationProperties;

/**
 * The VStar GUI.
 */
public class VStar {

	public static final String LOG_DIR = System.getProperty("user.home")
			+ File.separator + "vstar_log";

	public static final String LOG_PATH = LOG_DIR + File.separator + "vstar.log";
	
	public static Logger LOGGER;

	static {
		try {
			File logDir = new File(LOG_DIR);
			if (!logDir.isDirectory()) {
				logDir.mkdir();
			}
			Handler fh = new FileHandler(LOG_PATH);
			fh.setFormatter(new SimpleFormatter());
			LOGGER = Logger.getLogger("VStar Logger");
			LOGGER.setUseParentHandlers(false);
			LOGGER.addHandler(fh);

		} catch (Exception e) {
			// Default to console?
		}
	}

	private static boolean loadPlugins = true;
	
	private static boolean runScript = false;
	private static String scriptPath = null;

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
			} else if (os_name.startsWith("Windows")) {
				// Under Windows, the default TextArea font is too small.
				// This fixes the issue [https://stackoverflow.com/questions/6461506/jtextarea-default-font-very-small-in-windows]
				UIManager.getDefaults().put("TextArea.font", UIManager.getFont("TextField.font"));
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

		// If there's no command-line option that says we shouldn't load
		// plug-ins and the plug-in manager says it's okay to load them, then go
		// ahead.
		if (loadPlugins && PluginManager.shouldLoadPlugins()) {
			// Load plug-ins, if any exist and plug-in loading is enabled.
			PluginLoader.loadPlugins();
		}

		// Create an uncaught exception handler.
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			public void uncaughtException(Thread th, Throwable ex) {
				// LOGGER.log(Level.SEVERE, "Uncaught Exception", ex);
				MessageBox.showErrorDialog("Error", ex);
			}
		});

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

			frame.setSize(appProps.getMainWdwWidth(),
					appProps.getMainWdwHeight());
			frame.setLocation(appProps.getMainWdwUpperLeftX(),
					appProps.getMainWdwUpperLeftY());

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
					new Thread(shutdownTask, "Application shutdown task"));
			
			if (scriptPath != null) {
				new ScriptRunner(false).runScript(new File(scriptPath));
			}

		} catch (Throwable t) {
			MessageBox.showErrorDialog(Mediator.getUI().getComponent(),
					"Error", t.getLocalizedMessage());
		}
	}

	/**
	 * Process the command-line arguments. Note: If we do anything more complex
	 * than this, consideration should be given to using a library such as:
	 * http://commons.apache.org/cli/
	 * 
	 * @param args
	 *            The command-line arguments; may be empty.
	 */
	private static void processCmdLineArgs(String[] args) {
		for (String arg : args) {
			if ("--help".equals(arg)) {
				System.out.println("usage: vstar [--noplugins] [--script path]");
				System.exit(0);
			} else if ("--noplugins".equals(arg)) {
				loadPlugins = false;
			} else if ("--script".equals(arg)) {
				runScript = true;
			} else if (runScript) {
				scriptPath = arg;
			}
		}
	}
}
