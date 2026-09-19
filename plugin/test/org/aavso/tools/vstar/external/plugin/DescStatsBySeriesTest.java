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
package org.aavso.tools.vstar.external.plugin;

import java.util.ArrayList;
import java.util.List;

import org.aavso.tools.vstar.data.Magnitude;
import org.aavso.tools.vstar.data.ValidObservation;

import junit.framework.TestCase;

/**
 * Unit tests for non-UI helpers in {@link DescStatsBySeries}.
 */
public class DescStatsBySeriesTest extends TestCase {

	public DescStatsBySeriesTest(String name) {
		super(name);
	}

	public void testNumberInRangeSkipsDiscrepant() {
		List<ValidObservation> obs = mags(10.0, 11.0, 12.0);
		obs.get(1).setDiscrepant(true);
		assertEquals(2, DescStatsBySeries.calcNumberInRange(obs, 0, 2));
		assertEquals(3, DescStatsBySeries.calcNumberInRange(
				mags(10.0, 11.0, 12.0), 0, 2));
	}

	public void testNumberInRangeSubRange() {
		List<ValidObservation> obs = mags(1.0, 2.0, 3.0, 4.0);
		assertEquals(2, DescStatsBySeries.calcNumberInRange(obs, 1, 2));
	}

	public void testMedianOddCount() {
		List<ValidObservation> obs = mags(12.0, 10.0, 11.0);
		assertEquals(11.0,
				DescStatsBySeries.calcMagMedianInRange(obs, 0, 2), 1e-12);
	}

	public void testMedianEvenCount() {
		List<ValidObservation> obs = mags(10.0, 12.0, 11.0, 13.0);
		// Commons Math Median: average of two central values for even n
		assertEquals(11.5,
				DescStatsBySeries.calcMagMedianInRange(obs, 0, 3), 1e-12);
	}

	public void testMedianSkipsDiscrepant() {
		List<ValidObservation> obs = mags(10.0, 100.0, 12.0);
		obs.get(1).setDiscrepant(true);
		assertEquals(11.0,
				DescStatsBySeries.calcMagMedianInRange(obs, 0, 2), 1e-12);
	}

	public void testMedianAllDiscrepantIsNaN() {
		List<ValidObservation> obs = mags(10.0, 11.0);
		obs.get(0).setDiscrepant(true);
		obs.get(1).setDiscrepant(true);
		assertTrue(Double.isNaN(
				DescStatsBySeries.calcMagMedianInRange(obs, 0, 1)));
	}

	private static List<ValidObservation> mags(double... values) {
		List<ValidObservation> obs = new ArrayList<ValidObservation>();
		for (int i = 0; i < values.length; i++) {
			ValidObservation ob = new ValidObservation();
			ob.setJD(2459000.0 + i);
			ob.setMagnitude(new Magnitude(values[i], 0.01));
			obs.add(ob);
		}
		return obs;
	}
}
