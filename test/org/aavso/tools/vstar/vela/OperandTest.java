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
package org.aavso.tools.vstar.vela;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

/**
 * Operand unit tests.
 */
public class OperandTest extends TestCase {

    public OperandTest(String name) {
        super(name);
    }

    // object2Operand()

    public void testIntToOperand() {
        int obj = 42;
        Operand actual = Operand.object2Operand(Type.INTEGER, obj);
        assertEquals(new Operand(Type.INTEGER, 42), actual);
    }

    public void testLongToOperand() {
        long obj = 42L;
        Operand actual = Operand.object2Operand(Type.INTEGER, obj);
        assertEquals(new Operand(Type.INTEGER, 42), actual);
    }

    public void testFloatToOperand() {
        float obj = 42.0F;
        Operand actual = Operand.object2Operand(Type.REAL, obj);
        assertEquals(new Operand(Type.REAL, 42.0), actual);
    }

    public void testDoubleToOperand() {
        double obj = 42.0D;
        Operand actual = Operand.object2Operand(Type.REAL, obj);
        assertEquals(new Operand(Type.REAL, 42.0), actual);
    }

    public void testBooleanToOperand() {
        boolean obj = false;
        Operand actual = Operand.object2Operand(Type.BOOLEAN, obj);
        assertEquals(new Operand(Type.BOOLEAN, false), actual);
    }

    public void testStringToOperand() {
        String obj = "42";
        Operand actual = Operand.object2Operand(Type.STRING, obj);
        assertEquals(new Operand(Type.STRING, "42"), actual);
    }

    public void testFloatArrayToOperand() {
        float[] obj = new float[] { 42.0F };
        Operand actual = Operand.object2Operand(Type.LIST, obj);
        List<Operand> list = new ArrayList<Operand>();
        list.add(new Operand(Type.REAL, 42.0));
        Operand expected = new Operand(Type.LIST, list);
        assertEquals(expected, actual);
    }

    public void testDoubleArrayToOperand() {
        double[] obj = new double[] { 42.0D };
        Operand actual = Operand.object2Operand(Type.LIST, obj);
        List<Operand> list = new ArrayList<Operand>();
        list.add(new Operand(Type.REAL, 42.0));
        Operand expected = new Operand(Type.LIST, list);
        assertEquals(expected, actual);
    }

    public void testDoubleObjArrayToOperand() {
        Double[] obj = new Double[] { 42.0D };
        Operand actual = Operand.object2Operand(Type.LIST, obj);
        List<Operand> list = new ArrayList<Operand>();
        list.add(new Operand(Type.REAL, 42.0));
        Operand expected = new Operand(Type.LIST, list);
        assertEquals(expected, actual);
    }

    public void testIntArrayToOperand() {
        int[] obj = new int[] { 42 };
        Operand actual = Operand.object2Operand(Type.LIST, obj);
        List<Operand> list = new ArrayList<Operand>();
        list.add(new Operand(Type.INTEGER, 42));
        Operand expected = new Operand(Type.LIST, list);
        assertEquals(expected, actual);
    }

    public void testLongArrayToOperand() {
        long[] obj = new long[] { 42 };
        Operand actual = Operand.object2Operand(Type.LIST, obj);
        List<Operand> list = new ArrayList<Operand>();
        list.add(new Operand(Type.INTEGER, 42));
        Operand expected = new Operand(Type.LIST, list);
        assertEquals(expected, actual);
    }

    public void testBooleanArrayToOperand() {
        boolean[] obj = new boolean[] { true };
        Operand actual = Operand.object2Operand(Type.LIST, obj);
        List<Operand> list = new ArrayList<Operand>();
        list.add(new Operand(Type.BOOLEAN, true));
        Operand expected = new Operand(Type.LIST, list);
        assertEquals(expected, actual);
    }

    public void testStringArrayToOperand() {
        String[] obj = new String[] { "42" };
        Operand actual = Operand.object2Operand(Type.LIST, obj);
        List<Operand> list = new ArrayList<Operand>();
        list.add(new Operand(Type.STRING, "42"));
        Operand expected = new Operand(Type.LIST, list);
        assertEquals(expected, actual);
    }

    public void testInvalidArrayToOperand1() {
        int[] obj = new int[] { 42 };
        try {
            Operand actual = Operand.object2Operand(Type.REAL, obj);
            List<Operand> list = new ArrayList<Operand>();
            list.add(new Operand(Type.STRING, "42"));
            Operand expected = new Operand(Type.LIST, list);
            assertEquals(expected, actual);
            fail();
        } catch (VeLaEvalError e) {
            // should end up here since int array can't be converted to real
        }
    }

