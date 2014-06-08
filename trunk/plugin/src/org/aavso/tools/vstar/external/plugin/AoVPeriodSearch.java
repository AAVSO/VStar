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
package org.aavso.tools.vstar.external.plugin;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JDialog;
import javax.swing.JPanel;

import org.aavso.tools.vstar.data.DateInfo;
import org.aavso.tools.vstar.data.Magnitude;
import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.exception.AlgorithmError;
import org.aavso.tools.vstar.exception.CancellationException;
import org.aavso.tools.vstar.plugin.PluginComponentFactory;
import org.aavso.tools.vstar.plugin.period.PeriodAnalysisComponentFactory;
import org.aavso.tools.vstar.plugin.period.PeriodAnalysisDialogBase;
import org.aavso.tools.vstar.plugin.period.PeriodAnalysisPluginBase;
import org.aavso.tools.vstar.ui.NamedComponent;
import org.aavso.tools.vstar.ui.dialog.ITextComponent;
import org.aavso.tools.vstar.ui.dialog.MessageBox;
import org.aavso.tools.vstar.ui.dialog.MultiEntryComponentDialog;
import org.aavso.tools.vstar.ui.dialog.NumberField;
import org.aavso.tools.vstar.ui.dialog.TextField;
import org.aavso.tools.vstar.ui.dialog.period.PeriodAnalysis2DChartPane;
import org.aavso.tools.vstar.ui.dialog.period.PeriodAnalysisDataTablePane;
import org.aavso.tools.vstar.ui.dialog.period.PeriodAnalysisTopHitsTablePane;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.message.HarmonicSearchResultMessage;
import org.aavso.tools.vstar.ui.mediator.message.NewStarMessage;
import org.aavso.tools.vstar.ui.mediator.message.PeriodAnalysisSelectionMessage;
import org.aavso.tools.vstar.ui.model.list.PeriodAnalysisDataTableModel;
import org.aavso.tools.vstar.ui.model.plot.PhaseTimeElementEntity;
import org.aavso.tools.vstar.util.comparator.StandardPhaseComparator;
import org.aavso.tools.vstar.util.model.Harmonic;
import org.aavso.tools.vstar.util.model.PeriodAnalysisDerivedMultiPeriodicModel;
import org.aavso.tools.vstar.util.notification.Listener;
import org.aavso.tools.vstar.util.period.IPeriodAnalysisAlgorithm;
import org.aavso.tools.vstar.util.period.IPeriodAnalysisDatum;
import org.aavso.tools.vstar.util.period.PeriodAnalysisCoordinateType;
import org.aavso.tools.vstar.util.period.dcdft.PeriodAnalysisDataPoint;
import org.aavso.tools.vstar.util.stats.BinningResult;
import org.aavso.tools.vstar.util.stats.DescStats;
import org.aavso.tools.vstar.util.stats.PhaseCalcs;

/**
 * This plug-in implements the Analysis of Variance period search algorithm that
 * applies an ANOVA test to phased bins over a period range.
 * 
 * TODO:<br/>
 * o Add papers<br/>
 * 
 * http://articles.adsabs.harvard.edu/cgi-bin/nph-iarticle_query?1989MNRAS.241.
 * .153
 * S&amp;data_type=PDF_HIGH&amp;whole_paper=YES&amp;type=PRINTER&amp;filetype
 * =.pdf On the advantage of using analysis of variance in period search A.
 * Schwarzenberg-Czerny (1989)
 * 
 * http://iopscience.iop.org/1538-4357/460/2/L107/pdf/1538-4357_460_2_L107.pdf
 * 
 * o Get top-hits displaying in plot.<br/>
 * o Create a model? See Foster => disable Model button<br/>
 * o Fix cancellation handling in caller.<br/>
 * o Improve parameter dialog.<br/>
 * o Parallelise!<br/>
 */
public class AoVPeriodSearch extends PeriodAnalysisPluginBase {

	private final static int MAX_TOP_HITS = 20;

