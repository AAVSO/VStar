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
package org.aavso.tools.vstar.scripting;

import java.io.File;
import java.io.IOException;

import org.aavso.tools.vstar.exception.ObservationReadError;
import org.aavso.tools.vstar.ui.dialog.MessageBox;
import org.aavso.tools.vstar.ui.mediator.AnalysisType;
import org.aavso.tools.vstar.ui.mediator.Mediator;

/**
 * This is VStar's scripting Application Programming Interface. An instance of
 * this class will be passed to scripts.
 * 
 * TODO: we will have to determine the best way to synchronise between
 * concurrent tasks corresponding to long running operations and the exit of
 * each of these methods. These methods must be synchronous otherwise we will be
 * trying to perform a phase plot or period analysis before we have loaded a
 * dataset completely, for example.
 */
public class VStarScriptingAPI {

	// private boolean busy;

	private Mediator mediator;

	/**
	 * Constructor.
	 */
	public VStarScriptingAPI() {
		mediator = Mediator.getInstance();
		// busy = false;

		// Mediator.getInstance().getProgressNotifier().addListener(
		// createProgressListener());
	}

	// private Listener<ProgressInfo> createProgressListener() {
	// return new Listener<ProgressInfo>() {
	// @Override
	// public void update(ProgressInfo info) {
	// switch (info.getType()) {
	// case COMPLETE_PROGRESS:
	// busy = false;
	// break;
	// }
	// }
	//
	// @Override
	// public boolean canBeRemoved() {
	// return false;
	// }
	// };
	// }

	/**
	 * Load a dataset from the specified path. This is equivalent to
	 * "File -> New Star from File..."
	 * 
	 * @param path
	 *            The path to the file.
	 * @return Whether or not this operation succeeded.
	 */
	public void loadFromFile(String path) {
		File f = new File(path);

		try {
			// busy = true;
			mediator.createObservationArtefactsFromFile(f);
			// while (!busy);
			// while (!mediator.isCurrentTaskDone());
		} catch (IOException e) {
			MessageBox
					.showErrorDialog("Load File", "Cannot load file: " + path);
		} catch (ObservationReadError e) {
			MessageBox.showErrorDialog("Load File",
					"Error reading observations from file: " + path
							+ " (reason: " + e.getLocalizedMessage() + ")");
		}
	}

	/**
	 * Switch to phase plot mode. If no phase plot has been created yet, this
	 * will open the phase parameter dialog.
	 */
	public void phasePlotMode() {
		// busy = true;
		mediator.changeAnalysisType(AnalysisType.PHASE_PLOT);
		// while (!busy);
		// while (!mediator.isCurrentTaskDone());
	}

	/**
	 * Switch to phase raw (light curve) mode.
	 */
	public void lightCurveMode() {
		// busy = true;
		mediator.changeAnalysisType(AnalysisType.RAW_DATA);
		// while (!busy);
		// while (!mediator.isCurrentTaskDone());
	}

	/**
	 * Exit VStar.
	 */
	public void exit() {
		mediator.quit();
	}
}
