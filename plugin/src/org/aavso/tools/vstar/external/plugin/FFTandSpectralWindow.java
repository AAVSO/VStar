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
import java.util.Comparator;
import java.util.LinkedHashMap;
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
import org.aavso.tools.vstar.util.model.Harmonic;
import org.aavso.tools.vstar.util.model.PeriodAnalysisDerivedMultiPeriodicModel;
import org.aavso.tools.vstar.util.notification.Listener;
import org.aavso.tools.vstar.util.period.IPeriodAnalysisAlgorithm;
import org.aavso.tools.vstar.util.period.PeriodAnalysisCoordinateType;
import org.aavso.tools.vstar.util.period.dcdft.PeriodAnalysisDataPoint;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.DatasetRenderingOrder;

/**
 * 	FFT according to Deeming, T.J., 1975, Ap&SS, 36, 137
	plus FFT for the unit-amplitude signal = Spectral Window
 */
public class FFTandSpectralWindow extends PeriodAnalysisPluginBase {

	private int maxTopHits = -1; // set to -1 for the unlimited number!
	
	private boolean firstInvocation;
	private boolean interrupted;
	private boolean cancelled;
	private boolean legalParams;
	private boolean resetParams;

	private FtResult ftResult;
	private Double minFrequency, maxFrequency, resolution;

	private IPeriodAnalysisAlgorithm algorithm;

	/**
	 * Constructor
	 */
	public FFTandSpectralWindow() {
		super();
		firstInvocation = true;
		reset();
	}

	@Override
	public String getDescription() {
		return "Spectral Window";
	}

	@Override
	public String getDisplayName() {
		return "Spectral Window";
	}

	/**
	 * @see org.aavso.tools.vstar.plugin.IPlugin#getDocName()
	 */
	@Override
	public String getDocName() {
		return "Spectral Window Plug-In.pdf";
	}

	@Override
	public void executeAlgorithm(List<ValidObservation> obs)
			throws AlgorithmError, CancellationException {

		if (firstInvocation) {
			Mediator.getInstance().getNewStarNotifier()
					.addListener(getNewStarListener());

			firstInvocation = false;
		}

		// areParametersLegal displays a dialog, which should be done in the AWT thread.
		// To accomplish this, I (Max) use quite an ugly solution; there must be a more elegant way. 
		RunParametersDialog doAreParametersLegal = new RunParametersDialog(obs); 
		while (true) {
			try {
				javax.swing.SwingUtilities.invokeAndWait(doAreParametersLegal);
			}
			catch (Exception e) {
		        //e.printStackTrace();
				//tempAreParametersLegalResult = false;
				throw new AlgorithmError(e.getLocalizedMessage());
		    }
			if (cancelled)
				return;
			if (doAreParametersLegal.getParametersLegalResult())
				break;
		}
		
		// This displays the parameters dialog in a non-AWT thread. 
		//while (!areParametersLegal(obs) && !cancelled)
		//	;
		
		algorithm = new FFTandSpectralWindowAlgorithm(obs);
		algorithm.execute();
	}
	
	private class RunParametersDialog implements Runnable {
		private List<ValidObservation> obs;
		private boolean parametersLegalResult;
		public RunParametersDialog(List<ValidObservation> obs) {
			this.obs = obs;
		}
		public void run() {
			parametersLegalResult = areParametersLegal(obs);
	    }
		public boolean getParametersLegalResult() {
			return parametersLegalResult;
		}
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
		//private IPeriodAnalysisDatum selectedDataPoint;

		private PeriodAnalysisDataTablePane resultsTablePane;
		private PeriodAnalysisTopHitsTablePane topHitsTablePane;
		private PeriodAnalysis2DChartPane plotPane;
		private PeriodAnalysis2DChartPane plotPane2;

		public PeriodAnalysisDialog(SeriesType sourceSeriesType) {
			super("Spectral Window", false, true, false);

			this.sourceSeriesType = sourceSeriesType;

			prepareDialog();

			this.setNewPhasePlotButtonState(false);

			startup(); // Note: why does base class not call this in
			// prepareDialog()?
		}

