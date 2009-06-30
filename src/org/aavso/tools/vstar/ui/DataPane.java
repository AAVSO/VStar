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

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextArea;

import org.jfree.chart.ChartPanel;

/**
 * A panel for rendering data lists, plots and other observation-related
 * information.
 */
public class DataPane extends JPanel {

	private final static int WIDTH = 600;
	private final static int HEIGHT = 500;
	
	// Shared data and plot display pane.
	private JPanel cards;
	
	// UI objects for the cards.
	// TODO: what to have by default if anything?
	private ChartPanel obsPlot;
	private ChartPanel obsAndMeansPlot;
	private JPanel obsTablePlaceholder;
	private JPanel meansTablePlaceholder;

	/**
	 * Constructor.
	 */
	public DataPane() {
		super();

		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		this.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		this.setPreferredSize(new Dimension(WIDTH, HEIGHT));

		//this.obsPlot = new LightCurvePane(title, model, dims);
		
		createDataPanel();
	}

	// Create the data pane.
	private void createDataPanel() {
		// We create a top-level panel with a box layout and as
		// a simple way to have an empty border so other components 
		// are inset.
		JPanel topPane = new JPanel();
		topPane.setLayout(new BoxLayout(topPane, BoxLayout.PAGE_AXIS));
		topPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		
		// Create the panel that will be a shared space for
		// data tables and plots as a function of mode radio
		// button selection and Analysis menu item selection.
		// We use a CardLayout for this purpose.
		cards = new JPanel(new CardLayout());
		cards.setBorder(BorderFactory.createEtchedBorder());
		cards.setPreferredSize(new Dimension((int)(WIDTH*0.9), (int)(HEIGHT*0.9)));

		topPane.add(cards);

		showCard(MainFrame.PLOT_OBS);

		// Create space between shared space pane and observation
		// information.
		topPane.add(Box.createRigidArea(new Dimension(0, 10)));

		// Create the observation information text area.
		JTextArea obsInfo = new JTextArea();
		obsInfo.setBorder(BorderFactory.createEtchedBorder());
		obsInfo.setPreferredSize(new Dimension((int)(WIDTH*0.9), (int)(HEIGHT*0.1)));
		obsInfo.setText("Select a 'New Star' item from the File menu.");
		obsInfo.setEditable(false);
		topPane.add(obsInfo);
		
		this.add(topPane, BorderLayout.CENTER);
	}

	// Show the named card in the CardLayout.
	private void showCard(String name) {
		CardLayout cardLayout = (CardLayout) cards.getLayout();
		cardLayout.show(cards, name);
	}
}
