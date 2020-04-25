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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
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
import org.aavso.tools.vstar.util.date.AbstractDateUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runner.RunWith;

import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;

/**
 * This class contains unit tests for VeLa: VStar expression language.
 */
@RunWith(JUnitQuickcheck.class)
public class VeLaPropertyBasedTest {

	private String testName;

	private final static double DELTA = 0.001;

	private final static boolean VERBOSE = false;

	private VeLaInterpreter vela;

	private long start;

	public VeLaPropertyBasedTest() {
		vela = new VeLaInterpreter(VERBOSE);
	}

	@Rule
	public TestRule watcher = new TestWatcher() {
		protected void starting(Description description) {
			testName = description.getMethodName();
		}
	};

	@Before
	public void start() {
		start = System.currentTimeMillis();
	}

	@After
	public void end() {
		System.out.printf("** %s: %d ms\n", testName,
				System.currentTimeMillis() - start);
	}

	// ** Valid test cases **

	// Real expressions

	@Test
	public void testPositiveReal1() {
		double result = vela.realExpression("12.25");
		assertEquals(12.25, result, DELTA);
	}

	@Test
	public void testPositiveRealNoLeadingZero() {
		double result = vela.realExpression(".25");
		assertEquals(.25, result, DELTA);
	}

	@Test
	public void testNegativeReal1() {
		double result = vela.realExpression("-12.25");
		assertEquals(-12.25, result, DELTA);
	}

	@Test
	public void testNegativeRealNoLeadingZero() {
		double result = vela.realExpression("-.25");
		assertEquals(-.25, result, DELTA);
	}

	// Second property-based test in VStar.
	// Every real number represented as a string should parse and evaluate to
	// that number.
	@Property(trials = 25)
	public void testRealEval(double n) {
		double result = vela.realExpression(String.format("%s", n));
		assertEquals(result, n, DELTA);
	}

	@Test
	public void testAddition() {
		double result = vela.realExpression("2457580.25+1004");
		assertEquals(2458584.25, result, DELTA);
	}

	@Test
	public void testSubtraction() {
		double result = vela.realExpression("2457580.25-1004");
		assertEquals(2456576.25, result, DELTA);
	}

	@Test
	public void testMultiplication() {
		double result = vela.realExpression("2457580.25*10");
		assertEquals(24575802.5, result, DELTA);
	}

	@Test
	public void testDivision() {
		double result = vela.realExpression("2457580.25/10");
		assertEquals(245758.025, result, DELTA);
	}

	@Test
	public void testAddSubMul() {
		double result = vela.realExpression("2457580.25+1004*2-1");
		assertEquals(2459587.25, result, DELTA);
	}

	@Test
	public void testAddSubMulDiv() {
		Operand operand = vela.expressionToOperand("2+3-5*6/2");
		assertEquals(Type.INTEGER, operand.getType());
		assertEquals(-10, operand.intVal());
	}

	@Test
	public void testRealExponentiation1() {
		Operand operand = vela.expressionToOperand("2.0^3.0");
		assertEquals(Type.REAL, operand.getType());
		assertEquals(8.0, operand.doubleVal(), DELTA);
	}

	@Test
	public void testRealExponentiation2() {
		Operand operand = vela.expressionToOperand("2^3.0");
		assertEquals(Type.REAL, operand.getType());
		assertEquals(8.0, operand.doubleVal(), DELTA);
	}

	@Test
	public void testRealExponentiation3() {
		Operand operand = vela.expressionToOperand("3.0^4^2");
		assertEquals(Type.REAL, operand.getType());
		assertEquals(43046721.0, operand.doubleVal(), DELTA);
	}

	@Test
	public void testReal1() {
		double result = vela.realExpression("2.25+1");
		assertEquals(3.25, result, DELTA);
	}

	@Test
	public void testReal2() {
		double result = vela.realExpression("2.25-1");
		assertEquals(1.25, result, DELTA);
	}

	@Test
	public void testReal3() {
		double result = vela.realExpression("2.25+1+2");
		assertEquals(5.25, result, DELTA);
	}

	@Test
	public void testReal4() {
		double result = vela.realExpression("2.25*2*2");
		assertEquals(9.0, result, DELTA);
	}

