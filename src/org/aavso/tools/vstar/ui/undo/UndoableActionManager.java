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
import org.aavso.tools.vstar.ui.mediator.message.ProgressInfo;
import org.aavso.tools.vstar.ui.mediator.message.UndoActionMessage;
import org.aavso.tools.vstar.ui.mediator.message.UndoableActionType;
import org.aavso.tools.vstar.ui.task.UndoableActionTask;
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
	public void addAction(IUndoableAction action, UndoableActionType type) {

		if (type == UndoableActionType.UNDO) {
			undoStack.add(action);
		} else if (type == UndoableActionType.REDO) {
			redoStack.add(action);
		}

		UndoActionMessage msg = new UndoActionMessage(this, action, type);
		Mediator.getInstance().getUndoActionNotifier().notifyListeners(msg);
	}

	/**
	 * Clear pending undo/redo actions for actions with the specified display
	 * string.
	 * 
	 * @param displayString
	 *            The action's display string.
	 */
	public void clearPendingAction(String displayString) {
		if (!isUndoStackEmpty()) {
			if (undoStack.peek().getDisplayString().equals(displayString)) {
				undoStack.pop();
			}
		}

		if (!isRedoStackEmpty()) {
			if (redoStack.peek().getDisplayString().equals(displayString)) {
				redoStack.pop();
			}
		}
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
	 * Is the redo stack empty?
	 * 
	 * @return Whether or not the redo stack is empty.
	 */
	public boolean isRedoStackEmpty() {
		return redoStack.isEmpty();
	}

	/**
	 * Execute an undo action if one exists.
	 */
	public void executeUndoAction() {
		if (!undoStack.isEmpty()) {
			performUndoableAction(undoStack.pop(), UndoableActionType.UNDO);
		}
	}

	/**
	 * Execute a redo action if one exists.
	 */
	public void executeRedoAction() {
		if (!redoStack.isEmpty()) {
			performUndoableAction(redoStack.pop(), UndoableActionType.REDO);
		}
	}

	/**
	 * Start an asynchronous task to execute an undoable action.
	 * 
	 * @param action
	 *            The undoable action.
	 * @param type
	 *            The undoable action type (undo/redo).
	 */
	public UndoableActionTask performUndoableAction(IUndoableAction action,
			UndoableActionType type) {

		UndoableActionTask task = new UndoableActionTask(action, type);

		Mediator.getInstance().getProgressNotifier()
				.notifyListeners(ProgressInfo.START_PROGRESS);
		Mediator.getInstance().getProgressNotifier()
				.notifyListeners(ProgressInfo.BUSY_PROGRESS);

		task.execute();

		return task;
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
			action.execute(UndoableActionType.DO);
			addAction(action, UndoableActionType.UNDO);

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
