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
import java.util.Collections;
import java.util.List;

import org.aavso.tools.vstar.data.Magnitude;
import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.external.lib.OCAnalysisLib.EventType;

/**
 * Synthetic light curves for teaching O-C analysis (AAVSO Variable Star
 * Astronomy, ch. 13 / Grant Foster; Tables 13.1–13.2).
 *
 * <p>
 * Each {@link DemoScenario} places synthetic maxima at the chapter's observed
 * clock times (test theory: P = 1 d, epoch = cycle 0). Light-curve shape is
 * identical for every scenario; only timing differs, as in the clock analogy.
 * </p>
 */
public final class OCAnalysisDemoData {

    public static final double DEFAULT_EPOCH = 2450000.0;
    public static final double DEFAULT_PERIOD = 1.0;
    public static final int DEFAULT_NUM_CYCLES = 10;

    /** VSA / Foster test theory period (1 day). */
    public static final double FOSTER_THEORY_PERIOD = 1.0;

    /**
     * Observed event times in days from epoch (VSA Table 13.1, cycles 0–9).
     */
    public static final double[] FOSTER_CLOCK_1 = { 0, 1, 2, 3, 4, 5, 6, 7,
            8, 9 };
    public static final double[] FOSTER_CLOCK_2 = { 0.0035, 1.0035, 2.0035,
            3.0035, 4.0035, 5.0035, 6.0035, 7.0035, 8.0035, 9.0035 };
    public static final double[] FOSTER_CLOCK_3 = { 0, 1.0021, 2.0042, 3.0062,
            4.0083, 5.0104, 6.0125, 7.0146, 8.0167, 9.0188 };
    public static final double[] FOSTER_CLOCK_4 = { -0.0014, 0.9986, 1.9986,
            2.9986, 4.0257, 5.0257, 6.0257, 7.0257, 8.0257, 9.0257 };
    public static final double[] FOSTER_CLOCK_5 = { 0, 0.9986, 1.9972, 2.9958,
            3.9944, 4.9931, 5.9938, 6.9944, 7.9951, 8.9958 };
    public static final double[] FOSTER_CLOCK_6 = { 0, 1, 2.0007, 3.0021,
            4.0042, 5.0069, 6.0104, 7.0146, 8.0194, 9.0250 };

    /**
     * Demonstration scenarios for the six clocks in VSA ch. 13 (no eclipsing-binary
     * example in that chapter).
     */
    public enum DemoScenario {
        CORRECT_EPHEMERIS("Foster clock 1 — correct ephemeris",
                "Flat O-C at 0 (Table 13.2, clock 1)."),
        EPOCH_OFFSET("Foster clock 2 — epoch offset",
                "Flat O-C ≈ +0.0035 d (Table 13.2, clock 2)."),
        PERIOD_ERROR("Foster clock 3 — period error",
                "Linear O-C slope ≈ +0.0021 d/cycle (Table 13.2, clock 3)."),
        EPOCH_JUMP("Foster clock 4 — epoch jump",
                "O-C steps at cycle 4 (Table 13.2). Use Two-segment with "
                        + "break cycle 3 (cycles ≤ 3 pre-jump; cycle 4 is "
                        + "already post-jump)."),
        PERIOD_CHANGE("Foster clock 5 — period change",
                "O-C slope changes after cycle 5 (Table 13.2). Use "
                        + "Two-segment with break cycle 5 (cycles ≤ 5 first "
                        + "slope; cycle 6 starts the new regime)."),
        EVOLVING_PERIOD("Foster clock 6 — slowing clock",
                "Curved O-C (Table 13.2). Select Quadratic under Fit on plot "
                        + "and read the Fit summary interpretation.");

        private final String label;
        private final String expectedPattern;

        DemoScenario(String label, String expectedPattern) {
            this.label = label;
            this.expectedPattern = expectedPattern;
        }

        public String getLabel() {
            return label;
        }

        public String getExpectedPattern() {
            return expectedPattern;
        }
    }

    /**
     * Generated demo bundle: observations plus suggested ephemeris and hints.
     */
    public static final class DemoDataset {
        public final DemoScenario scenario;
        public final List<ValidObservation> observations;
        /** Ephemeris to enter in the O-C tool (VSA ch. 13 test theory). */
        public final double modelPeriod;
        public final double modelEpoch;
        public final double truePeriod;
        public final double trueEpoch;
        public final String starName;
        public final String description;
        public final String expectedPattern;
        public final EventType suggestedEventType;
        /** Suggested two-segment break cycle, or null. */
        public final Integer suggestedBreakCycle;

