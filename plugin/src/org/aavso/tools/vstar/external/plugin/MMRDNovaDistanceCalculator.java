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
package org.aavso.tools.vstar.external.plugin;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;

import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.exception.AlgorithmError;
import org.aavso.tools.vstar.external.lib.NovaExponentialModel;
import org.aavso.tools.vstar.plugin.ObservationToolPluginBase;
import org.aavso.tools.vstar.ui.dialog.DoubleField;
import org.aavso.tools.vstar.ui.dialog.ITextComponent;
import org.aavso.tools.vstar.ui.dialog.MessageBox;
import org.aavso.tools.vstar.ui.dialog.MultiEntryComponentDialog;
import org.aavso.tools.vstar.ui.dialog.SelectableTextField;
import org.aavso.tools.vstar.ui.dialog.TextArea;
import org.aavso.tools.vstar.ui.dialog.TextField;
import org.aavso.tools.vstar.ui.dialog.series.SingleSeriesSelectionDialog;
import org.aavso.tools.vstar.ui.mediator.AnalysisType;
import org.aavso.tools.vstar.ui.mediator.DocumentManager;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.model.plot.ISeriesInfoProvider;
import org.aavso.tools.vstar.ui.model.plot.ObservationAndMeanPlotModel;
import org.aavso.tools.vstar.util.Tolerance;
import org.aavso.tools.vstar.util.locale.LocaleProps;
import org.aavso.tools.vstar.util.prefs.NumericPrecisionPrefs;

/**
 * <p>
 * This plug-in calculates the distance to a nova using the MMRD (Maximum
 * Magnitude vs Rate of Decline) relationship, which connects a nova's peak
 * absolute magnitude with the time taken to decline by 2 (t2) or 3 (t3)
 * magnitudes from maximum. It does for novae what the Leavitt's Law distance
 * calculator plug-in does for Cepheids.
 * </p>
 * 
 * <p>
 * References:<br>
 * <ol>
 * <li>[1] Kantharia, N. G. 2017, "Novae: I. The maximum magnitude relation
 * with decline time (MMRD) and distance", arXiv:1703.04087</li>
 * <li>[2] Kok, Y. 2010, "Absolute Magnitudes and Distances of Recent Novae",
 * JAAVSO, 38, 193</li>
 * <li>[3] Schmidt, T. 1957, Z. Astrophys., 41, 182</li>
 * <li>[4] Cohen, J. G. 1985, ApJ, 292, 90</li>
 * <li>[5] della Valle, M., and Livio, M. 1995, ApJ, 452, 704</li>
 * <li>[6] Downes, R. A., and Duerbeck, H. W. 2000, AJ, 120, 2007</li>
 * </ol>
 * </p>
 * 
 * <p>
 * Note: the MMRD relations as typeset in [2] show negative log coefficients;
 * the positive-slope forms from the original calibration papers (cross-checked
 * against the summary in section 2.3 of [1] and reproducing Table 3 of [2])
 * are used here: brighter (more negative peak absolute magnitude) novae are
 * the faster decliners.
 * </p>
 *
 * <p>
 * After t2 and t3 have been obtained (by exponential fit or from the
 * observations), the input dialog can be used repeatedly to try different
 * MMRD relations without repeating the fit. The results dialog includes a
 * summary table of every relation applied to the same inputs.
 * </p>
 */
public class MMRDNovaDistanceCalculator extends ObservationToolPluginBase {

    private final static String EXPONENTIAL_FIT = "Exponential model fit (Kok 2010, eq. 10)";
    private final static String DIRECT_FROM_OBS = "Directly from observations";

    // Default sigma in magnitudes used in the error-weighted mean for
    // relations whose uncertainty cannot otherwise be quantified
    // (e.g. Schmidt 1957, for which no coefficient errors are quoted).
    private final static double DEFAULT_SIGMA = 0.3;

    /**
     * The MMRD calibrations supported by this plug-in. The default is the
     * two-step calibration of Kantharia (2017), equation (15).
     */
    public enum MMRDRelation {

        KANTHARIA_2017("Kantharia 2017 (linear, t2)",
                "Mv = 2.16 log10(t2) - 10.804"),

        UNWEIGHTED_MEAN("Unweighted mean of historical relations (Kok 2010)",
                "Mean of the historical MMRD relations; error is their inter-relation scatter"),

        WEIGHTED_MEAN("Inverse-variance weighted mean of historical relations",
                "Inverse-variance weighted mean; error is max(formal error, inter-relation scatter)"),

        COHEN_1985("Cohen 1985 (linear, t2)",
                "Mv = 2.41 log10(t2) - 10.70"),

        DOWNES_DUERBECK_2000_LINEAR_T2("Downes & Duerbeck 2000 (linear, t2)",
                "Mv = 2.55 log10(t2) - 11.32"),

        DELLA_VALLE_LIVIO_1995("della Valle & Livio 1995 (arctan, t2)",
                "Mv = -7.92 - 0.81 arctan((1.32 - log10(t2)) / 0.23)"),

        DOWNES_DUERBECK_2000_ARCTAN("Downes & Duerbeck 2000 (arctan, t2)",
                "Mv = -8.02 - 1.23 arctan((1.32 - log10(t2)) / 0.23)"),

        DOWNES_DUERBECK_2000_COND_T2("Downes & Duerbeck 2000 (conditional linear, t2)",
                "Mv = 1.53 log10(t2) - 10.79 if log10(t2) < 1.2, else 1.03 log10(t2) - 8.71"),

