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

import java.io.File;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.DoubleUnaryOperator;
import java.util.function.LongUnaryOperator;
import java.util.stream.Collectors;

import org.aavso.tools.vstar.data.DateInfo;
import org.aavso.tools.vstar.data.Magnitude;
import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.util.Tolerance;
import org.aavso.tools.vstar.util.date.AbstractDateUtil;
import org.quicktheories.WithQuickTheories;
import org.quicktheories.core.Gen;
import org.quicktheories.generators.Generate;

import junit.framework.TestCase;

/**
 * This class contains unit tests for VeLa: VStar expression language.
 */
public class VeLaTest extends TestCase implements WithQuickTheories {

    private final static double DELTA = 0.001;

    private final static boolean VERBOSE = false;

    private final static boolean ADD_VSTAR_API = false;

    private VeLaInterpreter vela;

    public VeLaTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        Locale.setDefault(Locale.ENGLISH);
        vela = new VeLaInterpreter(VERBOSE, ADD_VSTAR_API, Collections.emptyList());
    }

    // ** Valid test cases **

    // Real expressions

    public void testPositiveReal1() {
        Operand result = vela.expressionToOperand("12.25");
        assertTrue(Tolerance.areClose(12.25, result.doubleVal(), DELTA, true));
    }

    public void testPositiveRealNoLeadingZero() {
        Operand result = vela.expressionToOperand(".25");
        assertTrue(Tolerance.areClose(.25, result.doubleVal(), DELTA, true));
    }

    public void testNegativeReal1() {
        Operand result = vela.expressionToOperand("-12.25");
        assertTrue(Tolerance.areClose(-12.25, result.doubleVal(), DELTA, true));
    }

    public void testNegativeRealNoLeadingZero() {
        Operand result = vela.expressionToOperand("-.25");
        assertTrue(Tolerance.areClose(-.25, result.doubleVal(), DELTA, true));
    }

    // PBT: Any real number represented as a VeLa expression should
    // evaluate to that number. We exclude infinities and NaNs since
    // we don't represent them directly in VeLa code.
    public void testAnyRealEval() {
        // TODO: where is the VeLa code eval?
        qt().forAll(doubles().any().assuming((n) -> !n.isInfinite() && !n.isNaN()));
    }

    // Note: reveals a bug in which a positive max long value can't be parsed
    // because it has been separated from its unary negation which should
    // happen in the lexer.
