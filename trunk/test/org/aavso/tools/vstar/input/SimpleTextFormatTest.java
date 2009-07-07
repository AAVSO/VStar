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
package org.aavso.tools.vstar.input;

import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.List;

import junit.framework.TestCase;

import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.data.validation.SimpleTextFormatValidator;
import org.aavso.tools.vstar.exception.ObservationReadError;
import org.aavso.tools.vstar.exception.ObservationValidationError;

/**
 * This is a unit test for TextFormatObservationReader.
 * 
 * It contains tests for valid and invalid test data.
 * 
 * The format is for each line is: JD MAG [UNCERTAINTY] [OBSCODE] [VALFLAG]
 */
public class SimpleTextFormatTest extends TestCase {

	/**
	 * Constructor
	 * 
	 * @param name
	 */
	public SimpleTextFormatTest(String name) {
		super(name);
	}

	// Tests of valid simple text format.

	public void testValidJulianDayAndMagTSV() {
		commonValidJulianDayAndMagTest("2450001.5\t10.0\n", "\t");
	}

	public void testValidJulianDayAndMagCSV() {
		commonValidJulianDayAndMagTest("2450001.5,10.0\n", ",");
	}

	public void testValidMultipleLines() {
		StringBuffer lines = new StringBuffer();
		lines.append("2450001.5\t10.0\n");
		lines.append("2430002.0\t2.0");
		
		List<ValidObservation> obs = commonValidTest(lines.toString(), "\t");
		
		assertTrue(obs.size() == 2);

		ValidObservation ob0 = (ValidObservation) obs.get(0);
		assertEquals(2450001.5, ob0.getDateInfo().getJulianDay());
		
		ValidObservation ob1 = (ValidObservation) obs.get(1);
		assertEquals(2430002.0, ob1.getDateInfo().getJulianDay());
	}

	// Helpers
	
	private void commonValidJulianDayAndMagTest(String line, String delimiter) {
		List<ValidObservation> obs = commonValidTest(line, delimiter);
		
		assertTrue(obs.size() == 1);
		
		ValidObservation ob = (ValidObservation) obs.get(0);
		assertEquals(2450001.5, ob.getDateInfo().getJulianDay());
		assertEquals(10.0, ob.getMagnitude().getMagValue());
		assertFalse(ob.getMagnitude().isUncertain());
		assertFalse(ob.getMagnitude().isFainterThan());
	}

	private List<ValidObservation> commonValidTest(String str, String delimiter) {
		List<ValidObservation> obs = null;
		
		try {
			ObservationSourceAnalyser analyser = new ObservationSourceAnalyser(
					new LineNumberReader(new StringReader(str)), "Some String");
			analyser.analyse();
			
			ObservationRetrieverBase simpleTextFormatReader = new TextFormatObservationReader(
					new LineNumberReader(new StringReader(str)), analyser);

			simpleTextFormatReader.retrieveObservations();
			obs = simpleTextFormatReader.getValidObservations();
		} catch (ObservationReadError e) {
			fail(e.getMessage());
		} catch (IOException e) {
			fail(e.getMessage());
		}
		
		return obs;
	}

	// Tests of invalid simple text format.

	public void testInvalidMagTrailingDecimalPoint() {
		commonInvalidTest("2450001\t10.\n");
	}
	
	private void commonInvalidTest(String str) {		
		try {
			SimpleTextFormatValidator validator = new SimpleTextFormatValidator("\t", 2, 5);
			validator.validate(str);
			// We should have thrown a ObservationValidationError...
			fail();
		} catch (ObservationValidationError e) {
			// We expect to get here.
			assertTrue(true);
		}
	}
}
