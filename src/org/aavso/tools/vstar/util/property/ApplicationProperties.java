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
package org.aavso.tools.vstar.util.property;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import org.aavso.tools.vstar.ui.MainFrame;
import org.aavso.tools.vstar.ui.dialog.MessageBox;

/**
 * This class creates, reads, and updates VStar properties, e.g. Window location
 * and size. Although there can be more than one instance of this class, it
 * makes most sense for one class (e.g. MainFrame) to have control over just one
 * instance.
 */
public class ApplicationProperties {

	// The full path to the user properties file.
	private final static String PROPS_PATH = System.getProperty("user.home")
			+ File.separator + ".vstar";

	private final static String ERROR_DIALOG_TITLE = "User properties error";
	private final static String PROPS_FILE_COMMENT = "VStar user properties. Read on startup, written on exit.";

	// Property keys.
	private static final String MAIN_WDW_HEIGHT = "main_wdw_height";
	private static final String MAIN_WDW_WIDTH = "main_wdw_width";
	private static final String MAIN_WDW_UPPER_LEFT_X = "main_wdw_upper_left_x";
	private static final String MAIN_WDW_UPPER_LEFT_Y = "main_wdw_upper_left_y";

	// Default values.
	private static final int DEFAULT_MAIN_WDW_HEIGHT = MainFrame.HEIGHT;
	private static final int DEFAULT_MAIN_WDW_WIDTH = MainFrame.WIDTH;
	private static final int DEFAULT_MAIN_WDW_UPPER_LEFT_X = 0;
	private static final int DEFAULT_MAIN_WDW_UPPER_LEFT_Y = 0;

	private MainFrame frame;
	private File propsFile;
	private Properties props;

	/**
	 * Constructor.
	 * 
	 * If the properties file does not exist, create it, otherwise just read it.
	 * 
	 * @param frame
	 *            The main window instance from which to populate initial
	 *            properties.
	 */
	public ApplicationProperties(MainFrame frame) {
		propsFile = new File(PROPS_PATH);
		props = new Properties();

		this.frame = frame;

		try {
			if (!propsFile.exists()) {
				propsFile.createNewFile();
				init();
			} else {
				props.load(new FileReader(propsFile));
			}
		} catch (IOException e) {
			MessageBox.showErrorDialog(frame, ERROR_DIALOG_TITLE, e);
			System.exit(-1);
		}
	}

	// Property getters.

	// If a property value is not present (e.g. when migrating to newer versions
	// of the properties file) or corrupt (e.g. because of hand-editing), a
	// default value is returned.

	public int getMainWdwHeight() {
		Integer height = parseInt(props.getProperty(MAIN_WDW_HEIGHT));
		return height != null ? height : DEFAULT_MAIN_WDW_HEIGHT;
	}

	public int getMainWdwWidth() {
		Integer width = parseInt(props.getProperty(MAIN_WDW_WIDTH));
		return width != null ? width : DEFAULT_MAIN_WDW_WIDTH;
	}

	public int getMainWdwUpperLeftX() {
		Integer x = parseInt(props.getProperty(MAIN_WDW_UPPER_LEFT_X));
		return x != null ? x : DEFAULT_MAIN_WDW_UPPER_LEFT_X;
	}

	public int getMainWdwUpperLeftY() {
		Integer y = parseInt(props.getProperty(MAIN_WDW_UPPER_LEFT_Y));
		return y != null ? y : DEFAULT_MAIN_WDW_UPPER_LEFT_Y;
	}

	/**
	 * Return an integer from the specified string, or null.
	 * We may actually be dealing with a double value, but we
	 * convert to integer in that case.
	 * 
	 * @param s
	 *            The string to be parsed.
	 * @return The integer value in s, or null.
	 */
	private Integer parseInt(String s) {
		Integer n = null;

		try {
			n = Integer.parseInt(s);
		} catch (NumberFormatException e1) {
			try {
				n = (int) Double.parseDouble(s);
			} catch (NumberFormatException e2) {
				// now we're in trouble; not a number: return null
			}
		}

		return n;
	}

	/**
	 * Initialise the application properties and store them.
	 */
	public void init() {
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Dimension screenSize = toolkit.getScreenSize();
		double height = screenSize.getHeight();
		double width = screenSize.getWidth();
		
		props.setProperty(MAIN_WDW_HEIGHT, height + "");
		props.setProperty(MAIN_WDW_WIDTH, width + "");
		props.setProperty(MAIN_WDW_UPPER_LEFT_X, DEFAULT_MAIN_WDW_UPPER_LEFT_X + "");
		props.setProperty(MAIN_WDW_UPPER_LEFT_Y, DEFAULT_MAIN_WDW_UPPER_LEFT_Y + "");
		store();
	}

	/**
	 * Update the application properties and store them.
	 */
	public void update() {
		props.setProperty(MAIN_WDW_HEIGHT, frame.getHeight() + "");
		props.setProperty(MAIN_WDW_WIDTH, frame.getWidth() + "");
		props.setProperty(MAIN_WDW_UPPER_LEFT_X, frame.getX() + "");
		props.setProperty(MAIN_WDW_UPPER_LEFT_Y, frame.getY() + "");
		store();
	}

	private void store() {
		try {
			props.store(new FileWriter(propsFile), PROPS_FILE_COMMENT);
		} catch (IOException e) {
			MessageBox.showErrorDialog(frame, ERROR_DIALOG_TITLE, e);
			System.exit(-1);
		}
	}
}
