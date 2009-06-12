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

import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;

import org.aavso.tools.vstar.data.InvalidObservationDataModel;
import org.aavso.tools.vstar.data.ValidObservationDataModel;

/**
 * This class represents a GUI component that renders information about simple
 * text format observation data, including one or both of valid and invalid
 * observation data. If both are present, they are rendered as tables in a
 * vertical split pane. Otherwise, a single table will appear.
 */
public class SimpleTextFormatObservationPane extends JPanel {

	/**
	 * Constructor
	 * 
	 * @param validDataModel
	 *            A table data model that encapsulates valid observations.
	 * @param invalidDataModel
	 *            A table data model that encapsulates invalid observations.
	 */
	public SimpleTextFormatObservationPane(
			ValidObservationDataModel validDataModel,
			InvalidObservationDataModel invalidDataModel) {

		super(new GridLayout(1, 1));

		JScrollPane validDataScrollPane = null;
		
		if (validDataModel != null) {
			JTable validDataTable = new JTable(validDataModel);
			validDataScrollPane = new JScrollPane(validDataTable);
		}
		
		JScrollPane invalidDataScrollPane = null;

		if (invalidDataModel != null) {
			JTable invalidDataTable = new JTable(invalidDataModel);
			invalidDataScrollPane = new JScrollPane(invalidDataTable);
		}

		// In the presence of both valid and invalid data, we put 
		// them into a split pane.
		if (validDataScrollPane != null && invalidDataScrollPane != null) {
			JSplitPane splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
			splitter.setToolTipText("Valid and invalid data");
			splitter.setTopComponent(validDataScrollPane);
			splitter.setBottomComponent(invalidDataScrollPane);
			splitter.setResizeWeight(0.5);
			this.add(splitter);
		} else if (validDataScrollPane != null) {
			// Just valid data.
			this.add(validDataScrollPane);
		} else if (invalidDataScrollPane != null) {
			// Just invalid data.
			this.add(invalidDataScrollPane);
		} else {
			// We have no data at all. Let's say so.
			JLabel label = new JLabel("There is no data to be displayed");
			label.setHorizontalAlignment(JLabel.CENTER);
			this.setLayout(new GridLayout(1, 1));
			this.add(label);
		}
	}

	/**
	 * Constructor
	 * 
	 * @param validDataModel
	 *            A table data model that encapsulates valid observations.
	 */
	public SimpleTextFormatObservationPane(
			ValidObservationDataModel validDataModel) {
		this(validDataModel, null);
	}

	/**
	 * Constructor
	 * 
	 * @param invalidDataModel
	 *            A table data model that encapsulates invalid observations.
	 */
	public SimpleTextFormatObservationPane(
			InvalidObservationDataModel invalidDataModel) {
		this(null, invalidDataModel);
	}
}
