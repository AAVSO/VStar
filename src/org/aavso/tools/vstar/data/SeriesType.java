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
package org.aavso.tools.vstar.data;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.prefs.Preferences;

import org.aavso.tools.vstar.util.locale.LocaleProps;
import org.aavso.tools.vstar.util.notification.Notifier;

// TODO:
// - Eventually change this class so that it starts out with a default set
//   of static series values but when network is available, it "refreshes" that
//   Set<SeriesType> dynamically.

/**
 * A type for bands and other series types, e.g. fainter-thans, means.
 */
public class SeriesType implements Comparable<SeriesType> {

    // Static members

    private final static int NO_INDEX = -1;

    // Plot point size.
    public final static int DEFAULT_SIZE = 4;

    private final static String COLOR_PREFS_PREFIX = "SERIES_COLOR_";
    private final static String SIZE_PREFS_PREFIX = "SERIES_SIZE_";

    private static Map<Integer, SeriesType> index2SeriesMap = new HashMap<Integer, SeriesType>();
    private static Map<String, SeriesType> shortName2SeriesMap = new HashMap<String, SeriesType>();
    private static Map<String, SeriesType> description2SeriesMap = new HashMap<String, SeriesType>();
    private static Map<SeriesType, Color> series2ColorMap = new HashMap<SeriesType, Color>();
    private static Map<SeriesType, Integer> series2SizeMap = new HashMap<SeriesType, Integer>();

    private static Notifier<Map<SeriesType, Color>> seriesColorChangeNotifier = new Notifier<Map<SeriesType, Color>>();
    private static Notifier<Map<SeriesType, Integer>> seriesSizeChangeNotifier = new Notifier<Map<SeriesType, Integer>>();

    private static Set<SeriesType> values = new TreeSet<SeriesType>();

    private static Preferences prefs;

    static {
        // Create preferences node for series colors.
        try {
            prefs = Preferences.userNodeForPackage(SeriesType.class);
        } catch (Throwable t) {
            // We need VStar to function in the absence of prefs.
        }
    }

    public static void initClass() {
    }

    // ** Auto-generated bands from aid.bands start here **

    public static final SeriesType Visual = new SeriesType(0, LocaleProps.get("VISUAL_SERIES"), "Vis.",
            new Color(0, 0, 0));

    public static final SeriesType Unknown = new SeriesType(1, LocaleProps.get("UNKNOWN_SERIES"), "N/A",
            new Color(255, 255, 0));

    public static final SeriesType Johnson_U = new SeriesType(7, "Johnson U", "U", new Color(0, 255, 255));

    public static final SeriesType Johnson_B = new SeriesType(3, "Johnson B", "B", new Color(0, 0, 255));

    public static final SeriesType Johnson_V = new SeriesType(2, "Johnson V", "V", new Color(0, 255, 0));

    public static final SeriesType Johnson_R = new SeriesType(10, "Johnson R", "RJ", new Color(192, 0, 64));

    public static final SeriesType Johnson_I = new SeriesType(11, "Johnson I", "IJ", new Color(192, 64, 128));

    public static final SeriesType Halpha = new SeriesType(13, "Halpha", "HA", new Color(192, 32, 0));

    public static final SeriesType Halpha_continuum = new SeriesType(14, "Halpha-continuum", "HAC",
            new Color(160, 32, 32));

    public static final SeriesType Blue = new SeriesType(21, LocaleProps.get("BLUE_SERIES"),
            LocaleProps.get("BLUE_SERIES") + "-Vis.", new Color(0, 0, 75));

    public static final SeriesType Green = new SeriesType(22, LocaleProps.get("GREEN_SERIES"),
            LocaleProps.get("GREEN_SERIES") + "-Vis.", new Color(0, 75, 0));

    public static final SeriesType Red = new SeriesType(23, LocaleProps.get("RED_SERIES"),
            LocaleProps.get("RED_SERIES") + "-Vis.", new Color(75, 0, 0));

