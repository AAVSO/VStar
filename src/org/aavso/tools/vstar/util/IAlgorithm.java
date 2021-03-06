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

import org.aavso.tools.vstar.exception.AlgorithmError;

/**
 * Classes implementing an algorithm to be executed must realise this interface.
 */
public interface IAlgorithm extends IInteruptible {

	/**
	 * Executes the algorithm.
	 * 
	 * @throws AlgorithmError
	 *             in case of an algorithm error.
	 */
	abstract public void execute() throws AlgorithmError;
}
