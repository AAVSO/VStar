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

import org.aavso.tools.vstar.data.Property;

/**
 * VeLa: VStar expression Language
 * 
 * An enumeration of operand types.
 */
public enum Type {

    INTEGER, REAL, BOOLEAN, STRING, LIST, FUNCTION, OBJECT, NONE;

    public final static int[] INT_ARR = new int[0];
    public final static long[] LONG_ARR = new long[0];
    public final static float[] FLOAT_ARR = new float[0];
    public final static double[] DBL_ARR = new double[0];
    public final static Double[] DBL_CLASS_ARR = new Double[0];
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
        } else if (jtype == DBL_CLASS_ARR.getClass()) {
            vtype = LIST;
        } else if (jtype == BOOL_ARR.getClass()) {
            vtype = LIST;
        } else if (jtype == STR_ARR.getClass()) {
            vtype = LIST;
        } else if (jtype == void.class) {
            vtype = NONE;
        } else {
            // It's a class representing instances of *some* kind of object!
            // TODO: Needs thought, especially for 1st class objects in VeLa
            vtype = OBJECT;
//		} else {
//			throw new IllegalArgumentException("Invalid type: " + jtype);
        }

        return vtype;
    }

    public static Class<?> vela2Java(Type vtype) {
        Class<?> jtype = null;

        switch (vtype) {
        case INTEGER:
            jtype = int.class;
            break;
        case REAL:
            jtype = double.class;
            break;
        case STRING:
            jtype = String.class;
            break;
        case BOOLEAN:
            jtype = boolean.class;
            break;
        case LIST:
            // TODO
            break;
        case FUNCTION:
            // TODO
            break;
        case NONE:
            jtype = void.class;
            break;
        case OBJECT:
            jtype = Object.class;
            break;
        }

        return jtype;
    }

    public static Type name2Vela(String type) {
        Type vtype = null;

        switch(type.toLowerCase()) {
        case "int":
        case "integer":
        case "ℤ":
            vtype = INTEGER;
            break;

        case "real":
        case "ℝ":
            vtype = REAL;
            break;

        case "bool":
        case "boolean":
        case "𝔹":
            vtype = BOOLEAN;
            break;

        case "str":
        case "string":
            vtype = STRING;
            break;

        case "list":
            vtype = LIST;
            break;

        case "fun":
        case "function":
        case "λ":
        case "Λ":
            vtype = FUNCTION;
            break;
        }

        return vtype;
    }

    public static Type propertyToVela(Property prop) {
        Type vtype = null;

        switch (prop.getType()) {
        case INTEGER:
            vtype = Type.INTEGER;
            break;
        case REAL:
            vtype = Type.REAL;
            break;
        case BOOLEAN:
            vtype = Type.BOOLEAN;
            break;
        case STRING:
            vtype = Type.STRING;
            break;
        case NONE:
        default:
            vtype = Type.NONE;
            break;
        }

        return vtype;
    }

    public boolean isComposite() {
        return this == LIST || this == FUNCTION || this == OBJECT;
    }

    public boolean oneOf(Type... types) {
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
