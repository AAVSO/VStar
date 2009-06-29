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

import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import org.aavso.tools.vstar.ui.model.ObservationPlotModel;
import org.jfree.chart.ChartPanel;

/**
 * TODO: 
 * - Status bar with current star from plot, file loaded etc?
 * - Undoable edits, Edit menu?
 * - About box
 * - Splash Screen?
 * - Toolbar
 * 
 * - We need a DocManager class to store a mapping
 *   from data files to tabs/observation lists, and also to
 *   handle undo, document "is-dirty" handling, don't load same
 *   file twice etc.
 */

/**
 * The main VStar window.
 */
public class MainFrame extends JFrame {

	public final static String LIGHT_CURVE = "Light Curve";

	// Mode strings.
	public final static String PLOT_OBS = "Plot Observations";
	public final static String PLOT_OBS_AND_MEANS = "Plot Observations and Means";
	public final static String LIST_OBS = "List Observations";
	public final static String LIST_MEANS = "List Means";

	// The observation model.
	// TODO: very temporarily here; put this into a Document Manager
	private ObservationPlotModel obsModel;
	private ChartPanel lightCurveChartPane;

	// The application's menu bar.
	private JMenuBar menuBar;

	// The tabs for the main window.
	private Tabs tabs;

	/**
	 * Constructor
	 */
	public MainFrame() {
		super("VStar");

		this.obsModel = null;

		this.menuBar = new MenuBar(this);
		this.setJMenuBar(menuBar);

		this.setContentPane(createContent());

//		this.tabs = this.createContent();
//		JPanel panel = new JPanel(new GridLayout(1, 1));
//		panel.add(tabs);
//		this.setContentPane(panel);
		
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	// Create everything inside the main GUI view.
	private JPanel createContent() {
		// Top-level pane with left to right layout and an empty border.
		JPanel topPane = new JPanel();
		topPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		topPane.setLayout(new BoxLayout(topPane, BoxLayout.LINE_AXIS));
		
//		topPane.add(Box.createRigidArea(new Dimension(10, 0)));

		// The first (left-most) pane containing mode buttons.
		JPanel firstPane = new JPanel();
		firstPane.setLayout(new BoxLayout(firstPane, BoxLayout.PAGE_AXIS));
		firstPane.add(Box.createVerticalGlue());
		firstPane.add(createModeButtonPanel());
		firstPane.add(Box.createVerticalGlue());
		topPane.add(firstPane);
		topPane.add(Box.createRigidArea(new Dimension(10, 0)));
		
		// The second (right-most) pane containing data tables, plots,
		// and observation information.
		// TODO: panel with CardLayout
		topPane.add(createTextPanel("Data, Plot"));
		
		return topPane;
	}
	
	// Create the mode radio button group.
	private JPanel createModeButtonPanel() {
		// Create a radio button panel with N rows and 1 column,
		// a radio button group, and each radio button and its action
		// listener.
		JPanel modePanel = new JPanel(new GridLayout(0,1));
		ButtonGroup modeGroup = new ButtonGroup();
		
		JRadioButton plotObsRadioButton = new JRadioButton(PLOT_OBS);
		plotObsRadioButton.setActionCommand(PLOT_OBS);
		modePanel.add(plotObsRadioButton);
		modeGroup.add(plotObsRadioButton);
		
		JRadioButton plotObsAndMeansRadioButton = new JRadioButton(PLOT_OBS_AND_MEANS);
		plotObsAndMeansRadioButton.setActionCommand(PLOT_OBS_AND_MEANS);
		modePanel.add(plotObsAndMeansRadioButton);
		modeGroup.add(plotObsAndMeansRadioButton);
		
		JRadioButton listObsRadioButton = new JRadioButton(LIST_OBS);
		listObsRadioButton.setActionCommand(LIST_OBS);
		modePanel.add(listObsRadioButton);		
		modeGroup.add(listObsRadioButton);
		
		JRadioButton listMeansRadioButton = new JRadioButton(LIST_MEANS);
		listMeansRadioButton.setActionCommand(LIST_MEANS);
		modePanel.add(listMeansRadioButton);
		modeGroup.add(listMeansRadioButton);

		modePanel.setBorder(BorderFactory.createTitledBorder("Mode"));
		//modePanel.setPreferredSize(new Dimension(100,100));
		
		// TODO: add common action listener
		
		plotObsRadioButton.setSelected(true);

		return modePanel;
	}
	
	/**
	 * The main components of the main frame are created here.
	 * 
	 * TODO: ultimately when we have document management, we will support
	 * File->New creating a new document containing a plot, possibly multiple
	 * phase plots, and multiple file tabs, and in phase III, analysis tabs...
	 * 
	 * @return The main contents in the form of a tabbed pane.
	 */
//	private Tabs createContent() {
//		List<NamedComponent> namedComponents = new ArrayList<NamedComponent>();
//
//		namedComponents
//				.add(new NamedComponent(
//						LIGHT_CURVE,
//						createTextPanel("Open a data file or select a star from the database."),
//						"A plot of magnitude against Julian Day."));
//
//		return new Tabs(namedComponents);
//	}

	/**
	 * Create a text panel.
	 * 
	 * @param text
	 *            The text to be displayed.
	 * @return The panel component.
	 */
	private JComponent createTextPanel(String text) {
		JLabel label = new JLabel(text);
		label.setHorizontalAlignment(JLabel.CENTER);
		JPanel panel = new JPanel(false);
		panel.setLayout(new GridLayout(1, 1));
		panel.add(label);
		return panel;
	}

	/**
	 * Create a list in a scroll pane.
	 * 
	 * @param listModel
	 *            The list model to be used by the list.
	 * @return The scroll pane component.
	 */
	private JComponent createScrollPaneList(DefaultListModel listModel) {
		JList list = new JList(listModel);
		list.setSelectedIndex(0);
		list.setVisibleRowCount(20);
		return new JScrollPane(list);
	}

	// Getters

	public JTabbedPane getTabs() {
		return tabs;
	}

	/**
	 * @return the obsModel
	 */
	public ObservationPlotModel getObsModel() {
		return obsModel;
	}

	/**
	 * @param obsModel
	 *            the obsModel to set
	 */
	public void setObsModel(ObservationPlotModel obsModel) {
		this.obsModel = obsModel;
	}

	/**
	 * @return the lightCurveChartPane
	 */
	public ChartPanel getLightCurveChartPane() {
		return lightCurveChartPane;
	}

	/**
	 * @param lightCurveChartPane the lightCurveChartPane to set
	 */
	public void setLightCurveChartPane(ChartPanel lightCurveChartPane) {
		this.lightCurveChartPane = lightCurveChartPane;
	}
}
