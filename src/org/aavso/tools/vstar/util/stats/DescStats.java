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
package org.aavso.tools.vstar.util.stats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.aavso.tools.vstar.data.Magnitude;
import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.ui.model.plot.ITimeElementEntity;
import org.aavso.tools.vstar.ui.model.plot.JDTimeElementEntity;

/**
 * Descriptive statistics functions for observations.
 * 
 * For a series of mean observational data to make sense, it should only include
 * a single band (or highly related bands such as Johnson V and Visual), so the
 * data collections passed to the functions below should be chosen accordingly.
 * 
 * Discrepant observations are ignored in all calculations.
 */
public class DescStats {

	public final static int MEAN_MAG_INDEX = 0;
	public final static int MEAN_TIME_INDEX = 1;

	/**
	 * Calculates the means of a sequence of magnitudes and time elements for
	 * observations in a specified inclusive range.
	 * 
	 * @param observations      A list of valid observations.
	 * @param timeElementEntity A time element source for observations.
	 * @param minIndex          The first observation index in the inclusive range.
	 * @param maxIndex          The last observation index in the inclusive range.
	 * @return The means of magnitudes and time elements in the range as a 2-element
	 *         double array.
	 */
	public static double[] calcMagMeanInRange(List<ValidObservation> observations, ITimeElementEntity timeElementEntity,
			int minIndex, int maxIndex) {

		// Pre-conditions.
		assert (!observations.isEmpty());
		assert (maxIndex >= minIndex);
		assert (maxIndex < observations.size());

		double totalMag = 0;
		double totalTimeElement = 0;
		double included = 0;

		for (int i = minIndex; i <= maxIndex; i++) {
			if (!observations.get(i).isDiscrepant()) {
				totalMag += observations.get(i).getMag();
				totalTimeElement += timeElementEntity.getTimeElement(observations, i);
				included++;
			}
		}

		double[] meanPair = new double[2];
		meanPair[MEAN_MAG_INDEX] = totalMag / included;
		meanPair[MEAN_TIME_INDEX] = totalTimeElement / included;

		return meanPair;
	}

	/**
	 * Calculate the mean time element from a list of observations.
	 * 
	 * @param observations      A list of valid observations.
	 * @param timeElementEntity A time element source for observations.
	 * @return The mean of time elements.
	 */
	public static double calcTimeElementMean(List<ValidObservation> observations,
			ITimeElementEntity timeElementEntity) {

		// Pre-conditions.
		assert (!observations.isEmpty());

		double sum = 0;

		for (int i = 0; i < observations.size(); i++) {
			sum += timeElementEntity.getTimeElement(observations, i);
		}

		return sum / observations.size();
	}

	/**
	 * Calculates the variance of a sample of magnitudes for observations in a
	 * specified inclusive range.
	 * 
	 * @param observations The observations for which the variance will be computed.
	 * @param minIndex     The first observation index in the inclusive range.
	 * @param maxIndex     The last observation index in the inclusive range.
	 * @return The sample variance of the magnitudes in the range.
	 */
	public static double calcMagSampleVarianceInRange(List<ValidObservation> observations, int minIndex, int maxIndex) {

		return calcMagCommonVarianceInRange(observations, JDTimeElementEntity.instance, minIndex, maxIndex, true);
	}

	/**
	 * Calculates the sample standard deviation of a sample of magnitudes for
	 * observations in a specified inclusive range.
	 * 
	 * @param observations The observations for which the standard deviation will be
	 *                     computed.
	 * @param minIndex     The first observation index in the inclusive range.
	 * @param maxIndex     The last observation index in the inclusive range.
	 * @return The sample standard deviation of the magnitudes in the range.
	 */
	public static double calcMagSampleStdDevInRange(List<ValidObservation> observations, int minIndex, int maxIndex) {

		double variance = calcMagCommonVarianceInRange(observations, JDTimeElementEntity.instance, minIndex, maxIndex,
				true);
		return Math.sqrt(variance);
	}

