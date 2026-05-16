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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;

import junit.framework.TestCase;

/**
 * Tests for {@link RadioButtonDialog}.
 *
 * Uses the package-private 4-argument constructor (show=false) to build the
 * dialog without displaying it, avoiding the {@code Mediator.getUI()} call and
 * the blocking {@code setVisible(true)}.
 *
 * Part of issue #579 (GUI code coverage).
 */
public class RadioButtonDialogTest extends TestCase {

	@Override
	protected void setUp() {
		Locale.setDefault(Locale.ENGLISH);
	}

	private static Collection<String> options(String... vals) {
		return new LinkedHashSet<String>(Arrays.asList(vals));
	}

	public void testConstruction() {
		Collection<String> opts = options("Alpha", "Beta", "Gamma");
		RadioButtonDialog d = new RadioButtonDialog("Choose", opts, "Alpha", false);
		try {
			assertNotNull(d);
		} finally {
			d.dispose();
		}
	}

	public void testTitle() {
		Collection<String> opts = options("A", "B");
		RadioButtonDialog d = new RadioButtonDialog("Pick One", opts, "A", false);
		try {
			assertEquals("Pick One", d.getTitle());
		} finally {
			d.dispose();
		}
	}

	public void testIsModal() {
		Collection<String> opts = options("X", "Y");
		RadioButtonDialog d = new RadioButtonDialog("T", opts, "X", false);
		try {
			assertTrue(d.isModal());
		} finally {
			d.dispose();
		}
	}

	public void testIsCancelledByDefault() {
		Collection<String> opts = options("Red", "Blue");
		RadioButtonDialog d = new RadioButtonDialog("Color", opts, "Red", false);
		try {
			assertTrue(d.isCancelled());
		} finally {
			d.dispose();
		}
	}

	public void testInitialOptionIsSelectedOption() {
		Collection<String> opts = options("Red", "Blue", "Green");
		RadioButtonDialog d = new RadioButtonDialog("Color", opts, "Blue", false);
		try {
			assertEquals("Blue", d.getSelectedOption());
		} finally {
			d.dispose();
		}
	}

	public void testOkActionClearsCancelledFlag() {
		Collection<String> opts = options("Yes", "No");
		RadioButtonDialog d = new RadioButtonDialog("Confirm", opts, "Yes", false);
		assertTrue(d.isCancelled());
		d.okAction();
		assertFalse(d.isCancelled());
	}

	public void testCancelActionLeavesDialogCancelled() {
		Collection<String> opts = options("Yes", "No");
		RadioButtonDialog d = new RadioButtonDialog("Confirm", opts, "Yes", false);
		d.cancelAction();
		assertTrue(d.isCancelled());
		d.dispose();
	}

	public void testSingleOptionConstruction() {
		Collection<String> opts = Collections.singleton("Only");
		RadioButtonDialog d = new RadioButtonDialog("Solo", opts, "Only", false);
		try {
			assertEquals("Only", d.getSelectedOption());
		} finally {
			d.dispose();
		}
	}
}
