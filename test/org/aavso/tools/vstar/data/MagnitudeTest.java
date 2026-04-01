package org.aavso.tools.vstar.data;

import junit.framework.TestCase;

public class MagnitudeTest extends TestCase {

	public MagnitudeTest(String name) {
		super(name);
	}

	public void testGetMagValue() {
		Magnitude m = new Magnitude(12.34, MagnitudeModifier.NO_DELTA, false, 0);
		assertEquals(12.34, m.getMagValue(), 0.0);
	}

	public void testFainterThan() {
		Magnitude m = new Magnitude(10.0, MagnitudeModifier.FAINTER_THAN, false);
		assertTrue(m.isFainterThan());
		assertFalse(m.isBrighterThan());
	}

	public void testBrighterThan() {
		Magnitude m = new Magnitude(10.0, MagnitudeModifier.BRIGHTER_THAN, false);
		assertTrue(m.isBrighterThan());
		assertFalse(m.isFainterThan());
	}

	public void testNoDelta() {
		Magnitude m = new Magnitude(10.0, MagnitudeModifier.NO_DELTA, false);
		assertFalse(m.isFainterThan());
		assertFalse(m.isBrighterThan());
	}

	public void testUncertainty() {
		Magnitude m = new Magnitude(10.0, MagnitudeModifier.NO_DELTA, false, 0.03);
		assertEquals(0.03, m.getUncertainty(), 0.0);
	}

	public void testSetUncertainty() {
		Magnitude m = new Magnitude(10.0, MagnitudeModifier.NO_DELTA, false, 0);
		m.setUncertainty(0.05);
		assertEquals(0.05, m.getUncertainty(), 0.0);
	}

	public void testCopy() {
		Magnitude m = new Magnitude(9.0, MagnitudeModifier.FAINTER_THAN, true, 0.01);
		Magnitude c = m.copy();
		assertNotSame(m, c);
		assertTrue(m.equals(c));
	}

	public void testEqualsIdentical() {
		Magnitude a = new Magnitude(11.0, MagnitudeModifier.NO_DELTA, false, 0.02);
		Magnitude b = new Magnitude(11.0, MagnitudeModifier.NO_DELTA, false, 0.02);
		assertTrue(a.equals(b));
	}

	public void testNotEqualsDifferentMag() {
		Magnitude a = new Magnitude(11.0, MagnitudeModifier.NO_DELTA, false, 0.02);
		Magnitude b = new Magnitude(12.0, MagnitudeModifier.NO_DELTA, false, 0.02);
		assertFalse(a.equals(b));
	}

	public void testNotEqualsDifferentModifier() {
		Magnitude a = new Magnitude(11.0, MagnitudeModifier.NO_DELTA, false, 0.02);
		Magnitude b = new Magnitude(11.0, MagnitudeModifier.FAINTER_THAN, false, 0.02);
		assertFalse(a.equals(b));
	}

	public void testNotEqualsNull() {
		Magnitude a = new Magnitude(11.0, MagnitudeModifier.NO_DELTA, false, 0.02);
		assertFalse(a.equals(null));
	}

	public void testHashCodeConsistency() {
		Magnitude a = new Magnitude(11.0, MagnitudeModifier.NO_DELTA, false, 0.02);
		Magnitude b = new Magnitude(11.0, MagnitudeModifier.NO_DELTA, false, 0.02);
		assertEquals(a.hashCode(), b.hashCode());
	}

	public void testTwoArgConstructor() {
		Magnitude m = new Magnitude(10.5, 0.02);
		assertEquals(10.5, m.getMagValue(), 0.0);
		assertEquals(MagnitudeModifier.NO_DELTA, m.getMagModifier());
		assertFalse(m.isUncertain());
		assertEquals(0.02, m.getUncertainty(), 0.0);
	}
}
