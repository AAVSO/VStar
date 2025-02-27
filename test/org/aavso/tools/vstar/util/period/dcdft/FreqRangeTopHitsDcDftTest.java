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
 * DC DFT Frequency Range test cases testing against resulting top-hits.
 * 
 * These are equivalent to choosing "2: frequency range" from the AAVSO's TS
 * (t1201.f) Fortran program's Fourier analysis menu with the tcas.dat file
 * supplied with that program.
 */
public class FreqRangeTopHitsDcDftTest extends TopHitsDcDftTestBase {

	public FreqRangeTopHitsDcDftTest(String name) {
		super(name, TCasData.data);
	}

	// Valid test cases.

	/**
	 * The default parameters for low and high frequency and resolution
	 * <em>as used by TS</em> are used.
	 * 
	 * Expected powers for top-hits are from Peranso 3.0.4.4   
	 */
	public void testTSActualParmsDefaultDcDftFrequencyRangeTopHits() {
		double[] expectedPeriods = { 442.8868, 370.0835, 9005.3655, 529.7274,
				628.2813, 219.6431, 203.1285, 188.9238, 1421.8998, 303.5516,
				252.4869, 147.6289, 272.8899, 333.5321, 771.8885, 139.9798,
				154.3777, 169.9126, 124.4981, 115.9489 };

		double[] expectedPowers = { 141.19, 8.01, 5.43, 5.17, 3.97, 3.27, 3.27,
				2.98, 2.83, 2.81, 1.69, 1.59, 1.38, 1.24, 1.05, 0.80, 0.58,
				0.45, 0.42, 0.33 };

		double loFreq = 0.00011104491029346862;
		double hiFreq = 0.024800029965541325;
		double resolution = 7.4029940195645749e-05;

		testDcDftTopHits(expectedPeriods, expectedPowers, loFreq, hiFreq,
				resolution);
	}

	/**
	 * The default parameters for low and high frequency and resolution
	 * <em>as displayed by TS</em> (to lower precision than actually used
	 * internally) are used.
	 * 
	 * Expected powers for top-hits are from Peranso 3.0.4.4
	 * 
	 */
	public void testTSDefaultDisplayedParamsDcDftFrequencyRangeTopHits() {
		double[] expectedPeriods = { 443.0660, 370.2332, 9009.0090, 529.9417,
				628.5355, 219.7319, 203.2107, 189.0002, 1422.4751, 303.6745,
				252.5890, 147.6887, 273.0003, 333.6670, 772.2008, 140.0364,
				154.4402, 169.9813, 124.5485, 115.9958 };

		double[] expectedPowers = { 140.93, 7.99, 5.43, 5.18, 3.98, 3.28, 3.25,
				2.99, 2.83, 2.81, 1.69, 1.57, 1.39, 1.23, 1.06, 0.79, 0.58,
				0.45, 0.43, 0.33 };

		double loFreq = 0.0001110;
		double hiFreq = 0.0248000;
		double resolution = 0.0000740;

		testDcDftTopHits(expectedPeriods, expectedPowers, loFreq, hiFreq,
				resolution);
	}
}
