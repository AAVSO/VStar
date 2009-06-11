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
package org.aavso.tools.vstar.input;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.aavso.tools.vstar.data.InvalidObservation;
import org.aavso.tools.vstar.data.Observation;
import org.aavso.tools.vstar.data.validation.SimpleTextFormatValidator;
import org.aavso.tools.vstar.exception.ObservationReadError;
import org.aavso.tools.vstar.exception.ObservationValidationError;

/**
 * This class reads a simple variable star data file format containing lines of
 * the form: JD MAG [UNCERTAINTY] [OBSCODE] and yields a collection of
 * observations for one star. [REQ_VSTAR_SIMPLE_TEXT_FILE_READ]
 */
public class SimpleTextFormatReader extends ObservationRetrieverBase {

	private String starName; // may not be required
	private BufferedReader reader;

	/**
	 * Constructor
	 * 
	 * @param starName
	 *            The name of the star to which the observations pertain.
	 * @param reader
	 *            A buffered reader from which lines of observations can be
	 *            read.
	 */
	public SimpleTextFormatReader(String starName, BufferedReader reader) {
		this.starName = starName;
		this.reader = reader;
	}

	/**
	 * Constructor
	 * 
	 * @param reader
	 *            A buffered reader from which lines of observations can be
	 *            read.
	 */
	public SimpleTextFormatReader(BufferedReader reader) {
		this("", reader);
	}

	/**
	 * @see org.aavso.tools.vstar.input.ObservationRetrieverBase#retrieveObservations()
	 */
	public void retrieveObservations() throws ObservationReadError {
		SimpleTextFormatValidator validator = new SimpleTextFormatValidator();
		try {
			String line = reader.readLine();
			while (line != null) {
				try {
					validObservations.add(validator.validate(line));
				} catch (ObservationValidationError e) {
					invalidObservations.add(new InvalidObservation(line, e.getMessage()));	
				}
				
				line = reader.readLine();
			}
		} catch (IOException e) {
			throw new ObservationReadError("Error when attempting to read observation source.");
		}
	}
}