        SCHMIDT_1957("Schmidt 1957 (linear, t3)",
                "Mv = 2.5 log10(t3) - 11.75"),

        DOWNES_DUERBECK_2000_LINEAR_T3("Downes & Duerbeck 2000 (linear, t3)",
                "Mv = 2.54 log10(t3) - 11.79"),

        DOWNES_DUERBECK_2000_COND_T3("Downes & Duerbeck 2000 (conditional linear, t3)",
                "Mv = 1.58 log10(t3) - 11.26 if log10(t3) < 1.5, else 0.56 log10(t3) - 8.13");

        private final String displayName;
        private final String equation;

        private MMRDRelation(String displayName, String equation) {
            this.displayName = displayName;
            this.equation = equation;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getEquation() {
            return equation;
        }

        public static MMRDRelation fromDisplayName(String name) {
            for (MMRDRelation relation : values()) {
                if (relation.displayName.equals(name)) {
                    return relation;
                }
            }
            return null;
        }

        /**
         * Return the peak absolute magnitude according to this relation.
         * 
         * @param t2 The time in days for a decline of 2 magnitudes from
         *           maximum; may be null.
         * @param t3 The time in days for a decline of 3 magnitudes from
         *           maximum; may be null.
         * @return The peak absolute magnitude, or null if a required decline
         *         time is missing.
         */
        public Double absMag(Double t2, Double t3) {
            Double logT2 = t2 != null && t2 > 0 ? Math.log10(t2) : null;
            Double logT3 = t3 != null && t3 > 0 ? Math.log10(t3) : null;

            Double mag = null;

            switch (this) {
            case KANTHARIA_2017:
                if (logT2 != null) mag = 2.16 * logT2 - 10.804;
                break;
            case COHEN_1985:
                if (logT2 != null) mag = 2.41 * logT2 - 10.70;
                break;
            case DOWNES_DUERBECK_2000_LINEAR_T2:
                if (logT2 != null) mag = 2.55 * logT2 - 11.32;
                break;
            case DELLA_VALLE_LIVIO_1995:
                if (logT2 != null) mag = -7.92 - 0.81 * Math.atan((1.32 - logT2) / 0.23);
                break;
            case DOWNES_DUERBECK_2000_ARCTAN:
                if (logT2 != null) mag = -8.02 - 1.23 * Math.atan((1.32 - logT2) / 0.23);
                break;
            case DOWNES_DUERBECK_2000_COND_T2:
                if (logT2 != null) {
                    mag = logT2 < 1.2 ? 1.53 * logT2 - 10.79 : 1.03 * logT2 - 8.71;
                }
                break;
            case SCHMIDT_1957:
                if (logT3 != null) mag = 2.5 * logT3 - 11.75;
                break;
            case DOWNES_DUERBECK_2000_LINEAR_T3:
                if (logT3 != null) mag = 2.54 * logT3 - 11.79;
                break;
            case DOWNES_DUERBECK_2000_COND_T3:
                if (logT3 != null) {
                    mag = logT3 < 1.5 ? 1.58 * logT3 - 11.26 : 0.56 * logT3 - 8.13;
                }
                break;
            case UNWEIGHTED_MEAN:
                double[] unweightedMeanAndError = unweightedMeanAbsMag(t2, t3);
                mag = unweightedMeanAndError != null ? unweightedMeanAndError[0] : null;
                break;
            case WEIGHTED_MEAN:
                double[] meanAndError = weightedMeanAbsMag(t2, t3, null, null);
                mag = meanAndError != null ? meanAndError[0] : null;
                break;
            }

            return mag;
        }

        /**
         * Return the 1-sigma uncertainty on the peak absolute magnitude for
         * this relation, propagated from the published coefficient errors and
         * the optional decline time errors.
         * 
         * @param t2      The time in days for a 2 magnitude decline; may be null.
         * @param t3      The time in days for a 3 magnitude decline; may be null.
         * @param sigmaT2 The error on t2 in days; may be null.
         * @param sigmaT3 The error on t3 in days; may be null.
         * @return The 1-sigma absolute magnitude error, or null if a required
         *         decline time is missing.
         */
        public Double absMagError(Double t2, Double t3, Double sigmaT2, Double sigmaT3) {
            Double logT2 = t2 != null && t2 > 0 ? Math.log10(t2) : null;
            Double logT3 = t3 != null && t3 > 0 ? Math.log10(t3) : null;

            // Errors on log10(t2) and log10(t3).
            double sigmaLogT2 = logT2 != null && sigmaT2 != null
                    ? sigmaT2 / (t2 * Math.log(10)) : 0;
            double sigmaLogT3 = logT3 != null && sigmaT3 != null
                    ? sigmaT3 / (t3 * Math.log(10)) : 0;

            Double error = null;

            switch (this) {
            case KANTHARIA_2017:
                if (logT2 != null) {
                    error = quadSum(0.117, 0.16 * logT2, 2.16 * sigmaLogT2);
                }
                break;
            case COHEN_1985:
                if (logT2 != null) {
                    error = quadSum(0.30, 0.23 * logT2, 2.41 * sigmaLogT2);
                }
                break;
            case DOWNES_DUERBECK_2000_LINEAR_T2:
                if (logT2 != null) {
                    error = quadSum(0.44, 0.323 * logT2, 2.55 * sigmaLogT2);
                }
                break;
            case DELLA_VALLE_LIVIO_1995:
                if (logT2 != null) {
                    error = arctanSlope(logT2, 0.81) * sigmaLogT2;
                }
                break;
            case DOWNES_DUERBECK_2000_ARCTAN:
                if (logT2 != null) {
                    error = arctanSlope(logT2, 1.23) * sigmaLogT2;
                }
                break;
            case DOWNES_DUERBECK_2000_COND_T2:
                if (logT2 != null) {
                    error = logT2 < 1.2
                            ? quadSum(0.92, 1.15 * logT2, 1.53 * sigmaLogT2)
                            : quadSum(0.82, 0.51 * logT2, 1.03 * sigmaLogT2);
                }
                break;
            case SCHMIDT_1957:
                // No coefficient errors quoted by Schmidt (1957).
                if (logT3 != null) {
                    error = 2.5 * sigmaLogT3;
                }
                break;
            case DOWNES_DUERBECK_2000_LINEAR_T3:
                if (logT3 != null) {
                    error = quadSum(0.56, 0.35 * logT3, 2.54 * sigmaLogT3);
                }
                break;
            case DOWNES_DUERBECK_2000_COND_T3:
                if (logT3 != null) {
                    error = logT3 < 1.5
                            ? quadSum(0.84, 0.78 * logT3, 1.58 * sigmaLogT3)
                            : quadSum(1.26, 0.68 * logT3, 0.56 * sigmaLogT3);
                }
                break;
            case UNWEIGHTED_MEAN:
                double[] unweightedMeanAndError = unweightedMeanAbsMag(t2, t3);
                error = unweightedMeanAndError != null ? unweightedMeanAndError[1] : null;
                break;
            case WEIGHTED_MEAN:
                double[] meanAndError = weightedMeanAbsMag(t2, t3, sigmaT2, sigmaT3);
                error = meanAndError != null ? meanAndError[1] : null;
                break;
            }

            return error;
        }

        /**
         * Return a short description of the source of the reported absolute
         * magnitude error for this relation.
         */
        public String getErrorSource() {
            String source;

            switch (this) {
            case UNWEIGHTED_MEAN:
                source = "inter-relation scatter";
                break;
            case WEIGHTED_MEAN:
                source = "max(formal inverse-variance error, inter-relation scatter)";
                break;
            default:
                source = "published coefficient errors and optional t2/t3 errors";
                break;
            }

            return source;
        }

        // The magnitude of d(Mv)/d(log10(t2)) for the arctan relations.
        private static double arctanSlope(double logT2, double coefficient) {
            double u = (1.32 - logT2) / 0.23;
            return (coefficient / 0.23) / (1 + u * u);
        }

        private static double quadSum(double... terms) {
            double sum = 0;
            for (double term : terms) {
                sum += term * term;
            }
            return Math.sqrt(sum);
        }

        /**
         * Return the unweighted mean peak absolute magnitude over all
         * historical relations for which the required decline time is
         * available. The returned error is the population standard deviation
         * of those relation values.
         * 
         * @param t2 The time in days for a 2 magnitude decline; may be null.
         * @param t3 The time in days for a 3 magnitude decline; may be null.
         * @return A two element array containing the unweighted mean absolute
         *         magnitude and inter-relation scatter, or null if neither
         *         decline time is available.
         */
        public static double[] unweightedMeanAbsMag(Double t2, Double t3) {
            List<Double> mags = absMagsForIndividualRelations(t2, t3);

            if (mags.isEmpty()) {
                return null;
            }

            double mean = 0;
            for (Double mag : mags) {
                mean += mag;
            }
            mean /= mags.size();

            double variance = 0;
            for (Double mag : mags) {
                double diff = mag - mean;
                variance += diff * diff;
            }
            variance /= mags.size();

            return new double[] { mean, Math.sqrt(variance) };
        }

        /**
         * Return the inverse-variance weighted mean peak absolute magnitude
         * over all historical relations for which the required decline time is
         * available. The returned error is the larger of the formal
         * inverse-variance error and the inter-relation scatter.
         * 
         * @param t2      The time in days for a 2 magnitude decline; may be null.
         * @param t3      The time in days for a 3 magnitude decline; may be null.
         * @param sigmaT2 The error on t2 in days; may be null.
         * @param sigmaT3 The error on t3 in days; may be null.
         * @return A two element array containing the weighted mean absolute
         *         magnitude and its error, or null if neither decline time is
         *         available.
         */
        public static double[] weightedMeanAbsMag(Double t2, Double t3,
                Double sigmaT2, Double sigmaT3) {
            double numerator = 0;
            double sumOfWeights = 0;

            for (MMRDRelation relation : individualRelations()) {
                Double mag = relation.absMag(t2, t3);
                if (mag == null) continue;

                Double sigma = relation.absMagError(t2, t3, sigmaT2, sigmaT3);
                if (sigma == null || sigma <= 0) {
                    sigma = DEFAULT_SIGMA;
                }

                double weight = 1 / (sigma * sigma);
                numerator += weight * mag;
                sumOfWeights += weight;
            }

            if (sumOfWeights == 0) {
                return null;
            }

            double formalError = Math.sqrt(1 / sumOfWeights);
            double[] unweightedMeanAndScatter = unweightedMeanAbsMag(t2, t3);
            double scatter = unweightedMeanAndScatter != null
                    ? unweightedMeanAndScatter[1] : formalError;

            return new double[] { numerator / sumOfWeights,
                    Math.max(formalError, scatter) };
        }

        /**
         * Return all non-aggregate historical MMRD relations used by Kok
         * (2010). Kantharia (2017) is a later calibration and remains a
         * separate default choice, not part of the Kok aggregate options.
         */
        private static List<MMRDRelation> individualRelations() {
            List<MMRDRelation> relations = new ArrayList<MMRDRelation>();

            for (MMRDRelation relation : values()) {
                if (relation != KANTHARIA_2017
                        && relation != UNWEIGHTED_MEAN
                        && relation != WEIGHTED_MEAN) {
                    relations.add(relation);
                }
            }

            return relations;
        }

        /**
         * Return absolute magnitude values for all individual relations whose
         * required decline time is available.
         */
        private static List<Double> absMagsForIndividualRelations(Double t2, Double t3) {
            List<Double> mags = new ArrayList<Double>();

            for (MMRDRelation relation : individualRelations()) {
                Double mag = relation.absMag(t2, t3);
                if (mag != null) {
                    mags.add(mag);
                }
            }

            return mags;
        }
    }

