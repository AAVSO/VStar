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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.aavso.tools.vstar.data.SeriesType;

/**
 * Series color selection panel.
 */
public class SeriesColorSelectionPane extends JPanel implements
		IPreferenceComponent {

	private JComboBox seriesSelector;
	private JColorChooser colorChooser;
	private Map<SeriesType, Color> changedSeriesColorMap;
	private SeriesType currentSeries;

	/**
	 * Constructor.
	 */
	public SeriesColorSelectionPane() {
		super();

		changedSeriesColorMap = new HashMap<SeriesType, Color>();

		JPanel seriesColorPane = new JPanel();
		seriesColorPane.setLayout(new BoxLayout(seriesColorPane,
				BoxLayout.PAGE_AXIS));
		seriesColorPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

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
		seriesColorPane.add(seriesSelector);

		seriesColorPane.add(Box.createRigidArea(new Dimension(10, 10)));

		// Add a JColorChooser pane with the chooser set to the
		// initially selected series.
		String seriesDesc = (String) seriesSelector.getItemAt(0);
		currentSeries = SeriesType.getSeriesFromDescription(seriesDesc);
		Color initialColor = SeriesType.getColorFromSeries(currentSeries);

		colorChooser = new JColorChooser(initialColor);
		colorChooser.setToolTipText("Select Color");
		colorChooser.getSelectionModel().addChangeListener(
				createColorChooserChangeListener());
		colorChooser.setBorder(BorderFactory.createTitledBorder("Color"));
		seriesColorPane.add(colorChooser);

		seriesColorPane.add(Box.createRigidArea(new Dimension(10, 10)));

		// Add a local context button pane.
		seriesColorPane.add(createButtonPane());

		this.add(seriesColorPane);
	}

	protected JPanel createButtonPane() {
		JPanel panel = new JPanel(new BorderLayout());

		JButton setDefaultsButton = new JButton("Set Default Colors");
		setDefaultsButton
				.addActionListener(createSetDefaultsButtonActionListener());
		panel.add(setDefaultsButton, BorderLayout.LINE_START);

		JButton applyButton = new JButton("Apply");
		applyButton.addActionListener(createApplyButtonActionListener());
		panel.add(applyButton, BorderLayout.LINE_END);

		return panel;
	}

	// Series selector action listener creator.
	private ActionListener createSeriesSelectorActionListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Set the color chooser according to what the current
				// value of the selected series is.
				String seriesDesc = (String) seriesSelector.getSelectedItem();
				currentSeries = SeriesType.getSeriesFromDescription(seriesDesc);
				colorChooser.setColor(SeriesType
						.getColorFromSeries(currentSeries));
			}
		};
	}

	// Color selection state change listener.
	private ChangeListener createColorChooserChangeListener() {
		return new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				// Store the changed color in the changed-series-color
				// map to be applied later.
				Color color = colorChooser.getColor();
				changedSeriesColorMap.put(currentSeries, color);
			}
		};
	}

	// Set defaults action button listener.
	private ActionListener createSetDefaultsButtonActionListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Reset all series colors to defaults.
				SeriesType.setDefaultSeriesColors();

				// Bring the currently selected series color into
				// line with this.
				Color color = SeriesType.getColorFromSeries(currentSeries);
				colorChooser.setColor(color);
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
	 * Updates the global series colors.
	 */
	@Override
	public void update() {
		if (!changedSeriesColorMap.isEmpty()) {
			// Apply the changed color map to SeriesType and notify
			// listeners.
			// TODO: update colors in prefs (see key info above)
			SeriesType.updateSeriesColorMap(changedSeriesColorMap);
		}
	}

	/**
	 * Prepare this pane for use by resetting whatever needs to be.
	 */
	@Override
	public void reset() {
		// Ensure that the selected color matches SeriesType. This is
		// important if the last time the parent dialog was dismissed
		// it was cancelled.
		Color color = SeriesType.getColorFromSeries(currentSeries);
		colorChooser.setColor(color);

		// Start with a blank slate for the color series map.
		changedSeriesColorMap.clear();
	}

	/**
	 * @return the changedSeriesColorMap
	 */
	public Map<SeriesType, Color> getChangedSeriesColorMap() {
		return changedSeriesColorMap;
	}
}