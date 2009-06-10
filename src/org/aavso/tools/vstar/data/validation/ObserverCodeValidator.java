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

import org.aavso.tools.vstar.exception.ObservationValidationError;

/**
 * This class validates an observer code.
 */
public class ObserverCodeValidator implements IStringValidator<String> {

	// TODO: get rid of this!
	private final static String[] forbiddenCodes = new String[]{"TST01"};
	
	private final RegexValidator regexValidator;

	public ObserverCodeValidator() {
		this.regexValidator = new RegexValidator("^(([A-Za-z]|[0-9]){1,5})$",
				"Observer Code");
	}

	public String validate(String str) throws ObservationValidationError {
		String[] fields = this.regexValidator.validate(str);
		
		for (String code : forbiddenCodes) {
			if (code.equals(fields[0])) {
				throw new ObservationValidationError("The observer code '" + code + "' may not be used.");
			}
		}
		
		return fields[0];
	}

}
