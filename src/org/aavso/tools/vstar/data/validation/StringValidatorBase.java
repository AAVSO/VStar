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

/**
 * This is the base class for all string-based validators.
 */
public abstract class StringValidatorBase<T> {

	/**
	 * Validate the supplied string, throwing an exception on failure.
	 * 
	 * @param str The string to be validated.
	 * @throws ObservationValidationError
	 * 
	 * TODO: default to checking for legal empty strings?
	 */
	abstract public T validate(String str) throws ObservationValidationError;

	/**
	 * Is the supplied string legally empty or null?
	 * 
	 * @param str The string to be validated.
	 * @return True or False
	 * @throws ObservationValidationError if the string is empty or null but canBeEmpty() 
	 * returns false.
	 * @precondition The string is either null, empty, or contains characters besides whitespace. 
	 */
	protected boolean isLegallyEmpty(String str) throws ObservationValidationError {
		if (str == null || "".equals(str)) {
			if (canBeEmpty()) {
				return true;
			} else {
				throw new ObservationValidationError();
			}
		} else {
			return false;
		}
	}
	
	/**
	 * Can the string to be validated by this class be empty?
	 * TODO: Default to false?
	 * @return True or False
	 */
	abstract protected boolean canBeEmpty();
}	
