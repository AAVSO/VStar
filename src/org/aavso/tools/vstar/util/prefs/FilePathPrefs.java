/**
 * VStar: a statistical analysis tool for variable star data.
 * Copyright (C) 2026  AAVSO (http://www.aavso.org/)
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

import java.io.File;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;

/**
 * Default observation source and sink file-path preferences.
 */
public class FilePathPrefs {

	public final static String DEFAULT_DIR = "";

	private final static String PREFS_PREFIX = "FILE_PATH_";
	private final static String OBS_SOURCE_DIR = "OBS_SOURCE_DIR";
	private final static String OBS_SINK_DIR = "OBS_SINK_DIR";

	private static Preferences prefs;

	private static String obsSourceDir = DEFAULT_DIR;
	private static String obsSinkDir = DEFAULT_DIR;

	static {
		try {
			prefs = Preferences.userNodeForPackage(FilePathPrefs.class);
			loadPrefs();
		} catch (Throwable t) {
			// We need VStar to function in the absence of prefs.
		}
	}

	public static String getObsSourceDir() {
		return obsSourceDir;
	}

	public static void setObsSourceDir(String dir) {
		obsSourceDir = dir != null ? dir : DEFAULT_DIR;
		savePrefs();
	}

	public static String getObsSinkDir() {
		return obsSinkDir;
	}

	public static void setObsSinkDir(String dir) {
		obsSinkDir = dir != null ? dir : DEFAULT_DIR;
		savePrefs();
	}

	public static void setDefaultPrefs() {
		obsSourceDir = DEFAULT_DIR;
		obsSinkDir = DEFAULT_DIR;
		savePrefs();
	}

	/**
	 * Set the file chooser's current directory if the given path is a
	 * non-empty existing directory; otherwise leave the chooser unchanged.
	 */
	public static void applyDirectory(JFileChooser fileChooser, String path) {
		if (fileChooser == null || path == null) {
			return;
		}

		String trimmed = path.trim();
		if (trimmed.isEmpty()) {
			return;
		}

		File dir = new File(trimmed);
		if (dir.isDirectory()) {
			fileChooser.setCurrentDirectory(dir);
		}
	}

	private static void loadPrefs() {
		try {
			obsSourceDir = prefs.get(PREFS_PREFIX + OBS_SOURCE_DIR, DEFAULT_DIR);
			obsSinkDir = prefs.get(PREFS_PREFIX + OBS_SINK_DIR, DEFAULT_DIR);
		} catch (Throwable t) {
			// We need VStar to function in the absence of prefs.
		}
	}

	private static void savePrefs() {
		try {
			prefs.put(PREFS_PREFIX + OBS_SOURCE_DIR, obsSourceDir);
			prefs.put(PREFS_PREFIX + OBS_SINK_DIR, obsSinkDir);
			prefs.flush();
		} catch (Throwable t) {
			// We need VStar to function in the absence of prefs.
		}
	}
}
