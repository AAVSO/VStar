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
package org.aavso.tools.vstar.util.date;

import junit.framework.TestCase;

import org.aavso.tools.vstar.util.coords.DecInfo;
import org.aavso.tools.vstar.util.coords.EpochType;
import org.aavso.tools.vstar.util.coords.RAInfo;

/**
 * J2000HJDConverter unit tests.
 */
public class J2000EpochHJDConverterTest extends TestCase {

	private final static double MEEUS_EX24a_JD = 2448908.5;
	private final static double JD2 = 2457501.86733;
	
	// Singleton HJD converter instance.
	private J2000HJDConverter converter;

	public J2000EpochHJDConverterTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		converter = (J2000HJDConverter) AbstractHJDConverter
				.getInstance(EpochType.J2000);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	// Test cases

	public void testSecsToRadsPos1() {
		// From p 135 of Meeus
		double secs = 9.20;
		double rads = converter.secsToRads(secs);
		assertEquals("0.000044603", getNumToPrecision(rads, 9));
	}

	public void testSecsToRadsNeg1() {
		double secs = -9.20;
		double rads = converter.secsToRads(secs);
		assertEquals("-0.000044603", getNumToPrecision(rads, 9));
	}

	public void testDMSToDegsPos1() {
		// From p 135 of Meeus
		double n = converter.dmsToDegs(23, 26, 21.448);
		assertEquals("23.439291", getNumToPrecision(n, 6));
	}

	public void testDMSToDegsNeg1() {
		double n = converter.dmsToDegs(-23, 26, 21.448);
		assertEquals("-23.439291", getNumToPrecision(n, 6));
	}

	public void testDegsInRangeNeg1() {
		// See http://www.purplemath.com/modules/radians2.htm
		double degs = -3742;
		double degsInRange = converter.degsInRange(degs);
		assertEquals(218.0, degsInRange);
	}

	public void testDegsInRangePos1() {
		// See http://www.purplemath.com/modules/radians2.htm
		double degs = 15736;
		double degsInRange = converter.degsInRange(degs);
		assertEquals("256.0", getNumToPrecision(degsInRange, 1));
	}

	public void testRadsInRangeNeg1() {
		// See http://www.purplemath.com/modules/radians2.htm
		double rads = Math.toRadians(-3742);
		double radsInRange = converter.radsInRange(rads);
		assertEquals("218.0", getNumToPrecision(Math.toDegrees(radsInRange), 1));
	}

	public void testRadsInRangeNeg2() {
		// See http://www.purplemath.com/modules/radians2.htm
		double rads = -Math.PI / 4.0;
		double radsInRange = converter.radsInRange(rads);
		assertEquals(7.0 * Math.PI / 4.0, radsInRange);
	}

	public void testRadsInRangePos1() {
		// See http://www.purplemath.com/modules/radians2.htm
		double rads = Math.toRadians(15736);
		double radsInRange = converter.radsInRange(rads);
		assertEquals("256.0", getNumToPrecision(Math.toDegrees(radsInRange), 1));
	}

	public void testJulianCenturies1() {
		double jd = 2457498.04396;
		double T = converter.julianCenturies(jd);
		assertEquals("0.16298546091718", getNumToPrecision(T, 14));
	}

	public void testMeanObliquityLowPrecision1() {
		// From test case on p 136 of Meeus.
		double jd = 2446895.5;
		double T = converter.julianCenturies(jd);
		double meanObliq = converter.meanObliquityLowPrecision(T);
		double meanObliqDegs = Math.toDegrees(meanObliq);
		// In agreement with Meeus.
		assertEquals("23.440946490658636", getNumToPrecision(meanObliqDegs, 15));
	}

	public void testMeanObliquityHighPrecision1() {
		// From test case on p 136 of Meeus.
		double jd = 2446895.5;
		double T = converter.julianCenturies(jd);
		double meanObliq = converter.meanObliquityHighPrecision(T);
		double meanObliqDegs = Math.toDegrees(meanObliq);
		// In agreement with Meeus.
		assertEquals("23.440946290957320", getNumToPrecision(meanObliqDegs, 15));
	}

