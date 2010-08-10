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
 * This dialog allows the use to enter the name of a new star group.
 */
public class NewGroupDialog extends AbstractOkCancelDialog {

	private JTextField groupNameField;

	private String groupName;
	
	public NewGroupDialog() {
		super("Add group");

		setAlwaysOnTop(true);
		
		JPanel topPane = new JPanel();
		topPane.setLayout(new BoxLayout(topPane, BoxLayout.PAGE_AXIS));
		topPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		topPane.add(createGroupNamePane());
		topPane.add(createButtonPane());
		
		getContentPane().add(topPane);

		this.pack();
	}
	
	private JPanel createGroupNamePane() {
		JPanel panel = new JPanel();

		panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
		panel.setBorder(BorderFactory.createTitledBorder("Group Name"));

		groupNameField = new JTextField();
		groupNameField.setToolTipText("Enter new group name");
		panel.add(groupNameField);

		return panel;
	}

	
	/**
	 * @return the groupName
	 */
	public String getGroupName() {
		return groupName;
	}

	/**
	 * @see org.aavso.tools.vstar.ui.dialog.AbstractOkCancelDialog#reset()
	 */
	@Override
	protected void reset() {
		groupName = null;
		groupNameField.requestFocusInWindow();
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
		String group = groupNameField.getText().trim();

		if (group.length() != 0) {
			groupName = group;
			setCancelled(false);
			setVisible(false);
			dispose();			
		}
	}
}
