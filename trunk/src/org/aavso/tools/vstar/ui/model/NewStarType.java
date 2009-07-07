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
package org.aavso.tools.vstar.ui.model;

import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.data.validation.SimpleTextFormatValidator;
import org.aavso.tools.vstar.data.validation.StringValidatorBase;
import org.aavso.tools.vstar.input.ObservationFileAnalyser;

/**
 * A new star creation type. It also encodes the required number of fields for
 * each observation in the source, and acts as a Factory Method (GoF pattern)
 * for determining text format validator (simple or AAVSO download format).
 */
public enum NewStarType {

	// TODO: also create a NewStarInfo message class with GUI component refs for
	// DataPane

	NEW_STAR_FROM_SIMPLE_FILE(5, 5), NEW_STAR_FROM_DOWNLOAD_FILE(18, 18), NEW_STAR_FROM_DATABASE(
			18, 18);

	private final int minFields;
	private final int maxFields;

	/**
	 * Constructor.
	 * 
	 * @param minFields
	 *            The minimum allowed number of fields.
	 * @param maxFields
	 *            The maximum allowed number of fields.
	 */
	private NewStarType(int minFields, int maxFields) {
		this.minFields = minFields;
		this.maxFields = maxFields;
	}

	/**
	 * @return the minFields
	 */
	public int getMinFields() {
		return minFields;
	}

	/**
	 * @return the maxFields
	 */
	public int getMaxFields() {
		return maxFields;
	}

	/**
	 * Return an instance of the text format validator class to be used for
	 * creating observation objects from a sequence of lines containing comma or
	 * tab delimited fields (CSV, TSV).
	 * 
	 * TODO: keep a Singleton Registry of validator-delimiter combinations and
	 * reuse
	 * 
	 * @param delimiter
	 *            A tab or comma.
	 * @return The validator object corresponding to this "new star" type.
	 */
	public StringValidatorBase<ValidObservation> getTextFormatValidator(
			ObservationFileAnalyser analyser) {

		String delimiter = analyser.getDelimiter();
		assert (ObservationFileAnalyser.TAB_DELIM.equals(delimiter) || ObservationFileAnalyser.COMMA_DELIM
				.equals(delimiter));

		StringValidatorBase<ValidObservation> validator = null;

		if (NEW_STAR_FROM_SIMPLE_FILE.equals(this)) {
			validator = new SimpleTextFormatValidator(analyser.getDelimiter(),
					analyser.getType().getMinFields(), analyser.getType()
							.getMaxFields());
		} else if (NEW_STAR_FROM_DOWNLOAD_FILE.equals(this)) {

		}

		assert (validator != null);

		return validator;
	}
}
