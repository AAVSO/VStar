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

import org.aavso.tools.vstar.data.Magnitude;
import org.aavso.tools.vstar.exception.ObservationValidationError;

/**
 * This class represents a magnitude value including uncertainty and
 * fainter-than values.
 */
public class MagnitudeFieldValidator implements IStringValidator<Magnitude> {

	private final int FAINTER_THAN_INDEX = 0;
	private final int MAG_INDEX = 1;
	private final int UNCERTAINTY_INDEX = 2;

	private final RegexValidator regexValidator;
	private final MagnitudeValueValidator magnitudeValueValidator;

	/**
	 * Constructor.
	 */
	public MagnitudeFieldValidator() {
		// Optional '<' prefix, followed by a real number (0.4, -4, 5.56),
		// and optionally followed by a ':' suffix. Note the use of the
		// non-capturing group (?:...) for the fractional part. We just want
		// to group this as an optional sub-pattern, not obtain it separately.
		this.regexValidator = new RegexValidator("^(<)?(\\-?\\d+(?:\\.\\d+)?)(:)?$",
				"Magnitude");
		this.magnitudeValueValidator = new MagnitudeValueValidator(
				new InclusiveRangePredicate(-5, 25));
	}

	public Magnitude validate(String str) throws ObservationValidationError {
		Magnitude mag = null;

		String[] fields = this.regexValidator.validate(str);

		// Here we determine the parts present in the magnitude field.
		// The magnitude value itself is non-optional whereas the fainter-than
		// and uncertainty parts are both optional, so we test for combinations
		// of these two optional parts.

		double magnitude = this.magnitudeValueValidator.validate(fields[MAG_INDEX]);

		if (fields[FAINTER_THAN_INDEX] != null
				&& fields[UNCERTAINTY_INDEX] != null) {
			// All 3 parts are present.
			mag = new Magnitude(true, true, magnitude);
		} else if (fields[FAINTER_THAN_INDEX] != null
				&& fields[UNCERTAINTY_INDEX] == null) {
			// Fainter-than and magnitude parts are present.
			mag = new Magnitude(true, false, magnitude);
		} else if (fields[FAINTER_THAN_INDEX] == null
				&& fields[UNCERTAINTY_INDEX] != null) {
			// Magnitude and uncertainty parts are present.
			mag = new Magnitude(false, true, magnitude);
		} else if (fields[FAINTER_THAN_INDEX] == null
				&& fields[UNCERTAINTY_INDEX] == null) {
			// Only the magnitude part is present.
			mag = new Magnitude(false, false, magnitude);
		}

		return mag;
	}
}
