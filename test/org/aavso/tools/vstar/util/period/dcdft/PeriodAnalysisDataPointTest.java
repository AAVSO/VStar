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
package org.aavso.tools.vstar.util.period.dcdft;

import org.aavso.tools.vstar.util.period.PeriodAnalysisCoordinateType;
import org.quicktheories.WithQuickTheories;

import junit.framework.TestCase;

/**
 * Example and property-based tests for {@link PeriodAnalysisDataPoint}.
 */
public class PeriodAnalysisDataPointTest extends TestCase implements WithQuickTheories {

	public PeriodAnalysisDataPointTest(String name) {
		super(name);
	}

	public void testDefaultCtorMapsDcdftCoords() {
		PeriodAnalysisDataPoint p = new PeriodAnalysisDataPoint(0.1, 10.0, 5.0, 0.25);
		assertEquals(0.1, p.getFrequency(), 0.0);
		assertEquals(10.0, p.getPeriod(), 0.0);
		assertEquals(5.0, p.getPower(), 0.0);
		assertEquals(0.25, p.getSemiAmplitude(), 0.0);
		assertEquals(PeriodAnalysisDataPoint.DCDFT_COORD_TYPES, p.getCoordTypes());
	}

	public void testGetValueUnknownTypeIsNaN() {
		PeriodAnalysisDataPoint p = new PeriodAnalysisDataPoint(1.0, 1.0, 1.0, 1.0);
		assertTrue(Double.isNaN(p.getValue(PeriodAnalysisCoordinateType.AMPLITUDE)));
	}

	public void testEqualsAndHashCodeSameValues() {
		PeriodAnalysisDataPoint a = new PeriodAnalysisDataPoint(0.2, 5.0, 3.0, 0.1);
		PeriodAnalysisDataPoint b = new PeriodAnalysisDataPoint(0.2, 5.0, 3.0, 0.1);
		assertEquals(a, b);
		assertEquals(a.hashCode(), b.hashCode());
	}

	public void testNotEqualsWhenFrequencyDiffers() {
		PeriodAnalysisDataPoint a = new PeriodAnalysisDataPoint(0.2, 5.0, 3.0, 0.1);
		PeriodAnalysisDataPoint b = new PeriodAnalysisDataPoint(0.3, 5.0, 3.0, 0.1);
		assertFalse(a.equals(b));
	}

	public void testToStringContainsCoordinateLabels() {
		String s = new PeriodAnalysisDataPoint(0.1, 10.0, 2.0, 0.5).toString();
		assertTrue(s.contains(PeriodAnalysisCoordinateType.FREQUENCY.toString()));
		assertTrue(s.contains(PeriodAnalysisCoordinateType.PERIOD.toString()));
	}

	public void testComparatorTreatsEqualPointsAsZero() {
		PeriodAnalysisDataPoint a = new PeriodAnalysisDataPoint(1.0, 2.0, 3.0, 4.0);
		PeriodAnalysisDataPoint b = new PeriodAnalysisDataPoint(1.0, 2.0, 3.0, 4.0);
		assertEquals(0, PeriodAnalysisDataPointComparator.instance.compare(a, b));
	}

	public void testComparatorTreatsUnequalPointsAsNonZero() {
		PeriodAnalysisDataPoint a = new PeriodAnalysisDataPoint(1.0, 2.0, 3.0, 4.0);
		PeriodAnalysisDataPoint b = new PeriodAnalysisDataPoint(9.0, 2.0, 3.0, 4.0);
		assertEquals(1, PeriodAnalysisDataPointComparator.instance.compare(a, b));
	}

	/**
	 * Accessors agree with getValue for the standard DC DFT coordinate types.
	 */
	public void testAccessorsMatchGetValueProperty() {
		qt().forAll(doubles().between(1e-4, 10.0), doubles().between(0.0, 100.0), doubles().between(0.0, 50.0),
				doubles().between(0.0, 10.0)).check((freq, period, power, amp) -> {
					PeriodAnalysisDataPoint p = new PeriodAnalysisDataPoint(freq, period, power, amp);
					return p.getFrequency() == p.getValue(PeriodAnalysisCoordinateType.FREQUENCY)
							&& p.getPeriod() == p.getValue(PeriodAnalysisCoordinateType.PERIOD)
							&& p.getPower() == p.getValue(PeriodAnalysisCoordinateType.POWER)
							&& p.getSemiAmplitude() == p.getValue(PeriodAnalysisCoordinateType.SEMI_AMPLITUDE);
				});
	}

	/**
	 * Equal constructions imply equal hashCodes.
	 */
	public void testEqualsImpliesSameHashCodeProperty() {
		qt().forAll(doubles().between(1e-4, 5.0), doubles().between(0.1, 20.0), doubles().between(0.0, 10.0),
				doubles().between(0.0, 5.0)).check((freq, period, power, amp) -> {
					PeriodAnalysisDataPoint a = new PeriodAnalysisDataPoint(freq, period, power, amp);
					PeriodAnalysisDataPoint b = new PeriodAnalysisDataPoint(freq, period, power, amp);
					return a.equals(b) && a.hashCode() == b.hashCode();
				});
	}
}
