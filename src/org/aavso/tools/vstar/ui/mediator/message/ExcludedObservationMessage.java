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

import java.util.List;

import org.aavso.tools.vstar.data.ValidObservation;

/**
 * This message will be sent to Listener<ExcludedObservationMessage>
 * implementers registered with a Notifier<ExludedObservationMessage>
 * implementer.
 * 
 * The message signals that a collection of observations should be considered to
 * be "excluded" or "included" (i.e. moved from being excluded to included).
 */
public class ExcludedObservationMessage extends MessageBase {

	private List<ValidObservation> observations;
	
	/**
	 * Constructor.
	 * 
	 * @param observations
	 *            The observations that have changed.
	 * @param source The object that caused the change.
	 */
	public ExcludedObservationMessage(List<ValidObservation> observations, Object source) {
		super(source);
		this.observations = observations;
	}

	/**
	 * @return the observations
	 */
	public List<ValidObservation> getObservations() {
		return observations;
	}
}
