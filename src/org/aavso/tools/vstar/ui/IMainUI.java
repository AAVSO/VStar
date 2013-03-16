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
package org.aavso.tools.vstar.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;

/**
 * The interface for all VStar main UI components, e.g. desktop window, applet
 * UI component.
 */
public interface IMainUI {

	/**
	 * @return the uiType
	 */
	public UIType getUiType();

	/**
	 * @return the UI component
	 */
	public Component getComponent();

	/**
	 * @return the UI's content pane.
	 */
	public Container getContentPane();

	/**
	 * @return the statusPane
	 */
	public StatusPane getStatusPane();

	/**
	 * Set the cursor for this UI.
	 * 
	 * @param cursor
	 *            The cursor to set.
	 */
	public void setCursor(Cursor cursor);
}
