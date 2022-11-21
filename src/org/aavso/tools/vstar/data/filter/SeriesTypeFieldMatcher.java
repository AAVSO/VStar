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

import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;

/**
 * A series type field (band, series) matcher.
 */
public class SeriesTypeFieldMatcher extends
		AbstractObservationFieldMatcher<SeriesType> {

	private Kind kind;

	public enum Kind {
		BAND("Band"), SERIES("Series");
		public String name;
		Kind(String name) { this.name = name; }
	}

	public SeriesTypeFieldMatcher(Kind kind, SeriesType testValue, ObservationMatcherOp op) {
		super(testValue, op, ops);
		this.kind = kind;
	}

	public SeriesTypeFieldMatcher(Kind kind) {
		super(ops);
		this.kind = kind;
	}

	private final static ObservationMatcherOp[] ops = {
			ObservationMatcherOp.EQUALS, ObservationMatcherOp.NOT_EQUALS };

	@Override
	protected SeriesType getValueUnderTest(ValidObservation ob) {
		SeriesType val = null;
		if (kind == Kind.BAND) {
			val = ob.getBand();
		} else if (kind == Kind.SERIES) {
			val = ob.getSeries();
		}
		return (val == null) ? SeriesType.getDefault() : val;
	}

	@Override
	public IObservationFieldMatcher create(String fieldValue,
			ObservationMatcherOp op) {
		IObservationFieldMatcher matcher = null;

		// Currently requires long band name to be specified.
		if (!"".equals(fieldValue)) {
			SeriesType type = SeriesType.getSeriesFromDescription(fieldValue);
			matcher = new SeriesTypeFieldMatcher(kind, type, op);

			// Check that we didn't classify something else as "unspecified".
			if (type == SeriesType.Unspecified
					&& !fieldValue.equalsIgnoreCase("unspecified")) {
				matcher = null;
			}
		}

		return matcher;
	}

	@Override
	public boolean matches(ValidObservation ob) {
		boolean result = false;

		SeriesType value = getValueUnderTest(ob);

		switch (op) {
		case EQUALS:
			result = value == testValue;
			break;
		case NOT_EQUALS:
			result = value != testValue;
			break;
		}

		return result;
	}

	@Override
	public String getDisplayName() {
		return kind.name;
	}

	@Override
	public Class<?> getType() {
		return SeriesType.class;
	}

	@Override
	public String getDefaultTestValue() {
		return null;
	}

	@Override
	public String getTestValueFromObservation(ValidObservation ob) {
		return getValueUnderTest(ob).toString();
	}
}
