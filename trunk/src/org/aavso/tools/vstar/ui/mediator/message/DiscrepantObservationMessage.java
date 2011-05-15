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
package org.aavso.tools.vstar.ui.mediator.message;

import org.aavso.tools.vstar.data.ValidObservation;

/**
 * This message will be sent to Listener<DiscrepantObservationMessage> implementers
 * registered with a Notifier<DiscrepantObservationMessage> implementer.
 * 
 * The message signals that an observation should be marked as "discrepant". 
 */
public class DiscrepantObservationMessage extends MessageBase {

	private ValidObservation observation;
	
	/**
	 * Constructor.
	 * 
	 * @param observation The observation that has changed.
	 * @param source The object that caused the change.
	 */
	public DiscrepantObservationMessage(ValidObservation observation,
			 Object source) {
		super(source);
		this.observation = observation;
	}
	
	/**
	 * @return the observation
	 */
	public ValidObservation getObservation() {
		return observation;
	}
}
