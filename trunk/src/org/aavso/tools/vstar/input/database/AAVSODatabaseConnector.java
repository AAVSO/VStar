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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import org.aavso.tools.vstar.exception.AuthenticationError;
import org.aavso.tools.vstar.exception.CancellationException;
import org.aavso.tools.vstar.exception.ConnectionException;
import org.aavso.tools.vstar.ui.MainFrame;
import org.aavso.tools.vstar.ui.dialog.LoginDialog;
import org.aavso.tools.vstar.ui.dialog.MessageBox;
import org.aavso.tools.vstar.ui.resources.ResourceAccessor;

/**
 * This class handles the details of connecting to the MySQL AAVSO observation
 * database, a data accessor. This is a Singleton.
 * 
 * Portions of code were adapted (with permission) from the AAVSO Zapper code
 * base.
 * 
 * generateHexDigest() was adapted from Zapper UserInfo.encryptPassword()
 * 
 * TODO: Handle the case where the connection becomes invalid but we try to use
 * it, e.g. to create a statement or execute a query. We need to set the
 * connection to null and open an error dialog.
 * 
 * TODO: should we split this class into a data accessor object and connection
 * object?
 */
public class AAVSODatabaseConnector {

	 // 30 seconds connection timeout.
	private final static int MAX_CONN_TIME = 15 * 1000;
	
	private final static String CONN_URL = "jdbc:mysql://"
			+ ResourceAccessor.getParam(0);

	private DatabaseType type;
	private Driver driver;
	private Connection connection;

	private PreparedStatement authStmt;
	private PreparedStatement obsStmt;
	private PreparedStatement auidFromValidationStmt;
	private PreparedStatement auidFromAliasStmt;
	private PreparedStatement starNameFromValidationStmt;

	private boolean authenticatedWithCitizenSky;
	private String obsCode;

	public static AAVSODatabaseConnector observationDBConnector = new AAVSODatabaseConnector(
			DatabaseType.OBSERVATION);

	public static AAVSODatabaseConnector userDBConnector = new AAVSODatabaseConnector(
			DatabaseType.USER);

	protected static AAVSODatabaseConnector utDBConnector = new AAVSODatabaseConnector(
			DatabaseType.UT);

	static {
		try {
			Class.forName("com.mysql.jdbc.Driver", true,
					AAVSODatabaseConnector.class.getClassLoader());
		} catch (ClassNotFoundException e) {
			MessageBox.showErrorDialog(MainFrame.getInstance(), "Read from database", e);
		}
	}

	/**
	 * Constructor
	 */
	private AAVSODatabaseConnector(DatabaseType type) {
		this.type = type;
		this.driver = null;
		this.connection = null;
		this.obsStmt = null;
		this.authenticatedWithCitizenSky = false;
		this.obsCode = null;
	}

	/**
	 * Create a connection to the database if it has not already been created.
	 * 
	 * @throws ConnectionException
	 *             if there was an error creating the connection.
	 */
	public Connection createConnection() throws ConnectionException {
		int retries = 3;

		while (connection == null && retries > 0) {
			// TODO: provide status message updates re: retries 
			Properties props = new Properties();

			props.put("user", ResourceAccessor.getParam(4));
			props.put("password", ResourceAccessor.getParam(5));
			props.put("connectTimeout", MAX_CONN_TIME + "");

			try {
				// First try with port 3307...
				connection = DriverManager.getConnection(CONN_URL + ":3307/"
						+ ResourceAccessor.getParam(type.getDBNum()), props);
			} catch (Exception e1) {
				try {
					// ..and then with 3306.
					connection = DriverManager
							.getConnection(CONN_URL + ":3306/"
									+ ResourceAccessor
											.getParam(type.getDBNum()), props);
				} catch (Exception e) {
					throw new ConnectionException(e.getMessage());
				}
			}

			retries--;
		}

		return connection;
	}

	/**
	 * Return a prepared statement for the specified AUID and date range. This
	 * is a once-only-created prepared statement with parameters set.
	 * 
	 * @param connection
	 *            A JDBC connection.
	 * @return A prepared statement.
	 */
	public PreparedStatement createObservationQuery(Connection connection)
			throws SQLException {

		// TODO: also use ResultSet.TYPE_SCROLL_SENSITIVE for panning (later)?

		if (obsStmt == null) {
			obsStmt = connection
					.prepareStatement("SELECT\n"
							+ "observations.JD AS jd,\n"
							+ "observations.magnitude AS magnitude,\n"
							+ "observations.fainterthan AS fainterthan,\n"
							+ "observations.uncertain AS uncertain,\n"
							+ "IF (observations.uncertain, observations.uncertainty, 0) AS uncertainty,\n"
							+ "observations.uncertaintyhq AS hq_uncertainty,\n"
							+ "observations.band AS band,\n"
							+ "observations.obscode AS observer_code,\n"
							+ "observations.commentcode AS comment_code,\n"
							+ "observations.comp1_C AS comp_star_1,\n"
							+ "observations.comp2_K AS comp_star_2,\n"
							+ "observations.charts AS charts,\n"
							+ "observations.comments AS comments,\n"
							+ "IF (observations.transformed = 1, 'yes', 'no') AS transformed,\n"
							+ "observations.airmass AS airmass,\n"
							+ "IF (observations.valflag = 'T', 'D', observations.valflag) AS valflag,\n"
							+ "observations.CMag AS cmag,\n"
							+ "observations.KMag AS kmag,\n"
							+ "observations.HJD AS hjd,\n"
							+ "observations.name AS name\n" + "FROM\n"
							+ "observations\n" + "WHERE\n"
							+ "observations.AUID = ? AND\n"
							+ "observations.JD >= ? AND\n"
							+ "observations.JD <= ?\n" + "ORDER BY\n"
							+ "observations.JD;");
		}

		return obsStmt;
	}

