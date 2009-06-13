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
import org.aavso.tools.vstar.data.Magnitude;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.exception.ObservationValidationError;

/**
 * This class accepts a line of text for tokenising, validation, and
 * ValidObservation instance creation.
 */
public class SimpleTextFormatValidator implements
		IStringValidator<ValidObservation> {

	private final int JD_FIELD = 0;
	private final int MAG_FIELD = 1;

	private final JulianDayValidator julianDayValidator;
	private final MagnitudeFieldValidator magnitudeFieldValidator;
	private final MagnitudeValueValidator uncertaintyValueValidator;
	private final ObserverCodeValidator observerCodeValidator;

	/**
	 * Constructor.
	 */
	public SimpleTextFormatValidator() {
		this.julianDayValidator = new JulianDayValidator();
		this.magnitudeFieldValidator = new MagnitudeFieldValidator();
		this.uncertaintyValueValidator = new MagnitudeValueValidator(
				new ExclusiveRangePredicate(0, 1));
		this.observerCodeValidator = new ObserverCodeValidator();
	}

	/**
	 * <p>
	 * Validate an observation line and either return an ValidObservation
	 * instance, or throw an exception indicating the error.
	 * </p>
	 * 
	 * <p>
	 * Both uncertainty and observer code fields are optional. The uncertainty
	 * field *may* be present however, whether or not the magnitude field has a
	 * ":" suffix.
	 * 
	 * TODO: REVISE THIS: 
	 * If ":" is exists in the magnitude field, the uncertainty
	 * field is required. Otherwise, if present, it must be zero. In either
	 * case, if the uncertainty field is present, the observer code will be the
	 * 4th field, otherwise it must be the 3rd. If any of this does not hold
	 * true, we throw an exception. It's at this point that the attraction of
	 * CSV format becomes clear. :)
	 * </p>
	 * 
	 * @param line
	 *            The line of text to be tokenised and validated.
	 * @return The validated ValidObservation object.
	 * @throws ObservationError
	 */
	public ValidObservation validate(String line)
			throws ObservationValidationError {

		// Split fields on spaces and tabs.
		String[] fields = line.split("\\s+");

		if (fields.length < 2 || fields.length > 4) {
			throw new ObservationValidationError(
					"The observation contains an invalid number of fields.");
		}

		DateInfo dateInfo = julianDayValidator.validate(fields[JD_FIELD]);

		Magnitude magnitude = magnitudeFieldValidator
				.validate(fields[MAG_FIELD]);

		double uncertaintyMag = 0;
		String obsCode = "";

		if (fields.length == 4) {
			uncertaintyMag = this.uncertaintyValueValidator.validate(fields[2]);
			obsCode = this.observerCodeValidator.validate(fields[3]);
		} else if (fields.length == 3) {
			try {
				// Start by assuming uncertainty value in 3rd field.
				uncertaintyMag = this.uncertaintyValueValidator
						.validate(fields[2]);
			} catch (ObservationValidationError e) {
				// Not a valid uncertainty value. Assume observer code.
				obsCode = this.observerCodeValidator.validate(fields[2]);
			}
		}

		magnitude.setUncertainty(uncertaintyMag);

		assert (magnitude.getUncertainty() != Magnitude.ILLEGAL_UNCERTAINTY);

		return new ValidObservation(dateInfo, magnitude, obsCode);
	}
}
