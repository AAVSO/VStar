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

import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;

/**
 * Messages of this type are sent when a new series type, and accompanying
 * observations, is created.
 */
public class SeriesCreationMessage extends MessageBase {

	private SeriesType type;
	private List<ValidObservation> obs;

	public SeriesCreationMessage(Object source, SeriesType type, List<ValidObservation> obs) {
		super(source);
		this.type = type;
		this.obs = obs;
	}

	/**
	 * @return the type
	 */
	public SeriesType getType() {
		return type;
	}

	/**
	 * @return the obs
	 */
	public List<ValidObservation> getObs() {
		return obs;
	}
}
