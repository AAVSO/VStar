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
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

	public VeLaTest(String name) {
		super(name);
	}

	// ** Valid test cases **

	// Real expressions

	public void testPositiveReal1() {
		VeLaInterpreter vela = new VeLaInterpreter();
		double result = vela.realExpression("12.25");
		assertEquals(12.25, result);
	}

	public void testPositiveRealNoLeadingZero() {
		VeLaInterpreter vela = new VeLaInterpreter();
		double result = vela.realExpression(".25");
		assertEquals(.25, result);
	}

	public void testNegativeReal1() {
		VeLaInterpreter vela = new VeLaInterpreter();
		double result = vela.realExpression("-12.25");
		assertEquals(-12.25, result);
	}

	public void testNegativeRealNoLeadingZero() {
		VeLaInterpreter vela = new VeLaInterpreter();
		double result = vela.realExpression("-.25");
		assertEquals(-.25, result);
	}

	public void testAddition() {
		VeLaInterpreter vela = new VeLaInterpreter();
		double result = vela.realExpression("2457580.25+1004");
		assertEquals(2458584.25, result);
	}

	public void testSubtraction() {
		VeLaInterpreter vela = new VeLaInterpreter();
		double result = vela.realExpression("2457580.25-1004");
		assertEquals(2456576.25, result);
	}

	public void testMultiplication() {
		VeLaInterpreter vela = new VeLaInterpreter();
		double result = vela.realExpression("2457580.25*10");
		assertEquals(24575802.5, result);
	}

	public void testDivision() {
		VeLaInterpreter vela = new VeLaInterpreter();
		double result = vela.realExpression("2457580.25/10");
		assertEquals(245758.025, result);
	}

	public void testAddSubMulDiv1() {
		VeLaInterpreter vela = new VeLaInterpreter();
		double result = vela.realExpression("2457580.25+1004*2-1");
		assertEquals(2459587.25, result);
	}

	public void testAddSubMulDiv2() {
		VeLaInterpreter vela = new VeLaInterpreter();
		double result = vela.realExpression("2+3-5*6");
		assertEquals(-25.0, result);
	}

	public void testParens1() {
		VeLaInterpreter vela = new VeLaInterpreter();
		double result = vela.realExpression("(2457580.25+1004-2)*10");
		assertEquals(24585822.50, result);
	}

	public void testParens2() {
		VeLaInterpreter vela = new VeLaInterpreter();
		// 20 years before some JD.
		double result = vela.realExpression("2457580.25-(365.25*20)");
		assertEquals(2450275.25, result);
	}

	public void testParens3() {
		VeLaInterpreter vela = new VeLaInterpreter();
		double result = vela.realExpression("-(12.25*-2)");
		assertEquals(24.5, result);
	}

	public void testResultCacheTest1() {
		VeLaInterpreter vela = new VeLaInterpreter();
		// 20 years before some JD.
		double result = vela.realExpression("2457580.25-(365.25*20)");
		assertEquals(2450275.25, result);
	}

	public void testFuncParameterless1() {
		VeLaInterpreter vela = new VeLaInterpreter();
		double result = vela.realExpression("today");
		assertEquals(today(), result);
	}

	public void testFuncParameterless2() {
		VeLaInterpreter vela = new VeLaInterpreter();
		double result = vela.realExpression("today");
		assertEquals(today(), result);
	}

	public void testFuncParameterlessAsSubexpression1() {
		VeLaInterpreter vela = new VeLaInterpreter();
		double result = vela.realExpression("today+2");
		assertEquals(today() + 2, result);
	}

	public void testFuncParameterlessAsSubexpression() {
		VeLaInterpreter vela = new VeLaInterpreter();
		double result = vela.realExpression("today+2");
		assertEquals(today() + 2, result);
	}

	// Relational expressions

	public void testRealEquality() {
		VeLaInterpreter vela = new VeLaInterpreter();
		boolean result = vela.booleanExpression("42 = 42");
		assertTrue(result);
	}

	public void testRealInequality() {
		VeLaInterpreter vela = new VeLaInterpreter();
		boolean result = vela.booleanExpression("42 <> 4.2");
		assertTrue(result);
	}

	public void testRealGreaterThan() {
		VeLaInterpreter vela = new VeLaInterpreter();
		boolean result = vela.booleanExpression("42 > 4.2");
		assertTrue(result);
	}

	public void testRealLessThan() {
		VeLaInterpreter vela = new VeLaInterpreter();
		boolean result = vela.booleanExpression("4.22 < 42");
		assertTrue(result);
	}

	public void testRealGreaterThanOrEqual1() {
		VeLaInterpreter vela = new VeLaInterpreter();
		boolean result = vela.booleanExpression("42 >= 4.2");
		assertTrue(result);
	}

	public void testRealGreaterThanOrEqual2() {
		VeLaInterpreter vela = new VeLaInterpreter();
		boolean result = vela.booleanExpression("42 >= 42");
		assertTrue(result);
	}

	public void testRealLessThanOrEqual1() {
		VeLaInterpreter vela = new VeLaInterpreter();
		boolean result = vela.booleanExpression("4.2 <= 42");
		assertTrue(result);
	}

	public void testRealLessThanOrEqual2() {
		VeLaInterpreter vela = new VeLaInterpreter();
		boolean result = vela.booleanExpression("42 <= 42");
		assertTrue(result);
	}

	public void testRealAdditionAndEquality() {
		VeLaInterpreter vela = new VeLaInterpreter();
		boolean result = vela.booleanExpression("1+2 = 3");
		assertTrue(result);
	}

	public void testStringEquality1() {
		VeLaInterpreter vela = new VeLaInterpreter();
		boolean result = vela.booleanExpression("\"foo\" <> \"foobar\"");
		assertTrue(result);
	}

	public void testStringAdditionAndEquality2() {
		VeLaInterpreter vela = new VeLaInterpreter();
		boolean result = vela.booleanExpression("\"foo\"+\"bar\" = \"foobar\"");
		assertTrue(result);
	}

	public void testRegularExpression1() {
		VeLaInterpreter vela = new VeLaInterpreter();
		boolean result = vela.booleanExpression("\"Johnson V\" =~ \".+V\"");
		assertTrue(result);
	}

	public void testRegularExpression2() {
		VeLaInterpreter vela = new VeLaInterpreter();
		boolean result = vela.booleanExpression("not(\"Johnson B\" =~ \".+V\")");
		assertTrue(result);
	}

	public void testRegularExpression3() {
		VeLaInterpreter vela = new VeLaInterpreter();
		boolean result = vela.booleanExpression("\"12.345\" =~ \".+\\d+\"");
		assertTrue(result);
	}

	public void testRegularExpressionWithRealConvertedToString() {
		VeLaInterpreter vela = new VeLaInterpreter();
		boolean result = vela.booleanExpression("12.345 =~ \".+\\d+\"");
		assertTrue(result);
	}

	// Logical connective expressions

	public void testDisjunction1() {
		VeLaInterpreter vela = new VeLaInterpreter();
		boolean result = vela.booleanExpression("2 < 3 or 2 > 3", true);
		assertTrue(result);
	}

	public void testConjunction1() {
		VeLaInterpreter vela = new VeLaInterpreter();
		boolean result = vela.booleanExpression("2 < 3 and 3 < 5", true);
		assertTrue(result);
	}

	public void testConjunction2() {
		VeLaInterpreter vela = new VeLaInterpreter();
		boolean result = vela.booleanExpression("2 < 3 and 2 > 3", true);
		assertFalse(result);
	}

	public void testGroupedBooleanExpression1() {
		VeLaInterpreter vela = new VeLaInterpreter();
		boolean result = vela.booleanExpression("(2 < 3 and 3 < 5)", true);
		assertTrue(result);
	}

	public void testGroupedBooleanExpression2() {
		VeLaInterpreter vela = new VeLaInterpreter();
		boolean result = vela.booleanExpression("(2 < 3) and (3 < 5)", true);
		assertTrue(result);
	}

	public void testGroupedBooleanExpression3() {
		VeLaInterpreter vela = new VeLaInterpreter();
		boolean result = vela.booleanExpression("3 > 2 and (2 < 3 and 2 > 3)",
				true);
		assertFalse(result);
	}

	public void testGroupedBooleanExpression4() {
		VeLaInterpreter vela = new VeLaInterpreter();
		boolean result = vela.booleanExpression("3 > 2 or (2 < 3 and 2 > 3)",
				true);
		assertTrue(result);
	}

	public void testGroupedBooleanExpression5() {
		VeLaInterpreter vela = new VeLaInterpreter();
		boolean result = vela.booleanExpression("3 > 2 or 2 < 3 and 2 > 3",
				true);
		assertTrue(result);
	}

	public void testLogicalNegationExpression1() {
		VeLaInterpreter vela = new VeLaInterpreter();
		boolean result = vela.booleanExpression("not 3 > 2", true);
		assertFalse(result);
	}

	public void testLogicalNegationExpression2() {
		Map<String, Operand> env = new HashMap<String, Operand>();
		env.put("raining".toUpperCase(), new Operand(Type.BOOLEAN, false));
		VeLaInterpreter vela = new VeLaInterpreter(new VeLaMapEnvironment(env));
		boolean result = vela.booleanExpression("not raining", true);
		assertTrue(result);
	}

	// Variables

	public void testVariableMeaningOfLife() {
		Map<String, Operand> environment = new HashMap<String, Operand>();
		environment.put("meaning_of_life".toUpperCase(), new Operand(
				Type.DOUBLE, 42));
		VeLaInterpreter vela = new VeLaInterpreter(new VeLaMapEnvironment(
				environment));
		boolean result = vela.booleanExpression("meaning_of_life = 42");
		assertTrue(result);
	}

	public void _testVariableSingleCharacterVariable() {
		Map<String, Operand> environment = new HashMap<String, Operand>();
		environment.put("x".toUpperCase(), new Operand(Type.DOUBLE, 42));
		VeLaInterpreter vela = new VeLaInterpreter(new VeLaMapEnvironment(
				environment));
		boolean result = vela.booleanExpression("x = 42");
		assertTrue(result);
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
	}

	// ** Error cases **

	public void testAmpersand() {
		try {
			VeLaInterpreter vela = new VeLaInterpreter();
			vela.realExpression("2457580.25&1004");
			fail();
		} catch (VeLaParseError e) {
			assertEquals("token recognition error at: '&'", e.getMessage());
			assertEquals(1, e.getLineNum());
			assertEquals(10, e.getCharPos());
		}
	}

	public void testDivisionByZero() {
		VeLaInterpreter vela = new VeLaInterpreter();
		try {
			vela.realExpression("42/0");
			fail();
		} catch (VeLaEvalError e) {
			assertEquals(e.getMessage(), "42.0/0.0: division by zero error");
		}
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
		obs.add(ob);

		ob = new ValidObservation();
		ob.setMagnitude(new Magnitude(12.02, 0.01));
		ob.setDateInfo(new DateInfo(2457849.1));
		ob.setBand(SeriesType.Johnson_V);
		obs.add(ob);

		ob = new ValidObservation();
		ob.setMagnitude(new Magnitude(11, 0.1));
		ob.setDateInfo(new DateInfo(2457849.2));
		ob.setBand(SeriesType.Visual);
		obs.add(ob);

		ob = new ValidObservation();
		ob.setMagnitude(new Magnitude(11.05, 0.02));
		ob.setDateInfo(new DateInfo(2457849.2));
		ob.setBand(SeriesType.Johnson_V);
		obs.add(ob);

		return obs;
	}

	private List<ValidObservation> filterObs(String velaFilterExpr,
			List<ValidObservation> obs) {

		VeLaValidObservationEnvironment.reset();

		VeLaInterpreter vela = new VeLaInterpreter();

		List<ValidObservation> filteredObs = new ArrayList<ValidObservation>();

		for (ValidObservation ob : obs) {
			vela.setEnvironment(new VeLaValidObservationEnvironment(ob));

			if (vela.booleanExpression(velaFilterExpr, true)) {
				filteredObs.add(ob);
			}
		}

		return filteredObs;
	}
}
