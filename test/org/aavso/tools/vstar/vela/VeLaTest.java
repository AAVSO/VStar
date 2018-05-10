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
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import junit.framework.TestCase;

import org.aavso.tools.vstar.data.DateInfo;
import org.aavso.tools.vstar.data.Magnitude;
import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.util.date.AbstractDateUtil;

/**
 * This class contains unit tests for VeLa: VStar expression language.
 */
public class VeLaTest extends TestCase {

	private VeLaInterpreter vela;

	public VeLaTest(String name) {
		super(name);
		vela = new VeLaInterpreter(true);
	}

	// ** Valid test cases **

	// Real expressions

	public void testPositiveReal1() {
		double result = vela.realExpression("12.25");
		assertEquals(12.25, result);
	}

	public void testPositiveRealNoLeadingZero() {
		double result = vela.realExpression(".25");
		assertEquals(.25, result);
	}

	public void testNegativeReal1() {
		double result = vela.realExpression("-12.25");
		assertEquals(-12.25, result);
	}

	public void testNegativeRealNoLeadingZero() {
		double result = vela.realExpression("-.25");
		assertEquals(-.25, result);
	}

	public void testAddition() {
		double result = vela.realExpression("2457580.25+1004");
		assertEquals(2458584.25, result);
	}

	public void testSubtraction() {
		double result = vela.realExpression("2457580.25-1004");
		assertEquals(2456576.25, result);
	}

	public void testMultiplication() {
		double result = vela.realExpression("2457580.25*10");
		assertEquals(24575802.5, result);
	}

	public void testDivision() {
		double result = vela.realExpression("2457580.25/10");
		assertEquals(245758.025, result);
	}

	public void testAddSubMul() {
		double result = vela.realExpression("2457580.25+1004*2-1");
		assertEquals(2459587.25, result);
	}

	public void testAddSubMulDiv() {
		Operand operand = vela.expressionToOperand("2+3-5*6/2");
		assertEquals(Type.INTEGER, operand.getType());
		assertEquals(-10, operand.intVal());
	}

	public void testRealExponentiation1() {
		Operand operand = vela.expressionToOperand("2.0^3.0");
		assertEquals(Type.REAL, operand.getType());
		assertEquals(8.0, operand.doubleVal());
	}

	public void testRealExponentiation2() {
		Operand operand = vela.expressionToOperand("2^3.0");
		assertEquals(Type.REAL, operand.getType());
		assertEquals(8.0, operand.doubleVal());
	}

	public void testRealExponentiation3() {
		Operand operand = vela.expressionToOperand("3.0^4^2");
		assertEquals(Type.REAL, operand.getType());
		assertEquals(43046721.0, operand.doubleVal());
	}

	public void testReal1() {
		double result = vela.realExpression("2.25+1");
		assertEquals(3.25, result);
	}

	public void testReal2() {
		double result = vela.realExpression("2.25-1");
		assertEquals(1.25, result);
	}

	public void testReal3() {
		double result = vela.realExpression("2.25+1+2");
		assertEquals(5.25, result);
	}

	public void testReal4() {
		double result = vela.realExpression("2.25*2*2");
		assertEquals(9.0, result);
	}

	public void testReal5() {
		double result = vela.realExpression("1 - 6 / 2 + 4 * 5");
		assertEquals(18.0, result);
	}

	public void testReal6() {
		double result = new VeLaInterpreter(false)
				.realExpression("1 + 6 / 2 + 4 * 5");
		assertEquals(24.0, result);
	}

	public void testReal7() {
		double result = new VeLaInterpreter(false)
				.realExpression("1 + 6 / 2 - 4 * 5");
		assertEquals(-16.0, result);
	}

	public void testParens0() {
		double result = vela.realExpression("(2457580.25+1004)*10");
		assertEquals(24585842.50, result);
	}

	public void testParens1() {
		double result = vela.realExpression("(2457580.25+1004-2)*10");
		assertEquals(24585822.50, result);
	}

	public void testParens2() {
		// 20 years before some JD.
		double result = vela.realExpression("2457580.25-(365.25*20)");
		assertEquals(2450275.25, result);
	}

	public void testParens3() {
		double result = vela.realExpression("(12.25*-2)");
		assertEquals(-24.5, result);
	}

	public void testResultCacheTest1() {
		// 20 years before some JD.
		double result = vela.realExpression("2457580.25-(365.25*20)");
		assertEquals(2450275.25, result);
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
		boolean result = vela
				.booleanExpression("not(\"Johnson B\" =~ \".+V\")");
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
		VeLaInterpreter vela = new VeLaInterpreter(true);
		vela.pushEnvironment(new VeLaEnvironment<Operand>(env));
		boolean result = vela.booleanExpression("not raining");
		assertTrue(result);
	}

	// Variables

	public void testVariableMeaningOfLife() {
		Map<String, Operand> environment = new HashMap<String, Operand>();
		environment.put("meaning_of_life".toUpperCase(), new Operand(
				Type.INTEGER, 42));
		VeLaInterpreter vela = new VeLaInterpreter(true);
		vela.pushEnvironment(new VeLaEnvironment<Operand>(environment));
		boolean result = vela.booleanExpression("meaning_of_life = 42");
		assertTrue(result);
	}

