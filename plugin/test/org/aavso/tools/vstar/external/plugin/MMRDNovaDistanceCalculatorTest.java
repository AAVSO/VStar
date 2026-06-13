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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.aavso.tools.vstar.data.Magnitude;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.exception.AlgorithmError;
import org.aavso.tools.vstar.external.lib.NovaExponentialModel;
import org.aavso.tools.vstar.external.plugin.MMRDNovaDistanceCalculator.LightCurveParams;
import org.aavso.tools.vstar.external.plugin.MMRDNovaDistanceCalculator.MMRDRelation;

import junit.framework.TestCase;

/**
 * Unit tests for the MMRD nova distance calculator plug-in and the nova
 * exponential decline model, with respect to:
 * 
 * <ol>
 * <li>Kantharia, N. G. 2017, "Novae: I. The maximum magnitude relation with
 * decline time (MMRD) and distance", arXiv:1703.04087</li>
 * <li>Kok, Y. 2010, "Absolute Magnitudes and Distances of Recent Novae",
 * JAAVSO, 38, 193</li>
 * </ol>
 */
public class MMRDNovaDistanceCalculatorTest extends TestCase {

    // Kok (2010) Tables 1, 3, and 4. Columns: peak apparent magnitude m0,
    // t2, t2 error, t3, t3 error (Table 1); the peak absolute magnitude Mv
    // and its error (Table 3); Av(1) and the corresponding distance D(1) in
    // kpc, Av(2) and D(2) in kpc (Table 4). Av(1) is from the Schlegel et
    // al. (1998) full line-of-sight map; Av(2) from the Arenou et al. (1992)
    // tridimensional model.
    private static final String[] KOK_NOVA_NAMES = {
            "V351 Pup", "V4633 Sgr", "V2467 Cyg", "V1213 Cen", "V5583 Sgr", "V2672 Oph" };

    private static final double[][] KOK_TABLE_DATA = {
            // m0, t2, t2Err, t3, t3Err, Mv, MvErr, Av1, D1, Av2, D2
            { 6.4, 11.1, 5.5, 27.2, 12.5, -8.5, 0.3, 4.20, 1.4, 1.46, 4.9 },
            { 7.6, 19.5, 4.7, 42.9, 11.2, -7.7, 0.3, 1.30, 6.3, 0.71, 8.3 },
            { 7.4, 9.0, 3.6, 17.8, 5.6, -8.8, 0.2, 20.24, 0.0, 2.00, 7.0 },
            { 8.2, 6.6, 2.8, 15.3, 8.0, -9.1, 0.1, 6.72, 1.3, 1.89, 12.0 },
            { 7.0, 4.5, 1.2, 8.8, 1.7, -9.3, 0.1, 1.29, 9.9, 0.76, 13.0 },
            { 10.0, 1.0, 0.7, 2.0, 0.7, -9.8, 0.1, 5.02, 9.2, 1.69, 42.0 } };

    public MMRDNovaDistanceCalculatorTest(String name) {
        super(name);
    }

    // MMRD relation tests: Kantharia (2017)

    public void testKanthariaRelationSpotValues() {
        // Equation (15): Mv = 2.16 log10(t2) - 10.804
        assertEquals(-8.644, MMRDRelation.KANTHARIA_2017.absMag(10.0, null), 1e-9);
        assertEquals(-10.804, MMRDRelation.KANTHARIA_2017.absMag(1.0, null), 1e-9);
        assertEquals(-6.484, MMRDRelation.KANTHARIA_2017.absMag(100.0, null), 1e-9);
    }

    public void testKanthariaRelationErrors() {
        // At log10(t2) = 1 with no t2 error: sqrt(0.117^2 + 0.16^2).
        double expected = Math.sqrt(0.117 * 0.117 + 0.16 * 0.16);
        assertEquals(expected,
                MMRDRelation.KANTHARIA_2017.absMagError(10.0, null, null, null), 1e-9);
    }

    // MMRD relation tests: historical calibrations as listed in section 2.3
    // of Kantharia (2017) and (sign-corrected) in Kok (2010). All have a
    // positive slope: faster novae are intrinsically brighter.

