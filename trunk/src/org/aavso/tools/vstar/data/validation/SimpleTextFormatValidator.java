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
import org.aavso.tools.vstar.data.SimpleFormatFieldInfoSource;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.exception.ObservationValidationError;
import org.aavso.tools.vstar.input.ObservationFieldSplitter;

/**
 * This class accepts a line of text for tokenising, validation, and
 * ValidObservation instance creation.
 */
public class SimpleTextFormatValidator extends
		StringValidatorBase<ValidObservation> {

	private final int JD_FIELD = SimpleFormatFieldInfoSource.JD_FIELD;
	private final int MAG_FIELD = SimpleFormatFieldInfoSource.MAGNITUDE_FIELD;
	private final int UNCERTAINTY_FIELD = SimpleFormatFieldInfoSource.UNCERTAINTY_FIELD;
	private final int OBSCODE_FIELD = SimpleFormatFieldInfoSource.OBSERVER_CODE_FIELD;
	private final int VALFLAG_FIELD = SimpleFormatFieldInfoSource.VALFLAG_FIELD;
	
	private final ObservationFieldSplitter fieldSplitter;

	private final JulianDayValidator julianDayValidator;
	private final MagnitudeFieldValidator magnitudeFieldValidator;
	private final UncertaintyValueValidator uncertaintyValueValidator;
	private final ObserverCodeValidator observerCodeValidator;
	private final ValflagValidator valflagValidator;

	/**
	 * Constructor.
	 * 
	 * @param delimiter
	 *            The field delimiter to use.
	 * @param minFields
	 *            The minimum number of fields permitted in an observation line.
	 * @param maxFields
	 *            The maximum number of fields permitted in an observation line.
	 */
	public SimpleTextFormatValidator(String delimiter, int minFields,
			int maxFields) {
		super("simple text format observation line");
		this.fieldSplitter = new ObservationFieldSplitter(delimiter, minFields,
				maxFields);

		this.julianDayValidator = new JulianDayValidator();
		this.magnitudeFieldValidator = new MagnitudeFieldValidator();
		this.uncertaintyValueValidator = new UncertaintyValueValidator(
				new ExclusiveRangePredicate(0, 1));
		this.observerCodeValidator = new ObserverCodeValidator();
		this.valflagValidator = new ValflagValidator("D");
	}

	/**
	 * Validate an observation line and either return an ValidObservation
	 * instance, or throw an exception indicating the error.
	 * 
	 * Both uncertainty and observer code fields are optional. The uncertainty
	 * field may be present however, whether or not the magnitude field has a
	 * ":" suffix.
	 * 
	 * @param line
	 *            The line of text to be tokenised and validated.
	 * @return The validated ValidObservation object.
	 * @throws ObservationValidationError
	 */
	public ValidObservation validate(String line)
			throws ObservationValidationError {

		// JD MAG [UNCERTAINTY] [OBSCODE] [VALFLAG]

		ValidObservation observation = new ValidObservation();

		String[] fields = fieldSplitter.getFields(line);

		DateInfo dateInfo = julianDayValidator.validate(fields[JD_FIELD]);
		observation.setDateInfo(dateInfo);

		Magnitude magnitude = magnitudeFieldValidator
				.validate(fields[MAG_FIELD]);

		Double uncertaintyMag = uncertaintyValueValidator
				.validate(fields[UNCERTAINTY_FIELD]);

		if (uncertaintyMag != null) {
			magnitude.setUncertainty(uncertaintyMag);
		}
		
		observation.setMagnitude(magnitude);
		
		observation.setObsCode(observerCodeValidator.validate(fields[OBSCODE_FIELD]));
		
		observation.setValidationType(valflagValidator.validate(fields[VALFLAG_FIELD]));
		
		// TODO: assert which fields should not be null
		
		return observation;
	}
}
