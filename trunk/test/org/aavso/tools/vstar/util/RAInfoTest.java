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

import org.aavso.tools.vstar.util.coords.RAInfo;

/**
 * RAInfo unit tests.
 */
public class RAInfoTest extends TestCase {

	private final int EPOCH = 1950;
	
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
		RAInfo ra = new RAInfo(EPOCH, 0, 0, 0);
		assertEquals(0.0, ra.toDegrees());
	}

	public void testRAToDegrees2() {
		RAInfo ra = new RAInfo(EPOCH, 15, 2, 3.6);
		assertEquals(225.515, ra.toDegrees());
	}

	public void testRAToDegrees3() {
		RAInfo ra = new RAInfo(EPOCH, -12, 0, 0);
		assertEquals(-180.0, ra.toDegrees());
	}
}
