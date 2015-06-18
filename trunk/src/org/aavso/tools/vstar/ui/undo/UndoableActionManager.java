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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.message.MultipleObservationSelectionMessage;
import org.aavso.tools.vstar.ui.mediator.message.NewStarMessage;
import org.aavso.tools.vstar.ui.mediator.message.ObservationSelectionMessage;
import org.aavso.tools.vstar.ui.mediator.message.UndoActionMessage;
import org.aavso.tools.vstar.ui.mediator.message.UndoRedoType;
import org.aavso.tools.vstar.util.notification.Listener;

/**
 * This class manages actions on behalf of other components.
 */
public class UndoableActionManager {

	private Set<ValidObservation> selectedObs;

	private Stack<IUndoableAction> undoStack;
	private Stack<IUndoableAction> redoStack;

	/**
	 * Constructor.
	 */
	public UndoableActionManager() {
		selectedObs = new HashSet<ValidObservation>();
		undoStack = new Stack<IUndoableAction>();
		redoStack = new Stack<IUndoableAction>();
	}

	/**
	 * Add an action to the undo or redo stack and notify listeners of this.
	 * 
	 * @param action
	 *            The undoable action.
	 * @param type
	 *            The type of action (undo/redo).
	 */
	private void addAction(IUndoableAction action, UndoRedoType type) {

		action.prepare(type);

		if (type == UndoRedoType.UNDO) {
			undoStack.add(action);
		} else if (type == UndoRedoType.REDO) {
			redoStack.add(action);
		}

		UndoActionMessage msg = new UndoActionMessage(this, action, type);
		Mediator.getInstance().getUndoActionNotifier().notifyListeners(msg);
	}

	/**
	 * Is the undo stack empty?
	 * 
	 * @return Whether or not the undo stack is empty.
	 */
	public boolean isUndoStackEmpty() {
		return undoStack.isEmpty();
	}

	/**
	 * Execute an undo action if one exists.
	 */
	public void executeUndoAction() {
		if (!undoStack.isEmpty()) {
			IUndoableAction action = undoStack.pop();

			// Perform the action, then add its opposite to the redo
			// stack.
			action.execute();
			addAction(action, UndoRedoType.REDO);
		}
	}

	/**
	 * Is the redo stack empty?
	 * 
	 * @return Whether or not the redo stack is empty.
	 */
	public boolean isRedoStackEmpty() {
		return redoStack.isEmpty();
	}

	/**
	 * Execute a redo action if one exists.
	 */
	public void executeRedoAction() {
		if (!redoStack.isEmpty()) {
			IUndoableAction action = redoStack.pop();

			// Perform the action, then add its opposite to the undo
			// stack.
			action.execute();
			addAction(action, UndoRedoType.UNDO);
		}
	}

	// ** Specific actions and related methods. **

	/**
	 * Exclude the currently selected observation(s).
	 */
	public void excludeCurrentSelection() {

		if (!selectedObs.isEmpty()) {
			// Create an undoable exclusion action.
			List<ValidObservation> undoObs = new ArrayList<ValidObservation>();
			undoObs.addAll(selectedObs);

			ObservationExclusionAction action = new ObservationExclusionAction(
					undoObs, true);

			// Perform the exclusion action, then add its opposite to the undo
			// stack.
			action.execute();
			addAction(action, UndoRedoType.UNDO);

			// Now that we have excluded the selected observations,
			// clear the collection.
			selectedObs.clear();
		}
	}

	// Returns a new star listener.
	public Listener<NewStarMessage> createNewStarListener() {
		return new Listener<NewStarMessage>() {
			@Override
			public void update(NewStarMessage info) {
				selectedObs.clear();
				undoStack.clear();
				redoStack.clear();
			}

			@Override
			public boolean canBeRemoved() {
				return false;
			}
		};
	}

	// Returns a multiple observation selection listener that collects
	// observations that have been selected.
	public Listener<MultipleObservationSelectionMessage> createMultipleObservationSelectionListener() {
		return new Listener<MultipleObservationSelectionMessage>() {
			@Override
			public void update(MultipleObservationSelectionMessage info) {
				selectedObs.clear();
				selectedObs.addAll(info.getObservations());
			}

			@Override
			public boolean canBeRemoved() {
				return false;
			}
		};
	}

	// Returns an observation selection listener that collects
	// the single observation that has been selected.
	public Listener<ObservationSelectionMessage> createObservationSelectionListener() {
		return new Listener<ObservationSelectionMessage>() {
			@Override
			public void update(ObservationSelectionMessage info) {
				selectedObs.clear();
				selectedObs.add(info.getObservation());
			}

			@Override
			public boolean canBeRemoved() {
				return false;
			}
		};
	}
}
