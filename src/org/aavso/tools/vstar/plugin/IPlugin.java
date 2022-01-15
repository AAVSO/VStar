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

import org.aavso.tools.vstar.ui.resources.LoginInfo;

/**
 * <p>
 * All VStar plugins must implement this interface.
 * </p>
 * 
 * <p>
 * Plugin jars must be placed into the vstar_plugins directory in your home
 * directory.
 * </p>
 * 
 * <p>
 * Any dependent jar files not already known to VStar (in the extlib directory)
 * are expected to reside in the vstar_plugin_libs directory.
 * </p>
 * 
 * <p>
 * Note: plugins will have to be licensed under AGPL because they will use some
 * VStar classes!
 * </p>
 */
public interface IPlugin {

	/**
	 * Get the human-readable display name for this plugin, e.g. for a period
	 * analysis menu item.
	 */
	abstract public String getDisplayName();

	/**
	 * Get a description of this plugin.
	 */
	abstract public String getDescription();

	/**
	 * Get the group to which this plugin belongs (may be null).
	 */
	abstract public String getGroup();

	/**
	 * Does this plugin require authentication?
	 * 
	 * @return Whether or not the plugin requires authentication.
	 */
	abstract public boolean requiresAuthentication();

	/**
	 * This method will be called after requiresAuthentication() in case any
	 * additional authentication is required.
	 * 
	 * @param loginInfo The current session's login information.
	 * @return Whether or not any additional authentication has satisfied.
	 */
	abstract public boolean additionalAuthenticationSatisfied(LoginInfo loginInfo);

	// Test methods

	/**
	 * Method to be invoked in order to test this plug-in
	 * 
	 * @return whether the test passed (true, false) or null meaning no test
	 */
	abstract public Boolean test();

	/**
	 * Is the current invocation of the plug-in in test mode? This can help in
	 * design-for-test by allowing non-essential logic to change what code is
	 * invoked during a test.
	 * 
	 * @return true or false
	 */
	abstract public boolean inTestMode();

	/**
	 * Set the test mode. Each test() method must do this if required, and not all
	 * plug-in tests will require it.
	 * 
	 * @param mode true or false, denoting whether in test mode
	 */
	abstract public void setTestMode(boolean mode);
}