    public static final SeriesType Yellow = new SeriesType(24, LocaleProps.get("YELLOW_SERIES"),
            LocaleProps.get("YELLOW_SERIES") + "-Vis.", new Color(255, 255, 128));

    public static final SeriesType K_NIR_2pt2micron = new SeriesType(26, "K NIR 2.2micron", "K",
            new Color(255, 128, 255));

    public static final SeriesType H_NIR_1pt6micron = new SeriesType(27, "H NIR 1.6micron", "H",
            new Color(128, 128, 128));

    public static final SeriesType J_NIR_1pt2micron = new SeriesType(28, "J NIR 1.2micron", "J",
            new Color(255, 0, 255));

    public static final SeriesType Sloan_z = new SeriesType(29, "Sloan z", "SZ", new Color(255, 192, 0));

    public static final SeriesType Stromgren_u = new SeriesType(30, "Stromgren u", "STU", new Color(0, 192, 255));

    public static final SeriesType Stromgren_v = new SeriesType(31, "Stromgren v", "STV", new Color(0, 255, 192));

    public static final SeriesType Stromgren_b = new SeriesType(32, "Stromgren b", "STB", new Color(0, 0, 192));

    public static final SeriesType Stromgren_y = new SeriesType(33, "Stromgren y", "STY", new Color(192, 255, 0));

    public static final SeriesType Stromgren_Hbw = new SeriesType(34, "Stromgren Hbw", "STHBW", new Color(0, 128, 255));

    public static final SeriesType Stromgren_Hbn = new SeriesType(35, "Stromgren Hbn", "STHBN", new Color(0, 128, 192));

    public static final SeriesType Cousins_R = new SeriesType(4, "Cousins R", "R", new Color(255, 0, 0));

    public static final SeriesType Sloan_u = new SeriesType(40, "Sloan u", "SU", new Color(192, 192, 0));

    public static final SeriesType Sloan_g = new SeriesType(41, "Sloan g", "SG", new Color(0, 64, 64));

    public static final SeriesType Sloan_r = new SeriesType(42, "Sloan r", "SR", new Color(128, 64, 0));

    public static final SeriesType Sloan_i = new SeriesType(43, "Sloan i", "SI", new Color(192, 64, 0));

    public static final SeriesType PanSTARRS_Z_short = new SeriesType(44, "PanSTARRS Z-short", "ZS",
            new Color(255, 64, 32));

    public static final SeriesType PanSTARRS_Y = new SeriesType(45, "PanSTARRS Y", "Y", new Color(96, 0, 0));

    public static final SeriesType Cousins_I = new SeriesType(5, "Cousins I", "I", new Color(255, 64, 0));

    public static final SeriesType Tri_Color_Blue = new SeriesType(50, "Tri-Color Blue", "TB", new Color(0, 0, 128));

    public static final SeriesType Tri_Color_Green = new SeriesType(51, "Tri-Color Green", "TG", new Color(0, 128, 0));

    public static final SeriesType Tri_Color_Red = new SeriesType(52, "Tri-Color Red", "TR", new Color(128, 0, 0));

    public static final SeriesType Optec_Wing_A = new SeriesType(55, "Optec Wing A", "MA", new Color(128, 64, 255));

    public static final SeriesType Optec_Wing_B = new SeriesType(56, "Optec Wing B", "MB", new Color(128, 64, 128));

    public static final SeriesType Optec_Wing_C = new SeriesType(57, "Optec Wing C", "MI", new Color(128, 0, 192));

    public static final SeriesType Orange_Liller = new SeriesType(6, LocaleProps.get("ORANGE_SERIES") + " (Liller)",
            LocaleProps.get("ORANGE_SERIES"), new Color(255, 128, 0));

    public static final SeriesType Clear_Blue_Blocking = new SeriesType(60, "Clear Blue Blocking", "CBB",
            new Color(255, 220, 32));

    public static final SeriesType Unfiltered_with_V_Zeropoint = new SeriesType(8, "Unfiltered with V Zeropoint", "CV",
            new Color(0, 192, 0));

