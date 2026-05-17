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
package org.aavso.tools.vstar.ui.model.list;

import java.util.Collection;
import java.util.Locale;

import junit.framework.TestCase;

/**
 * Pure unit tests for {@link AAVSOFormatRawDataColumnInfoSource}.
 *
 * This is a data-only class (column metadata), so no Swing display is needed.
 *
 * Part of issue #579 (GUI code coverage, prong D).
 */
public class AAVSOFormatRawDataColumnInfoSourceTest extends TestCase {

	// Column indices from the production class (package-private constants
	// are not accessible here, so we replicate the expected values).
	private static final int EXPECTED_COLUMN_COUNT_NO_LINE_NUMS = 27;
	private static final int EXPECTED_COLUMN_COUNT_WITH_LINE_NUMS = 28;

	// Well-known column indices for type assertions.
	private static final int DISCREPANT_COLUMN = 25;
	private static final int LINE_NUM_COLUMN   = 27;

	private AAVSOFormatRawDataColumnInfoSource sourceWithLine;
	private AAVSOFormatRawDataColumnInfoSource sourceNoLine;

	@Override
	protected void setUp() {
		Locale.setDefault(Locale.ENGLISH);
		sourceWithLine = new AAVSOFormatRawDataColumnInfoSource(true);
		sourceNoLine   = new AAVSOFormatRawDataColumnInfoSource(false);
	}

	// --- getColumnCount ---

	public void testColumnCountWithLineNumbers() {
		assertEquals(EXPECTED_COLUMN_COUNT_WITH_LINE_NUMS,
				sourceWithLine.getColumnCount());
	}

	public void testColumnCountWithoutLineNumbers() {
		assertEquals(EXPECTED_COLUMN_COUNT_NO_LINE_NUMS,
				sourceNoLine.getColumnCount());
	}

	// --- getTableColumnTitle (representative sample) ---

	public void testColumnTitleAtIndexZeroIsTime() {
		String title = sourceWithLine.getTableColumnTitle(0);
		assertNotNull(title);
		assertTrue("Expected 'Time' but got: " + title,
				title.equalsIgnoreCase("time"));
	}

	public void testColumnTitleMagnitude() {
		assertEquals("Magnitude", sourceWithLine.getTableColumnTitle(2));
	}

	public void testColumnTitleUncertainty() {
		assertEquals("Uncertainty", sourceWithLine.getTableColumnTitle(3));
	}

	public void testColumnTitleBand() {
		assertEquals("Band", sourceWithLine.getTableColumnTitle(4));
	}

	public void testColumnTitleObserverCode() {
		assertEquals("Observer Code", sourceWithLine.getTableColumnTitle(5));
	}

	public void testColumnTitleDiscrepant() {
		assertEquals("Discrepant?", sourceWithLine.getTableColumnTitle(DISCREPANT_COLUMN));
	}

	public void testColumnTitleLineNum() {
		assertEquals("Line", sourceWithLine.getTableColumnTitle(LINE_NUM_COLUMN));
	}

	// --- getTableColumnClass ---

	public void testColumnClassDefaultIsString() {
		// Most columns are String.
		assertEquals(String.class, sourceWithLine.getTableColumnClass(2));  // Magnitude
		assertEquals(String.class, sourceWithLine.getTableColumnClass(4));  // Band
		assertEquals(String.class, sourceWithLine.getTableColumnClass(5));  // Observer Code
	}

	public void testColumnClassDiscrepantIsBoolean() {
		assertEquals(Boolean.class,
				sourceWithLine.getTableColumnClass(DISCREPANT_COLUMN));
	}

	public void testColumnClassLineNumIsInteger() {
		assertEquals(Integer.class,
				sourceWithLine.getTableColumnClass(LINE_NUM_COLUMN));
	}

	// --- getColumnNames ---

	public void testGetColumnNamesNotNull() {
		Collection<String> names = sourceWithLine.getColumnNames();
		assertNotNull(names);
	}

	public void testGetColumnNamesContainsExpected() {
		Collection<String> names = sourceWithLine.getColumnNames();
		assertTrue(names.contains("Magnitude"));
		assertTrue(names.contains("Band"));
		assertTrue(names.contains("Observer Code"));
		assertTrue(names.contains("Discrepant?"));
		assertTrue(names.contains("Line"));
	}

	// --- getColumnIndexByName ---

	public void testGetColumnIndexByNameMagnitude() {
		assertEquals(2, sourceWithLine.getColumnIndexByName("Magnitude"));
	}

	public void testGetColumnIndexByNameBand() {
		assertEquals(4, sourceWithLine.getColumnIndexByName("Band"));
	}

	public void testGetColumnIndexByNameDiscrepant() {
		assertEquals(DISCREPANT_COLUMN,
				sourceWithLine.getColumnIndexByName("Discrepant?"));
	}

	public void testGetColumnIndexByNameNullThrows() {
		try {
			sourceWithLine.getColumnIndexByName(null);
			fail("Expected IllegalArgumentException for null name");
		} catch (IllegalArgumentException e) {
			// expected
		}
	}

	public void testGetColumnIndexByNameUnknownThrows() {
		try {
			sourceWithLine.getColumnIndexByName("NoSuchColumn");
			fail("Expected IllegalArgumentException for unknown name");
		} catch (IllegalArgumentException e) {
			// expected
		}
	}

	// --- getDiscrepantColumnIndex ---

	public void testGetDiscrepantColumnIndex() {
		assertEquals(DISCREPANT_COLUMN, sourceWithLine.getDiscrepantColumnIndex());
	}
}
