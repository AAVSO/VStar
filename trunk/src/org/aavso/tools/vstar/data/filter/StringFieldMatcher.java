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
 * String code field matcher.
 */
public abstract class StringFieldMatcher extends
		AbstractObservationFieldMatcher<String> {

	private final static ObservationMatcherOp[] ops = {
			ObservationMatcherOp.EQUALS, ObservationMatcherOp.NOT_EQUALS,
			ObservationMatcherOp.CONTAINS };

	public StringFieldMatcher(String testValue, ObservationMatcherOp op,
			ObservationMatcherOp[] ops) {
		super(testValue, op, ops);
	}

	public StringFieldMatcher(String testValue, ObservationMatcherOp op) {
		this(testValue, op, ops);
	}

	protected StringFieldMatcher() {
		super(ops);
	}

	@Override
	public boolean matches(ValidObservation ob) {
		boolean result = false;

		String value = getValueUnderTest(ob);

		if (value == null || "".equals(value)) {
			if ("".equals(testValue)) {
				result = true;
			}
		} else {
			switch (op) {
			case EQUALS:
				result = value.equals(testValue);
				break;
			case NOT_EQUALS:
				result = !value.equals(testValue);
				break;
			case CONTAINS:
				result = value.contains(testValue);
				break;
			}
		}

		return result;
	}

	@Override
	public Class<?> getType() {
		return String.class;
	}
}
