package org.aavso.tools.vstar.data.validation;

import junit.framework.TestCase;

import org.aavso.tools.vstar.data.CommentCodes;
import org.aavso.tools.vstar.data.CommentType;
import org.aavso.tools.vstar.exception.ObservationValidationError;

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

/**
 * This is a unit test class for comment codes and their validation.
 */
public class CommentCodeValidationTest extends TestCase {

	private static CommentCodeValidator validator = new CommentCodeValidator(
			CommentType.getRegex());

	/**
	 * Constructor
	 * 
	 * @param name
	 *            Test name
	 */
	public CommentCodeValidationTest(String name) {
		super(name);
	}

	// Valid (positive) tests
	
	public void testSkyBrightCommentCode() {
		commonValidCommentCodeTest("B", CommentType.SKY_BRIGHT);
	}

	public void testCommentCodes1() {
		CommentCodes commentTypes = new CommentCodes(" CCDV ");
		
		for (CommentType type : commentTypes.getCommentcodes()) {
			System.out.println(type);
		}
	}
	
	// Helpers
	
	private void commonValidCommentCodeTest(String code, CommentType expected) {	
		try {
			String validatedCode = validator.validate(code);
			assertEquals(expected, CommentType.getTypeFromFlag(validatedCode));
		} catch (ObservationValidationError e) {
			fail();
		}
	}
	
	private void commonInvalidCommentCodeTest(String code) {
		try {
			validator.validate("C");
			fail(); // this test fails if we execute this line
		} catch (ObservationValidationError e) {
			// We expect to be here.
		}
	}
}
