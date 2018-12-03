/**
 * VStar: a statistical analysis tool for variable star data.
 * Copyright (C) 2018  AAVSO (http://www.aavso.org/)
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
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JDialog;
import javax.swing.JPanel;

import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.exception.AlgorithmError;
import org.aavso.tools.vstar.exception.CancellationException;
import org.aavso.tools.vstar.plugin.PluginComponentFactory;
import org.aavso.tools.vstar.plugin.period.PeriodAnalysisComponentFactory;
import org.aavso.tools.vstar.plugin.period.PeriodAnalysisDialogBase;
import org.aavso.tools.vstar.plugin.period.PeriodAnalysisPluginBase;
import org.aavso.tools.vstar.ui.NamedComponent;
import org.aavso.tools.vstar.ui.dialog.DoubleField;
import org.aavso.tools.vstar.ui.dialog.ITextComponent;
import org.aavso.tools.vstar.ui.dialog.MessageBox;
import org.aavso.tools.vstar.ui.dialog.MultiEntryComponentDialog;
import org.aavso.tools.vstar.ui.dialog.period.PeriodAnalysis2DChartPane;
import org.aavso.tools.vstar.ui.dialog.period.PeriodAnalysisDataTablePane;
import org.aavso.tools.vstar.ui.dialog.period.PeriodAnalysisTopHitsTablePane;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.message.NewStarMessage;
import org.aavso.tools.vstar.ui.mediator.message.PeriodAnalysisSelectionMessage;
import org.aavso.tools.vstar.ui.model.list.PeriodAnalysisDataTableModel;
import org.aavso.tools.vstar.ui.model.plot.PeriodAnalysis2DPlotModel;
import org.aavso.tools.vstar.util.Pair;
import org.aavso.tools.vstar.util.model.Harmonic;
import org.aavso.tools.vstar.util.model.PeriodAnalysisDerivedMultiPeriodicModel;
import org.aavso.tools.vstar.util.notification.Listener;
import org.aavso.tools.vstar.util.period.IPeriodAnalysisAlgorithm;
import org.aavso.tools.vstar.util.period.IPeriodAnalysisDatum;
import org.aavso.tools.vstar.util.period.PeriodAnalysisCoordinateType;
import org.aavso.tools.vstar.util.period.dcdft.PeriodAnalysisDataPoint;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.DatasetRenderingOrder;

/**
 * Looks for minimum magnitude scatter in folded lightcurve to look for best
 * period.
 * 
 * This is a port of Jeff Byron's Period Finder C code.
 * 
 * TODO:<br/>
 * o create a model from segments? o interrupted!
 */
public class MinimumScatterPeriodFinder extends PeriodAnalysisPluginBase {

	private final static int MAX_TOP_HITS = 20;

	// TODO: left this limit and remove second
	private final static int MAX_OBS = 20000;
	private final static int ASTERISK_COUNTER_LIMIT = 1000;

	private boolean firstInvocation;
	private boolean interrupted;
	private boolean cancelled;
	private boolean legalParams;

	private double filter;
	private Double minPeriod, maxPeriod, resolution;

	private IPeriodAnalysisAlgorithm algorithm;

	private PeriodAnalysisCoordinateType SCATTER, SEGMENT_SUM;

	private int observations;
	private double[] obsTime, mag, phase;

	/**
	 * Constructor
	 */
	public MinimumScatterPeriodFinder() {
		super();
		firstInvocation = true;
		reset();
	}

	@Override
	public String getDescription() {
		return "Minimum Scatter Period Finder";
	}

	@Override
	public String getDisplayName() {
		return "Minimum Scatter Period Finder";
	}

	@Override
	public void executeAlgorithm(List<ValidObservation> obs)
			throws AlgorithmError, CancellationException {

		if (firstInvocation) {
			Mediator.getInstance().getNewStarNotifier()
					.addListener(getNewStarListener());

			SCATTER = PeriodAnalysisCoordinateType.create("Scatter");
			SEGMENT_SUM = PeriodAnalysisCoordinateType
					.create("Sum of segments");

			firstInvocation = false;
		}

		algorithm = new PeriodFinderAlgorithm(obs);
		algorithm.execute();
	}

	@Override
	public JDialog getDialog(SeriesType sourceSeriesType) {
		return interrupted || cancelled ? null : new PeriodAnalysisDialog(
				sourceSeriesType);
	}

