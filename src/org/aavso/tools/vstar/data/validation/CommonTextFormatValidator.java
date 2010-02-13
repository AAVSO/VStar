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

import java.util.Map;

import org.aavso.tools.vstar.data.DateInfo;
import org.aavso.tools.vstar.data.Magnitude;
import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.data.ValidationType;
import org.aavso.tools.vstar.exception.ObservationValidationError;
import org.aavso.tools.vstar.input.text.ObservationFieldSplitter;

/**
 * This class accepts a line of text for tokenising, validation, and
 * ValidObservation instance creation that is common to all text format sources.
 * Currently, simple and AAVSO download file formats have an intersecting set of
 * mandatory and optional fields. The field indices differ across formats, as
 * does what counts as a legal valflag field value, but the fieldIndexMap and
 * valflagPatternStr constructor arguments cater for the differences.
 */
public class CommonTextFormatValidator extends
		AbstractStringValidator<ValidObservation> {

	private final ObservationFieldSplitter fieldSplitter;

	protected final JulianDayValidator julianDayValidator;
	protected final MagnitudeFieldValidator magnitudeFieldValidator;
	protected final UncertaintyValueValidator uncertaintyValueValidator;
	protected final ObserverCodeValidator observerCodeValidator;
	protected final ValflagValidator valflagValidator;

	protected final Map<String, Integer> fieldIndexMap;

	protected String[] fields;

	/**
	 * Constructor.
	 * 
	 * @param desc
	 *            A description of the kind of line we are validating.
	 * @param delimiter
	 *            The field delimiter to use.
	 * @param minFields
	 *            The minimum number of fields permitted in an observation line.
	 * @param maxFields
	 *            The maximum number of fields permitted in an observation line.
	 * @param valflagPatternStr
	 *            A regex pattern representing the alternations of permission
	 *            valflags for this validator instance, e.g. "D" (simple format)
	 *            or "G|D|P" (AAVSO download format). A mapping from field name
	 *            to index.
	 * @param fieldInfoSource
	 *            A mapping from field name to field index that makes sense for
	 *            the source.
	 */
	public CommonTextFormatValidator(String desc, String delimiter,
			int minFields, int maxFields, String valflagPatternStr,
			IFieldInfoSource fieldInfoSource) {

		this.fieldSplitter = new ObservationFieldSplitter(delimiter, minFields,
				maxFields);

		this.fieldIndexMap = fieldInfoSource.getFieldIndexMap();

		this.julianDayValidator = new JulianDayValidator();
		this.magnitudeFieldValidator = new MagnitudeFieldValidator();
		this.uncertaintyValueValidator = new UncertaintyValueValidator(
				new InclusiveRangePredicate(0, 1));
		this.observerCodeValidator = new ObserverCodeValidator();
		this.valflagValidator = new ValflagValidator(valflagPatternStr);

		this.fields = null;
	}

	/**
	 * Validate an observation line and either return a ValidObservation
	 * instance, or throw an exception indicating the error.
	 * 
	 * Uncertainty, observer code, and valflag fields are optional. The
	 * uncertainty field may be present however, whether or not the magnitude
	 * field has a ":" (meaning 'uncertain') suffix.
	 * 
	 * @param line
	 *            The line of text to be tokenised and validated.
	 * @return The validated ValidObservation object.
	 * @throws ObservationValidationError
	 */
	public ValidObservation validate(String line)
			throws ObservationValidationError {

		ValidObservation observation = null;

		// Create a new valid observation, making the assumption
		// that validation will pass.
		observation = new ValidObservation();

		// Get an array of fields split on the expected delimiter.
		fields = fieldSplitter.getFields(line);

		// Validate the fields.
		DateInfo dateInfo = julianDayValidator.validate(fields[fieldIndexMap
				.get("JD_FIELD")]);
		observation.setDateInfo(dateInfo);

		Magnitude magnitude = magnitudeFieldValidator
				.validate(fields[fieldIndexMap.get("MAGNITUDE_FIELD")]);

		Double uncertaintyMag = uncertaintyValueValidator
				.validate(fields[fieldIndexMap.get("UNCERTAINTY_FIELD")]);

		if (uncertaintyMag != null) {
			magnitude.setUncertainty(uncertaintyMag);
		}

		if (magnitude.isBrighterThan()) {
			throw new ObservationValidationError(
					"Was '>' intended (brighter than) or '<'?");
		}

		observation.setMagnitude(magnitude);

		if (observation.getBand() == null) {
			observation.setBand(SeriesType.Unspecified);
		}

		observation.setObsCode(observerCodeValidator
				.validate(fields[fieldIndexMap.get("OBSERVER_CODE_FIELD")]));

		// Here we convert 'P' to "pre-validated" since 'P' for database
		// source observations means "Published" (which is treated as "Good").
		// If we ever want to use this class for database observation field
		// validation, we should probably just change the SQL to change 'P'
		// to 'G' (published to good), allowing ValidationType to treat 'Z'
		// and 'P' as pre-validated. Then we can get rid of the first case 
		// below.
		String valflag = fields[fieldIndexMap.get("VALFLAG_FIELD")];
		if ("P".equals(valflag)) {
			observation.setValidationType(ValidationType.PREVALIDATION);
		} else {
			// G, D
			observation.setValidationType(valflagValidator.validate(valflag));
		}

		return observation;
	}
}
