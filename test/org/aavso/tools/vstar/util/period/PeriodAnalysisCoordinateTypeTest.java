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
package org.aavso.tools.vstar.util.period;

import java.util.Locale;

import org.quicktheories.WithQuickTheories;

import junit.framework.TestCase;

/**
 * Tests for {@link PeriodAnalysisCoordinateType} identity, lookup, and
 * ordering.
 */
public class PeriodAnalysisCoordinateTypeTest extends TestCase implements WithQuickTheories {

	public PeriodAnalysisCoordinateTypeTest(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		Locale.setDefault(Locale.ENGLISH);
	}

	public void testCommonInstancesHaveDescriptions() {
		assertNotNull(PeriodAnalysisCoordinateType.FREQUENCY.getDescription());
		assertNotNull(PeriodAnalysisCoordinateType.PERIOD.getDescription());
		assertNotNull(PeriodAnalysisCoordinateType.POWER.getDescription());
		assertNotNull(PeriodAnalysisCoordinateType.SEMI_AMPLITUDE.getDescription());
		assertNotNull(PeriodAnalysisCoordinateType.AMPLITUDE.getDescription());
	}

	public void testCreateReturnsSameInstanceForSameDescription() {
		String desc = "UT-Coord-" + System.nanoTime();
		PeriodAnalysisCoordinateType a = PeriodAnalysisCoordinateType.create(desc);
		PeriodAnalysisCoordinateType b = PeriodAnalysisCoordinateType.create(desc);
		assertSame(a, b);
		assertEquals(desc, a.getDescription());
		PeriodAnalysisCoordinateType.delete(a);
	}

	public void testGetTypeFromDescriptionFindsCommonType() {
		PeriodAnalysisCoordinateType found = PeriodAnalysisCoordinateType
				.getTypeFromDescription(PeriodAnalysisCoordinateType.FREQUENCY.getDescription());
		assertSame(PeriodAnalysisCoordinateType.FREQUENCY, found);
	}

	public void testGetTypeFromDescriptionUnknownReturnsNull() {
		assertNull(PeriodAnalysisCoordinateType.getTypeFromDescription("no-such-coord-type-xyz"));
	}

	public void testDeleteRemovesCustomType() {
		String desc = "UT-Delete-" + System.nanoTime();
		PeriodAnalysisCoordinateType type = PeriodAnalysisCoordinateType.create(desc);
		PeriodAnalysisCoordinateType.delete(type);
		assertNull(PeriodAnalysisCoordinateType.getTypeFromDescription(desc));
	}

	public void testEqualsConsistentWithDescription() {
		assertEquals(PeriodAnalysisCoordinateType.PERIOD, PeriodAnalysisCoordinateType.PERIOD);
		assertFalse(PeriodAnalysisCoordinateType.PERIOD.equals(PeriodAnalysisCoordinateType.FREQUENCY));
	}

	public void testCompareToAntisymmetricForCommonTypes() {
		PeriodAnalysisCoordinateType a = PeriodAnalysisCoordinateType.FREQUENCY;
		PeriodAnalysisCoordinateType b = PeriodAnalysisCoordinateType.PERIOD;
		assertEquals(-Integer.signum(b.compareTo(a)), Integer.signum(a.compareTo(b)));
	}

	/**
	 * create(desc) is idempotent: repeated create yields the same instance.
	 */
	public void testCreateIdempotentProperty() {
		qt().forAll(strings().basicLatinAlphabet().ofLengthBetween(8, 16)).check(suffix -> {
			String desc = "PBT-" + suffix;
			PeriodAnalysisCoordinateType a = PeriodAnalysisCoordinateType.create(desc);
			PeriodAnalysisCoordinateType b = PeriodAnalysisCoordinateType.create(desc);
			boolean ok = a == b && a.equals(b) && a.getDescription().equals(desc);
			PeriodAnalysisCoordinateType.delete(a);
			return ok;
		});
	}

	/**
	 * compareTo agrees with String.compareTo on descriptions.
	 */
	public void testCompareToMatchesDescriptionOrderProperty() {
		qt().forAll(strings().basicLatinAlphabet().ofLengthBetween(4, 10),
				strings().basicLatinAlphabet().ofLengthBetween(4, 10)).check((s1, s2) -> {
					String d1 = "PBT-A-" + s1;
					String d2 = "PBT-B-" + s2;
					PeriodAnalysisCoordinateType a = PeriodAnalysisCoordinateType.create(d1);
					PeriodAnalysisCoordinateType b = PeriodAnalysisCoordinateType.create(d2);
					int cmp = a.compareTo(b);
					int expected = d1.compareTo(d2);
					boolean ok = Integer.signum(cmp) == Integer.signum(expected);
					PeriodAnalysisCoordinateType.delete(a);
					PeriodAnalysisCoordinateType.delete(b);
					return ok;
				});
	}
}
