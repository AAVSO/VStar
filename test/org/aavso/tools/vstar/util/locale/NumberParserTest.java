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
package org.aavso.tools.vstar.util.locale;

import java.util.Locale;

import junit.framework.TestCase;

/**
 * NumberParser test cases for English locale.
 */
public class NumberParserTest extends TestCase {

	// Valid tests

	public void testParsePositiveRealNoFractionalComponent() {
		commonValidTest(1000, "1000");
	}

	public void testParseExplicitPositiveRealNoFractionalComponent() {
		commonValidTest(1000, "+1000");
	}

	public void testParseNegativeRealNoFractionalComponent() {
		commonValidTest(-1000, "-1000");
	}

	public void testParseExplicitNegtiveRealNoFractionalComponent() {
		commonValidTest(-1000, "-1000");
	}

	public void testParsePositiveReal1() {
		commonValidTest(12.25, "12.25");
	}

	public void testParsePositiveReal2() {
		commonValidTest(+12.25, "+12.25");
	}

	public void testParseNegativeReal1() {
		commonValidTest(-12.25, "-12.25");
	}

	public void testParsePositiveRealNoLeadingZero() {
		commonValidTest(.25, ".25");
	}

	public void testParseExplicitPositiveRealNoLeadingZero() {
		commonValidTest(.25, "+.25");
	}

	public void testParseNegativeRealNoLeadingZero() {
		commonValidTest(-.25, "-.25");
	}

	public void testParsePositiveRealLeadingZero() {
		commonValidTest(0.25, "0.25");
	}

	public void testParseNegativeRealLeadingZero() {
		commonValidTest(-0.25, "-0.25");
	}

	public void testParseExponentialFormat1() {
		commonValidTest(225, "2.25E2");
	}

	public void testParseExponentialFormat2() {
		commonValidTest(225.0, "2.25e2");
	}

	public void testParseExponentialFormat3() {
		commonValidTest(225.0, "2.25E2");
	}

	public void testParseExponentialFormat4() {
		commonValidTest(0.0225, "2.25e-2");
	}

	public void testParseExponentialFormat5() {
		commonValidTest(-0.0225, "-2.25e-2");
	}

	public void testParseExponentialFormat6() {
		commonValidTest(0.0225, "+2.25e-2");
	}

	public void testParseExponentialFormat7() {
		commonValidTest(400, "4E2");
	}

	public void testParseExponentialFormat8() {
		commonValidTest(400, "+4e2");
	}

	public void testParseExponentialFormat9() {
		commonValidTest(-400, "-4e2");
	}

	public void testParseExponentialFormat10() {
		commonValidTest(4, "400e-2");
	}

	public void testParsePositiveRealNonEnglishLocale() {
		Locale.setDefault(new Locale("de", "DE"));
		commonValidTest(12.25, "12,25");
		commonValidTest(12.25, "12.25"); // this also works!
		commonValidTest(-0.0225, "-2,25e-2");
		commonValidTest(225.0, "2,25E2");
		commonValidTest(400, "+4e2");
		Locale.setDefault(new Locale("en", "US"));
	}

	private void commonValidTest(double expected, String actual) {
		assertEquals(expected, NumberParser.parseDouble(actual));
	}

	// Invalid tests

	public void testParseExponentialFormatWithPlus1() {
		try {
			NumberParser.parseDouble("2.25E+2");
			fail(); // TODO: parse appears to yield 2.25 (i.e. stops at E)
		} catch (Exception e) {
		}
	}

	public void testParseExponentialFormatWithPlus2() {
		try {
			NumberParser.parseDouble("2.25e+2");
			fail(); // TODO: parse appears to yield 2.25 (i.e. stops at e)
		} catch (Exception e) {
		}
	}
}
