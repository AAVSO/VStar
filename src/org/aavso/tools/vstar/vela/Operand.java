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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

import org.aavso.tools.vstar.util.prefs.NumericPrecisionPrefs;

/**
 * VeLa: VStar expression Language
 *
 * A class that represents typed operands.
 * 
 * Note: should cache Operand instances
 */
public class Operand {

    private Type type;
    private long intVal;
    private double doubleVal;
    private String stringVal;
    private boolean booleanVal;
    private List<Operand> listVal;
    private FunctionExecutor functionVal;

    public static Operand EMPTY_LIST = new Operand(Type.LIST, Collections.emptyList());

    public static Operand NO_VALUE = new Operand(Type.NONE, false);

    public Operand(Type type, long value) {
        this.type = type;
        intVal = value;
    }

    public Operand(Type type, double value) {
        this.type = type;
        doubleVal = value;
    }

    public Operand(Type type, String value) {
        this.type = type;
        stringVal = value;
    }

    public Operand(Type type, boolean value) {
        this.type = type;
        booleanVal = value;
    }

    public Operand(Type type, List<Operand> value) {
        this.type = type;
        listVal = value;
    }

    public Operand(Type type, FunctionExecutor value) {
        this.type = type;
        functionVal = value;
    }

    // For object copy
    private Operand() {
    }

    /**
     * @return the type
     */
    public Type getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(Type type) {
        this.type = type;
    }

    /**
     * @param intVal the intVal to set
     */
    public void setIntegerVal(long intVal) {
        this.intVal = intVal;
    }

    /**
     * @param doubleVal the doubleVal to set
     */
    public void setDoubleVal(double doubleVal) {
        this.doubleVal = doubleVal;
    }

    /**
     * @return the intVal
     */
    public long intVal() {
        return intVal;
    }

    /**
     * @return the doubleVal
     */
    public double doubleVal() {
        return doubleVal;
    }

    /**
     * @param stringVal the stringVal to set
     */
    public void setStringVal(String stringVal) {
        this.stringVal = stringVal;
    }

    /**
     * @return the stringVal
     */
    public String stringVal() {
        return stringVal;
    }

    /**
     * @param booleanVal the booleanVal to set
     */
    public void setBooleanVal(boolean booleanVal) {
        this.booleanVal = booleanVal;
    }

    /**
     * @return the booleanVal
     */
    public boolean booleanVal() {
        return booleanVal;
    }

    /**
     * @return the listVal
     */
    public List<Operand> listVal() {
        return listVal;
    }

    /**
     * @param listVal the listVal to set
     */
    public void setListVal(List<Operand> listVal) {
        this.listVal = listVal;
    }

    /**
     * @return the functionVal
     */
    public FunctionExecutor functionVal() {
        return functionVal;
    }

    /**
     * @param functionVal the functionVal to set
     */
    public void setFunctionVal(FunctionExecutor functionVal) {
        this.functionVal = functionVal;
    }

    /**
     * Convert this operand to the required type, if possible, first making a copy
     * if the required type is different from the current type.
     * 
     * @param requiredType The required type.
     * @return The unchanged operand or a new operand that conforms to the required
     *         type.
     */
    public Operand convert(Type requiredType) {
        Operand operand = this;

        if (!type.isComposite()) {
            if (type != requiredType) {
                if (type == Type.INTEGER && requiredType == Type.REAL) {
                    operand = new Operand(Type.REAL, (double) intVal);
                } else if (type != Type.STRING && requiredType == Type.STRING) {
                    operand = operand.convertToString();
                }
            }
        }

        return operand;
    }

    /**
     * Convert this operand's type to string.
     */
    public Operand convertToString() {
        assert type == Type.INTEGER || type == Type.REAL || type == Type.BOOLEAN;

        Operand operand = this;

        switch (type) {
        case INTEGER:
            operand = new Operand(Type.STRING, Long.toString(intVal));
            break;
        case REAL:
            operand = new Operand(Type.STRING, NumericPrecisionPrefs.formatOther(doubleVal));
            break;
        case BOOLEAN:
            operand = new Operand(Type.STRING, Boolean.toString(booleanVal));
            break;
        default:
            break;
        }

        return operand;
    }

    public String toHumanReadableString() {
        String str = "";

        switch (type) {
        case INTEGER:
            str = Long.toString(intVal);
            break;
        case REAL:
            str = NumericPrecisionPrefs.formatOther(doubleVal);
            break;
        case BOOLEAN:
            str = booleanVal ? "True" : "False";
            break;
        case STRING:
            str = stringVal;
            break;
        case LIST:
            str = listVal.toString().replace(",", "");
            break;
        case FUNCTION:
            str = functionVal.toString();
            break;
        default:
            break;
        }

        return str;
    }

