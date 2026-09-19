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
package org.aavso.tools.vstar.data.filter;

import junit.framework.TestCase;

/**
 * Example unit tests for {@link ObservationMatcherOp} string round-trips.
 */
public class ObservationMatcherOpTest extends TestCase {

	public ObservationMatcherOpTest(String name) {
		super(name);
	}

	public void testFromStringHumanReadableRoundTrip() {
		for (ObservationMatcherOp op : ObservationMatcherOp.values()) {
			assertEquals(op, ObservationMatcherOp.fromString(op.toString()));
		}
	}

	public void testToParsableStringKnownTokens() {
		assertEquals("=", ObservationMatcherOp.EQUALS.toParsableString());
		assertEquals("<>", ObservationMatcherOp.NOT_EQUALS.toParsableString());
		assertEquals("contains", ObservationMatcherOp.CONTAINS.toParsableString());
		assertEquals("<", ObservationMatcherOp.LESS_THAN.toParsableString());
		assertEquals(">", ObservationMatcherOp.GREATER_THAN.toParsableString());
		assertEquals("<=", ObservationMatcherOp.LESS_THAN_OR_EQUAL.toParsableString());
		assertEquals(">=", ObservationMatcherOp.GREATER_THAN_OR_EQUAL.toParsableString());
	}

	public void testFromStringUnknownReturnsNull() {
		assertNull(ObservationMatcherOp.fromString("bogus"));
		assertNull(ObservationMatcherOp.fromString(""));
	}
}
