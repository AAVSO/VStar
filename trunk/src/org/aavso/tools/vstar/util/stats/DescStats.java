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
import java.util.LinkedList;
import java.util.List;

import org.aavso.tools.vstar.data.Magnitude;
import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.ui.model.plot.ITimeElementEntity;

/**
 * Descriptive statistics functions for observations.
 * 
 * For a series of mean data to make sense, it should only include a single
 * band, so the data sources passed to the functions below should should consist
 * of observations in just such a single band.
 * 
 * Discrepant observations are ignored in all calculations.
 */
public class DescStats {

	/**
	 * Calculates the mean of a sequence of magnitudes for observations in a
	 * specified inclusive range.
	 * 
	 * @param observations
	 *            A valid observation.
	 * @param minIndex
	 *            The first observation index in the inclusive range.
	 * @param maxIndex
	 *            The last observation index in the inclusive range.
	 * @return The mean of magnitudes in the range.
	 */
	public static double calcMagMeanInRange(
			List<ValidObservation> observations, int minIndex, int maxIndex) {

		// Pre-conditions.
		assert (maxIndex >= minIndex);
		assert (maxIndex < observations.size());

		double total = 0;
		double included = 0;

		for (int i = minIndex; i <= maxIndex; i++) {
			if (!observations.get(i).isDiscrepant()) {
				total += observations.get(i).getMag();
				included++;
			}
		}

		return total / included;
	}

	/**
	 * Calculates the standard deviation of a sample of magnitudes for
	 * observations in a specified inclusive range.
	 * 
	 * We use the sample standard deviation formula as per
	 * http://www.aavso.org/education/vsa/Chapter10.pdf. See also a discussion
	 * of this here: http://en.wikipedia.org/wiki/Standard_deviation
	 * 
	 * @param observations
	 *            The observations to which binning will be applied.
	 * @param minIndex
	 *            The first observation index in the inclusive range.
	 * @param maxIndex
	 *            The last observation index in the inclusive range.
	 * @return The sample standard deviation of the magnitudes in the range.
	 */
	public static double calcMagSampleStdDevInRange(
			List<ValidObservation> observations, int minIndex, int maxIndex) {

		// Pre-conditions.
		assert (maxIndex >= minIndex);
		assert (maxIndex < observations.size());

		double magMean = calcMagMeanInRange(observations, minIndex, maxIndex);

		double total = 0;
		double included = 0;

		for (int i = minIndex; i <= maxIndex; i++) {
			if (!observations.get(i).isDiscrepant()) {
				double delta = observations.get(i).getMag() - magMean;
				total += delta * delta;
				included++;
			}
		}

		// Standard sample variance.
		// The sample standard deviation requires total-1 as denominator.
		double variance = total / (included - 1);

		return Math.sqrt(variance);
	}

	/**
	 * Calculates the mean magnitude and the Standard Error of the Average 
	 * for a sample of magnitudes for observations in a specified inclusive 
	 * range.
	 * 
	 * We use the sample standard deviation formula as per
	 * http://www.aavso.org/education/vsa/Chapter10.pdf. See also a discussion
	 * of this here: http://en.wikipedia.org/wiki/Standard_deviation
	 * 
	 * @param observations
	 *            A list of valid observations.
	 * @param timeElementEntity
	 *            A time element source for observations.
	 * @param minIndex
	 *            The first observation index in the inclusive range.
	 * @param maxIndex
	 *            The last observation index in the inclusive range.
	 * @return A ValidObservation instance whose time parameter (JD, phase) is
	 *         at the mid-point between the two indexed observations, and whose
	 *         magnitude captures the mean of magnitude values in that range,
	 *         and the Standard Error of the Average for that mean magnitude
	 *         value.
	 */
	public static ValidObservation createMeanObservationForRange(
			List<ValidObservation> observations,
			ITimeElementEntity timeElementEntity, int minIndex, int maxIndex) {

		// Pre-conditions.
		assert (maxIndex >= minIndex);
		assert (maxIndex < observations.size());

		double magMean = calcMagMeanInRange(observations, minIndex, maxIndex);

		double total = 0;
		double included = 0;

		for (int i = minIndex; i <= maxIndex; i++) {
			if (!observations.get(i).isDiscrepant()) {
				double delta = observations.get(i).getMag() - magMean;
				total += delta * delta;
				included++;
			}
		}

		// Standard sample variance.
		double variance = total / (included - 1);
		double magStdDev = Math.sqrt(variance);
		double magStdErrOfMean = magStdDev / Math.sqrt(included);

		// If in any of the 3 steps above we get NaN, we use 0
		// (e.g. because there is only one sample), we set the
		// Standard Error of the Average to 0.
		if (Double.isNaN(magStdErrOfMean)) {
			magStdErrOfMean = 0;
		}

		// Create the mean observation.
		double meanTimeElement = (timeElementEntity.getTimeElement(
				observations, minIndex) + timeElementEntity.getTimeElement(
				observations, maxIndex)) / 2;

		ValidObservation observation = new ValidObservation();
		observation.setMagnitude(new Magnitude(magMean, magStdErrOfMean));
		observation.setBand(SeriesType.MEANS);		
		timeElementEntity.setTimeElement(observation, meanTimeElement);

		return observation;
	}

