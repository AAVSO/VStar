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
package org.aavso.tools.vstar.util.stats;

import java.util.List;

import org.aavso.tools.vstar.data.ValidObservation;
import org.apache.commons.math.stat.inference.OneWayAnova;
import org.apache.commons.math.stat.inference.OneWayAnovaImpl;

/**
 * The result of a binning operation including mean observations (with averages
 * and confidence intervals per bin), magnitude bins, and analysis of variance
 * results (F-test, p-value).
 */
public class BinningResult {

	private int sourceObsCount;
	private List<ValidObservation> meanObservations;
	private List<double[]> magnitudeBins;
	private double fValue;
	private double pValue;
	private boolean error;

	/**
	 * Constructor
	 * 
	 * @param sourceObsCount
	 *            The number of source observations.
	 * @param meanObservations
	 *            A list of mean observations.
	 * @param magnitudeBins
	 *            The corresponding magnitude bins that gave rise to the binned
	 *            mean observations.
	 */
	public BinningResult(int sourceObsCount,
			List<ValidObservation> meanObservations,
			List<double[]> magnitudeBins) {
		this.sourceObsCount = sourceObsCount;
		this.meanObservations = meanObservations;
		this.magnitudeBins = magnitudeBins;
		this.error = false;

		OneWayAnova anova = new OneWayAnovaImpl();
		try {
			fValue = anova.anovaFValue(this.magnitudeBins);
			pValue = anova.anovaPValue(this.magnitudeBins);
		} catch (Exception e) {
			error = true;
			fValue = Double.NaN;
			pValue = Double.NaN;
		}
	}

	/**
	 * @return the meanObservations
	 */
	public List<ValidObservation> getMeanObservations() {
		return meanObservations;
	}

	/**
	 * @return the magnitudeBins
	 */
	public List<double[]> getMagnitudeBins() {
		return magnitudeBins;
	}

	/**
	 * @return the fValue
	 */
	public double getFValue() {
		return fValue;
	}

	/**
	 * @return the pValue
	 */
	public double getPValue() {
		return pValue;
	}

	/**
	 * Returns the between-group degrees of freedom for the F-test result. This
	 * is defined as: df(b) = K-1 where K is the number of groups.
	 * 
	 * @return The degrees of freedom.
	 */
	public int getBetweenGroupDF() {
		return magnitudeBins.size() - 1;
	}

	/**
	 * Returns the within-group degrees of freedom for the F-test result. This
	 * is defined as: df(w) = (N1-1)+(N2-1)+...+(Nm-1) where N1..Nm are the
	 * number of values within each group. This should be k (the number of
	 * groups, i.e. bins) less than N, the total number of data-points (observations)
	 * used for binning.
	 * 
	 * @return The degrees of freedom.
	 */
	public int getWithinGroupDF() {
//		int sum = 0;
//
//		for (double[] binData : magnitudeBins) {
//			sum += (binData.length - 1);
//		}
		

		// N-k
		int N_minus_k = sourceObsCount - magnitudeBins.size();
//		assert N_minus_k == sum;
		return N_minus_k; 
	}

	/**
	 * Does this binning result have a sane F-test and p-values? Insufficient
	 * data is one reason why the values may not be valid.
	 * 
	 * @return Whether or not the ANOVA values are valid.
	 */
	public boolean hasValidAnovaValues() {
		return !error;
	}
}
