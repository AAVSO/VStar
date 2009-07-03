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
import java.awt.Component;
import java.awt.Dimension;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.aavso.tools.vstar.ui.model.ModeType;
import org.aavso.tools.vstar.ui.model.ModelManager;
import org.aavso.tools.vstar.ui.model.NewStarType;
import org.aavso.tools.vstar.ui.model.ObservationPlotModel;
import org.aavso.tools.vstar.util.Listener;

/**
 * A panel for rendering data lists, plots and other observation-related
 * information.
 */
public class DataPane extends JPanel {

	private ModelManager modelMgr = ModelManager.getInstance();

	private final static int WIDTH = 800;
	private final static int HEIGHT = 600;

	// Shared data and plot display pane.
	private JPanel cards;

	// UI objects for the cards.
	private Map<String, Component> cardMap;

	/**
	 * Constructor.
	 */
	public DataPane() {
		super();

		this.cardMap = new TreeMap<String, Component>();

		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		this.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		this.setPreferredSize(new Dimension(WIDTH, HEIGHT));

		createDataPanel();

		// We want to be notified of new star creation or changes to mode.
		modelMgr.getNewStarNotifier().addListener(createNewStarListener());
		modelMgr.getModeChangeNotifier()
				.addListener(createModeChangeListener());
	}

	/**
	 * Create the data pane.
	 */
	private void createDataPanel() {
		// We create a top-level panel with a box layout and as
		// a simple way to have an empty border so other components
		// are inset.
		JPanel topPane = new JPanel();
		topPane.setLayout(new BoxLayout(topPane, BoxLayout.PAGE_AXIS));
		topPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		// Create the panel that will be a shared space for
		// data tables and plots as a function of mode radio
		// button selection and Analysis menu item selection.
		// We use a CardLayout for this purpose.
		cards = new JPanel(new CardLayout());
		cards.setBorder(BorderFactory.createEtchedBorder());
		cards.setPreferredSize(new Dimension((int) (WIDTH * 0.9),
				(int) (HEIGHT * 0.9)));

		addDefaultCards();

		topPane.add(cards);

		// Create space between shared space pane and observation
		// information.
		topPane.add(Box.createRigidArea(new Dimension(0, 10)));

		// Create the observation information text area.
		JTextArea obsInfo = new JTextArea();
		obsInfo.setBorder(BorderFactory.createEtchedBorder());
		obsInfo.setPreferredSize(new Dimension((int) (WIDTH * 0.9),
				(int) (HEIGHT * 0.1)));
		obsInfo.setEditable(false);
		topPane.add(obsInfo);

		this.add(topPane, BorderLayout.CENTER);
	}

	/**
	 * Add default components to the card view and ensure appropriate default is
	 * shown.
	 */
	private void addDefaultCards() {
		setCard(ModeType.PLOT_OBS_MODE_DESC, Util
				.createTextPanel(noSomethingYet("Observations plot")));

		setCard(
				ModeType.PLOT_OBS_AND_MEANS_MODE_DESC,
				Util
						.createTextPanel(noSomethingYet("Observations and means plot")));

		setCard(ModeType.LIST_OBS_MODE_DESC, Util
				.createTextPanel(noSomethingYet("Observation list")));

		setCard(ModeType.LIST_MEANS_MODE_DESC, Util
				.createTextPanel(noSomethingYet("Means list")));

		showCard(ModeType.PLOT_OBS_MODE_DESC);
	}

	private String noSomethingYet(String s) {
		return s + " not yet available.";
	}

	/**
	 * Return a new star creation listener.
	 */
	private Listener<NewStarType> createNewStarListener() {
		return new Listener<NewStarType>() {
			// Set the cards components for each model type.
			public void update(NewStarType info) {
				if (info == NewStarType.NEW_STAR_FROM_SIMPLE_FILE) {
					// TODO: create in model manager instead?
					setCard(ModeType.PLOT_OBS_MODE_DESC, createLightCurve());
					setCard(ModeType.LIST_OBS_MODE_DESC, createObsTable());

					showCard(ModeType.PLOT_OBS_MODE_DESC);
				}
			}
		};
	}

	/**
	 * Return a mode change listener.
	 */
	private Listener<ModeType> createModeChangeListener() {
		return new Listener<ModeType>() {
			// Change the component in the specified card.
			public void update(ModeType info) {
				String modeDesc = info.getModeDesc();
				showCard(modeDesc);
			}
		};
	}

	/**
	 * Set the component corresponding to the named card in the CardLayout,
	 * removing the previous component if one existed.
	 * 
	 * @param name
	 *            The name of the card.
	 */
	private void setCard(String name, Component component) {
		if (cardMap.containsKey(name)) {
			cards.remove(cardMap.get(name));
		}

		cards.add(component, name);
		cardMap.put(name, component);
	}

	/**
	 * Show the named card in the CardLayout.
	 * 
	 * No ill-effect ensues from invoking CardLayout.show() where the named card
	 * does not exist.
	 * 
	 * @param name
	 *            The name of the card.
	 */
	private void showCard(String name) {
		CardLayout cardLayout = (CardLayout) cards.getLayout();
		cardLayout.show(cards, name);
	}

	/**
	 * Create the observation table component.
	 */
	private Component createObsTable() {

		return new SimpleTextFormatObservationPane(modelMgr
				.getValidObsTableModel(), modelMgr.getInvalidObsTableModel());
	}

	/**
	 * Create the light curve for a list of valid observations.
	 */
	private Component createLightCurve() {
		ObservationPlotModel model = modelMgr.getObsPlotModel();
		Dimension bounds = new Dimension((int) (WIDTH * 0.75),
				(int) (HEIGHT * 0.75));
		// TODO: make title more meaningful
		return new LightCurvePane("JD vs Magnitude", model, bounds);
	}
}
