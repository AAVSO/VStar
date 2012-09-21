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
 * Object name field matcher.
 */
public class ObjectNameFieldMatcher extends StringFieldMatcher {

	public ObjectNameFieldMatcher(String testValue, ObservationMatcherOp op) {
		super(testValue, op);
	}

	public ObjectNameFieldMatcher() {
		super();
	}
	
	@Override
	protected String getValueUnderTest(ValidObservation ob) {
		return ob.getName();
	}

	@Override
	public IObservationFieldMatcher create(String fieldValue,
			ObservationMatcherOp op) {
		return new ObjectNameFieldMatcher(fieldValue, op);
	}

	@Override
	public String getDefaultTestValue() {
		return null;
	}

	@Override
	public String getDisplayName() {
		return "Object Name";
	}

	@Override
	public String getTestValueFromObservation(ValidObservation ob) {
		return ob.getName();
	}
}