	private boolean interrupted;
	private boolean cancelled;
	private boolean legalParams;

	private int bins;
	private Double minPeriod, maxPeriod, resolution;

	private IPeriodAnalysisAlgorithm algorithm;

	/**
	 * Constructor
	 */
	public AoVPeriodSearch() {
		// periods = new ArrayList<Double>();
		// pValues = new ArrayList<Double>();
		// fValues = new ArrayList<Double>();
	}

	@Override
	public String getDescription() {
		// TODO: AoV "phased bin" period search?
		return "AoV period search";
	}

	@Override
	public String getDisplayName() {
		return "AoV with Period Range";
	}

	@Override
	public void executeAlgorithm(List<ValidObservation> obs)
			throws AlgorithmError, CancellationException {

		algorithm = new AoVAlgorithm(obs);
		algorithm.execute();
	}

	@Override
	public JDialog getDialog(SeriesType sourceSeriesType) {
		return new PeriodAnalysisDialog(sourceSeriesType);
	}

	@SuppressWarnings("serial")
	class PeriodAnalysisDialog extends PeriodAnalysisDialogBase implements
			Listener<PeriodAnalysisSelectionMessage> {

		private double period;
		private SeriesType sourceSeriesType;
		private IPeriodAnalysisDatum selectedDataPoint;

		private PeriodAnalysisDataTablePane resultsTablePane;
		private PeriodAnalysisTopHitsTablePane topHitsTablePane;
		private PeriodAnalysis2DChartPane plotPane;

		public PeriodAnalysisDialog(SeriesType sourceSeriesType) {
			super("AoV");

			this.sourceSeriesType = sourceSeriesType;

			prepareDialog();

			this.setNewPhasePlotButtonState(false);
			this.setFindHarmonicsButtonState(false);

			startup(); // Note: why does base class not call this in
			// prepareDialog()?
		}

		@Override
		protected Component createContent() {
			// Random plot
			// PeriodAnalysis2DPlotModel plotModel = new
			// PeriodAnalysis2DPlotModel(
			// algorithm.getTopHits(),
			// PeriodAnalysisCoordinateType.PERIOD,
			// PeriodAnalysisCoordinateType.POWER, false);

			// plotPane = createPeriodogramPlot("AoV Periodogram",
			// sourceSeriesType.getDescription(), plotModel);

			plotPane = PeriodAnalysisComponentFactory.createLinePlot(
					"AoV Periodogram", sourceSeriesType.getDescription(),
					algorithm.getResultSeries(),
					PeriodAnalysisCoordinateType.PERIOD,
					PeriodAnalysisCoordinateType.POWER, false, false);
			
			// Full results table
			PeriodAnalysisCoordinateType[] columns = {
					PeriodAnalysisCoordinateType.FREQUENCY,
					PeriodAnalysisCoordinateType.PERIOD,
					PeriodAnalysisCoordinateType.POWER,
					PeriodAnalysisCoordinateType.AMPLITUDE };

			// Note: algorithm won't be used (?) in this case but we must pass
			// it along.
			// TODO: subclass PeriodAnalysisDataTablePane to have no model
			// button
			// resultsTablePane =
			// PeriodAnalysisComponentFactory.createDataTable(
			// columns, algorithm.getResultSeries(), algorithm);

			PeriodAnalysisDataTableModel resultsModel = new PeriodAnalysisDataTableModel(
					columns, algorithm.getResultSeries());
			resultsTablePane = new NoModelPeriodAnalysisDataTablePane(
					resultsModel, algorithm);

			// Note: algorithm won't be used (?) in this case but we must pass
			// it along. TODO: how do we get top hit squares? See what DCDFT
			// does.
			// TODO: subclass PeriodAnalysisTopHitsTablePane to have no model
			// button
			// topHitsTablePane =
			// PeriodAnalysisComponentFactory.createDataTable(
			// columns, algorithm.getTopHits(), algorithm);

			PeriodAnalysisDataTableModel topHitsModel = new PeriodAnalysisDataTableModel(
					columns, algorithm.getTopHits());
			topHitsTablePane = new NoModelPeriodAnalysisTopHitsTablePane(
					topHitsModel, resultsModel, algorithm);

			// Return tabbed pane of plot and period display component.
			return PluginComponentFactory.createTabs(new NamedComponent(
					"Periodogram", plotPane), new NamedComponent("Results",
					resultsTablePane), new NamedComponent("Top Hits",
					topHitsTablePane));
		}

		// Send a period change message when the new-phase-plot button is
		// clicked.
		@Override
		protected void newPhasePlotButtonAction() {
			sendPeriodChangeMessage(period);
		}

		@Override
		protected void findHarmonicsButtonAction() {
			List<Double> data = algorithm.getResultSeries().get(
					PeriodAnalysisCoordinateType.FREQUENCY);
			List<Harmonic> harmonics = findHarmonics(selectedDataPoint
					.getFrequency(), data);
			HarmonicSearchResultMessage msg = new HarmonicSearchResultMessage(
					this, harmonics, selectedDataPoint);
			Mediator.getInstance().getHarmonicSearchNotifier().notifyListeners(
					msg);
		}

		@Override
		public void startup() {
			Mediator.getInstance().getPeriodAnalysisSelectionNotifier()
					.addListener(this);

			resultsTablePane.startup();
			topHitsTablePane.startup();
			plotPane.startup();
		}

		@Override
		public void cleanup() {
			Mediator.getInstance().getPeriodAnalysisSelectionNotifier()
					.removeListenerIfWilling(this);

			resultsTablePane.cleanup();
			topHitsTablePane.cleanup();
			plotPane.cleanup();
		}

		// Next two methods are for Listener<PeriodAnalysisSelectionMessage>

		@Override
		public boolean canBeRemoved() {
			return false;
		}

		@Override
		public void update(PeriodAnalysisSelectionMessage info) {
			period = info.getDataPoint().getPeriod();
			selectedDataPoint = info.getDataPoint();
			setNewPhasePlotButtonState(true);
			setFindHarmonicsButtonState(true);
		}

		// ** No model result and top-hit panes **

		class NoModelPeriodAnalysisDataTablePane extends
				PeriodAnalysisDataTablePane {

			public NoModelPeriodAnalysisDataTablePane(
					PeriodAnalysisDataTableModel model,
					IPeriodAnalysisAlgorithm algorithm) {
				super(model, algorithm);
			}

			@Override
			protected JPanel createButtonPanel() {
				return new JPanel();
			}

			@Override
			protected void enableButtons() {
				// Do nothing
			}
		}

		class NoModelPeriodAnalysisTopHitsTablePane extends
				PeriodAnalysisTopHitsTablePane {

			public NoModelPeriodAnalysisTopHitsTablePane(
					PeriodAnalysisDataTableModel topHitsModel,
					PeriodAnalysisDataTableModel fullDataModel,
					IPeriodAnalysisAlgorithm algorithm) {
				super(topHitsModel, fullDataModel, algorithm);
			}

			@Override
			protected JPanel createButtonPanel() {
				return new JPanel();
			}

			@Override
			protected void enableButtons() {
				// Do nothing
			}
		}
	}