	@Test
	public void testReal5() {
		double result = vela.realExpression("1 - 6 / 2 + 4 * 5");
		assertEquals(18.0, result, DELTA);
	}

	@Test
	public void testReal6() {
		double result = new VeLaInterpreter(VERBOSE)
				.realExpression("1 + 6 / 2 + 4 * 5");
		assertEquals(24.0, result, DELTA);
	}

	@Test
	public void testReal7() {
		double result = new VeLaInterpreter(VERBOSE)
				.realExpression("1 + 6 / 2 - 4 * 5");
		assertEquals(-16.0, result, DELTA);
	}

	@Test
	public void testParens0() {
		double result = vela.realExpression("(2457580.25+1004)*10");
		assertEquals(24585842.50, result, DELTA);
	}

	@Test
	public void testParens1() {
		double result = vela.realExpression("(2457580.25+1004-2)*10");
		assertEquals(24585822.50, result, DELTA);
	}

	@Test
	public void testParens2() {
		// 20 years before some JD.
		double result = vela.realExpression("2457580.25-(365.25*20)");
		assertEquals(2450275.25, result, DELTA);
	}

	@Test
	public void testParens3() {
		double result = vela.realExpression("(12.25*-2)");
		assertEquals(-24.5, result, DELTA);
	}

	@Test
	public void testResultCacheTest1() {
		// 20 years before some JD.
		double result = vela.realExpression("2457580.25-(365.25*20)");
		assertEquals(2450275.25, result, DELTA);
	}

	// Integer expressions

	@Test
	public void testIntegerExponentiation1() {
		Operand operand = vela.expressionToOperand("2^3");
		assertEquals(Type.INTEGER, operand.getType());
		assertEquals(8, operand.intVal());
	}

	@Test
	public void testIntegerExponentiation2() {
		Operand operand = vela.expressionToOperand("3^4^2");
		assertEquals(Type.INTEGER, operand.getType());
		assertEquals(43046721, operand.intVal());
	}

	@Test
	public void testIntegerExponentiation3() {
		Operand operand = vela.expressionToOperand("-3^4^2");
		assertEquals(Type.INTEGER, operand.getType());
		assertEquals(-43046721, operand.intVal());
	}

	@Test
	public void testIntegerExponentiation4() {
		Operand operand = vela.expressionToOperand("-(3^4)^2");
		assertEquals(Type.INTEGER, operand.getType());
		assertEquals(-6561, operand.intVal());
	}

	@Test
	public void testIntegerExponentiation5() {
		Operand operand = vela.expressionToOperand("(-3^4)^2");
		assertEquals(Type.INTEGER, operand.getType());
		assertEquals(6561, operand.intVal());
	}

	@Test
	public void testIntegerExponentiation6() {
		Operand operand = vela.expressionToOperand("3^4+2");
		assertEquals(Type.INTEGER, operand.getType());
		assertEquals(83, operand.intVal());
	}

	@Property(trials = 25)
	public void testAdditionEval(int n, int m) {
		String expr = String.format("%d+%d", n, m);
		Operand result = vela.expressionToOperand(expr);
		assertEquals(n+m, result.intVal(), DELTA);
	}

	// String expressions

	// Note tests suggest the importance of using expressionToOperand() and
	// checking the type where a particular type is expected/required.

	@Test
	public void testString1() {
		Operand operand = vela.expressionToOperand("\"foobar\"");
		assertEquals(Type.STRING, operand.getType());
		assertEquals("foobar", operand.stringVal());
	}

	@Test
	public void testString2() {
		Operand operand = vela.expressionToOperand("concat(\"foo\" \"bar\")");
		assertEquals(Type.STRING, operand.getType());
		assertEquals("foobar", operand.stringVal());
	}

	@Test
	public void testFormat() {
		String prog = "";
		prog += "s <- format(\"%d\n\" [42])";
		prog += "s";
		Optional<Operand> result = vela.program(prog);
		assertEquals("42\n", result.get().stringVal());
	}

	@Test
	public void testChr1() {
		String prog = "chr(65)";
		Optional<Operand> result = vela.program(prog);
		assertEquals("A", result.get().stringVal());
	}