	/**
	 * Calculates the variance of a population of magnitudes for observations in a
	 * specified inclusive range.
	 * 
	 * @param observations The observations for which the variance will be computed.
	 * @param minIndex     The first observation index in the inclusive range.
	 * @param maxIndex     The last observation index in the inclusive range.
	 * @return The population variance of the magnitudes in the range.
	 */
	public static double calcMagPopulationVarianceInRange(List<ValidObservation> observations, int minIndex, int maxIndex) {

		return calcMagCommonVarianceInRange(observations, JDTimeElementEntity.instance, minIndex, maxIndex, false);
	}

	/**
	 * Calculates the population standard deviation of a population of magnitudes
	 * for observations in a specified inclusive range.
	 * 
	 * @param observations The observations for which the standard deviation will be
	 *                     computed.
	 * @param minIndex     The first observation index in the inclusive range.
	 * @param maxIndex     The last observation index in the inclusive range.
	 * @return The population standard deviation of the magnitudes in the range.
	 */
	public static double calcMagPopulationStdDevInRange(List<ValidObservation> observations, int minIndex,
			int maxIndex) {

		double variance = calcMagCommonVarianceInRange(observations, JDTimeElementEntity.instance, minIndex, maxIndex,
				false);
		return Math.sqrt(variance);
	}

	/**
	 * Calculates the (sample of population) variance of magnitudes for observations
	 * in a specified inclusive range.
	 * 
	 * @param observations      The observations for which the variance will be
	 *                          computed.
	 * @param timeElementEntity A time element source for observations.
	 * @param minIndex          The first observation index in the inclusive range.
	 * @param maxIndex          The last observation index in the inclusive range.
	 * @param isSample          True if a sample variance is required, false if a
	 *                          population variance is required.
	 * @return The (sample) variance of the magnitudes in the range.
	 */
	private static double calcMagCommonVarianceInRange(List<ValidObservation> observations,
			ITimeElementEntity timeElementEntity, int minIndex, int maxIndex, boolean isSample) {

		// pre-conditions
		assert (!observations.isEmpty());
		assert (maxIndex >= minIndex);
		assert (maxIndex < observations.size());

		double magMean = calcMagMeanInRange(observations, timeElementEntity, minIndex, maxIndex)[MEAN_MAG_INDEX];

		double total = 0;
		double included = 0;

		for (int i = minIndex; i <= maxIndex; i++) {
			if (!observations.get(i).isDiscrepant()) {
				double delta = observations.get(i).getMag() - magMean;
				total += delta * delta;
				included++;
			}
		}

		// Population or sample variance; sample variance requires N-1 as
		// denominator.
		return total / (included - (isSample ? 1 : 0));
	}

