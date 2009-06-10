/**
 * VStar: a statistical analysis tool for variable star data.
 * Copyright (C) 2009  AAVSO (http://www.aavso.org/)
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
package org.aavso.tools.vstar.data;

import org.aavso.tools.vstar.util.AbstractDateUtil;
import org.aavso.tools.vstar.util.JDCalculator;
import org.aavso.tools.vstar.util.MeeusDateUtil;


/**
 * This class store Julian Day and corresponding calendar date information.
 */
public class DateInfo {
	
	private static AbstractDateUtil jdCalc = new MeeusDateUtil();
	
	private double julianDay;

	/**
	 * Constructor.
	 * 
	 * @param julianDay A Julian Day.
	 */
	public DateInfo(double julianDay) {
		this.julianDay = julianDay;
	}

	/**
	 * @return the julianDay
	 */
	public double getJulianDay() {
		return julianDay;
	}

	@Override
	public String toString() {
		StringBuffer strBuf = new StringBuffer();
		
		strBuf.append("JD: ");
		strBuf.append(julianDay);
		strBuf.append(" (");
		strBuf.append(jdCalc.jdToCalendar(julianDay));
		strBuf.append(")");
		
		return strBuf.toString();
	}
}
