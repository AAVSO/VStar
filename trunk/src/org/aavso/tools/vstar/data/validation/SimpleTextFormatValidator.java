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

import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.exception.ObservationValidationError;

/**
 * This class accepts a line of text for tokenising, validation, and
 * ValidObservation instance creation given a simple text format source.
 * 
 * JD MAG [UNCERTAINTY] [OBSCODE] [VALFLAG]
 */
public class SimpleTextFormatValidator extends CommonTextFormatValidator {

	/**
	 * Constructor.
	 * 
	 * @param delimiter
	 *            The field delimiter to use.
	 * @param minFields
	 *            The minimum number of fields permitted in an observation line.
	 * @param maxFields
	 *            The maximum number of fields permitted in an observation line.
	 * @param fieldInfoSource
	 *            A mapping from field name to field index that makes sense for
	 *            the source.
	 */
	public SimpleTextFormatValidator(String delimiter, int minFields,
			int maxFields, ITableFieldInfoSource fieldInfoSource) {
		super("simple text format observation line", delimiter, minFields,
				maxFields, "D", fieldInfoSource);
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
			throws ObservationValidationError {

		ValidObservation observation = super.validate(line);
		
		// TODO: assert which fields should not be null

		return observation;
	}
}