    @Override
    public String toString() {
        String str = "";

        switch (type) {
        case INTEGER:
            str = Long.toString(intVal);
            break;
        case REAL:
            str = NumericPrecisionPrefs.formatOther(doubleVal);
            break;
        case BOOLEAN:
            str = booleanVal ? "True" : "False";
            break;
        case STRING:
            str = "\"" + stringVal + "\"";
            break;
        case LIST:
            str = listVal.toString().replace(",", "");
            break;
        case FUNCTION:
            str = functionVal.toString();
            break;
        default:
            break;
        }

        return str;
    }

    /**
     * Given a VeLa type and a Java object, return an Operand instance.
     * 
     * @param type The VeLa type.
     * @param obj  The Java object.
     * @return A corresponding Operand instance.
     */
    public static Operand object2Operand(Type type, Object obj) {
        Operand operand = null;

        try {
            switch (type) {
            case INTEGER:
                if (obj instanceof Integer) {
                    operand = new Operand(Type.INTEGER, (int) obj);
                } else {
                    operand = new Operand(Type.INTEGER, (long) obj);
                }
                break;
            case REAL:
                if (obj instanceof Float) {
                    operand = new Operand(Type.REAL, (float) obj);
                } else {
                    operand = new Operand(Type.REAL, (double) obj);
                }
                break;
            case BOOLEAN:
                operand = new Operand(Type.BOOLEAN, (boolean) obj);
                break;
            case STRING:
                operand = new Operand(Type.STRING, (String) obj);
                break;
            case LIST:
                List<Operand> arr = new ArrayList<Operand>();

                try {
                    if (obj.getClass() == Type.FLOAT_ARR.getClass()) {
                        for (float n : (float[]) obj) {
                            arr.add(new Operand(Type.REAL, n));
                        }
                    } else if (obj.getClass() == Type.DBL_ARR.getClass()) {
                        for (double n : (double[]) obj) {
                            arr.add(new Operand(Type.REAL, n));
                        }
                    } else if (obj.getClass() == Type.DBL_CLASS_ARR.getClass()) {
                        for (Double n : (Double[]) obj) {
                            arr.add(new Operand(Type.REAL, n));
                        }
                    } else if (obj.getClass() == Type.INT_ARR.getClass()) {
                        for (int n : (int[]) obj) {
                            arr.add(new Operand(Type.INTEGER, n));
                        }
                    } else if (obj.getClass() == Type.LONG_ARR.getClass()) {
                        for (long n : (long[]) obj) {
                            arr.add(new Operand(Type.INTEGER, n));
                        }
                    } else if (obj.getClass() == Type.BOOL_ARR.getClass()) {
                        for (boolean b : (boolean[]) obj) {
                            arr.add(new Operand(Type.BOOLEAN, b));
                        }
                    } else if (obj.getClass() == Type.STR_ARR.getClass()) {
                        for (String s : (String[]) obj) {
                            arr.add(new Operand(Type.STRING, s));
                        }
                    }
                } finally {
                    if (!arr.isEmpty()) {
                        operand = new Operand(Type.LIST, arr);
                    }
                }
                break;
            case FUNCTION:
                operand = new Operand(Type.FUNCTION, (FunctionExecutor) obj);
                break;
            case OBJECT:
                // TODO
                break;
            case NONE:
                operand = NO_VALUE;
                break;
            }
        } catch (Exception e) {
            java2VeLaTypeError(obj.getClass(), type);
        }

        return operand;
    }

