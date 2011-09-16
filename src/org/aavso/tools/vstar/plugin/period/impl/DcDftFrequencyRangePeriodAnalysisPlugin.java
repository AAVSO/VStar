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

import java.util.ArrayList;
import java.util.List;

import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.exception.AlgorithmError;
import org.aavso.tools.vstar.exception.CancellationException;
import org.aavso.tools.vstar.ui.dialog.MultiNumberEntryDialog;
import org.aavso.tools.vstar.ui.dialog.NumberField;
import org.aavso.tools.vstar.util.period.dcdft.DcDftAnalysisType;
import org.aavso.tools.vstar.util.period.dcdft.TSDcDft;

/**
 * This class encapsulates the "frequency range" (as per AAVSO TS) form of
 * the DC DFT period analysis algorithm.
 */
public class DcDftFrequencyRangePeriodAnalysisPlugin extends
		DcDftPeriodAnalysisPluginBase {

	private Double currLoFreq;
	private Double currHiFreq;
	private Double currResolution;

	NumberField loFreqField;
	NumberField hiFreqField;
	NumberField resolutionField;

	/**
	 * Constructor
	 */
	public DcDftFrequencyRangePeriodAnalysisPlugin() {
		currLoFreq = null;
		currHiFreq = null;
		currResolution = null;
	}

	@Override
	public void executeAlgorithm(List<ValidObservation> obs)
			throws AlgorithmError, CancellationException {
		assert newStarMessage != null;

		periodAnalysisAlgorithm = new TSDcDft(obs, DcDftAnalysisType.FREQUENCY_RANGE);

		if (currLoFreq == null) {
			// Get these default values only once per dataset. See also reset()
			// which is called by newStarAtion().
			currLoFreq = periodAnalysisAlgorithm.getLoFreqValue();
			currHiFreq = periodAnalysisAlgorithm.getHiFreqValue();
			currResolution = periodAnalysisAlgorithm.getResolutionValue();
		}

		MultiNumberEntryDialog paramDialog = createParamDialog();

		if (!paramDialog.isCancelled()) {
			currLoFreq = loFreqField.getValue();
			currHiFreq = hiFreqField.getValue();
			currResolution = resolutionField.getValue();

			periodAnalysisAlgorithm.setLoFreqValue(currLoFreq);
			periodAnalysisAlgorithm.setHiFreqValue(currHiFreq);
			periodAnalysisAlgorithm.setResolutionValue(currResolution);

			periodAnalysisAlgorithm.execute();
		} else {
			throw new CancellationException();
		}
	}

	private MultiNumberEntryDialog createParamDialog() {
		List<NumberField> fields = new ArrayList<NumberField>();

		loFreqField = new NumberField("Low Frequency", 0.0, null, currLoFreq);
		fields.add(loFreqField);

		hiFreqField = new NumberField("High Frequency", 0.0, null, currHiFreq);
		fields.add(hiFreqField);

		resolutionField = new NumberField("Resolution", 0.0, null,
				currResolution);
		fields.add(resolutionField);

		return new MultiNumberEntryDialog("Parameters", fields);
	}

	@Override
	public String getDescription() {
		return "Date Compensated Discrete Fourier Transform with Frequency Range";
	}

	@Override
	public String getDisplayName() {
		return "DC DFT with Frequency Range";
	}

	public void reset() {
		currLoFreq = null;
		currHiFreq = null;
		currResolution = null;
	}
}
