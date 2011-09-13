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
import org.aavso.tools.vstar.ui.mediator.message.NewStarMessage;
import org.aavso.tools.vstar.ui.model.list.PeriodAnalysisDataTableModel;
import org.aavso.tools.vstar.ui.model.plot.PeriodAnalysis2DPlotModel;
import org.aavso.tools.vstar.util.period.PeriodAnalysisCoordinateType;
import org.aavso.tools.vstar.util.period.dcdft.TSDcDft;

/**
 * Date Compensated Discrete Fourier Transform period analysis plugin.
 */
abstract public class DcDftPeriodAnalysisPluginBase extends
		PeriodAnalysisPluginBase {

	protected NewStarMessage newStarMessage;
	protected TSDcDft periodAnalysisAlgorithm;
	
	private final static PeriodAnalysisCoordinateType[] DATA_COLUMN_TYPES = {
			PeriodAnalysisCoordinateType.FREQUENCY,
			PeriodAnalysisCoordinateType.PERIOD,
			PeriodAnalysisCoordinateType.POWER,
			PeriodAnalysisCoordinateType.AMPLITUDE };

	/**
	 * Constructor.
	 */
	public DcDftPeriodAnalysisPluginBase() {
		super();
	}

	// ** Mandatory interface methods **

	@Override
	public String getDisplayName() {
		return "Date Compensated DFT";
	}

	@Override
	public String getDescription() {
		return "Date Compensated Discrete Fourier Transform";
	}

	/**
	 * @see org.aavso.tools.vstar.plugin.PluginBase#getGroup()
	 */
	@Override
	public String getGroup() {
		return "DC DFT";
	}

	@Override
	public JDialog getDialog(SeriesType sourceSeriesType) {
			List<PeriodAnalysis2DPlotModel> models = new ArrayList<PeriodAnalysis2DPlotModel>();

			Map<PeriodAnalysisCoordinateType, List<Double>> resultDataMap = periodAnalysisAlgorithm
					.getResultSeries();

			// Frequency vs Power
			models.add(new PeriodAnalysis2DPlotModel(resultDataMap,
					PeriodAnalysisCoordinateType.FREQUENCY,
					PeriodAnalysisCoordinateType.POWER, false));

			// Frequency vs Amplitude
			models.add(new PeriodAnalysis2DPlotModel(resultDataMap,
					PeriodAnalysisCoordinateType.FREQUENCY,
					PeriodAnalysisCoordinateType.AMPLITUDE, false));

			// Period vs Power
//			models.add(new PeriodAnalysis2DPlotModel(resultDataMap,
//					PeriodAnalysisCoordinateType.PERIOD,
//					PeriodAnalysisCoordinateType.POWER, false));

			// Period vs Amplitude
//			models.add(new PeriodAnalysis2DPlotModel(resultDataMap,
//					PeriodAnalysisCoordinateType.PERIOD,
//					PeriodAnalysisCoordinateType.AMPLITUDE, false));

			return new PeriodAnalysis2DResultDialog(
					"Period Analysis (DC DFT) for "
							+ newStarMessage.getStarInfo().getDesignation(),
					"(series: " + sourceSeriesType.getDescription() + ")",
					models, new PeriodAnalysisDataTableModel(DATA_COLUMN_TYPES,
							resultDataMap), new PeriodAnalysisDataTableModel(
							DATA_COLUMN_TYPES, periodAnalysisAlgorithm
									.getTopHits()), periodAnalysisAlgorithm);
	}

	// ** Mandatory message listeners. **

	@Override
	protected void newStarAction(NewStarMessage message) {
		newStarMessage = message; 
		reset();
	}
}
