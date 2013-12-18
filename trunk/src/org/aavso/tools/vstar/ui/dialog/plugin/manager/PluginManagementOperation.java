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
package org.aavso.tools.vstar.ui.dialog.plugin.manager;

/**
 * Plugin management operation.
 */
abstract public class PluginManagementOperation {

	private PluginManager manager;
	private String message;

	/**
	 * @param manager
	 */
	public PluginManagementOperation(PluginManager manager, String message) {
		super();
		this.manager = manager;
		this.message = message;
	}

	/**
	 * @return the manager
	 */
	public PluginManager getManager() {
		return manager;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Interrupts the current operation.
	 */
	public void interrupt() {
		manager.interrupt();		
	}

	/**
	 * Execute the operation.
	 */
	abstract public void execute();
}
