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
package org.aavso.tools.vstar.data.filter;

/**
 * Observation matcher operator type.
 */
public enum ObservationMatcherOp {

	EQUALS,
	CONTAINS,
	LESS_THAN,
	GREATER_THAN,
	LESS_THAN_OR_EQUAL,
	GREATER_THAN_OR_EQUAL;
	
	/**
	 * Given a string, return an operator enum value, or null.
	 * Legal strings are those returned by toString().
	 * 
	 * @param str The operator as a string.
	 * @return The operator as an enum value.
	 */
	public static ObservationMatcherOp fromString(String str) {
		ObservationMatcherOp op = null;
		
		if (EQUALS.toString().equals(str)) {
			op = EQUALS;
		} else if (CONTAINS.toString().equals(str)) {
			op = CONTAINS;
		} else if (LESS_THAN.toString().equals(str)) {
			op = LESS_THAN;
		} else if (GREATER_THAN.toString().equals(str)) {
			op = GREATER_THAN;
		} else if (LESS_THAN_OR_EQUAL.toString().equals(str)) {
			op = LESS_THAN_OR_EQUAL;
		} else if (GREATER_THAN_OR_EQUAL.toString().equals(str)) {
			op = GREATER_THAN_OR_EQUAL;
		}
		
		return op;
	}
	
	/**
	 * Return a human-readable string for this operator.
	 */
	public String toString() {
		String s = null;
		
		switch(this) {
		case EQUALS:
			s = "equals";
			break;
		case CONTAINS:
			s = "contains";
			break;
		case LESS_THAN:
			s = "less than";
			break;
		case GREATER_THAN:
			s = "greater than";
			break;
		case LESS_THAN_OR_EQUAL:
			s = "less than or equal to";
			break;
		case GREATER_THAN_OR_EQUAL:
			s = "greater than or equal to";
			break;
		}
		
		return s;
	}
}
