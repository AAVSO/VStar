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
package org.aavso.tools.vstar.ui.dialog;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.ui.model.plot.ObservationPlotModel;

/**
 * This class represents a pane with checkboxes showing those series
 * that are rendered. The series to be displayed can be changed. 
 */
public class SeriesVisibilityPane extends JPanel {

	private ObservationPlotModel obsPlotModel;
	private Map<Integer, Boolean> visibilityDeltaMap;

	/**
	 * Constructor 
	 */
	public SeriesVisibilityPane(ObservationPlotModel obsPlotModel) {
		super();
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		this.setBorder(BorderFactory.createTitledBorder("Visibility"));
		this.setToolTipText("Select or deselect series for desired visibility.");

		this.obsPlotModel = obsPlotModel;
		this.visibilityDeltaMap = new HashMap<Integer, Boolean>();

		addSeriesCheckBoxes();
	}
	
	// Create a checkbox for each series.
	private void addSeriesCheckBoxes() {
		// Ensure the panel is always wide enough.
		this.add(Box.createRigidArea(new Dimension(75, 10)));
		
		for (SeriesType series : this.obsPlotModel.getSeriesKeys()) {
			if (series != SeriesType.MEANS) {
				String seriesName = series.getDescription();
				JCheckBox checkBox = new JCheckBox(seriesName);
				checkBox
						.addActionListener(createSeriesVisibilityCheckBoxListener());
				int seriesNum = obsPlotModel.getSrcTypeToSeriesNumMap().get(
						series);
				boolean vis = obsPlotModel.getSeriesVisibilityMap().get(
						seriesNum);
				checkBox.setSelected(vis);
				this.add(checkBox);
				this.add(Box.createRigidArea(new Dimension(10, 10)));
			}
		}
	}
	
	// Return a listener for the series visibility checkboxes.
	private ActionListener createSeriesVisibilityCheckBoxListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JCheckBox checkBox = (JCheckBox) e.getSource();
				String seriesName = checkBox.getText();
				int seriesNum = obsPlotModel.getSrcTypeToSeriesNumMap().get(
						SeriesType.getSeriesFromDescription(seriesName));
				visibilityDeltaMap.put(seriesNum, checkBox.isSelected());
			}
		};
	}

	/**
	 * @return the visibilityDeltaMap
	 */
	public Map<Integer, Boolean> getVisibilityDeltaMap() {
		return visibilityDeltaMap;
	}
}
