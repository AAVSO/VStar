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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.exception.AlgorithmError;
import org.aavso.tools.vstar.ui.model.plot.ContinuousModelFunction;
import org.aavso.tools.vstar.ui.model.plot.ICoordSource;
import org.aavso.tools.vstar.ui.model.plot.JDCoordSource;
import org.aavso.tools.vstar.util.ApacheCommonsBrentOptimiserExtremaFinder;
import org.aavso.tools.vstar.util.model.IModel;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.optimization.GoalType;

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
        MINIMUM("Minimum light (maximum magnitude)"),
        BOTH("Both maxima and minima (eclipsing binaries)");

        private final String label;

        EventType(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }

        public boolean isBoth() {
            return this == BOTH;
        }
    }

    /**
     * Method used to estimate the observed time On within a cycle.
     */
    public enum TimingMethod {
        PARABOLIC("Parabolic interpolation"),
        MEAN_OF_EXTREME("Mean JD of extreme N% of observations per cycle"),
        FROM_MODEL("From current model function");

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
        /** Required when {@link TimingMethod#FROM_MODEL} is selected. */
        public final IModel model;

        public Parameters(double period, double epoch, EventType eventType,
                TimingMethod timingMethod, int meanExtremePercent,
                int minObsPerCycle) {
            this(period, epoch, eventType, timingMethod, meanExtremePercent,
                    minObsPerCycle, null);
        }

        public Parameters(double period, double epoch, EventType eventType,
                TimingMethod timingMethod, int meanExtremePercent,
                int minObsPerCycle, IModel model) {
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
            if (timingMethod == TimingMethod.FROM_MODEL
                    && (model == null || !model.hasFuncDesc()
                            || model.getModelFunction() == null)) {
                throw new IllegalArgumentException(
                        "A function-based model is required for model timing");
            }
            this.period = period;
            this.epoch = epoch;
            this.eventType = eventType;
            this.timingMethod = timingMethod;
            this.meanExtremePercent = meanExtremePercent;
            this.minObsPerCycle = minObsPerCycle;
            this.model = model;
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
        /** O-C uncertainty in days, or {@link Double#NaN} if unknown. */
        public final double ocUncertainty;
        public final int obsInCycle;
        /** Extremum timed for this point (max, min, or inferred for imports). */
        public final EventType extremumType;

        Point(int cycle, double observedTime, double computedTime,
                double ocUncertainty, int obsInCycle, EventType extremumType) {
            this.cycle = cycle;
            this.observedTime = observedTime;
            this.computedTime = computedTime;
            this.oc = observedTime - computedTime;
            this.ocUncertainty = ocUncertainty;
            this.obsInCycle = obsInCycle;
            this.extremumType = extremumType;
        }
    }

    /**
     * An imported observed time of extremum (from file).
     */
    public static class ImportedTiming {
        public final Integer cycle;
        public final double observedTime;
        public final double uncertaintyDays;

        public ImportedTiming(Integer cycle, double observedTime,
                double uncertaintyDays) {
            this.cycle = cycle;
            this.observedTime = observedTime;
            this.uncertaintyDays = uncertaintyDays;
        }
    }

    /**
     * Least-squares linear fit y = intercept + slope * x.
     */
    public static class LinearFit {
        public final double intercept;
        public final double slope;
        public final double rms;
        public final int pointCount;

        LinearFit(double intercept, double slope, double rms, int pointCount) {
            this.intercept = intercept;
            this.slope = slope;
            this.rms = rms;
            this.pointCount = pointCount;
        }

        public double evaluate(double x) {
            return intercept + slope * x;
        }
    }

    /**
     * Two-segment linear O-C fit split at a cycle boundary.
     */
    public static class TwoSegmentFit {
        public final int breakCycle;
        public final LinearFit firstSegment;
        public final LinearFit secondSegment;

        TwoSegmentFit(int breakCycle, LinearFit firstSegment,
                LinearFit secondSegment) {
            this.breakCycle = breakCycle;
            this.firstSegment = firstSegment;
            this.secondSegment = secondSegment;
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
        if (params.eventType.isBoth()) {
            List<Point> combined = new ArrayList<Point>();
            combined.addAll(analyzeObservations(observations,
                    withEventType(params, EventType.MAXIMUM)).points);
            combined.addAll(analyzeObservations(observations,
                    withEventType(params, EventType.MINIMUM)).points);
            Collections.sort(combined, new Comparator<Point>() {
                @Override
                public int compare(Point a, Point b) {
                    int c = Integer.compare(a.cycle, b.cycle);
                    if (c != 0) {
                        return c;
                    }
                    return a.extremumType.compareTo(b.extremumType);
                }
            });
            return new Result(params, combined);
        }
        return analyzeObservations(observations, params);
    }

    /**
     * Build O-C points from imported observed extremum times.
     */
    public static Result analyzeImported(List<ImportedTiming> timings,
            Parameters params) {
        if (timings == null || timings.isEmpty()) {
            return new Result(params, Collections.<Point>emptyList());
        }
        EventType pointType = params.eventType.isBoth() ? EventType.MAXIMUM
                : params.eventType;
        List<Point> points = new ArrayList<Point>();
        for (ImportedTiming timing : timings) {
            int cycle = timing.cycle != null ? timing.cycle : cycleNumber(
                    timing.observedTime, params.epoch, params.period);
            double computed = params.computedTime(cycle);
            points.add(new Point(cycle, timing.observedTime, computed,
                    timing.uncertaintyDays, 0, pointType));
        }
        Collections.sort(points, new Comparator<Point>() {
            @Override
            public int compare(Point a, Point b) {
                return Integer.compare(a.cycle, b.cycle);
            }
        });
        return new Result(params, points);
    }

    /**
     * Optional ephemeris and event metadata from an O-C export CSV
     * ({@code # period=…, epoch=…} comments and {@code Event} column).
     */
    public static final class ImportFileMetadata {
        public final Double period;
        public final Double epoch;
        public final EventType eventType;

        public ImportFileMetadata(Double period, Double epoch,
                EventType eventType) {
            this.period = period;
            this.epoch = epoch;
            this.eventType = eventType;
        }
    }

    /**
     * Read period, epoch, and event from O-C export comment/header lines, if
     * present. Plain timing files return null fields.
     */
    public static ImportFileMetadata parseImportFileMetadata(
            List<String> lines) {
        Double period = null;
        Double epoch = null;
        EventType eventType = null;
        if (lines == null) {
            return new ImportFileMetadata(null, null, null);
        }
        for (String raw : lines) {
            String line = raw.trim();
            if (line.isEmpty()) {
                continue;
            }
            if (line.startsWith("#")) {
                int periodIdx = line.toLowerCase().indexOf("period=");
                int epochIdx = line.toLowerCase().indexOf("epoch=");
                if (periodIdx >= 0 && epochIdx > periodIdx) {
                    try {
                        String periodStr = line.substring(periodIdx + 7,
                                line.indexOf(',', periodIdx)).trim();
                        String epochStr = line.substring(epochIdx + 6).trim();
                        period = Double.parseDouble(periodStr);
                        epoch = Double.parseDouble(epochStr);
                    } catch (NumberFormatException | IndexOutOfBoundsException ex) {
                        // ignore malformed export comment
                    }
                }
                continue;
            }
            String[] parts = line.split("[\\s,;]+");
            if (parts.length < 1 || parts[0].equalsIgnoreCase("Event")) {
                continue;
            }
            if (eventType == null) {
                eventType = eventTypeFromExportName(parts[0]);
            }
        }
        return new ImportFileMetadata(period, epoch, eventType);
    }

    private static EventType eventTypeFromExportName(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        try {
            return EventType.valueOf(name.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    /**
     * Parse imported timing lines. Each non-comment line is {@code cycle time
     * [sigma]} or {@code time [sigma]} (cycle inferred from ephemeris). Times
     * must use the same system as the ephemeris epoch (JD, HJD, BJD, etc.).
     */
    public static List<ImportedTiming> parseImportedTimings(List<String> lines,
            double epoch, double period) throws IOException {
        List<ImportedTiming> timings = new ArrayList<ImportedTiming>();
        int lineNum = 0;
        for (String raw : lines) {
            lineNum++;
            String line = raw.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            String[] parts = line.split("[\\s,;]+");
            if (parts.length < 1) {
                continue;
            }
            if (parts[0].equalsIgnoreCase("Event")) {
                continue;
            }
            try {
                if (parts.length >= 3 && looksLikeInteger(parts[1])
                        && !looksLikeInteger(parts[0])) {
                    int cycle = Integer.parseInt(parts[1]);
                    double time = Double.parseDouble(parts[2]);
                    double sigma = Double.NaN;
                    if (parts.length > 6 && !parts[5].isEmpty()) {
                        sigma = Double.parseDouble(parts[5]);
                    }
                    timings.add(new ImportedTiming(cycle, time, sigma));
                } else if (parts.length >= 2 && looksLikeInteger(parts[0])) {
                    int cycle = Integer.parseInt(parts[0]);
                    double time = Double.parseDouble(parts[1]);
                    double sigma = parts.length >= 3
                            ? Double.parseDouble(parts[2]) : Double.NaN;
                    timings.add(new ImportedTiming(cycle, time, sigma));
                } else {
                    double time = Double.parseDouble(parts[0]);
                    double sigma = parts.length >= 2
                            ? Double.parseDouble(parts[1]) : Double.NaN;
                    int cycle = cycleNumber(time, epoch, period);
                    timings.add(new ImportedTiming(cycle, time, sigma));
                }
            } catch (NumberFormatException e) {
                throw new IOException("Invalid imported timing at line "
                        + lineNum + ": " + raw);
            }
        }
        return timings;
    }

    /**
     * Quadratic least-squares fit of O-C versus cycle number.
     */
    public static QuadraticFit fitQuadratic(List<Point> points) {
        if (points == null || points.size() < 3) {
            return null;
        }
        int n = points.size();
        double s0 = n;
        double s1 = 0;
        double s2 = 0;
        double s3 = 0;
        double s4 = 0;
        double t0 = 0;
        double t1 = 0;
        double t2 = 0;
        for (Point p : points) {
            double x = p.cycle;
            double y = p.oc;
            double x2 = x * x;
            s1 += x;
            s2 += x2;
            s3 += x2 * x;
            s4 += x2 * x2;
            t0 += y;
            t1 += x * y;
            t2 += x2 * y;
        }
        double[][] m = { { s0, s1, s2 }, { s1, s2, s3 }, { s2, s3, s4 } };
        double[] v = { t0, t1, t2 };
        double[] coeff = solve3x3(m, v);
        if (coeff == null) {
            return null;
        }
        double sumSq = 0;
        for (Point p : points) {
            double residual = p.oc - (coeff[0] + coeff[1] * p.cycle + coeff[2]
                    * p.cycle * p.cycle);
            sumSq += residual * residual;
        }
        double rms = Math.sqrt(sumSq / n);
        return new QuadraticFit(coeff[0], coeff[1], coeff[2], rms, n);
    }

    /**
     * Foster ch. 13 interpretation for a quadratic O-C trend (evolving period).
     */
    public static String interpretQuadraticFit(QuadraticFit fit,
            double modelPeriod) {
        if (fit == null) {
            return "";
        }
        double deltaPPerCycle = 2.0 * fit.quadratic;
        StringBuilder buf = new StringBuilder();
        buf.append("Quadratic fit (O-C vs cycle): epoch correction ≈ ");
        buf.append(formatSmallDays(fit.constant));
        buf.append(" d; linear coeff = ");
        buf.append(formatSmallDays(fit.linear));
        buf.append(" d/cycle → starting period correction ≈ ");
        buf.append(formatSmallDays(fit.linear - fit.quadratic));
        buf.append(" d; quadratic coeff = ");
        buf.append(formatSmallDays(fit.quadratic));
        buf.append(" d/cycle² → ΔP/cycle ≈ ");
        buf.append(formatSmallDays(deltaPPerCycle));
        buf.append(" d (evolving period, Foster §13.4); RMS = ");
        buf.append(formatSmallDays(fit.rms));
        buf.append(" d.");
        return buf.toString();
    }

    /**
     * Write O-C results as comma-separated text.
     */
    public static void writeCsv(Result result, PrintWriter writer,
            LinearFit linearFit, TwoSegmentFit twoSegmentFit,
            QuadraticFit quadraticFit) {
        writer.println("# O-C export");
        writer.println("# period=" + result.parameters.period + ", epoch="
                + result.parameters.epoch);
        if (linearFit != null) {
            writer.println("# linear_slope=" + linearFit.slope
                    + ", linear_intercept=" + linearFit.intercept);
        }
        if (quadraticFit != null) {
            writer.println("# quadratic_constant=" + quadraticFit.constant
                    + ", quadratic_linear=" + quadraticFit.linear
                    + ", quadratic_quadratic=" + quadraticFit.quadratic);
        }
        writer.println(
                "Event,Cycle,O_time,C_time,OC_days,OC_sigma,ObsInCycle");
        for (Point p : result.points) {
            writer.print(p.extremumType.name());
            writer.print(',');
            writer.print(p.cycle);
            writer.print(',');
            writer.print(p.observedTime);
            writer.print(',');
            writer.print(p.computedTime);
            writer.print(',');
            writer.print(p.oc);
            writer.print(',');
            if (!Double.isNaN(p.ocUncertainty) && p.ocUncertainty > 0) {
                writer.print(p.ocUncertainty);
            }
            writer.print(',');
            writer.println(p.obsInCycle);
        }
    }

    private static Result analyzeObservations(List<ValidObservation> observations,
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

            TimingEstimate estimate = estimateObservedTime(cycleObs, params);
            if (estimate == null || estimate.time == null
                    || Double.isNaN(estimate.time)
                    || Double.isInfinite(estimate.time)) {
                continue;
            }

            double computed = params.computedTime(cycle);
            points.add(new Point(cycle, estimate.time, computed,
                    estimate.uncertaintyDays, cycleObs.size(),
                    params.eventType));
        }

        return new Result(params, points);
    }

    private static Parameters withEventType(Parameters params,
            EventType eventType) {
        return new Parameters(params.period, params.epoch, eventType,
                params.timingMethod, params.meanExtremePercent,
                params.minObsPerCycle, params.model);
    }

    private static boolean looksLikeInteger(String token) {
        for (int i = 0; i < token.length(); i++) {
            char ch = token.charAt(i);
            if (i == 0 && ch == '-') {
                continue;
            }
            if (!Character.isDigit(ch)) {
                return false;
            }
        }
        return token.length() > 0;
    }

    private static double[] solve3x3(double[][] m, double[] v) {
        double[][] a = new double[3][4];
        for (int i = 0; i < 3; i++) {
            a[i][0] = m[i][0];
            a[i][1] = m[i][1];
            a[i][2] = m[i][2];
            a[i][3] = v[i];
        }
        for (int col = 0; col < 3; col++) {
            int pivot = col;
            for (int row = col + 1; row < 3; row++) {
                if (Math.abs(a[row][col]) > Math.abs(a[pivot][col])) {
                    pivot = row;
                }
            }
            if (Math.abs(a[pivot][col]) < 1e-18) {
                return null;
            }
            if (pivot != col) {
                double[] tmp = a[col];
                a[col] = a[pivot];
                a[pivot] = tmp;
            }
            for (int row = col + 1; row < 3; row++) {
                double factor = a[row][col] / a[col][col];
                for (int j = col; j < 4; j++) {
                    a[row][j] -= factor * a[col][j];
                }
            }
        }
        double[] x = new double[3];
        for (int row = 2; row >= 0; row--) {
            double sum = a[row][3];
            for (int j = row + 1; j < 3; j++) {
                sum -= a[row][j] * x[j];
            }
            x[row] = sum / a[row][row];
        }
        return x;
    }

    /**
     * Linear least-squares fit of O-C versus cycle number.
     */
    public static LinearFit fitLinear(List<Point> points) {
        if (points == null || points.size() < 2) {
            return null;
        }
        return fitLinearOnArrays(toCycleArray(points), toOcArray(points));
    }

    /**
     * Two-segment linear fit split at {@code breakCycle}; points with cycle
     * {@code <= breakCycle} form the first segment, the remainder the second.
     */
    public static TwoSegmentFit fitTwoSegment(List<Point> points,
            int breakCycle) {
        if (points == null || points.size() < 4) {
            return null;
        }
        List<Point> first = new ArrayList<Point>();
        List<Point> second = new ArrayList<Point>();
        for (Point p : points) {
            if (p.cycle <= breakCycle) {
                first.add(p);
            } else {
                second.add(p);
            }
        }
        if (first.size() < 2 || second.size() < 2) {
            return null;
        }
        LinearFit fit1 = fitLinear(first);
        LinearFit fit2 = fitLinear(second);
        if (fit1 == null || fit2 == null) {
            return null;
        }
        return new TwoSegmentFit(breakCycle, fit1, fit2);
    }

    /**
     * Quadratic least-squares fit O-C = constant + linear*n + quadratic*n².
     */
    public static class QuadraticFit {
        public final double constant;
        public final double linear;
        public final double quadratic;
        public final double rms;
        public final int pointCount;

        QuadraticFit(double constant, double linear, double quadratic,
                double rms, int pointCount) {
            this.constant = constant;
            this.linear = linear;
            this.quadratic = quadratic;
            this.rms = rms;
            this.pointCount = pointCount;
        }

        public double evaluate(int cycle) {
            double n = cycle;
            return constant + linear * n + quadratic * n * n;
        }
    }

    /**
     * Foster ch. 13 warning about period scatter in LPVs (non-white O-C noise).
     */
    public static String getPeriodScatterWarning() {
        return "Caution (Foster §13.5): for long-period variables and other stars "
                + "with cycle-to-cycle period scatter, O-C residuals are not white "
                + "noise — apparent trends may reflect intrinsic period jitter "
                + "rather than a true ephemeris change. See Foster (1993, JAAVSO 22, "
                + "145) on O-C autocorrelation.";
    }

    /**
     * Foster ch. 13 interpretation for a linear O-C trend versus cycle number.
     */
    public static String interpretLinearFit(LinearFit fit, double modelPeriod) {
        if (fit == null) {
            return "";
        }
        StringBuilder buf = new StringBuilder();
        buf.append("Linear fit (O-C vs cycle): slope = ");
        buf.append(formatSmallDays(fit.slope));
        buf.append(" d/cycle");
        if (Math.abs(fit.slope) > 0) {
            buf.append(" → corrected period ≈ ");
            buf.append(formatSmallDays(modelPeriod + fit.slope));
            buf.append(" d (ΔP ≈ ");
            buf.append(formatSmallDays(fit.slope));
            buf.append(" d)");
        } else {
            buf.append(" → period matches the ephemeris");
        }
        buf.append("; intercept = ");
        buf.append(formatSmallDays(fit.intercept));
        buf.append(" d → epoch correction ≈ ");
        buf.append(formatSmallDays(fit.intercept));
        buf.append(" d; RMS = ");
        buf.append(formatSmallDays(fit.rms));
        buf.append(" d.");
        return buf.toString();
    }

    /**
     * Interpretation for a two-segment fit (period change at the break).
     */
    public static String interpretTwoSegmentFit(TwoSegmentFit fit,
            double modelPeriod) {
        if (fit == null) {
            return "";
        }
        StringBuilder buf = new StringBuilder();
        buf.append("Two-segment fit with break at cycle ");
        buf.append(fit.breakCycle);
        buf.append(". First segment: ");
        buf.append(interpretLinearFit(fit.firstSegment, modelPeriod));
        buf.append(" Second segment: ");
        buf.append(interpretLinearFit(fit.secondSegment, modelPeriod));
        if (Math.abs(fit.firstSegment.slope - fit.secondSegment.slope) > 0) {
            buf.append(" Different slopes suggest a period change near cycle ");
            buf.append(fit.breakCycle);
            buf.append(" (Foster, ch. 13).");
        } else {
            buf.append(" Parallel segments suggest an epoch jump with unchanged "
                    + "period (Foster, ch. 13).");
        }
        return buf.toString();
    }

    private static String formatSmallDays(double days) {
        if (Math.abs(days) >= 0.0001) {
            return String.format("%.7f", days);
        }
        return String.format("%.3e", days);
    }

    private static double[] toCycleArray(List<Point> points) {
        double[] x = new double[points.size()];
        for (int i = 0; i < points.size(); i++) {
            x[i] = points.get(i).cycle;
        }
        return x;
    }

    private static double[] toOcArray(List<Point> points) {
        double[] y = new double[points.size()];
        for (int i = 0; i < points.size(); i++) {
            y[i] = points.get(i).oc;
        }
        return y;
    }

    private static LinearFit fitLinearOnArrays(double[] x, double[] y) {
        int n = x.length;
        double sumX = 0;
        double sumY = 0;
        double sumXY = 0;
        double sumXX = 0;
        for (int i = 0; i < n; i++) {
            sumX += x[i];
            sumY += y[i];
            sumXY += x[i] * y[i];
            sumXX += x[i] * x[i];
        }
        double denom = n * sumXX - sumX * sumX;
        if (Math.abs(denom) < 1e-18) {
            return null;
        }
        double slope = (n * sumXY - sumX * sumY) / denom;
        double intercept = (sumY - slope * sumX) / n;

        double sumSq = 0;
        for (int i = 0; i < n; i++) {
            double residual = y[i] - (intercept + slope * x[i]);
            sumSq += residual * residual;
        }
        double rms = Math.sqrt(sumSq / n);
        return new LinearFit(intercept, slope, rms, n);
    }

    private static class TimingEstimate {
        final Double time;
        final double uncertaintyDays;

        TimingEstimate(Double time, double uncertaintyDays) {
            this.time = time;
            this.uncertaintyDays = uncertaintyDays;
        }
    }

    /**
     * Cycle index for a Julian Date given an ephemeris.
     */
    public static int cycleNumber(double jd, double epoch, double period) {
        return (int) Math.round((jd - epoch) / period);
    }

    private static TimingEstimate estimateObservedTime(
            List<ValidObservation> cycleObs, Parameters params) {
        switch (params.timingMethod) {
        case PARABOLIC:
            Double parabolic = parabolicTime(cycleObs, params.eventType);
            return new TimingEstimate(parabolic,
                    estimateParabolicUncertainty(cycleObs, params.eventType,
                            parabolic));
        case MEAN_OF_EXTREME:
            Double meanTime = meanExtremeTime(cycleObs, params.eventType,
                    params.meanExtremePercent);
            return new TimingEstimate(meanTime,
                    estimateMeanExtremeUncertainty(cycleObs, params.eventType,
                            params.meanExtremePercent));
        case FROM_MODEL:
            Double modelTime = modelExtremumTime(cycleObs, params.model,
                    params.eventType);
            return new TimingEstimate(modelTime, Double.NaN);
        default:
            return null;
        }
    }

    private static Double modelExtremumTime(List<ValidObservation> cycleObs,
            IModel model, EventType eventType) {
        ContinuousModelFunction cmf = model.getModelFunction();
        if (cmf == null) {
            return null;
        }
        ICoordSource coordSrc = cmf.getCoordSrc();
        if (coordSrc != JDCoordSource.instance) {
            return null;
        }
        try {
            OCExtremaFinder finder = new OCExtremaFinder(cycleObs,
                    cmf.getFunction(), coordSrc, cmf.getZeroPoint());
            return finder.findEventTime(eventType);
        } catch (AlgorithmError e) {
            return null;
        }
    }

    private static double estimateParabolicUncertainty(
            List<ValidObservation> cycleObs, EventType eventType,
            Double time) {
        if (time == null || cycleObs.isEmpty()) {
            return Double.NaN;
        }
        int idx = extremumIndex(cycleObs, eventType);
        int lo = Math.max(0, idx - 1);
        int hi = Math.min(cycleObs.size() - 1, idx + 1);
        double t0 = cycleObs.get(lo).getJD();
        double t2 = cycleObs.get(hi).getJD();
        double m0 = mag(cycleObs.get(lo));
        double m2 = mag(cycleObs.get(hi));
        double dm = Math.abs(m2 - m0);
        if (dm < 1e-9 || Math.abs(t2 - t0) < 1e-9) {
            return meanUncertaintyDays(cycleObs);
        }
        double meanSigmaMag = meanMagUncertainty(cycleObs, lo, hi);
        if (Double.isNaN(meanSigmaMag)) {
            return Double.NaN;
        }
        return meanSigmaMag * Math.abs(t2 - t0) / dm;
    }

    private static double estimateMeanExtremeUncertainty(
            List<ValidObservation> cycleObs, EventType eventType,
            int meanExtremePercent) {
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
        double sumVar = 0;
        int n = 0;
        for (int i = 0; i < count; i++) {
            double sigma = magUncertainty(sorted.get(i));
            if (!Double.isNaN(sigma) && sigma > 0) {
                sumVar += sigma * sigma;
                n++;
            }
        }
        if (n == 0) {
            return Double.NaN;
        }
        return Math.sqrt(sumVar) / n;
    }

    private static double meanUncertaintyDays(List<ValidObservation> cycleObs) {
        double sumVar = 0;
        int n = 0;
        for (ValidObservation ob : cycleObs) {
            double sigma = magUncertainty(ob);
            if (!Double.isNaN(sigma) && sigma > 0) {
                sumVar += sigma * sigma;
                n++;
            }
        }
        if (n == 0) {
            return Double.NaN;
        }
        return Math.sqrt(sumVar) / n;
    }

    private static double meanMagUncertainty(List<ValidObservation> cycleObs,
            int lo, int hi) {
        double sum = 0;
        int n = 0;
        for (int i = lo; i <= hi; i++) {
            double sigma = magUncertainty(cycleObs.get(i));
            if (!Double.isNaN(sigma) && sigma > 0) {
                sum += sigma;
                n++;
            }
        }
        if (n == 0) {
            return Double.NaN;
        }
        return sum / n;
    }

    private static double magUncertainty(ValidObservation ob) {
        if (ob.getMagnitude() == null) {
            return Double.NaN;
        }
        double sigma = ob.getMagnitude().getUncertainty();
        return sigma > 0 ? sigma : Double.NaN;
    }

    /**
     * Finds an extremum time on a model function bracketed by cycle
     * observations.
     */
    static class OCExtremaFinder extends ApacheCommonsBrentOptimiserExtremaFinder {

        OCExtremaFinder(List<ValidObservation> obs,
                UnivariateRealFunction function, ICoordSource timeCoordSource,
                double zeroPoint) {
            super(obs, function, timeCoordSource, zeroPoint);
        }

        Double findEventTime(EventType eventType) throws AlgorithmError {
            GoalType goal = eventType == EventType.MAXIMUM ? GoalType.MINIMIZE
                    : GoalType.MAXIMIZE;
            int idx = extremumIndex(obs, eventType);
            int lo = Math.max(0, idx - 1);
            int hi = Math.min(obs.size() - 1, idx + 1);
            find(goal, new int[] { lo, hi });
            return getExtremeTime();
        }

        private static int extremumIndex(List<ValidObservation> cycleObs,
                EventType eventType) {
            int best = 0;
            double bestMag = cycleObs.get(0).getMag();
            for (int i = 1; i < cycleObs.size(); i++) {
                double m = cycleObs.get(i).getMag();
                if (eventType == EventType.MAXIMUM ? m < bestMag : m > bestMag) {
                    bestMag = m;
                    best = i;
                }
            }
            return best;
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