	@Test
	public void testChr2() {
		String prog = "chr(-1)";
		Optional<Operand> result = vela.program(prog);
		assertEquals("", result.get().stringVal());
	}

	@Test
	public void testOrd() {
		String prog = "ord(\"A\")";
		Optional<Operand> result = vela.program(prog);
		assertEquals(65, result.get().intVal());
	}

	// Boolean expressions

	@Test
	public void testTrue() {
		assertTrue(vela.booleanExpression("true"));
		assertTrue(vela.booleanExpression("true"));
	}

	@Test
	public void testFalse() {
		assertFalse(vela.booleanExpression("false"));
		assertFalse(vela.booleanExpression("false"));
	}

	// Relational expressions

	@Test
	public void testRealEquality() {
		boolean result = vela.booleanExpression("42 = 42");
		assertTrue(result);
	}

	@Test
	public void testRealInequality() {
		boolean result = vela.booleanExpression("42 <> 4.2");
		assertTrue(result);
	}

	@Test
	public void testRealGreaterThan() {
		boolean result = vela.booleanExpression("42 > 4.2");
		assertTrue(result);
	}

	@Test
	public void testRealLessThan() {
		boolean result = vela.booleanExpression("4.22 < 42");
		assertTrue(result);
	}

	@Test
	public void testRealGreaterThanOrEqual1() {
		boolean result = vela.booleanExpression("42 >= 4.2");
		assertTrue(result);
	}

	@Test
	public void testRealGreaterThanOrEqual2() {
		boolean result = vela.booleanExpression("42 >= 42");
		assertTrue(result);
	}

	@Test
	public void testRealLessThanOrEqual1() {
		boolean result = vela.booleanExpression("4.2 <= 42");
		assertTrue(result);
	}

	@Test
	public void testRealLessThanOrEqual2() {
		boolean result = vela.booleanExpression("42 <= 42");
		assertTrue(result);
	}

	@Test
	public void testRealAdditionAndEquality() {
		boolean result = vela.booleanExpression("1+2 = 3");
		assertTrue(result);
	}

	@Test
	public void testStringEquality1() {
		boolean result = vela.booleanExpression("\"foo\" <> \"foobar\"");
		assertTrue(result);
	}

	@Test
	public void testStringAdditionAndEquality2() {
		boolean result = vela.booleanExpression("\"foo\"+\"bar\" = \"foobar\"");
		assertTrue(result);
	}

	@Test
	public void testRegularExpression1() {
		boolean result = vela.booleanExpression("\"Johnson V\" =~ \".+V\"");
		assertTrue(result);
	}

	@Test
	public void testRegularExpression2() {
		boolean result = vela
				.booleanExpression("not(\"Johnson B\" =~ \".+V\")");
		assertTrue(result);
	}

	@Test
	public void testRegularExpression3() {
		boolean result = vela.booleanExpression("\"12.345\" =~ \".+\\d+\"");
		assertTrue(result);
	}

	@Test
	public void testRegularExpressionWithRealConvertedToString() {
		boolean result = vela.booleanExpression("12.345 =~ \".+\\d+\"");
		assertTrue(result);
	}

	// Logical connective expressions

	@Test
	public void testDisjunction1() {
		boolean result = vela.booleanExpression("2 < 3 or 2 > 3");
		assertTrue(result);
	}

	@Test
	public void testConjunction1() {
		boolean result = vela.booleanExpression("2 < 3 and 3 < 5");
		assertTrue(result);
	}

	@Test
	public void testConjunction2() {
		boolean result = vela.booleanExpression("2 < 3 and 2 > 3");
		assertFalse(result);
	}

	@Test
	public void testGroupedBooleanExpression1() {
		boolean result = vela.booleanExpression("(2 < 3 and 3 < 5)");
		assertTrue(result);
	}

	@Test
	public void testGroupedBooleanExpression2() {
		boolean result = vela.booleanExpression("(2 < 3) and (3 < 5)");
		assertTrue(result);
	}

	@Test
	public void testGroupedBooleanExpression3() {
		boolean result = vela.booleanExpression("3 > 2 and (2 < 3 and 2 > 3)");
		assertFalse(result);
	}

	@Test
	public void testGroupedBooleanExpression4() {
		boolean result = vela.booleanExpression("3 > 2 or (2 < 3 and 2 > 3)");
		assertTrue(result);
	}

