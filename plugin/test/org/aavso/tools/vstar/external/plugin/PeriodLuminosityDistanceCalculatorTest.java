/**
 * VStar: a statistical analysis tool for variable star data.
 * Copyright (C) 2026  AAVSO (http://www.aavso.org/)
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
package org.aavso.tools.vstar.external.plugin;

import junit.framework.TestCase;

/**
 * Unit tests for Period-Luminosity distance formulas (Turner DCEP PL and
 * distance modulus) used by {@link PeriodLuminosityDistanceCalculator}.
 */
public class PeriodLuminosityDistanceCalculatorTest extends TestCase {

	public PeriodLuminosityDistanceCalculatorTest(String name) {
		super(name);
	}

	public void testAbsMagForPeriodOneDay() {
		assertEquals(-1.29,
				PeriodLuminosityDistanceCalculator.calcAbsMagForDCEPType(1.0),
				1e-12);
	}

	public void testAbsMagForPeriodTenDays() {
		assertEquals(-4.07,
				PeriodLuminosityDistanceCalculator.calcAbsMagForDCEPType(10.0),
				1e-12);
	}

	public void testDistanceWhenApparentEqualsAbsoluteIsTenParsecs() {
		assertEquals(10.0,
				PeriodLuminosityDistanceCalculator.calcDistance(5.0, 5.0),
				1e-12);
	}

	public void testDistanceModulusCanonicalSteps() {
		assertEquals(100.0,
				PeriodLuminosityDistanceCalculator.calcDistance(10.0, 5.0),
				1e-12);
		assertEquals(1000.0,
				PeriodLuminosityDistanceCalculator.calcDistance(15.0, 5.0),
				1e-12);
	}

	public void testEndToEndPeriodTenApparentFive() {
		double absMag = PeriodLuminosityDistanceCalculator
				.calcAbsMagForDCEPType(10.0);
		double expectedPc = Math.pow(10, (5.0 - absMag + 5) / 5);
		double distancePc = PeriodLuminosityDistanceCalculator
				.calcDistance(5.0, absMag);
		assertEquals(-4.07, absMag, 1e-12);
		assertEquals(expectedPc, distancePc, 1e-12);
		assertEquals(expectedPc * 3.26,
				PeriodLuminosityDistanceCalculator
						.parsecsToLightYears(distancePc),
				1e-12);
	}
}
