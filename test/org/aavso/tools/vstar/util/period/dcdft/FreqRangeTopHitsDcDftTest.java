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
 * DC DFT Frequency Range test case.
 * 
 * This is equivalent to choosing "2: frequency range" from the AAVSO's TS
 * (t1201.f) Fortran program's Fourier analysis menu with the tcas.dat file
 * supplied with that program. The default parameters for low and high frequency
 * and resolution shown by the program are used.
 * 
 * The 20 top-hits results obtained from that program ("5: delete from table"
 * option in the Fourier analysis menu) are then:
 * 
 * 1 442.8868 140.77 11 252.4869 1.68 <br/>
 * 2 370.0835 7.98 12 147.6289 1.58 <br/>
 * 3 9005.3655 5.41 13 272.8899 1.38 <br/>
 * 4 529.7274 5.15 14 333.5321 1.23 <br/>
 * 5 628.2813 3.96 15 771.8885 1.05 <br/>
 * 6 219.6431 3.26 16 139.9798 0.79 <br/>
 * 7 203.1285 3.26 17 154.3777 0.57 <br/>
 * 8 188.9238 2.98 18 169.9126 0.45 <br/>
 * 9 1421.8998 2.82 19 124.4981 0.42 <br/>
 * 10 303.5516 2.81 20 115.9489 0.33 <br/>
 */
public class FreqRangeTopHitsDcDftTest extends TopHitsDcDftTestBase {

	public FreqRangeTopHitsDcDftTest(String name) {
		super(name, TCasData.data);
	}

	// Valid test cases.

	public void testDefaultDcDftFrequencyRangeTopHits() {
		double[] expectedPeriods = { 442.8868, 370.0835, 9005.3655, 529.7274,
				628.2813, 219.6431, 203.1285, 188.9238, 1421.8998, 303.5516,
				252.4869, 147.6289, 272.8899, 333.5321, 771.8885, 139.9798,
				154.3777, 169.9126, 124.4981, 115.9489 };

		double[] expectedPowers = { 140.77, 7.98, 5.41, 5.15, 3.96, 3.26, 3.26,
				2.98, 2.82, 2.81, 1.68, 1.58, 1.38, 1.23, 1.05, 0.79, 0.57,
				0.45, 0.42, 0.33 };

//		double loFreq = 0.0001110;
//		double hiFreq = 0.0248000;
//		double resolution = 0.0000740;

		double loFreq = 0.00011104491029346862;
		double hiFreq = 0.024800029965541325;
		double resolution = 7.4029940195645749e-05;
		
		testDcDftTopHits(expectedPeriods, expectedPowers, loFreq, hiFreq,
				resolution);
	}
	
	public void testDcDftFrequencyRangeTopHits() {

	}
}
