/**
 * VStar: a statistical analysis tool for variable star data.
 * Copyright (C) 2015  AAVSO (http://www.aavso.org/)
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
import java.util.LinkedHashMap;
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
import org.aavso.tools.vstar.external.lib.PiecewiseLinearModel;
import org.aavso.tools.vstar.plugin.PluginComponentFactory;
import org.aavso.tools.vstar.plugin.period.PeriodAnalysisComponentFactory;
import org.aavso.tools.vstar.plugin.period.PeriodAnalysisDialogBase;
import org.aavso.tools.vstar.plugin.period.PeriodAnalysisPluginBase;
import org.aavso.tools.vstar.ui.NamedComponent;
import org.aavso.tools.vstar.ui.dialog.DoubleField;
import org.aavso.tools.vstar.ui.dialog.ITextComponent;
import org.aavso.tools.vstar.ui.dialog.IntegerField;
import org.aavso.tools.vstar.ui.dialog.MessageBox;
import org.aavso.tools.vstar.ui.dialog.MultiEntryComponentDialog;
import org.aavso.tools.vstar.ui.dialog.period.PeriodAnalysis2DChartPane;
import org.aavso.tools.vstar.ui.dialog.period.PeriodAnalysisDataTablePane;
import org.aavso.tools.vstar.ui.dialog.period.PeriodAnalysisTopHitsTablePane;
import org.aavso.tools.vstar.ui.mediator.AnalysisType;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.message.NewStarMessage;
import org.aavso.tools.vstar.ui.mediator.message.PeriodAnalysisSelectionMessage;
import org.aavso.tools.vstar.ui.model.list.PeriodAnalysisDataTableModel;
import org.aavso.tools.vstar.ui.model.plot.PeriodAnalysis2DPlotModel;
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
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.DatasetRenderingOrder;

/**
 * This plug-in implements the Analysis of Variance period search algorithm that
 * applies an ANOVA test to phased bins over a period range.
 * 
 * References:<br/>
 * http://articles.adsabs.harvard.edu/cgi-bin/nph-iarticle_query?1989MNRAS.241.
 * .153
 * S&amp;data_type=PDF_HIGH&amp;whole_paper=YES&amp;type=PRINTER&amp;filetype
 * =.pdf On the advantage of using analysis of variance in period search A.
 * Schwarzenberg-Czerny (1989)
 * 
 * http://iopscience.iop.org/1538-4357/460/2/L107/pdf/1538-4357_460_2_L107.pdf
 * 
 * o Top hits performance!<br/>
 * o Create a piecewise model from means (see Foster)? Polynomial? Spline?<br/>
 * o Parallelise!<br/>
 */
public class AoVPeriodSearch extends PeriodAnalysisPluginBase {

    private final static int MAX_TOP_HITS = 20;

    private boolean firstInvocation;
    private boolean interrupted;
    private boolean cancelled;
    private boolean legalParams;

    private List<ValidObservation> obs;

    private int bins;
    private Double minPeriod, maxPeriod, resolution;

    private IPeriodAnalysisAlgorithm algorithm;

    private PeriodAnalysisCoordinateType F_STATISTIC;
    private PeriodAnalysisCoordinateType P_VALUE;

    /**
     * Constructor
     */
    public AoVPeriodSearch() {
        super();
        firstInvocation = true;
        reset();
    }

    @Override
    public String getDescription() {
        return "AoV period search";
    }

    @Override
    public String getDisplayName() {
        return "AoV with Period Range";
    }

    /**
     * @see org.aavso.tools.vstar.plugin.IPlugin#getDocName()
     */
    @Override
    public String getDocName() {
        return "AoV Period Analysis Plug-In.pdf";
    }

    @Override
    public void executeAlgorithm(List<ValidObservation> obs) throws AlgorithmError, CancellationException {

        this.obs = obs;

        if (firstInvocation) {
            Mediator.getInstance().getNewStarNotifier().addListener(getNewStarListener());

            F_STATISTIC = PeriodAnalysisCoordinateType.create("F-statistic");
            P_VALUE = PeriodAnalysisCoordinateType.create("p-value");

            firstInvocation = false;
        }

        algorithm = new AoVAlgorithm(obs);
        algorithm.execute();
    }