	public void testObliquity1() {
		// From test case on p 136 of Meeus.
		double jd = 2446895.5;
		double T = converter.julianCenturies(jd);
		double obliq = converter.obliquity(T);
		double obliqDegs = Math.toDegrees(obliq);
		// Approximately 0.025 degrees larger than in Meeus.
		assertEquals("23.443576286439995", getNumToPrecision(obliqDegs, 15));
	}

	// Meeus scenario, Example 24.a, p 153 (BEGIN).

	public void testJulianCenturiesEx24a() {
		double T = julianCenturiesEx24a();
		assertEquals("-0.072183436", getNumToPrecision(T, 9));
	}

	public void testEccentricityEx24a() {
		double T = julianCenturiesEx24a();
		double eDegs = Math.toDegrees(converter.eccentricity(T));
		assertEquals("0.016711651", getNumToPrecision(eDegs, 9));
	}

	public void testSolarCoordsEx24a() {
		double jd = MEEUS_EX24a_JD;
		double T = converter.julianCenturies(jd);
		int year = AbstractDateUtil.getInstance().jdToYMD(jd).getYear();

		SolarCoords coords = converter.solarCoords(T, year);

		double LoDegs = Math.toDegrees(coords.getGeometricMeanLongitude());
		assertEquals("201.80719", getNumToPrecision(LoDegs, 5));

		double MDegs = Math.toDegrees(coords.getTrueAnomaly());
		assertEquals("278.99396", getNumToPrecision(MDegs, 5));

		double CDegs = Math.toDegrees(coords.getEquationOfCenter());
		assertEquals("-1.89732", getNumToPrecision(CDegs, 5));

		// Meeus gives 198.38082, so we differ by 0.00001
		assertEquals("198.38083", getNumToPrecision(coords.getApparentRA(), 5));

		// Meeus gives -7.78507, so we differ by 0.00003
		assertEquals("-7.78504", getNumToPrecision(coords.getApparentDec(), 5));

		// Not provided in Meeus, but not wildly at odds with apparent RA.
		double ra = coords.getRA();
		assertEquals("198.38167", getNumToPrecision(ra, 5));
		// When Sun's longitude is referred to standard equinox of J2000.0
		// (trueSolarLong2000).
		// assertEquals("198.48617", getNumToPrecision(ra, 5));

		// Not provided in Meeus, but not wildly at odds with apparent Dec.
		double dec = coords.getDec();
		assertEquals("-7.78546", getNumToPrecision(dec, 5));
		// When Sun's longitude is referred to standard equinox of J2000.0
		// (trueSolarLong2000).
		// assertEquals("-7.82756", getNumToPrecision(dec, 5));
	}

	public void testRadiusVectorEx24a() {
		double jd = MEEUS_EX24a_JD;
		double T = converter.julianCenturies(jd);
		int year = AbstractDateUtil.getInstance().jdToYMD(jd).getYear();

		SolarCoords coords = converter.solarCoords(T, year);

		double R = converter.radiusVector(T, coords.getTrueAnomaly(),
				coords.getEquationOfCenter());
		// Meeus Ex 24.a gives R = 0.99766, whereas we have R = 0.99996.
		// Is this an error in Meeus or in our computation?
		assertEquals("0.99996", getNumToPrecision(R, 5));
	}

	private double julianCenturiesEx24a() {
		double jd = MEEUS_EX24a_JD;
		return converter.julianCenturies(jd);
	}

	// Meeus scenario, Example 24.a, p 153 (END).

	// HJD test cases using 3 stars at various declinations for two JDs.
	
	// R Car with Meeus's Ex24.a JD.
	public void testHJDRCar1() {
		RAInfo ra = new RAInfo(EpochType.J2000, 143.06083);
		DecInfo dec = new DecInfo(EpochType.J2000, -62.78889);

		double hjd = converter.convert(MEEUS_EX24a_JD, ra, dec);

		// The correction to JD here amounts to around 3.14 minutes (0.00218
		// days). The maximum correction is 8.3 minutes, corresponding to the
		// light-time across Earth's orbital radius.
		// This was tested using the BAA HJD calculator with RA and Dec from VSX
		// entry for R Car. That gave the result 2448908.4978207937, shortened
		// to 2448908.49782, whereas the convert() method gives
		// 2448908.497815484, the same to 5 decimal places (~1/100th of a
		// second).
		assertEquals("2448908.49782", getNumToPrecision(hjd, 5));
	}

