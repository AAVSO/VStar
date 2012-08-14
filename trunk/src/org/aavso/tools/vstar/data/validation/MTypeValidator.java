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

import org.aavso.tools.vstar.data.MTypeType;
import org.aavso.tools.vstar.exception.ObservationValidationError;
import org.aavso.tools.vstar.util.locale.LocaleProps;

/**
 * A magnitude type (mtype) field validator.
 */
public class MTypeValidator extends AbstractStringValidator<MTypeType> {

	private static final String KIND = LocaleProps
			.get("MAGNITUDE_TYPE_VALIDATOR_KIND");

	private final RegexValidator regexValidator;

	/**
	 * Constructor.
	 */
	public MTypeValidator() {
		super(KIND);
		this.regexValidator = new RegexValidator("^(STD|DIFF|STEP)$", KIND);
	}

	public MTypeType validate(String str) throws ObservationValidationError {
		if (this.isLegallyEmpty(str))
			return null;

		String validatedStr = this.regexValidator.validate(str)[0];

		MTypeType type = null;

		if ("STD".equals(validatedStr)) {
			type = MTypeType.STD;
		} else if ("DIFF".equals(validatedStr)) {
			type = MTypeType.DIFF;
		} else if ("STEP".equals(validatedStr)) {
			type = MTypeType.STEP;
		}

		assert (type != null);

		return type;
	}

	protected boolean canBeEmpty() {
		return true;
	}
}
