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
package org.aavso.tools.vstar.util;

import org.aavso.tools.vstar.util.date.AbstractDateUtil;

import junit.framework.TestCase;

/**
 * This is a set of unit test cases for conversion to and from 
 * Julian Day and Calendar Date.
 * 
 * Most test cases were taken from Jean Meeus's Astronomical Algorithms 
 * 1991, 1st edition, chapter 7. A few (marked) were were checked with  
 * http://www.sizes.com/time/dayJulianr.htm. Others were checked with
 * http://www.aavso.org/observing/aids/jdcalendar.shtml (marked).
 */
public class DateUtilTestBase extends TestCase {

	private AbstractDateUtil dateUtil;
	
	public DateUtilTestBase(String name, AbstractDateUtil dateUtil) {
		super(name);
		this.dateUtil = dateUtil;
	}

	// ** Julian Day to Calendar date **
	
	// From Meeus
	
	public void testJDtoCal2000() {
		commonJDtoCalTest(2451545.0, "2000 JAN 1");
	}

	public void testJDtoCal1987a() {
		commonJDtoCalTest(2446822.5, "1987 JAN 27");
	}
	
	public void testJDtoCal1987b() {
		commonJDtoCalTest(2446966.0, "1987 JUN 19");
	}

	public void testJDtoCal1988a() {
		commonJDtoCalTest(2447187.5, "1988 JAN 27");
	}

	public void testJDtoCa1988b() {
		commonJDtoCalTest(2447332.0, "1988 JUN 19");
	}

	public void testJDtoCal1957() {
		// Sputnik
		commonJDtoCalTest(2436116.31, "1957 OCT 4");
	}

	public void testJDtoCa1900() {
		commonJDtoCalTest(2415020.5, "1900 JAN 1");
	}

	public void testJDtoCal1600a() {
		commonJDtoCalTest(2305447.5, "1600 JAN 1");		
	}

	public void testJDtoCal1600b() {
		commonJDtoCalTest(2305812.5, "1600 DEC 31");		
	}

	public void testJDtoCal837() {
		commonJDtoCalTest(2026871.8, "837 APR 10");		
	}
	
	public void testJDtoCal333() {
		commonJDtoCalTest(1842713.0, "333 JAN 27");		
	}

	public void testJDtoCalNeg584() {
		commonJDtoCalTest(1507900.13, "-584 MAY 28");		
	}

	public void testJDtoCalNeg1000a() {
		commonJDtoCalTest(1356001.0, "-1000 JUL 12");		
	}

	public void testJDtoCalNeg1000b() {
		commonJDtoCalTest(1355866.5, "-1000 FEB 29");		
	}

	public void testJDtoCalNeg1001() {
		commonJDtoCalTest(1355671.4, "-1001 AUG 17");		
	}

	public void testJDtoCalNeg4712() {
		commonJDtoCalTest(0.0, "-4712 JAN 1");		
	}

	// From http://www.sizes.com/time/dayJulianr.htm

	public void testJDtoCal2010a() {
		commonJDtoCalTest(2455197.5, "2010 JAN 1");		
	}

	public void testJDtoCal2010b() {
		commonJDtoCalTest(2455561.5, "2010 DEC 31");		
	}

	public void testJDtoCal2009a() {
		commonJDtoCalTest(2454832.5, "2009 JAN 1");		
	}	
	
	public void testJDtoCal2009b() {
		commonJDtoCalTest(2455196.5, "2009 DEC 31");		
	}	

	// Helper
	private void commonJDtoCalTest(double jd, String expectedDate) {
		String date = this.dateUtil.jdToCalendar(jd);
		assertEquals(expectedDate, date);		
	}
		
	// ** Calendar date to Julian Day (reverse of the tests above) **
	
	// From Meeus
	
	public void testCaltoJD2000() {
		commonCaltoJDTest(2000, 1, 1.5, 2451545.0);
	}

	public void testCaltoJD1987a() {
		commonCaltoJDTest(1987, 1, 27, 2446822.5);
	}

	public void testCaltoJD1987b() {
		commonCaltoJDTest(1987, 6, 19.5, 2446966.0);
	}

	public void testCaltoJD1988a() {
		commonCaltoJDTest(1988, 1, 27, 2447187.5);
	}

	public void testCaltoJD1988b() {
		commonCaltoJDTest(1988, 6, 19.5, 2447332.0);
	}

	public void testCaltoJD1900() {
		commonCaltoJDTest(1900, 1, 1, 2415020.5);
	}

	public void testCaltoJD1600a() {
		commonCaltoJDTest(1600, 1, 1, 2305447.5);
	}
	
	public void testCaltoJD1600b() {
		commonCaltoJDTest(1600, 12, 31, 2305812.5);
	}

	public void testCaltoJD1957() {
		// Sputnik
		commonCaltoJDTest(1957, 10, 4.81, 2436116.31);
	}

	public void testCaltoJD837() {
		commonCaltoJDTest(837, 4, 10.3, 2026871.8);
	}

	public void testCaltoJD333() {
		commonCaltoJDTest(333, 1, 27.5, 1842713.0);
	}

	public void testCaltoJDNeg584() {
		commonCaltoJDTest(-584, 5, 28.63, 1507900.13);
	}

	public void testCaltoJDNeg1000a() {
		commonCaltoJDTest(-1000, 7, 12.5, 1356001.0);
	}

	public void testCaltoJDNeg1000b() {
		commonCaltoJDTest(-1000, 2, 29, 1355866.5);
	}

	public void testCaltoJDNeg1001() {
		commonCaltoJDTest(-1001, 8, 17.9, 1355671.4);
	}

	public void testCaltoJDNeg4712() {
		commonCaltoJDTest(-4712, 1, 1.5, 0.0);
	}

	// Checked with http://www.sizes.com/time/dayJulianr.htm
	
	public void testCaltoJD2010a() {
		commonCaltoJDTest(2010, 1, 1, 2455197.5);
	}

	public void testCaltoJD2010b() {
		commonCaltoJDTest(2010, 12, 31, 2455561.5);
	}

	public void testCaltoJD2009a() {
		commonCaltoJDTest(2009, 1, 1, 2454832.5);
	}

	public void testCaltoJD2009b() {
		commonCaltoJDTest(2009, 12, 31, 2455196.5);
	}

	// Checked with http://www.aavso.org/observing/aids/jdcalendar.shtml
	
	public void testCaltoJD2009c() {
		commonCaltoJDTest(2009, 9, 5, 2455079.5);
	}

	// Helper
	private void commonCaltoJDTest(int year, int month, double day, double expectedJD) {
		double jd = this.dateUtil.calendarToJD(year, month, day);
		assertEquals(expectedJD, jd);		
	}
}
