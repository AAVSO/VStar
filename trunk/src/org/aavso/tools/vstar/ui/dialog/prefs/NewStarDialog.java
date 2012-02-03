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
package org.aavso.tools.vstar.ui.dialog.prefs;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.aavso.tools.vstar.ui.dialog.AbstractOkCancelDialog;

/**
 * This dialog allows the use to enter the name of a new star for a group.
 */
public class NewStarDialog extends AbstractOkCancelDialog {

	private JTextField starNameField;

	private String starName;

	public NewStarDialog() {
		super("Add star");

		JPanel topPane = new JPanel();
		topPane.setLayout(new BoxLayout(topPane, BoxLayout.PAGE_AXIS));
		topPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		topPane.add(createStarNamePane());
		topPane.add(createButtonPane());

		getContentPane().add(topPane);

		this.pack();
	}

	private JPanel createStarNamePane() {
		JPanel panel = new JPanel();

		panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
		panel.setBorder(BorderFactory.createTitledBorder("Star Name"));

		starNameField = new JTextField();
		starNameField.setToolTipText("Enter new star name");
		panel.add(starNameField);

		return panel;
	}

	/**
	 * @return the starName
	 */
	public String getStarName() {
		return starName;
	}

	/**
	 * @see org.aavso.tools.vstar.ui.dialog.AbstractOkCancelDialog#reset()
	 */
	@Override
	protected void reset() {
		starName = null;
		starNameField.requestFocusInWindow();
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
		String star = starNameField.getText().trim();

		if (star.length() != 0) {
			starName = star;
			setCancelled(false);
			setVisible(false);
			dispose();
		}
	}
}
