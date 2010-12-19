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

import java.util.Collections;
import java.util.Map;
import java.util.Set;
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

	// Get Singleton instance.
	public static StarGroups getInstance() {
		return instance;
	}

	/**
	 * Private constructor to ensure Singleton.
	 */
	public StarGroups() {
		starGroupMap = new TreeMap<String, Map<String, String>>();

		defaultStarListTitle = PropertiesAccessor.getStarListTitle();
		addDefaultStarGroup();

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
	 * Return an ordered set of star group names.
	 */
	public Set<String> getGroupNames() {
		return starGroupMap.keySet();
	}

	/**
	 * @return the defaultStarListTitle
	 */
	public String getDefaultStarListTitle() {
		return defaultStarListTitle;
	}

	/**
	 * Return an ordered set of star names in the specified group.
	 * 
	 * @param groupName
	 *            The group name.
	 * @return An ordered set of stars in the group.
	 */
	public Set<String> getStarNamesInGroup(String groupName) {
		Set<String> starNames = null;

		if (starGroupMap.containsKey(groupName)) {
			starNames = starGroupMap.get(groupName).keySet();
		} else {
			starNames = Collections.EMPTY_SET;
		}

		return starNames;
	}

	/**
	 * Return the AUID for the specified group and star.
	 * 
	 * @param groupName
	 *            The group name.
	 * @param starName
	 *            The star name.
	 * @return The corresponding AUID; may be null.
	 */
	public String getAUID(String groupName, String starName) {
		String auid = null;

		if (starGroupMap.containsKey(groupName)) {
			auid = starGroupMap.get(groupName).get(starName);
		}

		return auid;
	}

	/**
	 * Does the specified group exist?
	 * 
	 * @param groupName
	 *            The name of the group.
	 * @return True if the group exists, false otherwise.
	 */
	public boolean doesGroupExist(String groupName) {
		return starGroupMap.containsKey(groupName);
	}

	/**
	 * Does the specified star exist in the specified group?
	 * 
	 * @param groupName
	 *            The name of the group.
	 * @param starName
	 *            The name of the star.
	 * @return True if the star exists in the group, false otherwise.
	 */
	public boolean doesStarExistInGroup(String groupName, String starName) {
		boolean exists = false;
		if (starGroupMap.containsKey(groupName)) {
			exists = starGroupMap.get(groupName).containsKey(starName);
		}
		return exists;
	}

	/**
	 * Add a new (empty) star group to the map. If the group previously existed,
	 * this will replace it.
	 * 
	 * @param groupName
	 *            The name of the group.
	 */
	public void addStarGroup(String groupName) {
		starGroupMap.put(groupName, new TreeMap<String, String>());
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

	/**
	 * Add a star (name and AUID) to the specified group. If the star previously
	 * existed in that group, this will replace it.
	 * 
	 * @param groupName
	 *            The name of the group.
	 * @param starName
	 *            The name of the star.
	 * @param auid
	 *            The AUID of the star. This is assumed to correspond to a
	 *            validated (i.e. existing the the database) AUID.
	 */
	public void addStar(String groupName, String starName, String auid) {
		starGroupMap.get(groupName).put(starName, auid);
	}

	/**
	 * Remove a star (name and AUID) from the specified group, if it exists.
	 * 
	 * @param groupName
	 *            The name of the group.
	 * @param starName
	 *            The name of the star.
	 */
	public void removeStar(String groupName, String starName) {
		starGroupMap.get(groupName).remove(starName);
	}

	/**
	 * Remove all groups except the default group.
	 */
	public void resetGroupsToDefault() {
		starGroupMap.clear();
		addDefaultStarGroup();
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

				if (!starMap.isEmpty()) {
					// ...|groupN:...
					if (prefsValue.length() != 0) {
						prefsValue += "|";
					}

					// groupN:
					prefsValue += groupName + ":";

					// starN,auidN;...
					for (String starName : starMap.keySet()) {
						String auid = starMap.get(starName);
						prefsValue += starName + "," + auid + ";";
					}

					// Remove trailing semi-colon after star-auid pairs.
					if (prefsValue.endsWith(";")) {
						prefsValue = prefsValue.substring(0, prefsValue
								.length() - 1);
					}
				}
			}
		}

		return prefsValue;
	}

	/**
	 * Store the star groups as a preferences string value.
	 */
	public void storeStarGroupPrefs() {
		try {
			String prefsValue = createStarGroupPrefsValue();
			prefs.put(PREFS_KEY, prefsValue);
			prefs.flush();
		} catch (Throwable t) {
			// We need VStar to function in the absence of prefs.
		}
	}

	// Helpers

	// Add known default star group.
	private void addDefaultStarGroup() {
		starGroupMap.put(defaultStarListTitle, new TreeMap<String, String>());

		for (Star star : PropertiesAccessor.getStarList()) {
			starGroupMap.get(defaultStarListTitle).put(star.getName(),
					star.getIdentifier());
		}
	}
}
