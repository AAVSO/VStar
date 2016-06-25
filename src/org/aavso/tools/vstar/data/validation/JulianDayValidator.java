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

import org.aavso.tools.vstar.data.DateInfo;
import org.aavso.tools.vstar.exception.ObservationValidationError;
import org.aavso.tools.vstar.util.locale.LocaleProps;

/**
 * This class validates provided text as a Julian Day.
 * 
 * TODO: Store a GregorianCalendar object?
 */
public class JulianDayValidator extends AbstractStringValidator<DateInfo> {

	// Integer or real time stamp value.
	// Note that we also allow the real format to be exponential in case
	// some (non AAVSO) observation sources require that.
	private final String pattern = "^(\\d+(\\.\\d+)?(?:(?:e|E)(?:\\-|\\+)\\d+)?)$";

	public static final boolean CAN_BE_EMPTY = true;

	private boolean canBeEmpty;

	private static final String KIND = LocaleProps
			.get("JULIAN_DAY_VALIDATOR_KIND");

	private final RegexValidator regexValidator;

	/**
	 * Constructor.
	 * 
	 * @param canBeEmpty
	 *            Can the Julian Day field be empty?
	 */
	public JulianDayValidator(boolean canBeEmpty) {
		super(KIND);
		this.regexValidator = new RegexValidator(pattern, KIND,
				LocaleProps.get("JULIAN_DAY_VALIDATOR_ERR_MSG"));
		this.canBeEmpty = canBeEmpty;
	}

	/**
	 * Constructor.
	 */
	public JulianDayValidator() {
		super(KIND);
		this.regexValidator = new RegexValidator(pattern, KIND,
				LocaleProps.get("JULIAN_DAY_VALIDATOR_ERR_MSG"));
		this.canBeEmpty = false;
	}

	/**
	 * Validate the specified string as a Julian Day.
	 * 
	 * @param str
	 *            The string to be validated.
	 * @return The returned DateInfo object if validation is successful.
	 * @throws ObservationValidationError
	 *             if validation is unsuccessful.
	 */
	public DateInfo validate(String str) throws ObservationValidationError {
		if (this.isLegallyEmpty(str))
			return null;

		String[] fields = this.regexValidator.validate(str);

		// By virtue of the regex pattern above,
		// this must parse as a double.
		double value = Double.parseDouble(fields[0]);

		return new DateInfo(value);
	}

	protected boolean canBeEmpty() {
		return this.canBeEmpty;
	}
}
