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
package org.aavso.tools.vstar.util.period.dcdft;

import java.util.ArrayList;
import java.util.List;

import org.aavso.tools.vstar.exception.AlgorithmError;
import org.aavso.tools.vstar.util.TCasData;
import org.aavso.tools.vstar.util.period.PeriodAnalysisCoordinateType;

/**
 * CLEANest unit tests.
 * 
 * We test as input:
 * 
 * 1. The top-most frequency. 2. The two top-most frequencies.
 * 
 */
public class CleanestTest extends TopHitsDcDftTestBase {

	public CleanestTest(String name) {
		super(name, TCasData.data);
	}

	/**
	 * Apply CLEANest to the top-most hit of a standard scan of the T Cas data.
	 * 
	 * This is equivalent to running a "1: standard scan" from the AAVSO's TS
	 * (t1201.f) Fortran program's Fourier analysis menu with the tcas.dat file
	 * supplied with that program, followed by a CLEANest (option 8 from the
	 * Fourier analysis menu) using the top-hit (period: 435.7435).
	 * 
	 * After CLEANest, the first 2 top-hits should be:
	 * 
	 * 1 437.0000 145.18 <br/>
	 * 2 435.7435 144.83 <br/>
	 * 
	 * i.e. a new entry in the first position with the previous entries pushed
	 * down one position.
	 *
	 * expectedPowers are taken from Peranso 3.0.4.4
	 * 
	 */
	public void testRefineFirstFreq() {
		// Perform a standard scan.
		TSDcDft dcdft = new TSDcDft(obs);
		try {
			dcdft.execute();

			// Apply CLEANest to top-hit frequency.
			double topFreq = dcdft.getTopHits().get(
					PeriodAnalysisCoordinateType.FREQUENCY).get(0);

			assertEquals("0.002295", String.format("%1.6f", topFreq));

			List<Double> freqs = new ArrayList<Double>();
			freqs.add(topFreq);

			try {
				dcdft.cleanest(freqs, null, null);
			} catch (InterruptedException e) {
				// We should never end up here in the course of this unit test
				// (no user in the loop).
				fail();
			}

			// Now check the new top-hit period and power.
			double newTopPeriod = dcdft.getTopHits().get(
					PeriodAnalysisCoordinateType.PERIOD).get(0);

			double newTopPower = dcdft.getTopHits().get(
					PeriodAnalysisCoordinateType.POWER).get(0);

			assertEquals("437.0000", String.format("%1.4f", newTopPeriod));
			assertEquals("145.18", String.format("%1.2f", newTopPower));

			// Check the whole table.
			double[] expectedPeriods = { 437.0000, 435.7435, 365.0824,
					13508.0482, 540.3219, 643.2404, 201.6127, 221.4434,
					1500.8942, 190.2542, 300.1788, 254.8688, 146.8266,
					275.6745, 794.5911, 329.4646, 139.2582, 155.2649, 125.0745,
					170.9880 };

			double[] expectedPowers = { 145.18, 144.83, 7.36, 5.55, 4.80, 4.06,
					3.34, 3.21, 2.92, 2.87, 2.55, 1.72, 1.68, 1.50, 1.07, 1.05,
					0.80, 0.52, 0.43, 0.41 };

			checkTopHitsTable(dcdft, expectedPeriods, expectedPowers);
		} catch (AlgorithmError e) {
			fail();
		}
	}

	/**
	 * Same as testRefineFirstFreq() with empty lists instead of nulls for
	 * variable and locked period parameters.
	 */
	public void testRefineFirstFreqWithEmptyVarAndLockedPeriods() {
		// Perform a standard scan.
		TSDcDft dcdft = new TSDcDft(obs);
		try {
			dcdft.execute();

			// Apply CLEANest to top-hit frequency.
			double topFreq = dcdft.getTopHits().get(
					PeriodAnalysisCoordinateType.FREQUENCY).get(0);

			assertEquals("0.002295", String.format("%1.6f", topFreq));

			List<Double> freqs = new ArrayList<Double>();
			freqs.add(topFreq);

			try {
				dcdft.cleanest(freqs, new ArrayList<Double>(),
						new ArrayList<Double>());
			} catch (InterruptedException e) {
				// We should never end up here in the course of this unit test
				// (no user in the loop).
				fail();
			}

			// Now check the new top-hit period and power.
			double newTopPeriod = dcdft.getTopHits().get(
					PeriodAnalysisCoordinateType.PERIOD).get(0);

			double newTopPower = dcdft.getTopHits().get(
					PeriodAnalysisCoordinateType.POWER).get(0);

			assertEquals("437.0000", String.format("%1.4f", newTopPeriod));
			assertEquals("145.18", String.format("%1.2f", newTopPower));

			// Check the whole table.
			double[] expectedPeriods = { 437.0000, 435.7435, 365.0824,
					13508.0482, 540.3219, 643.2404, 201.6127, 221.4434,
					1500.8942, 190.2542, 300.1788, 254.8688, 146.8266,
					275.6745, 794.5911, 329.4646, 139.2582, 155.2649, 125.0745,
					170.9880 };

			double[] expectedPowers = { 145.18, 144.83, 7.36, 5.55, 4.80, 4.06,
					3.34, 3.21, 2.92, 2.87, 2.55, 1.72, 1.68, 1.50, 1.07, 1.05,
					0.80, 0.52, 0.43, 0.41 };

			checkTopHitsTable(dcdft, expectedPeriods, expectedPowers);
		} catch (AlgorithmError e) {
			fail();
		}
	}