    @Override
    public JDialog getDialog(SeriesType sourceSeriesType) {
        return interrupted || cancelled ? null : new PeriodAnalysisDialog(sourceSeriesType);
    }

    @SuppressWarnings("serial")
    class PeriodAnalysisDialog extends PeriodAnalysisDialogBase implements Listener<PeriodAnalysisSelectionMessage> {

        private double period;
        private SeriesType sourceSeriesType;
        private IPeriodAnalysisDatum selectedDataPoint;

        private PeriodAnalysisDataTablePane resultsTablePane;
        private PeriodAnalysisTopHitsTablePane topHitsTablePane;
        private PeriodAnalysis2DChartPane plotPane;
        private PeriodAnalysis2DChartPane topHitsPlotPane;

        public PeriodAnalysisDialog(SeriesType sourceSeriesType) {
            super("AoV", false, true, false);

            this.sourceSeriesType = sourceSeriesType;

            prepareDialog();

            this.setNewPhasePlotButtonState(false);

            startup(); // Note: why does base class not call this in
            // prepareDialog()?
        }

        @Override
        protected Component createContent() {
            String title = "AoV Periodogram";

            PeriodAnalysis2DPlotModel dataPlotModel = new PeriodAnalysis2DPlotModel(algorithm.getResultSeries(),
                    PeriodAnalysisCoordinateType.PERIOD, F_STATISTIC, false);

            plotPane = PeriodAnalysisComponentFactory.createLinePlot(title, sourceSeriesType.getDescription(),
                    dataPlotModel, false);

            PeriodAnalysis2DPlotModel topHitsPlotModel = new PeriodAnalysis2DPlotModel(algorithm.getTopHits(),
                    PeriodAnalysisCoordinateType.PERIOD, F_STATISTIC, false);

            topHitsPlotPane = PeriodAnalysisComponentFactory.createScatterPlot(title, sourceSeriesType.getDescription(),
                    topHitsPlotModel, false);

            // Add the above line plot's model to the scatter plot.
            // Render the scatter plot last so the "handles" will be
            // the first items selected by the mouse.
            JFreeChart chart = topHitsPlotPane.getChart();
            chart.getXYPlot().setDataset(PeriodAnalysis2DChartPane.DATA_SERIES, dataPlotModel);
            chart.getXYPlot().setDataset(PeriodAnalysis2DChartPane.TOP_HIT_SERIES, topHitsPlotModel);
            chart.getXYPlot().setRenderer(PeriodAnalysis2DChartPane.DATA_SERIES,
                    plotPane.getChart().getXYPlot().getRenderer());
            chart.getXYPlot().setDatasetRenderingOrder(DatasetRenderingOrder.REVERSE);

            plotPane = topHitsPlotPane;

            // Full results table
            PeriodAnalysisCoordinateType[] columns = { PeriodAnalysisCoordinateType.FREQUENCY,
                    PeriodAnalysisCoordinateType.PERIOD, F_STATISTIC, P_VALUE };

            PeriodAnalysisDataTableModel dataTableModel = new PeriodAnalysisDataTableModel(columns,
                    algorithm.getResultSeries());
            resultsTablePane = new NoModelPeriodAnalysisDataTablePane(dataTableModel, algorithm);

            PeriodAnalysisDataTableModel topHitsModel = new PeriodAnalysisDataTableModel(columns,
                    algorithm.getTopHits());
            topHitsTablePane = new AoVPeriodAnalysisTopHitsTablePane(obs, topHitsModel, dataTableModel, algorithm);

            // Return tabbed pane of plot and period display component.
            return PluginComponentFactory.createTabs(new NamedComponent("Periodogram", plotPane),
                    new NamedComponent("Results", resultsTablePane), new NamedComponent("Top Hits", topHitsTablePane));
        }