	// The AoV algorithm implementation.
	class AoVAlgorithm implements IPeriodAnalysisAlgorithm {

		private List<ValidObservation> obs;

		private List<Double> frequencies;
		private LinkedList<Double> orderedFrequencies;

		private List<Double> periods;
		private LinkedList<Double> orderedPeriods;

		private List<Double> fValues;
		private LinkedList<Double> orderedFValues;

		private List<Double> pValues;
		private LinkedList<Double> orderedPValues;

		private double smallestFValue;
		private int smallestValueIndex;

		public AoVAlgorithm(List<ValidObservation> obs) {
			this.obs = obs;

			frequencies = new ArrayList<Double>();
			orderedFrequencies = new LinkedList<Double>();

			periods = new ArrayList<Double>();
			orderedPeriods = new LinkedList<Double>();

			fValues = new ArrayList<Double>();
			orderedFValues = new LinkedList<Double>();

			pValues = new ArrayList<Double>();
			orderedPValues = new LinkedList<Double>();

			smallestFValue = Double.MAX_VALUE;
			smallestValueIndex = 0;
		}

		@Override
		public String getRefineByFrequencyName() {
			return "None";
		}

		@Override
		public Map<PeriodAnalysisCoordinateType, List<Double>> getResultSeries() {
			Map<PeriodAnalysisCoordinateType, List<Double>> results = new HashMap<PeriodAnalysisCoordinateType, List<Double>>();

			results.put(PeriodAnalysisCoordinateType.FREQUENCY, frequencies);
			results.put(PeriodAnalysisCoordinateType.PERIOD, periods);
			results.put(PeriodAnalysisCoordinateType.POWER, fValues);
			results.put(PeriodAnalysisCoordinateType.AMPLITUDE, pValues);

			return results;
		}

