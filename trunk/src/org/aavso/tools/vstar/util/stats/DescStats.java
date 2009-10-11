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
import java.util.List;

import org.aavso.tools.vstar.data.DateInfo;
import org.aavso.tools.vstar.data.IMagAndJDSource;
import org.aavso.tools.vstar.data.Magnitude;
import org.aavso.tools.vstar.data.ValidObservation;

/**
 * Descriptive statistics functions for magnitude and Julian Day sources.
 * 
 * For a series of mean data to make sense, it should only include a single
 * band, so the data sources passed to the functions below should should
 * consist of observations in just such a single band.
 * 
 * Discrepant source values are ignored in all calculations.
 */
public class DescStats {

	public static final int DEFAULT_BIN_DAYS = 20;

	/**
	 * Calculates the mean of a sequence of magnitudes for Julian Days in a
	 * specified inclusive range.
	 * 
	 * @param source
	 *            A source of (magnitude, Julian Day) pairs.
	 * @param minJDIndex
	 *            The first Julian Day index in the inclusive range.
	 * @param maxJDIndex
	 *            The last Julian Day index in the inclusive range.
	 * @return The mean of magnitudes in the JD range.
	 */
	public static double calcMagMeanInJDRange(
			List<? extends IMagAndJDSource> source, int minJDIndex,
			int maxJDIndex) {

		// Pre-conditions.
		assert (maxJDIndex >= minJDIndex);
		assert (maxJDIndex < source.size());

		double total = 0;
		double included = 0;

		for (int i = minJDIndex; i <= maxJDIndex; i++) {
			if (!source.get(i).isDiscrepant()) {
				total += source.get(i).getMag();
				included++;
			}
		}

		return total / included;
	}

	/**
	 * Calculates the standard deviation of a sample of magnitudes for Julian
	 * Days in a specified inclusive range. We use the sample standard deviation
	 * formula as per http://www.aavso.org/education/vsa/Chapter10.pdf. See also
	 * a discussion of this here:
	 * http://en.wikipedia.org/wiki/Standard_deviation
	 * 
	 * @param source
	 *            A source of (magnitude, Julian Day) pairs.
	 * @param minJDIndex
	 *            The first Julian Day index in the inclusive range.
	 * @param maxJDIndex
	 *            The last Julian Day index in the inclusive range.
	 * @return The sample standard deviation of the magnitudes in the JD range.
	 */
	public static double calcMagSampleStdDevInJDRange(
			List<? extends IMagAndJDSource> source, int minJDIndex,
			int maxJDIndex) {

		// Pre-conditions.
		assert (maxJDIndex >= minJDIndex);
		assert (maxJDIndex < source.size());

		double magMean = calcMagMeanInJDRange(source, minJDIndex, maxJDIndex);

		double total = 0;
		double included = 0;

		for (int i = minJDIndex; i <= maxJDIndex; i++) {
			if (!source.get(i).isDiscrepant()) {
				double delta = source.get(i).getMag() - magMean;
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
	 * Calculates the Standard Error of the Average of a sample of magnitudes
	 * for Julian Days in a specified inclusive range. We use the sample
	 * standard deviation formula as per
	 * http://www.aavso.org/education/vsa/Chapter10.pdf. See also a discussion
	 * of this here: http://en.wikipedia.org/wiki/Standard_deviation
	 * 
	 * @param source
	 *            A source of (magnitude, Julian Day) pairs.
	 * @param minJDIndex
	 *            The first Julian Day index in the inclusive range.
	 * @param maxJDIndex
	 *            The last Julian Day index in the inclusive range.
	 * @return A ValidObservation instance whose Julian Day is at the mid-point
	 *         between the two indexed JDs, and whose magnitude captures the
	 *         mean of magnitude values in that range, and the Standard Error of
	 *         the Average for that mean magnitude value.
	 */
	public static ValidObservation createMeanObservationForJDRange(
			List<? extends IMagAndJDSource> source, int minJDIndex,
			int maxJDIndex) {

		// Pre-conditions.
		assert (maxJDIndex >= minJDIndex);
		assert (maxJDIndex < source.size());

		double magMean = calcMagMeanInJDRange(source, minJDIndex, maxJDIndex);

		double total = 0;
		double included = 0;

		for (int i = minJDIndex; i <= maxJDIndex; i++) {
			if (!source.get(i).isDiscrepant()) {
				double delta = source.get(i).getMag() - magMean;
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

		// Mean Julian Day. TODO: could we instead use median?
		double meanJD = (source.get(minJDIndex).getJD() + source
				.get(maxJDIndex).getJD()) / 2;

		ValidObservation observation = new ValidObservation();
		observation.setMagnitude(new Magnitude(magMean, magStdErrOfMean));
		observation.setDateInfo(new DateInfo(meanJD));

		return observation;
	}

	/**
	 * Create a sequence of observations based upon bin size. The observations
	 * represent mean magnitudes at the mid-point of each bin. Each bin consists
	 * of the range index..index+binSize-1
	 * 
	 * @param observations
	 *            The observations to which binning will be applied.
	 * @param daysInBin
	 *            The bin size in number of Julian Days or portions thereof.
	 * @return An observation sequence consisting of magnitude means per bin and
	 *         the Julian Day at the center point of each bin.
	 */
	public static List<ValidObservation> createdBinnedObservations(
			List<ValidObservation> observations, double daysInBin) {

		List<ValidObservation> binnedObs = new ArrayList<ValidObservation>();

		double minJD = observations.get(0).getJD();
		int minJDIndex = 0;
		int maxJDIndex = 0;

		int i = 1;

		while (i < observations.size()) {

			// If we have not reached the end of the observation list
			// and the current observation's Julian Day is less than the
			// minimum Julian Day for the bottom of the current range plus
			// the number of days in the bin, continue to the next observation.
			if (i < observations.size()
					&& observations.get(i).getJD() < (minJD + daysInBin)) {
				i++;
			} else {
				// Otherwise, we have found the top of the current range,
				// so add a ValidObservation containing mean and error value
				// to the list.
				maxJDIndex = i - 1;

				ValidObservation ob = createMeanObservationForJDRange(observations,
						minJDIndex, maxJDIndex);
				
				// If the mean magnitude value is NaN (e.g. because
				// there was no valid data in the JD range in question), 
				// it doesn't make sense to include this observation.
				if (!Double.isNaN(ob.getMag())) {
					binnedObs.add(ob);
				}
				
				minJDIndex = i;
				minJD = observations.get(i).getJD();

				i++;
			}
		}

		// Ensure that if we have reached the end of the observations
		// that we include any left over data that would otherwise be
		// excluded by the JD less-than constraint?
		if (maxJDIndex < observations.size() - 1) {
			ValidObservation ob = createMeanObservationForJDRange(observations,
					minJDIndex, observations.size() - 1);
			
			// If the mean magnitude value is NaN (e.g. because
			// there was no valid data in the JD range in question), 
			// it doesn't make sense to include this observation.
			if (!Double.isNaN(ob.getMag())) {
				binnedObs.add(ob);
			}
		}

		return binnedObs;
	}
}
