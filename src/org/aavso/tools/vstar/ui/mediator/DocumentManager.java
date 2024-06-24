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
package org.aavso.tools.vstar.ui.mediator;

import java.awt.Window;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.ui.IMainUI;
import org.aavso.tools.vstar.ui.UIType;
import org.aavso.tools.vstar.ui.mediator.message.NewStarMessage;
import org.aavso.tools.vstar.ui.mediator.message.PhaseChangeMessage;
import org.aavso.tools.vstar.ui.model.list.AbstractModelObservationTableModel;
import org.aavso.tools.vstar.ui.model.list.PhasePlotModelObservationTableModel;
import org.aavso.tools.vstar.ui.model.list.RawDataModelObservationTableModel;
import org.aavso.tools.vstar.ui.pane.list.SyntheticObservationListPane;
import org.aavso.tools.vstar.util.model.IModel;
import org.aavso.tools.vstar.util.notification.Listener;
import org.aavso.tools.vstar.util.stats.BinningResult;
import org.aavso.tools.vstar.util.stats.PhaseCalcs;

/**
 * This class manages the creation of VStar "documents", i.e. models and GUI
 * components and important state. It will also expand to manage document printing,
 * saving (and the need to) etc. Another thing this class will allow us to do is to
 * cache GUI components (and by association, their models) permitting reuse of these
 * and updates to models. TODO: call it ComponentManager instead?
 */
@SuppressWarnings("serial")
public class DocumentManager {

    private Mediator mediator;
    
	// Model and residuals maps.
	private Map<String, SyntheticObservationListPane<AbstractModelObservationTableModel>> rawDataModelComponents;
	private Map<String, SyntheticObservationListPane<AbstractModelObservationTableModel>> phasedModelComponents;

	private Map<String, SyntheticObservationListPane<AbstractModelObservationTableModel>> rawDataResidualComponents;
	private Map<String, SyntheticObservationListPane<AbstractModelObservationTableModel>> phasedResidualComponents;

	private boolean phasePlotExists;
	private double epoch;
	private double period;

	// state of user-controllable plot pane characteristics
	private Map<AnalysisType, Boolean> showErrorBars;
	private Map<AnalysisType, Boolean> showCrossHairs;
	private Map<AnalysisType, Boolean> invertRange;
	private Map<AnalysisType, Boolean> invertSeriesOrder;
	private Map<AnalysisType, Boolean> joinMeans;
	
	private Map<String, String> statsInfo;

	private static int filterNum = 0;

	public DocumentManager() {
	    mediator = Mediator.getInstance();

		init();
	}

	// ** phase, epoch, period methods **
	
	public boolean phasePlotExists() {
		return phasePlotExists;
	}

	public double getEpoch() {
		return epoch;
	}

	public double getPeriod() {
		return period;
	}

	// ** user-controllable plot pane methods **
	
	public void toggleErrorBarState() {
	    togglePlotControlState(showErrorBars);
	}

	public void toggleCrossHairState() {
	    togglePlotControlState(showCrossHairs);
	}

	public void toggleRangeAxisInversionState() {
	    togglePlotControlState(invertRange);
	}
	
	public void toggleSeriesOrderInversionState() {
	    togglePlotControlState(invertSeriesOrder);
	}

	public void toggleJoinMeansState() {
	    togglePlotControlState(joinMeans);
	}

	public boolean shouldShowErrorBars() {
		return showErrorBars.get(mediator.getAnalysisType());
	}

	public boolean shouldShowCrossHairs() {
		return showCrossHairs.get(mediator.getAnalysisType());
	}

	public boolean shouldInvertRange() {
		return invertRange.get(mediator.getAnalysisType());
	}

	public boolean shouldInvertSeriesOrder() {
		return invertSeriesOrder.get(mediator.getAnalysisType());
	}

	public boolean shouldJoinMeans() {
		return joinMeans.get(mediator.getAnalysisType());
	}

	private void togglePlotControlState(Map<AnalysisType, Boolean> map) {
	    AnalysisType analysisType = mediator.getAnalysisType();
        map.put(analysisType, !map.get(analysisType));
	}
	
	// ** List pane methods **

