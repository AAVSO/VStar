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
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
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
import org.aavso.tools.vstar.ui.mediator.message.HarmonicSearchResultMessage;
import org.aavso.tools.vstar.ui.mediator.message.NewStarMessage;
import org.aavso.tools.vstar.ui.mediator.message.PeriodAnalysisSelectionMessage;
import org.aavso.tools.vstar.ui.mediator.message.ProgressInfo;
import org.aavso.tools.vstar.ui.mediator.message.ProgressType;
import org.aavso.tools.vstar.ui.model.list.PeriodAnalysisDataTableModel;
import org.aavso.tools.vstar.ui.model.plot.PeriodAnalysis2DPlotModel;
import org.aavso.tools.vstar.util.locale.LocaleProps;
import org.aavso.tools.vstar.util.model.Harmonic;
import org.aavso.tools.vstar.util.model.PeriodAnalysisDerivedMultiPeriodicModel;
import org.aavso.tools.vstar.util.model.PeriodFitParameters;
import org.aavso.tools.vstar.util.notification.Listener;
import org.aavso.tools.vstar.util.period.IPeriodAnalysisAlgorithm;
import org.aavso.tools.vstar.util.period.IPeriodAnalysisDatum;
import org.aavso.tools.vstar.util.period.PeriodAnalysisCoordinateType;
import org.aavso.tools.vstar.util.period.dcdft.PeriodAnalysisDataPoint;
import org.aavso.tools.vstar.util.prefs.NumericPrecisionPrefs;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.apache.commons.math.stat.descriptive.rank.Median;
import org.apache.commons.math.stat.regression.OLSMultipleLinearRegression;

/**
 * 	DFT according to Deeming, T.J., 1975, Ap&SS, 36, 137
 *	Spectral Window (DFT for the unit-amplitude signal) 
 *	DCDFT
 *	Multi-Harmonic DFT
 */
public class DFTandSpectralWindow extends PeriodAnalysisPluginBase {

	private static final boolean USE_MULTI_THREAD_VERSION = true;
	
	private static final int PROGRESS_COUNTER_STEPS = 200;
	
	private static final int MHDFT_MAX_HARMONIC = 25;	

	private static int MAX_TOP_HITS = -1; // set to -1 for the unlimited number!
	
	private static boolean SHOW_CALC_TIME = true;
	private long algStartTime;
	
	private boolean firstInvocation;
    //I (Max) am not sure if it is required (volatile). However, it is accessed from different threads.
	private volatile boolean plugin_interrupted;
	private volatile boolean algorithmCreated;
	private boolean cancelled;

	private FtResult ftResult;
	private double minFrequency, maxFrequency, resolution;
	
	public enum FAnalysisType {
		DFT("DFT|Deeming 1975"), 
		SPW("Spectral Window|Deeming 1975"), 
		DCDFT("DC DFT|Ferraz-Mello 1981"),
		MHDFT("Multi-harmonic DFT|Andronov 1994");

		public final String label;

		private FAnalysisType(String label) {
			this.label = label;
		}

	}
	
	private FAnalysisType analysisType;
	private int harmonicCount;

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

		Mediator.getInstance().getProgressNotifier().notifyListeners(
				new ProgressInfo(ProgressType.BUSY_PROGRESS));
		
		if (firstInvocation) {
			Mediator.getInstance().getNewStarNotifier()
					.addListener(getNewStarListener());

			firstInvocation = false;
		}

		// Full reset if a new dataset was loaded (reset() was called)  
		if (ftResult == null) {
			ftResult = new FtResult(obs);
			minFrequency = 0.0;
			maxFrequency = 0.0;
			resolution = 0.0;
			double interval = ftResult.getMedianTimeInterval();
			double timeSpan = ftResult.getObservationTimeSpan();
			if (interval > 0.0 && timeSpan > 0.0) {
				// Trying to estimate the Nyquist frequency from the median interval between observations.
				// Restrict it if it is too high.
				maxFrequency = Math.min(0.5 / interval, 50.0);
				// The peak width in the frequency domain ~ the length of the observation time span.    
				resolution = 0.05 / timeSpan; 
			}
			analysisType = FAnalysisType.DFT;
			harmonicCount = 1;
		} else {
			//// Does the new dataset have a different time span and resolution?
			//double previousInterval = ftResult.getMedianTimeInterval();
			//double previousTimeSpan = ftResult.getObservationTimeSpan();
			ftResult = new FtResult(obs);
			//double interval = ftResult.getMedianTimeInterval();
			//double timeSpan = ftResult.getObservationTimeSpan();
			//if (interval != previousInterval || timeSpan != previousTimeSpan) {
			//	maxFrequency = 0.0;
			//	resolution = 0.0;
			//	if (interval > 0.0 && timeSpan > 0.0) {
			//		maxFrequency = Math.min(0.5 / interval, 50.0);
			//		resolution = 0.05 / timeSpan;
			//	}
			//	if (maxFrequency <= minFrequency)
			//		minFrequency = 0.0;
			//}
		}
		
