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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.SwingWorker;

import org.aavso.tools.vstar.data.InvalidObservation;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.exception.ObservationReadError;
import org.aavso.tools.vstar.input.ObservationFileAnalyser;
import org.aavso.tools.vstar.input.ObservationRetrieverBase;
import org.aavso.tools.vstar.input.SimpleTextFormatReader;
import org.aavso.tools.vstar.ui.DataPane;
import org.aavso.tools.vstar.ui.LightCurvePane;
import org.aavso.tools.vstar.ui.MessageBox;
import org.aavso.tools.vstar.ui.SimpleTextFormatObservationPane;
import org.aavso.tools.vstar.util.Notifier;
import org.jfree.chart.ChartPanel;

/**
 * This class manages models, in particular, the data models that under-pin the
 * table and chart views.
 * 
 * This is a Singleton since only one manager per application instance needs to
 * exist.
 * 
 * TODO: if we store GUI components here also, it should be DocManager
 * again...or ArtefactManager (like that one!) or...
 * 
 * TODO: also handle undo, document "is-dirty" handling, don't load same file
 * twice etc.
 * 
 */
public class ModelManager implements PropertyChangeListener {

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

	private ObservationPlotModel obsPlotModel;

	// TODO: mean, phase plot models...

	// Current GUI table and chart elements.
	private ChartPanel obsChartPane;
	private JPanel obsTablePane;

	// TODO: mean, phase plot models...

	// Notifiers.
	private Notifier<NewStarType> newStarNotifier;
	private Notifier<ModeType> modeChangeNotifier;
	private Notifier<ProgressInfo> progressNotifier;

	/**
	 * @return the newStarNotifier
	 */
	public Notifier<NewStarType> getNewStarNotifier() {
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

		private ObservationFileAnalyser analyser;
		private int plotTaskPortion;
		private Component parent;
		private int progress;

		/**
		 * Constructor.
		 * 
		 * @param analyser
		 *            An observation file analyser.
		 * @param plotTaskPortion
		 *            The portion of the total task that involves the light
		 *            curve plot.
		 * @param parent
		 *            A GUI component that can be considered a parent.
		 */
		public NewStarFromFileTask(ObservationFileAnalyser analyser,
				Component parent, int plotTaskPortion) {
			this.analyser = analyser;
			this.plotTaskPortion = plotTaskPortion;
			this.parent = parent;
			this.progress = 0;
		}

		/**
		 * Main task. Executed in background thread.
		 */
		public Void doInBackground() {
			try {
				createObservationModelsFromFileHelper(analyser);
			} catch (Exception e) {
				MessageBox.showErrorDialog(parent,
						"New Star From File Read Error", e.getMessage());

				modelMgr.newStarFileName = null;

				// TODO: set all other artefacts to null
			}
			return null;
		}

		/**
		 * Executed in event dispatching thread.
		 */
		public void done() {
			// Task ends.
			modelMgr.getProgressNotifier().notifyListeners(
					ProgressInfo.COMPLETE_PROGRESS);

			// Notify whoever is listening that a new star has been loaded.
			modelMgr.newStarNotifier.notifyListeners(analyser.getType());
		}

		/**
		 * Create observation table and plot models from a file.
		 * 
		 * @param analyser
		 *            An observation file analyser. TODO: rename method as
		 *            createObservationArtefacts(), possibly overload for DB
		 */
		private void createObservationModelsFromFileHelper(
				ObservationFileAnalyser analyser) throws IOException,
				ObservationReadError {

			// TODO: factory -> retriever instance

			// TODO: Use an abstract factory to determine observation
			// retriever class to use given the file type, along
			// with all other classes of relevance to us! The
			// concrete factory could be stored in the model manager.
			// Factory could give us NewStarType, ObservationRetrieverBase,
			// (In)ValidObservation (sub)classes etc.

			String delimiter = analyser.getDelimiter();

			ObservationRetrieverBase simpleTextFormatReader = new SimpleTextFormatReader(
					new LineNumberReader(new FileReader(analyser.getObsFile()
							.getPath())), delimiter);

			simpleTextFormatReader.retrieveObservations();

			clearData();

			modelMgr.validObsList = simpleTextFormatReader
					.getValidObservations();

			modelMgr.invalidObsList = simpleTextFormatReader
					.getInvalidObservations();

			if (!modelMgr.validObsList.isEmpty()) {
				modelMgr.validObsTableModel = new ValidObservationTableModel(
						modelMgr.validObsList);

				//setProgress(progress++);

				modelMgr.obsPlotModel = new ObservationPlotModel(analyser
						.getObsFile().getName(), modelMgr.validObsList);

				//setProgress(progress++);

				modelMgr.obsChartPane = createLightCurvePane();

				modelMgr.getProgressNotifier().notifyListeners(
						new ProgressInfo(ProgressType.INCREMENT_PROGRESS, plotTaskPortion));

				// TODO: same for means ...
			}

			if (!modelMgr.invalidObsList.isEmpty()) {
				modelMgr.invalidObsTableModel = new InvalidObservationTableModel(
						modelMgr.invalidObsList);

//				setProgress(progress++);
			}

			modelMgr.obsTablePane = createObsTablePane();
			setProgress(progress++);
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
		ObservationFileAnalyser analyser = new ObservationFileAnalyser(obsFile);
		analyser.analyse();

		// Task begins: Number of lines in file and a portion for the light
		// curve plot.
		int plotPortion = (int) (analyser.getLineCount() * 0.2);

		this.getProgressNotifier().notifyListeners(
				new ProgressInfo(ProgressType.MAX_PROGRESS, analyser
						.getLineCount()
						+ plotPortion));

		NewStarFromFileTask task = new NewStarFromFileTask(analyser, parent,
				plotPortion);
		task.addPropertyChangeListener(this);
		task.execute();
	}

	/**
	 * Invoked when task's progress property changes.
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		if ("progress" == evt.getPropertyName()) {
			// We don't care what the progress value is
			this.getProgressNotifier().notifyListeners(
					ProgressInfo.INCREMENT_PROGRESS);
		}
	}

	/**
	 * Create the observation table component.
	 */
	private JPanel createObsTablePane() {
		return new SimpleTextFormatObservationPane(
				this.getValidObsTableModel(), this.getInvalidObsTableModel());
	}

	/**
	 * Create the light curve for a list of valid observations.
	 */
	private ChartPanel createLightCurvePane() {
		ObservationPlotModel model = this.getObsPlotModel();
		Dimension bounds = new Dimension((int) (DataPane.WIDTH * 0.75),
				(int) (DataPane.HEIGHT * 0.75));
		return new LightCurvePane("Julian Day vs Magnitude", model, bounds);
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
	public ChartPanel getObsChartPane() {
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
		this.newStarNotifier = new Notifier<NewStarType>();
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
