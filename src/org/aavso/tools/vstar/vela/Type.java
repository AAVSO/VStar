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
package org.aavso.tools.vstar.vela;

import java.util.List;

/**
 * VeLa: VStar expression Language
 * 
 * An enumeration of operand types.
 */
public enum Type {

	INTEGER, REAL, STRING, BOOLEAN, LIST, FUNCTION, OBJECT, NONE;

	public final static int[] INT_ARR = new int[0];
	public final static double[] DBL_ARR = new double[0];
	public final static boolean[] BOOL_ARR = new boolean[0];
	public final static String[] STR_ARR = new String[0];

	public static Type java2Vela(Class<?> jtype) {
		Type vtype = null;
				
		if (jtype == int.class) {
			vtype = INTEGER;
		} else if (jtype == double.class) {
			vtype = REAL;
		} else if (jtype == String.class) {
			vtype = STRING;
		} else if (jtype == CharSequence.class) {
			vtype = STRING;
		} else if (jtype == boolean.class) {
			vtype = BOOLEAN;
		} else if (jtype == List.class) {
			vtype = LIST;
		} else if (jtype == INT_ARR.getClass()) {
			vtype = LIST;
		} else if (jtype == DBL_ARR.getClass()) {
			vtype = LIST;
		} else if (jtype == BOOL_ARR.getClass()) {
			vtype = LIST;
		} else if (jtype == STR_ARR.getClass()) {
			vtype = LIST;
		} else if (jtype == void.class) {
			vtype = NONE;
		} else {
			// It's a class representing instances of *some* kind of object!
			vtype = OBJECT;
//		} else {
//			throw new IllegalArgumentException("Invalid type: " + jtype);
		}

		return vtype;
	}
	
	public static Type name2Vela(String type) {
		Type vtype = null;

		if ("integer".equalsIgnoreCase(type)) {
			vtype = INTEGER;
		} else if ("real".equalsIgnoreCase(type)) {
			vtype = REAL;
		} else if ("string".equalsIgnoreCase(type)) {
			vtype = STRING;
		} else if ("boolean".equalsIgnoreCase(type)) {
			vtype = BOOLEAN;
		} else if ("list".equalsIgnoreCase(type)) {
			vtype = LIST;
		} else if ("function".equalsIgnoreCase(type)) {
			vtype = FUNCTION;
		} else {
			throw new IllegalArgumentException("Invalid type: " + type);
		}

		return vtype;
	}
	
	public boolean isComposite() {
		return this == LIST || this == FUNCTION;
	}
	
	public boolean oneOf(Type ...types) {
		boolean result = false;
		
		for (Type type : types) {
			if (this == type) {
				result = true;
				break;
			}
		}
		
		return result;
	}
}
