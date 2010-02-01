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
package org.aavso.tools.vstar.ui.mediator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.aavso.tools.vstar.exception.ConnectionException;
import org.aavso.tools.vstar.exception.ObservationReadError;
import org.aavso.tools.vstar.exception.UnknownAUIDError;
import org.aavso.tools.vstar.exception.UnknownStarError;
import org.aavso.tools.vstar.input.database.AAVSODatabaseConnector;
import org.aavso.tools.vstar.input.database.AAVSODatabaseObservationReader;
import org.aavso.tools.vstar.ui.MainFrame;
import org.aavso.tools.vstar.ui.MenuBar;
import org.aavso.tools.vstar.ui.dialog.MessageBox;
import org.jdesktop.swingworker.SwingWorker;

/**
 * A concurrent task in which a new star from file request task is handled.
 */
public class NewStarFromDatabaseTask extends SwingWorker<Void, Void> {

	private Mediator mediator = Mediator.getInstance();

	private String starName;
	private String auid;
	private double minJD;
	private double maxJD;
	private boolean success;

	/**
	 * Constructor.
	 * 
	 * @param starName
	 *            The name of the star.
	 * @param auid
	 *            AAVSO unique ID for the star.
	 * @param minJD
	 *            The minimum Julian Day of the requested range.
	 * @param maxJD
	 *            The maximum Julian Day of the requested range.
	 */
	public NewStarFromDatabaseTask(String starName, String auid, double minJD,
			double maxJD) {
		this.starName = starName;
		this.auid = auid;
		this.minJD = minJD;
		this.maxJD = maxJD;
		this.success = false;
	}

	/**
	 * Main task. Executed in background thread.
	 */
	// TODO: parameterise with Boolean return type
	public Void doInBackground() {
		createDatabaseBasedObservationArtefacts();
		return null;
	}

	/**
	 * Create observation table and plot models from a file.
	 */
	protected void createDatabaseBasedObservationArtefacts() {

		ResultSet results = null;

		try {
			// Connect to the observation database if we haven't already
			// done so.
			MainFrame.getInstance().getStatusPane().setMessage(
					"Connecting to AAVSO database...");

			// TODO: only need VSX connector if star is not a 10-star,
			// i.e. auid and starName are both null.
			AAVSODatabaseConnector vsxConnector = AAVSODatabaseConnector.vsxDBConnector;
			Connection vsxConnection = vsxConnector.createConnection();

			// TODO: need to cache period and epoch for 10-star stars
			// Use this 2 parameter constructor until then.
			StarInfo starInfo = new StarInfo(starName, auid);

			// Do we need to ask for the AUID from the database before
			// proceeding?
			if (auid == null) {
				starInfo = vsxConnector.getAUID(vsxConnection, starName);
				auid = starInfo.getAuid();
				if (auid == null) {
					throw new UnknownStarError(starName);
				}
			}

			// No, do we need instead to ask for the star name because
			// we have an AUID but no star name?
			if (starName == null) {
				starInfo = vsxConnector.getStarName(vsxConnection, auid);
				starName = starInfo.getDesignation();
				if (starName == null) {
					throw new UnknownAUIDError(auid);
				}
			}

			updateProgress(2);

			// Get a prepared statement to read a set of observations
			// from the database, setting the parameters for the star
			// we are targeting. We distinguish between the case where
			// no JD min/max is supplied, and when one is.

			// TODO: 
			// - Hide all this statement stuff behind a get-observations
			//   method.
			// - Obs stmt creation and param setting should be a single 
			//   operation to avoid possible mismatched calls! Either that
			//   or remove stmt parameter from parameter setting method.

			AAVSODatabaseConnector obsConnector = AAVSODatabaseConnector.observationDBConnector;
			Connection obsConnection = obsConnector.createConnection();

			PreparedStatement obsStmt = null;

			if (minJD == 0 && maxJD == Double.MAX_VALUE) {
				obsStmt = obsConnector
						.createObservationWithNoJDRangeQuery(obsConnection);
			} else {
				obsStmt = obsConnector
						.createObservationWithJDRangeQuery(obsConnection);
			}
			updateProgress(2);

			// Execute the query, passing the result set to the
			// database observation retriever to give us the valid
			// and observation lists and categorised valid observation
			// map from which all else flows.
			if (minJD == 0 && maxJD == Double.MAX_VALUE) {
				obsConnector.setObservationWithNoJDRangeQueryParams(obsStmt,
						auid);
			} else {
				obsConnector.setObservationWithJDRangeQueryParams(obsStmt,
						auid, minJD, maxJD);
			}

			MainFrame.getInstance().getStatusPane().setMessage(
					"Retrieving observations...");
			results = obsStmt.executeQuery();
			updateProgress(2);

			AAVSODatabaseObservationReader databaseObsReader = new AAVSODatabaseObservationReader(
					results);

			databaseObsReader.retrieveObservations();
			updateProgress(2);

			if (databaseObsReader.getValidObservations().isEmpty()) {
				throw new ObservationReadError(
						"No observations for the specified period.");
			}

			MainFrame.getInstance().getStatusPane().setMessage(
					"Creating charts and tables...");

			updateProgress(2);

			// Create table/plot models and GUI elements.
			mediator.createNewStarObservationArtefacts(
					NewStarType.NEW_STAR_FROM_DATABASE, starInfo,
					databaseObsReader, 2);

			success = true;

		} catch (ConnectionException ex) {
			success = false;
			MessageBox.showErrorDialog(MainFrame.getInstance(),
					MenuBar.NEW_STAR_FROM_DATABASE,
					"Cannot connect to database.");
			MainFrame.getInstance().getStatusPane().setMessage("");
		} catch (Throwable ex) {
			success = false;
			MessageBox.showErrorDialog(MainFrame.getInstance(),
					MenuBar.NEW_STAR_FROM_DATABASE, ex);
			MainFrame.getInstance().getStatusPane().setMessage("");
		} finally {
			try {
				if (results != null) {
					results.close();
				}
			} catch (SQLException e) {
				MessageBox.showErrorDialog(MainFrame.getInstance(),
						MenuBar.NEW_STAR_FROM_DATABASE, e);
			}
		}
	}

	/**
	 * Executed in event dispatching thread.
	 */
	public void done() {
		mediator.getProgressNotifier().notifyListeners(
				ProgressInfo.COMPLETE_PROGRESS);

		// TODO: how to detect task cancellation and clean up map etc
	}

	// Update the progress of the task by the specified number of steps.
	private void updateProgress(int steps) {
		mediator.getProgressNotifier().notifyListeners(
				new ProgressInfo(ProgressType.INCREMENT_PROGRESS, steps));
	}
}