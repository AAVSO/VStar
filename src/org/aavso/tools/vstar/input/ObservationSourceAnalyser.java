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

import java.io.IOException;
import java.io.LineNumberReader;

import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.data.validation.SimpleTextFormatValidator;
import org.aavso.tools.vstar.data.validation.StringValidatorBase;
import org.aavso.tools.vstar.exception.ObservationReadError;
import org.aavso.tools.vstar.ui.model.NewStarType;

/**
 * This class analyses an observation file (simple or download formats) and
 * makes information about the file available for use by consumers.
 */
public class ObservationSourceAnalyser {

	public static final String TAB_DELIM = "\t";
	public static final String COMMA_DELIM = ",";

	private LineNumberReader obsSource;
	private String obsSourceIdentifier;
	private int lineCount;
	private NewStarType newStarType;
	private String delimiter;

	/**
	 * Constructor.
	 * 
	 * @param obsSource
	 *            The observation source to be analysed.
	 */
	public ObservationSourceAnalyser(LineNumberReader obsSource,
			String obsSourceIdentifier) {
		this.obsSource = obsSource;
		this.obsSourceIdentifier = obsSourceIdentifier;
		this.lineCount = 0;
	}

	/**
	 * Analyse the source.
	 */
	public void analyse() throws IOException, ObservationReadError {

		boolean gleanedFormat = false;

		String line = obsSource.readLine();
		while (line != null) {
			// Using one line of data, glean format information.
			// Other than doing this once, just read all lines
			// so we can get a line count.
			if (!gleanedFormat) {
				// Ignore comment or blank line.
				if (!line.startsWith("#") && !line.matches("^\\s*$")) {
					// Try different delimiter types to guess CSV or TSV.
					gleanedFormat = determinedFormat(line, TAB_DELIM);
					if (!gleanedFormat) {
						gleanedFormat = determinedFormat(line, COMMA_DELIM);
						if (!gleanedFormat) {
							throw new ObservationReadError("'"
									+ obsSourceIdentifier
									+ "' is in an unknown format.");
						}
					}
				}
			}

			line = obsSource.readLine();
		}

		this.lineCount = obsSource.getLineNumber();
		obsSource.close();
	}

	/**
	 * Try to determine the format of the file from a single line: TSV vs CSV
	 * and simple vs download format.
	 * 
	 * @param line
	 *            The line to be analysed.
	 * @param delimiter
	 *            Tab or comma.
	 * @return Whether or not the format was determined.
	 */
	private boolean determinedFormat(String line, String delimiter) {
		boolean determined = false;

		String[] fields = line.split(delimiter);
		if (fields.length >= 2 && fields.length <= 5) {
			this.delimiter = delimiter;
			this.newStarType = NewStarType.NEW_STAR_FROM_SIMPLE_FILE; // TODO:
			// FILE
			// ->
			// FORMAT
			// ?
			determined = true;
		} else if (fields.length > 5) {
			this.delimiter = delimiter;
			this.newStarType = NewStarType.NEW_STAR_FROM_DOWNLOAD_FILE; // TODO:
			// FILE
			// ->
			// FORMAT
			// ?
			determined = true;
		}

		return determined;
	}

	/**
	 * @return the lineCount
	 */
	public int getLineCount() {
		return lineCount;
	}

	/**
	 * @return the newStarType
	 */
	public NewStarType getNewStarType() {
		return newStarType;
	}

	/**
	 * @return the delimiter
	 */
	public String getDelimiter() {
		return delimiter;
	}

	/**
	 * Return an instance of the text format validator class to be used for
	 * creating observation objects from a sequence of lines containing comma or
	 * tab delimited fields (CSV, TSV).
	 * 
	 * @return The validator object corresponding to this "new star" type.
	 */
	public StringValidatorBase<ValidObservation> getTextFormatValidator() {

		assert (ObservationSourceAnalyser.TAB_DELIM.equals(delimiter) || ObservationSourceAnalyser.COMMA_DELIM
				.equals(delimiter));

		StringValidatorBase<ValidObservation> validator = null;

		if (NewStarType.NEW_STAR_FROM_SIMPLE_FILE.equals(newStarType)) {
			validator = new SimpleTextFormatValidator(delimiter, newStarType
					.getMinFields(), newStarType.getMaxFields(), newStarType
					.getFieldInfoSource());
		} else if (NewStarType.NEW_STAR_FROM_DOWNLOAD_FILE.equals(newStarType)) {
			// TODO
		}

		assert (validator != null);

		return validator;
	}
}
