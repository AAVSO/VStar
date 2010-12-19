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
package org.aavso.tools.vstar.util.comparator;

import java.util.Arrays;

import junit.framework.TestCase;

/**
 * Power-index pair comparator test.
 */
public class RankedIndexPairComparatorTest extends TestCase {

	public RankedIndexPairComparatorTest(String name) {
		super(name);
	}

	public void testPairs1() {
		double[][] pairs = { { 1, 0 }, { 3, 1 }, { 2, 2 } };
		double[][] expected = { { 3, 1 }, { 2, 2 }, { 1, 0 } };

		Arrays.sort(pairs, RankedIndexPairComparator.instance);

		for (int i = 0; i < pairs.length; i++) {
			assertEquals(expected[i][0], pairs[i][0]);
			assertEquals(expected[i][1], pairs[i][1]);
		}
	}
}