    /**
     * Return a Java object corresponding to this Operand instance.
     * 
     * @param javaType The target Java type, that may be necessary to know.
     * @return A corresponding Java object.
     */
    public Object toObject(Class<?> javaType) {
        Object obj = null;

        switch (type) {
        case INTEGER:
            obj = numericVeLaToJava(javaType, intVal);
            break;
        case REAL:
            obj = numericVeLaToJava(javaType, doubleVal);
            break;
        case BOOLEAN:
            if (javaType == boolean.class) {
                obj = booleanVal;
            } else {
                vela2JavaTypeError(this, javaType);
            }
            break;
        case STRING:
            if (javaType == String.class || javaType == CharSequence.class) {
                obj = stringVal;
            } else {
                vela2JavaTypeError(this, javaType);
            }
            break;
        case LIST:
            try {
                if (javaType == Type.DBL_ARR.getClass() || javaType == Type.DBL_CLASS_ARR.getClass()) {
                    double[] reals = new double[listVal.size()];
                    list2Array(Type.REAL, (op, i) -> {
                        reals[i++] = op.doubleVal;
                    });
                    obj = reals;
                } else if (javaType == Type.INT_ARR.getClass()) {
                    long[] ints = new long[listVal.size()];
                    list2Array(Type.INTEGER, (op, i) -> {
                        ints[i++] = op.intVal;
                    });
                    obj = ints;
                } else if (javaType == Type.BOOL_ARR.getClass()) {
                    boolean[] booleans = new boolean[listVal.size()];
                    list2Array(Type.BOOLEAN, (op, i) -> {
                        booleans[i++] = op.booleanVal;
                    });
                    obj = booleans;
                } else if (javaType == Type.STR_ARR.getClass()) {
                    String[] strings = new String[listVal.size()];
                    list2Array(Type.STRING, (op, i) -> {
                        strings[i++] = op.stringVal;
                    });
                    obj = strings;
                }
            } catch (Exception e) {
                vela2JavaTypeError(this, javaType);
            }
            break;
        case FUNCTION:
            // TODO
            break;
        case OBJECT:
            // TODO
            break;
        case NONE:
            // TODO
            break;
        }

        return obj;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (booleanVal ? 1231 : 1237);
        long temp;
        temp = Double.doubleToLongBits(doubleVal);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + ((functionVal == null) ? 0 : functionVal.hashCode());
        result = prime * result + (int) (intVal ^ (intVal >>> 32));
        result = prime * result + ((listVal == null) ? 0 : listVal.hashCode());
        result = prime * result + ((stringVal == null) ? 0 : stringVal.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Operand other = (Operand) obj;
        if (booleanVal != other.booleanVal)
            return false;
        if (Double.doubleToLongBits(doubleVal) != Double.doubleToLongBits(other.doubleVal))
            return false;
        if (functionVal == null) {
            if (other.functionVal != null)
                return false;
        } else if (!functionVal.equals(other.functionVal))
            return false;
        if (intVal != other.intVal)
            return false;
        if (listVal == null) {
            if (other.listVal != null)
                return false;
        } else if (!listVal.equals(other.listVal))
            return false;
        if (stringVal == null) {
            if (other.stringVal != null)
                return false;
        } else if (!stringVal.equals(other.stringVal))
            return false;
        if (type != other.type)
            return false;
        return true;
    }

    public Operand copy() {
        Operand operand = new Operand();

        operand.type = type;

        switch (type) {
        case INTEGER:
            operand.intVal = intVal;
            break;
        case REAL:
            operand.doubleVal = doubleVal;
            break;
        case BOOLEAN:
            operand.booleanVal = booleanVal;
            break;
        case STRING:
            operand.stringVal = stringVal;
            break;
        case LIST:
            List<Operand> list = new ArrayList<Operand>();
            for (Operand op : listVal) {
                list.add(op.copy());
            }
            operand.listVal = list;
            break;
        case FUNCTION:
            operand.functionVal = functionVal;
            break;
        default:
            break;
        }

        return operand;
    }

    // Helpers

    private Number numericVeLaToJava(Class<?> javaType, Object obj) {

        Number num = null;

        if (javaType.equals(int.class)) {
            num = ((Number) obj).intValue();
        } else if (javaType.equals(long.class)) {
            num = ((Number) obj).longValue();
        } else if (javaType.equals(float.class)) {
            num = ((Number) obj).floatValue();
        } else if (javaType.equals(double.class)) {
            num = ((Number) obj).doubleValue();
        } else {
            throw new VeLaEvalError("Cannot convert object to numeric value");
        }

        return num;
    }

    /**
     * Build an array from a VeLa list via an assigner object carrying out type
     * conversions where necessary/possible. Concrete assigner objects exploit the
     * existence of an array in their environment (see toObject()).
     *
     * The purpose of this function is to avoid code duplication in toObject().
     *
     * @param requiredType The array element type required.
     * @param assigner     An object that assigns the appropriately typed value from
     *                     an operand to the ith element of an array.
     */
    private void list2Array(Type requiredType, BiConsumer<Operand, Integer> assigner) {
        for (int i = 0; i < listVal.size(); i++) {
            Operand op = listVal.get(i);
            op = op.convert(requiredType);
            if (op.type == requiredType) {
                assigner.accept(op, i);
            } else {
                throw new VeLaEvalError("Cannot convert from " + op.type + " to " + requiredType);
            }
        }
    }

    private static void vela2JavaTypeError(Operand op, Class<?> requiredType) {
        throw new VeLaEvalError("Cannot convert from " + op.type + " to " + requiredType);

    }

    private static void java2VeLaTypeError(Class<?> jtype, Type vtype) {
        throw new VeLaEvalError("Cannot convert from " + jtype + " to " + vtype);

    }
}
