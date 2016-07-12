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

import junit.framework.TestCase;

/**
 * This class contains unit tests for VeLa: VStar expression language.
 */
public class VeLaTest extends TestCase {

	public VeLaTest(String name) {
		super(name);
	}

	// Valid test cases

	public void testPositiveReal1() {
		VeLaInterpreter vela = new VeLaInterpreter();
		double result = vela.realExpression("12.25");
		assertEquals(12.25, result);
	}

	public void testParsePositiveRealNoLeadingZero() {
		VeLaInterpreter vela = new VeLaInterpreter();
		double result = vela.realExpression(".25");
		assertEquals(.25, result);
	}

	public void testParseNegativeRealNoLeadingZero() {
		VeLaInterpreter vela = new VeLaInterpreter();
		double result = vela.realExpression("-.25");
		assertEquals(-.25, result);
	}

	public void testParseAddition() {
		VeLaInterpreter vela = new VeLaInterpreter();
		double result = vela.realExpression("2457580.25+1004");
		assertEquals(2458584.25, result);
	}

	public void testParseSubtraction() {
		VeLaInterpreter vela = new VeLaInterpreter();
		double result = vela.realExpression("2457580.25-1004");
		assertEquals(2456576.25, result);
	}

	public void testParseMultiplication() {
		VeLaInterpreter vela = new VeLaInterpreter();
		double result = vela.realExpression("2457580.25*10");
		assertEquals(24575802.5, result);
	}

	public void testParseDivision() {
		VeLaInterpreter vela = new VeLaInterpreter();
		double result = vela.realExpression("2457580.25/10");
		assertEquals(245758.025, result);
	}

	// Invalid test cases

	public void testParseAmpersand() {
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
}