    public static final SeriesType Unfiltered_with_R_Zeropoint = new SeriesType(9, "Unfiltered with R Zeropoint", "CR",
            new Color(192, 0, 0));

    public static final SeriesType GAIA_G = new SeriesType(58, "GAIA G", "GG",
            new Color(255, 165, 0));
    
    // ** Auto-generated bands from aid.bands end here **

    public static final SeriesType FAINTER_THAN = new SeriesType(SeriesType.NO_INDEX,
            LocaleProps.get("FAINTER_THAN_SERIES"), "FainterThan", Color.YELLOW);

    public static final SeriesType MEANS = new SeriesType(SeriesType.NO_INDEX, LocaleProps.get("MEANS_SERIES"),
            LocaleProps.get("MEANS_SERIES"), Color.BLUE, true, false);

    // Aaron's suggestion was to make Discrepant points light gray.
    public static final SeriesType DISCREPANT = new SeriesType(SeriesType.NO_INDEX,
            LocaleProps.get("DISCREPANT_SERIES"), LocaleProps.get("DISCREPANT_SERIES"), Color.LIGHT_GRAY);

    public static final SeriesType Unspecified = new SeriesType(SeriesType.NO_INDEX,
            LocaleProps.get("UNSPECIFIED_SERIES"), LocaleProps.get("UNSPECIFIED_SERIES"), Color.ORANGE);

    public static final SeriesType Filtered = new SeriesType(SeriesType.NO_INDEX, LocaleProps.get("FILTERED_SERIES"),
            LocaleProps.get("FILTERED_SERIES"), new Color(0, 153, 204), true, false);

    // Model series.
    public static final SeriesType Model = new SeriesType(SeriesType.NO_INDEX, LocaleProps.get("MODEL_SERIES"),
            LocaleProps.get("MODEL_SERIES"), Color.RED, true, false);

    // Model function series.
    public static final SeriesType ModelFunction = new SeriesType(SeriesType.NO_INDEX, LocaleProps.get("MODEL_SERIES"),
            LocaleProps.get("MODEL_SERIES"), Color.RED, true, false);

    // Residuals series.
    public static final SeriesType Residuals = new SeriesType(SeriesType.NO_INDEX, LocaleProps.get("RESIDUALS_SERIES"),
            LocaleProps.get("RESIDUALS_SERIES"), Color.CYAN, true, false);

    // This series can be used to mark an observation as being excluded for some
    // other reason than it being discrepant and all that classification
    // entails. The key thing is that this provides a category under which an
    // observation can be grouped in order to remove it from consideration in
    // analysis.
    public static final SeriesType Excluded = new SeriesType(SeriesType.NO_INDEX, LocaleProps.get("EXCLUDED_SERIES"),
            LocaleProps.get("EXCLUDED_SERIES"), Color.DARK_GRAY);

    /**
     * @return The series color change notifier.
     */
    public static Notifier<Map<SeriesType, Color>> getSeriesColorChangeNotifier() {
        return seriesColorChangeNotifier;
    }

    /**
     * @return The series size change notifier.
     */
    public static Notifier<Map<SeriesType, Integer>> getSeriesSizeChangeNotifier() {
        return seriesSizeChangeNotifier;
    }

    /**
     * Adds a series type instance to the appropriate collections if it does not
     * already exist.
     * 
     * @param type The series type to be added.
     */
    private static void updateStaticCollections(SeriesType type) {
        if (!values.contains(type)) {
            values.add(type);
            index2SeriesMap.put(type.getIndex(), type);
            shortName2SeriesMap.put(type.getShortName(), type);
            description2SeriesMap.put(type.getDescription(), type);

            Color colorPref = getColorPref(type);
            series2ColorMap.put(type, colorPref == null ? type.getColor() : colorPref);

            series2SizeMap.put(type, getSizePref(type));
        }
    }

    // Instance members per SeriesType value.
    private int index;
    private String description;
    private String shortName;
    private Color color;
    private int size;
    private boolean synthetic;
    private boolean userDefined;

