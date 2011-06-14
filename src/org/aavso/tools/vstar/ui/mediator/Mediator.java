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
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;

import javax.swing.JFileChooser;
import javax.swing.SwingWorker;
import javax.swing.JTable.PrintMode;

import org.aavso.tools.vstar.data.InvalidObservation;
import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.exception.ObservationReadError;
import org.aavso.tools.vstar.input.AbstractObservationRetriever;
import org.aavso.tools.vstar.input.text.ObservationSourceAnalyser;
import org.aavso.tools.vstar.plugin.CustomFilterPluginBase;
import org.aavso.tools.vstar.plugin.ObservationSourcePluginBase;
import org.aavso.tools.vstar.plugin.ObservationToolPluginBase;
import org.aavso.tools.vstar.plugin.period.PeriodAnalysisPluginBase;
import org.aavso.tools.vstar.ui.MainFrame;
import org.aavso.tools.vstar.ui.MenuBar;
import org.aavso.tools.vstar.ui.TabbedDataPane;
import org.aavso.tools.vstar.ui.dialog.AbstractPlotControlDialog;
import org.aavso.tools.vstar.ui.dialog.MessageBox;
import org.aavso.tools.vstar.ui.dialog.ObservationDetailsDialog;
import org.aavso.tools.vstar.ui.dialog.PhaseParameterDialog;
import org.aavso.tools.vstar.ui.dialog.PhasePlotControlDialog;
import org.aavso.tools.vstar.ui.dialog.PolynomialDegreeDialog;
import org.aavso.tools.vstar.ui.dialog.RawPlotControlDialog;
import org.aavso.tools.vstar.ui.dialog.filter.ObservationFilterDialog;
import org.aavso.tools.vstar.ui.mediator.message.AnalysisTypeChangeMessage;
import org.aavso.tools.vstar.ui.mediator.message.DiscrepantObservationMessage;
import org.aavso.tools.vstar.ui.mediator.message.ExcludedObservationMessage;
import org.aavso.tools.vstar.ui.mediator.message.FilteredObservationMessage;
import org.aavso.tools.vstar.ui.mediator.message.MeanSourceSeriesChangeMessage;
import org.aavso.tools.vstar.ui.mediator.message.ModelSelectionMessage;
import org.aavso.tools.vstar.ui.mediator.message.NewStarMessage;
import org.aavso.tools.vstar.ui.mediator.message.ObservationSelectionMessage;
import org.aavso.tools.vstar.ui.mediator.message.PanRequestMessage;
import org.aavso.tools.vstar.ui.mediator.message.PeriodAnalysisRefinementMessage;
import org.aavso.tools.vstar.ui.mediator.message.PeriodAnalysisSelectionMessage;
import org.aavso.tools.vstar.ui.mediator.message.PeriodChangeMessage;
import org.aavso.tools.vstar.ui.mediator.message.ProgressInfo;
import org.aavso.tools.vstar.ui.mediator.message.ProgressType;
import org.aavso.tools.vstar.ui.mediator.message.StopRequestMessage;
import org.aavso.tools.vstar.ui.mediator.message.UndoActionMessage;
import org.aavso.tools.vstar.ui.mediator.message.ZoomRequestMessage;
import org.aavso.tools.vstar.ui.model.list.InvalidObservationTableModel;
import org.aavso.tools.vstar.ui.model.list.PhasePlotMeanObservationTableModel;
import org.aavso.tools.vstar.ui.model.list.RawDataMeanObservationTableModel;
import org.aavso.tools.vstar.ui.model.list.ValidObservationTableModel;
import org.aavso.tools.vstar.ui.model.plot.JDCoordSource;
import org.aavso.tools.vstar.ui.model.plot.JDTimeElementEntity;
import org.aavso.tools.vstar.ui.model.plot.ObservationAndMeanPlotModel;
import org.aavso.tools.vstar.ui.model.plot.PhaseCoordSource;
import org.aavso.tools.vstar.ui.model.plot.PhaseTimeElementEntity;
import org.aavso.tools.vstar.ui.pane.list.MeanObservationListPane;
import org.aavso.tools.vstar.ui.pane.list.ObservationListPane;
import org.aavso.tools.vstar.ui.pane.plot.ObservationAndMeanPlotPane;
import org.aavso.tools.vstar.ui.pane.plot.PhaseAndMeanPlotPane;
import org.aavso.tools.vstar.ui.pane.plot.TimeElementsInBinSettingPane;
import org.aavso.tools.vstar.ui.undo.UndoableActionManager;
import org.aavso.tools.vstar.util.comparator.JDComparator;
import org.aavso.tools.vstar.util.comparator.StandardPhaseComparator;
import org.aavso.tools.vstar.util.notification.Listener;
import org.aavso.tools.vstar.util.notification.Notifier;
import org.aavso.tools.vstar.util.polyfit.IPolynomialFitter;
import org.aavso.tools.vstar.util.polyfit.TSPolynomialFitter;
import org.aavso.tools.vstar.util.prefs.NumericPrecisionPrefs;
import org.aavso.tools.vstar.util.stats.BinningResult;
import org.aavso.tools.vstar.util.stats.PhaseCalcs;

/**
 * This class manages the creation of models and views and sends notifications
 * for changes to mode and analysis types.
 * 
 * This is a Singleton since only one mediator per application instance should
 * exist.
 * 
 * TODO: This is really 2 classes: a task manager and a message broker...
 */
public class Mediator {

	public static final String NOT_IMPLEMENTED_YET = "This feature is not implemented yet.";

	// Valid and invalid observation lists and series category map.
	private List<ValidObservation> validObsList;
	private List<InvalidObservation> invalidObsList;
	private Map<SeriesType, List<ValidObservation>> validObservationCategoryMap;
	private Map<SeriesType, List<ValidObservation>> phasedValidObservationCategoryMap;

	// Current observation and mean plot model.
	// Period search needs access to this to determine
	// the current mean source band.
	private ObservationAndMeanPlotModel obsAndMeanPlotModel;

