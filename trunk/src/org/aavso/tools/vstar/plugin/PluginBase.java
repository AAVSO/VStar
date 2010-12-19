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
package org.aavso.tools.vstar.plugin;

/**
 * <p>All VStar plugins must implement this interface.</p>
 *
 * <p>Plugin jars must be placed into the vstar_plugins directory in your home directory 
 * (for Mac or *nix that's $HOME or ~, "C:\Documents and Settings\<em>user</em>" under Windows).</p> 
 * 
 * <p>Any dependent jar files not already known to VStar (in the extlib directory) must go into 
 * the vstar_plugin_libs directory.</p>
 * 
 * <p>Note: plugins will have to be licensed under AGPL because they will use some
 * VStar classes!</p>
 */
public interface PluginBase {

	/**
	 * Get the human-readable display name for this plugin, e.g. for a period
	 * analysis menu item.
	 */
	abstract public String getDisplayName();

	/**
	 * Get a description of this plugin.
	 */
	abstract public String getDescription();
}