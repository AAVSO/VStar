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

import java.util.Collections;
import java.util.Locale;

import junit.framework.TestCase;

/**
 * Tests for {@link InfoDialog}.
 *
 * Uses the package-private {@code InfoDialog(messages, show=false)} constructor
 * to build the dialog without displaying it, bypassing the
 * {@code Mediator.getUI()} call and the blocking {@code setVisible(true)}.
 *
 * Note: text-content testing would require a fully populated
 * {@link org.aavso.tools.vstar.ui.mediator.message.NewStarMessage} with a
 * concrete {@link org.aavso.tools.vstar.input.AbstractObservationRetriever};
 * title and modal-flag verification are sufficient to exercise the constructor
 * path without that complexity.
 *
 * Part of issue #579 (GUI code coverage, prong D).
 */
public class InfoDialogTest extends TestCase {

	@Override
	protected void setUp() {
		Locale.setDefault(Locale.ENGLISH);
	}

	public void testTitle() {
		InfoDialog dialog = new InfoDialog(Collections.emptyList(), false);
		try {
			assertEquals("Information", dialog.getTitle());
		} finally {
			dialog.dispose();
		}
	}

	public void testNotModal() {
		InfoDialog dialog = new InfoDialog(Collections.emptyList(), false);
		try {
			assertFalse("InfoDialog should not be modal by default",
					dialog.isModal());
		} finally {
			dialog.dispose();
		}
	}
}
