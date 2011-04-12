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
package org.aavso.tools.vstar.plugin.period.impl;

import java.util.List;

import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.exception.AlgorithmError;
import org.aavso.tools.vstar.exception.CancellationException;
import org.aavso.tools.vstar.ui.dialog.PeriodAnalysisParameterDialog;
import org.aavso.tools.vstar.util.period.dcdft.TSDcDft;

/**
 * This class encapsulates the "frequency range scan" (as per AAVSO TS) form of
 * the DC DFT period analysis algorithm.
 */
public class DcDftFrequencyRangePeriodAnalysisPlugin extends
		DcDftPeriodAnalysisPluginBase {

	@Override
	public void executeAlgorithm(List<ValidObservation> obs)
			throws AlgorithmError, CancellationException {
		assert newStarMessage != null;

		if (periodAnalysisAlgorithm == null) {
			periodAnalysisAlgorithm = new TSDcDft(
					obs, true);

			double loFreq = periodAnalysisAlgorithm.getLoFreqValue();
			double hiFreq = periodAnalysisAlgorithm.getHiFreqValue();
			double resolution = periodAnalysisAlgorithm.getResolutionValue();

			PeriodAnalysisParameterDialog paramDialog = new PeriodAnalysisParameterDialog(
					loFreq, hiFreq, resolution);

			if (!paramDialog.isCancelled()) {
				periodAnalysisAlgorithm.setLoFreqValue(paramDialog.getLoFreq());
				periodAnalysisAlgorithm.setHiFreqValue(paramDialog.getHiFreq());
				periodAnalysisAlgorithm.setResolutionValue(paramDialog
						.getResolution());

				periodAnalysisAlgorithm.execute();
			} else {
				throw new CancellationException();
			}
		}
	}

	@Override
	public String getDescription() {
		return "Date Compensated Discrete Fourier Transform with Frequency Range";
	}

	@Override
	public String getDisplayName() {
		return "DC DFT with Frequency Range";
	}
}