	// Current view viewMode.
	private ViewModeType viewMode;

	// Current analysis type.
	private AnalysisType analysisType;

	// The latest new star message created and sent to listeners.
	private NewStarMessage newStarMessage;

	// Mapping from analysis type to the latest analysis change
	// messages created and sent to listeners.
	private Map<AnalysisType, AnalysisTypeChangeMessage> analysisTypeMap;

	// A file dialog for saving any kind of observation list.
	private JFileChooser obsListFileSaveDialog;

	// Persistent phase parameter dialog.
	private PhaseParameterDialog phaseParameterDialog;

	// Persistent observation filter dialog.
	private ObservationFilterDialog obsFilterDialog;

	// Plot control dialogs.
	private AbstractPlotControlDialog rawPlotControlDialog;
	private AbstractPlotControlDialog phasePlotControlDialog;

	// Notifiers.
	private Notifier<AnalysisTypeChangeMessage> analysisTypeChangeNotifier;
	private Notifier<NewStarMessage> newStarNotifier;
	private Notifier<ProgressInfo> progressNotifier;
	// TODO: This next notifier could be used to mark the "document"
	// (the current star's dataset) associated with the valid obs
	// as being in need of saving (optional for now).
	private Notifier<DiscrepantObservationMessage> discrepantObservationNotifier;
	private Notifier<ExcludedObservationMessage> excludedObservationNotifier;
	private Notifier<ObservationSelectionMessage> observationSelectionNotifier;
	private Notifier<PeriodAnalysisSelectionMessage> periodAnalysisSelectionNotifier;
	private Notifier<PeriodChangeMessage> periodChangeMessageNotifier;
	private Notifier<PeriodAnalysisRefinementMessage> periodAnalysisRefinementNotifier;
	private Notifier<MeanSourceSeriesChangeMessage> meanSourceSeriesChangeNotifier;
	private Notifier<ZoomRequestMessage> zoomRequestNotifier;
	private Notifier<FilteredObservationMessage> filteredObservationNotifier;
	private Notifier<ModelSelectionMessage> modelSelectionNofitier;
	private Notifier<PanRequestMessage> panRequestNotifier;
	private Notifier<UndoActionMessage> undoActionNotifier;
	private Notifier<StopRequestMessage> stopRequestNotifier;

	private UndoableActionManager undoableActionManager;

	// Currently active task.
	private SwingWorker currTask;

	// Singleton fields, constructor, getter.

	private static Mediator mediator = new Mediator();

	/**
	 * Private constructor.
	 */
	private Mediator() {
		this.analysisTypeChangeNotifier = new Notifier<AnalysisTypeChangeMessage>();
		this.newStarNotifier = new Notifier<NewStarMessage>();
		this.progressNotifier = new Notifier<ProgressInfo>();
		this.discrepantObservationNotifier = new Notifier<DiscrepantObservationMessage>();
		this.excludedObservationNotifier = new Notifier<ExcludedObservationMessage>();
		this.observationSelectionNotifier = new Notifier<ObservationSelectionMessage>();
		this.periodAnalysisSelectionNotifier = new Notifier<PeriodAnalysisSelectionMessage>();
		this.periodChangeMessageNotifier = new Notifier<PeriodChangeMessage>();
		this.periodAnalysisRefinementNotifier = new Notifier<PeriodAnalysisRefinementMessage>();
		this.meanSourceSeriesChangeNotifier = new Notifier<MeanSourceSeriesChangeMessage>();
		this.zoomRequestNotifier = new Notifier<ZoomRequestMessage>();
		this.filteredObservationNotifier = new Notifier<FilteredObservationMessage>();
		this.modelSelectionNofitier = new Notifier<ModelSelectionMessage>();
		this.panRequestNotifier = new Notifier<PanRequestMessage>();
		this.undoActionNotifier = new Notifier<UndoActionMessage>();
		this.stopRequestNotifier = new Notifier<StopRequestMessage>();

		this.obsListFileSaveDialog = new JFileChooser();

		// These (among other things) are created for each new star.
		this.validObsList = null;
		this.invalidObsList = null;
		this.validObservationCategoryMap = null;
		this.phasedValidObservationCategoryMap = null;
		this.obsAndMeanPlotModel = null;

		this.analysisTypeMap = new HashMap<AnalysisType, AnalysisTypeChangeMessage>();

		this.viewMode = ViewModeType.PLOT_OBS_MODE;
		this.analysisType = AnalysisType.RAW_DATA;
		this.newStarMessage = null;

		this.phaseParameterDialog = new PhaseParameterDialog();
		this.newStarNotifier.addListener(this.phaseParameterDialog);

		this.obsFilterDialog = new ObservationFilterDialog();
		this.newStarNotifier.addListener(this.obsFilterDialog
				.createNewStarListener());
		this.observationSelectionNotifier.addListener(this.obsFilterDialog
				.createObservationSelectionListener());

		this.rawPlotControlDialog = null;
		this.phasePlotControlDialog = null;

		this.periodChangeMessageNotifier
				.addListener(createPeriodChangeListener());

		this.modelSelectionNofitier.addListener(createModelSelectionListener());
		this.filteredObservationNotifier
				.addListener(createFilteredObservationListener());

		// Undoable action manager creation and listener setup.
		this.undoableActionManager = new UndoableActionManager();
		this.newStarNotifier.addListener(this.undoableActionManager
				.createNewStarListener());
		this.observationSelectionNotifier
				.addListener(this.undoableActionManager
						.createObservationSelectionListener());
	}

	/**
	 * Return the Singleton instance.
	 */
	public static Mediator getInstance() {
		return mediator;
	}

	/**
	 * @return the newStarMessage
	 */
	public NewStarMessage getNewStarMessage() {
		return newStarMessage;
	}

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
	 * @return the progressNotifier
	 */
	public Notifier<ProgressInfo> getProgressNotifier() {
		return progressNotifier;
	}