	/**
	 * Create a sequence of observations based upon bin size. The observations
	 * represent mean magnitudes at the mid-point of each bin. Each bin consists
	 * of the range index..index+binSize-1
	 * 
	 * Observation bins are populated from left to right of the time domain.
	 * 
	 * @param observations
	 *            The observations to which binning will be applied.
	 * @param timeElementEntity
	 *            A time element source for observations.
	 * @param timeElementsInBin
	 *            The bin size in number of time elements (days, phase
	 *            increments) or portions thereof.
	 * @return An observation sequence consisting of magnitude means per bin and
	 *         the observation at the center point of each bin.
	 * @deprecated Use createSymmetricBinnedObservations() in preference.
	 */
	public static List<ValidObservation> createLeftToRightBinnedObservations(
			List<ValidObservation> observations,
			ITimeElementEntity timeElementEntity, double timeElementsInBin) {

		List<ValidObservation> binnedObs = new ArrayList<ValidObservation>();

		double minTimeElement = timeElementEntity.getTimeElement(observations,
				0);
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

				ValidObservation ob = createMeanObservationForRange(
						observations, timeElementEntity, minIndex, maxIndex);

				// If the mean magnitude value is NaN (e.g. because
				// there was no valid data in the range in question),
				// it doesn't make sense to include this observation.
				if (!Double.isNaN(ob.getMag())) {
					binnedObs.add(ob);
				}

				minIndex = i;
				minTimeElement = timeElementEntity.getTimeElement(observations,
						i);

				i++;
			}
		}

		// Ensure that if we have reached the end of the observations
		// that we include any left over data that would otherwise be
		// excluded by the less-than constraint?
		if (maxIndex < observations.size() - 1) {
			ValidObservation ob = createMeanObservationForRange(observations,
					timeElementEntity, minIndex, observations.size() - 1);

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
	 * represent mean magnitudes at the mid-point of each bin. Each bin consists
	 * of the range index..index+binSize-1
	 * 
	 * Observation bins are populated from center to left, then from center to
	 * right of the time domain.
	 * 
	 * @param observations
	 *            The observations to which binning will be applied.
	 * @param timeElementEntity
	 *            A time element source for observations.
	 * @param timeElementsInBin
	 *            The bin size in number of time elements (days, phase
	 *            increments) or portions thereof.
	 * @return An observation sequence consisting of magnitude means per bin and
	 *         the observation at the center point of each bin.
	 */
	public static List<ValidObservation> createSymmetricBinnedObservations(
			List<ValidObservation> observations,
			ITimeElementEntity timeElementEntity, double timeElementsInBin) {

		List<ValidObservation> binnedObs = new LinkedList<ValidObservation>();

		createLeftmostBinnedObservations(observations,
				observations.size() / 2 - 1, timeElementEntity,
				timeElementsInBin, binnedObs);

		createRightmostBinnedObservations(observations,
				observations.size() / 2, timeElementEntity, timeElementsInBin,
				binnedObs);

		return binnedObs;
	}

	// Helpers

	/**
	 * Create a sequence of observations based upon bin size. The observations
	 * represent mean magnitudes at the mid-point of each bin. Each bin consists
	 * of the range index..index+binSize-1
	 * 
	 * Observation bins are populated only from the left-most region of the
	 * supplied list, starting at the specified index.
	 * 
	 * @param obs
	 *            The observations to which binning will be applied.
	 * @param startIndex
	 *            The starting index in the list.
	 * @param timeElementEntity
	 *            A time element source for observations.
	 * @param timeElementsInBin
	 *            The bin size in number of time elements (days, phase
	 *            increments) or portions thereof.
	 * @param binnedObs
	 *            An observation sequence consisting of magnitude means per bin
	 *            and the observation at the center point of each bin.
	 */
	protected static void createLeftmostBinnedObservations(
			List<ValidObservation> observations, int startIndex,
			ITimeElementEntity timeElementEntity, double timeElementsInBin,
			List<ValidObservation> binnedObs) {

		int maxIndex = startIndex;

		double maxTimeElement = timeElementEntity.getTimeElement(observations,
				maxIndex);

		int i = startIndex - 1;

		boolean finished = false;

		while (!finished) {
			// Are we still either:
			// o not at the start of the list or
			// o not at the bottom of the current range?
			// If either is true, search further to the left.
			if (i >= 0
					&& timeElementEntity.getTimeElement(observations, i)
							+ timeElementsInBin > maxTimeElement) {
				i--;
			} else {
				// Otherwise, we have found the bottom of the current range,
				// so add a ValidObservation containing mean and error value
				// to the list.
				ValidObservation ob = createMeanObservationForRange(
						observations, timeElementEntity, i + 1, maxIndex);

				// If the mean magnitude value is NaN (e.g. because
				// there was no valid data in the range in question),
				// it doesn't make sense to include this observation.
				if (!Double.isNaN(ob.getMag())) {
					// Notice that we add to the start of the list
					// to avoid having to reverse the list since we
					// are moving from right to left along the original
					// list.
					binnedObs.add(0, ob);
				}

				// If we have not yet reached the start of the list, prepare
				// for the next round of range finding.
				if (i >= 0) {
					maxIndex = i;
					maxTimeElement = timeElementEntity.getTimeElement(
							observations, maxIndex);

					i--;
				} else {
					finished = true;
				}
			}
		}
	}

	/**
	 * Create a sequence of observations based upon bin size. The observations
	 * represent mean magnitudes at the mid-point of each bin. Each bin consists
	 * of the range index..index+binSize-1
	 * 
	 * Observation bins are populated only from the right-most region of the
	 * supplied list, starting at the specified index.
	 * 
	 * @param obs
	 *            The observations to which binning will be applied.
	 * @param startIndex
	 *            The starting index in the list.
	 * @param timeElementEntity
	 *            A time element source for observations.
	 * @param timeElementsInBin
	 *            The bin size in number of time elements (days, phase
	 *            increments) or portions thereof.
	 * @param binnedObs
	 *            An observation sequence consisting of magnitude means per bin
	 *            and the observation at the center point of each bin.
	 */
	protected static void createRightmostBinnedObservations(
			List<ValidObservation> observations, int startIndex,
			ITimeElementEntity timeElementEntity, double timeElementsInBin,
			List<ValidObservation> binnedObs) {

		int minIndex = startIndex;

		double minTimeElement = timeElementEntity.getTimeElement(observations,
				minIndex);

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
				ValidObservation ob = createMeanObservationForRange(
						observations, timeElementEntity, minIndex, i - 1);

				// If the mean magnitude value is NaN (e.g. because
				// there was no valid data in the range in question),
				// it doesn't make sense to include this observation.
				if (!Double.isNaN(ob.getMag())) {
					binnedObs.add(ob);
				}

				if (i < observations.size()) {
					minIndex = i;
					minTimeElement = timeElementEntity.getTimeElement(
							observations, minIndex);

					i++;
				} else {
					finished = true;
				}
			}
		}
	}
}
