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

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.aavso.tools.vstar.exception.ConnectionException;
import org.aavso.tools.vstar.ui.dialog.MessageBox;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.StarInfo;
import org.aavso.tools.vstar.ui.resources.ResourceAccessor;

import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * This class handles the details of connecting to the MySQL AAVSO observation
 * database, a data accessor. This is a Singleton.
 * 
 * Portions of code were adapted (with permission) from the AAVSO Zapper code
 * base.
 */
public class AAVSODatabaseConnector {

	private final static int IDLE_TIMEOUT_HRS = 2 * 60 * 60; // 2 hours
	private final static int IDLE_EXCESS_CONN_TIMEOUT_MINS = 15 * 60; // 15
																		// minutes

	private final static int CONNECTION_RETRY_INTERVAL_MSECS = 500; // 500
																	// millisecs
	private final static int CONNECTION_RETRY_ATTEMPTS = 5;

	private final static int IDLE_CONNECTION_TEST_PERIOD_SECS = 120; // 2
																		// minutes

	private final static String PREFERRED_TEST_SQL = "SELECT 1";

	private final static int MAX_PREP_STATEMENTS = 180;

	private final static int INITIAL_CONNECTIONS = 5;

	private final static String CONN_URL = "jdbc:mysql://"
			+ ResourceAccessor.getParam(0);

	private DatabaseType type;
	ComboPooledDataSource connectionPool3306;
	ComboPooledDataSource connectionPool3307;
	// private Connection connection;

	// Observation retrieval statements.
	private PreparedStatement obsWithJDRangeStmt;
	private PreparedStatement obsWithNoJDRangeStmt;

	// Credit index to name map.
	private Map<Integer, String> creditMap = null;

	// Database connectors.
	public static AAVSODatabaseConnector observationDBConnector;

	public static AAVSODatabaseConnector csUserDBConnector;

	public static AAVSODatabaseConnector aavsoUserDBConnector;

	public static AAVSODatabaseConnector vsxDBConnector;

	public static AAVSODatabaseConnector utDBConnector;

	public static AAVSODatabaseConnector memberDBConnector;

	// Star name and AUID retrievers.

	private static IStarNameAndAUIDSource starNameAndAUIDRetriever = new VSXStarNameAndAUIDSource();

	static {
		try {
			observationDBConnector = new AAVSODatabaseConnector(
					DatabaseType.OBSERVATION);

			csUserDBConnector = new AAVSODatabaseConnector(DatabaseType.CS_USER);

			aavsoUserDBConnector = new AAVSODatabaseConnector(
					DatabaseType.AAVSO_USER);

			vsxDBConnector = new AAVSODatabaseConnector(DatabaseType.VSX);

			utDBConnector = new AAVSODatabaseConnector(DatabaseType.UT);

			memberDBConnector = new AAVSODatabaseConnector(DatabaseType.MEMBER);

		} catch (Exception e) {
			MessageBox.showErrorDialog(Mediator.getUI().getComponent(),
					"Database configuration", e);
		}
	}

	/**
	 * Constructor
	 */
	private AAVSODatabaseConnector(DatabaseType type) throws SQLException,
			PropertyVetoException {
		this.type = type;

		// TODO: are we running the risk of incorrectly caching these?
		this.obsWithJDRangeStmt = null;
		this.obsWithNoJDRangeStmt = null;

		Properties p = new Properties(System.getProperties());
		p.put("com.mchange.v2.log.MLog", "com.mchange.v2.log.FallbackMLog");
		p.put("com.mchange.v2.log.FallbackMLog.DEFAULT_CUTOFF_LEVEL", "SEVERE");
		System.setProperties(p);
		
		connectionPool3306 = new ComboPooledDataSource();
		initPool(connectionPool3306, 3306);
		connectionPool3307 = new ComboPooledDataSource();
		initPool(connectionPool3307, 3307);
	}

