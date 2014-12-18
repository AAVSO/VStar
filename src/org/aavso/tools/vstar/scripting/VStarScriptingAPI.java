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
import java.net.URL;

import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.exception.ObservationReadError;
import org.aavso.tools.vstar.plugin.InputType;
import org.aavso.tools.vstar.plugin.ObservationSourcePluginBase;
import org.aavso.tools.vstar.ui.dialog.MessageBox;
import org.aavso.tools.vstar.ui.mediator.AnalysisType;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.message.AnalysisTypeChangeMessage;
import org.aavso.tools.vstar.ui.model.plot.ObservationAndMeanPlotModel;
import org.aavso.tools.vstar.ui.resources.PluginLoader;
import org.aavso.tools.vstar.util.notification.Listener;

/**
 * This is VStar's scripting Application Programming Interface. An instance of
 * this class will be passed to scripts.
 * 
 * All methods are synchronised to ensure that only one API method is being
 * called at a time.
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
	 * 
	 * @param path
	 *            The path to the file.
	 * @param isAdditive
	 *            Is this load additive?
	 */
	private void commonLoadFromFile(final String path, boolean isAdditive) {
		init();

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
	 * Load a dataset from the specified path using the (possibly partial)
	 * plug-in name to identify the observation source plug-in to use. This is
	 * equivalent to "File -> New Star from <obs-source-type>..."
	 * 
	 * @param pluginName
	 *            The sub-string with which to match the plug-in name.
	 * @param path
	 *            The path to the file.
	 */
	public synchronized void loadFromFile(final String pluginName,
			final String path) {
		commonLoadFromFilePlugin(pluginName, InputType.FILE, path, false);
	}

	/**
	 * Load a dataset from the specified path, adding it to the existing dataset
	 * using the (possibly partial) plug-in name to identify the observation
	 * source plug-in to use. This is equivalent to
	 * "File -> New Star from <obs-source-type>..." with the additive checkbox
	 * selected.
	 * 
	 * @param pluginName
	 *            The sub-string with which to match the plug-in name.
	 * @param path
	 *            The path to the file.
	 */
	public synchronized void additiveLoadFromFile(final String pluginName,
			final String path) {
		commonLoadFromFilePlugin(pluginName, InputType.FILE, path, true);
	}

	/**
	 * Load a dataset from the specified URL using the (possibly partial)
	 * plug-in name to identify the observation source plug-in to use.
	 * 
	 * @param pluginName
	 *            The sub-string with which to match the plug-in name.
	 * @param url
	 *            The URL.
	 */
	public synchronized void loadFromURL(final String pluginName,
			final String url) {
		commonLoadFromFilePlugin(pluginName, InputType.URL, url, false);
	}

	/**
	 * Load a dataset from the specified URL, adding it to the existing dataset,
	 * using the (possibly partial) plug-in name to identify the observation
	 * source plug-in to use.
	 * 
	 * @param pluginName
	 *            The sub-string with which to match the plug-in name.
	 * @param url
	 *            The URL.
	 */
	public synchronized void additiveLoadFromURL(final String pluginName,
			final String url) {
		commonLoadFromFilePlugin(pluginName, InputType.URL, url, true);
	}

	/**
	 * Common dataset plug-in load method.
	 * 
	 * @param pluginName
	 *            The sub-string with which to match the plug-in name.
	 * @param inputType
	 *            The input type (e.g. file, URL).
	 * @param location
	 *            The path to the file or the URL.
	 * @param isAdditive
	 *            Is this load additive?
	 */
	private void commonLoadFromFilePlugin(final String pluginName,
			InputType inputType, final String location, boolean isAdditive) {
		init();

		ObservationSourcePluginBase obSourcePlugin = null;

		for (ObservationSourcePluginBase plugin : PluginLoader
				.getObservationSourcePlugins()) {
			if (plugin.getDisplayName().contains(pluginName)
					&& (plugin.getInputType() == inputType || plugin
							.getInputType() == InputType.FILE_OR_URL)) {
				obSourcePlugin = plugin;
				break;
			}
		}

		if (obSourcePlugin != null) {
			try {
				if (inputType == InputType.FILE) {
					mediator.createObservationArtefactsFromObSourcePlugin(
							obSourcePlugin, new File(location), isAdditive);
				} else if (inputType == InputType.URL) {
					mediator.createObservationArtefactsFromObSourcePlugin(
							obSourcePlugin, new URL(location), isAdditive);

				}
			} catch (IOException e) {
				MessageBox.showErrorDialog("Load File", "Cannot load file: "
						+ location);
			} catch (ObservationReadError e) {
				MessageBox.showErrorDialog("Load File",
						"Error reading observations from file: " + location
								+ " (reason: " + e.getLocalizedMessage() + ")");
			}
		} else {
			MessageBox.showErrorDialog("Load File",
					"No matching observation plugin found '" + pluginName
							+ "'");
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
		init();
		String auid = null;
		mediator.createObservationArtefactsFromDatabase(name, auid, minJD,
				maxJD, false);
		mediator.waitForJobCompletion();
	}

	/**
	 * Load a dataset from the AAVSO international database, adding it to the
	 * existing dataset.
	 * 
	 * @param name
	 *            The name of the object.
	 * @param minJD
	 *            The minimum JD of the range to be loaded.
	 * @param maxJD
	 *            The maximum JD of the range to be loaded.
	 */
	public synchronized void additiveLoadFromAID(final String name,
			double minJD, double maxJD) {
		init();
		String auid = null;
		mediator.createObservationArtefactsFromDatabase(name, auid, minJD,
				maxJD, true);
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
		init();
		lightCurveMode(); // save raw data not phase plot data
		mediator.saveObsListToFile(Mediator.getUI().getComponent(), new File(
				path), delimiter);
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
		init();
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
		init();
		phasePlotMode();
		mediator.saveCurrentPlotToFile(new File(path), width, height);
	}

	/**
	 * Switch to phase plot mode. If no phase plot has been created yet, this
	 * will open the phase parameter dialog.
	 */
	public synchronized void phasePlotMode() {
		init();
		mediator.changeAnalysisType(AnalysisType.PHASE_PLOT);
		mediator.waitForJobCompletion();
	}

	/**
	 * Switch to phase raw (light curve) mode.
	 */
	public synchronized void lightCurveMode() {
		init();
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
		init();
		mediator.createPhasePlot(period, epoch);
		mediator.waitForJobCompletion();
	}

	/**
	 * Return the last error.
	 * 
	 * @return The error string; may be null.
	 */
	public synchronized String getError() {
		return ScriptRunner.getInstance().getError();
	}

	/**
	 * Return the last warning.
	 * 
	 * @return The warning string; may be null.
	 */
	public synchronized String getWarning() {
		return ScriptRunner.getInstance().getWarning();
	}

	/**
	 * Shows the band names in the current dataset.
	 * 
	 * @deprecated return a string or array or strings instead!
	 */
	public synchronized void showBands() {
		init();
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
	 * Returns a comma-separated string of series names for the current dataset,
	 * including bands and synthetic series such as means, model, residuals.
	 * 
	 * @return a comma-separated series names
	 */
	public synchronized String getSeries() {
		init();
		ObservationAndMeanPlotModel model = analysisTypeMsg
				.getObsAndMeanChartPane().getObsModel();

		String nameStr = "";

		for (SeriesType type : model.getSeriesKeys()) {
			nameStr += type.getShortName() + ",";
		}

		return nameStr.substring(0, nameStr.lastIndexOf(",") - 1);
	}

	/**
	 * Makes the specified series in the current dataset visible. Calling this
	 * method more than one consecutive time with the same visibility value has
	 * no effect on the current visibility status of a series.
	 * 
	 * @param seriesName
	 *            The long name (e.g. "Johnson V" not "V") of the series to make
	 *            visible.
	 */
	public synchronized void makeVisible(final String seriesName) {
		init();
		ObservationAndMeanPlotModel obsPlotModel = analysisTypeMsg
				.getObsAndMeanChartPane().getObsModel();

		if (SeriesType.exists(seriesName)) {
			SeriesType series = SeriesType.getSeriesFromDescription(seriesName);

			if (obsPlotModel.getSeriesKeys().contains(series)) {
				int seriesNum = obsPlotModel.getSrcTypeToSeriesNumMap().get(
						series);
				obsPlotModel.changeSeriesVisibility(seriesNum, true);
			} else {
				ScriptRunner.getInstance().setError(
						"Series does not exist in loaded dataset: "
								+ seriesName);
			}
		} else {
			ScriptRunner.getInstance().setError(
					"Unknown series type: " + seriesName);
		}

		mediator.waitForJobCompletion();
	}

	// TODO: makeInvisible() or hideSeries() and showSeries()

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
	 * Exit VStar
	 */
	public synchronized void exit() {
		mediator.quit();
	}

	// Helpers

	private void init() {
		clearError();
	}

	private void clearError() {
		ScriptRunner.getInstance().setError(null);
	}
}
