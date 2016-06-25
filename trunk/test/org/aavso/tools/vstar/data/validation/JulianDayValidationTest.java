/**
 * VStar: a statistical analysis tool for variable star data.
 * Copyright (C) 2016  AAVSO (http://www.aavso.org/)
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
package org.aavso.tools.vstar.data.validation;

import junit.framework.TestCase;

import org.aavso.tools.vstar.data.DateInfo;
import org.aavso.tools.vstar.data.Magnitude;

/**
 * Julian Day value tests.
 */
public class JulianDayValidationTest extends TestCase {

	public JulianDayValidationTest(String name) {
		super(name);
	}

	public void testJDTestEmpty() {
		JulianDayValidator validator = new JulianDayValidator();

		try {
			validator.validate("");
			fail();
		} catch (Exception e) {
		}
	}

	public void testJDTestInt() {
		JulianDayValidator validator = new JulianDayValidator();

		try {
			assertEquals(new DateInfo(245756), validator.validate("245756"));
		} catch (Exception e) {
			fail();
		}
	}

	public void testJDTestReal() {
		JulianDayValidator validator = new JulianDayValidator();

		try {
			assertEquals(new DateInfo(245756.503589), validator.validate("245756.503589"));
		} catch (Exception e) {
			fail();
		}
	}

	public void testJDTestExp() {
		JulianDayValidator validator = new JulianDayValidator();

		try {
			assertEquals(new DateInfo(245756.503589),
					validator.validate("2457565.03589e-01"));
		} catch (Exception e) {
			fail();
		}
	}
}
