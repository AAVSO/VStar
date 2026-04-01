/**
 * VStar: a statistical analysis tool for variable star data.
 * Copyright (C) 2011  AAVSO (http://www.aavso.org/)
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

public class HarmonicTest extends TestCase {

	public HarmonicTest(String name) {
		super(name);
	}

	public void testFundamentalFrequency() {
		Harmonic h = new Harmonic(0.1);
		assertEquals(0.1, h.getFrequency(), 1e-12);
		assertEquals(1, h.getHarmonicNumber());
	}

	public void testIsFundamental() {
		assertTrue(new Harmonic(0.1).isFundamental());
	}

	public void testNotFundamental() {
		assertFalse(new Harmonic(0.2, 2).isFundamental());
	}

	public void testGetPeriod() {
		assertEquals(4.0, new Harmonic(0.25).getPeriod(), 1e-12);
	}

	public void testGetFundamentalFrequency() {
		assertEquals(0.1, new Harmonic(0.3, 3).getFundamentalFrequency(), 1e-12);
	}

	public void testIsHarmonic() {
		Harmonic h1 = new Harmonic(0.25);
		Harmonic h3 = new Harmonic(0.75, 3);
		assertTrue(h1.isHarmonic(h3));
	}

	public void testIsNotHarmonic() {
		Harmonic h1 = new Harmonic(0.1);
		Harmonic h2 = new Harmonic(0.25, 2);
		assertFalse(h1.isHarmonic(h2));
	}

	public void testInvalidFrequency() {
		try {
			new Harmonic(0);
			fail();
		} catch (IllegalArgumentException e) {
		}
	}

	public void testInvalidNegativeFrequency() {
		try {
			new Harmonic(-1.0);
			fail();
		} catch (IllegalArgumentException e) {
		}
	}

	public void testInvalidHarmonic() {
		try {
			new Harmonic(0.1, 0);
			fail();
		} catch (IllegalArgumentException e) {
		}
	}

	public void testCompareToLess() {
		assertTrue(new Harmonic(0.1).compareTo(new Harmonic(0.2)) < 0);
	}

	public void testCompareToGreater() {
		assertTrue(new Harmonic(0.2).compareTo(new Harmonic(0.1)) > 0);
	}

	public void testCompareToEqual() {
		assertEquals(0, new Harmonic(0.1).compareTo(new Harmonic(0.1)));
	}
}
