/**
 * VStar: a statistical analysis tool for variable star data.
 * Copyright (C) 2014  AAVSO (http://www.aavso.org/)
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

import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.exception.AlgorithmError;
import org.aavso.tools.vstar.ui.model.plot.ICoordSource;
import org.aavso.tools.vstar.util.locale.LocaleProps;
import org.aavso.tools.vstar.util.prefs.NumericPrecisionPrefs;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MaxIterationsExceededException;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.univariate.BracketFinder;
import org.apache.commons.math.optimization.univariate.BrentOptimizer;

/**
 * This class permits minima/maxima of Apache Commons Math continuous functions
 * to be found.
 * 
 * TODO:<br/>
 * - consider analytic approach<br/>
 * - some of the members of this class could form the basis of a base class<br/>
 */
public class ApacheCommonsBrentOptimiserExtremaFinder implements IInteruptible {

	private List<ValidObservation> obs;
	private ICoordSource timeCoordSource;
	private double zeroPoint;
	private UnivariateRealFunction function;

	private int numericallyMinMagIndex;
	private int numericallyMaxMagIndex;

	private Double extremeMag;
	private Double extremeTime;

	private boolean interrupt;

	/**
	 * Constructor
	 * 
	 * @param obs
	 *            The list of observations modeled by the function.
	 * @param function
	 *            An Apache Commons Math Univariate
	 * @param timeCoordSource
	 *            Time coordinate source.
	 * @param zeroPoint
	 *            The zeroPoint to be added to the extreme time result.
	 */
	public ApacheCommonsBrentOptimiserExtremaFinder(List<ValidObservation> obs,
			UnivariateRealFunction function, ICoordSource timeCoordSource,
			double zeroPoint) {

		this.obs = obs;
		this.function = function;
		this.timeCoordSource = timeCoordSource;
		this.zeroPoint = zeroPoint;
		
		this.numericallyMinMagIndex = getNumericallyMinimumMagnitudeIndex();
		this.numericallyMaxMagIndex = getNumericallyMaximumMagnitudeIndex();

		extremeMag = null;
		extremeTime = null;

		interrupt = false;
	}

	@Override
	public void interrupt() {
		interrupt = true;
	}

	protected Double getExtremeMag() {
		return extremeMag;
	}

	protected Double getExtremeTime() {
		return extremeTime;
	}

	/**
	 * Find the first numerically smaller values either side of the numerically
	 * largest (so, astronomical magnitude minimum) value.
	 * 
	 * @return an inclusive range that brackets the astronomical minimum
	 */
	protected int[] determineInitialSearchRangeForMinimum() {

		int[] range = new int[2];

		// To the left...
		range[0] = numericallyMaxMagIndex;
		for (int i = numericallyMaxMagIndex - 1; i >= 0; i--) {
			if (obs.get(i).getMag() < obs.get(numericallyMaxMagIndex).getMag()) {
				range[0] = i;
				break;
			}
		}

		// To the right...
		range[1] = numericallyMaxMagIndex;
		for (int i = numericallyMaxMagIndex + 1; i < obs.size(); i++) {
			if (obs.get(i).getMag() < obs.get(numericallyMaxMagIndex).getMag()) {
				range[1] = i;
				break;
			}
		}

		return range;
	}

	/**
	 * Find the first numerically larger values either side of the numerically
	 * smallest (so, astronomical magnitude maximum) value.
	 * 
	 * @return an inclusive range that brackets the astronomical maximum
	 */
	protected int[] determineInitialSearchRangeForMaximum() {
		int[] range = new int[2];

		// To the left...
		range[0] = numericallyMaxMagIndex;
		for (int i = numericallyMaxMagIndex - 1; i >= 0; i--) {
			if (obs.get(i).getMag() > obs.get(numericallyMaxMagIndex).getMag()) {
				range[0] = i;
				break;
			}
		}

		// To the right...
		range[1] = numericallyMaxMagIndex;
		for (int i = numericallyMaxMagIndex + 1; i < obs.size(); i++) {
			if (obs.get(i).getMag() > obs.get(numericallyMaxMagIndex).getMag()) {
				range[1] = i;
				break;
			}
		}

		return range;
	}

	// Return the index of the magnitude that is numerically the smallest.
	protected int getNumericallyMinimumMagnitudeIndex() {
		int minIndex = 0;

		for (int i = 0; i < obs.size(); i++) {
			if (obs.get(i).getMag() < obs.get(minIndex).getMag()) {
				minIndex = i;
			}
		}

		return minIndex;
	}

