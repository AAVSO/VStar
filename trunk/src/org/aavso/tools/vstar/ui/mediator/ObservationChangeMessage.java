/**
 * VStar: a statistical analysis tool for variable star data.
 * Copyright (C) 2009  AAVSO (http://www.aavso.org/)
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
package org.aavso.tools.vstar.ui.mediator;

import java.util.ArrayList;
import java.util.List;

import org.aavso.tools.vstar.data.ValidObservation;

/**
 * This message will be sent to Listener<ObservationChangeMessage> implementers
 * registered with a Notifier<ObservationChangeMessage> implementer.
 * 
 * The message signals a specific change in an observation by a source object. 
 */
public class ObservationChangeMessage extends MessageBase {

	private ValidObservation observation;
	private Iterable<ObservationChangeType> changes;
	
	/**
	 * Constructor.
	 * 
	 * @param observation The observation that has changed.
	 * @param changes The iterable set of changes to the observation.
	 * @param source The object that caused the change.
	 */
	public ObservationChangeMessage(ValidObservation observation,
			Iterable<ObservationChangeType> changes, Object source) {
		super(source);
		this.observation = observation;
		this.changes = changes;
	}

	/**
	 * Constructor.
	 * 
	 * @param observation The observation that has changed.
	 * @param change A single change to the observation.
	 * @param source The object that caused the change.
	 */
	public ObservationChangeMessage(ValidObservation observation,
			ObservationChangeType change, Object source) {
		super(source);
		this.observation = observation;
		List<ObservationChangeType> changes = new ArrayList<ObservationChangeType>();
		changes.add(change);
		this.changes = changes;
	}
	
	/**
	 * @return the observation
	 */
	public ValidObservation getObservation() {
		return observation;
	}
	
	/**
	 * @return an iterator over the changes
	 */
	public Iterable<ObservationChangeType> getChanges() {
		return changes;
	}
}
