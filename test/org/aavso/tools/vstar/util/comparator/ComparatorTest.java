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
package org.aavso.tools.vstar.util.comparator;

import java.util.Locale;

import junit.framework.TestCase;

import org.aavso.tools.vstar.data.ValidObservation;

public class ComparatorTest extends TestCase {

	public ComparatorTest(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		Locale.setDefault(Locale.ENGLISH);
	}

	public void testDoublesLessThan() {
		DoubleComparator c = new DoubleComparator();
		assertTrue(c.compare(1.0, 2.0) < 0);
	}

	public void testDoublesGreaterThan() {
		DoubleComparator c = new DoubleComparator();
		assertTrue(c.compare(2.0, 1.0) > 0);
	}

	public void testDoublesEqual() {
		DoubleComparator c = new DoubleComparator();
		assertEquals(0, c.compare(1.5, 1.5));
	}

	public void testJDLessThan() {
		JDComparator c = new JDComparator();
		ValidObservation ob1 = new ValidObservation();
		ValidObservation ob2 = new ValidObservation();
		ob1.setJD(2450000.0);
		ob2.setJD(2451000.0);
		assertTrue(c.compare(ob1, ob2) < 0);
	}

	public void testJDGreaterThan() {
		JDComparator c = new JDComparator();
		ValidObservation ob1 = new ValidObservation();
		ValidObservation ob2 = new ValidObservation();
		ob1.setJD(2451000.0);
		ob2.setJD(2450000.0);
		assertTrue(c.compare(ob1, ob2) > 0);
	}

	public void testJDEqual() {
		JDComparator c = new JDComparator();
		ValidObservation ob1 = new ValidObservation();
		ValidObservation ob2 = new ValidObservation();
		ob1.setJD(2455000.0);
		ob2.setJD(2455000.0);
		assertEquals(0, c.compare(ob1, ob2));
	}

	public void testStringDoublesLessThan() {
		DoubleAsStringComparator c = new DoubleAsStringComparator();
		assertTrue(c.compare("1.5", "2.5") < 0);
	}

	public void testStringDoublesGreaterThan() {
		DoubleAsStringComparator c = new DoubleAsStringComparator();
		assertTrue(c.compare("10.0", "5.0") > 0);
	}

	public void testStringDoublesEqual() {
		DoubleAsStringComparator c = new DoubleAsStringComparator();
		assertEquals(0, c.compare("3.14", "3.14"));
	}

	public void testSingleton() {
		FormattedDoubleComparator a = FormattedDoubleComparator.getInstance();
		FormattedDoubleComparator b = FormattedDoubleComparator.getInstance();
		assertNotNull(a);
		assertSame(a, b);
	}

	public void testCompare() {
		FormattedDoubleComparator c = FormattedDoubleComparator.getInstance();
		assertTrue(c.compare("1.0", "2.0") < 0);
	}

	public void testStandardPhaseLessThan() {
		StandardPhaseComparator c = StandardPhaseComparator.instance;
		ValidObservation o1 = new ValidObservation();
		ValidObservation o2 = new ValidObservation();
		o1.setStandardPhase(0.1);
		o2.setStandardPhase(0.9);
		assertTrue(c.compare(o1, o2) < 0);
	}

	public void testStandardPhaseEqual() {
		StandardPhaseComparator c = StandardPhaseComparator.instance;
		ValidObservation o1 = new ValidObservation();
		ValidObservation o2 = new ValidObservation();
		o1.setStandardPhase(0.5);
		o2.setStandardPhase(0.5);
		assertEquals(0, c.compare(o1, o2));
	}

	public void testPreviousCyclePhaseLessThan() {
		PreviousCyclePhaseComparator c = PreviousCyclePhaseComparator.instance;
		ValidObservation o1 = new ValidObservation();
		ValidObservation o2 = new ValidObservation();
		o1.setPreviousCyclePhase(0.05);
		o2.setPreviousCyclePhase(0.95);
		assertTrue(c.compare(o1, o2) < 0);
	}

	public void testPreviousCyclePhaseEqual() {
		PreviousCyclePhaseComparator c = PreviousCyclePhaseComparator.instance;
		ValidObservation o1 = new ValidObservation();
		ValidObservation o2 = new ValidObservation();
		o1.setPreviousCyclePhase(0.25);
		o2.setPreviousCyclePhase(0.25);
		assertEquals(0, c.compare(o1, o2));
	}
}
