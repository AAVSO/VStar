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
import org.aavso.tools.vstar.util.locale.NumberParser;

/**
 * A magnitude field matcher.
 */
public class MagnitudeFieldMatcher extends DoubleFieldMatcher {

	public MagnitudeFieldMatcher(Double testValue, ObservationMatcherOp op) {
		super(testValue, op);
	}

	public MagnitudeFieldMatcher() {
		super();
	}

	@Override
	public IObservationFieldMatcher create(String fieldValue,
			ObservationMatcherOp op) {
		IObservationFieldMatcher matcher = null;
		
		try {
			Double value = NumberParser.parseDouble(fieldValue);
			matcher = new MagnitudeFieldMatcher(value, op);
		} catch (NumberFormatException e) {
			// Nothing to do but return null.
		}
		
		return matcher;
	}

	@Override
	protected Double getValueUnderTest(ValidObservation ob) {
		// Magnitude is mandatory; it cannot be null.
		return ob.getMag();
	}
	
	@Override
	public String getDisplayName() {
		return "Magnitude";
	}

	@Override
	public String getDefaultTestValue() {
		return null;
	}
}