	/**
	 * @return the discrepantObservationNotifier
	 */
	public Notifier<DiscrepantObservationMessage> getDiscrepantObservationNotifier() {
		return discrepantObservationNotifier;
	}

	/**
	 * @return the excludedObservationNotifier
	 */
	public Notifier<ExcludedObservationMessage> getExcludedObservationNotifier() {
		return excludedObservationNotifier;
	}

	/**
	 * @return the observationSelectionNotifier
	 */
	public Notifier<ObservationSelectionMessage> getObservationSelectionNotifier() {
		return observationSelectionNotifier;
	}

	/**
	 * @return the periodAnalysisSelectionNotifier
	 */
	public Notifier<PeriodAnalysisSelectionMessage> getPeriodAnalysisSelectionNotifier() {
		return periodAnalysisSelectionNotifier;
	}

	/**
	 * @return the periodChangeMessageNotifier
	 */
	public Notifier<PeriodChangeMessage> getPeriodChangeMessageNotifier() {
		return periodChangeMessageNotifier;
	}

	/**
	 * @return the periodAnalysisRefinementNotifier
	 */
	public Notifier<PeriodAnalysisRefinementMessage> getPeriodAnalysisRefinementNotifier() {
		return periodAnalysisRefinementNotifier;
	}

	/**
	 * @return the meanSourceSeriesChangeNotifier
	 */
	public Notifier<MeanSourceSeriesChangeMessage> getMeanSourceSeriesChangeNotifier() {
		return meanSourceSeriesChangeNotifier;
	}

	/**
	 * @return the zoomRequestNotifier
	 */
	public Notifier<ZoomRequestMessage> getZoomRequestNotifier() {
		return zoomRequestNotifier;
	}

	/**
	 * @return the filteredObservationNotifier
	 */
	public Notifier<FilteredObservationMessage> getFilteredObservationNotifier() {
		return filteredObservationNotifier;
	}

	/**
	 * @return the modelSelectionNofitier
	 */
	public Notifier<ModelSelectionMessage> getModelSelectionNofitier() {
		return modelSelectionNofitier;
	}

	/**
	 * @return the panRequestNotifier
	 */
	public Notifier<PanRequestMessage> getPanRequestNotifier() {
		return panRequestNotifier;
	}

	/**
	 * @return the undoActionNotifier
	 */
	public Notifier<UndoActionMessage> getUndoActionNotifier() {
		return undoActionNotifier;
	}

	/**
	 * @return the undoableActionManager
	 */
	public UndoableActionManager getUndoableActionManager() {
		return undoableActionManager;
	}

	/**
	 * @return the stopRequestNotifier
	 */
	public Notifier<StopRequestMessage> getStopRequestNotifier() {
		return stopRequestNotifier;
	}

	/**
	 * Create a mean observation change listener and return it. Whenever the
	 * mean series source changes, we may want to perform a new period analysis
	 * or change the max time increments in for means binning.
	 */
	private Listener<BinningResult> createMeanObsChangeListener(
			int initialSeriesNum) {
		final int initialSeriesNumFinal = initialSeriesNum;

		return new Listener<BinningResult>() {
			private int meanSourceSeriesNum = initialSeriesNumFinal;

			public boolean canBeRemoved() {
				return true;
			}

			public void update(BinningResult info) {
				if (this.meanSourceSeriesNum != obsAndMeanPlotModel
						.getMeanSourceSeriesNum()) {

					this.meanSourceSeriesNum = obsAndMeanPlotModel
							.getMeanSourceSeriesNum();

					SeriesType meanSourceSeriesType = obsAndMeanPlotModel
							.getSeriesNumToSrcTypeMap().get(
									this.meanSourceSeriesNum);

					// Also send out a mean-source series notification.
					// TODO: why doesn't MeanSourcePane do this instead!!! ***
					meanSourceSeriesChangeNotifier
							.notifyListeners(new MeanSourceSeriesChangeMessage(
									this, meanSourceSeriesType));
				}
			}
		};
	}

