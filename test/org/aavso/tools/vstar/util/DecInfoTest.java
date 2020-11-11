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
package org.aavso.tools.vstar.util;

import org.aavso.tools.vstar.util.coords.DecInfo;
import org.aavso.tools.vstar.util.coords.EpochType;

import junit.framework.TestCase;

/**
 * DecInfo unit tests.
 */
public class DecInfoTest extends TestCase {

	public DecInfoTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	// Test cases.

	public void testDecToDegrees1() {
		DecInfo dec = new DecInfo(EpochType.B1950, 0, 0, 0);
		assertEquals(0.0, dec.toDegrees());
	}

	public void testDecToDegrees2() {
		DecInfo dec = new DecInfo(EpochType.B1950, -25, 45, 3);
		assertTrue(Tolerance.areClose(-25.7508333333333, dec.toDegrees(), 1e-8, true));
	}

	public void testDecToDegrees3() {
		DecInfo dec = new DecInfo(EpochType.B1950, 2, 0, 0);
		assertEquals(2.0, dec.toDegrees());
	}

	public void testDecDegsToDMS1() {
		DecInfo dec = new DecInfo(EpochType.J2000, 15.5092);
		Triple<Integer, Integer, Double> dms = dec.toDMS();
		assertEquals((int) 15, (int) dms.first);
		assertEquals((int) 30, (int) dms.second);
		assertTrue(Tolerance.areClose(33.12, dms.third, 1e-8, true));
	}

	public void testDecDegsToDMS2() {
		DecInfo dec = new DecInfo(EpochType.J2000, -15.5092);
		Triple<Integer, Integer, Double> dms = dec.toDMS();
		assertEquals(-15, (int) dms.first);
		assertEquals(30, (int) dms.second);
		assertTrue(Tolerance.areClose(33.12, dms.third, 1e-8, true));
	}
}
