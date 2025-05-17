/**
 * VStar: a statistical analysis tool for variable star data.
 * Copyright (C) 2010  AAVSO (http://www.aavso.org/)
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

import org.aavso.tools.vstar.exception.ObservationValidationError;
import org.aavso.tools.vstar.exception.ObservationValidationWarning;

/**
 * This is a validator for CMag and KMag field values.
 */
public class CKMagValidator extends AbstractStringValidator<String> {

	final private static int MAX = 8;

	public final static String CMAG_KIND = "CMag";
	public final static String KMAG_KIND = "KMag";
	
	/**
	 * Constructor.
	 * 
	 * @param kind
	 *            The kind of entity (e.g. field name) we are validating.
	 */
	public CKMagValidator(String kind) {
		super(kind);
		assert CMAG_KIND.equals(kind) || KMAG_KIND.equals(kind); 
	}

	@Override
	public String validate(String str) throws ObservationValidationError {
		if (this.isLegallyEmpty(str))
			return "";

		if (str.length() <= MAX) {
			return str;
		} else {
			throw new ObservationValidationError("The " + kind + " '" + str
					+ "' is more than " + MAX + " characters in length.");
		}
	}

	@Override
	protected boolean canBeEmpty() {
		return true;
	}
}
