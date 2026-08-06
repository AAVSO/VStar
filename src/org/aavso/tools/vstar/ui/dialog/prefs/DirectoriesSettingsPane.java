/**
 * VStar: a statistical analysis tool for variable star data.
 * Copyright (C) 2026  AAVSO (http://www.aavso.org/)
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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.aavso.tools.vstar.ui.mediator.DocumentManager;
import org.aavso.tools.vstar.util.locale.LocaleProps;
import org.aavso.tools.vstar.util.prefs.FilePathPrefs;

/**
 * Default observation source and sink directory preferences panel.
 */
@SuppressWarnings("serial")
public class DirectoriesSettingsPane extends JPanel implements
		IPreferenceComponent {

	private JTextField sourceDirField;
	private JTextField sinkDirField;
	private JFileChooser dirChooser;

	/**
	 * Constructor.
	 */
	public DirectoriesSettingsPane() {
		super();

		JPanel directoriesPane = new JPanel();
		directoriesPane.setLayout(new BoxLayout(directoriesPane,
				BoxLayout.PAGE_AXIS));
		directoriesPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		dirChooser = new JFileChooser();
		dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		sourceDirField = new JTextField(FilePathPrefs.getObsSourceDir(), 40);
		directoriesPane.add(createDirectoryRow("Source directory",
				"Default directory for opening observation files",
				sourceDirField, createBrowseListener(true)));

		directoriesPane.add(Box.createRigidArea(new Dimension(10, 10)));

		sinkDirField = new JTextField(FilePathPrefs.getObsSinkDir(), 40);
		directoriesPane.add(createDirectoryRow("Sink directory",
				"Default directory for saving observation lists and plot images",
				sinkDirField, createBrowseListener(false)));

		directoriesPane.add(Box.createRigidArea(new Dimension(10, 10)));

		directoriesPane.add(createButtonPane());

		this.add(directoriesPane);
	}

	private JPanel createDirectoryRow(String title, String toolTip,
			JTextField field, ActionListener browseListener) {
		JPanel row = new JPanel();
		row.setLayout(new BoxLayout(row, BoxLayout.LINE_AXIS));

		field.setToolTipText(toolTip);
		field.setBorder(BorderFactory.createTitledBorder(title));
		row.add(field);

		JButton browseButton = new JButton("Browse...");
		browseButton.addActionListener(browseListener);
		row.add(browseButton);

		return row;
	}

	private JPanel createButtonPane() {
		JPanel panel = new JPanel(new BorderLayout());

		JButton setDefaultsButton = new JButton("Set Defaults");
		setDefaultsButton
				.addActionListener(createSetDefaultsButtonActionListener());
		panel.add(setDefaultsButton, BorderLayout.LINE_START);

		JButton applyButton = new JButton(LocaleProps.get("APPLY_BUTTON"));
		applyButton.addActionListener(createApplyButtonActionListener());
		panel.add(applyButton, BorderLayout.LINE_END);

		return panel;
	}

	private ActionListener createBrowseListener(final boolean forSource) {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JTextField field = forSource ? sourceDirField : sinkDirField;
				String current = field.getText().trim();
				if (!current.isEmpty()) {
					File currentDir = new File(current);
					if (currentDir.isDirectory()) {
						dirChooser.setCurrentDirectory(currentDir);
					}
				}

				int retVal = dirChooser.showOpenDialog(DocumentManager
						.findActiveWindow());

				if (retVal == JFileChooser.APPROVE_OPTION) {
					File file = dirChooser.getSelectedFile();
					if (file.isDirectory()) {
						field.setText(file.getAbsolutePath());
					}
				}
			}
		};
	}

	private ActionListener createSetDefaultsButtonActionListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sourceDirField.setText(FilePathPrefs.DEFAULT_DIR);
				sinkDirField.setText(FilePathPrefs.DEFAULT_DIR);
			}
		};
	}

	private ActionListener createApplyButtonActionListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				update();
			}
		};
	}

	/**
	 * Updates the stored preferences from the UI.
	 */
	@Override
	public void update() {
		FilePathPrefs.setObsSourceDir(sourceDirField.getText().trim());
		FilePathPrefs.setObsSinkDir(sinkDirField.getText().trim());
	}

	/**
	 * Prepare this pane for use by resetting whatever state needs to be.
	 */
	@Override
	public void reset() {
		sourceDirField.setText(FilePathPrefs.getObsSourceDir());
		sinkDirField.setText(FilePathPrefs.getObsSinkDir());
	}
}