    /**
     * Create a new series type or return an existing one.
     * 
     * @param description The series type's description.
     * @param shortName   The series type's short name (AID.bands).
     * @param color       The series type's color.
     * @param synthetic   Is this series synthetic (i.e. not associated with data
     *                    but derived from data)?
     * @param userDefined Is this series user-defined?
     * @return the new or pre-existing SeriesType instance.
     */
    public static SeriesType create(String description, String shortName, Color color, boolean synthetic,
            boolean userDefined) {
        // Create the series type of interest.
        SeriesType newSeries = new SeriesType(NO_INDEX, description, shortName, color, synthetic, userDefined);

        // Find which ever one now exists in the values set. That may be the
        // new instance or a previously created instance.
        for (SeriesType series : values()) {
            // One series type is equal to another if their descriptions are the
            // same. We can't have 2 series with the same name!
            if (series.equals(newSeries)) {
                newSeries = series;
                break;
            }
        }

        return newSeries;
    }

    /**
     * Delete the specified series type.
     * 
     * @param type The series type to delete.
     */
    public static void delete(SeriesType type) {
        // We don't want to delete in-built series!
        assert type.isUserDefined();

        if (values.contains(type)) {
            values.remove(type);

            index2SeriesMap.remove(type.getIndex());
            shortName2SeriesMap.remove(type.getShortName());
            description2SeriesMap.remove(type.getDescription());

            series2ColorMap.remove(type);

            series2SizeMap.remove(type);
        }
    }

    /**
     * Constructor
     * 
     * @param index       The series type's index (AID.Code).
     * @param description The series type's description.
     * @param shortName   The series type's short name (AID.bands).
     * @param color       The series type's color.
     * @param synthetic   Is this series synthetic (i.e. not associated with data
     *                    but derived from data)?
     * @param userDefined Is this series user-defined?
     */
    private SeriesType(int index, String description, String shortName, Color color, boolean synthetic,
            boolean userDefined) {
        this.index = index;
        this.description = description;
        this.shortName = shortName;
        this.color = color;
        this.size = DEFAULT_SIZE;
        this.synthetic = synthetic;
        this.userDefined = userDefined;
        updateStaticCollections(this);
    }

    /**
     * Constructor
     * 
     * Non-synthetic series type.
     * 
     * @param index       The series type's index (AID.Code).
     * @param description The series type's description.
     * @param shortName   The series type's short name (AID.bands).
     * @param color       The series type's color.
     */
    private SeriesType(int index, String description, String shortName, Color color) {
        this(index, description, shortName, color, false, false);
    }

    /**
     * Constructor
     * 
     * Non-synthetic series type with no index.
     * 
     * @param description The series type's description.
     * @param shortName   The series type's short name (AID.bands).
     * @param color       The series type's color.
     */
    private SeriesType(String description, String shortName, Color color) {
        this(SeriesType.NO_INDEX, description, shortName, color, false, false);
    }

