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

import org.aavso.tools.vstar.data.ValidObservation;

/**
 * Double-precision value field matcher.
 */
public abstract class DoubleFieldMatcher extends
		AbstractObservationFieldMatcher<Double> {

	private final static ObservationMatcherOp[] ops = {
			ObservationMatcherOp.EQUALS, ObservationMatcherOp.NOT_EQUALS,
			ObservationMatcherOp.LESS_THAN,
			ObservationMatcherOp.GREATER_THAN,
			ObservationMatcherOp.GREATER_THAN_OR_EQUAL,
			ObservationMatcherOp.LESS_THAN_OR_EQUAL };

	public DoubleFieldMatcher(Double testValue, ObservationMatcherOp op,
			ObservationMatcherOp[] ops) {
		super(testValue, op, ops);
	}

	public DoubleFieldMatcher(Double testValue, ObservationMatcherOp op) {
		this(testValue, op, ops);
	}

	public DoubleFieldMatcher() {
		super(ops);
	}
	
	@Override
	public boolean matches(ValidObservation ob) {
		boolean result = false;

		int comparison = getValueUnderTest(ob).compareTo(testValue);

		switch (op) {
		case EQUALS:
			result = comparison == 0;
			break;
		case NOT_EQUALS:
			result = comparison != 0;
			break;
		case LESS_THAN:
			result = comparison < 0;
			break;
		case GREATER_THAN:
			result = comparison > 0;
			break;
		case LESS_THAN_OR_EQUAL:
			result = comparison <= 0;
			break;
		case GREATER_THAN_OR_EQUAL:
			result = comparison >= 0;
			break;
		}

		return result;
	}
	
	@Override
	public Class<?> getType() {
		return Double.class;
	}
}
