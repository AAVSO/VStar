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

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.aavso.tools.vstar.data.DateInfo;
import org.aavso.tools.vstar.data.Magnitude;
import org.aavso.tools.vstar.data.MagnitudeModifier;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.util.stats.PhaseCalcs;

/**
 * Phase calculation UTs.
 */
public class PhaseCalcsTest extends TestCase {

	private final static double[] mags1 = { 3, 3.5, 3.6, 3.2, 3.1 };
	private final static double[] jds1 = { 2450001.5, 2450002, 2450003.5, 2450004.5, 2450005 };
	
	/**
	 * Constructor
	 * 
	 * @param name
	 */
	public PhaseCalcsTest(String name) {
		super(name);
	}
	
	// Valid
	
	public void testStandardPhase1() {
		double phase = PhaseCalcs.calcStandardPhase(2450004, 2450002, 10);
		assertEquals(0.2, phase);
	}
	
	public void testStandardPhase2() {
		double phase = PhaseCalcs.calcStandardPhase(2450002, 2450004, 10);
		assertEquals(0.8, phase);
	}

	public void testDetermineEpoch1() {
		List<ValidObservation> observations = populateObservations(mags1, jds1);
		double epoch = PhaseCalcs.getEpoch(observations);		
		assertEquals(2450003.25, epoch);
	}
	
	public void testPhases1() {
		List<ValidObservation> observations = populateObservations(mags1, jds1);
		double epoch = 2450003.25;
		double period = 10;
		PhaseCalcs.setPhases(observations, epoch, period);
		
		// Standard phases: 0.82499999999999996, 0.875, 0.025000000000000001, 
		//                  0.125, 0.17499999999999999
		
		// Check two standard phases, one that had an intermediate negative 
		// value (standard phase of 0.875 in element 1).
		assertEquals(0.875, observations.get(1).getStandardPhase());
		assertEquals(-0.125, observations.get(1).getPreviousCyclePhase());

		assertEquals(0.125, observations.get(3).getStandardPhase());
		assertEquals(-0.875, observations.get(3).getPreviousCyclePhase());
	}
	
	// Helpers

	// Populates and returns a list of valid observations with supplied
	// magnitude values and JD values.
	private List<ValidObservation> populateObservations(double[] mags, double[] jds) {
		assertTrue(mags.length == jds.length);
		
		List<ValidObservation> observations = new ArrayList<ValidObservation>();
		
		for (int i=0;i < mags.length;i++) {
			ValidObservation obs = new ValidObservation();
			obs.setMagnitude(new Magnitude(mags[i], MagnitudeModifier.NO_DELTA,
					false));
			obs.setDateInfo(new DateInfo(jds[i]));
			observations.add(obs);
		}

		return observations;
	}
}
