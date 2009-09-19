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
 * This class validates the 'transformed' field.
 */
public class TransformedValidator extends AbstractStringValidator<Boolean> {

	private final OptionalityFieldValidator nonOptionalFieldValidator;

	/**
	 * Constructor
	 */
	public TransformedValidator() {
		this.nonOptionalFieldValidator = new OptionalityFieldValidator(
				OptionalityFieldValidator.CANNOT_BE_EMPTY);
	}

	public Boolean validate(String str) throws ObservationValidationError {
		boolean isTransformed = false;

		// It's safe to call toLower() below since an exception will
		// be thrown if the field is null.

		String transformed = nonOptionalFieldValidator.validate(str)
				.toLowerCase();
		
		if ("yes".equals(transformed)) {
			isTransformed = true;
		} else if ("no".equals(transformed)) {
			isTransformed = false;
		} else {
			throw new ObservationValidationError(
					"Transformed field must contain 'yes' or 'no'.");
		}

		return isTransformed;
	}

}
