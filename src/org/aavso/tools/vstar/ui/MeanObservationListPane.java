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

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.aavso.tools.vstar.ui.model.MeanObservationTableModel;

/**
 * This class is a component that renders observation mean and standard error of
 * the average data.
 */
public class MeanObservationListPane extends JPanel {

	/**
	 * Constructor.
	 * 
	 * @param meanObsTableModel The mean observation table model.
	 */
	public MeanObservationListPane(MeanObservationTableModel meanObsTableModel) {
		super(new GridLayout(1, 1));
		
		JTable meanObsTable = new JTable(meanObsTableModel);
		// This next line ensures we get a horizontal scrollbar if necessary
		// rather than trying to cram all the columns into the visible pane.
		//meanObsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		
		// Enable table sorting by clicking on a column.
		// Note: this is only available from Java 1.6
		//meanObsTable.setAutoCreateRowSorter(true);
		
		JScrollPane meanObsTableScrollPane = new JScrollPane(meanObsTable);
		
		this.add(meanObsTableScrollPane);
	}
}
