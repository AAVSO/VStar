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
 * Verify that multi-threaded WWZ execution matches single-threaded results.
 */
public class WWZThreadedConsistencyTest extends DataTestBase {

	private static final double[][] LARGE_DATA = buildLargeData();

	public WWZThreadedConsistencyTest() {
		super("WWZ threaded consistency test", LARGE_DATA);
	}

	public void testThreadedMatchesSingleThread() throws Exception {
		double minPeriod = 20.0;
		double maxPeriod = 300.0;
		double deltaPeriod = 0.5;
		double decay = 0.0005;
		double timeDivisions = 50.0;

		WeightedWaveletZTransform oneThread = new WeightedWaveletZTransform(obs, decay, timeDivisions);
		oneThread.setThreadCount(1);
		oneThread.make_freqs_from_period_range(minPeriod, maxPeriod, deltaPeriod);
		oneThread.execute();

		WeightedWaveletZTransform manyThreads = new WeightedWaveletZTransform(obs, decay, timeDivisions);
		manyThreads.setThreadCount(WeightedWaveletZTransform.getRecommendedThreadCount());
		manyThreads.make_freqs_from_period_range(minPeriod, maxPeriod, deltaPeriod);
		manyThreads.execute();

		List<WWZStatistic> a = oneThread.getStats();
		List<WWZStatistic> b = manyThreads.getStats();
		assertEquals(a.size(), b.size());
		for (int i = 0; i < a.size(); i++) {
			assertEquals(a.get(i).getTau(), b.get(i).getTau(), 1e-9);
			assertEquals(a.get(i).getFrequency(), b.get(i).getFrequency(), 1e-9);
			assertEquals(a.get(i).getWwz(), b.get(i).getWwz(), 1e-9);
			assertEquals(a.get(i).getSemiAmplitude(), b.get(i).getSemiAmplitude(), 1e-9);
			assertEquals(a.get(i).getMave(), b.get(i).getMave(), 1e-9);
			assertEquals(a.get(i).getNeff(), b.get(i).getNeff(), 1e-9);
		}

		List<WWZStatistic> ma = oneThread.getMaximalStats();
		List<WWZStatistic> mb = manyThreads.getMaximalStats();
		assertEquals(ma.size(), mb.size());
		for (int i = 0; i < ma.size(); i++) {
			assertEquals(ma.get(i).getTau(), mb.get(i).getTau(), 1e-9);
			assertEquals(ma.get(i).getFrequency(), mb.get(i).getFrequency(), 1e-9);
			assertEquals(ma.get(i).getWwz(), mb.get(i).getWwz(), 1e-9);
			assertEquals(ma.get(i).getSemiAmplitude(), mb.get(i).getSemiAmplitude(), 1e-9);
			assertEquals(ma.get(i).getMave(), mb.get(i).getMave(), 1e-9);
			assertEquals(ma.get(i).getNeff(), mb.get(i).getNeff(), 1e-9);
		}
	}

	private static double[][] buildLargeData() {
		double[][] base = TUmi2420000To2425000Data.data;
		int repeats = 6; // >1000 points to exercise threaded path heuristics
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

