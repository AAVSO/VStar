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

	private UnivariateRealFunction secondDerivative;

	/**
	 * Constructor
	 * 
	 * @param obs
	 *            The list of observations modeled by the function.
	 * @param function
	 *            An Apache Commons Math univariate function corresponding to
	 *            the first derivative of some function for which the extrema
	 *            are required.
	 * @param secondDerivative
	 *            An Apache Commons Math univariate function corresponding to
	 *            the second derivative of some function for which the extrema
	 *            are required. This function allows us to determine whether the
	 *            extremum is a minimum or maximum.
	 * @param timeCoordSource
	 *            Time coordinate source.
	 * @param zeroPoint
	 *            The zeroPoint to be added to the extreme time result.
	 */
	public ApacheCommonsDerivativeBasedExtremaFinder(
			List<ValidObservation> obs, UnivariateRealFunction function,
			UnivariateRealFunction secondDerivative,
			ICoordSource timeCoordSource, double zeroPoint) {
		super(obs, function, timeCoordSource, zeroPoint);
		this.secondDerivative = secondDerivative;
	}

	@Override
	public void find(GoalType goal, int[] bracketRange) throws AlgorithmError {
		// TODO
		// - Iterate over whole series (all obs in series), looking for where
		// the function (1st derivative) goes to zero or in particular, where it
		// goes from -ve to +ve (minimum) or +ve to -ve (maximum).
		// - Find the obs immediately before and after the transition and use
		// this as the bracket range.
		// - Choose a suitable resolution (e.g. 1 sec, 1 min) settable in prefs
		// to iterate over the 1st derivative to find where it goes to zero or
		// as close to zero as possible. Time values must include zeroPoint
		// addition.
		// - Once the extremum point is found, ask whether the 2nd derivative at
		// that point is -ve (maximum) or +ve (minimum). This may be a redundant
		// step since we are looking for the transition above.

	}
}
