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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.aavso.tools.vstar.data.Magnitude;
import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.external.lib.OCAnalysisDemoData;
import org.aavso.tools.vstar.external.lib.OCAnalysisDemoData.DemoDataset;
import org.aavso.tools.vstar.external.lib.OCAnalysisDemoData.DemoScenario;
import org.aavso.tools.vstar.external.lib.OCAnalysisLib.EditableTimingsModel;
import org.aavso.tools.vstar.external.lib.OCAnalysisLib.EventType;
import org.aavso.tools.vstar.external.lib.OCAnalysisLib.ImportedTiming;
import org.aavso.tools.vstar.external.lib.OCAnalysisLib.LinearFit;
import org.aavso.tools.vstar.external.lib.OCAnalysisLib.Parameters;
import org.aavso.tools.vstar.external.lib.OCAnalysisLib.Point;
import org.aavso.tools.vstar.external.lib.OCAnalysisLib.QuadraticFit;
import org.aavso.tools.vstar.external.lib.OCAnalysisLib.Result;
import org.aavso.tools.vstar.external.lib.OCAnalysisLib.TimingMethod;
import org.aavso.tools.vstar.external.lib.OCAnalysisLib.TwoSegmentFit;

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

    public void testInterpretOcDiagramFosterClock1FlatAtZero() {
        List<Point> points = fosterOcPoints(OCAnalysisDemoData.FOSTER_CLOCK_1);
        LinearFit linear = OCAnalysisLib.fitLinear(points);
        String text = OCAnalysisLib.interpretOcDiagram(linear, null, null,
                points, OCAnalysisLib.OcDiagramFitMode.LINEAR, 1.0);
        assertTrue(text.contains("flat O-C at 0"));
        assertTrue(text.contains("ephemeris"));
    }

    public void testInterpretOcDiagramFosterClock2EpochOffset() {
        List<Point> points = fosterOcPoints(OCAnalysisDemoData.FOSTER_CLOCK_2);
        LinearFit linear = OCAnalysisLib.fitLinear(points);
        String text = OCAnalysisLib.interpretOcDiagram(linear, null, null,
                points, OCAnalysisLib.OcDiagramFitMode.LINEAR, 1.0);
        assertTrue(text.contains("epoch wrong"));
        assertTrue(text.contains("period OK"));
    }

    public void testInterpretOcDiagramFosterClock3PeriodError() {
        List<Point> points = fosterOcPoints(OCAnalysisDemoData.FOSTER_CLOCK_3);
        LinearFit linear = OCAnalysisLib.fitLinear(points);
        String text = OCAnalysisLib.interpretOcDiagram(linear, null, null,
                points, OCAnalysisLib.OcDiagramFitMode.LINEAR, 1.0);
        assertTrue(text.contains("period wrong"));
    }

    public void testInterpretOcDiagramFosterClock4EpochJump() {
        List<Point> points = fosterOcPoints(OCAnalysisDemoData.FOSTER_CLOCK_4);
        // Break at 3 keeps the pre-jump plateaus separate from post-jump cycles.
        TwoSegmentFit fit = OCAnalysisLib.fitTwoSegment(points, 3);
        String text = OCAnalysisLib.interpretOcDiagram(
                OCAnalysisLib.fitLinear(points), null, fit, points,
                OCAnalysisLib.OcDiagramFitMode.TWO_SEGMENT, 1.0);
        assertTrue(text.contains("epoch jump"));
    }

    public void testInterpretOcDiagramFosterClock5PeriodChange() {
        List<Point> points = fosterOcPoints(OCAnalysisDemoData.FOSTER_CLOCK_5);
        TwoSegmentFit fit = OCAnalysisLib.fitTwoSegment(points, 5);
        String text = OCAnalysisLib.interpretOcDiagram(
                OCAnalysisLib.fitLinear(points), null, fit, points,
                OCAnalysisLib.OcDiagramFitMode.TWO_SEGMENT, 1.0);
        assertTrue(text.contains("period change"));
    }

    public void testInterpretOcDiagramFosterClock6EvolvingPeriod() {
        List<Point> points = fosterOcPoints(OCAnalysisDemoData.FOSTER_CLOCK_6);
        QuadraticFit quadratic = OCAnalysisLib.fitQuadratic(points);
        String text = OCAnalysisLib.interpretOcDiagram(
                OCAnalysisLib.fitLinear(points), quadratic, null, points,
                OCAnalysisLib.OcDiagramFitMode.QUADRATIC, 1.0);
        assertTrue(text.contains("evolving period"));
    }

    private static List<Point> fosterOcPoints(double[] dayOffsetsFromEpoch) {
        List<Point> points = new ArrayList<Point>();
        for (int n = 0; n < dayOffsetsFromEpoch.length; n++) {
            points.add(new Point(n, dayOffsetsFromEpoch[n], n, Double.NaN, 5,
                    EventType.MAXIMUM));
        }
        return points;
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
        assertTrue(warning.contains("Variable Star Astronomy"));
        assertTrue(warning.contains(OCAnalysisLib.VSA_CHAPTER13_PDF_URL));
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
        assertTrue(csv.contains("Event,Cycle,O_time,C_time,OC_days"));
        assertTrue(csv.contains("MAXIMUM,0,"));
    }

    public void testParseImportedTimingsFromExportCsv() throws IOException {
        double epoch = 2450000.0;
        List<String> lines = Arrays.asList(
                "# O-C export",
                "# period=1.19525556, epoch=2460646.2029",
                "Event,Cycle,O_time,C_time,OC_days,OC_sigma,ObsInCycle",
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

    public void testParseImportFileMetadataFromExportCsv() {
        List<String> lines = Arrays.asList(
                "# O-C export",
                "# period=1.19525556, epoch=2460646.2029",
                "Event,Cycle,O_time,C_time,OC_days,OC_sigma,ObsInCycle",
                "MINIMUM,100,2460746.5,2460746.48,0.02,0.001,5");
        OCAnalysisLib.ImportFileMetadata meta = OCAnalysisLib
                .parseImportFileMetadata(lines);
        assertEquals(1.19525556, meta.period, TOL);
        assertEquals(2460646.2029, meta.epoch, TOL);
        assertEquals(EventType.MINIMUM, meta.eventType);
    }

    public void testParseImportFileMetadataPlainTimingsEmpty() {
        List<String> lines = Arrays.asList("0 2450000.0", "1 2450001.0");
        OCAnalysisLib.ImportFileMetadata meta = OCAnalysisLib
                .parseImportFileMetadata(lines);
        assertNull(meta.period);
        assertNull(meta.epoch);
        assertNull(meta.eventType);
    }

    /**
     * From-observations points record finite peak magnitudes for light-curve
     * markers.
     */
    public void testObservedMagnitudesFromParabolicAnalysis() {
        DemoDataset dataset = OCAnalysisDemoData
                .generate(DemoScenario.CORRECT_EPHEMERIS);
        Parameters params = new Parameters(dataset.modelPeriod,
                dataset.modelEpoch, EventType.MAXIMUM, TimingMethod.PARABOLIC,
                10, 3);
        Result result = OCAnalysisLib.analyze(dataset.observations, params);
        assertTrue(result.points.size() >= 5);
        for (Point p : result.points) {
            assertFalse("cycle " + p.cycle + " mag",
                    Double.isNaN(p.observedMagnitude));
            assertFalse("cycle " + p.cycle + " mag",
                    Double.isInfinite(p.observedMagnitude));
            // Demo bumps peak near mag 10-ish; allow a generous range.
            assertTrue("cycle " + p.cycle + " mag=" + p.observedMagnitude,
                    p.observedMagnitude > 0 && p.observedMagnitude < 20);
        }
    }

    public void testToExtremumObservationsMatchesPoints() {
        DemoDataset dataset = OCAnalysisDemoData
                .generate(DemoScenario.EPOCH_OFFSET);
        Parameters params = new Parameters(dataset.modelPeriod,
                dataset.modelEpoch, EventType.MAXIMUM, TimingMethod.PARABOLIC,
                10, 3);
        Result result = OCAnalysisLib.analyze(dataset.observations, params);
        List<ValidObservation> markers = OCAnalysisLib
                .toExtremumObservations(result);
        assertEquals(result.points.size(), markers.size());
        SeriesType series = OCAnalysisLib.extremaSeriesType();
        assertEquals(OCAnalysisLib.EXTREMA_SERIES_DESCRIPTION,
                series.getDescription());
        for (int i = 0; i < result.points.size(); i++) {
            Point p = result.points.get(i);
            ValidObservation m = markers.get(i);
            assertEquals(p.observedTime, m.getJD(), TOL);
            assertEquals(p.observedMagnitude, m.getMag(), 1e-6);
            assertEquals(series, m.getBand());
            assertTrue(m.getComments().contains("cycle=" + p.cycle));
        }
    }

    public void testKweeVanWoerdenRecoversGaussianMinima() {
        double period = 1.0;
        double epoch = 2450000.0;
        List<ValidObservation> obs = new ArrayList<ValidObservation>();
        // Three well-sampled eclipses (minima fainter in mag).
        for (int cycle = 0; cycle < 3; cycle++) {
            double tMin = epoch + cycle * period + 0.01 * cycle; // slight O-C
            for (int i = -15; i <= 15; i++) {
                double t = tMin + i * 0.005;
                double mag = 12.0
                        + 0.4 * Math.exp(-0.5 * Math.pow(i * 0.005 / 0.04, 2));
                addObs(obs, t, mag);
            }
        }
        Parameters params = new Parameters(period, epoch, EventType.MINIMUM,
                TimingMethod.KWEE_VAN_WOERDEN, 10, 7, null, 5);
        Result result = OCAnalysisLib.analyze(obs, params);
        assertEquals(3, result.cyclesTimed);
        assertEquals(0, result.cyclesSkipped());
        assertEquals(3, result.points.size());
        for (int i = 0; i < 3; i++) {
            Point p = result.points.get(i);
            double expectedO = epoch + i * period + 0.01 * i;
            assertEquals("cycle " + p.cycle, expectedO, p.observedTime, 0.01);
            assertTrue("sigma", !Double.isNaN(p.ocUncertainty)
                    && p.ocUncertainty > 0);
            assertNotNull(p.qc);
            assertTrue(p.qc.summaryText().contains("KvW"));
            assertTrue(p.qc.windowObsCount >= OCAnalysisLib.KVW_MIN_POINTS);
        }
    }

    public void testKweeVanWoerdenSkipsSparseFlatCycle() {
        double period = 1.0;
        double epoch = 2450000.0;
        List<ValidObservation> obs = new ArrayList<ValidObservation>();
        // Flat, few points — window depth fails or n < 7.
        for (int i = 0; i < 5; i++) {
            addObs(obs, epoch + 0.1 * i, 12.0);
        }
        Parameters params = new Parameters(period, epoch, EventType.MINIMUM,
                TimingMethod.KWEE_VAN_WOERDEN, 10, 3, null, 5);
        Result result = OCAnalysisLib.analyze(obs, params);
        assertEquals(0, result.points.size());
        assertTrue(result.cyclesExamined >= 1 || result.cyclesTimed == 0);
        // Examined may be 1 if minObs=3 gate passed with 5 points, but no timing.
        if (result.cyclesExamined >= 1) {
            assertEquals(result.cyclesExamined, result.cyclesSkipped());
        }
    }

    public void testTrimEclipseWindowRequiresDepth() {
        List<ValidObservation> obs = new ArrayList<ValidObservation>();
        for (int i = 0; i < 10; i++) {
            addObs(obs, 2450000.0 + 0.01 * i, 12.0);
        }
        assertNull(OCAnalysisLib.trimEclipseWindow(obs, EventType.MINIMUM, 1.0));
    }

    public void testToExtremumObservationsSkipsImportedNaNMags()
            throws IOException {
        List<ImportedTiming> timings = Arrays.asList(
                new ImportedTiming(0, 2450000.0, Double.NaN),
                new ImportedTiming(1, 2450001.0, 0.001));
        Parameters params = new Parameters(1.0, 2450000.0, EventType.MAXIMUM,
                TimingMethod.PARABOLIC, 10, 1);
        Result result = OCAnalysisLib.analyzeImported(timings, params);
        assertEquals(2, result.points.size());
        assertTrue(Double.isNaN(result.points.get(0).observedMagnitude));
        List<ValidObservation> markers = OCAnalysisLib
                .toExtremumObservations(result);
        assertTrue(markers.isEmpty());
    }

    public void testEditableTimingsModelRebuildAndRemove() {
        Parameters params = new Parameters(1.0, 2450000.0, EventType.MAXIMUM,
                TimingMethod.PARABOLIC, 10, 1);
        EditableTimingsModel model = new EditableTimingsModel();
        model.add(2450000.01, OCAnalysisLib.TIMING_SOURCE_MANUAL);
        model.add(2450001.02, OCAnalysisLib.TIMING_SOURCE_SNAP);
        Result result = model.toResult(params);
        assertEquals(2, result.points.size());
        assertEquals(0.01, result.points.get(0).oc, TOL);
        assertEquals(0.02, result.points.get(1).oc, TOL);
        assertNotNull(result.points.get(0).qc);
        assertTrue(result.points.get(0).qc.summaryText().contains("manual"));
        assertTrue(result.points.get(1).qc.summaryText().contains("snap"));

        model.remove(0);
        result = model.toResult(params);
        assertEquals(1, result.points.size());
        assertEquals(0.02, result.points.get(0).oc, TOL);

        model.setObservedTime(0, 2450001.05, OCAnalysisLib.TIMING_SOURCE_MANUAL);
        result = model.toResult(params);
        assertEquals(0.05, result.points.get(0).oc, TOL);
    }

    public void testEditableTimingsModelFromResultSeedsAutos() {
        DemoDataset dataset = OCAnalysisDemoData
                .generate(DemoScenario.CORRECT_EPHEMERIS);
        Parameters params = new Parameters(dataset.modelPeriod,
                dataset.modelEpoch, EventType.MAXIMUM, TimingMethod.PARABOLIC,
                10, 3);
        Result auto = OCAnalysisLib.analyze(dataset.observations, params);
        assertTrue(auto.points.size() >= 2);
        EditableTimingsModel model = EditableTimingsModel.fromResult(auto);
        assertEquals(auto.points.size(), model.size());
        Result rebuilt = model.toResult(params);
        assertEquals(auto.points.size(), rebuilt.points.size());
        for (int i = 0; i < auto.points.size(); i++) {
            assertEquals(auto.points.get(i).observedTime,
                    rebuilt.points.get(i).observedTime, TOL);
        }
    }

    public void testNearestObservationJdWithinDelta() {
        List<ValidObservation> obs = new ArrayList<ValidObservation>();
        addObs(obs, 2450000.10, 11.0);
        addObs(obs, 2450000.20, 11.5);
        addObs(obs, 2450000.50, 12.0);
        Double near = OCAnalysisLib.nearestObservationJd(obs, 2450000.21,
                0.05);
        assertEquals(2450000.20, near, TOL);
        assertNull(OCAnalysisLib.nearestObservationJd(obs, 2450000.40, 0.05));
    }

    public void testNearestTimingIndex() {
        List<ImportedTiming> times = Arrays.asList(
                new ImportedTiming(null, 2450000.0, Double.NaN, "manual"),
                new ImportedTiming(null, 2450001.0, Double.NaN, "manual"));
        assertEquals(1, OCAnalysisLib.nearestTimingIndex(times, 2450001.01,
                0.05));
        assertEquals(-1, OCAnalysisLib.nearestTimingIndex(times, 2450002.5,
                0.05));
    }

    public void testXTriTable139ImportFixture() throws IOException {
        List<String> lines = readClasspathResourceLines(
                "data/oc/xtri_table_13_9.txt");
        assertTrue(lines.size() > 100);
        OCAnalysisLib.ImportFileMetadata meta = OCAnalysisLib
                .parseImportFileMetadata(lines);
        assertEquals(0.975352, meta.period, TOL);
        assertEquals(2442502.721, meta.epoch, TOL);

        List<ImportedTiming> timings = OCAnalysisLib.parseImportedTimings(lines,
                meta.epoch, meta.period);
        assertEquals(122, timings.size());
        assertEquals(Integer.valueOf(230), timings.get(0).cycle);
        assertEquals(2442726.175, timings.get(0).observedTime, TOL);

        Parameters params = new Parameters(meta.period, meta.epoch,
                EventType.MINIMUM, TimingMethod.PARABOLIC, 10, 1);
        Result result = OCAnalysisLib.analyzeImported(timings, params);
        assertEquals(122, result.points.size());
        // Cycle 230 with teaching ephemeris: O−C slightly negative (~−0.88 d).
        Point p0 = result.points.get(0);
        assertEquals(230, p0.cycle);
        assertEquals(-0.877, p0.oc, 0.002);
        // Long baseline: strong secular drift vs fixed linear ephemeris
        // (Activity 13.6: period/epoch not fully adequate; period evolution).
        LinearFit fit = OCAnalysisLib.fitLinear(result.points);
        assertNotNull(fit);
        assertTrue("expected large |slope| from teaching ephemeris, got "
                + fit.slope, Math.abs(fit.slope) > 0.001);
    }

    public void testZTauMaximaImportFixture() throws IOException {
        List<String> lines = readClasspathResourceLines(
                "data/oc/ztau_maxima.txt");
        assertTrue(lines.size() > 80);
        OCAnalysisLib.ImportFileMetadata meta = OCAnalysisLib
                .parseImportFileMetadata(lines);
        assertEquals(466.2, meta.period, TOL);
        assertEquals(2415246.2, meta.epoch, TOL);

        List<ImportedTiming> timings = OCAnalysisLib.parseImportedTimings(lines,
                meta.epoch, meta.period);
        assertEquals(84, timings.size());
        assertEquals(Integer.valueOf(4), timings.get(0).cycle);
        assertEquals(2417111.0, timings.get(0).observedTime, TOL);
        assertEquals(Integer.valueOf(90),
                timings.get(timings.size() - 1).cycle);
        assertEquals(2457970.0, timings.get(timings.size() - 1).observedTime,
                TOL);

        Parameters params = new Parameters(meta.period, meta.epoch,
                EventType.MAXIMUM, TimingMethod.PARABOLIC, 10, 1);
        Result result = OCAnalysisLib.analyzeImported(timings, params);
        assertEquals(84, result.points.size());
        // First table maximum: O−C ~ 0 with epoch tied to cycle numbering.
        assertEquals(0.0, result.points.get(0).oc, 0.01);
        // Mira period evolution vs fixed 466.2 d: large late O−C excursion.
        assertTrue("expected large late O-C, got "
                + result.points.get(result.points.size() - 1).oc,
                Math.abs(result.points.get(result.points.size() - 1).oc) > 100);
        LinearFit fit = OCAnalysisLib.fitLinear(result.points);
        assertNotNull(fit);
    }

    private static List<String> readClasspathResourceLines(String resource)
            throws IOException {
        InputStream in = OCAnalysisLibTest.class.getClassLoader()
                .getResourceAsStream(resource);
        assertNotNull("missing " + resource, in);
        List<String> lines = new ArrayList<String>();
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(in, StandardCharsets.UTF_8));
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } finally {
            reader.close();
        }
        return lines;
    }

    public void testMeanExtremeObservedMagnitude() {
        List<ValidObservation> cycle = new ArrayList<ValidObservation>();
        // Brightest (max light) are first two by mag order among bright end:
        // mags 11.0, 11.1 should average for 20% of 5 points → 1 point? 
        // 20% of 5 = 1 point of brightest.
        // Use 40% of 5 points = 2 brightest for a meaningful mean.
        addObs(cycle, 2450000.00, 12.0);
        addObs(cycle, 2450000.05, 11.0);
        addObs(cycle, 2450000.10, 11.2);
        addObs(cycle, 2450000.15, 12.1);
        addObs(cycle, 2450000.20, 12.5);
        // Make a multi-cycle set so analyze works with minObs=3 and period=1
        List<ValidObservation> obs = new ArrayList<ValidObservation>(cycle);
        addObs(obs, 2450001.00, 12.0);
        addObs(obs, 2450001.05, 11.0);
        addObs(obs, 2450001.10, 11.2);
        Parameters params = new Parameters(1.0, 2450000.0, EventType.MAXIMUM,
                TimingMethod.MEAN_OF_EXTREME, 40, 3);
        Result result = OCAnalysisLib.analyze(obs, params);
        assertTrue(result.points.size() >= 1);
        Point p0 = result.points.get(0);
        // Mean of 2 brightest of 5 (40%) at cycle 0: 11.0 and 11.2 → 11.1
        assertEquals(11.1, p0.observedMagnitude, 1e-6);
    }

    private static void addObs(List<ValidObservation> obs, double jd,
            double mag) {
        ValidObservation ob = new ValidObservation();
        ob.setJD(jd);
        ob.setMagnitude(new Magnitude(mag, 0.01));
        obs.add(ob);
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
