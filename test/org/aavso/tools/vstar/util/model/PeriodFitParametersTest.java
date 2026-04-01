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
package org.aavso.tools.vstar.util.model;

import junit.framework.TestCase;

import org.aavso.tools.vstar.util.Tolerance;

public class PeriodFitParametersTest extends TestCase {

	public PeriodFitParametersTest(String name) {
		super(name);
	}

	public void testGettersFromHarmonic() {
		Harmonic h = new Harmonic(0.05, 2);
		PeriodFitParameters p = new PeriodFitParameters(h, 1.2, 3.0, -4.0, 0.5, 2450000.0);
		assertSame(h, p.getHarmonic());
		assertEquals(0.05, p.getFrequency());
		assertEquals(2, p.getHarmonicNumber());
		assertEquals(20.0, p.getPeriod());
		assertEquals(1.2, p.getAmplitude());
		assertEquals(3.0, p.getCosineCoefficient());
		assertEquals(-4.0, p.getSineCoefficient());
		assertEquals(0.5, p.getConstantCoefficient());
		assertEquals(2450000.0, p.getZeroPointOffset());
	}

	public void testPhaseFromAtan2() {
		PeriodFitParameters p = new PeriodFitParameters(new Harmonic(0.1), 1.0, 1.0, 0.0, 0.0, 0.0);
		assertTrue(Tolerance.areClose(0.0, p.getPhase(), 1e-6, true));
		PeriodFitParameters q = new PeriodFitParameters(new Harmonic(0.1), 1.0, 0.0, 1.0, 0.0, 0.0);
		assertTrue(Tolerance.areClose(-Math.PI / 2.0, q.getPhase(), 1e-9, true));
	}

	public void testGetRelativeAmplitude() {
		PeriodFitParameters p = new PeriodFitParameters(new Harmonic(0.1), 2.0, 1.0, 0.0, 0.0, 0.0);
		assertEquals(0.5, p.getRelativeAmplitude(4.0));
	}

	public void testGetRelativePhaseWrapsToRange() {
		Harmonic h2 = new Harmonic(0.2, 2);
		PeriodFitParameters p = new PeriodFitParameters(h2, 1.0, 1.0, 0.0, 0.0, 0.0);
		double rel = p.getRelativePhase(Math.PI);
		assertTrue(rel >= 0);
		assertTrue(rel <= 2 * Math.PI + 1e-9);
	}

	public void testGetRelativePhaseInCycles() {
		Harmonic h = new Harmonic(0.1);
		PeriodFitParameters p = new PeriodFitParameters(h, 1.0, 1.0, 0.0, 0.0, 0.0);
		assertTrue(Tolerance.areClose(p.getRelativePhase(0.0) / (2 * Math.PI),
				p.getRelativePhaseInCycles(0.0), 1e-12, true));
	}

	public void testToValueAtZeroPoint() {
		Harmonic h = new Harmonic(1.0);
		double zp = 2450000.0;
		PeriodFitParameters p = new PeriodFitParameters(h, 0.0, 0.8, 0.6, 0.0, zp);
		double t = zp;
		double expected = 0.8;
		assertTrue(Tolerance.areClose(expected, p.toValue(t), 1e-12, true));
	}

	public void testToValueFullSinusoid() {
		Harmonic h = new Harmonic(1.0);
		double zp = 0.0;
		double cos = 0.0;
		double sin = 1.0;
		PeriodFitParameters p = new PeriodFitParameters(h, 0.0, cos, sin, 0.0, zp);
		double t = 0.25;
		double arg = 2 * Math.PI * 1.0 * (t - zp);
		double expected = cos * Math.cos(arg) + sin * Math.sin(arg);
		assertTrue(Tolerance.areClose(expected, p.toValue(t), 1e-12, true));
	}

	public void testEqualsWithinTolerance() {
		PeriodFitParameters a = new PeriodFitParameters(new Harmonic(0.1), 2.0, 1.0, -0.5, 0.1, 100.0);
		PeriodFitParameters b = new PeriodFitParameters(new Harmonic(0.1), 2.0, 1.0, -0.5, 0.1, 100.0);
		assertEquals(a, b);
	}

	public void testNotEqualsDifferentFrequency() {
		PeriodFitParameters a = new PeriodFitParameters(new Harmonic(0.1), 2.0, 1.0, 0.0, 0.0, 0.0);
		PeriodFitParameters b = new PeriodFitParameters(new Harmonic(0.11), 2.0, 1.0, 0.0, 0.0, 0.0);
		assertFalse(a.equals(b));
	}

	public void testNotEqualsWrongType() {
		PeriodFitParameters a = new PeriodFitParameters(new Harmonic(0.1), 1.0, 1.0, 0.0, 0.0, 0.0);
		assertFalse(a.equals("x"));
	}

	public void testCompareToByHarmonicFrequency() {
		PeriodFitParameters lo = new PeriodFitParameters(new Harmonic(0.05), 1.0, 1.0, 0.0, 0.0, 0.0);
		PeriodFitParameters hi = new PeriodFitParameters(new Harmonic(0.2), 1.0, 1.0, 0.0, 0.0, 0.0);
		assertTrue(lo.compareTo(hi) < 0);
		assertTrue(hi.compareTo(lo) > 0);
		assertEquals(0, lo.compareTo(new PeriodFitParameters(new Harmonic(0.05), 9.0, 9.0, 9.0, 9.0, 9.0)));
	}

	public void testToStringContainsCosSin() {
		PeriodFitParameters p = new PeriodFitParameters(new Harmonic(0.1), 0.0, 1.5, -0.25, 0.0, 0.0);
		String s = p.toString();
		assertTrue(s.contains("cos("));
		assertTrue(s.contains("sin("));
	}

	public void testToExcelStringUsesPIFunction() {
		PeriodFitParameters p = new PeriodFitParameters(new Harmonic(0.1), 0.0, 1.0, 0.0, 0.0, 2450000.0);
		String s = p.toExcelString();
		assertTrue(s.contains("PI()"));
		assertTrue(s.contains("COS("));
	}

	public void testToProsaicStringContainsLabels() {
		PeriodFitParameters p = new PeriodFitParameters(new Harmonic(0.1), 1.0, 1.0, 0.0, 0.25, 10.0);
		String s = p.toProsaicString();
		assertTrue(s.contains("frequency="));
		assertTrue(s.contains("period="));
		assertTrue(s.contains("amplitude="));
	}

	public void testToRStringContainsPiAndNewline() {
		PeriodFitParameters p = new PeriodFitParameters(new Harmonic(0.1), 0.0, 0.5, -0.25, 0.0, 0.0);
		String s = p.toRString();
		assertTrue(s.contains("pi*"));
		assertTrue(s.contains("cos("));
	}
}