    /**
     * @return the index
     */
    public int getIndex() {
        return index;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return the shortName
     */
    public String getShortName() {
        return shortName;
    }

    /**
     * @return the default color
     */
    public Color getColor() {
        return color;
    }

    /**
     * @return the default size
     */
    public int getSize() {
        return size;
    }

    /**
     * @return the synthetic
     */
    public boolean isSynthetic() {
        return synthetic;
    }

    /**
     * @return the userDefined
     */
    public boolean isUserDefined() {
        return userDefined;
    }

    /**
     * Map from AID band index to series type.
     * 
     * @param index The integer band index
     * @return The band, Unspecified if not found.
     */
    public static SeriesType getSeriesFromIndex(int index) {
        SeriesType type = index2SeriesMap.get(index);

        if (type == null) {
            type = getDefault();
        }

        return type;
    }

    /**
     * Map from short band description to series type.
     * 
     * @param shortName The short description of the band.
     * @return The band, Unspecified if not found.
     */
    public static SeriesType getSeriesFromShortName(String shortName) {
        SeriesType type = shortName2SeriesMap.get(shortName);

        if (type == null) {
            // TODO: We can remove this block when we have changed or
            // downloaded new (or just deleted) files to replace existing
            // ones in the case where band short-names have changed!
            // Actually, we still see such names as V and B in formats like
            // AAVSO extended upload file format.
            if (shortName.equals("Unknown")) {
                type = Unknown;
            } else if (shortName.equals("Visual")) {
                type = Visual;
            } else if (shortName.equals("U")) {
                type = Johnson_U;
            } else if (shortName.equals("B")) {
                type = Johnson_B;
            } else if (shortName.equals("V")) {
                type = Johnson_V;
            } else if (shortName.equals("R")) {
                type = Cousins_R;
            } else if (shortName.equals("I")) {
                type = Cousins_I;
            }
        }

        if (type == null) {
            type = getDefault();
        }

        return type;
    }

    /**
     * Map from band descriptive name to series type.
     * 
     * @param description The descriptive description of the band.
     * @return The band, Unspecified if not found.
     */
    public static SeriesType getSeriesFromDescription(String description) {
        SeriesType type = description2SeriesMap.get(description);

        if (type == null) {
            type = getDefault();
        }

        return type;
    }

    /**
     * Does the series, specified by description, exist?
     * 
     * @param description The description, as passed to create().
     * @return Whether or not the series already exists.
     */
    public static boolean exists(String description) {
        return description2SeriesMap.keySet().contains(description);
    }

    private static Color getColorPref(SeriesType series) {
        Color color = null;

        if (series != null) {
            String colorPrefName = COLOR_PREFS_PREFIX + series.getDescription();
            try {
                String colorPrefValue = prefs.get(colorPrefName, null);
                if (colorPrefValue != null) {
                    // We expect this to be an integer RGB color value
                    // but we need a way to distinguish between there
                    // being no preference for the value and a valid
                    // color RGB value which there is no way of doing
                    // with a primitive integer.
                    color = new Color(Integer.parseInt(colorPrefValue));
                }
            } catch (Throwable t) {
                // We need VStar to function in the absence of prefs.
            }
        }

        return color;
    }

    private static void setColorPref(SeriesType series, Color color) {
        if (series != null && color != null) {
            String colorPrefName = COLOR_PREFS_PREFIX + series.getDescription();
            try {
                prefs.put(colorPrefName, color.getRGB() + "");
            } catch (Throwable t) {
                // We need VStar to function in the absence of prefs.
            }
        }
    }

    private static Integer getSizePref(SeriesType series) {
        int size = DEFAULT_SIZE;

        if (series != null) {
            String sizePrefName = SIZE_PREFS_PREFIX + series.getDescription();
            try {
                size = prefs.getInt(sizePrefName, DEFAULT_SIZE);
            } catch (Throwable t) {
                // We need VStar to function in the absence of prefs.
            }
        }

        return size;
    }

    private static void setSizePref(SeriesType series, int size) {
        if (series != null) {
            String sizePrefName = SIZE_PREFS_PREFIX + series.getDescription();
            try {
                prefs.putInt(sizePrefName, size);
            } catch (Throwable t) {
                // We need VStar to function in the absence of prefs.
            }
        }
    }

    /**
     * Given a series, retrieve its color.
     * 
     * @param series The series in question.
     * @return The corresponding color.
     */
    public static Color getColorFromSeries(SeriesType series) {
        Color color = series2ColorMap.get(series);

        if (color == null) {
            color = getDefault().getColor();
        }

        return color;
    }

    /**
     * Given a series, retrieve its size.
     * 
     * @param series The series in question.
     * @return The corresponding size.
     */
    public static int getSizeFromSeries(SeriesType series) {
        Integer size = series2SizeMap.get(series);

        if (size == null) {
            size = getDefault().getSize();
        }

        return size;
    }

    /**
     * Updates the series to color mapping according to the pairs in the supplied
     * map. Note that this may be a subset of all series-color pairs, so it may not
     * completely replace the existing map, just overwrite some pairs. It also
     * updates the series color preference and notifies listeners of the change.
     * 
     * @param newSeries2ColorMap The map with which to update the series-color map.
     */
    public static void updateSeriesColorMap(Map<SeriesType, Color> newSeries2ColorMap) {

        if (!newSeries2ColorMap.isEmpty()) {
            for (SeriesType series : newSeries2ColorMap.keySet()) {
                Color color = newSeries2ColorMap.get(series);
                series2ColorMap.put(series, color);
                setColorPref(series, color);
            }

            try {
                prefs.flush();
            } catch (Throwable t) {
                // We need VStar to function in the absence of prefs.
            }

            seriesColorChangeNotifier.notifyListeners(newSeries2ColorMap);
        }
    }

    /**
     * Updates the series to size mapping according to the pairs in the supplied
     * map. Note that this may be a subset of all series-size pairs, so it may not
     * completely replace the existing map, just overwrite some pairs. It also
     * updates the series size preference and notifies listeners of the change.
     * 
     * @param newSeries2SizeMap The map with which to update the series-size map.
     */
    public static void updateSeriesSizeMap(Map<SeriesType, Integer> newSeries2SizeMap) {

        if (!newSeries2SizeMap.isEmpty()) {
            for (SeriesType series : newSeries2SizeMap.keySet()) {
                int size = newSeries2SizeMap.get(series);
                series2SizeMap.put(series, size);
                setSizePref(series, size);
            }

            try {
                prefs.flush();
            } catch (Throwable t) {
                // We need VStar to function in the absence of prefs.
            }

            seriesSizeChangeNotifier.notifyListeners(newSeries2SizeMap);
        }
    }

    /**
     * Restore the default series colors and notifies listeners.
     */
    public static void setDefaultSeriesColors() {
        series2ColorMap.clear();

        try {
            prefs.clear();
        } catch (Throwable t) {
            // We need VStar to function in the absence of prefs.
        }

        for (SeriesType type : values()) {
            Color color = type.getColor();
            series2ColorMap.put(type, color);
            setColorPref(type, color);
        }

        try {
            prefs.flush();
        } catch (Throwable t) {
            // We need VStar to function in the absence of prefs.
        }

        seriesColorChangeNotifier.notifyListeners(series2ColorMap);
    }

    /**
     * Restore the default series sizes and notifies listeners.
     */
    public static void setDefaultSeriesSizes() {
        series2SizeMap.clear();

        try {
            prefs.clear();
        } catch (Throwable t) {
            // We need VStar to function in the absence of prefs.
        }

        for (SeriesType type : values()) {
            int size = type.getSize();
            series2SizeMap.put(type, size);
            setSizePref(type, size);
        }

        try {
            prefs.flush();
        } catch (Throwable t) {
            // We need VStar to function in the absence of prefs.
        }

        seriesSizeChangeNotifier.notifyListeners(series2SizeMap);
    }

    /**
     * Returns the default series type. This is like the equivalent of null for this
     * type.
     * 
     * @return The default series type.
     */
    public static SeriesType getDefault() {
        return Unspecified;
    }

    /**
     * We override toString() to return description rather than enum name.
     */
    public String toString() {
        return this.getDescription();
    }

    @Override
    public int compareTo(SeriesType other) {
        int result = 0;

        // Full field equality otherwise relational value defined in terms of
        // description.
        if (index == other.index && description.equals(other.description) && shortName.equals(other.shortName)
                && color.equals(other.color) && synthetic == other.synthetic) {
            result = 0;
        } else {
            result = description.compareTo(other.description);
        }

        return result;
    }

    /**
     * @return the set of all series type values.
     */
    public static Set<SeriesType> values() {
        return values;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof SeriesType)) {
            return false;
        }
        SeriesType other = (SeriesType) obj;
        if (description == null) {
            if (other.description != null) {
                return false;
            }
        } else if (!description.equals(other.description)) {
            return false;
        }
        return true;
    }
}
