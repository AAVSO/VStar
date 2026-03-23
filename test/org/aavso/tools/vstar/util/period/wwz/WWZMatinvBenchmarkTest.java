/**
 * VStar: a statistical analysis tool for variable star data.
 * Copyright (C) 2010  AAVSO (http://www.aavso.org/)
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
package org.aavso.tools.vstar.util.period.wwz;

import org.aavso.tools.vstar.util.period.dcdft.DataTestBase;

/**
 * Benchmark to quantify speedup of closed-form 3×3 matinv over legacy Gauss-Jordan.
 * Same scenario as WWZTUmi2420000To2425000Test; runs multiple iterations and prints times.
 */
public class WWZMatinvBenchmarkTest extends DataTestBase {

    private static final int WARMUP = 1;
    private static final int ITERATIONS = 5;

    public WWZMatinvBenchmarkTest() {
        super("WWZ matinv benchmark", TUmi2420000To2425000Data.data);
    }

    /**
     * Run WWZ with new (closed-form) matinv then with legacy (Gauss-Jordan), report total times and speedup.
     */
    public void testMatinvBenchmark() throws Exception {
        double minFreq = 0.01;
        double maxFreq = 0.02;
        double deltaFreq = 0.001;
        double decay = 0.01;
        double timeDivisions = 50.0;

        // Warmup and time: closed-form (new) matinv
        WeightedWaveletZTransform.useLegacyMatinv = false;
        runWarmup(obs, decay, timeDivisions, minFreq, maxFreq, deltaFreq);
        long newNs = runIterations(obs, decay, timeDivisions, minFreq, maxFreq, deltaFreq, ITERATIONS);

        // Warmup and time: legacy Gauss-Jordan matinv
        WeightedWaveletZTransform.useLegacyMatinv = true;
        runWarmup(obs, decay, timeDivisions, minFreq, maxFreq, deltaFreq);
        long legacyNs = runIterations(obs, decay, timeDivisions, minFreq, maxFreq, deltaFreq, ITERATIONS);

        WeightedWaveletZTransform.useLegacyMatinv = false;

        double newMs = newNs / 1_000_000.0;
        double legacyMs = legacyNs / 1_000_000.0;
        double speedup = (double) legacyNs / (double) newNs;

        System.out.println("WWZ matinv benchmark (TUMi 2420000–2425000, " + ITERATIONS + " runs each):");
        System.out.println("  Closed-form (new) matinv: " + String.format("%.2f", newMs) + " ms total");
        System.out.println("  Legacy Gauss-Jordan     : " + String.format("%.2f", legacyMs) + " ms total");
        System.out.println("  Speedup (legacy/new): " + String.format("%.2fx", speedup));
    }

    private void runWarmup(java.util.List<org.aavso.tools.vstar.data.ValidObservation> obs,
            double decay, double timeDivisions, double minFreq, double maxFreq, double deltaFreq) throws Exception {
        for (int i = 0; i < WARMUP; i++) {
            WeightedWaveletZTransform wwt = new WeightedWaveletZTransform(obs, decay, timeDivisions);
            wwt.setThreadCount(1);
            wwt.make_freqs_from_freq_range(minFreq, maxFreq, deltaFreq);
            wwt.execute();
        }
    }

    private long runIterations(java.util.List<org.aavso.tools.vstar.data.ValidObservation> obs,
            double decay, double timeDivisions, double minFreq, double maxFreq, double deltaFreq, int n) throws Exception {
        long start = System.nanoTime();
        for (int i = 0; i < n; i++) {
            WeightedWaveletZTransform wwt = new WeightedWaveletZTransform(obs, decay, timeDivisions);
            wwt.setThreadCount(1);
            wwt.make_freqs_from_freq_range(minFreq, maxFreq, deltaFreq);
            wwt.execute();
        }
        return System.nanoTime() - start;
    }
}
