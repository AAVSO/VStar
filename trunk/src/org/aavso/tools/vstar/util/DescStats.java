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

import java.util.List;

import org.aavso.tools.vstar.data.validation.InclusiveRangePredicate;

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
	 * @param jdRange
	 *            An inclusive range of Julian Days.
	 * @return The mean of magnitudes in the JD range.
	 */
	public static double calcMagMeanInJDRange(List<? extends IMagAndJDSource> source,
			InclusiveRangePredicate jdRange) {

		int itemsInRange = 0;
		double total = 0;

		for (IMagAndJDSource item : source) {
			if (jdRange.holds(item.getJD())) {
				itemsInRange++;
				total += item.getMag();
			}
		}

		return total / itemsInRange;
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
	 * @param jdRange
	 *            An inclusive range of Julian Days.
	 * @return The sample standard deviation of the magnitudes in the JD range.
	 */
	public static double calcMagSampleStdDevInJDRange(
			List<? extends IMagAndJDSource> source, InclusiveRangePredicate jdRange) {

		double magMean = calcMagMeanInJDRange(source, jdRange);

		int itemsInRange = 0;
		double total = 0;

		for (IMagAndJDSource item : source) {
			if (jdRange.holds(item.getJD())) {
				itemsInRange++;
				double delta = item.getMag() - magMean;
				total += delta * delta;
			}
		}

		double variance = total / (itemsInRange - 1);
		
		return Math.sqrt(variance);
	}

	/**
	 * Calculates the standard error/deviation or the average of a sample of
	 * magnitudes for Julian Days in a specified inclusive range. We use the
	 * sample standard deviation formula as per
	 * http://www.aavso.org/education/vsa/Chapter10.pdf. See also a discussion
	 * of this here: http://en.wikipedia.org/wiki/Standard_deviation
	 * 
	 * @param source
	 *            A source of (magnitude, Julian Day) pairs.
	 * @param jdRange
	 *            An inclusive range of Julian Days.
	 * @return The standard error/deviation or the average of the magnitudes in
	 *         the JD range.
	 */
	public static double calcMagStdErrorOfAverageInJDRange(
			List<? extends IMagAndJDSource> source, InclusiveRangePredicate jdRange) {

		double magMean = calcMagMeanInJDRange(source, jdRange);

		int itemsInRange = 0;
		double total = 0;

		for (IMagAndJDSource item : source) {
			if (jdRange.holds(item.getJD())) {
				itemsInRange++;
				double delta = item.getMag() - magMean;
				total += delta * delta;
			}
		}

		double variance = total / (itemsInRange - 1);

		double magStdDev = Math.sqrt(variance);
		
		return magStdDev / Math.sqrt(itemsInRange);
	}
}
