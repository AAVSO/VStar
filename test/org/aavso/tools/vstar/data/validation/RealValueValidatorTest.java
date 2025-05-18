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

import org.aavso.tools.vstar.exception.ObservationValidationError;

import junit.framework.TestCase;

/**
 * Unit tests for real valid validator.
 */
public class RealValueValidatorTest extends TestCase {

    public RealValueValidatorTest(String name) {
        super(name);
    }

    public void testNegativeNotOKEmptyNotOKReal() {
        RealValueValidator validator = new RealValueValidator("A", false, false);

        try {
            validator.validate("4.2");
            validator.validate("0");
        } catch (ObservationValidationError e) {
            fail();
        }

        try {
            validator.validate("-4.2");
            fail();
        } catch (ObservationValidationError e) {
        }

        try {
            validator.validate("");
            fail();
        } catch (ObservationValidationError e) {
        }
    }
    
    public void testNegativeOKEmptyNotOKReal() {
        RealValueValidator validator = new RealValueValidator("B", true, false);

        try {
            validator.validate("4.2");
            validator.validate("0");
            validator.validate("-4.2");
        } catch (ObservationValidationError e) {
            fail();
        }
        
        try {
            validator.validate("");
            fail();
        } catch (ObservationValidationError e) {
        }
    }
    
    public void testNegativeOKEmptyOKReal() {
        RealValueValidator validator = new RealValueValidator("C", true, true);

        try {
            validator.validate("4.2");
            validator.validate("0");
            validator.validate("-4.2");
            validator.validate("");
        } catch (ObservationValidationError e) {
            fail();
        }
    }
}