	@Test
	public void testGroupedBooleanExpression5() {
		boolean result = vela.booleanExpression("3 > 2 or 2 < 3 and 2 > 3");
		assertTrue(result);
	}

	@Test
	public void testLogicalNegationExpression1() {
		boolean result = vela.booleanExpression("not 3 > 2");
		assertFalse(result);
	}

	@Test
	public void testLogicalNegationExpression2() {
		Map<String, Operand> env = new HashMap<String, Operand>();
		env.put("raining".toUpperCase(), new Operand(Type.BOOLEAN, false));
		Set<String> boundConstants = new HashSet<String>();
		boundConstants.add("raining");
		VeLaInterpreter vela = new VeLaInterpreter(VERBOSE);
		vela.pushEnvironment(new VeLaEnvironment<Operand>(env, boundConstants));
		boolean result = vela.booleanExpression("not raining");
		assertTrue(result);
	}

	// Variables

	@Test
	public void testVariableMeaningOfLife() {
		Map<String, Operand> environment = new HashMap<String, Operand>();
		environment.put("meaning_of_life".toUpperCase(), new Operand(
				Type.INTEGER, 42));
		Set<String> consts = new HashSet<String>();
		consts.add("meaning_of_life");
		VeLaInterpreter vela = new VeLaInterpreter(VERBOSE);
		vela.pushEnvironment(new VeLaEnvironment<Operand>(environment, consts));
		boolean result = vela.booleanExpression("meaning_of_life = 42");
		assertTrue(result);
	}

	// This test is important for nested or recursive function calls
	@Test
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

	@Test
	public void testVariableMultipleEnvironmentsOnStack() {
		Map<String, Operand> environment = new HashMap<String, Operand>();
		environment.put("x".toUpperCase(), new Operand(Type.INTEGER, 42));
		Set<String> consts = new HashSet<String>();
		consts.add("x");
		VeLaInterpreter vela = new VeLaInterpreter(VERBOSE);
		vela.pushEnvironment(new VeLaEnvironment<Operand>(environment, consts));
		boolean result = vela.booleanExpression("x = 42");
		assertTrue(result);
	}

	// While

	@Test
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

	@Test
	public void testListCaching() {
		Operand result1 = vela.expressionToOperand("[1 \"2\" 3.0]");
		assertEquals(result1.getType(), Type.LIST);
		assertEquals(result1.listVal(), Arrays.asList(new Operand(Type.INTEGER,
				1), new Operand(Type.STRING, "2"), new Operand(Type.REAL, 3.0)));

		// This one should be cached...
		Operand result2 = vela.expressionToOperand("[1 \"2\" 3.0]");
		assertEquals(result2.getType(), Type.LIST);
		assertEquals(result2.listVal(), Arrays.asList(new Operand(Type.INTEGER,
				1), new Operand(Type.STRING, "2"), new Operand(Type.REAL, 3.0)));
	}

	@Test
	public void testEmptyList() {
		Operand result = vela.expressionToOperand("[]");
		assertEquals(result.getType(), Type.LIST);
		assertTrue(result.listVal().isEmpty());
	}

	@Test
	public void testInListOperator() {
		assertTrue(vela.booleanExpression("2 in [1 2 3]"));
		// cached
		assertTrue(vela.booleanExpression("2 in [1 2 3]"));

		assertTrue(vela.booleanExpression("\"2\" in [1 \"2\" 3]"));

		assertTrue(vela.booleanExpression("3.0 in [1 \"2\" 3.0]"));
		assertTrue(vela.booleanExpression("3.0 IN [1 \"2\" 3.0]"));

		assertTrue(vela.booleanExpression("[2] in [1 [2] 3]"));
	}

	@Test
	public void testInListOperatorNot() {
		assertFalse(vela.booleanExpression("4 in [1 2 3]"));
	}

	@Test
	public void testInStringOperator() {
		assertTrue(vela.booleanExpression("2 in \"123\""));
		// cached
		assertTrue(vela.booleanExpression("2 in \"123\""));
	}

	@Test
	public void testHeterogenousList() {
		assertTrue(vela.booleanExpression("42 in [1 2.0 \"foo\" 42]"));
	}

