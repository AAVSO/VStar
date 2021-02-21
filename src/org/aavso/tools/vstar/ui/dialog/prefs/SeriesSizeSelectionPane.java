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
package org.aavso.tools.vstar.ui.dialog.prefs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.util.locale.LocaleProps;

/**
 * Series size selection panel.
 */
@SuppressWarnings("serial")
public class SeriesSizeSelectionPane extends JPanel implements
		IPreferenceComponent {

	private JComboBox seriesSelector;
	private JComboBox sizeSelector;
	private DotComponent dotComponent;

	private Map<SeriesType, Integer> changedSeriesSizeMap;
	private SeriesType currentSeries;
	
	private boolean seriesSelectorActionListenerEnabled = true;

	/**
	 * A dot component to show the size and color of the plot shape for the
	 * current series.
	 */
	class DotComponent extends JComponent {
		private int size;
		private Color color;
		private Shape dot;

		public DotComponent(int size, Color color) {
			change(size, color);
		}

		public void change(int size, Color color) {
			this.color = color;
			this.size = size;
			repaint();
		}

		@Override
		public void paint(Graphics g) {
			Graphics2D gfx = (Graphics2D) g;
			int height = this.getHeight();
			int width = this.getWidth();
			dot = new Ellipse2D.Float(width / 2 - size / 2, height / 2 - size
					/ 2, size, size);
			gfx.setPaint(color);
			gfx.fill(dot);
			gfx.draw(dot);
		}
	}

	/**
	 * Constructor.
	 */
	public SeriesSizeSelectionPane() {
		super();

		changedSeriesSizeMap = new HashMap<SeriesType, Integer>();

		JPanel seriesSizePane = new JPanel();
		seriesSizePane.setLayout(new BoxLayout(seriesSizePane,
				BoxLayout.PAGE_AXIS));
		seriesSizePane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		// Add a combo-box showing all series descriptions.
		SortedSet<String> seriesDescList = new TreeSet<String>();
		for (SeriesType series : SeriesType.values()) {
			seriesDescList.add(series.getDescription());
		}

		seriesSelector = new JComboBox(seriesDescList.toArray(new String[0]));
		seriesSelector.setToolTipText("Select Series");
		seriesSelector.addActionListener(createSeriesSelectorActionListener());
		seriesSelector.setBorder(BorderFactory
				.createTitledBorder("Series Description"));
		seriesSizePane.add(seriesSelector);

		seriesSizePane.add(Box.createRigidArea(new Dimension(10, 10)));

		// Add a combo-box pane and set it to the size of the initially selected
		// series.
		JPanel seriesSizeViewPane = new JPanel();
		seriesSizeViewPane.setLayout(new BoxLayout(seriesSizeViewPane,
				BoxLayout.LINE_AXIS));

		Integer[] sizes = new Integer[] { 2, 4, 6, 8, 10, 12, 14 };
		sizeSelector = new JComboBox(sizes);
		sizeSelector.setToolTipText("Select Size");
		sizeSelector.addActionListener(createSizeSelectorActionListener());
		sizeSelector.setBorder(BorderFactory.createTitledBorder("Series Size"));

		seriesSizeViewPane.add(sizeSelector);

		seriesSizeViewPane.add(Box.createRigidArea(new Dimension(10, 10)));

		// Add a dot size and color viewer.
		// Note: This would have to change if permit the shape of a
		// series plot point to change.
		String seriesDesc = (String) seriesSelector.getItemAt(0);
		currentSeries = SeriesType.getSeriesFromDescription(seriesDesc);
		Integer initialSize = SeriesType.getSizeFromSeries(currentSeries);

		dotComponent = new DotComponent(SeriesType
				.getSizeFromSeries(currentSeries), SeriesType
				.getColorFromSeries(currentSeries));

		JPanel dotPanel = new JPanel(new BorderLayout());
		dotPanel.add(dotComponent, BorderLayout.CENTER);
		dotPanel.setBorder(BorderFactory.createTitledBorder("Appearance"));

		seriesSizeViewPane.add(dotPanel);
		sizeSelector.setSelectedItem(initialSize);

		seriesSizePane.add(seriesSizeViewPane);

		seriesSizePane.add(Box.createRigidArea(new Dimension(10, 10)));

		// Add a local context button pane.
		seriesSizePane.add(createButtonPane());

		this.add(seriesSizePane);
	}

	protected JPanel createButtonPane() {
		JPanel panel = new JPanel(new BorderLayout());

		JButton setDefaultsButton = new JButton("Set Default Sizes");
		setDefaultsButton
				.addActionListener(createSetDefaultsButtonActionListener());
		panel.add(setDefaultsButton, BorderLayout.LINE_START);

		JButton applyButton = new JButton(LocaleProps.get("APPLY_BUTTON"));
		applyButton.addActionListener(createApplyButtonActionListener());
		panel.add(applyButton, BorderLayout.LINE_END);

		return panel;
	}

	// Series selector action listener creator.
	private ActionListener createSeriesSelectorActionListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (seriesSelectorActionListenerEnabled) {
					// Set the size selector according to what the current
					// value of the selected series is.
					String seriesDesc = (String) seriesSelector.getSelectedItem();
					currentSeries = SeriesType.getSeriesFromDescription(seriesDesc);
					sizeSelector.setSelectedItem(SeriesType
							.getSizeFromSeries(currentSeries));

					// Show the new dot size and color.
					dotComponent.change(
							SeriesType.getSizeFromSeries(currentSeries), SeriesType
							.getColorFromSeries(currentSeries));
				}
			}
		};
	}

	// Size selection state change listener.
	private ActionListener createSizeSelectorActionListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Store the changed size in the changed-series-size
				// map to be applied later.
				Integer size = (Integer) sizeSelector.getSelectedItem();
				changedSeriesSizeMap.put(currentSeries, size);

				// Show the new dot size.
				dotComponent.change(size, SeriesType
						.getColorFromSeries(currentSeries));
			}
		};
	}

	// Set defaults action button listener.
	private ActionListener createSetDefaultsButtonActionListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Reset all series sizes to defaults.
				SeriesType.setDefaultSeriesSizes();

				// Bring the currently selected series size into
				// line with this.
				Integer size = SeriesType.getSizeFromSeries(currentSeries);
				sizeSelector.setSelectedItem(size);
			}
		};
	}

	// Set apply button listener.
	private ActionListener createApplyButtonActionListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				update();
			}
		};
	}

	/**
	 * Updates the global series sizes.
	 */
	@Override
	public void update() {
		if (!changedSeriesSizeMap.isEmpty()) {
			// Apply the changed size map to SeriesType and notify listeners.
			SeriesType.updateSeriesSizeMap(changedSeriesSizeMap);
		}
	}

	/**
	 * Prepare this pane for use by resetting whatever needs to be.
	 */
	@Override
	public void reset() {
		// Refresh series list: series can be created dynamically
		// (see, for example, FlexibleTextFormat plugin).
		seriesSelectorActionListenerEnabled = false;
		try {
			seriesSelector.removeAllItems();		
			for (SeriesType series : SeriesType.values()) {
				seriesSelector.addItem(series.getDescription());
			}
			// Restore selection
			seriesSelector.setSelectedItem(currentSeries.getDescription());
		} finally {
			seriesSelectorActionListenerEnabled = true;
		}
		
		// Ensure that the selected size matches SeriesType. This is
		// important if the last time the parent dialog was dismissed,
		// it was cancelled.
		Integer size = SeriesType.getSizeFromSeries(currentSeries);
		sizeSelector.setSelectedItem(size);

		// Start with a blank slate for the size series map.
		changedSeriesSizeMap.clear();
	}

	/**
	 * @return the changedSeriesSizeMap
	 */
	public Map<SeriesType, Integer> getChangedSeriesSizeMap() {
		return changedSeriesSizeMap;
	}
}