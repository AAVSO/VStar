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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTable;

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
import org.aavso.tools.vstar.ui.dialog.DoubleField;
import org.aavso.tools.vstar.ui.dialog.ITextComponent;
import org.aavso.tools.vstar.ui.dialog.MessageBox;
import org.aavso.tools.vstar.ui.dialog.MultiEntryComponentDialog;
import org.aavso.tools.vstar.ui.dialog.model.HarmonicInputDialog;
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
import org.aavso.tools.vstar.util.model.PeriodFitParameters;
import org.aavso.tools.vstar.util.notification.Listener;
import org.aavso.tools.vstar.util.period.IPeriodAnalysisAlgorithm;
import org.aavso.tools.vstar.util.period.PeriodAnalysisCoordinateType;
import org.aavso.tools.vstar.util.period.dcdft.PeriodAnalysisDataPoint;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.apache.commons.math.stat.descriptive.rank.Median;
import org.apache.commons.math.stat.regression.OLSMultipleLinearRegression;

/**
 * 	DFT according to Deeming, T.J., 1975, Ap&SS, 36, 137
	plus DFT for the unit-amplitude signal == Spectral Window
 */
public class DFTandSpectralWindow extends PeriodAnalysisPluginBase {

	private boolean showCalcTime = true;
	private long algStartTime;

	// DCDFT via OLSMultipleLinearRegression: much slower then existing, 
	// no big amplitude damping near 0 freq.!
	// Set to 'true' to enable.
	private boolean showDCDFT = false;
	
	private static int MAX_TOP_HITS = -1; // set to -1 for the unlimited number!
	
	private boolean firstInvocation;
    //I (Max) am not sure if it is required (volatile). However, it is accessed from different threads.
	private volatile boolean interrupted;
	private volatile boolean algorithmCreated;
	private boolean cancelled;
	private boolean resetParams;

	private FtResult ftResult;
	private Double minFrequency, maxFrequency, resolution;
	
	public enum FAnalysisType {
		DFT("DFT (Deeming 1975)"), SPW("Spectral Window"), DCDFT("DC DFT");

		public final String label;

		private FAnalysisType(String label) {
			this.label = label;
		}

	}
	
	private FAnalysisType analysisType;

	private IPeriodAnalysisAlgorithm algorithm;
	
	private List<PeriodAnalysisDialog> resultDialogList;
	
	/**
	 * Constructor
	 */
	public DFTandSpectralWindow() {
		super();
		firstInvocation = true;
		reset();
	}

	@Override
	public String getDescription() {
		return "DFT and Spectral Window Frequency Range";
	}

	@Override
	public String getDisplayName() {
		return "DFT and Spectral Window Frequency Range";
	}

	/**
	 * @see org.aavso.tools.vstar.plugin.IPlugin#getDocName()
	 */
	@Override
	public String getDocName() {
		return "DFT and Spectral Window Plug-In.pdf";
	}

	@Override
	public void executeAlgorithm(List<ValidObservation> obs)
			throws AlgorithmError, CancellationException {

		if (firstInvocation) {
			Mediator.getInstance().getNewStarNotifier()
					.addListener(getNewStarListener());

			firstInvocation = false;
		}

		if (resetParams) {
			ftResult = new FtResult(obs);
			minFrequency = 0.0;
			maxFrequency = 0.0;
			resolution = null;
			double interval = ftResult.getMedianTimeInterval();
			if (interval > 0.0) {
				// Trying to estimate the Nyquist frequency from the median interval between observations.
				// Restrict it if it is too high.
				maxFrequency = Math.min(0.5 / interval, 50.0);
				// The peak width in the frequency domain ~ the length of the observation time span.    
				resolution = 0.05 / ftResult.getObservationTimeSpan(); 
			}
			analysisType = FAnalysisType.DFT;
			resetParams = false;
		}
		
		cancelled = !parametersDialog();
		if (cancelled)
			return;
		
		ftResult.setAnalysisType(analysisType);
		
		algorithm = new DFTandSpectralWindowAlgorithm(minFrequency, maxFrequency, resolution, ftResult);
		algorithmCreated = true;
		interrupted = false;
		algStartTime = System.currentTimeMillis();
		algorithm.execute();
	}
	
