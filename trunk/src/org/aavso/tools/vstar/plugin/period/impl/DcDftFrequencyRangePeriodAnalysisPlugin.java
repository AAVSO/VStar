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
import org.aavso.tools.vstar.ui.dialog.ITextComponent;
import org.aavso.tools.vstar.ui.dialog.MultiEntryComponentDialog;
import org.aavso.tools.vstar.ui.dialog.DoubleField;
import org.aavso.tools.vstar.util.locale.LocaleProps;
import org.aavso.tools.vstar.util.period.PeriodAnalysisCoordinateType;
import org.aavso.tools.vstar.util.period.dcdft.DcDftAnalysisType;
import org.aavso.tools.vstar.util.period.dcdft.TSDcDft;

/**
 * This class encapsulates the "frequency range" (as per AAVSO TS) form of the
 * DC DFT period analysis algorithm.
 */
public class DcDftFrequencyRangePeriodAnalysisPlugin extends
		DcDftPeriodAnalysisPluginBase {

	private Double currLoFreq;
	private Double currHiFreq;
	private Double currResolution;

	DoubleField loFreqField;
	DoubleField hiFreqField;
	DoubleField resolutionField;

	/**
	 * Constructor
	 */
	public DcDftFrequencyRangePeriodAnalysisPlugin() {
		super(PeriodAnalysisCoordinateType.FREQUENCY);
		currLoFreq = null;
		currHiFreq = null;
		currResolution = null;
	}

	@Override
	public void executeAlgorithm(List<ValidObservation> obs)
			throws AlgorithmError, CancellationException {
		assert newStarMessage != null;

		periodAnalysisAlgorithm = new TSDcDft(obs,
				DcDftAnalysisType.FREQUENCY_RANGE);

		if (currLoFreq == null) {
			// Get these default values only once per dataset. See also reset()
			// which is called by newStarAtion().
			currLoFreq = periodAnalysisAlgorithm.getLoFreqValue();
			currHiFreq = periodAnalysisAlgorithm.getHiFreqValue();
			currResolution = periodAnalysisAlgorithm.getResolutionValue();
		}

		MultiEntryComponentDialog paramDialog = createParamDialog();

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

	private MultiEntryComponentDialog createParamDialog() {
		List<ITextComponent<?>> fields = new ArrayList<ITextComponent<?>>();

		loFreqField = new DoubleField(LocaleProps
				.get("PERIOD_ANALYSIS_PARAMETERS_LOW_FREQUENCY_TITLE"), 0.0, null, currLoFreq);
		fields.add(loFreqField);

		hiFreqField = new DoubleField(LocaleProps
				.get("PERIOD_ANALYSIS_PARAMETERS_HIGH_FREQUENCY_TITLE"), 0.0, null, currHiFreq);
		fields.add(hiFreqField);

		resolutionField = new DoubleField(LocaleProps
				.get("PERIOD_ANALYSIS_PARAMETERS_RESOLUTION_TITLE"), 0.0, null, currResolution);
		fields.add(resolutionField);

		return new MultiEntryComponentDialog(LocaleProps
				.get("PERIOD_ANALYSIS_PARAMETERS_DLG_TITLE"), fields);
	}

	@Override
	public String getDescription() {
		return LocaleProps.get("DCDFT_WITH_FREQ_DESC");
	}

	@Override
	public String getDisplayName() {
		return LocaleProps.get("DCDFT_WITH_FREQ_DISPLAY_NAME");
	}

	public void reset() {
		currLoFreq = null;
		currHiFreq = null;
		currResolution = null;
	}
}