	// When the period changes, create a new phase plot passing the pre-existing
	// series visibility map if a previous phase plot was created.
	//
	// TODO: actually, createPhasePlotArtefacts() should not be necessary; it
	// should
	// only be necessary to a. set the phases with the new period and epoch
	// (need to
	// include the epoch in the message), and b. update the plot and table
	// models.
	private Listener<PeriodChangeMessage> createPeriodChangeListener() {
		return new Listener<PeriodChangeMessage>() {
			public void update(PeriodChangeMessage info) {
				try {
					AnalysisTypeChangeMessage msg = null;

					PhaseParameterDialog phaseDialog = Mediator.getInstance()
							.getPhaseParameterDialog();
					phaseDialog.setPeriodField(info.getPeriod());
					phaseDialog.showDialog();

					if (!phaseDialog.isCancelled()) {
						double period = phaseDialog.getPeriod();
						double epoch = phaseDialog.getEpoch();

						MainFrame.getInstance().setCursor(
								Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

						AnalysisTypeChangeMessage lastPhasePlotMsg = analysisTypeMap
								.get(AnalysisType.PHASE_PLOT);

						Map<SeriesType, Boolean> seriesVisibilityMap = null;

						if (lastPhasePlotMsg != null) {
							// Use the last phase plot's series visibility map.
							seriesVisibilityMap = lastPhasePlotMsg
									.getObsAndMeanChartPane().getObsModel()
									.getSeriesVisibilityMap();
						} else {
							// There has been no phase plot yet, so use the
							// light curve's series visibility map.
							AnalysisTypeChangeMessage lightCurveMsg = analysisTypeMap
									.get(AnalysisType.RAW_DATA);
							seriesVisibilityMap = lightCurveMsg
									.getObsAndMeanChartPane().getObsModel()
									.getSeriesVisibilityMap();
						}

						msg = createPhasePlotArtefacts(period, epoch,
								seriesVisibilityMap);
						analysisType = AnalysisType.PHASE_PLOT;

						analysisTypeChangeNotifier.notifyListeners(msg);
						MainFrame.getInstance().setCursor(null);
					}
				} catch (Exception e) {
					MainFrame.getInstance().setCursor(null);
					MessageBox.showErrorDialog(MainFrame.getInstance(),
							"New Phase Plot", e);
				}
			}

			public boolean canBeRemoved() {
				return false;
			}
		};
	}

	// Returns a model selection listener that updates the observation category
	// map with model and residuals series.
	protected Listener<ModelSelectionMessage> createModelSelectionListener() {
		return new Listener<ModelSelectionMessage>() {
			@Override
			public void update(ModelSelectionMessage info) {
				validObservationCategoryMap.put(SeriesType.Model, info
						.getModel().getFit());

				validObservationCategoryMap.put(SeriesType.Residuals, info
						.getModel().getResiduals());
			}

			@Override
			public boolean canBeRemoved() {
				return false;
			}
		};
	}

	// Returns a filtered observation listener that updates the observation
	// category map with the filtered series.
	protected Listener<FilteredObservationMessage> createFilteredObservationListener() {
		return new Listener<FilteredObservationMessage>() {
			@Override
			public void update(FilteredObservationMessage info) {
				if (info == FilteredObservationMessage.NO_FILTER) {
					validObservationCategoryMap.remove(SeriesType.Filtered);
				} else {
					// First, copy the set of filtered observations to a list.
					List<ValidObservation> obs = new ArrayList<ValidObservation>();
					for (ValidObservation ob : info.getFilteredObs()) {
						obs.add(ob);
					}
					validObservationCategoryMap.put(SeriesType.Filtered, obs);
				}
			}

			@Override
			public boolean canBeRemoved() {
				return false;
			}
		};
	}

	/**
	 * Remove all willing listeners from notifiers. This is essentially a move
	 * to free up any indirectly referenced objects that may cause a memory leak
	 * if left unchecked from new-star to new-star, e.g. mean observations.
	 * 
	 * @param obsAndMeanPlotModel
	 *            A raw observation and mean plot model from which to remove
	 *            willing listeners. This will change between stars.
	 */
	// private void removeWillingListeners(
	// ObservationAndMeanPlotModel obsAndMeanPlotModel) {
	// this.analysisTypeChangeNotifier.removeAllWillingListeners();
	// this.newStarNotifier.removeAllWillingListeners();
	// this.progressNotifier.removeAllWillingListeners();
	// this.observationChangeNotifier.removeAllWillingListeners();
	// this.observationSelectionNotifier.removeAllWillingListeners();
	// this.periodAnalysisSelectionNotifier.removeAllWillingListeners();
	//
	// obsAndMeanPlotModel.getMeansChangeNotifier()
	// .removeAllWillingListeners();
	//
	// SeriesType.getSeriesColorChangeNotifier().removeAllWillingListeners();
	// }

	/**
	 * Change the mode of VStar's focus (i.e what is to be presented to the
	 * user).
	 * 
	 * @param viewMode
	 *            The mode to change to.
	 */
	public void changeViewMode(ViewModeType viewMode) {
		if (viewMode != this.viewMode) {
			this.viewMode = viewMode;
		}
	}

	/**
	 * @return the viewMode
	 */
	public ViewModeType getViewMode() {
		return viewMode;
	}

	/**
	 * @return the phaseParameterDialog
	 */
	public PhaseParameterDialog getPhaseParameterDialog() {
		return phaseParameterDialog;
	}

	/**
	 * @return the obsFilterDialog
	 */
	public ObservationFilterDialog getObsFilterDialog() {
		return obsFilterDialog;
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
						this.analysisTypeChangeNotifier.notifyListeners(msg);
						String statusMsg = "Raw data mode ("
								+ this.newStarMessage.getStarInfo()
										.getDesignation() + ")";
						MainFrame.getInstance().getStatusPane().setMessage(
								statusMsg);
					}
					break;

				case PHASE_PLOT:
					// Create or retrieve phase plots and data tables passing
					// the light curve's series visibility map for the first
					// phase plot.
					msg = this.analysisTypeMap.get(AnalysisType.PHASE_PLOT);

					if (msg == null) {
						PhaseParameterDialog phaseDialog = Mediator
								.getInstance().getPhaseParameterDialog();
						phaseDialog.showDialog();
						if (!phaseDialog.isCancelled()) {
							double period = phaseDialog.getPeriod();
							double epoch = phaseDialog.getEpoch();
							MainFrame
									.getInstance()
									.setCursor(
											Cursor
													.getPredefinedCursor(Cursor.WAIT_CURSOR));

							AnalysisTypeChangeMessage rawDataMsg = analysisTypeMap
									.get(AnalysisType.RAW_DATA);

							Map<SeriesType, Boolean> seriesVisibilityMap = rawDataMsg
									.getObsAndMeanChartPane().getObsModel()
									.getSeriesVisibilityMap();

							msg = createPhasePlotArtefacts(period, epoch,
									seriesVisibilityMap);
						}
					}

					if (msg != null) {
						this.analysisType = analysisType;
						// TODO: we should only do this if msg != oldMsg
						// since we do this in createPhasePlotArtefacts();
						// should just make this an else clause of above if
						// stmt.
						this.analysisTypeChangeNotifier.notifyListeners(msg);
						String statusMsg = "Phase plot mode ("
								+ this.newStarMessage.getStarInfo()
										.getDesignation() + ")";
						MainFrame.getInstance().getStatusPane().setMessage(
								statusMsg);
						MainFrame.getInstance().setCursor(null);
					}
					break;
				}
			} catch (Exception e) {
				MainFrame.getInstance().setCursor(null);
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
	public void createObservationArtefactsFromFile(File obsFile)
			throws IOException, ObservationReadError {

		this.getProgressNotifier().notifyListeners(ProgressInfo.START_PROGRESS);

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
		this.currTask = task;
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
			this.getProgressNotifier().notifyListeners(
					ProgressInfo.START_PROGRESS);

			this.getProgressNotifier().notifyListeners(
					new ProgressInfo(ProgressType.MAX_PROGRESS, 10));

			NewStarFromDatabaseTask task = new NewStarFromDatabaseTask(
					starName, auid, minJD, maxJD);
			this.currTask = task;
			task.execute();
		} catch (Exception ex) {
			MessageBox.showErrorDialog(MainFrame.getInstance(),
					MenuBar.NEW_STAR_FROM_DATABASE, ex);
			MainFrame.getInstance().getStatusPane().setMessage("");
		}
	}

	/**
	 * Creates and executes a background task to handle
	 * new-star-from-external-source-plugin.
	 * 
	 * @param obSourcePlugin
	 *            The plugin that will be used to obtain observations.
	 */
	public void createObservationArtefactsFromObSourcePlugin(
			ObservationSourcePluginBase obSourcePlugin) {

		this.getProgressNotifier().notifyListeners(ProgressInfo.START_PROGRESS);
		this.getProgressNotifier().notifyListeners(ProgressInfo.BUSY_PROGRESS);

		NewStarFromObSourcePluginTask task = new NewStarFromObSourcePluginTask(
				obSourcePlugin);
		this.currTask = task;
		task.execute();
	}

	/**
	 * Create observation artefacts (models, GUI elements) on the assumption
	 * that a valid observation list and category map have already been created.
	 * 
	 * @param newStarType
	 *            The new star enum type.
	 * @param starInfo
	 *            Information about the star, e.g. name (designation), AUID (for
	 *            AID), period, epoch.
	 * @param obsRetriever
	 *            The observation source.
	 * @param obsArtefactProgressAmount
	 *            The amount the progress bar should be incremented by, a value
	 *            corresponding to a portion of the overall task of which this
	 *            is just a part.
	 */
	protected void createNewStarObservationArtefacts(NewStarType newStarType,
			StarInfo starInfo, AbstractObservationRetriever obsRetriever,
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
		RawDataMeanObservationTableModel meanObsTableModel = null;

		// Plot models.
		obsAndMeanPlotModel = null;

		// GUI table and chart components.
		ObservationListPane obsListPane = null;
		MeanObservationListPane meansListPane = null;
		ObservationAndMeanPlotPane obsAndMeanChartPane = null;

		if (!validObsList.isEmpty()) {

			// This is a specific fix for tracker 3007948.
			this.discrepantObservationNotifier = new Notifier<DiscrepantObservationMessage>();

			// Observation table and plot.
			validObsTableModel = new ValidObservationTableModel(validObsList,
					newStarType.getRawDataTableColumnInfoSource());

			String subTitle = "";
			if (newStarType == NewStarType.NEW_STAR_FROM_DATABASE) {
				subTitle = new Date().toString() + " (database)";
			} else {
				subTitle = "";
			}

			// Observation-and-mean table and plot.
			obsAndMeanPlotModel = new ObservationAndMeanPlotModel(
					validObservationCategoryMap, JDCoordSource.instance,
					JDComparator.instance, JDTimeElementEntity.instance, null);

			String meanPlotSubtitle = subTitle;
			if (!"".equals(meanPlotSubtitle)) {
				meanPlotSubtitle += ", ";
			}

			meanPlotSubtitle += "Mean error bars denote 95% Confidence Interval (twice Standard Error)";

			obsAndMeanChartPane = createObservationAndMeanPlotPane(
					"Raw Plot for " + starInfo.getDesignation(),
					meanPlotSubtitle, obsAndMeanPlotModel);

			obsAndMeanPlotModel.getMeansChangeNotifier().addListener(
					createMeanObsChangeListener(obsAndMeanPlotModel
							.getMeanSourceSeriesNum()));

			// The mean observation table model must listen to the plot
			// model to know when the means data has changed. We also pass
			// the initial means data obtained from the plot model to
			// the mean observation table model.
			meanObsTableModel = new RawDataMeanObservationTableModel(
					obsAndMeanPlotModel.getMeanObsList());

			obsAndMeanPlotModel.getMeansChangeNotifier().addListener(
					meanObsTableModel);

			rawPlotControlDialog = new RawPlotControlDialog(obsAndMeanChartPane);

			if (obsArtefactProgressAmount > 0) {
				// Update progress.
				getProgressNotifier().notifyListeners(
						new ProgressInfo(ProgressType.INCREMENT_PROGRESS,
								obsArtefactProgressAmount));
			}
		}

		if (!invalidObsList.isEmpty()) {
			invalidObsTableModel = new InvalidObservationTableModel(
					invalidObsList);
		}

		// The observation table pane contains valid and potentially
		// invalid data components. Tell the valid data table to have
		// a horizontal scrollbar if there will be too many columns.

		boolean enableColumnAutoResize = newStarType == NewStarType.NEW_STAR_FROM_SIMPLE_FILE
				|| newStarType == NewStarType.NEW_STAR_FROM_EXTERNAL_SOURCE;

		obsListPane = new ObservationListPane(validObsTableModel,
				invalidObsTableModel, enableColumnAutoResize);

		// We also create the means list pane.
		meansListPane = new MeanObservationListPane(meanObsTableModel);

		// Create a message to notify whoever is listening that a new star
		// has been loaded.
		newStarMessage = new NewStarMessage(newStarType, starInfo,
				validObsList, validObservationCategoryMap, obsRetriever
						.getMinMag(), obsRetriever.getMaxMag());

		// Create a message to notify whoever is listening that the analysis
		// type has changed (we could have been viewing a phase plot for a
		// different star before now) passing GUI components in the message.
		analysisType = AnalysisType.RAW_DATA;

		AnalysisTypeChangeMessage analysisTypeMsg = new AnalysisTypeChangeMessage(
				analysisType, obsAndMeanChartPane, obsListPane, meansListPane,
				ViewModeType.PLOT_OBS_MODE);

		// Commit to using the new observation lists and category map,
		// first making old values available for garbage collection.
		// TODO: It would be worth considering doing this at the start
		// of this method, not at the end, so more memory is free.

		if (this.validObsList != null) {
			this.validObsList.clear();
		}

		if (this.invalidObsList != null) {
			this.invalidObsList.clear();
		}

		if (this.validObservationCategoryMap != null) {
			this.validObservationCategoryMap.clear();
		}

		if (this.phasedValidObservationCategoryMap != null) {
			// In case we did a phase plot, free this up.
			this.phasedValidObservationCategoryMap.clear();
			this.phasedValidObservationCategoryMap = null;
		}

		// Throw away old artefacts from raw and phase plot,
		// if there was (at least) one.
		analysisTypeMap.clear();
		analysisTypeMap.put(analysisType, analysisTypeMsg);

		// Suggest garbage collection.
		System.gc();

		// Store new data.
		this.validObsList = validObsList;
		this.invalidObsList = invalidObsList;
		this.validObservationCategoryMap = validObservationCategoryMap;

		// Notify listeners of new star and analysis type.
		getNewStarNotifier().notifyListeners(newStarMessage);
		getAnalysisTypeChangeNotifier().notifyListeners(analysisTypeMsg);
	}

	/**
	 * Create phase plot artefacts, adding them to the analysis type map and
	 * returning this message.
	 * 
	 * @param period
	 *            The requested period of the phase plot.
	 * @param epoch
	 *            The epoch (first Julian Date) for the phase plot.
	 * @param seriesVisibilityMap
	 *            A mapping from series number to visibility status.
	 * @return An analysis type message consisting of phase plot artefacts.
	 */
	public AnalysisTypeChangeMessage createPhasePlotArtefacts(double period,
			double epoch, Map<SeriesType, Boolean> seriesVisibilityMap)
			throws Exception {
		String objName = newStarMessage.getStarInfo().getDesignation();

		String subTitle = "";
		String periodAndEpochStr = String.format("period: "
				+ NumericPrecisionPrefs.getOtherOutputFormat() + ", epoch: "
				+ NumericPrecisionPrefs.getTimeOutputFormat(), period, epoch);

		if (this.newStarMessage.getNewStarType() == NewStarType.NEW_STAR_FROM_DATABASE) {
			subTitle = new Date().toString() + " (database), "
					+ periodAndEpochStr;
		} else {
			subTitle = periodAndEpochStr;
		}

		// Here we modify the underlying ValidObservation objects which will
		// affect both validObsList and validObservationCategoryMap. Some
		// series are not in the main observation list, only in the map
		// (e.g. model, residuals, filtered obs), so we handle those separately.
		PhaseCalcs.setPhases(validObsList, epoch, period);
		setPhasesForSeries(SeriesType.Model, epoch, period);
		setPhasesForSeries(SeriesType.Residuals, epoch, period);
		setPhasesForSeries(SeriesType.Filtered, epoch, period);

		// We duplicate the valid observation category map
		// so that we have two sets of identical data for the
		// two cycles of the phase plot. This map will be shared
		// by ordinary plot and mean plot models.
		Map<SeriesType, List<ValidObservation>> phasedValidObservationCategoryMap = new TreeMap<SeriesType, List<ValidObservation>>();

		for (SeriesType series : validObservationCategoryMap.keySet()) {
			List<ValidObservation> obs = validObservationCategoryMap
					.get(series);

			List<ValidObservation> doubledObs = new ArrayList<ValidObservation>();
			doubledObs.addAll(obs);
			Collections.sort(doubledObs, StandardPhaseComparator.instance);
			doubledObs.addAll(doubledObs);

			phasedValidObservationCategoryMap.put(series, doubledObs);
		}

		// Table and plot models.
		ValidObservationTableModel validObsTableModel = new ValidObservationTableModel(
				validObsList, newStarMessage.getNewStarType()
						.getPhasePlotTableColumnInfoSource());

		// Observation-and-mean table and plot.
		ObservationAndMeanPlotModel obsAndMeanPlotModel = new ObservationAndMeanPlotModel(
				phasedValidObservationCategoryMap, PhaseCoordSource.instance,
				StandardPhaseComparator.instance,
				PhaseTimeElementEntity.instance, seriesVisibilityMap);

		// The mean observation table model must listen to the plot
		// model to know when the means data has changed. We also pass
		// the initial means data obtained from the plot model to
		// the mean observation table model.
		PhasePlotMeanObservationTableModel meanObsTableModel = new PhasePlotMeanObservationTableModel(
				obsAndMeanPlotModel.getMeanObsList());

		obsAndMeanPlotModel.getMeansChangeNotifier().addListener(
				meanObsTableModel);

		PhaseAndMeanPlotPane obsAndMeanChartPane = createPhaseAndMeanPlotPane(
				"Phase Plot for " + objName, subTitle, obsAndMeanPlotModel,
				epoch, period);

		phasePlotControlDialog = new PhasePlotControlDialog(obsAndMeanChartPane);

		// The observation table pane contains valid and potentially
		// invalid data components but for phase plot purposes, we only
		// display valid data, as opposed to the raw data view in which
		// both are shown. Tell the valid data table to have a horizontal
		// scrollbar if there will be too many columns.
		boolean enableColumnAutoResize = newStarMessage.getNewStarType() == NewStarType.NEW_STAR_FROM_SIMPLE_FILE
				|| newStarMessage.getNewStarType() == NewStarType.NEW_STAR_FROM_EXTERNAL_SOURCE;

		ObservationListPane obsListPane = new ObservationListPane(
				validObsTableModel, null, enableColumnAutoResize);

		MeanObservationListPane meansListPane = new MeanObservationListPane(
				meanObsTableModel);

		// Observation-and-mean table and plot.
		AnalysisTypeChangeMessage phasePlotMsg = new AnalysisTypeChangeMessage(
				AnalysisType.PHASE_PLOT, obsAndMeanChartPane, obsListPane,
				meansListPane, ViewModeType.PLOT_OBS_MODE);

		analysisTypeMap.put(AnalysisType.PHASE_PLOT, phasePlotMsg);

		this.analysisTypeChangeNotifier.notifyListeners(phasePlotMsg);

		return phasePlotMsg;
	}

	/**
	 * Set the phases for a particular series in the observation category map.
	 * 
	 * @param type
	 *            The series type of the observations whose phases are to be
	 *            set.
	 * @param epoch
	 *            The epoch to use for the phase calculation.
	 * @param period
	 *            The period to use for the phase calculation.
	 */
	public void setPhasesForSeries(SeriesType type, double epoch, double period) {
		if (validObservationCategoryMap.containsKey(type)) {
			List<ValidObservation> obs = validObservationCategoryMap.get(type);
			PhaseCalcs.setPhases(obs, epoch, period);
		}
	}

	/**
	 * Block, waiting for a job to complete. We only want to block if there is a
	 * concurrent task in progress.
	 */
	public void waitForJobCompletion() {
		if (currTask != null && !currTask.isDone()) {
			try {
				currTask.get();
			} catch (InterruptedException e) {
			} catch (ExecutionException e) {
			}
		}
	}

	/**
	 * Attempt to stop the current task.
	 */
	public void stopCurrentTask() {
		if (this.currTask != null) {
			this.currTask.cancel(true);
		}
	}

	/**
	 * Clear the current task if not already cleared.
	 */
	public void clearCurrentTask() {
		if (this.currTask != null) {
			this.currTask = null;
		}
	}

	/**
	 * Create the observation-and-mean plot pane for the current list of valid
	 * observations.
	 */
	private ObservationAndMeanPlotPane createObservationAndMeanPlotPane(
			String plotName, String subTitle,
			ObservationAndMeanPlotModel obsAndMeanPlotModel) {

		Dimension bounds = new Dimension((int) (TabbedDataPane.WIDTH * 0.9),
				(int) (TabbedDataPane.HEIGHT * 0.9));

		return new ObservationAndMeanPlotPane(plotName, subTitle,
				obsAndMeanPlotModel, new TimeElementsInBinSettingPane(
						"Days per Mean Series Bin", obsAndMeanPlotModel,
						JDTimeElementEntity.instance), bounds);
	}

	/**
	 * Create the observation-and-mean phase plot pane for the current list of
	 * valid observations.
	 */
	private PhaseAndMeanPlotPane createPhaseAndMeanPlotPane(String plotName,
			String subTitle, ObservationAndMeanPlotModel obsAndMeanPlotModel,
			double epoch, double period) {

		Dimension bounds = new Dimension((int) (TabbedDataPane.WIDTH * 0.9),
				(int) (TabbedDataPane.HEIGHT * 0.9));

		return new PhaseAndMeanPlotPane(plotName, subTitle,
				obsAndMeanPlotModel, bounds, epoch, period);
	}

	/**
	 * Create a period analysis dialog after the analysis is done. We apply the
	 * analysis to the series that is currently selected as being the mean
	 * series source. It only makes sense to apply the observations to a single
	 * band as per this Q & A between Matt Templeton and I: DB: Like mean curve
	 * creation in VStar, should we only apply DC DFT to a single band, e.g.
	 * visual? MT: Yes, because of two things: 1) The different bands will have
	 * different mean values, and 2) The different bands will have different
	 * amplitudes or frequencies depending on what is physically causing the
	 * variation. Variability caused by temperature changes can have wildly
	 * different amplitudes in U or B versus Rc or Ic.
	 */
	public void createPeriodAnalysisDialog(PeriodAnalysisPluginBase plugin) {
		try {
			if (this.newStarMessage != null && this.validObsList != null) {
				// TODO: make each plugin responsible for determining
				// and tracking source series (see mean source series dialog
				// which could be invoked from here or from within plugin code).

				List<ValidObservation> meanObsSourceList = getMeanSourceObservations();

				SeriesType meanObsSourceSeriesType = obsAndMeanPlotModel
						.getSeriesNumToSrcTypeMap().get(
								obsAndMeanPlotModel.getMeanSourceSeriesNum());

				this.getProgressNotifier().notifyListeners(
						ProgressInfo.START_PROGRESS);
				this.getProgressNotifier().notifyListeners(
						ProgressInfo.BUSY_PROGRESS);

				PeriodAnalysisTask task = new PeriodAnalysisTask(plugin,
						meanObsSourceSeriesType, meanObsSourceList);

				this.currTask = task;
				task.execute();
			}
		} catch (Exception e) {
			MessageBox.showErrorDialog(MainFrame.getInstance(),
					"Period Analysis", e);

			// TODO: why?
			this.getProgressNotifier().notifyListeners(
					ProgressInfo.START_PROGRESS);

			MainFrame.getInstance().getStatusPane().setMessage("");
		}
	}

	/**
	 * Open the plot control dialog relevant to the current analysis mode.
	 */
	public void showPlotControlDialog() {
		if (analysisType == AnalysisType.RAW_DATA) {
			rawPlotControlDialog.setVisible(true);
		} else if (analysisType == AnalysisType.PHASE_PLOT) {
			phasePlotControlDialog.setVisible(true);
		}
	}

	/**
	 * Perform a polynomial fit operation.
	 */
	public void performPolynomialFit() {
		try {
			if (this.newStarMessage != null && this.validObsList != null) {

				List<ValidObservation> meanObsSourceList = getMeanSourceObservations();

				// TODO: later, if we want to allow polynomial fit plugins, this
				// can be created by a plugin impl class just as we do
				// for DC DFT.
				IPolynomialFitter polynomialFitter = new TSPolynomialFitter(
						meanObsSourceList);

				int minDegree = polynomialFitter.getMinDegree();
				int maxDegree = polynomialFitter.getMaxDegree();

				PolynomialDegreeDialog dialog = new PolynomialDegreeDialog(
						minDegree, maxDegree);

				if (!dialog.isCancelled()) {
					polynomialFitter.setDegree(dialog.getDegree());

					PolynomialFitTask task = new PolynomialFitTask(
							polynomialFitter);

					this.currTask = task;

					this.getProgressNotifier().notifyListeners(
							ProgressInfo.START_PROGRESS);
					this.getProgressNotifier().notifyListeners(
							ProgressInfo.BUSY_PROGRESS);

					task.execute();
				}
			}
		} catch (Exception e) {
			MessageBox.showErrorDialog(MainFrame.getInstance(),
					"Polynomial Fit Error", e);

			this.getProgressNotifier().notifyListeners(
					ProgressInfo.START_PROGRESS);

			MainFrame.getInstance().getStatusPane().setMessage("");
		}
	}

	// Returns the observation list corresponding to the series that is the
	// current means series source (i.e. the source of the mean curve).
	private List<ValidObservation> getMeanSourceObservations() {
		int meanObsSourceSeriesNum = obsAndMeanPlotModel
				.getMeanSourceSeriesNum();

		return obsAndMeanPlotModel.getSeriesNumToObSrcListMap().get(
				meanObsSourceSeriesNum);
	}

	/**
	 * Invokes a tool plugin with the currently loaded observation set.
	 * 
	 * @param plugin
	 *            The tool plugin to be invoked.
	 */
	public void invokeTool(ObservationToolPluginBase plugin) {
		if (validObsList != null) {
			try {
				plugin.invoke(validObsList);
			} catch (Throwable t) {
				MessageBox.showErrorDialog("Tool Error", t);
			}
		} else {
			MessageBox.showMessageDialog(MainFrame.getInstance(), "Tool Error",
					"There are no observations loaded.");
		}
	}

	/**
	 * Applies the custom filter plugin to the currently loaded observation set.
	 * 
	 * @param plugin
	 *            The tool plugin to be invoked.
	 */
	public void applyCustomFilterToCurrentObservations(
			CustomFilterPluginBase plugin) {
		if (validObsList != null) {
			try {
				plugin.apply(validObsList);
			} catch (Throwable t) {
				MessageBox.showErrorDialog("Custom Filter Error", t);
			}
		} else {
			MessageBox.showMessageDialog(MainFrame.getInstance(),
					"Custom Filter", "There are no observations loaded.");
		}
	}

	/**
	 * Save the artefact corresponding to the current viewMode.
	 * 
	 * @param parent
	 *            The parent component to be used by an error dialog.
	 */
	public void saveCurrentMode(Component parent) {
		switch (viewMode) {
		case PLOT_OBS_MODE:
			try {
				this.analysisTypeMap.get(analysisType).getObsAndMeanChartPane()
						.getChartPanel().doSaveAs();
			} catch (IOException ex) {
				MessageBox.showErrorDialog(parent,
						"Save Observation and Means Plot", ex.getMessage());
			}
			break;
		case LIST_OBS_MODE:
			int returnVal = obsListFileSaveDialog.showSaveDialog(parent);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File outFile = obsListFileSaveDialog.getSelectedFile();
				saveObsListToFile(outFile);
			}
			break;
		case LIST_MEANS_MODE:
			MessageBox.showMessageDialog(parent, "Save Means",
					NOT_IMPLEMENTED_YET);
			break;
		case MODEL_MODE:
			MessageBox.showMessageDialog(parent, "Save Model",
					NOT_IMPLEMENTED_YET);
			break;
		case RESIDUALS_MODE:
			MessageBox.showMessageDialog(parent, "Save Residuals",
					NOT_IMPLEMENTED_YET);
			break;
		}
	}

	/**
	 * Save observation list to a file in a separate thread.
	 * 
	 * @param outFile
	 *            The output file.
	 */
	private void saveObsListToFile(File outFile) {
		this.getProgressNotifier().notifyListeners(ProgressInfo.START_PROGRESS);

		this.getProgressNotifier()
				.notifyListeners(
						new ProgressInfo(ProgressType.MAX_PROGRESS,
								validObsList.size()));

		ObsListFileSaveTask task = new ObsListFileSaveTask(validObsList,
				outFile, this.newStarMessage.getNewStarType());

		this.currTask = task;
		task.execute();
	}

	/**
	 * Print the artefact corresponding to the current mode.
	 * 
	 * @param parent
	 *            The parent component to be used by an error dialog.
	 */
	public void printCurrentMode(Component parent) {
		switch (viewMode) {
		case PLOT_OBS_MODE:
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
			try {
				MeanObservationListPane meanObsListPane = this.analysisTypeMap
						.get(analysisType).getMeansListPane();

				meanObsListPane.getMeanObsTable().print(PrintMode.FIT_WIDTH);
			} catch (PrinterException e) {
				MessageBox.showErrorDialog(parent, "Print Means", e
						.getMessage());
			}
			break;
		case MODEL_MODE:
			MessageBox.showMessageDialog(parent, "Print Model",
					NOT_IMPLEMENTED_YET);
			break;
		case RESIDUALS_MODE:
			MessageBox.showMessageDialog(parent, "Print Residuals",
					NOT_IMPLEMENTED_YET);
			break;
		}
	}

	/**
	 * Show the details of the currently selected observation in the current
	 * view mode (plot or table).
	 */
	public void showObservationDetails() {
		ValidObservation ob = null;

		switch (viewMode) {
		case PLOT_OBS_MODE:
			ob = this.analysisTypeMap.get(analysisType)
					.getObsAndMeanChartPane().getLastObSelected();
			break;
		case LIST_OBS_MODE:
			ob = this.analysisTypeMap.get(analysisType).getObsListPane()
					.getLastObSelected();
			break;
		case LIST_MEANS_MODE:
			ob = this.analysisTypeMap.get(analysisType).getMeansListPane()
					.getLastObSelected();
			break;
		}

		if (ob != null) {
			new ObservationDetailsDialog(ob);
		} else {
			MessageBox.showWarningDialog("Observation Details",
					"No observation selected");
		}
	}

	/**
	 * Exit VStar.
	 */
	public void quit() {
		// TODO: do other cleanup, e.g. if file needs saving;
		// need a document model including undo for this;
		// defer to Mediator.
		System.exit(0);
	}
}