    /**
     * Light curve parameters extracted from nova observations.
     */
    public static class LightCurveParams {
        public Double peakMag;
        public Double peakJD;
        public Double t2;
        public Double t3;
        public Double sigmaT2;
        public Double sigmaT3;
    }

    /**
     * Distance result for a single MMRD relation.
     */
    public static class MMRDResult {
        public MMRDRelation relation;
        public Double absMag;
        public Double absMagError;
        public Double distancePc;
        public Double lowerDistancePc;
        public Double upperDistancePc;
        public Double lowerErrorPc;
        public Double upperErrorPc;
    }

    /**
     * Extract the peak magnitude, time of peak, and the t2 and t3 decline
     * times from the supplied time-ordered observation list. The peak is the
     * brightest observation; the decline times are obtained from the first
     * crossings of peak+2 and peak+3 magnitudes thereafter, by linear
     * interpolation between adjacent observations.
     * 
     * @param obs The time-ordered observations.
     * @return The light curve parameters; t2 and/or t3 may be null if the
     *         light curve never declines by the corresponding amount.
     */
    public static LightCurveParams extractLightCurveParams(List<ValidObservation> obs) {
        LightCurveParams params = new LightCurveParams();

        if (obs == null || obs.isEmpty()) {
            return params;
        }

        int brightestIndex = 0;
        for (int i = 1; i < obs.size(); i++) {
            if (obs.get(i).getMag() < obs.get(brightestIndex).getMag()) {
                brightestIndex = i;
            }
        }

        params.peakMag = obs.get(brightestIndex).getMag();
        params.peakJD = obs.get(brightestIndex).getJD();

        params.t2 = firstCrossingTime(obs, brightestIndex, params.peakMag + 2);
        params.t3 = firstCrossingTime(obs, brightestIndex, params.peakMag + 3);
        params.sigmaT2 = null;
        params.sigmaT3 = null;

        return params;
    }

