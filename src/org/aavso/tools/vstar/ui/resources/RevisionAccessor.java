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
package org.aavso.tools.vstar.ui.resources;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The purpose of this class is to provide access to subversion revision number.
 */

public class RevisionAccessor {

	private static String REVISION = "877:940MP";

	private static final Pattern revNumPat = Pattern
			.compile("^\\d+:(\\d+).*$");

	/**
	 * Get the latest revision number if REVISION is of the form:
	 * n:m... (i.e. get m), otherwise just return the whole revision
	 * string. It doesn't really matter what it is so long as it's
	 * unique from one commit of dist/vstar.jar to the next.
	 */
	public static String getRevNum() {
		String rev = REVISION;

		Matcher revMatcher = revNumPat.matcher(rev);
		if (revMatcher.matches()) {
			rev = revMatcher.group(1);
		}
		
		return rev;
	}
}
