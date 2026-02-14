/**
 * VStar: a statistical analysis tool for variable star data.
 * Copyright (C) 2016  AAVSO (http://www.aavso.org/)
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

import org.aavso.tools.vstar.util.coords.DecInfo;
import org.aavso.tools.vstar.util.coords.RAInfo;

/**
 * <p>
 * This class contains a method for converting a Julian Date to a Heliocentric
 * Julian Date using the Low Accuracy method in Jean Meeus's Astronomical
 * Algorithms, ch 24.
 * </p>
 * <p>
 * See also:<br/>
 * - Meeus, ch 21, 24<br/>
 * - https://en.wikipedia.org/wiki/Heliocentric_Julian_Day -
 * http://britastro.org/computing/applets_dt.html<br/>
 * - http://www.physics.sfasu.edu/astro/javascript/hjd.html<br/>
 * </p>
 * <p>
 * Values are computed as degrees and converted to radians when necessary (when
 * passing to a trigonometric function). Where a variable name is not qualified
 * with a "Degs" suffix, it can be assumed to be in radians or to be a
 * non-trigonometric value.
 * </p>
 * <p>
 * Variable names often follow Meeus's conventions since this allows easier
 * correspondence between Meeus and code. This sometimes means abandoning the
 * usual Java naming conventions, e.g. using a single letter capital variable
 * name. Comments show the correspondence between code and equations in Meeus.
 * </p>
 */
public class J2000HJDConverter extends AbstractHJDConverter {

	/**
	 * Given a JD and a target's RA and Dec, return the Heliocentric Julian
	 * Date.
	 * 
	 * @param jd
	 *            The Julian Date to be converted.
	 * @param ra
	 *            The J2000 epoch right ascension coordinate in decimal degrees.
	 * @param dec
	 *            The J2000 epoch declination coordinate in decimal degrees.
	 * @return The corresponding Heliocentric Julian Date.
	 */
	@Override
	public double convert(double jd, RAInfo ra, DecInfo dec) {

		// Meeus 24.1
		// Time measured in Julian centuries.
		double T = julianCenturies(jd);

		int year = AbstractDateUtil.getInstance().jdToYMD(jd).getYear();

		SolarCoords coords = solarCoords(T, year);

		return hjd(jd, T, coords, ra, dec);
	}

	/**
	 * Time measured in Julian centuries of 36525 ephemeris days from epoch
	 * J2000.0 (2000 January 21.5 TD).<br/>
	 * 
	 * TODO: do we need to doing anything else for this to be in JDE/TD?
	 * 
	 * @param jd
	 *            The Julian Date.
	 * @return The time in Julian centuries.
	 */
	protected double julianCenturies(double jd) {

		// Meeus 24.1
		return (jd - 2451545.0) / 36525.0;
	}

	/**
	 * Compute and return the Heliocentric JD of the target.
	 * 
	 * @param jd
	 *            The Julian Date to be converted.
	 * @param T
	 *            The time in Julian centuries.
	 * @param coords
	 *            The Solar coordinates for the julian date.
	 * @param ra
	 *            The J2000 epoch right ascension coordinate in decimal degrees.
	 * @param dec
	 *            The J2000 epoch declination coordinate in decimal degrees.
	 * @return The corresponding Heliocentric Julian Date.
	 */
	protected double hjd(double jd, double T, SolarCoords coords, RAInfo ra,
			DecInfo dec) {

		double R = radiusVector(T, coords.getTrueAnomaly(),
				coords.getEquationOfCenter());

		// See https://en.wikipedia.org/wiki/Heliocentric_Julian_Day
		// c is speed of light, expressed in AU per day, since R is measured in
		// AU and we are correcting JD to give HJD (see
		// https://en.wikipedia.org/wiki/Astronomical_unit)
		double c = 173.144632674240;

		double targetRARads = Math.toRadians(ra.toDegrees());
		double targetDecRads = Math.toRadians(dec.toDegrees());

		double solarRARads = Math.toRadians(coords.getRA());
		double solarDecRads = Math.toRadians(coords.getDec());

		return jd
				- (R / c)
				* (Math.sin(targetDecRads) * Math.sin(solarDecRads) + Math
						.cos(targetDecRads)
						* Math.cos(solarDecRads)
						* Math.cos(targetRARads - solarRARads));
	}

