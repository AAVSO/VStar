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
package org.aavso.tools.vstar.util;

/**
 * Logic functions.
 */
public class Logic {

	/**
	 * Logical implication: p => q
	 * 
	 * p => q is the same as !p or q
	 * 
	 * --+---+-- <br/>
	 * p | q | r <br/>
	 * --+---+-- <br/>
	 * F | F | T <br/>
	 * F | T | T <br/>
	 * T | F | F <br/>
	 * T | T | T <br/>
	 * 
	 * @param p The first input.
	 * @param q The second input.
	 * @return The result of !p or q
	 */
	public static boolean imp(boolean p, boolean q) {
		return !p || q;
	}
}
