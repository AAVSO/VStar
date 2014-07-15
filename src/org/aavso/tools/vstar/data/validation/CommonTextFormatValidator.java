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

import java.io.IOException;
import java.util.Map;

import org.aavso.tools.vstar.data.DateInfo;
import org.aavso.tools.vstar.data.Magnitude;
import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.exception.ObservationValidationError;
import org.aavso.tools.vstar.exception.ObservationValidationWarning;
import org.aavso.tools.vstar.input.text.ObservationFieldSplitter;

import com.csvreader.CsvReader;

/**
 * This class accepts a line of text for tokenising, validation, and
 * ValidObservation instance creation that is common to all text format sources.
 * Currently, simple and AAVSO download file formats have an intersecting set of
 * mandatory and optional fields. The field indices differ across formats, as
 * does what counts as a legal valflag field value, but the fieldIndexMap and
 * valflagPatternStr constructor arguments cater for the differences.
 */
public class CommonTextFormatValidator {

	protected final ObservationFieldSplitter fieldSplitter;

	protected CsvReader lineReader;

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
	 * @param lineReader
	 *            The CsvReader that will be used to return fields, created with
	 *            the appropriate delimiter and data source.
	 * @param minFields
	 *            The minimum number of fields permitted in an observation line.
	 * @param maxFields
	 *            The maximum number of fields permitted in an observation line.
	 * @param valflagPatternStr
	 *            A regex pattern representing the alternations of permitted
	 *            valflags for this validator instance, e.g. "D" (simple format)
	 *            or "G|D|T|P|V|Z" (AAVSO download format).
	 * @param fieldInfoSource
	 *            A mapping from field name to field index that makes sense for
	 *            the source.
	 */
	public CommonTextFormatValidator(String desc, CsvReader lineReader,
			int minFields, int maxFields, String valflagPatternStr,
			IFieldInfoSource fieldInfoSource) throws IOException {

		this.lineReader = lineReader;

		this.fieldSplitter = new ObservationFieldSplitter(lineReader,
				minFields, maxFields);

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
	 * Attempt to read the next record, returning whether it was.
	 * 
	 * @return Whether the next record was read.
	 * @throws IOException
	 *             If a read error occurred.
	 */
	public boolean next() throws IOException {
		return lineReader.readRecord();
	}

	/**
	 * Return the current raw record.
	 * 
	 * @return The current raw record.
	 */
	public String getRawRecord() {
		return lineReader.getRawRecord();
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
	 * @return The validated ValidObservation object or null if one could not be
	 *         created from the current record's fields.
	 */
	public ValidObservation validate() throws IOException,
			ObservationValidationError, ObservationValidationWarning {

		ValidObservation observation = null;

		// Get an array of fields split on the expected delimiter.
		fields = fieldSplitter.getFields();

		if (fields.length != 0) {
			// Create a new valid observation, making the assumption
			// that validation will pass.
			observation = new ValidObservation();

			// Validate the fields.
			DateInfo dateInfo = julianDayValidator
					.validate(fields[fieldIndexMap.get("JD_FIELD")]);
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

			observation
					.setObsCode(observerCodeValidator
							.validate(fields[fieldIndexMap
									.get("OBSERVER_CODE_FIELD")]));

			String valflag = fields[fieldIndexMap.get("VALFLAG_FIELD")];
			observation.setValidationType(valflagValidator.validate(valflag));
		}

		return observation;
	}
}