	@Test
	public void testInList1() {
		assertTrue(vela.booleanExpression("3 in [1 2 3 4]"));
	}

	@Test
	public void testNestedList() {
		assertTrue(vela.booleanExpression("[3 4] in [1 2 [3 4]]"));
	}

	// Selection

	@Test
	public void testSelection() {
		String prog = "when\n3 > 2 -> 42.42\ntrue -> 21.21";

		Optional<Operand> result = vela.program(prog);

		if (result.isPresent()) {
			assertEquals(42.42, result.get().doubleVal(), DELTA);
		} else {
			fail();
		}
	}

	@Test
	public void testSelectionNested() {
		String prog = "";
		prog += "when\n";
		prog += "  3 < 2 -> 42.42\n";
		prog += "  true ->\n";
		prog += "     when 42 > 42 -> 42\n";
		prog += "          true -> 42.0*2";

		Optional<Operand> result = vela.program(prog);

		if (result.isPresent()) {
			assertEquals(84.0, result.get().doubleVal(), DELTA);
		} else {
			fail();
		}
	}

	// Functions

	@Test
	public void testFuncParameterless1() {
		double result = vela.realExpression("today()");
		assertEquals(today(), result, DELTA);
	}

	@Test
	public void testFuncParameterlessAsSubexpression1() {
		double result = vela.realExpression("today()+2");
		assertEquals(today() + 2, result, DELTA);
	}

	@Test
	public void testFunctionSin() {
		double result = vela.realExpression("sin(pi/2)");
		assertEquals(1.0, result, DELTA);
	}

	@Test
	public void testFunctionSqrt() {
		double result = vela.realExpression("2*sqrt(144.0)");
		assertEquals(24.0, result, DELTA);
	}

	// List head

	@Test
	public void testListHead1() {
		String expr = "head([\"first\" 2 \"3rd\"])";
		Operand result = vela.expressionToOperand(expr);
		assertEquals("first", result.stringVal());
	}

	@Test
	public void testListHead2() {
		String expr = "head([])";
		Operand result = vela.expressionToOperand(expr);
		assertEquals(Operand.EMPTY_LIST, result);
	}

	@Test
	public void testListHead3() {
		String expr = "head([[\"first\" 2] \"3rd\"])";
		Operand actual = vela.expressionToOperand(expr);
		Operand expected = vela.expressionToOperand("[\"first\" 2]");
		assertEquals(expected, actual);
	}

	// List nth

	@Test
	public void testListNth1() {
		String expr = "nth([\"first\" 2 \"3rd\"] 1)";
		Operand actual = vela.expressionToOperand(expr);
		Operand expected = vela.expressionToOperand("2");
		assertEquals(expected, actual);
	}

	@Test
	public void testListNth2() {
		String expr = "nth([] 42)";
		Operand result = vela.expressionToOperand(expr);
		assertEquals(Operand.EMPTY_LIST, result);
	}

	@Test
	public void testListNth3() {
		String expr = "nth([[\"first\" 2] \"3rd\"] 0)";
		Operand actual = vela.expressionToOperand(expr);
		Operand expected = vela.expressionToOperand("[\"first\" 2]");
		assertEquals(expected, actual);
	}

	// List tail

	@Test
	public void testListTail1() {
		String expr = "tail([\"first\" 2 \"3rd\"])";
		Operand actual = vela.expressionToOperand(expr);
		Operand expected = vela.expressionToOperand("[2 \"3rd\"]");
		assertEquals(expected, actual);
	}

	@Test
	public void testListTail2() {
		String expr = "tail([])";
		Operand result = vela.expressionToOperand(expr);
		assertEquals(Operand.EMPTY_LIST, result);
	}

	@Test
	public void testListTail3() {
		String expr = "tail([[\"first\" 2] \"3rd\"])";
		Operand actual = vela.expressionToOperand(expr);
		Operand expected = vela.expressionToOperand("[\"3rd\"]");
		assertEquals(expected, actual);
	}

	// Eval

	@Test
	public void testEval1() {
		String prog = "eval(\"2+3\")";
		Optional<Operand> result = vela.program(prog);
		assertTrue(result.isPresent());
		assertEquals(Type.LIST, result.get().getType());
		assertEquals(1, result.get().listVal().size());
		assertEquals(5, result.get().listVal().get(0).intVal());
	}

