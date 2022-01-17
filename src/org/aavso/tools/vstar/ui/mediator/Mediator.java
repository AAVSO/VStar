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
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;

import javax.swing.JDialog;
import javax.swing.JTable.PrintMode;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.SwingWorker;

import org.aavso.tools.vstar.data.InvalidObservation;
import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.data.ValidObservation.JDflavour;
import org.aavso.tools.vstar.exception.AuthenticationError;
import org.aavso.tools.vstar.exception.CancellationException;
import org.aavso.tools.vstar.exception.ConnectionException;
import org.aavso.tools.vstar.exception.ObservationReadError;
import org.aavso.tools.vstar.input.AbstractObservationRetriever;
import org.aavso.tools.vstar.input.database.Authenticator;
import org.aavso.tools.vstar.plugin.CustomFilterPluginBase;
import org.aavso.tools.vstar.plugin.ModelCreatorPluginBase;
import org.aavso.tools.vstar.plugin.ObservationSinkPluginBase;
import org.aavso.tools.vstar.plugin.ObservationSourcePluginBase;
import org.aavso.tools.vstar.plugin.ObservationToolPluginBase;
import org.aavso.tools.vstar.plugin.ObservationTransformerPluginBase;
import org.aavso.tools.vstar.plugin.period.PeriodAnalysisPluginBase;
import org.aavso.tools.vstar.ui.IMainUI;
import org.aavso.tools.vstar.ui.NamedComponent;
import org.aavso.tools.vstar.ui.TabbedDataPane;
import org.aavso.tools.vstar.ui.dialog.DelimitedFieldFileSaveChooser;
import org.aavso.tools.vstar.ui.dialog.DiscrepantReportDialog;
import org.aavso.tools.vstar.ui.dialog.DoubleField;
import org.aavso.tools.vstar.ui.dialog.IntegerField;
import org.aavso.tools.vstar.ui.dialog.MessageBox;
import org.aavso.tools.vstar.ui.dialog.MultiEntryComponentDialog;
import org.aavso.tools.vstar.ui.dialog.ObservationDetailsDialog;
import org.aavso.tools.vstar.ui.dialog.PNGImageFileSaveChooser;
import org.aavso.tools.vstar.ui.dialog.PhaseDialog;
import org.aavso.tools.vstar.ui.dialog.PhaseParameterDialog;
import org.aavso.tools.vstar.ui.dialog.PlotControlDialog;
import org.aavso.tools.vstar.ui.dialog.FileIOchoosers;
import org.aavso.tools.vstar.ui.dialog.FileIOchoosers.fileIOchooserMode;
import org.aavso.tools.vstar.ui.dialog.filter.ObservationFilterDialog;
import org.aavso.tools.vstar.ui.dialog.filter.ObservationFiltersDialog;
import org.aavso.tools.vstar.ui.dialog.model.ModelDialog;
import org.aavso.tools.vstar.ui.dialog.plugin.manager.PluginManagementOperation;
import org.aavso.tools.vstar.ui.dialog.series.MultipleSeriesSelectionDialog;
import org.aavso.tools.vstar.ui.dialog.series.SingleSeriesSelectionDialog;
import org.aavso.tools.vstar.ui.mediator.message.AnalysisTypeChangeMessage;
import org.aavso.tools.vstar.ui.mediator.message.DiscrepantObservationMessage;
import org.aavso.tools.vstar.ui.mediator.message.ExcludedObservationMessage;
import org.aavso.tools.vstar.ui.mediator.message.FilteredObservationMessage;
import org.aavso.tools.vstar.ui.mediator.message.HarmonicSearchResultMessage;
import org.aavso.tools.vstar.ui.mediator.message.MeanSourceSeriesChangeMessage;
import org.aavso.tools.vstar.ui.mediator.message.ModelCreationMessage;
import org.aavso.tools.vstar.ui.mediator.message.ModelSelectionMessage;
import org.aavso.tools.vstar.ui.mediator.message.MultipleObservationSelectionMessage;
import org.aavso.tools.vstar.ui.mediator.message.NewStarMessage;
import org.aavso.tools.vstar.ui.mediator.message.ObservationSelectionMessage;
import org.aavso.tools.vstar.ui.mediator.message.PanRequestMessage;
import org.aavso.tools.vstar.ui.mediator.message.PeriodAnalysisRefinementMessage;
import org.aavso.tools.vstar.ui.mediator.message.PeriodAnalysisSelectionMessage;
import org.aavso.tools.vstar.ui.mediator.message.PeriodChangeMessage;
import org.aavso.tools.vstar.ui.mediator.message.PhaseChangeMessage;
import org.aavso.tools.vstar.ui.mediator.message.PhaseSelectionMessage;
import org.aavso.tools.vstar.ui.mediator.message.ProgressInfo;
import org.aavso.tools.vstar.ui.mediator.message.ProgressType;
import org.aavso.tools.vstar.ui.mediator.message.SeriesCreationMessage;
import org.aavso.tools.vstar.ui.mediator.message.SeriesVisibilityChangeMessage;
import org.aavso.tools.vstar.ui.mediator.message.StopRequestMessage;
import org.aavso.tools.vstar.ui.mediator.message.UndoActionMessage;
import org.aavso.tools.vstar.ui.mediator.message.UndoableActionType;
import org.aavso.tools.vstar.ui.mediator.message.ZoomRequestMessage;
import org.aavso.tools.vstar.ui.model.list.AbstractMeanObservationTableModel;
import org.aavso.tools.vstar.ui.model.list.AbstractModelObservationTableModel;
import org.aavso.tools.vstar.ui.model.list.InvalidObservationTableModel;
import org.aavso.tools.vstar.ui.model.list.PhasePlotMeanObservationTableModel;
import org.aavso.tools.vstar.ui.model.list.RawDataMeanObservationTableModel;
import org.aavso.tools.vstar.ui.model.list.ValidObservationTableModel;
import org.aavso.tools.vstar.ui.model.plot.ContinuousModelFunction;
import org.aavso.tools.vstar.ui.model.plot.ISeriesInfoProvider;
import org.aavso.tools.vstar.ui.model.plot.InViewObservationFilter;
import org.aavso.tools.vstar.ui.model.plot.JDCoordSource;
import org.aavso.tools.vstar.ui.model.plot.JDTimeElementEntity;
import org.aavso.tools.vstar.ui.model.plot.ObservationAndMeanPlotModel;
import org.aavso.tools.vstar.ui.model.plot.PhaseTimeElementEntity;
import org.aavso.tools.vstar.ui.model.plot.PhasedObservationAndMeanPlotModel;
import org.aavso.tools.vstar.ui.model.plot.PreviousCyclePhaseCoordSource;
import org.aavso.tools.vstar.ui.model.plot.StandardPhaseCoordSource;
import org.aavso.tools.vstar.ui.pane.list.ObservationListPane;
import org.aavso.tools.vstar.ui.pane.list.SyntheticObservationListPane;
import org.aavso.tools.vstar.ui.pane.plot.ObservationAndMeanPlotPane;
import org.aavso.tools.vstar.ui.pane.plot.PhaseAndMeanPlotPane;
import org.aavso.tools.vstar.ui.pane.plot.TimeElementsInBinSettingPane;
import org.aavso.tools.vstar.ui.resources.ResourceAccessor;
import org.aavso.tools.vstar.ui.task.ModellingTask;
import org.aavso.tools.vstar.ui.task.NewStarFromObSourcePluginTask;
import org.aavso.tools.vstar.ui.task.NewStarFromObSourcePluginWithSuppliedFileTask;
import org.aavso.tools.vstar.ui.task.NewStarFromObSourcePluginWithSuppliedURLTask;
import org.aavso.tools.vstar.ui.task.ObsListFileSaveTask;
import org.aavso.tools.vstar.ui.task.PeriodAnalysisTask;
import org.aavso.tools.vstar.ui.task.PhasePlotTask;
import org.aavso.tools.vstar.ui.task.PluginManagerOperationTask;
import org.aavso.tools.vstar.ui.undo.IUndoableAction;
import org.aavso.tools.vstar.ui.undo.UndoableActionManager;
import org.aavso.tools.vstar.ui.vela.VeLaFileLoadChooser;
import org.aavso.tools.vstar.ui.vela.VeLaFileSaveChooser;
import org.aavso.tools.vstar.util.Triple;
import org.aavso.tools.vstar.util.comparator.JDComparator;
import org.aavso.tools.vstar.util.comparator.PreviousCyclePhaseComparator;
import org.aavso.tools.vstar.util.comparator.StandardPhaseComparator;
import org.aavso.tools.vstar.util.coords.DecInfo;
import org.aavso.tools.vstar.util.coords.EpochType;
import org.aavso.tools.vstar.util.coords.RAInfo;
import org.aavso.tools.vstar.util.date.AbstractHJDConverter;
import org.aavso.tools.vstar.util.discrepant.DiscrepantReport;
import org.aavso.tools.vstar.util.discrepant.IDiscrepantReporter;
import org.aavso.tools.vstar.util.discrepant.VSXWebServiceZapperLogger;
import org.aavso.tools.vstar.util.locale.LocaleProps;
import org.aavso.tools.vstar.util.model.IModel;
import org.aavso.tools.vstar.util.notification.Listener;
import org.aavso.tools.vstar.util.notification.Notifier;
import org.aavso.tools.vstar.util.prefs.NumericPrecisionPrefs;
import org.aavso.tools.vstar.util.stats.BinningResult;
import org.aavso.tools.vstar.util.stats.PhaseCalcs;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;

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

	private static IMainUI ui;

	// Valid and invalid observation lists and series category map.
	private List<ValidObservation> validObsList;
	private List<InvalidObservation> invalidObsList;

	// Note: it would be useful to update these with mean obs, excluded obs etc
	// so they could be used in places where currently the model must be
	// consulted instead; especially the first map, e.g. for period analysis.
	private Map<SeriesType, List<ValidObservation>> validObservationCategoryMap;
	private Map<SeriesType, List<ValidObservation>> phasedValidObservationCategoryMap;

	// Current observation and mean plot model.
	// Period search (TODO: did I mean ANOVA vs period search?) needs access to
	// this to determine the current mean source band.
	private ObservationAndMeanPlotModel obsAndMeanPlotModel;

	// Current observation table model.
	private ValidObservationTableModel validObsTableModel;

	// Current view viewMode.
	private ViewModeType viewMode;

	// Current analysis type.
	private AnalysisType analysisType;

	// The new star messages created and sent to listeners, most recent at
	// the highest index.
	private List<NewStarMessage> newStarMessageList;

	// The latest model selection message created and sent to listeners.
	private ModelSelectionMessage modelSelectionMessage;

	// Mapping from analysis type to the latest analysis change
	// messages created and sent to listeners.
	private Map<AnalysisType, AnalysisTypeChangeMessage> analysisTypeMap;

	// A file dialog for saving any kind of observation list.
	private DelimitedFieldFileSaveChooser obsListFileSaveDialog;

	// A file dialog for saving an image, e.g. a plot.
	private PNGImageFileSaveChooser imageSaveDialog;

	// A file dialog for loading a VeLa code file.
	private VeLaFileLoadChooser velaFileLoadDialog;

	// A file dialog for saving a VeLa code file.
	private VeLaFileSaveChooser velaFileSaveDialog;
	
	// Methods to load/save an XML as String from/to VeLa Model XML files with respective choosers.  
	private FileIOchoosers velaXMLchoosers;

	// Persistent phase parameter dialog.
	private PhaseParameterDialog phaseParameterDialog;

	// Persistent observation filter dialog.
	private ObservationFilterDialog obsFilterDialog;

	// Model dialog.
	private ModelDialog modelDialog;

	// A dialog to manage phase plots.
	private PhaseDialog phaseDialog;

	// A dialog to manage filters.
	private ObservationFiltersDialog observationFiltersDialog;

	// Notifiers.
	private Notifier<AnalysisTypeChangeMessage> analysisTypeChangeNotifier;
	private Notifier<NewStarMessage> newStarNotifier;
	private Notifier<ProgressInfo> progressNotifier;
	// TODO: This next notifier could be used to mark the "document"
	// (the current star's dataset) associated with the valid obs
	// as being in need of saving (optional for now). See DocumentManager
	private Notifier<DiscrepantObservationMessage> discrepantObservationNotifier;
	private Notifier<ExcludedObservationMessage> excludedObservationNotifier;
	private Notifier<ObservationSelectionMessage> observationSelectionNotifier;
	private Notifier<MultipleObservationSelectionMessage> multipleObservationSelectionNotifier;
	private Notifier<PeriodAnalysisSelectionMessage> periodAnalysisSelectionNotifier;
	private Notifier<PeriodChangeMessage> periodChangeNotifier;
	private Notifier<PhaseChangeMessage> phaseChangeNotifier;
	private Notifier<PhaseSelectionMessage> phaseSelectionNotifier;
	private Notifier<PeriodAnalysisRefinementMessage> periodAnalysisRefinementNotifier;
	private Notifier<MeanSourceSeriesChangeMessage> meanSourceSeriesChangeNotifier;
	private Notifier<ZoomRequestMessage> zoomRequestNotifier;
	private Notifier<FilteredObservationMessage> filteredObservationNotifier;
	private Notifier<ModelSelectionMessage> modelSelectionNofitier;
	private Notifier<ModelCreationMessage> modelCreationNotifier;
	private Notifier<PanRequestMessage> panRequestNotifier;
	private Notifier<UndoActionMessage> undoActionNotifier;
	private Notifier<StopRequestMessage> stopRequestNotifier;
	private Notifier<SeriesVisibilityChangeMessage> seriesVisibilityChangeNotifier;
	private Notifier<HarmonicSearchResultMessage> harmonicSearchNotifier;
	private Notifier<SeriesCreationMessage> seriesCreationNotifier;

	private DocumentManager documentManager;

	private UndoableActionManager undoableActionManager;

	// Currently active task.
	private SwingWorker currTask;

	// Singleton fields, constructor, getter.

	private static Mediator mediator;

	/**
	 * Private constructor.
	 */
	private Mediator() {
		ui = null;

		this.analysisTypeChangeNotifier = new Notifier<AnalysisTypeChangeMessage>();
		this.newStarNotifier = new Notifier<NewStarMessage>();
		this.progressNotifier = new Notifier<ProgressInfo>();
		this.discrepantObservationNotifier = new Notifier<DiscrepantObservationMessage>();
		this.excludedObservationNotifier = new Notifier<ExcludedObservationMessage>();
		this.observationSelectionNotifier = new Notifier<ObservationSelectionMessage>();
		this.multipleObservationSelectionNotifier = new Notifier<MultipleObservationSelectionMessage>();
		this.periodAnalysisSelectionNotifier = new Notifier<PeriodAnalysisSelectionMessage>();
		this.periodChangeNotifier = new Notifier<PeriodChangeMessage>();
		this.phaseChangeNotifier = new Notifier<PhaseChangeMessage>();
		this.phaseSelectionNotifier = new Notifier<PhaseSelectionMessage>();
		this.periodAnalysisRefinementNotifier = new Notifier<PeriodAnalysisRefinementMessage>();
		this.meanSourceSeriesChangeNotifier = new Notifier<MeanSourceSeriesChangeMessage>();
		this.zoomRequestNotifier = new Notifier<ZoomRequestMessage>();
		this.filteredObservationNotifier = new Notifier<FilteredObservationMessage>();
		this.modelSelectionNofitier = new Notifier<ModelSelectionMessage>();
		this.modelCreationNotifier = new Notifier<ModelCreationMessage>();
		this.panRequestNotifier = new Notifier<PanRequestMessage>();
		this.undoActionNotifier = new Notifier<UndoActionMessage>();
		this.stopRequestNotifier = new Notifier<StopRequestMessage>();
		this.seriesVisibilityChangeNotifier = new Notifier<SeriesVisibilityChangeMessage>();
		this.harmonicSearchNotifier = new Notifier<HarmonicSearchResultMessage>();
		this.seriesCreationNotifier = new Notifier<SeriesCreationMessage>();

		this.obsListFileSaveDialog = new DelimitedFieldFileSaveChooser();
		this.imageSaveDialog = new PNGImageFileSaveChooser();
		this.velaFileLoadDialog = new VeLaFileLoadChooser();
		this.velaFileSaveDialog = new VeLaFileSaveChooser();
		{
			FileNameExtensionFilter[] extensionFilterOpen = new FileNameExtensionFilter[2]; 
			FileNameExtensionFilter[] extensionFilterSave = new FileNameExtensionFilter[1];
			extensionFilterOpen[0] = new FileNameExtensionFilter("VeLa XML files (*.vlx)", "vlx");
			extensionFilterOpen[1] = new FileNameExtensionFilter("VeLa files (*.txt, *.vl, *.vela)", "txt", "vl", "vela");
			extensionFilterSave[0] = extensionFilterOpen[0];
			this.velaXMLchoosers = new FileIOchoosers(extensionFilterOpen, extensionFilterSave, 
					"vlx", "Open VeLa XML File", "Save VeLa XML File As",
					fileIOchooserMode.OPEN_SAVE);
		}

		// These (among other things) are created for each new star.
		this.validObsList = null;
		this.invalidObsList = null;
		this.validObservationCategoryMap = null;
		this.phasedValidObservationCategoryMap = null;
		this.obsAndMeanPlotModel = null;

		this.analysisTypeMap = new HashMap<AnalysisType, AnalysisTypeChangeMessage>();

		this.viewMode = ViewModeType.PLOT_OBS_MODE;
		this.analysisType = AnalysisType.RAW_DATA;

		this.newStarMessageList = new ArrayList<NewStarMessage>();
		this.modelSelectionMessage = null;

		this.periodChangeNotifier.addListener(createPeriodChangeListener());

		this.phaseSelectionNotifier.addListener(createPhaseSelectionListener());

		this.modelSelectionNofitier.addListener(createModelSelectionListener());
		this.filteredObservationNotifier.addListener(createFilteredObservationListener());

		this.seriesCreationNotifier.addListener(createSeriesCreationListener());
	}

	/**
	 * Return the Singleton instance, optionally creating it first
	 */
	public static synchronized Mediator getInstance() {
		if (mediator == null) {
			mediator = new Mediator();
		}
		return mediator;
	}

	/**
	 * @param ui the ui to set
	 */
	public void setUI(IMainUI ui) {
		this.ui = ui;
	}

	public static IMainUI getUI() {
		return ui;
	}

	/**
	 * @return the latest newStarMessage, or null if none present.
	 */
	public NewStarMessage getLatestNewStarMessage() {
		NewStarMessage msg = null;

		if (!newStarMessageList.isEmpty()) {
			msg = newStarMessageList.get(newStarMessageList.size() - 1);
		}

		return msg;
	}

	/**
	 * @return the newStarMessageList
	 */
	public List<NewStarMessage> getNewStarMessageList() {
		return newStarMessageList;
	}

	/**
	 * @return the validObservationCategoryMap
	 */
	public Map<SeriesType, List<ValidObservation>> getValidObservationCategoryMap() {
		return validObservationCategoryMap;
	}

	/**
	 * Given an analysis type, the plot pane for the specified analysis type.
	 * 
	 * @param type The analysis type.
	 * 
	 * @return The plot pane.
	 */
	public ObservationAndMeanPlotPane getPlotPane(AnalysisType type) {
		return analysisTypeMap.get(type).getObsAndMeanChartPane();
	}

	/**
	 * Given an analysis type, the observation list pane for the specified analysis
	 * type.
	 * 
	 * @param type The analysis type.
	 * 
	 * @return The observation list pane.
	 */
	public ObservationListPane getObservationListPane(AnalysisType type) {
		return analysisTypeMap.get(type).getObsListPane();
	}

	/**
	 * @return the modelSelectionMessage
	 */
	public ModelSelectionMessage getModelSelectionMessage() {
		return modelSelectionMessage;
	}

	/**
	 * @return the analysisTypeChangeNotifier
	 */
	public Notifier<AnalysisTypeChangeMessage> getAnalysisTypeChangeNotifier() {
		return analysisTypeChangeNotifier;
	}

	/**
	 * @param analysisType the analysisType to set
	 */
	public void setAnalysisType(AnalysisType analysisType) {
		this.analysisType = analysisType;
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
	 * @return the multipleObservationSelectionNotifier
	 */
	public Notifier<MultipleObservationSelectionMessage> getMultipleObservationSelectionNotifier() {
		return multipleObservationSelectionNotifier;
	}

	/**
	 * @return the periodAnalysisSelectionNotifier
	 */
	public Notifier<PeriodAnalysisSelectionMessage> getPeriodAnalysisSelectionNotifier() {
		return periodAnalysisSelectionNotifier;
	}

	/**
	 * @return the periodChangeNotifier
	 */
	public Notifier<PeriodChangeMessage> getPeriodChangeNotifier() {
		return periodChangeNotifier;
	}

	/**
	 * @return the phaseChangeNotifier
	 */
	public Notifier<PhaseChangeMessage> getPhaseChangeNotifier() {
		return phaseChangeNotifier;
	}

	/**
	 * @return the phaseSelectionNotifier
	 */
	public Notifier<PhaseSelectionMessage> getPhaseSelectionNotifier() {
		return phaseSelectionNotifier;
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
	 * @return the modelCreationNotifier
	 */
	public Notifier<ModelCreationMessage> getModelCreationNotifier() {
		return modelCreationNotifier;
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
	 * @return the stopRequestNotifier
	 */
	public Notifier<StopRequestMessage> getStopRequestNotifier() {
		return stopRequestNotifier;
	}

	/**
	 * @return the seriesVisibilityChangeNotifier
	 */
	public Notifier<SeriesVisibilityChangeMessage> getSeriesVisibilityChangeNotifier() {
		return seriesVisibilityChangeNotifier;
	}

	/**
	 * @return the harmonicSearchNotifier
	 */
	public Notifier<HarmonicSearchResultMessage> getHarmonicSearchNotifier() {
		return harmonicSearchNotifier;
	}

	/**
	 * @return the seriesCreationNotifier
	 */
	public Notifier<SeriesCreationMessage> getSeriesCreationNotifier() {
		return seriesCreationNotifier;
	}

	/**
	 * @return the velaFileLoadDialog
	 */
	public VeLaFileLoadChooser getVelaFileLoadDialog() {
		return velaFileLoadDialog;
	}

	/**
	 * @return the velaFileSaveDialog
	 */
	public VeLaFileSaveChooser getVelaFileSaveDialog() {
		return velaFileSaveDialog;
	}
	
	/**
	 * @return the velaXMLchoosers
	 */
	public FileIOchoosers getVelaXMLchoosers() {
		return velaXMLchoosers;
	}

	/**
	 * Create a mean observation change listener and return it. Whenever the mean
	 * series source changes, listeners may want to perform a new period analysis or
	 * change the max time increments for means binning.
	 */
	private Listener<BinningResult> createMeanObsChangeListener(int initialSeriesNum) {
		final int initialSeriesNumFinal = initialSeriesNum;

		return new Listener<BinningResult>() {
			private int meanSourceSeriesNum = initialSeriesNumFinal;

			public boolean canBeRemoved() {
				return false;
			}

			public void update(BinningResult info) {
				// TODO: would removing this guard permit listeners
				// to do other things, e.g. compare old and new binning results,
				// e.g. for change to days-in-bin?
				if (this.meanSourceSeriesNum != obsAndMeanPlotModel.getMeanSourceSeriesNum()) {

					this.meanSourceSeriesNum = obsAndMeanPlotModel.getMeanSourceSeriesNum();

					SeriesType meanSourceSeriesType = obsAndMeanPlotModel.getSeriesNumToSrcTypeMap()
							.get(this.meanSourceSeriesNum);

					meanSourceSeriesChangeNotifier
							.notifyListeners(new MeanSourceSeriesChangeMessage(this, meanSourceSeriesType));
				}
			}
		};
	}

	// When the period changes, create a new phase plot passing the pre-existing
	// series visibility map if a previous phase plot was created.
	//
	// TODO: actually, it should only be necessary to a. set the phases with the
	// new period and epoch (need to include the epoch in the message), and b.
	// update the plot and table models.
	private Listener<PeriodChangeMessage> createPeriodChangeListener() {
		return new Listener<PeriodChangeMessage>() {
			public void update(PeriodChangeMessage info) {
				PhaseParameterDialog phaseDialog = getPhaseParameterDialog();
				phaseDialog.setPeriodField(info.getPeriod());
				phaseDialog.showDialog();

				if (!phaseDialog.isCancelled()) {
					double period = phaseDialog.getPeriod();
					double epoch = phaseDialog.getEpoch();

					AnalysisTypeChangeMessage lastPhasePlotMsg = analysisTypeMap.get(AnalysisType.PHASE_PLOT);

					Map<SeriesType, Boolean> seriesVisibilityMap = null;

					if (lastPhasePlotMsg != null) {
						// Use the last phase plot's series visibility map.
						seriesVisibilityMap = lastPhasePlotMsg.getObsAndMeanChartPane().getObsModel()
								.getSeriesVisibilityMap();
					} else {
						// There has been no phase plot yet, so use the
						// light curve's series visibility map.
						AnalysisTypeChangeMessage lightCurveMsg = analysisTypeMap.get(AnalysisType.RAW_DATA);
						seriesVisibilityMap = lightCurveMsg.getObsAndMeanChartPane().getObsModel()
								.getSeriesVisibilityMap();
					}

					performPhasePlot(period, epoch, seriesVisibilityMap);
				}
			}

			public boolean canBeRemoved() {
				return false;
			}
		};
	}

	// Returns a phase selection message listener, the purpose of which is to
	// recreate a previous phase plot.
	protected Listener<PhaseSelectionMessage> createPhaseSelectionListener() {
		final Mediator me = this;
		return new Listener<PhaseSelectionMessage>() {
			@Override
			public void update(PhaseSelectionMessage info) {
				if (info.getSource() != me) {
					performPhasePlot(info.getPeriod(), info.getEpoch(), info.getSeriesVisibilityMap());
				}
			}

			@Override
			public boolean canBeRemoved() {
				return false;
			}
		};
	}

	
	/**
	 * Removes phase plot if exists.
	 * 
	 */
	public void dropPhasePlotAnalysis() {
		assert(analysisType != AnalysisType.PHASE_PLOT);
		analysisTypeMap.remove(AnalysisType.PHASE_PLOT);
	}
	
	/**
	 * Create a phase plot, first asking for period and epoch.
	 * 
	 * The series visibility map for the phase plot is taken from the currently
	 * visible plot (raw data or phase plot).
	 */
	public void createPhasePlot() {
		PhaseParameterDialog phaseDialog = getPhaseParameterDialog();
		phaseDialog.showDialog();
		if (!phaseDialog.isCancelled()) {
			double period = phaseDialog.getPeriod();
			double epoch = phaseDialog.getEpoch();

			Map<SeriesType, Boolean> seriesVisibilityMap = analysisTypeMap.get(analysisType).getObsAndMeanChartPane()
					.getObsModel().getSeriesVisibilityMap();

			performPhasePlot(period, epoch, seriesVisibilityMap);
		}
	}

	/**
	 * Create a phase plot, given the period and epoch.
	 * 
	 * The series visibility map for the phase plot is taken from the currently
	 * visible plot (raw data or phase plot).
	 * 
	 * @param period The requested period of the phase plot.
	 * @param epoch  The epoch (first Julian Date) for the phase plot.
	 */
	public void createPhasePlot(double period, double epoch) {
		Map<SeriesType, Boolean> seriesVisibilityMap = analysisTypeMap.get(analysisType).getObsAndMeanChartPane()
				.getObsModel().getSeriesVisibilityMap();

		performPhasePlot(period, epoch, seriesVisibilityMap);
	}

	/**
	 * Common phase plot handler.
	 * 
	 * @param period              The requested period of the phase plot.
	 * @param epoch               The epoch (first Julian Date) for the phase plot.
	 * @param seriesVisibilityMap A mapping from series number to visibility status.
	 */
	public void performPhasePlot(double period, double epoch, Map<SeriesType, Boolean> seriesVisibilityMap) {

		PhasePlotTask task = new PhasePlotTask(period, epoch, seriesVisibilityMap);

		try {
			currTask = task;
			task.execute();
		} catch (Exception e) {
			Mediator.getUI().setCursor(null);
			MessageBox.showErrorDialog(Mediator.getUI().getComponent(), "New Phase Plot", e);
		}
	}

	/**
	 * Perform plugin manager operation.
	 */
	public void performPluginManagerOperation(PluginManagementOperation op) {
		PluginManagerOperationTask task = new PluginManagerOperationTask(op);

		try {
			currTask = task;
			task.execute();
		} catch (Exception e) {
			Mediator.getUI().setCursor(null);
			MessageBox.showErrorDialog(Mediator.getUI().getComponent(), "Plugin Manager", e.getLocalizedMessage());
		}
	}

	// ************************************************************************
	// The following listener methods ensure that the obs category map is kept
	// up to date with models, filters, new series. We handle means separately,
	// although it would be more consistent if we did not. For example, perhaps
	// we should just reconstruct this map each time it is required, from the
	// obs model.
	// ************************************************************************

	// Returns a model selection listener that updates the observation
	// category map with model and residuals series.
	private Listener<ModelSelectionMessage> createModelSelectionListener() {
		return new Listener<ModelSelectionMessage>() {
			@Override
			public void update(ModelSelectionMessage info) {
				validObservationCategoryMap.put(SeriesType.Model, info.getModel().getFit());

				validObservationCategoryMap.put(SeriesType.Residuals, info.getModel().getResiduals());

				modelSelectionMessage = info;
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

	// Returns a series creation listener that updates the observation
	// category map with the series.
	protected Listener<SeriesCreationMessage> createSeriesCreationListener() {
		return new Listener<SeriesCreationMessage>() {
			@Override
			public void update(SeriesCreationMessage info) {
				validObservationCategoryMap.put(info.getType(), info.getObs());
			}

			@Override
			public boolean canBeRemoved() {
				return false;
			}
		};
	}

	/**
	 * Change the mode of VStar's focus (i.e what is to be presented to the user).
	 * 
	 * @param viewMode The mode to change to.
	 */
	public void changeViewMode(ViewModeType viewMode) {
		if (viewMode != this.viewMode) {
			this.viewMode = viewMode;
		}
	}

	public ViewModeType getViewMode() {
		return viewMode;
	}

	// Dialog singleton getters

	public PhaseParameterDialog getPhaseParameterDialog() {
		if (phaseParameterDialog == null) {
			phaseParameterDialog = new PhaseParameterDialog();
			newStarNotifier.addListener(phaseParameterDialog, true);
		}

		return phaseParameterDialog;
	}

	public ObservationFilterDialog getObsFilterDialog() {
		if (obsFilterDialog == null) {
			obsFilterDialog = new ObservationFilterDialog();
			newStarNotifier.addListener(obsFilterDialog.createNewStarListener(), true);
			observationSelectionNotifier.addListener(obsFilterDialog.createObservationSelectionListener(), true);
		}

		return obsFilterDialog;
	}

	public ModelDialog getModelDialog() {
		if (modelDialog == null) {
			modelDialog = new ModelDialog();
			newStarNotifier.addListener(modelDialog.createNewStarListener(), true);
			modelCreationNotifier.addListener(modelDialog.createModelCreationListener(), true);
		}

		return modelDialog;
	}

	public PhaseDialog getPhaseDialog() {
		if (phaseDialog == null) {
			phaseDialog = new PhaseDialog();
			newStarNotifier.addListener(phaseDialog.createNewStarListener(), true);
			phaseChangeNotifier.addListener(phaseDialog.createPhaseChangeListener(), true);
		}

		return phaseDialog;
	}

	public ObservationFiltersDialog getObservationFiltersDialog() {
		if (observationFiltersDialog == null) {
			observationFiltersDialog = new ObservationFiltersDialog();
			newStarNotifier.addListener(observationFiltersDialog.createNewStarListener(), true);
			filteredObservationNotifier.addListener(observationFiltersDialog.createFilterListener(), true);
		}

		return observationFiltersDialog;
	}

	public DocumentManager getDocumentManager() {
		if (documentManager == null) {
			documentManager = new DocumentManager();
			phaseChangeNotifier.addListener(documentManager.createPhaseChangeListener(), true);
			newStarNotifier.addListener(documentManager.createNewStarListener(), true);
		}

		return documentManager;
	}

	public UndoableActionManager getUndoableActionManager() {
		if (undoableActionManager == null) {
			undoableActionManager = new UndoableActionManager();
			newStarNotifier.addListener(undoableActionManager.createNewStarListener(), true);
			observationSelectionNotifier.addListener(undoableActionManager.createObservationSelectionListener(), true);
			multipleObservationSelectionNotifier
					.addListener(undoableActionManager.createMultipleObservationSelectionListener(), true);
		}

		return undoableActionManager;
	}

	/**
	 * Get the object that has information about available series and observations
	 * pertaining thereto.
	 * 
	 * @return The series information provider.
	 */
	public ISeriesInfoProvider getSeriesInfoProvider() {
		return analysisTypeMap.get(AnalysisType.RAW_DATA).getObsAndMeanChartPane().getObsModel();
	}

	/**
	 * Get the observation plot model for the specified analysis type.
	 * 
	 * @return The observation plot model.
	 */
	public ObservationAndMeanPlotModel getObservationPlotModel(AnalysisType type) {
		return analysisTypeMap.get(type).getObsAndMeanChartPane().getObsModel();
	}

	/**
	 * Change the analysis type. If the old and new types are the same, there will
	 * be no effect.
	 * 
	 * @param analysisType The analysis type to change to.
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
								+ this.getLatestNewStarMessage().getStarInfo().getDesignation() + ")";
						Mediator.getUI().getStatusPane().setMessage(statusMsg);
					}
					break;

				case PHASE_PLOT:
					// Create or retrieve phase plots and data tables passing
					// the light curve's series visibility map for the first
					// phase plot.
					msg = this.analysisTypeMap.get(AnalysisType.PHASE_PLOT);

					if (msg == null) {
						createPhasePlot();
					} else {
						// Change to the existing phase plot.
						this.analysisType = analysisType;
						this.analysisTypeChangeNotifier.notifyListeners(msg);
						setPhasePlotStatusMessage();
					}
					break;
				}
			} catch (Exception e) {
				MessageBox.showErrorDialog(Mediator.getUI().getComponent(), "Analysis Type Change", e);
			}
		}

		return this.analysisType;
	}

	/**
	 * Set the status bar to display phase plot information.
	 */
	public void setPhasePlotStatusMessage() {
		String statusMsg = "Phase plot mode (" + this.getLatestNewStarMessage().getStarInfo().getDesignation() + ")";
		Mediator.getUI().getStatusPane().setMessage(statusMsg);
	}

	/**
	 * @return the analysisType
	 */
	public AnalysisType getAnalysisType() {
		return analysisType;
	}

	/**
	 * Remove messages and all listeners that are willing, from all notifiers, to
	 * ensure that no old, unnecessary listeners/messages remain from one new-star
	 * load to another. Such listeners could receive notifications that make no
	 * sense (e.g. location of an observation within a dataset) and guard against
	 * memory leaks.
	 */
	private void freeListeners() {
		analysisTypeChangeNotifier.cleanup();
		newStarNotifier.cleanup();
		progressNotifier.cleanup();
		discrepantObservationNotifier.cleanup();
		excludedObservationNotifier.cleanup();
		observationSelectionNotifier.cleanup();
		multipleObservationSelectionNotifier.cleanup();
		periodAnalysisSelectionNotifier.cleanup();
		periodChangeNotifier.cleanup();
		phaseChangeNotifier.cleanup();
		phaseSelectionNotifier.cleanup();
		periodAnalysisRefinementNotifier.cleanup();
		meanSourceSeriesChangeNotifier.cleanup();
		zoomRequestNotifier.cleanup();
		filteredObservationNotifier.cleanup();
		modelSelectionNofitier.cleanup();
		modelCreationNotifier.cleanup();
		panRequestNotifier.cleanup();
		undoActionNotifier.cleanup();
		stopRequestNotifier.cleanup();
		seriesVisibilityChangeNotifier.cleanup();
		harmonicSearchNotifier.cleanup();
		observationSelectionNotifier.cleanup();
	}

	/**
	 * Creates and executes a background task to handle
	 * new-star-from-external-source-plugin.
	 * 
	 * @param obSourcePlugin The plugin that will be used to obtain observations.
	 */
	public void createObservationArtefactsFromObSourcePlugin(ObservationSourcePluginBase obSourcePlugin) {

		this.getProgressNotifier().notifyListeners(ProgressInfo.START_PROGRESS);
		this.getProgressNotifier().notifyListeners(ProgressInfo.BUSY_PROGRESS);

		NewStarFromObSourcePluginTask task = new NewStarFromObSourcePluginTask(obSourcePlugin);
		this.currTask = task;
		task.configure();
		if (task.isConfigured()) {
			task.execute();
		}
		task.done();
	}

	/**
	 * Creates and executes a background task to handle
	 * new-star-from-external-source-plugin when a file is supplied.
	 * 
	 * @param obSourcePlugin The plugin that will be used to obtain observations.
	 * @param file           The file to used as input.
	 * @param isAdditive     Is this an additive load?
	 */
	public void createObservationArtefactsFromObSourcePlugin(ObservationSourcePluginBase obSourcePlugin, File file,
			boolean isAdditive) throws IOException, ObservationReadError {

		this.getProgressNotifier().notifyListeners(ProgressInfo.START_PROGRESS);
		this.getProgressNotifier().notifyListeners(ProgressInfo.BUSY_PROGRESS);

		NewStarFromObSourcePluginWithSuppliedFileTask task = new NewStarFromObSourcePluginWithSuppliedFileTask(
				obSourcePlugin, file, isAdditive);
		this.currTask = task;
		task.execute();
	}

	/**
	 * Creates and executes a background task to handle
	 * new-star-from-external-source-plugin when a URL is supplied.
	 * 
	 * @param obSourcePlugin The plugin that will be used to obtain observations.
	 * @param url            The URL to used as input.
	 * @param isAdditive     Is this an additive load?
	 */
	public void createObservationArtefactsFromObSourcePlugin(ObservationSourcePluginBase obSourcePlugin, URL url,
			boolean isAdditive) throws IOException, ObservationReadError {

		this.getProgressNotifier().notifyListeners(ProgressInfo.START_PROGRESS);
		this.getProgressNotifier().notifyListeners(ProgressInfo.BUSY_PROGRESS);

		NewStarFromObSourcePluginWithSuppliedURLTask task = new NewStarFromObSourcePluginWithSuppliedURLTask(
				obSourcePlugin, url, isAdditive);
		this.currTask = task;
		task.execute();
	}

	/**
	 * Create observation artefacts (models, GUI elements) on the assumption that a
	 * valid observation list and category map have already been created.
	 * 
	 * @param newStarType               The new star enum type.
	 * @param starInfo                  Information about the star, e.g. name
	 *                                  (designation), AUID (for AID), period,
	 *                                  epoch, including observation retriever.
	 * @param obsArtefactProgressAmount The amount the progress bar should be
	 *                                  incremented by, a value corresponding to a
	 *                                  portion of the overall task of which this is
	 *                                  just a part.
	 * @param addObs                    Should the observations be added to the
	 *                                  existing loaded dataset?
	 */
	public void createNewStarObservationArtefacts(NewStarType newStarType, StarInfo starInfo,
			int obsArtefactProgressAmount, boolean addObs) throws ObservationReadError {

		// Given raw valid and invalid observation data, create observation
		// table and plot models, along with corresponding GUI components.

		// Handle additive load if requested and observations are already
		// loaded.
		if (addObs && getLatestNewStarMessage() != null) {
			// convertObsToHJD(starInfo);

			starInfo.getRetriever().collectAllObservations(validObsList, starInfo.getRetriever().getSourceName());

			starInfo.getRetriever().addAllInvalidObservations(invalidObsList);

			// If any loaded data source type is different from the current data
			// source type, use arbitrary data source type that accommodates any
			// data source type. We should change this so that all data sources
			// have the same type!
			for (NewStarMessage msg : getNewStarMessageList()) {
				if (msg.getNewStarType() != newStarType) {
					newStarType = NewStarType.NEW_STAR_FROM_ARBITRARY_SOURCE;
					break;
				}
			}
		}

		List<ValidObservation> validObsList = starInfo.getRetriever().getValidObservations();

		List<InvalidObservation> invalidObsList = starInfo.getRetriever().getInvalidObservations();

		Map<SeriesType, List<ValidObservation>> validObservationCategoryMap = starInfo.getRetriever()
				.getValidObservationCategoryMap();

		// Table models.
		validObsTableModel = null;
		InvalidObservationTableModel invalidObsTableModel = null;
		RawDataMeanObservationTableModel meanObsTableModel = null;

		// Plot models.
		obsAndMeanPlotModel = null;

		// GUI table and chart components.
		ObservationListPane obsListPane = null;
		SyntheticObservationListPane<AbstractMeanObservationTableModel> meansListPane = null;
		ObservationAndMeanPlotPane obsAndMeanChartPane = null;

		if (!validObsList.isEmpty()) {

			freeListeners();

			// Create a message to notify whoever is listening that a new star
			// has been loaded.
			NewStarMessage newStarMsg = new NewStarMessage(newStarType, starInfo, validObsList,
					validObservationCategoryMap, starInfo.getRetriever().getMinMag(),
					starInfo.getRetriever().getMaxMag(), starInfo.getRetriever().getSourceName());

			if (!addObs) {
				newStarMessageList.clear();
			} else {
				// Exclude all but the most recent new star message if the newly
				// loaded dataset's series set is the same as that of any
				// previously loaded dataset.
				Set<SeriesType> newSeriesTypes = validObservationCategoryMap.keySet();

				List<NewStarMessage> dupMessages = new ArrayList<NewStarMessage>();

				for (NewStarMessage msg : newStarMessageList) {
					if (newSeriesTypes.equals(msg.getObsCategoryMap().keySet())) {
						dupMessages.add(msg);
					}
				}

				for (NewStarMessage msg : dupMessages) {
					newStarMessageList.remove(msg);
				}
			}

			newStarMessageList.add(newStarMsg);

			// This is a specific fix for tracker 3007948.
			this.discrepantObservationNotifier = new Notifier<DiscrepantObservationMessage>();

			// Observation table and plot.
			validObsTableModel = new ValidObservationTableModel(validObsList,
					newStarType.getRawDataTableColumnInfoSource());

			// Observation-and-mean table and plot.
			obsAndMeanPlotModel = new ObservationAndMeanPlotModel(validObservationCategoryMap, JDCoordSource.instance,
					JDComparator.instance, JDTimeElementEntity.instance, null);

			if (false) {
				// Record initial ANOVA information and make the document
				// manager
				// listen to changes to ANOVA via new binning results.
				getDocumentManager().updateAnovaInfo(obsAndMeanPlotModel.getBinningResult());
			}

			obsAndMeanPlotModel.getMeansChangeNotifier().addListener(getDocumentManager().createBinChangeListener());

			getDocumentManager().addStatsInfo("Confidence Interval",
					"Mean error bars denote 95% Confidence Interval (twice Standard Error)");

			obsAndMeanChartPane = createObservationAndMeanPlotPane(
					LocaleProps.get("LIGHT_CURVE") + " " + LocaleProps.get("FOR") + " " + starInfo.getDesignation(),
					null, obsAndMeanPlotModel, starInfo.getRetriever());

			obsAndMeanPlotModel.getMeansChangeNotifier()
					.addListener(createMeanObsChangeListener(obsAndMeanPlotModel.getMeanSourceSeriesNum()));

			// The mean observation table model must listen to the plot
			// model to know when the means data has changed. We also pass
			// the initial means data obtained from the plot model to
			// the mean observation table model.
			meanObsTableModel = new RawDataMeanObservationTableModel(obsAndMeanPlotModel.getMeanObsList());

			obsAndMeanPlotModel.getMeansChangeNotifier().addListener(meanObsTableModel);

			if (obsArtefactProgressAmount > 0) {
				// Update progress.
				getProgressNotifier()
						.notifyListeners(new ProgressInfo(ProgressType.INCREMENT_PROGRESS, obsArtefactProgressAmount));
			}
		}

		if (!invalidObsList.isEmpty()) {
			invalidObsTableModel = new InvalidObservationTableModel(invalidObsList);
		}

		// The observation table pane contains valid and potentially
		// invalid data components. Tell the valid data table to have
		// a horizontal scrollbar if there will be too many columns.

		boolean enableColumnAutoResize = newStarType == NewStarType.NEW_STAR_FROM_SIMPLE_FILE
				|| (newStarType == NewStarType.NEW_STAR_FROM_ARBITRARY_SOURCE && !addObs);

		obsListPane = new ObservationListPane(starInfo.getDesignation(), validObsTableModel, invalidObsTableModel,
				enableColumnAutoResize, obsAndMeanPlotModel.getVisibleSeries(), AnalysisType.RAW_DATA);

		// We also create the means list pane.
		meansListPane = new SyntheticObservationListPane<AbstractMeanObservationTableModel>(meanObsTableModel, null);

		// Create a message to notify whoever is listening that the analysis
		// type has changed (we could have been viewing a phase plot for a
		// different star before now) passing GUI components in the message.
		analysisType = AnalysisType.RAW_DATA;

		AnalysisTypeChangeMessage analysisTypeMsg = new AnalysisTypeChangeMessage(analysisType, obsAndMeanChartPane,
				obsListPane, meansListPane, ViewModeType.PLOT_OBS_MODE);

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
		newStarNotifier.notifyListeners(getLatestNewStarMessage());
		analysisTypeChangeNotifier.notifyListeners(analysisTypeMsg);
	}

	// Request the J2000.0 RA in HH:MM:SS.n
	public RAInfo requestRA(RAInfo ra) {
		Integer h = null;
		Integer m = null;
		Double s = null;

		if (ra != null) {
			Triple<Integer, Integer, Double> hms = ra.toHMS();
			h = hms.first;
			m = hms.second;
			s = hms.third;
		}

		IntegerField raHours = new IntegerField("Hours", 0, 23, h);
		IntegerField raMinutes = new IntegerField("Minutes", 0, 59, m);
		DoubleField raSeconds = new DoubleField("Seconds", 0.0, 59.99, s);
		MultiEntryComponentDialog dialog = new MultiEntryComponentDialog("RA (" + EpochType.J2000 + ")", raHours,
				raMinutes, raSeconds);

		RAInfo raInfo = null;
		if (!dialog.isCancelled()) {
			raInfo = new RAInfo(EpochType.J2000, raHours.getValue(), raMinutes.getValue(), raSeconds.getValue());
		}

		return raInfo;
	}

	// Request the J2000.0 RA in HH:MM:SS.n
	public RAInfo requestRA() {
		return requestRA(null);
	}

	// Request the J2000.0 Dec in DD:MM:SS.n
	public DecInfo requestDec(DecInfo dec) {
		Integer d = null;
		Integer m = null;
		Double s = null;

		if (dec != null) {
			Triple<Integer, Integer, Double> dms = dec.toDMS();
			d = dms.first;
			m = dms.second;
			s = dms.third;
		}

		IntegerField decDegrees = new IntegerField("Degrees", -90, 90, d);
		IntegerField decMinutes = new IntegerField("Minutes", 0, 59, m);
		DoubleField decSeconds = new DoubleField("Seconds", 0.0, 59.99, s);

		DecInfo decInfo;		
		while (true) {
			decInfo = null;			
			MultiEntryComponentDialog dialog = new MultiEntryComponentDialog("Dec (" + EpochType.J2000 + ")", decDegrees,
					decMinutes, decSeconds);
			if (dialog.isCancelled())
				break;
			decInfo = new DecInfo(EpochType.J2000, decDegrees.getValue(), decMinutes.getValue(), decSeconds.getValue());
			
			double degrees = decInfo.toDegrees();

			// If Degrees = 90 and Min or Sec > 0, the resulted value is out of range.
			if (degrees >= -90.0 && degrees <= 90.0)
				break;

			MessageBox.showErrorDialog(Mediator.getUI().getComponent(), 
					"Error", "Please check input: Dec must be between -90.0 and 90.0");
		}

		return decInfo;
	}

	// Request the J2000.0 Dec in DD:MM:SS.n
	public DecInfo requestDec() {
		return requestDec(null);
	}

	/**
	 * Convert the specified observations to use HJD (if not already) rather than
	 * JD.
	 * 
	 * @param obs The list of observations to be converted.
	 * @param ra  The RA for the object.
	 * @param dec The Dec for the object.
	 * @return The number of observations converted.
	 */
	public int convertObsToHJD(List<ValidObservation> obs, RAInfo ra, DecInfo dec) {
		int count = 0;

		AbstractHJDConverter converter = AbstractHJDConverter.getInstance(ra.getEpoch());

		for (ValidObservation ob : obs) {
			if (ob.getJDflavour() == JDflavour.JD) {
				ob.setJD(converter.convert(ob.getJD(), ra, dec));
				ob.setJDflavour(JDflavour.HJD);
				count++;
			}
		}

		return count;
	}

	/**
	 * Create phase plot artefacts, adding them to the analysis type map and
	 * returning this message.
	 * 
	 * @param period              The requested period of the phase plot.
	 * @param epoch               The epoch (first Julian Date) for the phase plot.
	 * @param seriesVisibilityMap A mapping from series number to visibility status.
	 * @return An analysis type message consisting of phase plot artefacts.
	 */
	public AnalysisTypeChangeMessage createPhasePlotArtefacts(double period, double epoch,
			Map<SeriesType, Boolean> seriesVisibilityMap) throws Exception {
		String objName = getLatestNewStarMessage().getStarInfo().getDesignation();

		String subTitle = "";
		String periodAndEpochStr = String.format(
				LocaleProps.get("PERIOD") + ": %s, " + LocaleProps.get("EPOCH") + ": %s",
				NumericPrecisionPrefs.formatOther(period), NumericPrecisionPrefs.formatTime(epoch));

		if (this.getLatestNewStarMessage().getNewStarType() == NewStarType.NEW_STAR_FROM_DATABASE) {
			Date now = Calendar.getInstance().getTime();
			String formattedDate = DateFormat.getDateInstance().format(now);
			subTitle = formattedDate + " (" + LocaleProps.get("DATABASE") + "), " + periodAndEpochStr;
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
		// so that it can vary from the main plot's over time.
		// TODO: but is it ever mutated in the plot models? is it enough to
		// duplicate and sort means?
		Map<SeriesType, List<ValidObservation>> phasedValidObservationCategoryMap = new TreeMap<SeriesType, List<ValidObservation>>();

		for (SeriesType series : validObservationCategoryMap.keySet()) {
			List<ValidObservation> obs = validObservationCategoryMap.get(series);

			List<ValidObservation> phasedObs = new ArrayList<ValidObservation>(obs);

			Collections.sort(phasedObs, StandardPhaseComparator.instance);

			phasedValidObservationCategoryMap.put(series, phasedObs);
		}

		// TODO:
		// o fix occurrences of obs doubling and just copy and sort
		// o indeed: is this needed now anyway? see plot model/pane code

		// Table and plot models.
		ValidObservationTableModel validObsTableModel = new ValidObservationTableModel(validObsList,
				getLatestNewStarMessage().getNewStarType().getPhasePlotTableColumnInfoSource());

		// Observation-and-mean plot and table.
		ContinuousModelFunction rawModelFuncModel = obsAndMeanPlotModel.getModelFunction();

		ContinuousModelFunction prevCyclePhaseModelFuncModel = null;
		ContinuousModelFunction stdPhaseModelFuncModel = null;
		int modelFuncSeriesNum = ObservationAndMeanPlotModel.NO_SERIES;

		if (rawModelFuncModel != null) {
			// Use sorted fit from category map; this will also be compatible,
			// order-wise, with previous cycle phase.
			List<ValidObservation> phasedFit = phasedValidObservationCategoryMap.get(SeriesType.Model);

			prevCyclePhaseModelFuncModel = new ContinuousModelFunction(rawModelFuncModel.getFunction(), phasedFit,
					rawModelFuncModel.getZeroPoint(), PreviousCyclePhaseCoordSource.instance);

			stdPhaseModelFuncModel = new ContinuousModelFunction(rawModelFuncModel.getFunction(), phasedFit,
					rawModelFuncModel.getZeroPoint(), StandardPhaseCoordSource.instance);

			modelFuncSeriesNum = obsAndMeanPlotModel.getModelFunctionSeriesNum();
		}

		PhasedObservationAndMeanPlotModel obsAndMeanPlotModel1 = new PhasedObservationAndMeanPlotModel(
				phasedValidObservationCategoryMap, PreviousCyclePhaseCoordSource.instance,
				PreviousCyclePhaseComparator.instance, PhaseTimeElementEntity.instance, seriesVisibilityMap,
				prevCyclePhaseModelFuncModel, modelFuncSeriesNum);

		if (prevCyclePhaseModelFuncModel != null) {
			prevCyclePhaseModelFuncModel.setPpModel(obsAndMeanPlotModel1);
		}

		PhasedObservationAndMeanPlotModel obsAndMeanPlotModel2 = new PhasedObservationAndMeanPlotModel(
				phasedValidObservationCategoryMap, StandardPhaseCoordSource.instance, StandardPhaseComparator.instance,
				PhaseTimeElementEntity.instance, seriesVisibilityMap, stdPhaseModelFuncModel, modelFuncSeriesNum);

		if (stdPhaseModelFuncModel != null) {
			stdPhaseModelFuncModel.setPpModel(obsAndMeanPlotModel2);
		}

		// Select an arbitrary model for mean.
		obsAndMeanPlotModel = obsAndMeanPlotModel1;

		// The mean observation table model must listen to the plot
		// model to know when the means data has changed. We also pass
		// the initial means data obtained from the plot model to
		// the mean observation table model.
		PhasePlotMeanObservationTableModel meanObsTableModel = new PhasePlotMeanObservationTableModel(
				obsAndMeanPlotModel1.getMeanObsList());

		obsAndMeanPlotModel1.getMeansChangeNotifier().addListener(meanObsTableModel);

		obsAndMeanPlotModel2.getMeansChangeNotifier().addListener(meanObsTableModel);

		PhaseAndMeanPlotPane obsAndMeanChartPane = createPhaseAndMeanPlotPane(
				LocaleProps.get("PHASE_PLOT") + " " + LocaleProps.get("FOR") + " " + objName, subTitle,
				obsAndMeanPlotModel1, obsAndMeanPlotModel2, epoch, period,
				getLatestNewStarMessage().getStarInfo().getRetriever());

		// The observation table pane contains valid and potentially
		// invalid data components but for phase plot purposes, we only
		// display valid data, as opposed to the raw data view in which
		// both are shown. Tell the valid data table to have a horizontal
		// scrollbar if there will be too many columns.
		boolean enableColumnAutoResize = getLatestNewStarMessage()
				.getNewStarType() == NewStarType.NEW_STAR_FROM_SIMPLE_FILE
				|| getLatestNewStarMessage().getNewStarType() == NewStarType.NEW_STAR_FROM_ARBITRARY_SOURCE;

		ObservationListPane obsListPane = new ObservationListPane(objName, validObsTableModel, null,
				enableColumnAutoResize, obsAndMeanPlotModel1.getVisibleSeries(), AnalysisType.PHASE_PLOT);

		SyntheticObservationListPane<AbstractMeanObservationTableModel> meansListPane = new SyntheticObservationListPane<AbstractMeanObservationTableModel>(
				meanObsTableModel, null);

		// Create a phase change message so that existing plot and tables can
		// update their GUI components and/or models accordingly. Also,
		// recording the series visibility map permits the existence of a phase
		// change creation listener that collects phase change messages for the
		// purpose of later being able to re-create the same phase plot.
		PhaseChangeMessage phaseChangeMessage = new PhaseChangeMessage(this, period, epoch, seriesVisibilityMap);
		phaseChangeNotifier.notifyListeners(phaseChangeMessage);

		// Observation-and-mean table and plot.
		AnalysisTypeChangeMessage phasePlotMsg = new AnalysisTypeChangeMessage(AnalysisType.PHASE_PLOT,
				obsAndMeanChartPane, obsListPane, meansListPane, ViewModeType.PLOT_OBS_MODE);

		analysisTypeMap.put(AnalysisType.PHASE_PLOT, phasePlotMsg);

		analysisTypeChangeNotifier.notifyListeners(phasePlotMsg);

		return phasePlotMsg;
	}

	/**
	 * Set the phases for a particular series in the observation category map.
	 * 
	 * @param type   The series type of the observations whose phases are to be set.
	 * @param epoch  The epoch to use for the phase calculation.
	 * @param period The period to use for the phase calculation.
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
	private ObservationAndMeanPlotPane createObservationAndMeanPlotPane(String plotName, String subTitle,
			ObservationAndMeanPlotModel obsAndMeanPlotModel, AbstractObservationRetriever retriever) {

		Dimension bounds = new Dimension((int) (TabbedDataPane.WIDTH * 0.9), (int) (TabbedDataPane.HEIGHT * 0.9));

		return new ObservationAndMeanPlotPane(plotName, subTitle, obsAndMeanPlotModel, bounds, retriever);
	}

	/**
	 * Create the observation-and-mean phase plot pane for the current list of valid
	 * observations.
	 */
	private PhaseAndMeanPlotPane createPhaseAndMeanPlotPane(String plotName, String subTitle,
			PhasedObservationAndMeanPlotModel obsAndMeanPlotModel1,
			PhasedObservationAndMeanPlotModel obsAndMeanPlotModel2, double epoch, double period,
			AbstractObservationRetriever retriever) {

		Dimension bounds = new Dimension((int) (TabbedDataPane.WIDTH * 0.9), (int) (TabbedDataPane.HEIGHT * 0.9));

		return new PhaseAndMeanPlotPane(plotName, subTitle, bounds, epoch, period, retriever, obsAndMeanPlotModel1,
				obsAndMeanPlotModel2);
	}

	/**
	 * Create a period analysis dialog after the analysis is done. It only makes
	 * sense to apply the observations to a single band as per this Q & A between
	 * Matt Templeton and I:<br/>
	 * DB: Like mean curve creation in VStar, should we only apply DC DFT to a
	 * single band, e.g. visual? MT: Yes, because of two things: 1) The different
	 * bands will have different mean values, and 2) The different bands will have
	 * different amplitudes or frequencies depending on what is physically causing
	 * the variation. Variability caused by temperature changes can have wildly
	 * different amplitudes in U or B versus Rc or Ic.
	 */
	public void performPeriodAnalysis(PeriodAnalysisPluginBase plugin) {
		try {
			if (getLatestNewStarMessage() != null && validObsList != null) {
				SingleSeriesSelectionDialog dialog = new SingleSeriesSelectionDialog(obsAndMeanPlotModel);

				if (!dialog.isCancelled()) {
					SeriesType type = dialog.getSeries();

					List<ValidObservation> obs = getSeriesInfoProvider().getObservations(type);

					this.getProgressNotifier().notifyListeners(ProgressInfo.START_PROGRESS);
					this.getProgressNotifier().notifyListeners(ProgressInfo.BUSY_PROGRESS);

					PeriodAnalysisTask task = new PeriodAnalysisTask(plugin, type, obs);

					this.currTask = task;
					task.execute();
				}
			}
		} catch (Exception e) {
			MessageBox.showErrorDialog(Mediator.getUI().getComponent(), LocaleProps.get("PERIOD_ANALYSIS"), e);

			// TODO: why not ProgressInfo.COMPLETE_PROGRESS then
			// ProgressInfo.CLEAR_PROGRESS?
			this.getProgressNotifier().notifyListeners(ProgressInfo.START_PROGRESS);

			Mediator.getUI().getStatusPane().setMessage("");
		}
	}

	/**
	 * Open the plot control dialog relevant to the current analysis mode.<br/>
	 * TODO: move to DocumentManager
	 */
	public void showPlotControlDialog() {
		String title = null;
		ObservationAndMeanPlotPane plotPane = analysisTypeMap.get(analysisType).getObsAndMeanChartPane();
		TimeElementsInBinSettingPane binSettingPane = null;
		NamedComponent extra = null;

		if (analysisType == AnalysisType.RAW_DATA) {
			title = LocaleProps.get("LIGHT_CURVE_CONTROL_DLG_TITLE");
			binSettingPane = new TimeElementsInBinSettingPane(LocaleProps.get("DAYS_PER_MEAN_SERIES_BIN"), plotPane,
					JDTimeElementEntity.instance);
		} else if (analysisType == AnalysisType.PHASE_PLOT) {
			title = LocaleProps.get("PHASE_PLOT_CONTROL_DLG_TITLE");
			binSettingPane = new TimeElementsInBinSettingPane(LocaleProps.get("PHASE_STEPS_PER_MEAN_SERIES_BIN"),
					plotPane, PhaseTimeElementEntity.instance);
		}

		PlotControlDialog dialog = new PlotControlDialog(title, plotPane, binSettingPane, extra, analysisType);
		dialog.setVisible(true);
	}

	/**
	 * Open the model dialog.
	 */
	public void showModelDialog() {
		getModelDialog().showDialog();
	}

	/**
	 * Open the phase plots dialog.
	 */
	public void showPhaseDialog() {
		getPhaseDialog().showDialog();
	}

	/**
	 * Opens the filters dialog.
	 */
	public void showFiltersDialog() {
		getObservationFiltersDialog().showDialog();
	}

	/**
	 * Perform a plugin based modeling operation.
	 */
	public void performModellingOperation(ModelCreatorPluginBase plugin) {
		try {
			if (getLatestNewStarMessage() != null && validObsList != null) {
				SingleSeriesSelectionDialog seriesDialog = new SingleSeriesSelectionDialog(obsAndMeanPlotModel);

				if (!seriesDialog.isCancelled()) {
					SeriesType type = seriesDialog.getSeries();

					List<ValidObservation> obs = getSeriesInfoProvider().getObservations(type);

					// TODO: possibly need to add: getUI()/invokeDialog()
					// compare with modelling and obs src plugins

					IModel model = plugin.getModel(obs);

					if (model != null) {
						ModellingTask task = new ModellingTask(model);

						this.currTask = task;

						this.getProgressNotifier().notifyListeners(ProgressInfo.START_PROGRESS);
						this.getProgressNotifier().notifyListeners(ProgressInfo.BUSY_PROGRESS);

						task.execute();
					}
				}
			}
		} catch (Exception e) {
			MessageBox.showErrorDialog(Mediator.getUI().getComponent(), "Modelling Error", e);

			this.getProgressNotifier().notifyListeners(ProgressInfo.START_PROGRESS);

			Mediator.getUI().getStatusPane().setMessage("");
		}
	}

	/**
	 * Perform a non-plugin based modeling operation.
	 * 
	 * @param model The model object for this plugin whose execute() method can be
	 *              invoked to create the model artifacts.
	 */
	public void performModellingOperation(IModel model) {
		try {
			ModellingTask task = new ModellingTask(model);

			this.currTask = task;

			this.getProgressNotifier().notifyListeners(ProgressInfo.START_PROGRESS);
			this.getProgressNotifier().notifyListeners(ProgressInfo.BUSY_PROGRESS);

			task.execute();
		} catch (Exception e) {
			MessageBox.showErrorDialog(Mediator.getUI().getComponent(), "Modelling Error", e);

			this.getProgressNotifier().notifyListeners(ProgressInfo.START_PROGRESS);

			Mediator.getUI().getStatusPane().setMessage("");
		}
	}

	/**
	 * Perform an observation transformation plugin operation.
	 */
	public void performObservationTransformationOperation(ObservationTransformerPluginBase plugin) {
		try {
			if (getLatestNewStarMessage() != null && validObsList != null) {
				MultipleSeriesSelectionDialog seriesDialog = new MultipleSeriesSelectionDialog(obsAndMeanPlotModel);

				if (!seriesDialog.isCancelled()) {
					IUndoableAction action = plugin.createAction(Mediator.getInstance().getSeriesInfoProvider(),
							seriesDialog.getSelectedSeries());

					currTask = getUndoableActionManager().performUndoableAction(action, UndoableActionType.DO);
				}
			}
		} catch (Exception e) {
			MessageBox.showErrorDialog(Mediator.getUI().getComponent(), "Observation Transformartion Error",
					e.getLocalizedMessage());

			this.getProgressNotifier().notifyListeners(ProgressInfo.START_PROGRESS);

			Mediator.getUI().getStatusPane().setMessage("");
		}
	}

	/**
	 * Fire data model change events.<br/>
	 * This method should be called after observation plot or table model changes.
	 */
	public void updatePlotsAndTables() {
		getObservationPlotModel(AnalysisType.RAW_DATA).update();

		if (analysisTypeMap.containsKey(AnalysisType.PHASE_PLOT)) {
			ObservationAndMeanPlotModel phase_model = getObservationPlotModel(AnalysisType.PHASE_PLOT);
			phase_model.update();
		}

		validObsTableModel.fireTableDataChanged();
	}

	/**
	 * Invokes a tool plugin with the currently loaded observations.
	 * 
	 * @param plugin The tool plugin to be invoked.
	 */
	public void invokeTool(ObservationToolPluginBase plugin) {
		if (validObservationCategoryMap != null) {
			try {
				// getSeriesInfoProvider() always returns series info for RAW_DATA.
				// PHASE_PLOT can have other series visibility, so it is more logical to pass
				// series info for the active analysis.
				ISeriesInfoProvider seriesInfo = analysisTypeMap.get(analysisType).getObsAndMeanChartPane().getObsModel();
				plugin.invoke(seriesInfo);
				//plugin.invoke(getSeriesInfoProvider());				
			} catch (Throwable t) {
				MessageBox.showErrorDialog("Tool Error", t);
			}
		} else {
			MessageBox.showMessageDialog(Mediator.getUI().getComponent(), "Tool Error",
					"There are no observations loaded.");
		}
	}

	/**
	 * Apply the custom filter plug-in to the currently loaded observation set.
	 * 
	 * @param plugin The tool plug-in to be invoked.
	 */
	public void applyCustomFilterToCurrentObservations(CustomFilterPluginBase plugin) {
		if (validObsList != null) {
			try {
				plugin.apply(validObsList);
			} catch (Throwable t) {
				MessageBox.showErrorDialog("Custom Filter Error", t);
			}
		} else {
			MessageBox.showMessageDialog(Mediator.getUI().getComponent(), "Custom Filter",
					"There are no observations loaded.");
		}
	}

	/**
	 * Create a filter from the current plot view.
	 */
	public void createFilterFromPlot() {
		InViewObservationFilter filter = new InViewObservationFilter();
		filter.execute();
	}

	/**
	 * Save the artefact corresponding to the current viewMode.
	 * 
	 * @param parent The parent component to be used in dialogs.
	 */
	public void saveCurrentMode(Component parent) {
		List<ValidObservation> obs = null;

		switch (viewMode) {
		case PLOT_OBS_MODE:
			savePlotToFile(parent);
			break;
		case LIST_OBS_MODE:
			saveObsListToFile(parent);
			break;
		case LIST_MEANS_MODE:
			obs = analysisTypeMap.get(analysisType).getMeansListPane().getObsTableModel().getObs();
			saveSyntheticObsListToFile(parent, obs);
			break;
		case MODEL_MODE:
			if (modelSelectionMessage != null) {
				obs = modelSelectionMessage.getModel().getFit();
				saveSyntheticObsListToFile(parent, obs);
			}
			break;
		case RESIDUALS_MODE:
			if (modelSelectionMessage != null) {
				obs = modelSelectionMessage.getModel().getResiduals();
				saveSyntheticObsListToFile(parent, obs);
			}
			break;
		}
	}

	/**
	 * Save the current plot (as a PNG) to the specified file.<br/>
	 * Used by VStar scripting API.
	 * 
	 * @param path   The file to write the PNG image to.
	 * @param width  The desired width of the image.
	 * @param height The desired height of the image.
	 */
	public void saveCurrentPlotToFile(File file, int width, int height) {
		ChartPanel chart = analysisTypeMap.get(analysisType).getObsAndMeanChartPane().getChartPanel();

		try {
			ChartUtils.saveChartAsPNG(file, chart.getChart(), width, height);
		} catch (IOException e) {
			MessageBox.showErrorDialog("Save plot to file", "Cannot save plot to " + "'" + file.getPath() + "'.");
		}
	}

	/**
	 * Save observation list to a file in a separate thread. Note that we want to
	 * save just those observations that are in view in the observation list
	 * currently.
	 * 
	 * Used by the VStar Scripting API.
	 * 
	 * @param parent    The parent component to be used in dialogs.
	 * @param plugin    The observation sink plugin.
	 * @param path      The path of the file to save to.
	 * @param delimiter The delimiter between data items.
	 */
	public void saveObsListToFile(Component parent, ObservationSinkPluginBase plugin, File path, String delimiter) {
		List<ValidObservation> obs = this.analysisTypeMap.get(analysisType).getObsListPane().getObservationsInView();

		if (!obs.isEmpty()) {
			this.getProgressNotifier().notifyListeners(ProgressInfo.START_PROGRESS);

			this.getProgressNotifier().notifyListeners(new ProgressInfo(ProgressType.MAX_PROGRESS, obs.size()));

			ObsListFileSaveTask task = new ObsListFileSaveTask(plugin, obs, path, delimiter);

			this.currTask = task;
			task.execute();
		} else {
			MessageBox.showMessageDialog(parent, "Save Observations", "There are no visible observations to save.");
		}
	}

	/**
	 * Save synthetic observation list (means, model, residuals) to a file in a
	 * separate thread.<br/>
	 * 
	 * Used by VStar scripting API.
	 * 
	 * @param parent    The parent component to be used in dialogs.
	 * @param plugin    The observation sink plugin.
	 * @param mode      The current synthetic view mode.
	 * @param path      The path of the file to save to.
	 * @param delimiter The delimiter between data items.
	 */
	public void saveSyntheticObsListToFile(Component parent, ObservationSinkPluginBase plugin, ViewModeType mode,
			File path, String delimiter) {

		List<ValidObservation> obs = null;

		switch (mode) {
		case LIST_MEANS_MODE:
			obs = analysisTypeMap.get(analysisType).getMeansListPane().getObsTableModel().getObs();
			break;
		case MODEL_MODE:
			if (modelSelectionMessage != null) {
				obs = modelSelectionMessage.getModel().getFit();
			} else {
				MessageBox.showMessageDialog(parent, "Save Observations", "There are no observations to save.");
			}
			break;
		case RESIDUALS_MODE:
			if (modelSelectionMessage != null) {
				obs = modelSelectionMessage.getModel().getResiduals();
			} else {
				MessageBox.showMessageDialog(parent, "Save Observations", "There are no observations to save.");
			}
			// Note: we include these for completeness otherwise the type
			// checker will complain. We assert that we should never arrive
			// here. We could of course merge this method and
			// saveObsListToFile().
		case LIST_OBS_MODE:
		case PLOT_OBS_MODE:
			assert (false);
			break;
		}

		if (!obs.isEmpty()) {
			this.getProgressNotifier().notifyListeners(ProgressInfo.START_PROGRESS);

			this.getProgressNotifier().notifyListeners(new ProgressInfo(ProgressType.MAX_PROGRESS, obs.size()));

			// We re-use the same observation list file save task as
			// elsewhere but specify simple file type to match the fact that
			// we are only going to save JD, magnitude, and uncertainty
			// (for means).
			ObsListFileSaveTask task = new ObsListFileSaveTask(plugin, obs, path, obsListFileSaveDialog.getDelimiter());

			this.currTask = task;
			task.execute();
		} else {
			MessageBox.showMessageDialog(parent, "Save Observations", "There are no observations to save.");
		}
	}

	/**
	 * Save the currently visible mode's plot. The file is requested from the user
	 * via a dialog.
	 * 
	 * @param parent The parent component of the file dialog.
	 */
	private void savePlotToFile(Component parent) {
		try {
			ChartPanel chartPanel = analysisTypeMap.get(analysisType).getObsAndMeanChartPane().getChartPanel();

			if (imageSaveDialog.showDialog(parent)) {
				File path = imageSaveDialog.getSelectedFile();
				if (path.exists() && path.isFile() && !MessageBox.showConfirmDialog(LocaleProps.get("FILE_MENU_SAVE"),
						LocaleProps.get("SAVE_OVERWRITE"))) {
					return;
				}
				JFreeChart chart = chartPanel.getChart();
				int width = chartPanel.getWidth();
				int height = chartPanel.getHeight();

				ChartUtils.saveChartAsPNG(path, chart, width, height);
			}
		} catch (IOException ex) {
			MessageBox.showErrorDialog(parent, "Save Observation and Means Plot", ex.getMessage());
		}
	}

	/**
	 * Save observation list to a file in a separate thread. Note that we want to
	 * save just those observations that are in view in the observation list
	 * currently. The file is requested from the user via a dialog.
	 * 
	 * @param parent The parent component to be used in dialogs.
	 */
	private void saveObsListToFile(Component parent) {
		List<ValidObservation> obs = this.analysisTypeMap.get(analysisType).getObsListPane().getObservationsInView();

		if (!obs.isEmpty()) {
			if (obsListFileSaveDialog.showDialog(parent)) {
				File outFile = obsListFileSaveDialog.getSelectedFile();

				if (outFile.exists() && outFile.isFile() && !MessageBox
						.showConfirmDialog(LocaleProps.get("FILE_MENU_SAVE"), LocaleProps.get("SAVE_OVERWRITE"))) {
					return;
				}

				this.getProgressNotifier().notifyListeners(ProgressInfo.START_PROGRESS);

				this.getProgressNotifier().notifyListeners(new ProgressInfo(ProgressType.MAX_PROGRESS, obs.size()));

				ObsListFileSaveTask task = new ObsListFileSaveTask(obsListFileSaveDialog.getSelectedPlugin(), obs,
						outFile, obsListFileSaveDialog.getDelimiter());

				this.currTask = task;
				task.execute();
			}
		} else {
			MessageBox.showMessageDialog(parent, "Save Observations", "There are no visible observations to save.");
		}
	}

	/**
	 * Save synthetic observation list (means, model, residuals) to a file in a
	 * separate thread.
	 * 
	 * @param parent The parent component to be used in dialogs.
	 * @param plugin The observation sink plugin.
	 * @param obs    The list of observations to be saved.
	 */
	private void saveSyntheticObsListToFile(Component parent, List<ValidObservation> obs) {

		if (!obs.isEmpty()) {
			if (obsListFileSaveDialog.showDialog(parent)) {
				File outFile = obsListFileSaveDialog.getSelectedFile();

				if (outFile.exists() && outFile.isFile() && !MessageBox
						.showConfirmDialog(LocaleProps.get("FILE_MENU_SAVE"), LocaleProps.get("SAVE_OVERWRITE"))) {
					return;
				}

				this.getProgressNotifier().notifyListeners(ProgressInfo.START_PROGRESS);

				this.getProgressNotifier().notifyListeners(new ProgressInfo(ProgressType.MAX_PROGRESS, obs.size()));

				// We re-use the same observation list file save task as
				// above but specify simple file type to match the fact that
				// we are only going to save JD, magnitude, and uncertainty
				// (for means).
				ObsListFileSaveTask task = new ObsListFileSaveTask(obsListFileSaveDialog.getSelectedPlugin(), obs,
						outFile, obsListFileSaveDialog.getDelimiter());

				this.currTask = task;
				task.execute();
			}
		} else {
			MessageBox.showMessageDialog(parent, "Save Observations", "There are no observations to save.");
		}
	}

	/**
	 * Print the artefact corresponding to the current mode.
	 * 
	 * @param parent The parent component to be used by an error dialog.
	 */
	public void printCurrentMode(Component parent) {
		switch (viewMode) {
		case PLOT_OBS_MODE:
			this.analysisTypeMap.get(analysisType).getObsAndMeanChartPane().getChartPanel().createChartPrintJob();
			break;

		case LIST_OBS_MODE:
			try {
				ObservationListPane obsListPane = this.analysisTypeMap.get(analysisType).getObsListPane();

				obsListPane.getValidDataTable().print(PrintMode.FIT_WIDTH);

				if (obsListPane.getInvalidDataTable() != null) {
					obsListPane.getInvalidDataTable().print(PrintMode.FIT_WIDTH);
				}
			} catch (PrinterException e) {
				MessageBox.showErrorDialog(parent, "Print Observations", e.getMessage());
			}
			break;

		case LIST_MEANS_MODE:
			try {
				SyntheticObservationListPane<AbstractMeanObservationTableModel> meanObsListPane = this.analysisTypeMap
						.get(analysisType).getMeansListPane();

				meanObsListPane.getObsTable().print(PrintMode.FIT_WIDTH);
			} catch (PrinterException e) {
				MessageBox.showErrorDialog(parent, "Print Mean Values", e.getMessage());
			}
			break;
		case MODEL_MODE:
			if (modelSelectionMessage != null) {
				try {
					SyntheticObservationListPane<AbstractModelObservationTableModel> modelListPane = getDocumentManager()
							.getModelListPane(analysisType, modelSelectionMessage.getModel());

					modelListPane.getObsTable().print(PrintMode.FIT_WIDTH);
				} catch (PrinterException e) {
					MessageBox.showErrorDialog(parent, "Print Model Values", e.getMessage());
				}
			}
			break;
		case RESIDUALS_MODE:
			if (modelSelectionMessage != null) {
				try {
					SyntheticObservationListPane<AbstractModelObservationTableModel> residualsListPane = getDocumentManager()
							.getResidualsListPane(analysisType, modelSelectionMessage.getModel());

					residualsListPane.getObsTable().print(PrintMode.FIT_WIDTH);
				} catch (PrinterException e) {
					MessageBox.showErrorDialog(parent, "Print Residual Values", e.getMessage());
				}
			}
			break;
		}
	}

	/**
	 * Show the details of the currently selected observation in the current view
	 * mode (plot or table).
	 */
	public void showObservationDetails() {
		ValidObservation ob = null;

		switch (viewMode) {
		case PLOT_OBS_MODE:
			ob = this.analysisTypeMap.get(analysisType).getObsAndMeanChartPane().getLastObSelected();
			break;
		case LIST_OBS_MODE:
			ob = this.analysisTypeMap.get(analysisType).getObsListPane().getLastObSelected();
			break;
		case LIST_MEANS_MODE:
			ob = this.analysisTypeMap.get(analysisType).getMeansListPane().getLastObSelected();
			break;
		case MODEL_MODE:
			ob = getDocumentManager().getModelListPane(analysisType, modelSelectionMessage.getModel())
					.getLastObSelected();
			break;
		case RESIDUALS_MODE:
			ob = getDocumentManager().getResidualsListPane(analysisType, modelSelectionMessage.getModel())
					.getLastObSelected();
			break;
		}

		if (ob != null) {
			new ObservationDetailsDialog(ob);
		} else {
			MessageBox.showWarningDialog("Observation Details", "No observation selected");
		}
	}

	private void updateChartPropertiesForAnalysisType(AnalysisType type) {
		AnalysisTypeChangeMessage m = analysisTypeMap.get(type);
		if (m != null) {
			ObservationAndMeanPlotPane pane = m.getObsAndMeanChartPane();
			if (pane != null) {
				pane.updateChartProperties();
			}
		}
	}

	/**
	 * 
	 * Updates properties for charts (light curve, phase plot)
	 * 
	 * @param backgroundColor Chart background
	 *
	 * @param gridlinesColor  Color of gridlines
	 * 
	 */
	public void updateChartProperties() {
		List<AnalysisType> list = Arrays.asList(AnalysisType.values());
		for (AnalysisType type : list) {
			updateChartPropertiesForAnalysisType(type);
		}
	}

	/**
	 * Report a discrepant observation to AAVSO (if the dataset was AID-downloaded).
	 * 
	 * @param ob     The observation to be reported.
	 * @param dialog A parent dialog to set non-visible and dispose. May be null.
	 */
	public void reportDiscrepantObservation(ValidObservation ob, JDialog dialog)
			throws AuthenticationError, CancellationException, ConnectionException {
		// If the dataset was loaded from AID and the change was
		// to mark this observation as discrepant, we ask the user
		// whether to report this to AAVSO.
		if (ob.isDiscrepant() && getLatestNewStarMessage().getNewStarType() == NewStarType.NEW_STAR_FROM_DATABASE) {

			String auid = getLatestNewStarMessage().getStarInfo().getAuid();
			String name = ob.getName();
			int uniqueId = ob.getRecordNumber();

			DiscrepantReportDialog reportDialog = new DiscrepantReportDialog(auid, ob);

			if (!reportDialog.isCancelled()) {
				try {
					Mediator.getUI().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

					Authenticator.getInstance().authenticate();

					String userName = ResourceAccessor.getLoginInfo().getUserName();

					String editor = "vstar:" + userName;

					if (dialog != null) {
						dialog.setVisible(false);
					}

					// Create and submit the discrepant report.
					DiscrepantReport report = new DiscrepantReport(auid, name, uniqueId, editor,
							reportDialog.getComments());

					IDiscrepantReporter reporter = new VSXWebServiceZapperLogger();

					reporter.lodge(report);

					getUI().setCursor(null);

					if (dialog != null) {
						dialog.dispose();
					}
				} finally {
					Mediator.getUI().setCursor(null);

				}
			}
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
