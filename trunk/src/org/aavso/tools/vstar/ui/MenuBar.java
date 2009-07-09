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

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import org.aavso.tools.vstar.ui.model.ModelManager;
import org.aavso.tools.vstar.ui.model.NewStarType;
import org.aavso.tools.vstar.ui.model.ProgressInfo;
import org.aavso.tools.vstar.util.Listener;

/**
 * VStar's menu bar.
 * 
 * TODO: - put menu item names in property file
 */
public class MenuBar extends JMenuBar {

	// TODO: factor out into a common class to be shared by menu and tool bar?
	public static final String NEW_STAR_FROM_DATABASE = "New Star from AAVSO Database...";
	public static final String NEW_STAR_FROM_FILE = "New Star from File...";

	private ModelManager modelMgr = ModelManager.getInstance();

	private JFileChooser fileOpenDialog;

	// The parent window.
	private MainFrame parent;

	// Menu items.
	JMenuItem fileNewStarFromDatabaseItem;
	JMenuItem fileNewStarFromFileItem;
	JMenuItem fileSaveItem;
	JMenuItem filePrintItem;
	JMenuItem fileQuitItem;

	JMenuItem analysisRawDataItem;
	JMenuItem analysisPhasePlotItem;
	JMenuItem analysisPeriodSearchItem;

	JMenuItem helpContentsItem;
	JMenuItem helpAboutItem;

	/**
	 * Constructor
	 */
	public MenuBar(MainFrame parent) {
		super();

		this.parent = parent;

		List<String> extensions = new ArrayList<String>();
		extensions.add("csv");
		extensions.add("tsv");
		extensions.add("txt");

		this.fileOpenDialog = new JFileChooser();
		this.fileOpenDialog.setFileFilter(new FileExtensionFilter(extensions));

		createFileMenu();
		createAnalysisMenu();
		createHelpMenu();

		this.modelMgr.getNewStarNotifier().addListener(createNewStarListener());
		this.modelMgr.getProgressNotifier().addListener(
				createProgressListener());
	}

	private void createFileMenu() {
		JMenu fileMenu = new JMenu("File");

		fileNewStarFromDatabaseItem = new JMenuItem(NEW_STAR_FROM_DATABASE);
		fileNewStarFromDatabaseItem
				.addActionListener(createNewStarFromDatabaseListener());
		fileMenu.add(fileNewStarFromDatabaseItem);

		fileNewStarFromFileItem = new JMenuItem(NEW_STAR_FROM_FILE);
		fileNewStarFromFileItem
				.addActionListener(createNewStarFromFileListener());
		fileMenu.add(fileNewStarFromFileItem);

		fileSaveItem = new JMenuItem("Save...");
		fileSaveItem.addActionListener(this.createSaveListener());
		fileSaveItem.setEnabled(false);
		fileMenu.add(fileSaveItem);

		filePrintItem = new JMenuItem("Print...");
		filePrintItem.addActionListener(this.createPrintListener());
		filePrintItem.setEnabled(false);
		fileMenu.add(filePrintItem);

		fileMenu.addSeparator();

		fileQuitItem = new JMenuItem("Quit", KeyEvent.VK_Q);
		// fileQuitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,
		// ActionEvent.META_MASK));
		fileQuitItem.addActionListener(createQuitListener());
		fileMenu.add(fileQuitItem);

		this.add(fileMenu);
	}

	private void createAnalysisMenu() {
		JMenu analysisMenu = new JMenu("Analysis");

		// TODO: add checkboxes for these menu items
		// see what NetBeans yields, see Sun docs

		// TODO: add listeners
		analysisRawDataItem = new JMenuItem("Raw Data");
		analysisRawDataItem.setEnabled(false);
		analysisMenu.add(analysisRawDataItem);

		analysisPhasePlotItem = new JMenuItem("Phase Plot...");
		analysisPhasePlotItem.setEnabled(false);
		analysisMenu.add(analysisPhasePlotItem);

		analysisPeriodSearchItem = new JMenuItem("Period Search...");
		analysisPeriodSearchItem.setEnabled(false);
		analysisMenu.add(analysisPeriodSearchItem);

		this.add(analysisMenu);
	}

	private void createHelpMenu() {
		JMenu helpMenu = new JMenu("Help");

		helpContentsItem = new JMenuItem("Help Contents...", KeyEvent.VK_H);
		helpContentsItem.addActionListener(createHelpContentsListener());
		helpMenu.add(helpContentsItem);

		helpMenu.addSeparator();

		helpAboutItem = new JMenuItem("About...", KeyEvent.VK_A);
		helpAboutItem.addActionListener(createAboutListener());
		helpMenu.add(helpAboutItem);

		this.add(helpMenu);
	}

