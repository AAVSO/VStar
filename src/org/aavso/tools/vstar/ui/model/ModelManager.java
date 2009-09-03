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

import javax.swing.JTable.PrintMode;

import org.aavso.tools.vstar.data.InvalidObservation;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.exception.ObservationReadError;
import org.aavso.tools.vstar.input.text.ObservationSourceAnalyser;
import org.aavso.tools.vstar.ui.DataPane;
import org.aavso.tools.vstar.ui.MeanObservationListPane;
import org.aavso.tools.vstar.ui.ObservationAndMeanPlotPane;
import org.aavso.tools.vstar.ui.ObservationListPane;
import org.aavso.tools.vstar.ui.ObservationPlotPane;
import org.aavso.tools.vstar.ui.dialog.MessageBox;
import org.aavso.tools.vstar.util.Notifier;

/**
 * This class manages models, in particular, the data models that under-pin the
 * table and chart views.
 * 
 * This is a Singleton since only one manager per application instance needs to
 * exist.
 * 
 * TODO: since we store GUI components here also, it should be DocManager
 * again...or ArtefactManager or similar.
 * 
 * TODO: also handle undo, document "needs saving", don't load same file twice
 * etc. Only need to detect whether table models need saving, in particular the
 * valid observations portion of the table model, and probably *only* that. All
 * other artefacts (means, plots) are derived from that.
 * 
 * TODO: where we currently refer to modelMgr artefacts, we should use local
 * variables instead where possible and get rid of those members.
 */
public class ModelManager {

	public static final String NOT_IMPLEMENTED_YET = "This feature is not implemented yet.";

	// Current mode.
	private ModeType currentMode;

	// Current star name or file name (assuming file source).
	private String newStarName;

	// Current models.
	// TODO: To handle Analysis menu items, may want a map of analysis type
	// to an object containing the models/charts/tables for that analysis
	// type.
	private List<ValidObservation> validObsList;
	private List<InvalidObservation> invalidObsList;
	private Map<String, List<ValidObservation>> validObservationCategoryMap;

	private ValidObservationTableModel validObsTableModel;
	private InvalidObservationTableModel invalidObsTableModel;
	private MeanObservationTableModel meanObsTableModel;

	private ObservationPlotModel obsPlotModel;
	private ObservationAndMeanPlotModel obsAndMeanPlotModel;

