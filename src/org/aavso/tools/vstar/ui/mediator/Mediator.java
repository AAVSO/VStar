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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JTable.PrintMode;

import org.aavso.tools.vstar.data.InvalidObservation;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.exception.CancellationException;
import org.aavso.tools.vstar.exception.ConnectionException;
import org.aavso.tools.vstar.exception.ObservationReadError;
import org.aavso.tools.vstar.input.AbstractObservationRetriever;
import org.aavso.tools.vstar.input.database.AAVSODatabaseConnector;
import org.aavso.tools.vstar.input.text.ObservationSourceAnalyser;
import org.aavso.tools.vstar.ui.DataPane;
import org.aavso.tools.vstar.ui.MainFrame;
import org.aavso.tools.vstar.ui.MeanObservationListPane;
import org.aavso.tools.vstar.ui.MenuBar;
import org.aavso.tools.vstar.ui.ObservationAndMeanPlotPane;
import org.aavso.tools.vstar.ui.ObservationListPane;
import org.aavso.tools.vstar.ui.ObservationPlotPane;
import org.aavso.tools.vstar.ui.PhaseAndMeanPlotPane;
import org.aavso.tools.vstar.ui.PhasePlotPane;
import org.aavso.tools.vstar.ui.dialog.MessageBox;
import org.aavso.tools.vstar.ui.dialog.PhaseParameterDialog;
import org.aavso.tools.vstar.ui.model.ICoordSource;
import org.aavso.tools.vstar.ui.model.InvalidObservationTableModel;
import org.aavso.tools.vstar.ui.model.JDCoordSource;
import org.aavso.tools.vstar.ui.model.MeanObservationTableModel;
import org.aavso.tools.vstar.ui.model.NewStarType;
import org.aavso.tools.vstar.ui.model.ObservationAndMeanPlotModel;
import org.aavso.tools.vstar.ui.model.ObservationPlotModel;
import org.aavso.tools.vstar.ui.model.PhaseCoordSource;
import org.aavso.tools.vstar.ui.model.ProgressInfo;
import org.aavso.tools.vstar.ui.model.ProgressType;
import org.aavso.tools.vstar.ui.model.SeriesType;
import org.aavso.tools.vstar.ui.model.ValidObservationTableModel;
import org.aavso.tools.vstar.util.notification.Notifier;
import org.aavso.tools.vstar.util.stats.PhaseCalcs;

/**
 * This class manages the creation of models and views and sends notifications
 * for changes to mode and analysis types.
 * 
 * This is a Singleton since only one mediator per application instance needs to
 * exist.
 */
public class Mediator {

	public static final String NOT_IMPLEMENTED_YET = "This feature is not implemented yet.";

	// Valid and invalid observation lists and series category map.
	private List<ValidObservation> validObsList;
	private List<InvalidObservation> invalidObsList; // TODO: need to store
	// this?
	private Map<SeriesType, List<ValidObservation>> validObservationCategoryMap;

	// Current mode.
	// TODO: why doesn't this live in ModePane?
	private ModeType mode;

	// Current analysis type.
	private AnalysisType analysisType;

	// The latest new star message created and sent to listeners.
	private NewStarMessage newStarMessage;

	// Mapping from analysis type to the latest analysis change
	// messages created and sent to listeners.
	private Map<AnalysisType, AnalysisTypeChangeMessage> analysisTypeMap;

	// Notifiers.
	private Notifier<AnalysisTypeChangeMessage> analysisTypeChangeNotifier;
	private Notifier<NewStarMessage> newStarNotifier;
	private Notifier<ModeType> modeChangeNotifier;
	private Notifier<ProgressInfo> progressNotifier;

