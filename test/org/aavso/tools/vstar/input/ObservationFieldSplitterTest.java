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

import junit.framework.TestCase;

import org.aavso.tools.vstar.exception.ObservationValidationError;

/**
 * Unit test for ObservationFieldSplitter class.
 * 
 * The format is for a simple line is: JD MAG [UNCERTAINTY] [OBSCODE] [VALFLAG]
 * where each field is either delimited by commas or tabs.
 */
public class ObservationFieldSplitterTest extends TestCase {

	// Valid

	public void testRightNumberOfFields2To5() {
		String line = "2450001.5\t10.0\n";

		try {
			ObservationFieldSplitter splitter = new ObservationFieldSplitter(
					"\t", 2, 5);
			String[] fields = splitter.getFields(line);
			assertEquals(5, fields.length);
		} catch (ObservationValidationError e) {
			fail();
		}
	}

	public void testRightNumberOfFields5() {
		String line = "2450001.5\t10.0\n";

		try {
			ObservationFieldSplitter splitter = new ObservationFieldSplitter(
					"\t", 2, 5);
			String[] fields = splitter.getFields(line);
			assertEquals(5, fields.length);
		} catch (ObservationValidationError e) {
			fail();
		}
	}

	public void testRightNumberOfFields5AllPresent() {
		String line = "2450001.5\t10.0\t0.1\tDJB\tD\n";

		try {
			ObservationFieldSplitter splitter = new ObservationFieldSplitter(
					"\t", 5, 5);
			String[] fields = splitter.getFields(line);
			assertEquals(5, fields.length);
		} catch (ObservationValidationError e) {
			fail();
		}
	}

	public void testRightNumberOfFields5NoObsCode() {
		String line = "2450001.5\t10.0\t0.1\t\tD\n";

		try {
			ObservationFieldSplitter splitter = new ObservationFieldSplitter(
					"\t", 5, 5);
			String[] fields = splitter.getFields(line);
			assertEquals(5, fields.length);
		} catch (ObservationValidationError e) {
			fail();
		}
	}

	// Invalid

	public void testTooManyFields() {
		String line = "2450001.5\t10.0\t0.1\tXYZ\tD\t42\n";

		try {
			ObservationFieldSplitter splitter = new ObservationFieldSplitter(
					"\t", 5, 5);
			splitter.getFields(line);
			fail();
		} catch (ObservationValidationError e) {
			// We should get to here.
		}
	}
}
