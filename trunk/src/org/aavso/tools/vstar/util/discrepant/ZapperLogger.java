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
package org.aavso.tools.vstar.util.discrepant;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.aavso.tools.vstar.exception.ConnectionException;
import org.aavso.tools.vstar.input.database.AAVSODatabaseConnector;
import org.aavso.tools.vstar.ui.dialog.MessageBox;
import org.aavso.tools.vstar.ui.resources.ResourceAccessor;

/**
 * A discrepant reporter that writes to the Zapper log.
 * 
 * Notes: - We could also obtain a user database connection here when we want to
 * authenticate on-demand to AAVSO/CS for discrepant reporting. - It would also
 * be good to query the zapperlog to check that the discrepant has not already
 * been reported; which combination of fields makes sense to be checked (e.g.
 * unique_id & editor)?
 */
public class ZapperLogger implements IDiscrepantReporter {

	private static ZapperLogger instance = null;

	private PreparedStatement updateLogStatement;
	private Connection connection;

	/**
	 * Singleton getter.
	 */
	public static ZapperLogger getInstance() {
		if (instance == null) {
			instance = new ZapperLogger();
		}

		return instance;
	}

	/**
	 * Singleton constructor.
	 */
	private ZapperLogger() {
		updateLogStatement = null;
		try {
			connection = AAVSODatabaseConnector.observationDBConnector
					.createConnection();
		} catch (ConnectionException e) {
			MessageBox.showErrorDialog("Zapper Logger Creation Error", e);
		}
	}

	@Override
	public void lodge(DiscrepantReport report) {
		try {
			createUpdateLogStatement();

			updateLogStatement.setString(1, report.getAuid());
			updateLogStatement.setString(2, report.getName());
			updateLogStatement.setInt(3, report.getUniqueId());
			updateLogStatement.setString(4, report.getEditor());
			updateLogStatement.setDouble(5, report.getJd());
			updateLogStatement.setString(6, report.getComments());

			updateLogStatement.executeUpdate();

			MessageBox.showMessageDialog("Discrepant Report",
					"Lodged Discrepant Report with AAVSO");
		} catch (SQLException e) {
			MessageBox.showErrorDialog("Discrepant Reporting Error", e);
		}
	}

	private PreparedStatement createUpdateLogStatement() throws SQLException {
		final String z1 = ResourceAccessor.getParam(7) + "."
				+ ResourceAccessor.getParam(7) + "log";

		if (updateLogStatement == null) {
			updateLogStatement = connection.prepareStatement("INSERT INTO "
					+ z1 + " VALUES (?, ?, ?, ?, ?, ?, '0')");
		}

		return updateLogStatement;
	}
}
