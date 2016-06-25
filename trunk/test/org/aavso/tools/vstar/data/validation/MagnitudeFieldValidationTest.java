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

import org.aavso.tools.vstar.data.Magnitude;

/**
 * Magnitude field value tests.
 */
public class MagnitudeFieldValidationTest extends TestCase {

	public MagnitudeFieldValidationTest(String name) {
		super(name);
	}

	public void testMagEmpty() {
		MagnitudeFieldValidator validator = new MagnitudeFieldValidator();

		try {
			validator.validate("");
			fail();
		} catch (Exception e) {
		}
	}

	public void testMagPos() {
		MagnitudeFieldValidator validator = new MagnitudeFieldValidator();

		try {
			assertEquals(new Magnitude(23, 0), validator.validate("23"));
		} catch (Exception e) {
			fail();
		}
	}

	public void testMagNeg() {
		MagnitudeFieldValidator validator = new MagnitudeFieldValidator();

		try {
			assertEquals(new Magnitude(-1, 0), validator.validate("-1"));
		} catch (Exception e) {
			fail();
		}
	}
	
	public void testMagReal() {
		MagnitudeFieldValidator validator = new MagnitudeFieldValidator();

		try {
			assertEquals(new Magnitude(23.25, 0), validator.validate("23.25"));
		} catch (Exception e) {
			fail();
		}
	}

	public void testMagExp() {
		MagnitudeFieldValidator validator = new MagnitudeFieldValidator();

		try {
			assertEquals(new Magnitude(2.325, 0), validator.validate("23.25e-01"));
		} catch (Exception e) {
			fail();
		}
	}
	
	public void testMagFainterThan() {
		MagnitudeFieldValidator validator = new MagnitudeFieldValidator();

		try {
			Magnitude mag = validator.validate("<23.25");
			assertEquals(23.25, mag.getMagValue());
			assertTrue(mag.isFainterThan());
		} catch (Exception e) {
			fail();
		}
	}

	public void testMagBrighterThan() {
		MagnitudeFieldValidator validator = new MagnitudeFieldValidator();

		try {
			Magnitude mag = validator.validate(">23.25");
			assertEquals(23.25, mag.getMagValue());
			assertTrue(mag.isBrighterThan());
		} catch (Exception e) {
			fail();
		}
	}

	public void testMagUncertain() {
		MagnitudeFieldValidator validator = new MagnitudeFieldValidator();

		try {
			Magnitude mag = validator.validate("23.25:");
			assertEquals(23.25, mag.getMagValue());
			assertTrue(mag.isUncertain());
		} catch (Exception e) {
			fail();
		}
	}

	public void testMagFainterThanAndUncertain() {
		MagnitudeFieldValidator validator = new MagnitudeFieldValidator();

		try {
			Magnitude mag = validator.validate("<23.25:");
			assertEquals(23.25, mag.getMagValue());
			assertTrue(mag.isFainterThan());
			assertTrue(mag.isUncertain());
		} catch (Exception e) {
			fail();
		}
	}
}