	// Current GUI table and chart components.
	private ObservationPlotPane obsChartPane;
	private ObservationAndMeanPlotPane obsAndMeanChartPane;
	private ObservationListPane obsListPane;
	private MeanObservationListPane meansListPane;

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
	 * Creates and executes a background task to handle new-star-from-file.
	 * 
	 * @param obsFile
	 *            The file from which to load the star observations.
	 * @param parent
	 *            The GUI component that can be used to display.
	 */
	public void createObservationArtefactsFromFile(File obsFile,
			Component parent) throws FileNotFoundException, IOException,
			ObservationReadError {

		modelMgr.setNewStarName(obsFile.getPath());

		this.getProgressNotifier().notifyListeners(ProgressInfo.RESET_PROGRESS);

		// Analyse the observation file.
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
				plotPortion);
		task.execute();
	}

	/**
	 * Creates and executes a background task to handle new-star-from-database.
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
	public void createObservationArtefactsFromDatabase(String starName,
			String auid, double minJD, double maxJD) {

		modelMgr.setNewStarName(starName);

		this.getProgressNotifier().notifyListeners(ProgressInfo.RESET_PROGRESS);

		this.getProgressNotifier().notifyListeners(
				new ProgressInfo(ProgressType.MAX_PROGRESS, 10));

		NewStarFromDatabaseTask task = new NewStarFromDatabaseTask(starName,
				auid, minJD, maxJD);
		task.execute();
	}

	/**
	 * Create observation artefacts (models, GUI elements) on the assumption
	 * that a valid observation list and category map have already been created.
	 * 
	 * @param newStarType
	 *            The new star enum type.
	 * @param objName
	 *            The name of the object for display in plot panes.
	 * @param obsArtefactProgressAmount
	 *            The amount the progress bar should be incremented by, a value
	 *            corresponding to a portion of the overall task of which this
	 *            is just a part.
	 */
	protected void createObservationArtefacts(NewStarType newStarType,
			String objName, int obsArtefactProgressAmount) {
		// Given raw valid and invalid observation data, create observation
		// table and plot models, along with corresponding GUI components.

		if (!modelMgr.validObsList.isEmpty()) {
			// Observation table and plot.
			modelMgr.validObsTableModel = new ValidObservationTableModel(
					modelMgr.validObsList, newStarType);

			modelMgr.obsPlotModel = new ObservationPlotModel(
					modelMgr.validObservationCategoryMap);
			modelMgr.validObsTableModel.getObservationChangeNotifier()
					.addListener(modelMgr.obsPlotModel);

			modelMgr.obsChartPane = createObservationPlotPane(objName);

			// Observation-and-mean table and plot.

			modelMgr.obsAndMeanPlotModel = createObservationAndMeanPlotModel();
			modelMgr.validObsTableModel.getObservationChangeNotifier()
					.addListener(modelMgr.obsAndMeanPlotModel);

			modelMgr.obsAndMeanChartPane = createObservationAndMeanPlotPane(objName);

			// The mean observation table model must listen to the plot
			// model to know when the means data has changed. We also pass
			// the initial means data obtained from the plot model to
			// the mean observation table model.
			modelMgr.meanObsTableModel = new MeanObservationTableModel(
					modelMgr.obsAndMeanPlotModel.getMeanObsList());

			modelMgr.obsAndMeanPlotModel.getMeansChangeNotifier().addListener(
					modelMgr.meanObsTableModel);

			// Update progress.
			modelMgr.getProgressNotifier().notifyListeners(
					new ProgressInfo(ProgressType.INCREMENT_PROGRESS,
							obsArtefactProgressAmount));
		}

		if (!modelMgr.invalidObsList.isEmpty()) {
			modelMgr.invalidObsTableModel = new InvalidObservationTableModel(
					modelMgr.invalidObsList);
		}

		// The observation table pane contains valid and potentially
		// invalid data components. Tell the valid data table to have
		// a horizontal scrollbar if it the source was a simple-format
		// file since there won't be many columns. We don't want to do that
		// when there are many columns (i.e. for AAVSO download format files
		// and database source).
		// TODO: Why don't we delegate this to the component that renders the obs list?
		// The model manager shouldn't care about this kind of stuff.
		boolean enableColumnAutoResize = newStarType == NewStarType.NEW_STAR_FROM_SIMPLE_FILE;
		modelMgr.obsListPane = createObsListPane(enableColumnAutoResize);

		// We also create the means list pane.
		modelMgr.meansListPane = createMeanObsListPane();
	}

	/**
	 * Create the observation list component.
	 */
	private ObservationListPane createObsListPane(boolean enableColumnAutoResize) {
		return new ObservationListPane(this.getValidObsTableModel(), this
				.getInvalidObsTableModel(), enableColumnAutoResize, true);
	}

	/**
	 * Create the means list pane.
	 */
	private MeanObservationListPane createMeanObsListPane() {
		return new MeanObservationListPane(this.getMeanObsTableModel());
	}

	/**
	 * Create the observation pane for a plot of valid observations.
	 */
	private ObservationPlotPane createObservationPlotPane(String plotName) {
		Dimension bounds = new Dimension((int) (DataPane.WIDTH * 0.9),
				(int) (DataPane.HEIGHT * 0.9));
		return new ObservationPlotPane("Light Curve for " + plotName,
				this.obsPlotModel, bounds);
	}

	/**
	 * Create a plot model containing a means series based upon
	 * a default bin size that the user can change later.
	 * 
	 * @return The plot model.
	 */
	private ObservationAndMeanPlotModel createObservationAndMeanPlotModel() {
		return new ObservationAndMeanPlotModel(
				modelMgr.validObservationCategoryMap);
	}

	/**
	 * Create the observation-and-mean plot pane for the current list of valid
	 * observations.
	 */
	private ObservationAndMeanPlotPane createObservationAndMeanPlotPane(
			String plotName) {
		Dimension bounds = new Dimension((int) (DataPane.WIDTH * 0.9),
				(int) (DataPane.HEIGHT * 0.9));
		return new ObservationAndMeanPlotPane("Light Curve for " + plotName,
				this.obsAndMeanPlotModel, bounds);
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
				MessageBox.showErrorDialog(parent, "Save Observation Plot", ex
						.getMessage());
			}
			break;
		case PLOT_OBS_AND_MEANS_MODE:
			try {
				this.obsChartPane.getChartPanel().doSaveAs();
			} catch (IOException ex) {
				MessageBox.showErrorDialog(parent,
						"Save Observation and Means Plot", ex.getMessage());
			}
			break;
		case LIST_OBS_MODE:
			MessageBox.showMessageDialog(parent, "Save Observations",
					NOT_IMPLEMENTED_YET);
			break;
		case LIST_MEANS_MODE:
			MessageBox.showMessageDialog(parent, "Save Means",
					NOT_IMPLEMENTED_YET);
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
			this.obsAndMeanChartPane.getChartPanel().createChartPrintJob();
			break;
		case LIST_OBS_MODE:
			try {
				this.obsListPane.getValidDataTable().print(PrintMode.FIT_WIDTH);

				if (this.obsListPane.getInvalidDataTable() != null) {
					this.obsListPane.getInvalidDataTable().print(
							PrintMode.FIT_WIDTH);
				}
			} catch (PrinterException e) {
				MessageBox.showErrorDialog(parent, "Print Observations", e
						.getMessage());
			}
			break;
		case LIST_MEANS_MODE:
			MessageBox.showMessageDialog(parent, "Print Means",
					NOT_IMPLEMENTED_YET);
			break;
		}
	}

	/**
	 * Clear all data (lists, models). TODO: or only have fundamental data
	 * collections with everything derived and passed via messages to listeners?
	 */
	protected void clearData() {
		this.invalidObsList = null;
		this.invalidObsTableModel = null;
		this.validObsList = null;
		this.validObsTableModel = null;
		this.obsPlotModel = null;
		this.obsAndMeanPlotModel = null;
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
	 * @return the meanObsTableModel
	 */
	public MeanObservationTableModel getMeanObsTableModel() {
		return meanObsTableModel;
	}

	/**
	 * @return the obsPlotModel
	 */
	public ObservationPlotModel getObsPlotModel() {
		return obsPlotModel;
	}

	/**
	 * @return the newStarName
	 */
	public String getNewStarName() {
		return newStarName;
	}

	/**
	 * @return the obsChartPane
	 */
	public ObservationPlotPane getObsChartPane() {
		return obsChartPane;
	}

	/**
	 * @return the obsListPane
	 */
	public ObservationListPane getObsListPane() {
		return obsListPane;
	}

	// ** Singleton member, constructor, and getter. **

	/**
	 * @param currentMode
	 *            the currentMode to set
	 */
	public void setCurrentMode(ModeType currentMode) {
		this.currentMode = currentMode;
	}

	/**
	 * @param validObsList
	 *            the validObsList to set
	 */
	public void setValidObsList(List<ValidObservation> validObsList) {
		this.validObsList = validObsList;
	}

	/**
	 * @param invalidObsList
	 *            the invalidObsList to set
	 */
	public void setInvalidObsList(List<InvalidObservation> invalidObsList) {
		this.invalidObsList = invalidObsList;
	}

	/**
	 * @param validObsTableModel
	 *            the validObsTableModel to set
	 */
	public void setValidObsTableModel(
			ValidObservationTableModel validObsTableModel) {
		this.validObsTableModel = validObsTableModel;
	}

	/**
	 * @param invalidObsTableModel
	 *            the invalidObsTableModel to set
	 */
	public void setInvalidObsTableModel(
			InvalidObservationTableModel invalidObsTableModel) {
		this.invalidObsTableModel = invalidObsTableModel;
	}

	/**
	 * @param meanObsTableModel
	 *            the meanObsTableModel to set
	 */
	public void setMeanObsTableModel(MeanObservationTableModel meanObsTableModel) {
		this.meanObsTableModel = meanObsTableModel;
	}

	/**
	 * @param obsPlotModel
	 *            the obsPlotModel to set
	 */
	public void setObsPlotModel(ObservationPlotModel obsPlotModel) {
		this.obsPlotModel = obsPlotModel;
	}

	/**
	 * @param obsAndMeanPlotModel
	 *            the obsAndMeanPlotModel to set
	 */
	public void setObsAndMeanPlotModel(
			ObservationAndMeanPlotModel obsAndMeanPlotModel) {
		this.obsAndMeanPlotModel = obsAndMeanPlotModel;
	}

	/**
	 * @param obsChartPane
	 *            the obsChartPane to set
	 */
	public void setObsChartPane(ObservationPlotPane obsChartPane) {
		this.obsChartPane = obsChartPane;
	}

	/**
	 * @param obsAndMeanChartPane
	 *            the obsAndMeanChartPane to set
	 */
	public void setObsAndMeanChartPane(
			ObservationAndMeanPlotPane obsAndMeanChartPane) {
		this.obsAndMeanChartPane = obsAndMeanChartPane;
	}

	/**
	 * @param obsListPane
	 *            the obsListPane to set
	 */
	public void setObsListPane(ObservationListPane obsListPane) {
		this.obsListPane = obsListPane;
	}

	/**
	 * @param meansListPane
	 *            the meansListPane to set
	 */
	public void setMeansListPane(MeanObservationListPane meansListPane) {
		this.meansListPane = meansListPane;
	}

	/**
	 * @param modelMgr
	 *            the modelMgr to set
	 */
	public static void setModelMgr(ModelManager modelMgr) {
		ModelManager.modelMgr = modelMgr;
	}

	/**
	 * @return the obsAndMeanPlotModel
	 */
	public ObservationAndMeanPlotModel getObsAndMeanPlotModel() {
		return obsAndMeanPlotModel;
	}

	/**
	 * @return the obsAndMeanChartPane
	 */
	public ObservationAndMeanPlotPane getObsAndMeanChartPane() {
		return obsAndMeanChartPane;
	}

	/**
	 * @return the meansListPane
	 */
	public MeanObservationListPane getMeansListPane() {
		return meansListPane;
	}

	/**
	 * @return the validObservationCategoryMap
	 */
	public Map<String, List<ValidObservation>> getValidObservationCategoryMap() {
		return validObservationCategoryMap;
	}

	/**
	 * @param validObservationCategoryMap
	 *            the validObservationCategoryMap to set
	 */
	public void setValidObservationCategoryMap(
			Map<String, List<ValidObservation>> validObservationCategoryMap) {
		this.validObservationCategoryMap = validObservationCategoryMap;
	}

	// Singleton fields, constructor, getter.

	/**
	 * @param newStarName
	 *            the newStarName to set
	 */
	public void setNewStarName(String newStarName) {
		this.newStarName = newStarName;
	}

	private static ModelManager modelMgr = new ModelManager();

	/**
	 * Private constructor.
	 */
	private ModelManager() {
		this.newStarNotifier = new Notifier<NewStarMessage>();
		this.modeChangeNotifier = new Notifier<ModeType>();
		this.progressNotifier = new Notifier<ProgressInfo>();

		this.currentMode = ModeType.PLOT_OBS_MODE;
		this.newStarName = null;
	}

	/**
	 * Return the Singleton instance.
	 */
	public static ModelManager getInstance() {
		return modelMgr;
	}
}
