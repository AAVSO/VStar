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
package org.aavso.tools.vstar.util;

import java.util.List;

import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.exception.AlgorithmError;
import org.aavso.tools.vstar.ui.model.plot.ICoordSource;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.DifferentiableUnivariateRealFunction;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.optimization.GoalType;

/**
 * An extrema finder class that uses the derivatives of a univariate real
 * function.
 * 
 * See also DifferentiableUnivariateRealFunction
 */
public class ApacheCommonsDerivativeBasedExtremaFinder extends
		AbstractExtremaFinder {

	/**
	 * Constructor
	 * 
	 * @param obs
	 *            The list of observations modeled by the function.
	 * @param function
	 *            An Apache Commons Math univariate function for which extrema
	 *            are required.
	 * @param timeCoordSource
	 *            Time coordinate source.
	 * @param zeroPoint
	 *            The zeroPoint to be added to the extreme time result.
	 */
	public ApacheCommonsDerivativeBasedExtremaFinder(
			List<ValidObservation> obs,
			DifferentiableUnivariateRealFunction function,
			ICoordSource timeCoordSource, double zeroPoint) {
		super(obs, function, timeCoordSource, zeroPoint);
	}

	@Override
	public void find(GoalType goal, int[] bracketRange) throws AlgorithmError {
		// Iterate over whole series (all obs in series), looking for where the
		// function (1st derivative) goes to zero or in particular, where it
		// goes from -ve to +ve (minimum) or +ve to -ve (maximum).
		UnivariateRealFunction firstDerivative = ((DifferentiableUnivariateRealFunction) function)
				.derivative();

		UnivariateRealFunction secondDerivative = ((DifferentiableUnivariateRealFunction) firstDerivative)
				.derivative();

		double jd = 0;

		// One sixth of a second.
		double resolution = 0.00001;

		try {
			int minDerivIndex = 0;
			double minDeriv = Double.POSITIVE_INFINITY;

			// TODO: refactor loops!

			// Find the obs immediately before and after the transition and use
			// this as the bracket range.
			for (int i = 0; i < obs.size(); i++) {
				if (interrupt)
					break;

				jd = obs.get(i).getJD();

				double deriv = Math.abs(firstDerivative.value(jd - zeroPoint));

				// if (deriv <= tolerance) {
				// double mag = function.value(jd - zeroPoint);
				// double deriv2 = secondDerivative.value(jd - zeroPoint);
				// System.out.printf("%d => %f: %f (f': %f, f'': %f)\n", i,
				// jd, mag, deriv, deriv2);

				if (deriv < minDeriv) {
					minDeriv = deriv;
					minDerivIndex = i;
				}
				// }
			}

			int firstIndex = minDerivIndex > 0 ? minDerivIndex - 1
					: minDerivIndex;
			int lastIndex = minDerivIndex < obs.size() - 1 ? minDerivIndex + 1
					: minDerivIndex;

			// We are trying to get the first derivative to be as close to zero
			// as possible. This could be at firstJD but since we may have
			// already gone past that time above, if not already at start or
			// end of observation list.
			double firstJD = obs.get(firstIndex).getJD();
			double lastJD = obs.get(lastIndex).getJD();
			double minDerivJD = firstJD;

			for (jd = firstJD; jd <= lastJD; jd += resolution) {
				if (interrupt)
					break;

				double deriv = Math.abs(firstDerivative.value(jd - zeroPoint));

				// if (deriv <= tolerance) {
				double mag = function.value(jd - zeroPoint);
				// double deriv2 = secondDerivative.value(jd - zeroPoint);
				// System.out.printf("%f: %f (f': %f, f'': %f)\n", jd, mag,
				// deriv,
				// deriv2);

				if (deriv < minDeriv) {
					minDeriv = deriv;
					minDerivJD = jd;
				}
			}
			// }

			if (matchesDesiredGoal(secondDerivative, minDerivJD, goal)) {
				extremeTime = minDerivJD;
				extremeMag = function.value(minDerivJD - zeroPoint);
			} else {
				extremeTime = Double.POSITIVE_INFINITY;
				extremeMag = Double.POSITIVE_INFINITY;
			}
		} catch (FunctionEvaluationException e) {
			throw new AlgorithmError(String.format(
					"Error obtaining derivative value for JD %f", jd));
		}
	}

	private boolean matchesDesiredGoal(UnivariateRealFunction secondDerivative,
			double jd, GoalType goal) throws FunctionEvaluationException {

		boolean matches = false;
		// The inflection point determines whether the inflection point is a
		// minimum or maximum.
		double deriv2 = secondDerivative.value(jd - zeroPoint);

		if (deriv2 < 0 && goal == GoalType.MAXIMIZE) {
			matches = true;
		} else if (deriv2 > 0 && goal == GoalType.MINIMIZE) {
			matches = true;
		}

		return matches;
	}
}
