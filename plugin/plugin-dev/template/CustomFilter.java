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
package template;

import java.util.List;

import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.plugin.CustomFilterPluginBase;
import org.aavso.tools.vstar.util.Pair;

/**
 * Custom filter
 */
public class CustomFilter extends CustomFilterPluginBase {

	/**
	 * Get the display name for this plugin, e.g. for a menu item.
	 */
	@Override
	public String getDisplayName() {
		return null;
	}

	/**
	 * Get a description of this plugin, e.g. for display in a plugin manager.
	 */
	@Override
	public String getDescription() {
		return null;
	}

	/**
	 * Filter a list of observation returning a filter name and string
	 * representation.<br/>
	 * 
	 * The base class addToSubset() method can be used to add observations to
	 * the filtered subset of observations.
	 * 
	 * @param obs
	 *            The list of observations to be filtered.
	 * @return A pair containing a filter name and a string representation of
	 *         the filtered subset.
	 */
	@Override
	protected Pair<String, String> filter(List<ValidObservation> obs) {
		return null;
	}
}