	public SyntheticObservationListPane<AbstractModelObservationTableModel> getModelListPane(
			AnalysisType type, IModel model) {
		SyntheticObservationListPane<AbstractModelObservationTableModel> pane = null;
		String key = model.getDescription();

		switch (type) {
		case RAW_DATA:
			if (!rawDataModelComponents.containsKey(key)) {
				// Create model table model and GUI component since they have
				// not been.
				RawDataModelObservationTableModel modelTableModel = new RawDataModelObservationTableModel(
						model.getFit(), SeriesType.Model);
				String summary = model.toString();
				SyntheticObservationListPane<AbstractModelObservationTableModel> modelPane = new SyntheticObservationListPane<AbstractModelObservationTableModel>(
						modelTableModel, summary);

				rawDataModelComponents.put(key, modelPane);
			}

			pane = rawDataModelComponents.get(key);
			break;

		case PHASE_PLOT:
			key = getPhasedModelKey(model);

			if (!phasedModelComponents.containsKey(key)) {
				// Set the fit list's phases according to the last phase change.
				// It's okay to modify the original data.
				PhaseCalcs.setPhases(model.getFit(), epoch, period);

				// Create model table model and GUI component since they have
				// not been.
				PhasePlotModelObservationTableModel modelTableModel = new PhasePlotModelObservationTableModel(
						model.getFit(), SeriesType.Model);
				String summary = model.toString();
				SyntheticObservationListPane<AbstractModelObservationTableModel> modelPane = new SyntheticObservationListPane<AbstractModelObservationTableModel>(
						modelTableModel, summary);

				phasedModelComponents.put(key, modelPane);
			}

			pane = phasedModelComponents.get(key);
			break;
		}

		return pane;
	}

	public SyntheticObservationListPane<AbstractModelObservationTableModel> getResidualsListPane(
			AnalysisType type, IModel model) {
		SyntheticObservationListPane<AbstractModelObservationTableModel> pane = null;
		String key = model.getDescription();

		switch (type) {
		case RAW_DATA:
			if (!rawDataResidualComponents.containsKey(key)) {
				RawDataModelObservationTableModel residualsTableModel = new RawDataModelObservationTableModel(
						model.getResiduals(), SeriesType.Residuals);
				String summary = model.toString();
				SyntheticObservationListPane<AbstractModelObservationTableModel> residualsPane = new SyntheticObservationListPane<AbstractModelObservationTableModel>(
						residualsTableModel, summary);

				rawDataResidualComponents.put(key, residualsPane);
			}

			pane = rawDataResidualComponents.get(key);
			break;

		case PHASE_PLOT:
			key = getPhasedModelKey(model);

			if (!phasedResidualComponents.containsKey(key)) {
				// Set the residual list's phases according to the last phase
				// change.
				// It's okay to modify the original data.
				PhaseCalcs.setPhases(model.getResiduals(), epoch, period);

				// Create model table model and GUI component since they have
				// not been.
				PhasePlotModelObservationTableModel residualsTableModel = new PhasePlotModelObservationTableModel(
						model.getResiduals(), SeriesType.Residuals);
				String summary = model.toString();
				SyntheticObservationListPane<AbstractModelObservationTableModel> residualsPane = new SyntheticObservationListPane<AbstractModelObservationTableModel>(
						residualsTableModel, summary);

				phasedResidualComponents.put(key, residualsPane);
			}

			pane = phasedResidualComponents.get(key);
			break;
		}

		return pane;
	}

	// ** Stats info methods **

	/**
	 * Add a statistics information string.
	 * 
	 * @param key
	 *            The key against which this information is to be stored.
	 * @param info
	 *            The information string to be added.
	 */
	public void addStatsInfo(String key, String info) {
		statsInfo.put(key, info);
	}

	/**
	 * @return the statsInfo
	 */
	public Map<String, String> getStatsInfo() {
		return statsInfo;
	}

	// Returns a mean observation change (binning result) listener.
	protected Listener<BinningResult> createBinChangeListener() {
		return new Listener<BinningResult>() {
			public void update(BinningResult info) {
				updateAnovaInfo(info);
			}

			public boolean canBeRemoved() {
				return false;
			}
		};
	}

	/**
	 * Update the anova information from a binning result.
	 * 
	 * @param binningResult
	 *            The binning result to use.
	 */
	public void updateAnovaInfo(BinningResult binningResult) {
		addStatsInfo("Mean Source Series", binningResult.getSeries()
				.getDescription());

		addStatsInfo("anova", createAnovaText(binningResult));
	}

	// Returns ANOVA result text suitable for display.
	public String createAnovaText(BinningResult binningResult) {
		return binningResult.createAnovaText();
	}

	// ** Filter-related methods **

	/**
	 * Return the name of the next untitled filter.
	 * 
	 * @return the next untitled filter name
	 */
	public String getNextUntitledFilterName() {
		filterNum++;
		return "Untitled Filter " + filterNum;
	}

