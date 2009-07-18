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
package org.aavso.tools.vstar.util;

import java.util.ArrayList;
import java.util.List;

import org.aavso.tools.vstar.data.DateInfo;
import org.aavso.tools.vstar.data.Magnitude;
import org.aavso.tools.vstar.data.ValidObservation;

/**
 * Descriptive statistics functions for magnitude and Julian Day sources.
 */
public class DescStats {

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

		assert (maxJDIndex >= minJDIndex);

		double total = 0;

		for (int i = minJDIndex; i <= maxJDIndex; i++) {
			total += source.get(i).getMag();
		}

		return total / (maxJDIndex - minJDIndex + 1);
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

		assert (maxJDIndex >= minJDIndex);

		double magMean = calcMagMeanInJDRange(source, minJDIndex, maxJDIndex);

		double total = 0;

		for (int i = minJDIndex; i <= maxJDIndex; i++) {
			double delta = source.get(i).getMag() - magMean;
			total += delta * delta;
		}

		// Standard sample variance.
		double variance = total / (maxJDIndex - minJDIndex);

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

		assert (maxJDIndex >= minJDIndex);

		double magMean = calcMagMeanInJDRange(source, minJDIndex, maxJDIndex);

		double total = 0;

		for (int i = minJDIndex; i <= maxJDIndex; i++) {
			double delta = source.get(i).getMag() - magMean;
			total += delta * delta;
		}

		// Standard sample variance.
		double variance = total / (maxJDIndex - minJDIndex);
		double magStdDev = Math.sqrt(variance);
		double magStdErrOfMean = magStdDev
				/ Math.sqrt(maxJDIndex - minJDIndex + 1);

		// Mean Julian Day. TODO: should we instead use median?
		double meanJD = (source.get(minJDIndex).getJD() + source
				.get(maxJDIndex).getJD()) / 2;

		ValidObservation observation = new ValidObservation();
		observation.setMagnitude(new Magnitude(magMean, magStdErrOfMean));
		observation.setDateInfo(new DateInfo(meanJD));

		return observation;
	}

	/**
	 * Create a sequence of observations based upon bin size.
	 * The observations represent mean magnitudes at the mid-point of each bin.
	 * Each bin consists of the range index..index+binSize-1
	 * 
	 * @param observations
	 *            The observations to which binning will be applied.
	 * @param binSize
	 *            The bin size in whole number of Julian Days.
	 * @return The observation sequence consisting of magnitude means per bin
	 *         and the Julian Day at the center point of each bin.
	 */
	public static List<ValidObservation> createdBinnedObservations(
			List<ValidObservation> observations, int binSize) {
		List<ValidObservation> binnedObs = new ArrayList<ValidObservation>();

		for (int i = 0; i < observations.size(); i += binSize) {
			binnedObs.add(createMeanObservationForJDRange(observations, i, i
					+ binSize - 1));
		}

		return binnedObs;
	}
}
