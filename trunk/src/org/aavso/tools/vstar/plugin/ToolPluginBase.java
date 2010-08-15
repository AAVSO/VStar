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

import java.util.List;

import org.aavso.tools.vstar.data.ValidObservation;

/**
 * <p>This is the base class for all arbitrary VStar "tool" plugins, where "tool"
 * can be anything that takes as input a list of valid observations and does any
 * arbitrary thing with them.</p>
 * 
 * <p>Tool plugins will appear in VStar's Tools menu when its jar file is placed into 
 * the vstar_plugins directory.</p>
 * 
 * @see org.aavso.tools.vstar.plugin.PluginBase
 * @see org.aavso.tools.vstar.ui.mediator.Mediator#invokeToolWithCurrentObservations(org.aavso.tools.vstar.plugin.ToolPluginBase)
 */
abstract public class ToolPluginBase implements PluginBase {

	/**
	 * Given a list of valid observations, do something, <em>anything</em> in fact. :)
	 * 
	 * @param obs
	 *            A list of currently loaded observations.
	 */
	abstract public void invoke(List<ValidObservation> obs);
}
