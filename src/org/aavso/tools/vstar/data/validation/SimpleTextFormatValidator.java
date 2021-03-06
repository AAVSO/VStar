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

import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.exception.ObservationValidationError;
import org.aavso.tools.vstar.exception.ObservationValidationWarning;

import com.csvreader.CsvReader;

/**
 * This class accepts a line of text for tokenising, validation, and
 * ValidObservation instance creation given a simple text format source:
 * 
 * JD MAG [UNCERTAINTY] [OBSCODE] [VALFLAG]
 * 
 * REQ_VSTAR_SIMPLE_TEXT_FILE_READ
 */
public class SimpleTextFormatValidator extends CommonTextFormatValidator {

	/**
	 * Constructor.
	 * 
	 * @param lineReader
	 *            The CsvReader that will be used to return fields, created with
	 *            the appropriate delimiter and data source.
	 * @param minFields
	 *            The minimum number of fields permitted in an observation line.
	 * @param maxFields
	 *            The maximum number of fields permitted in an observation line.
	 * @param fieldInfoSource
	 *            A mapping from field name to field index that makes sense for
	 *            the source.
	 */
	public SimpleTextFormatValidator(CsvReader lineReader, int minFields,
			int maxFields, IFieldInfoSource fieldInfoSource) throws IOException {
		super("simple text format observation line", lineReader, minFields,
				maxFields, COMMON_VALFLAG_PATTERN, fieldInfoSource);
	}

	/**
	 * Validate an observation line and either return a ValidObservation
	 * instance, or throw an exception indicating the error.
	 * 
	 * @param line
	 *            The line of text to be tokenised and validated.
	 * @return The validated ValidObservation object.
	 * @throws ObservationValidationError
	 */
	public ValidObservation validate(String line)
			throws IOException, ObservationValidationError, ObservationValidationWarning {

		ValidObservation observation = super.validate();
		
		// TODO: assert which fields should not be null

		return observation;
	}
}