	public void initPool(ComboPooledDataSource connectionPool, int port)
			throws SQLException, PropertyVetoException {
		connectionPool.setDriverClass("com.mysql.jdbc.Driver");
		connectionPool.setJdbcUrl(CONN_URL + ":" + port + "/"
				+ ResourceAccessor.getParam(type.getDBNum()));

		connectionPool.setUser(ResourceAccessor.getParam(6));
		connectionPool.setPassword(ResourceAccessor.getParam(7));

		connectionPool.setMaxStatements(MAX_PREP_STATEMENTS);
		connectionPool.setPreferredTestQuery(PREFERRED_TEST_SQL);
		connectionPool.setTestConnectionOnCheckout(true);

		// connectionPool.setAcquireRetryDelay(CONNECTION_RETRY_INTERVAL_MSECS);
		// connectionPool.setAcquireRetryAttempts(CONNECTION_RETRY_ATTEMPTS);
		// connectionPool.setMaxIdleTime(IDLE_TIMEOUT_HRS);
		// connectionPool.setInitialPoolSize(INITIAL_CONNECTIONS);
		// connectionPool.setMaxIdleTimeExcessConnections(IDLE_EXCESS_CONN_TIMEOUT_MINS);
		// connectionPool.setIdleConnectionTestPeriod(IDLE_CONNECTION_TEST_PERIOD_SECS);
	}

	public void cleanupPools() {
		connectionPool3306.close();
		connectionPool3307.close();
	}

	public static void cleanup() {
		observationDBConnector.cleanupPools();
		csUserDBConnector.cleanupPools();
		aavsoUserDBConnector.cleanupPools();
		vsxDBConnector.cleanupPools();
		utDBConnector.cleanupPools();
		memberDBConnector.cleanupPools();
	}

	/**
	 * Obtain a connection to the database from the connection pool.
	 * 
	 * @throws ConnectionException
	 *             if there was an error obtaining the connection.
	 */
	public Connection getConnection() throws ConnectionException {
		// TODO: have prep statements ask for connections so it's all internal!

		try {
			// First try with port 3306...
			return connectionPool3306.getConnection();
		} catch (Exception e1) {
			try {
				// ...and then with 3307.
				return connectionPool3307.getConnection();
			} catch (Exception e2) {
				throw new ConnectionException(e2.getMessage());
			}
		}
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
							// +
							// "IF (observations.uncertain, observations.uncertainty, 0) AS uncertainty,\n"
							+ "observations.uncertaintyhq AS hq_uncertainty,\n"
							+ "observations.obstype AS obstype,\n"
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
							+ "observations.group AS 'group',\n"
							+ "observations.pubref AS pubref,\n"
							+ "observations.digitizer AS digitizer,\n"
							+ "observations.mtype AS mtype,\n"
							+ "observations.credit AS credit\n" + "FROM\n"
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
							+ "observations.uncertainty AS uncertainty,\n"
							// +
							// "IF (observations.uncertain, observations.uncertainty, 0) AS uncertainty,\n"
							+ "observations.uncertaintyhq AS hq_uncertainty,\n"
							+ "observations.obstype AS obstype,\n"
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
							+ "observations.group AS 'group',\n"
							+ "observations.pubref AS pubref,\n"
							+ "observations.digitizer AS digitizer,\n"
							+ "observations.mtype AS mtype,\n"
							+ "observations.credit AS credit\n" + "FROM\n"
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
	 * Retrieve a map of credit indices (used in AID credit field) to credit
	 * names.
	 * 
	 * @return The mapping of indices to names.
	 */
	public synchronized Map<Integer, String> retrieveCreditMap() {
		if (creditMap == null) {
			creditMap = new TreeMap<Integer, String>();

			Connection conn = null;
			try {
				conn = getConnection();
				final PreparedStatement stmt = conn
						.prepareStatement("select * from aid.credits;");

				ResultSet source = stmt.executeQuery();

				while (source.next()) {
					Integer id = source.getInt("id");
					String abbr = source.getString("abbr");
					creditMap.put(id, abbr);
				}
			} catch (SQLException e) {
				MessageBox.showWarningDialog("Credits",
						"Cannot retrieve credits");
			} catch (ConnectionException e) {
				MessageBox.showWarningDialog("Credits",
						"Cannot retrieve credits; no connection");
			} finally {
				// try {
				// if (conn != null) {
				// conn.close();
				// }
				// } catch (SQLException e) {
				// MessageBox.showWarningDialog("Credits",
				// "Cannot retrieve credits");
				// }
			}
		}

		return creditMap;
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
