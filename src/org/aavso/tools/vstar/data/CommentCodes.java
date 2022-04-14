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

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((commentcodes == null) ? 0 : commentcodes.hashCode());
		result = prime * result
				+ ((origString == null) ? 0 : origString.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof CommentCodes)) {
			return false;
		}
		CommentCodes other = (CommentCodes) obj;
		if (commentcodes == null) {
			if (other.commentcodes != null) {
				return false;
			}
		} else if (!commentcodes.equals(other.commentcodes)) {
			return false;
		}
		if (origString == null) {
			if (other.origString != null) {
				return false;
			}
		} else if (!origString.equals(other.origString)) {
			return false;
		}
		return true;
	}
}
