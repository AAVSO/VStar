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
import org.aavso.tools.vstar.util.stats.epoch.AlphaOmegaMeanJDEpochStrategy;
import org.aavso.tools.vstar.util.stats.epoch.MaxMagEpochStrategy;
import org.aavso.tools.vstar.util.stats.epoch.MinMagEpochStrategy;
import org.quicktheories.WithQuickTheories;

/**
 * Phase calculation UTs.
 */
public class PhaseCalcsTest extends TestCase implements WithQuickTheories {

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

	public void testDetermineEpochAlphaAndOmegaMeanJD() {
		List<ValidObservation> observations = populateObservations(mags1, jds1);
		double epoch = new AlphaOmegaMeanJDEpochStrategy().determineEpoch(observations);		
		assertEquals(2450003.25, epoch);
	}
	
	public void testDetermineMinMagJD() {
		List<ValidObservation> observations = populateObservations(mags1, jds1);
		double epoch = new MinMagEpochStrategy().determineEpoch(observations);		
		assertEquals(2450001.5, epoch);		
	}
	
	public void testDetermineMaxMagJD() {
		List<ValidObservation> observations = populateObservations(mags1, jds1);
		double epoch = new MaxMagEpochStrategy().determineEpoch(observations);		
		assertEquals(2450003.5, epoch);		
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
	
    public void testPhaseInRangeProperty() {
        double minJD = 2400000;
        double maxJD = 2460000;
        double minPeriod = 0.0001;
        double maxPeriod = 1e6;

        qt().forAll(
                doubles().between(minJD, maxJD),
                doubles().between(minJD, maxJD),
                doubles().from(minPeriod).upToAndIncluding(maxPeriod))
                .check((jd, epoch, period) -> {
                    double phase = PhaseCalcs.calcStandardPhase(jd, epoch, period);
                    return phase >= 0 && phase <= 1;
                });
    }

    /**
     * Phase is periodic in JD: shifting JD by one period should not change the
     * phase (within floating-point tolerance). Comparison uses circular
     * distance since phases 0.0 and ~1.0 are adjacent on the unit circle.
     */
    public void testPhasePeriodicityProperty() {
        double minJD = 2400000;
        double maxJD = 2460000;
        double minPeriod = 0.001;
        double maxPeriod = 1e4;

        qt().forAll(
                doubles().between(minJD, maxJD),
                doubles().between(minJD, maxJD),
                doubles().from(minPeriod).upToAndIncluding(maxPeriod))
                .check((jd, epoch, period) -> {
                    double phase1 = PhaseCalcs.calcStandardPhase(jd, epoch, period);
                    double phase2 = PhaseCalcs.calcStandardPhase(jd + period, epoch, period);
                    double diff = Math.abs(phase1 - phase2);
                    double circularDist = Math.min(diff, 1.0 - diff);
                    return circularDist < 1e-6;
                });
    }

    /**
     * previousCyclePhase = standardPhase - 1, so it must always lie in [-1, 0].
     */
    public void testPreviousCyclePhaseRangeProperty() {
        double minJD = 2400000;
        double maxJD = 2460000;
        double minPeriod = 0.0001;
        double maxPeriod = 1e6;

        qt().forAll(
                doubles().between(minJD, maxJD),
                doubles().between(minJD, maxJD),
                doubles().from(minPeriod).upToAndIncluding(maxPeriod))
                .check((jd, epoch, period) -> {
                    double phase = PhaseCalcs.calcStandardPhase(jd, epoch, period);
                    double prev = phase - 1;
                    return prev >= -1 && prev <= 0;
                });
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
