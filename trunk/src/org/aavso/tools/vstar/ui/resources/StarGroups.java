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
package org.aavso.tools.vstar.ui.resources;

import java.util.Map;
import java.util.TreeMap;
import java.util.prefs.Preferences;

import org.aavso.tools.vstar.data.SeriesType;

/**
 * This class provides access to the list of star groups available to the
 * new-star-from-database dialog, including their persistence across sessions.
 */
public class StarGroups {

	private final static StarGroups instance = new StarGroups();
	
	private final static String PREFS_KEY = "STAR_GROUPS";

	private Map<String, Map<String, String>> starGroupMap;
	private Preferences prefs;
	private String defaultStarListTitle;

	/**
	 * Singleton getter.
	 */
	public static StarGroups getInstance() {
		return instance;
	}
	
	/**
	 * Private constructor to ensure Singleton.
	 */
	private StarGroups() {
		starGroupMap = new TreeMap<String, Map<String, String>>();

		// Add known default Citizen Sky 10-star group.
		defaultStarListTitle = PropertiesAccessor.getStarListTitle();
		starGroupMap.put(defaultStarListTitle, new TreeMap<String, String>());

		for (Star star : PropertiesAccessor.getStarList()) {
			starGroupMap.get(defaultStarListTitle).put(star.getName(),
					star.getIdentifier());
		}

		// Add to star group map from user preferences.
		try {
			prefs = Preferences.userNodeForPackage(SeriesType.class);

			String starGroupPrefs = prefs.get(PREFS_KEY, null);
			if (starGroupPrefs != null) {
				populateStarGroupMapFromPrefs(starGroupPrefs);
			}
		} catch (Throwable t) {
			// We need VStar to function in the absence of prefs.
		}
	}

	/**
	 * Adds to the star groups map given a preferences string of the form:
	 * 
	 * group1:star1,auid1;star2,auid2|group2:star1,auid1;star2,auid2|...
	 * 
	 * @param starGroupPrefsValue
	 *            The star groups preferences string.
	 */
	private void populateStarGroupMapFromPrefs(String starGroupPrefsValue) {
		assert starGroupPrefsValue != null;

		String[] starGroups = starGroupPrefsValue.split("\\|");

		for (String starGroup : starGroups) {
			int colonIndex = starGroup.indexOf(':');

			String groupName = starGroup.substring(0, colonIndex);

			String[] starPairs = starGroup.substring(colonIndex + 1).split(";");
			Map<String, String> starMap = new TreeMap<String, String>();

			for (String starPair : starPairs) {
				int commaIndex = starPair.indexOf(',');
				String starName = starPair.substring(0, commaIndex);
				String auid = starPair.substring(commaIndex + 1);
				starMap.put(starName, auid);
			}

			starGroupMap.put(groupName, starMap);
		}
	}

	/**
	 * Creates a string of the form:
	 * 
	 * group1:star1,auid1;star2,auid2|group2:star1,auid1;star2,auid2|...
	 * 
	 * from the star groups map.
	 * 
	 * @return The preferences value.
	 */
	private String createStarGroupPrefsValue() {
		String prefsValue = "";

		for (String groupName : starGroupMap.keySet()) {
			// Don't include default star list in preferences.
			if (!defaultStarListTitle.equals(groupName)) {
				Map<String, String> starMap = starGroupMap.get(groupName);
			}
		}

		return prefsValue;
	}

	/**
	 * @return the starGroupMap
	 */
	public Map<String, Map<String, String>> getStarGroupMap() {
		return starGroupMap;
	}

	/**
	 * Store the star groups as a preferences string value.
	 */
	public void storeStarGroupPrefs() {
		try {
			String prefsValue = createStarGroupPrefsValue();
			if (prefsValue != "") {
				prefs.put(PREFS_KEY, prefsValue);
			}
		} catch (Throwable t) {
			// We need VStar to function in the absence of prefs.
		}
	}

	/**
	 * Add a star group to the map. If the group previously existed, this will
	 * replace it.
	 * 
	 * @param groupName
	 *            The name of the group.
	 * @param stars
	 *            A mapping from star names to AUIDs for stars to be associated
	 *            with the group.
	 */
	public void addStarGroup(String groupName, Map<String, String> stars) {
		starGroupMap.put(groupName, stars);
	}

	/**
	 * Remove the named star group, if it exists.
	 * 
	 * @param groupName
	 *            The name of the group.
	 */
	public void removeStarGroup(String groupName) {
		starGroupMap.remove(groupName);
	}
}