	/**
	 * Calculates the mean magnitude and the Standard Error of the Average for a
	 * sample of magnitudes for observations in a specified inclusive range.
	 * 
	 * We use the sample standard deviation formula as per
	 * https://www.aavso.org/sites/default/files/education/vsa/Chapter10.pdf<br/>
	 * See also a discussion of this here:
	 * http://en.wikipedia.org/wiki/Standard_deviation
	 * 
	 * @param observations      A list of valid observations.
	 * @param timeElementEntity A time element source for observations.
	 * @param minIndex          The first observation index in the inclusive range.
	 * @param maxIndex          The last observation index in the inclusive range.
	 * @return A Bin object containing magnitude bin data and a ValidObservation
	 *         instance whose time parameter (JD or phase) is the mean of the
	 *         indexed observations, and whose magnitude captures the mean of
	 *         magnitude values in that range, and the Standard Error of the Average
	 *         for that mean magnitude value. The binned magnitude data can be used
	 *         for further analysis such as ANOVA.
	 */
	public static Bin createMeanObservationForRange(List<ValidObservation> observations,
			ITimeElementEntity timeElementEntity, int minIndex, int maxIndex) {

		// Pre-conditions.
		assert (!observations.isEmpty());
		assert (maxIndex >= minIndex);
		assert (maxIndex < observations.size());

		double[] meanPair = calcMagMeanInRange(observations, timeElementEntity, minIndex, maxIndex);

		double magMean = meanPair[MEAN_MAG_INDEX];
		double timeMean = meanPair[MEAN_TIME_INDEX];

		double total = 0;
		double included = 0;

		int size = (maxIndex - minIndex) + 1;
		double[] binData = new double[size];

		for (int i = minIndex; i <= maxIndex; i++) {
			if (!observations.get(i).isDiscrepant()) {
				double mag = observations.get(i).getMag();
				double delta = observations.get(i).getMag() - magMean;
				total += delta * delta;
				included++;
				binData[i - minIndex] = mag;
			}
		}

		// Standard sample variance, deviation and error of average.
		double variance = total / (included - 1);
		double magStdDev = Math.sqrt(variance);
		double magStdErrOfMean = magStdDev / Math.sqrt(included);

		// If in any of the steps above we get NaN, we use 0
		// (e.g. because there is only one sample), we set the
		// Standard Error of the Average to 0.
		if (Double.isNaN(magStdErrOfMean)) {
			magStdErrOfMean = 0;
		}

		// Create the mean observation, using an arbitrary observation
		// to obtain the object name.
		ValidObservation observation = new ValidObservation();
		observation.setMagnitude(new Magnitude(magMean, magStdErrOfMean));
		observation.setBand(SeriesType.MEANS);
		observation.setName(observations.get(0).getName());
		timeElementEntity.setTimeElement(observation, timeMean);

		// If bin data array contains only one element, we replace this
		// with a pair since some implementations of ANOVA (e.g. Apache
		// Commons Math) require all sample sizes to be greater than one.
		// Question: even though the mean resulting from this will be just
		// the value itself, will the greater sample size of this bin skew
		// the ANOVA result in any way?
		if (binData.length == 1) {
			double datum = binData[0];
			binData = new double[] { datum, datum };
		}

		return new Bin(observation, binData);
	}

	/**
	 * Create a sequence of observations based upon bin size. The observations
	 * represent mean magnitudes at the mid-point of each bin. Each bin consists of
	 * the range index..index+binSize-1
	 * 
	 * Observation bins are populated from left to right of the time domain.
	 * 
	 * @param observations      The observations to which binning will be applied.
	 * @param timeElementEntity A time element source for observations.
	 * @param timeElementsInBin The bin size in number of time elements (days, phase
	 *                          increments) or portions thereof.
	 * @return An observation sequence consisting of magnitude means per bin and the
	 *         observation at the center point of each bin.
	 * Use createSymmetricBinnedObservations() in preference.
	 */
	@Deprecated
	public static List<ValidObservation> createLeftToRightBinnedObservations(List<ValidObservation> observations,
			ITimeElementEntity timeElementEntity, double timeElementsInBin) {

		// Pre-conditions.
		assert (!observations.isEmpty());

		List<ValidObservation> binnedObs = new ArrayList<ValidObservation>();

		double minTimeElement = timeElementEntity.getTimeElement(observations, 0);
		int minIndex = 0;
		int maxIndex = 0;

		int i = 1;

		while (i < observations.size()) {

			// If we have not reached the end of the observation list
			// and the current observation's time element is less than the
			// minimum time element for the bottom of the current range plus
			// the number of time elements in the bin, continue to the next
			// observation.
			if (i < observations.size()
					&& timeElementEntity.getTimeElement(observations, i) < (minTimeElement + timeElementsInBin)) {
				i++;
			} else {
				// Otherwise, we have found the top of the current range,
				// so add a ValidObservation containing mean and error value
				// to the list.
				maxIndex = i - 1;

				Bin bin = createMeanObservationForRange(observations, timeElementEntity, minIndex, maxIndex);

				ValidObservation ob = bin.getMeanObservation();

				// If the mean magnitude value is NaN (e.g. because
				// there was no valid data in the range in question),
				// it doesn't make sense to include this observation.
				if (!Double.isNaN(ob.getMag())) {
					binnedObs.add(ob);
				}

				minIndex = i;
				minTimeElement = timeElementEntity.getTimeElement(observations, i);

				i++;
			}
		}

		// Ensure that if we have reached the end of the observations
		// that we include any left over data that would otherwise be
		// excluded by the less-than constraint?
		if (maxIndex < observations.size() - 1) {
			Bin bin = createMeanObservationForRange(observations, timeElementEntity, minIndex, observations.size() - 1);

			ValidObservation ob = bin.getMeanObservation();

			// If the mean magnitude value is NaN (e.g. because
			// there was no valid data in the range in question),
			// it doesn't make sense to include this observation.
			if (!Double.isNaN(ob.getMag())) {
				binnedObs.add(ob);
			}
		}

		return binnedObs;
	}

