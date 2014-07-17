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
package org.aavso.tools.vstar.ui.undo;

import org.aavso.tools.vstar.ui.mediator.message.UndoRedoType;

/**
 * All classes that represent undoable actions must implement this interface.
 */
public interface IUndoableAction {

	/**
	 * Execute the action corresponding to the undoable action.
	 */
	public void execute();

	/**
	 * Return a human-readable display string for this action.
	 * 
	 * @return The display string.
	 */
	public String getDisplayString();

	/**
	 * Change the internal state of this action to prepare for a call to
	 * execute() in the context of an undo or redo operation.
	 * 
	 * @param type
	 *            The type of operation (redo/undo).
	 */
	public void prepare(UndoRedoType type);
}