        // Send a period change message when the new-phase-plot button is
        // clicked.
        @Override
        protected void newPhasePlotButtonAction() {
            sendPeriodChangeMessage(period);
        }

        @Override
        public void startup() {
            Mediator.getInstance().getPeriodAnalysisSelectionNotifier().addListener(this);

            resultsTablePane.startup();
            topHitsTablePane.startup();
            plotPane.startup();
        }

        @Override
        public void cleanup() {
            Mediator.getInstance().getPeriodAnalysisSelectionNotifier().removeListenerIfWilling(this);

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
        }

        // ** No model result and top-hit panes **

        class NoModelPeriodAnalysisDataTablePane extends PeriodAnalysisDataTablePane {

            public NoModelPeriodAnalysisDataTablePane(PeriodAnalysisDataTableModel model,
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

        class AoVPeriodAnalysisTopHitsTablePane extends PeriodAnalysisTopHitsTablePane {

            private List<ValidObservation> obs;

            public AoVPeriodAnalysisTopHitsTablePane(List<ValidObservation> obs,
                    PeriodAnalysisDataTableModel topHitsModel, PeriodAnalysisDataTableModel fullDataModel,
                    IPeriodAnalysisAlgorithm algorithm) {
                super(topHitsModel, fullDataModel, algorithm);
                this.obs = obs;
            }

            @Override
            protected void modelAction(List<PeriodAnalysisDataPoint> dataPoints) {
                final JPanel parent = this;

                try {
                    // Compute binning result again for selected top-hit period.
                    double period = dataPoints.get(0).getPeriod();
                    double epoch = PhaseCalcs.epochStrategyMap.get("alpha").determineEpoch(obs);

                    Mediator.getInstance().createPhasePlot(period, epoch);
                    Mediator.getInstance().waitForJobCompletion();

                    Mediator.getInstance().changeAnalysisType(AnalysisType.PHASE_PLOT);

                    // Note: 1 / bins = 1 cycle divided into N bins
                    BinningResult binningResult = DescStats.createSymmetricBinnedObservations(obs,
                            PhaseTimeElementEntity.instance, 1.0 / bins);

                    List<ValidObservation> meanObs = binningResult.getMeanObservations();

                    PiecewiseLinearModel model = new PiecewiseLinearModel(obs, meanObs);

                    Mediator.getInstance().performModellingOperation(model);
                } catch (Exception ex) {
                    MessageBox.showErrorDialog(parent, "Modelling", ex.getLocalizedMessage());
                }
            }
        }

        @Override
        protected void findHarmonicsButtonAction() {
            // Do nothing since we don't include a find-harmonics button for
            // AoV.
        }
    }

    // The AoV algorithm implementation.
    class AoVAlgorithm implements IPeriodAnalysisAlgorithm {

        private List<ValidObservation> obs;

        private List<Double> frequencies;
        private ArrayList<Double> orderedFrequencies;

        private List<Double> periods;
        private ArrayList<Double> orderedPeriods;

        private List<Double> fValues;
        private ArrayList<Double> orderedFValues;

        private List<Double> pValues;
        private ArrayList<Double> orderedPValues;

        // private double smallestFValue;
        // private int smallestValueIndex;

        public AoVAlgorithm(List<ValidObservation> obs) {
            this.obs = obs;

            frequencies = new ArrayList<Double>();
            orderedFrequencies = new ArrayList<Double>();

            periods = new ArrayList<Double>();
            orderedPeriods = new ArrayList<Double>();

            fValues = new ArrayList<Double>();
            orderedFValues = new ArrayList<Double>();

            pValues = new ArrayList<Double>();
            orderedPValues = new ArrayList<Double>();

            // smallestFValue = Double.MAX_VALUE;
            // smallestValueIndex = 0;
        }

        @Override
        public String getRefineByFrequencyName() {
            return null;
        }

