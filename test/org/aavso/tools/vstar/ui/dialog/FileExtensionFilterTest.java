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
package org.aavso.tools.vstar.ui.dialog;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

/**
 * Unit tests for {@link FileExtensionFilter}.
 */
public class FileExtensionFilterTest extends TestCase {

	private static final List<String> EXTENSIONS = Arrays.asList(".csv", ".txt");

	private FileExtensionFilter filter;

	@Override
	protected void setUp() {
		filter = new FileExtensionFilter(EXTENSIONS);
	}

	public void testDirectoryIsAlwaysAccepted() {
		File dir = new File(System.getProperty("java.io.tmpdir"));
		assertTrue("A directory should always be accepted", filter.accept(dir));
	}

	public void testFileWithMatchingExtensionIsAccepted() {
		File f = new File("data.csv");
		assertTrue("A file with a matching extension should be accepted", filter.accept(f));
	}

	public void testFileWithMatchingExtensionCaseInsensitive() {
		File f = new File("data.TXT");
		assertTrue("Extension matching should be case-insensitive", filter.accept(f));
	}

	public void testFileWithNonMatchingExtensionIsRejected() {
		File f = new File("data.xml");
		assertFalse("A file with a non-matching extension should be rejected", filter.accept(f));
	}

	public void testFileWithNoExtensionIsRejected() {
		File f = new File("datafile");
		assertFalse("A file with no extension should be rejected", filter.accept(f));
	}
}
