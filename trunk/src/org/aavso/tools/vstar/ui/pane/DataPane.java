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
package org.aavso.tools.vstar.ui.pane;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.ViewModeType;
import org.aavso.tools.vstar.ui.mediator.message.AnalysisTypeChangeMessage;
import org.aavso.tools.vstar.util.notification.Listener;

/**
 * A panel for rendering data lists, plots and other observation-related
 * information.
 * @deprecated
 */
public class DataPane extends JPanel {

	private Mediator mediator = Mediator.getInstance();

	public final static int WIDTH = 800;
	public final static int HEIGHT = 600;

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
		mediator.getAnalysisTypeChangeNotifier().addListener(
				createAnalysisTypeChangeListener());

//		mediator.getViewModeChangeNotifier()
//				.addListener(createModeChangeListener());
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
		topPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		// Create the panel that will be a shared space for
		// data tables and plots as a function of mode radio
		// button selection and Analysis menu item selection.
		// We use a CardLayout for this purpose.
		cards = new JPanel(new CardLayout());
		cards.setBorder(BorderFactory.createEtchedBorder());
		cards.setPreferredSize(new Dimension((int) (WIDTH * 0.9),
				(int) (HEIGHT * 0.95)));

		setDefaultCards();

		topPane.add(cards);

		this.add(new JScrollPane(topPane), BorderLayout.CENTER);
	}

	/**
	 * Create default components to the card view and ensure appropriate default
	 * is shown.
	 */
	private void setDefaultCards() {
		setCard(ViewModeType.PLOT_OBS_MODE_DESC,
				createTextPanel(noSomethingYet("Observation plot")));

		setCard(ViewModeType.PLOT_OBS_AND_MEANS_MODE_DESC,
				createTextPanel(noSomethingYet("Observation and mean plot")));

		setCard(ViewModeType.LIST_OBS_MODE_DESC,
				createTextPanel(noSomethingYet("Observation list")));

		setCard(ViewModeType.LIST_MEANS_MODE_DESC,
				createTextPanel(noSomethingYet("Means list")));

		showCard(ViewModeType.PLOT_OBS_MODE_DESC);
	}

	private static JComponent createTextPanel(String text) {
		JLabel label = new JLabel(text);
		label.setHorizontalAlignment(JLabel.CENTER);
		JPanel panel = new JPanel(false);
		panel.setLayout(new GridLayout(1, 1));
		panel.add(label);
		return panel;
	}

	private String noSomethingYet(String s) {
		return "No '" + s.toLowerCase() + "' yet.";
	}

	/**
	 * Return a new star creation listener.
	 */
	private Listener<AnalysisTypeChangeMessage> createAnalysisTypeChangeListener() {
		return new Listener<AnalysisTypeChangeMessage>() {
			// Set the cards components for each model type.
			public void update(AnalysisTypeChangeMessage msg) {
				JPanel obsPlotPane = msg.getObsChartPane();
				JPanel obsAndMeanPane = msg.getObsAndMeanChartPane();
				JPanel obsListPane = msg.getObsListPane();
				JPanel meansListPane = msg.getMeansListPane();

				if (obsPlotPane != null && obsAndMeanPane != null
						&& obsListPane != null && meansListPane != null) {
					setCard(ViewModeType.PLOT_OBS_MODE_DESC, obsPlotPane);
					setCard(ViewModeType.PLOT_OBS_AND_MEANS_MODE_DESC,
							obsAndMeanPane);
					setCard(ViewModeType.LIST_OBS_MODE_DESC, obsListPane);
					setCard(ViewModeType.LIST_MEANS_MODE_DESC, meansListPane);

					// This class will respond to this message via
					// createModeChangeListener(). Other components
					// may also want to know, e.g. status bar, hence
					// the choice of this message "broadcast".
					mediator.changeViewMode(msg.getViewMode());
				}
			}
			
			public boolean canBeRemoved() {
				return false;
			}
		};
	}

	/**
	 * Return a mode change listener.
	 */
	private Listener<ViewModeType> createModeChangeListener() {
		return new Listener<ViewModeType>() {
			// Change the component in the specified card.
			public void update(ViewModeType info) {
				String modeDesc = info.getModeDesc();
				showCard(modeDesc);
			}
			
			public boolean canBeRemoved() {
				return false;
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
}
