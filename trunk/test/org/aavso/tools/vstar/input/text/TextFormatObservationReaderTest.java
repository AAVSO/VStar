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
package org.aavso.tools.vstar.input.text;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.List;

import junit.framework.TestCase;

import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.data.validation.SimpleTextFormatValidator;
import org.aavso.tools.vstar.exception.ObservationReadError;
import org.aavso.tools.vstar.exception.ObservationValidationError;
import org.aavso.tools.vstar.input.AbstractObservationRetriever;
import org.aavso.tools.vstar.input.text.ObservationSourceAnalyser;
import org.aavso.tools.vstar.input.text.TextFormatObservationReader;
import org.aavso.tools.vstar.ui.model.NewStarType;

/**
 * This is a unit test for TextFormatObservationReader.
 * 
 * It contains tests for valid and invalid test data.
 * 
 * The format is for each line is: JD MAG [UNCERTAINTY] [OBSCODE] [VALFLAG]
 */
public class TextFormatObservationReaderTest extends TestCase {

	/**
	 * Constructor
	 * 
	 * @param name
	 */
	public TextFormatObservationReaderTest(String name) {
		super(name);
	}

	// Tests of valid simple text format.

	public void testSimpleValidJulianDayAndMagTSV() {
		commonValidJulianDayAndMagTest("2450001.5\t10.0\n", "\t");
	}

	public void testSimpleValidJulianDayAndMagCSV() {
		commonValidJulianDayAndMagTest("2450001.5,10.0\n", ",");
	}

	public void testSimpleValidFullObservationTSV() {
		ValidObservation ob = commonValidJulianDayAndMagTest(
				"2450001.5\t10.0\t0.1\tDJB\tD\n", "\t");
		assertEquals(0.1, ob.getMagnitude().getUncertainty());
		assertEquals("DJB", ob.getObsCode());
		assertTrue(ob.isDiscrepant());
	}

	public void testSimpleValidAllButUncertaintyTSV() {
		ValidObservation ob = commonValidJulianDayAndMagTest(
				"2450001.5\t10.0\t\tDJB\tD\n", "\t");
		assertEquals(0.0, ob.getMagnitude().getUncertainty());
		assertEquals("DJB", ob.getObsCode());
		assertTrue(ob.isDiscrepant());
	}

	public void testSimpleValidAllButUncertaintyAndValflagTSV() {
		ValidObservation ob = commonValidJulianDayAndMagTest(
				"2450001.5\t10.0\t\tDJB\n", "\t");
		assertEquals(0.0, ob.getMagnitude().getUncertainty());
		assertEquals("DJB", ob.getObsCode());
		assertTrue(!ob.isDiscrepant());
	}

	public void testSimpleValidAllButUncertaintyAndValflagCSV() {
		ValidObservation ob = commonValidJulianDayAndMagTest(
				"2450001.5,10.0,,DJB\n", ",");
		assertEquals(0.0, ob.getMagnitude().getUncertainty());
		assertEquals("DJB", ob.getObsCode());
		assertTrue(!ob.isDiscrepant());
	}

	public void testSimpleValidMultipleLines() {
		StringBuffer lines = new StringBuffer();
		lines.append("2450001.5\t10.0\n");
		lines.append("2430002.0\t2.0");

		List<ValidObservation> obs = commonValidTest(lines.toString(), "\t");

		assertTrue(obs.size() == 2);

		ValidObservation ob0 =  obs.get(0);
		assertEquals(2450001.5, ob0.getDateInfo().getJulianDay());

		ValidObservation ob1 =  obs.get(1);
		assertEquals(2430002.0, ob1.getDateInfo().getJulianDay());
	}

	// Tests of valid AAVSO Download format.
	
	public void testAAVSODownloadTSV1() {
		StringBuffer lines = new StringBuffer();
		lines.append("2400020	3.86			Visual	AFW	K					No		G				miu Cep	NULL\n");				
		lines.append("2400038	4			Visual	WAI	K					No		G				miu Cep	0\n");
		
		List<ValidObservation> obs = commonValidTest(lines.toString(), "\t");

		assertTrue(obs.size() == 2);

		// A few checks.
		ValidObservation ob0 = obs.get(0);
		assertEquals(2400020.0, ob0.getDateInfo().getJulianDay());
		assertEquals("Visual", ob0.getBand());
		assertEquals("miu Cep", ob0.getName());
	}

	public void testAAVSODownloadCSV1() {
		StringBuffer lines = new StringBuffer();
		lines.append("2454924.60694,3.95,,,Visual,SSW,,34,45,1036bbr,,No,,G,,,,000-BCT-763\n");				
		lines.append("2454931.86042,3.6,,,Visual,MDP,B,37,34,Star Tutorial,MOON,No,,G,,,,000-BCT-763\n");
		lines.append("2454933.89861,4.0,,,Visual,MDP,B,37,44,Star Tutorial,MOON AND TWILIGHT,No,,G,,,,000-BCT-763\n");
		
		List<ValidObservation> obs = commonValidTest(lines.toString(), ",");

		assertTrue(obs.size() == 3);

		// A few checks.
		ValidObservation ob0 = obs.get(0);
		assertEquals(2454924.60694, ob0.getDateInfo().getJulianDay());
		assertEquals("Visual", ob0.getBand());
		assertEquals("000-BCT-763", ob0.getName());
	}

	// Tests of invalid simple text format.

	public void testSimpleInvalidMagTrailingDecimalPoint() {
		commonInvalidTest("2450001\t10.\n");
	}

	public void testSimpleInvalidAllButUncertaintyAndValflagTSV() {
		// There should be another tab between the magnitude and obscode
		// to account for the missing uncertainty value field.
		commonInvalidTest("2450001.5\t10.0\tDJB\n");
	}

	// Helpers

	private ValidObservation commonValidJulianDayAndMagTest(String line,
			String delimiter) {
		List<ValidObservation> obs = commonValidTest(line, delimiter);

		assertTrue(obs.size() == 1);

		ValidObservation ob =  obs.get(0);
		assertEquals(2450001.5, ob.getDateInfo().getJulianDay());
		assertEquals(10.0, ob.getMagnitude().getMagValue());
		assertFalse(ob.getMagnitude().isUncertain());
		assertFalse(ob.getMagnitude().isFainterThan());

		return ob;
	}

	private List<ValidObservation> commonValidTest(String str, String delimiter) {
		List<ValidObservation> obs = null;

		try {
			ObservationSourceAnalyser analyser = new ObservationSourceAnalyser(
					new LineNumberReader(new StringReader(str)), "Some String");
			analyser.analyse();

			AbstractObservationRetriever simpleTextFormatReader = new TextFormatObservationReader(
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

	private void commonInvalidTest(String str) {
		try {
			SimpleTextFormatValidator validator = new SimpleTextFormatValidator(
					"\t", 2, 5, NewStarType.NEW_STAR_FROM_SIMPLE_FILE
							.getFieldInfoSource());
			validator.validate(str);
			// We should have thrown a ObservationValidationError...
			fail();
		} catch (ObservationValidationError e) {
			// We expect to get here.
			assertTrue(true);
		}
	}
}
