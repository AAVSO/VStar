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
package org.aavso.tools.vstar.ui.model.plot;

import java.util.List;

import org.aavso.tools.vstar.data.ValidObservation;

/**
 * A phase element source/sink.
 */
public class PhaseTimeElementEntity implements ITimeElementEntity {

	private static final double DEFAULT_BIN_PHASE_INCREMENTS = 0.1;

	public static final PhaseTimeElementEntity instance = new PhaseTimeElementEntity();

	public double getTimeElement(List<ValidObservation> obs, int index) {
		ValidObservation ob = obs.get(index);
		Double phase = null;

		if (index < obs.size() / 2) {
			phase = ob.getPreviousCyclePhase();
		} else {
			phase = ob.getStandardPhase();
		}

		assert phase != null;

		return phase;
	}

	public void setTimeElement(ValidObservation ob, double meanPhase) {
		if (meanPhase >= 0) {
			// The mean phase value represents a standard phase
			// so store this and calculate the mean previous cycle.
			ob.setStandardPhase(meanPhase);
			ob.setPreviousCyclePhase(meanPhase - 1);
		} else {
			// The mean phase value represents a previous cycle 
			// phase so store this and calculate the mean standard.
			ob.setPreviousCyclePhase(meanPhase);
			ob.setStandardPhase(meanPhase + 1);
		}
	}

	public double getDefaultTimeElementsInBin() {
		return DEFAULT_BIN_PHASE_INCREMENTS;
	}

	public double getDefaultTimeIncrements() {
		return 0.05;
	}

	public String getNumberFormat() {
		return "  #.####";
	}
}
