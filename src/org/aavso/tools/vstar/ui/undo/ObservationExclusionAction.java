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
import org.aavso.tools.vstar.ui.mediator.message.UndoRedoType;

/**
 * This class reverses an exclusion/inclusion operation.
 */
public class ObservationExclusionAction implements IUndoableAction {

	private List<ValidObservation> obs;
	private boolean exclusionState;
	
	/**
	 * Constructor.
	 * 
	 * @param obs The observations to be included/exclusionState.
	 * @param exclusionState The exclusion/inclusion state.
	 */
	public ObservationExclusionAction(List<ValidObservation> obs,
			boolean exclusionState) {
		super();
		this.obs = obs;
		this.exclusionState = exclusionState;
	}

	/**
	 * @param exclusionState the exclusionState to set
	 */
	public void setExclusionState(boolean exclusionState) {
		this.exclusionState = exclusionState;
	}

	/**
	 * @see org.aavso.tools.vstar.ui.undo.IUndoableAction#execute()
	 */
	@Override
	public void execute() {
		// Set the exclusion state of each observation.
		for (ValidObservation ob : obs) {
			ob.setExcluded(exclusionState);
		}
		
		// Send an exclusion message.
		ExcludedObservationMessage msg = new ExcludedObservationMessage(
				obs, this);

		Mediator.getInstance().getExcludedObservationNotifier()
				.notifyListeners(msg);
	}

	@Override
	public String getDisplayString() {
		return "observation exclusion";
	}

	@Override
	public void prepare(UndoRedoType type) {
		// Just toggle the exclusion state.
		exclusionState = !exclusionState;
	}
}
