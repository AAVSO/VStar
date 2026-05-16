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

import org.aavso.tools.vstar.data.Magnitude;
import org.aavso.tools.vstar.data.ValidObservation;

/**
 * Tests for {@link DiscrepantReportDialog}.
 *
 * Uses the package-private 3-argument constructor (show=false) to build the
 * dialog without displaying it, avoiding the {@code Mediator.getUI()} call and
 * the blocking {@code setVisible(true)}.
 *
 * Part of issue #579 (GUI code coverage).
 */
public class DiscrepantReportDialogTest extends TestCase {

	private ValidObservation ob;

	@Override
	protected void setUp() {
		Locale.setDefault(Locale.ENGLISH);
		ob = new ValidObservation();
		ob.setName("R Car");
		ob.setJD(2458000.5);
		ob.setMagnitude(new Magnitude(7.8, 0.1));
	}

	public void testConstruction() {
		DiscrepantReportDialog d = new DiscrepantReportDialog("123-CVF", ob, false);
		try {
			assertNotNull(d);
		} finally {
			d.dispose();
		}
	}

	public void testTitle() {
		DiscrepantReportDialog d = new DiscrepantReportDialog("123-CVF", ob, false);
		try {
			assertEquals("AAVSO Discrepant Report", d.getTitle());
		} finally {
			d.dispose();
		}
	}

	public void testIsModal() {
		DiscrepantReportDialog d = new DiscrepantReportDialog("123-CVF", ob, false);
		try {
			assertTrue(d.isModal());
		} finally {
			d.dispose();
		}
	}

	public void testIsCancelledByDefault() {
		DiscrepantReportDialog d = new DiscrepantReportDialog("123-CVF", ob, false);
		try {
			assertTrue(d.isCancelled());
		} finally {
			d.dispose();
		}
	}

	public void testInitialCommentsAreEmpty() {
		DiscrepantReportDialog d = new DiscrepantReportDialog("123-CVF", ob, false);
		try {
			assertEquals("", d.getComments());
		} finally {
			d.dispose();
		}
	}

	public void testCancelActionLeavesDialogCancelled() {
		DiscrepantReportDialog d = new DiscrepantReportDialog("123-CVF", ob, false);
		d.cancelAction();
		assertTrue(d.isCancelled());
		d.dispose();
	}
}
