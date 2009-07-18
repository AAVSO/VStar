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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JTable.PrintMode;

import org.aavso.tools.vstar.data.InvalidObservation;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.exception.ObservationReadError;
import org.aavso.tools.vstar.input.ObservationRetrieverBase;
import org.aavso.tools.vstar.input.ObservationSourceAnalyser;
import org.aavso.tools.vstar.input.TextFormatObservationReader;
import org.aavso.tools.vstar.ui.DataPane;
import org.aavso.tools.vstar.ui.MessageBox;
import org.aavso.tools.vstar.ui.ObservationListPane;
import org.aavso.tools.vstar.ui.ObservationPlotPane;
import org.aavso.tools.vstar.util.Notifier;
import org.jdesktop.swingworker.SwingWorker;

/**
 * This class manages models, in particular, the data models that under-pin the
 * table and chart views.
 * 
 * This is a Singleton since only one manager per application instance needs to
 * exist.
 * 
 * TODO: if we store GUI components here also, it should be DocManager
 * again...or ArtefactManager or...
 * 
 * TODO: also handle undo, document "is-dirty" handling, don't load same file
 * twice etc.
 */
public class ModelManager {

	// Current mode.
	private ModeType currentMode;

	// Current star file name, assuming file source.
	private String newStarFileName;

	// Current models.
	// TODO: To handle Analysis menu items, may want a map of analysis type
	// to an object containing the models/charts/tables for that analysis
	// type.
	private List<ValidObservation> validObsList;
	private List<InvalidObservation> invalidObsList;

	private ValidObservationTableModel validObsTableModel;
	private InvalidObservationTableModel invalidObsTableModel;
	private Map<String, List<ValidObservation>> validObservationCategoryMap;

	private ObservationPlotModel obsPlotModel;

	// TODO: mean, phase plot models...

	// Current GUI table and chart components.
	private ObservationPlotPane obsChartPane;
	private ObservationListPane obsTablePane;

	// TODO: mean, phase plot GUI components...

	// Notifiers.
	private Notifier<NewStarMessage> newStarNotifier;
	private Notifier<ModeType> modeChangeNotifier;
	private Notifier<ProgressInfo> progressNotifier;

	/**
	 * @return the newStarNotifier
	 */
	public Notifier<NewStarMessage> getNewStarNotifier() {
		return newStarNotifier;
	}

	/**
	 * @return the modeChangeNotifier
	 */
	public Notifier<ModeType> getModeChangeNotifier() {
		return modeChangeNotifier;
	}

	/**
	 * @return the progressNotifier
	 */
	public Notifier<ProgressInfo> getProgressNotifier() {
		return progressNotifier;
	}

	/**
	 * Change the mode of VStar's focus (i.e what is to be presented to the
	 * user).
	 * 
	 * @param mode
	 *            The mode to change to.
	 */
	public void changeMode(ModeType mode) {
		this.currentMode = mode;
		this.getModeChangeNotifier().notifyListeners(mode);
		// TODO: Combine command with Analysis menu selection to
		// set the table/plot to view from ModelManager. For now
		// just assume Analysis->Raw Data.
	}

	/**
	 * A concurrent task in which a new star from file request task is handled.
	 * TODO: move to a different src file
	 */
	public class NewStarFromFileTask extends SwingWorker<Void, Void> {
		private ModelManager modelMgr = ModelManager.getInstance();

		private File obsFile;
		private ObservationSourceAnalyser analyser;
		private int plotTaskPortion;
		private Component parent;
		private boolean success;

		/**
		 * Constructor.
		 * 
		 * @param obsFile
		 *            The file from which to load the star observations.
		 * @param analyser
		 *            An observation file analyser.
		 * @param plotTaskPortion
		 *            The portion of the total task that involves the light
		 *            curve plot.
		 * @param parent
		 *            A GUI component that can be considered a parent.
		 */
		public NewStarFromFileTask(File obsFile,
				ObservationSourceAnalyser analyser, Component parent,
				int plotTaskPortion) {
			this.obsFile = obsFile;
			this.analyser = analyser;
			this.plotTaskPortion = plotTaskPortion;
			this.parent = parent;
			this.success = false;
		}

