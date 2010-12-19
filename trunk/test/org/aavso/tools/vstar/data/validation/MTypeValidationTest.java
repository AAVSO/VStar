/**
 * VStar: a statistical analysis tool for variable star data.
 * Copyright (C) 2009  AAVSO (http://www.aavso.org/)
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

import org.aavso.tools.vstar.data.MTypeType;
import org.aavso.tools.vstar.exception.ObservationValidationError;

import junit.framework.TestCase;

/**
 * Unit tests for magnitude type validation.
 */
public class MTypeValidationTest extends TestCase {

	private static MTypeValidator validator = new MTypeValidator();

	/**
	 * Constructor.
	 */
	public MTypeValidationTest(String name) {
		super(name);
	}

	// Valid tests.

	public void testSTD() {
		try {
			assertEquals(MTypeType.STD, validator.validate("STD"));
		} catch (ObservationValidationError e) {
			fail();
		}
	}

	public void testDIFF() {
		try {
			assertEquals(MTypeType.DIFF, validator.validate("DIFF"));
		} catch (ObservationValidationError e) {
			fail();
		}
	}

	public void testSTEP() {
		try {
			assertEquals(MTypeType.STEP, validator.validate("STEP"));
		} catch (ObservationValidationError e) {
			fail();
		}
	}

	public void testNull() {
		try {
			assertEquals(null, validator.validate(null));
		} catch (ObservationValidationError e) {
			fail();
		}
	}

	// Invalid tests.

	public void testFOO() {
		try {			
			validator.validate("FOO");
			fail();
		} catch (ObservationValidationError e) {
			// We expect to be here.
		}
	}

	public void testLowerCase() {
		try {			
			validator.validate("diff");
			fail();
		} catch (ObservationValidationError e) {
			// We expect to be here.
		}
	}

	public void testMixedCase() {
		try {			
			validator.validate("Diff");
			fail();
		} catch (ObservationValidationError e) {
			// We expect to be here.
		}
	}
}
