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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

/**
 * A check-box panel component.
 */
public class CheckBoxPanel extends JPanel implements ItemListener {

	private boolean selected;
	private JCheckBox checkBox;
	
	/**
	 * Constructor
	 */
	public CheckBoxPanel() {
		super(new GridLayout(1, 1));
		this.selected = false;
		this.checkBox = new JCheckBox();
		this.checkBox.addItemListener(this);
		this.add(checkBox);
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	/**
	 * If the checkbox's state changes, change the state of this check box.
	 * 
	 * @param ItemEvent The check-box state change event.
	 */
	public void itemStateChanged(ItemEvent e) {
		this.setSelected(e.getStateChange() == ItemEvent.SELECTED);
	}
}
