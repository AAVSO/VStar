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

import org.aavso.tools.vstar.ui.mediator.message.UndoableActionType;

/**
 * All classes that represent undoable actions must implement this interface.
 */
public interface IUndoableAction {

	/**
	 * Executes the action corresponding to the undoable action.
	 *
	 * @param type
	 *            The type of operation (do/redo/undo).
	 * @return Was the action execution successful? If not, then there's nothing
	 *         to undo or redo.
	 */
	public boolean execute(UndoableActionType type);

	/**
	 * Returns a human-readable display string for this action.
	 * 
	 * @return The display string.
	 */
	public String getDisplayString();
}
