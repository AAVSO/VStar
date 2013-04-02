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
package org.aavso.tools.vstar.util.model;

import java.util.List;
import java.util.Map;

import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.util.IAlgorithm;

/**
 * All model classes must implement this interface, e.g. polynomial fits,
 * Fourier coefficient based models.
 */
public interface IModel extends IAlgorithm {

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

	/**
	 * Return the list of coefficients that gives rise to the model.
	 * May return null.
	 * 
	 * @return A list of fit coefficients or null if none available.
	 */
	abstract public List<PeriodFitParameters> getParameters();

	/**
	 * Does this model have a function-based description?
	 * 
	 * @return True or false.
	 */
	abstract public boolean hasFuncDesc();
	
	/**
	 * Return a human-readable description of this model.
	 * 
	 * @return The model description.
	 */
	abstract public String getDescription();

	/**
	 * Return a human-readable 'kind' string (e.g. "Model", "Polynomial Fit")
	 * indicating what kind of a model this is.
	 * 
	 * @return The 'kind' string.
	 */
	abstract public String getKind();
	
	/**
	 * Return a mapping from names to strings representing model functions.
	 * 
	 * @return The model function string map.
	 */
	public Map<String, String> getFunctionStrings();
}
