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
package org.aavso.tools.vstar.ui.model.plot;

import java.util.List;

import org.aavso.tools.vstar.data.ValidObservation;

/**
 * This interface must be implemented by any class wanting to be
 * a source or sink of time element values (Julian Day, phase) from 
 * a list of observations.
 */
public interface ITimeElementEntity {

	/**
	 * Get the time element given an observation and an index.
	 * 
	 * @param obs A list of observations.
	 * @param index An index into the list.
	 * @return A time element from within the observation at the list index. 
	 */
	abstract public double getTimeElement(List<ValidObservation> obs, int index);

	/**
	 * Set the time element on a specified observation.
	 * 
	 * @param ob An observation.
	 * @param value The value to be set.
	 */
	abstract public void setTimeElement(ValidObservation ob, double value);
	
	/**
	 * Get the default time elements in a time bin.
	 * @return The default number of time elements per bin. 
	 */
	abstract public double getDefaultTimeElementsInBin();
	
	/**
	 * Get the default time increments or steps over a time bin range.
	 * @return The default time increment. 
	 */
	abstract public double getDefaultTimeIncrements();
	
	
}
