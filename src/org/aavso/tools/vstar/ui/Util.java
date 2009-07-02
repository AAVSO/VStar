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

import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * UI utility methods.
 */
public class Util {

	/**
	 * Create a text panel.
	 * 
	 * @param text
	 *            The text to be displayed.
	 * @return The panel component.
	 */
	public static JComponent createTextPanel(String text) {
		JLabel label = new JLabel(text);
		label.setHorizontalAlignment(JLabel.CENTER);
		JPanel panel = new JPanel(false);
		panel.setLayout(new GridLayout(1, 1));
		panel.add(label);
		return panel;
	}

	/**
	 * Create a list in a scroll pane.
	 * 
	 * @param listModel
	 *            The list model to be used by the list.
	 * @return The scroll pane component.
	 */
//	public static JComponent createScrollPaneList(DefaultListModel listModel) {
//		JList list = new JList(listModel);
//		list.setSelectedIndex(0);
//		list.setVisibleRowCount(20);
//		return new JScrollPane(list);
//	}
}
