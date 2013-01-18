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

import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.exception.ObservationReadError;
import org.aavso.tools.vstar.ui.MainFrame;
import org.aavso.tools.vstar.ui.dialog.MessageBox;
import org.aavso.tools.vstar.ui.mediator.AnalysisType;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.message.AnalysisTypeChangeMessage;
import org.aavso.tools.vstar.ui.model.plot.ObservationAndMeanPlotModel;
import org.aavso.tools.vstar.util.notification.Listener;

/**
 * This is VStar's scripting Application Programming Interface. An instance of
 * this class will be passed to scripts.
 * 
 * All methods are synchronised to ensure that only one API is being called at a
 * time.
 */
public class VStarScriptingAPI {

	private Mediator mediator = Mediator.getInstance();

	private final static VStarScriptingAPI instance = new VStarScriptingAPI();

	private AnalysisTypeChangeMessage analysisTypeMsg;

	/**
	 * Constructor.
	 */
	private VStarScriptingAPI() {
		mediator = Mediator.getInstance();
		analysisTypeMsg = null;
		mediator.getAnalysisTypeChangeNotifier().addListener(
				createAnalysisTypeChangeListener());
	}

	/**
	 * Return Singleton.
	 */
	public static VStarScriptingAPI getInstance() {
		return instance;
	}

	/**
	 * Return an analysis type change listener.
	 */
	private Listener<AnalysisTypeChangeMessage> createAnalysisTypeChangeListener() {
		return new Listener<AnalysisTypeChangeMessage>() {
			@Override
			public void update(AnalysisTypeChangeMessage info) {
				analysisTypeMsg = info;
			}

			@Override
			public boolean canBeRemoved() {
				return false;
			}
		};
	}

	/**
	 * Load a dataset from the specified path. This is equivalent to
	 * "File -> New Star from File..."
	 * 
	 * @param path
	 *            The path to the file.
	 */
	public synchronized void loadFromFile(final String path) {
		commonLoadFromFile(path, false);
	}

	/**
	 * Load a dataset from the specified path, adding it to the existing
	 * dataset. This is equivalent to "File -> New Star from File..." with the
	 * additive checkbox selected.
	 * 
	 * @param path
	 *            The path to the file.
	 */
	public synchronized void additiveLoadFromFile(final String path) {
		commonLoadFromFile(path, true);
	}

	/**
	 * Common dataset file load method. 
	 * @param path
	 *            The path to the file.
	 * @param isAdditive
	 *            Is this load additive?
	 */
	private void commonLoadFromFile(final String path, boolean isAdditive) {
		File f = new File(path);

		try {
			mediator.createObservationArtefactsFromFile(f, isAdditive);
		} catch (IOException e) {
			MessageBox
					.showErrorDialog("Load File", "Cannot load file: " + path);
		} catch (ObservationReadError e) {
			MessageBox.showErrorDialog("Load File",
					"Error reading observations from file: " + path
							+ " (reason: " + e.getLocalizedMessage() + ")");
		}

		mediator.waitForJobCompletion();
	}

	/**
	 * Load a dataset from the AAVSO international database.
	 * 
	 * @param name
	 *            The name of the object.
	 * @param minJD
	 *            The minimum JD of the range to be loaded.
	 * @param maxJD
	 *            The maximum JD of the range to be loaded.
	 */
	public synchronized void loadFromAID(final String name, double minJD,
			double maxJD) {
		String auid = null;
		mediator.createObservationArtefactsFromDatabase(name, auid, minJD,
				maxJD);
		mediator.waitForJobCompletion();
	}

	/**
	 * Save the raw dataset (light curve observation list) to a file of rows of
	 * values separated by the specified delimiter.
	 * 
	 * @param path
	 *            The path to the file to save to (as a string).
	 * @param delimiter
	 *            The delimiter between data items.
	 */
	public synchronized void saveRawData(final String path, String delimiter) {
		lightCurveMode(); // save raw data not phase plot data
		mediator.saveObsListToFile(MainFrame.getInstance(), new File(path),
				delimiter);
		mediator.waitForJobCompletion();
	}

	// TODO: save means, model, residuals methods...

	/**
	 * Save the light curve to a PNG image file.
	 * 
	 * @param path
	 *            The path to the file to save to (as a string).
	 * @param width
	 *            The desired width of the image.
	 * @param height
	 *            The desired height of the image.
	 */
	public synchronized void saveLightCurve(final String path, int width,
			int height) {
		lightCurveMode();
		mediator.saveCurrentPlotToFile(new File(path), width, height);
	}

	/**
	 * Save the phase plot to a PNG image file.
	 * 
	 * @param path
	 *            The path to the file to save to (as a string).
	 * @param width
	 *            The desired width of the image.
	 * @param height
	 *            The desired height of the image.
	 */
	public synchronized void savePhasePlot(final String path, int width,
			int height) {
		phasePlotMode();
		mediator.saveCurrentPlotToFile(new File(path), width, height);
	}

	/**
	 * Switch to phase plot mode. If no phase plot has been created yet, this
	 * will open the phase parameter dialog.
	 */
	public synchronized void phasePlotMode() {
		mediator.changeAnalysisType(AnalysisType.PHASE_PLOT);
		mediator.waitForJobCompletion();
	}

	/**
	 * Switch to phase raw (light curve) mode.
	 */
	public synchronized void lightCurveMode() {
		mediator.changeAnalysisType(AnalysisType.RAW_DATA);
		mediator.waitForJobCompletion();
	}

	/**
	 * Create a phase plot given period and epoch.
	 * 
	 * @param period
	 *            The period on which to fold the data.
	 * @param epoch
	 *            The epoch (first Julian Date) for the phase plot.
	 */
	public synchronized void phasePlot(double period, double epoch) {
		mediator.createPhasePlot(period, epoch);
		mediator.waitForJobCompletion();
	}

	/**
	 * Shows the band names in the current dataset.
	 */
	public synchronized void showBands() {
		ObservationAndMeanPlotModel model = analysisTypeMsg
				.getObsAndMeanChartPane().getObsModel();

		String[] names = new String[model.getSeriesCount()];

		int i = 0;
		for (SeriesType type : model.getSeriesKeys()) {
			names[i++] = type.getShortName();
			System.out.println(type.getShortName());
		}
	}

	/**
	 * Pause for the specified number of milliseconds.
	 * 
	 * @param millis
	 *            The time to pause for.
	 */
	public synchronized void pause(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
		}
	}

	/**
	 * Exit VStar.
	 */
	public synchronized void exit() {
		mediator.quit();
	}
}
