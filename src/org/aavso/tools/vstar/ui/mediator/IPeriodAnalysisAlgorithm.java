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
package org.aavso.tools.vstar.ui.mediator;

import java.util.List;
import java.util.Map;

import org.aavso.tools.vstar.util.period.PeriodAnalysisCoordinateType;

/**
 * Classes implementing a period analysis algorithm to be executed must
 * realise this interface.
 */
public interface IPeriodAnalysisAlgorithm extends IAlgorithm {

	/**
	 * Return the result of the 
	 * @return
	 */
	abstract public Map<PeriodAnalysisCoordinateType, List<Double>> getResultSeries();
	
	/**
	 * From the resulting data, create an array of power-index pairs
	 * (first and second elements respectively) sorted by power.
	 * 
	 * It is a precondition that results have been generated, i.e. the
	 * execute() method has been invoked.
	 * TODO: the name and return type of this method may not be general enough
	 * and indeed, the double[][] should be a class since the 2nd dimension's
	 * 2nd elements are actually integer indices.
	 */
	abstract public double[][] getTopNRankedIndices(int topN);
}
