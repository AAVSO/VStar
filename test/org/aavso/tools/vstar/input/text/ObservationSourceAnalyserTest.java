/**
 * VStar: a statistical analysis tool for variable star data.
 * Copyright (C) 2026  AAVSO (http://www.aavso.org/)
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

import java.io.LineNumberReader;
import java.io.StringReader;

import junit.framework.TestCase;

import org.aavso.tools.vstar.data.validation.AAVSODownloadFormatValidator;
import org.aavso.tools.vstar.data.validation.CommonTextFormatValidator;
import org.aavso.tools.vstar.data.validation.SimpleTextFormatValidator;
import org.aavso.tools.vstar.exception.ObservationReadError;
import org.aavso.tools.vstar.ui.mediator.NewStarType;

public class ObservationSourceAnalyserTest extends TestCase {

	public ObservationSourceAnalyserTest(String name) {
		super(name);
	}

	public void testSimpleTabDelimited() throws Exception {
		String data = "2450001.5\t10.0\n";
		ObservationSourceAnalyser a = new ObservationSourceAnalyser(
				new LineNumberReader(new StringReader(data)), "id1");
		a.analyse();
		assertEquals(NewStarType.NEW_STAR_FROM_SIMPLE_FILE, a.getNewStarType());
		assertEquals(ObservationSourceAnalyser.TAB_DELIM, a.getDelimiter());
		assertEquals("id1", a.getObsSourceIdentifier());
		assertTrue(a.getLineCount() > 0);
	}

	public void testSimpleCommaDelimited() throws Exception {
		String data = "2450001.5,10.0\n";
		ObservationSourceAnalyser a = new ObservationSourceAnalyser(
				new LineNumberReader(new StringReader(data)), "id2");
		a.analyse();
		assertEquals(NewStarType.NEW_STAR_FROM_SIMPLE_FILE, a.getNewStarType());
		assertEquals(ObservationSourceAnalyser.COMMA_DELIM, a.getDelimiter());
	}

	public void testSimpleSpaceDelimited() throws Exception {
		String data = "2450001.5 10.0\n";
		ObservationSourceAnalyser a = new ObservationSourceAnalyser(
				new LineNumberReader(new StringReader(data)), "id3");
		a.analyse();
		assertEquals(NewStarType.NEW_STAR_FROM_SIMPLE_FILE, a.getNewStarType());
		assertEquals(ObservationSourceAnalyser.SPACE_DELIM, a.getDelimiter());
	}

	public void testSkipsCommentAndBlankBeforeData() throws Exception {
		String data = "# header\n\n  \n2450001.5\t10.0\n";
		ObservationSourceAnalyser a = new ObservationSourceAnalyser(
				new LineNumberReader(new StringReader(data)), "id4");
		a.analyse();
		assertEquals(NewStarType.NEW_STAR_FROM_SIMPLE_FILE, a.getNewStarType());
		assertEquals(ObservationSourceAnalyser.TAB_DELIM, a.getDelimiter());
	}

	public void testDownloadFormatManyFields() throws Exception {
		StringBuilder line = new StringBuilder();
		for (int i = 0; i < 7; i++) {
			if (i > 0) {
				line.append('\t');
			}
			line.append("f").append(i);
		}
		line.append('\n');
		ObservationSourceAnalyser a = new ObservationSourceAnalyser(
				new LineNumberReader(new StringReader(line.toString())), "id5");
		a.analyse();
		assertEquals(NewStarType.NEW_STAR_FROM_DOWNLOAD_FILE, a.getNewStarType());
	}

	public void testUnknownFormatThrows() throws Exception {
		String data = "onlyonefield\n";
		ObservationSourceAnalyser a = new ObservationSourceAnalyser(
				new LineNumberReader(new StringReader(data)), "badfile");
		try {
			a.analyse();
			fail("expected ObservationReadError");
		} catch (ObservationReadError e) {
			assertTrue(e.getMessage().contains("badfile"));
			assertTrue(e.getMessage().toLowerCase().contains("unknown"));
		}
	}

	public void testGetTextFormatValidatorSimple() throws Exception {
		String data = "2450001.5\t10.0\n";
		ObservationSourceAnalyser a = new ObservationSourceAnalyser(
				new LineNumberReader(new StringReader(data)), "v");
		a.analyse();
		CommonTextFormatValidator v = a.getTextFormatValidator(
				new LineNumberReader(new StringReader(data)));
		assertTrue(v instanceof SimpleTextFormatValidator);
	}

	public void testGetTextFormatValidatorDownload() throws Exception {
		StringBuilder line = new StringBuilder();
		for (int i = 0; i < 7; i++) {
			if (i > 0) {
				line.append('\t');
			}
			line.append("x").append(i);
		}
		line.append('\n');
		String s = line.toString();
		ObservationSourceAnalyser a = new ObservationSourceAnalyser(
				new LineNumberReader(new StringReader(s)), "d");
		a.analyse();
		CommonTextFormatValidator v = a.getTextFormatValidator(
				new LineNumberReader(new StringReader(s)));
		assertTrue(v instanceof AAVSODownloadFormatValidator);
	}

	public void testLineCountMultipleLines() throws Exception {
		String data = "2450001.5\t10.0\n2450002.5\t10.1\n";
		ObservationSourceAnalyser a = new ObservationSourceAnalyser(
				new LineNumberReader(new StringReader(data)), "multi");
		a.analyse();
		assertEquals(2, a.getLineCount());
	}
}
