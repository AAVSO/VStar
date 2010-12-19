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
package org.aavso.tools.vstar.util.stats.anova;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math.MathException;
import org.apache.commons.math.stat.inference.OneWayAnova;
import org.apache.commons.math.stat.inference.OneWayAnovaImpl;

import junit.framework.TestCase;

public class CommonsMathAnovaTest extends TestCase {

	public CommonsMathAnovaTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	// A simple test of the Apache ANOVA library.
	public void testApacheAnova() {
		// See http://en.wikipedia.org/wiki/F-test for example data used here.
		double[] a1 = { 6, 8, 4, 5, 3, 4 };
		double[] a2 = { 8, 12, 9, 11, 6, 8 };
		double[] a3 = { 13, 9, 11, 8, 7, 12 };

		List<double[]> data = new ArrayList<double[]>();
		data.add(a1);
		data.add(a2);
		data.add(a3);

		OneWayAnova anova = new OneWayAnovaImpl();

		try {
			// 9.264705882352942
			double fValue = anova.anovaFValue(data);
			assertEquals("9.26", String.format("%1.2f", fValue));

			// 0.002398777329392865
			double pValue = anova.anovaPValue(data);
			assertEquals("0.002", String.format("%1.3f", pValue));

			// alpha value can be 0 < alpha <= 0.5
			boolean rejectNullHypothesis = anova.anovaTest(data, 0.1);
			assertTrue(rejectNullHypothesis);
		} catch (MathException e) {
			System.err.println(e.getMessage());
			fail();
		}
	}
}
