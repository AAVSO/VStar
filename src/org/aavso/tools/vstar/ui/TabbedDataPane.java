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
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.aavso.tools.vstar.ui.mediator.AnalysisType;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.ViewModeType;
import org.aavso.tools.vstar.ui.mediator.message.AnalysisTypeChangeMessage;
import org.aavso.tools.vstar.ui.mediator.message.ModelSelectionMessage;
import org.aavso.tools.vstar.ui.mediator.message.NewStarMessage;
import org.aavso.tools.vstar.util.model.IModel;
import org.aavso.tools.vstar.util.notification.Listener;

/**
 * A panel for rendering data lists, plots and other observation-related views.
 * A tabbed pane is used for each view.
 */
@SuppressWarnings("serial")
public class TabbedDataPane extends JPanel {

	private Mediator mediator = Mediator.getInstance();

	private JTabbedPane tabs;
	private Map<ViewModeType, Integer> viewModeToTabIndexMap;
	private Map<Integer, ViewModeType> tabIndexToViewModeMap;
	private AnalysisType analysisType;
	private int index;

	private IModel currModel;

	/**
	 * Constructor.
	 */
	public TabbedDataPane() {
		super();

		viewModeToTabIndexMap = new HashMap<ViewModeType, Integer>();
		tabIndexToViewModeMap = new HashMap<Integer, ViewModeType>();
		analysisType = AnalysisType.RAW_DATA;
		index = 0;
		currModel = null;

		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		this.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		this.setPreferredSize(new Dimension(WIDTH, HEIGHT));

		createDataPanel();

		mediator.getAnalysisTypeChangeNotifier().addListener(createAnalysisTypeChangeListener());

		mediator.getModelSelectionNofitier().addListener(createModelSelectionListener());

		mediator.getNewStarNotifier().addListener(createNewStarListener());
	}

	/**
	 * Create a tab with the specified component.
	 * 
	 * @param name      The tab name.
	 * @param viewMode  The component's view mode (e.g. plot).
	 * @param component The component to be contained in the tab when created.
	 */
	public void createTab(String name, ViewModeType viewMode, Component component) {
		tabs.addTab(name, component);
		// TODO: may need to make these multi-maps so each mode can have N > 1
		// components
		// viewModeToTabIndexMap.put(viewMode, index);
		// tabIndexToViewModeMap.put(index, viewMode);
		nextTabIndex();
	}

	/**
	 * Create a tab with the specified component.
	 * 
	 * @param viewMode  The component's view mode (e.g. plot).
	 * @param component The component to be comtained in the tab when created.
	 */
	public void createTab(ViewModeType viewMode, Component component) {
		tabs.addTab(viewMode.getModeDesc(), component);
		viewModeToTabIndexMap.put(viewMode, index);
		tabIndexToViewModeMap.put(index, viewMode);
		nextTabIndex();
	}

	/**
	 * Advance to the next new tab index to be used.
	 * 
	 * @return
	 */
	private void nextTabIndex() {
		index++;
	}

	/**
	 * Create the data pane.
	 */
	private void createDataPanel() {
		// We create a top-level panel with a box layout as a
		// simple way to have an empty border so other components
		// are inset.
		JPanel topPane = new JPanel();
		topPane.setLayout(new BoxLayout(topPane, BoxLayout.PAGE_AXIS));
		topPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		// Create tabbed pane that will be a shared space for
		// data tables and plots, adding place holder panes.
		tabs = new JTabbedPane();

		tabs.setBorder(BorderFactory.createEtchedBorder());
		tabs.setPreferredSize(new Dimension((int) (MainFrame.WIDTH * 0.9), (int) (MainFrame.HEIGHT * 0.95)));

		for (ViewModeType type : ViewModeType.values()) {
			String desc = type.getModeDesc();
			createTab(type, createTextPanel(noSomethingYet(desc)));
		}

		tabs.addChangeListener(createTabChangeListener());

		topPane.add(tabs);

		this.add(new JScrollPane(topPane), BorderLayout.CENTER);
	}

	/**
	 * Create a text pane with a centered string.
	 * 
	 * @param text The text to be displayed.
	 * @return The text pane component.
	 */
	private static Component createTextPanel(String text) {
		JLabel label = new JLabel(text);
		label.setHorizontalAlignment(JLabel.CENTER);
		JPanel panel = new JPanel(false);
		panel.setLayout(new GridLayout(1, 1));
		panel.add(label);
		return panel;
	}