	@SuppressWarnings("serial")
	class PeriodAnalysisDialog extends PeriodAnalysisDialogBase implements
			Listener<PeriodAnalysisSelectionMessage> {

		private double period;
		private SeriesType sourceSeriesType;
		private IPeriodAnalysisDatum selectedDataPoint;

		private PeriodAnalysisDataTablePane resultsTablePane;
		private PeriodAnalysisTopHitsTablePane topHitsTablePane;
		private PeriodAnalysis2DChartPane powerPlotPane;
		private PeriodAnalysis2DChartPane topHitsPowerPlotPane;
		private PeriodAnalysis2DChartPane segmentSumPlotPane;
		private PeriodAnalysis2DChartPane topHitsSegmentSumPlotPane;
		private PeriodAnalysis2DChartPane scatterPlotPane;
		private PeriodAnalysis2DChartPane topHitsPlotPane;

		public PeriodAnalysisDialog(SeriesType sourceSeriesType) {
			super("Period Finder", false, true, false);

			this.sourceSeriesType = sourceSeriesType;

			prepareDialog();

			this.setNewPhasePlotButtonState(false);

			startup(); // Note: why does base class not call this in
			// prepareDialog()?
		}

		@Override
		protected Component createContent() {
			String title = "Periodogram";

			// Power plot model and plot
			if (false) {
				PeriodAnalysis2DPlotModel powerPlotModel = new PeriodAnalysis2DPlotModel(
						algorithm.getResultSeries(),
						PeriodAnalysisCoordinateType.PERIOD,
						PeriodAnalysisCoordinateType.POWER, false);

				powerPlotPane = PeriodAnalysisComponentFactory.createLinePlot(
						title + ": Segment Sums vs Period",
						sourceSeriesType.getDescription(), powerPlotModel,
						false);

				PeriodAnalysis2DPlotModel powerPlotTopHitsModel = new PeriodAnalysis2DPlotModel(
						algorithm.getTopHits(),
						PeriodAnalysisCoordinateType.PERIOD,
						PeriodAnalysisCoordinateType.POWER, false);

				topHitsPowerPlotPane = PeriodAnalysisComponentFactory
						.createScatterPlot(title,
								sourceSeriesType.getDescription(),
								powerPlotTopHitsModel, true);

				JFreeChart powerChart = topHitsPowerPlotPane.getChart();
				powerChart.getXYPlot().setDataset(
						PeriodAnalysis2DChartPane.DATA_SERIES, powerPlotModel);
				powerChart.getXYPlot().setDataset(
						PeriodAnalysis2DChartPane.TOP_HIT_SERIES,
						powerPlotTopHitsModel);
				powerChart.getXYPlot().setRenderer(
						PeriodAnalysis2DChartPane.DATA_SERIES,
						powerPlotPane.getChart().getXYPlot().getRenderer());
				powerChart.getXYPlot().setDatasetRenderingOrder(
						DatasetRenderingOrder.REVERSE);

				powerPlotPane = topHitsPowerPlotPane;
			}

			// Segment sum plot model and plot (all results)
			PeriodAnalysis2DPlotModel segmentSumPlotModel = new PeriodAnalysis2DPlotModel(
					algorithm.getResultSeries(),
					PeriodAnalysisCoordinateType.PERIOD, SEGMENT_SUM, false);

			segmentSumPlotPane = PeriodAnalysisComponentFactory.createLinePlot(
					title + ": Segment Sum vs Period",
					sourceSeriesType.getDescription(), segmentSumPlotModel,
					false);

			// Segment sum plot model and plot (top hits)
			PeriodAnalysis2DPlotModel segmentSumTopHitsPlotModel = new PeriodAnalysis2DPlotModel(
					algorithm.getTopHits(),
					PeriodAnalysisCoordinateType.PERIOD, SEGMENT_SUM, false);

			topHitsSegmentSumPlotPane = PeriodAnalysisComponentFactory
					.createScatterPlot(title,
							sourceSeriesType.getDescription(),
							segmentSumTopHitsPlotModel, true);

			JFreeChart segmentSumChart = topHitsSegmentSumPlotPane.getChart();
			segmentSumChart.getXYPlot().setDataset(
					PeriodAnalysis2DChartPane.DATA_SERIES, segmentSumPlotModel);
			segmentSumChart.getXYPlot().setDataset(
					PeriodAnalysis2DChartPane.TOP_HIT_SERIES,
					segmentSumTopHitsPlotModel);
			segmentSumChart.getXYPlot().setRenderer(
					PeriodAnalysis2DChartPane.DATA_SERIES,
					segmentSumPlotPane.getChart().getXYPlot().getRenderer());
			segmentSumChart.getXYPlot().setDatasetRenderingOrder(
					DatasetRenderingOrder.REVERSE);

			segmentSumPlotPane = topHitsSegmentSumPlotPane;

			// Scatter plot model and plot (all results)
			PeriodAnalysis2DPlotModel scatterPlotModel = new PeriodAnalysis2DPlotModel(
					algorithm.getResultSeries(),
					PeriodAnalysisCoordinateType.PERIOD, SCATTER, false);

			scatterPlotPane = PeriodAnalysisComponentFactory.createLinePlot(
					title + ": Scatter vs Period",
					sourceSeriesType.getDescription(), scatterPlotModel, false);

			// Scatter plot model and plot (top hits)
			PeriodAnalysis2DPlotModel scatterTopHitsPlotModel = new PeriodAnalysis2DPlotModel(
					algorithm.getTopHits(),
					PeriodAnalysisCoordinateType.PERIOD, SCATTER, false);

			topHitsPlotPane = PeriodAnalysisComponentFactory.createScatterPlot(
					title, sourceSeriesType.getDescription(),
					scatterTopHitsPlotModel, true);

			// Add the scatter line plot's model to the scatter plot.
			// Render the scatter plot last so the "handles" will be
			// the first items selected by the mouse.
			JFreeChart scatterChart = topHitsPlotPane.getChart();
			scatterChart.getXYPlot().setDataset(
					PeriodAnalysis2DChartPane.DATA_SERIES, scatterPlotModel);
			scatterChart.getXYPlot().setDataset(
					PeriodAnalysis2DChartPane.TOP_HIT_SERIES,
					scatterTopHitsPlotModel);
			scatterChart.getXYPlot().setRenderer(
					PeriodAnalysis2DChartPane.DATA_SERIES,
					scatterPlotPane.getChart().getXYPlot().getRenderer());
			scatterChart.getXYPlot().setDatasetRenderingOrder(
					DatasetRenderingOrder.REVERSE);

			scatterPlotPane = topHitsPlotPane;

			// Full results table
			PeriodAnalysisCoordinateType[] columns = {
					PeriodAnalysisCoordinateType.FREQUENCY,
					PeriodAnalysisCoordinateType.PERIOD,
					// PeriodAnalysisCoordinateType.POWER,
					SCATTER, SEGMENT_SUM };

			// Note: algorithm won't be used (?) in this case but we must pass
			// it along.

			// TODO: subclass PeriodAnalysisDataTablePane to have no model
			// button

			PeriodAnalysisDataTableModel dataTableModel = new PeriodAnalysisDataTableModel(
					columns, algorithm.getResultSeries());
			resultsTablePane = new NoModelPeriodAnalysisDataTablePane(
					dataTableModel, algorithm);

			// Note: algorithm won't be used (?) in this case but we must pass
			// it along.

			PeriodAnalysisDataTableModel topHitsModel = new PeriodAnalysisDataTableModel(
					columns, algorithm.getTopHits());
			topHitsTablePane = new NoModelPeriodAnalysisTopHitsTablePane(
					topHitsModel, dataTableModel, algorithm);

			// Return tabbed pane of plot and period display component.
			return PluginComponentFactory.createTabs(/*
													 * new NamedComponent(
													 * "Power vs Period",
													 * powerPlotPane),
													 */new NamedComponent(
					"Scatter vs Period", scatterPlotPane), new NamedComponent(
					"Segment Sum vs Period", segmentSumPlotPane),
					new NamedComponent("Scatter vs Period", scatterPlotPane),
					new NamedComponent("Segment Sum vs Period",
							segmentSumPlotPane), new NamedComponent("Results",
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
		public void startup() {
			Mediator.getInstance().getPeriodAnalysisSelectionNotifier()
					.addListener(this);

			resultsTablePane.startup();
			topHitsTablePane.startup();
			scatterPlotPane.startup();
		}

		@Override
		public void cleanup() {
			Mediator.getInstance().getPeriodAnalysisSelectionNotifier()
					.removeListenerIfWilling(this);

			resultsTablePane.cleanup();
			topHitsTablePane.cleanup();
			scatterPlotPane.cleanup();
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

		@Override
		protected void findHarmonicsButtonAction() {
			// Do nothing since we don't include a find-harmonics button for
			// AoV.
		}
	}

	// The AoV algorithm implementation.
	class PeriodFinderAlgorithm implements IPeriodAnalysisAlgorithm {

		private List<ValidObservation> obs;

		private LinkedList<Double> frequencies;
		private ArrayList<Double> orderedFrequencies;

		private LinkedList<Double> periods;
		private ArrayList<Double> orderedPeriods;

		private LinkedList<Double> scatterValues;
		private ArrayList<Double> orderedScatterValues;

		private LinkedList<Double> segmentSumValues;
		private ArrayList<Double> orderedSegmentSumValues;

		private List<Double> power;

		public PeriodFinderAlgorithm(List<ValidObservation> obs) {
			this.obs = obs;

			frequencies = new LinkedList<Double>();
			orderedFrequencies = new ArrayList<Double>();

			periods = new LinkedList<Double>();
			orderedPeriods = new ArrayList<Double>();

			scatterValues = new LinkedList<Double>();
			orderedScatterValues = new ArrayList<Double>();

			segmentSumValues = new LinkedList<Double>();
			orderedSegmentSumValues = new ArrayList<Double>();
		}

		@Override
		public String getRefineByFrequencyName() {
			return "None";
		}

		@Override
		public Map<PeriodAnalysisCoordinateType, List<Double>> getResultSeries() {
			Map<PeriodAnalysisCoordinateType, List<Double>> results = new LinkedHashMap<PeriodAnalysisCoordinateType, List<Double>>();

			results.put(PeriodAnalysisCoordinateType.FREQUENCY, frequencies);
			results.put(PeriodAnalysisCoordinateType.PERIOD, periods);
			// results.put(PeriodAnalysisCoordinateType.POWER, power);
			results.put(SCATTER, scatterValues);
			results.put(SEGMENT_SUM, segmentSumValues);

			return results;
		}

		@Override
		public Map<PeriodAnalysisCoordinateType, List<Double>> getTopHits() {
			Map<PeriodAnalysisCoordinateType, List<Double>> topHits = new LinkedHashMap<PeriodAnalysisCoordinateType, List<Double>>();

			topHits.put(PeriodAnalysisCoordinateType.FREQUENCY,
					orderedFrequencies);
			topHits.put(PeriodAnalysisCoordinateType.PERIOD, orderedPeriods);
			// topHits.put(PeriodAnalysisCoordinateType.POWER, power.stream()
			// .sorted().limit(MAX_TOP_HITS).collect(Collectors.toList()));
			topHits.put(SCATTER, orderedScatterValues);
			topHits.put(SEGMENT_SUM, orderedSegmentSumValues);

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
				// without disturbing the original observation object.
				List<ValidObservation> phObs = new ArrayList<ValidObservation>();

				// TODO: cache these by JD range between new star resets...

				interrupted = false;

				inputData(obs);

				stepThroughPeriods(minPeriod, maxPeriod, resolution, filter);

				// for (ValidObservation ob : obs) {
				// if (interrupted)
				// break;
				//
				// ValidObservation phOb = new ValidObservation();
				//
				// double jd = ob.getDateInfo().getJulianDay();
				// phOb.setDateInfo(new DateInfo(jd));
				//
				// Magnitude mag = new Magnitude(ob.getMagnitude()
				// .getMagValue(), ob.getMagnitude().getUncertainty());
				// phOb.setMagnitude(mag);
				//
				// phObs.add(phOb);
				// }

				// Choose an epoch value.
				// double epoch = PhaseCalcs.epochStrategyMap.get("alpha")
				// .determineEpoch(phObs);

				// Iterate over the periods in the range at the specified
				// resolution.

				// TODO: multi-core approach => iterate over a subset of the
				// period range but over all observations, where the full set
				// is copied for each core (set phases, sort mutate obs and
				// list...); top-hits will have to be combined and ordered once
				// at end as part of or before prune operation

				// for (double period = minPeriod; period <= maxPeriod; period
				// += resolution) {
				// if (interrupted)
				// break;
				//
				// PhaseCalcs.setPhases(phObs, epoch, period);
				//
				// Collections.sort(phObs, StandardPhaseComparator.instance);
				//
				// // Note: 1 / bins = 1 cycle divided into N bins
				// BinningResult binningResult = DescStats
				// .createSymmetricBinnedObservations(phObs,
				// PhaseTimeElementEntity.instance, 1.0 / bins);
				//
				// // Collect results
				// frequencies.add(1.0 / period);
				// periods.add(period);
				// scatterValues.add(fValue);
				//
				// updateOrderedValues();
				// }

				// Create a power series where elements are a fraction of
				// maximum scatter subtracted from one such that values from
				// 0 to 1 represent lower scatter and higher power. The
				// resulting data and plot will reveal a power spectrum that is
				// of the kind expected by a user of DCDFT or AoV and whose
				// data points are more easily selectable. We sort the data
				// double maxScatter = Collections.max(scatterValues);
				// power = scatterValues.stream().map((n) -> {
				// return 1 - n / maxScatter;
				// }).sorted(Collections.reverseOrder())
				// .collect(Collectors.toList());

				pruneTopHits();
			}
		}

		// Steps through the periods to test, calling scatterCalc() for each
		// test
		// period. Writes data to output files and closes them.
		void stepThroughPeriods(double minPeriod, double maxPeriod,
				double periodStep, double filter) {
			int i, counter;
			double period, trialPeriod, bestMatch, scatter, sumSegs;
			// time_t startTime, endTime;
			// char* c_time_string;

			period = 0;

			// Output data
			// TODO: could use MAXINT
			bestMatch = 1000000000.0; // Initialise to much more than expected
										// final
										// value
			counter = 1;
			for (trialPeriod = minPeriod; trialPeriod < maxPeriod + periodStep; trialPeriod += periodStep) {
				if (++counter > ASTERISK_COUNTER_LIMIT) {
					// printf("*");
					counter = 1;
				}

				Pair<Double, Double> scatterPair = scatterCalc(trialPeriod);
				sumSegs = scatterPair.first;
				scatter = scatterPair.second;

				if ((scatter <= filter) || (filter < 0.1)) {
					// System.out.printf("%.10f\t%f\n", trialPeriod, scatter);
				}

				if (scatter < bestMatch) {
					bestMatch = scatter;
					period = trialPeriod;
					// Collect results
					frequencies.addFirst(1.0 / period);
					periods.addFirst(period);
					scatterValues.addFirst(scatter);
					segmentSumValues.addFirst(sumSegs);
					updateOrderedValues();
				}
			}
		}

		private void updateOrderedValues() {
			if (orderedFrequencies.isEmpty()) {
				orderedFrequencies.add(frequencies.get(0));
				orderedPeriods.add(periods.get(0));
				orderedScatterValues.add(scatterValues.get(0));
				orderedSegmentSumValues.add(segmentSumValues.get(0));
			} else {
				int i = 0;

				double frequency = frequencies.get(i);
				double period = periods.get(i);
				double scatterValue = scatterValues.get(i);
				double segmentSumValue = segmentSumValues.get(i);

				// Starting from highest scatter value, find index to insert
				// value and...
				int index = 0;
				for (int j = 0; j < orderedScatterValues.size(); j++) {
					if (scatterValue > orderedScatterValues.get(j)) {
						// Insertion index is one after the matched element's
						// index since the list's elements are in ascending
						// order.
						index++;
					}
				}

				// ...apply to all ordered collections.
				if (index >= 0) {
					orderedFrequencies.add(index, frequency);
					orderedPeriods.add(index, period);
					orderedScatterValues.add(index, scatterValue);
					orderedSegmentSumValues.add(index, segmentSumValue);
				} else {
					orderedFrequencies.add(0, frequency);
					orderedPeriods.add(0, period);
					orderedScatterValues.add(0, scatterValue);
					orderedSegmentSumValues.add(0, segmentSumValue);
				}
			}
		}

		private void pruneTopHits() {
			if (periods.size() > MAX_TOP_HITS) {
				orderedFrequencies = new ArrayList<Double>(
						orderedFrequencies.subList(0, MAX_TOP_HITS));

				orderedPeriods = new ArrayList<Double>(orderedPeriods.subList(
						0, MAX_TOP_HITS));

				orderedScatterValues = new ArrayList<Double>(
						orderedScatterValues.subList(0, MAX_TOP_HITS));

				orderedSegmentSumValues = new ArrayList<Double>(
						orderedSegmentSumValues.subList(0, MAX_TOP_HITS));
			}
		}

		@Override
		public void interrupt() {
			interrupted = true;
		}
	}

	// Ask user for period min, max, resolution and number of bins.
	private boolean areParametersLegal(List<ValidObservation> obs) {
		legalParams = true;

		List<ITextComponent<?>> fields = new ArrayList<ITextComponent<?>>();

		// / double days = obs.get(obs.size() - 1).getJD() - obs.get(0).getJD();
		DoubleField minPeriodField = new DoubleField("Minimum Period", 0.0,
				null, minPeriod);
		fields.add(minPeriodField);

		DoubleField maxPeriodField = new DoubleField("Maximum Period", 0.0,
				null, maxPeriod);
		fields.add(maxPeriodField);

		DoubleField resolutionField = new DoubleField("Resolution", 0.0, 1.0,
				resolution);
		fields.add(resolutionField);

		// Set<String> binSet = new TreeSet<String>();
		// binSet.add("4");
		// binSet.add("10");
		// binSet.add("20");
		// binSet.add("50");
		// SelectableTextField binsField = new SelectableTextField("Bins",
		// binSet);

		DoubleField filterField = new DoubleField("Filter", 0.0, 100.0, filter);
		fields.add(filterField);

		MultiEntryComponentDialog dlg = new MultiEntryComponentDialog(
				"PeriodFinder Parameters", fields);

		cancelled = dlg.isCancelled();

		if (!cancelled) {

			try {
				filter = filterField.getValue();
//				if (filter > 1) {
//					MessageBox.showErrorDialog("PeriodFinder Parameters",
//							"Filter must be less than one");
//					legalParams = false;
//				}
			} catch (Exception e) {
				legalParams = false;
			}

			minPeriod = minPeriodField.getValue();
			maxPeriod = maxPeriodField.getValue();
			resolution = resolutionField.getValue();

			if (minPeriod >= maxPeriod) {
				MessageBox
						.showErrorDialog("PeriodFinder Parameters",
								"Minimum period must be less than or equal to maximum period");
				legalParams = false;
			}

			if (resolution <= 0.0) {
				MessageBox.showErrorDialog("PeriodFinder Parameters",
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
		minPeriod = 0.0;
		maxPeriod = 0.0;
		resolution = 0.1;
		filter = 0.1; // TODO: check Jeff's default
	}

	// Helpers

	// Reads in observation data (and closes input file).
	private void inputData(List<ValidObservation> obs) {
		int i = 0;
		obsTime = new double[obs.size()];
		mag = new double[obs.size()];
		phase = new double[obs.size()];
		observations = obs.size();

		// TODO: take the opportunity to use this for multi-threading
		for (ValidObservation ob : obs) {
			obsTime[i] = ob.getJD();
			mag[i] = ob.getMag();
			i++;
		}
	}

	// For a given period, this routine calculates the scatter of magnitude
	// (comparing
	// adjacent points). It also calculates the sum of the segments connecting
	// points on a phase - magnitude plot.
	// At the time of writing, it was not clear which would be the better
	// technique.
	// (In fact, the former was thought to be at least as good and less
	// computation.
	// Initially, experience seemed to be indicating that sum-of-segments worked
	// far better,
	// but when errors (uncertainties) are considered, the "improvement" is
	// probably illusionary.)
	// But tests have indicated that disabling calculation of sum-of-segments
	// makes very little
	// difference to the overall computation time.
	private Pair<Double, Double> scatterCalc(double period/* , double *seg */) {
		int i;
		double seg, scatter;
		// Generate phase data
		// TODO: use my code?
		for (i = 0; i < observations; i++) {
			phase[i] = (obsTime[i] / period) - Math.floor(obsTime[i] / period);
			// System.out.printf("Period = %f\tTime = %f\tPhase = %f\n", period,
			// obsTime[i], phase[i]);
		}

		// TODO: use my code?
		sort(/* period */);

		scatter = 0;
		seg = 0;
		// TODO: i can't start at 0 else i-1 = -1
		for (i = 1; i < observations; i++) {
			seg += Math.sqrt((phase[i] - phase[i - 1])
					* (phase[i] - phase[i - 1]) + (mag[i] - mag[i - 1])
					* (mag[i] - mag[i - 1]));

			scatter += Math.abs(mag[i] - mag[i - 1]);
		}

		return new Pair<Double, Double>(seg, scatter);
	}

	// Performs bubble-sort of phase-folded data.
	// TODO: replace with efficient sort as per execute() but in scatterCalc()?
	void sort(/* double period */) {
		int i, changes;
		double tempPhase, tempTime, tempMag;
		do {
			changes = 0;
			for (i = 0; i < observations - 1; i++) {
				if (phase[i] > phase[i + 1]) {
					tempPhase = phase[i + 1];
					tempTime = obsTime[i + 1];
					tempMag = mag[i + 1];
					phase[i + 1] = phase[i];
					obsTime[i + 1] = obsTime[i];
					mag[i + 1] = mag[i];
					phase[i] = tempPhase;
					obsTime[i] = tempTime;
					mag[i] = tempMag;
					changes++;
				}
			}
		} while (changes > 0);
	}
}
