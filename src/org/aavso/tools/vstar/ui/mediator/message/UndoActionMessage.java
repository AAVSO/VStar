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
package org.aavso.tools.vstar.ui.mediator.message;

import org.aavso.tools.vstar.ui.undo.IUndoableAction;

/**
 * Undo/redo action message.
 */
public class UndoActionMessage extends MessageBase {

	private IUndoableAction action;
	private UndoableActionType type;
	
	public UndoActionMessage(Object source, IUndoableAction action, UndoableActionType type) {
		super(source);
		
		this.action = action;
		this.type = type;
	}

	/**
	 * @return the action
	 */
	public IUndoableAction getAction() {
		return action;
	}

	/**
	 * @return the type
	 */
	public UndoableActionType getType() {
		return type;
	}
}
