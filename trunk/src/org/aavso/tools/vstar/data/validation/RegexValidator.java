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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.aavso.tools.vstar.exception.ObservationValidationError;

/**
 * This class validates strings using regular expressions.
 */
public class RegexValidator implements IStringValidator<String[]> {

	private final Pattern pattern;
	private final String kind;
	private final String errorNotes;
	
	/**
	 * Constructor.
	 * 
	 * @param patternStr The regular expression string to be used for validation.
	 * @param kind The kind of entity we are validating.
	 * @param errorNotes Additional error comment to be added to validation failure error message.
	 */
	public RegexValidator(String patternStr, String kind, String errorNotes) {
		this.pattern = Pattern.compile(patternStr);
		this.kind = kind;
		this.errorNotes = errorNotes;
	}
	
	/**
	 * Constructor.
	 * 
	 * @param patternStr The regular expression string to be used for validation.
	 * @param kind The kind of entity we are validating.
	 */
	public RegexValidator(String patternStr, String kind) {
		this(patternStr, kind, null);
	}
	
	/**
	 * @see org.aavso.tools.vstar.util.validation.IStringValidator#validate(java.lang.String)
	 */
	public String[] validate(String str) throws ObservationValidationError {
		List<String> groups = new ArrayList<String>();
		
		Matcher matcher = pattern.matcher(str);
		if (!matcher.matches()) {
			StringBuffer msgBuf = new StringBuffer();
			msgBuf.append("'");
			msgBuf.append(str);
			msgBuf.append("' is not a well-formed '");
			msgBuf.append(kind);
			msgBuf.append("'");
			if (this.errorNotes != null) {
				msgBuf.append(". ");
				msgBuf.append(this.errorNotes);
			}
			throw new ObservationValidationError(msgBuf.toString());
		} else {
			for (int i=1;i<=matcher.groupCount();i++) {
				groups.add(matcher.group(i));
			}
		}
		
		return groups.toArray(new String[0]);
	}
}
