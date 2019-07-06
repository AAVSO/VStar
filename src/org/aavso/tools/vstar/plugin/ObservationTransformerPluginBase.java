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

import java.util.Set;

import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.ui.mediator.message.UndoRedoType;
import org.aavso.tools.vstar.ui.model.plot.ISeriesInfoProvider;
import org.aavso.tools.vstar.ui.resources.LoginInfo;
import org.aavso.tools.vstar.ui.undo.IUndoableAction;

/**
 * The base class of all observation transformation plugins.<br/>
 * Such plugins must provide an undoable action given ...
 */
abstract public class ObservationTransformerPluginBase implements IPlugin {

	/**
	 * Create and return an undoable action that can transform the supplied list
	 * of observations and undo this transformation.<br/>
	 * 
	 * @param obs
	 *            The list of observations to be transformed.
	 * @return The undoable action.
	 */
	abstract public IUndoableAction execute(ISeriesInfoProvider seriesInfo,
			Set<SeriesType> series);

	/**
	 * @see org.aavso.tools.vstar.plugin.IPlugin#getGroup()
	 */
	@Override
	public String getGroup() {
		return null;
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

	/**
	 * If there is a plugin-specific dialog to be invoked before execute(),
	 * override this method.
	 * 
	 * @return Whether or not it is OK to continue.
	 */
	public boolean invokeDialog() {
		return true;
	}
}
