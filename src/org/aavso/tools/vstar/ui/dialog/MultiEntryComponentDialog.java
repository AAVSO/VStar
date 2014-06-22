/**
 * VStar: a statistical analysis tool for variable star data.
 * Copyright (C) 2010  AAVSO (http://www.aavso.org/)
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

import java.awt.Container;
import java.awt.Dimension;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.aavso.tools.vstar.ui.mediator.Mediator;

/**
 * This dialog class permits multiple named, ranged, numeric (double) values to
 * be entered and returned. The dialog can be dismissed when legal values are
 * present in each text field. The number numberFields passed in will contain
 * these values.
 */
@SuppressWarnings("serial")
public class MultiEntryComponentDialog extends AbstractOkCancelDialog {

	private List<ITextComponent<?>> fields;

	/**
	 * Constructor
	 * 
	 * @param title
	 *            Title for the dialog.
	 * @param textFields
	 *            The list of text fields.
	 * @param numberFields
	 *            The list of number fields.
	 */
	public MultiEntryComponentDialog(String title,
			List<ITextComponent<?>> fields) {
		super(title);
		this.fields = fields;

		Container contentPane = this.getContentPane();

		JPanel topPane = new JPanel();
		topPane.setLayout(new BoxLayout(topPane, BoxLayout.PAGE_AXIS));
		topPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		topPane.add(createParameterPane());

		// OK, Cancel
		topPane.add(createButtonPane());

		contentPane.add(topPane);

		this.pack();
		setLocationRelativeTo(Mediator.getUI().getContentPane());
		this.setVisible(true);
	}

	// Add the fields.
	private JPanel createParameterPane() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

		for (ITextComponent<?> field : fields) {
			field.setEditable(!field.isReadOnly());
			panel.add(field.getUIComponent());
			panel.add(Box.createRigidArea(new Dimension(75, 10)));
		}

		return panel;
	}

	/**
	 * @see org.aavso.tools.vstar.ui.dialog.AbstractOkCancelDialog#cancelAction()
	 */
	@Override
	protected void cancelAction() {
		// Nothing to do.
	}

	/**
	 * @see org.aavso.tools.vstar.ui.dialog.AbstractOkCancelDialog#okAction()
	 */
	@Override
	protected void okAction() {
		boolean ok = true;

		// If there is a field that is returning a null value or a field that
		// cannot be empty, but is, we cannot dismiss the dialog. Asking whether
		// a field value is null is OK because if it's a string, that can never
		// be the case and if it's a numeric field, it can be.
		for (ITextComponent<?> field : fields) {
			if (field.getValue() == null || !field.canBeEmpty()
					&& field.getStringValue().trim().length() == 0) {
				ok = false;
				break;
			}
		}

		if (ok) {
			cancelled = false;
			setVisible(false);
			dispose();
		}
	}
}
