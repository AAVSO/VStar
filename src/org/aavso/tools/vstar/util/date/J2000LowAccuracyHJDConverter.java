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

import org.aavso.tools.vstar.util.coords.DecInfo;
import org.aavso.tools.vstar.util.coords.RAInfo;

/**
 * This class contains a method for converting a Julian Date to a Heliocentric
 * Julian Date using the Low Accuracy method in Jean Meeus's Astronomical
 * Algorithms, ch 24.<br/>
 * 
 * See also:<br/>
 * - Meeus, ch 21, 24<br/>
 * - https://en.wikipedia.org/wiki/Heliocentric_Julian_Day
 */
public class J2000LowAccuracyHJDConverter extends AbstractHJDConverter {

	/**
	 * Given a JD, RA, and Dec, return HJD.
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

		// Values are computed as degrees and converted to radians when
		// necessary (when passing to a trigonometric function). Where a
		// variable name is not qualified with a "Degs"
		// suffix, it can be assumed to be in radians or to be a
		// non-trigonometric value.

		// Meeus 24.1
		// Time measured in Julian centuries.
		double T = julianCenturies(jd);

		// Meeus 24.3
		// Geometric mean longitude of the Sun.
		double LoDegs = 280.46645 + 36000.76983 * T + 0.0003032 * (T * T);

		// Meeus 24.3
		// Mean anomaly of the Sun.
		double MDegs = 357.52910 + 35999.05030 * T - 0.0001559 * (T * T)
				- 0.00000048 * (T * T * T);

		// Meeus 24.4
		// Eccentricity of Earth's orbit.
		double eDegs = 0.016708617 - 0.000042037 * T - 0.0000001236 * (T * T);
		double e = Math.toRadians(eDegs);

		// Meeus (p 152)
		// Sun's equation of center.
		double C = Math.toRadians(1.914600 - 0.004817 * T - 0.000014 * (T * T))
				* Math.sin(Math.toRadians(MDegs))
				+ Math.toRadians(0.019993 - 0.000101 * T)
				* Math.sin(Math.toRadians(2 * MDegs))
				+ Math.toRadians(0.000290)
				* Math.sin(Math.toRadians(3 * MDegs));
		double CDegs = Math.toDegrees(C);

		// Meeus (p 152)
		// True solar longitude.
		double trueSolarLong = Math.toRadians(LoDegs) + C;
		double trueSolarLongDegs = Math.toDegrees(trueSolarLong);

		// Meeus (p 152)
		// True solar anomaly.
		double v = Math.toRadians(MDegs) + C;
		double vDegs = Math.toDegrees(v);

		// Meeus (24.5)
		// The Sun's radius vector (Earth-Sun distance) in AU.
		double R = (1.000001018 * (1 - e * e)) / (1 + e * Math.cos(e));

		// Meeus (p 152)
		// Sun's longitude with respect to J2000.0 epoch.
		int year = AbstractDateUtil.getInstance().jdToYMD(jd).getYear();

		double trueSolarLong2000 = trueSolarLong - Math.toRadians(0.01397)
				* (year - 2000);
		double trueSolarLong2000Degs = Math.toDegrees(trueSolarLong2000);

		// Obliquity of the ecliptic.
		double obliq = obliquity(T);
		double obliqDegs = Math.toDegrees(obliq);

		// Meeus (24.6)
		// Right ascension of the Sun.
		double solarRA = Math.atan2(Math.cos(trueSolarLong2000),
				Math.cos(obliq) * Math.sin(trueSolarLong2000));
		double solarRADegs = Math.toDegrees(solarRA);

		// Meeus (24.7)
		// Declination of the Sun.
		double solarDec = Math.asin(Math.sin(obliq)
				* Math.sin(trueSolarLong2000));
		double solarDecDegs = Math.toDegrees(solarDec);

		// Heliocentric JD of the target.
		// See https://en.wikipedia.org/wiki/Heliocentric_Julian_Day
		// c is speed of light, expressed in AU per day, since R is measured in
		// AU and we are correcting JD to give HJD (see
		// https://en.wikipedia.org/wiki/Astronomical_unit)
		// raRads and decRads are target RA and declination in radians.
		double c = 173.144632674240;
		double raRads = Math.toRadians(ra.toDegrees());
		double decRads = Math.toRadians(dec.toDegrees());
		double hjd = jd
				- (R / c)
				* (Math.sin(decRads) * Math.sin(solarDec) + Math.cos(decRads)
						* Math.cos(solarDec) * Math.cos(raRads - solarRA));

		return hjd;
	}

	/**
	 * Time measured in Julian centuries of 36525 ephemeris days from epoch
	 * J2000.0 (2000 January 21.5 TD).<br/>
	 * 
	 * TODO: do we need to doing anything else for this to be in JDE?
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

		// Meeus (p 132)
		// Longitude of ascending node of Moon's mean orbit on ecliptic,
		// measured from mean equinox of date.
		double omegaDegs = 125.04452 - 1934.136261 * T + 0.0020708 * (T * T)
				+ (T * T * T) / 450000.0;
		double omegaRads = Math.toRadians(omegaDegs);

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
}
