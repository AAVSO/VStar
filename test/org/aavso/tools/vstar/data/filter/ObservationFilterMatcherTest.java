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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.aavso.tools.vstar.data.Magnitude;
import org.aavso.tools.vstar.data.MagnitudeModifier;
import org.aavso.tools.vstar.data.ValidObservation;

public class ObservationFilterMatcherTest extends TestCase {

	public ObservationFilterMatcherTest(String name) {
		super(name);
	}

	private static ValidObservation makeBaselineObservation() {
		ValidObservation ob = new ValidObservation();
		ob.setJD(2450001.0);
		ob.setMagnitude(new Magnitude(10.5, MagnitudeModifier.NO_DELTA, false));
		return ob;
	}

	public void testEmptyFilterMatchesAll() {
		List<ValidObservation> obs = new ArrayList<ValidObservation>();
		obs.add(makeBaselineObservation());
		ValidObservation second = makeBaselineObservation();
		second.setJD(2450002.0);
		obs.add(second);
		ObservationFilter filter = new ObservationFilter();
		Set<ValidObservation> out = filter.getFilteredObservations(obs, true,
				true, true);
		assertEquals(2, out.size());
	}

	public void testJDMatcherEquals() {
		JDFieldMatcher template = new JDFieldMatcher();
		IObservationFieldMatcher matcher = template.create("2450001.0",
				ObservationMatcherOp.EQUALS);
		assertNotNull(matcher);
		ValidObservation match = makeBaselineObservation();
		ValidObservation noMatch = makeBaselineObservation();
		noMatch.setJD(2450999.0);
		assertTrue(matcher.matches(match));
		assertFalse(matcher.matches(noMatch));
	}

	public void testFilterWithNoMatchers() {
		List<ValidObservation> obs = new ArrayList<ValidObservation>();
		ValidObservation a = makeBaselineObservation();
		ValidObservation b = makeBaselineObservation();
		b.setJD(2450100.0);
		obs.add(a);
		obs.add(b);
		ObservationFilter filter = new ObservationFilter();
		Set<ValidObservation> out = filter.getFilteredObservations(obs, true,
				true, true);
		assertTrue(out.contains(a));
		assertTrue(out.contains(b));
		assertEquals(2, out.size());
	}

	public void testFilterExcludesFainterThan() {
		ValidObservation ob = makeBaselineObservation();
		ob.setMagnitude(new Magnitude(10.0, MagnitudeModifier.FAINTER_THAN,
				false));
		List<ValidObservation> obs = new ArrayList<ValidObservation>();
		obs.add(ob);
		ObservationFilter filter = new ObservationFilter();
		Set<ValidObservation> out = filter.getFilteredObservations(obs, false,
				true, true);
		assertEquals(0, out.size());
	}

	public void testFilterIncludesFainterThan() {
		ValidObservation ob = makeBaselineObservation();
		ob.setMagnitude(new Magnitude(10.0, MagnitudeModifier.FAINTER_THAN,
				false));
		List<ValidObservation> obs = new ArrayList<ValidObservation>();
		obs.add(ob);
		ObservationFilter filter = new ObservationFilter();
		Set<ValidObservation> out = filter.getFilteredObservations(obs, true,
				true, true);
		assertEquals(1, out.size());
		assertTrue(out.contains(ob));
	}

	public void testFilterExcludesDiscrepant() {
		ValidObservation ob = makeBaselineObservation();
		ob.setDiscrepant(true);
		List<ValidObservation> obs = new ArrayList<ValidObservation>();
		obs.add(ob);
		ObservationFilter filter = new ObservationFilter();
		Set<ValidObservation> out = filter.getFilteredObservations(obs, true,
				false, true);
		assertEquals(0, out.size());
	}

	public void testFilterExcludesExcluded() {
		ValidObservation ob = makeBaselineObservation();
		ob.setExcluded(true);
		List<ValidObservation> obs = new ArrayList<ValidObservation>();
		obs.add(ob);
		ObservationFilter filter = new ObservationFilter();
		Set<ValidObservation> out = filter.getFilteredObservations(obs, true,
				true, false);
		assertEquals(0, out.size());
	}

	public void testFilterReset() {
		ObservationFilter filter = new ObservationFilter();
		filter.addMatcher(new ObsCodeFieldMatcher("X",
				ObservationMatcherOp.EQUALS));
		filter.reset();
		assertTrue(filter.getMatchers().isEmpty());
	}

	public void testFilterMultipleObs() {
		ValidObservation normal = makeBaselineObservation();
		ValidObservation faint = makeBaselineObservation();
		faint.setJD(2450002.0);
		faint.setMagnitude(new Magnitude(11.0, MagnitudeModifier.FAINTER_THAN,
				false));
		ValidObservation disc = makeBaselineObservation();
		disc.setJD(2450003.0);
		disc.setDiscrepant(true);
		List<ValidObservation> obs = new ArrayList<ValidObservation>();
		obs.add(normal);
		obs.add(faint);
		obs.add(disc);
		ObservationFilter filter = new ObservationFilter();
		Set<ValidObservation> all = filter.getFilteredObservations(obs, true,
				true, true);
		assertEquals(3, all.size());
		Set<ValidObservation> noFaint = filter.getFilteredObservations(obs,
				false, true, true);
		assertEquals(2, noFaint.size());
		assertTrue(noFaint.contains(normal));
		assertTrue(noFaint.contains(disc));
		Set<ValidObservation> noDisc = filter.getFilteredObservations(obs, true,
				false, true);
		assertEquals(2, noDisc.size());
		assertTrue(noDisc.contains(normal));
		assertTrue(noDisc.contains(faint));
	}
}
