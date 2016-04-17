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
 * Julian Date.<br/>
 * 
 * Based upon Perl code provided by Matthew Templeton, AAVSO. See
 * https://sourceforge
 * .net/tracker/?func=detail&aid=3391997&group_id=263306&atid=1152052<br/>
 * 
 * TODO: Independent reference to algorithm; see tracker
 */
public class B1950HJDConverter extends AbstractHJDConverter {

	/**
	 * Given a JD, RA, and Dec, return HJD.
	 * 
	 * @param jd
	 *            The Julian Date to be converted.
	 * @param ra
	 *            The B1950 epoch right ascension coordinate.
	 * @param dec
	 *            The B1950 epoch declination coordinate.
	 * @return The corresponding Heliocentric Julian Date.
	 */
	@Override
	public double convert(double jd, RAInfo ra, DecInfo dec) {
		double hjd = 0;
	
		// TODO: use Math.toRadians(arg0)?
	
		double deg2rad = 3.1415926535 / 180.;
		double raRadians = ra.toDegrees() * deg2rad;
		double decRadians = dec.toDegrees() * deg2rad;
	
		double eps = (23. + (27. / 60.)) * deg2rad;
	
		double T = (jd - 2415020.) / 36525.;
	
		double p = (1.396041 + (0.000308 * (T + 0.5))) * (T - 0.499998);
	
		double L = 279.696678 + (36000.76892 * T) + (0.000303 * T * T) - p;
	
		double G = 358.475833 + (35999.04975 * T) - (0.00015 * T * T);
	
		L = L * deg2rad;
		G = G * deg2rad;
	
		double AJ = 225.444651 + (2880. * T) + (154.906654 * T * T);
		AJ = AJ * deg2rad;
	
		double X = 0.99986 * Math.cos(L) - 0.025127 * Math.cos(G - L)
				+ 0.0008374 * Math.cos(G + L) + 0.000105 * Math.cos(G + G + L)
				+ 0.000063 * T * Math.cos(G - L) + 0.000035
				* Math.cos(G + G + L) - 0.000026 * Math.sin(G - L - AJ)
				- 0.000021 * T * Math.cos(G + L);
	
		double Y = 0.917308 * Math.sin(L) + 0.023053 * Math.sin(G - L)
				+ 0.007683 * Math.sin(G + L) + 0.000097 * Math.sin(G + G + L)
				- 0.000057 * T * Math.sin(G - L) - 0.000032
				* Math.sin(G + G - L) - 0.000024 * Math.cos(G - L - AJ)
				- 0.000019 * T * Math.sin(G + L);
	
		double dt = -0.0057755
				* ((Math.cos(decRadians) * Math.cos(raRadians) * X) + ((Math
						.tan(eps)
						* Math.sin(decRadians) + Math.cos(decRadians)
						* Math.sin(raRadians)) * Y));
	
		hjd = jd + dt;
	
		return hjd;
	}
}
