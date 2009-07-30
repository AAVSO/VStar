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
import java.sql.Driver;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

import org.aavso.tools.vstar.ui.LoginDialog;
import org.aavso.tools.vstar.ui.ResourceAccessor;

/**
 * This class handles the details of connecting to the MySQL AAVSO observation
 * database, a data accessor. This is a Singleton.
 * 
 * Portions of code were adapted (with permission) from the AAVSO Zapper code
 * base.
 */
public class AAVSODatabaseConnector {

	private final static String CONN_URL = "jdbc:mysql://"
			+ ResourceAccessor.getParam(0) + "/";

	private DatabaseType type;
	private Driver driver;
	private Connection connection;
	private PreparedStatement stmt;

	public static AAVSODatabaseConnector observationDBConnector = new AAVSODatabaseConnector(DatabaseType.OBSERVATION);
	public static AAVSODatabaseConnector userDBConnector = new AAVSODatabaseConnector(DatabaseType.USER);
	
	/**
	 * Constructor
	 */
	public AAVSODatabaseConnector(DatabaseType type) {
		this.type = type;
		this.driver = null;
		this.connection = null;
		this.stmt = null;
	}

	/**
	 * Create a connection to the database if it has not already been created.
	 * 
	 * @throws Exception
	 *             if there was an error creating the connection.
	 */
	public Connection createConnection() throws Exception {

		// TODO:
		// - Set connection timeout (property?)

		int retries = 3;

		// TODO: isValid() on 1.5 JDBC?
		while ((connection == null /*|| !connection.isValid(5)*/) && retries > 0) {
			LoginDialog loginDialog = new LoginDialog(
					"Enter AAVSO database login details");

			if (!loginDialog.isCancelled()) {
				Properties props = new Properties();
				props.put("user", loginDialog.getUsername());
				props.put("password", new String(loginDialog.getPassword()));
				try {
					props.put("port", (3 * 11 * 100 + 7) + "");
					connection = getDriver().connect(
							CONN_URL + ResourceAccessor.getParam(type.getDBNum()), props);
				} catch (Exception e1) {
					props.put("port", ((3 * 11 * 100 + 7) - 1) + "");
					connection = getDriver().connect(
							CONN_URL + ResourceAccessor.getParam(type.getDBNum()), props);
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
	 * @return A prepared statement.
	 */
	public PreparedStatement createObservationQuery(Connection connection)
			throws SQLException {

		// TODO: also use ResultSet.TYPE_SCROLL_SENSITIVE for panning?

		if (stmt == null) {
			stmt = connection
					.prepareStatement("SELECT\n"
							+ "observations.JD AS jd,\n"
							+ "observations.magnitude AS magnitude,\n"
							+ "observations.fainterthan AS fainterthan,\n"
							+ "observations_extra.uncertain AS uncertain,\n"
							+ "IF (observations_extra.uncertain, observations.uncertainty, 0) AS uncertainty,\n"
							+ "observations.uncertaintyhq AS hq_uncertainty,\n"
							+ "observations.band AS band,\n"
							+ "observations.obscode AS observer_code,\n"
							+ "observations.commentcode AS comment_code,\n"
							+ "observations.comp1_C AS comp_star_1,\n"
							+ "observations.comp2_K AS comp_star_2,\n"
							+ "observations.charts AS charts,\n"
							+ "observations_extra.comments AS comments,\n"
							+ "IF (observations_extra.transformed = 1, 'yes', 'no') AS transformed,\n"
							+ "observations_extra.airmass AS airmass,\n"
							+ "IF (observations.valflag = 'T', 'D', observations.valflag) AS valflag,\n"
							+ "observations_extra.CMag AS cmag,\n"
							+ "observations_extra.KMag AS kmag,\n"
							+ "observations_extra.HJD AS hjd,\n"
							+ "observations.name AS name\n"
							+ "FROM\n"
							+ "observations,\n"
							+ "observations_extra\n"
							+ "WHERE\n"
							+ "observations.unique_id = observations_extra.unique_id AND\n"
							+ "observations.AUID = ? AND\n"
							+ "observations.JD >= ? AND\n"
							+ "observations.JD <= ? " + "ORDER BY\n"
							+ "observations.JD;");
		}

		return stmt;
	}

	/**
	 * Return a prepared statement for the specified AUID and date range. This
	 * may be a once-only-created prepared statement with parameters set.
	 * 
	 * @param stmt
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
	 * Get an instance of a MySQL database JDBC driver class.
	 * 
	 * @return The driver class instance.
	 * @throws SQLException
	 *             If there was a problem creating this instance.
	 */
	public Driver getDriver() throws SQLException, ClassCastException,
			ClassNotFoundException, InstantiationException,
			IllegalAccessException {
		if (driver == null) {
			Class<?> driverClass = Class.forName("com.mysql.jdbc.Driver", true,
					AAVSODatabaseConnector.class.getClassLoader());
			driver = (Driver) driverClass.newInstance();
		}
		return driver;
	}
}