	/**
	 * Create a sequence of observations based upon bin size. The observations
	 * represent mean magnitudes at the mid-point of each bin. Each bin consists of
	 * the range index..index+binSize-1
	 * 
	 * Observation bins are populated from center to left, then from center to right
	 * of the time domain to ensure symmetric bins.
	 * 
	 * The final result also includes a one-way anova statistic.
	 * 
	 * @param observations      The observations to which binning will be applied.
	 * @param timeElementEntity A time element source for observations.
	 * @param timeElementsInBin The bin size in number of time elements (days, phase
	 *                          increments) or portions thereof.
	 * @return An observation sequence consisting of magnitude means per bin and the
	 *         observation at the center point of each bin. If there were
	 *         insufficient observations, the empty list is returned.
	 */
	public static BinningResult createSymmetricBinnedObservations(List<ValidObservation> observations,
			ITimeElementEntity timeElementEntity, double timeElementsInBin) {

		// Pre-conditions.
		assert (!observations.isEmpty());

		SeriesType series = SeriesType.Unknown;
		List<ValidObservation> binnedObs = Collections.EMPTY_LIST;
		List<double[]> magnitudeBins = Collections.EMPTY_LIST;

		// Are there sufficient (size > 1) observations to create
		// binned mean observations?
		if (observations.size() > 1) {
			binnedObs = new LinkedList<ValidObservation>();
			magnitudeBins = new LinkedList<double[]>();

			createLeftmostBinnedObservations(observations, observations.size() / 2 - 1, timeElementEntity,
					timeElementsInBin, binnedObs, magnitudeBins);

			createRightmostBinnedObservations(observations, observations.size() / 2, timeElementEntity,
					timeElementsInBin, binnedObs, magnitudeBins);

			series = observations.get(0).getBand();
		}

		return new BinningResult(series, observations.size(), binnedObs, magnitudeBins);
	}

	// Helpers