    // Return the time in days after the peak at which the magnitude first
    // declines to targetMag, by linear interpolation, or null if it never does.
    private static Double firstCrossingTime(List<ValidObservation> obs,
            int peakIndex, double targetMag) {
        double peakJD = obs.get(peakIndex).getJD();

        for (int i = peakIndex + 1; i < obs.size(); i++) {
            ValidObservation previous = obs.get(i - 1);
            ValidObservation current = obs.get(i);

            if (current.getMag() >= targetMag) {
                double magRange = current.getMag() - previous.getMag();
                double fraction = magRange != 0
                        ? (targetMag - previous.getMag()) / magRange : 1;
                // Clamp in case the previous observation was already fainter
                // than the target (e.g. non-monotonic decline).
                fraction = Math.max(0, Math.min(1, fraction));
                double jd = previous.getJD()
                        + fraction * (current.getJD() - previous.getJD());
                return jd - peakJD;
            }
        }

        return null;
    }

    /**
     * Given the peak apparent magnitude, the peak absolute magnitude, and the
     * visual extinction, calculate the distance in parsecs via the distance
     * modulus.
     * 
     * @param apparentMag The peak apparent magnitude, mv.
     * @param absoluteMag The peak absolute magnitude, Mv.
     * @param extinction  The visual extinction, Av.
     * @return The distance in parsecs.
     */
    public static double calcDistance(double apparentMag, double absoluteMag,
            double extinction) {
        return Math.pow(10, 0.2 * (apparentMag - extinction - absoluteMag + 5));
    }

    /**
     * Calculate a distance and lower/upper bounds by propagating a symmetric
     * peak absolute magnitude error through the distance modulus.
     * 
     * @param apparentMag The peak apparent magnitude, mv.
     * @param absoluteMag The peak absolute magnitude, Mv.
     * @param extinction  The visual extinction, Av.
     * @param absMagError The 1-sigma error in peak absolute magnitude.
     * @return A five element array: lower distance, nominal distance, upper
     *         distance, lower error, upper error, all in parsecs.
     */
    public static double[] calcDistanceBounds(double apparentMag,
            double absoluteMag, double extinction, double absMagError) {
        double distance = calcDistance(apparentMag, absoluteMag, extinction);
        double lowerDistance = calcDistance(apparentMag, absoluteMag + absMagError,
                extinction);
        double upperDistance = calcDistance(apparentMag, absoluteMag - absMagError,
                extinction);

        return new double[] { lowerDistance, distance, upperDistance,
                distance - lowerDistance, upperDistance - distance };
    }

