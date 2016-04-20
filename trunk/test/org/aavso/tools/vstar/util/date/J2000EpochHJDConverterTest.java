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
		// In good agreement with Meeus.
		assertEquals("23.440946490658636", getNumToPrecision(meanObliqDegs, 15));
	}

	public void testMeanObliquityHighPrecision1() {
		// From test case on p 136 of Meeus.
		double jd = 2446895.5;
		double T = converter.julianCenturies(jd);
		double meanObliq = converter.meanObliquityHighPrecision(T);
		double meanObliqDegs = Math.toDegrees(meanObliq);
		// In good agreement with Meeus.
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

		double MDegs = Math.toDegrees(coords.getTrueAnomaly());
		assertEquals("278.99396", getNumToPrecision(MDegs, 5));

		double CDegs = Math.toDegrees(coords.getEquationOfCenter());
		assertEquals("-1.89732", getNumToPrecision(CDegs, 5));

	}

	public void testRadiusVectorEx24a() {
		double jd = MEEUS_EX24a_JD;
		double T = converter.julianCenturies(jd);
		int year = AbstractDateUtil.getInstance().jdToYMD(jd).getYear();

		SolarCoords coords = converter.solarCoords(T, year);

		double R = converter.radiusVector(T, coords.getEquationOfCenter(),
				coords.getTrueAnomaly());
		assertEquals("0.99766", getNumToPrecision(R, 5));
	}

	private double julianCenturiesEx24a() {
		double jd = MEEUS_EX24a_JD;
		return converter.julianCenturies(jd);
	}

	// Meeus scenario, Example 24.a, p 153 (END).

	public void testConversion1() {
		// RAInfo ra = new RAInfo(EpochType.J2000, 0, 0, 0);
		// DecInfo dec = new DecInfo(EpochType.J2000, 0, 0, 0);
		// double jd = 2448908.5;
		// double hjd = converter.convert(jd, ra, dec);
		// String hjdStr = getNumToPrecision(hjd, 8);
		// assertEquals(getNumToPrecision(2448908.5, 8), hjdStr);
	}

	// public void testConversion2() {
	// RAInfo ra = new RAInfo(EpochType.J2000, 15, 2, 3.6);
	// DecInfo dec = new DecInfo(EpochType.J2000, -25, 45, 3);
	// double hjd = converter.convert(JD, ra, dec);
	// String hjdStr = getNumToPrecision(hjd, PRECISION);
	// assertEquals(getNumToPrecision(2445239.39611801, PRECISION), hjdStr);
	// }
	//
	// public void testConversion3() {
	// RAInfo ra = new RAInfo(EpochType.J2000, 12, 0, 0);
	// DecInfo dec = new DecInfo(EpochType.J2000, 2, 0, 0);
	// double hjd = converter.convert(JD, ra, dec);
	// String hjdStr = getNumToPrecision(hjd, PRECISION);
	// assertEquals(getNumToPrecision(2445239.39422482, PRECISION), hjdStr);
	// }

	// Helpers

	private String getNumToPrecision(double n, int precision) {
		return String.format("%1." + precision + "f", n);
	}
}
