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
import java.util.Map;

import org.aavso.tools.vstar.data.Magnitude;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.exception.AlgorithmError;
import org.aavso.tools.vstar.external.plugin.AoVPeriodSearch.AoVAlgorithm;
import org.aavso.tools.vstar.util.period.PeriodAnalysisCoordinateType;

import junit.framework.TestCase;

/**
 * Algorithm-focused unit tests for {@link AoVPeriodSearch} (no UI).
 */
public class AoVPeriodSearchTest extends TestCase {

	private static final double TRUE_PERIOD = 2.0;
	private static final double JD0 = 2459000.0;

	public AoVPeriodSearchTest(String name) {
		super(name);
	}

	public void testResultSeriesAlignedAndFinite() throws AlgorithmError {
		AoVPeriodSearch plugin = new AoVPeriodSearch();
		plugin.setSearchParametersForTest(1.5, 2.5, 0.1, 10);
		AoVAlgorithm alg = plugin.new AoVAlgorithm(sineWaveObs(TRUE_PERIOD, 80, 0.05));
		alg.execute();

		Map<PeriodAnalysisCoordinateType, List<Double>> series = alg
				.getResultSeries();
		List<Double> periods = series.get(PeriodAnalysisCoordinateType.PERIOD);
		List<Double> freqs = series.get(PeriodAnalysisCoordinateType.FREQUENCY);
		List<Double> fStats = seriesByDescription(series, "F-statistic");
		List<Double> pVals = seriesByDescription(series, "p-value");

		assertNotNull(periods);
		assertEquals(periods.size(), freqs.size());
		assertEquals(periods.size(), fStats.size());
		assertEquals(periods.size(), pVals.size());
		assertEquals(expectedPeriodCount(1.5, 2.5, 0.1), periods.size());

		for (int i = 0; i < periods.size(); i++) {
			assertFalse(Double.isInfinite(periods.get(i)));
			assertFalse(Double.isInfinite(freqs.get(i)));
			assertFalse(Double.isInfinite(fStats.get(i)));
			assertFalse(Double.isInfinite(pVals.get(i)));
			assertEquals(1.0 / periods.get(i), freqs.get(i), 1e-9);
		}
	}

	public void testTopHitsOrderedByDescendingFAndPruned()
			throws AlgorithmError {
		AoVPeriodSearch plugin = new AoVPeriodSearch();
		// Many steps so prune (>20) is exercised.
		plugin.setSearchParametersForTest(1.0, 4.0, 0.05, 10);
		AoVAlgorithm alg = plugin.new AoVAlgorithm(sineWaveObs(TRUE_PERIOD, 100, 0.04));
		alg.execute();

		Map<PeriodAnalysisCoordinateType, List<Double>> topHits = alg.getTopHits();
		List<Double> fStats = seriesByDescription(topHits, "F-statistic");
		List<Double> periods = topHits.get(PeriodAnalysisCoordinateType.PERIOD);

		assertNotNull(fStats);
		assertTrue(fStats.size() <= 20);
		assertEquals(fStats.size(), periods.size());
		for (int i = 1; i < fStats.size(); i++) {
			double prev = fStats.get(i - 1);
			double cur = fStats.get(i);
			if (!Double.isNaN(prev) && !Double.isNaN(cur)) {
				assertTrue(prev + " >= " + cur, prev >= cur - 1e-12);
			}
		}
	}

	public void testPeriodScanRecoversSyntheticPeriod() throws AlgorithmError {
		AoVPeriodSearch plugin = new AoVPeriodSearch();
		plugin.setSearchParametersForTest(1.5, 2.5, 0.05, 12);
		AoVAlgorithm alg = plugin.new AoVAlgorithm(sineWaveObs(TRUE_PERIOD, 120, 0.04));
		alg.execute();

		List<Double> periods = alg.getTopHits()
				.get(PeriodAnalysisCoordinateType.PERIOD);
		assertNotNull(periods);
		assertFalse(periods.isEmpty());
		assertEquals("best AoV period should be near synthetic period",
				TRUE_PERIOD, periods.get(0), 0.1);
	}

	public void testFlatLightCurveYieldsFiniteSeries() throws AlgorithmError {
		List<ValidObservation> obs = new ArrayList<ValidObservation>();
		for (int i = 0; i < 40; i++) {
			ValidObservation ob = new ValidObservation();
			ob.setJD(JD0 + i * 0.1);
			ob.setMagnitude(new Magnitude(10.0, 0.01));
			obs.add(ob);
		}

		AoVPeriodSearch plugin = new AoVPeriodSearch();
		plugin.setSearchParametersForTest(1.0, 2.0, 0.2, 8);
		AoVAlgorithm alg = plugin.new AoVAlgorithm(obs);
		alg.execute();

		List<Double> fStats = seriesByDescription(alg.getResultSeries(),
				"F-statistic");
		assertFalse(fStats.isEmpty());
		for (Double f : fStats) {
			assertFalse(Double.isInfinite(f));
		}
	}

	private static int expectedPeriodCount(double min, double max, double res) {
		int n = 0;
		for (double p = min; p <= max; p += res) {
			n++;
		}
		return n;
	}

	private static List<Double> seriesByDescription(
			Map<PeriodAnalysisCoordinateType, List<Double>> series,
			String description) {
		for (Map.Entry<PeriodAnalysisCoordinateType, List<Double>> e : series
				.entrySet()) {
			if (description.equals(e.getKey().getDescription())) {
				return e.getValue();
			}
		}
		return null;
	}

	private static List<ValidObservation> sineWaveObs(double period, int n,
			double dt) {
		List<ValidObservation> obs = new ArrayList<ValidObservation>();
		for (int i = 0; i < n; i++) {
			double jd = JD0 + i * dt;
			double mag = 10.0
					+ 0.5 * Math.sin(2.0 * Math.PI * (jd - JD0) / period);
			ValidObservation ob = new ValidObservation();
			ob.setJD(jd);
			ob.setMagnitude(new Magnitude(mag, 0.01));
			obs.add(ob);
		}
		return obs;
	}
}