    /**
     * Combine an explicit visual extinction with optional reddening. A non-zero
     * Av takes precedence; otherwise Av = 3.1 E(B-V).
     */
    public static double effectiveExtinction(Double extinction, Double reddening) {
        if (extinction != null && extinction != 0) {
            return extinction;
        }
        return reddening != null ? 3.1 * reddening : 0;
    }

    /**
     * Compute the distance result for one MMRD relation.
     */
    public static MMRDResult resultFor(MMRDRelation relation, double peakMag,
            Double t2, Double t3, Double sigmaT2, Double sigmaT3,
            double extinction) {
        MMRDResult result = new MMRDResult();
        result.relation = relation;
        result.absMag = relation.absMag(t2, t3);

        if (result.absMag == null) {
            return result;
        }

        result.absMagError = relation.absMagError(t2, t3, sigmaT2, sigmaT3);
        result.distancePc = calcDistance(peakMag, result.absMag, extinction);

        if (result.absMagError != null) {
            double[] bounds = calcDistanceBounds(peakMag, result.absMag,
                    extinction, result.absMagError);
            result.lowerDistancePc = bounds[0];
            result.upperDistancePc = bounds[2];
            result.lowerErrorPc = bounds[3];
            result.upperErrorPc = bounds[4];
        }

        return result;
    }

    /**
     * Compute distance results for every supported MMRD relation, in enum order.
     */
    public static List<MMRDResult> resultsForAllRelations(double peakMag,
            Double t2, Double t3, Double sigmaT2, Double sigmaT3,
            double extinction) {
        List<MMRDResult> results = new ArrayList<MMRDResult>();

        for (MMRDRelation relation : MMRDRelation.values()) {
            results.add(resultFor(relation, peakMag, t2, t3, sigmaT2, sigmaT3,
                    extinction));
        }

        return results;
    }

    /**
     * Return a copy of the results sorted by increasing distance (nearest
     * first). Relations with no distance are placed last.
     */
    public static List<MMRDResult> sortedByDistance(List<MMRDResult> results) {
        List<MMRDResult> sorted = new ArrayList<MMRDResult>(results);

        Collections.sort(sorted, new Comparator<MMRDResult>() {
            @Override
            public int compare(MMRDResult a, MMRDResult b) {
                return NULLS_LAST_DOUBLE.compare(a.distancePc, b.distancePc);
            }
        });

        return sorted;
    }

    @Override
    public void invoke(ISeriesInfoProvider seriesInfo) {
        // Request the series to be used.
        ObservationAndMeanPlotModel model = Mediator.getInstance()
                .getObservationPlotModel(AnalysisType.RAW_DATA);

        SingleSeriesSelectionDialog seriesDlg = new SingleSeriesSelectionDialog(model);

        if (seriesDlg.isCancelled()) return;

        SeriesType series = seriesDlg.getSeries();
        List<ValidObservation> obs = seriesInfo.getObservations(series);

        // Request the source of the light curve parameters.
        List<String> sources = new ArrayList<String>();
        sources.add(EXPONENTIAL_FIT);
        sources.add(DIRECT_FROM_OBS);
        SelectableTextField sourceField = new SelectableTextField(
                "Light Curve Parameter Source", sources, EXPONENTIAL_FIT);

        MultiEntryComponentDialog sourceDlg = new MultiEntryComponentDialog(
                "t2/t3 Source", sourceField);

        if (sourceDlg.isCancelled()) return;

        LightCurveParams params;

        if (EXPONENTIAL_FIT.equals(sourceField.getValue())) {
            params = fitExponentialModel(obs);
            if (params == null) return;
        } else {
            params = extractLightCurveParams(obs);
        }

        // Compare MMRD relations without repeating series selection or the fit.
        compareRelations(params);
    }

    // Present the MMRD input dialog repeatedly until the user cancels. Each
    // OK computes the selected relation in detail and a summary table of every
    // supported relation.
    private void compareRelations(LightCurveParams params) {
        String selectedRelationName = MMRDRelation.KANTHARIA_2017.getDisplayName();
        Double peakMag = params.peakMag;
        Double t2 = params.t2;
        Double t3 = params.t3;
        Double sigmaT2 = params.sigmaT2;
        Double sigmaT3 = params.sigmaT3;
        Double extinction = 0.0;
        Double reddening = 0.0;

        while (true) {
            List<String> relationNames = new ArrayList<String>();
            for (MMRDRelation relation : MMRDRelation.values()) {
                relationNames.add(relation.getDisplayName());
            }

            MMRDRelation initialRelation = MMRDRelation
                    .fromDisplayName(selectedRelationName);
            if (initialRelation == null) {
                initialRelation = MMRDRelation.KANTHARIA_2017;
                selectedRelationName = initialRelation.getDisplayName();
            }

            final TextArea equationField = new TextArea(
                    "MMRD Relation Equation and Error Source",
                    relationEquationDisplay(initialRelation), 2, 40, true, true);
            JTextArea equationTextArea = (JTextArea) equationField.getUIComponent();
            equationTextArea.setLineWrap(true);
            equationTextArea.setWrapStyleWord(true);

            final SelectableTextField relationField = new SelectableTextField(
                    "MMRD Relation", relationNames, selectedRelationName);
            relationField.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    MMRDRelation relation = MMRDRelation
                            .fromDisplayName(relationField.getValue());
                    if (relation != null) {
                        equationField.setValue(relationEquationDisplay(relation));
                    }
                }
            });

