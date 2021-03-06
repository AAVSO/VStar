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
import org.aavso.tools.vstar.util.locale.LocaleProps;
import org.aavso.tools.vstar.util.period.PeriodAnalysisCoordinateType;
import org.aavso.tools.vstar.util.period.dcdft.TSDcDft;

/**
 * This class encapsulates the "standard scan" (as per AAVSO TS) form of the DC
 * DFT period analysis algorithm.
 */
public class DcDftStandardScanPeriodAnalysisPlugin extends
		DcDftPeriodAnalysisPluginBase {

	/**
	 * Constructor
	 */
	public DcDftStandardScanPeriodAnalysisPlugin() {
		super(PeriodAnalysisCoordinateType.FREQUENCY);
	}
	
	@Override
	public void executeAlgorithm(List<ValidObservation> obs)
			throws AlgorithmError, CancellationException {
		assert newStarMessage != null;

		periodAnalysisAlgorithm = new TSDcDft(obs);

		periodAnalysisAlgorithm.execute();
	}

	@Override
	public String getDescription() {
		return LocaleProps.get("DCDFT_STANDARD_SCAN_DESC");
	}

	@Override
	public String getDisplayName() {
		return LocaleProps.get("DCDFT_STANDARD_SCAN_NAME");
	}

	@Override
	public void reset() {
		// Nothing to do.
	}
}