    public void testHistoricalLinearRelationSpotValues() {
        // Cohen 1985: Mv = 2.41 log10(t2) - 10.70
        assertEquals(-8.29, MMRDRelation.COHEN_1985.absMag(10.0, null), 1e-9);

        // Downes & Duerbeck 2000: Mv = 2.55 log10(t2) - 11.32
        assertEquals(-8.77,
                MMRDRelation.DOWNES_DUERBECK_2000_LINEAR_T2.absMag(10.0, null), 1e-9);

        // Schmidt 1957: Mv = 2.5 log10(t3) - 11.75
        assertEquals(-9.25, MMRDRelation.SCHMIDT_1957.absMag(null, 10.0), 1e-9);

        // Downes & Duerbeck 2000: Mv = 2.54 log10(t3) - 11.79
        assertEquals(-9.25,
                MMRDRelation.DOWNES_DUERBECK_2000_LINEAR_T3.absMag(null, 10.0), 1e-9);
    }

    public void testArctanRelationSpotValues() {
        // At log10(t2) = 1.32, the arctan term vanishes, leaving the
        // zero-points of the two calibrations.
        double t2 = Math.pow(10, 1.32);

        assertEquals(-7.92, MMRDRelation.DELLA_VALLE_LIVIO_1995.absMag(t2, null), 1e-9);
        assertEquals(-8.02,
                MMRDRelation.DOWNES_DUERBECK_2000_ARCTAN.absMag(t2, null), 1e-9);
    }

    public void testConditionalRelationSpotValues() {
        // t2 branches: log10(t2) < 1.2 and >= 1.2.
        assertEquals(-9.26,
                MMRDRelation.DOWNES_DUERBECK_2000_COND_T2.absMag(10.0, null), 1e-9);
        assertEquals(-6.65,
                MMRDRelation.DOWNES_DUERBECK_2000_COND_T2.absMag(100.0, null), 1e-9);

        // t3 branches: log10(t3) < 1.5 and >= 1.5.
        assertEquals(-9.68,
                MMRDRelation.DOWNES_DUERBECK_2000_COND_T3.absMag(null, 10.0), 1e-9);
        assertEquals(-7.01,
                MMRDRelation.DOWNES_DUERBECK_2000_COND_T3.absMag(null, 100.0), 1e-9);
    }

    public void testRelationsWithMissingDeclineTimes() {
        // t2-based relations require t2; t3-based require t3.
        assertNull(MMRDRelation.KANTHARIA_2017.absMag(null, 10.0));
        assertNull(MMRDRelation.SCHMIDT_1957.absMag(10.0, null));
        assertNull(MMRDRelation.WEIGHTED_MEAN.absMag(null, null));

        // The weighted mean degrades gracefully to the available subset.
        assertNotNull(MMRDRelation.WEIGHTED_MEAN.absMag(10.0, null));
        assertNotNull(MMRDRelation.WEIGHTED_MEAN.absMag(null, 10.0));
    }

    // Reproduction of Kok (2010), Table 3: error-weighted mean peak absolute
    // magnitudes from t2 and t3. The paper does not fully specify its
    // weighting scheme, so the agreement tolerance is wider than the paper's
    // quoted uncertainties; the fastest nova (V2672 Oph) shows the largest
    // weighting sensitivity since the relations diverge most at small t2.

    public void testKokTable3WeightedMeanAbsoluteMagnitudes() {
        double[] tolerances = { 0.25, 0.25, 0.25, 0.25, 0.25, 0.6 };

        for (int i = 0; i < KOK_TABLE_DATA.length; i++) {
            double[] row = KOK_TABLE_DATA[i];
            double[] meanAndError = MMRDRelation.weightedMeanAbsMag(
                    row[1], row[3], row[2], row[4]);

            assertNotNull(KOK_NOVA_NAMES[i], meanAndError);
            assertEquals(KOK_NOVA_NAMES[i] + ": Mv", row[5], meanAndError[0],
                    tolerances[i]);
        }
    }

    // Reproduction of Kok (2010), Table 4: distances from the paper's own
    // peak absolute magnitudes and both extinction estimates. This isolates
    // the distance equation, D = 10^(0.2(m0 - Av - Mv + 5)) pc, from the
    // weighted mean; tolerances reflect the paper's rounding of Mv and D.

