/**
 * VStar: a statistical analysis tool for variable star data.
 * Copyright (C) 2010  AAVSO (http://www.aavso.org/)
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

import javax.swing.JCheckBox;
import javax.swing.JComponent;

/**
 * This component is a checkbox for use with MultiEntryComponentDialog.
 */
public class Checkbox implements ITextComponent<Boolean> {

	private String name;
	private JCheckBox checkbox;

	/**
	 * Constructor
	 * 
	 * @param question
	 *            The question to be asked and to which the checkbox state will
	 *            provide an answer.
	 * @param initialValue
	 *            Is the checkbox initially selected?
	 */
	public Checkbox(String question, boolean initialValue) {
		super();
		name = question;
		checkbox = new JCheckBox(question, initialValue);
	}

	@Override
	public boolean canBeEmpty() {
		return false;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getStringValue() {
		return checkbox.isSelected() + "";
	}

	@Override
	public JComponent getUIComponent() {
		return checkbox;
	}

	@Override
	public Boolean getValue() {
		return checkbox.isSelected();
	}

	@Override
	public boolean isReadOnly() {
		return false;
	}

	@Override
	public void setEditable(boolean state) {
		checkbox.setEnabled(state);
	}

	@Override
	public void setValue(Boolean value) {
		checkbox.setSelected(value);
	}
}