	/**
	 * Apply CLEANest to the 2 top-most hits of a standard scan of the T Cas
	 * data.
	 * 
	 * This is equivalent to running a "1: standard scan" from the AAVSO's TS
	 * (t1201.f) Fortran program's Fourier analysis menu with the tcas.dat file
	 * supplied with that program, followed by a CLEANest (option 8 from the
	 * Fourier analysis menu) using 2 the top-hits (periods: 435.7435,
	 * 365.0824).
	 * 
	 * After CLEANest, the first 4 top-hits should be:
	 * 
	 * 1 438.0000 146.15 <br/>
	 * 2 381.0000 146.15 <br/>
	 * 3 435.7435 144.83 <br/>
	 * 4 365.0824 7.36 <br/>
	 * 
	 * i.e. two new entries in the first two positions with the same power
	 * values and the previous entries pushed down.
	 * 
	 * expectedPowers are taken from Peranso 3.0.4.4
	 * 
	 */
	public void testRefineTwoFreqs() {
		// Perform a standard scan.
		TSDcDft dcdft = new TSDcDft(obs);
		try {
			dcdft.execute();

			// Apply CLEANest to topmost two top-hit frequencies.
			double topFreq1 = dcdft.getTopHits().get(
					PeriodAnalysisCoordinateType.FREQUENCY).get(0);
			assertEquals("0.002295", String.format("%1.6f", topFreq1));

			double topFreq2 = dcdft.getTopHits().get(
					PeriodAnalysisCoordinateType.FREQUENCY).get(1);
			assertEquals("0.002739", String.format("%1.6f", topFreq2));

			List<Double> freqs = new ArrayList<Double>();
			freqs.add(topFreq1);
			freqs.add(topFreq2);

			try {
				dcdft.cleanest(freqs, null, null);
			} catch (InterruptedException e) {
				// We should never end up here in the course of this unit test
				// (no user in the loop).
				fail();
			}

			// Now check the new first few top-hits (period and power).

			double[] expectedPeriods = { 438.0000, 381.0000, 435.7435, 365.0824 };

			double[] expectedPowers = { 146.15, 146.15, 144.83, 7.36 };

			for (int i = 0; i < 3; i++) {
				double period = dcdft.getTopHits().get(
						PeriodAnalysisCoordinateType.PERIOD).get(i);
				double power = dcdft.getTopHits().get(
						PeriodAnalysisCoordinateType.POWER).get(i);

				assertEquals(String.format("%1.4f", period), String.format(
						"%1.4f", expectedPeriods[i]));

				assertEquals(String.format("%1.2f", power), String.format(
						"%1.2f", expectedPowers[i]));
			}
		} catch (AlgorithmError e) {
			fail();
		}
	}

