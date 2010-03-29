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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.ui.MainFrame;
import org.aavso.tools.vstar.ui.dialog.period.PeriodAnalysis2DResultDialog;
import org.aavso.tools.vstar.ui.model.list.PeriodAnalysisDataTableModel;
import org.aavso.tools.vstar.ui.model.list.PeriodAnalysisTopHitsTableModel;
import org.aavso.tools.vstar.ui.model.plot.PeriodAnalysis2DPlotModel;
import org.aavso.tools.vstar.util.period.PeriodAnalysisCoordinateType;
import org.jdesktop.swingworker.SwingWorker;

/**
 * A concurrent task in which a potentially long-running periodAnalysisAlgorithm
 * is executed.
 */
public class PeriodAnalysisTask extends SwingWorker<Void, Void> {

	private IPeriodAnalysisAlgorithm periodAnalysisAlgorithm;
	private StarInfo starInfo;
	private SeriesType sourceSeriesType;

	/**
	 * Constructor
	 * 
	 * @param periodAnalysisAlgorithm
	 *            The periodAnalysisAlgorithm to be executed.
	 * @param info
	 *            Information about the star.
	 */
	public PeriodAnalysisTask(IPeriodAnalysisAlgorithm periodAnalysisAlgorithm,
			StarInfo info, SeriesType sourceSeriesType) {
		this.periodAnalysisAlgorithm = periodAnalysisAlgorithm;
		this.starInfo = info;
		this.sourceSeriesType = sourceSeriesType;
	}

	/**
	 * @see org.jdesktop.swingworker.SwingWorker#doInBackground()
	 */
	protected Void doInBackground() throws Exception {
		if (Mediator.getInstance().getPeriodAnalysisResultDialog() == null) {
			MainFrame.getInstance().getStatusPane().setMessage(
					"Performing Period Analysis...");
			periodAnalysisAlgorithm.execute();
			MainFrame.getInstance().getStatusPane().setMessage("");
		}
		return null;
	}

	/**
	 * Executed in event dispatching thread.
	 */
	public void done() {
		Mediator.getInstance().getProgressNotifier().notifyListeners(
				ProgressInfo.COMPLETE_PROGRESS);

		if (Mediator.getInstance().getPeriodAnalysisResultDialog() == null) {
			List<PeriodAnalysis2DPlotModel> models = new ArrayList<PeriodAnalysis2DPlotModel>();

			Map<PeriodAnalysisCoordinateType, List<Double>> seriesMap = periodAnalysisAlgorithm
					.getResultSeries();

			// Frequency vs Power
//			models.add(new PeriodAnalysis2DPlotModel(seriesMap
//					.get(PeriodAnalysisCoordinateType.FREQUENCY), seriesMap
//					.get(PeriodAnalysisCoordinateType.POWER),
//					PeriodAnalysisCoordinateType.FREQUENCY,
//					PeriodAnalysisCoordinateType.POWER));

			// Frequency vs Amplitude
			models.add(new PeriodAnalysis2DPlotModel(seriesMap
					.get(PeriodAnalysisCoordinateType.FREQUENCY), seriesMap
					.get(PeriodAnalysisCoordinateType.AMPLITUDE),
					PeriodAnalysisCoordinateType.FREQUENCY,
					PeriodAnalysisCoordinateType.AMPLITUDE));

			// Period vs Power
//			models.add(new PeriodAnalysis2DPlotModel(seriesMap
//					.get(PeriodAnalysisCoordinateType.PERIOD), seriesMap
//					.get(PeriodAnalysisCoordinateType.POWER),
//					PeriodAnalysisCoordinateType.PERIOD,
//					PeriodAnalysisCoordinateType.POWER));

			// Period vs Amplitude
			models.add(new PeriodAnalysis2DPlotModel(seriesMap
					.get(PeriodAnalysisCoordinateType.PERIOD), seriesMap
					.get(PeriodAnalysisCoordinateType.AMPLITUDE),
					PeriodAnalysisCoordinateType.PERIOD,
					PeriodAnalysisCoordinateType.AMPLITUDE));

			int maxHits = 20; // TODO: parameterise this; make user selectable

			Mediator.getInstance().setPeriodAnalysisResultDialog(
					new PeriodAnalysis2DResultDialog(
							"Period Analysis (DC DFT) for "
									+ starInfo.getDesignation(), "(series: "
									+ sourceSeriesType.getDescription() + ")",
							models,
							new PeriodAnalysisDataTableModel(seriesMap),
							new PeriodAnalysisTopHitsTableModel(seriesMap,
									periodAnalysisAlgorithm
											.getTopNRankedIndices(maxHits),
									maxHits)));
		} else {
			Mediator.getInstance().getPeriodAnalysisResultDialog().setVisible(
					true);
		}

		// TODO: how to detect task cancellation and clean up map etc?
	}
}