            DoubleField peakMagField = new DoubleField("Peak Apparent Mag (mv)",
                    null, null, peakMag);
            DoubleField t2Field = new DoubleField("t2 (days)", 0.0, null, t2);
            DoubleField t3Field = new DoubleField("t3 (days)", 0.0, null, t3);
            DoubleField sigmaT2Field = new DoubleField("sigma t2 (days)",
                    0.0, null, sigmaT2);
            DoubleField sigmaT3Field = new DoubleField("sigma t3 (days)",
                    0.0, null, sigmaT3);
            DoubleField extinctionField = new DoubleField("Extinction (Av)",
                    null, null, extinction);
            DoubleField reddeningField = new DoubleField(
                    "or Reddening E(B-V); Av = 3.1 E(B-V)", null, null,
                    reddening);

            List<ITextComponent<?>> fields = new ArrayList<ITextComponent<?>>();
            fields.add(relationField);
            fields.add(equationField);
            fields.add(peakMagField);
            fields.add(t2Field);
            fields.add(t3Field);
            fields.add(sigmaT2Field);
            fields.add(sigmaT3Field);
            fields.add(extinctionField);
            fields.add(reddeningField);

            MultiEntryComponentDialog inputDlg = new MultiEntryComponentDialog(
                    "MMRD Inputs", fields);

            if (inputDlg.isCancelled()) {
                return;
            }

            selectedRelationName = relationField.getValue();
            peakMag = peakMagField.getValue();
            t2 = t2Field.getValue();
            t3 = t3Field.getValue();
            sigmaT2 = sigmaT2Field.getValue();
            sigmaT3 = sigmaT3Field.getValue();
            extinction = extinctionField.getValue();
            reddening = reddeningField.getValue();

            MMRDRelation relation = MMRDRelation
                    .fromDisplayName(selectedRelationName);

            if (relation == null || peakMag == null) {
                MessageBox.showErrorDialog("MMRD Nova Distance",
                        "A relation and peak apparent magnitude are required.");
                continue;
            }

            double av = effectiveExtinction(extinction, reddening);
            List<MMRDResult> allResults = resultsForAllRelations(peakMag, t2, t3,
                    sigmaT2, sigmaT3, av);
            MMRDResult selected = resultFor(relation, peakMag, t2, t3, sigmaT2,
                    sigmaT3, av);

            if (!anyResultAvailable(allResults)) {
                MessageBox.showErrorDialog("MMRD Nova Distance",
                        "The selected relation requires a decline time (t2 and/or t3) "
                                + "that is not available. The light curve may not "
                                + "decline far enough below maximum; the value can "
                                + "also be entered manually.");
                continue;
            }

