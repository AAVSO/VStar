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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.aavso.tools.vstar.data.DateInfo;
import org.aavso.tools.vstar.data.Magnitude;
import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.util.Tolerance;
import org.aavso.tools.vstar.util.date.AbstractDateUtil;

import junit.framework.TestCase;

/**
 * This class contains unit tests for VeLa: VStar expression language.
 */
public class VeLaTest extends TestCase {

	private final static double DELTA = 0.001;

	private final static boolean VERBOSE = false;

	private final static boolean ADD_VSTAR_API = false;

	private VeLaInterpreter vela;

	public VeLaTest(String name) {
		super(name);
		vela = new VeLaInterpreter(VERBOSE, ADD_VSTAR_API, Collections.emptyList());
	}

	// ** Valid test cases **

	// Real expressions

	public void testPositiveReal1() {
		double result = vela.realExpression("12.25");
		assertTrue(Tolerance.areClose(12.25, result, DELTA, true));
	}

	public void testPositiveRealNoLeadingZero() {
		double result = vela.realExpression(".25");
		assertTrue(Tolerance.areClose(.25, result, DELTA, true));
	}

	public void testNegativeReal1() {
		double result = vela.realExpression("-12.25");
		assertTrue(Tolerance.areClose(-12.25, result, DELTA, true));
	}

	public void testNegativeRealNoLeadingZero() {
		double result = vela.realExpression("-.25");
		assertTrue(Tolerance.areClose(-.25, result, DELTA, true));
	}

	public void testAddition() {
		double result = vela.realExpression("2457580.25+1004");
		assertTrue(Tolerance.areClose(2458584.25, result, DELTA, true));
	}

	public void testSubtraction() {
		double result = vela.realExpression("2457580.25-1004");
		assertTrue(Tolerance.areClose(2456576.25, result, DELTA, true));
	}

	public void testMultiplication() {
		double result = vela.realExpression("2457580.25*10");
		assertTrue(Tolerance.areClose(24575802.5, result, DELTA, true));
	}

	public void testDivision() {
		double result = vela.realExpression("2457580.25/10");
		assertTrue(Tolerance.areClose(245758.025, result, DELTA, true));
	}