	/**
	 * Create a sequence of observations based upon bin size. The observations
	 * represent mean magnitudes at the mid-point of each bin. Each bin consists of
	 * the range index..index+binSize-1
	 * 
	 * Observation bins are populated only from the left-most region of the supplied
	 * list, starting at the specified index.
	 * 
	 * @param obs               The observations to which binning will be applied.
	 * @param startIndex        The starting index in the list.
	 * @param timeElementEntity A time element source for observations.
	 * @param timeElementsInBin The bin size in number of time elements (days, phase
	 *                          increments) or portions thereof.
	 * @param binnedObs         An observation sequence consisting of magnitude
	 *                          means per bin and the observation at the center
	 *                          point of each bin.
	 * @param bins              A list of binned data (magnitude) arrays.
	 */
	protected static void createLeftmostBinnedObservations(List<ValidObservation> observations, int startIndex,
			ITimeElementEntity timeElementEntity, double timeElementsInBin, List<ValidObservation> binnedObs,
			List<double[]> bins) {

		int maxIndex = startIndex;

		double maxTimeElement = timeElementEntity.getTimeElement(observations, maxIndex);

		int i = startIndex - 1;

		boolean finished = false;

		while (!finished) {
			// Are we still either:
			// o not at the start of the list or
			// o not at the bottom of the current range?
			// If either is true, search further to the left.
			if (i >= 0 && timeElementEntity.getTimeElement(observations, i) + timeElementsInBin > maxTimeElement) {
				i--;
			} else {
				// Otherwise, we have found the bottom of the current range,
				// so add a ValidObservation containing mean and error value
				// to the list.
				Bin bin = createMeanObservationForRange(observations, timeElementEntity, i + 1, maxIndex);

				ValidObservation ob = bin.getMeanObservation();

				// If the mean magnitude value is NaN (e.g. because
				// there was no valid data in the range in question),
				// it doesn't make sense to include this bin.
				if (!Double.isNaN(ob.getMag())) {
					// Notice that we add to the start of the list
					// to avoid having to reverse the list since we
					// are moving from right to left along the original
					// list.
					binnedObs.add(0, ob);
					bins.add(0, bin.getMagnitudes());
				}

				// If we have not yet reached the start of the list, prepare
				// for the next round of range finding.
				if (i >= 0) {
					maxIndex = i;
					maxTimeElement = timeElementEntity.getTimeElement(observations, maxIndex);

					i--;
				} else {
					finished = true;
				}
			}
		}
	}

	/**
	 * Create a sequence of observations based upon bin size. The observations
	 * represent mean magnitudes at the mid-point of each bin. Each bin consists of
	 * the range index..index+binSize-1
	 * 
	 * Observation bins are populated only from the right-most region of the
	 * supplied list, starting at the specified index.
	 * 
	 * @param obs               The observations to which binning will be applied.
	 * @param startIndex        The starting index in the list.
	 * @param timeElementEntity A time element source for observations.
	 * @param timeElementsInBin The bin size in number of time elements (days, phase
	 *                          increments) or portions thereof.
	 * @param binnedObs         An observation sequence consisting of magnitude
	 *                          means per bin and the observation at the center
	 *                          point of each bin.
	 * @param bins              A list of binned data (magnitude) arrays.
	 */
	protected static void createRightmostBinnedObservations(List<ValidObservation> observations, int startIndex,
			ITimeElementEntity timeElementEntity, double timeElementsInBin, List<ValidObservation> binnedObs,
			List<double[]> bins) {

		int minIndex = startIndex;

		double minTimeElement = timeElementEntity.getTimeElement(observations, minIndex);

		int i = startIndex + 1;

		boolean finished = false;

		while (!finished) {
			// Are we still either:
			// o not at the end of the list or
			// o not at the top of the current range?
			// If either is true, search further to the right.
			if (i < observations.size()
					&& (minTimeElement + timeElementsInBin) > timeElementEntity.getTimeElement(observations, i)) {
				i++;
			} else {
				// Otherwise, we have found the top of the current range,
				// so add a ValidObservation containing mean and error value
				// to the list.
				Bin bin = createMeanObservationForRange(observations, timeElementEntity, minIndex, i - 1);

				ValidObservation ob = bin.getMeanObservation();

				// If the mean magnitude value is NaN (e.g. because
				// there was no valid data in the range in question),
				// it doesn't make sense to include this bin.
				if (!Double.isNaN(ob.getMag())) {
					binnedObs.add(ob);
					bins.add(bin.getMagnitudes());
				}

				if (i < observations.size()) {
					minIndex = i;
					minTimeElement = timeElementEntity.getTimeElement(observations, minIndex);

					i++;
				} else {
					finished = true;
				}
			}
		}
	}
}