	// List length

	@Test
	public void testListLength1() {
		String expr = "length([\"first\" 2 \"3rd\"])";
		Operand actual = vela.expressionToOperand(expr);
		assertEquals(3, actual.intVal());
	}

	@Test
	public void testListLength2() {
		String expr = "length([])";
		Operand result = vela.expressionToOperand(expr);
		assertEquals(0, result.intVal());
	}

	// List concatenation

	@Test
	public void testListConcat1() {
		String expr = "concat([\"first\" 2 \"3rd\"] [4 5])";
		Operand actual = vela.expressionToOperand(expr);
		Operand expected = vela
				.expressionToOperand("[\"first\" 2 \"3rd\" 4 5]");
		assertEquals(expected, actual);
	}

	@Test
	public void testListConcat2() {
		String expr = "concat([] [])";
		Operand result = vela.expressionToOperand(expr);
		assertEquals(Operand.EMPTY_LIST, result);
		expr = "concat([\"first\" 2 \"3rd\"] [4 5])";
		result = vela.expressionToOperand(expr);
	}

	// List append

	@Test
	public void testListAppend1() {
		String expr = "append([\"first\" 2 \"3rd\"] [4 5])";
		Operand actual = vela.expressionToOperand(expr);
		Operand expected = vela
				.expressionToOperand("[\"first\" 2 \"3rd\" [4 5]]");
		assertEquals(expected, actual);
	}

	@Test
	public void testListAppend2() {
		String expr = "append([\"first\" 2 \"3rd\"] \"4\")";
		Operand actual = vela.expressionToOperand(expr);
		Operand expected = vela
				.expressionToOperand("[\"first\" 2 \"3rd\" \"4\"]");
		assertEquals(expected, actual);
	}

	@Test
	public void testListAppend3() {
		String expr = "append([\"first\" 2 \"3rd\"] 42)";
		Operand actual = vela.expressionToOperand(expr);
		Operand expected = vela.expressionToOperand("[\"first\" 2 \"3rd\" 42]");
		assertEquals(expected, actual);
	}

	@Test
	public void testListAppend4() {
		String expr = "append([\"first\" 2 \"3rd\"] 4.2)";
		Operand actual = vela.expressionToOperand(expr);
		Operand expected = vela
				.expressionToOperand("[\"first\" 2 \"3rd\" 4.2]");
		assertEquals(expected, actual);
	}

	@Test
	public void testListAppend5() {
		String expr = "append([\"first\" 2 \"3rd\"] true)";
		Operand actual = vela.expressionToOperand(expr);
		Operand expected = vela
				.expressionToOperand("[\"first\" 2 \"3rd\" true]");
		assertEquals(expected, actual);
	}

	@Test
	public void testIntegerSeq() {
		String prog = "seq(1 5 1)";

		Optional<Operand> result = vela.program(prog);

		assertTrue(result.isPresent());

		List<Operand> expected = Arrays.asList(new Operand(Type.INTEGER, 1),
				new Operand(Type.INTEGER, 2), new Operand(Type.INTEGER, 3),
				new Operand(Type.INTEGER, 4), new Operand(Type.INTEGER, 5));

		assertEquals(expected, result.get().listVal());
	}

	@Test
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

	@Test
	public void testRealSeq() {
		String prog = "seq(1.0 5.0 1.0)";

		Optional<Operand> result = vela.program(prog);

		assertTrue(result.isPresent());

		List<Operand> expected = Arrays.asList(new Operand(Type.REAL, 1.0),
				new Operand(Type.REAL, 2.0), new Operand(Type.REAL, 3.0),
				new Operand(Type.REAL, 4.0), new Operand(Type.REAL, 5.0));

		assertEquals(expected, result.get().listVal());
	}

	// Intrinsic string functions (from String class)

	@Test
	public void testFunctionContains() {
		boolean result = vela
				.booleanExpression("contains(\"xyz123abc\" \"23a\")");
		assertTrue(result);
	}

	@Test
	public void testFunctionEndsWith() {
		assertTrue(vela.booleanExpression("endsWith(\"12345\" \"45\")"));
	}

