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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aavso.tools.vstar.data.ValidObservation;

/**
 * O-C (observed minus computed) analysis for times of light-curve extrema.
 *
 * <p>
 * See Grant Foster, "Analyzing Light Curves", chapter 13. For cycle n with
 * ephemeris (epoch t0, period P), the computed time of maximum is Cn = t0 + nP
 * and (O-C)n = On - Cn.
 * </p>
 */
public class OCAnalysisLib {

    /**
     * Which extremum to time in each cycle.
     */
    public enum EventType {
        MAXIMUM("Maximum light (minimum magnitude)"),
        MINIMUM("Minimum light (maximum magnitude)");

        private final String label;

        EventType(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    /**
     * Method used to estimate the observed time On within a cycle.
     */
    public enum TimingMethod {
        PARABOLIC("Parabolic interpolation"),
        MEAN_OF_EXTREME("Mean JD of extreme N% of observations per cycle");

        private final String label;

        TimingMethod(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    /**
     * Analysis parameters.
     */
    public static class Parameters {
        public final double period;
        public final double epoch;
        public final EventType eventType;
        public final TimingMethod timingMethod;
        /** Percent of observations per cycle used by {@link TimingMethod#MEAN_OF_EXTREME}. */
        public final int meanExtremePercent;
        /** Cycles with fewer observations are skipped. */
        public final int minObsPerCycle;

        public Parameters(double period, double epoch, EventType eventType,
                TimingMethod timingMethod, int meanExtremePercent,
                int minObsPerCycle) {
            if (period <= 0) {
                throw new IllegalArgumentException("Period must be positive");
            }
            if (meanExtremePercent < 1 || meanExtremePercent > 100) {
                throw new IllegalArgumentException(
                        "Mean extreme percent must be between 1 and 100");
            }
            if (minObsPerCycle < 1) {
                throw new IllegalArgumentException(
                        "Minimum observations per cycle must be at least 1");
            }
            this.period = period;
            this.epoch = epoch;
            this.eventType = eventType;
            this.timingMethod = timingMethod;
            this.meanExtremePercent = meanExtremePercent;
            this.minObsPerCycle = minObsPerCycle;
        }

        public double computedTime(int cycle) {
            return epoch + cycle * period;
        }
    }

    /**
     * One O-C data point.
     */
    public static class Point {
        public final int cycle;
        public final double observedTime;
        public final double computedTime;
        public final double oc;
        public final int obsInCycle;

        Point(int cycle, double observedTime, double computedTime, int obsInCycle) {
            this.cycle = cycle;
            this.observedTime = observedTime;
            this.computedTime = computedTime;
            this.oc = observedTime - computedTime;
            this.obsInCycle = obsInCycle;
        }
    }

    /**
     * Analysis output.
     */
    public static class Result {
        public final Parameters parameters;
        public final List<Point> points;

        Result(Parameters parameters, List<Point> points) {
            this.parameters = parameters;
            this.points = Collections.unmodifiableList(points);
        }
    }

    /**
     * Perform O-C analysis on the given observations.
     *
     * @param observations
     *            Observations for one series (discrepant points are ignored).
     * @param params
     *            Ephemeris and analysis options.
     * @return O-C points sorted by cycle number, possibly empty.
     */
    public static Result analyze(List<ValidObservation> observations,
            Parameters params) {
        List<ValidObservation> usable = new ArrayList<ValidObservation>();
        for (ValidObservation ob : observations) {
            if (!ob.isDiscrepant()) {
                usable.add(ob);
            }
        }

        if (usable.isEmpty()) {
            return new Result(params, Collections.<Point>emptyList());
        }

        Collections.sort(usable, new Comparator<ValidObservation>() {
            @Override
            public int compare(ValidObservation a, ValidObservation b) {
                return Double.compare(a.getJD(), b.getJD());
            }
        });

        Map<Integer, List<ValidObservation>> byCycle = new HashMap<Integer, List<ValidObservation>>();
        for (ValidObservation ob : usable) {
            int cycle = cycleNumber(ob.getJD(), params.epoch, params.period);
            List<ValidObservation> bucket = byCycle.get(cycle);
            if (bucket == null) {
                bucket = new ArrayList<ValidObservation>();
                byCycle.put(cycle, bucket);
            }
            bucket.add(ob);
        }

        List<Integer> cycles = new ArrayList<Integer>(byCycle.keySet());
        Collections.sort(cycles);

        List<Point> points = new ArrayList<Point>();
        for (int cycle : cycles) {
            List<ValidObservation> cycleObs = byCycle.get(cycle);
            if (cycleObs.size() < params.minObsPerCycle) {
                continue;
            }
            Collections.sort(cycleObs, new Comparator<ValidObservation>() {
                @Override
                public int compare(ValidObservation a, ValidObservation b) {
                    return Double.compare(a.getJD(), b.getJD());
                }
            });

            Double observed = estimateObservedTime(cycleObs, params);
            if (observed == null || Double.isNaN(observed)
                    || Double.isInfinite(observed)) {
                continue;
            }

            double computed = params.computedTime(cycle);
            points.add(new Point(cycle, observed, computed, cycleObs.size()));
        }

        return new Result(params, points);
    }

    /**
     * Cycle index for a Julian Date given an ephemeris.
     */
    public static int cycleNumber(double jd, double epoch, double period) {
        return (int) Math.round((jd - epoch) / period);
    }

    private static Double estimateObservedTime(List<ValidObservation> cycleObs,
            Parameters params) {
        switch (params.timingMethod) {
        case PARABOLIC:
            return parabolicTime(cycleObs, params.eventType);
        case MEAN_OF_EXTREME:
            return meanExtremeTime(cycleObs, params.eventType,
                    params.meanExtremePercent);
        default:
            return null;
        }
    }

    private static double mag(ValidObservation ob) {
        return ob.getMag();
    }

    private static boolean isBetterExtremum(double candidate, double incumbent,
            EventType eventType) {
        return eventType == EventType.MAXIMUM ? candidate < incumbent
                : candidate > incumbent;
    }

    private static int extremumIndex(List<ValidObservation> cycleObs,
            EventType eventType) {
        int best = 0;
        double bestMag = mag(cycleObs.get(0));
        for (int i = 1; i < cycleObs.size(); i++) {
            double m = mag(cycleObs.get(i));
            if (isBetterExtremum(m, bestMag, eventType)) {
                bestMag = m;
                best = i;
            }
        }
        return best;
    }

    /**
     * Parabolic refinement using up to three observations bracketing the
     * discrete extremum (by time order within the cycle).
     */
    static Double parabolicTime(List<ValidObservation> cycleObs,
            EventType eventType) {
        int imin = extremumIndex(cycleObs, eventType);
        ValidObservation centre = cycleObs.get(imin);

        if (cycleObs.size() == 1) {
            return centre.getJD();
        }

        ValidObservation left = imin > 0 ? cycleObs.get(imin - 1) : centre;
        ValidObservation right = imin < cycleObs.size() - 1
                ? cycleObs.get(imin + 1) : centre;

        if (left == centre && right == centre) {
            return centre.getJD();
        }

        if (left == centre || right == centre) {
            // Two distinct points: linear crossing at the discrete extremum time.
            return centre.getJD();
        }

        double t0 = left.getJD();
        double m0 = mag(left);
        double t1 = centre.getJD();
        double m1 = mag(centre);
        double t2 = right.getJD();
        double m2 = mag(right);

        return parabolicExtremumTime(t0, m0, t1, m1, t2, m2, eventType);
    }

    /**
     * Abscissa of the extremum of a parabola through three (time, magnitude)
     * points. Magnitude is treated as the ordinate; for maximum light the
     * parabolic minimum in magnitude is returned.
     */
    static double parabolicExtremumTime(double t0, double m0, double t1,
            double m1, double t2, double m2, EventType eventType) {
        double det = (t0 - t1) * (t0 - t2) * (t1 - t2);
        if (Math.abs(det) < 1e-18) {
            return t1;
        }

        double a = (t2 * (m1 - m0) + t1 * (m0 - m2) + t0 * (m2 - m1)) / det;
        double b = (t2 * t2 * (m0 - m1) + t1 * t1 * (m2 - m0) + t0 * t0
                * (m1 - m2)) / det;

        if (Math.abs(a) < 1e-18) {
            return t1;
        }

        double tExt = -b / (2.0 * a);

        double lo = Math.min(t0, Math.min(t1, t2));
        double hi = Math.max(t0, Math.max(t1, t2));
        if (tExt < lo || tExt > hi) {
            return t1;
        }

        // For minimum-light events the parabola opens upward in magnitude space.
        if (eventType == EventType.MINIMUM && a < 0) {
            return t1;
        }
        if (eventType == EventType.MAXIMUM && a > 0) {
            return t1;
        }

        return tExt;
    }

    static Double meanExtremeTime(List<ValidObservation> cycleObs,
            EventType eventType, int meanExtremePercent) {
        List<ValidObservation> sorted = new ArrayList<ValidObservation>(
                cycleObs);
        Collections.sort(sorted, new Comparator<ValidObservation>() {
            @Override
            public int compare(ValidObservation a, ValidObservation b) {
                if (eventType == EventType.MAXIMUM) {
                    return Double.compare(mag(a), mag(b));
                }
                return Double.compare(mag(b), mag(a));
            }
        });

        int count = Math.max(1,
                (int) Math.ceil(sorted.size() * meanExtremePercent / 100.0));
        double sum = 0;
        for (int i = 0; i < count; i++) {
            sum += sorted.get(i).getJD();
        }
        return sum / count;
    }
}
