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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

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
import org.aavso.tools.vstar.ui.dialog.TextDialog;
import org.aavso.tools.vstar.ui.dialog.TextField;
import org.aavso.tools.vstar.ui.dialog.series.SingleSeriesSelectionDialog;
import org.aavso.tools.vstar.ui.mediator.AnalysisType;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.model.plot.ISeriesInfoProvider;
import org.aavso.tools.vstar.ui.model.plot.ObservationAndMeanPlotModel;
import org.aavso.tools.vstar.util.Tolerance;
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
                "Mv = 1.58 log10(t3) - 11.26 if log10(t3) < 1.5, else 0.56 log10(t3) - 8.13"),

        WEIGHTED_MEAN("Error-weighted mean of all relations (Kok 2010)",
                "Inverse-variance weighted mean of all of the above relations");

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
            case WEIGHTED_MEAN:
                double[] meanAndError = weightedMeanAbsMag(t2, t3, sigmaT2, sigmaT3);
                error = meanAndError != null ? meanAndError[1] : null;
                break;
            }

            return error;
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
         * Return the inverse-variance weighted mean peak absolute magnitude
         * over all relations for which the required decline time is
         * available, after the approach of Kok (2010).
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

            for (MMRDRelation relation : values()) {
                if (relation == WEIGHTED_MEAN) continue;

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

            return sumOfWeights > 0
                    ? new double[] { numerator / sumOfWeights, Math.sqrt(1 / sumOfWeights) }
                    : null;
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

        // Request the relation and permit the parameters to be overridden.
        List<String> relationNames = new ArrayList<String>();
        for (MMRDRelation relation : MMRDRelation.values()) {
            relationNames.add(relation.getDisplayName());
        }

        final TextField equationField = new TextField("MMRD Relation Equation",
                MMRDRelation.KANTHARIA_2017.getEquation(), true, false);

        final SelectableTextField relationField = new SelectableTextField(
                "MMRD Relation", relationNames,
                MMRDRelation.KANTHARIA_2017.getDisplayName());
        relationField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MMRDRelation relation = MMRDRelation
                        .fromDisplayName(relationField.getValue());
                if (relation != null) {
                    equationField.setValue(relation.getEquation());
                }
            }
        });

        DoubleField peakMagField = new DoubleField("Peak Apparent Mag (mv)",
                null, null, params.peakMag);
        DoubleField t2Field = new DoubleField("t2 (days)", 0.0, null, params.t2);
        DoubleField t3Field = new DoubleField("t3 (days)", 0.0, null, params.t3);
        DoubleField extinctionField = new DoubleField("Extinction (Av)",
                null, null, 0.0);
        DoubleField reddeningField = new DoubleField(
                "or Reddening E(B-V); Av = 3.1 E(B-V)", null, null, 0.0);

        List<ITextComponent<?>> fields = new ArrayList<ITextComponent<?>>();
        fields.add(relationField);
        fields.add(equationField);
        fields.add(peakMagField);
        fields.add(t2Field);
        fields.add(t3Field);
        fields.add(extinctionField);
        fields.add(reddeningField);

        MultiEntryComponentDialog inputDlg = new MultiEntryComponentDialog(
                "MMRD Inputs", fields);

        if (inputDlg.isCancelled()) return;

        MMRDRelation relation = MMRDRelation
                .fromDisplayName(relationField.getValue());
        Double peakMag = peakMagField.getValue();
        Double t2 = t2Field.getValue();
        Double t3 = t3Field.getValue();
        Double extinction = extinctionField.getValue();
        Double reddening = reddeningField.getValue();

        if (relation == null || peakMag == null) {
            MessageBox.showErrorDialog("MMRD Nova Distance",
                    "A relation and peak apparent magnitude are required.");
            return;
        }

        double effectiveExtinction = extinction != null && extinction != 0
                ? extinction
                : (reddening != null ? 3.1 * reddening : 0);

        Double absMag = relation.absMag(t2, t3);

        if (absMag == null) {
            MessageBox.showErrorDialog("MMRD Nova Distance",
                    "The selected relation requires a decline time (t2 and/or t3) "
                            + "that is not available. The light curve may not "
                            + "decline far enough below maximum; the value can "
                            + "also be entered manually.");
            return;
        }

        Double absMagError = relation.absMagError(t2, t3, null, null);
        double distance = calcDistance(peakMag, absMag, effectiveExtinction);

        showResults(relation, absMag, absMagError, peakMag, effectiveExtinction,
                distance);
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

            return params;
        } catch (AlgorithmError e) {
            MessageBox.showErrorDialog("MMRD Nova Distance",
                    "Exponential model fit failed: " + e.getLocalizedMessage());
            return null;
        }
    }

    // Show the absolute magnitude and distance results in a dialog.
    private void showResults(MMRDRelation relation, double absMag,
            Double absMagError, double peakMag, double extinction,
            double distance) {
        List<ITextComponent<String>> resultFields = new ArrayList<ITextComponent<String>>();

        resultFields.add(new TextField("MMRD Relation",
                relation.getDisplayName(), true, false));
        resultFields.add(new TextField("Equation", relation.getEquation(),
                true, false));

        String absMagStr = NumericPrecisionPrefs.formatMag(absMag);
        if (absMagError != null) {
            absMagStr += " \u00B1 " + NumericPrecisionPrefs.formatMag(absMagError);
        }
        resultFields.add(new TextField("Peak Absolute Magnitude (Mv)",
                absMagStr, true, false));

        double distanceModulus = peakMag - extinction - absMag;
        resultFields.add(new TextField("Distance Modulus (mv - Av - Mv)",
                NumericPrecisionPrefs.formatMag(distanceModulus), true, false));

        resultFields.add(new TextField("Distance (parsecs)",
                NumericPrecisionPrefs.formatOther(distance), true, false));
        resultFields.add(new TextField("Distance (kpc)",
                NumericPrecisionPrefs.formatOther(distance / 1000), true, false));
        resultFields.add(new TextField("Distance (light years)",
                NumericPrecisionPrefs.formatOther(distance * 3.26), true, false));

        if (absMagError != null) {
            double minDistance = calcDistance(peakMag, absMag - absMagError,
                    extinction);
            double maxDistance = calcDistance(peakMag, absMag + absMagError,
                    extinction);
            resultFields.add(new TextField("Distance Range (parsecs)",
                    NumericPrecisionPrefs.formatOther(maxDistance) + " to "
                            + NumericPrecisionPrefs.formatOther(minDistance),
                    true, false));
        }

        if (extinction == 0) {
            resultFields.add(new TextField("Note",
                    "No extinction was applied (Av = 0), so the distance is "
                            + "an upper limit.", true, false));
        }

        new TextDialog("MMRD Nova Distance", resultFields);
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
