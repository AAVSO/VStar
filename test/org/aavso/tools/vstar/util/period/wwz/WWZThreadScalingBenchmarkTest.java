/**
 * VStar: a statistical analysis tool for variable star data.
 * Copyright (C) 2010  AAVSO (http://www.aavso.org/)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 */
package org.aavso.tools.vstar.util.period.wwz;

import java.util.List;

import org.aavso.tools.vstar.util.period.dcdft.DataTestBase;

/**
 * Benchmark WWZ runtime scaling across thread counts.
 */
public class WWZThreadScalingBenchmarkTest extends DataTestBase {

	private static final double[][] LARGE_DATA = buildLargeData();

	public WWZThreadScalingBenchmarkTest() {
		super("WWZ thread scaling benchmark", LARGE_DATA);
	}

	public void testThreadScalingBenchmark() throws Exception {
		int[] threadCounts = new int[] { 1, 2, 4, 8, WeightedWaveletZTransform.getRecommendedThreadCount() };
		double minPeriod = 20.0;
		double maxPeriod = 300.0;
		double deltaPeriod = 0.5;
		double decay = 0.0005;
		double timeDivisions = 50.0;
		int iterations = 3;

		double baselineMs = -1.0;
		System.out.println("WWZ thread scaling benchmark (synthetic >1000 obs, " + iterations + " runs each):");
		for (int tc : threadCounts) {
			long t0 = System.nanoTime();
			for (int i = 0; i < iterations; i++) {
				WeightedWaveletZTransform wwt = new WeightedWaveletZTransform(obs, decay, timeDivisions);
				wwt.setThreadCount(tc);
				wwt.make_freqs_from_period_range(minPeriod, maxPeriod, deltaPeriod);
				wwt.execute();
				List<WWZStatistic> stats = wwt.getStats();
				assertTrue(stats.size() > 0);
			}
			double totalMs = (System.nanoTime() - t0) / 1_000_000.0;
			if (baselineMs < 0.0) {
				baselineMs = totalMs;
			}
			double speedup = baselineMs / totalMs;
			System.out.println("  threads=" + tc + " total=" + String.format("%.2f", totalMs) + " ms speedup="
					+ String.format("%.2fx", speedup));
		}
	}

	private static double[][] buildLargeData() {
		double[][] base = TUmi2420000To2425000Data.data;
		int repeats = 6;
		double[][] out = new double[base.length * repeats][2];
		double span = 5000.0;
		int p = 0;
		for (int r = 0; r < repeats; r++) {
			for (int i = 0; i < base.length; i++) {
				out[p][0] = base[i][0] + r * span;
				out[p][1] = base[i][1];
				p++;
			}
		}
		return out;
	}
}

