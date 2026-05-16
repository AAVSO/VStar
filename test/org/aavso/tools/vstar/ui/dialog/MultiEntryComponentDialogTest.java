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
import java.util.Locale;
import java.util.Optional;

import junit.framework.TestCase;

/**
 * Tests for {@link MultiEntryComponentDialog}.
 *
 * Uses the package-private 5-argument constructor (show=false) to build
 * dialogs without displaying them, avoiding the {@code Mediator.getUI()} call
 * and the blocking {@code setVisible(true)}.
 *
 * Notes on scope:
 * - okAction() with INVALID values is NOT tested here because it calls
 *   {@code MessageBox.showErrorDialog()}, which creates a modal
 *   {@code JOptionPane} dialog and calls {@code setVisible(true)} —
 *   that would block the test runner when Mediator.getUI()==null.
 * - When okAction() SUCCEEDS it calls dispose() internally; the test
 *   must NOT call dispose() a second time to avoid a double-dispose crash.
 *
 * Part of issue #579 (GUI code coverage).
 */
public class MultiEntryComponentDialogTest extends TestCase {

	@Override
	protected void setUp() {
		Locale.setDefault(Locale.ENGLISH);
	}

	private MultiEntryComponentDialog build(ITextComponent<?>... fields) {
		return new MultiEntryComponentDialog(
				"Test", null, Arrays.asList(fields), Optional.empty(), false);
	}

	private MultiEntryComponentDialog buildWithHelp(
			String helpTopic, ITextComponent<?>... fields) {
		return new MultiEntryComponentDialog(
				"Test", helpTopic, Arrays.asList(fields), Optional.empty(), false);
	}

	// -----------------------------------------------------------
	// Construction and initial state
	// -----------------------------------------------------------

	public void testConstructionWithSingleDoubleField() {
		DoubleField f = new DoubleField("Period", 0.0, 100.0, 5.0);
		MultiEntryComponentDialog d = build(f);
		assertNotNull(d);
		d.dispose();
	}

	public void testConstructionWithMultipleFields() {
		DoubleField f1 = new DoubleField("Min", 0.0, 50.0, 1.0);
		DoubleField f2 = new DoubleField("Max", 0.0, 50.0, 10.0);
		MultiEntryComponentDialog d = build(f1, f2);
		assertNotNull(d);
		d.dispose();
	}

	public void testConstructionWithHelpTopic() {
		DoubleField f = new DoubleField("Value", null, null, 1.0);
		MultiEntryComponentDialog d = buildWithHelp("http://example.com/help", f);
		assertNotNull(d);
		d.dispose();
	}

	public void testIsCancelledByDefault() {
		DoubleField f = new DoubleField("Value", null, null, 1.0);
		MultiEntryComponentDialog d = build(f);
		assertTrue(d.isCancelled());
		d.dispose();
	}

	public void testIsModal() {
		DoubleField f = new DoubleField("Value", null, null, 1.0);
		MultiEntryComponentDialog d = build(f);
		assertTrue(d.isModal());
		d.dispose();
	}

	public void testTitle() {
		DoubleField f = new DoubleField("Value", null, null, 1.0);
		MultiEntryComponentDialog d = build(f);
		assertEquals("Test", d.getTitle());
		d.dispose();
	}

	// -----------------------------------------------------------
	// okAction with valid field values
	// (invalid-value tests omitted: okAction shows a blocking modal
	//  error dialog via MessageBox when Mediator.getUI()==null)
	// -----------------------------------------------------------

	public void testOkActionWithValidFieldClearsCancelledFlag() {
		DoubleField f = new DoubleField("Period", 0.0, 100.0, 5.0);
		MultiEntryComponentDialog d = build(f);
		assertTrue(d.isCancelled());
		d.okAction();
		// okAction() disposes the dialog on success; do NOT call d.dispose() again.
		assertFalse("okAction() with valid value should clear cancelled flag",
				d.isCancelled());
	}

	// -----------------------------------------------------------
	// cancelAction
	// -----------------------------------------------------------

	public void testCancelActionLeavesDialogCancelled() {
		DoubleField f = new DoubleField("Value", null, null, 1.0);
		MultiEntryComponentDialog d = build(f);
		d.cancelAction();
		assertTrue(d.isCancelled());
		d.dispose();
	}

	// -----------------------------------------------------------
	// Additional UI component
	// -----------------------------------------------------------

	public void testConstructionWithAdditionalComponent() {
		DoubleField f = new DoubleField("Value", null, null, 1.0);
		javax.swing.JLabel extra = new javax.swing.JLabel("Note");
		MultiEntryComponentDialog d = new MultiEntryComponentDialog(
				"Test", null,
				Arrays.asList((ITextComponent<?>) f),
				Optional.of(extra),
				false);
		assertNotNull(d);
		d.dispose();
	}

	public void testConstructionWithEmptyOptional() {
		DoubleField f = new DoubleField("Value", null, null, 1.0);
		MultiEntryComponentDialog d = new MultiEntryComponentDialog(
				"Test", null,
				Arrays.asList((ITextComponent<?>) f),
				Optional.empty(),
				false);
		assertNotNull(d);
		d.dispose();
	}
}