            showResults(selected, allResults, peakMag, av);
        }
    }

    private static boolean anyResultAvailable(List<MMRDResult> results) {
        for (MMRDResult result : results) {
            if (result.absMag != null) {
                return true;
            }
        }
        return false;
    }

    // Fit the exponential decline model, submitting a copy of it to the
    // Mediator so that the fit becomes visible as a Model series.
    private LightCurveParams fitExponentialModel(List<ValidObservation> obs) {
        try {
            NovaExponentialModel expModel = new NovaExponentialModel(obs);
            expModel.execute();

            if (!inTestMode()) {
                // A fresh instance is submitted since the Mediator's modelling
                // task will execute the model it is given.
                Mediator.getInstance().performModellingOperation(
                        new NovaExponentialModel(obs));
            }

            LightCurveParams params = new LightCurveParams();
            // The decline times are measured from the observed maximum
            // magnitude (per section 2.2 of Kok 2010), i.e. they are the
            // crossing times of peak+2 and peak+3 on the fitted curve.
            params.peakMag = expModel.getObservedPeakMagnitude();
            params.peakJD = expModel.getPeakJD();
            params.t2 = expModel.timeToDecline(2, params.peakMag);
            params.t3 = expModel.timeToDecline(3, params.peakMag);
            params.sigmaT2 = expModel.timeToDeclineError(2, params.peakMag);
            params.sigmaT3 = expModel.timeToDeclineError(3, params.peakMag);

            return params;
        } catch (AlgorithmError e) {
            MessageBox.showErrorDialog("MMRD Nova Distance",
                    "Exponential model fit failed: " + e.getLocalizedMessage());
            return null;
        }
    }

    // Build a two-line description of a relation for the input dialog: the
    // relation equation on the first line and its error source on the second.
    // The aggregate-mean relations embed their error description after a
    // semicolon, so that clause is dropped in favour of the uniform
    // "Error source: ..." second line used by all relations.
    private static String relationEquationDisplay(MMRDRelation relation) {
        String equation = relation.getEquation();
        int semicolon = equation.indexOf(';');
        String firstLine = semicolon >= 0
                ? equation.substring(0, semicolon).trim() : equation;
        return firstLine + "\nError source: " + relation.getErrorSource();
    }

    // Show the selected-relation details together with a summary table of
    // every MMRD relation. Dismissing the dialog returns to the input dialog.
    private void showResults(MMRDResult selected, List<MMRDResult> allResults,
            double peakMag, double extinction) {
        List<ITextComponent<String>> resultFields = new ArrayList<ITextComponent<String>>();

        resultFields.add(new TextField("MMRD Relation",
                selected.relation.getDisplayName(), true, false));

        if (selected.absMag != null) {
            String absMagStr = NumericPrecisionPrefs.formatMag(selected.absMag);
            if (selected.absMagError != null) {
                absMagStr += " \u00B1 "
                        + NumericPrecisionPrefs.formatMag(selected.absMagError);
            }
            resultFields.add(new TextField("Peak Absolute Magnitude (Mv)",
                    absMagStr, true, false));

            double distanceModulus = peakMag - extinction - selected.absMag;
            resultFields.add(new TextField("Distance Modulus (mv - Av - Mv)",
                    NumericPrecisionPrefs.formatMag(distanceModulus), true,
                    false));

            resultFields.add(new TextField("Distance (kpc)",
                    NumericPrecisionPrefs.formatOther(selected.distancePc / 1000),
                    true, false));
            resultFields.add(new TextField("Distance (light years)",
                    NumericPrecisionPrefs.formatOther(selected.distancePc * 3.26),
                    true, false));

            if (selected.absMagError != null) {
                resultFields.add(new TextField("Distance Lower Bound (kpc)",
                        NumericPrecisionPrefs.formatOther(
                                selected.lowerDistancePc / 1000),
                        true, false));
                resultFields.add(new TextField("Distance Upper Bound (kpc)",
                        NumericPrecisionPrefs.formatOther(
                                selected.upperDistancePc / 1000),
                        true, false));
                resultFields.add(new TextField("Distance Error (kpc)",
                        "-" + NumericPrecisionPrefs.formatOther(
                                selected.lowerErrorPc / 1000)
                                + " / +"
                                + NumericPrecisionPrefs.formatOther(
                                        selected.upperErrorPc / 1000),
                        true, false));
            }
        } else {
            resultFields.add(new TextField("Peak Absolute Magnitude (Mv)",
                    "n/a", true, false));
            resultFields.add(new TextField("Note",
                    "The selected relation needs a decline time that is not "
                            + "available; other relations are shown in the table.",
                    true, false));
        }

        if (extinction == 0) {
            resultFields.add(new TextField("Note",
                    "No extinction was applied (Av = 0), so the distances are "
                            + "upper limits.",
                    true, false));
        }

        new MMRDResultsDialog(resultFields, allResults, selected.relation);
    }

    @SuppressWarnings("serial")
    private class MMRDResultsDialog extends JDialog {

        public MMRDResultsDialog(List<ITextComponent<String>> resultFields,
                List<MMRDResult> allResults, MMRDRelation selectedRelation) {
            super(DocumentManager.findActiveWindow(), "MMRD Nova Distance",
                    ModalityType.APPLICATION_MODAL);
            setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

            ActionListener dismissListener = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setVisible(false);
                    dispose();
                }
            };
            getRootPane().registerKeyboardAction(dismissListener,
                    KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                    JComponent.WHEN_IN_FOCUSED_WINDOW);

            JPanel topPane = new JPanel();
            topPane.setLayout(new BoxLayout(topPane, BoxLayout.PAGE_AXIS));
            topPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

            for (ITextComponent<String> field : resultFields) {
                field.setEditable(false);
                topPane.add(field.getUIComponent());
            }

            topPane.add(createTablePane(allResults, selectedRelation));
            topPane.add(createButtonPane(dismissListener));

            getContentPane().add(topPane);
            pack();
            setLocationRelativeTo(Mediator.getUI().getContentPane());
            setVisible(true);
        }

        private JScrollPane createTablePane(List<MMRDResult> allResults,
                MMRDRelation selectedRelation) {
            MMRDSummaryTableModel tableModel = new MMRDSummaryTableModel(
                    allResults);
            JTable table = new JTable(tableModel);
            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            table.getColumnModel().getColumn(0).setPreferredWidth(340);
            table.getColumnModel().getColumn(1).setPreferredWidth(70);
            table.getColumnModel().getColumn(2).setPreferredWidth(70);
            table.getColumnModel().getColumn(3).setPreferredWidth(80);
            table.getColumnModel().getColumn(4).setPreferredWidth(100);
            table.getColumnModel().getColumn(5).setPreferredWidth(100);
            // Whole-row selection. Do not call setCellSelectionEnabled(false):
            // that method also turns off row selection.
            table.setColumnSelectionAllowed(false);
            table.setRowSelectionAllowed(true);
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            table.setFocusable(true);

            TableNumberRenderer numberRenderer = new TableNumberRenderer();
            for (int column = 1; column < tableModel.getColumnCount(); column++) {
                table.getColumnModel().getColumn(column)
                        .setCellRenderer(numberRenderer);
            }

            TableRowSorter<MMRDSummaryTableModel> sorter =
                    new TableRowSorter<MMRDSummaryTableModel>(tableModel);
            for (int column = 1; column < tableModel.getColumnCount(); column++) {
                sorter.setComparator(column, NULLS_LAST_DOUBLE);
            }
            sorter.setSortKeys(Collections.singletonList(
                    new RowSorter.SortKey(MMRDSummaryTableModel.COL_D_KPC,
                            SortOrder.ASCENDING)));
            table.setRowSorter(sorter);

            int modelRow = indexOfRelation(allResults, selectedRelation);
            if (modelRow >= 0) {
                int viewRow = table.convertRowIndexToView(modelRow);
                if (viewRow >= 0) {
                    table.setRowSelectionInterval(viewRow, viewRow);
                    table.scrollRectToVisible(
                            table.getCellRect(viewRow, 0, true));
                }
            }

            JScrollPane pane = new JScrollPane(table);
            pane.setBorder(BorderFactory.createTitledBorder("All MMRD relations"));
            pane.setPreferredSize(new Dimension(780, 220));
            return pane;
        }

        private JPanel createButtonPane(ActionListener dismissListener) {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
            panel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

            JButton dismissButton = new JButton(LocaleProps.get("DISMISS_BUTTON"));
            dismissButton.addActionListener(dismissListener);
            panel.add(dismissButton);
            getRootPane().setDefaultButton(dismissButton);

            return panel;
        }
    }

    private static int indexOfRelation(List<MMRDResult> results,
            MMRDRelation relation) {
        for (int i = 0; i < results.size(); i++) {
            if (results.get(i).relation == relation) {
                return i;
            }
        }
        return -1;
    }

    private static class MMRDSummaryTableModel extends AbstractTableModel {

        static final int COL_RELATION = 0;
        static final int COL_MV = 1;
        static final int COL_SIGMA_MV = 2;
        static final int COL_D_KPC = 3;
        static final int COL_D_LOWER = 4;
        static final int COL_D_UPPER = 5;

        private static final String[] COLUMN_NAMES = { "Relation", "Mv",
                "sigma Mv", "D (kpc)", "D lower (kpc)", "D upper (kpc)" };

        private final List<MMRDResult> results;

        public MMRDSummaryTableModel(List<MMRDResult> results) {
            this.results = results;
        }

        @Override
        public int getColumnCount() {
            return COLUMN_NAMES.length;
        }

        @Override
        public int getRowCount() {
            return results.size();
        }

        @Override
        public String getColumnName(int column) {
            return COLUMN_NAMES[column];
        }

        @Override
        public Class<?> getColumnClass(int column) {
            return column == COL_RELATION ? String.class : Double.class;
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }

        @Override
        public Object getValueAt(int row, int column) {
            MMRDResult result = results.get(row);

            switch (column) {
            case COL_RELATION:
                return result.relation.getDisplayName();
            case COL_MV:
                return result.absMag;
            case COL_SIGMA_MV:
                return result.absMagError;
            case COL_D_KPC:
                return toKpc(result.distancePc);
            case COL_D_LOWER:
                return toKpc(result.lowerDistancePc);
            case COL_D_UPPER:
                return toKpc(result.upperDistancePc);
            default:
                return null;
            }
        }
    }

    @SuppressWarnings("serial")
    private static class TableNumberRenderer extends DefaultTableCellRenderer {

        public TableNumberRenderer() {
            setHorizontalAlignment(SwingConstants.RIGHT);
        }

        @Override
        protected void setValue(Object value) {
            if (value == null) {
                setText("n/a");
            } else if (value instanceof Double) {
                setText(formatTableNumber((Double) value));
            } else {
                super.setValue(value);
            }
        }
    }

    // Null distances/magnitudes sort after numeric values so n/a rows stay last
    // on an ascending sort.
    static final Comparator<Double> NULLS_LAST_DOUBLE = new Comparator<Double>() {
        @Override
        public int compare(Double a, Double b) {
            if (a == null && b == null) {
                return 0;
            }
            if (a == null) {
                return 1;
            }
            if (b == null) {
                return -1;
            }
            return Double.compare(a, b);
        }
    };

    // Comparison-table precision: two decimal places, independent of the
    // numeric-precision preferences (which default to 6 mag / 12 other).
    private static final int TABLE_DECIMAL_PLACES = 2;

    private static Double toKpc(Double distancePc) {
        return distancePc == null ? null : distancePc / 1000;
    }

    private static String formatTableNumber(double value) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(
                Locale.getDefault());
        symbols.setMinusSign('-');
        DecimalFormat format = new DecimalFormat("0.00", symbols);
        format.setMinimumFractionDigits(TABLE_DECIMAL_PLACES);
        format.setMaximumFractionDigits(TABLE_DECIMAL_PLACES);
        format.setGroupingUsed(false);
        return format.format(value);
    }

    @Override
    public String getDescription() {
        return "MMRD (Maximum Magnitude vs Rate of Decline) nova distance calculator";
    }

    @Override
    public String getDisplayName() {
        return "MMRD nova distance calculator";
    }

    /**
     * @see org.aavso.tools.vstar.plugin.IPlugin#getDocName()
     */
    @Override
    public String getDocName() {
        return "MMRD.pdf";
    }

    // Plug-in test

    @Override
    public Boolean test() {
        boolean result = true;

        setTestMode(true);

        try {
            // Kantharia (2017), equation (15) spot values.
            result &= Tolerance.areClose(-8.644,
                    MMRDRelation.KANTHARIA_2017.absMag(10.0, null), 1e-6, true);
            result &= Tolerance.areClose(-10.804,
                    MMRDRelation.KANTHARIA_2017.absMag(1.0, null), 1e-6, true);

            // Distance modulus identity: m = M => 10 pc.
            result &= Tolerance.areClose(10.0, calcDistance(5.0, 5.0, 0), 1e-9, true);

            // Kok (2010): V5583 Sgr, t2 = 4.5 +/- 1.2, t3 = 8.8 +/- 1.7
            // (Table 1); expected Mv = -9.3 +/- 0.1 (Table 3) and, with
            // mv = 7.0 and Av = 0.76, D = 13 +/- 2 kpc (Table 4).
            double[] meanAndError = MMRDRelation.weightedMeanAbsMag(4.5, 8.8, 1.2, 1.7);
            result &= meanAndError != null;
            if (meanAndError != null) {
                result &= Math.abs(meanAndError[0] - -9.3) <= 0.25;

                double distanceKpc = calcDistance(7.0, meanAndError[0], 0.76) / 1000;
                result &= Math.abs(distanceKpc - 13.0) <= 2.0;
            }
        } finally {
            setTestMode(false);
        }

        return result;
    }
}
