/**
 * VStar: a statistical analysis tool for variable star data.
 * Copyright (C) 2020  AAVSO (http://www.aavso.org/)
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

public class Tolerance {
	
	/**
	 * Determine if two values are equivalent within a tolerance range
     *
	 * @param a
	 * 			The first value to compare
	 * @param b
	 * 			The second value to compare
	 * @param epsilon
	 * 			The tolerance range
	 * @param absolute
	 * 			Whether or not to use a hard range for absolute mode or 
	 * 			a range relative to both values individually
	 */
	public static boolean areClose(double a, double b, double epsilon, boolean absolute) {
		double diff = Math.abs(a - b);
		if(absolute){
			return diff < epsilon;
		}
		
		else {			  
			if( (diff == 0.0) || 
			    ((diff <= Math.abs(epsilon * a)) && 
			    (diff <= Math.abs(epsilon * b))) ){
				return true;
			  }
			else return false;
		}
	}

}