	@Test
	public void testFunctionMatches() {
		assertTrue(vela.booleanExpression("matches(\"12345\" \"^\\d{3}45$\")"));
	}

	@Test
	public void testFunctionReplace() {
		assertTrue(vela
				.booleanExpression("replace(\"abcd\" \"bc\" \"BC\") = \"aBCd\""));
	}

	@Test
	public void testFunctionConcat() {
		assertTrue(vela
				.booleanExpression("concat(\"abcd\" \"ef\") = \"abcdef\""));
	}

	@Test
	public void testLastIndexOf() {
		Operand operand = vela
				.expressionToOperand("lastIndexOf(\"dabcde\" \"d\")");
		assertEquals(Type.INTEGER, operand.getType());
		assertEquals(4, operand.intVal());
	}

	// User defined functions

	@Test
	public void testNamedFunSquare() {
		String prog = "";
		prog += "f(x:integer y:integer) : integer { x^y }\n";
		prog += "x <- f(12 2)\n";
		prog += "x";

		Optional<Operand> result = vela.program(prog);

		assertTrue(result.isPresent());
		assertEquals(144, result.get().intVal());
	}

	@Test
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

	@Test
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
	@Test
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

	@Test
	public void testAnonFunExponentiation() {
		String prog = "function(x:integer y:integer) : integer { x^y }(12 2)";

		Optional<Operand> result = vela.program(prog);

		assertTrue(result.isPresent());
		assertEquals(144, result.get().intVal());
	}

	@Test
	public void testAnonFunExponentiationWithReturnTypeConversion() {
		// Returned value should be coerced from integer to real.
		String prog = "function(x:integer y:integer) : real { x^y }(12 2)";

		Optional<Operand> result = vela.program(prog);

		assertTrue(result.isPresent());
		assertEquals(144.0, result.get().doubleVal(), DELTA);
	}

	@Test
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

	@Test
	public void testBoundFun() {
		String prog = "";
		prog += "fact(n:integer) : integer {";
		prog += "    when";
		prog += "      n <= 0 -> 1";
		prog += "      true -> n*fact(n-1)";
		// prog += "    n*n";
		prog += "}";
		prog += "f <- fact\n";
		prog += "fact(6)";

		Optional<Operand> result = vela.program(prog);

		assertTrue(result.isPresent());
		assertEquals(720, result.get().intVal());
	}

	@Test
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

	@Test
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

	@Test
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

	@Test
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

	@Test
	public void testFunReduceReal() {
		String prog = "";
		prog += "prod(x:real y:real) : real {";
		prog += "    x*y";
		prog += "}";
		// Factorial via reduce
		prog += "reduce(prod [5 4 3 2 1] 1)";

		Optional<Operand> result = vela.program(prog);

		assertTrue(result.isPresent());
		assertEquals(120.0, result.get().doubleVal(), DELTA);
	}

	@Test
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

	@Test
	public void testFunFor2() {
		String prog = "";
		prog += "cubeplus1(n:integer) {\n";
		prog += "    print(n^3+1 \"\n\"\n)";
		prog += "}\n";

		prog += "nums <- [2 4 6 8]\n";
		prog += "for(cubeplus1 nums)\n";

		vela.program(prog);
	}