		@Override
		public Map<PeriodAnalysisCoordinateType, List<Double>> getTopHits() {
			// todo: create top hits by sorting doubles in descending order
			// pairs of doubles;
			// limit to MAX_TOP_HITS = 100

			Map<PeriodAnalysisCoordinateType, List<Double>> topHits = new HashMap<PeriodAnalysisCoordinateType, List<Double>>();

			topHits.put(PeriodAnalysisCoordinateType.FREQUENCY,
					orderedFrequencies);
			topHits.put(PeriodAnalysisCoordinateType.PERIOD, orderedPeriods);
			topHits.put(PeriodAnalysisCoordinateType.POWER, orderedFValues);
			topHits.put(PeriodAnalysisCoordinateType.AMPLITUDE, orderedPValues);

			return topHits;
		}

		@Override
		public void multiPeriodicFit(List<Harmonic> harmonics,
				PeriodAnalysisDerivedMultiPeriodicModel model)
				throws AlgorithmError {
			// TODO Auto-generated method stub
		}

		@Override
		public List<PeriodAnalysisDataPoint> refineByFrequency(
				List<Double> freqs, List<Double> variablePeriods,
				List<Double> lockedPeriod) throws AlgorithmError {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void execute() throws AlgorithmError {
			// Request parameters
			// TODO: move this to top-level execute method and just pass actual
			// parameters to this class?
			while (!areParametersLegal(obs) && !cancelled)
				;

			if (!cancelled) {
				// Duplicate the obs (just JD and mag) so we can set phases
				// without
				// disturbing the original observation object.
				List<ValidObservation> phObs = new ArrayList<ValidObservation>();

				// TODO: cache these by JD range between new star resets...

				for (ValidObservation ob : obs) {
					if (interrupted)
						break;

					ValidObservation phOb = new ValidObservation();

					double jd = ob.getDateInfo().getJulianDay();
					phOb.setDateInfo(new DateInfo(jd));

					Magnitude mag = new Magnitude(ob.getMagnitude()
							.getMagValue(), ob.getMagnitude().getUncertainty());
					phOb.setMagnitude(mag);

					phObs.add(phOb);
				}

				// Choose an epoch value.
				double epoch = PhaseCalcs.epochStrategyMap.get("alpha")
						.determineEpoch(phObs);

				// Iterate over the periods in the range at the specified
				// resolution.
				for (double period = minPeriod; period <= maxPeriod; period += resolution) {
					if (interrupted)
						break;

					PhaseCalcs.setPhases(phObs, epoch, period);

					Collections.sort(phObs, StandardPhaseComparator.instance);

					// Note: 1 / bins = 1 cycle divided into N bins
					BinningResult binningResult = DescStats
							.createSymmetricBinnedObservations(phObs,
									PhaseTimeElementEntity.instance, 1.0 / bins);

					// Collect results
					frequencies.add(1.0 / period);
					periods.add(period);

					double fValue = binningResult.getFValue();
					fValues.add(fValue);
					if (fValue < smallestFValue) {
						smallestFValue = fValue;
						smallestValueIndex = fValues.size() - 1;
					}

					pValues.add(1 - binningResult.getPValue());
				}

				collectTopHits();
			}
		}

		// Collect the ordered top-hits
		// TODO: improve efficiency!
		private void collectTopHits() {
			orderedFrequencies.add(frequencies.get(smallestValueIndex));
			orderedPeriods.add(periods.get(smallestValueIndex));
			orderedFValues.add(fValues.get(smallestValueIndex));
			orderedPValues.add(pValues.get(smallestValueIndex));

			for (int i = 0; i < periods.size(); i++) {
				double frequency = frequencies.get(i);
				double period = periods.get(i);
				double fValue = fValues.get(i);
				double pValue = pValues.get(i);

				if (fValue > orderedFValues.get(0)) {
					orderedFrequencies.addFirst(frequency);
					orderedPeriods.addFirst(period);
					orderedFValues.addFirst(fValue);
					orderedPValues.addFirst(pValue);
				} else {
					orderedFrequencies.add(frequency);
					orderedPeriods.add(period);
					orderedFValues.add(fValue);
					orderedPValues.add(pValue);
				}
			}

			// Remove all but MAX_TOP_HITS
//			if (periods.size() > MAX_TOP_HITS) {
//				for (int i = MAX_TOP_HITS; i < periods.size(); i++) {
//					orderedFrequencies.remove(i);
//					orderedPeriods.remove(i);
//					orderedFValues.remove(i);
//					orderedPValues.remove(i);
//				}
//			}
		}

		@Override
		public void interrupt() {
			interrupted = true;
		}
	}

