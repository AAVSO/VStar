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
 * This class represents a widget that permits a star group to be selected
 * from a pop-up list and a star in that group from another pop-up list.
 */
public class StarGroupSelectionPane extends JPanel {

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
		Set<String> starGroupMapKeys = starGroups.getStarGroupMap().keySet();

		starGroupSelector = new JComboBox(starGroupMapKeys
				.toArray(new String[0]));
		selectedStarGroup = (String) starGroupSelector.getItemAt(0);
		starGroupSelector.setBorder(BorderFactory
				.createTitledBorder("Group"));
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
				// Select a new star & AUID.
				selectedStarName = (String) starSelector.getSelectedItem();
				selectedAUID = starGroups.getStarGroupMap().get(
						selectedStarGroup).get(selectedStarName);
			}
		};
	}

	// Populate the star list combo-box given the currently selected star group.
	private void populateStarListForSelectedGroup() {
		starSelector.removeAllItems();

		for (String starName : starGroups.getStarGroupMap().get(
				selectedStarGroup).keySet()) {
			starSelector.addItem(starName);
		}
		
		// Maintain the invariant that a star & AUID are always selected.
		selectedStarName = (String) starSelector.getItemAt(0);
		selectedAUID = starGroups.getStarGroupMap().get(
				selectedStarGroup).get(selectedStarName);		
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