	/**
	 * Given a string 'something', return a string indicating that there is not one
	 * of these 'something's yet.
	 * 
	 * @param s The 'something' string.
	 * @return The annotated string.
	 */
	private String noSomethingYet(String s) {
		// TODO: need to map to full text replacements in terms of LocaleProps.get()
		// mapped by s.
//		return "No " + s.toLowerCase() + " yet.";
		return "";
	}

	/**
	 * Return a tab selection change listener.
	 */
	private ChangeListener createTabChangeListener() {
		return new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				assert e.getSource() == tabs;
				int index = tabs.getSelectedIndex();
				mediator.changeViewMode(tabIndexToViewModeMap.get(index));
			}
		};
	}

	/**
	 * Return a new star creation listener.
	 */
	private Listener<AnalysisTypeChangeMessage> createAnalysisTypeChangeListener() {
		return new Listener<AnalysisTypeChangeMessage>() {
			// Set the tabbed pane components for each type.
			public void update(AnalysisTypeChangeMessage msg) {
				// Update the current analysis type.
				analysisType = msg.getAnalysisType();

				// Update standard plot and list panes.
				JPanel obsAndMeanPane = msg.getObsAndMeanChartPane();
				JPanel obsListPane = msg.getObsListPane();
				JPanel meansListPane = msg.getMeansListPane();

				if (obsAndMeanPane != null && obsListPane != null && meansListPane != null) {
					tabs.setComponentAt(viewModeToTabIndexMap.get(ViewModeType.PLOT_OBS_MODE), obsAndMeanPane);
					tabs.setComponentAt(viewModeToTabIndexMap.get(ViewModeType.LIST_OBS_MODE), obsListPane);
					tabs.setComponentAt(viewModeToTabIndexMap.get(ViewModeType.LIST_MEANS_MODE), meansListPane);

					// If a model has been created, set the appropriate
					// components.
					if (currModel != null) {
						Component modelPane = mediator.getDocumentManager().getModelListPane(analysisType, currModel);

						tabs.setComponentAt(viewModeToTabIndexMap.get(ViewModeType.MODEL_MODE), modelPane);

						Component residualsPane = mediator.getDocumentManager().getResidualsListPane(analysisType,
								currModel);

						tabs.setComponentAt(viewModeToTabIndexMap.get(ViewModeType.RESIDUALS_MODE), residualsPane);
					}

					tabs.repaint();
				}
			}

			public boolean canBeRemoved() {
				return false;
			}
		};
	}

	/**
	 * Return a model selection listener that sets model and residual tab content
	 * for the current analysis mode (raw, phase).
	 */
	private Listener<ModelSelectionMessage> createModelSelectionListener() {
		return new Listener<ModelSelectionMessage>() {
			@Override
			public void update(ModelSelectionMessage info) {
				currModel = info.getModel();

				// Obtain the list components for models and residuals.
				Component modelPane = mediator.getDocumentManager().getModelListPane(analysisType, currModel);

				Component residualsPane = mediator.getDocumentManager().getResidualsListPane(analysisType, currModel);

				// Have the model tabs been created yet?
				if (!viewModeToTabIndexMap.containsKey(ViewModeType.MODEL_MODE)) {
					// No, so create tabs with component...
					createTab(ViewModeType.MODEL_MODE, modelPane);
					createTab(ViewModeType.RESIDUALS_MODE, residualsPane);
				} else {
					// Yes, so, set the components in the existing tabs...
					// TODO: should be able to instead update models from
					// info.getModel() getters!
					tabs.setComponentAt(viewModeToTabIndexMap.get(ViewModeType.MODEL_MODE), modelPane);
					tabs.setComponentAt(viewModeToTabIndexMap.get(ViewModeType.RESIDUALS_MODE), residualsPane);
				}

				tabs.repaint();
			}

			@Override
			public boolean canBeRemoved() {
				return false;
			}
		};
	}

	/**
	 * Return a new star listener that clears the model and residual panes.
	 */
	private Listener<NewStarMessage> createNewStarListener() {
		return new Listener<NewStarMessage>() {
			@Override
			public void update(NewStarMessage info) {
				// Get rid of any previous model.
				currModel = null;

				String desc = null;

				// TODO: should be able to instead clear models

				desc = ViewModeType.MODEL_MODE_DESC;
				tabs.setComponentAt(viewModeToTabIndexMap.get(ViewModeType.MODEL_MODE),
						createTextPanel(noSomethingYet(desc)));

				desc = ViewModeType.RESIDUALS_MODE_DESC;
				tabs.setComponentAt(viewModeToTabIndexMap.get(ViewModeType.RESIDUALS_MODE),
						createTextPanel(noSomethingYet(desc)));

				tabs.repaint();
			}

			@Override
			public boolean canBeRemoved() {
				return false;
			}
		};
	}
}
