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
package org.aavso.tools.vstar.ui.dialog.prefs;

import java.awt.Component;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JPanel;

import junit.framework.TestCase;

/**
 * Tests for plugin preference-tab de-duplication.
 *
 * Standalone VStar loads each plugin jar with its own classloader, so a shared
 * helper pane is a different instance per plugin. Tabs must be keyed by id
 * (or component name), not by instance identity alone.
 */
public class PreferencesDialogTest extends TestCase {

	private List<Component> addedPanes;
	private Set<String> seenIds;

	public PreferencesDialogTest(String name) {
		super(name);
	}

	protected void setUp() {
		addedPanes = new ArrayList<Component>();
		seenIds = new HashSet<String>();
	}

	public void testNullPaneIsNotAdded() {
		assertFalse(PreferenceTabDeduper.shouldAddPreferenceTab(null, "id",
				addedPanes, seenIds));
	}

	public void testSameInstanceIsNotAddedTwice() {
		JPanel pane = new JPanel();
		assertTrue(PreferenceTabDeduper.shouldAddPreferenceTab(pane, null,
				addedPanes, seenIds));
		addedPanes.add(pane);
		PreferenceTabDeduper.recordPreferenceTab(pane, null, seenIds);

		assertFalse(PreferenceTabDeduper.shouldAddPreferenceTab(pane, null,
				addedPanes, seenIds));
	}

	public void testDistinctInstancesWithSameIdShareOneTab() {
		JPanel first = new JPanel();
		JPanel second = new JPanel();
		String id = "org.aavso.tools.vstar.external.lib.ConvertHelper";

		assertTrue(PreferenceTabDeduper.shouldAddPreferenceTab(first, id,
				addedPanes, seenIds));
		addedPanes.add(first);
		PreferenceTabDeduper.recordPreferenceTab(first, id, seenIds);

		assertFalse(PreferenceTabDeduper.shouldAddPreferenceTab(second, id,
				addedPanes, seenIds));
	}

	public void testDistinctInstancesWithSameComponentNameShareOneTab() {
		JPanel first = new JPanel();
		JPanel second = new JPanel();
		first.setName("shared.prefs");
		second.setName("shared.prefs");

		assertTrue(PreferenceTabDeduper.shouldAddPreferenceTab(first, null,
				addedPanes, seenIds));
		addedPanes.add(first);
		PreferenceTabDeduper.recordPreferenceTab(first, null, seenIds);

		assertFalse(PreferenceTabDeduper.shouldAddPreferenceTab(second, null,
				addedPanes, seenIds));
	}

	public void testDistinctPanesWithoutIdAreBothAdded() {
		JPanel first = new JPanel();
		JPanel second = new JPanel();

		assertTrue(PreferenceTabDeduper.shouldAddPreferenceTab(first, null,
				addedPanes, seenIds));
		addedPanes.add(first);
		PreferenceTabDeduper.recordPreferenceTab(first, null, seenIds);

		assertTrue(PreferenceTabDeduper.shouldAddPreferenceTab(second, null,
				addedPanes, seenIds));
	}
}
