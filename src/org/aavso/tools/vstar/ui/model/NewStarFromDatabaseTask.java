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
package org.aavso.tools.vstar.ui.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.aavso.tools.vstar.input.database.AAVSODatabaseConnector;
import org.aavso.tools.vstar.input.database.AAVSODatabaseObservationReader;
import org.aavso.tools.vstar.ui.MainFrame;
import org.aavso.tools.vstar.ui.MenuBar;
import org.aavso.tools.vstar.ui.MessageBox;
import org.aavso.tools.vstar.ui.StatusPane;
import org.jdesktop.swingworker.SwingWorker;

/**
 * A concurrent task in which a new star from file request task is handled.
 */
public class NewStarFromDatabaseTask extends SwingWorker<Void, Void> {
	private ModelManager modelMgr = ModelManager.getInstance();
	private StatusPane statusBar = MainFrame.getInstance().getStatusPane();

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
	public Void doInBackground() {
		this.success = createDatabaseBasedObservationArtefacts();
		return null;
	}

	/**
	 * Create observation table and plot models from a file.
	 */
	protected boolean createDatabaseBasedObservationArtefacts() {

		boolean success = true;

		try {
			// CitizenSky authentication.
			AAVSODatabaseConnector userConnector = AAVSODatabaseConnector.userDBConnector;
			userConnector.authenticateWithCitizenSky(statusBar);
			updateProgress(2);

			// TODO: query for observer code
			
			// Connect to the observation database if we haven't already
			// done so.
			statusBar.setMessage("Connecting to AAVSO database...");
			AAVSODatabaseConnector obsConnector = AAVSODatabaseConnector.observationDBConnector;

			Connection obsConnection = obsConnector.createConnection();

			// Get a prepared statement to read a set of observations
			// from the database, setting the parameters for the star
			// we are targeting.
			PreparedStatement obsStmt = obsConnector
					.createObservationQuery(obsConnection);
			updateProgress(2);

			// Execute the query, passing the result set to the
			// database observation retriever to give us the valid
			// and observation lists and categorised valid observation
			// map from which all else flows.
			obsConnector.setObservationQueryParams(obsStmt, auid, minJD, maxJD);
			statusBar.setMessage("Retrieving observations...");
			ResultSet results = obsStmt.executeQuery();
			updateProgress(2);

			AAVSODatabaseObservationReader databaseObsReader = new AAVSODatabaseObservationReader(
					results);

			databaseObsReader.retrieveObservations();
			updateProgress(2);

			statusBar.setMessage("Creating charts and tables...");

			modelMgr.clearData();

			modelMgr.setValidObsList(databaseObsReader.getValidObservations());
			modelMgr.setInvalidObsList(databaseObsReader
					.getInvalidObservations());
			modelMgr.setValidObservationCategoryMap(databaseObsReader
					.getValidObservationCategoryMap());

			updateProgress(2);

			// Create table/plot models and GUI elements.
			modelMgr.createObservationArtefacts(
					NewStarType.NEW_STAR_FROM_DATABASE, starName, 2);
		} catch (Exception ex) {
			MessageBox.showErrorDialog(MainFrame.getInstance(),
					MenuBar.NEW_STAR_FROM_DATABASE, ex);
			success = false;
		}

		return success;
	}

	/**
	 * Executed in event dispatching thread.
	 */
	public void done() {
		// Task ends.
		modelMgr.getProgressNotifier().notifyListeners(
				ProgressInfo.COMPLETE_PROGRESS);

		if (success) {
			// Notify whoever is listening that a new star has been loaded,
			// passing GUI components in the message.
			NewStarMessage msg = new NewStarMessage(
					NewStarType.NEW_STAR_FROM_DATABASE, modelMgr
							.getObsChartPane(), modelMgr
							.getObsAndMeanChartPane(), modelMgr
							.getObsListPane(), modelMgr.getMeansListPane());

			modelMgr.getNewStarNotifier().notifyListeners(msg);
		}
	}

	// Update the progress of the task by the specified number of steps.
	private void updateProgress(int steps) {
		modelMgr.getProgressNotifier().notifyListeners(
				new ProgressInfo(ProgressType.INCREMENT_PROGRESS, steps));
	}
}