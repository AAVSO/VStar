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
import java.awt.Font;
import java.util.prefs.Preferences;

/**
 * Chart properties format preferences.
 */
public class ChartPropertiesPrefs {

	private static Color DEFAULT_CHART_BACKGROUND_COLOR = Color.WHITE;
	private static Color DEFAULT_CHART_GRIDLINES_COLOR = Color.WHITE;
	
	private static Color chartBackgroundColor = DEFAULT_CHART_BACKGROUND_COLOR;
	private static Color chartGridlinesColor = DEFAULT_CHART_GRIDLINES_COLOR;
	
	private static Font extraLargeFont = null;
	private static Font largeFont = null;
	private static Font regularFont = null;
	private static Font smallFont = null;	

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

	public static Font getChartExtraLargeFont() {
		return extraLargeFont;
	}

	public static void setChartExtraLargeFont(Font font) {
		extraLargeFont = font;
	}

	public static Font getChartLargeFont() {
		return largeFont;
	}

	public static void setChartLargeFont(Font font) {
		largeFont = font;
	}
	
	public static Font getChartRegularFont() {
		return regularFont;
	}

	public static void setChartRegularFont(Font font) {
		regularFont = font;
	}

	public static Font getChartSmallFont() {
		return smallFont;
	}

	public static void setChartSmallFont(Font font) {
		smallFont = font;
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

	private static Font createFontFromPrefs(String key) {
		String name = prefs.get(PREFS_PREFIX + key + "_Name", null);
		if (name == null || "".equals(name))
			return null;
		int size = prefs.getInt(PREFS_PREFIX + key + "_Size", 0);
		int style = prefs.getInt(PREFS_PREFIX + key + "_Style", 0);
		Font font = new Font(name, style, size);
		return font;
	}
	
	private static void saveFontToPrefs(String key, Font font) {
		if (font != null) {
			prefs.put(PREFS_PREFIX + key + "_Name", font.getName());
			prefs.putInt(PREFS_PREFIX + key + "_Style", font.getStyle());
			prefs.putInt(PREFS_PREFIX + key + "_Size", font.getSize());
		} else {
			prefs.put(PREFS_PREFIX + key + "_Name", "");
			prefs.putInt(PREFS_PREFIX + key + "_Style", 0);
			prefs.putInt(PREFS_PREFIX + key + "_Size", 0);
		}
		
	}
	
	private static void retrieveChartPropertiesPrefs() {
		chartBackgroundColor = new Color(prefs.getInt(PREFS_PREFIX + "background_color", DEFAULT_CHART_BACKGROUND_COLOR.getRGB()));
		chartGridlinesColor = new Color(prefs.getInt(PREFS_PREFIX + "gridlines_color", DEFAULT_CHART_GRIDLINES_COLOR.getRGB()));
		extraLargeFont = createFontFromPrefs("extraLargeFont");
		largeFont = createFontFromPrefs("largeFont");
		regularFont = createFontFromPrefs("regularFont");
		smallFont = createFontFromPrefs("smallFont");
		
	}

	public static void storeChartPropertiesPrefs() {
		try {
			prefs.putInt(PREFS_PREFIX + "background_color",	chartBackgroundColor.getRGB());
			prefs.putInt(PREFS_PREFIX + "gridlines_color", chartGridlinesColor.getRGB());
			saveFontToPrefs("extraLargeFont", extraLargeFont);
			saveFontToPrefs("largeFont", largeFont);
			saveFontToPrefs("regularFont", regularFont);
			saveFontToPrefs("smallFont", smallFont);
			prefs.flush();
		} catch (Throwable t) {
			// We need VStar to function in the absence of prefs.
		}
	}
	
	public static void setDefaultChartPrefs() {
		chartBackgroundColor = DEFAULT_CHART_BACKGROUND_COLOR;
		chartGridlinesColor = DEFAULT_CHART_GRIDLINES_COLOR;
		extraLargeFont = null;
		largeFont = null;
		regularFont = null;
		smallFont = null;	
		storeChartPropertiesPrefs();
	}

}