	// Ask user for period min, max, resolution and number of bins.
	private boolean areParametersLegal(List<ValidObservation> obs) {
		legalParams = true;

		List<ITextComponent> textFields = new ArrayList<ITextComponent>();
		TextField binsField = new TextField("Bins", bins + "", false, false,
				TextField.Kind.LINE);
		textFields.add(binsField);

		List<NumberField> numFields = new ArrayList<NumberField>();

		double days = obs.get(obs.size() - 1).getJD() - obs.get(0).getJD();
		NumberField minPeriodField = new NumberField("Minimum Period", 0.01,
				days, minPeriod);
		numFields.add(minPeriodField);

		NumberField maxPeriodField = new NumberField("Maximum Period", 0.0,
				days, maxPeriod == 0.0 ? days : maxPeriod);
		numFields.add(maxPeriodField);

		NumberField resolutionField = new NumberField("Resolution", 0.0, 1.0,
				resolution);
		numFields.add(resolutionField);

		MultiEntryComponentDialog dlg = new MultiEntryComponentDialog(
				"AoV Parameters", textFields, numFields);

		cancelled = dlg.isCancelled();

		if (!cancelled) {

			try {
				bins = Integer.parseInt(binsField.getValue());
				if (bins <= 0) {
					MessageBox.showErrorDialog("AoV Parameters",
							"Number of bins must be more than zero");
					legalParams = false;
				}
			} catch (Exception e) {
				legalParams = false;
			}

			minPeriod = minPeriodField.getValue();
			maxPeriod = maxPeriodField.getValue();
			resolution = resolutionField.getValue();

			if (minPeriod > maxPeriod) {
				MessageBox
						.showErrorDialog("AoV Parameters",
								"Minimum period must be less than or equal to maximum period");
				legalParams = false;
			}

			if (resolution <= 0) {
				MessageBox.showErrorDialog("AoV Parameters",
						"Resolution must be between 0 and 1");
				legalParams = false;
			}
		}

		return legalParams;
	}

	@Override
	public void interrupt() {
		interrupted = true;
	}

	@Override
	protected void newStarAction(NewStarMessage message) {
		reset();
	}

	@Override
	public void reset() {
		cancelled = false;
		legalParams = false;
		interrupted = false;
		minPeriod = 0.1;
		maxPeriod = 0.0;
		resolution = 0.1;
		bins = 10;
	}
}
