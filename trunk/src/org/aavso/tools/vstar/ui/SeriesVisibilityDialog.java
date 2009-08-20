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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.aavso.tools.vstar.ui.model.ObservationPlotModel;
import org.aavso.tools.vstar.ui.model.SeriesType;

/**
 * This dialog permits the visibility of plot series to be changed.
 */
public class SeriesVisibilityDialog extends JDialog {

	private ObservationPlotModel obsPlotModel;
	private Container contentPane;
	private Map<Integer, Boolean> visibilityDeltaMap;

	private boolean cancelled;

	/**
	 * Constructor.
	 * 
	 * @param obsPlotModel
	 *            An observation plot model.
	 */
	public SeriesVisibilityDialog(ObservationPlotModel obsPlotModel) {
		super();
		this.setTitle("Change Series Visibility");
		this.setModal(true);

		this.obsPlotModel = obsPlotModel;

		visibilityDeltaMap = new HashMap<Integer, Boolean>();

		contentPane = this.getContentPane();

		// TODO: need this to become one component, and have a
		// separate radio button component for analysis
		
		JPanel topPane = new JPanel();
		topPane.setLayout(new BoxLayout(topPane, BoxLayout.PAGE_AXIS));
		topPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		topPane
				.setToolTipText("Select or deselect series for desired visibility.");

		topPane.add(createSeriesCheckBoxPane());
		topPane.add(Box.createRigidArea(new Dimension(10, 10)));
		topPane.add(createButtonPane());

		contentPane.add(topPane);

		this.pack();
		this.setLocationRelativeTo(MainFrame.getInstance().getContentPane());
		this.setVisible(true);
	}

	private JPanel createSeriesCheckBoxPane() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

		for (String seriesName : this.obsPlotModel.getSeriesKeys()) {
			if (!SeriesType.MEANS.getName().equals(seriesName)) {
				JCheckBox checkBox = new JCheckBox(seriesName);
				checkBox
						.addActionListener(createSeriesVisibilityCheckBoxListener());
				int seriesNum = obsPlotModel.getSrcNameToSeriesNumMap().get(
						seriesName);
				boolean vis = obsPlotModel.getSeriesVisibilityMap().get(
						seriesNum);
				checkBox.setSelected(vis);
				panel.add(checkBox);
				panel.add(Box.createRigidArea(new Dimension(10, 10)));
			}
		}

		return panel;
	}

	// TODO: need to refactor code for OkCancelDialog
	
	private JPanel createButtonPane() {
		JPanel panel = new JPanel(new BorderLayout());

		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(createCancelButtonListener());
		panel.add(cancelButton, BorderLayout.LINE_START);

		JButton okButton = new JButton("OK");
		okButton.addActionListener(createOKButtonListener());
		panel.add(okButton, BorderLayout.LINE_END);

		return panel;
	}

	// Return a listener for the series visibility checkboxes.
	private ActionListener createSeriesVisibilityCheckBoxListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JCheckBox checkBox = (JCheckBox) e.getSource();
				String seriesName = checkBox.getText();
				int seriesNum = obsPlotModel.getSrcNameToSeriesNumMap().get(
						seriesName);
				visibilityDeltaMap.put(seriesNum, checkBox.isSelected());
			}
		};
	}

	// Return a listener for the "OK" button.
	private ActionListener createOKButtonListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				dispose();
			}
		};
	}

	// Return a listener for the "cancel" button.
	private ActionListener createCancelButtonListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cancelled = true;
				setVisible(false);
				dispose();
			}
		};
	}

	/**
	 * @return has the dialog been cancelled?
	 */
	public boolean isCancelled() {
		return cancelled;
	}

	/**
	 * @return the visibilityDeltaMap
	 */
	public Map<Integer, Boolean> getVisibilityDeltaMap() {
		return visibilityDeltaMap;
	}
}
