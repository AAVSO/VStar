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
 * The interface for all observation field matchers.
 */
// TODO: why not just replace with with AbstractObservationFieldMatcher?
// TODO: add tool-tip getter, e.g. for band (long name)
public interface IObservationFieldMatcher {

	/**
	 * Creates and returns an instance of an observation matcher for the current
	 * field matcher type. The returned matcher contains a value-to-be-matched
	 * against the expected type given the supplied string field value. If the
	 * latter cannot be converted to the expected type, null is returned. The
	 * match operator to be used is also included in the returned object.
	 * 
	 * @param fieldValue
	 *            The string field value to be converted to a match object.
	 * @param op
	 *            The match operator to be used.
	 * @return The created matcher or null if the field value does not conform
	 *         to the type required by the matcher.
	 */
	// TODO: should be able to genericise this class with T and use T
	// fieldValue instead of string; the onus for conversion of the
	// field-value would then be on the caller (e.g. filter dialog code)
	// and things would be more type-safe and in particular, null would 
	// not need to be returned for non-comforming field values.
	public abstract IObservationFieldMatcher create(String fieldValue,
			ObservationMatcherOp op);

	/**
	 * Does the specified observation match a test value?
	 * 
	 * @param ob
	 *            The observation under test.
	 * @return True or false.
	 */
	public abstract boolean matches(ValidObservation ob);

	/**
	 * The display name for this matcher.
	 */
	public abstract String getDisplayName();

	/**
	 * An array of operations supported by this matcher.
	 */
	public abstract ObservationMatcherOp[] getMatcherOps();

	/**
	 * What is the type of the field to be matched against?
	 * 
	 * @return A class representing a type.
	 */
	public abstract Class<?> getType();
}