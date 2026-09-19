/**
 * VStar: a statistical analysis tool for variable star data.
 * Copyright (C) 2026  AAVSO (http://www.aavso.org/)
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

import java.util.ArrayList;
import java.util.List;

import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.ui.model.plot.JDCoordSource;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.optimization.GoalType;

import junit.framework.TestCase;

/**
 * Unit tests for {@link ApacheCommonsBrentOptimiserExtremaFinder} on a known
 * analytic model (upward-opening parabola).
 */
public class ApacheCommonsBrentOptimiserExtremaFinderTest extends TestCase {

	private static final double VERTEX_JD = 2450000.0;
	private static final double VERTEX_MAG = 12.0;

	public ApacheCommonsBrentOptimiserExtremaFinderTest(String name) {
		super(name);
	}

	private static double magAt(double jd) {
		double d = jd - VERTEX_JD;
		return d * d + VERTEX_MAG;
	}

	private static UnivariateRealFunction parabola() {
		// mag = (jd - VERTEX_JD)^2 + VERTEX_MAG  → numerical minimum at VERTEX
		return jd -> magAt(jd);
	}

	private static List<ValidObservation> sampleObs() {
		List<ValidObservation> obs = new ArrayList<ValidObservation>();
		for (int i = -10; i <= 10; i++) {
			double jd = VERTEX_JD + i;
			ValidObservation ob = new ValidObservation();
			ob.setJD(jd);
			ob.setMagnitude(new org.aavso.tools.vstar.data.Magnitude(magAt(jd), 0));
			obs.add(ob);
		}
		return obs;
	}

	public void testFindNumericalMinimumNearVertex() throws Exception {
		List<ValidObservation> obs = sampleObs();
		ApacheCommonsBrentOptimiserExtremaFinder finder = new ApacheCommonsBrentOptimiserExtremaFinder(
				obs, parabola(), JDCoordSource.instance, 0.0);

		finder.find(GoalType.MINIMIZE, new int[] { 0, obs.size() - 1 });

		assertNotNull(finder.getExtremeTime());
		assertNotNull(finder.getExtremeMag());
		assertEquals(VERTEX_JD, finder.getExtremeTime(), 1e-4);
		assertEquals(VERTEX_MAG, finder.getExtremeMag(), 1e-4);
	}

	public void testFindNumericalMaximumAtBracketEdgeForUpwardParabola() throws Exception {
		// On an upward parabola over a symmetric bracket, MAXIMIZE lands at an
		// endpoint (higher value). Use an asymmetric bracket so the right edge
		// is unambiguously higher.
		List<ValidObservation> obs = sampleObs();
		ApacheCommonsBrentOptimiserExtremaFinder finder = new ApacheCommonsBrentOptimiserExtremaFinder(
				obs, parabola(), JDCoordSource.instance, 0.0);

		finder.find(GoalType.MAXIMIZE, new int[] { 5, obs.size() - 1 });

		assertNotNull(finder.getExtremeTime());
		assertNotNull(finder.getExtremeMag());
		assertTrue(finder.getExtremeMag() > VERTEX_MAG);
	}
}