		@Override
		protected Component createContent() {
			String title = "Spectral Window Periodogram";
			
			PeriodAnalysis2DChartPane topHitsPlotPane;
			PeriodAnalysis2DChartPane topHitsPlotPane2;


			{
				// POWER
				PeriodAnalysis2DPlotModel dataPlotModel = new PeriodAnalysis2DPlotModel(
						algorithm.getResultSeries(),
						PeriodAnalysisCoordinateType.FREQUENCY, 
						PeriodAnalysisCoordinateType.POWER, 
						false);
	
				plotPane = PeriodAnalysisComponentFactory.createLinePlot(
						title,
						sourceSeriesType.getDescription(), 
						dataPlotModel, 
						false);
	
				PeriodAnalysis2DPlotModel topHitsPlotModel = new PeriodAnalysis2DPlotModel(
						algorithm.getTopHits(),
						PeriodAnalysisCoordinateType.FREQUENCY, 
						PeriodAnalysisCoordinateType.POWER, 
						false);
	
				topHitsPlotPane = PeriodAnalysisComponentFactory.createScatterPlot(
						title, 
						sourceSeriesType.getDescription(), 
						topHitsPlotModel,
						false);
				
				// POWER
				// Add the above line plot's model to the scatter plot.
				// Render the scatter plot last so the "handles" will be
				// the first items selected by the mouse.
				JFreeChart chart = topHitsPlotPane.getChart();
				chart.getXYPlot().setDataset(PeriodAnalysis2DChartPane.DATA_SERIES, dataPlotModel);
				chart.getXYPlot().setDataset(PeriodAnalysis2DChartPane.TOP_HIT_SERIES, topHitsPlotModel);
				chart.getXYPlot().setRenderer(PeriodAnalysis2DChartPane.DATA_SERIES, plotPane.getChart().getXYPlot().getRenderer());
				chart.getXYPlot().setDatasetRenderingOrder(DatasetRenderingOrder.REVERSE);
			}
			
			{
				// SEMI-AMPLITUDE
				PeriodAnalysis2DPlotModel dataPlotModel2 = new PeriodAnalysis2DPlotModel(
						algorithm.getResultSeries(),
						PeriodAnalysisCoordinateType.FREQUENCY, 
						PeriodAnalysisCoordinateType.SEMI_AMPLITUDE, 
						false);
				
				plotPane2 = PeriodAnalysisComponentFactory.createLinePlot(
						title,
						sourceSeriesType.getDescription(), 
						dataPlotModel2, 
						false);
				
				PeriodAnalysis2DPlotModel topHitsPlotModel2 = new PeriodAnalysis2DPlotModel(
						algorithm.getTopHits(),
						PeriodAnalysisCoordinateType.FREQUENCY, 
						PeriodAnalysisCoordinateType.SEMI_AMPLITUDE, 
						false);
	
				topHitsPlotPane2 = PeriodAnalysisComponentFactory.createScatterPlot(
						title, 
						sourceSeriesType.getDescription(), 
						topHitsPlotModel2,
						false);
	
				// SEMI_AMPLITUDE
				// Add the above line plot's model to the scatter plot.
				// Render the scatter plot last so the "handles" will be
				// the first items selected by the mouse.
				JFreeChart chart2 = topHitsPlotPane2.getChart();
				chart2.getXYPlot().setDataset(PeriodAnalysis2DChartPane.DATA_SERIES, dataPlotModel2);
				chart2.getXYPlot().setDataset(PeriodAnalysis2DChartPane.TOP_HIT_SERIES, topHitsPlotModel2);
				chart2.getXYPlot().setRenderer(PeriodAnalysis2DChartPane.DATA_SERIES, plotPane2.getChart().getXYPlot().getRenderer());
				chart2.getXYPlot().setDatasetRenderingOrder(DatasetRenderingOrder.REVERSE);
			}
			
			// Full results table
			PeriodAnalysisCoordinateType[] columns = {
					PeriodAnalysisCoordinateType.FREQUENCY,
					PeriodAnalysisCoordinateType.PERIOD, 
					PeriodAnalysisCoordinateType.POWER, 
					PeriodAnalysisCoordinateType.SEMI_AMPLITUDE };

			PeriodAnalysisDataTableModel dataTableModel = new PeriodAnalysisDataTableModel(columns, algorithm.getResultSeries());
			resultsTablePane = new NoModelPeriodAnalysisDataTablePane(dataTableModel, algorithm);

			PeriodAnalysisDataTableModel topHitsModel = new PeriodAnalysisDataTableModel(columns, algorithm.getTopHits());
			topHitsTablePane = new NoModelPeriodAnalysisTopHitsTablePane(topHitsModel, dataTableModel, algorithm);

			// Return tabbed pane of plot and period display component.
			return PluginComponentFactory.createTabs(
					new NamedComponent("Power", topHitsPlotPane), 
					new NamedComponent("Semi-Amplitude", topHitsPlotPane2),
					new NamedComponent("Results", resultsTablePane), 
					new NamedComponent("Top Hits", topHitsTablePane));
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
			plotPane.startup();
			plotPane2.startup();
		}

		@Override
		public void cleanup() {
			Mediator.getInstance().getPeriodAnalysisSelectionNotifier()
					.removeListenerIfWilling(this);

			resultsTablePane.cleanup();
			topHitsTablePane.cleanup();
			plotPane.cleanup();
			plotPane2.cleanup();
		}

