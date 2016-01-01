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
public class ApacheCommonsBrentOptimiserExtremaFinder extends AbstractExtremaFinder {

	/**
	 * Constructor
	 * 
	 * @param obs
	 *            The list of observations modeled by the function.
	 * @param function
	 *            An Apache Commons Math Univariate function.
	 * @param timeCoordSource
	 *            Time coordinate source.
	 * @param zeroPoint
	 *            The zeroPoint to be added to the extreme time result.
	 */
	public ApacheCommonsBrentOptimiserExtremaFinder(List<ValidObservation> obs,
			UnivariateRealFunction function, ICoordSource timeCoordSource,
			double zeroPoint) {
		super(obs, function, timeCoordSource, zeroPoint);
	}

	@Override
	public void find(GoalType goal, int[] bracketRange) throws AlgorithmError {
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