	@Override
	public JDialog getDialog(SeriesType sourceSeriesType) {
		return interrupted || cancelled ? null : new PeriodAnalysisDialog(sourceSeriesType, analysisType);
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

		// Keep local analysisType because there can be several instances of this dialog opened simultaneously.
		FAnalysisType analysisType;
		
		public PeriodAnalysisDialog(SeriesType sourceSeriesType, FAnalysisType analysisType) {
			super("", false, true, false);
			
			this.analysisType = analysisType;
			
			String dialogTitle = analysisType.label;
			if (showCalcTime)
				dialogTitle += (" | " + Double.toString((System.currentTimeMillis() - algStartTime) / 1000.0) + 's');
			setTitle(dialogTitle);
			
			this.sourceSeriesType = sourceSeriesType;

			prepareDialog();

			this.setNewPhasePlotButtonState(false);

			startup(); // Note: why does base class not call this in
			// prepareDialog()?
		}

		@Override
		protected Component createContent() {
			String title = analysisType.label;

			plotPanes = new ArrayList<PeriodAnalysis2DChartPane>();
			List<NamedComponent> namedComponents = new ArrayList<NamedComponent>();
			Map<PeriodAnalysis2DPlotModel, String>plotModels = new LinkedHashMap<PeriodAnalysis2DPlotModel, String>();
			
			plotModels.put(new PeriodAnalysis2DPlotModel(
					algorithm.getResultSeries(),
					PeriodAnalysisCoordinateType.FREQUENCY, 
					PeriodAnalysisCoordinateType.POWER, 
					false), "PowerPaneFrequency");

			plotModels.put(new PeriodAnalysis2DPlotModel(
					algorithm.getResultSeries(),
					PeriodAnalysisCoordinateType.FREQUENCY, 
					PeriodAnalysisCoordinateType.SEMI_AMPLITUDE, 
					false), "SemiAmplitudePaneFrequency");

			if (analysisType != FAnalysisType.SPW) {
				plotModels.put(new PeriodAnalysis2DPlotModel(
						algorithm.getResultSeries(),
						PeriodAnalysisCoordinateType.PERIOD, 
						PeriodAnalysisCoordinateType.POWER, 
						false), "PowerPanePeriod");
	
				plotModels.put(new PeriodAnalysis2DPlotModel(
						algorithm.getResultSeries(),
						PeriodAnalysisCoordinateType.PERIOD, 
						PeriodAnalysisCoordinateType.SEMI_AMPLITUDE, 
						false), "SemiAmplitudePanePeriod");
			}
			
			for (PeriodAnalysis2DPlotModel dataPlotModel : plotModels.keySet()) { 
				PeriodAnalysis2DChartPane plotPane = PeriodAnalysisComponentFactory.createLinePlot(
						title,
						sourceSeriesType.getDescription(), 
						dataPlotModel, 
						true);

				PeriodAnalysis2DPlotModel topHitsPlotModel = new PeriodAnalysis2DPlotModel(
						algorithm.getTopHits(),
						dataPlotModel.getDomainType(),
						dataPlotModel.getRangeType(),
						false);
	
				PeriodAnalysis2DChartPane topHitsPlotPane = PeriodAnalysisComponentFactory.createScatterPlot(
						title, 
						sourceSeriesType.getDescription(), 
						topHitsPlotModel,
						true);

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
			resultsTablePane = new PeriodAnalysisDataTablePaneMod(dataTableModel, algorithm, analysisType != FAnalysisType.SPW);
			resultsTablePane.setTablePaneID("DataTable");
			namedComponents.add(new NamedComponent(LocaleProps.get("DATA_TAB"), resultsTablePane));


			PeriodAnalysisDataTableModel topHitsModel = new PeriodAnalysisDataTableModel(columns, algorithm.getTopHits());
			topHitsTablePane = new PeriodAnalysisTopHitsTablePaneMod(topHitsModel, dataTableModel, algorithm, analysisType != FAnalysisType.SPW);
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
			
			if (resultDialogList == null) 
				resultDialogList = new ArrayList<PeriodAnalysisDialog>();
			resultDialogList.add(this);

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
			
			if (resultDialogList != null)
				resultDialogList.remove(this);
			
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
			// !! We must distinguish different instances of the same dialog here.
			if (this.getName() == info.getTag()) {
				period = info.getDataPoint().getPeriod();
				//selectedDataPoint = info.getDataPoint();
				if (analysisType != FAnalysisType.SPW)
					setNewPhasePlotButtonState(true);
			}
		}

		// ** Modified result and top-hit panes **

		// The uncertainty estimator was created for DC DFT. We need to heck first if it is correct for the simple DFT. 
		class PeriodAnalysisDerivedMultiPeriodicModelMod extends PeriodAnalysisDerivedMultiPeriodicModel {

			private boolean isDCDFT;
			
			public PeriodAnalysisDerivedMultiPeriodicModelMod(PeriodAnalysisDataPoint topDataPoint,
					List<Harmonic> harmonics, IPeriodAnalysisAlgorithm algorithm, boolean isDCDFT) {
				super(topDataPoint, harmonics, algorithm);
				this.isDCDFT = isDCDFT;
			}
			
			@Override
			public String toUncertaintyString() throws AlgorithmError {
				if (isDCDFT)
					return super.toUncertaintyString();
				else
				    return "Not implemented for this type of analysis";
			}
			
		}
		
		// Model button listener.
		protected ActionListener createModelButtonHandlerMod(
				JPanel parentPanel, JTable table, PeriodAnalysisDataTableModel model, Map<Double, List<Harmonic>> freqToHarmonicsMap) {
			
			final JPanel parent = parentPanel;
			
			return new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					
					List<PeriodAnalysisDataPoint> dataPoints = new ArrayList<PeriodAnalysisDataPoint>();
					List<Double> userSelectedFreqs = new ArrayList<Double>();
					int[] selectedTableRowIndices = table.getSelectedRows();
					if (selectedTableRowIndices.length < 1) {
						MessageBox.showMessageDialog(LocaleProps.get("CREATE_MODEL_BUTTON"), "Please select a row");
						return;
					}
					for (int row : selectedTableRowIndices) {
						int modelRow = table.convertRowIndexToModel(row);

						PeriodAnalysisDataPoint dataPoint = model.getDataPointFromRow(modelRow);
						dataPoints.add(dataPoint);
						userSelectedFreqs.add(dataPoint.getFrequency());
					}

					HarmonicInputDialog dialog = new HarmonicInputDialog(parent, userSelectedFreqs, freqToHarmonicsMap);

					if (!dialog.isCancelled()) {
						List<Harmonic> harmonics = dialog.getHarmonics();
						if (!harmonics.isEmpty()) {
							try {
								PeriodAnalysisDerivedMultiPeriodicModel model = new PeriodAnalysisDerivedMultiPeriodicModelMod(
										dataPoints.get(0), harmonics, algorithm, analysisType == FAnalysisType.DCDFT);

								Mediator.getInstance().performModellingOperation(model);
							} catch (Exception ex) {
								MessageBox.showErrorDialog(parent, "Modelling", ex.getLocalizedMessage());
							}
						} else {
							MessageBox.showErrorDialog("Create Model", "Period list error");
						}
					}
				}
			};
		}
		
