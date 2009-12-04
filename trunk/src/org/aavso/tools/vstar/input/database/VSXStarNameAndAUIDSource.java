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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * This class obtains star name and AUID information from the VSX database.
 */
public class VSXStarNameAndAUIDSource implements IStarNameAndAUIDSource {

	private static String STARTABLE = "vsx_objects";
	private static String ALIASTABLE = "vsx_crossids";

	private PreparedStatement findAUIDFromNameStatement;
	private PreparedStatement findAUIDFromAliasStatement;

	/**
	 * Return the AUID of the named star.
	 * 
	 * @param name
	 *            The star name or alias.
	 * @return The AUID as a string, or null if it is not recognised as a valid
	 *         star name in the VSX database.
	 */
	public String getAUID(Connection connection, String name)
			throws SQLException {
		String auid = null;

		createFindAUIDFromNameStatement(connection);
		findAUIDFromNameStatement.setString(1, name);
		findAUIDFromNameStatement.setString(2, name);
		findAUIDFromNameStatement.setString(3, name);
		ResultSet rs = findAUIDFromNameStatement.executeQuery();
		if (!rs.first()) {
			createFindAUIDFromAliasStatement(connection);
			findAUIDFromAliasStatement.setString(1, name);
			findAUIDFromAliasStatement.setString(2, name);
			rs = findAUIDFromAliasStatement.executeQuery();
		}

		return auid;
	}

	/**
	 * Return the name of the star given an AUID.
	 * 
	 * @param name
	 *            The AUID.
	 * @return The star name as a string, or null if it is not recognised as a
	 *         valid AUID in the VSX database.
	 */
	public String getStarName(Connection connection, String auid)
			throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	// Helpers

	protected PreparedStatement createFindAUIDFromNameStatement(
			Connection connect) throws SQLException {
		if (findAUIDFromNameStatement == null) {
			findAUIDFromNameStatement = connect
					.prepareStatement("SELECT o_auid, o_designation, "
							+ "o_varType, o_period, o_specType from "
							+ STARTABLE
							+ " WHERE (o_auid = ? OR o_designation = ? OR REPLACE(o_designation, \"V0\", \"V\") = ?) "
							+ "AND o_auid is not null");
		}
		return findAUIDFromNameStatement;
	}

	protected PreparedStatement createFindAUIDFromAliasStatement(
			Connection connect) throws SQLException {
		if (findAUIDFromAliasStatement == null) {
			findAUIDFromAliasStatement = connect
					.prepareStatement("SELECT o_auid, o_designation, "
							+ "o_varType, o_period, o_specType from "
							+ STARTABLE
							+ ", "
							+ ALIASTABLE
							+ " WHERE "
							+ STARTABLE
							+ ".oid = "
							+ ALIASTABLE
							+ ".oid "
							+ "AND (x_catName = ? OR CONCAT(x_catAcronym, REPLACE(x_catName,\" \",\"\")) "
							+ "= REPLACE(?,\" \",\"\")) AND o_auid is not null");
		}
		return findAUIDFromAliasStatement;
	}
}
