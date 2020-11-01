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

import junit.framework.TestCase;

import org.aavso.tools.vstar.util.coords.EpochType;
import org.aavso.tools.vstar.util.coords.RAInfo;

/**
 * RAInfo unit tests.
 */
public class RAInfoTest extends TestCase {
	
	public RAInfoTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	// Test cases.
	
	public void testRAToDegrees1() {
		RAInfo ra = new RAInfo(EpochType.B1950, 0, 0, 0);
		assertEquals(0.0, ra.toDegrees());
	}

	public void testRAToDegrees2() {
		RAInfo ra = new RAInfo(EpochType.B1950, 15, 2, 3.6);
		assertEquals(225.515, ra.toDegrees());
	}

	public void testRAToDegrees3() {
		RAInfo ra = new RAInfo(EpochType.B1950, -12, 0, 0);
		assertEquals(-180.0, ra.toDegrees());
	}
	
	public void testRADegsToHMS1() {
		RAInfo ra = new RAInfo(EpochType.J2000, 232.6375);
		Triple<Integer, Integer, Double> hms = ra.toHMS();
		assertEquals(15, (int)hms.first);
		assertEquals(30, (int)hms.second);
		assertTrue(Tolerance.areClose(33.12, hms.third, 1e6, true));
	}

	public void testRADegsToHMS2() {
		RAInfo ra = new RAInfo(EpochType.J2000, 7.6375);
		Triple<Integer, Integer, Double> hms = ra.toHMS();
		assertEquals(0, (int)hms.first);
		assertEquals(30, (int)hms.second);
		assertTrue(Tolerance.areClose(33.12, hms.third, 1e6, true));
	}

	public void testRADegsToHMS3() {
		RAInfo ra = new RAInfo(EpochType.J2000, 352.6375);
		Triple<Integer, Integer, Double> hms = ra.toHMS();
		assertEquals(23, (int)hms.first);
		assertEquals(30, (int)hms.second);
		assertTrue(Tolerance.areClose(33.12, hms.third, 1e6, true));
	}
}
