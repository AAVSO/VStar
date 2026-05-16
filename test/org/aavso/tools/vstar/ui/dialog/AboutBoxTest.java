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

import java.util.Locale;

import junit.framework.TestCase;

/**
 * Tests for {@link AboutBox}.
 *
 * Uses the package-private {@code AboutBox(boolean show)} constructor
 * (show=false) to build the dialog without displaying it, avoiding the
 * {@code Mediator.getUI()} call and the blocking {@code setVisible(true)}.
 *
 * NOTE: These tests hang on macOS when run via Ant outside the EDT (a
 * macOS AppKit/main-thread constraint). They pass correctly under
 * Linux with {@code xvfb-run} as used in CI.
 *
 * Part of issue #579 (GUI code coverage).
 */
public class AboutBoxTest extends TestCase {

	public void testAboutBox() {
		Locale.setDefault(Locale.ENGLISH);
		AboutBox dialog = new AboutBox(false);
		try {
			assertEquals("About VStar", dialog.getTitle());
			assertTrue(dialog.isModal());

			String text = dialog.getAboutBoxText();
			assertNotNull(text);
			assertTrue(text.length() > 0);
			assertTrue(text.contains("VStar"));
			assertTrue(text.contains("David Benn"));
			assertTrue(text.contains("GNU Affero General Public License"));
			assertTrue(text.contains("aavso.org"));
		} finally {
			dialog.dispose();
		}
	}
}
