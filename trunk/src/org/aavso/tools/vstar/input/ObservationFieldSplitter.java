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


/**
 * This class splits an observation line given a delimiter. If the result of the
 * split is less than a specified number, then the appropriate number of null
 * fields is added to the result.
 */
public class ObservationFieldSplitter {

	private final String delimiter;
	private final int requiredFields;

	/**
	 * Constructor.
	 * 
	 * @param delimiter
	 *            The delimiter to use to split the fields.
	 * @param requiredFields
	 *            The required number of fields to be returned.
	 */
	public ObservationFieldSplitter(String delimiter, int requiredFields) {
		this.delimiter = delimiter;
		this.requiredFields = requiredFields;
	}

	/**
	 * Given a line, return the required number of fields, appending with nulls
	 * if too few fields are present in the line.
	 * 
	 * @param line
	 *            The line to be split.
	 * @return The fields in the line.
	 * @postcondition: The returned field array's length must correspond to the
	 *                 required number of fields.
	 */
	public String[] getFields(String line) {
		String[] fields = line.split(this.delimiter);

		if (fields.length < this.requiredFields) {
			int howManyMoreRequired = requiredFields - fields.length;
			int total = fields.length + howManyMoreRequired;
			String[] moreFields = new String[total];
			// Copy fields to new array. The additional fields
			// on the end will default to null.
			for (int i = 0; i < fields.length; i++) {
				moreFields[i] = fields[i];
			}
			fields = moreFields;
		}

		assert (fields.length == this.requiredFields);

		return fields;
	}
}