		cancelled = !parametersDialog();
		if (cancelled)
			return;

		if (analysisType == FAnalysisType.MHDFT) {
			if (harmonicCount < 1 || harmonicCount > MHDFT_MAX_HARMONIC)
				throw new AlgorithmError("Invalid number of harmonics");
		}

		ftResult.setAnalysisType(analysisType, harmonicCount);		
		
		algorithm = new DFTandSpectralWindowAlgorithm(minFrequency, maxFrequency, resolution, ftResult);
		Mediator.getInstance().getProgressNotifier().notifyListeners(
				new ProgressInfo(ProgressType.MAX_PROGRESS, ((DFTandSpectralWindowAlgorithm)algorithm).getNumberOfSteps()));
		algorithmCreated = true;
		plugin_interrupted = false;
		algStartTime = System.currentTimeMillis();
		algorithm.execute();
	}
	
	@Override
	public JDialog getDialog(SeriesType sourceSeriesType) {
		return plugin_interrupted || cancelled ? null : new PeriodAnalysisDialog(sourceSeriesType, analysisType, harmonicCount);
	}

	@SuppressWarnings("serial")
	class PeriodAnalysisDialog extends PeriodAnalysisDialogBase implements
			Listener<PeriodAnalysisSelectionMessage> {

		private SeriesType sourceSeriesType;

		private IPeriodAnalysisDatum selectedDataPoint;
		
		private JTabbedPane tabbedPane;

		private String findHarmonicsButtonText;
		private String newPhasePlotButtonText;
		
		private static final double FREQUENCY_RELATIVE_TOLERANCE = 1e-3;
		
		private double currentTolerance = FREQUENCY_RELATIVE_TOLERANCE;
		
		DoubleField toleranceField;

		private PeriodAnalysisDataTablePane resultsTablePane;
		private PeriodAnalysisTopHitsTablePane topHitsTablePane;
		private List<PeriodAnalysis2DChartPane> plotPanes;

		// Keep local analysisType because there can be several instances of this dialog opened simultaneously.
		FAnalysisType analysisType;
		int harmonicCount;
		
		public PeriodAnalysisDialog(SeriesType sourceSeriesType, FAnalysisType analysisType, int harmonicCount) {
			super("", false, true, true);
			
			this.analysisType = analysisType;
			this.harmonicCount = harmonicCount;
			
			String dialogTitle = analysisType.label;
			if (SHOW_CALC_TIME)
				dialogTitle += (" | " + Double.toString((System.currentTimeMillis() - algStartTime) / 1000.0) + 's');
			setTitle(dialogTitle);
			
			this.sourceSeriesType = sourceSeriesType;

			prepareDialog();

			this.setNewPhasePlotButtonState(false);
			this.setFindHarmonicsButtonState(false);
			
			findHarmonicsButtonText = findHarmonicsButton.getText();
			newPhasePlotButtonText = newPhasePlotButton.getText();
			
			if (analysisType == FAnalysisType.SPW) {
				newPhasePlotButton.setVisible(false);
				findHarmonicsButton.setVisible(false);
			} else if (analysisType == FAnalysisType.MHDFT) {
				findHarmonicsButton.setVisible(false);
			}


			startup(); // Note: why does base class not call this in
			// prepareDialog()?
		}

		private class PeriodAnalysis2DPlotModelNamed extends PeriodAnalysis2DPlotModel {
			
			private String name;
			
			PeriodAnalysis2DPlotModelNamed(
					Map<PeriodAnalysisCoordinateType, List<Double>> analysisValues,
					PeriodAnalysisCoordinateType domainType,
					PeriodAnalysisCoordinateType rangeType, 
					boolean isLogarithmic, 
					String name) {
						super(analysisValues, domainType, rangeType, isLogarithmic);
						this.name = name;
					}
			
			public String getName() {
				return name;
			}
		}		
		
		@Override
		protected Component createContent() {
			String title = get1stWord(analysisType.label);
			if (analysisType == FAnalysisType.MHDFT && harmonicCount > 1)
				title += " (" + harmonicCount + " harmonics)";

			plotPanes = new ArrayList<PeriodAnalysis2DChartPane>();
			List<NamedComponent> namedComponents = new ArrayList<NamedComponent>();
			ArrayList<PeriodAnalysis2DPlotModelNamed>plotModels = new ArrayList<PeriodAnalysis2DPlotModelNamed>();
			
			plotModels.add(new PeriodAnalysis2DPlotModelNamed(
					algorithm.getResultSeries(),
					PeriodAnalysisCoordinateType.FREQUENCY, 
					PeriodAnalysisCoordinateType.POWER, 
					false, "PowerPaneFrequency"));

			if (analysisType != FAnalysisType.MHDFT || harmonicCount == 1) {
				plotModels.add(new PeriodAnalysis2DPlotModelNamed(
						algorithm.getResultSeries(),
						PeriodAnalysisCoordinateType.FREQUENCY, 
						PeriodAnalysisCoordinateType.SEMI_AMPLITUDE, 
						false, "SemiAmplitudePaneFrequency"));
			}

			if (analysisType != FAnalysisType.SPW) {
				plotModels.add(new PeriodAnalysis2DPlotModelNamed(
						algorithm.getResultSeries(),
						PeriodAnalysisCoordinateType.PERIOD, 
						PeriodAnalysisCoordinateType.POWER, 
						false, "PowerPanePeriod"));
	
				if (analysisType != FAnalysisType.MHDFT || harmonicCount == 1) {
					plotModels.add(new PeriodAnalysis2DPlotModelNamed(
							algorithm.getResultSeries(),
							PeriodAnalysisCoordinateType.PERIOD, 
							PeriodAnalysisCoordinateType.SEMI_AMPLITUDE, 
							false, "SemiAmplitudePanePeriod"));
				}
			}
			
			for (PeriodAnalysis2DPlotModelNamed dataPlotModel : plotModels) { 
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
				plotPane.setChartPaneID(dataPlotModel.getName());
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
			topHitsTablePane.setTablePaneID("TopHitsTable");
			namedComponents.add(new NamedComponent(LocaleProps.get("TOP_HITS_TAB"), topHitsTablePane));			

			// Return tabbed pane of plot and period display component.
			tabbedPane = PluginComponentFactory.createTabs(namedComponents);
			return tabbedPane;
		}

		// Send a period change message when the new-phase-plot button is
		// clicked.
		@Override
		protected void newPhasePlotButtonAction() {
			sendPeriodChangeMessage(selectedDataPoint.getPeriod());
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
				selectedDataPoint = info.getDataPoint();
				if (analysisType != FAnalysisType.SPW) {
					setNewPhasePlotButtonState(true);
					newPhasePlotButton.setText(newPhasePlotButtonText + " [" + NumericPrecisionPrefs.formatOther(selectedDataPoint.getPeriod()) + " d]");					
					if (analysisType != FAnalysisType.MHDFT) {
						setFindHarmonicsButtonState(true);
						findHarmonicsButton.setText(findHarmonicsButtonText + " [" + NumericPrecisionPrefs.formatOther(selectedDataPoint.getFrequency()) + " 1/d]");
					}
				}
			}
		}

		// ** Modified result and top-hit panes **

		// The uncertainty estimator was created for DC DFT. We need to heck first if it is correct for the simple DFT. 
		class PeriodAnalysisDerivedMultiPeriodicModelMod extends PeriodAnalysisDerivedMultiPeriodicModel {

			private FAnalysisType analysisType;
			
			public PeriodAnalysisDerivedMultiPeriodicModelMod(PeriodAnalysisDataPoint topDataPoint,
					List<Harmonic> harmonics, IPeriodAnalysisAlgorithm algorithm, FAnalysisType analysisType) {
				super(topDataPoint, harmonics, algorithm);
				this.analysisType = analysisType;
			}
			
			@Override
			public String toUncertaintyString() throws AlgorithmError {
				switch (analysisType) {
					case DCDFT:
						return super.toUncertaintyString();
					case DFT:
						return "Not implemented for " + analysisType.label;
					case MHDFT:
						return "Not implemented for " + analysisType.label;
					default:
					    return "Not available for " + analysisType.label;
				}
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
										dataPoints.get(0), harmonics, algorithm, analysisType);

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
			String componentID = null;
			Component c = tabbedPane.getSelectedComponent();
			if (c instanceof PeriodAnalysis2DChartPane) {
				componentID = ((PeriodAnalysis2DChartPane)c).getChartPaneID();
			} else if (c instanceof PeriodAnalysisDataTablePane) {
				componentID = ((PeriodAnalysisDataTablePane)c).getTablePaneID();
			}
			
			if (componentID == null) {
				MessageBox.showMessageDialog("Find Harmonic", "Not implemented for this view");
				return;
			}
			
			MultiEntryComponentDialog paramDialog = createToleranceDialog();
			if (paramDialog.isCancelled()) {
				return;
			}
			currentTolerance = toleranceField.getValue();
			//List<Double> data = algorithm.getResultSeries().get(PeriodAnalysisCoordinateType.FREQUENCY);
			List<Double> data = algorithm.getTopHits().get(PeriodAnalysisCoordinateType.FREQUENCY);
			List<Harmonic> harmonics = findHarmonics(selectedDataPoint.getFrequency(), data, currentTolerance);
			HarmonicSearchResultMessage msg = new HarmonicSearchResultMessage(this,
					harmonics, selectedDataPoint, currentTolerance);
			msg.setTag(this.getName());
			msg.setIDstring(componentID);
			Mediator.getInstance().getHarmonicSearchNotifier().notifyListeners(msg);
		}
		
		private MultiEntryComponentDialog createToleranceDialog() {
			toleranceField = new DoubleField("Relative Frequency Tolerance", 0.0, 1.0, currentTolerance); 
			return new MultiEntryComponentDialog("Find Harmonics", toleranceField);
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
		private volatile boolean algorithm_interrupted;

		CountDownLatch startLatch;		
		CountDownLatch doneLatch;

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
						if (powers.get(i) > powers.get(i - 1) && powers.get(i) >= powers.get(i + 1)) {
							top = true;
						}
					} else if (i == 0) {
						// Fourier transform is symmetric relative to 0 frequency because it continues to the negative frequencies.  
						if (powers.get(i) > powers.get(i + 1)) {
							top = true;
						}
					} else if (i == frequencies.size() - 1) {
						// We cannot determine if the last point is a top-hit or not.
//						if (powers.get(i) > powers.get(i - 1)) {
//							top = true;
//						}
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

			algorithm_interrupted = false;
			
			boolean calculationErrorOccured; 
				
			int n_steps = getNumberOfSteps();

			if (USE_MULTI_THREAD_VERSION) {
				calculationErrorOccured = multiThreadDFT(minFrequency, resolution, n_steps);
			} else {
				calculationErrorOccured = singleThreadDFT(minFrequency, resolution, n_steps);
			}
			
			if (calculationErrorOccured) {
				Runnable dialog = new Runnable() {
		            @Override
		            public void run() {
		            	MessageBox.showWarningDialog("Warning", "Calculations failed for some frequencies above the default cut-off threshold");
		            }
				};
				try {
					javax.swing.SwingUtilities.invokeAndWait(dialog);
				}
				catch (Exception e) {
					throw new AlgorithmError(e.getLocalizedMessage());
			    }
			}
		}
		
		public int getNumberOfSteps() {
			return (int)Math.ceil((maxFrequency - minFrequency) / resolution) + 1;
		}
		
		private void incrementProgress(int steps) {
			Mediator.getInstance().getProgressNotifier().notifyListeners(
						new ProgressInfo(ProgressType.INCREMENT_PROGRESS, steps));
		}
		
		private boolean singleThreadDFT(double minFrequency, double resolution, int n_steps) {
			
			boolean calcFailedForSomeFreq = false;
			
			int progress_counter = 0;
			for (int i = 0; i < n_steps; i++) {
				if (algorithm_interrupted)
					break;
				
				double frequency = minFrequency + i * resolution;
				
				frequencies.add(frequency);
				periods.add(fixInf(1 / frequency));

				double[] result;
				try {
					result = ftResult.calculateF(frequency);
				} catch (Exception ex) {
					calcFailedForSomeFreq = true;
					result = new double[] {Double.NaN, Double.NaN};
				}
				
				semiAmplitudes.add(fixInf(result[0]));
				powers.add(fixInf(result[1]));
				
				progress_counter++;
				if (progress_counter >= PROGRESS_COUNTER_STEPS) {
					incrementProgress(progress_counter);
					progress_counter = 0;								
				}
				incrementProgress(progress_counter);
			}
			
			return calcFailedForSomeFreq;
		}

		private boolean multiThreadDFT(double minFrequency, double resolution, int n_steps)
				throws AlgorithmError {
			
			boolean calcFailedForSomeFreq = false;
			
			int cores = Runtime.getRuntime().availableProcessors();
			int steps_per_core = n_steps / cores;
			int remainder = n_steps - steps_per_core * cores;
			
			List<DftWorker>workers = new ArrayList<DftWorker>();
			
			doneLatch = new CountDownLatch(cores);
			startLatch = new CountDownLatch(1);
			for (int n = 0; n < cores; n++) {
				int start_n = steps_per_core * n;
				int steps_to_do = steps_per_core;
				if (n == cores - 1)
					steps_to_do += remainder;
				DftWorker worker = new DftWorker(n, minFrequency, resolution, start_n, steps_to_do, ftResult, startLatch, doneLatch);
				workers.add(worker);
				new Thread(worker).start();
			}
			startLatch.countDown();
			
			try {
				doneLatch.await();
			} catch (InterruptedException ex) {
				algorithm_interrupted = true;
				//throw new AlgorithmError("Interrupted");
			}
			//if (algorithm_interrupted) System.out.println("Algorithm interrupted.");
			
			if (!algorithm_interrupted) {
				for (DftWorker worker : workers) {
					String error = worker.getErrorMessage();
					if (error != null) {
						throw new AlgorithmError(error);
					}
					if (worker.getCalculationFailed())
						calcFailedForSomeFreq = true;
					double[] frqArray = worker.getFrequencies();
					double[] perArray = worker.getPeriods();
					double[] pwrArray = worker.getPowers();
					double[] ampArray = worker.getSemiAmplitudes();
					for (int i = 0; i < worker.getStepsToDo(); i++) {
						frequencies.add(frqArray[i]);
						periods.add(fixInf(perArray[i]));
						powers.add(pwrArray[i]);
						semiAmplitudes.add(fixInf(ampArray[i]));
					}
				}
			}
			
			return calcFailedForSomeFreq;
		}
		
		private class DftWorker implements Runnable {
			
			private final CountDownLatch startLatch;
			private final CountDownLatch doneLatch;			
			
			private double minFrequency;
			private double resolution;
			private int start_n;
			private int steps_to_do;
			//private int thread_n; 
			
			private double[] frqArray;
			private double[] perArray;
			private double[] pwrArray;
			private double[] ampArray;
			
			private FtResult ftResult;
			
			private String errorMessage = null;
			private boolean calculationFailed = false;
			
			public DftWorker(
					int thread_n,
					double minFrequency, 
					double resolution, 
					int start_n, 
					int steps_to_do,
					FtResult ftResult,
					CountDownLatch startLatch,
					CountDownLatch doneLatch) {
				this.minFrequency = minFrequency;
				this.resolution = resolution;
				this.start_n = start_n;
				this.steps_to_do = steps_to_do;
				//this.thread_n = thread_n;
				this.startLatch = startLatch;
				this.doneLatch = doneLatch;
				this.frqArray = new double[steps_to_do];
				this.perArray = new double[steps_to_do];
				this.pwrArray = new double[steps_to_do];
				this.ampArray = new double[steps_to_do];
				this.ftResult = ftResult;
			}
			
			public void run() {
				try {
					try {
						startLatch.await();				
						//System.out.println("DftThread #" + thread_n + " started. start_n=" + start_n + "; steps_to_do=" + steps_to_do);
						int progress_counter = 0;
						for (int i = 0; i < steps_to_do; i++) {
							if (algorithm_interrupted) {
								//System.out.println("DftThread #" + thread_n + " interrupted");
								break;
							}
							
							double frequency = minFrequency + (start_n + i) * resolution;
							
							double[] result;
							try {
								result = ftResult.calculateF(frequency);
							} catch (Exception ex) {
								calculationFailed = true;
								result = new double[] {Double.NaN, Double.NaN};
							}
							
							frqArray[i] = frequency;
							perArray[i] = 1 / frequency;
							ampArray[i] = result[0];
							pwrArray[i] = result[1];
							
							progress_counter++;
							if (progress_counter >= PROGRESS_COUNTER_STEPS) {
								incrementProgress(progress_counter);
								progress_counter = 0;								
							}
						}
						incrementProgress(progress_counter);
						//System.out.println("DftThread #" + thread_n + " finished.");					
					} catch (InterruptedException ex) {
						// return;
					} catch (Exception ex) {
						errorMessage = ex.getMessage();
						if (errorMessage == null)
							errorMessage = "Unknown Error";
					}
				} finally {
					if (!algorithm_interrupted) doneLatch.countDown();
				}
			}

			public String getErrorMessage() {
				return errorMessage;
			}

			public boolean getCalculationFailed() {
				return calculationFailed;
			}
			
			public int getStepsToDo() {
				return steps_to_do;
			}
			
			public double[] getFrequencies() {
				return frqArray;
			}
			
			public double[] getPeriods() {
				return perArray;
			}
			
			public double[] getPowers() {
				return pwrArray;
			}
			
			public double[] getSemiAmplitudes() {
				return ampArray;
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
			algorithm_interrupted = true;
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
					analysisType,
					harmonicCount);
		
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
			harmonicCount = runParametersDialog.getHarmonicCount();
			return true;
		}
		
		return false;
	}

	private class RunParametersDialog implements Runnable {

		private double minFrequency; 
		private double maxFrequency; 
		private double resolution;
		private FAnalysisType analysisType;
		private int harmonicCount;
		private boolean dialogCancelled;
	
		public RunParametersDialog(
				double minFrequency, 
				double maxFrequency, 
				double resolution, 
				FAnalysisType analysisType,
				int harmonicCount) {
			this.minFrequency = minFrequency;
			this.maxFrequency = maxFrequency;
			this.resolution = resolution;
			this.analysisType = analysisType;
			this.harmonicCount = harmonicCount;
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
			analysisTypePane.setLayout(new GridLayout(0, 3));
			analysisTypePane.setBorder(BorderFactory.createTitledBorder("Analysis Type"));
			ButtonGroup analysisTypeGroup = new ButtonGroup();
			
			String[] harmonicNumbers = new String[MHDFT_MAX_HARMONIC];
			for (int i = 0; i < MHDFT_MAX_HARMONIC; i++) {
				harmonicNumbers[i] = i + 1 + "";
			}
			JComboBox<String> harmonicSelector = new JComboBox<String>(harmonicNumbers);
			
			JRadioButton dftRadioButton = new JRadioButton(get1stWord(FAnalysisType.DFT.label));
			dftRadioButton.addActionListener(e -> harmonicSelector.setEnabled(false));
			analysisTypeGroup.add(dftRadioButton);
			analysisTypePane.add(dftRadioButton);
			analysisTypePane.add(new JLabel());
			analysisTypePane.add(new LinkLabel("<html><a href=''>" + get2ndWord(FAnalysisType.DFT.label) + "</a></html>", "https://ui.adsabs.harvard.edu/abs/1975Ap%26SS..36..137D/abstract"));
			
			JRadioButton spwRadioButton = new JRadioButton(get1stWord(FAnalysisType.SPW.label));
			spwRadioButton.addActionListener(e -> harmonicSelector.setEnabled(false));
			analysisTypeGroup.add(spwRadioButton);
			analysisTypePane.add(spwRadioButton);
			analysisTypePane.add(new JLabel());
			analysisTypePane.add(new LinkLabel("<html><a href=''>" + get2ndWord(FAnalysisType.DFT.label) + "</a></html>", "https://ui.adsabs.harvard.edu/abs/1975Ap%26SS..36..137D/abstract"));
			
			JRadioButton dcdftRadioButton = new JRadioButton(get1stWord(FAnalysisType.DCDFT.label));
			dcdftRadioButton.addActionListener(e -> harmonicSelector.setEnabled(false));
			analysisTypeGroup.add(dcdftRadioButton);
			analysisTypePane.add(dcdftRadioButton);
			analysisTypePane.add(new JLabel());
			analysisTypePane.add(new LinkLabel("<html><a href=''>" + get2ndWord(FAnalysisType.DCDFT.label) + "</a></html>", "https://ui.adsabs.harvard.edu/abs/1981AJ.....86..619F/abstract"));
			
			JRadioButton mhdftRadioButton = new JRadioButton(get1stWord(FAnalysisType.MHDFT.label));
			mhdftRadioButton.addActionListener(e -> harmonicSelector.setEnabled(true));
			analysisTypeGroup.add(mhdftRadioButton);
			analysisTypePane.add(mhdftRadioButton);
			JPanel harmonicPane = new JPanel();
			harmonicPane.add(new JLabel("Harmonics: "));
			harmonicPane.add(harmonicSelector);
			analysisTypePane.add(harmonicPane);
			analysisTypePane.add(new LinkLabel("<html><a href=''>" + get2ndWord(FAnalysisType.MHDFT.label) + "</a></html>", "https://ui.adsabs.harvard.edu/abs/1994OAP.....7...49A/abstract"));

			//analysisTypePane.add(Box.createRigidArea(new Dimension(75, 10)));
			switch (analysisType) {
				case DFT:
					dftRadioButton.setSelected(true);
					break;
				case SPW:
					spwRadioButton.setSelected(true);
					break;
				case DCDFT:
					dcdftRadioButton.setSelected(true);
					break;
				default:
					mhdftRadioButton.setSelected(true);
			}
			
			harmonicSelector.setEnabled(analysisType == FAnalysisType.MHDFT);
			
			if (harmonicCount > 0 && harmonicCount <= MHDFT_MAX_HARMONIC)
				harmonicSelector.setSelectedIndex(harmonicCount - 1);
			else
				harmonicSelector.setSelectedIndex(0);
			
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
				else if (dcdftRadioButton.isSelected())
					analysisType = FAnalysisType.DCDFT;
				else
					analysisType = FAnalysisType.MHDFT;
				
				harmonicCount = harmonicSelector.getSelectedIndex() + 1;
				
				minFrequency = minFrequencyField.getValue();
				maxFrequency = maxFrequencyField.getValue();
				resolution = resolutionField.getValue();
	
				if (minFrequency >= maxFrequency) {
					MessageBox.showErrorDialog("Parameters", 
							"Minimum frequency must be less than or equal to maximum frequency");
					legalParams = false;
					continue;
				}
	
				if (resolution <= 0.0) {
					MessageBox.showErrorDialog("Parameters",
							"Resolution must be > 0");
					legalParams = false;
					continue;
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
		
		public int getHarmonicCount() {
			return harmonicCount;
		}
		
		public boolean getDialogCancelled() {
			return dialogCancelled;
		}

	}
	
	private static String get1stWord(String s) {
		return s.split("\\|")[0];
	}

	private static String get2ndWord(String s) {
		return s.split("\\|")[1];
	}
	
	@Override
	public void interrupt() {
		// Executed in EDT thread.
		plugin_interrupted = true;
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
		ftResult = null;		
		cancelled = false;
		plugin_interrupted = false;
		algorithmCreated = false;
		minFrequency = 0.0;
		maxFrequency = 0.0;
		resolution = 0.0;
		analysisType = FAnalysisType.DFT;
		harmonicCount = 1;
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
		private double varpTime;
		private double medianTimeInterval;
		private double zeroFrequencyCut;
		private int count;
		private FAnalysisType analysisType;
		private int harmonicCount;
		
		public FtResult(List<ValidObservation> obs) {
			setAnalysisType(FAnalysisType.DFT, 1);
			
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
			varpTime = calcPopVariance(times);
			medianTimeInterval = calcMedianTimeInterval(times);
			
			zeroFrequencyCut = 0.95 / Math.sqrt(12.0 * varpTime) / 4.0;
		}
		
		public static double calcPopVariance(double d[]) {
			double sum_n = 0.0;
			double sum_nn = 0.0;			
			int count = d.length;
			for (int i = 0; i < count; i++) {
				sum_n += d[i];				
			}
			double mean = sum_n / count; 
			for (int i = 0; i < count; i++) {
				double v = d[i] - mean; 
				sum_nn += v * v;
			}
			return sum_nn / count;
		}
		
		public static double calcMedianTimeInterval(double[] times) {
			if (times.length < 2)
				return 0.0;
			List<Double> sorted_times = new ArrayList<Double>();
			for (Double t : times) {
				sorted_times.add(t);
			}
			sorted_times.sort(new DoubleComparator());
            // Rarely, equal times may occur.
			List<Double>intervalList = new ArrayList<Double>();
			for (int i = 1; i < times.length; i++) {
				double interval = times[i] - times[i - 1];
				if (interval > 0.0)
					intervalList.add(interval);
			}
			if (intervalList.size() < 1)
				return 0.0;
			double[] intervals = new double[intervalList.size()];
			for (int i = 0; i < intervalList.size(); i++) {
				intervals[i] = intervalList.get(i);
			}
			Median median = new Median();
			return median.evaluate(intervals);
		}
		
		public double[] calculateF(double nu) {
            double amp;
            double pwr;
	        double reF = 0.0;
            double imF = 0.0;
            double omega = 2 * Math.PI * nu;            
            if (analysisType == FAnalysisType.DFT || analysisType == FAnalysisType.SPW) {
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
//            } else if (analysisType == FAnalysisType.DCDFT) {
//            	// Use the zero-frequency cut, like in Foster's code, to suppress the huge amplitude peak at zero.
//            	if (nu < zeroFrequencyCut) {
//            		amp = Double.NaN;
//            		pwr = Double.NaN;
//            	} else {
//	            	double[] a = new double[times.length];
//	            	double[][] cos_sin = new double[times.length][2];
//	            	for (int i = 0; i < times.length; i++) {
//	            		a[i] = omega * times[i];
//	            		//cos_sin[i][0] = Math.cos(a[i]);
//	            		//cos_sin[i][1] = Math.sin(a[i]);
//		           		double tanAd2 = Math.tan(a[i] / 2.0);
//		            	double tanAd2squared = tanAd2 * tanAd2;
//		            	cos_sin[i][0] = (1 - tanAd2squared) / (1 + tanAd2squared);
//		            	cos_sin[i][1] = (2.0 * tanAd2 / (1 + tanAd2squared));
//	            	}
//	
//	            	OLSMultipleLinearRegression regression = new OLSMultipleLinearRegression(); 
//	    			regression.newSampleData(mags, cos_sin);
//	    			
//	    			double[] beta = regression.estimateRegressionParameters();
//	    			double b1 = beta[1];
//	    			double b2 = beta[2];
//	    			double[] predicted_mags = new double[times.length]; // excluding mag zero level, not needed
//	    			for (int i = 0; i < times.length; i++) {
//	    				predicted_mags[i] = b1 * cos_sin[i][0] + b2 * cos_sin[i][1];
//	    			}
//	            	amp = Math.sqrt(b1 * b1 + b2 * b2);
//	            	pwr = calcPopVariance(predicted_mags) * (times.length - 1) / varpMag / 2.0;
//            	}
            } else {
            	// Use the zero-frequency cut, like in Foster's code, to suppress the huge amplitude peak at zero.
            	if (nu < zeroFrequencyCut) {
            		amp = Double.NaN;
            		pwr = Double.NaN;
            	} else {
            		int localHarmonicCount = harmonicCount;
            		if (analysisType == FAnalysisType.DCDFT)
            			localHarmonicCount = 1;
	            	double[] a = new double[times.length];
	            	double[][] cos_sin = new double[times.length][2 * localHarmonicCount];
	            	for (int i = 0; i < times.length; i++) {
	            		a[i] = omega * times[i];
	            		for (int n = 0; n < localHarmonicCount; n++) {
	            			//cos_sin[i][2 * n] = Math.cos((n + 1) * a[i]);
	            			//cos_sin[i][2 * n + 1] = Math.sin((n + 1) * a[i]);
			           		double tanAd2 = Math.tan((n + 1) * a[i] / 2.0);
			            	double tanAd2squared = tanAd2 * tanAd2;
			            	cos_sin[i][2 * n] = (1 - tanAd2squared) / (1 + tanAd2squared);
			            	cos_sin[i][2 * n + 1] = (2.0 * tanAd2 / (1 + tanAd2squared));
	            		}
	            	}

	            	OLSMultipleLinearRegression regression = new OLSMultipleLinearRegression(); 
	    			regression.newSampleData(mags, cos_sin);
	    			
	    			double[] beta = regression.estimateRegressionParameters();
	    			double[] predicted_mags = new double[times.length]; // excluding mag zero level, not needed
	    			for (int i = 0; i < times.length; i++) {
	    				predicted_mags[i] = 0.0;
	    				for (int n = 0; n < localHarmonicCount; n++) {
	    					predicted_mags[i] += beta[2 * n + 1] * cos_sin[i][2 * n] + beta[2 * n + 2] * cos_sin[i][2 * n + 1];
	    				}
	    			}
	    			if (localHarmonicCount == 1) {
	    				amp = Math.sqrt(beta[1] * beta[1] + beta[2] * beta[2]);
	    			} else {
	    				amp = Double.NaN;
	    			}
	            	pwr = calcPopVariance(predicted_mags) / varpMag;
	            	if (analysisType == FAnalysisType.DCDFT)
	            		pwr = pwr * (times.length - 1) / 2.0;	
            	}
            }
            return new double[] {amp, pwr};
		}

		public void setAnalysisType(FAnalysisType analysisType, int harmonicCount) {
			this.analysisType = analysisType;
			this.harmonicCount = harmonicCount;
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

    class LinkLabel extends JLabel {
    	
    	public LinkLabel(String text, String urlString) {
    		super(text);
    		this.setForeground(Color.BLUE);
    		this.setCursor(new Cursor(Cursor.HAND_CURSOR));
    		this.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    try {
                        Desktop.getDesktop().browse(new URI(urlString));
                    } catch (Exception ex) {
                    	MessageBox.showErrorDialog("Error", "Cannot open the link");
                    }
                }
            });
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
	    	System.out.println(e.getMessage());
	        success = false;
	    }
		
		setTestMode(false);
		
		return success;
	}

}
