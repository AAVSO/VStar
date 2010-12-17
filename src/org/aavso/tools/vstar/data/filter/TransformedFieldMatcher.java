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
 * A "transformed" field matcher.
 */
public class TransformedFieldMatcher extends BooleanFieldMatcher {

	public TransformedFieldMatcher(Boolean testValue, ObservationMatcherOp op) {
		super(testValue, op);
	}

	public TransformedFieldMatcher() {
		super();
	}

	@Override
	protected Boolean getValueUnderTest(ValidObservation ob) {
		// Defaults to false, so it's safe not to check for null.
		return ob.isTransformed();
	}

	@Override
	public IObservationFieldMatcher create(String fieldValue,
			ObservationMatcherOp op) {
		IObservationFieldMatcher matcher = null;

		Boolean value = false;
		if ("no".equalsIgnoreCase(fieldValue)) {
			value = false;
		} else if ("yes".equalsIgnoreCase(fieldValue)) {
			value = true;
		} else {
			value = Boolean.parseBoolean(fieldValue);
		}
		matcher = new TransformedFieldMatcher(value, op);

		return matcher;
	}

	@Override
	public String getDisplayName() {
		return "Transformed";
	}

	@Override
	public String getDefaultTestValue() {
		return null;
	}

	@Override
	public String getTestValueFromObservation(ValidObservation ob) {
		return ob.isTransformed() + "";
	}
}
