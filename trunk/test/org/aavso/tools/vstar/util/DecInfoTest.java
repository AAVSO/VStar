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

import org.aavso.tools.vstar.util.coords.DecInfo;

/**
 * DecInfo unit tests.
 */
public class DecInfoTest extends TestCase {

	private final int EPOCH = 1950;
	private static final int PRECISION = 8;

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
		DecInfo dec = new DecInfo(EPOCH, 0, 0, 0);
		assertEquals(0.0, dec.toDegrees());
	}

	public void testDecToDegrees2() {
		DecInfo dec = new DecInfo(EPOCH, -25, 45, 3);
		String decStr = getNumToPrecision(dec.toDegrees(), PRECISION);
		assertEquals(getNumToPrecision(-25.7508333333333, PRECISION), decStr);
	}

	public void testDecToDegrees3() {
		DecInfo dec = new DecInfo(EPOCH, 2, 0, 0);
		assertEquals(2.0, dec.toDegrees());
	}
	
	// Helpers
	
	private String getNumToPrecision(double n, int precision) {
		return String.format("%1." + precision + "f", n);
	}
}
