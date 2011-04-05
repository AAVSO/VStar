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
package org.aavso.tools.vstar.plugin.period.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JDialog;

import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.plugin.period.PeriodAnalysisPluginBase;
import org.aavso.tools.vstar.ui.dialog.period.PeriodAnalysis2DResultDialog;
import org.aavso.tools.vstar.ui.mediator.message.MeanSourceSeriesChangeMessage;
import org.aavso.tools.vstar.ui.mediator.message.NewStarMessage;
import org.aavso.tools.vstar.ui.model.list.PeriodAnalysisDataTableModel;
import org.aavso.tools.vstar.ui.model.plot.PeriodAnalysis2DPlotModel;
import org.aavso.tools.vstar.util.period.PeriodAnalysisCoordinateType;
import org.aavso.tools.vstar.util.period.dcdft.DateCompensatedDiscreteFourierTransform;

/**
 * Date Compensated Discrete Fourier Transform period analysis plugin.
 */
abstract public class DcDftPeriodAnalysisPluginBase extends
		PeriodAnalysisPluginBase {

	private final static PeriodAnalysisCoordinateType[] DATA_COLUMN_TYPES = {
			PeriodAnalysisCoordinateType.FREQUENCY,
			PeriodAnalysisCoordinateType.PERIOD,
			PeriodAnalysisCoordinateType.POWER,
			PeriodAnalysisCoordinateType.AMPLITUDE };

	protected NewStarMessage newStarMessage;

	protected MeanSourceSeriesChangeMessage meanSourceSeriesChangeMessage;

	protected DateCompensatedDiscreteFourierTransform periodAnalysisAlgorithm;

	private JDialog dialog;

	/**
	 * Constructor.
	 */
	public DcDftPeriodAnalysisPluginBase() {
		super();
		newStarMessage = null;
		meanSourceSeriesChangeMessage = null;
		periodAnalysisAlgorithm = null;
		dialog = null;
	}

	// ** Mandatory interface methods **

	public String getDisplayName() {
		return "Date Compensated DFT";
	}

	public String getDescription() {
		return "Date Compensated Discrete Fourier Transform";
	}

	public JDialog getDialog(SeriesType sourceSeriesType) {
		assert newStarMessage != null;

		if (dialog == null) {
			List<PeriodAnalysis2DPlotModel> models = new ArrayList<PeriodAnalysis2DPlotModel>();

			Map<PeriodAnalysisCoordinateType, List<Double>> resultDataMap = periodAnalysisAlgorithm
					.getResultSeries();

			// Frequency vs Power
			models.add(new PeriodAnalysis2DPlotModel(resultDataMap
					.get(PeriodAnalysisCoordinateType.FREQUENCY), resultDataMap
					.get(PeriodAnalysisCoordinateType.POWER),
					PeriodAnalysisCoordinateType.FREQUENCY,
					PeriodAnalysisCoordinateType.POWER));

			// Period vs Power
//			models.add(new PeriodAnalysis2DPlotModel(resultDataMap
//					.get(PeriodAnalysisCoordinateType.PERIOD), resultDataMap
//					.get(PeriodAnalysisCoordinateType.POWER),
//					PeriodAnalysisCoordinateType.PERIOD,
//					PeriodAnalysisCoordinateType.POWER));

			// Frequency vs Amplitude
			models.add(new PeriodAnalysis2DPlotModel(resultDataMap
					.get(PeriodAnalysisCoordinateType.FREQUENCY), resultDataMap
					.get(PeriodAnalysisCoordinateType.AMPLITUDE),
					PeriodAnalysisCoordinateType.FREQUENCY,
					PeriodAnalysisCoordinateType.AMPLITUDE));

			dialog = new PeriodAnalysis2DResultDialog(
					"Period Analysis (DC DFT) for "
							+ newStarMessage.getStarInfo().getDesignation(),
					"(series: " + sourceSeriesType.getDescription() + ")",
					models, new PeriodAnalysisDataTableModel(DATA_COLUMN_TYPES,
							resultDataMap), new PeriodAnalysisDataTableModel(
							DATA_COLUMN_TYPES, periodAnalysisAlgorithm.getTopHits()));
		}

		return dialog;
	}

	// ** Mandatory message listeners. **

	protected void newStarAction(NewStarMessage message) {
		reset();
		newStarMessage = message;
	}

	protected void meanSourceSeriesChangeAction(
			MeanSourceSeriesChangeMessage message) {
		reset();
		meanSourceSeriesChangeMessage = message;
	}

	// ** Private helper methods **

	public void reset() {
		periodAnalysisAlgorithm = null;
		dialog = null;
	}
}
