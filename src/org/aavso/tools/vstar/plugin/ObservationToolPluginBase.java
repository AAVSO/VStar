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

import org.aavso.tools.vstar.ui.model.plot.ISeriesInfoProvider;
import org.aavso.tools.vstar.ui.resources.LoginInfo;

/**
 * <p>
 * This is the base class for all observation VStar "tool" plugins. Such tools
 * perform arbitrary processing on loaded observations.
 * </p>
 * 
 * <p>
 * A tool plugin will appear in VStar's Tools menu when its jar file is placed
 * into the vstar_plugins directory.
 * </p>
 * 
 * @see org.aavso.tools.vstar.plugin.IPlugin
 * @see org.aavso.tools.vstar.ui.mediator.Mediator#invokeTool(org.aavso.tools.vstar.plugin.ObservationToolPluginBase)
 */
abstract public class ObservationToolPluginBase implements IPlugin {

	protected boolean testMode = false;

	/**
	 * Given information about observations per series, perform some arbitrary
	 * processing on a subset of the observations.
	 * 
	 * @param seriesInfo
	 *            A mapping from series type to lists of currently loaded
	 *            observations.
	 */
	abstract public void invoke(ISeriesInfoProvider seriesInfo);

	/**
	 * @see org.aavso.tools.vstar.plugin.IPlugin#getDocName()
	 */
	@Override
	public String getDocName() {
		return null;
	}
	
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
