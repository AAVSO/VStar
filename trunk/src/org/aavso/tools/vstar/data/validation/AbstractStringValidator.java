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
package org.aavso.tools.vstar.data.validation;

import org.aavso.tools.vstar.exception.ObservationValidationError;
import org.aavso.tools.vstar.exception.ObservationValidationWarning;

/**
 * This is the base class for all string-based validators.
 */
public abstract class AbstractStringValidator<T> {

	// The kind of field (e.g. name) we are validating.
	protected String kind;

	/**
	 * Constructor.
	 * 
	 * @param kind
	 *            The kind of entity (e.g. field name) we are validating.
	 */
	public AbstractStringValidator(String kind) {
		this.kind = kind;
	}

	/**
	 * Constructor.
	 */
	public AbstractStringValidator() {
		this(null);
	}

	/**
	 * Validate the supplied string, throwing an exception on failure.
	 * 
	 * @param str
	 *            The string to be validated.
	 * @throws ObservationValidationError
	 *             , ObservationValidationWarning
	 */
	abstract public T validate(String str) throws ObservationValidationError,
			ObservationValidationWarning;

	/**
	 * Is the supplied string legally empty or null?
	 * 
	 * @param str
	 *            The string to be validated.
	 * @return True if the string is allowed to be empty and is empty or null,
	 *         and False if the string is allowed to be empty but is NOT empty
	 *         or null.
	 * @throws ObservationValidationError
	 *             if the string is empty or null but canBeEmpty() returns
	 *             false.
	 * @precondition The string is either null, empty, or contains characters
	 *               besides whitespace.
	 */
	protected boolean isLegallyEmpty(String str)
			throws ObservationValidationError {
		if (str == null || "".equals(str)) {
			if (canBeEmpty()) {
				return true;
			} else {
				if (kind != null) {
					throw new ObservationValidationError("The " + kind
							+ " field cannot be empty.");
				} else {
					throw new ObservationValidationError();
				}
			}
		} else {
			return false;
		}
	}

	/**
	 * Can the string to be validated by this class be empty? Defaults to false.
	 * 
	 * @return True or False
	 */
	protected boolean canBeEmpty() {
		return false;
	}
}
