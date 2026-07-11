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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.aavso.tools.vstar.data.Magnitude;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.external.lib.OCAnalysisDemoData;
import org.aavso.tools.vstar.external.lib.OCAnalysisDemoData.DemoDataset;
import org.aavso.tools.vstar.external.lib.OCAnalysisDemoData.DemoScenario;
import org.aavso.tools.vstar.external.lib.OCAnalysisLib.EventType;
import org.aavso.tools.vstar.external.lib.OCAnalysisLib.ImportedTiming;
import org.aavso.tools.vstar.external.lib.OCAnalysisLib.Parameters;
import org.aavso.tools.vstar.external.lib.OCAnalysisLib.Point;
import org.aavso.tools.vstar.external.lib.OCAnalysisLib.QuadraticFit;
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
        DemoDataset dataset = OCAnalysisDemoData
                .generate(DemoScenario.CORRECT_EPHEMERIS);
        Parameters params = new Parameters(dataset.modelPeriod,
                dataset.modelEpoch, EventType.MAXIMUM, TimingMethod.PARABOLIC,
                10, 3);
        Result result = OCAnalysisLib.analyze(dataset.observations, params);

        assertTrue(result.points.size() >= 5);
        for (Point p : result.points) {
            assertEquals("cycle " + p.cycle, 0.0, p.oc, 0.05);
        }
    }

    /**
     * Wrong epoch, correct period: constant O-C offset (Foster clock #2).
     */
    public void testEpochOffsetGivesConstantOC() {
        DemoDataset dataset = OCAnalysisDemoData
                .generate(DemoScenario.EPOCH_OFFSET);
        Parameters params = new Parameters(dataset.modelPeriod,
                dataset.modelEpoch, EventType.MAXIMUM, TimingMethod.PARABOLIC,
                10, 3);
        Result result = OCAnalysisLib.analyze(dataset.observations, params);

        assertTrue(result.points.size() >= 5);
        for (Point p : result.points) {
            assertEquals("cycle " + p.cycle, 0.0035, p.oc, 0.05);
        }
    }

    /**
     * Wrong period: O-C slope approximates delta-P (Foster clock #3).
     */
    public void testPeriodErrorGivesLinearOC() {
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

    public void testMeanExtremeTimingMethod() {
        DemoDataset dataset = OCAnalysisDemoData
                .generate(DemoScenario.CORRECT_EPHEMERIS);
        Parameters params = new Parameters(dataset.modelPeriod,
                dataset.modelEpoch, EventType.MAXIMUM,
                TimingMethod.MEAN_OF_EXTREME, 20, 3);
        Result result = OCAnalysisLib.analyze(dataset.observations, params);

        assertTrue(result.points.size() >= 3);
        for (Point p : result.points) {
            assertEquals("cycle " + p.cycle, 0.0, p.oc, 0.08);
        }
    }

    public void testSkipsSparseCycles() {
        DemoDataset dataset = OCAnalysisDemoData
                .generate(DemoScenario.CORRECT_EPHEMERIS, 2450000.0, 1.0, 3);
        double epoch = dataset.modelEpoch;
        double period = dataset.modelPeriod;
        List<ValidObservation> obs = dataset.observations;
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

    public void testLinearFitSlope() {
        List<OCAnalysisLib.Point> points = new ArrayList<OCAnalysisLib.Point>();
        for (int n = 0; n < 10; n++) {
            double oc = 0.0021 * n;
            points.add(new OCAnalysisLib.Point(n, n + oc, n, Double.NaN, 5,
                    EventType.MAXIMUM));
        }
        OCAnalysisLib.LinearFit fit = OCAnalysisLib.fitLinear(points);
        assertNotNull(fit);
        assertEquals(0.0021, fit.slope, 1e-6);
        assertEquals(0.0, fit.intercept, 1e-6);
    }

    public void testTwoSegmentFit() {
        List<OCAnalysisLib.Point> points = new ArrayList<OCAnalysisLib.Point>();
        for (int n = 0; n < 5; n++) {
            points.add(new OCAnalysisLib.Point(n, n, n, Double.NaN, 5,
                    EventType.MAXIMUM));
        }
        for (int n = 5; n < 10; n++) {
            double oc = 0.01;
            points.add(new OCAnalysisLib.Point(n, n + oc, n, Double.NaN, 5,
                    EventType.MAXIMUM));
        }
        OCAnalysisLib.TwoSegmentFit fit = OCAnalysisLib.fitTwoSegment(points, 4);
        assertNotNull(fit);
        assertEquals(0.0, fit.firstSegment.slope, 1e-6);
        assertEquals(0.0, fit.secondSegment.slope, 1e-6);
        assertEquals(0.01, fit.secondSegment.intercept, 1e-6);
    }

    public void testInterpretLinearFitContainsCorrections() {
        OCAnalysisLib.LinearFit fit = new OCAnalysisLib.LinearFit(0.0035, 0.0021,
                0.0001, 10);
        String text = OCAnalysisLib.interpretLinearFit(fit, 1.0);
        assertTrue(text.contains("ΔP"));
        assertTrue(text.contains("epoch correction"));
    }

    public void testParabolicExtremumAtCentre() {
        double t = OCAnalysisLib.parabolicExtremumTime(0.0, 2.0, 1.0, 0.0, 2.0,
                2.0, OCAnalysisLib.EventType.MAXIMUM);
        assertEquals(1.0, t, 1e-6);
    }

    public void testParseImportedTimingsWithExplicitCycle() throws IOException {
        List<String> lines = Arrays.asList("# header", "0 2450000.0 0.001",
                "2 2450002.5");
        List<ImportedTiming> timings = OCAnalysisLib.parseImportedTimings(lines,
                2450000.0, 1.0);
        assertEquals(2, timings.size());
        assertEquals(Integer.valueOf(0), timings.get(0).cycle);
        assertEquals(2450000.0, timings.get(0).observedTime, TOL);
        assertEquals(0.001, timings.get(0).uncertaintyDays, TOL);
        assertEquals(Integer.valueOf(2), timings.get(1).cycle);
        assertEquals(2450002.5, timings.get(1).observedTime, TOL);
        assertTrue(Double.isNaN(timings.get(1).uncertaintyDays));
    }

    public void testParseImportedTimingsInfersCycle() throws IOException {
        List<String> lines = Arrays.asList("2450001.0 0.002", "2450003.0");
        List<ImportedTiming> timings = OCAnalysisLib.parseImportedTimings(lines,
                2450000.0, 1.0);
        assertEquals(2, timings.size());
        assertEquals(Integer.valueOf(1), timings.get(0).cycle);
        assertEquals(0.002, timings.get(0).uncertaintyDays, TOL);
        assertEquals(Integer.valueOf(3), timings.get(1).cycle);
    }

    public void testParseImportedTimingsCommaSemicolonSeparators()
            throws IOException {
        List<String> lines = Arrays.asList("0,2450000.0,0.001",
                "1;2450001.0;0.002", "2450002.0,0.003");
        List<ImportedTiming> timings = OCAnalysisLib.parseImportedTimings(lines,
                2450000.0, 1.0);
        assertEquals(3, timings.size());
        assertEquals(Integer.valueOf(0), timings.get(0).cycle);
        assertEquals(2450000.0, timings.get(0).observedTime, TOL);
        assertEquals(0.001, timings.get(0).uncertaintyDays, TOL);
        assertEquals(Integer.valueOf(1), timings.get(1).cycle);
        assertEquals(0.002, timings.get(1).uncertaintyDays, TOL);
        assertEquals(Integer.valueOf(2), timings.get(2).cycle);
        assertEquals(0.003, timings.get(2).uncertaintyDays, TOL);
    }

    public void testParseImportedTimingsInvalidLineThrows() {
        List<String> lines = Arrays.asList("not-a-number");
        try {
            OCAnalysisLib.parseImportedTimings(lines, 2450000.0, 1.0);
            fail("Expected IOException");
        } catch (IOException ex) {
            assertTrue(ex.getMessage().contains("line 1"));
        }
    }

    public void testAnalyzeImportedComputesOC() {
        double epoch = 2450000.0;
        double period = 1.0;
        List<ImportedTiming> timings = Arrays.asList(
                new ImportedTiming(0, epoch, Double.NaN),
                new ImportedTiming(1, epoch + period + 0.0035, Double.NaN));
        Parameters params = new Parameters(period, epoch, EventType.MAXIMUM,
                TimingMethod.PARABOLIC, 10, 1);
        Result result = OCAnalysisLib.analyzeImported(timings, params);
        assertEquals(2, result.points.size());
        assertEquals(0.0, result.points.get(0).oc, TOL);
        assertEquals(0.0035, result.points.get(1).oc, TOL);
        assertEquals(EventType.MAXIMUM, result.points.get(0).extremumType);
    }

    public void testAnalyzeImportedEmptyList() {
        Parameters params = new Parameters(1.0, 2450000.0, EventType.MAXIMUM,
                TimingMethod.PARABOLIC, 10, 1);
        Result result = OCAnalysisLib.analyzeImported(
                new ArrayList<ImportedTiming>(), params);
        assertTrue(result.points.isEmpty());
        Result nullResult = OCAnalysisLib.analyzeImported(null, params);
        assertTrue(nullResult.points.isEmpty());
    }

    public void testQuadraticFitOnEvolvingPeriodOC() {
        List<Point> points = new ArrayList<Point>();
        for (int n = 0; n < 10; n++) {
            double oc = 0.0001 * n * n;
            points.add(new Point(n, n + oc, n, Double.NaN, 5,
                    EventType.MAXIMUM));
        }
        QuadraticFit fit = OCAnalysisLib.fitQuadratic(points);
        assertNotNull(fit);
        assertEquals(0.0, fit.constant, 1e-6);
        assertEquals(0.0, fit.linear, 1e-6);
        assertEquals(0.0001, fit.quadratic, 1e-6);
    }

    public void testQuadraticFitReturnsNullForTwoPoints() {
        List<Point> points = Arrays.asList(
                new Point(0, 0.0, 0.0, Double.NaN, 5, EventType.MAXIMUM),
                new Point(1, 0.0021, 1.0, Double.NaN, 5, EventType.MAXIMUM));
        assertNull(OCAnalysisLib.fitQuadratic(points));
    }

    public void testInterpretQuadraticFitMentionsEvolvingPeriod() {
        QuadraticFit fit = new QuadraticFit(0.0, 0.0021, 0.0001, 0.0005, 10);
        String text = OCAnalysisLib.interpretQuadraticFit(fit, 1.0);
        assertTrue(text.contains("evolving period"));
        assertTrue(text.contains("ΔP/cycle"));
    }

    public void testAnalyzeBothEventTypeReturnsMaxAndMin() {
        double epoch = 2450000.0;
        double period = 1.0;
        List<ValidObservation> obs = new ArrayList<ValidObservation>();
        for (int n = 0; n < 5; n++) {
            addSyntheticMaximum(obs, epoch + n * period);
            addSyntheticMinimum(obs, epoch + n * period + period / 2.0);
        }
        Parameters params = new Parameters(period, epoch, EventType.BOTH,
                TimingMethod.PARABOLIC, 10, 3);
        Result result = OCAnalysisLib.analyze(obs, params);
        assertTrue(result.points.size() >= 6);
        boolean hasMax = false;
        boolean hasMin = false;
        for (Point p : result.points) {
            if (p.extremumType == EventType.MAXIMUM) {
                hasMax = true;
            } else if (p.extremumType == EventType.MINIMUM) {
                hasMin = true;
            }
        }
        assertTrue(hasMax);
        assertTrue(hasMin);
    }

    private static void addSyntheticMaximum(List<ValidObservation> obs,
            double tMax) {
        for (double dt = -0.2; dt <= 0.2; dt += 0.05) {
            ValidObservation ob = new ValidObservation();
            ob.setJD(tMax + dt);
            ob.setMagnitude(new Magnitude(10.0 + 20.0 * dt * dt, 0.01));
            obs.add(ob);
        }
    }

    private static void addSyntheticMinimum(List<ValidObservation> obs,
            double tMin) {
        for (double dt = -0.2; dt <= 0.2; dt += 0.05) {
            ValidObservation ob = new ValidObservation();
            ob.setJD(tMin + dt);
            ob.setMagnitude(new Magnitude(12.0 - 20.0 * dt * dt, 0.01));
            obs.add(ob);
        }
    }

    public void testGetPeriodScatterWarningNotEmpty() {
        String warning = OCAnalysisLib.getPeriodScatterWarning();
        assertNotNull(warning);
        assertTrue(warning.contains("Foster"));
        assertTrue(warning.contains("period scatter"));
    }

    public void testWriteCsvContainsHeaderAndData() {
        double epoch = 2450000.0;
        List<Point> points = Arrays.asList(new Point(0, epoch, epoch, 0.001, 5,
                EventType.MAXIMUM));
        Parameters params = new Parameters(1.0, epoch, EventType.MAXIMUM,
                TimingMethod.PARABOLIC, 10, 3);
        Result result = new Result(params, points);
        StringWriter sw = new StringWriter();
        OCAnalysisLib.writeCsv(result, new PrintWriter(sw), null, null, null);
        String csv = sw.toString();
        assertTrue(csv.contains("Event,Cycle,O_HJD,C_HJD,OC_days"));
        assertTrue(csv.contains("MAXIMUM,0,"));
    }

    public void testParseImportedTimingsFromExportCsv() throws IOException {
        double epoch = 2450000.0;
        List<String> lines = Arrays.asList(
                "# O-C Analysis export",
                "# period=1.19525556, epoch=2460646.2029",
                "Event,Cycle,O_HJD,C_HJD,OC_days,OC_sigma,ObsInCycle",
                "MINIMUM,100,2460746.5,2460746.48,0.02,0.001,5",
                "MINIMUM,101,2460747.7,2460747.68,0.02,,3");
        List<ImportedTiming> timings = OCAnalysisLib.parseImportedTimings(lines,
                epoch, 1.19525556);
        assertEquals(2, timings.size());
        assertEquals(Integer.valueOf(100), timings.get(0).cycle);
        assertEquals(2460746.5, timings.get(0).observedTime, TOL);
        assertEquals(0.001, timings.get(0).uncertaintyDays, TOL);
        assertEquals(Integer.valueOf(101), timings.get(1).cycle);
        assertTrue(Double.isNaN(timings.get(1).uncertaintyDays));
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
