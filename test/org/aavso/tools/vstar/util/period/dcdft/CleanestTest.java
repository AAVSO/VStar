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

import org.aavso.tools.vstar.util.TCasData;
import org.aavso.tools.vstar.util.period.PeriodAnalysisCoordinateType;

/**
 * CLEANest unit test.
 * 
 * This is equivalent to running a "1: standard scan" from the AAVSO's TS
 * (t1201.f) Fortran program's Fourier analysis menu with the tcas.dat file
 * supplied with that program, followed by a CLEANest (option 8 from the Fourier
 * analysis menu) using the top-hit (period = 435.7435).
 * 
 * After CLEANest, the first 2 top-hits should be:
 * 
 * 1 437.0000 144.75 <br/>
 * 2 435.7435 144.40 <br/>
 * 
 * i.e. a new entry in the first position with the previous entries pushed down
 * one position.
 * 
 * The full table will be:
 * 
 * 1 437.0000 144.75 11 300.1788 2.54 <br/>
 * 2 435.7435 144.40 12 254.8688 1.72 <br/>
 * 3 365.0824 7.34 13 146.8266 1.67 <br/>
 * 4 13508.0482 5.54 14 275.6745 1.49 <br/>
 * 5 540.3219 4.78 15 794.5911 1.06 <br/>
 * 6 643.2404 4.05 16 329.4646 1.05 <br/>
 * 7 201.6127 3.33 17 139.2582 0.80 <br/>
 * 8 221.4434 3.20 18 155.2649 0.52 <br/>
 * 9 1500.8942 2.91 19 125.0745 0.43 <br/>
 * 10 190.2542 2.86 20 170.9880 0.41 <br/>
 */
public class CleanestTest extends TopHitsDcDftTestBase {

	public CleanestTest(String name) {
		super(name, TCasData.data);
	}

	// Valid test cases.

	// Apply CLEANest to the top-most hit of a standard scan of the T Cas data.
	public void testRefineFirstFreq() {
		// Perform a standard scan.
		TSDcDft dcdft = new TSDcDft(obs);
		dcdft.execute();

		// Apply CLEANest to top-hit frequency.
		double topFreq = dcdft.getTopHits().get(
				PeriodAnalysisCoordinateType.FREQUENCY).get(0);

		assertEquals("0.002295", String.format("%1.6f", topFreq));

		List<Double> freqs = new ArrayList<Double>();
		freqs.add(topFreq);

		dcdft.cleanest(freqs);

		// Now check the new top-hit period and power.
		double newTopPeriod = dcdft.getTopHits().get(
				PeriodAnalysisCoordinateType.PERIOD).get(0);

		double newTopPower = dcdft.getTopHits().get(
				PeriodAnalysisCoordinateType.POWER).get(0);

		assertEquals("437.0000", String.format("%1.4f", newTopPeriod));
		assertEquals("144.75", String.format("%1.2f", newTopPower));

		// Check the whole table.
		double[] expectedPeriods = { 437.0000, 435.7435, 365.0824, 13508.0482,
				540.3219, 643.2404, 201.6127, 221.4434, 1500.8942, 190.2542,
				300.1788, 254.8688, 146.8266, 275.6745, 794.5911, 329.4646,
				139.2582, 155.2649, 125.0745, 170.9880 };

		double[] expectedPowers = { 144.75, 144.40, 7.34, 5.54, 4.78, 4.05,
				3.33, 3.20, 2.91, 2.86, 2.54, 1.72, 1.67, 1.49, 1.06, 1.05,
				0.80, 0.52, 0.43, 0.41 };

		checkTopHitsTable(dcdft, expectedPeriods, expectedPowers);
	}
}
