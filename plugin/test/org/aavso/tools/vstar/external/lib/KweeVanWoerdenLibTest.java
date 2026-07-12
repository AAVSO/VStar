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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.aavso.tools.vstar.exception.AlgorithmError;
import org.aavso.tools.vstar.util.Tolerance;

import junit.framework.TestCase;

/**
 * Unit tests for {@link KweeVanWoerdenLib}, including synthetic eclipses and
 * golden values from Deeg CM Dra example light curves.
 */
public class KweeVanWoerdenLibTest extends TestCase {

	private static final double RMS_CM_DRA = 0.00138;

	public KweeVanWoerdenLibTest(String name) {
		super(name);
	}

	public void testSyntheticParabolicMinimum() throws AlgorithmError {
		double tMid = 2459000.5;
		int n = 41;
		double dt = 0.001;
		double[] times = new double[n];
		double[] flux = new double[n];
		for (int i = 0; i < n; i++) {
			times[i] = tMid + (i - n / 2) * dt;
			double x = times[i] - tMid;
			flux[i] = 1.0 - 0.4 * Math.exp(-x * x / (2 * 0.008 * 0.008));
		}

		KweeVanWoerdenLib.Params params = new KweeVanWoerdenLib.Params();
		params.nfold = 5;
		params.t1Mode = KweeVanWoerdenLib.T1Mode.MIDPOINT;
		params.mu = 0.001;
		params.resampleIfNeeded = false;

		KweeVanWoerdenLib.Result result = KweeVanWoerdenLib.analyze(times, flux, params);

		assertTrue(Tolerance.areClose(tMid, result.t0, 5e-5, true));
		assertTrue(result.sigmaDeeg > 0 && !Double.isNaN(result.sigmaDeeg));
		assertFalse(result.equidistanceWarning);
	}

	public void testSyntheticMaximumViaEventType() throws AlgorithmError {
		double tMid = 2459001.0;
		int n = 51;
		double dt = 0.002;
		double[] times = new double[n];
		double[] mag = new double[n];
		for (int i = 0; i < n; i++) {
			times[i] = tMid + (i - n / 2) * dt;
			double x = times[i] - tMid;
			// Peak brightness = minimum magnitude
			mag[i] = 10.0 - 1.5 * Math.exp(-x * x / (2 * 0.012 * 0.012));
		}

		KweeVanWoerdenLib.Params params = new KweeVanWoerdenLib.Params();
		params.nfold = 5;
		params.t1Mode = KweeVanWoerdenLib.T1Mode.MIDPOINT;
		params.eventType = KweeVanWoerdenLib.EventType.MAXIMUM;
		params.mu = 0.01;

		KweeVanWoerdenLib.Result result = KweeVanWoerdenLib.analyze(times, mag, params);
		assertTrue(Tolerance.areClose(tMid, result.t0, 1e-4, true));
		assertTrue(result.sigmaDeeg > 0);
	}

	public void testCMDra7024Golden() throws Exception {
		double[][] lc = readLc("data/kvw/CMDra7024.lc");
		KweeVanWoerdenLib.Params params = goldenParams();
		KweeVanWoerdenLib.Result r = KweeVanWoerdenLib.analyze(lc[0], lc[1], params);

		assertTrue(Tolerance.areClose(58739.9291169, r.t0, 1e-7, true));
		assertTrue(Tolerance.areClose(0.0000125, r.sigmaDeeg, 5e-8, true));
		assertTrue(Double.isNaN(r.sigmaClassic));
	}

	public void testCMDra7023Golden() throws Exception {
		double[][] lc = readLc("data/kvw/CMDra7023.lc");
		KweeVanWoerdenLib.Params params = goldenParams();
		KweeVanWoerdenLib.Result r = KweeVanWoerdenLib.analyze(lc[0], lc[1], params);

		assertTrue(Tolerance.areClose(58738.6607358, r.t0, 1e-7, true));
		assertTrue(Tolerance.areClose(0.0000191, r.sigmaDeeg, 5e-8, true));
		assertTrue(Tolerance.areClose(0.0000662, r.sigmaClassic, 5e-7, true));
	}

	public void testTooFewPoints() {
		double[] t = { 1, 2, 3, 4, 5 };
		double[] v = { 1, 0.5, 0.2, 0.5, 1 };
		try {
			KweeVanWoerdenLib.analyze(t, v, new KweeVanWoerdenLib.Params());
			fail("Expected AlgorithmError");
		} catch (AlgorithmError e) {
			// expected
		}
	}

	private static KweeVanWoerdenLib.Params goldenParams() {
		KweeVanWoerdenLib.Params params = new KweeVanWoerdenLib.Params();
		params.nfold = 5;
		params.t1Mode = KweeVanWoerdenLib.T1Mode.EXTREMUM;
		params.mu = RMS_CM_DRA;
		params.resampleIfNeeded = false;
		params.eventType = KweeVanWoerdenLib.EventType.MINIMUM;
		return params;
	}

	private static double[][] readLc(String resourcePath) throws Exception {
		InputStream in = KweeVanWoerdenLibTest.class.getClassLoader().getResourceAsStream(resourcePath);
		if (in == null) {
			java.io.File f = new java.io.File("test/" + resourcePath);
			if (!f.exists()) {
				f = new java.io.File("plugin/test/" + resourcePath);
			}
			in = new java.io.FileInputStream(f);
		}
		List<Double> times = new ArrayList<Double>();
		List<Double> vals = new ArrayList<Double>();
		BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
		String line;
		while ((line = reader.readLine()) != null) {
			line = line.trim();
			if (line.isEmpty() || line.startsWith("#")) {
				continue;
			}
			String[] parts = line.split("\\s+");
			times.add(Double.parseDouble(parts[0]));
			vals.add(Double.parseDouble(parts[1]));
		}
		reader.close();
		double[] t = new double[times.size()];
		double[] v = new double[vals.size()];
		for (int i = 0; i < t.length; i++) {
			t[i] = times.get(i);
			v[i] = vals.get(i);
		}
		return new double[][] { t, v };
	}
}