	// R Car with JD2.
	public void testHJDRCar2() {
		RAInfo ra = new RAInfo(EpochType.J2000, 143.06083);
		DecInfo dec = new DecInfo(EpochType.J2000, -62.78889);

		double hjd = converter.convert(JD2, ra, dec);

		// The correction to JD here amounts to around 3.14 minutes (0.00218
		// days). The maximum correction is 8.3 minutes, corresponding to the
		// light-time across Earth's orbital radius.
		// This was tested using the BAA HJD calculator with RA and Dec from VSX
		// entry for R Car. That gave the result 2457501.869426729, shortened
		// to 2457501.86943, whereas the convert() method gives
		// 2457501.8694125116, the same to 4 decimal places (~1/10th of a
		// second).
		assertEquals("2457501.86941", getNumToPrecision(hjd, 5));
	}

	// X Sgr with Meeus's Ex24.a JD.
	public void testHJDXSgr1() {
		RAInfo ra = new RAInfo(EpochType.J2000, 266.89013);
		DecInfo dec = new DecInfo(EpochType.J2000, -27.83081);

		double hjd = converter.convert(MEEUS_EX24a_JD, ra, dec);

		// The correction to JD here amounts to around 3.14 minutes (0.00218
		// days). The maximum correction is 8.3 minutes, corresponding to the
		// light-time across Earth's orbital radius.
		// This was tested using the BAA HJD calculator with RA and Dec from VSX
		// entry for R Car. That gave the result 2448908.4977766555, shortened
		// to 2448908.49778, whereas the convert() method gives
		// 2448908.497780874.
		assertEquals("2448908.49778", getNumToPrecision(hjd, 5));
	}

	// X Sgr with JD2.
	public void testHJDXSgr2() {
		RAInfo ra = new RAInfo(EpochType.J2000, 266.89013);
		DecInfo dec = new DecInfo(EpochType.J2000, -27.83081);

		double hjd = converter.convert(JD2, ra, dec);

		// The correction to JD here amounts to around 3.14 minutes (0.00218
		// days). The maximum correction is 8.3 minutes, corresponding to the
		// light-time across Earth's orbital radius.
		// This was tested using the BAA HJD calculator with RA and Dec from VSX
		// entry for R Car. That gave the result 2457501.8707473096, shortened
		// to 2457501.87075, whereas the convert() method gives
		// 2457501.870747149.
		assertEquals("2457501.87075", getNumToPrecision(hjd, 5));
	}

	// Sig Oct with Meeus's Ex24.a JD.
	public void testHJDSigOct1() {
		RAInfo ra = new RAInfo(EpochType.J2000, 21, 8, 46.84);
		DecInfo dec = new DecInfo(EpochType.J2000, -88, 57, 23.40);

		double hjd = converter.convert(MEEUS_EX24a_JD, ra, dec);

		// The correction to JD here amounts to around 3.14 minutes (0.00218
		// days). The maximum correction is 8.3 minutes, corresponding to the
		// light-time across Earth's orbital radius.
		// This was tested using the BAA HJD calculator with RA and Dec from VSX
		// entry for R Car. That gave the result 2448908.4992657346, shortened
		// to 2448908.49927, whereas the convert() method gives
		// 2448908.499268005.
		assertEquals("2448908.49927", getNumToPrecision(hjd, 5));
	}

	// Sig Oct with JD2.
	public void testHJDSigOct2() {
		RAInfo ra = new RAInfo(EpochType.J2000, 21, 8, 46.84);
		DecInfo dec = new DecInfo(EpochType.J2000, -88, 57, 23.40);

		double hjd = converter.convert(JD2, ra, dec);

		// The correction to JD here amounts to around 3.14 minutes (0.00218
		// days). The maximum correction is 8.3 minutes, corresponding to the
		// light-time across Earth's orbital radius.
		// This was tested using the BAA HJD calculator with RA and Dec from VSX
		// entry for R Car. That gave the result 2457501.8685732614, shortened
		// to 2457501.86857, whereas the convert() method gives
		// 2457501.868574388.
		assertEquals("2457501.86857", getNumToPrecision(hjd, 5));
	}
	
	// Helpers

	private String getNumToPrecision(double n, int precision) {
		return String.format("%1." + precision + "f", n);
	}
}