        DemoDataset(DemoScenario scenario, List<ValidObservation> observations,
                double modelPeriod, double modelEpoch, double truePeriod,
                double trueEpoch, String starName, String description,
                EventType suggestedEventType, Integer suggestedBreakCycle) {
            this.scenario = scenario;
            this.observations = Collections
                    .unmodifiableList(new ArrayList<ValidObservation>(observations));
            this.modelPeriod = modelPeriod;
            this.modelEpoch = modelEpoch;
            this.truePeriod = truePeriod;
            this.trueEpoch = trueEpoch;
            this.starName = starName;
            this.description = description;
            this.expectedPattern = scenario.getExpectedPattern();
            this.suggestedEventType = suggestedEventType;
            this.suggestedBreakCycle = suggestedBreakCycle;
        }
    }

    private OCAnalysisDemoData() {
    }

    public static DemoDataset generate(DemoScenario scenario) {
        return generate(scenario, DEFAULT_EPOCH, FOSTER_THEORY_PERIOD,
                DEFAULT_NUM_CYCLES);
    }

    public static DemoDataset generate(DemoScenario scenario, double baseEpoch,
            double modelPeriod, int numCycles) {
        double[] fosterDays;
        Integer breakCycle;
        switch (scenario) {
        case CORRECT_EPHEMERIS:
            fosterDays = FOSTER_CLOCK_1;
            breakCycle = null;
            break;
        case EPOCH_OFFSET:
            fosterDays = FOSTER_CLOCK_2;
            breakCycle = null;
            break;
        case PERIOD_ERROR:
            fosterDays = FOSTER_CLOCK_3;
            breakCycle = null;
            break;
        case EPOCH_JUMP:
            fosterDays = FOSTER_CLOCK_4;
            // Last cycle of the pre-jump plateau (cycle 4 is already post-jump).
            breakCycle = Integer.valueOf(3);
            break;
        case PERIOD_CHANGE:
            fosterDays = FOSTER_CLOCK_5;
            // Last cycle of the first slope (cycle 6 is already in the new regime).
            breakCycle = Integer.valueOf(5);
            break;
        case EVOLVING_PERIOD:
            fosterDays = FOSTER_CLOCK_6;
            breakCycle = null;
            break;
        default:
            throw new IllegalArgumentException("Unknown scenario: " + scenario);
        }
        return buildFosterClockDemo(scenario, fosterDays, baseEpoch, modelPeriod,
                numCycles, breakCycle);
    }

    public static DemoScenario scenarioFromLabel(String label) {
        for (DemoScenario scenario : DemoScenario.values()) {
            if (scenario.getLabel().equals(label)) {
                return scenario;
            }
        }
        return null;
    }

    /**
     * VSA Table 13.2 O-C for a clock, using theory P = 1 d and epoch at
     * cycle 0.
     */
    public static double[] fosterOcValues(double[] fosterObservedDays) {
        double[] oc = new double[fosterObservedDays.length];
        for (int n = 0; n < fosterObservedDays.length; n++) {
            oc[n] = fosterObservedDays[n] - n * FOSTER_THEORY_PERIOD;
        }
        return oc;
    }

    private static DemoDataset buildFosterClockDemo(DemoScenario scenario,
            double[] fosterObservedDays, double baseEpoch, double modelPeriod,
            int numCycles, Integer suggestedBreakCycle) {
        int count = Math.min(numCycles, fosterObservedDays.length);
        List<ValidObservation> obs = new ArrayList<ValidObservation>();
        for (int n = 0; n < count; n++) {
            addParabolicMaximum(obs, baseEpoch + fosterObservedDays[n]);
        }
        String starName = "FOSTER_CLOCK_" + (scenario.ordinal() + 1);
        prepareObservations(obs, starName);
        return new DemoDataset(scenario, obs, modelPeriod, baseEpoch,
                FOSTER_THEORY_PERIOD, baseEpoch, starName, scenario.getLabel(),
                EventType.MAXIMUM, suggestedBreakCycle);
    }

    private static void addParabolicMaximum(List<ValidObservation> obs,
            double tMax) {
        for (double dt = -0.2; dt <= 0.2; dt += 0.05) {
            double jd = tMax + dt;
            double mag = 10.0 + 20.0 * dt * dt;
            obs.add(observation(jd, mag));
        }
    }

    private static ValidObservation observation(double jd, double mag) {
        ValidObservation ob = new ValidObservation();
        ob.setJD(jd);
        ob.setMagnitude(new Magnitude(mag, 0.01));
        return ob;
    }

    private static void prepareObservations(List<ValidObservation> obs,
            String starName) {
        SeriesType band = SeriesType.Johnson_V;
        int record = 0;
        for (ValidObservation ob : obs) {
            ob.setName(starName);
            ob.setBand(band);
            ob.setRecordNumber(++record);
        }
    }
}