    public void testKokTable4Distances() {
        for (int i = 0; i < KOK_TABLE_DATA.length; i++) {
            double[] row = KOK_TABLE_DATA[i];
            double m0 = row[0];
            double absMag = row[5];

            double d1Kpc = MMRDNovaDistanceCalculator.calcDistance(m0, absMag, row[7]) / 1000;
            double d2Kpc = MMRDNovaDistanceCalculator.calcDistance(m0, absMag, row[9]) / 1000;

            if (row[8] == 0.0) {
                // V2467 Cyg with the full line-of-sight Av of 20.24 yields a
                // degenerate, essentially zero, distance in the paper.
                assertTrue(KOK_NOVA_NAMES[i] + ": D(1)", d1Kpc < 0.05);
            } else {
                assertEquals(KOK_NOVA_NAMES[i] + ": D(1)", row[8], d1Kpc,
                        0.05 * row[8]);
            }

            assertEquals(KOK_NOVA_NAMES[i] + ": D(2)", row[10], d2Kpc,
                    0.05 * row[10]);
        }
    }

    public void testKokV2672OphDistanceWithMeasuredReddening() {
        // Kok (2010), section 3: with E(B-V) = +1.6 (Kato 2009), i.e.
        // Av = 3.1 * 1.6 = 4.96, the V2672 Oph distance becomes 9.4 kpc.
        double av = 3.1 * 1.6;
        double dKpc = MMRDNovaDistanceCalculator.calcDistance(10.0, -9.8, av) / 1000;
        assertEquals(9.4, dKpc, 0.05 * 9.4);
    }

    // Full pipeline: t2/t3 -> weighted mean Mv -> distance, compared with
    // Kok (2010) Table 4 D(2) within the paper's quoted distance errors.

    public void testKokFullPipelineDistances() {
        double[] paperDistanceErrors = { 1.3, 1.6, 1.7, 3.0, 2.0, 10.0 };

        for (int i = 0; i < KOK_TABLE_DATA.length; i++) {
            double[] row = KOK_TABLE_DATA[i];
            double[] meanAndError = MMRDRelation.weightedMeanAbsMag(
                    row[1], row[3], row[2], row[4]);

            double dKpc = MMRDNovaDistanceCalculator.calcDistance(
                    row[0], meanAndError[0], row[9]) / 1000;

            assertEquals(KOK_NOVA_NAMES[i] + ": D(2) from pipeline", row[10],
                    dKpc, paperDistanceErrors[i]);
        }
    }

    // Distance modulus identity: 5 log10(D/10) = m - Av - M.

    public void testDistanceModulusRoundTrip() {
        double[] apparentMags = { 2.0, 7.0, 10.5, 15.0 };
        double[] absoluteMags = { -10.804, -9.8, -7.7, -6.484 };
        double[] extinctions = { 0.0, 0.76, 1.69, 4.96 };

        for (double m : apparentMags) {
            for (double absMag : absoluteMags) {
                for (double av : extinctions) {
                    double d = MMRDNovaDistanceCalculator.calcDistance(m, absMag, av);
                    assertEquals(m - av - absMag, 5 * Math.log10(d / 10), 1e-9);
                }
            }
        }
    }

    // Exponential decline model tests: Kok (2010), equation (10),
    // mv = P1 - P2*exp(-P3*(t - t0)).

    public void testExponentialModelRecoversExactParameters() throws AlgorithmError {
        final double p1 = 16;
        final double p2 = 9;
        final double p3 = 0.05;
        final double t0 = 2455000;

        NovaExponentialModel model = new NovaExponentialModel(
                exponentialObs(p1, p2, p3, t0, 120, 2, 0));
        model.execute();

        assertEquals(p1, model.getP1(), 1e-4);
        assertEquals(p2, model.getP2(), 1e-4);
        assertEquals(p3, model.getP3(), 1e-6);
        assertEquals(p1 - p2, model.getFittedPeakMagnitude(), 1e-4);
        assertEquals(t0, model.getPeakJD(), 1e-9);

        // Closed-form decline times: t(delta) = ln(P2/(P2 - delta))/P3.
        // For these parameters: t2 = ln(9/7)/0.05, t3 = ln(9/6)/0.05,
        // i.e. a fast nova similar to V5583 Sgr.
        assertEquals(Math.log(9.0 / 7.0) / 0.05, model.getT2(), 1e-3);
        assertEquals(Math.log(9.0 / 6.0) / 0.05, model.getT3(), 1e-3);
    }

