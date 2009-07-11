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

package org.aavso.tools.vstar.input;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.aavso.tools.vstar.data.InvalidObservation;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.exception.ObservationReadError;

/**
 * This is the abstract base class for all observation retrieval classes,
 * irrespective of source (AAVSO standard file format, simple file format,
 * VStar database).
 */
public abstract class ObservationRetrieverBase {

	/**
	 * The list of valid observations retrieved.
	 */
	protected List<ValidObservation> validObservations;
	
	/**
	 * The list of invalid observations retrieved.
	 */
	protected List<InvalidObservation> invalidObservations;

	/**
	 * A mapping from observation category (e.g. band, fainter-than) to
	 * list of valid observations.
	 */
	protected Map<String, List<ValidObservation>> validObservationCategoryMap;
	
	/**
	 * Constructor.
	 */
	public ObservationRetrieverBase() {
		this.validObservations = new ArrayList<ValidObservation>();
		this.invalidObservations = new ArrayList<InvalidObservation>();
		this.validObservationCategoryMap = new TreeMap<String, List<ValidObservation>>();
	}

	/**
	 * Retrieve the set of observations from the specified source.
	 * 
	 * @throws throws ObservationReadError
	 */
	public abstract void retrieveObservations()
			throws ObservationReadError;

	/**
	 * @return the validObservations
	 */
	public List<ValidObservation> getValidObservations() {
		return validObservations;
	}

	/**
	 * @return the invalidObservations
	 */
	public List<InvalidObservation> getInvalidObservations() {
		return invalidObservations;
	}

	/**
	 * @return the validObservationCategoryMap
	 */
	public Map<String, List<ValidObservation>> getValidObservationCategoryMap() {
		return validObservationCategoryMap;
	}
}