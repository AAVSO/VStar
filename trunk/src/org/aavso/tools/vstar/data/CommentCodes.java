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
package org.aavso.tools.vstar.data;

import org.aavso.tools.vstar.data.CommentType;
import java.util.Collection;
import java.util.HashSet;

/**
 * This class stores the possible array of comment codes
 */
public class CommentCodes {

	private Collection<CommentType> commentcodes;
	private String origString;

	/**
	 * Constructor.
	 * 
	 * @param commentcodes
	 *            comment code string from the data
	 */
	public CommentCodes(String cc) {
		// parse the string and bust it up and fill the array with the values
		// returned from the enum
		commentcodes = new HashSet<CommentType>();

		if (cc != null) {
			origString = cc;
			for (char c : cc.toCharArray()) {
				if (c != ' ') {
					CommentType commentCode = CommentType
							.getTypeFromFlag(String.valueOf(c));

					if (commentCode != CommentType.OTHER) {
						commentcodes.add(commentCode);
					} else {
						// If we encounter an "other" comment,
						// stop processing and just use "other".
						// See tracker 2909469.
						commentcodes.clear();
						commentcodes.add(CommentType.OTHER);
						break;
					}
				}
			}
		} else {
			origString = new String();
		}

	}

	/**
	 * @return the commentcodes
	 */
	public Collection<CommentType> getCommentcodes() {
		return commentcodes;
	}

	public String toString() {
		StringBuffer strBuf = new StringBuffer();

		for (CommentType t : commentcodes) {
			strBuf.append(t); // or t.toString() if necessary
		}

		return strBuf.toString();
	}

	public String getOrigString() {
		return origString;
	}
}
