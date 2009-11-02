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
package org.aavso.tools.vstar.ui.model;

import java.util.List;
import java.util.Map;

import org.aavso.tools.vstar.data.ValidObservation;

/**
 * This interface must be implemented by all coordinate sources 
 * for items within a plot series. This interface is motivated out
 * of a pragmatic need to decouple plot models from coordinate
 * information, allowing them to vary independently and for the
 * latter to be aggregated by the former, and at the end of the
 * day to avoid code duplication or promote code reuse depending
 * upon whether your class is half empty of half full.
 */
public interface ICoordSource {

	/**
	 * Get the number of items associated with this specified series.
	 * 
	 * Note that it may seem "obvious" that this value should be
	 * seriesNumToObSrcListMap.size(). Don't make that assumption!
	 * 
	 * @param series The series of interest.
	 * @return The number of items in this series.
	 */
	public int getItemCount(int series, Map<Integer, List<ValidObservation>> seriesNumToObSrcListMap);
	
	/**
	 * Get the X coordinate value associated with the specified series and item.
	 * 
	 * @param series The series of interest. 
	 * @param item The target item.
	 * @param seriesNumToObSrcListMap A mapping from series number to a list of observations.
	 * @return The X coordinate.
	 */
	public double getXCoord(int series, int item, Map<Integer, List<ValidObservation>> seriesNumToObSrcListMap);

	/**
	 * Get the actual item number for the Y coordinate value associated with
	 * the specified series and item.
	 * 
	 * Note that it may seem "obvious" that this value should be
	 * item. Don't make that assumption!
	 * 
	 * @param series The series of interest. 
	 * @param item The target item.
	 * @param seriesNumToObSrcListMap A mapping from series number to a list of observations.
	 * @return The actual Y item number.
	 */
	public int getActualYItemNum(int series, int item, Map<Integer, List<ValidObservation>> seriesNumToObSrcListMap);
}
