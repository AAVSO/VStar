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
 * Observer code field matcher.
 */
public class ObsCodeFieldMatcher extends
		AbstractObservationFieldMatcher<String> {

	private final static ObservationMatcherOp[] ops = { ObservationMatcherOp.EQUALS };

	public ObsCodeFieldMatcher(String testValue, ObservationMatcherOp op) {
		super(testValue, op, ops);
	}

	protected ObsCodeFieldMatcher() {
		super(ops);
	}

	/**
	 * @see org.aavso.tools.vstar.data.filter.IObservationFieldMatcher#matches(org.aavso.tools.vstar.data.ValidObservation)
	 */
	public boolean matches(ValidObservation ob) {
		boolean result = false;

		switch (op) {
		case EQUALS:
			result = testValue.equals(ob.getObsCode());
		}

		return result;
	}

	/**
	 * @see org.aavso.tools.vstar.data.filter.IObservationFieldMatcher#getDisplayName()
	 */
	public String getDisplayName() {
		return "observer code";
	}
}