	/**
	 * Given the year and time in Julian centuries, return the Sun's
	 * coordinates.
	 * 
	 * @param T
	 *            The time in Julian centuries.
	 * @param year
	 *            The year.
	 * @return The solar coordinates for the specified date.
	 */
	protected SolarCoords solarCoords(double T, int year) {

		// Meeus 24.3
		// Geometric mean longitude of the Sun.
		double Lo = Math.toRadians(degsInRange(280.46645 + 36000.76983 * T
				+ 0.0003032 * (T * T)));

		// Meeus 24.3
		// Mean anomaly of the Sun.
		double M = radsInRange(Math.toRadians(357.52910 + 35999.05030 * T
				- 0.0001559 * (T * T) - 0.00000048 * (T * T * T)));

		// Meeus (p 152)
		// Sun's equation of center.
		double C = Math.toRadians(1.914600 - 0.004817 * T - 0.000014 * (T * T))
				* Math.sin(M) + Math.toRadians(0.019993 - 0.000101 * T)
				* Math.sin(2 * M) + Math.toRadians(0.000290) * Math.sin(3 * M);

		// Meeus (p 152)
		// True solar longitude.
		double trueSolarLong = Lo + C;

	    // Meeus (p 152)
        // Sun's longitude with respect to J2000.0 epoch.
        double trueSolarLong2000 = trueSolarLong - Math.toRadians(0.01397)
                * (year - 2000.0);

		// double omegaDegs = 125.04 - 1934.136 * T;
		double omega = longitudeOfAscendingNode(T);

		// Meeus (p152)
		// Apparent longitude of Sun.
		// Note: not strictly needed.
		double lambda = trueSolarLong2000 - Math.toRadians(0.00569)
				- Math.toRadians(0.00478) * Math.sin(omega);

		// Obliquity of the ecliptic.
		double obliq = obliquity(T);
		double apparentObliq = obliq + Math.toRadians(0.00256)
				* Math.cos(omega);

		// Meeus 24.6
		// Right ascension of the Sun.
		double solarRA = Math.atan2(Math.cos(obliq) * Math.sin(trueSolarLong2000),
				Math.cos(trueSolarLong2000));

		double apparentSolarRA = Math.atan2(
				Math.cos(apparentObliq) * Math.sin(lambda), Math.cos(lambda));

		// Meeus (24.7)
		// Declination of the Sun.
		double solarDec = Math.asin(Math.sin(obliq) * Math.sin(trueSolarLong2000));

		double apparentSolarDec = Math.asin(Math.sin(apparentObliq)
				* Math.sin(lambda));

		return new SolarCoords(degsInRange(Math.toDegrees(solarRA)),
				Math.toDegrees(solarDec),
				degsInRange(Math.toDegrees(apparentSolarRA)),
				Math.toDegrees(apparentSolarDec), Lo, M, C);
	}

	/**
	 * Return the longitude of ascending node of Moon's mean orbit on ecliptic,
	 * measured from mean equinox of date.
	 * 
	 * @param T
	 *            The time in Julian centuries.
	 * 
	 * @return The longitude of the ascending node of Moon's mean orbit in
	 *         radians.
	 */
	protected double longitudeOfAscendingNode(double T) {
		// Meeus (p 132)
		// Longitude of ascending node of Moon's mean orbit on ecliptic,
		// measured from mean equinox of date.
		// TODO: compare overall result against using less precise version of
		// this
		double omegaDegs = 125.04452 - 1934.136261 * T + 0.0020708 * (T * T)
				+ (T * T * T) / 450000.0;

		return Math.toRadians(omegaDegs);
	}

	/**
	 * Given the time in Julian centuries, return radius vector (Earth-Sun
	 * distance) in AU.
	 * 
	 * @param T
	 *            The time in Julian centuries.
	 * @param M
	 *            The true solar anomaly.
	 * @param C
	 *            The Sun's equation of center.
	 * @return The Sun's radius vector in AU.
	 */
	protected double radiusVector(double T, double M, double C) {

		double e = eccentricity(T);

		// Meeus (p 152)
		// True solar anomaly.
		double v = M + C;

		// Meeus (24.5)
		// The Sun's radius vector (Earth-Sun distance) in AU.
		return (1.000001018 * (1.0 - e * e)) / (1.0 + e * Math.cos(v));
	}

	/**
	 * Given the time in Julian centuries, return eccentricity of Earth's orbit.
	 * Eccentricity is dimensionless (unitless); it must not be converted to
	 * radians, as it is used in the radius-vector formula (24.5) as a pure
	 * number.
	 *
	 * @param T
	 *            The time in Julian centuries.
	 * @return The eccentricity (dimensionless).
	 */
	protected double eccentricity(double T) {

		// Meeus 24.4
		// Eccentricity of Earth's orbit.
		return 0.016708617 - 0.000042037 * T - 0.0000001236 * (T * T);
	}