	/**
	 * Return a prepared statement for the specified AUID and date range. This
	 * may be a once-only-created prepared statement with parameters set.
	 * 
	 * @param obsStmt
	 *            The prepared statement on which to set parameters.
	 * @param auid
	 *            The star's AUID.
	 * @param minJD
	 *            The minimum Julian Day.
	 * @param maxJD
	 *            The maximum Julian Day.
	 * @return The prepared statement.
	 */
	public void setObservationQueryParams(PreparedStatement stmt, String auid,
			double minJD, double maxJD) throws SQLException {
		stmt.setString(1, auid);
		stmt.setDouble(2, minJD);
		stmt.setDouble(3, maxJD);
	}

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
	 * Return the AUID of the named star.
	 * 
	 * @param connection
	 *            A JDBC connection.
	 * @param name
	 *            The star name or alias.
	 * @return The AUID as a string, or null if it is not recognised as a valid
	 *         star name in the AAVSO International Database.
	 */
	public String getAUID(Connection connection, String name)
			throws SQLException {
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

		return auid;
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

	/**
	 * Return the name of the star given an AUID.
	 * 
	 * @param connection
	 *            A JDBC connection.
	 * @param name
	 *            The AUID.
	 * @return The star name as a string, or null if it is not recognised as a
	 *         valid AUID in the AAVSO International Database.
	 */
	public String getStarName(Connection connection, String auid)
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

		return starName;
	}

	/**
	 * Return a prepared statement for the specified CitizenSky user login. This
	 * is a once-only-created prepared statement with parameters set for each
	 * query execution.
	 * 
	 * @param connection
	 *            database connection.
	 * @return A prepared statement.
	 */
	protected PreparedStatement createCitizenSkyLoginQuery(Connection connection)
			throws SQLException {
		if (authStmt == null) {
			authStmt = connection
					.prepareStatement("select users.name, users.pass from users where users.name = ?");
		}

		return authStmt;
	}

	/**
	 * Authenticate with the CitizenSky database by prompting the user to enter
	 * credentials in a dialog, throwing an exception upon failure after
	 * retries. A manifest reason for this authentication is to obtain the
	 * observer code.
	 */
	public void authenticateWithCitizenSky() throws Exception {

		assert (this == userDBConnector);

		int retries = 3;
		boolean cancelled = false;

		while (!cancelled && !authenticatedWithCitizenSky && retries > 0) {
			MainFrame.getInstance().getStatusPane().setMessage(
					"CitizenSky Login...");

			LoginDialog loginDialog = new LoginDialog(
					"CitizenSky Authentication");

			cancelled = loginDialog.isCancelled();

			if (!cancelled) {
				String username = loginDialog.getUsername();
				String suppliedPassword = new String(loginDialog.getPassword());
				String passwordDigest = generateHexDigest(suppliedPassword);

				// Login to CitizenSky if we haven't done so already.
				MainFrame.getInstance().getStatusPane().setMessage(
						"Checking CitizenSky credentials...");
				Connection userConnection = createConnection();

				// Get a prepared statement to read a user details
				// from the database, setting the parameters for user
				// who is logging in.
				PreparedStatement userStmt = createCitizenSkyLoginQuery(userConnection);
				userStmt.setString(1, username);

				ResultSet userResults = userStmt.executeQuery();

				if (userResults.next()) {
					String actualPassword = userResults.getString("pass");
					if (passwordDigest.equals(actualPassword)) {
						authenticatedWithCitizenSky = true;
					} else {
						retries--;
					}
				}
			}
		}

		MainFrame.getInstance().getStatusPane().setMessage("");

		if (cancelled) {
			throw new CancellationException();
		}

		if (!authenticatedWithCitizenSky) {
			throw new AuthenticationError("Unable to authenticate.");
		}
	}

	/**
	 * @return the obsCode
	 */
	public String getObsCode() {
		return obsCode;
	}

	/**
	 * Generate a string consisting of 2 hex digits per byte of a MD5 message
	 * digest.
	 * 
	 * @param str
	 *            the string to generate a digest from
	 * @return the message digest as hex digits
	 */
	protected static String generateHexDigest(String str) {
		String digest = null;

		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.reset();
			md.update(str.getBytes());
			byte messageDigest[] = md.digest();
			StringBuffer hexString = new StringBuffer();
			for (int byteVal : messageDigest) {
				hexString.append(String.format("%02x", 0xFF & byteVal));
			}
			digest = hexString.toString();
		} catch (NoSuchAlgorithmException e) {
			MessageBox.showErrorDialog(MainFrame.getInstance(),
					"Error generating digest", e);
		}

		return digest;
	}
}
