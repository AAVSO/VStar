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
 * This is equivalent to running a standard scan from the AAVSO's TS (t1201.f)
 * Fortran program's Fourier analysis menu on the tcas.dat file supplied with
 * that program.
 * 
 * The 20 top-hits results obtained from that program (5. delete from table
 * option in the Fourier analysis menu):
 * 
 * 1 435.7435 144.40 11 254.8688 1.72 <br/>
 * 2 365.0824 7.34 12 146.8266 1.67 <br/>
 * 3 13508.0482 5.54 13 275.6745 1.49 <br/>
 * 4 540.3219 4.78 14 794.5911 1.06 <br/>
 * 5 643.2404 4.05 15 329.4646 1.05 <br/>
 * 6 201.6127 3.33 16 139.2582 0.80 <br/>
 * 7 221.4434 3.20 17 155.2649 0.52 <br/>
 * 8 1500.8942 2.91 18 125.0745 0.43 <br/>
 * 9 190.2542 2.86 19 170.9880 0.41 <br/>
 * 10 300.1788 2.54 20 115.4534 0.34 <br/>
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

		double[] expectedPowers = { 144.40, 7.34, 5.54, 4.78, 4.05, 3.33, 3.20,
				2.91, 2.86, 2.54, 1.72, 1.67, 1.49, 1.06, 1.05, 0.80, 0.52,
				0.43, 0.41, 0.34 };

		super.testDcDftTopHits(expectedPeriods, expectedPowers);
	}
}