    // toObject()

    public void testToIntegerObject() {
        Operand op = new Operand(Type.INTEGER, 42);
        assertEquals(42, op.toObject(int.class));
    }

    public void testToLongObject() {
        Operand op = new Operand(Type.INTEGER, 42);
        assertEquals(42L, op.toObject(long.class));
    }

    public void testToFloatObject() {
        Operand op = new Operand(Type.REAL, 42.0);
        assertEquals(42.0F, op.toObject(float.class));
    }

    public void testToDoubletObject() {
        Operand op = new Operand(Type.REAL, 42.0);
        assertEquals(42.0D, op.toObject(double.class));
    }

    public void testToBooleanObject() {
        Operand op = new Operand(Type.BOOLEAN, true);
        assertEquals(true, op.toObject(boolean.class));
    }

    public void testToInvalidBooleanObject() {
        Operand op = new Operand(Type.BOOLEAN, true);
        try {
            assertEquals(true, op.toObject(double.class));
            fail();
        } catch (VeLaEvalError e) {
        }
    }

    public void testToStringObject() {
        Operand op = new Operand(Type.STRING, "42");
        assertEquals("42", op.toObject(String.class));
    }

    public void testToRealArray() {
        Operand elt1 = new Operand(Type.REAL, 42.0);
        Operand elt2 = new Operand(Type.REAL, 84.0);
        Operand op = new Operand(Type.LIST, Arrays.asList(elt1, elt2));
        double[] expected = new double[] { 42.0, 84.0 };
        double[] actual = (double[]) op.toObject(Type.DBL_ARR.getClass());
        assertEquals(expected.length, actual.length);
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], actual[i]);
        }
    }

    public void testToIntegerArray() {
        Operand elt1 = new Operand(Type.INTEGER, 42);
        Operand elt2 = new Operand(Type.INTEGER, 84);
        Operand op = new Operand(Type.LIST, Arrays.asList(elt1, elt2));
        long[] expected = new long[] { 42L, 84L };
        long[] actual = (long[]) op.toObject(Type.INT_ARR.getClass());
        assertEquals(expected.length, actual.length);
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], actual[i]);
        }
    }

    public void testToBooleanArray() {
        Operand elt1 = new Operand(Type.BOOLEAN, true);
        Operand elt2 = new Operand(Type.BOOLEAN, false);
        Operand op = new Operand(Type.LIST, Arrays.asList(elt1, elt2));
        boolean[] expected = new boolean[] { true, false };
        boolean[] actual = (boolean[]) op.toObject(Type.BOOL_ARR.getClass());
        assertEquals(expected.length, actual.length);
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], actual[i]);
        }
    }

    public void testToStringArray() {
        Operand elt1 = new Operand(Type.STRING, "42");
        Operand elt2 = new Operand(Type.STRING, "84");
        Operand op = new Operand(Type.LIST, Arrays.asList(elt1, elt2));
        String[] expected = new String[] { "42", "84" };
        String[] actual = (String[]) op.toObject(Type.STR_ARR.getClass());
        assertEquals(expected.length, actual.length);
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], actual[i]);
        }
    }

    // convert()

    public void testConvertIntegerToReal() {
        Operand op = new Operand(Type.INTEGER, 42);
        assertEquals(op.convert(Type.REAL), new Operand(Type.REAL, 42.0));
    }

    public void testConvertIntegerToString() {
        Operand op = new Operand(Type.INTEGER, 42);
        assertEquals(op.convert(Type.STRING), new Operand(Type.STRING, "42"));
    }

    public void testConvertRealToString() {
        Operand op = new Operand(Type.REAL, 42.0);
        assertEquals(op.convert(Type.STRING), new Operand(Type.STRING, "42"));
    }

    public void testConvertBooleanTrueToString() {
        Operand op = new Operand(Type.BOOLEAN, true);
        assertEquals(op.convert(Type.STRING), new Operand(Type.STRING, "true"));
    }

    public void testConvertBooleanFalseToString() {
        Operand op = new Operand(Type.BOOLEAN, false);
        assertEquals(op.convert(Type.STRING), new Operand(Type.STRING, "false"));
    }

    public void testConvertRealToInteger() {
        Operand op = new Operand(Type.REAL, 42.0);
        // expect no conversion
        assertEquals(op.convert(Type.REAL), new Operand(Type.REAL, 42.0));
    }

}