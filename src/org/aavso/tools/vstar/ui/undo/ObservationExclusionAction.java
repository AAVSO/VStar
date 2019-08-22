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

import java.util.List;

import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.message.ExcludedObservationMessage;
import org.aavso.tools.vstar.ui.mediator.message.UndoableActionType;

/**
 * This class reverses an exclusion/inclusion operation.
 */
public class ObservationExclusionAction implements IUndoableAction {

	private List<ValidObservation> obs;
	private boolean exclusionState;

	/**
	 * Constructor
	 * 
	 * @param obs
	 *            The observations to be included/exclusionState.
	 * @param exclusionState
	 *            The exclusion/inclusion state.
	 */
	public ObservationExclusionAction(List<ValidObservation> obs,
			boolean exclusionState) {
		super();
		this.obs = obs;
		this.exclusionState = exclusionState;
	}

	/**
	 * @param exclusionState
	 *            the exclusionState to set
	 */
	public void setExclusionState(boolean exclusionState) {
		this.exclusionState = exclusionState;
	}

	/**
	 * @see org.aavso.tools.vstar.ui.undo.IUndoableAction#execute()
	 */
	@Override
	public boolean execute(UndoableActionType type) {
		// Undo and Redo toggle the exclusion state, whereas we just use the
		// first exclusion state for the first ("do") action.
		if (type != UndoableActionType.DO) {
			exclusionState = !exclusionState;
		}

		// Set the exclusion state of each observation.
		int i;
		for (i = 0; i < obs.size(); i++) {
			ValidObservation ob = obs.get(i);
			ob.setExcluded(exclusionState);
		}

		// Send an exclusion message.
		ExcludedObservationMessage msg = new ExcludedObservationMessage(obs,
				this);

		Mediator.getInstance().getExcludedObservationNotifier()
				.notifyListeners(msg);
		
		return true;
	}

	@Override
	public String getDisplayString() {
		return "observation exclusion";
	}
}
