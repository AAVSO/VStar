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
package org.aavso.tools.vstar.vela;

/**
 * VeLa: VStar expression Language interpreter
 * 
 * This class propagates parse error information.
 */
@SuppressWarnings("serial")
public class VeLaParseError extends RuntimeException {

	private String message;
	private int lineNum, charPos;

	/**
	 * Construct a VeLa parse exception.
	 * 
	 * @param message
	 *            The error message.
	 * @param lineNum
	 *            The line number on which the error occurred.
	 * @param charPos
	 *            The character position within the line where the error
	 *            occurred.
	 */
	public VeLaParseError(String message, int lineNum, int charPos) {
		super();
		this.message = message;
		this.lineNum = lineNum;
		this.charPos = charPos;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @return the lineNum
	 */
	public int getLineNum() {
		return lineNum;
	}

	/**
	 * @return the charPos
	 */
	public int getCharPos() {
		return charPos;
	}
}
