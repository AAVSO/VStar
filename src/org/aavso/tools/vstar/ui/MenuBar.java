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
package org.aavso.tools.vstar.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import org.aavso.tools.vstar.data.InvalidObservation;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.input.ObservationRetrieverBase;
import org.aavso.tools.vstar.input.SimpleTextFormatReader;
import org.aavso.tools.vstar.ui.model.InvalidObservationDataModel;
import org.aavso.tools.vstar.ui.model.ValidObservationDataModel;

/**
 * VStar's menu bar.
 */
public class MenuBar extends JMenuBar {

	// TODO: we may need more instances for different file types
	private JFileChooser fileOpenDialog;

	// The parent window.
	private MainFrame parent;

	/**
	 * Constructor
	 * 
	 * @param dataListModel
	 *            Model for valid observations.
	 * @param dataErrorListModel
	 *            Model for invalid (erroneous) observations.
	 */
	public MenuBar(MainFrame parent) {
		super();

		this.parent = parent;

		List<String> extensions = new ArrayList<String>();
		extensions.add("sim");
		extensions.add("simple");
		this.fileOpenDialog = new JFileChooser();
		this.fileOpenDialog.setFileFilter(new FileExtensionFilter(extensions));

		JMenu fileMenu = new JMenu("File");

		JMenuItem fileOpenItem = new JMenuItem("Open...");
		fileOpenItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
				ActionEvent.META_MASK));
		fileOpenItem.addActionListener(createFileOpenListener());
		fileMenu.add(fileOpenItem);

		fileMenu.addSeparator();

		JMenuItem quitItem = new JMenuItem("Quit", KeyEvent.VK_Q);
		quitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,
				ActionEvent.META_MASK));
		quitItem.addActionListener(createQuitListener());
		fileMenu.add(quitItem);

		this.add(fileMenu);

		JMenu helpMenu = new JMenu("Help");

		JMenuItem helpContentsItem = new JMenuItem("Help Contents...",
				KeyEvent.VK_H);
		helpContentsItem.setEnabled(false);
		helpMenu.add(helpContentsItem);

		helpMenu.addSeparator();

		JMenuItem aboutItem = new JMenuItem("About...", KeyEvent.VK_A);
		aboutItem.addActionListener(createAboutListener());
		helpMenu.add(aboutItem);

		this.add(helpMenu);
	}

	/**
	 * Returns the action listener to be invoked for File->Open...
	 * 
	 * The action is to open a file dialog to allow the user to select a single
	 * file.
	 */
	private ActionListener createFileOpenListener() {
		final JFileChooser fileOpenDialog = this.fileOpenDialog;
		final MainFrame parent = this.parent;

		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int returnVal = fileOpenDialog.showOpenDialog(parent);

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File f = fileOpenDialog.getSelectedFile();

					try {
						FileReader fileReader = new FileReader(f.getPath());
						
						// TODO: Use a factory method to determine observation
						// retriever class to use given the file type.
						ObservationRetrieverBase simpleTextFormatReader = new SimpleTextFormatReader(
								new LineNumberReader(fileReader));

						simpleTextFormatReader.retrieveObservations();
						List<ValidObservation> validObs = simpleTextFormatReader
								.getValidObservations();
						List<InvalidObservation> invalidObs = simpleTextFormatReader
								.getInvalidObservations();

						// TODO: we need a DocManager class to store a mapping from
						// data files to tabs/observation lists, and also to handle
						// undo, document "is-dirty" handling etc.
						
						// Add a new tab with the observation data.

						ValidObservationDataModel validObsModel = null;
						InvalidObservationDataModel invalidObsModel = null;

						if (!validObs.isEmpty()) {
							validObsModel = new ValidObservationDataModel(
									validObs);
						}

						if (!invalidObs.isEmpty()) {
							invalidObsModel = new InvalidObservationDataModel(
									invalidObs);
						}

						parent.getTabs().insertTab(
								f.getName() + " Data",
								null, // TODO: add a nice icon; also need a close box
								new SimpleTextFormatObservationPane(
										validObsModel, invalidObsModel),
								f.getPath(), 0);
						
						parent.getTabs().setSelectedIndex(0);

					} catch (Exception ex) {
						MessageBox
								.showErrorDialog(parent,
										"Simple Text Format File Open", ex
												.getMessage());
					}

				}
			}
		};
	}

	/**
	 * Returns the action listener to be invoked for File->Quit
	 */
	private ActionListener createQuitListener() {
		return new ActionListener() {
			// TODO: do other cleanup, e.g. if file needs saving (phase II);
			//       need a document model including undo for this 
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		};
	}

	/**
	 * Returns the action listener to be invoked for Help->About...
	 */
	private ActionListener createAboutListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				StringBuffer strBuf = new StringBuffer();
				strBuf.append("VStar\n\n");
				strBuf.append("A variable star data statistical analysis tool\n");
				strBuf.append("developed for:\n\n");
				strBuf.append("  The American Association of Variable Star\n");
				strBuf.append("  Observers: http://www.aavso.org/\n\n");
				strBuf.append("  and\n\n");
				strBuf.append("  The CitizenSky Project: http://www.citizensky.org/");
				MessageBox.showMessageDialog(parent, "VStar", strBuf.toString());
			}
		};
	}
}
