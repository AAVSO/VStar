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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.aavso.tools.vstar.ui.MainFrame;
import org.aavso.tools.vstar.ui.mediator.AnalysisTypeChangeMessage;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.ViewModeType;
import org.aavso.tools.vstar.util.notification.Listener;

/**
 * A panel for rendering data lists, plots and other observation-related views.
 * A tabbed pane is used for each view.
 */
public class TabbedDataPane extends JPanel {

	private Mediator mediator = Mediator.getInstance();

	private JTabbedPane tabs;
	private Map<ViewModeType, Integer> viewModeToTabIndexMap;
	private Map<Integer, ViewModeType> tabIndexToViewModeMap;

	/**
	 * Constructor.
	 */
	public TabbedDataPane() {
		super();

		viewModeToTabIndexMap = new HashMap<ViewModeType, Integer>();
		tabIndexToViewModeMap = new HashMap<Integer, ViewModeType>();

		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		this.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		this.setPreferredSize(new Dimension(WIDTH, HEIGHT));

		createDataPanel();

		mediator.getAnalysisTypeChangeNotifier().addListener(
				createAnalysisTypeChangeListener());
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
		tabs.setPreferredSize(new Dimension((int) (MainFrame.WIDTH * 0.9),
				(int) (MainFrame.HEIGHT * 0.95)));

		int index = 0;
		for (ViewModeType type : ViewModeType.values()) {
			String desc = type.getModeDesc();
			tabs.addTab(desc, createTextPanel(noSomethingYet(desc)));
			viewModeToTabIndexMap.put(type, index);
			tabIndexToViewModeMap.put(index, type);
			index++;
		}

		tabs.addChangeListener(createTabChangeListener());
		
		topPane.add(tabs);

		this.add(new JScrollPane(topPane), BorderLayout.CENTER);
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
			// Set the tabbed pane components for each model type.
			public void update(AnalysisTypeChangeMessage msg) {
				JPanel obsPlotPane = msg.getObsChartPane();
				JPanel obsAndMeanPane = msg.getObsAndMeanChartPane();
				JPanel obsListPane = msg.getObsListPane();
				JPanel meansListPane = msg.getMeansListPane();

				if (obsPlotPane != null && obsAndMeanPane != null
						&& obsListPane != null && meansListPane != null) {
					tabs.setComponentAt(viewModeToTabIndexMap
							.get(ViewModeType.PLOT_OBS_MODE), obsPlotPane);
					tabs.setComponentAt(viewModeToTabIndexMap
							.get(ViewModeType.PLOT_OBS_AND_MEANS_MODE),
							obsAndMeanPane);
					tabs.setComponentAt(viewModeToTabIndexMap
							.get(ViewModeType.LIST_OBS_MODE), obsListPane);
					tabs.setComponentAt(viewModeToTabIndexMap
							.get(ViewModeType.LIST_MEANS_MODE), meansListPane);

					mediator.changeViewMode(msg.getViewMode());
					
					tabs.repaint();
				}
			}

			public boolean canBeRemoved() {
				return false;
			}
		};
	}
}
