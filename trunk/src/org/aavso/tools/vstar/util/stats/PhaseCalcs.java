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
package org.aavso.tools.vstar.util.stats;

import java.util.List;

import org.aavso.tools.vstar.data.ValidObservation;

/**
 * This class contains static methods for phase calculations.
 */
public class PhaseCalcs {

	/**
	 * Determine the epoch for phase plot calculations.
	 * 
	 * TODO: parameterise with an IEpochStrategy
	 * 
	 * @param validObs A list of valid observations.
	 * @return The epoch.
	 */
	public static double getEpoch(List<ValidObservation> obs) {
		return (obs.get(0).getJD() + obs.get(obs.size()-1).getJD())/2;
	}
	
	/**
	 * Set the standard and previous cycle phases for each observation
	 * given the specified epoch and period.
	 * 
	 * @param obs A list of valid observations.
	 * @param epoch An epoch (starting JD).
	 * @param period A period on which to base the phases.
	 * @precondition The epoch was determined from the same list of valid
	 * observations passed to this method.
	 */
	public static void setPhases(List<ValidObservation> obs, double epoch, double period) {
		
		for (ValidObservation ob : obs) {
			double phase = calcStandardPhase(ob.getJD(), epoch, period);
			ob.setStandardPhase(phase);
			ob.setPreviousCyclePhase(phase-1);
		}
	}

	/**
	 * Calculate the standard phase, i.e. a phase value in the inclusive range 0..1.
	 * 
	 * @param jd A Julian Date.
	 * @param epoch An epoch (starting JD).
	 * @param period A period on which to base the phases.
	 * @return The standard phase.
	 */
	protected static double calcStandardPhase(double jd, double epoch, double period) {
		double phase = (jd - epoch) / period;
		
		// Notice that this works for negative and positive values,
		// e.g. 2.75 => 2.75 - 2 = 0.75
		// e.g. -2.75 => -2.75 - -3 = -2.75 + 3 = 0.25 
		phase = phase - Math.floor(phase);
		
		return phase;
	}
}
