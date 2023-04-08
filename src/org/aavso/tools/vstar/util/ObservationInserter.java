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
package org.aavso.tools.vstar.util;

import java.util.ArrayList;
import java.util.List;

import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.exception.ObservationReadError;
import org.aavso.tools.vstar.input.AbstractObservationRetriever;

/**
 * Minimally overridden retriever that can be used to maintain observation order
 * when user defined observations are added to the initial observation set. It's
 * a slight abuse of this base class and arguably the addValidObservation() method
 * called by updateObservationsList() should be factored out of the retriever base
 * class. Yet it has the desired effect without code duplication.
 */
public class ObservationInserter extends AbstractObservationRetriever {

	public ObservationInserter() {
		validObservations = new ArrayList<ValidObservation>();
	}

	public ObservationInserter(List<ValidObservation> observations) {
		validObservations = new ArrayList<ValidObservation>(observations);
	}

	/**
	 * Add observations to the current list, maintaining ordering, also keeping
	 * track of min/max magnitude values, accessible via get{Min,Max}Mag() methods.
	 * 
	 * @param observations The observations to be added.
	 * @return The complete updated list of observations.
	 */
	public List<ValidObservation> addValidObservations(List<ValidObservation> observations) {
		observations.stream().forEach(ob -> addValidObservation(ob));
		return getValidObservations();
	}

	@Override
	public void retrieveObservations() throws ObservationReadError, InterruptedException {
		// nothing to do; see constructor
	}

	@Override
	public String getSourceType() {
		return null;
	}

	@Override
	public String getSourceName() {
		return null;
	}
}