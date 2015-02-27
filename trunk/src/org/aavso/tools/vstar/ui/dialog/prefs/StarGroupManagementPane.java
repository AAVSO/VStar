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

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

import org.aavso.tools.vstar.input.database.AAVSODatabaseConnector;
import org.aavso.tools.vstar.ui.dialog.MessageBox;
import org.aavso.tools.vstar.ui.dialog.StarGroupSelectionPane;
import org.aavso.tools.vstar.ui.mediator.StarInfo;
import org.aavso.tools.vstar.ui.resources.PropertiesAccessor;
import org.aavso.tools.vstar.ui.resources.StarGroups;
import org.aavso.tools.vstar.util.locale.LocaleProps;

/**
 * This is a preferences pane for managing star groups.
 */
@SuppressWarnings("serial")
public class StarGroupManagementPane extends JPanel implements
		IPreferenceComponent {

	private final static String DEFAULT_GROUP_ERROR_MSG_FMT = "Cannot change '%s' group";

	private String defaultGroupName;

	private StarGroupSelectionPane starGroupSelectionPane;
	private StarGroups starGroups;

	private NewGroupDialog newGroupDialog;
	private NewStarDialog newStarDialog;
	private NewGroupWithStarsDialog newGroupWithStarsDialog;

	/**
	 * Constructor.
	 */
	public StarGroupManagementPane() {
		defaultGroupName = PropertiesAccessor.getStarListTitle();

		starGroupSelectionPane = new StarGroupSelectionPane(null);
		starGroups = starGroupSelectionPane.getStarGroups();

		newGroupDialog = new NewGroupDialog();
		newStarDialog = new NewStarDialog();
		newGroupWithStarsDialog = new NewGroupWithStarsDialog();

		JPanel topPane = new JPanel();
		topPane.setLayout(new BoxLayout(topPane, BoxLayout.PAGE_AXIS));
		topPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		topPane.add(starGroupSelectionPane);
		topPane.add(Box.createRigidArea(new Dimension(20, 20)));
		topPane.add(createGroupButtonPane());
		topPane.add(Box.createRigidArea(new Dimension(20, 20)));
		topPane.add(createStarButtonPane());
		topPane.add(Box.createRigidArea(new Dimension(20, 20)));
		topPane.add(createGroupWithStarsButtonPane());
		topPane.add(Box.createRigidArea(new Dimension(20, 20)));
		topPane.add(createClearButtonPane());
		topPane.add(Box.createRigidArea(new Dimension(20, 20)));
		topPane.add(createApplyButtonPane());

		this.add(topPane);
	}

	private JPanel createGroupButtonPane() {
		JPanel panel = new JPanel(new BorderLayout());

		JButton addGroupButton = new JButton("Add Group");
		addGroupButton.addActionListener(createAddGroupButtonActionListener());
		panel.add(addGroupButton, BorderLayout.LINE_START);

		JButton deleteGroupButton = new JButton("Delete Group");
		deleteGroupButton
				.addActionListener(createDeleteGroupButtonActionListener());
		panel.add(deleteGroupButton, BorderLayout.LINE_END);

		return panel;
	}

	private JPanel createStarButtonPane() {
		JPanel panel = new JPanel(new BorderLayout());

		JButton addStarButton = new JButton("Add Star");
		addStarButton.addActionListener(createAddStarButtonActionListener());
		panel.add(addStarButton, BorderLayout.LINE_START);

		JButton deleteStarButton = new JButton("Delete Star");
		deleteStarButton
				.addActionListener(createDeleteStarButtonActionListener());
		panel.add(deleteStarButton, BorderLayout.LINE_END);

		return panel;
	}

	private JPanel createGroupWithStarsButtonPane() {
		JPanel panel = new JPanel(new BorderLayout());

		JButton addGroupAndStarsButton = new JButton("Add Group & Stars");
		addGroupAndStarsButton
				.addActionListener(createGroupWithStarsButtonActionListener());
		panel.add(addGroupAndStarsButton, BorderLayout.CENTER);

		return panel;
	}

	private JPanel createClearButtonPane() {
		JPanel panel = new JPanel(new BorderLayout());

		JButton clearButton = new JButton("Clear");
		clearButton.addActionListener(createClearButtonActionListener());
		panel.add(clearButton, BorderLayout.CENTER);

		return panel;
	}

	private JPanel createApplyButtonPane() {
		JPanel panel = new JPanel(new BorderLayout());

		JButton applyButton = new JButton(LocaleProps.get("APPLY_BUTTON"));
		applyButton.addActionListener(createApplyButtonActionListener());
		panel.add(applyButton, BorderLayout.CENTER);

		return panel;
	}

	// Create add-star-group button listener.
	private ActionListener createAddGroupButtonActionListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				newGroupDialog.showDialog();
				String newGroup = newGroupDialog.getGroupName();

				if (starGroups.doesGroupExist(newGroup)) {
					error(String.format("The group already '%s' exists.",
							newGroup));
				} else {
					starGroupSelectionPane.addGroup(newGroup);
				}
			}
		};
	}

	// Create delete-star-group button listener.
	private ActionListener createDeleteGroupButtonActionListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String groupName = starGroupSelectionPane
						.getSelectedStarGroupName();

				if (defaultGroupName.equals(groupName)) {
					error(String.format(DEFAULT_GROUP_ERROR_MSG_FMT,
							defaultGroupName));
				} else {
					starGroupSelectionPane.removeGroup(groupName);
				}
			}
		};
	}

	// Create add-star button listener.
	private ActionListener createAddStarButtonActionListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String groupName = starGroupSelectionPane
						.getSelectedStarGroupName();

				if (defaultGroupName.equals(groupName)) {
					error(String.format(DEFAULT_GROUP_ERROR_MSG_FMT,
							defaultGroupName));
				} else {
					newStarDialog.showDialog();
					String newStar = newStarDialog.getStarName();

					if (starGroups.doesStarExistInGroup(groupName, newStar)) {
						error(String.format(
								"The star '%s' exists in the group '%s'.",
								newStar, groupName));
					} else {
						addStar(groupName, newStar);
					}
				}
			}
		};
	}

	// Create delete-star button listener.
	private ActionListener createDeleteStarButtonActionListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String groupName = starGroupSelectionPane
						.getSelectedStarGroupName();

				if (defaultGroupName.equals(groupName)) {
					error(String.format(DEFAULT_GROUP_ERROR_MSG_FMT,
							defaultGroupName));
				} else {
					String starName = starGroupSelectionPane
							.getSelectedStarName();
					starGroupSelectionPane.removeStar(groupName, starName);
				}
			}
		};
	}

	// Create add-group-with-stars button listener.
	private ActionListener createGroupWithStarsButtonActionListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				newGroupWithStarsDialog.showDialog();
				if (!newGroupWithStarsDialog.isCancelled()) {
					String newGroup = newGroupWithStarsDialog.getGroupName();
					String[] stars = newGroupWithStarsDialog.getStarList();
					if (starGroups.doesGroupExist(newGroup)) {
						error(String.format("The group already '%s' exists.",
								newGroup));
					} else {
						starGroupSelectionPane.addGroup(newGroup);
						for (String starName : stars) {
							addStar(newGroup, starName);
						}
					}
				}
			}
		};
	}

	// Validate a star's AUID and add it to the specified star group.
	private void addStar(String groupName, String starName) {
		String auid = retrieveAUID(starName);
		if (auid != null) {
			starGroupSelectionPane.addStar(groupName, starName, auid);
		} else {
			error(String.format("Unknown star: '%s'.", starName));
		}
	}

	// Create clear button listener.
	private ActionListener createClearButtonActionListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				starGroupSelectionPane.resetGroups();
			}
		};
	}

	// Create apply button listener.
	private ActionListener createApplyButtonActionListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				update();
			}
		};
	}

	@Override
	public void update() {
		starGroups.storeStarGroupPrefs();
	}

	@Override
	public void reset() {
		// Nothing to do.
	}

	// Helpers

	private void error(String msg) {
		MessageBox.showErrorDialog(this, "Star Group Error", msg);
	}

	/**
	 * Retrieve the AUID of the specified star from the database, first
	 * prompting for authentication if necessary.
	 * 
	 * @param starName
	 *            The star whose AUID we want.
	 * @return The AUID or null if the star is not known.
	 */
	public String retrieveAUID(String starName) {
		String auid = null;

		Connection vsxConnection = null;
		try {
			getParent().setCursor(
					Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

			AAVSODatabaseConnector vsxConnector = AAVSODatabaseConnector.vsxDBConnector;
			vsxConnection = vsxConnector.getConnection();

			StarInfo starInfo = vsxConnector.getAUID(vsxConnection, starName);
			auid = starInfo.getAuid();

			getParent().setCursor(null);
		} catch (Exception e) {
			MessageBox.showErrorDialog(this,
					"Star Information Retrieval Error", e);
		} finally {
//			try {
//				if (vsxConnection != null) {
//					vsxConnection.close();
//				}
//			} catch (SQLException e) {
//				MessageBox.showErrorDialog(this,
//						"Star Information Retrieval Error", e);
//			}
		}

		return auid;
	}
}