	// Return the index of the magnitude that is numerically the largest.
	protected int getNumericallyMaximumMagnitudeIndex() {
		int maxIndex = 0;

		for (int i = 0; i < obs.size(); i++) {
			if (obs.get(i).getMag() > obs.get(maxIndex).getMag()) {
				maxIndex = i;
			}
		}

		return maxIndex;
	}

	/**
	 * Return a string representation of the minimum magnitude (numerical
	 * maximum) generated by the function.
	 * 
	 * @return The minimum string.
	 * @throws AlgorithmError
	 *             if there is an error in minima determination.
	 */
	public String toMinimaString() throws AlgorithmError {
		int[] range = determineInitialSearchRangeForMinimum();

		return toExtremumString("MODEL_INFO_MINIMA_TITLE", GoalType.MAXIMIZE,
				function, range);
	}

	/**
	 * Return a string representation of the maximum magnitude (numerical
	 * minimum) generated by the function.
	 * 
	 * @return The maximum string.
	 * @throws AlgorithmError
	 *             if there is an error in maxima determination.
	 */
	public String toMaximaString() throws AlgorithmError {
		int[] range = determineInitialSearchRangeForMaximum();

		return toExtremumString("MODEL_INFO_MAXIMA_TITLE", GoalType.MINIMIZE,
				function, range);
	}

	/**
	 * Return a string representation of the extreme magnitude (numerical
	 * opposite of magnitude) generated by the function.
	 * 
	 * @param goal
	 *            The goal (numericallyMinMagIndex, numericallyMaxMagIndex).
	 * @param function
	 *            The function for which the extremum is required.
	 * @param bracketRange
	 *            The inclusive range within which to look for the extremum.
	 * @return The extremum string.
	 * @throws AlgorithmError
	 *             if there is an error in extrema determination.
	 */
	protected String toExtremumString(String titleKey, GoalType goal,
			UnivariateRealFunction function, int[] bracketRange)
			throws AlgorithmError {

		find(goal, bracketRange);

		double extremeMag = getExtremeMag();

		String strRepr = String.format("%s: %s, Mag: %s [%s]",
				timeCoordSource.getUnit(),
				NumericPrecisionPrefs.formatTime(getExtremeTime()),
				NumericPrecisionPrefs.formatMag(extremeMag),
				LocaleProps.get(titleKey));

		// Is the extremum within a reasonable range? If not,
		// set it to null.
		if (strRepr != null) {
			if (goal == GoalType.MAXIMIZE) {
				if (extremeMag > numericallyMaxMagIndex) {
					strRepr = null;
				}
			} else if (goal == GoalType.MINIMIZE) {
				if (extremeMag < numericallyMinMagIndex) {
					strRepr = null;
				}
			}
		}

		return strRepr;
	}

	@Override
	public String toString() {
		String str = "";

		try {
			String maxStr = toMaximaString();
			String minStr = toMinimaString();

			if (minStr != null) {
				str += minStr;
				str += "\n";
			}

			if (maxStr != null) {
				str += maxStr;
			}
		} catch (AlgorithmError e) {
		}

		return "".equals(str) ? null : str;
	}

	// Helpers

	/**
	 * Find the extremum according to the goal.
	 * 
	 * @param goal
	 *            Minimum or maximum?
	 * @param bracketRange
	 *            The inclusive range within which to look for the extremum.
	 */
	private void find(GoalType goal, int[] bracketRange) throws AlgorithmError {

		double growthLimit = 0.1;
		int maxIterations = 100;
		int retries = 5;

		for (int i = 1; i <= retries && !interrupt; i++) {
			try {
				BracketFinder bracketFinder = new BracketFinder(growthLimit,
						maxIterations);

				bracketFinder.search(function, goal, obs.get(bracketRange[0])
						.getJD(), obs.get(bracketRange[1]).getJD());

				BrentOptimizer extremaFinder = new BrentOptimizer();

				extremeTime = extremaFinder.optimize(function, goal,
						bracketFinder.getLo(), bracketFinder.getHi(),
						bracketFinder.getMid())
						+ zeroPoint; // TODO: or - zeroPoint?

				extremeMag = extremaFinder.getFunctionValue();

			} catch (FunctionEvaluationException e) {
				maxIterations *= 10;
			} catch (MaxIterationsExceededException e) {
				maxIterations *= 10;
			}
		}
	}
}