	/**
	 * Given the time in Julian Centuries from epoch J2000.0, return the
	 * obliquity of the ecliptic in radians.
	 * 
	 * @param T
	 *            Time measured in Julian centuries from epoch J2000.0.
	 * @return The obliquity in radians.
	 */
	protected double obliquity(double T) {

		// Meeus (p 132)
		// Mean longitude of Sun.
		double LDegs = 280.4665 + 36000.7698 * T;
		double LRads = Math.toRadians(LDegs);

		// Meeus (p 132)
		// Mean longitude of Moon.
		double LprimeDegs = 218.3165 + 481267.8813 * T;
		double LprimeRads = Math.toRadians(LprimeDegs);

		double omegaRads = longitudeOfAscendingNode(T);

		// Meeus (p 132)
		// Nutation in obliquity.
		double deltaEps = secsToRads(9.2) * Math.cos(omegaRads)
				+ secsToRads(0.57) * Math.cos(2 * LRads) + secsToRads(0.1)
				* Math.cos(2 * LprimeRads) - secsToRads(0.09)
				* Math.cos(2 * omegaRads);

		// Meeus (p135)
		// True obliquity of ecliptic.
		return meanObliquityHighPrecision(T) + deltaEps;
	}

	/**
	 * Given the time in Julian Centuries from epoch J2000.0, return the mean
	 * obliquity of the ecliptic in radians.
	 * 
	 * @param T
	 *            Time measured in Julian centuries from epoch J2000.0.
	 * @return The mean obliquity in radians.
	 */
	protected double meanObliquityLowPrecision(double T) {
		// Meeus (21.2)
		// Obliquity of the ecliptic.
		return Math.toRadians(dmsToDegs(23, 26, 21.448))
				- secsToRads(46.8150 * T) - secsToRads(0.00059 * (T * T))
				+ secsToRads(0.001813 * (T * T * T));
	}

	/**
	 * Given the time in Julian Centuries from epoch J2000.0, return the mean
	 * obliquity of the ecliptic in radians.
	 * 
	 * @param T
	 *            Time measured in Julian centuries from epoch J2000.0.
	 * @return The mean obliquity in radians.
	 */
	protected double meanObliquityHighPrecision(double T) {
		// Meeus (21.3)
		// Obliquity of the ecliptic.
		double U = T / 100.0;
		return Math.toRadians(dmsToDegs(23, 26, 21.448))
				- secsToRads(4680.93 * U) - secsToRads(1.55 * (U * U))
				+ secsToRads(1999.25 * (U * U * U))
				- secsToRads(51.38 * Math.pow(U, 4))
				- secsToRads(249.67 * Math.pow(U, 5))
				- secsToRads(39.05 * Math.pow(U, 6))
				+ secsToRads(7.12 * Math.pow(U, 7))
				+ secsToRads(27.87 * Math.pow(U, 8))
				+ secsToRads(5.79 * Math.pow(U, 9))
				+ secsToRads(2.45 * Math.pow(U, 10));
	}

	/**
	 * Given a value in seconds, return the corresponding number of radians.
	 * 
	 * @param secs
	 *            The value in seconds.
	 * @return The corresponding value in radians.
	 */
	protected double secsToRads(double secs) {
		return Math.toRadians(secs / 3600.0);
	}

	/**
	 * Given degrees, minutes, and seconds, return decimal degrees.
	 * 
	 * @param degs
	 *            integer degrees
	 * @param mins
	 *            integer minutes
	 * @param secs
	 *            double seconds
	 * @return The corresponding value in decimal degrees.
	 */
	protected double dmsToDegs(int degs, int mins, double secs) {
		double decDegs = Math.signum(degs)
				* (Math.abs(degs) + mins / 60.0 + secs / 3600.0);
		return decDegs;
	}

	/**
	 * Given a value in radians, return the corresponding value in the range 0
	 * to 2PI.<br/>
	 * 
	 * See, for example, http://www.purplemath.com/modules/radians2.htm
	 * 
	 * @param n
	 *            The value in radians.
	 * @return The corresponding value in the range 0 to 2PI.
	 */
	protected double radsInRange(double n) {
		final double TWOPI = 2 * Math.PI;
		if (n > TWOPI) {
			n = n - Math.floor(n / TWOPI) * TWOPI;
		} else if (n < 0) {
			n = n + Math.ceil(-n / TWOPI) * TWOPI;
		}

		return n;
	}

	/**
	 * Given a value in degrees, return the corresponding value in the range 0
	 * to 360.<br/>
	 * 
	 * See, for example, http://www.purplemath.com/modules/radians2.htm
	 * 
	 * @param n
	 *            The value in degrees.
	 * @return The corresponding value in the range 0 to 360.
	 */
	protected double degsInRange(double n) {
		if (n > 360.0) {
			n = n - Math.floor(n / 360.0) * 360.0;
		} else if (n < 0) {
			n = n + Math.ceil(-n / 360.0) * 360.0;
		}

		return n;
	}
}