	// Helpers

	/**
	 * Returns a unique phase model key for a model given the current epoch and
	 * period associated with a phase change.
	 * 
	 * @param model
	 *            The model whose description we will use as part of the key.
	 * @return The unique key from the tuple: <description, epoch, period>.
	 */
	private String getPhasedModelKey(IModel model) {
		return String.format("%s:e=%f,p=%f", model.getDescription(), epoch,
				period);
	}

	/**
	 * Initialise (or reset) data members
	 */
	public void init() {
        phasePlotExists = false;
        epoch = 0;
        period = 0;

	    // model maps
	    if (rawDataModelComponents == null) {
	        rawDataModelComponents = new HashMap<String, SyntheticObservationListPane<AbstractModelObservationTableModel>>();
	    }
	    rawDataModelComponents.clear();

        if (phasedModelComponents == null) {
            phasedModelComponents = new HashMap<String, SyntheticObservationListPane<AbstractModelObservationTableModel>>();
        }
        phasedModelComponents.clear();

        if (rawDataResidualComponents == null) {
            rawDataResidualComponents = new HashMap<String, SyntheticObservationListPane<AbstractModelObservationTableModel>>();
        }
        rawDataResidualComponents.clear();

        if (phasedResidualComponents == null) {
            phasedResidualComponents = new HashMap<String, SyntheticObservationListPane<AbstractModelObservationTableModel>>();
        }
        phasedResidualComponents.clear();

	    // Boolean maps
	    if (showErrorBars == null) {
            showErrorBars = new HashMap<AnalysisType, Boolean>();
	    }
	    showErrorBars.put(AnalysisType.RAW_DATA, true);
	    showErrorBars.put(AnalysisType.PHASE_PLOT, true);

        if (showCrossHairs == null) {
            showCrossHairs = new HashMap<AnalysisType, Boolean>();
        }
        showCrossHairs.put(AnalysisType.RAW_DATA, true);
        showCrossHairs.put(AnalysisType.PHASE_PLOT, true);

        if (invertRange == null) {
            invertRange = new HashMap<AnalysisType, Boolean>();
        }
        invertRange.put(AnalysisType.RAW_DATA, true);
        invertRange.put(AnalysisType.PHASE_PLOT, true);

        if (invertSeriesOrder == null) {
            invertSeriesOrder = new HashMap<AnalysisType, Boolean>();
        }
        invertSeriesOrder.put(AnalysisType.RAW_DATA, true);
        invertSeriesOrder.put(AnalysisType.PHASE_PLOT, true);

        if (joinMeans == null) {
            joinMeans = new HashMap<AnalysisType, Boolean>();
        }
        joinMeans.put(AnalysisType.RAW_DATA, true);
        joinMeans.put(AnalysisType.PHASE_PLOT, true);

        // stats info map
        if (statsInfo == null) {
            statsInfo = new TreeMap<String, String>();
        }
        statsInfo.clear();
	}

	/**
	 * Return a phase change listener that updates epoch and period information
	 * in preparation for creating or retrieving phase plot components.<br/>
	 * TODO: when we have finally unified observations as a single list across
	 * all models, a listener for this message can call setPhases() on that
	 * list.
	 */
	public Listener<PhaseChangeMessage> createPhaseChangeListener() {
		return new Listener<PhaseChangeMessage>() {
			@Override
			public void update(PhaseChangeMessage info) {
				phasePlotExists = true;
				epoch = info.getEpoch();
				period = info.getPeriod();
			}

			@Override
			public boolean canBeRemoved() {
				return false;
			}
		};
	}

	/**
	 * Find and return the active window or null if one does not exist, e.g. the
	 * case where the UI is that of an applet.
	 */
	public static Window findActiveWindow() {
		Window wdw = null;

		IMainUI ui = Mediator.getUI();

		if (ui != null) {
			if (ui.getUiType() == UIType.DESKTOP) {
				if (Window.getWindows().length > 0) {
					for (Window window : Window.getWindows()) {
						// At least find the main window...
						if (window instanceof org.aavso.tools.vstar.ui.MainFrame) {
							wdw = window;
						}
						// ...even better if it's the focus owner. If nothing
						// else, by the end of the loop, we should have found
						// the main window, whether or not it has the focus.
						if (window.isFocusOwner()) {
							wdw = window;
							break;
						}
					}
				}
			}
		}

		return wdw;
	}
}