	/**
	 * @return the analysisTypeChangeNotifier
	 */
	public Notifier<AnalysisTypeChangeMessage> getAnalysisTypeChangeNotifier() {
		return analysisTypeChangeNotifier;
	}

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
		if (mode != this.mode) {
			this.mode = mode;
			this.getModeChangeNotifier().notifyListeners(mode);
		}
	}

	/**
	 * @return the mode
	 */
	public ModeType getMode() {
		return mode;
	}

	/**
	 * Change the analysis type. If the old and new types are the same, there
	 * will be no effect.
	 * 
	 * @param analysisType
	 *            The analysis type to change to.
	 */
	public AnalysisType changeAnalysisType(AnalysisType analysisType) {
		if (this.analysisType != analysisType) {
			try {
				AnalysisTypeChangeMessage msg;

				switch (analysisType) {
				case RAW_DATA:
					// Create or retrieve raw plots and data tables.
					// There has to be observations loaded already in order
					// to be able to switch to raw data analysis mode.
					msg = this.analysisTypeMap.get(AnalysisType.RAW_DATA);

					if (msg != null) {
						this.analysisType = analysisType;
						// TODO: why are we doing this again here?
						this.analysisTypeChangeNotifier.notifyListeners(msg);
					}
					break;

				case PHASE_PLOT:
					// Create or retrieve phase plots and data tables.
					msg = this.analysisTypeMap.get(AnalysisType.PHASE_PLOT);

					if (msg == null) {
						PhaseParameterDialog phaseDialog = new PhaseParameterDialog();
						if (!phaseDialog.isCancelled()) {
							double period = phaseDialog.getPeriod();
							msg = createPhasePlotArtefacts(period);
						}
					}

					// TODO: sort out what should happen here and above...
					if (msg != null) {
						this.analysisType = analysisType;
						this.analysisTypeChangeNotifier.notifyListeners(msg);
					}
					break;

				case PERIOD_SEARCH:
					// TODO: Shouldn't get here yet!
					break;
				}
			} catch (Exception e) {
				MessageBox.showErrorDialog(MainFrame.getInstance(),
						"Analysis Type Change", e);
			}
		}

		return this.analysisType;
	}

	/**
	 * @return the analysisType
	 */
	public AnalysisType getAnalysisType() {
		return analysisType;
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

		try {
			// CitizenSky authentication.
			AAVSODatabaseConnector userConnector = AAVSODatabaseConnector.userDBConnector;
			userConnector.authenticateWithCitizenSky();

			// TODO: query for observer code; return value of
			// authenticateWithCitizenSky() above?

			this.getProgressNotifier().notifyListeners(
					ProgressInfo.RESET_PROGRESS);

			this.getProgressNotifier().notifyListeners(
					new ProgressInfo(ProgressType.MAX_PROGRESS, 10));

			NewStarFromDatabaseTask task = new NewStarFromDatabaseTask(
					starName, auid, minJD, maxJD);
			task.execute();
		} catch (CancellationException ex) {
			MainFrame.getInstance().getStatusPane().setMessage("");
		} catch (ConnectionException ex) {
			MessageBox.showErrorDialog(MainFrame.getInstance(),
					MenuBar.NEW_STAR_FROM_DATABASE,
					"Cannot connect to database.");
			MainFrame.getInstance().getStatusPane().setMessage("");
		} catch (Exception ex) {
			MessageBox.showErrorDialog(MainFrame.getInstance(),
					MenuBar.NEW_STAR_FROM_DATABASE, ex);
			MainFrame.getInstance().getStatusPane().setMessage("");
		}
	}

	/**
	 * Create observation artefacts (models, GUI elements) on the assumption
	 * that a valid observation list and category map have already been created.
	 * 
	 * @param newStarType
	 *            The new star enum type.
	 * @param objName
	 *            The name of the object for display in plot panes.
	 * @param obsRetriever
	 *            The observation source.
	 * @param obsArtefactProgressAmount
	 *            The amount the progress bar should be incremented by, a value
	 *            corresponding to a portion of the overall task of which this
	 *            is just a part.
	 */
	// TODO: split this up!
	protected void createNewStarObservationArtefacts(NewStarType newStarType,
			String objName, AbstractObservationRetriever obsRetriever,
			int obsArtefactProgressAmount) {

		// Given raw valid and invalid observation data, create observation
		// table and plot models, along with corresponding GUI components.

		List<ValidObservation> validObsList = obsRetriever
				.getValidObservations();

		List<InvalidObservation> invalidObsList = obsRetriever
				.getInvalidObservations();

		Map<SeriesType, List<ValidObservation>> validObservationCategoryMap = obsRetriever
				.getValidObservationCategoryMap();

		// Table models.
		ValidObservationTableModel validObsTableModel = null;
		InvalidObservationTableModel invalidObsTableModel = null;
		MeanObservationTableModel meanObsTableModel = null;

		// Plot models.
		ObservationPlotModel obsPlotModel = null;
		ObservationAndMeanPlotModel obsAndMeanPlotModel = null;

		// GUI table and chart components.
		ObservationListPane obsListPane = null;
		MeanObservationListPane meansListPane = null;
		ObservationPlotPane obsChartPane = null;
		ObservationAndMeanPlotPane obsAndMeanChartPane = null;

		if (!validObsList.isEmpty()) {
			// Observation and mean plot models can both share the
			// same X coordinate source (Julian Day).
			ICoordSource coordSrc = new JDCoordSource();

			// Observation table and plot.
			validObsTableModel = new ValidObservationTableModel(validObsList,
					newStarType);

			obsPlotModel = new ObservationPlotModel(
					validObservationCategoryMap, coordSrc);
			validObsTableModel.getObservationChangeNotifier().addListener(
					obsPlotModel);

			String subTitle = "";
			if (newStarType == NewStarType.NEW_STAR_FROM_DATABASE) {
				subTitle = new Date().toString() + " (database)";
			} else {
				subTitle = objName;
			}

			obsChartPane = createObservationPlotPane("Light Curve for "
					+ objName, subTitle, obsPlotModel);

			// Observation-and-mean table and plot.
			obsAndMeanPlotModel = new ObservationAndMeanPlotModel(
					validObservationCategoryMap, coordSrc);

			validObsTableModel.getObservationChangeNotifier().addListener(
					obsAndMeanPlotModel);

			obsAndMeanChartPane = createObservationAndMeanPlotPane(
					"Light Curve with Means for " + objName, subTitle,
					obsAndMeanPlotModel);

			// The mean observation table model must listen to the plot
			// model to know when the means data has changed. We also pass
			// the initial means data obtained from the plot model to
			// the mean observation table model.
			meanObsTableModel = new MeanObservationTableModel(
					obsAndMeanPlotModel.getMeanObsList());

			obsAndMeanPlotModel.getMeansChangeNotifier().addListener(
					meanObsTableModel);

			// Update progress.
			getProgressNotifier().notifyListeners(
					new ProgressInfo(ProgressType.INCREMENT_PROGRESS,
							obsArtefactProgressAmount));
		}

		if (!invalidObsList.isEmpty()) {
			invalidObsTableModel = new InvalidObservationTableModel(
					invalidObsList);
		}

		// The observation table pane contains valid and potentially
		// invalid data components. Tell the valid data table to have
		// a horizontal scrollbar if it the source was a simple-format
		// file since there won't be many columns. We don't want to do that
		// when there are many columns (i.e. for AAVSO download format files
		// and database source).
		boolean enableColumnAutoResize = newStarType == NewStarType.NEW_STAR_FROM_SIMPLE_FILE;
		obsListPane = new ObservationListPane(validObsTableModel,
				invalidObsTableModel, enableColumnAutoResize, true);

		// We also create the means list pane.
		meansListPane = new MeanObservationListPane(meanObsTableModel);

		// Notify whoever is listening that a new star has been loaded.
		newStarMessage = new NewStarMessage(newStarType, objName);

		// Notify whoever is listening that the analysis type has changed
		// (we could have been viewing a phase plot for a different star
		// before now) passing GUI components in the message.
		analysisType = AnalysisType.RAW_DATA;

		AnalysisTypeChangeMessage analysisTypeMsg = new AnalysisTypeChangeMessage(
				analysisType, obsChartPane, obsAndMeanChartPane, obsListPane,
				meansListPane, ModeType.PLOT_OBS_MODE);

		analysisTypeMap.clear(); // throw away old artefacts
		analysisTypeMap.put(analysisType, analysisTypeMsg);

		// Commit to using the new observation lists and category map.
		this.validObsList = validObsList;
		this.invalidObsList = invalidObsList;
		this.validObservationCategoryMap = validObservationCategoryMap;

		// Notify listeners of new star and analysis type change.
		getNewStarNotifier().notifyListeners(newStarMessage);
		getAnalysisTypeChangeNotifier().notifyListeners(analysisTypeMsg);
	}

	/**
	 * Create phase plot artefacts, adding them to the analysis type map and
	 * returning this message.
	 * 
	 * @param period
	 *            The requested period of the phase plot.
	 * @return An analysis type message consisting of phase plot artefacts.
	 */
	public AnalysisTypeChangeMessage createPhasePlotArtefacts(double period)
			throws Exception {

		// TODO: invoke dialog from here
		// - for dialog, start with period entry box
		// - add radio buttons for epoch calc algorithm later

		// TODO: enable busy cursor, progress bar, status pane updates...

		// Get the existing new star message for now, to reuse some components.
		// TODO: This is temporary.
		AnalysisTypeChangeMessage rawDataMsg = this.analysisTypeMap
				.get(AnalysisType.RAW_DATA);
		assert (rawDataMsg != null); // failure of this assertion implies a
		// programmatic error

		// TODO: refactor this code against what is in
		// createObservationArtefacts()

		String objName = newStarMessage.getNewStarName();

		String subTitle = "";
		if (this.newStarMessage.getNewStarType() == NewStarType.NEW_STAR_FROM_DATABASE) {
			subTitle = new Date().toString() + " (database)";
		} else {
			subTitle = objName;
		}

		// Observation and mean plot models can both share the
		// same X coordinate source (phases).
		PhaseCoordSource phaseCoordSrc = new PhaseCoordSource();

		// Table and plot models.
		// TODO: consider reusing plot models across all modes, just
		// changing the coordinate source when mode-switching (to save
		// memory)...
		ObservationPlotModel obsPlotModel = new ObservationPlotModel(
				validObservationCategoryMap, phaseCoordSrc);

		// PhaseAndMeanPlotModel phaseAndMeanPlotModel = new
		// PhaseAndMeanPlotModel(
		// validObservationCategoryMap, phaseCoordSrc);

		// Set the phases for the valid observation models in each series,
		// including the means series.

		double epoch = PhaseCalcs.getEpoch(validObsList); // TODO: dialog box
		// with radio
		// buttons
		// double period = validObsList.size() / 2; // essentially meaningless;
		// TODO: same dialog box as
		// above
		PhaseCalcs.setPhases(validObsList, epoch, period);

		// phaseCoordSrc.setMeanObs(obsAndMeanPlotModel.getMeanObsList(),
		// obsAndMeanPlotModel.getMeansSeriesNum(), epoch, period);

		// TODO: 1. to avoid the visual mean value line joining problem, we
		// will need to double the means list and set the means series number
		// in PhaseCoordSource, treating that series differently from all
		// others; either this, or we have to double all series lists and
		// that's just wasting memory we may not have!
		// TODO: 2. we are going to need an Observer or callback that gets
		// invoked
		// when we change the means series; use a notifying list -> an obvious
		// choice?
		// PhaseCalcs.setPhases(phaseAndMeanPlotModel.getMeanObsList(), epoch,
		// period);

		// TODO:
		// 1. create a separate coord src subclass and instance for mean plot
		// 2. pass means model or just means list and means series num to coord
		// src where it will be duplicated

		// TODO: add table models x 2

		// GUI table and chart components.
		PhasePlotPane obsChartPane = createPhasePlotPane("Phase Plot for "
				+ objName, subTitle, obsPlotModel);

		// PhaseAndMeanPlotPane obsAndMeanChartPane =
		// createPhaseAndMeanPlotPane(
		// "Phase Plot with Means for " + objName, subTitle,
		// phaseAndMeanPlotModel);

		// TODO: use this until we have PhaseAndMeanPlot{Pane,Model} working.
		ObservationAndMeanPlotPane obsAndMeanChartPane = rawDataMsg
				.getObsAndMeanChartPane();

		ObservationListPane obsListPane = rawDataMsg.getObsListPane(); // TODO:
		// fix to be phase-friendly
		MeanObservationListPane meansListPane = rawDataMsg.getMeansListPane(); // TODO:
		// fix to be phase-friendly

		// Observation-and-mean table and plot.
		AnalysisTypeChangeMessage phasePlotMsg = new AnalysisTypeChangeMessage(
				AnalysisType.PHASE_PLOT, obsChartPane, obsAndMeanChartPane,
				obsListPane, meansListPane, ModeType.PLOT_OBS_MODE);

		analysisTypeMap.put(AnalysisType.PHASE_PLOT, phasePlotMsg);

		this.analysisTypeChangeNotifier.notifyListeners(phasePlotMsg);

		return phasePlotMsg;
	}

	/**
	 * Create the observation pane for a plot of valid observations.
	 */
	private ObservationPlotPane createObservationPlotPane(String plotName,
			String subTitle, ObservationPlotModel obsPlotModel) {

		Dimension bounds = new Dimension((int) (DataPane.WIDTH * 0.9),
				(int) (DataPane.HEIGHT * 0.9));

		return new ObservationPlotPane(plotName, subTitle, obsPlotModel, bounds);
	}

	/**
	 * Create the observation-and-mean plot pane for the current list of valid
	 * observations.
	 */
	private ObservationAndMeanPlotPane createObservationAndMeanPlotPane(
			String plotName, String subTitle,
			ObservationAndMeanPlotModel obsAndMeanPlotModel) {

		Dimension bounds = new Dimension((int) (DataPane.WIDTH * 0.9),
				(int) (DataPane.HEIGHT * 0.9));

		return new ObservationAndMeanPlotPane(plotName, subTitle,
				obsAndMeanPlotModel, bounds);
	}

	/**
	 * Create the pane for a phase plot of valid observations.
	 */
	private PhasePlotPane createPhasePlotPane(String plotName, String subTitle,
			ObservationPlotModel obsPlotModel) {

		Dimension bounds = new Dimension((int) (DataPane.WIDTH * 0.9),
				(int) (DataPane.HEIGHT * 0.9));

		return new PhasePlotPane(plotName, subTitle, obsPlotModel, bounds);
	}

	/**
	 * Create the observation-and-mean phase plot pane for the current list of
	 * valid observations.
	 */
	private PhaseAndMeanPlotPane createPhaseAndMeanPlotPane(String plotName,
			String subTitle, ObservationAndMeanPlotModel obsAndMeanPlotModel) {

		Dimension bounds = new Dimension((int) (DataPane.WIDTH * 0.9),
				(int) (DataPane.HEIGHT * 0.9));

		return new PhaseAndMeanPlotPane(plotName, subTitle,
				obsAndMeanPlotModel, bounds);
	}

	/**
	 * Save the artefact corresponding to the current mode.
	 * 
	 * @param parent
	 *            The parent component to be used by an error dialog.
	 */
	public void saveCurrentMode(Component parent) {
		switch (mode) {
		case PLOT_OBS_MODE:
			try {
				this.analysisTypeMap.get(analysisType).getObsChartPane()
						.getChartPanel().doSaveAs();
			} catch (IOException ex) {
				MessageBox.showErrorDialog(parent, "Save Observation Plot", ex
						.getMessage());
			}
			break;
		case PLOT_OBS_AND_MEANS_MODE:
			try {
				this.analysisTypeMap.get(analysisType).getObsAndMeanChartPane()
						.getChartPanel().doSaveAs();
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
		switch (mode) {
		case PLOT_OBS_MODE:
			this.analysisTypeMap.get(analysisType).getObsChartPane()
					.getChartPanel().createChartPrintJob();
			break;
		case PLOT_OBS_AND_MEANS_MODE:
			this.analysisTypeMap.get(analysisType).getObsAndMeanChartPane()
					.getChartPanel().createChartPrintJob();
			break;
		case LIST_OBS_MODE:
			try {
				ObservationListPane obsListPane = this.analysisTypeMap.get(
						analysisType).getObsListPane();

				obsListPane.getValidDataTable().print(PrintMode.FIT_WIDTH);

				if (obsListPane.getInvalidDataTable() != null) {
					obsListPane.getInvalidDataTable()
							.print(PrintMode.FIT_WIDTH);
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

	// Singleton fields, constructor, getter.

	private static Mediator mediator = new Mediator();

	/**
	 * Private constructor.
	 */
	private Mediator() {
		this.analysisTypeChangeNotifier = new Notifier<AnalysisTypeChangeMessage>();
		this.newStarNotifier = new Notifier<NewStarMessage>();
		this.modeChangeNotifier = new Notifier<ModeType>();
		this.progressNotifier = new Notifier<ProgressInfo>();

		// These 3 are created for each new star.
		this.validObsList = null;
		this.invalidObsList = null;
		this.validObservationCategoryMap = null;

		this.analysisTypeMap = new HashMap<AnalysisType, AnalysisTypeChangeMessage>();

		this.mode = ModeType.PLOT_OBS_MODE;
		this.analysisType = AnalysisType.RAW_DATA;
		this.newStarMessage = null;
	}

	/**
	 * Return the Singleton instance.
	 */
	public static Mediator getInstance() {
		return mediator;
	}
}
