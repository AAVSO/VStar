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
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

import org.aavso.tools.vstar.exception.ConnectionException;
import org.aavso.tools.vstar.ui.MainFrame;
import org.aavso.tools.vstar.ui.dialog.MessageBox;
import org.aavso.tools.vstar.ui.mediator.StarInfo;
import org.aavso.tools.vstar.ui.resources.ResourceAccessor;

/**
 * This class handles the details of connecting to the MySQL AAVSO observation
 * database, a data accessor. This is a Singleton.
 * 
 * Portions of code were adapted (with permission) from the AAVSO Zapper code
 * base.
 */
public class AAVSODatabaseConnector {

	// 30 seconds connection timeout.
	private final static int MAX_CONN_TIME = 15 * 1000;

	private final static String CONN_URL = "jdbc:mysql://"
			+ ResourceAccessor.getParam(0);

	private DatabaseType type;
	private Connection connection;

	// Observation retrieval statements.
	private PreparedStatement obsWithJDRangeStmt;
	private PreparedStatement obsWithNoJDRangeStmt;

	// Database connectors.
	public static AAVSODatabaseConnector observationDBConnector = new AAVSODatabaseConnector(
			DatabaseType.OBSERVATION);

	public static AAVSODatabaseConnector csUserDBConnector = new AAVSODatabaseConnector(
			DatabaseType.CS_USER);

	public static AAVSODatabaseConnector aavsoUserDBConnector = new AAVSODatabaseConnector(
			DatabaseType.AAVSO_USER);

	public static AAVSODatabaseConnector vsxDBConnector = new AAVSODatabaseConnector(
			DatabaseType.VSX);

	protected static AAVSODatabaseConnector utDBConnector = new AAVSODatabaseConnector(
			DatabaseType.UT);

	// Star name and AUID retrievers.

	private static IStarNameAndAUIDSource starNameAndAUIDRetriever = new VSXStarNameAndAUIDSource();

	static {
		try {
			Class.forName("com.mysql.jdbc.Driver", true,
					AAVSODatabaseConnector.class.getClassLoader());
		} catch (ClassNotFoundException e) {
			MessageBox.showErrorDialog(MainFrame.getInstance(),
					"Read from database", e);
		}
	}

	/**
	 * Constructor
	 */
	private AAVSODatabaseConnector(DatabaseType type) {
		this.type = type;
		this.connection = null;

		this.obsWithJDRangeStmt = null;
		this.obsWithNoJDRangeStmt = null;
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

			props.put("user", ResourceAccessor.getParam(6));
			props.put("password", ResourceAccessor.getParam(7));
			props.put("connectTimeout", MAX_CONN_TIME + "");

			try {
				// First try with port 3307...
				connection = DriverManager.getConnection(CONN_URL + ":3307/"
						+ ResourceAccessor.getParam(type.getDBNum()), props);
			} catch (Exception e1) {
				try {
					// ...and then with 3306.
					connection = DriverManager
							.getConnection(CONN_URL
									+ ":3306/"
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
	 * @return the connection
	 * TODO: build connection loss/timeout logic into here
	 */
	public Connection getConnection() throws ConnectionException {
		createConnection();
		return connection;
	}

	/**
	 * Return a prepared statement for for observation retrieval with a JD
	 * range. This is a once-only-created prepared statement with parameters
	 * set.
	 * 
	 * @param connection
	 *            A JDBC connection.
	 * @return A prepared statement.
	 */
	public PreparedStatement createObservationWithJDRangeQuery(
			Connection connection) throws SQLException {

		// TODO: also use ResultSet.TYPE_SCROLL_SENSITIVE for panning (later)?

		if (obsWithJDRangeStmt == null) {
			obsWithJDRangeStmt = connection
					.prepareStatement("SELECT\n"
							+ "observations.unique_id AS unique_id,\n"
							+ "observations.JD AS jd,\n"
							+ "observations.magnitude AS magnitude,\n"
							+ "observations.fainterthan AS fainterthan,\n"
							+ "observations.uncertain AS uncertain,\n"
							+ "observations.uncertainty AS uncertainty,\n"
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
							+ "observations.name AS name,\n"
							+ "observations.mtype AS mtype\n" + "FROM\n"
							+ "observations\n" + "WHERE\n"
							+ "observations.AUID = ? AND\n"
							+ "observations.JD >= ? AND\n"
							+ "observations.JD <= ? AND\n"
							+ "observations.valflag <> 'Y'\n" + "ORDER BY\n"
							+ "observations.JD;");
		}

		return obsWithJDRangeStmt;
	}

	/**
	 * Set prepared statement parameters for observation retrieval with AUID and
	 * date range.
	 * 
	 * @param obsWithJDRangeStmt
	 *            The prepared statement on which to set parameters.
	 * @param auid
	 *            The star's AUID.
	 * @param minJD
	 *            The minimum Julian Day.
	 * @param maxJD
	 *            The maximum Julian Day.
	 */
	public void setObservationWithJDRangeQueryParams(PreparedStatement stmt,
			String auid, double minJD, double maxJD) throws SQLException {
		stmt.setString(1, auid);
		stmt.setDouble(2, minJD);
		stmt.setDouble(3, maxJD);
	}

	/**
	 * Return a prepared statement for observation retrieval with no JD range.
	 * This is a once-only-created prepared statement with parameters set.
	 * 
	 * @param connection
	 *            A JDBC connection.
	 * @return A prepared statement.
	 */
	public PreparedStatement createObservationWithNoJDRangeQuery(
			Connection connection) throws SQLException {

		// TODO: also use ResultSet.TYPE_SCROLL_SENSITIVE for panning (later)?

		if (obsWithNoJDRangeStmt == null) {
			obsWithNoJDRangeStmt = connection
					.prepareStatement("SELECT\n"
							+ "observations.unique_id AS unique_id,\n"
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
							+ "observations.name AS name,\n"
							+ "observations.mtype AS mtype\n" + "FROM\n"
							+ "observations\n" + "WHERE\n"
							+ "observations.AUID = ?\n" + "ORDER BY\n"
							+ "observations.JD;");
		}

		return obsWithNoJDRangeStmt;
	}

	/**
	 * Set prepared statement parameters for observation retrieval with AUID but
	 * no date range.
	 * 
	 * @param obsWithJDRangeStmt
	 *            The prepared statement on which to set parameters.
	 * @param auid
	 *            The star's AUID.
	 */
	public void setObservationWithNoJDRangeQueryParams(PreparedStatement stmt,
			String auid) throws SQLException {
		stmt.setString(1, auid);
	}

	/**
	 * Return the AUID of the named star.
	 * 
	 * @param connection
	 *            A JDBC connection.
	 * @param name
	 *            The star name or alias.
	 * @return Information about the star, e.g. name, AUID, period.
	 */
	public StarInfo getAUID(Connection connection, String name)
			throws SQLException {
		return starNameAndAUIDRetriever.getStarByName(connection, name);
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
	public StarInfo getStarName(Connection connection, String auid)
			throws SQLException {
		return starNameAndAUIDRetriever.getStarByAUID(connection, auid);
	}
}
