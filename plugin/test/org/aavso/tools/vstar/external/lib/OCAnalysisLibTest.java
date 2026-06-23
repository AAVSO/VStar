/**
 * VStar: a statistical analysis tool for variable star data.
 * Copyright (C) 2026  AAVSO (http://www.aavso.org/)
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
package org.aavso.tools.vstar.external.lib;

import java.util.ArrayList;
import java.util.List;

import org.aavso.tools.vstar.data.Magnitude;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.external.lib.OCAnalysisLib.EventType;
import org.aavso.tools.vstar.external.lib.OCAnalysisLib.Parameters;
import org.aavso.tools.vstar.external.lib.OCAnalysisLib.Point;
import org.aavso.tools.vstar.external.lib.OCAnalysisLib.Result;
import org.aavso.tools.vstar.external.lib.OCAnalysisLib.TimingMethod;

import junit.framework.TestCase;

/**
 * Unit tests for {@link OCAnalysisLib}.
 */
public class OCAnalysisLibTest extends TestCase {

    private static final double TOL = 1e-4;

    public OCAnalysisLibTest(String name) {
        super(name);
    }

    public void testCycleNumber() {
        assertEquals(0, OCAnalysisLib.cycleNumber(2450000.0, 2450000.0, 1.0));
        assertEquals(5, OCAnalysisLib.cycleNumber(2450005.4, 2450000.0, 1.0));
        assertEquals(-1, OCAnalysisLib.cycleNumber(2449999.4, 2450000.0, 1.0));
    }

    /**
     * Correct ephemeris: O-C should be near zero (Foster clock #1 analogue).
     */
    public void testCorrectEphemerisNearZeroOC() {
        double epoch = 2450000.0;
        double period = 1.0;
        List<ValidObservation> obs = syntheticMaxima(epoch, period, 0.0, 8);

        Parameters params = new Parameters(period, epoch, EventType.MAXIMUM,
                TimingMethod.PARABOLIC, 10, 3);
        Result result = OCAnalysisLib.analyze(obs, params);

        assertTrue(result.points.size() >= 5);
        for (Point p : result.points) {
            assertEquals("cycle " + p.cycle, 0.0, p.oc, 0.05);
        }
    }

    /**
     * Wrong epoch, correct period: constant O-C offset (Foster clock #2).
     */
    public void testEpochOffsetGivesConstantOC() {
        double trueEpoch = 2450000.0035;
        double modelEpoch = 2450000.0;
        double period = 1.0;
        List<ValidObservation> obs = syntheticMaxima(trueEpoch, period, 0.0, 8);

        Parameters params = new Parameters(period, modelEpoch, EventType.MAXIMUM,
                TimingMethod.PARABOLIC, 10, 3);
        Result result = OCAnalysisLib.analyze(obs, params);

        assertTrue(result.points.size() >= 5);
        for (Point p : result.points) {
            assertEquals("cycle " + p.cycle, 0.0035, p.oc, 0.05);
        }
    }

    /**
     * Wrong period: O-C slope approximates delta-P (Foster clock #3).
     */
    public void testPeriodErrorGivesLinearOC() {
        double epoch = 2450000.0;
        double truePeriod = 1.0021;
        double modelPeriod = 1.0;
        List<ValidObservation> obs = syntheticMaxima(epoch, truePeriod, 0.0, 10);

        Parameters params = new Parameters(modelPeriod, epoch, EventType.MAXIMUM,
                TimingMethod.PARABOLIC, 10, 3);
        Result result = OCAnalysisLib.analyze(obs, params);

        assertTrue(result.points.size() >= 5);
        double slope = estimateSlope(result.points);
        assertEquals(0.0021, slope, 0.0005);
    }

    public void testMeanExtremeTimingMethod() {
        double epoch = 2450000.0;
        double period = 1.0;
        List<ValidObservation> obs = syntheticMaxima(epoch, period, 0.0, 6);

        Parameters params = new Parameters(period, epoch, EventType.MAXIMUM,
                TimingMethod.MEAN_OF_EXTREME, 20, 3);
        Result result = OCAnalysisLib.analyze(obs, params);

        assertTrue(result.points.size() >= 3);
        for (Point p : result.points) {
            assertEquals("cycle " + p.cycle, 0.0, p.oc, 0.08);
        }
    }

    public void testSkipsSparseCycles() {
        double epoch = 2450000.0;
        double period = 1.0;
        List<ValidObservation> obs = syntheticMaxima(epoch, period, 0.0, 3);
        // Remove observations from cycle 1 so it has fewer than minObsPerCycle.
        List<ValidObservation> trimmed = new ArrayList<ValidObservation>();
        for (ValidObservation ob : obs) {
            int cycle = OCAnalysisLib.cycleNumber(ob.getJD(), epoch, period);
            if (cycle != 1) {
                trimmed.add(ob);
            }
        }

        Parameters params = new Parameters(period, epoch, EventType.MAXIMUM,
                TimingMethod.PARABOLIC, 10, 3);
        Result result = OCAnalysisLib.analyze(trimmed, params);

        for (Point p : result.points) {
            assertFalse(p.cycle == 1);
        }
    }

    public void testParabolicExtremumAtCentre() {
        double t = OCAnalysisLib.parabolicExtremumTime(0.0, 2.0, 1.0, 0.0, 2.0,
                2.0, EventType.MAXIMUM);
        assertEquals(1.0, t, 1e-6);
    }

    /**
     * Build a synthetic light curve with maxima at epoch + n*period + offset.
     * Each cycle has observations spanning +/- 0.2 days around the maximum.
     */
    private static List<ValidObservation> syntheticMaxima(double epoch,
            double period, double timeOffset, int numCycles) {
        List<ValidObservation> obs = new ArrayList<ValidObservation>();
        for (int n = 0; n < numCycles; n++) {
            double tMax = epoch + n * period + timeOffset;
            for (double dt = -0.2; dt <= 0.2; dt += 0.05) {
                double jd = tMax + dt;
                double mag = 10.0 + 20.0 * dt * dt;
                obs.add(ob(jd, mag));
            }
        }
        return obs;
    }

    private static ValidObservation ob(double jd, double mag) {
        ValidObservation ob = new ValidObservation();
        ob.setJD(jd);
        ob.setMagnitude(new Magnitude(mag, 0.01));
        return ob;
    }

    private static double estimateSlope(List<Point> points) {
        if (points.size() < 2) {
            return Double.NaN;
        }
        double sumX = 0;
        double sumY = 0;
        double sumXY = 0;
        double sumXX = 0;
        int n = points.size();
        for (Point p : points) {
            sumX += p.cycle;
            sumY += p.oc;
            sumXY += p.cycle * p.oc;
            sumXX += p.cycle * p.cycle;
        }
        return (n * sumXY - sumX * sumY) / (n * sumXX - sumX * sumX);
    }
}