		// Period analysis pane with modified modelButton handler
		class PeriodAnalysisDataTablePaneMod extends
				PeriodAnalysisDataTablePane {

			public PeriodAnalysisDataTablePaneMod(
					PeriodAnalysisDataTableModel model,
					IPeriodAnalysisAlgorithm algorithm,
					boolean wantModelButton) {
				super(model, algorithm, wantModelButton);
			}

			@Override
			protected JPanel createButtonPanel() {
				JPanel buttonPane = new JPanel();

				modelButton = new JButton(LocaleProps.get("CREATE_MODEL_BUTTON"));
				modelButton.setEnabled(false);
				if (wantModelButton) {
					modelButton.addActionListener(createModelButtonHandlerMod(this, table, model, freqToHarmonicsMap));
				} else {
					modelButton.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							MessageBox.showMessageDialog(LocaleProps.get("CREATE_MODEL_BUTTON"), "Not available");
						}
					} );
				}

				if (!wantModelButton) {
					modelButton.setVisible(false);
				}

				buttonPane.add(modelButton, BorderLayout.LINE_END);

				return buttonPane;
			}

			@Override
			protected void enableButtons() {
				super.enableButtons();
			}
		}
		
		// Top hits pane with modified modelButton handler and without refineButton
		class PeriodAnalysisTopHitsTablePaneMod extends
				PeriodAnalysisTopHitsTablePane {

			public PeriodAnalysisTopHitsTablePaneMod(
					PeriodAnalysisDataTableModel topHitsModel,
					PeriodAnalysisDataTableModel fullDataModel,
					IPeriodAnalysisAlgorithm algorithm,
					boolean wantModelButton) {
				super(topHitsModel, fullDataModel, algorithm);
				if (!wantModelButton) {
				    for(ActionListener al : modelButton.getActionListeners()) {
				    	modelButton.removeActionListener(al);
					}					
					modelButton.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							MessageBox.showMessageDialog(LocaleProps.get("CREATE_MODEL_BUTTON"), "Not available");
						}
					} );
					modelButton.setVisible(false);
				}
			}

			@Override
			protected JPanel createButtonPanel() {
				JPanel buttonPane = new JPanel();

				modelButton = new JButton(LocaleProps.get("CREATE_MODEL_BUTTON"));
				modelButton.setEnabled(false);
				modelButton.addActionListener(createModelButtonHandlerMod(this, table, model, freqToHarmonicsMap));

				if (!wantModelButton) {
					modelButton.setVisible(false);
				}

				buttonPane.add(modelButton, BorderLayout.LINE_END);

				return buttonPane;
			}

			@Override
			protected void enableButtons() {
				modelButton.setEnabled(true);
			}
		}

		@Override
		protected void findHarmonicsButtonAction() {
			// To-do: harmonic search for DFT
		}
	}

	// DFT according to Deeming, T.J., 1975, Ap&SS, 36, 137
	public static class DFTandSpectralWindowAlgorithm implements IPeriodAnalysisAlgorithm {

		private List<Double> frequencies;
		private List<Double> periods;
		private List<Double> powers;
		private List<Double> semiAmplitudes;
		
		double minFrequency, maxFrequency, resolution;
		
		private FtResult ftResult;
		
		//I (Max) am not sure if it is required (volatile). However, it is accessed from different threads.
		private volatile boolean interrupted;

		public DFTandSpectralWindowAlgorithm(
				double minFrequency, double maxFrequency, double resolution,
				FtResult ftResult) {
			this.minFrequency = minFrequency;
			this.maxFrequency = maxFrequency;
			this.resolution = resolution;
			this.ftResult = ftResult;
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
				
				hitPowersRaw.sort(new IntDoublePairComparator(false));

				// Here we can limit the number of the top hits, however, is it worth to?
				// set maxTopHits to -1 for the unrestricted number
				int count = 0;
				for (IntDoublePair pair : hitPowersRaw) {
					if (MAX_TOP_HITS >= 0 && count >= MAX_TOP_HITS)
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
		
		@Override
		public void multiPeriodicFit(List<Harmonic> harmonics,
				PeriodAnalysisDerivedMultiPeriodicModel model)
				throws AlgorithmError {

			if (harmonics.size() > 100) {
				throw new AlgorithmError("Too many parameters.");
			}
			
			List<ValidObservation> modelObs = model.getFit();
			List<ValidObservation> residualObs = model.getResiduals();
			List<PeriodFitParameters> parameters = model.getParameters();
			
			double timeOffset = Math.round(ftResult.getObservationMeanTime() * 10.0) / 10.0;
			
			int nobs = ftResult.getCount();
			
			double[] times = new double[nobs];
			for (int i = 0; i < nobs; i++) {
				times[i] = ftResult.getTime(i) - timeOffset;
			}
			
			double[] y_data = new double[nobs];			
			for (int i = 0; i < nobs; i++) {
				y_data[i] = ftResult.getMag(i);
			}
			
			double[][] x_data = new double[nobs][2 * harmonics.size()];
			
			for (int r = 0; r < nobs; r++) {
				for (int c = 0; c < harmonics.size(); c++) {
					double frequency = harmonics.get(c).getFrequency();
					double a = 2.0 * Math.PI * frequency * times[r];
					double sin = Math.sin(a);
					double cos = Math.cos(a);
					x_data[r][2 * c] = sin;
					x_data[r][2 * c + 1] = cos;
				}
			}

//			double[] y_data = new double[nobs];
//			double[][] x_data = new double[nobs][1];
//			for (int r = 0; r < nobs; r++) {
//				y_data[r] = originalObs.get(r).getMag();
//				x_data[r][0] = times[r];
//			}
			
			OLSMultipleLinearRegression regression = new OLSMultipleLinearRegression();
			regression.newSampleData(y_data, x_data);
			
			double[] beta = regression.estimateRegressionParameters();
			
//			System.out.println("Intercept: " + beta[0]);
//	        for (int i = 1; i < beta.length; i++) {
//	            System.out.println("Coefficient " + i + ": " + beta[i]);
//	        }

//	        double rSquared = regression.calculateRSquared();
//	        System.out.println("R-squared: " + rSquared);
	        
	        double zeroPoint = timeOffset;
	        for (int i = 0; i < harmonics.size(); i++) {
	        	int idx = 2 * i;
	        	double sin_coef = beta[idx + 1];
	        	double cos_coef = beta[idx + 2];
	        	double amp = Math.sqrt(sin_coef * sin_coef + cos_coef * cos_coef);
				parameters.add(new PeriodFitParameters(
						harmonics.get(i), 
						amp, 
						cos_coef, 
						sin_coef, 
						beta[0], 
						zeroPoint));
	        }
//	        System.out.println(parameters);
//	        for (PeriodFitParameters p : parameters) {
//	        	System.out.println(p.toProsaicString());	    	   
//	        }
	        
	        String modelDescripton = "Trigonometric Polynomial Model";
	        double[] y_predicted = new double[nobs];
	        for (int i = 0; i < nobs; i++) { 
	        	y_predicted[i] = beta[0]; 
	        	for (int j = 0; j < 2 * harmonics.size(); j++) {
	        		y_predicted[i] += beta[j + 1] * x_data[i][j];
	        	}
	        	//System.out.println(times[i] + " " + y_predicted[i]);
	        	
	        	ValidObservation modelOb = new ValidObservation();
				modelOb.setDateInfo(new DateInfo(times[i] + timeOffset));
				modelOb.setMagnitude(new Magnitude(y_predicted[i], 0));
				modelOb.setComments(modelDescripton);
				modelOb.setBand(SeriesType.Model);
				modelObs.add(modelOb);

				ValidObservation residualOb = new ValidObservation();
				residualOb.setDateInfo(new DateInfo(times[i] + timeOffset));
				residualOb.setMagnitude(new Magnitude(y_data[i] - y_predicted[i], 0));
				residualOb.setComments(modelDescripton);
				residualOb.setBand(SeriesType.Residuals);
				residualObs.add(residualOb);
	        }

		}

		@Override
		public List<PeriodAnalysisDataPoint> refineByFrequency(
				List<Double> freqs, List<Double> variablePeriods,
				List<Double> lockedPeriod) throws AlgorithmError {
			return null;
		}

		@Override
		public void execute() throws AlgorithmError {

			interrupted = false;
				
			int n_steps = (int)Math.ceil((maxFrequency - minFrequency) / resolution) + 1;
			double frequency = minFrequency;
				
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
	private boolean parametersDialog() throws AlgorithmError {

		// We should invoke Swing dialogs in EDT.
		RunParametersDialog runParametersDialog = 
				new RunParametersDialog(
					minFrequency, 
					maxFrequency, 
					resolution, 
					analysisType);
		
		try {
			javax.swing.SwingUtilities.invokeAndWait(runParametersDialog);
		}
		catch (Exception e) {
			throw new AlgorithmError(e.getLocalizedMessage());
	    }
		
		if (!runParametersDialog.getDialogCancelled()) {
			minFrequency = runParametersDialog.getMinFrequency();
			maxFrequency = runParametersDialog.getMaxFrequency();
			resolution = runParametersDialog.getResolution();
			analysisType = runParametersDialog.getAnalysisType();
			return true;
		}
		
		return false;
	}

	private class RunParametersDialog implements Runnable {

		private double minFrequency; 
		private double maxFrequency; 
		private double resolution;
		private FAnalysisType analysisType;
		private boolean dialogCancelled;
	
		public RunParametersDialog(
				double minFrequency, 
				double maxFrequency, 
				double resolution, 
				FAnalysisType analysisType) {
			this.minFrequency = minFrequency;
			this.maxFrequency = maxFrequency;
			this.resolution = resolution;
			this.analysisType = analysisType;
		}
		
		public void run() {
			
			List<ITextComponent<?>> fields = new ArrayList<ITextComponent<?>>();

			DoubleField minFrequencyField = new DoubleField("Minimum Frequency", 0.0, null, minFrequency);
			fields.add(minFrequencyField);

			DoubleField maxFrequencyField = new DoubleField("Maximum Frequency", 0.0, null, maxFrequency);
			fields.add(maxFrequencyField);

			DoubleField resolutionField = new DoubleField("Resolution", 0.0, null, resolution);
			fields.add(resolutionField);

			JPanel analysisTypePane = new JPanel();
			analysisTypePane.setLayout(new GridLayout(showDCDFT ? 3 : 2, 1));
			analysisTypePane.setBorder(BorderFactory.createTitledBorder("Analysis Type"));
			ButtonGroup analysisTypeGroup = new ButtonGroup();
			JRadioButton dftRadioButton = new JRadioButton(FAnalysisType.DFT.label);
			analysisTypeGroup.add(dftRadioButton);
			analysisTypePane.add(dftRadioButton);
			JRadioButton spwRadioButton = new JRadioButton(FAnalysisType.SPW.label);
			analysisTypeGroup.add(spwRadioButton);
			analysisTypePane.add(spwRadioButton);
			JRadioButton dcdftRadioButton = new JRadioButton(FAnalysisType.DCDFT.label);			
			if (showDCDFT) {
				analysisTypeGroup.add(dcdftRadioButton);
				analysisTypePane.add(dcdftRadioButton);
			}

			//analysisTypePane.add(Box.createRigidArea(new Dimension(75, 10)));
			switch (analysisType) {
			case DFT:
				dftRadioButton.setSelected(true);
				break;
			case SPW:
				spwRadioButton.setSelected(true);
				break;
			default:
				dcdftRadioButton.setSelected(true);
			}
			
			while (true) {
				boolean legalParams = true;
				
				MultiEntryComponentDialog dlg = 
						new MultiEntryComponentDialog(
								"Parameters",
								getDocName(),						
								fields, 
								Optional.of(analysisTypePane));

				dialogCancelled = dlg.isCancelled();
				if (dialogCancelled)
					return;

				if (dftRadioButton.isSelected())
					analysisType = FAnalysisType.DFT;
				else if (spwRadioButton.isSelected())
					analysisType = FAnalysisType.SPW;
				else
					analysisType = FAnalysisType.DCDFT;
				
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
				
				if (legalParams)
					break;
			}
			
	    }
		
		public double getMinFrequency() {
			return minFrequency;
		} 
		
		public double getMaxFrequency() {
			return maxFrequency;
		}
		
		public double getResolution() {
			return resolution;
		}
		
		public FAnalysisType getAnalysisType() {
			return analysisType;
		};
		
		public boolean getDialogCancelled() {
			return dialogCancelled;
		}

	}
	
	@Override
	public void interrupt() {
		// Executed in EDT thread.
		interrupted = true;
		if (algorithmCreated) {
			algorithm.interrupt();
		}
	}

	@Override
	protected void newStarAction(NewStarMessage message) {
		reset();
	}

	@Override
	public void reset() {
		cancelled = false;
		interrupted = false;
		algorithmCreated = false;
		resetParams = true;
		minFrequency = 0.0;
		maxFrequency = 0.0;
		resolution = null;
		analysisType = FAnalysisType.DFT;
		ftResult = null;
		if (resultDialogList != null) {
			List<PeriodAnalysisDialog> tempResultDialogList = resultDialogList;
			resultDialogList = null;
			for (PeriodAnalysisDialog dialog : tempResultDialogList) {
				dialog.setVisible(false);
				dialog.cleanup();
				dialog.dispose();
			}
		}
	}
	
	public static class FtResult {
		private double[] times;
		private double[] mags;
		private double maxTime;
		private double minTime;
		private double meanTime;
		private double meanMag;
		private double varpMag;
		private double medianTimeInterval;
		private int count;
		private FAnalysisType analysisType;
		
		OLSMultipleLinearRegression regression; // for DCDFT
		
		private double amp = 0.0;
		private double pwr = 0.0;
		
		public FtResult(List<ValidObservation> obs) {
			setAnalysisType(FAnalysisType.DFT);
			
			count = obs.size();
			times = new double[count];
			mags = new double[count];
			for (int i = 0; i < count; i++) {
				ValidObservation ob = obs.get(i);
				times[i] = ob.getJD();
				mags[i] = ob.getMag();
			}
			
			minTime = 0.0;
			maxTime = 0.0;
			meanTime = 0.0;
			meanMag = 0.0;
			boolean first = true;
			for (int i = 0; i < count; i++) {
				double t = times[i];
				double m = mags[i];
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
				meanTime += t;
				meanMag += m;
			}
			meanTime /= count;
			meanMag /= count;
			
			varpMag = calcPopVariance(mags);
			
			medianTimeInterval = calcMedianTimeInterval(times);
		}
		
		private double calcPopVariance(double d[]) {
			double mean = 0.0;
			double varp = 0.0;
			int count = d.length;
			for (int i = 0; i < count; i++) {
				mean += d[i];
			}
			mean = mean / count;
			for (int i = 0; i < count; i++) {
				varp += (d[i] - mean) * (d[i] - mean);
			}
			return varp / count;
		}
		
		private Double calcMedianTimeInterval(double[] times) {
			if (times.length < 2)
				return 0.0;
			List<Double> sorted_times = new ArrayList<Double>();
			for (Double t : times) {
				sorted_times.add(t);
			}
			sorted_times.sort(new DoubleComparator());
            double intervals[] = new double[times.length - 1];
			for (int i = 1; i < times.length; i++) {
				intervals[i - 1] = times[i] - times[i - 1];
			}
			Median median = new Median();
			return median.evaluate(intervals);
		}
		
		public void calculateF(double nu) {
	        double reF = 0.0;
            double imF = 0.0;
            double omega = 2 * Math.PI * nu;            
            if (analysisType != FAnalysisType.DCDFT) {
	            boolean typeIsDFT = analysisType != FAnalysisType.SPW;
	            for (int i = 0; i < count; i++) {
	            	double a = omega * times[i];
	            	double b = typeIsDFT ? mags[i] - meanMag : 0.5;
	                //reF += b * Math.cos(a);
	                //imF += b * Math.sin(a);
	            	// Faster than Math.sin, Math.cos
	           		double tanAd2 = Math.tan(a / 2.0);
	            	double tanAd2squared = tanAd2 * tanAd2;
	                reF += b * ((1 - tanAd2squared) / (1 + tanAd2squared));
	                imF += b * (2.0 * tanAd2 / (1 + tanAd2squared));
	            }
	            // Like Period04
	            amp = 2.0 * Math.sqrt(reF * reF + imF * imF) / count;
	            pwr = amp * amp;
            } else {
            	if (omega == 0) {
            		amp = Double.NaN;
            		pwr = Double.NaN;
            		return;
            	}
            	double[] a = new double[times.length];
            	double[][] cos_sin = new double[times.length][2];
            	for (int i = 0; i < times.length; i++) {
            		a[i] = omega * times[i];
            		//cos_sin[i][0] = Math.cos(a[i]);
            		//cos_sin[i][1] = Math.sin(a[i]);
	           		double tanAd2 = Math.tan(a[i] / 2.0);
	            	double tanAd2squared = tanAd2 * tanAd2;
	            	cos_sin[i][0] = (1 - tanAd2squared) / (1 + tanAd2squared);
	            	cos_sin[i][1] = (2.0 * tanAd2 / (1 + tanAd2squared));
            	}

    			regression.newSampleData(mags, cos_sin);
    			
    			double[] beta = regression.estimateRegressionParameters();
    			double b1 = beta[1];
    			double b2 = beta[2];
    			double[] predicted_mags = new double[times.length]; // excluding mag zero level, not needed
    			for (int i = 0; i < times.length; i++) {
    				predicted_mags[i] = b1 * cos_sin[i][0] + b2 * cos_sin[i][1];
    			}
            	amp = Math.sqrt(b1 * b1 + b2 * b2);
            	pwr = calcPopVariance(predicted_mags) * (times.length - 1) / varpMag / 2.0;
            }
		}

		public void setAnalysisType(FAnalysisType value) {
			analysisType = value;
			if (analysisType == FAnalysisType.DCDFT) {
    			regression = new OLSMultipleLinearRegression();
			}
		}
		
		public double getAmp() {
			return amp;
		}
		
		public double getPwr() {
			return pwr;
		}
		
		public double getMedianTimeInterval() {
			return medianTimeInterval;
		}
		
		public double getObservationTimeSpan() {
			return maxTime - minTime;
		}
		
		public double getObservationMeanTime() {
			return meanTime;
		}
		
		public int getCount() {
			return count;
		}
		
		public double getTime(int i) {
			return times[i];
		}
		
		public double getMag(int i) {
			return mags[i];
		}
		
	}
	
	private static class DoubleComparator implements Comparator<Double> {
	    @Override
	    public int compare(Double a, Double b) {
	        if (a > b) 
	        	return 1;
	        else if (a < b)
	        	return -1;
	        else
	        	return 0;
	    }
	}

	private static class IntDoublePair {
		public int i;
		public double d;
		public IntDoublePair(int i, double d) {
			this.i = i;
			this.d = d;
		}
	}
	
	private static class IntDoublePairComparator implements Comparator<IntDoublePair> {
		
		private boolean direct;
		
	    public IntDoublePairComparator(boolean direct) {
	    	this.direct = direct;
		}

		@Override
	    public int compare(IntDoublePair a, IntDoublePair b) {
	        if (a.d > b.d) 
	        	return direct ? 1 : -1;
	        else if (a.d < b.d)
	        	return direct ? -1 : 1;
	        else
	        	return 0;
	    }
	}
	
//////////////////////////////////////////////////////////////////////////////
// Unit test
//////////////////////////////////////////////////////////////////////////////

	@Override
	public Boolean test() {
		boolean success = true;
		
		setTestMode(true);
		try {
			DFTandSpectralWindowTest test = new DFTandSpectralWindowTest("DFT test");
			test.testDcDft();
	    } catch (Exception e) {
	    	//System.out.println(e.getMessage());
	        success = false;
	    }
		
		setTestMode(false);
		
		return success;
	}

}
