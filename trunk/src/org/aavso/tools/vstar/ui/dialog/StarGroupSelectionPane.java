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
package org.aavso.tools.vstar.ui.dialog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import org.aavso.tools.vstar.ui.resources.StarGroups;

/**
 * This class represents a widget that permits a star group to be selected from
 * a pop-up list and a star in that group from another pop-up list.
 */
public class StarGroupSelectionPane extends JPanel {

	private final static String NO_STARS = "No stars";

	private JComboBox starGroupSelector;
	private JComboBox starSelector;
	private ActionListener starSelectorListener;

	private StarGroups starGroups;

	// Selected star group, name and AUID.
	private String selectedStarGroup;
	private String selectedStarName;
	private String selectedAUID;

	/**
	 * Constructor.
	 */
	public StarGroupSelectionPane() {
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		this.setBorder(BorderFactory.createEtchedBorder());

		selectedStarGroup = null;

		selectedStarName = null;
		selectedAUID = null;

		starGroups = StarGroups.getInstance();
		Set<String> starGroupMapKeys = starGroups.getGroupNames();

		starGroupSelector = new JComboBox(starGroupMapKeys
				.toArray(new String[0]));
		selectedStarGroup = (String) starGroupSelector.getItemAt(0);
		starGroupSelector.setBorder(BorderFactory.createTitledBorder("Group"));
		starGroupSelector.addActionListener(createStarGroupSelectorListener());

		starSelector = new JComboBox();
		populateStarListForSelectedGroup();
		starSelector.setBorder(BorderFactory.createTitledBorder("Star"));
		starSelectorListener = createStarSelectorListener();
		starSelector.addActionListener(starSelectorListener);

		this.add(starGroupSelector);
		this.add(starSelector);
	}

	// Star group selector listener.
	private ActionListener createStarGroupSelectorListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Populate the star selector list according
				// to the selected group.
				selectedStarGroup = (String) starGroupSelector
						.getSelectedItem();
				starSelector.removeActionListener(starSelectorListener);
				populateStarListForSelectedGroup();
				starSelector.addActionListener(starSelectorListener);
			}
		};
	}

	// Star selector listener.
	private ActionListener createStarSelectorListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String starName = (String) starSelector.getSelectedItem();
				if (!NO_STARS.equals(starName)) {
					// Select a new star & AUID.
					selectedStarName = starName;
					selectedAUID = starGroups.getAUID(selectedStarGroup,
							selectedStarName);
				}
			}
		};
	}

	/**
	 * Populate the star list combo-box given the currently selected star group.
	 */
	public void populateStarListForSelectedGroup() {
		starSelector.removeAllItems();

		if (!starGroups.getStarNamesInGroup(selectedStarGroup).isEmpty()) {

			for (String starName : starGroups
					.getStarNamesInGroup(selectedStarGroup)) {
				starSelector.addItem(starName);
			}

			// Maintain the invariant that a star & AUID are always selected.
			selectedStarName = (String) starSelector.getItemAt(0);
			selectedAUID = starGroups.getAUID(selectedStarGroup,
					selectedStarName);
		} else {
			starSelector.addItem(NO_STARS);
		}
	}

	/**
	 * Add the specified group (to the map and visually) if it does not exist.
	 * 
	 * @param groupName
	 *            The group to add.
	 */
	public void addGroup(String groupName) {
		if (!starGroups.doesGroupExist(groupName)) {
			starGroups.addStarGroup(groupName);
			starGroupSelector.addItem(groupName);
			selectAndRefreshStarsInGroup(groupName);
		}
	}

	/**
	 * Remove the specified group (from the map and visually) if it exists.
	 * 
	 * @param groupName
	 *            The group to remove.
	 */
	public void removeGroup(String groupName) {
		if (starGroups.doesGroupExist(groupName)) {
			starGroups.removeStarGroup(groupName);
			starGroupSelector.removeItem(groupName);
			selectAndRefreshStarsInGroup((String) starGroupSelector
					.getItemAt(0));
		}
	}

	/**
	 * Add the specified group-star-AUID triple.
	 * 
	 * @param groupName
	 *            The group to add.
	 * @param starName
	 *            The star to add to the specified group.
	 * @param auid
	 *            The AUID of the star to be added.
	 */
	public void addStar(String groupName, String starName, String auid) {
		if (starGroups.doesGroupExist(groupName)) {
			starGroups.addStar(groupName, starName, auid);
			selectAndRefreshStarsInGroup(groupName);
		}
	}

	/**
	 * Remove the specified star in the specified group.
	 * 
	 * @param groupName
	 *            The group to add.
	 * @param starName
	 */
	public void removeStar(String groupName, String starName) {
		if (starGroups.doesGroupExist(groupName)) {
			starGroups.removeStar(groupName, starName);
			selectAndRefreshStarsInGroup(groupName);
		}
	}

	/**
	 * Remove all groups except the default group.
	 */
	public void clearGroups() {
		starGroups.clearGroups();
		
		while (starGroupSelector.getItemCount() > 1) {
			String groupName = (String) starGroupSelector.getSelectedItem();
			if (!starGroups.getDefaultStarListTitle().equals(groupName)) {
				starGroupSelector.removeItem(groupName);
			}
		}
		
		selectAndRefreshStarsInGroup((String)starGroupSelector.getItemAt(0));
	}

	/**
	 * Select the specified group and refresh its stars.
	 * 
	 * @param groupName
	 *            The group to select.
	 */
	public void selectAndRefreshStarsInGroup(String groupName) {
		if (starGroups.doesGroupExist(groupName)) {
			starGroupSelector.setSelectedItem(groupName);
			selectedStarGroup = groupName;
			populateStarListForSelectedGroup();
		}
	}

	/**
	 * @return the starGroups
	 */
	public StarGroups getStarGroups() {
		return starGroups;
	}

	/**
	 * @return the selectedStarGroup
	 */
	public String getSelectedStarGroupName() {
		return selectedStarGroup;
	}

	/**
	 * @return the selectedStarName
	 */
	public String getSelectedStarName() {
		return selectedStarName;
	}

	/**
	 * @return the selectedAUID
	 */
	public String getSelectedAUID() {
		return selectedAUID;
	}
}
