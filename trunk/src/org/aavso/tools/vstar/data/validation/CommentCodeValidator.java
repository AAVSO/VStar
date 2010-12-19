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
 * A comment code field validator.
 */
public class CommentCodeValidator extends AbstractStringValidator<String> {

	private static final String KIND = "comment code";

	private final RegexValidator regexValidator;

	/**
	 * Constructor.
	 * 
	 * @param commentCodePatternStr
	 *            A regex pattern representing the alternations of permissible
	 *            comment codes. This pattern string will be wrapped in a
	 *            ^(...)$ to ensure that nothing else exists in the string, and
	 *            that there is one capturing group.
	 */
	public CommentCodeValidator(String commentCodePatternStr) {
		super(KIND);
		//FIXME: AW I'm putting in what is probably a god awful hack.  This is so
		//       the generic regex engine will return the entire group of comment codes
		//       in one swoop, otherwise it returns just the last character it finds
		this.regexValidator = new RegexValidator("^((" + commentCodePatternStr
				+ "|\\s)+)$", KIND);
	}

	public String validate(String str)
			throws ObservationValidationError {
		if (this.isLegallyEmpty(str)) return null;

		// TODO: note, we should indeed do what we say below
		// leaving much work for CommentCodes to do. We would
		// need to genericise this class on CommentType not String.
		// Indeed, we could probably return a CommentCodes instance
		// from here.
		
		// We could return an enum value of some kind,
		// but VStar doesn't currently need to make
		// decisions based upon comment code as it does
		// with say, valflag.

		return this.regexValidator.validate(str)[0];
	}

	protected boolean canBeEmpty() {
		return true;
	}
}
