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
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

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
import org.aavso.tools.vstar.util.locale.LocaleProps;
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
	plus FFT for the unit-amplitude signal == Spectral Window
 */
public class FFTandSpectralWindow extends PeriodAnalysisPluginBase {

	private final String ANALYSIS_TYPE_FFT = "FFT (Deeming 1975)";
	private final String ANALYSIS_TYPE_SPW = "Spectral Window";
	
	private int maxTopHits = -1; // set to -1 for the unlimited number!
	
	private boolean firstInvocation;
	private boolean interrupted;
	private boolean cancelled;
	private boolean resetParams;

	private FtResult ftResult;
	private Double minFrequency, maxFrequency, resolution;
	private boolean analysisTypeIsFFT;

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
		return "FFT and Spectral Window Frequency Range";
	}

	@Override
	public String getDisplayName() {
		return "FFT and Spectral Window Frequency Range";
	}

	/**
	 * @see org.aavso.tools.vstar.plugin.IPlugin#getDocName()
	 */
	@Override
	public String getDocName() {
		return "FFT and Spectral Window Plug-In.pdf";
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
		
		ftResult.setTypeIsFFT(analysisTypeIsFFT);
		
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
		private List<PeriodAnalysis2DChartPane> plotPanes;

		public PeriodAnalysisDialog(SeriesType sourceSeriesType) {
			super(analysisTypeIsFFT ? ANALYSIS_TYPE_FFT : ANALYSIS_TYPE_SPW, false, true, false);

			this.sourceSeriesType = sourceSeriesType;

			prepareDialog();

			this.setNewPhasePlotButtonState(false);

			startup(); // Note: why does base class not call this in
			// prepareDialog()?
		}

		@Override
		protected Component createContent() {
			String title = analysisTypeIsFFT ? ANALYSIS_TYPE_FFT : ANALYSIS_TYPE_SPW;

			plotPanes = new ArrayList<PeriodAnalysis2DChartPane>();
			List<NamedComponent> namedComponents = new ArrayList<NamedComponent>();
			Map<PeriodAnalysis2DPlotModel, String>plotModels = new LinkedHashMap<PeriodAnalysis2DPlotModel, String>();
			
			plotModels.put(new PeriodAnalysis2DPlotModel(
					algorithm.getResultSeries(),
					PeriodAnalysisCoordinateType.FREQUENCY, 
					PeriodAnalysisCoordinateType.POWER, 
					false), "PowerPane");

			plotModels.put(new PeriodAnalysis2DPlotModel(
					algorithm.getResultSeries(),
					PeriodAnalysisCoordinateType.FREQUENCY, 
					PeriodAnalysisCoordinateType.SEMI_AMPLITUDE, 
					false), "SemiAmplitudePane");
			
			for (PeriodAnalysis2DPlotModel dataPlotModel : plotModels.keySet()) { 
				PeriodAnalysis2DChartPane plotPane = PeriodAnalysisComponentFactory.createLinePlot(
						title,
						sourceSeriesType.getDescription(), 
						dataPlotModel, 
						false);

				PeriodAnalysis2DPlotModel topHitsPlotModel = new PeriodAnalysis2DPlotModel(
						algorithm.getTopHits(),
						dataPlotModel.getDomainType(),
						dataPlotModel.getRangeType(),
						false);
	
				PeriodAnalysis2DChartPane topHitsPlotPane = PeriodAnalysisComponentFactory.createScatterPlot(
						title, 
						sourceSeriesType.getDescription(), 
						topHitsPlotModel,
						false);

				// Add the above line plot's model to the scatter plot.
				// Render the scatter plot last so the "handles" will be
				// the first items selected by the mouse.
				JFreeChart chart = topHitsPlotPane.getChart();
				chart.getXYPlot().setDataset(PeriodAnalysis2DChartPane.DATA_SERIES, dataPlotModel);
				chart.getXYPlot().setDataset(PeriodAnalysis2DChartPane.TOP_HIT_SERIES, topHitsPlotModel);
				chart.getXYPlot().setRenderer(PeriodAnalysis2DChartPane.DATA_SERIES, plotPane.getChart().getXYPlot().getRenderer());
				chart.getXYPlot().setDatasetRenderingOrder(DatasetRenderingOrder.REVERSE);
				
				plotPane = topHitsPlotPane;
				plotPane.setChartPaneID(plotModels.get(dataPlotModel));
				plotPanes.add(plotPane);
				String tabName = dataPlotModel.getRangeType() + " vs " + dataPlotModel.getDomainType();
				namedComponents.add(new NamedComponent(tabName, plotPane));
			}
			
			// Full results table
			PeriodAnalysisCoordinateType[] columns = {
					PeriodAnalysisCoordinateType.FREQUENCY,
					PeriodAnalysisCoordinateType.PERIOD, 
					PeriodAnalysisCoordinateType.POWER, 
					PeriodAnalysisCoordinateType.SEMI_AMPLITUDE };

			PeriodAnalysisDataTableModel dataTableModel = new PeriodAnalysisDataTableModel(columns, algorithm.getResultSeries());
			resultsTablePane = new NoModelPeriodAnalysisDataTablePane(dataTableModel, algorithm);
			resultsTablePane.setTablePaneID("DataTable");
			namedComponents.add(new NamedComponent(LocaleProps.get("DATA_TAB"), resultsTablePane));


			PeriodAnalysisDataTableModel topHitsModel = new PeriodAnalysisDataTableModel(columns, algorithm.getTopHits());
			topHitsTablePane = new NoModelPeriodAnalysisTopHitsTablePane(topHitsModel, dataTableModel, algorithm);
			resultsTablePane.setTablePaneID("TopHitsTable");
			namedComponents.add(new NamedComponent(LocaleProps.get("TOP_HITS_TAB"), topHitsTablePane));			

			// Return tabbed pane of plot and period display component.
			return PluginComponentFactory.createTabs(namedComponents);
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
			for (PeriodAnalysis2DChartPane plotPane : plotPanes) {
				plotPane.startup();
			}
		}

		@Override
		public void cleanup() {
			Mediator.getInstance().getPeriodAnalysisSelectionNotifier()
					.removeListenerIfWilling(this);

			resultsTablePane.cleanup();
			topHitsTablePane.cleanup();
			for (PeriodAnalysis2DChartPane plotPane : plotPanes) {
				plotPane.cleanup();
			}
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
			if (analysisTypeIsFFT)
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
				Map<Integer, Double> hitFrequenciesRaw = new HashMap<Integer, Double>();
				Map<Integer, Double> hitPeriodsRaw = new HashMap<Integer, Double>();
				ArrayList<IntDoublePair> hitPowersRaw = new ArrayList<IntDoublePair>();
				Map<Integer, Double> hitSemiAmplitudesRaw = new HashMap<Integer, Double>();
				
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
						hitFrequenciesRaw.put(i, frequencies.get(i));
						hitPeriodsRaw.put(i, periods.get(i));
						hitPowersRaw.add(new IntDoublePair(i, powers.get(i)));
						hitSemiAmplitudesRaw.put(i, semiAmplitudes.get(i));
					}
				}
				
				hitPowersRaw.sort(new IntDoublePairComparator());

				// Here we can limit the number of the top hits, however, is it worth to?
				// set maxTopHits to -1 for the unrestricted number
				int count = 0;
				for (IntDoublePair pair : hitPowersRaw) {
					if (maxTopHits >= 0 && count >= maxTopHits)
						break;
					hitFrequencies.add(hitFrequenciesRaw.get(pair.i));
					hitPeriods.add(hitPeriodsRaw.get(pair.i));
					hitPowers.add(pair.d);
					hitSemiAmplitudes.add(hitSemiAmplitudesRaw.get(pair.i));
					count++;
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
				
				int n_steps = (int)Math.floor((maxFrequency - minFrequency) / resolution) + 2;
				double frequency = minFrequency;
				//for (double frequency = minFrequency; frequency <= maxFrequency; frequency += resolution) {
				for (int i = 0; i < n_steps; i++) {
					if (interrupted)
						break;
					
					frequencies.add(frequency);
					periods.add(fixInf(1/frequency));
					
					ftResult.calculateF(frequency);
					
					powers.add(fixInf(ftResult.getPwr()));
					semiAmplitudes.add(fixInf(ftResult.getAmp()));
					frequency += resolution;
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
		boolean legalParams = true;

		//List<Double> times = new ArrayList<Double>();
		//for (ValidObservation ob : obs) {
		//	times.add(ob.getJD());
		//}
		ftResult = new FtResult(obs);
		
		if (resetParams) {
			minFrequency = 0.0;
			maxFrequency = 10.0;
			resolution = 0.0001;
			analysisTypeIsFFT = true;
			resetParams = false;
		}
	
		List<ITextComponent<?>> fields = new ArrayList<ITextComponent<?>>();

		DoubleField minFrequencyField = new DoubleField("Minimum Frequency", 0.0, null, minFrequency);
		fields.add(minFrequencyField);

		DoubleField maxFrequencyField = new DoubleField("Maximum Frequency", 0.0, null, maxFrequency);
		fields.add(maxFrequencyField);

		DoubleField resolutionField = new DoubleField("Resolution", 0.0, 1.0, resolution);
		fields.add(resolutionField);

		JPanel analysisTypePane = new JPanel();
		analysisTypePane.setLayout(new GridLayout(2, 1));
		analysisTypePane.setBorder(BorderFactory.createTitledBorder("Analysis Type"));
		ButtonGroup analysisTypeGroup = new ButtonGroup();
		JRadioButton fftRadioButton = new JRadioButton(ANALYSIS_TYPE_FFT);
		analysisTypeGroup.add(fftRadioButton);
		analysisTypePane.add(fftRadioButton);
		JRadioButton spwRadioButton = new JRadioButton(ANALYSIS_TYPE_SPW);
		analysisTypeGroup.add(spwRadioButton);
		analysisTypePane.add(spwRadioButton);
		//analysisTypePane.add(Box.createRigidArea(new Dimension(75, 10)));
		if (analysisTypeIsFFT)
			fftRadioButton.setSelected(true);
		else
			spwRadioButton.setSelected(true);
		
		MultiEntryComponentDialog dlg = 
				new MultiEntryComponentDialog(
						"Parameters",
						getDocName(),						
						fields, 
						Optional.of(analysisTypePane));

		cancelled = dlg.isCancelled();

		if (!cancelled) {

			analysisTypeIsFFT = fftRadioButton.isSelected();
			
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
		//legalParams = false;
		interrupted = false;
		resetParams = true;
		minFrequency = 0.0;
		maxFrequency = 0.0;
		resolution = 0.1;
	}
	
	private class FtResult {
		private List<Double> times;
		private List<Double> mags;
		private double maxTime;
		private double minTime;
		//private double meanTime;
		private double meanMag;
		//private double p_varianceTime;
		private int count;
		private boolean typeIsFFT;
		
		private double amp = 0.0;
		private double pwr = 0.0;
		
		public FtResult(List<ValidObservation> obs) {
			typeIsFFT = true;
			
			times = new ArrayList<Double>();
			mags = new ArrayList<Double>();
			for (ValidObservation ob : obs) {
				times.add(ob.getJD());
				mags.add(ob.getMag()) ;
			}
			count = times.size();
			minTime = 0.0;
			maxTime = 0.0;
			//meanTime = 0.0;
			meanMag = 0.0;
			boolean first = true;
			for (int i = 0; i < count; i++) {
				double t = times.get(i);
				double m = mags.get(i);
				if (first) {
					minTime = t;
					maxTime = minTime;
					first = false;
				} else {
					if (t < minTime)
						minTime = t;
					if (t > maxTime)
						maxTime = t;
				}
				//meanTime += t;
				meanMag += m;
			}
			//meanTime /= count;
			meanMag /= count;
			
//			p_varianceTime = 0.0;
//			for (Double t : times) {
//				p_varianceTime += (t - meanTime) * (t - meanTime);
//			}
//			p_varianceTime /= count;
		}
		
		public void calculateF(double nu) {
	        double reF = 0.0;
            double imF = 0.0;
            for (int i = 0; i < count; i++) {
            	double t = times.get(i);
            	double a = 2 * Math.PI * nu * t;
            	double b = typeIsFFT ? mags.get(i) - meanMag : 0.5;
                reF += b * Math.cos(a);
                imF += b * Math.sin(a);
            }
            // Like in Period04
            amp = 2.0 * Math.sqrt(reF * reF + imF * imF) / count;
            pwr = amp * amp;
		}

		public void setTypeIsFFT(boolean value) {
			typeIsFFT = value;
		}
		
		public double getAmp() {
			return amp;
		}
		
		public double getPwr() {
			return pwr;
		}
		
//		public int getCount() {
//			return count;
//		}
//		
//		public double getPVarianceTime() {
//			return p_varianceTime;
//		}
		
	}
}
