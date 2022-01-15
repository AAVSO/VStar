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
package org.aavso.tools.vstar.plugin;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.data.filter.IFilterDescription;
import org.aavso.tools.vstar.ui.dialog.MessageBox;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.message.FilteredObservationMessage;
import org.aavso.tools.vstar.ui.resources.LoginInfo;
import org.aavso.tools.vstar.util.Pair;

/**
 * <p>
 * This is the base class for all custom filter plugins.
 * </p>
 * 
 * <p>
 * A Custom Filter plugin will appear in VStar's View menu when its jar file is
 * placed into the vstar_plugins directory.
 * </p>
 * 
 * @see org.aavso.tools.vstar.plugin.IPlugin
 */
abstract public class CustomFilterPluginBase implements IPlugin {

	protected boolean testMode = false;
	
	// Subset of observations.
	// We use a LinkedHashSet to maintain addition and lookup efficiency
	// while maintaining insertion order.
	protected Set<ValidObservation> filteredObs;

	/**
	 * <p>
	 * Filter a list of observation returning a filter name and string
	 * representation.
	 * </p>
	 * 
	 * @param obs
	 *            A list of currently loaded observations.
	 * @return A pair containing a filter name and a string representation of
	 *         the filter.
	 */
	abstract protected Pair<String, String> filter(List<ValidObservation> obs);

	/**
	 * @see org.aavso.tools.vstar.plugin.IPlugin#getGroup()
	 */
	@Override
	public String getGroup() {
		return null;
	}

	/**
	 * Adds an observation to the filtered subset.
	 * 
	 * @param ob
	 *            The observation to be added.
	 */
	final protected void addToSubset(ValidObservation ob) {
		filteredObs.add(ob);
	}

	/**
	 * Applies the filter to the list of observations and sends a filtered
	 * observation message to be consumed by listeners (plots, tables).
	 * 
	 * @param obs
	 *            A list of currently loaded observations.
	 */
	final public void apply(List<ValidObservation> obs) {
		filteredObs = new LinkedHashSet<ValidObservation>();

		final Pair<String, String> rep = filter(obs);

		if (filteredObs.size() != 0) {

			IFilterDescription desc = new IFilterDescription() {

				@Override
				public boolean isParsable() {
					return filtersAreParsable();
				}

				@Override
				public String getFilterName() {
					return rep.first;
				}

				@Override
				public String getFilterDescription() {
					return rep.second;
				}
			};

			FilteredObservationMessage msg = new FilteredObservationMessage(
					this, desc, filteredObs);

			Mediator.getInstance().getFilteredObservationNotifier()
					.notifyListeners(msg);
		} else if (rep != null) {
			String errMsg = "No observations matched.";
			MessageBox.showWarningDialog(Mediator.getUI().getComponent(),
					"Observation Filter", errMsg);
		}
	}

	/**
	 * Returns whether or not the filters associated with the concrete subclass
	 * are parsable.
	 * 
	 * @return Whether parsable or not.
	 */
	public boolean filtersAreParsable() {
		return false;
	}

	/**
	 * @see org.aavso.tools.vstar.plugin.IPlugin#requiresAuthentication()
	 */
	@Override
	public boolean requiresAuthentication() {
		return false;
	}

	/**
	 * @see org.aavso.tools.vstar.plugin.IPlugin#additionalAuthenticationSatisfied(org.aavso.tools.vstar.ui.resources.LoginInfo)
	 */
	@Override
	public boolean additionalAuthenticationSatisfied(LoginInfo loginInfo) {
		return true;
	}
	
	@Override
	public Boolean test() {
		return null;
	}

	@Override
	public boolean inTestMode() {
		return testMode;
	}

	@Override
	public void setTestMode(boolean mode) {
		testMode = mode;	
	}
}
