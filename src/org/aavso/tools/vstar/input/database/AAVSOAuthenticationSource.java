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
package org.aavso.tools.vstar.input.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.aavso.tools.vstar.exception.AuthenticationError;
import org.aavso.tools.vstar.exception.ConnectionException;
import org.aavso.tools.vstar.ui.resources.LoginType;
import org.aavso.tools.vstar.ui.resources.ResourceAccessor;

/**
 * This class authenticates with respect to an AAVSO source.
 * @deprecated @see AAVSOPostAuthenticationSource
 */
public class AAVSOAuthenticationSource implements IAuthenticationSource {

	private AAVSODatabaseConnector userConnector;
	private AAVSODatabaseConnector memberConnector;
	private PreparedStatement authStmt;
	private PreparedStatement obsCodeStmt;
	private boolean authenticated;

	public AAVSOAuthenticationSource() {
		this.userConnector = AAVSODatabaseConnector.aavsoUserDBConnector;
		this.memberConnector = AAVSODatabaseConnector.memberDBConnector;
		this.authenticated = false;
	}

	@Override
	public boolean authenticate(String username, String password)
			throws AuthenticationError, ConnectionException {
		if (!authenticated) {
			try {
				// Get a prepared statement to read a user's details
				// from the database, setting the parameters for user
				// who is authenticating.
				PreparedStatement userStmt = createLoginQuery(userConnector
						.getConnection());
				userStmt.setString(1, username);

				String passwordDigest = Authenticator
						.generateHexDigest(password);

				ResultSet userResults = userStmt.executeQuery();

				if (userResults.next()) {
					String actualPassword = userResults.getString("pass");
					if (passwordDigest.equals(actualPassword)) {
						// We're authenticated, so update login info and
						// retrieve user information, e.g. observer code.
						authenticated = true;

						ResourceAccessor.getLoginInfo().setType(getLoginType());
						ResourceAccessor.getLoginInfo().setUserName(username);

						retrieveUserInfo(username);
					}
				}
			} catch (SQLException e) {
				throw new AuthenticationError(e.getLocalizedMessage());
			}
		}

		return authenticated;
	}

	@Override
	public LoginType getLoginType() {
		return LoginType.AAVSO;
	}

	/**
	 * Retrieve user information such as observer code given the user name, if
	 * we are authenticated, otherwise return null.
	 */
	private void retrieveUserInfo(String username) {
		if (authenticated) {
			try {
				PreparedStatement userInfoStmt = createObserverCodeQuery(memberConnector
						.getConnection());
				userInfoStmt.setString(1, username);
				ResultSet userInfoResults = userInfoStmt.executeQuery();

				if (userInfoResults.next()) {
					String obsCode = userInfoResults.getString("obscode");
					if (!userInfoResults.wasNull()) {
						ResourceAccessor.getLoginInfo()
								.setObserverCode(obsCode);
					}

					Integer memberBenefits = userInfoResults
							.getInt("member_benefits");
					if (!userInfoResults.wasNull()) {
						ResourceAccessor.getLoginInfo().setMember(
								memberBenefits == 1);
					}
				}
			} catch (SQLException e) {
				// Nothing to do: just return null.
			} catch (ConnectionException e) {
				// Nothing to do: just return null.
			}
		}
	}

	/**
	 * Return a prepared statement for the specified AAVSO user login.
	 * 
	 * This is a once-only-created prepared statement with parameters set for
	 * each query execution.
	 * 
	 * @param connection
	 *            database connection.
	 * @return A prepared statement.
	 */
	private PreparedStatement createLoginQuery(Connection connection)
			throws SQLException {
		if (authStmt == null) {
			authStmt = connection
					.prepareStatement("SELECT users.name, users.pass, users.uid "
							+ "FROM users WHERE users.name = ?");
		}

		return authStmt;
	}

	/**
	 * Return a prepared statement to obtain the observer code from the members
	 * table given the AAVSO user's user ID.
	 * 
	 * This is a once-only-created prepared statement with parameters set for
	 * each query execution.
	 * 
	 * @param connection
	 *            database connection.
	 * @return A prepared statement.
	 */
	private PreparedStatement createObserverCodeQuery(Connection connection)
			throws SQLException {
		if (obsCodeStmt == null) {
			String member = ResourceAccessor.getParam(DatabaseType.MEMBER
					.getDBNum());
			String live = ResourceAccessor.getParam(DatabaseType.AAVSO_USER
					.getDBNum());

			obsCodeStmt = connection
					.prepareStatement("SELECT obscode, member_benefits "
							+ "from "
							+ member
							+ ".view_member_info WHERE id = (SELECT value from "
							+ live
							+ ".users as u, "
							+ live
							+ ".profile_values as v "
							+ "WHERE v.uid = u.uid and v.fid = 19 and u.name = ?);");
		}

		return obsCodeStmt;
	}
}
