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

	private final OptionalityFieldValidator optionalFieldValidator;

	/**
	 * Constructor
	 */
	public TransformedValidator() {
		optionalFieldValidator = new OptionalityFieldValidator(
				OptionalityFieldValidator.CAN_BE_EMPTY);
	}

	public Boolean validate(String str) throws ObservationValidationError {
		boolean isTransformed = false;

		String transformed = optionalFieldValidator.validate(str);

		// Legal values: yes, no, 0, 1, empty, null
		//
		// In fact, I think 1 and 0 will only ever appear in the database, but
		// we check for them anyway but don't advertise the fact. Ditto re:
		// empty field.
		//
		// See tracker
		// https://sourceforge.net/tracker/index.php?func=detail&aid=2915572&group_id=263306&atid=1152052

		// If null or empty, default to false.
		if (transformed != null) {
			transformed = transformed.toLowerCase();
			if (!"".equals(transformed)) {
				if ("yes".equalsIgnoreCase(transformed)
						|| "1".equals(transformed)) {
					isTransformed = true;
				} else if ("no".equalsIgnoreCase(transformed)
						|| "0".equals(transformed)) {
					isTransformed = false;
				} else {
					throw new ObservationValidationError(
							"Transformed field must contain 'yes' or 'no', or be empty.");
				}
			}
		}

		return isTransformed;
	}
}