//    public void testIntProperty() {
//        String expr = String.format("%d", -9223372036854775808L);
//        Number expected = -9223372036854775808L;
//        Number actual = vela.expressionToOperand(expr.intVal();
//        assertEquals(expected, actual);
//    }

    public void testPosExponent() {
        commonNumericLocaleTest("3.141592E5", 314159.2, 1e-6);
    }

    public void testNegExponent() {
        commonNumericLocaleTest("3.141592E-1", 0.314159, 1e-6);
    }

    public void testAddition() {
        Operand result = vela.expressionToOperand("2457580.25+1004");
        assertTrue(Tolerance.areClose(2458584.25, result.doubleVal(), DELTA, true));
    }

    // PBT: Any two integers added in VeLa should equal the sum of those
    // two integers (longs in VeLa); should actually be longs() but see
    // testIntProperty().
    public void testAnyIntegerAddition() {
        qt().forAll(integers().all(), integers().all()).check((n, m) -> {
            String expr = String.format("%d+%d", (long) n, (long) m);
            return (long) n + (long) m == vela.expressionToOperand(expr).intVal();
        });
    }

    public void testSubtraction() {
        Operand result = vela.expressionToOperand("2457580.25-1004");
        assertTrue(Tolerance.areClose(2456576.25, result.doubleVal(), DELTA, true));
    }

    // PBT: Any two integers subtracted in VeLa should equal the difference
    // of those two integers (longs in VeLa); should actually be longs()
    // but see testIntProperty().
    public void testAnyIntegerSubtraction() {
        qt().forAll(integers().all(), integers().all()).check((n, m) -> {
            String expr = String.format("%d-%d", (long) n, (long) m);
            return (long) n - (long) m == vela.expressionToOperand(expr).intVal();
        });
    }

    public void testMultiplication() {
        Operand result = vela.expressionToOperand("2457580.25*10");
        assertTrue(Tolerance.areClose(24575802.5, result.doubleVal(), DELTA, true));
    }

    // PBT: Any two integers multiplied in VeLa should equal the product
    // of those two integers (longs in VeLa); should actually be longs()
    // but see testIntProperty().
    public void testAnyIntegerMultiplication() {
        qt().forAll(integers().all(), integers().all()).check((n, m) -> {
            String expr = String.format("%d*%d", (long) n, (long) m);
            return (long) n * (long) m == vela.expressionToOperand(expr).intVal();
        });
    }

    public void testDivision() {
        Operand result = vela.expressionToOperand("2457580.25/10");
        assertTrue(Tolerance.areClose(245758.025, result.doubleVal(), DELTA, true));
    }

    // PBT: Any two integers divided in VeLa should equal the quotient
    // of those two integers (longs in VeLa); should actually be longs()
    // but see testIntProperty().
    public void testAnyIntegerDivision() {
        qt().forAll(integers().all(), integers().all()).assuming((n, m) -> m != 0).check((n, m) -> {
            String expr = String.format("%d/%d", (long) n, (long) m);
            return (long) n / (long) m == vela.expressionToOperand(expr).intVal();
        });
    }

    public void testAddSubMul() {
        Operand result = vela.expressionToOperand("2457580.25+1004*2-1");
        assertTrue(Tolerance.areClose(2459587.25, result.doubleVal(), DELTA, true));
    }

    public void testAddSubMulDiv() {
        Operand operand = vela.expressionToOperand("2+3-5*6/2");
        assertEquals(Type.INTEGER, operand.getType());
        assertEquals(-10, operand.intVal());
    }

    public void testRealExponentiation1() {
        Operand operand = vela.expressionToOperand("2.0^3.0");
        assertEquals(Type.REAL, operand.getType());
        assertTrue(Tolerance.areClose(8.0, operand.doubleVal(), DELTA, true));
    }

    public void testRealExponentiation2() {
        Operand operand = vela.expressionToOperand("2^3.0");
        assertEquals(Type.REAL, operand.getType());
        assertTrue(Tolerance.areClose(8.0, operand.doubleVal(), DELTA, true));
    }

    public void testRealExponentiation3() {
        Operand operand = vela.expressionToOperand("3.0^4^2");
        assertEquals(Type.REAL, operand.getType());
        assertTrue(Tolerance.areClose(43046721.0, operand.doubleVal(), DELTA, true));
    }

    public void testRealExponentiation4() {
        Operand operand = vela.expressionToOperand("42.5510075031329^5");
        assertEquals(Type.REAL, operand.getType());
        assertTrue(Tolerance.areClose(139491979.67346534, operand.doubleVal(), DELTA, true));
    }

    public void testReal1() {
        Operand result = vela.expressionToOperand("2.25+1");
        assertTrue(Tolerance.areClose(3.25, result.doubleVal(), DELTA, true));
    }

    public void testReal2() {
        Operand result = vela.expressionToOperand("2.25-1");
        assertTrue(Tolerance.areClose(1.25, result.doubleVal(), DELTA, true));
    }

    public void testReal3() {
        Operand result = vela.expressionToOperand("2.25+1+2");
        assertTrue(Tolerance.areClose(5.25, result.doubleVal(), DELTA, true));
    }

    public void testReal4() {
        Operand result = vela.expressionToOperand("2.25*2*2");
        assertTrue(Tolerance.areClose(9.0, result.doubleVal(), DELTA, true));
    }

    public void testReal5() {
        Operand result = vela.expressionToOperand("1 - 6 / 2 + 4 * 5");
        assertEquals(18, result.intVal());
    }

    public void testReal6() {
        Operand result = vela.expressionToOperand("1 + 6 / 2 + 4 * 5");
        assertEquals(24, result.intVal());
    }

    public void testReal7() {
        Operand result = vela.expressionToOperand("1 + 6 / 2 - 4 * 5");
        assertEquals(-16, result.intVal());
    }

    public void testParens0() {
        Operand result = vela.expressionToOperand("(2457580.25+1004)*10");
        assertTrue(Tolerance.areClose(24585842.50, result.doubleVal(), DELTA, true));
    }

    public void testParens1() {
        Operand result = vela.expressionToOperand("(2457580.25+1004-2)*10");
        assertTrue(Tolerance.areClose(24585822.50, result.doubleVal(), DELTA, true));
    }

    public void testParens2() {
        // 20 years before some JD.
        Operand result = vela.expressionToOperand("2457580.25-(365.25*20)");
        assertTrue(Tolerance.areClose(2450275.25, result.doubleVal(), DELTA, true));
    }

    public void testParens3() {
        Operand result = vela.expressionToOperand("(12.25*-2)");
        assertTrue(Tolerance.areClose(-24.5, result.doubleVal(), DELTA, true));
    }

    public void testResultCacheTest1() {
        // 20 years before some JD.
        Operand result = vela.expressionToOperand("2457580.25-(365.25*20)");
        assertTrue(Tolerance.areClose(2450275.25, result.doubleVal(), DELTA, true));
    }

    // Integer expressions

    public void testIntegerExponentiation1() {
        Operand operand = vela.expressionToOperand("2^3");
        assertEquals(Type.INTEGER, operand.getType());
        assertEquals(8, operand.intVal());
    }

    public void testIntegerExponentiation2() {
        Operand operand = vela.expressionToOperand("3^4^2");
        assertEquals(Type.INTEGER, operand.getType());
        assertEquals(43046721, operand.intVal());
    }

    public void testIntegerExponentiation3() {
        Operand operand = vela.expressionToOperand("-3^4^2");
        assertEquals(Type.INTEGER, operand.getType());
        assertEquals(-43046721, operand.intVal());
    }

    public void testIntegerExponentiation4() {
        Operand operand = vela.expressionToOperand("-(3^4)^2");
        assertEquals(Type.INTEGER, operand.getType());
        assertEquals(-6561, operand.intVal());
    }

    public void testIntegerExponentiation5() {
        Operand operand = vela.expressionToOperand("(-3^4)^2");
        assertEquals(Type.INTEGER, operand.getType());
        assertEquals(6561, operand.intVal());
    }

    public void testIntegerExponentiation6() {
        Operand operand = vela.expressionToOperand("3^4+2");
        assertEquals(Type.INTEGER, operand.getType());
        assertEquals(83, operand.intVal());
    }

    public void testIntegerExponentiation7() {
        Operand operand = vela.expressionToOperand("10^0");
        assertEquals(Type.INTEGER, operand.getType());
        assertEquals(1, operand.intVal());
    }

    public void testIntegerExponentiation8() {
        Operand operand = vela.expressionToOperand("10^1");
        assertEquals(Type.INTEGER, operand.getType());
        assertEquals(10, operand.intVal());
    }

    public void testIntegerExponentiation9() {
        Operand operand = vela.expressionToOperand("(-1)^7");
        assertEquals(Type.INTEGER, operand.getType());
        assertEquals(-1, operand.intVal());
    }

    // String expressions

    // Note tests suggest the importance of using expressionToOperand() and
    // checking the type where a particular type is expected/required.

    public void testString1() {
        Operand operand = vela.expressionToOperand("\"foobar\"");
        assertEquals(Type.STRING, operand.getType());
        assertEquals("foobar", operand.stringVal());
    }

    public void testString2() {
        Operand operand = vela.expressionToOperand("concat(\"foo\" \"bar\")");
        assertEquals(Type.STRING, operand.getType());
        assertEquals("foobar", operand.stringVal());
    }

    public void testFormat() {
        String prog = "";
        prog += "s is format(\"%d\n\" [42])";
        prog += "s";
        Optional<Operand> result = vela.program(prog);
        assertEquals("42\n", result.get().stringVal());
    }

    public void testChr1() {
        String prog = "chr(65)";
        Optional<Operand> result = vela.program(prog);
        assertEquals("A", result.get().stringVal());
    }

    public void testChr2() {
        String prog = "chr(-1)";
        Optional<Operand> result = vela.program(prog);
        assertEquals("", result.get().stringVal());
    }

    public void testOrd() {
        String prog = "ord(\"A\")";
        Optional<Operand> result = vela.program(prog);
        assertEquals(65, result.get().intVal());
    }

    // Boolean expressions

    public void testTrue() {
        assertTrue(vela.booleanExpression("true"));
        assertTrue(vela.booleanExpression("true"));
    }

    public void testFalse() {
        assertFalse(vela.booleanExpression("false"));
        assertFalse(vela.booleanExpression("false"));
    }

    public void testEqBoolVals() {
        assertTrue(vela.booleanExpression("true = true"));
        assertTrue(vela.booleanExpression("false = false"));
        assertFalse(vela.booleanExpression("false = true"));
        assertFalse(vela.booleanExpression("true = false"));
    }

    public void testNotEqBoolVals() {
        assertFalse(vela.booleanExpression("true <> true"));
        assertFalse(vela.booleanExpression("false <>false"));
        assertTrue(vela.booleanExpression("false <> true"));
        assertTrue(vela.booleanExpression("true <> false"));
    }

    // Bitwise operations

    public void testIntegerBitwiseAndDecimal() {
        Optional<Operand> maybeOperand = vela.program("10 and 2");
        assert maybeOperand.isPresent();
        Operand operand = maybeOperand.get();
        assertEquals(Type.INTEGER, operand.getType());
        assertEquals(2, operand.intVal());
    }

    public void testIntegerBitwiseAndBinary() {
        Optional<Operand> maybeOperand = vela.program("0b1010 and 0B10");
        assert maybeOperand.isPresent();
        Operand operand = maybeOperand.get();
        assertEquals(Type.INTEGER, operand.getType());
        assertEquals(0b10, operand.intVal());
    }

    public void testIntegerBitwiseAndHexadecimalUpper() {
        Optional<Operand> maybeOperand = vela.program("0xA and 0X2");
        assert maybeOperand.isPresent();
        Operand operand = maybeOperand.get();
        assertEquals(Type.INTEGER, operand.getType());
        assertEquals(2, operand.intVal());
    }

    public void testIntegerBitwiseAndHexadecimalLower() {
        Optional<Operand> maybeOperand = vela.program("0xa and 2");
        assert maybeOperand.isPresent();
        Operand operand = maybeOperand.get();
        assertEquals(Type.INTEGER, operand.getType());
        assertEquals(2, operand.intVal());
    }

    public void testIntegerBitwiseOr() {
        Optional<Operand> maybeOperand = vela.program("8 or 2");
        assert maybeOperand.isPresent();
        Operand operand = maybeOperand.get();
        assertEquals(Type.INTEGER, operand.getType());
        assertEquals(10, operand.intVal());
    }

    public void testIntegerBitwiseXor() {
        Optional<Operand> maybeOperand = vela.program("10 xor 5");
        assert maybeOperand.isPresent();
        Operand operand = maybeOperand.get();
        assertEquals(Type.INTEGER, operand.getType());
        assertEquals(15, operand.intVal());
    }

    public void testIntegerBitwiseNot() {
        // Note: "and 15" masks out all but the least significant 4 bits
        Optional<Operand> maybeOperand = vela.program("not 8 and 15");
        assert maybeOperand.isPresent();
        Operand operand = maybeOperand.get();
        assertEquals(Type.INTEGER, operand.getType());
        assertEquals(7, operand.intVal());
    }

    public void testIntegerShiftLeft() {
        Optional<Operand> maybeOperand = vela.program("1 << 2");
        assert maybeOperand.isPresent();
        Operand operand = maybeOperand.get();
        assertEquals(Type.INTEGER, operand.getType());
        assertEquals(4, operand.intVal());
    }

    public void testIntegerShiftRightMSBZero() {
        Optional<Operand> maybeOperand = vela.program("4 >> 2");
        assert maybeOperand.isPresent();
        Operand operand = maybeOperand.get();
        assertEquals(Type.INTEGER, operand.getType());
        assertEquals(1, operand.intVal());
    }

    public void testIntegerShiftRightMSBOne() {
        Optional<Operand> maybeOperand = vela.program("(not 0) >> 2");
        assert maybeOperand.isPresent();
        Operand operand = maybeOperand.get();
        assertEquals(Type.INTEGER, operand.getType());
        assertEquals(~0, operand.intVal());
    }

    // Relational expressions

    public void testRealEquality() {
        boolean result = vela.booleanExpression("42 = 42");
        assertTrue(result);
    }

    public void testRealInequality() {
        boolean result = vela.booleanExpression("42 <> 4.2");
        assertTrue(result);
    }

    public void testRealGreaterThan() {
        boolean result = vela.booleanExpression("42 > 4.2");
        assertTrue(result);
    }

    public void testRealLessThan() {
        boolean result = vela.booleanExpression("4.22 < 42");
        assertTrue(result);
    }

    public void testRealGreaterThanOrEqual1() {
        boolean result = vela.booleanExpression("42 >= 4.2");
        assertTrue(result);
    }

    public void testRealGreaterThanOrEqual2() {
        boolean result = vela.booleanExpression("42 >= 42");
        assertTrue(result);
    }

    public void testRealLessThanOrEqual1() {
        boolean result = vela.booleanExpression("4.2 <= 42");
        assertTrue(result);
    }

    public void testRealLessThanOrEqual2() {
        boolean result = vela.booleanExpression("42 <= 42");
        assertTrue(result);
    }

    public void testRealAdditionAndEquality() {
        boolean result = vela.booleanExpression("1+2 = 3");
        assertTrue(result);
    }

    public void testStringEquality1() {
        boolean result = vela.booleanExpression("\"foo\" <> \"foobar\"");
        assertTrue(result);
    }

    public void testStringAdditionAndEquality2() {
        boolean result = vela.booleanExpression("\"foo\"+\"bar\" = \"foobar\"");
        assertTrue(result);
    }

    public void testRegularExpression1() {
        boolean result = vela.booleanExpression("\"Johnson V\" =~ \".+V\"");
        assertTrue(result);
    }

    public void testRegularExpression2() {
        boolean result = vela.booleanExpression("not(\"Johnson B\" =~ \".+V\")");
        assertTrue(result);
    }

    public void testRegularExpression3() {
        boolean result = vela.booleanExpression("\"12.345\" =~ \".+\\d+\"");
        assertTrue(result);
    }

    public void testRegularExpressionWithRealConvertedToString() {
        boolean result = vela.booleanExpression("12.345 =~ \".+\\d+\"");
        assertTrue(result);
    }

    // Logical connective expressions

    public void testDisjunction1() {
        boolean result = vela.booleanExpression("2 < 3 or 2 > 3");
        assertTrue(result);
    }

    public void testConjunction1() {
        boolean result = vela.booleanExpression("2 < 3 and 3 < 5");
        assertTrue(result);
    }

    public void testConjunction2() {
        boolean result = vela.booleanExpression("2 < 3 and 2 > 3");
        assertFalse(result);
    }

    public void testGroupedBooleanExpression1() {
        boolean result = vela.booleanExpression("(2 < 3 and 3 < 5)");
        assertTrue(result);
    }

    public void testGroupedBooleanExpression2() {
        boolean result = vela.booleanExpression("(2 < 3) and (3 < 5)");
        assertTrue(result);
    }

    public void testGroupedBooleanExpression3() {
        boolean result = vela.booleanExpression("3 > 2 and (2 < 3 and 2 > 3)");
        assertFalse(result);
    }

    public void testGroupedBooleanExpression4() {
        boolean result = vela.booleanExpression("3 > 2 or (2 < 3 and 2 > 3)");
        assertTrue(result);
    }

    public void testGroupedBooleanExpression5() {
        boolean result = vela.booleanExpression("3 > 2 or 2 < 3 and 2 > 3");
        assertTrue(result);
    }

    public void testLogicalNegationExpression1() {
        boolean result = vela.booleanExpression("not 3 > 2");
        assertFalse(result);
    }

    public void testLogicalNegationExpression2() {
        Map<String, Operand> env = new HashMap<String, Operand>();
        env.put("raining".toUpperCase(), new Operand(Type.BOOLEAN, false));
        Set<String> boundConstants = new HashSet<String>();
        boundConstants.add("raining");
        vela.pushEnvironment(new VeLaEnvironment<Operand>(env, boundConstants));
        boolean result = vela.booleanExpression("not raining");
        assertTrue(result);
    }

    // Variables

    public void testVariableMeaningOfLife() {
        Map<String, Operand> environment = new HashMap<String, Operand>();
        environment.put("meaning_of_life".toUpperCase(), new Operand(Type.INTEGER, 42));
        Set<String> consts = new HashSet<String>();
        consts.add("meaning_of_life");
        vela.pushEnvironment(new VeLaEnvironment<Operand>(environment, consts));
        boolean result = vela.booleanExpression("meaning_of_life = 42");
        assertTrue(result);
    }

    // This test is important for nested or recursive function calls

    public void testVariableSingleCharacterVariable() {
        Map<String, Operand> environment1 = new HashMap<String, Operand>();
        environment1.put("x".toUpperCase(), new Operand(Type.INTEGER, 4.2));
        Set<String> consts1 = new HashSet<String>();
        consts1.add("x");
        vela.pushEnvironment(new VeLaEnvironment<Operand>(environment1, consts1));

        Map<String, Operand> environment2 = new HashMap<String, Operand>();
        environment2.put("x".toUpperCase(), new Operand(Type.INTEGER, 42));
        Set<String> consts2 = new HashSet<String>();
        consts2.add("x");
        vela.pushEnvironment(new VeLaEnvironment<Operand>(environment2, consts2));

        // The value of x bound to 42 should be on top of the stack.
        boolean result = vela.booleanExpression("x = 42");
        assertTrue(result);
    }

    public void testVariableMultipleEnvironmentsOnStack() {
        Map<String, Operand> environment = new HashMap<String, Operand>();
        environment.put("x".toUpperCase(), new Operand(Type.INTEGER, 42));
        Set<String> consts = new HashSet<String>();
        consts.add("x");
        vela.pushEnvironment(new VeLaEnvironment<Operand>(environment, consts));
        boolean result = vela.booleanExpression("x = 42");
        assertTrue(result);
    }

    // While

    public void testWhileLoop1() {
        String prog = "";
        prog += "i <- 0\n";
        prog += "while i < 10 {";
        prog += "  i <- i + 1";
        prog += "}";
        prog += "i";

        Optional<Operand> result = vela.program(prog);

        assertTrue(result.isPresent());
        assertEquals(10, result.get().intVal());
    }

    // List

    public void testListCaching() {
        Operand result1 = vela.expressionToOperand("[1 \"2\" 3.0]");
        assertEquals(result1.getType(), Type.LIST);
        assertEquals(result1.listVal(), Arrays.asList(new Operand(Type.INTEGER, 1), new Operand(Type.STRING, "2"),
                new Operand(Type.REAL, 3.0)));

        // This one should be cached...
        Operand result2 = vela.expressionToOperand("[1 \"2\" 3.0]");
        assertEquals(result2.getType(), Type.LIST);
        assertEquals(result2.listVal(), Arrays.asList(new Operand(Type.INTEGER, 1), new Operand(Type.STRING, "2"),
                new Operand(Type.REAL, 3.0)));
    }

    public void testEmptyList() {
        Operand result = vela.expressionToOperand("[]");
        assertEquals(result.getType(), Type.LIST);
        assertTrue(result.listVal().isEmpty());
    }

    public void testInListOperator() {
        assertTrue(vela.booleanExpression("2 in [1 2 3]"));
        // cached
        assertTrue(vela.booleanExpression("2 in [1 2 3]"));

        assertTrue(vela.booleanExpression("\"2\" in [1 \"2\" 3]"));

        assertTrue(vela.booleanExpression("3.0 in [1 \"2\" 3.0]"));
        assertTrue(vela.booleanExpression("3.0 IN [1 \"2\" 3.0]"));

        assertTrue(vela.booleanExpression("[2] in [1 [2] 3]"));
    }

    public void testInListOperatorNot() {
        assertFalse(vela.booleanExpression("4 in [1 2 3]"));
    }

    public void testInStringOperator() {
        assertTrue(vela.booleanExpression("2 in \"123\""));
        // cached
        assertTrue(vela.booleanExpression("2 in \"123\""));
    }

    public void testHeterogenousList() {
        assertTrue(vela.booleanExpression("42 in [1 2.0 \"foo\" 42]"));
    }

    public void testInList1() {
        assertTrue(vela.booleanExpression("3 in [1 2 3 4]"));
    }

    public void testNestedList() {
        assertTrue(vela.booleanExpression("[3 4] in [1 2 [3 4]]"));
    }

    // Selection

    public void testWhen() {
        String prog = "when\n3 > 2 -> 42.42\ntrue -> 21.21";

        Optional<Operand> result = vela.program(prog);

        if (result.isPresent()) {
            assertTrue(Tolerance.areClose(42.42, result.get().doubleVal(), DELTA, true));
        } else {
            fail();
        }
    }

    public void testWhenNested() {
        String prog = "";
        prog += "when\n";
        prog += "  3 < 2 -> 42.42\n";
        prog += "  true ->\n";
        prog += "     when 42 > 42 -> 42\n";
        prog += "          true -> 42.0*2";

        Optional<Operand> result = vela.program(prog);

        if (result.isPresent()) {
            assertTrue(Tolerance.areClose(84.0, result.get().doubleVal(), DELTA, true));
        } else {
            fail();
        }
    }

    public void testIfThen() {
        String prog = "if 3 > 2 then 42.42";

        Optional<Operand> result = vela.program(prog);

        if (result.isPresent()) {
            assertTrue(Tolerance.areClose(42.42, result.get().doubleVal(), DELTA, true));
        } else {
            fail();
        }
    }

    public void testIfThenElse() {
        String prog = "if 2 > 3 then 42.42 else 21.21";

        Optional<Operand> result = vela.program(prog);

        if (result.isPresent()) {
            assertTrue(Tolerance.areClose(21.21, result.get().doubleVal(), DELTA, true));
        } else {
            fail();
        }
    }

    public void testCompoundIfThenElse() {
        String prog = "if 3 > 2 then if 2 > 2 then 1 else 2";

        Optional<Operand> result = vela.program(prog);

        if (result.isPresent()) {
            assertEquals(2, result.get().intVal());
        } else {
            fail();
        }
    }

    public void testIfThenElseWithBlocks() {
        String prog = "";
        prog += "if 2 > 3 then {\n";
        prog += "  42.42\n";
        prog += "} else {\n";
        prog += "  if 1 = 1 and 2 > 0 then 21.21 else 2\n";
        prog += "}";

        Optional<Operand> result = vela.program(prog);

        if (result.isPresent()) {
            assertTrue(Tolerance.areClose(21.21, result.get().doubleVal(), DELTA, true));
        } else {
            fail();
        }
    }

    // Functions

    public void testFuncParameterless1() {
        Operand result = vela.expressionToOperand("today()");
        assertTrue(Tolerance.areClose(today(), result.doubleVal(), DELTA, true));
    }

    public void testFuncParameterlessAsSubexpression1() {
        Operand result = vela.expressionToOperand("2+today()");
        assertTrue(Tolerance.areClose(today() + 2, result.doubleVal(), DELTA, true));
    }

    public void testFunctionSin() {
        Operand result = vela.expressionToOperand("sin(pi/2)");
        assertTrue(Tolerance.areClose(1.0, result.doubleVal(), DELTA, true));
    }

    public void testFunctionSqrt() {
        Operand result = vela.expressionToOperand("2*sqrt(144.0)");
        assertTrue(Tolerance.areClose(24.0, result.doubleVal(), DELTA, true));
    }

    // List head

    public void testListHead1() {
        String expr = "head([\"first\" 2 \"3rd\"])";
        Operand result = vela.expressionToOperand(expr);
        assertEquals("first", result.stringVal());
    }

    public void testListHead2() {
        String expr = "head([])";
        Operand result = vela.expressionToOperand(expr);
        assertEquals(Operand.EMPTY_LIST, result);
    }

    public void testListHead3() {
        String expr = "head([[\"first\" 2] \"3rd\"])";
        Operand actual = vela.expressionToOperand(expr);
        Operand expected = vela.expressionToOperand("[\"first\" 2]");
        assertEquals(expected, actual);
    }

    // List nth

    public void testListNth1() {
        String expr = "nth([\"first\" 2 \"3rd\"] 1)";
        Operand actual = vela.expressionToOperand(expr);
        Operand expected = vela.expressionToOperand("2");
        assertEquals(expected, actual);
    }

    public void testListNth2() {
        String expr = "nth([] 42)";
        Operand result = vela.expressionToOperand(expr);
        assertEquals(Operand.EMPTY_LIST, result);
    }

    public void testListNth3() {
        String expr = "nth([[\"first\" 2] \"3rd\"] 0)";
        Operand actual = vela.expressionToOperand(expr);
        Operand expected = vela.expressionToOperand("[\"first\" 2]");
        assertEquals(expected, actual);
    }

    // List tail

    public void testListTail1() {
        String expr = "tail([\"first\" 2 \"3rd\"])";
        Operand actual = vela.expressionToOperand(expr);
        Operand expected = vela.expressionToOperand("[2 \"3rd\"]");
        assertEquals(expected, actual);
    }

    public void testListTail2() {
        String expr = "tail([])";
        Operand result = vela.expressionToOperand(expr);
        assertEquals(Operand.EMPTY_LIST, result);
    }

    public void testListTail3() {
        String expr = "tail([[\"first\" 2] \"3rd\"])";
        Operand actual = vela.expressionToOperand(expr);
        Operand expected = vela.expressionToOperand("[\"3rd\"]");
        assertEquals(expected, actual);
    }

    // Eval

    public void testEval1() {
        String prog = "eval(\"2+3\")";
        Optional<Operand> result = vela.program(prog);
        assertTrue(result.isPresent());
        assertEquals(Type.LIST, result.get().getType());
        assertEquals(1, result.get().listVal().size());
        assertEquals(5, result.get().listVal().get(0).intVal());
    }

    // List length

    public void testListLength1() {
        String expr = "length([\"first\" 2 \"3rd\"])";
        Operand actual = vela.expressionToOperand(expr);
        assertEquals(3, actual.intVal());
    }

    public void testListLength2() {
        String expr = "length([])";
        Operand result = vela.expressionToOperand(expr);
        assertEquals(0, result.intVal());
    }

    // List concatenation

    public void testListConcat1() {
        String expr = "concat([\"first\" 2 \"3rd\"] [4 5])";
        Operand actual = vela.expressionToOperand(expr);
        Operand expected = vela.expressionToOperand("[\"first\" 2 \"3rd\" 4 5]");
        assertEquals(expected, actual);
    }

    public void testListConcat2() {
        String expr = "concat([] [])";
        Operand result = vela.expressionToOperand(expr);
        assertEquals(Operand.EMPTY_LIST, result);
        expr = "concat([\"first\" 2 \"3rd\"] [4 5])";
        result = vela.expressionToOperand(expr);
    }

    // List append

    public void testListAppend1() {
        String expr = "append([\"first\" 2 \"3rd\"] [4 5])";
        Operand actual = vela.expressionToOperand(expr);
        Operand expected = vela.expressionToOperand("[\"first\" 2 \"3rd\" [4 5]]");
        assertEquals(expected, actual);
    }

    public void testListAppend2() {
        String expr = "append([\"first\" 2 \"3rd\"] \"4\")";
        Operand actual = vela.expressionToOperand(expr);
        Operand expected = vela.expressionToOperand("[\"first\" 2 \"3rd\" \"4\"]");
        assertEquals(expected, actual);
    }

    public void testListAppend3() {
        String expr = "append([\"first\" 2 \"3rd\"] 42)";
        Operand actual = vela.expressionToOperand(expr);
        Operand expected = vela.expressionToOperand("[\"first\" 2 \"3rd\" 42]");
        assertEquals(expected, actual);
    }

    public void testListAppend4() {
        String expr = "append([\"first\" 2 \"3rd\"] 4.2)";
        Operand actual = vela.expressionToOperand(expr);
        Operand expected = vela.expressionToOperand("[\"first\" 2 \"3rd\" 4.2]");
        assertEquals(expected, actual);
    }

    public void testListAppend5() {
        String expr = "append([\"first\" 2 \"3rd\"] true)";
        Operand actual = vela.expressionToOperand(expr);
        Operand expected = vela.expressionToOperand("[\"first\" 2 \"3rd\" true]");
        assertEquals(expected, actual);
    }

    // Binary operations over lists

    public void testAddTwoIntegerLists() {
        String expr = "[1 2 3 4] + [5 6 7 8]";
        Operand actual = vela.expressionToOperand(expr);
        Operand expected = vela.expressionToOperand("[6 8 10 12]");
        assertEquals(expected, actual);
    }

    public void testAddIntegerAndList() {
        String expr = "4 + [5 6 7 8]";
        Operand actual = vela.expressionToOperand(expr);
        Operand expected = vela.expressionToOperand("[9 10 11 12]");
        assertEquals(expected, actual);
    }

    public void testAddListAndInteger() {
        String expr = "[5 6 7 8] + 4";
        Operand actual = vela.expressionToOperand(expr);
        Operand expected = vela.expressionToOperand("[9 10 11 12]");
        assertEquals(expected, actual);
    }

    public void testShiftLeftTwoIntegerLists() {
        String expr = "[1 2 3 4] << [5 6 7 8]";
        Optional<Operand> actual = vela.program(expr);
        Optional<Operand> expected = vela.program("[32 128 384 1024]");
        assertEquals(expected, actual);
    }

    public void tesCommutativityOverIntegerListOperations() {
        String expr = "24 * [1 2] = [1 2] * 24";
        Optional<Operand> actual = vela.program(expr);
        Optional<Operand> expected = vela.program("[true true]");
        assertEquals(expected, actual);
    }

    public void testLessThanTwoIntegerLists() {
        String expr = "[1 6 3 9] < [5 6 7 8]";
        Optional<Operand> actual = vela.program(expr);
        Optional<Operand> expected = vela.program("[true false true false]");
        assertEquals(expected, actual);
    }

    // PBT: binary boolean operations over two lists
    public void testOperationsOverBooleanLists() {
        List<String> ops = Arrays.asList("and", "or", "xor");

        Gen<String> operators = Generate.pick(ops);
        qt().forAll(operators).check((operator) -> {
            Optional<Operand> expected = null;

            String expr = String.format("[true false] %s [false true]", operator);
            Optional<Operand> actual = vela.program(expr);

            switch (Operation.getBinaryOp(operator)) {
            case AND:
                expected = vela.program("[false false]");
                break;
            case OR:
                expected = vela.program("[true true]");
                break;
            case XOR:
                expected = vela.program("[true true]");
                break;
            default:
                assertTrue(false);
            }

            return expected.equals(actual);
        });
    }

    public void testNegateIntegerList() {
        String expr = "-[5 6 7 8]";
        Optional<Operand> actual = vela.program(expr);
        assertTrue(actual.isPresent());
        List<Operand> expected = Arrays.asList(-5, -6, -7, -8).stream().map((n) -> new Operand(Type.INTEGER, n))
                .collect(Collectors.toList());
        assertEquals(actual.get().listVal(), expected);
    }

    public void testNegateRealList() {
        String expr = "-[5.0 6.0]";
        Optional<Operand> actual = vela.program(expr);
        assertTrue(actual.isPresent());
        List<Operand> expected = Arrays.asList(-5.0, -6.0).stream().map((n) -> new Operand(Type.REAL, n))
                .collect(Collectors.toList());
        assertEquals(actual.get().listVal(), expected);
    }

    public void testNotIntegerList() {
        String expr = "not [5 6 7 8]";
        Optional<Operand> actual = vela.program(expr);
        assertTrue(actual.isPresent());
        List<Operand> expected = Arrays.asList(-6, -7, -8, -9).stream().map((n) -> new Operand(Type.INTEGER, n))
                .collect(Collectors.toList());
        assertEquals(actual.get().listVal(), expected);
    }

    public void testNotBooleanList() {
        String expr = "not [true false]";
        Optional<Operand> actual = vela.program(expr);
        Optional<Operand> expected = vela.program("[false true]");
        assertEquals(actual, expected);
    }

    public void testBooleanAndList() {
        String expr = "true and [true false]";
        Optional<Operand> actual = vela.program(expr);
        Optional<Operand> expected = vela.program("[true false]");
        assertEquals(expected, actual);
    }

    public void testListAndBoolean() {
        String expr = "[true false] and true";
        Optional<Operand> actual = vela.program(expr);
        Optional<Operand> expected = vela.program("[true false]");
        assertEquals(expected, actual);
    }

    public void testOperationsOverIntegersAndList() {
        commonTestOperationsOverIntegersAndList(true);
    }

    public void testOperationsOverListAndIntegers() {
        commonTestOperationsOverIntegersAndList(false);
    }

    // PBT: Arithmetic over lists where one operand is an integer and
    // the other is a (two element) list, e.g. n + [5 6] or [5 6] + n
    public void commonTestOperationsOverIntegersAndList(boolean isNumFirst) {
        List<String> ops = Arrays.asList("+", "-", "*", "/", "^", "<<", ">>");
        Gen<String> operators = Generate.pick(ops);

        int max = 1000;
        int min = 1; // TODO: n < 0 yields errors for real ^

        qt().forAll(integers().between(min, max), integers().from(1).upToAndIncluding(10),
                integers().from(1).upToAndIncluding(10), operators).check((n, a, b, operator) -> {
                    // Construct a VeLa expression that combines a random integer
                    // and a list with a random operation.
                    List<Integer> op2List = Arrays.asList(a, b);
                    String op2 = "["
                            + String.join(" ", op2List.stream().map(Object::toString).collect(Collectors.toList()))
                            + "]";

                    String expr;
                    if (isNumFirst) {
                        expr = String.format("%d %s %s", n, operator, op2);
                    } else {
                        expr = String.format("%s %s %d", op2, operator, n);
                    }

                    LongUnaryOperator operatorFunc = operatorFunc(operator, n, !isNumFirst);

                    List<Long> expected = op2List.stream().map((m) -> operatorFunc.applyAsLong(m))
                            .collect(Collectors.toList());

                    Optional<Operand> result = vela.program(expr);
                    boolean success = result.isPresent();

                    if (success) {
                        List<Long> actual = result.get().listVal().stream().map((operand) -> operand.intVal())
                                .collect(Collectors.toList());

                        success &= expected.equals(actual);
                    }

                    return success;
                });
    }

    // PBT: Arithmetic over lists where the first operand is a real and
    // the second operand is a list, e.g. n + [5 6 7 8]
    public void testOperationsOverRealsAndAList() {
        List<String> ops = Arrays.asList("+", "-", "*", "/", "^");
        Gen<String> operators = Generate.pick(ops);

        int max = 10;
        int min = 0; // TODO: n < 0 and > ~10 yields errors for real ^

        qt().forAll(doubles().from(min).upToAndIncluding(max), doubles().from(1).upToAndIncluding(10),
                doubles().from(1).upToAndIncluding(10), operators).check((n, a, b, operator) -> {
                    // Construct a VeLa expression that combines a random integer
                    // and a list with a random operation.
                    List<Double> op2List = Arrays.asList(a, b);
                    String op2 = "["
                            + String.join(" ", op2List.stream().map(Object::toString).collect(Collectors.toList()))
                            + "]";

                    String expr = String.format("%f %s %s", n, operator, op2);

                    DoubleUnaryOperator operatorFunc = operatorFunc(operator, n);

                    List<Double> expected = op2List.stream().map((m) -> operatorFunc.applyAsDouble(m))
                            .collect(Collectors.toList());

                    Optional<Operand> result = vela.program(expr);
                    boolean success = result.isPresent();

                    if (success) {
                        List<Double> actual = result.get().listVal().stream().map((operand) -> operand.doubleVal())
                                .collect(Collectors.toList());

                        for (int i = 0; i < actual.size(); i++) {
                            success &= Tolerance.areClose(actual.get(i), expected.get(i), 1e6, true);
                        }
                    }

                    return success;
                });
    }

    public void testIntegerSeq() {
        String prog = "seq(1 5 1)";

        Optional<Operand> result = vela.program(prog);

        assertTrue(result.isPresent());

        List<Operand> expected = Arrays.asList(new Operand(Type.INTEGER, 1), new Operand(Type.INTEGER, 2),
                new Operand(Type.INTEGER, 3), new Operand(Type.INTEGER, 4), new Operand(Type.INTEGER, 5));

        assertEquals(expected, result.get().listVal());
    }

    public void testIntegerSeqWithReduce() {
        String prog = "";
        prog += "sum(x:integer y:integer) : integer {";
        prog += "    x+y";
        prog += "}";
        prog += "reduce(sum seq(1 5 1) 0)";

        Optional<Operand> result = vela.program(prog);

        assertTrue(result.isPresent());
        assertEquals(15, result.get().intVal());
    }

    public void testRealSeq() {
        String prog = "seq(1.0 5.0 1.0)";

        Optional<Operand> result = vela.program(prog);

        assertTrue(result.isPresent());

        List<Operand> expected = Arrays.asList(new Operand(Type.REAL, 1.0), new Operand(Type.REAL, 2.0),
                new Operand(Type.REAL, 3.0), new Operand(Type.REAL, 4.0), new Operand(Type.REAL, 5.0));

        assertEquals(expected, result.get().listVal());
    }

    // Intrinsic string functions (from String class)

    public void testFunctionContains() {
        Operand result = vela.expressionToOperand("contains(\"xyz123abc\" \"23a\")");
        assertTrue(result.booleanVal());
    }

    public void testFunctionEndsWith() {
        Operand result = vela.expressionToOperand("endsWith(\"12345\" \"45\")");
        assertTrue(result.booleanVal());
    }

    public void testFunctionMatches() {
        Operand result = vela.expressionToOperand("matches(\"12345\" \"^\\d{3}45$\")");
        assertTrue(result.booleanVal());
    }

    public void testFunctionReplace() {
        Operand result = vela.expressionToOperand("replace(\"abcd\" \"bc\" \"BC\") = \"aBCd\"");
        assertTrue(result.booleanVal());
    }

    public void testFunctionConcat() {
        Operand result = vela.expressionToOperand("concat(\"abcd\" \"ef\") = \"abcdef\"");
        assertTrue(result.booleanVal());
    }

    public void testLastIndexOf() {
        Operand operand = vela.expressionToOperand("lastIndexOf(\"dabcde\" \"d\")");
        assertEquals(Type.INTEGER, operand.getType());
        assertEquals(4, operand.intVal());
    }

    public void testIntrinsics() {
        Operand operand = vela.expressionToOperand("length(intrinsics()) <> 0");
        assertEquals(Type.BOOLEAN, operand.getType());
        assertEquals(true, operand.booleanVal());
    }

    public void testHelpVar() {
        Operand operand = vela.expressionToOperand("help(pi)");
        assertEquals(Type.STRING, operand.getType());
        assertEquals("REAL : 3.14159265359\n\n", operand.stringVal());
    }

    public void testHelpCos() {
        Operand operand = vela.expressionToOperand("help(cos)");
        assertEquals(Type.STRING, operand.getType());
        assertEquals("COS(REAL) : REAL\n\n", operand.stringVal());
    }

    public void testHelpHelp() {
        Operand operand = vela.expressionToOperand("help(help)");
        assertEquals(Type.STRING, operand.getType());
        StringBuffer expected = new StringBuffer();
        expected.append("HELP(ANY) : STRING\n");
        expected.append("Returns a help string given ");
        expected.append("an arbitrary parameter.\n\n");
        assertEquals(expected.toString(), operand.stringVal());
    }

    // User defined functions

    public void testNamedFunSquare() {
        String prog = "";
        prog += "f(x:integer y:integer) : integer { x^y }\n";
        prog += "x is f(12 2)\n";
        prog += "x";

        Optional<Operand> result = vela.program(prog);

        assertTrue(result.isPresent());
        assertEquals(144, result.get().intVal());
    }

    public void testNamedFunWithSelect() {
        String prog = "";
        prog += "f(n:integer) : integer {";
        prog += "    when";
        prog += "      n <= 0 -> 1";
        prog += "      true -> n*n";
        prog += "}";
        prog += "x is f(12)";
        prog += "x";

        Optional<Operand> result = vela.program(prog);

        assertTrue(result.isPresent());
        assertEquals(144, result.get().intVal());
    }

    public void testNamedFunRecursiveLoop() {
        String prog = "";
        prog += "<<";
        prog += " Not properly tail recursive loop function.";
        prog += ">>";
        prog += "loop(n:integer) {";
        prog += "    print(n \"^2 = \" n*n \"\n\")";
        prog += "    when n < 10 -> loop(n+1)";
        prog += "}";
        prog += "loop(1)";

        vela.program(prog);
    }

    public void testParamConversionDoesNotChangeActualParam() {
        String prog = "";
        prog += "a is 42\n" + "f(n:real):real{n/5}\n" + "f(a)\n" + "a\n";

        Optional<Operand> result = vela.program(prog);

        assertTrue(result.isPresent());
        assertEquals(42, result.get().intVal());
        assertEquals(Type.INTEGER, result.get().getType());
    }

    // Turing Completeness attained: Dec 20 2018, 18:55 :)

    public void testNamedFunRecursiveFactorial() {
        String prog = "";
        prog += "<< Recursive factorial function >>";
        prog += "fact(n:integer) : integer {";
        prog += "    when";
        prog += "      n <= 0 -> 1";
        prog += "      true -> n*fact(n-1)";
        prog += "}";
        prog += "x is fact(6)";
        prog += "x";

        Optional<Operand> result = vela.program(prog);

        assertTrue(result.isPresent());
        assertEquals(720, result.get().intVal());
    }

    public void testAnonFunExponentiation() {
        String prog = "function(x:integer y:integer) : integer { x^y }(12 2)";

        Optional<Operand> result = vela.program(prog);

        assertTrue(result.isPresent());
        assertEquals(144, result.get().intVal());
    }

    public void testAnonUpperCaseLambdaExponentiation() {
        String prog = "Λ(x:integer y:integer) : integer { x^y }(12 2)";

        Optional<Operand> result = vela.program(prog);

        assertTrue(result.isPresent());
        assertEquals(144, result.get().intVal());
    }

    public void testAnonLowerCaseLambdaExponentiation() {
        String prog = "λ(x:integer y:integer) : integer { x^y }(12 2)";

        Optional<Operand> result = vela.program(prog);

        assertTrue(result.isPresent());
        assertEquals(144, result.get().intVal());
    }

    public void testAnonFunExponentiationWithReturnTypeConversion() {
        // Returned value should be coerced from integer to real.
        String prog = "function(x:integer y:integer) : real { x^y }(12 2)";

        Optional<Operand> result = vela.program(prog);

        assertTrue(result.isPresent());
        assertTrue(Tolerance.areClose(144.0, result.get().doubleVal(), DELTA, true));
    }

    public void testHOF1() {
        String prog = "";
        prog += "f(g:function h:function n:integer) : integer {";
        prog += "    x is g(h(n))";
        prog += "    print(\"g o h \" n \" = \" x \"\n\")";
        prog += "    x";
        prog += "}\n";

        prog += "fact(n:integer) : integer {";
        prog += "    when";
        prog += "      n <= 0 -> 1";
        prog += "      true -> n*fact(n-1)";
        prog += "}\n";

        prog += "f(fact function(n:integer) : integer {n*n} 3)";

        Optional<Operand> result = vela.program(prog);

        assertTrue(result.isPresent());
        assertEquals(362880, result.get().intVal());
    }

    public void HOF2() {
        String prog = "";
        prog += "g(f:function) : function {";
        prog += "    function(n:integer) : integer {";
        prog += "        f(n)";
        prog += "    }";
        prog += "}\n";

        prog += "cube(n:integer):integer {";
        prog += "    n*n*n";
        prog += "}\n";

        // g(cube) returns a function that when called with an
        // integer parameter, returns the result of calling cube(n).
        // Note that g(cube) must be wrapped in parentheses otherwise
        // it is not invoked.
        prog += "(g(cube))(3)";

        Optional<Operand> result = vela.program(prog);

        assertTrue(result.isPresent());
        assertEquals(27, result.get().intVal());
    }

    public void testHOF3() {
        String prog = "";
        // Lambda as function type instead of "function".
        prog += "f(g:λ x:integer) : integer {";
        prog += "    g(x)";
        prog += "}\n";

        prog += "f(λ(x:integer) : integer{x*x} 4)\n";

        Optional<Operand> result = vela.program(prog);

        assertTrue(result.isPresent());
        assertEquals(16, result.get().intVal());
    }

    public void testYCombinator() {
        // The Y Combinator is a good test of HOFs and revealed some bugs in VeLa in the
        // past. This example defines and use Y to compute anonymous recursive
        // factorial.
        String prog = "";
        prog += "Y is λ(h : λ) : λ {";
        prog += "    λ(f : λ) : λ {";
        prog += "        f(f)";
        prog += "    } (λ(f : λ) : λ {";
        prog += "        h(λ(n : integer) : integer {";
        prog += "            (f(f))(n)";
        prog += "        })";
        prog += "    })";
        prog += "}\n";

        prog += "result is (Y(λ(g : λ) : λ {";
        prog += "    λ(n : integer) : integer {";
        prog += "        if n < 2 then 1 else n * g(n - 1)";
        prog += "    }";
        prog += "})) (8)\n";

        prog += "result\n";

        Optional<Operand> result = vela.program(prog);

        assertTrue(result.isPresent());
        assertEquals(40320, result.get().intVal());
    }

    public void testBoundFun() {
        String prog = "";
        prog += "fact(n:integer) : integer {";
        prog += "    when";
        prog += "      n <= 0 -> 1";
        prog += "      true -> n*fact(n-1)";
        prog += "}";
        prog += "f is fact\n";
        prog += "fact(6)";

        Optional<Operand> result = vela.program(prog);

        assertTrue(result.isPresent());
        assertEquals(720, result.get().intVal());
    }

    // see https://github.com/AAVSO/VStar/issues/441
    public void testFunReturningLiteralExpr() {
        String prog = "";
        prog += "f(n:real) : real { 12.0 }";
        prog += "f(4)";

        Optional<Operand> result = vela.program(prog);

        assertTrue(result.isPresent());
        assertEquals(12.0, result.get().doubleVal());
    }

    public void testFunMap() {
        String prog = "";
        prog += "fact(n:integer) : integer {";
        prog += "    when";
        prog += "      n <= 0 -> 1";
        prog += "      true -> n*fact(n-1)";
        prog += "}";
        prog += "map(fact [1 2 3 4 5])";

        Optional<Operand> result = vela.program(prog);

        assertTrue(result.isPresent());
        List<Operand> expected = new ArrayList<Operand>();
        expected.add(new Operand(Type.INTEGER, 1));
        expected.add(new Operand(Type.INTEGER, 2));
        expected.add(new Operand(Type.INTEGER, 6));
        expected.add(new Operand(Type.INTEGER, 24));
        expected.add(new Operand(Type.INTEGER, 120));
        List<Operand> actual = result.get().listVal();
        assertEquals(expected, actual);
    }

    public void testFunFilter() {
        String prog = "";
        prog += "lessthan10(n:integer) : boolean {";
        prog += "    n < 5";
        prog += "}";
        prog += "filter(lessthan10 [1 2 3 4 5])";

        Optional<Operand> result = vela.program(prog);

        assertTrue(result.isPresent());
        List<Operand> expected = new ArrayList<Operand>();
        expected.add(new Operand(Type.INTEGER, 1));
        expected.add(new Operand(Type.INTEGER, 2));
        expected.add(new Operand(Type.INTEGER, 3));
        expected.add(new Operand(Type.INTEGER, 4));
        List<Operand> actual = result.get().listVal();
        assertEquals(expected, actual);
    }

    public void testFunFilterWithClosure() {
        String prog = "";
        // This tests that a closure works! (January 22 2018)
        prog += "lessthan(n:integer) : function {";
        prog += "    function(x: integer) : boolean { x < n }";
        prog += "}";
        prog += "filter(lessthan(7) [1 2 3 4 5 6 7 8 9 10])";

        Optional<Operand> result = vela.program(prog);
        assertTrue(result.isPresent());
        List<Operand> actual = result.get().listVal();

        List<Operand> expected = new ArrayList<Operand>();
        expected.add(new Operand(Type.INTEGER, 1));
        expected.add(new Operand(Type.INTEGER, 2));
        expected.add(new Operand(Type.INTEGER, 3));
        expected.add(new Operand(Type.INTEGER, 4));
        expected.add(new Operand(Type.INTEGER, 5));
        expected.add(new Operand(Type.INTEGER, 6));
        assertEquals(expected, actual);
    }

    public void testFunReduceInteger() {
        String prog = "";
        prog += "sum(x:integer y:integer) : integer {";
        prog += "    x+y";
        prog += "}";
        prog += "reduce(sum [1 2 3 4 5] 0)";

        Optional<Operand> result = vela.program(prog);

        assertTrue(result.isPresent());
        assertEquals(15, result.get().intVal());
    }

    public void testFunReduceReal() {
        String prog = "";
        prog += "prod(x:real y:real) : real {";
        prog += "    x*y";
        prog += "}";
        // Factorial via reduce
        prog += "reduce(prod [5 4 3 2 1] 1)";

        Optional<Operand> result = vela.program(prog);

        assertTrue(result.isPresent());
        assertTrue(Tolerance.areClose(120.0, result.get().doubleVal(), DELTA, true));
    }

    public void ignoreTestFunFor1() {
        String prog = "";
        prog += "cubeplus1(n:integer) {\n";
        prog += "    result is append(result n^3+1)\n";
        prog += "}\n";

        prog += "nums is [2 4 6 8]\n";
        prog += "result is []\n";
        prog += "for(cubeplus1 nums)\n";
        prog += "result";

        Optional<Operand> result = vela.program(prog);
        assertTrue(result.isPresent());
        List<Operand> actual = result.get().listVal();

        List<Operand> expected = new ArrayList<Operand>();
        expected.add(new Operand(Type.INTEGER, 9));
        expected.add(new Operand(Type.INTEGER, 65));
        expected.add(new Operand(Type.INTEGER, 217));
        expected.add(new Operand(Type.INTEGER, 513));

        assertEquals(expected, actual);
    }

    public void testFunFor2() {
        String prog = "";
        prog += "cubeplus1(n:integer) {\n";
        prog += "    print(n^3+1 \"\n\"\n)";
        prog += "}\n";

        prog += "nums is [2 4 6 8]\n";
        prog += "for(cubeplus1 nums)\n";

        vela.program(prog);
    }

    public void testFourierModelFunction() {
        String prog = "";

        prog += "f(t:real) : real {\n";
        prog += "  11.7340392\n";
        prog += "  -0.6588158 * cos(2*PI*0.0017177*(t-2451700))\n";
        prog += "  +1.3908874 * sin(2*PI*0.0017177*(t-2451700))";
        prog += "}\n";

        prog += "f(2447121.5)\n";

        Optional<Operand> result = vela.program(prog);

        assertTrue(result.isPresent());
        assertTrue(Tolerance.areClose(12.34620932, result.get().doubleVal(), 1e-6, true));
    }

    public void testFourierModelFunctionWithObsEnv() {
        String prog = "";

        prog += "f(t:real) : real {\n";
        prog += "  11.7340392\n";
        prog += "  -0.6588158 * cos(2*PI*0.0017177*(t-2451700))\n";
        prog += "  +1.3908874 * sin(2*PI*0.0017177*(t-2451700))";
        prog += "}\n";

        prog += "f(2447121.5)\n";

        // This model makes no use of obs but could do so.
        // See also https://github.com/AAVSO/VStar/issues/429
        List<ValidObservation> obs = commonObs();
        vela.pushEnvironment(new VeLaValidObservationEnvironment(obs.get(0)));

        Optional<Operand> result = vela.program(prog);

        assertTrue(result.isPresent());
        assertTrue(Tolerance.areClose(12.34620932, result.get().doubleVal(), 1e-6, true));
    }

    public void testModelFunctionWithAllLocales() {
        String prog = "";
        // <- only necessary here because zeroPoint will be modified
        // via commonNumericLocaleTest() multiple times
        prog += "zeroPoint <- 2459332.35709\n";
        prog += "f(t:real) : real {\n";
        prog += "3.513493389760E19*(t-zeroPoint)^15 +\n";
        prog += "1.486885038629E18*(t-zeroPoint)^14 +\n";
        prog += "-6.994358939253E17*(t-zeroPoint)^13 +\n";
        prog += "-2.778248197535E16*(t-zeroPoint)^12 +\n";
        prog += "5.705533522828E15*(t-zeroPoint)^11 +\n";
        prog += "2.089863042692E14*(t-zeroPoint)^10 +\n";
        prog += "-2.460853674112E13*(t-zeroPoint)^9 +\n";
        prog += "-8.103385927143E11*(t-zeroPoint)^8 +\n";
        prog += "6.037656131770E10*(t-zeroPoint)^7 +\n";
        prog += "1.722865043295E09*(t-zeroPoint)^6 +\n";
        prog += "-8.456524341291E07*(t-zeroPoint)^5 +\n";
        prog += "-1.993607493521E06*(t-zeroPoint)^4 +\n";
        prog += "6.522979829427E04*(t-zeroPoint)^3 +\n";
        prog += "1.170687252721E03*(t-zeroPoint)^2 +\n";
        prog += "-2.269285161529E01*(t-zeroPoint)^1 +\n";
        prog += "1.033057728586E01\n";
        prog += "}\n";
        prog += "f(2459332.28594)";
        commonNumericLocaleTest(prog, 10.231882, 1e-6);
    }

    public void testMean() {
        String prog = "";
        prog += "mean(vals:list) : real {";
        prog += "  reduce(function(n:real m:real):real{ n+m } vals 0) / length(vals)";
        prog += "}";
        prog += "mags is [3.678 3.776 3.866 3.943 4 4.062 4.117 4.089 3.883 3.651 3.653]";
        prog += "mean(mags)";

        Optional<Operand> result = vela.program(prog);
        assertTrue(result.isPresent());
        assertTrue(Tolerance.areClose(3.8834545, result.get().doubleVal(), 1e-7, true));
    }

    // Bindings

    public void testBindingNonConstant() {
        // Bind X to 2 then retrieve the bound value of X.
        String prog = "";
        prog += "x <- 12\n";
        prog += "x";

        Optional<Operand> result = vela.program(prog);

        assertTrue(result.isPresent());
        assertEquals(12, result.get().intVal());

        // Bind X again.
        result = vela.program("x <- x + 1  x");
        assertTrue(result.isPresent());
        assertEquals(13, result.get().intVal());
    }

    public void testBindingNonConstantTwoTypes() {
        // Bind X to 2
        String prog = "x is 2\n";

        vela.program(prog);

        // Attempt to bind X again but to a value of
        // type string, not integer. This should fail.
        try {
            vela.program("x is \"2\"");
            fail();
        } catch (Exception e) {
            // This is where we expect to be.
        }
    }

    public void testBindingConstant1() {
        // Bind X to 12 then retrieve the bound value of X.
        String prog = "";
        prog += "x is 12\n";
        prog += "x";

        Optional<Operand> result = vela.program(prog);

        assertTrue(result.isPresent());
        assertEquals(12, result.get().intVal());

        // Attempt to bind X again by assignment.
        // This should fail.
        try {
            vela.program("x is x + 1");
            fail();
        } catch (Exception e) {
            // This is where we expect to be.
        }
    }

    public void testBindingConstant2() {
        // Bind X to 12 then retrieve the bound value of X.
        String prog = "";
        prog += "x is 12\n";
        prog += "x";

        Optional<Operand> result = vela.program(prog);

        assertTrue(result.isPresent());
        assertEquals(12, result.get().intVal());

        // Attempt to bind X again as a constant.
        // This should fail.
        try {
            vela.program("x is x + 1");
            fail();
        } catch (Exception e) {
            // This is where we expect to be.
        }
    }

    public void testBindingConstant3() {
        // Bind X to 42 then retrieve the bound value of X.
        String prog = "";
        prog += "x is 12\n";
        prog += "x";

        Optional<Operand> result = vela.program(prog);

        assertTrue(result.isPresent());
        assertEquals(12, result.get().intVal());

        // Attempt to bind X as a constant.
        // This should fail.
        try {
            vela.program("x is x + 1");
            fail();
        } catch (Exception e) {
            // This is where we expect to be.
        }
    }

    public void testConstantWithUnicodeName() {
        String prog = "";
        prog += "π";

        Optional<Operand> result = vela.program(prog);

        assertTrue(result.isPresent());
        assertTrue(Tolerance.areClose(Math.PI, result.get().doubleVal(), 0.00001, true));
    }

    public void testClosureBasedCounter() {
        String prog = "";
        prog += "mkcounter(start:integer) : function {\n";
        prog += "  count is start\n";
        prog += "  counter(n:integer) : integer { count <- count + n  count }\n";
        prog += "  counter\n";
        prog += "}\n";
        prog += "c is mkcounter(10)\n";
        prog += "c(1)\n";
        prog += "c(1)\n";
        prog += "c(1)\n";

        Optional<Operand> result = vela.program(prog);

        assertTrue(result.isPresent());
        assertEquals(13, result.get().intVal());
    }

    public void testAnonymousClosureBasedCounter() {
        String prog = "";
        prog += "mkcounter(start:integer) : function {\n";
        prog += "  count is start\n";
        prog += "  function(n:integer) : integer { count <- count + n  count }\n";
        prog += "}\n";
        prog += "c is mkcounter(10)\n";
        prog += "c(1)\n";
        prog += "c(1)\n";
        prog += "c(1)\n";

        Optional<Operand> result = vela.program(prog);

        assertTrue(result.isPresent());
        assertEquals(13, result.get().intVal());
    }
    // Sequence

    public void testSequence() {
        String prog = "";
        prog += "str <- \"\"";
        prog += "ch is \"1\"";
        prog += "str <- when";
        prog += "           ch = \"1\" -> str + \" ONE\"";
        prog += "           ch = \"2\" -> str + \" TWO\"";
        prog += "           true -> { println(\"OTHER\") str }\n";
        // prog += "println(format(\"%s: '%s'\" [ch str]))";
        prog += "str";

        Optional<Operand> result = vela.program(prog);

        assertTrue(result.isPresent());
        assertEquals(" ONE", result.get().stringVal());
    }

    // I/O test cases by inspection...

    public void testFormattedPrint() {
        String prog = "";
        prog += "print(format(\"%d\n\" [42]))";
        vela.program(prog);
    }

    public void testFormattedPrintln() {
        String prog = "";
        prog += "println(format(\"%d\" [42]))";
        vela.program(prog);
    }

    // Filter test cases

    public void testVeLaBooleanExpressionsAsFilters1() {
        List<ValidObservation> obs = commonObs();
        String expr = "uncertainty >= 0.1";
        assertEquals(2, filterObs(expr, obs).size());
    }

    public void testVeLaBooleanExpressionsAsFilters2() {
        List<ValidObservation> obs = commonObs();
        String expr = "uncertainty > 0.01 and uncertainty < 0.03";
        assertEquals(1, filterObs(expr, obs).size());
    }

    public void testVeLaBooleanExpressionsAsFilters3() {
        List<ValidObservation> obs = commonObs();
        String expr = "magnitude > 12 and (uncertainty > 0 and uncertainty <= 0.01)";
        assertEquals(1, filterObs(expr, obs).size());
    }

    // Comments

    public void testComments1() {
        Operand result = vela.expressionToOperand("# comment test\n\r12+2");
        assertEquals(14, result.intVal());
    }

    public void testComments2() {
        Operand result = vela.expressionToOperand("# comment test\r\n12+2");
        assertEquals(14, result.intVal());
    }

    public void testComments3() {
        Operand result = vela.expressionToOperand("# comment test\n12+2");
        assertEquals(14, result.intVal());
    }

    public void testComments4() {
        Optional<Operand> operand = vela.program("# comment test");
        assertFalse(operand.isPresent());
    }

    // ** Error cases **

    public void testAmpersand() {
        try {
            vela.expressionToOperand("2457580.25&1004");
            fail();
        } catch (VeLaParseError e) {
            assertTrue(e.getMessage().contains("token recognition error at: '&'"));
            assertEquals(1, e.getLineNum());
            assertEquals(10, e.getCharPos());
        }
    }

    public void testDivisionByZero1() {
        try {
            vela.expressionToOperand("42.42/0.0");
            fail();
        } catch (VeLaEvalError e) {
            assertEquals(e.getMessage(), "42.42/0.0: division by zero error");
        }
    }

    public void testDivisionByZero2() {
        try {
            vela.expressionToOperand("42/0");
            fail();
        } catch (VeLaEvalError e) {
            assertEquals(e.getMessage(), "42/0: division by zero error");
        }
    }

    public void testGreaterThanOrEqualSwappedCharacters() {
        try {
            vela.program("2 => 3");
            fail();
        } catch (VeLaParseError e) {
            assertTrue(e.getMessage().contains("no viable alternative at input '>'"));
        }
    }

    public void testFunProperlyTailRecursive1() {
        String prog = "";
        prog += "infinite_loop() {";
        prog += "    infinite_loop()";
        prog += "}";
        prog += "infinite_loop()";

        try {
            vela.program(prog);
            fail("Should end in stack overflow, since VeLaInterpreter " + "not yet properly tail recursive.");
        } catch (StackOverflowError e) {
            // We expect to end up here
        }
    }

    public void testFunctionDefinitionOrder() {
        // Current directory must be VStar root.
        File code = new File("test/org/aavso/tools/vstar/vela/code/func-order.vela");
        Optional<Operand> result = vela.program(code);
        assertTrue(result.isPresent());
        assertEquals(8, result.get().intVal());
    }

    public void testUserCode1() {
        // Current directory must be VStar root.
        File code = new File("test/org/aavso/tools/vstar/vela/code/sqr.vela");
        Optional<Operand> result = vela.program(code);
        assertTrue(result.isPresent());
        assertEquals(144.0, result.get().doubleVal());
    }

    public void testUserCode2() {
        List<File> dirs = new ArrayList<File>();
        // Current directory must be VStar root.
        dirs.add(new File("test/org/aavso/tools/vstar/vela/code"));
        VeLaInterpreter vela = new VeLaInterpreter(VERBOSE, ADD_VSTAR_API, dirs);
        Optional<Operand> result = vela.program("cube(2)");
        assertTrue(result.isPresent());
        assertEquals(8.0, result.get().doubleVal());
    }

    // Standard Library Functions: VeLa and intrinsic (Java)
    // Iteration used for speed comparison.

    public void testStdFunVeLaNthRec() {
        List<File> dirs = new ArrayList<File>();
        // Current directory must be VStar root.
        dirs.add(new File("test/org/aavso/tools/vstar/vela/code"));
        VeLaInterpreter vela = new VeLaInterpreter(VERBOSE, ADD_VSTAR_API, dirs);
        Optional<Operand> result = null;
        for (int i = 1; i < 10; i++) {
            result = vela.program("_nthrec(seq(1 100 1) 41)");
        }
        assertTrue(result.isPresent());
        assertEquals(42, result.get().intVal());
    }

    public void testStdFunVeLaNthIter() {
        List<File> dirs = new ArrayList<File>();
        // Current directory must be VStar root.
        dirs.add(new File("test/org/aavso/tools/vstar/vela/code"));
        VeLaInterpreter vela = new VeLaInterpreter(VERBOSE, ADD_VSTAR_API, dirs);
        Optional<Operand> result = null;
        for (int i = 1; i < 10; i++) {
            result = vela.program("_nthiter(seq(1 100 1) 41)");
        }
        assertTrue(result.isPresent());
        assertEquals(42, result.get().intVal());
    }

    public void testStdFunJavaNth() {
        Optional<Operand> result = null;
        for (int i = 1; i < 10; i++) {
            result = vela.program("nth(seq(1 100 1) 41)");
        }
        assertTrue(result.isPresent());
        assertEquals(42, result.get().intVal());
    }

    // Helpers

    /**
     * Given a VeLa program, test that it works in all available locales. We replace
     * the decimal separator "." in prog with the locale-specific separator (e.g.
     * ",") first.
     * 
     * @param prog A string containing arbitrary VeLa code.
     */
    private void commonNumericLocaleTest(String prog, double expected, double tolerance) {
        // see issues #229, #236 re: testNegExponent() failure
        Set<String> localesToIgnore = new HashSet<String>();
        localesToIgnore.add("ar-JO");

        for (Locale locale : Locale.getAvailableLocales()) {
            Locale.setDefault(locale);
            if (localesToIgnore.contains(locale.toLanguageTag())) {
                System.err.printf("** Ignoring locale: %s\n", locale.toLanguageTag());
                continue;
            }
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault());
            char sep = symbols.getDecimalSeparator();
            prog = prog.replace('.', sep);
            Optional<Operand> result = Optional.ofNullable(null);
            try {
                result = vela.program(prog);
                assertTrue(result.isPresent());
                assertTrue(Tolerance.areClose(expected, result.get().doubleVal(), tolerance, true));
            } catch (NumberFormatException e) {
                // allows us to debug a failure more easily via breakpoints
                Operand val = result.get();
                fail(String.format("Number format exception thrown: locale=%s, result=%s", locale.toLanguageTag(),
                        val.toHumanReadableString()));
            }
        }
    }

    private double today() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1; // 0..11 -> 1..12
        int day = cal.get(Calendar.DAY_OF_MONTH);
        return AbstractDateUtil.getInstance().calendarToJD(year, month, day);
    }

    private List<ValidObservation> commonObs() {
        List<ValidObservation> obs = new ArrayList<ValidObservation>();

        ValidObservation ob = new ValidObservation();
        ob.setMagnitude(new Magnitude(12, 0.1));
        ob.setDateInfo(new DateInfo(2457849.1));
        ob.setBand(SeriesType.Visual);
        ob.setObsCode("PEX");
        obs.add(ob);

        ob = new ValidObservation();
        ob.setMagnitude(new Magnitude(12.02, 0.01));
        ob.setDateInfo(new DateInfo(2457849.1));
        ob.setBand(SeriesType.Johnson_V);
        ob.setObsCode("PLA");
        obs.add(ob);

        ob = new ValidObservation();
        ob.setMagnitude(new Magnitude(11, 0.1));
        ob.setDateInfo(new DateInfo(2457849.2));
        ob.setBand(SeriesType.Visual);
        ob.setObsCode("ABC");
        obs.add(ob);

        ob = new ValidObservation();
        ob.setMagnitude(new Magnitude(11.05, 0.02));
        ob.setDateInfo(new DateInfo(2457849.2));
        ob.setBand(SeriesType.Johnson_V);
        ob.setObsCode("XYZ");
        obs.add(ob);

        return obs;
    }

    private List<ValidObservation> filterObs(String velaFilterExpr, List<ValidObservation> obs) {

        VeLaValidObservationEnvironment.reset();

        List<ValidObservation> filteredObs = new ArrayList<ValidObservation>();

        for (ValidObservation ob : obs) {
            vela.pushEnvironment(new VeLaValidObservationEnvironment(ob));

            Optional<Operand> result = vela.program(velaFilterExpr);
            if (result.isPresent() && result.get().booleanVal()) {
                filteredObs.add(ob);
            }

            vela.popEnvironment();
        }

        return filteredObs;
    }

    /**
     * Given an operator string and an integer, return a unary function that takes
     * an integer and returns the result of combining the integer with another using
     * the operator. If the operator is unknown, an assertion is thrown.
     * 
     * @param operator The operator string
     * @param n        The known integer
     * @param reverse  Reverse the operands?
     * @return A function combining n with another integer according to the operator
     */
    private LongUnaryOperator operatorFunc(String operator, int n, boolean reverse) {
        final LongUnaryOperator operatorFunc;

        switch (Operation.getBinaryOp(operator)) {
        case ADD:
            operatorFunc = (m) -> (long) (n + m);
            break;
        case SUB:
            operatorFunc = (m) -> (long) (reverse ? (m - n) : (n - m));
            break;
        case MUL:
            operatorFunc = (m) -> (long) (n * m);
            break;
        case DIV:
            operatorFunc = (m) -> (long) (reverse ? (m / n) : (n / m));
            break;
        case POW:
            operatorFunc = (m) -> {
                long result;
                result = reverse ? m : n;
                if (reverse) {
                    if (n == 0) {
                        result = 1;
                    } else {
                        // multiply operand1 by itself n-1 times
                        for (int i = 1; i <= n - 1; i++) {
                            result *= m;
                        }
                    }
                } else {
                    if (m == 0) {
                        result = 1;
                    } else {
                        // multiply operand1 by itself n-1 times
                        for (int i = 1; i <= m - 1; i++) {
                            result *= n;
                        }
                    }
                }
                return result;
            };
            break;
        case SHL:
            operatorFunc = (m) -> (long) (reverse ? (m << n) : (n << m));
            break;
        case SHR:
            operatorFunc = (m) -> (long) (reverse ? (m >> n) : (n >> m));
            break;
        default:
            // to avoid "may not be initialized" error
            operatorFunc = null;
            String msg = String.format("unknown operator '%s'", operator);
            throw new IllegalArgumentException(msg);
        }

        return operatorFunc;
    }

    /**
     * Given an operator string and a real return a unary function that takes a real
     * and returns the result of combining the real with another using the operator.
     * If the operator is unknown, an assertion is thrown.
     * 
     * @param operator The operator string
     * @param n        The known real
     * @return A function combining n with another real according to the operator
     */
    private DoubleUnaryOperator operatorFunc(String operator, double n) {
        final DoubleUnaryOperator operatorFunc;

        switch (Operation.getBinaryOp(operator)) {
        case ADD:
            operatorFunc = (m) -> n + m;
            break;
        case SUB:
            operatorFunc = (m) -> n - m;
            break;
        case MUL:
            operatorFunc = (m) -> n * m;
            break;
        case DIV:
            operatorFunc = (m) -> n / m;
            break;
        case POW:
            operatorFunc = (m) -> Math.pow(n, m);
            break;
        default:
            // to avoid "may not be initialized" error
            operatorFunc = null;
            String msg = String.format("unknown operator '%s'", operator);
            throw new IllegalArgumentException(msg);
        }

        return operatorFunc;
    }
}