		/**
		 * Main task. Executed in background thread.
		 */
		public Void doInBackground() {
			this.success = createFileBasedObservationArtefacts(obsFile, analyser);
			return null;
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
				NewStarMessage msg = new NewStarMessage(analyser
						.getNewStarType(), modelMgr.obsChartPane, modelMgr.obsTablePane);
				
				modelMgr.newStarNotifier.notifyListeners(msg);
			}
		}

		/**
		 * Create observation table and plot models from a file.
		 * 
		 * @param obsFile
		 *            The file from which to load the star observations.
		 * 
		 * @param analyser
		 *            An observation file analyser.
		 */
		private boolean createFileBasedObservationArtefacts(File obsFile,
				ObservationSourceAnalyser analyser) {

			try {
				ObservationRetrieverBase textFormatReader = new TextFormatObservationReader(
						new LineNumberReader(new FileReader(obsFile.getPath())),
						analyser);

				textFormatReader.retrieveObservations();

				clearData();

				modelMgr.validObsList = textFormatReader.getValidObservations();
				modelMgr.invalidObsList = textFormatReader
						.getInvalidObservations();
				modelMgr.validObservationCategoryMap = textFormatReader
						.getValidObservationCategoryMap();
			} catch (Exception e) {
				MessageBox.showErrorDialog(parent,
						"New Star From File Read Error", e);
				modelMgr.newStarFileName = null;
				clearData();
				return false;
			}

			// Given raw valid and invalid observation data, create observation 
			// table and plot models, along with corresponding GUI components.
			
			if (!modelMgr.validObsList.isEmpty()) {
				modelMgr.validObsTableModel = new ValidObservationTableModel(
						modelMgr.validObsList, analyser.getNewStarType());

				modelMgr.obsPlotModel = new ObservationPlotModel(
						modelMgr.validObservationCategoryMap);

				modelMgr.obsChartPane = createLightCurvePane(this.obsFile
						.getName());

				// TODO: same for means ...

				modelMgr.getProgressNotifier().notifyListeners(
						new ProgressInfo(ProgressType.INCREMENT_PROGRESS,
								plotTaskPortion));
			}

			if (!modelMgr.invalidObsList.isEmpty()) {
				modelMgr.invalidObsTableModel = new InvalidObservationTableModel(
						modelMgr.invalidObsList);
			}

			modelMgr.obsTablePane = createObsTablePane();

			return true;
		}
	}

	/**
	 * Creates and executes a background task to handle new-star-from-file.
	 * 
	 * @param obsFile
	 *            The file from which to load the star observations.
	 * @param parent
	 *            The GUI component that can be used to display.
	 */
	public void createObservationModelsFromFile(File obsFile, Component parent)
			throws FileNotFoundException, IOException, ObservationReadError {

		modelMgr.newStarFileName = obsFile.getPath();

		this.getProgressNotifier().notifyListeners(ProgressInfo.RESET_PROGRESS);

		// Analyse the observation file.
		// TODO: include this step under progressing task below?
		ObservationSourceAnalyser analyser = new ObservationSourceAnalyser(
				new LineNumberReader(new FileReader(obsFile)), obsFile
						.getName());
		analyser.analyse();

		// Task begins: Number of lines in file and a portion for the light
		// curve plot.
		int plotPortion = (int) (analyser.getLineCount() * 0.2);

		this.getProgressNotifier().notifyListeners(
				new ProgressInfo(ProgressType.MAX_PROGRESS, analyser
						.getLineCount()
						+ plotPortion));

		NewStarFromFileTask task = new NewStarFromFileTask(obsFile, analyser,
				parent, plotPortion);
		task.execute();
	}

	/**
	 * Create the observation table component.
	 */
	private ObservationListPane createObsTablePane() {
		return new ObservationListPane(this.getValidObsTableModel(), this
				.getInvalidObsTableModel());
	}

	/**
	 * Create the light curve for a list of valid observations.
	 */
	private ObservationPlotPane createLightCurvePane(String plotName) {
		ObservationPlotModel model = this.getObsPlotModel();
		Dimension bounds = new Dimension((int) (DataPane.WIDTH * 0.9),
				(int) (DataPane.HEIGHT * 0.9));
		return new ObservationPlotPane("Julian Day vs Magnitude Plot for "
				+ plotName, model, bounds);
	}

	/**
	 * Save the artefact corresponding to the current mode.
	 * 
	 * @param parent
	 *            The parent component to be used by an error dialog.
	 */
	public void saveCurrentMode(Component parent) {
		switch (currentMode) {
		case PLOT_OBS_MODE:
			try {
				this.obsChartPane.getChartPanel().doSaveAs();
			} catch (IOException ex) {
				MessageBox.showErrorDialog(parent, "Save Light Curve", ex
						.getMessage());
			}
			break;
		case PLOT_OBS_AND_MEANS_MODE:
			break;
		case LIST_OBS_MODE:
			MessageBox.showMessageDialog(parent, "Save Observations",
					"This feature is not implemented yet.");
			break;
		case LIST_MEANS_MODE:
			break;
		}
	}

	/**
	 * Print the artefact corresponding to the current mode.
	 * 
	 * @param parent
	 *            The parent component to be used by an error dialog.
	 */
	public void printCurrentMode(Component parent) {
		switch (currentMode) {
		case PLOT_OBS_MODE:
			this.obsChartPane.getChartPanel().createChartPrintJob();
			break;
		case PLOT_OBS_AND_MEANS_MODE:
			break;
		case LIST_OBS_MODE:
			try {
				this.obsTablePane.getValidDataTable()
						.print(PrintMode.FIT_WIDTH);

				if (this.obsTablePane.getInvalidDataTable() != null) {
					this.obsTablePane.getInvalidDataTable().print(
							PrintMode.FIT_WIDTH);
				}
			} catch (PrinterException e) {
				MessageBox.showErrorDialog(parent, "Print Observations", e
						.getMessage());
			}
			break;
		case LIST_MEANS_MODE:
			break;
		}
	}

	/**
	 * Clear all data (lists, models).
	 */
	private void clearData() {
		this.invalidObsList = null;
		this.invalidObsTableModel = null;
		this.validObsList = null;
		this.validObsTableModel = null;
		this.obsPlotModel = null;
		// TODO: GUI components too?
	}

	/**
	 * @return the validObsList
	 */
	public List<ValidObservation> getValidObsList() {
		return validObsList;
	}

	/**
	 * @return the invalidObsList
	 */
	public List<InvalidObservation> getInvalidObsList() {
		return invalidObsList;
	}

	/**
	 * @return the validObsTableModel
	 */
	public ValidObservationTableModel getValidObsTableModel() {
		return validObsTableModel;
	}

	/**
	 * @return the invalidObsTableModel
	 */
	public InvalidObservationTableModel getInvalidObsTableModel() {
		return invalidObsTableModel;
	}

	/**
	 * @return the obsPlotModel
	 */
	public ObservationPlotModel getObsPlotModel() {
		return obsPlotModel;
	}

	/**
	 * @return the newStarFileName
	 */
	public String getNewStarFileName() {
		return newStarFileName;
	}

	/**
	 * @return the obsChartPane
	 */
	public ObservationPlotPane getObsChartPane() {
		return obsChartPane;
	}

	/**
	 * @return the obsTablePane
	 */
	public JPanel getObsTablePane() {
		return obsTablePane;
	}

	// ** Singleton member, constructor, and getter. **

	private static ModelManager modelMgr = new ModelManager();

	/**
	 * Private constructor.
	 */
	private ModelManager() {
		this.newStarNotifier = new Notifier<NewStarMessage>();
		this.modeChangeNotifier = new Notifier<ModeType>();
		this.progressNotifier = new Notifier<ProgressInfo>();

		this.currentMode = ModeType.PLOT_OBS_MODE;
		this.newStarFileName = null;
	}

	/**
	 * Return the Singleton instance.
	 */
	public static ModelManager getInstance() {
		return modelMgr;
	}
}
