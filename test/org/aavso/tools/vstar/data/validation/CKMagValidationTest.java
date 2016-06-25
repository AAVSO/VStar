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
package org.aavso.tools.vstar.data.validation;

import org.aavso.tools.vstar.exception.ObservationValidationError;
import org.aavso.tools.vstar.exception.ObservationValidationWarning;

import junit.framework.TestCase;

/**
 * CMag, KMag tests.
 * 
 * See https://sourceforge.net/tracker/?func=detail&aid=2915572&group_id=263306&atid=1152052
 */
public class CKMagValidationTest extends TestCase {

	/**
	 * Constructor
	 * 
	 * @param name
	 *            Test name
	 */
	public CKMagValidationTest(String name) {
		super(name);
	}

	public void testCMagTest1() {
		CKMagValidator validator = new CKMagValidator(CKMagValidator.CMAG_KIND);

		try {
			assertEquals("", validator.validate(null));
		} catch (Exception e) {
			fail();
		}
	}

	public void testKMagTest1() {
		CKMagValidator validator = new CKMagValidator(CKMagValidator.KMAG_KIND);

		try {
			assertEquals("", validator.validate(""));
		} catch (Exception e) {
			fail();
		}
	}

	public void testCMagTest2() {
		CKMagValidator validator = new CKMagValidator(CKMagValidator.CMAG_KIND);

		try {
			assertEquals("123", validator.validate("123"));
		} catch (Exception e) {
			fail();
		}
	}

	public void testCMagTest3() {
		CKMagValidator validator = new CKMagValidator(CKMagValidator.CMAG_KIND);

		try {
			// Weird, but we're allowing it.
			// Note: having since done DSLR photometry I now understand that instrumental magnitudes may be negative!
			assertEquals("-123", validator.validate("-123"));
		} catch (Exception e) {
			fail();
		}
	}

	public void testCMagTest4() {
		CKMagValidator validator = new CKMagValidator(CKMagValidator.CMAG_KIND);

		try {
			assertEquals("Foo", validator.validate("Foo"));
		} catch (Exception e) {
			fail();
		}
	}

	public void testCMagTest5() {
		CKMagValidator validator = new CKMagValidator(CKMagValidator.CMAG_KIND);

		try {
			validator.validate("123456789");
			fail();
		} catch (ObservationValidationError e) {
			assertTrue(true);
		} catch (ObservationValidationWarning w) {
			fail();
		}
	}
}
