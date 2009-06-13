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

import org.aavso.tools.vstar.data.DateInfo;
import org.aavso.tools.vstar.exception.ObservationValidationError;

/**
 * This class validates provided text as a Julian Day.
 * 
 * TODO: 
 * - Store Calendar object.
 * - Configure "business rule" values such as minimum date via
 *   constructor-based Inversion of Control (Spring, PicoContainer, ...)? 
 * - Code reuse from Zapper etc subject to AGPL restrictions? 
 * - Existing JD code -ve year bug caused by use of rounding int cast? UT!
 */
public class JulianDayValidator implements IStringValidator<DateInfo> {
	
	private final RegexValidator regexValidator; 
	
	/**
	 * Constructor.
	 */
	public JulianDayValidator() {
		this.regexValidator = new RegexValidator("^(\\d+(\\.\\d+)?)$", "Julian Day");
	}

	/**
	 * Validate the specified string as a Julian Day.
	 * 
	 * @param str
	 *            The string to be validated.
	 * @return The returned DateInfo object if validation is successful.
	 * @throws ObservationValidationError
	 *             if validation is unsuccessful.
	 */
	public DateInfo validate(String str) throws ObservationValidationError {
		String[] fields = this.regexValidator.validate(str);
		
		// By virtue of the regex pattern above, 
		// this must parse as a double.
		double value = Double.parseDouble(fields[0]);
			
		return new DateInfo(value);
	}
}
