package org.aavso.tools.vstar.data.validation;

import org.aavso.tools.vstar.exception.ObservationValidationError;

import junit.framework.TestCase;

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

    // invalid test

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
