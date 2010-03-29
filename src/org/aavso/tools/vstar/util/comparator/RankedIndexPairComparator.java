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
package org.aavso.tools.vstar.util.comparator;

import java.util.Comparator;

/**
 * This comparator compares two 2-dimensional arrays of double
 * values where the first element is a ranking value of some kind
 * and the second element is assumed to be an index into another
 * collection. Comparison applies to the first element (power). 
 * What we want here is for the largest values to come first, e.g. 
 * {{1,0}, {3,1}, {2,2}} would be sorted as {{3,1}, {2,2}, {1,0}}.
 */
public class RankedIndexPairComparator implements Comparator<double[]> {

	public final static RankedIndexPairComparator instance = new RankedIndexPairComparator();
	
	public int compare(double[] o1, double[] o2) {
		// Negate the result to reverse the ordinary
		// sense of the comparison.
		return -Double.compare(o1[0], o2[0]);
	}
}
