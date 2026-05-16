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
 * Tests for {@link PhaseParameterDialog}.
 *
 * The {@link PhaseParameterDialog} constructor does NOT call
 * {@code Mediator.getUI()} — that call is deferred to {@code showDialog()}.
 * Therefore the dialog can be constructed in a headless test without any
 * special constructor variant.
 *
 * Part of issue #579 (GUI code coverage).
 */
public class PhaseParameterDialogTest extends TestCase {

	@Override
	protected void setUp() {
		Locale.setDefault(Locale.ENGLISH);
	}

	public void testConstruction() {
		PhaseParameterDialog d = new PhaseParameterDialog();
		try {
			assertNotNull(d);
		} finally {
			d.dispose();
		}
	}

	public void testTitle() {
		PhaseParameterDialog d = new PhaseParameterDialog();
		try {
			assertEquals("Phase Plot", d.getTitle());
		} finally {
			d.dispose();
		}
	}

	public void testIsModal() {
		PhaseParameterDialog d = new PhaseParameterDialog();
		try {
			assertTrue(d.isModal());
		} finally {
			d.dispose();
		}
	}

	public void testInitialPeriodIsZero() {
		PhaseParameterDialog d = new PhaseParameterDialog();
		try {
			assertEquals(0.0, d.getPeriod(), 0.0);
		} finally {
			d.dispose();
		}
	}

	public void testInitialEpochIsZero() {
		PhaseParameterDialog d = new PhaseParameterDialog();
		try {
			assertEquals(0.0, d.getEpoch(), 0.0);
		} finally {
			d.dispose();
		}
	}

	public void testSetPeriodField() {
		PhaseParameterDialog d = new PhaseParameterDialog();
		try {
			d.setPeriodField(3.14);
			// period field set to text; the numeric value is only updated by okAction
			assertEquals(0.0, d.getPeriod(), 0.0);
		} finally {
			d.dispose();
		}
	}

	public void testSetEpochField() {
		PhaseParameterDialog d = new PhaseParameterDialog();
		try {
			d.setEpochField(2450000.0);
			// epoch field set to text; the numeric value is only updated by okAction
			assertEquals(0.0, d.getEpoch(), 0.0);
		} finally {
			d.dispose();
		}
	}
}
