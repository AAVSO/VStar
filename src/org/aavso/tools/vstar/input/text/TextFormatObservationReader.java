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

import java.io.IOException;
import java.io.LineNumberReader;

import org.aavso.tools.vstar.data.InvalidObservation;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.data.validation.StringValidatorBase;
import org.aavso.tools.vstar.exception.ObservationReadError;
import org.aavso.tools.vstar.exception.ObservationValidationError;
import org.aavso.tools.vstar.input.ObservationRetrieverBase;
import org.aavso.tools.vstar.ui.model.ModelManager;
import org.aavso.tools.vstar.ui.model.ProgressInfo;

/**
 * This class reads a variable star data file format containing lines of
 * text or comma separated fields, and yields a collection of observations 
 * for one star.
 * 
 * REQ_VSTAR_SIMPLE_TEXT_FILE_READ REQ_VSTAR_AAVSO_DATA_DOWNLOAD_FILE_READ
 */
public class TextFormatObservationReader extends ObservationRetrieverBase {

	private ModelManager modelMgr = ModelManager.getInstance();

	private LineNumberReader reader;

	private ObservationSourceAnalyser analyser;

	/**
	 * Constructor
	 * 
	 * @param reader
	 *            A line number buffered reader from which lines of observations
	 *            can be read.
	 * @param analyser
	 *            An observation file analyser.
	 */
	public TextFormatObservationReader(LineNumberReader reader,
			ObservationSourceAnalyser analyser) {
		this.reader = reader;
		this.analyser = analyser;
	}

	/**
	 * @see org.aavso.tools.vstar.input.ObservationRetrieverBase#retrieveObservations()
	 */
	public void retrieveObservations() throws ObservationReadError {

		StringValidatorBase<ValidObservation> validator = this.analyser
				.getTextFormatValidator();

		try {
			String line = reader.readLine();

			while (line != null) {
				// Ignore comment or blank line.
				if (!line.startsWith("#") && !line.matches("^\\s*$")) {
					int lineNum = reader.getLineNumber();
					try {
						ValidObservation validOb = validator.validate(line);
						validOb.setLineNumber(lineNum);
						validObservations.add(validOb);
						categoriseValidObservation(validOb);
					} catch (ObservationValidationError e) {
						InvalidObservation invalidOb = new InvalidObservation(
								line, e.getMessage());
						invalidOb.setLineNumber(lineNum);
						invalidObservations.add(invalidOb);
					}
				}

				modelMgr.getProgressNotifier().notifyListeners(
						ProgressInfo.INCREMENT_PROGRESS);

				line = reader.readLine();
			}
		} catch (IOException e) {
			throw new ObservationReadError(
					"Error when attempting to read observation source.");
		}
	}
}