        @Override
        public Map<PeriodAnalysisCoordinateType, List<Double>> getResultSeries() {
            Map<PeriodAnalysisCoordinateType, List<Double>> results = new LinkedHashMap<PeriodAnalysisCoordinateType, List<Double>>();

            results.put(PeriodAnalysisCoordinateType.FREQUENCY, frequencies);
            results.put(PeriodAnalysisCoordinateType.PERIOD, periods);
            results.put(F_STATISTIC, fValues);
            results.put(P_VALUE, pValues);

            return results;
        }

        @Override
        public Map<PeriodAnalysisCoordinateType, List<Double>> getTopHits() {
            // TODO: create top hits by sorting doubles in descending order
            // pairs of doubles;
            // limit to MAX_TOP_HITS = 100

            Map<PeriodAnalysisCoordinateType, List<Double>> topHits = new LinkedHashMap<PeriodAnalysisCoordinateType, List<Double>>();

            topHits.put(PeriodAnalysisCoordinateType.FREQUENCY, orderedFrequencies);
            topHits.put(PeriodAnalysisCoordinateType.PERIOD, orderedPeriods);
            topHits.put(F_STATISTIC, orderedFValues);
            topHits.put(P_VALUE, orderedPValues);

            return topHits;
        }

        @Override
        public void multiPeriodicFit(List<Harmonic> harmonics, PeriodAnalysisDerivedMultiPeriodicModel model)
                throws AlgorithmError {
            // TODO: msg box: unsupported
        }

        @Override
        public List<PeriodAnalysisDataPoint> refineByFrequency(List<Double> freqs, List<Double> variablePeriods,
                List<Double> lockedPeriod) throws AlgorithmError {
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
                // TODO: cache these by JD range between new star resets...

                interrupted = false;

                // Duplicate the obs (just JD and mag) so we can set phases
                // without disturbing the original observation object.
                // TODO: for a multi-threaded range-subset approach, we would
                // need to do this once for each thread
                List<ValidObservation> phObs = copyObs(obs);

                // Choose an epoch value.
                double epoch = PhaseCalcs.epochStrategyMap.get("alpha").determineEpoch(phObs);

                // Iterate over the periods in the range at the specified
                // resolution.

                // TODO: multi-core approach => iterate over a subset of the
                // period range but over all observations, where the full set
                // is copied for each core (set phases, sort mutate obs and
                // list...); top-hits will have to be combined and ordered once
                // at end as part of or before prune operation; instead, could
                // just iterate over a subset of observations; this would only
                // give a large speedup if many observations; such a for-loop
                // unrolling would be simpler and less memory intensive though;
                // may be worth trying first

                for (double period = minPeriod; period <= maxPeriod; period += resolution) {
                    if (interrupted)
                        break;

                    PhaseCalcs.setPhases(phObs, epoch, period);

                    Collections.sort(phObs, StandardPhaseComparator.instance);

                    // Note: 1 / bins = 1 cycle divided into N bins
                    BinningResult binningResult = DescStats.createSymmetricBinnedObservations(phObs,
                            PhaseTimeElementEntity.instance, 1.0 / bins);

                    // Collect results
                    // PMAK, Issue #152:
                    // Use fixInf() to prevent
                    // 'java.lang.IllegalArgumentException: Must be finite'
                    // error in AoV chart when period = 0
                    frequencies.add(fixInf(1.0 / period));
                    periods.add(period);
                    fValues.add(fixInf(binningResult.getFValue()));
                    pValues.add(fixInf(binningResult.getPValue()));

                    updateOrderedValues();
                }

                pruneTopHits();
            }
        }

        // replace +-Infinity by NaN
        private double fixInf(double v) {
            if (Double.isInfinite(v))
                return Double.NaN;
            else
                return v;
        }

