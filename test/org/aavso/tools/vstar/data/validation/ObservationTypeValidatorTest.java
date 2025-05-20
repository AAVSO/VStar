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
 * Unit tests for observation type validator.
 */
public class ObservationTypeValidatorTest extends TestCase {

    private ObservationTypeValidator validator;

    public ObservationTypeValidatorTest(String name) {
        super(name);
        validator = new ObservationTypeValidator();
    }

    // valid tests

    public void testVisualObsType() {
        commonTest("Visual");
    }

    public void testCCDObsType() {
        commonTest("CCD");
    }

    public void testPEPObsType() {
        commonTest("PEP");
    }

    public void testWedgePhotometerObsType() {
        commonTest("Wedge Photometer");
    }

    public void testPTGObsType() {
        commonTest("PTG");
    }

    public void testDSLRObsType() {
        commonTest("DSLR");
    }

    // invalid tests

    public void testUnknownObsType1() {
        try {
            validator.validate("Unknown");
            fail();
        } catch (ObservationValidationError e) {
        }
    }

    public void testUnknownObsType2() {
        try {
            assertEquals("Unknown", validator.validate("FooBar"));
            fail();
        } catch (ObservationValidationError e) {
        }
    }

    // Helpers

    private void commonTest(String obsType) {
        try {
            assertEquals(obsType, validator.validate(obsType));
        } catch (ObservationValidationError e) {
            fail();
        }
    }
}
