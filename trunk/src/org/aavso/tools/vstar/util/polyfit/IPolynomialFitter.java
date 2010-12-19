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
package org.aavso.tools.vstar.util.polyfit;

import java.util.List;

import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.util.IAlgorithm;

/**
 * All polynomial fit implementations must realise this interface.
 */
public interface IPolynomialFitter extends IAlgorithm {

	/**
	 * Returns the minimum degree supported by a concrete polynomial fitter.
	 * 
	 * @return The minimum degree.
	 */
	 abstract int getMinDegree();

	/**
	 * Returns the maximum degree supported by a concrete polynomial fitter.
	 * 
	 * @return The maximum degree.
	 */
	abstract int getMaxDegree();

	/**
	 * Set the degree of the polynomial for the fitting operation.
	 * 
	 * @param degree
	 *            The degree of the polynomial.
	 */
	abstract void setDegree(int degree);

	/**
	 * Return the fitted observations, after having executed the algorithm.
	 * 
	 * @return A list of observations that represent the fit.
	 */
	abstract public List<ValidObservation> getFit();

	/**
	 * Return the residuals as observations, after having executed the
	 * algorithm.
	 * 
	 * @return A list of observations that represent the residuals.
	 */
	abstract public List<ValidObservation> getResiduals();
}
