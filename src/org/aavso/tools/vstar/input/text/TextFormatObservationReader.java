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
package org.aavso.tools.vstar.input.text;

import java.io.LineNumberReader;

import org.aavso.tools.vstar.data.InvalidObservation;
import org.aavso.tools.vstar.data.MTypeType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.data.validation.CommonTextFormatValidator;
import org.aavso.tools.vstar.exception.ObservationReadError;
import org.aavso.tools.vstar.exception.ObservationValidationError;
import org.aavso.tools.vstar.exception.ObservationValidationWarning;
import org.aavso.tools.vstar.input.AbstractObservationRetriever;
import org.aavso.tools.vstar.ui.mediator.StarInfo;

/**
 * This class reads a variable star data file format containing lines of text or
 * comma separated fields, and yields a collection of observations for one star.
 * 
 * REQ_VSTAR_SIMPLE_TEXT_FILE_READ REQ_VSTAR_AAVSO_DATA_DOWNLOAD_FILE_READ
 */
public class TextFormatObservationReader extends AbstractObservationRetriever {

	private LineNumberReader reader;

	private ObservationSourceAnalyser analyser;

	/**
	 * Constructor.
	 * 
	 * We pass the number of lines to be read to the base class to ensure a
	 * large enough observation list capacity in the case where we an read
	 * out-of-order dataset.
	 * 
	 * @param reader
	 *            The reader that is the source of the observation.
	 * @param analyser
	 *            An observation file analyser.
	 */
	public TextFormatObservationReader(LineNumberReader reader,
			ObservationSourceAnalyser analyser) {
		super(analyser.getLineCount());
		this.reader = reader;
		this.analyser = analyser;
	}

	/**
	 * @see org.aavso.tools.vstar.input.AbstractObservationRetriever#retrieveObservations()
	 */
	public void retrieveObservations() throws ObservationReadError {

		try {
			CommonTextFormatValidator validator = this.analyser
					.getTextFormatValidator(reader);

			int lineNum = 0;

			while (validator.next() && !wasInterrupted()) {
				// Ignore comment, blank line or column header line
				// (e.g. JD,Magnitude,...).
				String line = validator.getRawRecord();
				lineNum++;

				if (!line.startsWith("#") && !line.matches("^\\s*$")
						&& !isColumnHeaderLine(line)) {

					try {
						ValidObservation validOb = validator.validate();
						if (validOb != null) {
							addValidObservation(validOb, lineNum);
						}
					} catch (ObservationValidationError e) {
						InvalidObservation invalidOb = new InvalidObservation(
								validator.getRawRecord(), e.getMessage());
						invalidOb.setRecordNumber(lineNum);
						addInvalidObservation(invalidOb);
					} catch (ObservationValidationWarning e) {
						InvalidObservation invalidOb = new InvalidObservation(
								validator.getRawRecord(), e.getMessage(), true);
						invalidOb.setRecordNumber(lineNum);
						addInvalidObservation(invalidOb);

						addValidObservation(e.getObservation(), lineNum);
					}
				}

				incrementProgress();
			}
		} catch (Throwable t) {
			throw new ObservationReadError(
					"Error when attempting to read observation source.");
		} finally {
			// TODO: once analyser moved into here
//			lines.clear();
		}
	}

	@Override
	public Integer getNumberOfRecords() throws ObservationReadError {
		return analyser.getLineCount();
	}

	@Override
	public String getSourceType() {
		return analyser.getNewStarType().toString();
	}

	@Override
	public String getSourceName() {
		return analyser.getObsSourceIdentifier();
	}

	@Override
	public StarInfo getStarInfo() {
		// Try to get the name of the object from one of the observations,
		// otherwise just use the source name (file name or URL).

		String name = getValidObservations().get(0).getName();

		if (name == null) {
			name = getSourceName();
		}

		return new StarInfo(this, name);
	}

	// Helpers

	private void addValidObservation(ValidObservation validOb, int lineNum) {
		if (validOb.getMType() == MTypeType.STD) {
			validOb.setRecordNumber(lineNum);
			addValidObservation(validOb);
			categoriseValidObservation(validOb);
		}
	}

	// Is the specified line a column header?
	private boolean isColumnHeaderLine(String line) {
		return validObservations.isEmpty() && invalidObservations.isEmpty()
				&& line.matches("^[A-Za-z].+$");
	}
}
