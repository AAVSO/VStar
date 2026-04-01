package org.aavso.tools.vstar.util;

import junit.framework.TestCase;

public class UtilTest extends TestCase {

	public UtilTest(String name) {
		super(name);
	}

	public void testImpFalseFalse() {
		assertTrue(Logic.imp(false, false));
	}

	public void testImpFalseTrue() {
		assertTrue(Logic.imp(false, true));
	}

	public void testImpTrueFalse() {
		assertFalse(Logic.imp(true, false));
	}

	public void testImpTrueTrue() {
		assertTrue(Logic.imp(true, true));
	}

	public void testAbsoluteClose() {
		assertTrue(Tolerance.areClose(1.0, 1.05, 0.1, true));
	}

	public void testAbsoluteNotClose() {
		assertFalse(Tolerance.areClose(1.0, 1.2, 0.1, true));
	}

	public void testAbsoluteExactlyEqual() {
		assertTrue(Tolerance.areClose(5.0, 5.0, 0.001, true));
	}

	public void testRelativeClose() {
		assertTrue(Tolerance.areClose(100.0, 101.0, 0.02, false));
	}

	public void testRelativeNotClose() {
		assertFalse(Tolerance.areClose(100.0, 110.0, 0.05, false));
	}

	public void testRelativeExactlyEqual() {
		assertTrue(Tolerance.areClose(0.0, 0.0, 0.001, false));
	}

	public void testRelativeZeroAndNonZero() {
		assertFalse(Tolerance.areClose(0.0, 0.001, 0.1, false));
	}

	public void testPairEquals() {
		Pair<String, Integer> p1 = new Pair<String, Integer>("a", Integer.valueOf(1));
		Pair<String, Integer> p2 = new Pair<String, Integer>("a", Integer.valueOf(1));
		assertTrue(p1.equals(p2));
	}

	public void testPairNotEquals() {
		Pair<String, Integer> p1 = new Pair<String, Integer>("a", Integer.valueOf(1));
		Pair<String, Integer> p2 = new Pair<String, Integer>("b", Integer.valueOf(1));
		assertFalse(p1.equals(p2));
	}

	public void testPairHashCodeConsistent() {
		Pair<String, Integer> p1 = new Pair<String, Integer>("a", Integer.valueOf(1));
		Pair<String, Integer> p2 = new Pair<String, Integer>("a", Integer.valueOf(1));
		assertEquals(p1.hashCode(), p2.hashCode());
	}

	public void testPairEqualsNull() {
		Pair<String, Integer> p = new Pair<String, Integer>("a", Integer.valueOf(1));
		assertFalse(p.equals(null));
	}

	public void testPairEqualsSelf() {
		Pair<String, Integer> p = new Pair<String, Integer>("a", Integer.valueOf(1));
		assertTrue(p.equals(p));
	}

	public void testPairNullFields() {
		Pair<String, String> a = new Pair<String, String>(null, "x");
		Pair<String, String> b = new Pair<String, String>(null, "x");
		assertTrue(a.equals(b));
		Pair<String, String> c = new Pair<String, String>("x", null);
		Pair<String, String> d = new Pair<String, String>("x", null);
		assertTrue(c.equals(d));
		Pair<String, String> z = new Pair<String, String>(null, null);
		Pair<String, String> w = new Pair<String, String>(null, null);
		assertTrue(z.equals(w));
	}

	public void testTripleEquals() {
		Triple<String, Integer, Double> t1 = new Triple<String, Integer, Double>("a",
				Integer.valueOf(1), Double.valueOf(2.0));
		Triple<String, Integer, Double> t2 = new Triple<String, Integer, Double>("a",
				Integer.valueOf(1), Double.valueOf(2.0));
		assertTrue(t1.equals(t2));
	}

	public void testTripleNotEquals() {
		Triple<String, Integer, Double> t1 = new Triple<String, Integer, Double>("a",
				Integer.valueOf(1), Double.valueOf(2.0));
		Triple<String, Integer, Double> t2 = new Triple<String, Integer, Double>("a",
				Integer.valueOf(1), Double.valueOf(3.0));
		assertFalse(t1.equals(t2));
	}

	public void testTripleHashCodeConsistent() {
		Triple<String, Integer, Double> t1 = new Triple<String, Integer, Double>("a",
				Integer.valueOf(1), Double.valueOf(2.0));
		Triple<String, Integer, Double> t2 = new Triple<String, Integer, Double>("a",
				Integer.valueOf(1), Double.valueOf(2.0));
		assertEquals(t1.hashCode(), t2.hashCode());
	}

	public void testTripleNullFields() {
		Triple<String, String, String> a = new Triple<String, String, String>(null, "b", "c");
		Triple<String, String, String> b = new Triple<String, String, String>(null, "b", "c");
		assertTrue(a.equals(b));
	}

	public void testTripleEqualsSelf() {
		Triple<String, Integer, Double> t = new Triple<String, Integer, Double>("a",
				Integer.valueOf(1), Double.valueOf(2.0));
		assertTrue(t.equals(t));
	}
}
