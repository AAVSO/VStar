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

import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.external.lib.OCAnalysisDemoData.DemoDataset;
import org.aavso.tools.vstar.external.lib.OCAnalysisDemoData.DemoScenario;
import org.aavso.tools.vstar.external.lib.OCAnalysisLib.EventType;
import org.aavso.tools.vstar.external.lib.OCAnalysisLib.Parameters;
import org.aavso.tools.vstar.external.lib.OCAnalysisLib.Point;
import org.aavso.tools.vstar.external.lib.OCAnalysisLib.Result;
import org.aavso.tools.vstar.external.lib.OCAnalysisLib.TimingMethod;

import junit.framework.TestCase;

/**
 * Unit tests for {@link OCAnalysisDemoData} and Foster-style demo scenarios.
 */
public class OCAnalysisDemoDataTest extends TestCase {

    private static final double TOL = 0.08;

    public OCAnalysisDemoDataTest(String name) {
        super(name);
    }

    public void testGenerateCorrectEphemerisProducesObservations() {
        DemoDataset dataset = OCAnalysisDemoData
                .generate(DemoScenario.CORRECT_EPHEMERIS);
        assertTrue(dataset.observations.size() > 0);
        assertEquals(DemoScenario.CORRECT_EPHEMERIS, dataset.scenario);
        assertEquals(EventType.MAXIMUM, dataset.suggestedEventType);
    }

    public void testCorrectEphemerisDemoNearZeroOC() {
        assertDemoOcNear(DemoScenario.CORRECT_EPHEMERIS, 0.0);
    }

    public void testEpochOffsetDemoConstantOC() {
        assertDemoOcNear(DemoScenario.EPOCH_OFFSET, 0.0035);
    }

    public void testPeriodErrorDemoLinearSlope() {
        DemoDataset dataset = OCAnalysisDemoData
                .generate(DemoScenario.PERIOD_ERROR);
        Parameters params = new Parameters(dataset.modelPeriod,
                dataset.modelEpoch, EventType.MAXIMUM, TimingMethod.PARABOLIC,
                10, 3);
        Result result = OCAnalysisLib.analyze(dataset.observations, params);
        assertTrue(result.points.size() >= 5);
        double slope = estimateSlope(result.points);
        assertEquals(0.0021, slope, 0.0005);
    }

    public void testFosterClock2Table13_2OcValues() {
        double[] oc = OCAnalysisDemoData
                .fosterOcValues(OCAnalysisDemoData.FOSTER_CLOCK_2);
        for (int n = 0; n < oc.length; n++) {
            assertEquals("cycle " + n, 0.0035, oc[n], 0.0001);
        }
    }

    public void testFosterClock4SuggestedBreakCycle() {
        DemoDataset dataset = OCAnalysisDemoData
                .generate(DemoScenario.EPOCH_JUMP);
        assertEquals(Integer.valueOf(3), dataset.suggestedBreakCycle);
        assertTrue(dataset.expectedPattern.contains("break cycle 3"));
    }

    public void testFosterClock5SuggestedBreakCycle() {
        DemoDataset dataset = OCAnalysisDemoData
                .generate(DemoScenario.PERIOD_CHANGE);
        assertEquals(Integer.valueOf(5), dataset.suggestedBreakCycle);
        assertTrue(dataset.expectedPattern.contains("break cycle 5"));
    }

    public void testFosterClock6HasNoBreakCycle() {
        DemoDataset dataset = OCAnalysisDemoData
                .generate(DemoScenario.EVOLVING_PERIOD);
        assertNull(dataset.suggestedBreakCycle);
        assertTrue(dataset.expectedPattern.contains("Quadratic"));
    }

    public void testScenarioFromLabel() {
        assertEquals(DemoScenario.PERIOD_ERROR, OCAnalysisDemoData
                .scenarioFromLabel(DemoScenario.PERIOD_ERROR.getLabel()));
        assertNull(OCAnalysisDemoData.scenarioFromLabel("unknown"));
    }

    private static void assertDemoOcNear(DemoScenario scenario,
            double expectedOc) {
        DemoDataset dataset = OCAnalysisDemoData.generate(scenario);
        Parameters params = new Parameters(dataset.modelPeriod,
                dataset.modelEpoch, EventType.MAXIMUM, TimingMethod.PARABOLIC,
                10, 3);
        Result result = OCAnalysisLib.analyze(dataset.observations, params);
        assertTrue(result.points.size() >= 5);
        for (Point p : result.points) {
            assertEquals("cycle " + p.cycle, expectedOc, p.oc, TOL);
        }
    }

    private static double estimateSlope(java.util.List<Point> points) {
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