	/**
	 * Apply CLEANest to the top-most hit of a standard scan of the T Cas data
	 * and a single variable period.
	 * 
	 * This is equivalent to running a "1: standard scan" from the AAVSO's TS
	 * (t1201.f) Fortran program's Fourier analysis menu with the tcas.dat file
	 * supplied with that program, followed by a CLEANest (option 8 from the
	 * Fourier analysis menu) using the top-hit (period: 435.7435) and a
	 * variable period (123.5).
	 * 
	 * After CLEANest, the first 4 top-hits should be:
	 * 
	 * 1 438.0000 146.26 <br/>
	 * 2 247.0000 146.26 <br/>
	 * 3 435.7435 144.83 <br/>
	 * 4 365.0824 7.36 <br/>
	 * 
	 * i.e. two new entries in the first two positions with the same power
	 * values and the previous entries pushed down.
	 */
	public void testRefineOneFreqAndOneVariablePeriod() {
		// Perform a standard scan.
		TSDcDft dcdft = new TSDcDft(obs);
		try {
			dcdft.execute();

			// Apply CLEANest to topmost two top-hit frequencies.
			double topFreq1 = dcdft.getTopHits().get(
					PeriodAnalysisCoordinateType.FREQUENCY).get(0);
			assertEquals("0.002295", String.format("%1.6f", topFreq1));

			List<Double> freqs = new ArrayList<Double>();
			freqs.add(topFreq1);

			List<Double> varPeriods = new ArrayList<Double>();
			varPeriods.add(123.5);

			try {
				dcdft.cleanest(freqs, varPeriods, null);
			} catch (InterruptedException e) {
				// We should never end up here in the course of this unit test
				// (no user in the loop).
				fail();
			}

			// Now check the new first few top-hits (period and power).

			double[] expectedPeriods = { 438.0000, 247.0000, 435.7435, 365.0824 };

			double[] expectedPowers = { 146.26, 146.26, 144.83, 7.36 };

			for (int i = 0; i < 3; i++) {
				double period = dcdft.getTopHits().get(
						PeriodAnalysisCoordinateType.PERIOD).get(i);
				double power = dcdft.getTopHits().get(
						PeriodAnalysisCoordinateType.POWER).get(i);

				assertEquals(String.format("%1.4f", period), String.format(
						"%1.4f", expectedPeriods[i]));

				assertEquals(String.format("%1.2f", power), String.format(
						"%1.2f", expectedPowers[i]));
			}
		} catch (AlgorithmError e) {
			fail();
		}
	}

	/**
	 * Apply CLEANest to the top-most hit of a standard scan of the T Cas data
	 * and a single locked period.
	 * 
	 * This is equivalent to running a "1: standard scan" from the AAVSO's TS
	 * (t1201.f) Fortran program's Fourier analysis menu with the tcas.dat file
	 * supplied with that program, followed by a CLEANest (option 8 from the
	 * Fourier analysis menu) using the top-hit (period: 435.7435) and a locked
	 * period (123.5).
	 * 
	 * After CLEANest, the first 4 top-hits should be:
	 * 
	 * 1 437.0000 145.25 <br/>
	 * 2 123.5000 145.25 <br/>
	 * 3 435.7435 144.83 <br/>
	 * 4 365.0824 7.36 <br/>
	 * 
	 * i.e. two new entries in the first two positions with the same power
	 * values and the previous entries pushed down.
	 */
	public void testRefineOneFreqAndOneLockedPeriod() {
		// Perform a standard scan.
		TSDcDft dcdft = new TSDcDft(obs);
		try {
			dcdft.execute();

			// Apply CLEANest to topmost two top-hit frequencies.
			double topFreq1 = dcdft.getTopHits().get(
					PeriodAnalysisCoordinateType.FREQUENCY).get(0);
			assertEquals("0.002295", String.format("%1.6f", topFreq1));

			List<Double> freqs = new ArrayList<Double>();
			freqs.add(topFreq1);

			List<Double> lockedPeriods = new ArrayList<Double>();
			lockedPeriods.add(123.5);

			try {
				dcdft.cleanest(freqs, null, lockedPeriods);
			} catch (InterruptedException e) {
				// We should never end up here in the course of this unit test
				// (no user in the loop).
				fail();
			}

			// Now check the new first few top-hits (period and power).

			double[] expectedPeriods = { 437.0000, 123.5000, 435.7435, 365.0824 };

			double[] expectedPowers = { 145.25, 145.25, 144.83, 7.36 };

			for (int i = 0; i < 3; i++) {
				double period = dcdft.getTopHits().get(
						PeriodAnalysisCoordinateType.PERIOD).get(i);
				double power = dcdft.getTopHits().get(
						PeriodAnalysisCoordinateType.POWER).get(i);

				assertEquals(String.format("%1.4f", period), String.format(
						"%1.4f", expectedPeriods[i]));

				assertEquals(String.format("%1.2f", power), String.format(
						"%1.2f", expectedPowers[i]));
			}
		} catch (AlgorithmError e) {
			fail();
		}
	}

	// TODO:
	// - a test for both locked and variable periods
}
