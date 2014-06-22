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
 */package org.aavso.tools.vstar.ui.dialog;

import javax.swing.JComponent;

/**
 * The interface to be implemented by all text components in a dialog.
 * 
 * @param <T> The type of the value to be returned by this component.
 */
public interface ITextComponent<T> {

	/**
	 * @return the name
	 */
	public abstract String getName();

	/**
	 * @return the canBeEmpty
	 */
	public abstract boolean canBeEmpty();

	/**
	 * @return the readOnly
	 */
	public abstract boolean isReadOnly();

	/**
	 * @return the typed value
	 */
	public abstract T getValue();

	/**
	 * @return the underlying string value
	 */
	public abstract String getStringValue();

	/**
	 * Set the component to be editable or not.
	 * 
	 * @param state
	 *            True or false for editability.
	 */
	public void setEditable(boolean state);
	
	/**
	 * Returns the UI component. 
	 */
	public JComponent getUIComponent();
}