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
package org.aavso.tools.vstar.data.filter;

import junit.framework.TestCase;

import org.aavso.tools.vstar.data.Magnitude;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.data.filter.MagnitudeFieldMatcher;
import org.aavso.tools.vstar.data.filter.ObsCodeFieldMatcher;
import org.aavso.tools.vstar.data.filter.ObservationFilter;
import org.aavso.tools.vstar.data.filter.ObservationMatcherOp;

/**
 * Observation filtering unit test.
 */
public class ObservationFilterTest extends TestCase {

	public ObservationFilterTest(String name) {
		super(name);
	}

	// Valid observation matcher tests.

	public void testObsCodeFieldMatcherTest() {
		ObsCodeFieldMatcher matcher = new ObsCodeFieldMatcher("ABC",
				ObservationMatcherOp.EQUALS);

		ValidObservation ob = new ValidObservation();
		ob.setObsCode("ABC");

		assertTrue(matcher.matches(ob));
	}

	public void testMagnitudeMatcherTest1() {
		MagnitudeFieldMatcher matcher = new MagnitudeFieldMatcher(4.0,
				ObservationMatcherOp.EQUALS);
		
		ValidObservation ob = new ValidObservation();
		ob.setMagnitude(new Magnitude(4.0, 0));
		
		assertTrue(matcher.matches(ob));
	}

	public void testMagnitudeMatcherTest2() {
		MagnitudeFieldMatcher matcher = new MagnitudeFieldMatcher(4.0,
				ObservationMatcherOp.LESS_THAN);
		
		ValidObservation ob = new ValidObservation();
		ob.setMagnitude(new Magnitude(3.0, 0));
		
		assertTrue(matcher.matches(ob));
	}

	public void testMagnitudeMatcherTest3() {
		MagnitudeFieldMatcher matcher = new MagnitudeFieldMatcher(4.0,
				ObservationMatcherOp.GREATER_THAN);
		
		ValidObservation ob = new ValidObservation();
		ob.setMagnitude(new Magnitude(5.0, 0));
		
		assertTrue(matcher.matches(ob));
	}

	public void testMagnitudeMatcherTest4a() {
		MagnitudeFieldMatcher matcher = new MagnitudeFieldMatcher(4.0,
				ObservationMatcherOp.LESS_THAN_OR_EQUAL);
		
		ValidObservation ob = new ValidObservation();
		ob.setMagnitude(new Magnitude(3.0, 0));
		
		assertTrue(matcher.matches(ob));
	}

	public void testMagnitudeMatcherTest4b() {
		MagnitudeFieldMatcher matcher = new MagnitudeFieldMatcher(4.0,
				ObservationMatcherOp.LESS_THAN_OR_EQUAL);
		
		ValidObservation ob = new ValidObservation();
		ob.setMagnitude(new Magnitude(4.0, 0));
		
		assertTrue(matcher.matches(ob));
	}

	public void testMagnitudeMatcherTest5a() {
		MagnitudeFieldMatcher matcher = new MagnitudeFieldMatcher(4.0,
				ObservationMatcherOp.GREATER_THAN_OR_EQUAL);
		
		ValidObservation ob = new ValidObservation();
		ob.setMagnitude(new Magnitude(5.0, 0));
		
		assertTrue(matcher.matches(ob));
	}

	public void testMagnitudeMatcherTest5b() {
		MagnitudeFieldMatcher matcher = new MagnitudeFieldMatcher(4.0,
				ObservationMatcherOp.GREATER_THAN_OR_EQUAL);
		
		ValidObservation ob = new ValidObservation();
		ob.setMagnitude(new Magnitude(4.0, 0));
		
		assertTrue(matcher.matches(ob));
	}

	// Valid observation filter tests.

	public void testObservationFilterTest1() {
		ObsCodeFieldMatcher matcher1 = new ObsCodeFieldMatcher("ABC",
				ObservationMatcherOp.EQUALS);

		ObservationFilter filter = new ObservationFilter();
		filter.addMatcher(matcher1);

		ValidObservation ob = new ValidObservation();
		ob.setObsCode("ABC");

		assertTrue(filter.matches(ob));
	}

	public void testObservationFilterTest2() {
		ObsCodeFieldMatcher matcher1 = new ObsCodeFieldMatcher("ABC",
				ObservationMatcherOp.EQUALS);

		MagnitudeFieldMatcher matcher2 = new MagnitudeFieldMatcher(4.0,
				ObservationMatcherOp.LESS_THAN);
		
		ObservationFilter filter = new ObservationFilter();
		filter.addMatcher(matcher1);
		filter.addMatcher(matcher2);

		ValidObservation ob = new ValidObservation();
		ob.setObsCode("ABC");
		ob.setMagnitude(new Magnitude(3.0, 0));
		
		assertTrue(filter.matches(ob));
	}
}
