/**
 * VStar: a statistical analysis tool for variable star data.
 * Copyright (C) 2010  AAVSO (http://www.aavso.org/)
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
package org.aavso.tools.vstar.util.prefs;

import java.awt.Color;
import java.util.prefs.Preferences;

/**
 * Chart properties format preferences.
 */
public class ChartPropertiesPrefs {

	private static Color DEFAULT_CHART_BACKGROUND_COLOR = Color.WHITE;
	private static Color DEFAULT_CHART_GRIDLINES_COLOR = Color.WHITE;
	
	private static Color chartBackgroundColor = DEFAULT_CHART_BACKGROUND_COLOR;
	private static Color chartGridlinesColor = DEFAULT_CHART_GRIDLINES_COLOR;	

	public static Color getChartBackgroundColor() {
		return chartBackgroundColor;
	}
	
	public static void setChartBackgroundColor(Color color) {
		chartBackgroundColor = color;
	}
	
	public static Color getChartGridlinesColor() {
		return chartGridlinesColor;
	}
	
	public static void setChartGridlinesColor(Color color) {
		chartGridlinesColor = color;
	}
	
	// Preferences members.

	private final static String PREFS_PREFIX = "CHART_PROPERTIES_";

	private static Preferences prefs;

	static {
		// Create preferences node for chart preferences.
		try {
			prefs = Preferences.userNodeForPackage(ChartPropertiesPrefs.class);
			retrieveChartPropertiesPrefs();
		} catch (Throwable t) {
			// We need VStar to function in the absence of prefs.
		}
	}
	
	private static void retrieveChartPropertiesPrefs() {
		chartBackgroundColor = new Color(prefs.getInt(PREFS_PREFIX + "background_color", DEFAULT_CHART_BACKGROUND_COLOR.getRGB()));
		chartGridlinesColor = new Color(prefs.getInt(PREFS_PREFIX + "gridlines_color", DEFAULT_CHART_GRIDLINES_COLOR.getRGB()));
	}

	public static void storeChartPropertiesPrefs() {
		try {
			prefs.putInt(PREFS_PREFIX + "background_color",	chartBackgroundColor.getRGB());
			prefs.putInt(PREFS_PREFIX + "gridlines_color", chartGridlinesColor.getRGB());
			prefs.flush();
		} catch (Throwable t) {
			// We need VStar to function in the absence of prefs.
		}
	}
	
	public static void setDefaultChartPrefs() {
		chartBackgroundColor = DEFAULT_CHART_BACKGROUND_COLOR;
		chartGridlinesColor = DEFAULT_CHART_GRIDLINES_COLOR;
		storeChartPropertiesPrefs();
	}

}
