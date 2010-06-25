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
package org.aavso.tools.vstar.ui.mediator;

import java.util.Map;

import org.aavso.tools.vstar.data.ValidObservation;

/**
 * Instances of this message should be sent when an observation filtering
 * operation has occurred.
 */
public class FilteredObservationMessage extends MessageBase {

	public static final FilteredObservationMessage NO_FILTER = null;
	
	private Map<Integer, ValidObservation> filteredObsMap;

	/**
	 * Constructor.
	 * 
	 * @param source
	 *            The source of the message.
	 * @param filteredObsMap
	 *            A mapping from index to observation which denotes a subset of
	 *            valid observations.
	 */
	public FilteredObservationMessage(Object source,
			Map<Integer, ValidObservation> filteredObsMap) {
		super(source);
		this.filteredObsMap = filteredObsMap;
	}

	/**
	 * @return the filteredObsMap
	 */
	public Map<Integer, ValidObservation> getFilteredObsMap() {
		return filteredObsMap;
	}
}