	// This test is important for nested or recursive function calls
	public void testVariableSingleCharacterVariable() {
		Map<String, Operand> environment1 = new HashMap<String, Operand>();
		environment1.put("x".toUpperCase(), new Operand(Type.INTEGER, 4.2));
		vela.pushEnvironment(new VeLaEnvironment<Operand>(environment1));

		Map<String, Operand> environment2 = new HashMap<String, Operand>();
		environment2.put("x".toUpperCase(), new Operand(Type.INTEGER, 42));
		vela.pushEnvironment(new VeLaEnvironment<Operand>(environment2));

		// The value of x bound to 42 should be on top of the stack.
		boolean result = vela.booleanExpression("x = 42");
		assertTrue(result);
	}

	public void testVariableMultipleEnvironmentsOnStack() {
		Map<String, Operand> environment = new HashMap<String, Operand>();
		environment.put("x".toUpperCase(), new Operand(Type.INTEGER, 42));
		VeLaInterpreter vela = new VeLaInterpreter(true);
		vela.pushEnvironment(new VeLaEnvironment<Operand>(environment));
		boolean result = vela.booleanExpression("x = 42");
		assertTrue(result);
	}

	// List

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
			assertEquals(42.42, result.get().doubleVal());
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
			assertEquals(84.0, result.get().doubleVal());
		} else {
			fail();
		}
	}

	// Functions

	public void testFuncParameterless1() {
		double result = vela.realExpression("today()");
		assertEquals(today(), result);
	}

	public void testFuncParameterlessAsSubexpression1() {
		double result = vela.realExpression("today()+2");
		assertEquals(today() + 2, result);
	}

	public void testFunctionSin() {
		double result = vela.realExpression("sin(pi/2)");
		assertEquals(1.0, result);
	}

	public void testFunctionSqrt() {
		double result = vela.realExpression("2*sqrt(144.0)");
		assertEquals(24.0, result);
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
		Operand expected = vela
				.expressionToOperand("[\"first\" 2 \"3rd\" 4 5]");
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
		Operand expected = vela
				.expressionToOperand("[\"first\" 2 \"3rd\" [4 5]]");
		assertEquals(expected, actual);
	}

	public void testListAppend2() {
		String expr = "append([\"first\" 2 \"3rd\"] \"4\")";
		Operand actual = vela.expressionToOperand(expr);
		Operand expected = vela
				.expressionToOperand("[\"first\" 2 \"3rd\" \"4\"]");
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
		Operand expected = vela
				.expressionToOperand("[\"first\" 2 \"3rd\" 4.2]");
		assertEquals(expected, actual);
	}

	public void testListAppend5() {
		String expr = "append([\"first\" 2 \"3rd\"] true)";
		Operand actual = vela.expressionToOperand(expr);
		Operand expected = vela
				.expressionToOperand("[\"first\" 2 \"3rd\" true]");
		assertEquals(expected, actual);
	}

	// Intrinsic string functions (from String class)

	public void testFunctionContains() {
		boolean result = vela
				.booleanExpression("contains(\"xyz123abc\" \"23a\")");
		assertTrue(result);
	}

	public void testFunctionEndsWith() {
		assertTrue(vela.booleanExpression("endsWith(\"12345\" \"45\")"));
	}

	public void testFunctionMatches() {
		assertTrue(vela.booleanExpression("matches(\"12345\" \"^\\d{3}45$\")"));
	}

	public void testFunctionReplace() {
		assertTrue(vela
				.booleanExpression("replace(\"abcd\" \"bc\" \"BC\") = \"aBCd\""));
	}

	public void testFunctionConcat() {
		assertTrue(vela
				.booleanExpression("concat(\"abcd\" \"ef\") = \"abcdef\""));
	}

	public void testLastIndexOf() {
		Operand operand = vela
				.expressionToOperand("lastIndexOf(\"dabcde\" \"d\")");
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
		// TODO: whitespace is significant in parameter lists
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
		assertEquals(144.0, result.get().doubleVal());
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
		// prog += "    n*n";
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
		assertEquals(120.0, result.get().doubleVal());
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

		Optional<Operand> result = new VeLaInterpreter(true).program(prog);

		assertTrue(result.isPresent());
		assertTrue(areClose(12.34620932, result.get().doubleVal(), 1e-6));
	}

	// Bindings

	public void testBinding1() {
		// Bind X to 42 then retrieve the bound value of X.
		String prog = "";
		prog += "x <- 12\n";
		prog += "y <- x*x\n";
		prog += "print(\"x squared is \" y \"\n\"\n)";
		prog += "x";

		Optional<Operand> result = vela.program(prog);

		assertTrue(result.isPresent());
		assertEquals(12, result.get().intVal());

		// Attempt to bind X again.
		try {
			vela.program("x <- 1");
			fail();
		} catch (Exception e) {
			// It's an error to bind the same variable twice in the current
			// scope, so we should end up here.
		}
	}

	// I/O test cases

	public void testFormat() {
		String prog = "";
		prog += "s <- format(\"%d\n\" [42])";
		prog += "s";
		Optional<Operand> result = vela.program(prog);
		assertEquals("42\n", result.get().stringVal());
	}

	// By inspection...
	
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
		assertEquals(14.0, result);
	}

	public void testComments2() {
		double result = vela.realExpression("-- comment test\r\n12+2");
		assertEquals(14.0, result);
	}

	public void testComments3() {
		double result = vela.realExpression("-- comment test\n12+2");
		assertEquals(14.0, result);
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
			assertEquals("token recognition error at: '&'", e.getMessage());
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

	public void testFunProperlyTailRecursive1() {
		String prog = "";
		prog += "infinite_loop() {";
		prog += "    infinite_loop()";
		prog += "}";
		prog += "infinite_loop()";

		try {
			vela.program(prog);
		} catch (StackOverflowError e) {
			// We expect to end up here
		}
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
