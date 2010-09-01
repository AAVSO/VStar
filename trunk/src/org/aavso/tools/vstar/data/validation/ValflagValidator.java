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

import org.aavso.tools.vstar.data.ValidationType;
import org.aavso.tools.vstar.exception.ObservationValidationError;

/**
 * This class validates a valflag.
 */
public class ValflagValidator extends AbstractStringValidator<ValidationType> {

	private static final String KIND = "validation flag";

	private final RegexValidator regexValidator;

	/**
	 * Constructor.
	 * 
	 * @param valflagPatternStr A regex pattern representing the
	 * alternations of permission valflags for this validator instance,
	 * e.g. "D" (simple format) or "G|V|D|P|Z" (AAVSO download format).
	 * This pattern string will be wrapped in a ^(...)$ to ensure that nothing
	 * else exists in the string, and that there is one capturing group.
	 */
	public ValflagValidator(String valflagPatternStr) {
		super(KIND);
		this.regexValidator = new RegexValidator("^(" + valflagPatternStr + ")$",
				KIND);
	}

	public ValidationType validate(String str) throws ObservationValidationError {
		if (this.isLegallyEmpty(str)) return null;

		String field = this.regexValidator.validate(str)[0];
		return ValidationType.getTypeFromFlag(field);
	}

	protected boolean canBeEmpty() {
		return true;
	}
}
