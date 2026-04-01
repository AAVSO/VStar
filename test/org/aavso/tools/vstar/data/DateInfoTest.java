package org.aavso.tools.vstar.data;

import junit.framework.TestCase;

public class DateInfoTest extends TestCase {

	public DateInfoTest(String name) {
		super(name);
	}

	public void testGetJulianDay() {
		DateInfo d = new DateInfo(2451545.0);
		assertEquals(2451545.0, d.getJulianDay(), 0.0);
	}

	public void testGetCalendarDate() {
		DateInfo d = new DateInfo(2451545.0);
		assertEquals("2000-01-01", d.getCalendarDate());
	}

	public void testEquals() {
		DateInfo a = new DateInfo(2451545.0);
		DateInfo b = new DateInfo(2451545.0);
		assertTrue(a.equals(b));
	}

	public void testNotEquals() {
		DateInfo a = new DateInfo(2451545.0);
		DateInfo b = new DateInfo(2451546.0);
		assertFalse(a.equals(b));
	}

	public void testNotEqualsNull() {
		DateInfo a = new DateInfo(2451545.0);
		assertFalse(a.equals(null));
	}

	public void testHashCodeConsistency() {
		DateInfo a = new DateInfo(2451545.0);
		DateInfo b = new DateInfo(2451545.0);
		assertEquals(a.hashCode(), b.hashCode());
	}

	public void testToString() {
		DateInfo d = new DateInfo(2451545.0);
		String s = d.toString();
		assertTrue(s.contains("2451545"));
		assertTrue(s.contains(d.getCalendarDate()));
	}
}