	/**
	 * Returns the action listener to be invoked for File->New Star from AAVSO
	 * Database...
	 */
	public ActionListener createNewStarFromDatabaseListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MessageBox.showMessageDialog(parent, NEW_STAR_FROM_DATABASE,
						"This feature is not implemented yet.");
			}
		};
	}

	/**
	 * Returns the action listener to be invoked for File->New Star from File...
	 * 
	 * The action is to open a file dialog to allow the user to select a single
	 * file.
	 */
	public ActionListener createNewStarFromFileListener() {
		final JFileChooser fileOpenDialog = this.fileOpenDialog;
		final MainFrame parent = this.parent;

		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int returnVal = fileOpenDialog.showOpenDialog(parent);

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File f = fileOpenDialog.getSelectedFile();

					try {
						modelMgr.createObservationModelsFromFile(f, parent);
					} catch (Exception ex) {
						MessageBox.showErrorDialog(parent,
								"New Star from File", ex);
					}
				}
			}
		};
	}

	/**
	 * Returns the action listener to be invoked for File->Save...
	 */
	private ActionListener createSaveListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// try {
				// // TODO: need to ask model mgr to save curr model/doc
				// //parent.getLightCurveChartPane().doSaveAs();
				// } catch (IOException ex) {
				// MessageBox.showErrorDialog(parent, "Light Curve Save", ex
				// .getMessage());
				// }
			}
		};
	}

	/**
	 * Returns the action listener to be invoked for File->Print...
	 */
	private ActionListener createPrintListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// TODO: need to ask model mgr to print curr model/doc
				// parent.getLightCurveChartPane().createChartPrintJob();
			}
		};
	}

	/**
	 * Returns the action listener to be invoked for File->Quit
	 */
	private ActionListener createQuitListener() {
		return new ActionListener() {
			// TODO: do other cleanup, e.g. if file needs saving;
			// need a document model including undo for this
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		};
	}

	/**
	 * Returns the action listener to be invoked for Help->Help Contents...
	 */
	private ActionListener createHelpContentsListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				javax.swing.SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						HelpContentsDialog helpContentsDialog = new HelpContentsDialog();
						helpContentsDialog.pack();
						helpContentsDialog.setVisible(true);
					}
				});
			}
		};
	}

	/**
	 * Returns the action listener to be invoked for Help->About...
	 * 
	 * TODO: Make a separate component for the About Box? Possibly put text into
	 * a resource file and use a JEditorPane to render HTML?
	 */
	private ActionListener createAboutListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				StringBuffer strBuf = new StringBuffer();
				strBuf.append("VStar (alpha)\n\n");
				strBuf
						.append("A variable star observation data analysis tool\n");
				strBuf.append("developed for:\n\n");
				strBuf.append("  The American Association of Variable Star\n");
				strBuf.append("  Observers: http://www.aavso.org/\n\n");
				strBuf.append("  as part of\n\n");
				strBuf
						.append("  The CitizenSky Project: http://www.citizensky.org/\n\n");

				strBuf
						.append("This project was funded in part by grant No. 000379097\n");
				strBuf.append("from the National Science Foundation.\n\n");

				strBuf.append("Code by: David Benn\n");
				strBuf.append("Contact: aavso@aavso.org\n");
				strBuf.append("License: GNU Affero General Public License\n\n");
				strBuf
						.append("Images as appeared in Variable Star Astronomy at\n");
				strBuf.append("http://www.aavso.org/education/vsa/\n\n");

				strBuf
						.append("Thanks to the staff of AAVSO for their support, in particular:\n\n");
				strBuf
						.append(" Sara Beck, Arne Henden, Doc Kinne, Aaron Price,\n");
				strBuf
						.append(" Matt Templeton, Rebecca Turner, and Elizabeth Waagen.");

				MessageBox
						.showMessageDialog(parent, "VStar", strBuf.toString());
			}
		};
	}

	// Comment by Aaron in email (6 July 2009):
	//	
	// I think you can use anything in HOA or the Citizen Sky web site.
	// However, you'll need to retain whatever credit is shown in VSA and
	// maybe add "as appeared in VSA at
	// "http://www.aavso.org/education/vsa/".
	//
	// Add a credit to Citizen Sky and the National Science Foundation to
	// the About box (if you haven't already). A good NSF logo is here:
	// http://www.nsf.gov/policies/logos.jsp
	//
	// The NSF credit should be something like "This project was funded in
	// part by grant No. 000379097 from the National Science Foundation."

	/**
	 * Return a new star creation listener.
	 */
	private Listener<NewStarType> createNewStarListener() {
		return new Listener<NewStarType>() {
			public void update(NewStarType info) {
			}
		};
	}

	/**
	 * Return a progress listener.
	 */
	private Listener<ProgressInfo> createProgressListener() {
		final MenuBar self = this;
		final MainFrame parent = this.parent;
		return new Listener<ProgressInfo>() {
			public void update(ProgressInfo info) {
				switch (info.getType()) {
				case MIN_PROGRESS:
					break;
				case MAX_PROGRESS:
					break;
				case RESET_PROGRESS:
					parent.setCursor(Cursor
							.getPredefinedCursor(Cursor.WAIT_CURSOR));
					setEnabledFileAndAnalysisMenuItems(false);
					break;
				case COMPLETE_PROGRESS:
					parent.setCursor(null); // turn off the wait cursor
					setEnabledFileAndAnalysisMenuItems(true);
					break;
				case INCREMENT_PROGRESS:
					break;
				}
			}
		};
	}

	// Enables or disabled all File and Analysis menu items.
	private void setEnabledFileAndAnalysisMenuItems(boolean state) {
		this.fileNewStarFromDatabaseItem.setEnabled(state);
		this.fileNewStarFromFileItem.setEnabled(state);
		this.fileSaveItem.setEnabled(state);
		this.filePrintItem.setEnabled(state);

		this.analysisRawDataItem.setEnabled(state);
		this.analysisPhasePlotItem.setEnabled(state);
		this.analysisPeriodSearchItem.setEnabled(state);
	}
}
