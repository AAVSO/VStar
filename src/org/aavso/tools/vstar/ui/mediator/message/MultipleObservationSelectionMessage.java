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

import java.util.List;

import org.aavso.tools.vstar.data.ValidObservation;

/**
 * This message is sent to denote the selection of multiple valid observations 
 * from a specific source.
 */
public class MultipleObservationSelectionMessage extends MessageBase {

	private List<ValidObservation> observations;
	
	/**
	 * Constructor.
	 * 
	 * @param observations
	 *            The observations.
	 * @param source
	 *            The source of the message.
	 */
	public MultipleObservationSelectionMessage(List<ValidObservation> observations,
			Object source) {
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
