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
import java.util.prefs.Preferences;

import org.aavso.tools.vstar.ui.MainFrame;
import org.aavso.tools.vstar.ui.dialog.MessageBox;

/**
 * This class creates, reads, and updates VStar properties, e.g. Window location
 * and size. Although there can be more than one instance of this class, it
 * makes most sense for one class to have control over just one instance.
 */
public class ApplicationProperties {

	private final static String ERROR_DIALOG_TITLE = "User properties error";

	// Property keys.
	private static final String MAIN_WDW_HEIGHT = "main_wdw_height";
	private static final String MAIN_WDW_WIDTH = "main_wdw_width";
	private static final String MAIN_WDW_UPPER_LEFT_X = "main_wdw_upper_left_x";
	private static final String MAIN_WDW_UPPER_LEFT_Y = "main_wdw_upper_left_y";

	// Default values.
	private static final int DEFAULT_MIN_MAINWDW_HEIGHT = 600;
	private static final int DEFAULT_MIN_MAINWDW_WIDTH = 800;
	
	// Defaults initialized in constructor
	private int defaultMainWDWheight;
	private int defaultMainWDWwidth;
	private int defaultMainWDWupperLeftX;
	private int defaultMainWDWupperLeftY;
	
	// screen size
	private int screenHeight;
	private int screenWidth;
	
	private final static String PREFS_PREFIX = "VSTAR_APPLICATION_PROPERTIES_";
	
	private MainFrame frame;
	
	private Preferences prefs;
	
	
	/**
	 * Constructor.
	 * 
	 * If the properties file does not exist, create it.
	 * 
	 * @param frame
	 *            The main window instance from which to populate initial
	 *            properties.
	 */
	public ApplicationProperties(MainFrame frame) {
		// Create preferences node.
		try {
			prefs = Preferences.userNodeForPackage(ApplicationProperties.class);
		} catch (Throwable t) {
			// We need VStar to function in the absence of prefs.
			prefs = null;
		}
		this.frame = frame;
		// Tune default values
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Dimension screenSize = toolkit.getScreenSize();
		screenHeight = screenSize.height;
		screenWidth = screenSize.width;
		
		// if prefs == null (e.g. VStar invoked via Web Start) we  provide some reasonable defaults.		
		defaultMainWDWheight = Math.min(Math.max(MainFrame.HEIGHT, screenHeight / 6 * 5), screenHeight);
		defaultMainWDWwidth = Math.min(Math.max(MainFrame.WIDTH, screenWidth / 6 * 5), screenWidth);
		defaultMainWDWupperLeftX = (screenWidth - defaultMainWDWwidth) / 2;
		defaultMainWDWupperLeftY = (screenHeight - defaultMainWDWheight) / 2;
	}

	// Property getters.
	
	public int getMainWdwHeight() {
		int height = prefs == null ? 
				defaultMainWDWheight : 
					prefs.getInt(PREFS_PREFIX + MAIN_WDW_HEIGHT, defaultMainWDWheight);
		if (height > screenHeight)
			height = screenHeight;
		if (height < DEFAULT_MIN_MAINWDW_HEIGHT)
			height = Math.min(DEFAULT_MIN_MAINWDW_HEIGHT, screenHeight);
		return height;
	}

	public int getMainWdwWidth() {
		int width = prefs == null ? 
				defaultMainWDWwidth : 
					prefs.getInt(PREFS_PREFIX + MAIN_WDW_WIDTH, defaultMainWDWwidth);
		if (width > screenWidth)
			width = screenWidth;
		if (width < DEFAULT_MIN_MAINWDW_WIDTH)
			width = Math.min(DEFAULT_MIN_MAINWDW_WIDTH, screenWidth); 
		return width;
	}

	public int getMainWdwUpperLeftX() {
		int x = prefs == null ? 
				defaultMainWDWupperLeftX : 
					prefs.getInt(PREFS_PREFIX + MAIN_WDW_UPPER_LEFT_X, defaultMainWDWupperLeftX);
		if (x < 0)
			x = 0;
		else if (x + 20 > screenWidth)
			x = defaultMainWDWupperLeftX;
		return x;
	}

	public int getMainWdwUpperLeftY() {
		int y = prefs == null ? 
				defaultMainWDWupperLeftY : 
					prefs.getInt(PREFS_PREFIX + MAIN_WDW_UPPER_LEFT_Y, defaultMainWDWupperLeftY);
		if (y < 0)
			y = 0;
		else if (y + 20 > screenHeight)
			y = defaultMainWDWupperLeftY;
		return y;
	}

	
	/**
	 * Update the application properties and store them.
	 */
	public void update() {
		if (prefs != null) {
			try {
				prefs.putInt(PREFS_PREFIX + MAIN_WDW_HEIGHT, frame.getHeight());
				prefs.putInt(PREFS_PREFIX + MAIN_WDW_WIDTH, frame.getWidth());
				prefs.putInt(PREFS_PREFIX + MAIN_WDW_UPPER_LEFT_X, frame.getX());
				prefs.putInt(PREFS_PREFIX + MAIN_WDW_UPPER_LEFT_Y, frame.getY());
				prefs.flush();
			} catch (Throwable t) {
				MessageBox.showErrorDialog(frame, ERROR_DIALOG_TITLE, t);
				System.exit(-1);
			}
		}
	}
}
