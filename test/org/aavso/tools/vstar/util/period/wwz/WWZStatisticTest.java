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
package org.aavso.tools.vstar.util.period.wwz;

import org.aavso.tools.vstar.util.period.PeriodAnalysisCoordinateType;
import org.quicktheories.WithQuickTheories;

import junit.framework.TestCase;

/**
 * Example and property-based tests for {@link WWZStatistic}.
 */
public class WWZStatisticTest extends TestCase implements WithQuickTheories {

	public WWZStatisticTest(String name) {
		super(name);
	}

	public void testAccessorsAndPeriodReciprocal() {
		WWZStatistic s = new WWZStatistic(2450000.0, 0.25, 2.0, 0.5, 12.0, 8.0);
		assertEquals(2450000.0, s.getTau(), 0.0);
		assertEquals(0.25, s.getFrequency(), 0.0);
		assertEquals(4.0, s.getPeriod(), 1e-12);
		assertEquals(2.0, s.getWwz(), 0.0);
		assertEquals(0.5, s.getSemiAmplitude(), 0.0);
		assertEquals(12.0, s.getMave(), 0.0);
		assertEquals(8.0, s.getNeff(), 0.0);
		assertEquals(2.0, s.getPower(), 0.0);
	}

	public void testWwzCoordinateTypeMapping() {
		WWZStatistic s = new WWZStatistic(1.0, 2.0, 3.0, 4.0, 5.0, 6.0);
		assertEquals(1.0, s.getValue(WWZCoordinateType.TAU), 0.0);
		assertEquals(2.0, s.getValue(WWZCoordinateType.FREQUENCY), 0.0);
		assertEquals(0.5, s.getValue(WWZCoordinateType.PERIOD), 1e-12);
		assertEquals(3.0, s.getValue(WWZCoordinateType.WWZ), 0.0);
		assertEquals(4.0, s.getValue(WWZCoordinateType.SEMI_AMPLITUDE), 0.0);
		assertEquals(5.0, s.getValue(WWZCoordinateType.MEAN_MAG), 0.0);
		assertEquals(6.0, s.getValue(WWZCoordinateType.EFFECTIVE_NUM_DATA), 0.0);
	}

	public void testPeriodAnalysisCoordinateTypeMapping() {
		WWZStatistic s = new WWZStatistic(1.0, 0.5, 9.0, 0.2, 11.0, 3.0);
		assertEquals(0.5, s.getValue(PeriodAnalysisCoordinateType.FREQUENCY), 0.0);
		assertEquals(2.0, s.getValue(PeriodAnalysisCoordinateType.PERIOD), 1e-12);
		assertEquals(0.2, s.getValue(PeriodAnalysisCoordinateType.AMPLITUDE), 0.0);
		assertEquals(9.0, s.getValue(PeriodAnalysisCoordinateType.POWER), 0.0);
	}

	public void testEquals() {
		WWZStatistic a = new WWZStatistic(1, 2, 3, 4, 5, 6);
		WWZStatistic b = new WWZStatistic(1, 2, 3, 4, 5, 6);
		WWZStatistic c = new WWZStatistic(1, 2, 3, 4, 5, 7);
		assertEquals(a, b);
		assertFalse(a.equals(c));
		assertFalse(a.equals(null));
		assertFalse(a.equals("x"));
	}

	public void testToStringAndStructString() {
		WWZStatistic s = new WWZStatistic(1.5, 0.25, 2.0, 0.5, 12.0, 8.0);
		assertTrue(s.toString().contains("tau="));
		assertTrue(s.toStructString().startsWith("{"));
		assertTrue(s.toStructString().endsWith("}"));
	}

	/**
	 * period * frequency == 1 for positive frequencies.
	 */
	public void testPeriodFrequencyReciprocalProperty() {
		qt().forAll(doubles().between(1e-4, 10.0)).check(freq -> {
			WWZStatistic s = new WWZStatistic(0.0, freq, 1.0, 1.0, 10.0, 1.0);
			return Math.abs(s.getPeriod() * s.getFrequency() - 1.0) < 1e-9;
		});
	}

	/**
	 * getPower() is an alias for getWwz().
	 */
	public void testPowerAliasesWwzProperty() {
		qt().forAll(doubles().between(-10.0, 100.0)).check(wwz -> {
			WWZStatistic s = new WWZStatistic(0.0, 1.0, wwz, 0.0, 0.0, 0.0);
			return s.getPower() == s.getWwz();
		});
	}

	/**
	 * Equals is reflexive and agrees across identical constructions.
	 */
	public void testEqualsReflexiveAndConstructionProperty() {
		qt().forAll(doubles().between(2400000.0, 2500000.0), doubles().between(1e-3, 5.0),
				doubles().between(0.0, 20.0), doubles().between(0.0, 5.0)).check((tau, freq, wwz, amp) -> {
					double mave = 10.0;
					double neff = 5.0;
					WWZStatistic a = new WWZStatistic(tau, freq, wwz, amp, mave, neff);
					WWZStatistic b = new WWZStatistic(tau, freq, wwz, amp, mave, neff);
					return a.equals(a) && a.equals(b);
				});
	}
}
