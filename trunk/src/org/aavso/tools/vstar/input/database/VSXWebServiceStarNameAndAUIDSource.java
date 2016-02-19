/**
 * VStar: a statistical analysis tool for variable star data.
 * Copyright (C) 2014  AAVSO (http://www.aavso.org/)
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

import org.aavso.tools.vstar.input.IStarInfoSource;
import org.aavso.tools.vstar.ui.mediator.StarInfo;

/**
 * 
 */
public class VSXWebServiceStarNameAndAUIDSource implements
		IStarInfoSource {

	/* (non-Javadoc)
	 * @see org.aavso.tools.vstar.input.database.IStarInfoSource#getStarByName(java.sql.Connection, java.lang.String)
	 */
	@Override
	public StarInfo getStarByName(Connection connection, String name)
			throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.aavso.tools.vstar.input.database.IStarInfoSource#getStarByAUID(java.sql.Connection, java.lang.String)
	 */
	@Override
	public StarInfo getStarByAUID(Connection connection, String auid)
			throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}
}