		// Next two methods are for Listener<PeriodAnalysisSelectionMessage>

		@Override
		public boolean canBeRemoved() {
			return false;
		}

		@Override
		public void update(PeriodAnalysisSelectionMessage info) {
			period = info.getDataPoint().getPeriod();
			//selectedDataPoint = info.getDataPoint();
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
			// To-do: harmonic search for FFT only
		}
	}

	// FFT according to Deeming, T.J., 1975, Ap&SS, 36, 137
	class FFTandSpectralWindowAlgorithm implements IPeriodAnalysisAlgorithm {

		//private List<ValidObservation> obs;

		private List<Double> frequencies;
		private List<Double> periods;
		private List<Double> powers;
		private List<Double> semiAmplitudes;

		public FFTandSpectralWindowAlgorithm(List<ValidObservation> obs) {
			//this.obs = obs;
			frequencies = new ArrayList<Double>();
			periods = new ArrayList<Double>();
			powers = new ArrayList<Double>();
			semiAmplitudes = new ArrayList<Double>();
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
			results.put(PeriodAnalysisCoordinateType.POWER, powers);
			results.put(PeriodAnalysisCoordinateType.SEMI_AMPLITUDE, semiAmplitudes);

			return results;
		}

		@Override
		public Map<PeriodAnalysisCoordinateType, List<Double>> getTopHits() {
			
			ArrayList<Double> hitFrequencies = new ArrayList<Double>();
			ArrayList<Double> hitPeriods = new ArrayList<Double>();
			ArrayList<Double> hitPowers = new ArrayList<Double>();
			ArrayList<Double> hitSemiAmplitudes = new ArrayList<Double>();
			
			// Extracting top hits (local maxima)
			if (frequencies.size() > 1) {
				for (int i = 0; i < frequencies.size(); i++) {
					boolean top = false;
					if (i > 0 && i < frequencies.size() - 1) {
						if (powers.get(i) > powers.get(i - 1) && powers.get(i) > powers.get(i + 1)) {
							top = true;
						}
					} else if (i == 0) {
						if (powers.get(i) > powers.get(i + 1)) {
							top = true;
						}
					} else if (i == frequencies.size() - 1) {
						if (powers.get(i) > powers.get(i - 1)) {
							top = true;
						}
					}
					if (top) {
						hitFrequencies.add(frequencies.get(i));
						hitPeriods.add(periods.get(i));
						hitPowers.add(powers.get(i));
						hitSemiAmplitudes.add(semiAmplitudes.get(i));
					}
	
				}
				
				// Here we can limit the number of the top hits, however, is it worth to?
				// set maxTopHits to -1 for the unrestricted number
				if (maxTopHits >= 0) {
					ArrayList<IntDoublePair>hitIndices = new ArrayList<IntDoublePair>();
					for (int i = 0; i < hitPowers.size(); i++) {
						hitIndices.add(new IntDoublePair(i, hitPowers.get(i)));
					}
					hitIndices.sort(new IntDoublePairComparator());
					
					ArrayList<Integer>selectedHitIndices = new ArrayList<Integer>();
					for (int i = 0; i < Math.min(maxTopHits, hitIndices.size()); i++) {
						selectedHitIndices.add(hitIndices.get(i).i);
					}
					
					Collections.sort(selectedHitIndices);
				
					for (int i = hitPowers.size() - 1; i >= 0; i--) {
						if (!selectedHitIndices.contains(i)) {
							hitFrequencies.remove(i);
							hitPeriods.remove(i);
							hitPowers.remove(i);
							hitSemiAmplitudes.remove(i);
						}
					}
				}
			}
			
			Map<PeriodAnalysisCoordinateType, List<Double>> topHits = new LinkedHashMap<PeriodAnalysisCoordinateType, List<Double>>();

			topHits.put(PeriodAnalysisCoordinateType.FREQUENCY,	hitFrequencies);
			topHits.put(PeriodAnalysisCoordinateType.PERIOD, hitPeriods);
			topHits.put(PeriodAnalysisCoordinateType.POWER, hitPowers);
			topHits.put(PeriodAnalysisCoordinateType.SEMI_AMPLITUDE, hitSemiAmplitudes);
			
			return topHits;
		}
		
		private class IntDoublePair {
			public int i;
			public double d;
			public IntDoublePair(int i, double d) {
				this.i = i;
				this.d = d;
			}
		}
		
		// Inverse sort
		private class IntDoublePairComparator implements Comparator<IntDoublePair> {
		    @Override
		    public int compare(IntDoublePair a, IntDoublePair b) {
		        if (a.d > b.d) 
		        	return -1;
		        else if (a.d < b.d)
		        	return 1;
		        else
		        	return 0;
		    }
		}

