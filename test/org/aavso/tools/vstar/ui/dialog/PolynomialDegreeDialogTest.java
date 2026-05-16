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
 * Tests for {@link PolynomialDegreeDialog}.
 *
 * Uses the package-private 3-argument constructor (show=false) to build the
 * dialog without displaying it, avoiding the {@code Mediator.getUI()} call and
 * the blocking {@code setVisible(true)}.
 *
 * Part of issue #579 (GUI code coverage).
 */
public class PolynomialDegreeDialogTest extends TestCase {

	@Override
	protected void setUp() {
		Locale.setDefault(Locale.ENGLISH);
	}

	public void testConstruction() {
		PolynomialDegreeDialog d = new PolynomialDegreeDialog(1, 10, false);
		try {
			assertNotNull(d);
		} finally {
			d.dispose();
		}
	}

	public void testTitle() {
		PolynomialDegreeDialog d = new PolynomialDegreeDialog(1, 10, false);
		try {
			assertEquals("Polynomial Degree", d.getTitle());
		} finally {
			d.dispose();
		}
	}

	public void testIsModal() {
		PolynomialDegreeDialog d = new PolynomialDegreeDialog(1, 10, false);
		try {
			assertTrue(d.isModal());
		} finally {
			d.dispose();
		}
	}

	public void testIsCancelledByDefault() {
		PolynomialDegreeDialog d = new PolynomialDegreeDialog(1, 10, false);
		try {
			assertTrue(d.isCancelled());
		} finally {
			d.dispose();
		}
	}

	public void testInitialDegreeIsHalfRange() {
		PolynomialDegreeDialog d = new PolynomialDegreeDialog(0, 10, false);
		try {
			assertEquals(5, d.getDegree());
		} finally {
			d.dispose();
		}
	}

	public void testInitialDegreeWithMinOne() {
		PolynomialDegreeDialog d = new PolynomialDegreeDialog(1, 11, false);
		try {
			assertEquals(5, d.getDegree());
		} finally {
			d.dispose();
		}
	}

	public void testOkActionClearsCancelledFlag() {
		PolynomialDegreeDialog d = new PolynomialDegreeDialog(1, 10, false);
		assertTrue(d.isCancelled());
		d.okAction();
		assertFalse(d.isCancelled());
	}

	public void testCancelActionLeavesDialogCancelled() {
		PolynomialDegreeDialog d = new PolynomialDegreeDialog(1, 10, false);
		d.cancelAction();
		assertTrue(d.isCancelled());
		d.dispose();
	}
}
