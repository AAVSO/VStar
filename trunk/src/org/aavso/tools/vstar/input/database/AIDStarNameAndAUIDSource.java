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

import org.aavso.tools.vstar.input.IStarInfoSource;
import org.aavso.tools.vstar.ui.mediator.StarInfo;

/**
 * This class obtains star name and AUID information from the AAVSO 
 * International Database (AID).
 * @deprecated Use VSXStarNameAndAUIDSource instead.
 */
public class AIDStarNameAndAUIDSource implements IStarInfoSource {
	
	private PreparedStatement auidFromValidationStmt;
	private PreparedStatement auidFromAliasStmt;
	private PreparedStatement starNameFromValidationStmt;

	/**
	 * Return the AUID of the named star.
	 * 
	 * @param connection
	 *            A JDBC connection.
	 * @param name
	 *            The star name or alias.
	 * @return Information about the star, e.g. name, AUID, period.
	 */
	public StarInfo getStarByName(Connection connection, String name) throws SQLException {
		String auid = null;

		// Can we find the name in the validation table?
		PreparedStatement validationStmt = this
				.createAUIDFromValidationQuery(connection);
		validationStmt.setString(1, name);
		ResultSet validationResults = validationStmt.executeQuery();

		if (validationResults.next()) {
			auid = validationResults.getString("auid");
		} else {
			// No, how about in the aliases database?
			PreparedStatement aliasStmt = this
					.createAUIDFromAliasQuery(connection);
			aliasStmt.setString(1, name);
			ResultSet aliasResults = aliasStmt.executeQuery();

			if (aliasResults.next()) {
				auid = aliasResults.getString("auid");
			}
		}

		return new StarInfo(name, auid);
	}

	/**
	 * Return the name of the star given an AUID.
	 * 
	 * @param connection
	 *            A JDBC connection.
	 * @param name
	 *            The AUID.
	 * @return Information about the star, e.g. name, AUID, period.
	 */
	public StarInfo getStarByAUID(Connection connection, String auid)
			throws SQLException {
		String starName = null;

		// Can we find the AUID in the validation table?
		PreparedStatement starNamePreparedStatement = this
				.createStarNameFromValidationQuery(connection);
		starNamePreparedStatement.setString(1, auid);
		ResultSet validationResults = starNamePreparedStatement.executeQuery();
		if (validationResults.next()) {
			starName = validationResults.getString("name");
		}

		return new StarInfo(starName, auid);
	}
	
	// Helpers
	
	/**
	 * Return a prepared statement to find the AUID from the validation table
	 * given a star name. This is a once-only-created prepared statement.
	 * 
	 * @param connection
	 *            A JDBC connection.
	 * @return A prepared statement.
	 */
	protected PreparedStatement createAUIDFromValidationQuery(
			Connection connection) throws SQLException {

		if (auidFromValidationStmt == null) {
			auidFromValidationStmt = connection
					.prepareStatement("SELECT auid FROM validation WHERE validation.name = ? limit 1;");
		}

		return auidFromValidationStmt;
	}

	/**
	 * Return a prepared statement to find the AUID from the alias table given a
	 * star name. This is a once-only-created prepared statement.
	 * 
	 * @param connection
	 *            A JDBC connection.
	 * @return A prepared statement.
	 */
	protected PreparedStatement createAUIDFromAliasQuery(Connection connection)
			throws SQLException {

		if (auidFromAliasStmt == null) {
			auidFromAliasStmt = connection
					.prepareStatement("SELECT auid FROM aliases WHERE aliases.name = ? limit 1;");
		}

		return auidFromAliasStmt;
	}

	/**
	 * Return a prepared statement to find the star name from the validation
	 * table given an AUID. This is a once-only-created prepared statement.
	 * 
	 * @param connection
	 *            A JDBC connection.
	 * @return A prepared statement.
	 */
	protected PreparedStatement createStarNameFromValidationQuery(
			Connection connection) throws SQLException {

		if (starNameFromValidationStmt == null) {
			starNameFromValidationStmt = connection
					.prepareStatement("SELECT name FROM validation WHERE validation.auid = ? LIMIT 1;");
		}

		return starNameFromValidationStmt;
	}
}