	@Test
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
		assertTrue(areClose(12.34620932, result.get().doubleVal(), 1e-6));
	}

	@Test
	public void testMean() {
		String prog = "";
		prog += "mean(vals:list) : real {";
		prog += "  reduce(function(n:real m:real) : real { n+m } vals 0) / length(vals)";
		prog += "}";
		prog += "mags is [3.678 3.776 3.866 3.943 4 4.062 4.117 4.089 3.883 3.651 3.653]";
		prog += "mean(mags)";

		Optional<Operand> result = vela.program(prog);
		assertTrue(result.isPresent());
		assertTrue(areClose(3.8834545, result.get().doubleVal(), 1e-7));
	}

	// Bindings

	@Test
	public void testBindingNonConstant() {
		// Bind X to 42 then retrieve the bound value of X.
		String prog = "";
		prog += "x <- 12\n";
		prog += "y <- x*x\n";
		prog += "print(\"x squared is \" y \"\n\"\n)";
		prog += "x";

		Optional<Operand> result = vela.program(prog);

		assertTrue(result.isPresent());
		assertEquals(12, result.get().intVal());

		// Bind X again.
		result = vela.program("x <- x + 1  x");
		assertTrue(result.isPresent());
		assertEquals(13, result.get().intVal());
	}

	@Test
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

	@Test
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

	@Test
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

	@Test
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

	@Test
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

	@Test
	public void testFormattedPrint() {
		String prog = "";
		prog += "print(format(\"%d\n\" [42]))";
		vela.program(prog);
	}

	@Test
	public void testFormattedPrintln() {
		String prog = "";
		prog += "println(format(\"%d\" [42]))";
		vela.program(prog);
	}

	// Filter test cases

	@Test
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

	@Test
	public void testComments1() {
		double result = vela.realExpression("-- comment test\n\r12+2");
		assertEquals(14.0, result, DELTA);
	}

	@Test
	public void testComments2() {
		double result = vela.realExpression("-- comment test\r\n12+2");
		assertEquals(14.0, result, DELTA);
	}

	@Test
	public void testComments3() {
		double result = vela.realExpression("-- comment test\n12+2");
		assertEquals(14.0, result, DELTA);
	}

	@Test
	public void testComments4() {
		Optional<Operand> operand = vela.program("-- comment test");
		assertFalse(operand.isPresent());
	}

	// ** Error cases **

	@Test
	public void testAmpersand() {
		try {
			vela.realExpression("2457580.25&1004");
			fail();
		} catch (VeLaParseError e) {
			assertEquals("token recognition error at: '&'", e.getMessage());
			assertEquals(1, e.getLineNum());
			assertEquals(10, e.getCharPos());
		}
	}

	@Test
	public void testDivisionByZero1() {
		try {
			vela.realExpression("42.42/0.0");
			fail();
		} catch (VeLaEvalError e) {
			assertEquals(e.getMessage(), "42.42/0.0: division by zero error");
		}
	}

	@Test
	public void testDivisionByZero2() {
		try {
			vela.realExpression("42/0");
			fail();
		} catch (VeLaEvalError e) {
			assertEquals(e.getMessage(), "42/0: division by zero error");
		}
	}

	@Test
	public void testGreaterThanOrEqualSwappedCharacters() {
		try {
			vela.booleanExpression("2 => 3");
			fail();
		} catch (VeLaParseError e) {
			assertTrue(e.getMessage().contains("extraneous input '>'"));
		}
	}

	@Test
	public void testFunProperlyTailRecursive1() {
		String prog = "";
		prog += "infinite_loop() {";
		prog += "    infinite_loop()";
		prog += "}";
		prog += "infinite_loop()";

		try {
			vela.program(prog);
			fail("Should end in stack overflow, since VeLaInterpreter "
					+ "not yet properly tail recursive.");
		} catch (StackOverflowError e) {
			// We expect to end up here
		}
	}

	@Test
	public void testFunctionDefinitionOrder() {
		// Current directory must be VStar root.
		File code = new File(
				"test/org/aavso/tools/vstar/vela/code/func-order.vela");
		Optional<Operand> result = vela.program(code);
		assertTrue(result.isPresent());
		assertEquals(8, result.get().intVal());
	}

	@Test
	public void testUserCode1() {
		// Current directory must be VStar root.
		File code = new File("test/org/aavso/tools/vstar/vela/code/sqr.vela");
		Optional<Operand> result = vela.program(code);
		assertTrue(result.isPresent());
		assertEquals(144, result.get().intVal());
	}

	@Test
	public void testUserCode2() {
		List<File> dirs = new ArrayList<File>();
		// Current directory must be VStar root.
		dirs.add(new File("test/org/aavso/tools/vstar/vela/code"));
		VeLaInterpreter vela = new VeLaInterpreter(VERBOSE, dirs);
		Optional<Operand> result = vela.program("cube(2)");
		assertTrue(result.isPresent());
		assertEquals(8, result.get().intVal());
	}

	// Helpers

	private boolean areClose(double a, double b, double epsilon) {
		return Math.abs(a - b) < epsilon;
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

	private List<ValidObservation> filterObs(String velaFilterExpr,
			List<ValidObservation> obs) {

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
