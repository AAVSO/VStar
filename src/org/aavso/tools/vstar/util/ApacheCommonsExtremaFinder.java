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

import org.aavso.tools.vstar.exception.AlgorithmError;
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
 * TODO: consider analytic approach
 */
public class ApacheCommonsExtremaFinder implements IExtremaFinder {

	private UnivariateRealFunction function;
	private GoalType goal;
	private double min;
	private double max;
	private double zeroPoint;
	
	private Double extremeMag;
	private Double extremeTime;

	private boolean interrupt;
	
	/**
	 * Constructor
	 * 
	 * @param function
	 *            An Apache Commons Math Univariate
	 * @param goal
	 *            Minimise or maximise?
	 * @param min
	 *            Initial bounding minimum time.
	 * @param max
	 *            Initial bounding maximum time.
	 * @param zeroPoint
	 *            The zeroPoint to be added to the extreme time result.
	 */
	public ApacheCommonsExtremaFinder(UnivariateRealFunction function, GoalType goal,
			double min, double max, double zeroPoint) {

		this.function = function;
		this.goal = goal;
		this.min = min;
		this.max = max;
		this.zeroPoint = zeroPoint;
		
		extremeMag = null;
		extremeTime = null;
		interrupt = false;
	}
	
	/**
	 * Construct an extrema finder with a value of 0 for the zero-point.
	 * 
	 * @param function
	 *            An Apache Commons Math Univariate
	 * @param goal
	 *            Minimise or maximise?
	 * @param min
	 *            Initial bounding minimum time.
	 * @param max
	 *            Initial bounding maximum time.
	 */
	public ApacheCommonsExtremaFinder(UnivariateRealFunction function, GoalType goal,
			double min, double max) {
		this(function, goal, min, max, 0);
	}
	
	@Override
	public Double getExtremeMag() {
		return extremeMag;
	}

	@Override
	public Double getExtremeTime() {
		return extremeTime;
	}

	@Override
	public void execute() throws AlgorithmError {
		
		double growthLimit = 0.1;
		int maxIterations = 100;
		int retries = 5;

		for (int i = 1; i <= retries && !interrupt; i++) {
			try {
				BracketFinder bracketFinder = new BracketFinder(growthLimit,
						maxIterations);

				bracketFinder.search(function, goal, min, max);

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

	@Override
	public void interrupt() {
		interrupt = true;
	}
}
