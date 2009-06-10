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
package org.aavso.tools.vstar.data.validation;

/**
 * This abstract base class represents a predicate involving a numeric range.
 */
public abstract class RangePredicate {

	protected final double lower;
	protected final double upper;
	
	/**
	 * Constructor.
	 * 
	 * @param lower The lower bound of the range.
	 * @param upper The upper bound of the range.
	 */
	public RangePredicate(double lower, double upper) {
		this.lower = lower;
		this.upper = upper;
	}
	
	/**
	 * Does the predicate hold true?
	 * 
	 * @param value The value to which to apply the predicate.
	 * @return True or False.
	 */
	public abstract boolean holds(double value);
}
