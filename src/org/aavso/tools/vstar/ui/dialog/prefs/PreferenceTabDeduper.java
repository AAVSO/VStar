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
import java.util.List;
import java.util.Set;

/**
 * De-duplicates plugin preference tabs.
 * <p>
 * Same-instance panes are skipped (Eclipse / shared classloader). Panes that
 * share a preferences id or component name are also skipped: standalone VStar
 * loads each plugin jar with its own classloader, so a shared helper such as
 * ConvertHelper yields a distinct instance per plugin.
 */
final class PreferenceTabDeduper {

	private PreferenceTabDeduper() {
	}

	static boolean shouldAddPreferenceTab(Component pane, String prefsId,
			List<Component> addedPanes, Set<String> seenIds) {
		if (pane == null) {
			return false;
		}
		for (Component added : addedPanes) {
			if (added == pane) {
				return false;
			}
		}
		if (isSeenPreferenceId(prefsId, seenIds)) {
			return false;
		}
		return !isSeenPreferenceId(pane.getName(), seenIds);
	}

	static void recordPreferenceTab(Component pane, String prefsId,
			Set<String> seenIds) {
		addPreferenceId(prefsId, seenIds);
		if (pane != null) {
			addPreferenceId(pane.getName(), seenIds);
		}
	}

	private static boolean isSeenPreferenceId(String id, Set<String> seenIds) {
		return id != null && !id.isEmpty() && seenIds.contains(id);
	}

	private static void addPreferenceId(String id, Set<String> seenIds) {
		if (id != null && !id.isEmpty()) {
			seenIds.add(id);
		}
	}
}
