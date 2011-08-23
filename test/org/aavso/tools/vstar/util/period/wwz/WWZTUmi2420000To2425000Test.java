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

import java.util.List;

import org.aavso.tools.vstar.util.period.dcdft.DataTestBase;

/**
 * Weighted Wavelet Z-Transform unit test using TUMi AID data from JD 2420000 to
 * 2425000.
 * 
 * See data classes in this package for more detail.
 */
public class WWZTUmi2420000To2425000Test extends DataTestBase {

	private static List<WWZStatistic> expectedStats;
	private static List<WWZStatistic> expectedMaximalStats;

	public WWZTUmi2420000To2425000Test() {
		super("WWZ TUMi 2420000 to 2425000 unit test",
				TUmi2420000To2425000Data.data);

		expectedStats = TUmi2420000To2425000ExpectedWWZ.getWWZStats();

		expectedMaximalStats = TUmi242000To2425000ExpectedMaximalWWZ
				.getWWZStats();
	}

	/**
	 * Apply WWZ algorithm to T UMi observations over a frequency range of
	 * 0.01..0.02 in 0.001 steps, with a decay constant of 0.01.
	 */
	public void testWWZTUmi() {
		double minFreq = 0.01;
		double maxFreq = 0.02;
		double deltaFreq = 0.001;
		double decay = 0.01;

		try {
			WeightedWaveletZTransform wwt = new WeightedWaveletZTransform(obs,
					minFreq, maxFreq, deltaFreq, decay);

			wwt.execute();

			List<WWZStatistic> stats = wwt.getStats();
			List<WWZStatistic> maximalStats = wwt.getMaximalStats();

			assertEquals(expectedStats.size(), stats.size());
			checkWWZStats(expectedStats, stats);

			assertEquals(expectedMaximalStats.size(), maximalStats.size());
			checkWWZStats(expectedMaximalStats, maximalStats);
		} catch (Exception e) {
			fail();
		}
	}

	// Helpers

	private void checkWWZStats(List<WWZStatistic> expectedStats,
			List<WWZStatistic> actualStats) {
		for (int i = 0; i < expectedStats.size(); i++) {
			WWZStatistic expected = expectedStats.get(i);
			WWZStatistic actual = actualStats.get(i);

			assertEquals(String.format("%1.4f", expected.getTau()), String
					.format("%1.4f", actual.getTau()));

			assertEquals(String.format("%1.4f", expected.getFrequency()), String
					.format("%1.4f", actual.getFrequency()));

			assertEquals(String.format("%1.4f", expected.getWwz()), String
					.format("%1.4f", actual.getWwz()));

			assertEquals(String.format("%1.4f", expected.getAmplitude()), String
					.format("%1.4f", actual.getAmplitude()));

			assertEquals(String.format("%1.4f", expected.getMave()), String
					.format("%1.4f", actual.getMave()));

			assertEquals(String.format("%1.4f", expected.getNeff()), String
					.format("%1.4f", actual.getNeff()));
		}

	}
}
