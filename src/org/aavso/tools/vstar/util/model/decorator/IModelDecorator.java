/**
 * VStar: a statistical analysis tool for variable star data.
 * Copyright (C) 2015  AAVSO (http://www.aavso.org/)
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
package org.aavso.tools.vstar.util.model.decorator;

import org.aavso.tools.vstar.ui.model.plot.ContinuousModelFunction;

/**
 * The interface for all model decorators. A model decorator takes a function
 * and generates a string that can be used to add to a model description, e.g.
 * as it appears in multiple tabs in the Model information dialog for each
 * model.
 */
public interface IModelDecorator {

	/**
	 * What is the name of the decoration?
	 * 
	 * @return the decoration name, e.g. "R function" or "Minimum", preferably
	 *         localised.
	 */
	public String getDecorationName();

	/**
	 * Given a model function object, return a decoration string.
	 * 
	 * @param function
	 *            The model function object.
	 * @return the decoration string.
	 */
	public String getDecoration(ContinuousModelFunction function);
}
