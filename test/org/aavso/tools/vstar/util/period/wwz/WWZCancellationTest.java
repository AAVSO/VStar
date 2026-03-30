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

import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.util.period.dcdft.DataTestBase;

/**
 * Verify cancellation semantics for threaded WWZ execution.
 */
public class WWZCancellationTest extends DataTestBase {

	private static final double[][] LARGE_DATA = buildLargeData();

	public WWZCancellationTest() {
		super("WWZ cancellation test", LARGE_DATA);
	}

	public void testInterruptMarksCancelledAndClearsResults() throws Exception {
		final WeightedWaveletZTransform wwt = new WeightedWaveletZTransform(obs, 0.0005, 50.0);
		wwt.setThreadCount(WeightedWaveletZTransform.getRecommendedThreadCount());
		wwt.make_freqs_from_period_range(20.0, 300.0, 0.5);

		Thread runThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					wwt.execute();
				} catch (Exception e) {
					// execute handles cancellation internally; no exception expected here
				}
			}
		});
		runThread.start();
		Thread.sleep(20);
		wwt.interrupt();
		runThread.join(10000);

		assertTrue("WWZ execution thread should terminate after interrupt", !runThread.isAlive());
		assertTrue("WWZ should report cancelled state", wwt.isCancelled());
		assertEquals("Cancelled run should not expose partial stats", 0, wwt.getStats().size());
		assertEquals("Cancelled run should not expose partial maximal stats", 0, wwt.getMaximalStats().size());
	}

	private static double[][] buildLargeData() {
		double[][] base = TUmi2420000To2425000Data.data;
		int repeats = 8;
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

