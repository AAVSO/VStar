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
package org.aavso.tools.vstar.util.period;

import java.util.List;
import java.util.Map;

import org.aavso.tools.vstar.exception.AlgorithmError;
import org.aavso.tools.vstar.util.IAlgorithm;
import org.aavso.tools.vstar.util.model.Harmonic;
import org.aavso.tools.vstar.util.model.PeriodAnalysisDerivedMultiPeriodicModel;
import org.aavso.tools.vstar.util.period.dcdft.PeriodAnalysisDataPoint;

/**
 * Classes implementing a period analysis algorithm to be executed must realise
 * this interface.
 */
public interface IPeriodAnalysisAlgorithm extends IAlgorithm {

	/**
	 * Return the result of the period analysis.
	 */
	abstract public Map<PeriodAnalysisCoordinateType, List<Double>> getResultSeries();

	/**
	 * Return the "top hits" of the period analysis.
	 * 
	 * It is a precondition that results have been generated, i.e. the execute()
	 * method has been invoked.
	 */
	abstract public Map<PeriodAnalysisCoordinateType, List<Double>> getTopHits();

	/**
	 * <p>
	 * Refine the period analysis in some way that makes sense for the
	 * algorithm, e.g. for DC DFT, CLEANest.
	 * </p>
	 * 
	 * <p>
	 * Note: This method is provisional. There are all kinds of meanings that
	 * could be applied to "refine", e.g. multi-period analysis (such as
	 * CLEANest).
	 * </p>
	 * 
	 * @param freqs
	 *            A list of frequencies on which to refine the results.
	 * @param varPeriods
	 *            The variable periods to be included. May be null.
	 * @param lockedPeriods
	 *            The locked periods to be included. May be null.
	 * @return the new top-hits created by this refinement.
	 */
	abstract public List<PeriodAnalysisDataPoint> refineByFrequency(
			List<Double> freqs, List<Double> variablePeriods,
			List<Double> lockedPeriod) throws AlgorithmError;

	/**
	 * Get the refine-by-frequency algorithm name.
	 * 
	 * @return The name of the refine-by-frequency algorithm, or null if none.
	 */
	abstract public String getRefineByFrequencyName();

	/**
	 * Create a multi-periodic fit from the data from a list of harmonics.
	 * 
	 * @param harmonics
	 *            The harmonics to be used to create the fit.
	 * @param model
	 *            A multi-period fit class that takes place in the context of a
	 *            period analysis. Data members in this parameter are populated
	 *            as a result of invoking this method.
	 */
	abstract public void multiPeriodicFit(List<Harmonic> harmonics,
			PeriodAnalysisDerivedMultiPeriodicModel model)
			throws AlgorithmError;
}
