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

		try {
			int maxDerivIndex = 0;
			double maxDeriv = Double.NEGATIVE_INFINITY;

			// Find the obs immediately before and after the transition and use
			// this as the bracket range.
			for (int i = 0; i < obs.size(); i++) {
				jd = obs.get(i).getJD();

				double deriv = firstDerivative.value(jd - zeroPoint);

				if (deriv <= 0) {
					double mag = function.value(jd - zeroPoint);
					double deriv2 = secondDerivative.value(jd - zeroPoint);

					// Note: first one that goes negative seems to be close; go
					// back one ob and start searching...

					System.out.printf("%d => %f: %f (f': %f, f'': %f)\n", i,
							jd, mag, deriv, deriv2);

					if (deriv > maxDeriv) {
						maxDeriv = deriv;
						maxDerivIndex = i;
					}
				}
			}

			int firstIndex = maxDerivIndex > 0 ? maxDerivIndex - 1
					: maxDerivIndex;
			int lastIndex = maxDerivIndex < obs.size() ? maxDerivIndex + 1
					: maxDerivIndex;

			// TODO: make resolution dependent upon JD range of series under
			// analysis;
			// we are trying to get the first derivative to be as close to zero
			// as possible.
			double firstJD = obs.get(firstIndex).getJD();
			double lastJD = obs.get(lastIndex).getJD();
			double maxDerivJD = firstJD;
			
			for (jd = firstJD; jd <= lastJD; jd += 0.0001) {

				double deriv = firstDerivative.value(jd - zeroPoint);

				// TODO: don't need to check for zero here; remove if.
				if (deriv <= 0) {
					double mag = function.value(jd - zeroPoint);
					double deriv2 = secondDerivative.value(jd - zeroPoint);

					// Note: first one that goes negative seems to be close; go
					// back one ob and start searching...

					System.out.printf("%f: %f (f': %f, f'': %f)\n", jd, mag,
							deriv, deriv2);
					
					if (deriv > maxDeriv) {
						maxDeriv = deriv;
						maxDerivJD = jd;
					}
				}
			}

			extremeTime = maxDerivJD;
			extremeMag = function.value(maxDerivJD - zeroPoint);
			
		} catch (FunctionEvaluationException e) {
			throw new AlgorithmError(String.format(
					"Error obtaining derivative value for JD %f", jd));
		}

		// TODO
		// - Choose a suitable resolution/tolerance (e.g. 1 sec, 1 min) settable in prefs
		// to iterate over the 1st derivative to find where it goes to zero or
		// as close to zero as possible. Time values must include zeroPoint
		// addition.
		// - Once the extremum point is found, ask whether the 2nd derivative at
		// that point is -ve (maximum) or +ve (minimum). This may be a redundant
		// step since we are looking for the transition above.

	}
}
