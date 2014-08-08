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
import java.io.StringReader;

import junit.framework.TestCase;

import org.aavso.tools.vstar.exception.ObservationValidationError;

import com.csvreader.CsvReader;

/**
 * Unit test for ObservationFieldSplitter class.
 * 
 * The format is for a simple line is: JD MAG [UNCERTAINTY] [OBSCODE] [VALFLAG]
 * where each field is either delimited by commas or tabs.
 */
public class ObservationFieldSplitterTest extends TestCase {

	// Valid

	public void testRightNumberOfFields2To5() throws IOException {
		String line = "2450001.5\t10.0\n";

		try {
			CsvReader reader = new CsvReader(new StringReader(line));
			reader.setDelimiter('\t');
			assertTrue(reader.readRecord());
			ObservationFieldSplitter splitter = new ObservationFieldSplitter(
					reader, 2, 5);
			String[] fields = splitter.getFields();
			assertEquals(5, fields.length);

		} catch (ObservationValidationError e) {
			fail();
		}
	}

	public void testRightNumberOfFields5() throws IOException {
		String line = "2450001.5\t10.0\n";

		try {
			CsvReader reader = new CsvReader(new StringReader(line));
			reader.setDelimiter('\t');
			assertTrue(reader.readRecord());
			ObservationFieldSplitter splitter = new ObservationFieldSplitter(
					reader, 2, 5);
			String[] fields = splitter.getFields();
			assertEquals(5, fields.length);
		} catch (ObservationValidationError e) {
			fail();
		}
	}

	public void testRightNumberOfFields5AllPresent() throws IOException {
		String line = "2450001.5\t10.0\t0.1\tDJB\tD\n";

		try {
			CsvReader reader = new CsvReader(new StringReader(line));
			reader.setDelimiter('\t');
			assertTrue(reader.readRecord());
			ObservationFieldSplitter splitter = new ObservationFieldSplitter(
					reader, 5, 5);
			String[] fields = splitter.getFields();
			assertEquals(5, fields.length);
		} catch (ObservationValidationError e) {
			fail();
		}
	}

	public void testRightNumberOfFields5NoObsCode() throws IOException {
		String line = "2450001.5\t10.0\t0.1\t\tD\n";

		try {
			CsvReader reader = new CsvReader(new StringReader(line));
			reader.setDelimiter('\t');
			assertTrue(reader.readRecord());
			ObservationFieldSplitter splitter = new ObservationFieldSplitter(
					reader, 5, 5);
			String[] fields = splitter.getFields();
			assertEquals(5, fields.length);
		} catch (ObservationValidationError e) {
			fail();
		}
	}

	public void testTabDelimitedFieldsWithSingleQuotedField()
			throws IOException {
		String line = "2456362.04142\t8.2\t\t\tVis.\tBDJB\tBU\t82\t76\t10 star\t\"\"\"8\\\"\" SCT, 24.5mm eyepiece\"\"\"\t\t\tZ\t\t\t\tR CAR\t\tSTD\t\t\t";

		try {
			CsvReader reader = new CsvReader(new StringReader(line));
			reader.setDelimiter('\t');
			assertTrue(reader.readRecord());
			ObservationFieldSplitter splitter = new ObservationFieldSplitter(
					reader, 23, 23);
			String[] fields = splitter.getFields();
			assertEquals(23, fields.length);
		} catch (ObservationValidationError e) {
			fail();
		}
	}

	public void testCommaDelimitedFieldsWithSingleQuotedField()
			throws IOException {
		String line = "2456362.04142,8.2,,,Vis.,BDJB,BU,82,76,10 star,\"\"\"8\\\"\" SCT, 24.5mm eyepiece\"\"\",,,Z,,,,R CAR,,STD,,,";

		try {
			CsvReader reader = new CsvReader(new StringReader(line));
			reader.setDelimiter(',');
			assertTrue(reader.readRecord());
			ObservationFieldSplitter splitter = new ObservationFieldSplitter(
					reader, 23, 23);
			String[] fields = splitter.getFields();
			assertEquals(23, fields.length);
		} catch (ObservationValidationError e) {
			fail();
		}
	}

	// Invalid

	// Note: This test currently fails (2.16.3) because we no longer take into
	// account maximum number of fields, only discriminating on file type by
	// minimum number of fields.
	public void testTooManyFields() throws IOException {
		String line = "2450001.5\t10.0\t0.1\tXYZ\tD\t42\n";

		try {
			CsvReader reader = new CsvReader(new StringReader(line));
			reader.setDelimiter('\t');
			assertTrue(reader.readRecord());
			ObservationFieldSplitter splitter = new ObservationFieldSplitter(
					reader, 5, 5);
			splitter.getFields();
			fail();
		} catch (ObservationValidationError e) {
			// We should get to here.
		}
	}
}