	public void testAddSubMul() {
		double result = vela.realExpression("2457580.25+1004*2-1");
		assertTrue(Tolerance.areClose(2459587.25, result, DELTA, true));
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

	public void testReal1() {
		double result = vela.realExpression("2.25+1");
		assertTrue(Tolerance.areClose(3.25, result, DELTA, true));
	}

	public void testReal2() {
		double result = vela.realExpression("2.25-1");
		assertTrue(Tolerance.areClose(1.25, result, DELTA, true));
	}

	public void testReal3() {
		double result = vela.realExpression("2.25+1+2");
		assertTrue(Tolerance.areClose(5.25, result, DELTA, true));
	}

	public void testReal4() {
		double result = vela.realExpression("2.25*2*2");
		assertTrue(Tolerance.areClose(9.0, result, DELTA, true));
	}

	public void testReal5() {
		double result = vela.realExpression("1 - 6 / 2 + 4 * 5");
		assertTrue(Tolerance.areClose(18.0, result, DELTA, true));
	}

	public void testReal6() {
		double result = new VeLaInterpreter(VERBOSE).realExpression("1 + 6 / 2 + 4 * 5");
		assertTrue(Tolerance.areClose(24.0, result, DELTA, true));
	}

	public void testReal7() {
		double result = new VeLaInterpreter(VERBOSE).realExpression("1 + 6 / 2 - 4 * 5");
		assertTrue(Tolerance.areClose(-16.0, result, DELTA, true));
	}

	public void testParens0() {
		double result = vela.realExpression("(2457580.25+1004)*10");
		assertTrue(Tolerance.areClose(24585842.50, result, DELTA, true));
	}

	public void testParens1() {
		double result = vela.realExpression("(2457580.25+1004-2)*10");
		assertTrue(Tolerance.areClose(24585822.50, result, DELTA, true));
	}

	public void testParens2() {
		// 20 years before some JD.
		double result = vela.realExpression("2457580.25-(365.25*20)");
		assertTrue(Tolerance.areClose(2450275.25, result, DELTA, true));
	}

	public void testParens3() {
		double result = vela.realExpression("(12.25*-2)");
		assertTrue(Tolerance.areClose(-24.5, result, DELTA, true));
	}

	public void testResultCacheTest1() {
		// 20 years before some JD.
		double result = vela.realExpression("2457580.25-(365.25*20)");
		assertTrue(Tolerance.areClose(2450275.25, result, DELTA, true));
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
		prog += "s <- format(\"%d\n\" [42])";
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
		VeLaInterpreter vela = new VeLaInterpreter(VERBOSE, ADD_VSTAR_API, Collections.emptyList());
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
		VeLaInterpreter vela = new VeLaInterpreter(VERBOSE, ADD_VSTAR_API, Collections.emptyList());
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
		VeLaInterpreter vela = new VeLaInterpreter(VERBOSE, ADD_VSTAR_API, Collections.emptyList());
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

	public void testSelection() {
		String prog = "when\n3 > 2 -> 42.42\ntrue -> 21.21";

		Optional<Operand> result = vela.program(prog);

		if (result.isPresent()) {
			assertTrue(Tolerance.areClose(42.42, result.get().doubleVal(), DELTA, true));
		} else {
			fail();
		}
	}

	public void testSelectionNested() {
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

	// Functions

	public void testFuncParameterless1() {
		double result = vela.realExpression("today()");
		assertTrue(Tolerance.areClose(today(), result, DELTA, true));
	}

	public void testFuncParameterlessAsSubexpression1() {
		double result = vela.realExpression("today()+2");
		assertTrue(Tolerance.areClose(today() + 2, result, DELTA, true));
	}

	public void testFunctionSin() {
		double result = vela.realExpression("sin(pi/2)");
		assertTrue(Tolerance.areClose(1.0, result, DELTA, true));
	}

	public void testFunctionSqrt() {
		double result = vela.realExpression("2*sqrt(144.0)");
		assertTrue(Tolerance.areClose(24.0, result, DELTA, true));
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
		boolean result = vela.booleanExpression("contains(\"xyz123abc\" \"23a\")");
		assertTrue(result);
	}

	public void testFunctionEndsWith() {
		assertTrue(vela.booleanExpression("endsWith(\"12345\" \"45\")"));
	}

	public void testFunctionMatches() {
		assertTrue(vela.booleanExpression("matches(\"12345\" \"^\\d{3}45$\")"));
	}

	public void testFunctionReplace() {
		assertTrue(vela.booleanExpression("replace(\"abcd\" \"bc\" \"BC\") = \"aBCd\""));
	}

	public void testFunctionConcat() {
		assertTrue(vela.booleanExpression("concat(\"abcd\" \"ef\") = \"abcdef\""));
	}

	public void testLastIndexOf() {
		Operand operand = vela.expressionToOperand("lastIndexOf(\"dabcde\" \"d\")");
		assertEquals(Type.INTEGER, operand.getType());
		assertEquals(4, operand.intVal());
	}

	// User defined functions

	public void testNamedFunSquare() {
		String prog = "";
		prog += "f(x:integer y:integer) : integer { x^y }\n";
		prog += "x <- f(12 2)\n";
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
		prog += "x <- f(12)";
		prog += "x";

		Optional<Operand> result = vela.program(prog);

		assertTrue(result.isPresent());
		assertEquals(144, result.get().intVal());
	}

	public void testNamedFunRecursiveLoop() {
		String prog = "";
		prog += "loop(n:integer) {";
		prog += "    print(n \"^2 = \" n*n \"\n\")";
		prog += "    when n < 10 -> loop(n+1)";
		prog += "}";
		prog += "loop(1)";

		vela.program(prog);
	}

	// Turing Completeness attained: Dec 20 2018, 18:55 :)

	public void testNamedFunRecursiveFactorial() {
		String prog = "";
		prog += "fact(n:integer) : integer {";
		prog += "    when";
		prog += "      n <= 0 -> 1";
		prog += "      true -> n*fact(n-1)";
		prog += "}";
		prog += "x <- fact(6)";
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
		prog += "    x <- g(h(n))";
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

	public void testBoundFun() {
		String prog = "";
		prog += "fact(n:integer) : integer {";
		prog += "    when";
		prog += "      n <= 0 -> 1";
		prog += "      true -> n*fact(n-1)";
		// prog += " n*n";
		prog += "}";
		prog += "f <- fact\n";
		prog += "fact(6)";

		Optional<Operand> result = vela.program(prog);

		assertTrue(result.isPresent());
		assertEquals(720, result.get().intVal());
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
		prog += "    result <- append(result n^3+1)\n";
		prog += "}\n";

		prog += "nums <- [2 4 6 8]\n";
		prog += "result <- []\n";
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

		prog += "nums <- [2 4 6 8]\n";
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

		Optional<Operand> result = new VeLaInterpreter(VERBOSE).program(prog);

		assertTrue(result.isPresent());
		assertTrue(Tolerance.areClose(12.34620932, result.get().doubleVal(), 1e-6, true));
	}

	public void testMean() {
		String prog = "";
		prog += "mean(vals:list) : real {";
		prog += "  reduce(function(n:real m:real) : real { n+m } vals 0) / length(vals)";
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
		String prog = "x <- 2\n";

		vela.program(prog);

		// Attempt to bind X again but to a value of
		// type string, not integer. This should fail.
		try {
			vela.program("x <- \"2\"");
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
			vela.program("x <- x + 1");
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
			vela.program("x <- x + 1");
			fail();
		} catch (Exception e) {
			// This is where we expect to be.
		}
	}

	public void testBindingConstant3() {
		// Bind X to 42 then retrieve the bound value of X.
		String prog = "";
		prog += "x <- 12\n";
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
		prog += "  count <- start\n";
		prog += "  counter(n:integer) : integer { count <- count + n  count }\n";
		prog += "  counter\n";
		prog += "}\n";
		prog += "c <- mkcounter(10)\n";
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
		prog += "ch <- \"1\"";
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

	public void testVeLaBooleanExpressionsAsFilters() {
		List<ValidObservation> obs = commonObs();
		String expr;

		expr = "uncertainty >= 0.1";
		assertEquals(2, filterObs(expr, obs).size());

		expr = "uncertainty > 0.01 and uncertainty < 0.03";
		assertEquals(1, filterObs(expr, obs).size());

		expr = "magnitude > 12 and (uncertainty > 0 and uncertainty <= 0.01)";
		assertEquals(1, filterObs(expr, obs).size());

		expr = "obscode = \"PEX\"";
		assertEquals(1, filterObs(expr, obs).size());

		expr = "mag > 6 and mag < 15 and obscode = \"PEX\"";
		assertEquals(1, filterObs(expr, obs).size());

		expr = "mag > 6 and mag < 15 and obscode in [\"PEX\" \"PLA\"]";
		assertEquals(2, filterObs(expr, obs).size());
	}

	// Comments

	public void testComments1() {
		double result = vela.realExpression("-- comment test\n\r12+2");
		assertTrue(Tolerance.areClose(14.0, result, DELTA, true));
	}

	public void testComments2() {
		double result = vela.realExpression("-- comment test\r\n12+2");
		assertTrue(Tolerance.areClose(14.0, result, DELTA, true));
	}

	public void testComments3() {
		double result = vela.realExpression("-- comment test\n12+2");
		assertTrue(Tolerance.areClose(14.0, result, DELTA, true));
	}

	public void testComments4() {
		Optional<Operand> operand = vela.program("-- comment test");
		assertFalse(operand.isPresent());
	}

	// ** Error cases **

	public void testAmpersand() {
		try {
			vela.realExpression("2457580.25&1004");
			fail();
		} catch (VeLaParseError e) {
			assertTrue(e.getMessage().contains("token recognition error at: '&'"));
			assertEquals(1, e.getLineNum());
			assertEquals(10, e.getCharPos());
		}
	}

	public void testDivisionByZero1() {
		try {
			vela.realExpression("42.42/0.0");
			fail();
		} catch (VeLaEvalError e) {
			assertEquals(e.getMessage(), "42.42/0.0: division by zero error");
		}
	}

	public void testDivisionByZero2() {
		try {
			vela.realExpression("42/0");
			fail();
		} catch (VeLaEvalError e) {
			assertEquals(e.getMessage(), "42/0: division by zero error");
		}
	}

	public void testGreaterThanOrEqualSwappedCharacters() {
		try {
			vela.booleanExpression("2 => 3");
			fail();
		} catch (VeLaParseError e) {
			assertTrue(e.getMessage().contains("extraneous input '>'"));
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
		assertEquals(144, result.get().intVal());
	}

	public void testUserCode2() {
		List<File> dirs = new ArrayList<File>();
		// Current directory must be VStar root.
		dirs.add(new File("test/org/aavso/tools/vstar/vela/code"));
		VeLaInterpreter vela = new VeLaInterpreter(VERBOSE, ADD_VSTAR_API, dirs);
		Optional<Operand> result = vela.program("cube(2)");
		assertTrue(result.isPresent());
		assertEquals(8, result.get().intVal());
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
		VeLaInterpreter vela = new VeLaInterpreter(VERBOSE, ADD_VSTAR_API, Collections.emptyList());
		Optional<Operand> result = null;
		for (int i = 1; i < 10; i++) {
			result = vela.program("nth(seq(1 100 1) 41)");
		}
		assertTrue(result.isPresent());
		assertEquals(42, result.get().intVal());
	}

	// Helpers

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
}