        private void updateOrderedValues() {
            if (orderedFrequencies.isEmpty()) {
                orderedFrequencies.add(frequencies.get(0));
                orderedPeriods.add(periods.get(0));
                orderedFValues.add(fValues.get(0));
                orderedPValues.add(pValues.get(0));
            } else {
                int i = periods.size() - 1;

                double frequency = frequencies.get(i);
                double period = periods.get(i);
                double fValue = fValues.get(i);
                double pValue = pValues.get(i);

                // Starting from highest fValue, find index to insert value
                // and...
                int index = 0;
                for (int j = 0; j < orderedFValues.size(); j++) {
                    if (fValue < orderedFValues.get(j)) {
                        // Insertion index is one after the matched element's
                        // index since the list's elements are in descending
                        // order.
                        index++;
                    }
                }

                // ...apply to all ordered collections.
                if (index >= 0) {
                    orderedFrequencies.add(index, frequency);
                    orderedPeriods.add(index, period);
                    orderedFValues.add(index, fValue);
                    orderedPValues.add(index, pValue);
                } else {
                    orderedFrequencies.add(0, frequency);
                    orderedPeriods.add(0, period);
                    orderedFValues.add(0, fValue);
                    orderedPValues.add(0, pValue);
                }
            }
        }

        private void pruneTopHits() {
            if (periods.size() > MAX_TOP_HITS) {
                orderedFrequencies = new ArrayList<Double>(orderedFrequencies.subList(0, MAX_TOP_HITS));

                orderedPeriods = new ArrayList<Double>(orderedPeriods.subList(0, MAX_TOP_HITS));

                orderedFValues = new ArrayList<Double>(orderedFValues.subList(0, MAX_TOP_HITS));

                orderedPValues = new ArrayList<Double>(orderedPValues.subList(0, MAX_TOP_HITS));
            }
        }

        @Override
        public void interrupt() {
            interrupted = true;
        }
    }

    // Return a copy of the specified observation list
    private List<ValidObservation> copyObs(List<ValidObservation> obs) {
        List<ValidObservation> copiedObs = new ArrayList<ValidObservation>();

        for (ValidObservation ob : obs) {
            if (interrupted)
                break;

            ValidObservation copiedOb = new ValidObservation();

            double jd = ob.getDateInfo().getJulianDay();
            copiedOb.setDateInfo(new DateInfo(jd));

            Magnitude mag = new Magnitude(ob.getMagnitude().getMagValue(), ob.getMagnitude().getUncertainty());
            copiedOb.setMagnitude(mag);

            copiedObs.add(copiedOb);
        }

        return copiedObs;
    }

    // Ask user for period min, max, resolution and number of bins.
    private boolean areParametersLegal(List<ValidObservation> obs) {
        legalParams = true;

        List<ITextComponent<?>> fields = new ArrayList<ITextComponent<?>>();

        // / double days = obs.get(obs.size() - 1).getJD() - obs.get(0).getJD();
        DoubleField minPeriodField = new DoubleField("Minimum Period", 0.0, null, minPeriod);
        fields.add(minPeriodField);

        DoubleField maxPeriodField = new DoubleField("Maximum Period", 0.0, null, maxPeriod);
        fields.add(maxPeriodField);

        DoubleField resolutionField = new DoubleField("Resolution", 0.0, 1.0, resolution);
        fields.add(resolutionField);

        IntegerField binsField = new IntegerField("Bins", 0, 50, bins);
        fields.add(binsField);

        MultiEntryComponentDialog dlg = new MultiEntryComponentDialog("AoV Parameters", fields);

        cancelled = dlg.isCancelled();

        if (!cancelled) {

            try {
                bins = binsField.getValue();
                if (bins <= 0) {
                    MessageBox.showErrorDialog("AoV Parameters", "Number of bins must be greater than zero");
                    legalParams = false;
                }
            } catch (Exception e) {
                legalParams = false;
            }

            minPeriod = minPeriodField.getValue();
            maxPeriod = maxPeriodField.getValue();
            resolution = resolutionField.getValue();

            if (minPeriod >= maxPeriod) {
                MessageBox.showErrorDialog("AoV Parameters",
                        "Minimum period must be less than or equal to maximum period");
                legalParams = false;
            }

            if (resolution <= 0.0) {
                MessageBox.showErrorDialog("AoV Parameters", "Resolution must be between 0 and 1");
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
        bins = 10;
    }
}
