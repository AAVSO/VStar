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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import junit.framework.TestCase;

import org.aavso.tools.vstar.data.ValidObservation;

/**
 * Tests for {@link SeriesTypeCreationDialog}.
 *
 * The {@link SeriesTypeCreationDialog} constructor does NOT call
 * {@code Mediator.getUI()} or {@code setVisible(true)} — it only calls
 * {@code pack()}. Therefore it can be constructed in a headless test without
 * any special constructor variant.
 *
 * Part of issue #579 (GUI code coverage).
 */
public class SeriesTypeCreationDialogTest extends TestCase {

	private List<ValidObservation> emptyObs;

	@Override
	protected void setUp() {
		Locale.setDefault(Locale.ENGLISH);
		emptyObs = new ArrayList<ValidObservation>();
	}

	public void testConstruction() {
		SeriesTypeCreationDialog d = new SeriesTypeCreationDialog(emptyObs);
		try {
			assertNotNull(d);
		} finally {
			d.dispose();
		}
	}

	public void testTitle() {
		SeriesTypeCreationDialog d = new SeriesTypeCreationDialog(emptyObs);
		try {
			assertEquals("Create Series", d.getTitle());
		} finally {
			d.dispose();
		}
	}

	public void testIsModal() {
		SeriesTypeCreationDialog d = new SeriesTypeCreationDialog(emptyObs);
		try {
			assertTrue(d.isModal());
		} finally {
			d.dispose();
		}
	}

	public void testIsCancelledByDefault() {
		SeriesTypeCreationDialog d = new SeriesTypeCreationDialog(emptyObs);
		try {
			assertTrue(d.isCancelled());
		} finally {
			d.dispose();
		}
	}

	public void testCancelActionLeavesDialogCancelled() {
		SeriesTypeCreationDialog d = new SeriesTypeCreationDialog(emptyObs);
		d.cancelAction();
		assertTrue(d.isCancelled());
		d.dispose();
	}
}
