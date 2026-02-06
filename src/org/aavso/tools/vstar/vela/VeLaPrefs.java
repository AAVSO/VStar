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
package org.aavso.tools.vstar.vela;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import org.aavso.tools.vstar.ui.resources.ResourceAccessor;

/**
 * VeLa preferences class.
 */
public class VeLaPrefs {

    public final static String DEFAULT_CODE_DIR_STR = "";
    public final static String DEFAULT_DIAGNOSTIC_MODE_STR = "false";
    public final static String DEFAULT_VERBOSE_MODE_STR = "false";

    private final static String PREFS_PREFIX = "VELA_";

    private final static String CODE_DIRS = "CODE_DIRS";
    private final static String DIAGNOSTIC_MODE = "DIAGNOSTIC_MODE";

    private static Preferences prefs;

    private static String codeDirs = DEFAULT_CODE_DIR_STR;
    private static Boolean diagnosticMode = Boolean.parseBoolean(DEFAULT_DIAGNOSTIC_MODE_STR);
    private static Boolean verboseMode = Boolean.parseBoolean(DEFAULT_VERBOSE_MODE_STR);
    static {
        // Create preferences node.
        try {
            prefs = Preferences.userNodeForPackage(VeLaPrefs.class);
        } catch (Throwable t) {
            // We need VStar to function in the absence of prefs.
        }
    }

    // Preference value getters

    public static String getCodeDirs() {
        loadCodeDirs();
        return codeDirs;
    }

    public static void setCodeDirs(String codeDirs) {
        VeLaPrefs.codeDirs = codeDirs;
        saveCodeDirs(codeDirs);
    }

    public static List<File> getCodeDirsList() {
        getCodeDirs();

        String[] dirs = codeDirs.split("\n");

        List<File> dirList = new ArrayList<File>();

        for (String dir : dirs) {
            dirList.add(new File(dir));
        }

        return dirList;
    }

    public static boolean getDiagnosticMode() {
        loadDiagnosticMode();
        return diagnosticMode;
    }

    public static void setDiagnosticMode(Boolean diagnosticMode) {
        VeLaPrefs.diagnosticMode = diagnosticMode;
        saveDiagnosticMode(diagnosticMode);
    }

    // Note: uncertain that verbose mode is needed, so just stop at getter for now
    public static boolean getVerboseMode() {
        return verboseMode;
    }

    // Preference save/load methods

    public static void saveCodeDirs(String codeDirs) {
        try {
            prefs.put(PREFS_PREFIX + CODE_DIRS, codeDirs);
        } catch (Throwable t) {
            // We need VStar to function in the absence of prefs.
        }
    }

    public static void loadCodeDirs() {
        try {
            codeDirs = prefs.get(PREFS_PREFIX + CODE_DIRS, DEFAULT_CODE_DIR_STR);
        } catch (Throwable t) {
            // We need VStar to function in the absence of prefs.
        }
    }

    public static void saveDiagnosticMode(Boolean diagnosticMode) {
        try {
            prefs.put(PREFS_PREFIX + DIAGNOSTIC_MODE, diagnosticMode.toString());
        } catch (Throwable t) {
            // We need VStar to function in the absence of prefs.
        }
    }

    public static void loadDiagnosticMode() {
        String modeStr = DEFAULT_DIAGNOSTIC_MODE_STR;

        try {
            modeStr = prefs.get(PREFS_PREFIX + DIAGNOSTIC_MODE, DEFAULT_DIAGNOSTIC_MODE_STR);
        } catch (Throwable t) {
            // We need VStar to function in the absence of prefs.
        }

        diagnosticMode = Boolean.parseBoolean(modeStr);
    }
}