    public void testExponentialModelRecoversParametersWithNoise() throws AlgorithmError {
        final double p1 = 16;
        final double p2 = 9;
        final double p3 = 0.05;
        final double t0 = 2455000;

        NovaExponentialModel model = new NovaExponentialModel(
                exponentialObs(p1, p2, p3, t0, 120, 1, 0.05));
        model.execute();

        assertEquals(p1, model.getP1(), 0.1);
        assertEquals(p2, model.getP2(), 0.15);
        assertEquals(p3, model.getP3(), 0.05 * p3);

        double expectedT2 = Math.log(9.0 / 7.0) / 0.05;
        assertEquals(expectedT2, model.getT2(), 0.05 * expectedT2);
    }

    public void testExponentialModelDeclineFromReferenceMagnitude() throws AlgorithmError {
        final double p1 = 16;
        final double p2 = 9;
        final double p3 = 0.05;

        NovaExponentialModel model = new NovaExponentialModel(
                exponentialObs(p1, p2, p3, 2455000, 120, 2, 0));
        model.execute();

        // Decline measured from the fitted peak (7.0) and from a chart-read
        // maximum magnitude (7.5), per section 2.2 of Kok (2010):
        // t(delta) = ln(P2/(P1 - m0 - delta))/P3.
        assertEquals(Math.log(9.0 / 7.0) / 0.05, model.timeToDecline(2, 7.0), 1e-3);
        assertEquals(Math.log(9.0 / 6.5) / 0.05, model.timeToDecline(2, 7.5), 1e-3);

        // A maximum magnitude so faint that peak+delta exceeds the asymptote
        // is unreachable.
        assertNull(model.timeToDecline(2, 14.5));
    }

    public void testExponentialModelDeclineTimeUnreachable() throws AlgorithmError {
        // An outburst amplitude of 2.5 magnitudes never declines by 3.
        NovaExponentialModel model = new NovaExponentialModel(
                exponentialObs(9.5, 2.5, 0.05, 2455000, 120, 2, 0));
        model.execute();

        assertNotNull(model.getT2());
        assertNull(model.getT3());
    }

    // Light curve parameter extraction by interpolation.

    public void testExtractLightCurveParamsByInterpolation() {
        double[][] data = {
                { 2455000, 8.0 },
                { 2455001, 7.0 }, // peak
                { 2455003, 8.0 },
                { 2455005, 9.0 }, // t2 crossing exactly here: 4 days after peak
                { 2455007, 9.5 },
                { 2455009, 10.2 }, // t3 crossing interpolated between 9.5 and 10.2
                { 2455011, 10.4 } };

        LightCurveParams params = MMRDNovaDistanceCalculator
                .extractLightCurveParams(obsFromPairs(data));

        assertEquals(7.0, params.peakMag, 1e-9);
        assertEquals(2455001.0, params.peakJD, 1e-9);
        assertEquals(4.0, params.t2, 1e-9);

        // Interpolated t3: target 10.0 between (2455007, 9.5) and
        // (2455009, 10.2) => 2455007 + 2*(0.5/0.7) - 2455001.
        assertEquals(6.0 + 2 * (0.5 / 0.7), params.t3, 1e-9);
    }

    public void testExtractLightCurveParamsInsufficientDecline() {
        double[][] data = {
                { 2455000, 7.0 }, // peak
                { 2455005, 8.0 },
                { 2455010, 8.4 } };

        LightCurveParams params = MMRDNovaDistanceCalculator
                .extractLightCurveParams(obsFromPairs(data));

        assertEquals(7.0, params.peakMag, 1e-9);
        assertNull(params.t2);
        assertNull(params.t3);
    }

    // Helpers

    private List<ValidObservation> exponentialObs(double p1, double p2,
            double p3, double t0, int days, int stepDays, double noiseSigma) {
        List<ValidObservation> obs = new ArrayList<ValidObservation>();
        Random random = new Random(42);

        for (int day = 0; day <= days; day += stepDays) {
            double mag = p1 - p2 * Math.exp(-p3 * day);
            if (noiseSigma > 0) {
                mag += random.nextGaussian() * noiseSigma;
            }
            ValidObservation ob = new ValidObservation();
            ob.setJD(t0 + day);
            ob.setMagnitude(new Magnitude(mag, 0));
            obs.add(ob);
        }

        return obs;
    }

    private List<ValidObservation> obsFromPairs(double[][] jdAndMagPairs) {
        List<ValidObservation> obs = new ArrayList<ValidObservation>();

        for (double[] pair : jdAndMagPairs) {
            ValidObservation ob = new ValidObservation();
            ob.setJD(pair[0]);
            ob.setMagnitude(new Magnitude(pair[1], 0));
            obs.add(ob);
        }

        return obs;
    }
}
