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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.aavso.tools.vstar.exception.ObservationValidationError;

import com.csvreader.CsvReader;

/**
 * This class splits an observation line given a delimiter. If the result of the
 * split is less than a specified number, then the appropriate number of null
 * fields is added to the result.
 * @deprecated
 */
public class ObservationFieldSplitter {

	private final static Pattern quoted = Pattern.compile("^\"(.+)\"$");

	private final int minFields;
	private final int maxFields;
	private CsvReader lineReader;

	/**
	 * Constructor.
	 * 
	 * @param lineReader
	 *            The CsvReader that will be used to return fields, created with
	 *            the appropriate delimiter and data source.
	 * @param minFields
	 *            The minimum allowed number of fields to be returned.
	 * @param maxFields
	 *            The maximum allowed number of fields to be returned.
	 */
	public ObservationFieldSplitter(CsvReader lineReader, int minFields,
			int maxFields) {
		this.lineReader = lineReader;
		this.minFields = minFields;
		this.maxFields = maxFields;
	}

	/**
	 * Return the required number of fields for the next line, appending with
	 * nulls if too few fields are present in the line.
	 * 
	 * @return The fields in the next line.
	 * @throws IOException
	 *             if there is an error reading the line.
	 * @throws ObservationValidationError
	 *             If the number of fields does not fall into the required
	 *             range.
	 * @postcondition: The returned field array's length must be maxFields to
	 *                 simplify validation.
	 */
	public String[] getFields() throws IOException, ObservationValidationError {
		String[] fields = lineReader.getValues();

		if (fields.length < this.minFields/* || fields.length > this.maxFields*/) {
			StringBuffer strBuf = new StringBuffer();
			strBuf.append("The number of fields (");
			strBuf.append(fields.length);
			strBuf.append(") ");
			strBuf.append("is less than expected: ");
			strBuf.append(minFields);
//			strBuf.append("..");
//			strBuf.append(maxFields);

			throw new ObservationValidationError(strBuf.toString());
		}

		// Remove leading and trailing quotes from quoted fields.
		for (int i = 0; i < fields.length; i++) {
			Matcher matcher = quoted.matcher(fields[i]);
			if (matcher.matches()) {
				fields[i] = matcher.group(1);
			}
		}

		// TODO: or convert trailing (or all?) "" files to nulls?
		if (fields.length < this.maxFields) {
			int howManyMoreRequired = maxFields - fields.length;
			int total = fields.length + howManyMoreRequired;
			String[] moreFields = new String[total];
			// Copy fields to new array. The additional fields
			// on the end will default to null.
			for (int i = 0; i < fields.length; i++) {
				moreFields[i] = fields[i];
			}
			fields = moreFields;
		}

		return fields;
	}
}