		@Override
		public void multiPeriodicFit(List<Harmonic> harmonics,
				PeriodAnalysisDerivedMultiPeriodicModel model)
				throws AlgorithmError {
		}

		@Override
		public List<PeriodAnalysisDataPoint> refineByFrequency(
				List<Double> freqs, List<Double> variablePeriods,
				List<Double> lockedPeriod) throws AlgorithmError {
			return null;
		}

		@Override
		public void execute() throws AlgorithmError {
			// Request parameters
			// TODO: move this to top-level execute method and just pass actual
			// parameters to this class?
			//while (!areParametersLegal(obs) && !cancelled)
			//	;

			if (!cancelled) {
				interrupted = false;
				
				for (double frequency = minFrequency; frequency <= maxFrequency; frequency += resolution) {
					if (interrupted)
						break;
					
					frequencies.add(frequency);
					periods.add(fixInf(1/frequency));
					
					ftResult.calculateF(frequency);
					
					powers.add(fixInf(ftResult.getPwr()));
					semiAmplitudes.add(fixInf(ftResult.getAmp()));

				}
				
			}
		}

		// replace +-Infinity by NaN
		private double fixInf(double v) {
			if (Double.isInfinite(v))
				return Double.NaN;
			else
				return v;
		}

		@Override
		public void interrupt() {
			interrupted = true;
		}
	}

	// Ask user for frequency min, max, and resolution.
	private boolean areParametersLegal(List<ValidObservation> obs) {
		legalParams = true;

		List<Double> times = new ArrayList<Double>();
		for (ValidObservation ob : obs) {
			times.add(ob.getJD());
		}
		ftResult = new FtResult(times);
		
		if (resetParams) {
			double x = 1.0 / Math.sqrt(ftResult.getPVariance() * 12.0) / 4.0;
			minFrequency = 0.0;
			maxFrequency = x * ftResult.getCount() * 2;
			resolution = x / 10.0;
			resetParams = false;
		}
	
		List<ITextComponent<?>> fields = new ArrayList<ITextComponent<?>>();

		DoubleField minFrequencyField = new DoubleField("Minimum Frequency", 0.0, null, minFrequency);
		fields.add(minFrequencyField);

		DoubleField maxFrequencyField = new DoubleField("Maximum Frequency", 0.0, null, maxFrequency);
		fields.add(maxFrequencyField);

		DoubleField resolutionField = new DoubleField("Resolution", 0.0, 1.0, resolution);
		fields.add(resolutionField);

		MultiEntryComponentDialog dlg = new MultiEntryComponentDialog("Parameters", fields);

		cancelled = dlg.isCancelled();

		if (!cancelled) {

			minFrequency = minFrequencyField.getValue();
			maxFrequency = maxFrequencyField.getValue();
			resolution = resolutionField.getValue();

			if (minFrequency >= maxFrequency) {
				MessageBox.showErrorDialog("Parameters", 
						"Minimum frequency must be less than or equal to maximum frequency");
				legalParams = false;
			}

			if (resolution <= 0.0) {
				MessageBox.showErrorDialog("Parameters",
						"Resolution must be > 0");
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
		resetParams = true;
		minFrequency = 0.0;
		maxFrequency = 0.0;
		resolution = 0.1;
	}
	
	private class FtResult {
		private List<Double> times;
		private double max;
		private double min;
		private double mean;
		private double p_variance;
		private int count;
		
		private double amp = 0.0;
		private double pwr = 0.0;
		
		public FtResult(List<Double> times) {
			this.times = times;
			count = times.size();
		
			min = 0.0;
			max = 0.0;
			mean = 0.0;
			boolean b = true; 
			for (Double t : times) {
				if (b) {
					min = t;
					max = min;
					b = false;
				} else {
					if (t < min)
						min = t;
					if (t > max)
						max = t;
				}
				mean += t;
			}
			mean /= count;
			
			p_variance = 0.0;
			for (Double t : times) {
				p_variance += (t - mean) * (t - mean);
			}
			p_variance /= count; 
		}
		
		public void calculateF(double nu) {
	        double reF = 0.0;
            double imF = 0.0;
            for (Double t: times) {
            	double a = 2 * Math.PI * nu * t;
                reF += 1.0 * Math.cos(a);
                imF += 1.0 * Math.sin(a);
            }
            // Like in Period04
            amp = Math.sqrt(reF * reF + imF * imF) / times.size();
            pwr = amp * amp;
		}
		
		public double getAmp() {
			return amp;
		}
		
		public double getPwr() {
			return pwr;
		}
		
		public int getCount() {
			return count;
		}
		
//		public double getMin() {
//			return min;
//		}
		
//		public double getMax() {
//			return max;
//		}
		
		public double getPVariance() {
			return p_variance;
		}
	}
}
