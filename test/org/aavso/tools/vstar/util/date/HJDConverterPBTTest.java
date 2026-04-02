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
import org.quicktheories.WithQuickTheories;

/**
 * Property-based tests for the J2000 HJD converter.
 *
 * These tests express universal properties of the HJD correction and
 * angle-normalization helpers, suitable for later promotion to formal
 * proofs (e.g. in Lean 4 or via JML/OpenJML).
 */
public class HJDConverterPBTTest extends TestCase implements WithQuickTheories {

	private J2000HJDConverter converter;

	public HJDConverterPBTTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		converter = (J2000HJDConverter) AbstractHJDConverter
				.getInstance(EpochType.J2000);
	}

	// -- degsInRange properties --

	/**
	 * degsInRange always returns a value in [0, 360).
	 */
	public void testDegsInRangeOutputRangeProperty() {
		qt().forAll(doubles().between(-1e6, 1e6)).check(degs -> {
			double result = converter.degsInRange(degs);
			return result >= 0 && result < 360.0;
		});
	}

	/**
	 * degsInRange is idempotent: applying it twice gives the same result.
	 */
	public void testDegsInRangeIdempotentProperty() {
		qt().forAll(doubles().between(-1e6, 1e6)).check(degs -> {
			double once = converter.degsInRange(degs);
			double twice = converter.degsInRange(once);
			return Math.abs(once - twice) < 1e-10;
		});
	}

	/**
	 * degsInRange respects 360-degree periodicity.
	 */
	public void testDegsInRangePeriodicProperty() {
		qt().forAll(doubles().between(-1e4, 1e4)).check(degs -> {
			double r1 = converter.degsInRange(degs);
			double r2 = converter.degsInRange(degs + 360.0);
			return Math.abs(r1 - r2) < 1e-9;
		});
	}

	// -- radsInRange properties --

	/**
	 * radsInRange always returns a value in [0, 2*pi).
	 */
	public void testRadsInRangeOutputRangeProperty() {
		final double TWOPI = 2 * Math.PI;
		qt().forAll(doubles().between(-1e4, 1e4)).check(rads -> {
			double result = converter.radsInRange(rads);
			return result >= 0 && result < TWOPI + 1e-12;
		});
	}

	/**
	 * radsInRange is idempotent.
	 */
	public void testRadsInRangeIdempotentProperty() {
		qt().forAll(doubles().between(-1e4, 1e4)).check(rads -> {
			double once = converter.radsInRange(rads);
			double twice = converter.radsInRange(once);
			return Math.abs(once - twice) < 1e-10;
		});
	}

	/**
	 * radsInRange respects 2*pi periodicity.
	 */
	public void testRadsInRangePeriodicProperty() {
		final double TWOPI = 2 * Math.PI;
		qt().forAll(doubles().between(-1e3, 1e3)).check(rads -> {
			double r1 = converter.radsInRange(rads);
			double r2 = converter.radsInRange(rads + TWOPI);
			return Math.abs(r1 - r2) < 1e-9;
		});
	}

	// -- HJD correction bound --

	/**
	 * The HJD correction is bounded by R_max/c where R_max ~ 1.017 AU and
	 * c = 173.14 AU/day, giving a maximum correction of ~0.00588 days
	 * (~8.5 minutes). We use 0.006 days as a generous bound.
	 *
	 * This property holds for any sky position and any JD in a reasonable
	 * historical/future range.
	 */
	public void testHJDCorrectionBoundProperty() {
		final double MAX_CORRECTION_DAYS = 0.006;

		qt().forAll(
				doubles().between(2415000, 2470000),
				doubles().between(0.0, 360.0),
				doubles().between(-90.0, 90.0))
				.check((jd, raDeg, decDeg) -> {
					RAInfo ra = new RAInfo(EpochType.J2000, raDeg);
					DecInfo dec = new DecInfo(EpochType.J2000, decDeg);
					double hjd = converter.convert(jd, ra, dec);
					double correction = Math.abs(hjd - jd);
					return correction <= MAX_CORRECTION_DAYS;
				});
	}

	// -- julianCenturies is linear --

	/**
	 * julianCenturies(jd) is a linear function of jd:
	 * T(jd + delta) - T(jd) = delta / 36525.
	 */
	public void testJulianCenturiesLinearProperty() {
		qt().forAll(
				doubles().between(2415000, 2470000),
				doubles().between(0.1, 365.25))
				.check((jd, delta) -> {
					double t1 = converter.julianCenturies(jd);
					double t2 = converter.julianCenturies(jd + delta);
					double expected = delta / 36525.0;
					return Math.abs((t2 - t1) - expected) < 1e-12;
				});
	}

	// -- Radius vector bound --

	/**
	 * The Earth-Sun radius vector R is bounded: approximately
	 * 0.983 AU (perihelion) <= R <= 1.017 AU (aphelion).
	 * We use slightly wider bounds for robustness with the low-accuracy model.
	 */
	public void testRadiusVectorBoundProperty() {
		qt().forAll(doubles().between(2415000, 2470000)).check(jd -> {
			double T = converter.julianCenturies(jd);
			int year = AbstractDateUtil.getInstance().jdToYMD(jd).getYear();
			SolarCoords coords = converter.solarCoords(T, year);
			double R = converter.radiusVector(T, coords.getTrueAnomaly(),
					coords.getEquationOfCenter());
			return R >= 0.97 && R <= 1.04;
		});
	}
}
