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
package org.aavso.tools.vstar.input.database;

import java.sql.Connection;
import java.sql.SQLException;

import org.aavso.tools.vstar.ui.mediator.StarInfo;

/**
 * This interface must be implemented by any class wishing to retrieve
 * star name or AUID from a database.
 * 
 * TODO: refactor this so as to abstract the source, e.g. database vs 
 * web service. We'd need a different package then also.
 */
public interface IStarNameAndAUIDSource {

	/**
	 * Return the AUID of the named star.
	 * 
	 * @param name
	 *            The star name or alias.
	 * @return Information about the star, e.g. name, AUID, period.
	 */
	public StarInfo getStarByName(Connection connection, String name) throws SQLException;

	/**
	 * Return the name of the star given an AUID.
	 * 
	 * @param name
	 *            The AUID.
	 * @return Information about the star, e.g. name, AUID, period.
	 */
	public StarInfo getStarByAUID(Connection connection, String auid) throws SQLException;
}
