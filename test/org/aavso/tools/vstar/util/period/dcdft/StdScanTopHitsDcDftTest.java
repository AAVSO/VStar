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

import org.aavso.tools.vstar.util.TCasData;

/**
 * DC DFT Standard Scan test case.
 * 
 * This is equivalent to running a "1: standard scan" from the AAVSO's TS
 * (t1201.f) Fortran program's Fourier analysis menu with the tcas.dat file
 * supplied with that program.
 * 
 * The 20 top-hits results obtained from Peranso 3.0.4.4
 *  
 */
public class StdScanTopHitsDcDftTest extends TopHitsDcDftTestBase {

	public StdScanTopHitsDcDftTest(String name) {
		super(name, TCasData.data);
	}

	// Valid test cases.

	public void testDcDftTopHits() {
		double[] expectedPeriods = { 435.7435, 365.0824, 13508.0482, 540.3219,
				643.2404, 201.6127, 221.4434, 1500.8942, 190.2542, 300.1788,
				254.8688, 146.8266, 275.6745, 794.5911, 329.4646, 139.2582,
				155.2649, 125.0745, 170.9880, 115.4534 };

		double[] expectedPowers = { 144.83, 7.36, 5.55, 4.80, 4.06, 3.34, 3.21,
				2.92, 2.87, 2.55, 1.72, 1.68, 1.50, 1.07, 1.05, 0.80, 0.52,
				0.43, 0.41, 0.34 };

		testDcDftTopHits(expectedPeriods, expectedPowers);
	}
}
