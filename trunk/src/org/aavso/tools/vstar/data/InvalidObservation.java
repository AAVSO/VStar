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


/**
 * This class corresponds to a single invalid variable star observation.
 */
public class InvalidObservation extends Observation {

	private String inputLine;
	private String error;

	/**
	 * Constructor
	 * 
	 * @param lineNum
	 *            The line number at which this observation was found in the
	 *            originating source file.
	 * @param inputLine
	 *            The original input line.
	 * @param error
	 *            The error message.
	 */
	public InvalidObservation(String inputLine, String error) {
		super(0);
		this.inputLine = inputLine;
		this.error = error;
	}

	public String getInputLine() {
		return inputLine;
	}

	public String getError() {
		return error;
	}

	public String toString() {
		StringBuffer strBuf = new StringBuffer();

		strBuf.append(inputLine);
		strBuf.append(": ");
		strBuf.append(error);

		return strBuf.toString();
	}
}
